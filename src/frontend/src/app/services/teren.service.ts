import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Teren } from '../models/teren.model';

@Injectable({
  providedIn: 'root'
})
export class TerenService {
  private apiUrl = '/api/terenuri';

  constructor(private http: HttpClient) {}

  getAllTerenuri(): Observable<Teren[]> {
    return this.http.get<Teren[]>(this.apiUrl);
  }

  getTerenByGospodarieId(gospodarieId: number): Observable<Teren> {
    return this.http.get<Teren>(`${this.apiUrl}/gospodarie/${gospodarieId}`);
  }

  getTerenById(id: number): Observable<Teren> {
    return this.http.get<Teren>(`${this.apiUrl}/${id}`);
  }

  createTeren(teren: Teren): Observable<Teren> {
    return this.http.post<Teren>(this.apiUrl, teren);
  }

  createTerenWithParcela(dto: any): Observable<Teren> {
    return this.http.post<Teren>(`${this.apiUrl}/with-parcela`, dto);
  }

  updateTeren(id: number, teren: Teren): Observable<Teren> {
    return this.http.put<Teren>(`${this.apiUrl}/${id}`, teren);
  }

  deleteTeren(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
