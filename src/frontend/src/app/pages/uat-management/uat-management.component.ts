import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { LayoutComponent } from '../../components/layout/layout.component';
import { PageHeaderComponent } from '../../components/page-header/page-header.component';
import { GenericTableComponent, TableColumn, TableFilter, TableAction } from '../../components/generic-table/generic-table.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';
import { GenericFormComponent } from '../../components/generic-form/generic-form.component';
import { FormConfig } from '../../components/generic-form/generic-form.models';

interface UAT {
  id?: number;
  codSiruta: string;
  denumire: string;
  judet: string;
  tipUat: string;
  isActive: boolean;
  tenantId?: string;
}

type ViewMode = 'list' | 'assign-picker' | 'create' | 'edit';

@Component({
  selector: 'app-uat-management',
  standalone: true,
  imports: [CommonModule, FormsModule, LayoutComponent, PageHeaderComponent, GenericTableComponent, BreadcrumbsComponent, GenericFormComponent],
  templateUrl: './uat-management.component.html',
  styleUrls: ['./uat-management.component.scss']
})
export class UatManagementComponent implements OnInit, OnDestroy {

  // ── State ─────────────────────────────────────────────────────────────────
  user: any = null;
  viewMode: ViewMode = 'list';

  /** UATs shown in the main list (tenant UATs for ADMIN/USER, all public for SUPER_ADMIN) */
  tenantUats: UAT[] = [];

  /** All global UATs (public registry), used for assign-picker and super-admin list */
  allPublicUats: UAT[] = [];

  viewingUat: UAT | null = null;
  editingUat: UAT | null = null;
  formInitialData: any = {};

  successMessage = '';
  errorMessage = '';
  loadError = '';

  breadcrumbItems: BreadcrumbItem[] = [{ label: 'UAT-uri' }];

  private subs = new Subscription();

  // ── Computed role flags ───────────────────────────────────────────────────
  get isSuperAdmin(): boolean { return this.user?.role === 'ROLE_SUPER_ADMIN'; }
  get isAdmin(): boolean { return this.user?.role === 'ROLE_ADMIN'; }
  get isUser(): boolean { return this.user?.role === 'ROLE_USER'; }

  // ── API URLs ──────────────────────────────────────────────────────────────
  private readonly publicApiUrl = '/api/uats';
  private readonly tenantApiUrl = '/api/uats/tenant';

  // ── Generic Table config ─────────────────────────────────────────────────

  /** Columns for the tenant UAT list (Admin/User) */
  tenantColumns: TableColumn[] = [
    { field: 'codSiruta', header: 'Cod SIRUTA' },
    { field: 'denumire', header: 'Localitate' },
    { field: 'judet', header: 'Județ' },
    { field: 'tipUat', header: 'Tip' },
    {
      field: 'isActive', header: 'Status', type: 'badge',
      badgeClasses: { 'true': 'active', 'false': 'archived' },
      format: (val) => val ? 'Activ' : 'Inactiv'
    }
  ];

  tenantFilters: TableFilter[] = [
    { field: 'search', label: 'Caută localitate sau județ', type: 'search', searchFields: ['denumire', 'judet', 'codSiruta'] },
    {
      field: 'tipUat', label: 'Tip UAT', type: 'select',
      options: [
        { label: 'Municipiu', value: 'Municipiu' },
        { label: 'Oraș', value: 'Oraș' },
        { label: 'Comună', value: 'Comună' }
      ]
    }
  ];

  /** Actions for the tenant list — admin can remove, both can view */
  get tenantActions(): TableAction[] {
    const actions: TableAction[] = [
      { icon: 'view', tooltip: 'Detalii', action: (row) => this.viewUat(row) }
    ];
    if (this.isAdmin) {
      actions.push({ icon: 'delete', tooltip: 'Elimină din tenant', action: (row) => this.removeFromTenant(row) });
    }
    return actions;
  }

  /** Columns for the public UAT assign-picker */
  publicColumns: TableColumn[] = [
    { field: 'codSiruta', header: 'Cod SIRUTA' },
    { field: 'denumire', header: 'Localitate' },
    { field: 'judet', header: 'Județ' },
    { field: 'tipUat', header: 'Tip' }
  ];

  publicFilters: TableFilter[] = [
    { field: 'search', label: 'Caută localitate sau județ', type: 'search', searchFields: ['denumire', 'judet', 'codSiruta'] },
    {
      field: 'judet', label: 'Filtrează județ', type: 'select',
      options: [
        'Alba','Arad','Argeș','Bacău','Bihor','Bistrița-Năsăud','Botoșani','Brăila','Brașov',
        'București','Buzău','Călărași','Caraș-Severin','Cluj','Constanța','Covasna','Dâmbovița',
        'Dolj','Galați','Giurgiu','Gorj','Harghita','Hunedoara','Ialomița','Iași','Ilfov',
        'Maramureș','Mehedinți','Mureș','Neamț','Olt','Prahova','Sălaj','Satu Mare','Sibiu',
        'Suceava','Teleorman','Timiș','Tulcea','Vâlcea','Vaslui','Vrancea'
      ].map(j => ({ label: j, value: j }))
    }
  ];

  publicActions: TableAction[] = [
    {
      icon: 'add', tooltip: 'Adaugă în tenant',
      action: (row) => this.assignToTenant(row),
      showIf: (row) => !this.tenantUats.some(t => t.codSiruta === row.codSiruta)
    }
  ];

  /** Super Admin columns for full public registry management */
  superAdminColumns: TableColumn[] = [
    { field: 'codSiruta', header: 'Cod SIRUTA' },
    { field: 'denumire', header: 'Localitate' },
    { field: 'judet', header: 'Județ' },
    { field: 'tipUat', header: 'Tip' },
    {
      field: 'isActive', header: 'Status', type: 'badge',
      badgeClasses: { 'true': 'active', 'false': 'archived' },
      format: (val) => val ? 'Activ' : 'Inactiv'
    }
  ];

  superAdminFilters: TableFilter[] = [
    { field: 'search', label: 'Caută după localitate, județ sau cod SIRUTA', type: 'search', searchFields: ['denumire', 'judet', 'codSiruta'] },
    {
      field: 'judet', label: 'Județ', type: 'select',
      options: [
        'Alba','Arad','Argeș','Bacău','Bihor','Bistrița-Năsăud','Botoșani','Brăila','Brașov',
        'București','Buzău','Călărași','Caraș-Severin','Cluj','Constanța','Covasna','Dâmbovița',
        'Dolj','Galați','Giurgiu','Gorj','Harghita','Hunedoara','Ialomița','Iași','Ilfov',
        'Maramureș','Mehedinți','Mureș','Neamț','Olt','Prahova','Sălaj','Satu Mare','Sibiu',
        'Suceava','Teleorman','Timiș','Tulcea','Vâlcea','Vaslui','Vrancea'
      ].map(j => ({ label: j, value: j }))
    }
  ];

  superAdminActions: TableAction[] = [
    { icon: 'view',   tooltip: 'Detalii',   action: (row) => this.viewUat(row) },
    { icon: 'edit',   tooltip: 'Editează',  action: (row) => this.editUat(row) },
    { icon: 'delete', tooltip: 'Șterge',    action: (row) => this.deletePublicUat(row) }
  ];

  // ── Form config (Super Admin create/edit) ─────────────────────────────────
  judete = [
    'Alba','Arad','Argeș','Bacău','Bihor','Bistrița-Năsăud','Botoșani','Brăila','Brașov',
    'București','Buzău','Călărași','Caraș-Severin','Cluj','Constanța','Covasna','Dâmbovița',
    'Dolj','Galați','Giurgiu','Gorj','Harghita','Hunedoara','Ialomița','Iași','Ilfov',
    'Maramureș','Mehedinți','Mureș','Neamț','Olt','Prahova','Sălaj','Satu Mare','Sibiu',
    'Suceava','Teleorman','Timiș','Tulcea','Vâlcea','Vaslui','Vrancea'
  ];

  formConfig: FormConfig = {
    submitText: 'Creare UAT',
    cancelText: 'Anulare',
    sections: [{
      fields: [
        { name: 'denumire', label: 'Localitate', type: 'text', required: true, placeholder: 'ex. Cluj-Napoca', width: 'half' },
        { name: 'codSiruta', label: 'Cod SIRUTA', type: 'text', required: true, placeholder: 'ex. 54975', width: 'half', disabled: false },
        {
          name: 'judet', label: 'Județ', type: 'select', required: true, width: 'half', placeholder: '-- Selectează Județ --',
          options: this.judete.map(j => ({ label: j, value: j }))
        },
        {
          name: 'tipUat', label: 'Tip UAT', type: 'select', required: true, width: 'half',
          options: [
            { label: 'Municipiu', value: 'Municipiu' },
            { label: 'Oraș', value: 'Oraș' },
            { label: 'Comună', value: 'Comună' }
          ]
        },
        { name: 'isActive', label: 'Status Activ', type: 'checkbox', required: false, width: 'full' }
      ]
    }]
  };

  // ─────────────────────────────────────────────────────────────────────────

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.subs.add(
      this.authService.currentUser.subscribe(user => {
        if (!user) { this.router.navigate(['/login']); return; }
        this.user = user;
        this.loadData();
      })
    );

    this.subs.add(
      this.route.queryParams.subscribe(params => {
        if (params['action'] === 'create') this.openCreateForm();
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  // ── Data loading ──────────────────────────────────────────────────────────

  loadData(): void {
    this.loadError = '';
    if (this.isSuperAdmin) {
      this.loadPublicUats();
    } else {
      // Admin and User: load their tenant's UATs
      this.loadTenantUats();
    }
  }

  loadTenantUats(): void {
    this.http.get<UAT[]>(this.tenantApiUrl).subscribe({
      next: (data) => { this.tenantUats = data ?? []; },
      error: () => {
        this.tenantUats = [];
        this.loadError = 'Nu s-au putut încărca UAT-urile. Verifică conexiunea cu serverul.';
      }
    });
  }

  loadPublicUats(): void {
    this.http.get<UAT[]>(this.publicApiUrl).subscribe({
      next: (data) => { this.allPublicUats = data ?? []; },
      error: () => {
        this.allPublicUats = [];
        this.loadError = 'Nu s-au putut încărca UAT-urile din registrul global.';
      }
    });
  }

  /** Load both tenant AND public UATs (for the assign picker — admin needs both lists) */
  loadBothForPicker(): void {
    import('rxjs').then(({ forkJoin }) => {
      forkJoin({
        tenantUats: this.http.get<UAT[]>(this.tenantApiUrl),
        publicUats: this.http.get<UAT[]>(this.publicApiUrl)
      }).subscribe({
        next: (result) => {
          this.tenantUats = result.tenantUats ?? [];
          const assignedCodes = new Set(this.tenantUats.map(t => t.codSiruta));
          this.allPublicUats = (result.publicUats ?? []).filter(u => !assignedCodes.has(u.codSiruta));
        },
        error: () => { this.loadError = 'Nu s-au putut încărca UAT-urile.'; }
      });
    });
  }

  // ── Navigation / View Modes ───────────────────────────────────────────────

  openAssignPicker(): void {
    this.viewMode = 'assign-picker';
    this.successMessage = '';
    this.errorMessage = '';
    this.loadBothForPicker();
    this.updateBreadcrumbs();
  }

  openCreateForm(): void {
    if (!this.isSuperAdmin) return;
    this.viewMode = 'create';
    this.formInitialData = { tipUat: 'Comună', isActive: true };
    this.formConfig.submitText = 'Creare UAT';
    this.formConfig.sections[0].fields.find(f => f.name === 'codSiruta')!.disabled = false;
    this.successMessage = '';
    this.errorMessage = '';
    this.updateBreadcrumbs();
  }

  editUat(uat: UAT): void {
    this.editingUat = { ...uat };
    this.viewMode = 'edit';
    this.formInitialData = { ...uat };
    this.formConfig.submitText = 'Salvează Modificările';
    this.formConfig.sections[0].fields.find(f => f.name === 'codSiruta')!.disabled = true;
    this.successMessage = '';
    this.errorMessage = '';
    this.updateBreadcrumbs();
  }

  viewUat(uat: UAT): void {
    this.router.navigate(['/uats', uat.codSiruta]);
  }

  backToList(): void {
    this.viewMode = 'list';
    this.viewingUat = null;
    this.editingUat = null;
    this.successMessage = '';
    this.errorMessage = '';
    this.updateBreadcrumbs();
  }

  updateBreadcrumbs(): void {
    const baseItem: BreadcrumbItem = {
      label: 'UAT-uri',
      link: '/uats',
      action: () => this.backToList()
    };

    switch (this.viewMode) {
      case 'list':
        this.breadcrumbItems = [{ label: 'UAT-uri' }];
        break;
      case 'assign-picker':
        this.breadcrumbItems = [baseItem, { label: 'Adăugare UAT în Tenant' }];
        break;
      case 'create':
        this.breadcrumbItems = [baseItem, { label: 'Creare UAT' }];
        break;
      case 'edit':
        this.breadcrumbItems = [baseItem, { label: `Editare: ${this.editingUat?.denumire}` }];
        break;
    }
  }

  // ── Tenant Assign / Remove (Admin) ────────────────────────────────────────

  assignToTenant(uat: UAT): void {
    if (this.tenantUats.some(t => t.codSiruta === uat.codSiruta)) {
      this.errorMessage = `UAT-ul "${uat.denumire}" este deja adăugat în tenant.`;
      return;
    }
    this.http.post<UAT>(`${this.tenantApiUrl}/${uat.codSiruta}`, {}).subscribe({
      next: (added) => {
        this.tenantUats = [...this.tenantUats, added];
        this.allPublicUats = this.allPublicUats.filter(u => u.codSiruta !== added.codSiruta);
        this.successMessage = `"${uat.denumire}" a fost adăugat cu succes.`;
        this.errorMessage = '';
      },
      error: (err) => {
        this.errorMessage = err.error?.message || `Nu s-a putut adăuga "${uat.denumire}".`;
      }
    });
  }

  removeFromTenant(uat: UAT): void {
    if (!confirm(`Ești sigur că vrei să elimini "${uat.denumire}" din tenant? Această acțiune este reversibilă.`)) return;
    this.http.delete(`${this.tenantApiUrl}/${uat.codSiruta}`).subscribe({
      next: () => {
        this.tenantUats = this.tenantUats.filter(t => t.codSiruta !== uat.codSiruta);
        this.successMessage = `"${uat.denumire}" a fost eliminat din tenant.`;
        this.errorMessage = '';
      },
      error: (err) => {
        this.errorMessage = err.error?.message || `Nu s-a putut elimina "${uat.denumire}".`;
      }
    });
  }

  // ── Super Admin CRUD (public registry) ────────────────────────────────────

  saveUat(uatData: any): void {
    if (this.viewMode === 'edit' && this.editingUat) {
      this.http.put<UAT>(`${this.publicApiUrl}/${this.editingUat.codSiruta}`, uatData).subscribe({
        next: () => {
          this.successMessage = 'UAT actualizat cu succes!';
          this.loadPublicUats();
          this.backToList();
        },
        error: (err) => { this.errorMessage = err.error?.message || 'Eroare la actualizare.'; }
      });
    } else {
      this.http.post<UAT>(this.publicApiUrl, uatData).subscribe({
        next: () => {
          this.successMessage = 'UAT creat cu succes!';
          this.loadPublicUats();
          this.backToList();
        },
        error: (err) => { this.errorMessage = err.error?.message || 'Eroare la creare.'; }
      });
    }
  }

  deletePublicUat(uat: UAT): void {
    if (!confirm(`Ești sigur că vrei să ștergi "${uat.denumire}" din registrul global? Această acțiune este ireversibilă.`)) return;
    this.http.delete(`${this.publicApiUrl}/${uat.codSiruta}`).subscribe({
      next: () => {
        this.successMessage = 'UAT șters cu succes!';
        this.loadPublicUats();
      },
      error: (err) => { this.errorMessage = err.error?.message || 'Eroare la ștergere.'; }
    });
  }
}
