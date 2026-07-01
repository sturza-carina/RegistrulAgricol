package com.multitenant.model.registru;

/**
 * Tipul de zona al unei parcele agricole.
 * Critic pentru calculul impozitului pe teren (Codul Fiscal, art. 463-467):
 * - INTRAVILAN: teren in intravilanul localitatii (regim de impozitare diferentiat)
 * - EXTRAVILAN: teren agricol in afara localitatii
 */
public enum TipZona {
    INTRAVILAN,
    EXTRAVILAN
}
