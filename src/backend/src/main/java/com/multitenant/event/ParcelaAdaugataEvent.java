package com.multitenant.event;

/**
 * Eveniment publicat de {@link com.multitenant.service.ParcelaService}
 * imediat dupa ce o Parcela noua a fost persistata cu succes in baza de date.
 *
 * Consumat de {@link CarteFunciaraEventListener#onParcelaAdaugata(ParcelaAdaugataEvent)}
 * pentru a recalcula automat suprafata_totala_intabulata din CarteFunciara aferenta terenului.
 *
 * Se folosesc ID-uri (nu entitati) pentru a evita problemele de LazyInitializationException
 * atunci cand listenerul ruleaza intr-o noua tranzactie separata (REQUIRES_NEW).
 *
 * @param parcelaId ID-ul parcelei proaspat persistate
 * @param terenId   ID-ul terenului caruia ii apartine parcela
 */
public record ParcelaAdaugataEvent(Long parcelaId, Long terenId) {
}
