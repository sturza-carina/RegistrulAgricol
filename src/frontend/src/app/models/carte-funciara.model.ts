export interface CarteFunciara {
  id?: number;
  terenId?: number;
  numarCf?: string;
  numarTopografic?: string;
  suprafataTotalaIntabulata?: number;
  createdAt?: string;
  updatedAt?: string;

  // Denormalized for display (populated client-side)
  terenDenumire?: string;
}
