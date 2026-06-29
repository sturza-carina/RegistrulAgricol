import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CulturaParcela } from '../models/cultura-parcela.model';
import { PaginatedResponse } from '../models/paginated-response.model';

@Injectable({
  providedIn: 'root'
})
export class CulturaParcelaService {

  private apiUrl = '/api/parcele';

  constructor(private http: HttpClient) { }

  getCulturi(parcelaId: number, page: number = 0, size: number = 20): Observable<PaginatedResponse<CulturaParcela>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PaginatedResponse<CulturaParcela>>(`${this.apiUrl}/${parcelaId}/culturi`, { params });
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
