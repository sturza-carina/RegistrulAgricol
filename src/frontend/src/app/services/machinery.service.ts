import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Machinery } from '../models/machinery.model';
import { PaginatedResponse } from '../models/paginated-response.model';

@Injectable({
  providedIn: 'root'
})
export class MachineryService {
  private readonly apiUrl = '/api/machinery';

  constructor(private http: HttpClient) {}

  getMachineryByGospodarie(gospodarieId: number, page: number = 0, size: number = 20): Observable<PaginatedResponse<Machinery>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PaginatedResponse<Machinery>>(`${this.apiUrl}/gospodarie/${gospodarieId}`, { params });
  }

  create(machinery: Machinery): Observable<Machinery> {
    return this.http.post<Machinery>(this.apiUrl, machinery);
  }

  update(id: number, machinery: Machinery): Observable<Machinery> {
    return this.http.put<Machinery>(`${this.apiUrl}/${id}`, machinery);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
