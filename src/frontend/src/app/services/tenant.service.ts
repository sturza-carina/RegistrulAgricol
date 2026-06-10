import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Tenant {
  id: string;
  name: string;
  schemaName: string;
  createdAt: string;
  active: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class TenantService {
  private apiUrl = '/api/tenants';

  constructor(private http: HttpClient) {}

  getTenants(): Observable<Tenant[]> {
    return this.http.get<Tenant[]>(this.apiUrl);
  }

  getTenantById(id: string): Observable<Tenant> {
    return this.http.get<Tenant>(`${this.apiUrl}/${id}`);
  }

  createTenant(name: string): Observable<Tenant> {
    return this.http.post<Tenant>(this.apiUrl, { name });
  }

  updateTenant(id: string, name: string, active: boolean): Observable<Tenant> {
    return this.http.put<Tenant>(`${this.apiUrl}/${id}`, { name, active });
  }

  deleteTenant(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { responseType: 'text' as 'json' });
  }
}
