import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cladire } from '../models/cladire.model';

@Injectable({
  providedIn: 'root'
})
export class CladireService {
  private apiUrl = `/api/gospodarii`;

  constructor(private http: HttpClient) {}

  getCladiri(gospodarieId: number): Observable<Cladire[]> {
    return this.http.get<Cladire[]>(`${this.apiUrl}/${gospodarieId}/cladiri`, { withCredentials: true });
  }

  createCladire(gospodarieId: number, cladire: Cladire): Observable<Cladire> {
    return this.http.post<Cladire>(`${this.apiUrl}/${gospodarieId}/cladiri`, cladire, { withCredentials: true });
  }

  updateCladire(gospodarieId: number, id: number, cladire: Cladire): Observable<Cladire> {
    return this.http.put<Cladire>(`${this.apiUrl}/${gospodarieId}/cladiri/${id}`, cladire, { withCredentials: true });
  }

  deleteCladire(gospodarieId: number, id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${gospodarieId}/cladiri/${id}`, { withCredentials: true });
  }
}
