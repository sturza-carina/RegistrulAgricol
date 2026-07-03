import { Component, OnInit, Input, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { HttpClient } from '@angular/common/http';
import { Client, Message } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, SidebarComponent],
  templateUrl: './layout.component.html'
})
export class LayoutComponent implements OnInit, OnDestroy {
  @Input() activeGospodarieId?: number;
  @Input() activeTab?: string;
  
  user: any = null;
  cereri: any[] = [];
  unreadCereri: number = 0;
  showNotifications: boolean = false;
  showLangDropdown: boolean = false;
  currentLocale: string = 'ro';
  private stompClient: Client;

  constructor(
    private authService: AuthService,
    private router: Router,
    private http: HttpClient
  ) {
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('/api/ws'),
      onConnect: () => {
        if (this.user && this.user.tenantId) {
          this.stompClient.subscribe('/topic/tenant/' + this.user.tenantId + '/cereri', (message: Message) => {
            const newCerere = JSON.parse(message.body);
            if (newCerere.status === 'PENDING' && !this.router.url.includes('/cereri-admin')) {
              this.unreadCereri++;
            }
          });
        }
      }
    });

    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      if (event.urlAfterRedirects.includes('/cereri-admin')) {
        this.unreadCereri = 0;
      }
    });
  }

  ngOnInit() {
    this.currentLocale = window.location.pathname.startsWith('/en/') ? 'en' : 'ro';
    this.authService.currentUser.subscribe(u => {
      this.user = u;
      if (this.user && this.user.tenantId) {
        this.loadCereri();
        this.stompClient.activate();
      }
    });
  }

  ngOnDestroy() {
    if (this.stompClient) {
      this.stompClient.deactivate();
    }
  }

  loadCereri() {
    this.http.get<any[]>(`/api/admin/cereri`).subscribe(res => {
      this.cereri = res.filter(c => c.status === 'PENDING');
      if (this.router.url.includes('/cereri-admin')) {
        this.unreadCereri = 0;
      } else {
        this.unreadCereri = this.cereri.length;
      }
    });
  }

  openCereriAdmin() {
    this.unreadCereri = 0;
    this.router.navigate(['/cereri-admin']);
  }

  acceptCerere(cerere: any) {
    this.http.put(`/api/admin/cereri/${cerere.id}/status?status=ACCEPTED`, {}).subscribe(() => {
      this.cereri = this.cereri.filter(c => c.id !== cerere.id);
    });
  }

  declineCerere(cerere: any) {
    this.http.put(`/api/admin/cereri/${cerere.id}/status?status=DECLINED`, {}).subscribe(() => {
      this.cereri = this.cereri.filter(c => c.id !== cerere.id);
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.lang-switcher-container')) {
      this.showLangDropdown = false;
    }
  }

  toggleLangDropdown() {
    this.showLangDropdown = !this.showLangDropdown;
  }

  changeLanguage(locale: string): void {
    if (locale === this.currentLocale) return;
    document.cookie = `lang=${locale};path=/;max-age=31536000`;
    const path = window.location.pathname;
    if (path.startsWith('/ro/') || path.startsWith('/en/')) {
      const newPath = path.replace(/^\/(ro|en)/, `/${locale}`);
      window.location.href = window.location.origin + newPath + window.location.search + window.location.hash;
    } else {
      alert(`Language switched to ${locale.toUpperCase()}. In production, this will redirect to /${locale}/. In development, run the corresponding build config (e.g. npm run start -- --configuration=${locale}) to view.`);
      window.location.reload();
    }
  }
}
