import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { HttpClient } from '@angular/common/http';

import { LayoutComponent } from '../../components/layout/layout.component';
import { PageHeaderComponent } from '../../components/page-header/page-header.component';
import { FormsModule } from '@angular/forms';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';
import { GenericFormComponent } from '../../components/generic-form/generic-form.component';
import { FormConfig } from '../../components/generic-form/generic-form.models';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-tenant-admin-dashboard',
  standalone: true,
  imports: [CommonModule, LayoutComponent, PageHeaderComponent, FormsModule, BreadcrumbsComponent, GenericFormComponent],
  templateUrl: './tenant-admin-dashboard.component.html',
})
export class TenantAdminDashboardComponent implements OnInit {

  user: any = null;
  users: any[] = [];
  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'Panou de Control (Tenant Admin)', link: '/dashboard' }
  ];

  showViewModal: boolean = false;
  viewingUser: any = null;
  
  showEditModal: boolean = false;
  editingId: number | null = null;
  errorMessage: string = '';
  isSaving: boolean = false;

  formInitialData: any = {};
  formConfig: FormConfig = {
    submitText: 'Salvează',
    cancelText: 'Anulează',
    sections: [
      {
        fields: [
          { name: 'nume', label: 'Nume', type: 'text', required: false, width: 'full', disabled: true },
          { name: 'username', label: 'Username', type: 'text', required: false, width: 'full', disabled: true },
          { name: 'email', label: 'Email', type: 'email', required: false, width: 'full', disabled: true },
          { name: 'role', label: 'Rol', type: 'select', required: true, width: 'full', options: [
            { label: 'Admin', value: 'ROLE_ADMIN' },
            { label: 'User', value: 'ROLE_USER' }
          ] }
        ]
      }
    ]
  };

  constructor(
    private router: Router,
    private authService: AuthService,
    private http: HttpClient,
    private toastService: ToastService
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
    this.http.get<any>('/api/users?page=0&size=1000').subscribe({
      next: (response) => {
        this.users = response.content.map((u: any) => {
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

  viewUser(user: any): void {
    this.viewingUser = { ...user };
    this.showViewModal = true;
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.viewingUser = null;
  }

  editUser(user: any): void {
    this.editingId = user.id;
    this.formInitialData = { ...user };
    this.showEditModal = true;
    this.errorMessage = '';
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.editingId = null;
  }

  updateUser(formData: any): void {
    if (!this.editingId) return;
    
    // We only send the role to be updated, or merge with existing object
    // But since other fields are disabled, they might not be in formData depending on reactive forms behavior
    // To be safe we merge
    const payload = {
      ...this.formInitialData,
      role: formData.role
    };

    this.isSaving = true;
    this.http.put(`/api/users/${this.editingId}`, payload).subscribe({
      next: () => {
        this.isSaving = false;
        this.loadUsers();
        this.closeEditModal();
      },
      error: (err) => {
        this.isSaving = false;
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
          this.toastService.error('A apărut o eroare la ștergere: ' + (err.error?.message || err.message));
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


}
