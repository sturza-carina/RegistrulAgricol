import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { HttpClient } from '@angular/common/http';
import { LayoutComponent } from '../../components/layout/layout.component';
import { PageHeaderComponent } from '../../components/page-header/page-header.component';

import { GenericTableComponent, TableColumn, TableFilter, TableAction } from '../../components/generic-table/generic-table.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';

import { GenericFormComponent } from '../../components/generic-form/generic-form.component';
import { FormConfig } from '../../components/generic-form/generic-form.models';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule, LayoutComponent, PageHeaderComponent, GenericTableComponent, BreadcrumbsComponent, GenericFormComponent],
  templateUrl: './user-management.component.html',
})
export class UserManagementComponent implements OnInit {

  user: any = null;
  users: any[] = [];
  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'Utilizatori', link: '/user-management' }
  ];
  
  columns: TableColumn[] = [
    { field: 'name', header: 'Utilizator', type: 'avatar', subField: 'handle' },
    { field: 'email', header: 'Email' },
    { field: 'roleDisplay', header: 'Rol', type: 'badge', badgeClasses: { 'SUPER_ADMIN': 'super', 'ADMIN': 'admin', 'USER': 'utilizator' } },
    { field: 'status', header: 'Status', type: 'badge', badgeClasses: { 'Activ': 'activ', 'Inactiv': 'inactiv' } }
  ];

  filters: TableFilter[] = [
    { field: 'search', label: 'Caută după nume, email sau username...', type: 'search', searchFields: ['name', 'handle', 'email'] },
    { field: 'roleDisplay', label: 'Rol', type: 'select', options: [{ label: 'Administrator', value: 'ADMIN' }, { label: 'Super Administrator', value: 'SUPER_ADMIN' }, { label: 'Utilizator / Registrator', value: 'USER' }] },
    { field: 'status', label: 'Status', type: 'select', options: [{ label: 'Activ', value: 'Activ' }, { label: 'Inactiv', value: 'Inactiv' }] }
  ];

  actions: TableAction[] = [
    { icon: 'view', tooltip: 'Detalii', action: (row) => this.viewUser(row) },
    { icon: 'edit', tooltip: 'Editare', action: (row) => this.editUser(row) },
    { icon: 'delete', tooltip: 'Șterge', action: (row) => this.deleteUser(row) }
  ];

  creatingUser = false;
  viewingUser: any = null;
  editingUser: any = null;
  formInitialData: any = {};
  
  formConfig: FormConfig = {
    submitText: 'Creare Utilizator',
    cancelText: 'Anulare',
    sections: [
      {
        fields: [
          { name: 'username', label: 'Username', type: 'text', required: true, placeholder: 'ex. popescu.ion', width: 'half' },
          { name: 'nume', label: 'Nume Complet', type: 'text', required: false, placeholder: 'ex. Ion Popescu', width: 'half' },
          { name: 'email', label: 'Adresă Email', type: 'email', required: true, placeholder: 'ex. ion.popescu@primaria-x.ro', width: 'half' },
          { name: 'password', label: 'Parolă', type: 'password', required: true, placeholder: '********', width: 'half', hint: '' },
          { 
            name: 'role', label: 'Rol Utilizator', type: 'select', required: true, width: 'half',
            options: [] // populated dynamically in ngOnInit
          },
          { name: 'activ', label: 'Status Activ (Utilizatorul se poate autentifica)', type: 'checkbox', required: false, width: 'full' }
        ]
      }
    ]
  };

  successMessage = '';
  errorMessage = '';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.authService.currentUser.subscribe(u => {
      if (!u) {
        this.router.navigate(['/login']);
      } else {
        this.user = u;
        this.updateRoleOptions();
        this.loadUsers();
      }
    });

    this.route.queryParams.subscribe(params => {
      if (params['action'] === 'create') {
        this.openAddForm();
      }
    });
  }

  updateRoleOptions(): void {
    const roleField = this.formConfig.sections[0].fields.find(f => f.name === 'role');
    if (roleField) {
      if (this.user?.role === 'ROLE_SUPER_ADMIN' || this.user?.role === 'SUPER_ADMIN') {
        roleField.options = [
          { label: 'Super Administrator (Acces Global)', value: 'ROLE_SUPER_ADMIN' },
          { label: 'Administrator (Nivel Tenant)', value: 'ROLE_ADMIN' },
          { label: 'Utilizator / Registrator', value: 'ROLE_USER' }
        ];
      } else {
        roleField.options = [
          { label: 'Administrator (Nivel Tenant)', value: 'ROLE_ADMIN' },
          { label: 'Utilizator / Registrator', value: 'ROLE_USER' }
        ];
      }
      this.formConfig = { ...this.formConfig };
    }
  }

  loadUsers(): void {
    this.http.get<any[]>('/api/users').subscribe({
      next: (data) => {
        this.users = data.map(u => ({
          id: u.id,
          name: u.nume || u.username,
          initials: (u.nume ? u.nume.charAt(0) : u.username.charAt(0)).toUpperCase(),
          handle: '@' + u.username,
          avatarBg: '#0369a1',
          img: null,
          email: u.email || 'No email provided',
          role: u.role,
          roleDisplay: u.role ? u.role.replace('ROLE_', '') : 'USER',
          status: u.activ ? 'Activ' : 'Inactiv',
          lastLogin: 'Unknown',
          raw: u
        })).sort((a, b) => (b.id || 0) - (a.id || 0));
      },
      error: () => {
        this.users = [];
        console.error('Failed to load users');
      }
    });
  }

  openAddForm(): void {
    this.creatingUser = true;
    this.viewingUser = null;
    this.editingUser = null;
    
    this.formInitialData = { role: 'ROLE_USER', activ: true };
    this.formConfig.submitText = 'Creare Utilizator';
    this.formConfig.sections[0].fields.find(f => f.name === 'password')!.required = true;
    this.formConfig.sections[0].fields.find(f => f.name === 'password')!.hint = '';

    this.successMessage = '';
    this.errorMessage = '';
    this.updateBreadcrumbs();
  }

  closeAddForm(): void {
    this.creatingUser = false;
    this.errorMessage = '';
    this.successMessage = '';
    this.updateBreadcrumbs();
  }

  viewUser(u: any): void {
    this.viewingUser = u;
    this.creatingUser = false;
    this.editingUser = null;
    this.errorMessage = '';
    this.successMessage = '';
    this.updateBreadcrumbs();
  }

  closeViewUser(): void {
    this.viewingUser = null;
    this.updateBreadcrumbs();
  }

  editUser(u: any): void {
    this.editingUser = { ...u.raw };
    this.viewingUser = null;
    this.creatingUser = false;
    
    this.formInitialData = {
      username: u.raw.username,
      nume: u.raw.nume,
      email: u.raw.email,
      role: u.raw.role,
      activ: u.raw.activ
    };
    
    this.formConfig.submitText = 'Salvează Modificările';
    this.formConfig.sections[0].fields.find(f => f.name === 'password')!.required = false;
    this.formConfig.sections[0].fields.find(f => f.name === 'password')!.hint = 'Lăsați gol pentru a păstra parola actuală';

    this.errorMessage = '';
    this.successMessage = '';
    this.updateBreadcrumbs();
  }

  closeEditUser(): void {
    this.editingUser = null;
    this.errorMessage = '';
    this.successMessage = '';
    this.updateBreadcrumbs();
  }

  updateBreadcrumbs(): void {
    this.breadcrumbItems = [
      { label: 'Utilizatori', link: (this.creatingUser || this.viewingUser || this.editingUser) ? undefined : '/user-management' }
    ];
    if (this.creatingUser || this.viewingUser || this.editingUser) {
      this.breadcrumbItems[0].link = '/user-management';
      this.breadcrumbItems[0].action = () => {
        if (this.creatingUser) this.closeAddForm();
        if (this.viewingUser) this.closeViewUser();
        if (this.editingUser) this.closeEditUser();
      };
    }

    if (this.creatingUser) {
      this.breadcrumbItems.push({ label: 'Creare Utilizator' });
    } else if (this.viewingUser) {
      this.breadcrumbItems.push({ label: `Detalii: ${this.viewingUser.name || this.viewingUser.username}` });
    } else if (this.editingUser) {
      this.breadcrumbItems.push({ label: `Editare: ${this.editingUser.nume || this.editingUser.username}` });
    }
  }

  saveUser(userData: any): void {
    if (this.editingUser) {
      if (!userData.password) {
        delete userData.password;
      }
      this.http.put(`/api/users/${this.editingUser.id}`, userData).subscribe({
        next: () => {
          this.successMessage = 'Utilizator actualizat cu succes!';
          this.loadUsers(); 
          this.closeEditUser(); 
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Eroare la actualizare.';
        }
      });
    } else {
      this.http.post('/api/users', userData).subscribe({
        next: () => { 
          this.successMessage = 'Utilizator creat cu succes!';
          this.loadUsers(); 
          this.closeAddForm(); 
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Eroare la creare.';
        }
      });
    }
  }

  deleteUser(u: any): void {
    if (confirm(`Ești sigur că vrei să ștergi utilizatorul ${u.name}? Această acțiune este ireversibilă.`)) {
      this.http.delete(`/api/users/${u.id}`).subscribe({
        next: () => {
          this.successMessage = 'Utilizator șters cu succes!';
          this.loadUsers();
          if (this.viewingUser?.id === u.id) this.closeViewUser();
          if (this.editingUser?.id === u.id) this.closeEditUser();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Eroare la ștergere.';
        }
      });
    }
  }


}
