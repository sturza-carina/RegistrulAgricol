import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UatService {
  private readonly apiUrl = '/api/uats';

  constructor(private http: HttpClient) {}

  getJudete(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/judete`);
  }

  getLocalitatiByJudet(judet: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/localitati`, {
      params: { judet }
    });
  }

  getTenantUats(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/tenant`);
  }
}