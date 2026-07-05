export enum TipInregistrareVita {
  IZOLAT = 'IZOLAT',
  PLANTATIE = 'PLANTATIE'
}

export interface VitaDeVie {
  id?: number;
  tipInregistrare: TipInregistrareVita | '';
  specie: string;
  soi?: string;
  anPlantare?: number;
  numarVite?: number;
  suprafataHa?: number;
  densitateViteHa?: number;
  stareVita?: string;
  sistemIntretinere?: string;
  sistemIrigare?: string;
  productieEstimataKg?: number;
  observatii?: string;
  parcelaId?: number;
}
