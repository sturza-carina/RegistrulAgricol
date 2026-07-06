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
    this.checkSession();
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  public isLoggedIn(): boolean {
    return !!this.currentUserValue;
  }

  checkSession() {
    this.http.get<User>('/api/auth/me').subscribe({
      next: (user) => {
        if (user && user.role === 'CETATEAN') {
          this.currentUserSubject.next(user);
        } else {
          this.currentUserSubject.next(null);
        }
      },
      error: () => this.currentUserSubject.next(null)
    });
  }

  login(email: string, parola: string): Observable<any> {
    return this.http.post<User>(`${this.apiUrl}/login`, { email, parola }).pipe(
      tap(user => {
        if (user && user.role === 'CETATEAN') {
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
    this.http.post('/api/auth/signout', {}, { responseType: 'text' }).subscribe({
      next: () => {
        this.currentUserSubject.next(null);
        this.router.navigate(['/login']);
      },
      error: () => {
        this.currentUserSubject.next(null);
        this.router.navigate(['/login']);
      }
    });
  }
}
