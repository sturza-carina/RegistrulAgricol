import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Client, Message } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { UATS } from '../../data/uats.data';

@Component({
  selector: 'app-stadiu',
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './stadiu.html',
  styleUrl: './stadiu.css',
})
export class Stadiu implements OnInit, OnDestroy {
  codCerere: string = '';
  uatId: string = '';
  judete: string[] = [];
  localitati: any[] = [];
  toateLocalitatile: any[] = [];
  selectedJudet: string = '';
  
  cerereStatus: any = null;
  isChecking = false;
  error: string | null = null;
  
  private stompClient: Client | null = null;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.toateLocalitatile = UATS;
    this.judete = [...new Set(UATS.filter((l: any) => l && l.judet).map((l: any) => l.judet))].sort((a: any, b: any) => a.localeCompare(b));
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

  ngOnDestroy() {
    if (this.stompClient) {
      this.stompClient.deactivate();
    }
  }

  checkStatus() {
    this.isChecking = true;
    this.error = null;
    this.cerereStatus = null;
    
    // Disconnect old socket if any
    if (this.stompClient) {
      this.stompClient.deactivate();
    }

    this.http.get<any>(`/api/public/cereri/${this.codCerere}?uatId=${this.uatId}`).subscribe({
      next: (res) => {
        this.cerereStatus = res;
        this.isChecking = false;
        this.connectToWebSocket();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isChecking = false;
        this.error = 'Cererea nu a fost găsită. Verificați codul și localitatea.';
        this.cdr.detectChanges();
      }
    });
  }
  
  connectToWebSocket() {
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('/api/ws'),
      onConnect: () => {
        this.stompClient!.subscribe('/topic/cereri/' + this.codCerere, (message: Message) => {
          const updatedCerere = JSON.parse(message.body);
          this.cerereStatus = updatedCerere;
          this.cdr.detectChanges();
        });
      }
    });
    this.stompClient.activate();
  }
}
