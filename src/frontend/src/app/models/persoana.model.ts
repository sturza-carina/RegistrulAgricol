export interface Adresa {
  county?: string;
  localitate?: string;
  street?: string;
  streetNumber?: string;
  building?: string;
  staircase?: string;
  floor?: number;
  apartmentNumber?: number;
  postalCode?: string;
}

export interface Persoana {
  id?: number;
  personType: 'PHYSICAL_PERSON' | 'LEGAL_ENTITY';
  adresa?: Adresa;
  phoneNumber?: string;
  email?: string;
  registerVolume?: string;
  registerPosition?: string;
  notes?: string;
  gospodarieIds?: number[];
  gospodarii?: { id: number }[];
  tenantId?: string;
}

export interface PersoanaFizica extends Persoana {
  personType: 'PHYSICAL_PERSON';
  firstName: string;
  lastName: string;
  cnp?: string;
  dateOfBirth?: string; // YYYY-MM-DD
}

export interface PersoanaJuridica extends Persoana {
  personType: 'LEGAL_ENTITY';
  companyName: string;
  cui?: string;
  registrationNumber?: string;
  legalRepresentative?: string;
}


