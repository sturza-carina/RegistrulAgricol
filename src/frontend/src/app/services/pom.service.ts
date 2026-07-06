import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Pom } from '../models/pom.model';
import { PaginatedResponse } from '../models/paginated-response.model';

@Injectable({
  providedIn: 'root'
})
export class PomService {

  private apiUrl = '/api/parcele';

  constructor(private http: HttpClient) { }

  getPomi(parcelaId: number, page: number = 0, size: number = 20): Observable<PaginatedResponse<Pom>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PaginatedResponse<Pom>>(`${this.apiUrl}/${parcelaId}/pomi`, { params });
  }

  createPom(parcelaId: number, data: Pom): Observable<Pom> {
    return this.http.post<Pom>(`${this.apiUrl}/${parcelaId}/pomi`, data);
  }

  updatePom(parcelaId: number, pomId: number, data: Pom): Observable<Pom> {
    return this.http.put<Pom>(`${this.apiUrl}/${parcelaId}/pomi/${pomId}`, data);
  }

  deletePom(parcelaId: number, pomId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${parcelaId}/pomi/${pomId}`);
  }
}
