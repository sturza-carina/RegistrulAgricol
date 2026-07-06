import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RaportStatisticService {
  private readonly apiUrl = '/api/v1/statistici';

  constructor(private http: HttpClient) {}

  getComplet(an: number, uatCode?: string): Observable<any> {
    let params = new HttpParams().set('an', an.toString());
    if (uatCode) {
      params = params.set('uatCode', uatCode);
    }
    return this.http.get<any>(`${this.apiUrl}/complet`, { params });
  }

  exportVegetal(an: number, uatCode?: string): Observable<Blob> {
    let params = new HttpParams().set('an', an.toString());
    if (uatCode) {
      params = params.set('uatCode', uatCode);
    }
    return this.http.get(`${this.apiUrl}/export/vegetal`, { params, responseType: 'blob' });
  }

  exportZootehnic(uatCode?: string): Observable<Blob> {
    let params = new HttpParams();
    if (uatCode) {
      params = params.set('uatCode', uatCode);
    }
    return this.http.get(`${this.apiUrl}/export/zootehnic`, { params, responseType: 'blob' });
  }

  exportUtilaje(uatCode?: string): Observable<Blob> {
    let params = new HttpParams();
    if (uatCode) {
      params = params.set('uatCode', uatCode);
    }
    return this.http.get(`${this.apiUrl}/export/utilaje`, { params, responseType: 'blob' });
  }
}
