package com.multitenant.event;

import com.multitenant.model.registru.Teren;

/**
 * Eveniment publicat de {@link com.multitenant.service.TerenService}
 * imediat dupa ce un Teren nou a fost persistat cu succes in baza de date.
 *
 * Consumat de {@link CarteFunciaraEventListener#onTerenCreated(TerenCreatedEvent)}
 * pentru a crea automat o CarteFunciara goala asociata terenului.
 *
 * @param teren entitatea Teren deja persistata (cu id non-null)
 */
public record TerenCreatedEvent(Teren teren) {
}
