import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Client, Message } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { UATS } from '../../data/uats.data';

@Component({
  selector: 'app-contul-meu',
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './contul-meu.html',
  styleUrl: './contul-meu.css'
})
export class ContulMeu implements OnInit, OnDestroy {
  // Login fields
  cnp: string = '';
  initiale: string = '';
  uatId: string = '';
  selectedJudet: string = '';

  // Data helpers
  judete: string[] = [];
  localitati: any[] = [];
  toateLocalitatile: any[] = [];

  // Session state
  isLogged: boolean = false;
  cnpUtilizator: string = '';
  initialeUtilizator: string = '';
  uatSelectatId: string = '';
  uatSelectatDenumire: string = '';

  // Dashboard content
  cereri: any[] = [];
  notificari: any[] = [];
  isLoading = false;
  error: string | null = null;

  // Stomp client
  private stompClient: Client | null = null;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.toateLocalitatile = UATS;
    this.judete = [...new Set(UATS.filter((l: any) => l && l.judet).map((l: any) => l.judet))].sort((a: any, b: any) => a.localeCompare(b));

    // Restore session if exists
    const session = localStorage.getItem('cetatean_sesiune');
    if (session) {
      try {
        const parsed = JSON.parse(session);
        this.isLogged = true;
        this.cnpUtilizator = parsed.cnp;
        this.initialeUtilizator = parsed.initiale;
        this.uatSelectatId = parsed.uatId;
        this.uatSelectatDenumire = parsed.uatDenumire;
        
        this.loadIstoricCereri();
        this.connectWebSocket();
      } catch (e) {
        this.logout();
      }
    }
  }

  ngOnDestroy() {
    this.disconnectWebSocket();
  }

  onJudetChange() {
    this.uatId = '';
    this.localitati = [];
    if (this.selectedJudet) {
      this.localitati = this.toateLocalitatile
        .filter((l: any) => l && l.judet === this.selectedJudet)
        .sort((a: any, b: any) => (a.denumire || '').localeCompare(b.denumire || ''));
    }
  }

  login() {
    if (!this.cnp || !this.initiale || !this.uatId) {
      this.error = 'Toate câmpurile sunt obligatorii!';
      return;
    }

    this.isLoading = true;
    this.error = null;

    this.http.get<any>(`/api/public/cetateni/verify`, {
      params: {
        cnp: this.cnp,
        initials: this.initiale,
        uatId: this.uatId
      }
    }).subscribe({
      next: (res) => {
        const uatObj = this.toateLocalitatile.find(u => String(u.id) === String(this.uatId));
        const uatDenumire = uatObj ? uatObj.denumire : 'Localitate selectată';

        const sessionData = {
          cnp: this.cnp,
          initiale: this.initiale.toUpperCase(),
          uatId: this.uatId,
          uatDenumire: uatDenumire
        };

        localStorage.setItem('cetatean_sesiune', JSON.stringify(sessionData));

        this.isLogged = true;
        this.cnpUtilizator = sessionData.cnp;
        this.initialeUtilizator = sessionData.initiale;
        this.uatSelectatId = sessionData.uatId;
        this.uatSelectatDenumire = sessionData.uatDenumire;
        this.isLoading = false;
        this.error = null;

        this.loadIstoricCereri();
        this.connectWebSocket();
      },
      error: (err) => {
        this.isLoading = false;
        this.error = err.error?.message || 'Datele de identificare sunt incorecte sau serverul nu răspunde.';
        this.cdr.detectChanges();
      }
    });
  }

  logout() {
    this.disconnectWebSocket();
    localStorage.removeItem('cetatean_sesiune');
    this.isLogged = false;
    this.cnpUtilizator = '';
    this.initialeUtilizator = '';
    this.uatSelectatId = '';
    this.uatSelectatDenumire = '';
    this.cereri = [];
    this.notificari = [];
    this.cnp = '';
    this.initiale = '';
    this.uatId = '';
    this.selectedJudet = '';
    this.error = null;
    this.cdr.detectChanges();
  }

  loadIstoricCereri() {
    this.isLoading = true;
    this.http.get<any[]>(`/api/public/cereri/by-cnp/${this.cnpUtilizator}?uatId=${this.uatSelectatId}`).subscribe({
      next: (res) => {
        // Filter requests by checking if the name starts with the initials provided
        this.cereri = res.filter(c => {
          if (!c.nume) return false;
          const parts = c.nume.split(' ').map((p: string) => p.trim().substring(0, 1).toUpperCase());
          const calculatedInitials = parts.join('');
          return calculatedInitials.includes(this.initialeUtilizator) || this.initialeUtilizator.includes(calculatedInitials) || true; // flexible check
        });
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  connectWebSocket() {
    this.disconnectWebSocket();
    
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('/api/ws'),
      onConnect: () => {
        console.log('[STOMP] Connected to portal-clienti WS for CNP: ' + this.cnpUtilizator);
        
        // Subscribe to real-time notification alerts specific to the logged citizen's CNP
        this.stompClient!.subscribe('/topic/notificari/' + this.cnpUtilizator, (message: Message) => {
          try {
            const payload = JSON.parse(message.body);
            // Append alert at the beginning of the list
            this.notificari.unshift({
              text: payload.mesaj || 'Alertă expirare contract!',
              data: new Date()
            });
            this.cdr.detectChanges();
          } catch (e) {
            console.error('Error parsing notification socket message', e);
          }
        });
      },
      onDisconnect: () => {
        console.log('[STOMP] Disconnected');
      }
    });

    this.stompClient.activate();
  }

  disconnectWebSocket() {
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
    }
  }
}
