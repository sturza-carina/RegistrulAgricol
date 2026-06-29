import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CategorieFolosinta } from '../models/categorie-folosinta.model';
import { PaginatedResponse } from '../models/paginated-response.model';

@Injectable({
  providedIn: 'root'
})
export class CategorieFolosintaService {
  private apiUrl = '/api';

  constructor(private http: HttpClient) {}

  getCategoriiForTeren(terenId: number, page: number = 0, size: number = 20): Observable<PaginatedResponse<CategorieFolosinta>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PaginatedResponse<CategorieFolosinta>>(`${this.apiUrl}/terenuri/${terenId}/categorii-folosinta`, { params });
  }

  createCategorie(terenId: number, categorie: CategorieFolosinta): Observable<CategorieFolosinta> {
    return this.http.post<CategorieFolosinta>(`${this.apiUrl}/terenuri/${terenId}/categorii-folosinta`, categorie);
  }

  updateCategorie(id: number, categorie: CategorieFolosinta): Observable<CategorieFolosinta> {
    return this.http.put<CategorieFolosinta>(`${this.apiUrl}/categorii-folosinta/${id}`, categorie);
  }

  deleteCategorie(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/categorii-folosinta/${id}`);
  }
}
