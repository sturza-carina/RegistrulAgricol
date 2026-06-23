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
    this.authService.currentUser.subscribe(user => {
      if (!user) {
        this.reset();
        return;
      }
      // incarca UAT-urile doar prima data, nu la fiecare emit al currentUser
      if (!this.uatsLoaded && user.role !== 'ROLE_SUPER_ADMIN') {
        this.uatsLoaded = true;
        this.loadUats(user.tenantId);
      }
    });
  }

  private loadUats(tenantId: string): void {
    this.http.get<Uat[]>(`/api/uats?tenantId=${tenantId}`).subscribe({
      next: (uats) => {
        this.availableUatsSubject.next(uats);

        const savedCode = localStorage.getItem('activeUatCode');
        console.log('savedCode din localStorage:', savedCode);
        console.log('activeUat curent:', this.activeUatSubject.getValue());
        console.log('uats primite:', uats.map(u => u.codSiruta));

        const savedUat = savedCode
          ? uats.find(u => u.codSiruta === savedCode)
          : null;

        console.log('savedUat gasit:', savedUat);

        if (!this.activeUatSubject.getValue()) {
          this.setActiveUat(savedUat ?? uats[0] ?? null);
        }
      },
      error: () => this.reset()
    });
  }

  setActiveUat(uat: Uat | null): void {
    console.log('setActiveUat apelat cu:', uat?.codSiruta, new Error().stack);
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
    console.log('RESET APELAT!', new Error().stack);
    this.availableUatsSubject.next([]);
    this.activeUatSubject.next(null);
    this.uatsLoaded = false;
    localStorage.removeItem('activeUatCode');
  }
}
