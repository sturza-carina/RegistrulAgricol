import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PasuneFaneata } from '../models/pasune-faneata.model';
import { PaginatedResponse } from '../models/paginated-response.model';

@Injectable({
  providedIn: 'root'
})
export class PasuneFaneataService {

  private apiUrl = '/api/parcele';

  constructor(private http: HttpClient) { }

  getPasuneFaneata(parcelaId: number, page: number = 0, size: number = 20): Observable<PaginatedResponse<PasuneFaneata>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PaginatedResponse<PasuneFaneata>>(`${this.apiUrl}/${parcelaId}/pasuni-fanete`, { params });
  }

  create(parcelaId: number, data: PasuneFaneata): Observable<PasuneFaneata> {
    return this.http.post<PasuneFaneata>(`${this.apiUrl}/${parcelaId}/pasuni-fanete`, data);
  }

  update(parcelaId: number, id: number, data: PasuneFaneata): Observable<PasuneFaneata> {
    return this.http.put<PasuneFaneata>(`${this.apiUrl}/${parcelaId}/pasuni-fanete/${id}`, data);
  }

  delete(parcelaId: number, id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${parcelaId}/pasuni-fanete/${id}`);
  }
}
