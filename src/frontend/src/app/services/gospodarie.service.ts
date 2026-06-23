import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Gospodarie } from '../models/gospodarie.model';

@Injectable({
  providedIn: 'root'
})
export class GospodarieService {
  private apiUrl = '/api/gospodarii';

  constructor(private http: HttpClient) {}

  getAllGospodarii(uatCode?: string): Observable<Gospodarie[]> {
    let params = new HttpParams();
    if (uatCode) {
      params = params.set('uatCode', uatCode);
    }
    return this.http.get<Gospodarie[]>(this.apiUrl, { params });
  }

  getGospodarieById(id: number): Observable<Gospodarie> {
    return this.http.get<Gospodarie>(`${this.apiUrl}/${id}`);
  }

  createGospodarie(gospodarie: Gospodarie): Observable<Gospodarie> {
    return this.http.post<Gospodarie>(this.apiUrl, gospodarie);
  }

  updateGospodarie(id: number, gospodarie: Gospodarie): Observable<Gospodarie> {
    return this.http.put<Gospodarie>(`${this.apiUrl}/${id}`, gospodarie);
  }

  deleteGospodarie(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
