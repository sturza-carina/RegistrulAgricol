import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Persoana } from '../models/persoana.model';
import { Parcela } from '../models/parcela.model';
import { PaginatedResponse } from '../models/paginated-response.model';

export type ParcelaRef = Partial<Parcela> & { id: number };
export type PersoanaRef = Partial<Persoana> & { id: number };

export interface ContractUtilizare {
  id?: number;
  parcela?: ParcelaRef | null;
  locatorProprietar?: PersoanaRef | null;
  locatorUtilizator?: PersoanaRef | null;
  tipContract: string; // ARENDA, COMODAT, CONCESIUNE, INCHIRIERE, ALTELE
  numarContract: string;
  dataSemnare?: string | null;
  dataInceput?: string | null;
  dataSfarsit?: string | null;
  pretArendaRonAn?: number | null;
  pretArendaGrauKgHa?: number | null;
  indexarePret?: boolean;
  statusContract?: string; // ACTIV, EXPIRAT, REZILIAT, SUSPENDAT
  motivIncetare?: string | null;
  dataOperare?: string | null;
  esteActiv?: boolean;
  semnatElectronic?: boolean;
  dataSemnaturiiElectronice?: string | null;
  hashDocumentSemnat?: string | null;
}

export interface ContractUtilizareRequest {
  parcelaId: number;
  locatorProprietarId?: number | null;
  locatorUtilizatorId?: number | null;
  tipContract: string;
  numarContract: string;
  dataSemnare?: string | null;
  dataInceput?: string | null;
  dataSfarsit?: string | null;
  pretArendaRonAn?: number | null;
  pretArendaGrauKgHa?: number | null;
  indexarePret?: boolean;
  statusContract?: string;
  motivIncetare?: string | null;
  dataOperare?: string | null;
  esteActiv?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ContractUtilizareService {
  private apiUrl = '/api/contracte';

  constructor(private http: HttpClient) {}

  getAllContracts(uatCode?: string, page: number = 0, size: number = 20): Observable<PaginatedResponse<ContractUtilizare>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (uatCode) {
      params = params.set('uatCode', uatCode);
    }
    return this.http.get<PaginatedResponse<ContractUtilizare>>(this.apiUrl, { params });
  }

  getContractById(id: number): Observable<ContractUtilizare> {
    return this.http.get<ContractUtilizare>(`${this.apiUrl}/${id}`);
  }

  createContract(contract: ContractUtilizareRequest): Observable<ContractUtilizare> {
    return this.http.post<ContractUtilizare>(this.apiUrl, contract);
  }

  updateContract(id: number, contract: ContractUtilizareRequest): Observable<ContractUtilizare> {
    return this.http.put<ContractUtilizare>(`${this.apiUrl}/${id}`, contract);
  }

  deleteContract(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  semneazaContract(id: number, semnaturaImagineBase64?: string): Observable<ContractUtilizare> {
    return this.http.post<ContractUtilizare>(`${this.apiUrl}/${id}/semnare`, { semnaturaImagineBase64 });
  }

  downloadDocumentSemnat(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/document-semnat`, { responseType: 'blob' });
  }
}
