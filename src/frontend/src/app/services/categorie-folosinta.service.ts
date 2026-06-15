import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CategorieFolosinta } from '../models/categorie-folosinta.model';

@Injectable({
  providedIn: 'root'
})
export class CategorieFolosintaService {
  private apiUrl = '/api';

  constructor(private http: HttpClient) {}

  getCategoriiForTeren(terenId: number): Observable<CategorieFolosinta[]> {
    return this.http.get<CategorieFolosinta[]>(`${this.apiUrl}/terenuri/${terenId}/categorii-folosinta`);
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
