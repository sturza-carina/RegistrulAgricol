import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { VitaDeVie } from '../models/vita-de-vie.model';
import { PaginatedResponse } from '../models/paginated-response.model';

@Injectable({
  providedIn: 'root'
})
export class VitaDeVieService {

  private apiUrl = '/api/parcele';

  constructor(private http: HttpClient) { }

  getVitaDeVie(parcelaId: number, page: number = 0, size: number = 20): Observable<PaginatedResponse<VitaDeVie>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PaginatedResponse<VitaDeVie>>(`${this.apiUrl}/${parcelaId}/vita-de-vie`, { params });
  }

  createVita(parcelaId: number, data: VitaDeVie): Observable<VitaDeVie> {
    return this.http.post<VitaDeVie>(`${this.apiUrl}/${parcelaId}/vita-de-vie`, data);
  }

  updateVita(parcelaId: number, vitaId: number, data: VitaDeVie): Observable<VitaDeVie> {
    return this.http.put<VitaDeVie>(`${this.apiUrl}/${parcelaId}/vita-de-vie/${vitaId}`, data);
  }

  deleteVita(parcelaId: number, vitaId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${parcelaId}/vita-de-vie/${vitaId}`);
  }
}
