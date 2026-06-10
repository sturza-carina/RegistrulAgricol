import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { TenantService, Tenant } from '../../services/tenant.service';

@Component({
  selector: 'app-super-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './super-admin-dashboard.component.html',
})
export class SuperAdminDashboardComponent implements OnInit {

  user: any = null;
  tenants: Tenant[] = [];
  errorMessage = '';

  constructor(
    private router: Router,
    private authService: AuthService,
    private tenantService: TenantService
  ) {}

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => {
      if (!user) {
        this.router.navigate(['/login']);
      } else {
        this.user = user;
        this.loadTenants();
      }
    });
  }

  loadTenants(): void {
    this.tenantService.getTenants().subscribe({
      next: (data) => {
        this.tenants = data;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Eroare la incarcarea listei de tenanti.';
      }
    });
  }

  goToCreateTenant(): void {
    this.router.navigate(['/tenants/new']);
  }

  goToUsers(): void {
    this.router.navigate(['/user-management']);
  }

  goToTenants(): void {
    this.router.navigate(['/tenants']);
  }

  goToSettings(): void {
    // settings page not yet implemented
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  exportRecords(): void {
    console.log('Export records clicked');
    // TODO: implement export
  }

  getInitials(name: string): string {
    if (!name) return 'TN';
    const parts = name.split(' ');
    if (parts.length > 1) {
      return (parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }
    return name.slice(0, 2).toUpperCase();
  }

  getRandomColor(id: string): string {
    const colors = ['#1a6b3c', '#0369a1', '#9333ea', '#15803d', '#4b5563', '#b91c1c', '#d97706'];
    let hash = 0;
    for (let i = 0; i < id.length; i++) {
      hash = id.charCodeAt(i) + ((hash << 5) - hash);
    }
    const index = Math.abs(hash) % colors.length;
    return colors[index];
  }
}
