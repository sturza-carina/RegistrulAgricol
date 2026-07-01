import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { HttpClient } from '@angular/common/http';
import { ToastService } from '../../services/toast.service';

import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';
import { GenericFormComponent } from '../../components/generic-form/generic-form.component';
import { FormConfig } from '../../components/generic-form/generic-form.models';
import { GenericTableComponent, TableColumn, TableFilter, TableAction } from '../../components/generic-table/generic-table.component';

@Component({
  selector: 'app-create-tenant',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent, BreadcrumbsComponent, GenericFormComponent, GenericTableComponent],
  templateUrl: './create-tenant.component.html',
})
export class CreateTenantComponent implements OnInit {

  user: any = null;
  isSubmitting = false;
  successMessage = '';
  errorMessage = '';

  tenants: any[] = [];
  filteredTenants: any[] = [];
  searchTerm: string = '';
  showCreateForm = false;
  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'Tenanți', link: '/tenants' }
  ];
  selectedTenant: any = null;
  allUats: any[] = [];
  selectedUatToAssign: string = '';
  currentPage = 1;
  pageSize = 10;
  Math = Math;

  formInitialData: any = {};
  formConfig: FormConfig = {
    submitText: 'Creare Tenant',
    cancelText: 'Anulare',
    sections: [
      {
        fields: [
          { name: 'orgName', label: 'Nume Organizație', type: 'text', required: true, placeholder: 'ex., Primăria Giroc', width: 'half', hint: 'Numele oficial al entității înregistrate.' },
          { name: 'schemaName', label: 'Cod SIRUTA', type: 'text', required: true, placeholder: 'ex., 155286', width: 'half', hint: 'Cod din 5 sau 6 cifre.' }
        ]
      }
    ]
  };

  tableColumns: TableColumn[] = [
    { field: 'id', header: 'Cod SIRUTA (ID)' },
    { field: 'name', header: 'Nume Organizație' },
    { field: 'schemaName', header: 'Nume Schemă' }
  ];

  tableFilters: TableFilter[] = [
    { field: 'search', label: 'Caută după nume, ID sau schemă...', type: 'search', searchFields: ['name', 'id', 'schemaName'] }
  ];

  tableActions: TableAction[] = [
    { icon: 'view', tooltip: 'Detalii', action: (row) => this.selectTenant(row) },
    { icon: 'edit', tooltip: 'Edit', action: (row) => this.editTenant(row) },
    { icon: 'delete', tooltip: 'Delete', action: (row) => this.deleteTenant(row) }
  ];

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private http: HttpClient,
    private toastService: ToastService
  ) {}

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

    this.route.queryParams.subscribe(params => {
      if (params['action'] === 'create') {
        this.openCreateForm();
      }
    });
  }

  loadTenants(): void {
    this.http.get<any[]>('/api/tenants').subscribe({
      next: (data) => {
        this.tenants = data;
        this.applyFilter();
      },
      error: (err) => {
        console.error('Failed to load tenants', err);
      }
    });
  }

  onSearch(event: any): void {
    this.searchTerm = event.target.value.toLowerCase();
    this.applyFilter();
  }

  applyFilter(): void {
    this.currentPage = 1;
    this.filteredTenants = this.tenants.filter(t => 
      t.name.toLowerCase().includes(this.searchTerm) ||
      t.id.toLowerCase().includes(this.searchTerm) ||
      t.schemaName.toLowerCase().includes(this.searchTerm)
    );
  }

  get paginatedTenants() {
    const start = (this.currentPage - 1) * this.pageSize;
    const end = start + this.pageSize;
    return this.filteredTenants.slice(start, end);
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredTenants.length / this.pageSize));
  }

  prevPage(): void {
    if (this.currentPage > 1) this.currentPage--;
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) this.currentPage++;
  }

  openCreateForm() {
    this.showCreateForm = true;
    this.selectedTenant = null;
    this.formInitialData = { orgName: '', schemaName: '' };
    this.formConfig.submitText = 'Creare Tenant';
    this.errorMessage = '';
    this.successMessage = '';
    this.updateBreadcrumbs();
  }

  editingTenant: any = null;

  closeCreateForm() {
    this.showCreateForm = false;
    this.errorMessage = '';
    this.successMessage = '';
    this.updateBreadcrumbs();
  }

  selectTenant(tenant: any) {
    this.selectedTenant = tenant;
    this.showCreateForm = false;
    this.editingTenant = null;
    this.errorMessage = '';
    this.successMessage = '';
    this.selectedUatToAssign = '';
    this.loadUats();
    this.updateBreadcrumbs();
  }

  closeTenantDetails() {
    this.selectedTenant = null;
    this.errorMessage = '';
    this.successMessage = '';
    this.updateBreadcrumbs();
  }

  editTenant(t: any) {
    this.editingTenant = { ...t }; // copy object
    this.selectedTenant = null;
    this.showCreateForm = false;
    this.errorMessage = '';
    this.successMessage = '';
    this.updateBreadcrumbs();
  }

  closeEditTenant() {
    this.editingTenant = null;
    this.errorMessage = '';
    this.successMessage = '';
    this.updateBreadcrumbs();
  }

  updateBreadcrumbs(): void {
    this.breadcrumbItems = [
      { label: 'Tenanți', link: (this.showCreateForm || this.selectedTenant || this.editingTenant) ? undefined : '/tenants' }
    ];
    if (this.showCreateForm || this.selectedTenant || this.editingTenant) {
      this.breadcrumbItems[0].link = '/tenants';
      this.breadcrumbItems[0].action = () => {
        if (this.showCreateForm) this.closeCreateForm();
        if (this.selectedTenant) this.closeTenantDetails();
        if (this.editingTenant) this.closeEditTenant();
      };
    }

    if (this.showCreateForm) {
      this.breadcrumbItems.push({ label: 'Creare Tenant' });
    } else if (this.selectedTenant) {
      this.breadcrumbItems.push({ label: `Detalii: ${this.selectedTenant.name}` });
    } else if (this.editingTenant) {
      this.breadcrumbItems.push({ label: `Editare: ${this.editingTenant.name}` });
    }
  }

  saveEditTenant() {
    if (!this.editingTenant || !this.editingTenant.name || this.editingTenant.name.trim() === '') {
      this.errorMessage = 'Numele nu poate fi gol.';
      return;
    }
    
    this.isSubmitting = true;
    this.errorMessage = '';
    
    this.http.put(`/api/tenants/${this.editingTenant.id}`, { name: this.editingTenant.name }).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.successMessage = 'Numele a fost actualizat cu succes!';
        this.editingTenant = null;
        this.loadTenants();
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message || 'Eroare la actualizarea tenantului.';
      }
    });
  }

  deleteTenant(t: any) {
    if (confirm(`Ești sigur că vrei să ștergi tenantul ${t.name}? Această acțiune este ireversibilă.`)) {
      this.toastService.info('Ștergerea unui tenant necesită intervenție manuală la nivel de bază de date momentan.');
    }
  }

  loadUats(): void {
    this.http.get<any[]>('/api/uats').subscribe({
      next: (data) => {
        this.allUats = data;
      },
      error: (err) => {
        console.error('Failed to load UATs', err);
      }
    });
  }

  get assignedUats() {
    const tenantId = this.selectedTenant ? this.selectedTenant.id : (this.editingTenant ? this.editingTenant.id : null);
    if (!tenantId) return [];
    return this.allUats.filter(u => u.tenantId === tenantId);
  }

  get unassignedUats() {
    return this.allUats.filter(u => !u.tenantId);
  }

  assignUat(): void {
    if (!this.selectedUatToAssign) {
      this.errorMessage = 'Vă rugăm să selectați un UAT.';
      return;
    }
    const tenantId = this.selectedTenant ? this.selectedTenant.id : (this.editingTenant ? this.editingTenant.id : null);
    if (!tenantId) return;

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.http.post(`/api/tenants/${tenantId}/uats/${this.selectedUatToAssign}`, {}).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.successMessage = 'UAT asociat cu succes!';
        this.selectedUatToAssign = '';
        this.loadUats(); // refresh
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message || 'A apărut o eroare la asocierea UAT-ului.';
      }
    });
  }

  removeUat(uat: any): void {
    const tenantId = this.selectedTenant ? this.selectedTenant.id : (this.editingTenant ? this.editingTenant.id : null);
    if (!tenantId) return;

    if (confirm(`Ești sigur că vrei să dezasociezi UAT-ul ${uat.denumire}?`)) {
      this.http.delete(`/api/tenants/${tenantId}/uats/${uat.codSiruta}`).subscribe({
        next: () => {
          this.successMessage = 'UAT dezasociat cu succes!';
          this.loadUats();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'A apărut o eroare la dezasocierea UAT-ului.';
        }
      });
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  goToDashboard(): void {
    this.router.navigate(['/super-admin']);
  }

  cancel(): void {
    this.closeCreateForm();
  }

  onCreateTenant(formData: any): void {
    if (!formData.orgName || !formData.schemaName) {
      this.errorMessage = 'Vă rugăm să completați toate câmpurile obligatorii.';
      return;
    }
    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.http.post('/api/tenants', {
      name: formData.orgName,
      sirutaCode: formData.schemaName,
    }).subscribe({
      next: (res: any) => {
        this.successMessage = `Tenant "${res.name}" a fost creat cu succes!`;
        this.isSubmitting = false;
        this.showCreateForm = false;
        this.loadTenants();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'A apărut o eroare la crearea Tenant-ului.';
        this.isSubmitting = false;
      }
    });
  }
}
