import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Teren } from '../models/teren.model';
import { PaginatedResponse } from '../models/paginated-response.model';

@Injectable({
  providedIn: 'root'
})
export class TerenService {
  private apiUrl = '/api/terenuri';

  constructor(private http: HttpClient) {}

  getAllTerenuri(page: number = 0, size: number = 20): Observable<PaginatedResponse<Teren>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PaginatedResponse<Teren>>(this.apiUrl, { params });
  }

  getTerenByGospodarieId(gospodarieId: number, page: number = 0, size: number = 20): Observable<PaginatedResponse<Teren>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PaginatedResponse<Teren>>(`${this.apiUrl}/gospodarie/${gospodarieId}`, { params });
  }

  getTerenById(id: number): Observable<Teren> {
    return this.http.get<Teren>(`${this.apiUrl}/${id}`);
  }

  createTeren(teren: Teren): Observable<Teren> {
    return this.http.post<Teren>(this.apiUrl, teren);
  }

  createTerenWithParcela(dto: any): Observable<Teren> {
    return this.http.post<Teren>(`${this.apiUrl}/with-parcela`, dto);
  }

  updateTeren(id: number, teren: Teren): Observable<Teren> {
    return this.http.put<Teren>(`${this.apiUrl}/${id}`, teren);
  }

  deleteTeren(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
