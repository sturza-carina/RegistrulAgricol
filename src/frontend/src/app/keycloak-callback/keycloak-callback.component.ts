import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-keycloak-callback',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div style="
      display: flex; flex-direction: column;
      align-items: center; justify-content: center;
      height: 100vh; font-family: 'DM Sans', sans-serif;
      background: #f7faf8; color: #4a6b57; gap: 16px;
    ">
      <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#1a6b3c" stroke-width="2">
        <path d="M12 2L2 7l10 5 10-5-10-5z"/>
        <path d="M2 17l10 5 10-5"/>
        <path d="M2 12l10 5 10-5"/>
      </svg>
      <span style="font-size: 15px; font-weight: 500;">
        {{ error ? error : 'Se procesează autentificarea Keycloak...' }}
      </span>
      <span *ngIf="error" style="font-size: 13px; color: #b91c1c;">
        Vă rugăm să încercați din nou.
        <a href="/login" style="color: #1a6b3c; text-decoration: underline;">Înapoi la login</a>
      </span>
    </div>
  `
})
export class KeycloakCallbackComponent implements OnInit {
  error: string = '';

  constructor(private router: Router, private authService: AuthService) {}

  async ngOnInit(): Promise<void> {
    const params = new URLSearchParams(window.location.search);
    const code   = params.get('code');
    const errParam = params.get('error');

    if (errParam) {
      this.error = 'Autentificarea Keycloak a fost anulată sau a eșuat.';
      return;
    }

    if (!code) {
      this.error = 'Cod de autorizare lipsă.';
      return;
    }

    try {
      await this.authService.handleKeycloakCallback(code);
      const user = this.authService.currentUserSubject.value;

      if (user?.role === 'ROLE_SUPER_ADMIN') {
        this.router.navigate(['/super-admin']);
      } else if (user?.role === 'ROLE_ADMIN') {
        this.router.navigate(['/tenant-admin']);
      } else {
        this.router.navigate(['/gospodarii']);
      }
    } catch (e: any) {
      this.error = 'Eroare la procesarea token-ului Keycloak: ' + (e?.message || 'necunoscută');
    }
  }
}
