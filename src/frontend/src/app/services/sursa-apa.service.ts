import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SursaApa } from '../models/sursa-apa.model';
import { PaginatedResponse } from '../models/paginated-response.model';

@Injectable({
  providedIn: 'root'
})
export class SursaApaService {

  private apiUrl = '/api/parcele';

  constructor(private http: HttpClient) {}

  getSurse(parcelaId: number, page: number = 0, size: number = 20): Observable<PaginatedResponse<SursaApa>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PaginatedResponse<SursaApa>>(`${this.apiUrl}/${parcelaId}/surse-apa`, { params });
  }

  createSursa(parcelaId: number, data: SursaApa): Observable<SursaApa> {
    return this.http.post<SursaApa>(`${this.apiUrl}/${parcelaId}/surse-apa`, data);
  }

  updateSursa(parcelaId: number, sursaId: number, data: SursaApa): Observable<SursaApa> {
    return this.http.put<SursaApa>(`${this.apiUrl}/${parcelaId}/surse-apa/${sursaId}`, data);
  }

  deleteSursa(parcelaId: number, sursaId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${parcelaId}/surse-apa/${sursaId}`);
  }
}
