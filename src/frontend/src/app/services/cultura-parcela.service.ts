import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CulturaParcela } from '../models/cultura-parcela.model';

@Injectable({
  providedIn: 'root'
})
export class CulturaParcelaService {

  private apiUrl = '/api/parcele';

  constructor(private http: HttpClient) { }

  getCulturi(parcelaId: number): Observable<CulturaParcela[]> {
    return this.http.get<CulturaParcela[]>(`${this.apiUrl}/${parcelaId}/culturi`);
  }

  createCultura(parcelaId: number, data: CulturaParcela): Observable<CulturaParcela> {
    return this.http.post<CulturaParcela>(`${this.apiUrl}/${parcelaId}/culturi`, data);
  }

  updateCultura(parcelaId: number, culturaId: number, data: CulturaParcela): Observable<CulturaParcela> {
    return this.http.put<CulturaParcela>(`${this.apiUrl}/${parcelaId}/culturi/${culturaId}`, data);
  }

  deleteCultura(parcelaId: number, culturaId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${parcelaId}/culturi/${culturaId}`);
  }
}
