export interface TratamentFitosanitar {
  id?: number;
  dataEfectuarii: string;
  fenofaza: string;
  parcelaId: number;
  parcelaDenumire?: string;
  agentDaunator: string;
  catalogPppId: number;
  catalogPppDenumire?: string;
  catalogPppDozaOmologata?: number;
  catalogPppTimpPauza?: number;
  dozaUtilizata: number;
  suprafataTratata: number;
  cantitateTotala?: number;
  responsabil: string;
  semnaturaElectronica?: string;
  dataIncepereRecoltare?: string;
  documentDareConsum?: string;
  dozaDepasita?: boolean;
  justificareSupradozaj?: string;
  unitateMasuraDoza?: string;
  dataLansarii?: string;
  numarCutiiIndivizi?: number;
  cicluProductieId?: number;
  cicluProductieCultura?: string;
}
