package com.multitenant.model.animal;

/**
 * Tipurile de evenimente care pot fi înregistrate în istoricul unui animal individual.
 * Ordinea reflectă ciclul de viață tipic al unui animal (SNIIA/ANSVSA).
 *
 * Categorii:
 *  - ORIGINE: NASTERE, CUMPARARE, TRANSFER_INTRARE — un singur eveniment de origine per animal
 *  - INTERMEDIARE: TRATAMENT_VETERINAR — repetabile, nu afectează starea activă
 *  - TERMINALE: VANZARE, SACRIFICARE_PROPRIE, MOARTE, UCIDERE_FOCAR, DISPARITIE — îngheață istoricul
 */
public enum TipEvenimentAnimal {
    NASTERE,
    CUMPARARE,
    TRANSFER_INTRARE,
    TRATAMENT_VETERINAR,   // Tratament medical (ANSVSA — obligatoriu de înregistrat)
    VANZARE,
    SACRIFICARE_PROPRIE,
    MOARTE,
    UCIDERE_FOCAR,
    DISPARITIE
}
