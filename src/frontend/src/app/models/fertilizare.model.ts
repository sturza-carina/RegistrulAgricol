export interface Fertilizare {
  id?: number;
  dataAplicarii: string;
  parcelaId: number;
  parcelaDenumire?: string;
  catalogIngrasaminteId: number;
  catalogIngrasaminteDenumire?: string;
  catalogIngrasaminteTip?: string;
  procentAzot?: number;
  procentFosfor?: number;
  procentPotasiu?: number;
  cantitateBruta: number;
  unitateMasura: string;
  aportAzot?: number;
  aportFosfor?: number;
  aportPotasiu?: number;
  cicluProductieId?: number;
  cicluProductieCultura?: string;
}
