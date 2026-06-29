import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Parcela } from '../models/parcela.model';
import { PaginatedResponse } from '../models/paginated-response.model';

@Injectable({
  providedIn: 'root'
})
export class ParcelaService {
  private apiUrl = '/api/parcele';

  constructor(private http: HttpClient) {}

  getAllParcele(page: number = 0, size: number = 20): Observable<PaginatedResponse<Parcela>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PaginatedResponse<Parcela>>(this.apiUrl, { params });
  }

  getParcele(terenId: number, page: number = 0, size: number = 20): Observable<PaginatedResponse<Parcela>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PaginatedResponse<Parcela>>(`${this.apiUrl}/teren/${terenId}`, { params });
  }

  createParcela(terenId: number, parcela: Parcela): Observable<Parcela> {
    return this.http.post<Parcela>(`${this.apiUrl}/teren/${terenId}`, parcela);
  }

  updateParcela(id: number, parcela: Parcela): Observable<Parcela> {
    return this.http.put<Parcela>(`${this.apiUrl}/${id}`, parcela);
  }

  deleteParcela(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
