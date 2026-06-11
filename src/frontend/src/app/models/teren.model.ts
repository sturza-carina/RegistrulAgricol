import { Gospodarie } from './gospodarie.model';

export interface Teren {
  id?: number;
  denumire: string;

  gospodarie?: Gospodarie;
  gospodarieId?: number;
}
