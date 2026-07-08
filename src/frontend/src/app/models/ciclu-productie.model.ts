export interface CicluProductie {
  id?: number;
  parcelaId: number;
  parcelaDenumire?: string;
  cultura: string;
  dataInfiintare: string;
  dataDefisare?: string;
  status: 'ACTIV' | 'FINALIZAT';
  programSprijin: boolean;
  warning?: string;
}
