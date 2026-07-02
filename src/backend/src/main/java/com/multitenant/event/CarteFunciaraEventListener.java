package com.multitenant.event;

import com.multitenant.model.registru.CarteFunciara;
import com.multitenant.model.registru.Teren;
import com.multitenant.repository.CarteFunciaraRepository;
import com.multitenant.repository.TerenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener pentru evenimentele domeniului cadastral.
 *
 * Gestioneaza automat ciclul de viata al CarteFunciara:
 * 1. La crearea unui Teren nou → instantiaza o CarteFunciara goala asociata.
 * 2. La adaugarea unei Parcele noi → recalculeaza suprafata_totala_intabulata.
 *
 * DESIGN:
 * - Foloseste @TransactionalEventListener(phase = AFTER_COMMIT) pentru a garanta ca
 *   entitatile Teren/Parcela sunt deja vizibile in DB inainte de a actiona.
 * - Ruleaza intr-o NOUA TRANZACTIE separata (REQUIRES_NEW) pentru izolare corecta.
 * - In cazul unui esec al listenerului, tranzactia parinte (creare Teren/Parcela)
 *   ramane commitata — CF-ul poate fi creat/actualizat manual prin endpoint-ul PUT.
 */
@Component
public class CarteFunciaraEventListener {

    private static final Logger log = LoggerFactory.getLogger(CarteFunciaraEventListener.class);

    private final CarteFunciaraRepository carteFunciaraRepository;
    private final TerenRepository terenRepository;

    public CarteFunciaraEventListener(CarteFunciaraRepository carteFunciaraRepository,
                                      TerenRepository terenRepository) {
        this.carteFunciaraRepository = carteFunciaraRepository;
        this.terenRepository = terenRepository;
    }

    /**
     * Creeaza automat o CarteFunciara goala pentru fiecare Teren nou creat.
     *
     * Campurile numarCf si numarTopografic sunt intentionat null la creare —
     * vor fi completate de operator dupa sincronizarea cu ANCPI/Cadastru.
     *
     * @param event evenimentul publicat de TerenService dupa persistarea unui Teren
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onTerenCreated(TerenCreatedEvent event) {
        Long terenId = event.teren().getId();
        log.info("[CF] Teren creat cu id={}. Se instantiaza CarteFunciara goala.", terenId);

        // Reincarca entitatea in noua tranzactie (sesiune Hibernate noua)
        Teren teren = terenRepository.findById(terenId)
                .orElseThrow(() -> new IllegalStateException(
                        "[CF] Teren cu id=" + terenId + " nu a fost gasit la crearea CarteFunciara."));

        // Verifica idempotenta: nu crea un al doilea CF daca exista deja unul
        if (carteFunciaraRepository.findByTerenId(terenId).isPresent()) {
            log.warn("[CF] CarteFunciara exista deja pentru teren_id={}. Se omite crearea.", terenId);
            return;
        }

        CarteFunciara cf = new CarteFunciara();
        cf.setTeren(teren);
        // numarCf, numarTopografic, suprafataTotalaIntabulata raman null intentionat

        carteFunciaraRepository.save(cf);
        log.info("[CF] CarteFunciara creata cu succes pentru teren_id={}.", terenId);
    }

    /**
     * Recalculeaza suprafata totala intabulata din CarteFunciara la adaugarea unei Parcele noi.
     *
     * Calculeaza SUM(suprafata) direct in baza de date (fara a incarca toate parcelele in memorie)
     * si actualizeaza campul corespunzator din CarteFunciara.
     *
     * @param event evenimentul publicat de ParcelaService dupa persistarea unei Parcele
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onParcelaAdaugata(ParcelaAdaugataEvent event) {
        Long terenId = event.terenId();
        log.info("[CF] Parcela id={} adaugata pe teren_id={}. Se recalculeaza suprafata CF.", event.parcelaId(), terenId);

        CarteFunciara cf = carteFunciaraRepository.findByTerenId(terenId)
                .orElseGet(() -> {
                    // Situatie de fallback: CF-ul ar fi trebuit creat la crearea terenului,
                    // dar daca lipseste (ex: date vechi migrate), il cream acum.
                    log.warn("[CF] CarteFunciara lipsa pentru teren_id={}. Se creeaza acum.", terenId);
                    Teren teren = terenRepository.findById(terenId)
                            .orElseThrow(() -> new IllegalStateException(
                                    "[CF] Teren cu id=" + terenId + " nu a fost gasit la fallback CF."));
                    CarteFunciara newCf = new CarteFunciara();
                    newCf.setTeren(teren);
                    return carteFunciaraRepository.save(newCf);
                });

        Double suprafataNoua = carteFunciaraRepository.sumSuprafataByTerenId(terenId);
        cf.setSuprafataTotalaIntabulata(suprafataNoua);
        carteFunciaraRepository.save(cf);

        log.info("[CF] Suprafata totala intabulata actualizata: {} ha pentru teren_id={}.", suprafataNoua, terenId);
    }
}
