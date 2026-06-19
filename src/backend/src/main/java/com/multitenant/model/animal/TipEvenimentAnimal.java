package com.multitenant.model.animal;

/**
 * Tipurile de evenimente care pot fi înregistrate în istoricul unui animal individual.
 * Ordinea reflectă ciclul de viață tipic al unui animal.
 */
public enum TipEvenimentAnimal {
    NASTERE,
    CUMPARARE,
    TRANSFER_INTRARE,
    VANZARE,
    SACRIFICARE_PROPRIE,
    MOARTE,
    UCIDERE_FOCAR,
    DISPARITIE
}
