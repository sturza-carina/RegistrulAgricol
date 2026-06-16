export interface Machinery {
  id?: number;
  tipUtilaj: string;
  marca: string;
  model: string;
  anFabricatie: number | null;
  numarInmatriculare?: string | null;
  gospodarieId: number;
}
