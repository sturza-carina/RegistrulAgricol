import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Recoltare } from '../models/recoltare.model';
import { PaginatedResponse } from '../models/paginated-response.model';

export interface CentralizatorRecoltare {
  cultura: string;
  tipMediu: string;
  cantitateTotalaKg: number;
  suprafataTotalaMp: number;
  randamentKgMp: number;
}

@Injectable({
  providedIn: 'root'
})
export class RecoltareService {

  private apiUrl = '/api/recoltari';

  constructor(private http: HttpClient) { }

  getRecoltari(parcelaId: number, page: number = 0, size: number = 20): Observable<PaginatedResponse<Recoltare>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PaginatedResponse<Recoltare>>(`${this.apiUrl}/parcela/${parcelaId}`, { params });
  }

  getRecoltareById(id: number): Observable<Recoltare> {
    return this.http.get<Recoltare>(`${this.apiUrl}/${id}`);
  }

  createRecoltare(data: Recoltare): Observable<Recoltare> {
    return this.http.post<Recoltare>(this.apiUrl, data);
  }

  updateRecoltare(id: number, data: Recoltare): Observable<Recoltare> {
    return this.http.put<Recoltare>(`${this.apiUrl}/${id}`, data);
  }

  deleteRecoltare(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getCentralizator(anAgricol?: number): Observable<CentralizatorRecoltare[]> {
    let params = new HttpParams();
    if (anAgricol) {
      params = params.set('anAgricol', anAgricol.toString());
    }
    return this.http.get<CentralizatorRecoltare[]>(`${this.apiUrl}/centralizator`, { params });
  }
}
