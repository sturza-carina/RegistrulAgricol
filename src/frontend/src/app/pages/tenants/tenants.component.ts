import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { TenantService, Tenant } from '../../services/tenant.service';

@Component({
  selector: 'app-tenants',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tenants.component.html',
})
export class TenantsComponent implements OnInit {

  user: any = null;
  tenants: Tenant[] = [];
  errorMessage = '';
  successMessage = '';

  editingTenantId: string | null = null;
  editForm = {
    name: '',
    active: true
  };

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
        this.errorMessage = err.error?.message || 'Eroare la încărcarea listei de tenanți.';
      }
    });
  }

  goToDashboard(): void {
    this.router.navigate(['/super-admin']);
  }

  goToUsers(): void {
    this.router.navigate(['/user-management']);
  }

  goToSettings(): void {}

  goToTenants(): void {
    this.router.navigate(['/tenants']);
  }

  goToCreateTenant(): void {
    this.router.navigate(['/tenants/new']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  startEdit(tenant: Tenant): void {
    this.editingTenantId = tenant.id;
    this.editForm.name = tenant.name;
    this.editForm.active = tenant.active;
  }

  cancelEdit(): void {
    this.editingTenantId = null;
  }

  saveEdit(id: string): void {
    if (!this.editForm.name.trim()) {
      this.errorMessage = 'Numele tenantului nu poate fi gol.';
      return;
    }

    this.tenantService.updateTenant(id, this.editForm.name, this.editForm.active).subscribe({
      next: (updatedTenant) => {
        this.successMessage = 'Tenantul "' + updatedTenant.name + '" a fost actualizat cu succes!';
        this.editingTenantId = null;
        this.errorMessage = '';
        this.loadTenants();
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (err) => {
        console.error('Update tenant error:', err);
        const status = err.status || 'unknown';
        const msg = err.error?.message || err.message || 'Eroare la actualizarea tenantului.';
        this.errorMessage = `Eroare (HTTP ${status}): ${msg}`;
      }
    });
  }

  deleteTenant(tenant: Tenant): void {
    if (confirm('Sunteti sigur ca doriti sa stergeti tenantul "' + tenant.name + '"?\nATENTIE: Aceasta actiune va sterge permanent schema din baza de date si toti utilizatorii asociati!')) {
      this.tenantService.deleteTenant(tenant.id).subscribe({
        next: () => {
          this.successMessage = 'Tenantul "' + tenant.name + '" a fost sters!';
          this.errorMessage = '';
          this.loadTenants();
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (err) => {
          console.error('Delete tenant error:', err);
          const status = err.status || 'unknown';
          const msg = err.error?.message || err.message || 'Eroare la stergerea tenantului.';
          this.errorMessage = `Eroare (HTTP ${status}): ${msg}`;
        }
      });
    }
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
