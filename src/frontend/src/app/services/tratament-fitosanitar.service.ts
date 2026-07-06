import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TratamentFitosanitar } from '../models/tratament-fitosanitar.model';
import { PaginatedResponse } from '../models/paginated-response.model';

@Injectable({
  providedIn: 'root'
})
export class TratamentFitosanitarService {

  private apiUrl = '/api/tratamente';

  constructor(private http: HttpClient) { }

  getTratamente(parcelaId?: number, page: number = 0, size: number = 20): Observable<PaginatedResponse<TratamentFitosanitar>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (parcelaId) {
      params = params.set('parcelaId', parcelaId.toString());
    }
    return this.http.get<PaginatedResponse<TratamentFitosanitar>>(this.apiUrl, { params });
  }

  getById(id: number): Observable<TratamentFitosanitar> {
    return this.http.get<TratamentFitosanitar>(`${this.apiUrl}/${id}`);
  }

  create(data: TratamentFitosanitar): Observable<TratamentFitosanitar> {
    return this.http.post<TratamentFitosanitar>(this.apiUrl, data);
  }

  update(id: number, data: TratamentFitosanitar): Observable<TratamentFitosanitar> {
    return this.http.put<TratamentFitosanitar>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  downloadPdf(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/export/pdf`, { responseType: 'blob' });
  }
}
