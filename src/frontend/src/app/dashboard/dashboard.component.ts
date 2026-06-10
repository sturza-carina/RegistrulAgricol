import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService, JwtResponse } from '../services/auth.service';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  user: JwtResponse | null = null;
  activeTab: string = 'overview';
  tenants: any[] = [];

  // Add Tenant Form Fields
  tenantName: string = '';
  
  // Find / Search / Select Tenant
  searchQuery: string = '';
  selectedTenant: any | null = null;

  // Edit Tenant Form Fields
  editTenantName: string = '';
  editTenantIsActive: boolean = true;

  successMessage: string = '';
  errorMessage: string = '';
  isSubmitting: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private http: HttpClient
  ) { }

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => {
      if (!user) {
        this.router.navigate(['/login']);
      } else {
        this.user = user;
        if (this.user.role === 'ROLE_SUPER_ADMIN') {
          this.loadTenants();
        }
      }
    });
  }

  loadTenants() {
    this.http.get<any[]>('/api/tenants').subscribe({
      next: (data) => {
        this.tenants = data;
      },
      error: (err) => {
        console.error('Failed to load tenants', err);
      }
    });
  }

  setTab(tab: string) {
    this.activeTab = tab;
    this.successMessage = '';
    this.errorMessage = '';
    this.selectedTenant = null;
    if (tab === 'tenants') {
      this.loadTenants();
    }
  }

  get filteredTenants() {
    if (!this.searchQuery.trim()) {
      return this.tenants;
    }
    const query = this.searchQuery.toLowerCase();
    return this.tenants.filter(t => 
      t.name.toLowerCase().includes(query) || 
      t.id.toLowerCase().includes(query)
    );
  }

  createTenant() {
    if (!this.tenantName.trim()) {
      this.errorMessage = 'Please enter a tenant name.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.http.post('/api/tenants', {
      name: this.tenantName
    }).subscribe({
      next: (res: any) => {
        this.successMessage = `Tenant "${res.name}" successfully created with ID "${res.id}"!`;
        this.tenantName = '';
        this.isSubmitting = false;
        this.loadTenants();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'An error occurred while creating the tenant.';
        this.isSubmitting = false;
      }
    });
  }

  selectTenantForEdit(tenant: any) {
    this.selectedTenant = tenant;
    this.editTenantName = tenant.name;
    this.editTenantIsActive = tenant.active !== undefined ? tenant.active : tenant.isActive;
    this.successMessage = '';
    this.errorMessage = '';
  }

  cancelEdit() {
    this.selectedTenant = null;
    this.editTenantName = '';
    this.editTenantIsActive = true;
  }

  updateTenant() {
    if (!this.selectedTenant) return;
    if (!this.editTenantName.trim()) {
      this.errorMessage = 'Please enter a tenant name.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    const payload = {
      name: this.editTenantName,
      active: this.editTenantIsActive,
      isActive: this.editTenantIsActive
    };

    this.http.put(`/api/tenants/${this.selectedTenant.id}`, payload).subscribe({
      next: (res: any) => {
        this.successMessage = `Tenant "${res.name}" successfully updated!`;
        this.isSubmitting = false;
        this.selectedTenant = null;
        this.loadTenants();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'An error occurred while updating the tenant.';
        this.isSubmitting = false;
      }
    });
  }

  deleteTenant(tenant: any) {
    const confirmDelete = confirm(`Are you sure you want to permanently delete tenant "${tenant.name}" (ID: ${tenant.id})? This will drop its schema and delete all associated data.`);
    if (!confirmDelete) return;

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.http.delete(`/api/tenants/${tenant.id}`).subscribe({
      next: () => {
        this.successMessage = `Tenant "${tenant.name}" successfully deleted.`;
        this.isSubmitting = false;
        if (this.selectedTenant && this.selectedTenant.id === tenant.id) {
          this.selectedTenant = null;
        }
        this.loadTenants();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'An error occurred while deleting the tenant.';
        this.isSubmitting = false;
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
