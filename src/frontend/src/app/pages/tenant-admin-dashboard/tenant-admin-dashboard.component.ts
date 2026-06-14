import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { HttpClient } from '@angular/common/http';

import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-tenant-admin-dashboard',
  standalone: true,
  imports: [CommonModule, SidebarComponent, FormsModule],
  templateUrl: './tenant-admin-dashboard.component.html',
})
export class TenantAdminDashboardComponent implements OnInit {

  user: any = null;

  users: any[] = [];

  constructor(
    private router: Router,
    private authService: AuthService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.authService.currentUser.subscribe(u => {
      if (!u) {
        this.router.navigate(['/login']);
      } else {
        this.user = u;
        this.loadUsers();
      }
    });
  }

  loadUsers(): void {
    this.http.get<any[]>('/api/users').subscribe({
      next: (data) => {
        this.users = data.map(u => {
          const displayName = u.nume || u.username || 'Utilizator';
          const init = displayName.substring(0, 2).toUpperCase();
          const colors = ['#1a6b3c', '#0369a1', '#9333ea', '#ea580c', '#b91c1c'];
          const colorHash = displayName.length % colors.length;
          
          return {
            ...u,
            name: displayName,
            initials: init,
            color: colors[colorHash],
            lastLogin: 'Recent' // Placeholder since backend lacks this field
          };
        });
      },
      error: () => {
        this.users = [
          { name: 'Andrei Ionescu', initials: 'AI', color: '#1a6b3c', email: 'a.ionescu@primaria-s1.ro', role: 'Admin',     lastLogin: 'Today, 09:45 AM' },
          { name: 'Maria Popescu',  initials: 'MP', color: '#0369a1', email: 'm.popescu@primaria-s1.ro', role: 'Registrar', lastLogin: 'Yesterday, 04:12 PM' },
          { name: 'Cristian Dumitru', initials: 'CD', color: '#9333ea', email: 'c.dumitru@primaria-s1.ro', role: 'Viewer', lastLogin: '3 days ago' },
        ];
      }
    });
  }

  showViewModal: boolean = false;
  viewingUser: any = null;
  showEditModal: boolean = false;
  editingUser: any = null;
  errorMessage: string = '';

  viewUser(user: any): void {
    this.viewingUser = { ...user };
    this.showViewModal = true;
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.viewingUser = null;
  }

  editUser(user: any): void {
    this.editingUser = { ...user };
    this.showEditModal = true;
    this.errorMessage = '';
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.editingUser = null;
  }

  updateUser(): void {
    if (!this.editingUser) return;
    this.http.put(`/api/users/${this.editingUser.id}`, this.editingUser).subscribe({
      next: () => {
        this.loadUsers();
        this.closeEditModal();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'A apărut o eroare la salvare.';
      }
    });
  }

  deleteUser(user: any): void {
    if (confirm('Ești sigur?')) {
      this.http.delete(`/api/users/${user.id}`).subscribe({
        next: () => {
          this.loadUsers();
        },
        error: (err) => {
          alert('A apărut o eroare la ștergere: ' + (err.error?.message || err.message));
        }
      });
    }
  }

  goToCreateUat(): void {
    this.router.navigate(['/uats'], { queryParams: { action: 'create' } });
  }

  goToCreateGospodarie(): void {
    this.router.navigate(['/gospodarii/new']);
  }

  goToCreatePersoana(): void {
    this.router.navigate(['/persoane/new']);
  }

  goToCreateUser(): void {
    this.router.navigate(['/user-management'], { queryParams: { action: 'create' } });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
