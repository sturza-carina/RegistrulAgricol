import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Fertilizare } from '../models/fertilizare.model';
import { PaginatedResponse } from '../models/paginated-response.model';

@Injectable({
  providedIn: 'root'
})
export class FertilizareService {

  private apiUrl = '/api/fertilizari';

  constructor(private http: HttpClient) { }

  getFertilizari(parcelaId?: number, page: number = 0, size: number = 20): Observable<PaginatedResponse<Fertilizare>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (parcelaId) {
      params = params.set('parcelaId', parcelaId.toString());
    }
    return this.http.get<PaginatedResponse<Fertilizare>>(this.apiUrl, { params });
  }

  getById(id: number): Observable<Fertilizare> {
    return this.http.get<Fertilizare>(`${this.apiUrl}/${id}`);
  }

  create(data: Fertilizare, confirmInterdictie: boolean = false): Observable<Fertilizare> {
    const params = new HttpParams().set('confirmInterdictie', confirmInterdictie.toString());
    return this.http.post<Fertilizare>(this.apiUrl, data, { params });
  }

  update(id: number, data: Fertilizare, confirmInterdictie: boolean = false): Observable<Fertilizare> {
    const params = new HttpParams().set('confirmInterdictie', confirmInterdictie.toString());
    return this.http.put<Fertilizare>(`${this.apiUrl}/${id}`, data, { params });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
