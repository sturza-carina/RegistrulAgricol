import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { LayoutComponent } from '../../components/layout/layout.component';
import { AuthService } from '../../services/auth.service';
import { Client, Message } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

@Component({
  selector: 'app-cereri-admin',
  standalone: true,
  imports: [CommonModule, LayoutComponent],
  templateUrl: './cereri-admin.component.html',
  styleUrl: './cereri-admin.component.css'
})
export class CereriAdminComponent implements OnInit, OnDestroy {
  cereri: any[] = [];
  selectedCerere: any = null;
  activeTab: 'PENDING' | 'RESOLVED' = 'PENDING';
  private stompClient: Client;

  constructor(private http: HttpClient, private authService: AuthService, private cdr: ChangeDetectorRef) {
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('/api/ws'),
      onConnect: () => {
        this.authService.currentUser.subscribe(user => {
          if (user && user.tenantId) {
            this.stompClient.subscribe('/topic/tenant/' + user.tenantId + '/cereri', (message: Message) => {
              const newCerere = JSON.parse(message.body);
              // Update local array if not already present
              if (!this.cereri.find(c => c.id === newCerere.id)) {
                this.cereri = [newCerere, ...this.cereri];
                this.cereri.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
                this.cdr.detectChanges();
              }
            });
          }
        });
      }
    });
  }

  ngOnInit() {
    this.loadCereri();
    this.stompClient.activate();
  }

  ngOnDestroy() {
    if (this.stompClient) {
      this.stompClient.deactivate();
    }
  }

  loadCereri() {
    this.http.get<any[]>('/api/admin/cereri').subscribe(res => {
      // Sort by date desc
      this.cereri = res.sort((a, b) => {
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
      });
      this.cdr.detectChanges();
    });
  }

  get filteredCereri() {
    if (this.activeTab === 'PENDING') {
      return this.cereri.filter(c => c.status === 'PENDING');
    } else {
      return this.cereri.filter(c => c.status !== 'PENDING');
    }
  }

  setTab(tab: 'PENDING' | 'RESOLVED') {
    this.activeTab = tab;
  }

  selectCerere(cerere: any) {
    this.selectedCerere = cerere;
  }

  closeModal() {
    this.selectedCerere = null;
  }

  acceptCerere() {
    if (!this.selectedCerere) return;
    this.http.put(`/api/admin/cereri/${this.selectedCerere.id}/status?status=ACCEPTED`, {}).subscribe(() => {
      this.selectedCerere.status = 'ACCEPTED';
      this.closeModal();
      this.loadCereri();
    });
  }

  declineCerere() {
    if (!this.selectedCerere) return;
    this.http.put(`/api/admin/cereri/${this.selectedCerere.id}/status?status=DECLINED`, {}).subscribe(() => {
      this.selectedCerere.status = 'DECLINED';
      this.closeModal();
      this.loadCereri();
    });
  }

  downloadCererePdf() {
    if (!this.selectedCerere) return;
    this.http.get(`/api/admin/cereri/${this.selectedCerere.id}/pdf`, { responseType: 'blob' }).subscribe({
      next: (blob: Blob) => {
        const fileURL = URL.createObjectURL(blob);
        window.open(fileURL, '_blank');
      },
      error: () => alert('Eroare la descărcarea sau generarea PDF-ului pentru cerere.')
    });
  }
}
