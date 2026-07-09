package com.multitenant.service;

import com.multitenant.dto.NotificareSuccesiuneDTO;
import com.multitenant.model.persoana.Persoana;
import com.multitenant.model.persoana.PersoanaFizica;
import com.multitenant.model.registru.NotificareSuccesiune;
import com.multitenant.repository.NotificareSuccesiuneRepository;
import com.multitenant.repository.PersoanaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificareSuccesiuneServiceImpl implements NotificareSuccesiuneService {

    private final NotificareSuccesiuneRepository notificareSuccesiuneRepository;
    private final PersoanaRepository persoanaRepository;
    private final NotificareSuccesiuneKafkaProducer notificareSuccesiuneKafkaProducer;

    @Override
    @Transactional
    public NotificareSuccesiuneDTO save(NotificareSuccesiuneDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Datele notificării nu pot fi nule.");
        }
        if (dto.getDefunctId() == null) {
            throw new IllegalArgumentException("ID-ul defunctului este obligatoriu.");
        }

        Persoana defunct = persoanaRepository.findById(dto.getDefunctId())
                .orElseThrow(() -> new RuntimeException("Defunctul nu a fost găsit cu ID-ul: " + dto.getDefunctId()));

        if (!(defunct instanceof PersoanaFizica)) {
            throw new IllegalArgumentException("Defunctul trebuie să fie o persoană fizică.");
        }

        PersoanaFizica defunctFizica = (PersoanaFizica) defunct;

        NotificareSuccesiune entity = new NotificareSuccesiune();
        entity.setDefunct(defunctFizica);
        entity.setNumeNotarSpnBin(dto.getNumeNotarSpnBin());
        entity.setNumarAdresaOficiala(dto.getNumarAdresaOficiala());
        entity.setDataTrimitere(dto.getDataTrimitere());
        
        // Stadiu implicit: TRIMIS dacă nu este furnizat altul
        String stadiu = (dto.getStadiuNotificare() != null) ? dto.getStadiuNotificare() : "TRIMIS";
        entity.setStadiuNotificare(stadiu);
        entity.setObservatii(dto.getObservatii());

        // Extragere automată securizată din contextul de securitate
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (auth != null) ? auth.getName() : "sistem";
        entity.setUtilizatorOperare(currentUsername);

        // Actualizăm starea decesului pe defunct în mod automat la salvarea notificării
        defunctFizica.setEsteDecedat(true);
        if (defunctFizica.getCnpHash() == null && defunctFizica.getCnp() != null) {
            defunctFizica.setCnpHash(PersoanaFizica.generateBlindIndex(defunctFizica.getCnp()));
        }
        if (dto.getDataDecesului() != null) {
            defunctFizica.setDataDecesului(dto.getDataDecesului());
        }
        if (dto.getNumarCertificatDeces() != null) {
            defunctFizica.setNumarCertificatDeces(dto.getNumarCertificatDeces());
        }
        persoanaRepository.save(defunctFizica);

        NotificareSuccesiune saved = notificareSuccesiuneRepository.save(entity);

        // Trimitere eveniment de deces/notificare succesiune prin Kafka
        try {
            notificareSuccesiuneKafkaProducer.trimiteEvenimentDeces(
                defunctFizica.getId(),
                defunctFizica.getLastName() + " " + defunctFizica.getFirstName(),
                dto.getNumeNotarSpnBin(),
                dto.getNumarAdresaOficiala()
            );
        } catch (Exception e) {
            System.err.println("[NotificareSuccesiuneServiceImpl] Eroare trimitere mesaj Kafka: " + e.getMessage());
        }

        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificareSuccesiuneDTO> getAll() {
        return notificareSuccesiuneRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificareSuccesiuneDTO> findByDefunctCnp(String cnpClar) {
        return notificareSuccesiuneRepository.findByDefunctCnpClar(cnpClar).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificareSuccesiuneDTO updateStadiu(Long id, String noulStadiu) {
        if (id == null) {
            throw new IllegalArgumentException("ID-ul notificării nu poate fi nul.");
        }
        if (noulStadiu == null || noulStadiu.trim().isEmpty()) {
            throw new IllegalArgumentException("Noul stadiu nu poate fi nul sau gol.");
        }

        NotificareSuccesiune existing = notificareSuccesiuneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificarea de succesiune cu ID-ul " + id + " nu a fost găsită."));

        existing.setStadiuNotificare(noulStadiu.trim());
        
        // Actualizăm și utilizatorul de operare
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (auth != null) ? auth.getName() : "sistem";
        existing.setUtilizatorOperare(currentUsername);

        return toDto(notificareSuccesiuneRepository.save(existing));
    }

    private NotificareSuccesiuneDTO toDto(NotificareSuccesiune entity) {
        NotificareSuccesiuneDTO dto = new NotificareSuccesiuneDTO();
        dto.setId(entity.getId());
        dto.setDefunctId(entity.getDefunct().getId());
        dto.setDefunctNume(entity.getDefunct().getLastName() + " " + entity.getDefunct().getFirstName());
        dto.setDefunctCnpHash(entity.getDefunctCnpHash());
        dto.setNumeNotarSpnBin(entity.getNumeNotarSpnBin());
        dto.setNumarAdresaOficiala(entity.getNumarAdresaOficiala());
        dto.setDataTrimitere(entity.getDataTrimitere());
        dto.setStadiuNotificare(entity.getStadiuNotificare());
        dto.setObservatii(entity.getObservatii());
        dto.setUtilizatorOperare(entity.getUtilizatorOperare());
        dto.setDataInregistrare(entity.getDataInregistrare());
        
        // Mapăm înapoi detaliile de deces din entitatea defunct
        dto.setDataDecesului(entity.getDefunct().getDataDecesului());
        dto.setNumarCertificatDeces(entity.getDefunct().getNumarCertificatDeces());
        return dto;
    }
}
