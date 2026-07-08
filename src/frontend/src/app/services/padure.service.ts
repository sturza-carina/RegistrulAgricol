import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Padure } from '../models/padure.model';

@Injectable({
  providedIn: 'root'
})
export class PadureService {
  private apiUrl = '/api/parcele';

  constructor(private http: HttpClient) {}

  getPaduri(parcelaId: number, page: number = 0, size: number = 20): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/${parcelaId}/paduri`, { params });
  }

  createPadure(parcelaId: number, padure: Padure): Observable<Padure> {
    return this.http.post<Padure>(`${this.apiUrl}/${parcelaId}/paduri`, padure);
  }

  updatePadure(parcelaId: number, id: number, padure: Padure): Observable<Padure> {
    return this.http.put<Padure>(`${this.apiUrl}/${parcelaId}/paduri/${id}`, padure);
  }

  deletePadure(parcelaId: number, id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${parcelaId}/paduri/${id}`);
  }
}
