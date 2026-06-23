import { Gospodarie } from './gospodarie.model';
import { Persoana } from './persoana.model';

// =========================================================
// Enums
// =========================================================

export enum SpecieAnimal {
  BOVINE  = 'BOVINE',
  PORCINE = 'PORCINE',
  OVINE   = 'OVINE',
  CAPRINE = 'CAPRINE',
  PASARI  = 'PASARI',
  ECVINE  = 'ECVINE',
  APICOLE = 'APICOLE',
  ALTELE  = 'ALTELE'
}

export enum SexAnimal {
  MASCULIN = 'MASCULIN',
  FEMININ  = 'FEMININ'
}

export enum TipEvenimentAnimal {
  NASTERE              = 'NASTERE',
  CUMPARARE            = 'CUMPARARE',
  TRANSFER_INTRARE     = 'TRANSFER_INTRARE',
  TRATAMENT_VETERINAR  = 'TRATAMENT_VETERINAR', // Tratament medical ANSVSA
  VANZARE              = 'VANZARE',
  SACRIFICARE_PROPRIE  = 'SACRIFICARE_PROPRIE',
  MOARTE               = 'MOARTE',
  UCIDERE_FOCAR        = 'UCIDERE_FOCAR',
  DISPARITIE           = 'DISPARITIE'
}

// =========================================================
// Interfaces
// =========================================================

export interface AnimalIndividual {
  id?: number;
  gospodarie?: Gospodarie;
  gospodarieId?: number;
  proprietar?: Persoana;
  proprietarId?: number;
  numarCrotal?: string;   // opțional — animale netreticotalizate la naștere (ANSVSA permite)
  specie: SpecieAnimal;
  rasa?: string;
  sex: SexAnimal;
  dataNastere?: string;   // YYYY-MM-DD
  greutateKg?: number;
  stareActiva: boolean;   // READ-ONLY pe frontend — se modifică EXCLUSIV prin evenimente
  tenantId?: string;
}

/** Un snapshot al efectivului de grup la o anumită dată. Model append-only. */
export interface EfectivGrup {
  id?: number;
  gospodarie?: Gospodarie;
  gospodarieId?: number;
  proprietar?: Persoana;
  proprietarId?: number;
  specie: SpecieAnimal;
  numarCapeteFamilii: number;
  dataInregistrare?: string; // YYYY-MM-DD — data snapshot-ului (ANSVSA obligatoriu)
  detalii?: string;
  tenantId?: string;
}

export interface ProprietarAnimals {
  individuals: AnimalIndividual[];
  groups: EfectivGrup[];
}

export interface GospodarieAnimals {
  individuals: AnimalIndividual[];
  grupuriCurente: EfectivGrup[];   // cel mai recent snapshot per specie
  grupuriIstorice: EfectivGrup[];  // istoricul complet
}

export interface EvenimentAnimal {
  id?: number;
  animal?: AnimalIndividual;
  tipEveniment: TipEvenimentAnimal;
  dataEveniment: string;          // YYYY-MM-DD
  detalii?: string;
  destinatarTenantId?: string;    // completat automat la VANZARE cross-tenant
  tenantId?: string;
}

/** Payload pentru transferul cross-tenant al unui animal individual. */
export interface TransferRequest {
  destinatarTenantId: string;
  destinatarGospodarieId: number;
  destinatarProprietarId: number;
  detaliiTransfer?: string;
}

export interface TransferResponse {
  message: string;
  sourceTenant: string;
  destinatarTenantId: string;
  newAnimalId: number;
}
