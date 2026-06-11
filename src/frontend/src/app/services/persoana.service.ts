import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Persoana } from '../models/persoana.model';

@Injectable({
  providedIn: 'root'
})
export class PersoanaService {
  private apiUrl = '/api/persoane';

  constructor(private http: HttpClient) { }

  getAllPersons(search?: string, type?: string): Observable<Persoana[]> {
    let params = new HttpParams();
    if (search) params = params.set('search', search);
    if (type) params = params.set('type', type);
    
    return this.http.get<Persoana[]>(this.apiUrl, { params });
  }

  getPersonsByGospodarieId(gospodarieId: number): Observable<Persoana[]> {
    return this.http.get<Persoana[]>(`${this.apiUrl}/gospodarie/${gospodarieId}`);
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
}

