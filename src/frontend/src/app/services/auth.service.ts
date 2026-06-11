import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';

export interface JwtResponse {
  token: string;
  id: number;
  username: string;
  role: string;
  tenantId: string;
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
      this.currentUserSubject.next(JSON.parse(userStr));
    }
  }

  login(credentials: any): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${this.apiUrl}/signin`, credentials)
      .pipe(
        tap(user => {
          localStorage.setItem('currentUser', JSON.stringify(user));
          this.currentUserSubject.next(user);
        })
      );
  }

  logout() {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
    this.stopImpersonation();
  }

  setImpersonation(tenantId: string) {
    this.impersonatedTenantSubject.next(tenantId);
  }

  stopImpersonation() {
    this.impersonatedTenantSubject.next(null);
  }

  get currentTenantId(): string {
    return this.impersonatedTenantSubject.value || this.currentUserSubject.value?.tenantId || '';
  }
}
