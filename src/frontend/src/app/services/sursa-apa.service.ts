import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SursaApa } from '../models/sursa-apa.model';

@Injectable({
  providedIn: 'root'
})
export class SursaApaService {

  private apiUrl = '/api/parcele';

  constructor(private http: HttpClient) {}

  getSurse(parcelaId: number): Observable<SursaApa[]> {
    return this.http.get<SursaApa[]>(`${this.apiUrl}/${parcelaId}/surse-apa`);
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
