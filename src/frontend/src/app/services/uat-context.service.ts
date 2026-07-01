import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject } from 'rxjs';
import { take } from 'rxjs/operators';
import { AuthService } from './auth.service';
import { Uat } from '../models/gospodarie.model';

@Injectable({ providedIn: 'root' })
export class UatContextService {
  private availableUatsSubject = new BehaviorSubject<Uat[]>([]);
  private activeUatSubject = new BehaviorSubject<Uat | null>(null);
  private uatsLoaded = false; // flag ca sa nu reincarcam la fiecare emit

  availableUats$ = this.availableUatsSubject.asObservable();
  activeUat$ = this.activeUatSubject.asObservable();

  constructor(private http: HttpClient, private authService: AuthService) {
    this.authService.impersonatedTenant.subscribe(tenantId => {
      const user = this.authService.currentUserSubject.value;
      if (!user) {
        this.reset();
        return;
      }

      if (tenantId) {
        this.uatsLoaded = true;
        this.loadUats(tenantId);
      } else {
        if (user.role !== 'ROLE_SUPER_ADMIN') {
          this.uatsLoaded = true;
          this.loadUats(user.tenantId || '');
        } else {
          this.reset();
        }
      }
    });

    this.authService.currentUser.subscribe(user => {
      if (!user) {
        this.reset();
        return;
      }
      const impersonated = this.authService.impersonatedTenantSubject.value;
      if (!impersonated && user.role !== 'ROLE_SUPER_ADMIN' && !this.uatsLoaded) {
        this.uatsLoaded = true;
        this.loadUats(user.tenantId || '');
      }
    });
  }

  private loadUats(tenantId: string): void {
    this.http.get<Uat[]>(`/api/uats/tenant`).subscribe({
      next: (uats) => {
        this.availableUatsSubject.next(uats);

        const savedCode = localStorage.getItem('activeUatCode');

        const savedUat = savedCode
          ? uats.find(u => u.codSiruta === savedCode)
          : null;

        if (!this.activeUatSubject.getValue()) {
          this.setActiveUat(savedUat ?? uats[0] ?? null);
        }
      },
      error: () => this.reset()
    });
  }

  setActiveUat(uat: Uat | null): void {
    this.activeUatSubject.next(uat);
    if (uat) {
      localStorage.setItem('activeUatCode', uat.codSiruta);
    } else {
      localStorage.removeItem('activeUatCode');
    }
  }

  getActiveUat(): Uat | null {
    return this.activeUatSubject.getValue();
  }

  private reset(): void {
    this.availableUatsSubject.next([]);
    this.activeUatSubject.next(null);
    this.uatsLoaded = false;
    localStorage.removeItem('activeUatCode');
  }
}
