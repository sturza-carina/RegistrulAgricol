import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Gospodarie } from '../models/gospodarie.model';

@Injectable({
  providedIn: 'root'
})
export class GospodarieService {
  private apiUrl = '/api/gospodarii';

  constructor(private http: HttpClient) {}

  getAllGospodarii(): Observable<Gospodarie[]> {
    return this.http.get<Gospodarie[]>(this.apiUrl);
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
