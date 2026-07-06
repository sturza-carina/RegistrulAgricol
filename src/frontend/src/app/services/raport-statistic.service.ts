import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RaportStatisticService {
  private readonly apiUrl = '/api/v1/statistici';

  constructor(private http: HttpClient) {}

  getComplet(an: number): Observable<any> {
    const params = new HttpParams().set('an', an.toString());
    return this.http.get<any>(`${this.apiUrl}/complet`, { params });
  }

  exportVegetal(an: number): Observable<Blob> {
    const params = new HttpParams().set('an', an.toString());
    return this.http.get(`${this.apiUrl}/export/vegetal`, { params, responseType: 'blob' });
  }

  exportZootehnic(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/export/zootehnic`, { responseType: 'blob' });
  }

  exportUtilaje(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/export/utilaje`, { responseType: 'blob' });
  }
}
