import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';
import { Router } from '@angular/router';

export interface User {
  id: number;
  username: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = '/api/public/cetatean';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {
    const userStr = localStorage.getItem('cetateanUser');
    if (userStr) {
      try {
        const parsed = JSON.parse(userStr);
        if (parsed && parsed.role === 'CETATEAN') {
          this.currentUserSubject.next(parsed);
        }
      } catch (e) {
        console.error('Failed to parse user from localStorage', e);
      }
    }
    this.checkSession();
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  public isLoggedIn(): boolean {
    return !!this.currentUserValue;
  }

  checkSession() {
    this.http.get<any>('/api/public/cetatean/me').subscribe({
      next: (cetatean) => {
        if (cetatean && cetatean.id) {
          const normalizedUser: User = {
            id: cetatean.id,
            username: cetatean.email,
            role: 'CETATEAN'
          };
          localStorage.setItem('cetateanUser', JSON.stringify(normalizedUser));
          this.currentUserSubject.next(normalizedUser);
        } else {
          localStorage.removeItem('cetateanUser');
          this.currentUserSubject.next(null);
        }
      },
      error: () => {
        localStorage.removeItem('cetateanUser');
        this.currentUserSubject.next(null);
      }
    });
  }

  login(email: string, parola: string): Observable<any> {
    return this.http.post<User>(`${this.apiUrl}/login`, { email, parola }).pipe(
      tap(user => {
        if (user && user.role === 'CETATEAN') {
          localStorage.setItem('cetateanUser', JSON.stringify(user));
          this.currentUserSubject.next(user);
        }
      }),
      catchError(err => throwError(() => err))
    );
  }

  register(data: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, data, { responseType: 'text' }).pipe(
      catchError(err => throwError(() => err))
    );
  }

  logout() {
    this.http.post(`${this.apiUrl}/signout`, {}, { responseType: 'text' }).subscribe({
      next: () => {
        localStorage.removeItem('cetateanUser');
        this.currentUserSubject.next(null);
        this.router.navigate(['/']);
      },
      error: () => {
        localStorage.removeItem('cetateanUser');
        this.currentUserSubject.next(null);
        this.router.navigate(['/']);
      }
    });
  }
}
