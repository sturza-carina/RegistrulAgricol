import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AnimalIndividual,
  EfectivGrup,
  ProprietarAnimals,
  GospodarieAnimals,
  EvenimentAnimal,
  TransferRequest,
  TransferResponse
} from '../models/animal.model';

import { PaginatedResponse } from '../models/paginated-response.model';

@Injectable({
  providedIn: 'root'
})
export class AnimalService {
  private baseApiUrl = '/api/animals';

  constructor(private http: HttpClient) {}

  // -------------------------------------------------------
  // AnimalIndividual — CRUD
  // -------------------------------------------------------

  getAllIndividuals(page: number = 0, size: number = 20): Observable<PaginatedResponse<AnimalIndividual>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PaginatedResponse<AnimalIndividual>>(`${this.baseApiUrl}/individual`, { params });
  }

  getIndividualById(id: number): Observable<AnimalIndividual> {
    return this.http.get<AnimalIndividual>(`${this.baseApiUrl}/individual/${id}`);
  }

  createIndividual(animal: AnimalIndividual): Observable<AnimalIndividual> {
    return this.http.post<AnimalIndividual>(`${this.baseApiUrl}/individual`, animal);
  }

  updateIndividual(id: number, animal: AnimalIndividual): Observable<AnimalIndividual> {
    return this.http.put<AnimalIndividual>(`${this.baseApiUrl}/individual/${id}`, animal);
  }

  deleteIndividual(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseApiUrl}/individual/${id}`);
  }

  // -------------------------------------------------------
  // AnimalIndividual — Timeline
  // -------------------------------------------------------

  adaugaEveniment(animalId: number, eveniment: EvenimentAnimal): Observable<EvenimentAnimal> {
    return this.http.post<EvenimentAnimal>(
      `${this.baseApiUrl}/individual/${animalId}/evenimente`, eveniment
    );
  }

  getTimeline(animalId: number): Observable<EvenimentAnimal[]> {
    return this.http.get<EvenimentAnimal[]>(
      `${this.baseApiUrl}/individual/${animalId}/evenimente`
    );
  }

  // -------------------------------------------------------
  // AnimalIndividual — Cross-Tenant Transfer
  // -------------------------------------------------------

  /**
   * Inițiază transferul unui animal din tenant-ul curent în altul.
   * Returnează ID-ul noului animal creat în schema destinatară.
   */
  transferAnimal(animalId: number, request: TransferRequest): Observable<TransferResponse> {
    return this.http.post<TransferResponse>(
      `${this.baseApiUrl}/individual/${animalId}/transfer`, request
    );
  }

  // -------------------------------------------------------
  // EfectivGrup — Snapshot Model
  // -------------------------------------------------------

  getAllGroups(page: number = 0, size: number = 20): Observable<PaginatedResponse<EfectivGrup>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PaginatedResponse<EfectivGrup>>(`${this.baseApiUrl}/grup`, { params });
  }

  getGroupById(id: number): Observable<EfectivGrup> {
    return this.http.get<EfectivGrup>(`${this.baseApiUrl}/grup/${id}`);
  }

  createGroup(grup: EfectivGrup): Observable<EfectivGrup> {
    return this.http.post<EfectivGrup>(`${this.baseApiUrl}/grup`, grup);
  }

  /**
   * Adaugă un snapshot nou (nu modifică rândul existent — model append-only ANSVSA).
   */
  addGrupSnapshot(referenceId: number, grup: EfectivGrup): Observable<EfectivGrup> {
    return this.http.post<EfectivGrup>(`${this.baseApiUrl}/grup/${referenceId}/snapshot`, grup);
  }

  getGrupHistory(gospodarieId: number, page: number = 0, size: number = 20): Observable<PaginatedResponse<EfectivGrup>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PaginatedResponse<EfectivGrup>>(`${this.baseApiUrl}/grup/${gospodarieId}/history`, { params });
  }

  deleteGroup(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseApiUrl}/grup/${id}`);
  }

  // -------------------------------------------------------
  // Combined Queries
  // -------------------------------------------------------

  getAnimalsByProprietar(proprietarId: number, page: number = 0, size: number = 20): Observable<ProprietarAnimals> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<ProprietarAnimals>(`${this.baseApiUrl}/proprietar/${proprietarId}`, { params });
  }

  /**
   * Returnează animale individuale + efectiv grup (curent + istoric) pentru o gospodărie.
   */
  getAnimalsByGospodarie(gospodarieId: number, page: number = 0, size: number = 20): Observable<GospodarieAnimals> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<GospodarieAnimals>(`${this.baseApiUrl}/gospodarie/${gospodarieId}`, { params });
  }

  /**
   * Returnează toate UAT-urile înregistrate în sistem pentru dropdown-ul de transfer.
   */
  getAllUats(): Observable<any[]> {
    return this.http.get<any[]>('/api/uats');
  }

  /**
   * Returnează gospodăriile dintr-un tenant specific (folosind header-ul X-Tenant-ID),
   * opțional filtrate după codul SIRUTA al UAT-ului (uatCode).
   */
  getGospodariiByTenant(tenantId: string, uatCode?: string): Observable<any[]> {
    const params: { [param: string]: string } = {};
    if (uatCode) {
      params['uatCode'] = uatCode;
    }
    return this.http.get<any[]>('/api/gospodarii', {
      headers: { 'X-Tenant-ID': tenantId },
      params
    });
  }

  /**
   * Returnează persoanele dintr-un tenant specific (folosind header-ul X-Tenant-ID).
   */
  getPersonsByTenant(tenantId: string): Observable<any[]> {
    return this.http.get<any[]>('/api/persons', {
      headers: { 'X-Tenant-ID': tenantId }
    });
  }
}
