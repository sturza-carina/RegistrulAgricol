import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Parcela } from '../models/parcela.model';

@Injectable({
  providedIn: 'root'
})
export class ParcelaService {
  private apiUrl = '/api/parcele';

  constructor(private http: HttpClient) {}

  getAllParcele(): Observable<Parcela[]> {
    return this.http.get<Parcela[]>(this.apiUrl);
  }

  getParcele(terenId: number): Observable<Parcela[]> {
    return this.http.get<Parcela[]>(`${this.apiUrl}/teren/${terenId}`);
  }

  createParcela(terenId: number, parcela: Parcela): Observable<Parcela> {
    return this.http.post<Parcela>(`${this.apiUrl}/teren/${terenId}`, parcela);
  }

  updateParcela(id: number, parcela: Parcela): Observable<Parcela> {
    return this.http.put<Parcela>(`${this.apiUrl}/${id}`, parcela);
  }

  deleteParcela(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
