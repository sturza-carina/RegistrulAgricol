export enum TipInregistrarePom {
  IZOLAT = 'IZOLAT',
  PLANTATIE = 'PLANTATIE'
}

export interface Pom {
  id?: number;
  tipInregistrare: TipInregistrarePom | '';
  specie: string;
  soi?: string;
  anPlantare?: number;
  numarPomi?: number;
  suprafataHa?: number;
  densitatePomiHa?: number;
  starePomi?: string;
  sistemIntretinere?: string;
  sistemIrigare?: string;
  productieEstimataKg?: number;
  observatii?: string;
  parcelaId?: number;
}
