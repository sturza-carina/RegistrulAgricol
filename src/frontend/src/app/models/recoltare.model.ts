export interface Recoltare {
  id?: number;
  parcelaId: number;
  parcelaDenumire?: string;
  cicluProductieId?: number;
  cicluProductieCultura?: string;
  cultura: string;
  dataRecoltare: string;
  cantitateKg: number;
}
