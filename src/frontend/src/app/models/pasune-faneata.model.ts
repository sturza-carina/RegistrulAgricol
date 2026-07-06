export enum TipFolosintaPasune {
  PASUNAT = 'PASUNAT',
  COSIT = 'COSIT',
  MIXT = 'MIXT'
}

export interface PasuneFaneata {
  id?: number;
  tipFolosinta: TipFolosintaPasune | '';
  suprafataHa: number;
  speciiDominante?: string;
  numarAnimalePasunat?: number;
  numarCosiriAnuale?: number;
  productieEstimataKgHa?: number;
  stareVegetatie?: string;
  sistemIntretinere?: string;
  sistemIrigare?: string;
  observatii?: string;
  parcelaId?: number;
}
