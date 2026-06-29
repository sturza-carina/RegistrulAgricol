import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';

export interface UserInfo {
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
  currentUserSubject = new BehaviorSubject<UserInfo | null>(null);
  public currentUser = this.currentUserSubject.asObservable();
  
  impersonatedTenantSubject = new BehaviorSubject<string | null>(null);
  public impersonatedTenant = this.impersonatedTenantSubject.asObservable();

  constructor(private http: HttpClient) {
    const userStr = localStorage.getItem('currentUser');
    if (userStr) {
      const parsed = JSON.parse(userStr);
      if (parsed && parsed.id) {
        this.currentUserSubject.next(parsed);
      }
    }
  }

  login(credentials: any): Observable<UserInfo> {
    return this.http.post<UserInfo>(`${this.apiUrl}/signin`, credentials)
      .pipe(
        tap(user => {
          localStorage.setItem('currentUser', JSON.stringify(user));
          this.currentUserSubject.next(user);
        })
      );
  }

  logout() {
    this.http.post(`${this.apiUrl}/signout`, {}, { responseType: 'text' }).subscribe({
      next: () => {
        this.clearLocalSession();
      },
      error: (err) => {
        console.error('Logout failed', err);
        this.clearLocalSession();
      }
    });
  }

  private clearLocalSession() {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
    this.impersonatedTenantSubject.next(null);
  }

  setImpersonation(tenantId: string): Observable<UserInfo> {
    return this.http.post<UserInfo>(`${this.apiUrl}/impersonate`, { tenantId })
      .pipe(
        tap(user => {
          localStorage.setItem('currentUser', JSON.stringify(user));
          this.currentUserSubject.next(user);
          this.impersonatedTenantSubject.next(tenantId);
        })
      );
  }

  stopImpersonation(): Observable<UserInfo> {
    return this.http.post<UserInfo>(`${this.apiUrl}/impersonate`, { tenantId: 'public' })
      .pipe(
        tap(user => {
          localStorage.setItem('currentUser', JSON.stringify(user));
          this.currentUserSubject.next(user);
          this.impersonatedTenantSubject.next(null);
        })
      );
  }

  get currentTenantId(): string {
    return this.impersonatedTenantSubject.value || this.currentUserSubject.value?.tenantId || '';
  }
}

