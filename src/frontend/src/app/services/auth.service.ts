import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';

export interface JwtResponse {
  token: string;
  // Extracted from token locally
  id?: number;
  username?: string;
  role?: string;
  tenantId?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = '/api/auth';
  currentUserSubject = new BehaviorSubject<JwtResponse | null>(null);
  public currentUser = this.currentUserSubject.asObservable();
  
  impersonatedTenantSubject = new BehaviorSubject<string | null>(null);
  public impersonatedTenant = this.impersonatedTenantSubject.asObservable();

  constructor(private http: HttpClient) {
    const userStr = localStorage.getItem('currentUser');
    if (userStr) {
      const parsed = JSON.parse(userStr);
      if (parsed && parsed.token) {
        this.currentUserSubject.next(this.decodeToken(parsed.token));
      }
    }
  }

  private decodeToken(token: string): JwtResponse {
    try {
      const payloadBase64 = token.split('.')[1];
      const payloadJson = atob(payloadBase64.replace(/-/g, '+').replace(/_/g, '/'));
      const claims = JSON.parse(payloadJson);
      return {
        token,
        id: claims.userId,
        username: claims.sub,
        role: claims.role,
        tenantId: claims.tenantId
      };
    } catch (e) {
      console.error('Error decoding JWT token', e);
      return { token };
    }
  }

  login(credentials: any): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${this.apiUrl}/signin`, credentials)
      .pipe(
        tap(response => {
          const user = this.decodeToken(response.token);
          localStorage.setItem('currentUser', JSON.stringify({ token: response.token }));
          this.currentUserSubject.next(user);
        })
      );
  }

  logout() {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
    this.impersonatedTenantSubject.next(null);
  }

  setImpersonation(tenantId: string): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${this.apiUrl}/impersonate`, { tenantId })
      .pipe(
        tap(response => {
          const user = this.decodeToken(response.token);
          localStorage.setItem('currentUser', JSON.stringify({ token: response.token }));
          this.currentUserSubject.next(user);
          this.impersonatedTenantSubject.next(tenantId);
        })
      );
  }

  stopImpersonation(): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${this.apiUrl}/impersonate`, { tenantId: 'public' })
      .pipe(
        tap(response => {
          const user = this.decodeToken(response.token);
          localStorage.setItem('currentUser', JSON.stringify({ token: response.token }));
          this.currentUserSubject.next(user);
          this.impersonatedTenantSubject.next(null);
        })
      );
  }

  get currentTenantId(): string {
    return this.impersonatedTenantSubject.value || this.currentUserSubject.value?.tenantId || '';
  }
}
