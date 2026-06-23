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

export interface Uat {
  id?: number;
  codSiruta: string;
  denumire: string;
  judet: string;
  tipUat: string;
  isActive: boolean;
  tenantId?: string;
}

export interface Gospodarie {
  id?: number;
  codGospodarie: string;
  adresa: Adresa;
  tipGospodarie: string; // 'INDIVIDUALA', 'COLECTIVA', 'ASOCIATIE'
  activa: boolean;
  uat?: Uat;
}
