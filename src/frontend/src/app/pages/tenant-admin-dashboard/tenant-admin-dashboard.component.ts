import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { HttpClient } from '@angular/common/http';

import { SidebarComponent } from '../../components/sidebar/sidebar.component';

@Component({
  selector: 'app-tenant-admin-dashboard',
  standalone: true,
  imports: [CommonModule, SidebarComponent],
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
      next: (data) => this.users = data,
      error: () => {
        this.users = [
          { name: 'Andrei Ionescu', initials: 'AI', color: '#1a6b3c', email: 'a.ionescu@primaria-s1.ro', role: 'Admin',     lastLogin: 'Today, 09:45 AM' },
          { name: 'Maria Popescu',  initials: 'MP', color: '#0369a1', email: 'm.popescu@primaria-s1.ro', role: 'Registrar', lastLogin: 'Yesterday, 04:12 PM' },
          { name: 'Cristian Dumitru', initials: 'CD', color: '#9333ea', email: 'c.dumitru@primaria-s1.ro', role: 'Viewer', lastLogin: '3 days ago' },
        ];
      }
    });
  }

  editUser(user: any): void {
    console.log('Edit user:', user);
    // TODO: open edit modal or navigate to edit page
  }

  goToCreateUser(): void {
    // TODO: navigate to create user page
  }
}
