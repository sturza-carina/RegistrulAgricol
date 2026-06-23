import { Gospodarie } from './gospodarie.model';
import { Persoana } from './persoana.model';

export enum SpecieAnimal {
  BOVINE = 'BOVINE',
  PORCINE = 'PORCINE',
  OVINE = 'OVINE',
  CAPRINE = 'CAPRINE',
  PASARI = 'PASARI',
  ECVINE = 'ECVINE',
  APICOLE = 'APICOLE',
  ALTELE = 'ALTELE'
}

export enum SexAnimal {
  MASCULIN = 'MASCULIN',
  FEMININ = 'FEMININ'
}

export interface AnimalIndividual {
  id?: number;
  gospodarie?: Gospodarie;
  gospodarieId?: number;
  proprietar?: Persoana;
  proprietarId?: number;
  numarCrotal: string;
  specie: SpecieAnimal;
  rasa?: string;
  sex: SexAnimal;
  dataNastere?: string; // YYYY-MM-DD
  greutateKg?: number;
  stareActiva: boolean;
  tenantId?: string;
}

export interface EfectivGrup {
  id?: number;
  gospodarie?: Gospodarie;
  gospodarieId?: number;
  proprietar?: Persoana;
  proprietarId?: number;
  specie: SpecieAnimal;
  numarCapeteFamilii: number;
  detalii?: string;
  tenantId?: string;
}

export interface ProprietarAnimals {
  individuals: AnimalIndividual[];
  groups: EfectivGrup[];
}

export enum TipEvenimentAnimal {
  NASTERE           = 'NASTERE',
  CUMPARARE         = 'CUMPARARE',
  TRANSFER_INTRARE  = 'TRANSFER_INTRARE',
  VANZARE           = 'VANZARE',
  SACRIFICARE_PROPRIE = 'SACRIFICARE_PROPRIE',
  MOARTE            = 'MOARTE',
  UCIDERE_FOCAR     = 'UCIDERE_FOCAR',
  DISPARITIE        = 'DISPARITIE'
}

export interface EvenimentAnimal {
  id?: number;
  animal?: AnimalIndividual;
  tipEveniment: TipEvenimentAnimal;
  dataEveniment: string; // YYYY-MM-DD
  detalii?: string;
  tenantId?: string;
}
