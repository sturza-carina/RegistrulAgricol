import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CatalogPpp } from '../models/catalog-ppp.model';
import { PaginatedResponse } from '../models/paginated-response.model';

@Injectable({
  providedIn: 'root'
})
export class CatalogPppService {

  private apiUrl = '/api/catalog/ppp';

  constructor(private http: HttpClient) { }

  getCatalog(query?: string, page: number = 0, size: number = 20): Observable<PaginatedResponse<CatalogPpp>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (query) {
      params = params.set('query', query);
    }
    return this.http.get<PaginatedResponse<CatalogPpp>>(this.apiUrl, { params });
  }

  getById(id: number): Observable<CatalogPpp> {
    return this.http.get<CatalogPpp>(`${this.apiUrl}/${id}`);
  }

  create(data: CatalogPpp): Observable<CatalogPpp> {
    return this.http.post<CatalogPpp>(this.apiUrl, data);
  }

  update(id: number, data: CatalogPpp): Observable<CatalogPpp> {
    return this.http.put<CatalogPpp>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
