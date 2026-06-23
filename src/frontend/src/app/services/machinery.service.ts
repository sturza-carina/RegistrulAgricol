import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Machinery } from '../models/machinery.model';

@Injectable({
  providedIn: 'root'
})
export class MachineryService {
  private readonly apiUrl = '/api/machinery';

  constructor(private http: HttpClient) {}

  getMachineryByGospodarie(gospodarieId: number): Observable<Machinery[]> {
    return this.http.get<Machinery[]>(`${this.apiUrl}/gospodarie/${gospodarieId}`);
  }

  create(machinery: Machinery): Observable<Machinery> {
    return this.http.post<Machinery>(this.apiUrl, machinery);
  }

  update(id: number, machinery: Machinery): Observable<Machinery> {
    return this.http.put<Machinery>(`${this.apiUrl}/${id}`, machinery);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
