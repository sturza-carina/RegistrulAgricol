import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from './services/auth.service';
import { WebsocketService } from './services/websocket.service';

@Component({
  selector: 'app-root',
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('portal-cetateni');
  toastMessage: string | null = null;
  private toastTimeout: any;

  constructor(public authService: AuthService, private websocketService: WebsocketService) {
    this.authService.currentUser$.subscribe(user => {
      if (user && user.id) {
        this.websocketService.connect(user.id);
      } else {
        this.websocketService.disconnect();
      }
    });

    this.websocketService.cerereUpdates.subscribe(update => {
      if (update) {
        this.showToast(`Cererea cu codul ${update.codCerere} și-a schimbat statusul în ${update.status}.`);
      }
    });
  }

  showToast(message: string) {
    this.toastMessage = message;
    if (this.toastTimeout) {
      clearTimeout(this.toastTimeout);
    }
    this.toastTimeout = setTimeout(() => {
      this.closeToast();
    }, 5000);
  }

  closeToast() {
    this.toastMessage = null;
  }

  logout() {
    this.authService.logout();
  }
}
