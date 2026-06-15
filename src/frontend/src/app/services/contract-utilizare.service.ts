import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ContractUtilizare {
  id?: number;
  teren?: { id: number; denumire?: string } | null;
  locatorProprietar?: { id: number; username?: string; nume?: string } | null;
  locatorUtilizator?: { id: number; username?: string; nume?: string } | null;
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
  utilizatorOperare?: { id: number; username?: string; nume?: string } | null;
  esteActiv?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ContractUtilizareService {
  private apiUrl = '/api/contracte';

  constructor(private http: HttpClient) {}

  getAllContracts(): Observable<ContractUtilizare[]> {
    return this.http.get<ContractUtilizare[]>(this.apiUrl);
  }

  getContractById(id: number): Observable<ContractUtilizare> {
    return this.http.get<ContractUtilizare>(`${this.apiUrl}/${id}`);
  }

  createContract(contract: ContractUtilizare): Observable<ContractUtilizare> {
    return this.http.post<ContractUtilizare>(this.apiUrl, contract);
  }

  updateContract(id: number, contract: ContractUtilizare): Observable<ContractUtilizare> {
    return this.http.put<ContractUtilizare>(`${this.apiUrl}/${id}`, contract);
  }

  deleteContract(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
