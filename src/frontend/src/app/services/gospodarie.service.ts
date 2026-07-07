import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Gospodarie } from '../models/gospodarie.model';
import { PaginatedResponse } from '../models/paginated-response.model';

@Injectable({
  providedIn: 'root'
})
export class GospodarieService {
  private apiUrl = '/api/gospodarii';

  constructor(private http: HttpClient) {}

  getAllGospodarii(uatCode?: string, page: number = 0, size: number = 20): Observable<PaginatedResponse<Gospodarie>> {
    let params = new HttpParams();
    if (uatCode) {
      params = params.set('uatCode', uatCode);
    }
    params = params.set('page', page.toString());
    params = params.set('size', size.toString());
    return this.http.get<PaginatedResponse<Gospodarie>>(this.apiUrl, { params });
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

  generateAdeverintaRolAgricol(gospodarieId: number, persoanaId: number): Observable<Blob> {
    // Note: ensure API prefix handles '/api/v1' if not configured elsewhere
    return this.http.get(`/api/v1/adeverinte/rol-agricol/${gospodarieId}/persoana/${persoanaId}`, {
      responseType: 'blob'
    });
  }

  seteazaCapGospodarie(gospodarieId: number, persoanaId: number | null): Observable<void> {
    if (persoanaId === null) {
      return this.http.put<void>(`${this.apiUrl}/${gospodarieId}/cap-gospodarie`, null);
    }
    return this.http.put<void>(`${this.apiUrl}/${gospodarieId}/cap-gospodarie/${persoanaId}`, null);
  }

  getIstoricMembri(gospodarieId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${gospodarieId}/istoric-membri`);
  }

  adaugaEvenimentIstoric(gospodarieId: number, eveniment: any): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${gospodarieId}/istoric-membri`, eveniment);
  }

  updateEvenimentIstoric(gospodarieId: number, evenimentId: number, eveniment: any): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${gospodarieId}/istoric-membri/${evenimentId}`, eveniment);
  }
}
