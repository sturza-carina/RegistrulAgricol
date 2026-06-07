import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-management.component.html',
})
export class UserManagementComponent implements OnInit {

  user: any = null;
  users: any[] = [];
  roleFilter = 'All Roles';
  statusFilter = 'All Statuses';

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
          { name: 'Elena Ionescu',  initials: 'EI', handle: '@elena_admin',  avatarBg: '#1a6b3c', img: null, email: 'e.ionescu@registru.gov.ro', role: 'Admin',     status: 'Active',   lastLogin: '2 hours ago' },
          { name: 'Mihai Radu',     initials: 'MR', handle: '@m_radu_reg',   avatarBg: '#0369a1', img: null, email: 'm.radu@registru.gov.ro',    role: 'Registrar', status: 'Active',   lastLogin: 'Yesterday, 14:20' },
          { name: 'Dana Popescu',   initials: 'DP', handle: '@dana_viewer',  avatarBg: '#9333ea', img: null, email: 'd.popescu@partner.ro',      role: 'Viewer',    status: 'Inactive', lastLogin: '3 days ago' },
          { name: 'Andrei Marin',   initials: 'AM', handle: '@amarin_reg',   avatarBg: '#15803d', img: null, email: 'a.marin@registru.gov.ro',   role: 'Registrar', status: 'Active',   lastLogin: 'Just now' },
        ];
      }
    });
  }

  goToDashboard(): void {
    const role = this.user?.role;
    if (role === 'ROLE_SUPER_ADMIN') {
      this.router.navigate(['/super-admin']);
    } else {
      this.router.navigate(['/tenant-admin']);
    }
  }

  goToSettings(): void {}

  goToRegistry(): void {}

  goToReports(): void {}

  goToCreateUser(): void {
    // TODO: navigate to create user page
  }

  editUser(u: any): void {
    console.log('Edit user:', u);
  }

  clearFilters(): void {
    this.roleFilter = 'All Roles';
    this.statusFilter = 'All Statuses';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
