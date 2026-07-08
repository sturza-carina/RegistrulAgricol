import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CicluProductie } from '../models/ciclu-productie.model';
import { PaginatedResponse } from '../models/paginated-response.model';

@Injectable({
  providedIn: 'root'
})
export class CicluProductieService {

  private apiUrl = '/api/cicluri-productie';

  constructor(private http: HttpClient) { }

  getCicluri(parcelaId: number, page: number = 0, size: number = 20): Observable<PaginatedResponse<CicluProductie>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PaginatedResponse<CicluProductie>>(`${this.apiUrl}/parcela/${parcelaId}`, { params });
  }

  getCicluById(id: number): Observable<CicluProductie> {
    return this.http.get<CicluProductie>(`${this.apiUrl}/${id}`);
  }

  createCiclu(data: CicluProductie): Observable<CicluProductie> {
    return this.http.post<CicluProductie>(this.apiUrl, data);
  }

  updateCiclu(id: number, data: CicluProductie): Observable<CicluProductie> {
    return this.http.put<CicluProductie>(`${this.apiUrl}/${id}`, data);
  }

  deleteCiclu(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  downloadPdf(anAgricol?: number): Observable<Blob> {
    let params = new HttpParams();
    if (anAgricol) {
      params = params.set('anAgricol', anAgricol.toString());
    }
    return this.http.get(`${this.apiUrl}/export/pdf`, { params, responseType: 'blob' });
  }
}
