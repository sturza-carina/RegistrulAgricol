import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CatalogIngrasaminte } from '../models/catalog-ingrasaminte.model';
import { PaginatedResponse } from '../models/paginated-response.model';

@Injectable({
  providedIn: 'root'
})
export class CatalogIngrasaminteService {

  private apiUrl = '/api/catalog/ingrasaminte';

  constructor(private http: HttpClient) { }

  getCatalog(query?: string, page: number = 0, size: number = 20): Observable<PaginatedResponse<CatalogIngrasaminte>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (query) {
      params = params.set('query', query);
    }
    return this.http.get<PaginatedResponse<CatalogIngrasaminte>>(this.apiUrl, { params });
  }

  getById(id: number): Observable<CatalogIngrasaminte> {
    return this.http.get<CatalogIngrasaminte>(`${this.apiUrl}/${id}`);
  }

  create(data: CatalogIngrasaminte): Observable<CatalogIngrasaminte> {
    return this.http.post<CatalogIngrasaminte>(this.apiUrl, data);
  }

  update(id: number, data: CatalogIngrasaminte): Observable<CatalogIngrasaminte> {
    return this.http.put<CatalogIngrasaminte>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
