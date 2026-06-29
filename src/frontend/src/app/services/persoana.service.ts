import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Persoana } from '../models/persoana.model';
import { PaginatedResponse } from '../models/paginated-response.model';

@Injectable({
  providedIn: 'root'
})
export class PersoanaService {
  private apiUrl = '/api/persons';

  constructor(private http: HttpClient) { }

  getAllPersons(search?: string, type?: string, page: number = 0, size: number = 20): Observable<PaginatedResponse<Persoana>> {
    let params = new HttpParams();
    if (search) params = params.set('search', search);
    if (type) params = params.set('type', type);
    params = params.set('page', page.toString());
    params = params.set('size', size.toString());
    
    return this.http.get<PaginatedResponse<Persoana>>(this.apiUrl, { params });
  }

  getPersonsByGospodarieId(gospodarieId: number, page: number = 0, size: number = 20): Observable<PaginatedResponse<Persoana>> {
    let params = new HttpParams();
    params = params.set('page', page.toString());
    params = params.set('size', size.toString());
    return this.http.get<PaginatedResponse<Persoana>>(`${this.apiUrl}/gospodarie/${gospodarieId}`, { params });
  }

  getPersonById(id: number): Observable<Persoana> {
    return this.http.get<Persoana>(`${this.apiUrl}/${id}`);
  }

  createPerson(persoana: Persoana): Observable<Persoana> {
    return this.http.post<Persoana>(this.apiUrl, persoana);
  }

  updatePerson(id: number, persoana: Persoana): Observable<Persoana> {
    return this.http.put<Persoana>(`${this.apiUrl}/${id}`, persoana);
  }

  deletePerson(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  addPersonToGospodarie(persoanaId: number, gospodarieId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${persoanaId}/gospodarii/${gospodarieId}`, {});
  }
}

