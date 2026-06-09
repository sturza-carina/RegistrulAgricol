export interface Address {
  county?: string;
  locality?: string;
  street?: string;
  streetNumber?: string;
  building?: string;
  staircase?: string;
  floor?: number;
  apartmentNumber?: number;
  postalCode?: string;
}

export interface Person {
  id?: number;
  personType: 'PHYSICAL_PERSON' | 'LEGAL_ENTITY';
  address?: Address;
  phoneNumber?: string;
  email?: string;
  registerVolume?: string;
  registerPosition?: string;
  notes?: string;
  tenantId?: string;
}

export interface PhysicalPerson extends Person {
  personType: 'PHYSICAL_PERSON';
  firstName: string;
  lastName: string;
  cnp?: string;
  dateOfBirth?: string; // YYYY-MM-DD
  isHeadOfHousehold?: boolean;
}

export interface LegalEntity extends Person {
  personType: 'LEGAL_ENTITY';
  companyName: string;
  cui?: string;
  registrationNumber?: string;
  legalRepresentative?: string;
}
