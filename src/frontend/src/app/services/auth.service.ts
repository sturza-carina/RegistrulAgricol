import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';

export interface JwtResponse {
  token: string;           // token-ul (local sau Keycloak access_token)
  id: number;
  username: string;
  role: string;
  tenantId: string;
  tokenType?: 'local' | 'keycloak';
}

// ── Keycloak config ────────────────────────────────────────────────────────────
const KEYCLOAK_URL    = 'http://localhost:8081';
const KEYCLOAK_REALM  = 'RegistruAgricol';
const KEYCLOAK_CLIENT = 'frontend-client';

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

  // ── Login local ────────────────────────────────────────────────────────────

  login(credentials: any): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${this.apiUrl}/signin`, credentials)
      .pipe(
        tap(user => {
          const userWithType = { ...user, tokenType: 'local' as const };
          localStorage.setItem('currentUser', JSON.stringify(userWithType));
          this.currentUserSubject.next(userWithType);
        })
      );
  }

  // ── Login Keycloak (PKCE) ──────────────────────────────────────────────────

  loginWithKeycloak(): void {
    const verifier   = this.generateCodeVerifier();
    const state      = crypto.randomUUID();

    sessionStorage.setItem('pkce_verifier', verifier);
    sessionStorage.setItem('pkce_state',    state);

    this.generateCodeChallenge(verifier).then(challenge => {
      const redirectUri = `${window.location.origin}/keycloak-callback`;
      const params = new URLSearchParams({
        client_id:             KEYCLOAK_CLIENT,
        redirect_uri:          redirectUri,
        response_type:         'code',
        scope:                 'openid profile',
        code_challenge:        challenge,
        code_challenge_method: 'S256',
        state
      });

      window.location.href =
        `${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/auth?${params}`;
    });
  }

  async handleKeycloakCallback(code: string): Promise<void> {
    const verifier   = sessionStorage.getItem('pkce_verifier') || '';
    const redirectUri = `${window.location.origin}/keycloak-callback`;

    sessionStorage.removeItem('pkce_verifier');
    sessionStorage.removeItem('pkce_state');

    const body = new URLSearchParams({
      grant_type:    'authorization_code',
      client_id:     KEYCLOAK_CLIENT,
      code,
      redirect_uri:  redirectUri,
      code_verifier: verifier
    });

    const response = await fetch(
      `${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token`,
      {
        method:  'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body:    body.toString()
      }
    );

    if (!response.ok) {
      throw new Error('Keycloak token exchange failed: ' + response.status);
    }

    const tokens = await response.json();
    const accessToken: string = tokens.access_token;

    // Decodifică JWT payload (fără verificare — verificarea o face backend-ul)
    const payload = JSON.parse(atob(accessToken.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));

    const user: JwtResponse = {
      token:     accessToken,
      id:        0,
      username:  payload.preferred_username || payload.sub,
      role:      payload.role || 'ROLE_USER',
      tenantId:  payload.tenant_id || '',
      tokenType: 'keycloak'
    };

    localStorage.setItem('currentUser', JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  // ── Logout ─────────────────────────────────────────────────────────────────

  logout() {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
    this.stopImpersonation();
  }

  // ── Impersonare tenant (super admin) ───────────────────────────────────────

  setImpersonation(tenantId: string) {
    this.impersonatedTenantSubject.next(tenantId);
  }

  stopImpersonation() {
    this.impersonatedTenantSubject.next(null);
  }

  get currentTenantId(): string {
    return this.impersonatedTenantSubject.value || this.currentUserSubject.value?.tenantId || '';
  }

  // ── PKCE helpers ───────────────────────────────────────────────────────────

  private generateCodeVerifier(): string {
    const array = new Uint8Array(32);
    crypto.getRandomValues(array);
    return btoa(String.fromCharCode(...array))
      .replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
  }

  private async generateCodeChallenge(verifier: string): Promise<string> {
    const encoder = new TextEncoder();
    const data    = encoder.encode(verifier);
    const digest  = await crypto.subtle.digest('SHA-256', data);
    return btoa(String.fromCharCode(...new Uint8Array(digest)))
      .replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
  }
}
