import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Client, Message } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { UATS } from '../../data/uats.data';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-contul-meu',
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './contul-meu.html',
  styleUrl: './contul-meu.css'
})
export class ContulMeu implements OnInit, OnDestroy {
  // Profile data
  profile: any = null;
  
  // Data helpers
  judete: string[] = [];
  localitati: any[] = [];
  toateLocalitatile: any[] = [];

  // Notifications
  notificari: any[] = [];
  
  isLoading = false;
  isSaving = false;
  successMessage: string | null = null;
  error: string | null = null;

  // Stomp client
  private stompClient: Client | null = null;

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.toateLocalitatile = UATS;
    this.judete = [...new Set(UATS.filter((l: any) => l && l.judet).map((l: any) => l.judet))].sort((a: any, b: any) => a.localeCompare(b));

    this.loadProfile();
  }

  ngOnDestroy() {
    this.disconnectWebSocket();
  }

  onJudetChange() {
    this.profile.localitate = '';
    this.updateLocalitati();
  }
  
  updateLocalitati() {
    this.localitati = [];
    if (this.profile && this.profile.judet) {
      this.localitati = this.toateLocalitatile
        .filter((l: any) => l && l.judet === this.profile.judet)
        .sort((a: any, b: any) => (a.denumire || '').localeCompare(b.denumire || ''));
    }
  }

  loadProfile() {
    this.isLoading = true;
    this.error = null;
    
    this.http.get<any>('/api/public/cetatean/me').subscribe({
      next: (res) => {
        this.profile = res;
        this.updateLocalitati();
        this.isLoading = false;
        
        if (this.profile.cnp) {
           this.connectWebSocket(this.profile.cnp);
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isLoading = false;
        this.error = 'A apărut o eroare la încărcarea profilului.';
        this.cdr.detectChanges();
      }
    });
  }

  salveazaDate() {
    if (!this.profile) return;
    
    // Basic validation
    if (!this.profile.nume || !this.profile.prenume || !this.profile.telefon || !this.profile.judet || !this.profile.localitate || !this.profile.strada || !this.profile.numar) {
      this.error = 'Te rugăm să completezi toate câmpurile obligatorii.';
      return;
    }
    
    this.isSaving = true;
    this.error = null;
    this.successMessage = null;
    
    const payload = {
      nume: this.profile.nume,
      prenume: this.profile.prenume,
      telefon: this.profile.telefon,
      judet: this.profile.judet,
      localitate: this.profile.localitate,
      strada: this.profile.strada,
      numar: this.profile.numar,
      bloc: this.profile.bloc,
      scara: this.profile.scara,
      etaj: this.profile.etaj,
      apartament: this.profile.apartament
    };
    
    this.http.put<any>('/api/public/cetatean/me', payload).subscribe({
      next: (res) => {
        this.profile = res;
        this.isSaving = false;
        this.successMessage = 'Datele au fost salvate cu succes!';
        setTimeout(() => this.successMessage = null, 5000);
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isSaving = false;
        this.error = 'A apărut o eroare la salvarea datelor.';
        this.cdr.detectChanges();
      }
    });
  }

  connectWebSocket(cnp: string) {
    this.disconnectWebSocket();
    
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('/api/ws'),
      onConnect: () => {
        console.log('[STOMP] Connected to portal-cetateni WS for CNP: ' + cnp);
        
        // Subscribe to real-time notification alerts
        this.stompClient!.subscribe('/topic/notificari/' + cnp, (message: Message) => {
          try {
            const payload = JSON.parse(message.body);
            this.notificari.unshift({
              text: payload.mesaj || 'Notificare nouă!',
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
