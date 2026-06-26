import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { LayoutComponent } from '../../components/layout/layout.component';
import { PageHeaderComponent } from '../../components/page-header/page-header.component';
import { GenericTableComponent, TableColumn, TableFilter, TableAction } from '../../components/generic-table/generic-table.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';
import { GenericFormComponent } from '../../components/generic-form/generic-form.component';
import { FormConfig } from '../../components/generic-form/generic-form.models';

interface UAT {
  codSiruta: string;
  denumire: string;
  judet: string;
  tipUat: string;
  isActive: boolean;
  tenantId?: string;
}

@Component({
  selector: 'app-uat-management',
  standalone: true,
  imports: [CommonModule, FormsModule, LayoutComponent, PageHeaderComponent, GenericTableComponent, BreadcrumbsComponent, GenericFormComponent],
  templateUrl: './uat-management.component.html',
  styleUrls: ['./uat-management.component.scss']
})
export class UatManagementComponent implements OnInit {
  judete = [
    'Alba', 'Arad', 'Argeș', 'Bacău', 'Bihor', 'Bistrița-Năsăud',
    'Botoșani', 'Brăila', 'Brașov', 'București', 'Buzău', 'Călărași',
    'Caraș-Severin', 'Cluj', 'Constanța', 'Covasna', 'Dâmbovița', 'Dolj',
    'Galați', 'Giurgiu', 'Gorj', 'Harghita', 'Hunedoara', 'Ialomița',
    'Iași', 'Ilfov', 'Maramureș', 'Mehedinți', 'Mureș', 'Neamț', 'Olt',
    'Prahova', 'Sălaj', 'Satu Mare', 'Sibiu', 'Suceava', 'Teleorman',
    'Timiș', 'Tulcea', 'Vâlcea', 'Vaslui', 'Vrancea'
  ];


  uats: UAT[] = [];
  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'UAT-uri', link: '/uats' }
  ];

  creatingUat = false;
  viewingUat: UAT | null = null;
  editingUat: UAT | null = null;
  formInitialData: any = {};
  
  formConfig: FormConfig = {
    submitText: 'Creare UAT',
    cancelText: 'Anulare',
    sections: [
      {
        fields: [
          { name: 'denumire', label: 'Nume UAT', type: 'text', required: true, placeholder: 'ex. Cluj-Napoca', width: 'half' },
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
      }
    ]
  };

  columns: TableColumn[] = [
    { field: 'denumire', header: 'Denumire', type: 'text', subField: 'judet' },
    { field: 'codSiruta', header: 'Cod SIRUTA', format: val => val },
    { field: 'tipUat', header: 'Tip UAT', type: 'badge', badgeClasses: { 'Comună': 'viewer', 'Oraș': 'viewer', 'Municipiu': 'viewer' } },
    { field: 'isActive', header: 'Status', type: 'badge', format: val => val ? 'Activ' : 'Inactiv', badgeClasses: { 'true': 'activ', 'false': 'inactiv' } }
  ];

  filters: TableFilter[] = [
    { field: 'search', label: 'Caută după denumire, județ...', type: 'search', searchFields: ['denumire', 'judet', 'codSiruta'] },
    { field: 'tipUat', label: 'Tip UAT', type: 'select', options: [{label: 'Comună', value: 'Comună'}, {label: 'Oraș', value: 'Oraș'}, {label: 'Municipiu', value: 'Municipiu'}] },
    { field: 'isActive', label: 'Status', type: 'select', options: [{label: 'Activ', value: true}, {label: 'Inactiv', value: false}] }
  ];

  actions: TableAction[] = [
    { icon: 'view', tooltip: 'Accesare Context UAT', action: (row) => this.manageUat(row), showIf: (row) => row.isActive },
    { icon: 'edit', tooltip: 'Editare UAT', action: (row) => this.editUat(row) },
    { icon: 'delete', tooltip: 'Ștergere UAT', action: (row) => this.deleteUat(row) }
  ];

  loadError = '';
  successMessage = '';
  errorMessage = '';

  private apiUrl = '/api/uats';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private http: HttpClient
  ) {
  }

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => {
      if (!user) {
        this.router.navigate(['/login']);
      }
    });
    this.loadUats();

    this.route.queryParams.subscribe(params => {
      if (params['action'] === 'create') {
        this.openAddForm();
      }
    });
  }

  // CRUD
  loadUats(): void {
    this.loadError = '';
    this.http.get<UAT[]>(this.apiUrl).subscribe({
      next: (data) => {
        this.uats = data;
      },
      error: () => {
        this.uats = [];
        this.loadError = 'Nu s-au putut încărca UAT-urile. Verifică dacă backend-ul rulează.';
      }
    });
  }

  openAddForm(): void {
    this.creatingUat = true;
    this.viewingUat = null;
    this.editingUat = null;
    
    this.formInitialData = { tipUat: 'Comună', isActive: true };
    this.formConfig.submitText = 'Creare UAT';
    this.formConfig.sections[0].fields.find(f => f.name === 'codSiruta')!.disabled = false;
    
    this.successMessage = '';
    this.errorMessage = '';
    this.updateBreadcrumbs();
  }

  closeAddForm(): void {
    this.creatingUat = false;
    this.errorMessage = '';
    this.successMessage = '';
    this.updateBreadcrumbs();
  }

  viewUat(uat: UAT): void {
    this.viewingUat = uat;
    this.creatingUat = false;
    this.editingUat = null;
    this.errorMessage = '';
    this.successMessage = '';
    this.updateBreadcrumbs();
  }

  closeViewUat(): void {
    this.viewingUat = null;
    this.updateBreadcrumbs();
  }

  editUat(uat: UAT): void {
    this.editingUat = { ...uat };
    this.viewingUat = null;
    this.creatingUat = false;
    
    this.formInitialData = { ...uat };
    this.formConfig.submitText = 'Salvează Modificările';
    this.formConfig.sections[0].fields.find(f => f.name === 'codSiruta')!.disabled = true;

    this.errorMessage = '';
    this.successMessage = '';
    this.updateBreadcrumbs();
  }

  closeEditUat(): void {
    this.editingUat = null;
    this.errorMessage = '';
    this.successMessage = '';
    this.updateBreadcrumbs();
  }

  updateBreadcrumbs(): void {
    this.breadcrumbItems = [
      { label: 'UAT-uri', link: (this.creatingUat || this.viewingUat || this.editingUat) ? undefined : '/uats' }
    ];
    if (this.creatingUat || this.viewingUat || this.editingUat) {
      this.breadcrumbItems[0].link = '/uats';
      this.breadcrumbItems[0].action = () => {
        if (this.creatingUat) this.closeAddForm();
        if (this.viewingUat) this.closeViewUat();
        if (this.editingUat) this.closeEditUat();
      };
    }

    if (this.creatingUat) {
      this.breadcrumbItems.push({ label: 'Creare UAT' });
    } else if (this.viewingUat) {
      this.breadcrumbItems.push({ label: `Detalii: ${this.viewingUat.denumire}` });
    } else if (this.editingUat) {
      this.breadcrumbItems.push({ label: `Editare: ${this.editingUat.denumire}` });
    }
  }

  manageUat(uat: UAT): void {
    if (!uat.tenantId) {
      alert('This UAT does not have an active tenant associated with it.');
      return;
    }
    this.authService.setImpersonation(uat.tenantId).subscribe(() => {
      this.router.navigate(['/gospodarii']);
    });
  }

  saveUat(uatData: any): void {
    if (this.editingUat) {
      this.http.put(`${this.apiUrl}/${uatData.codSiruta}`, uatData).subscribe({
        next: () => {
          this.successMessage = 'UAT actualizat cu succes!';
          this.loadUats(); 
          this.closeEditUat(); 
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'A apărut o eroare la actualizarea UAT-ului.';
        }
      });
    } else {
      this.http.post(this.apiUrl, uatData).subscribe({
        next: () => { 
          this.successMessage = 'UAT creat cu succes!';
          this.loadUats(); 
          this.closeAddForm(); 
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'A apărut o eroare la crearea UAT-ului.';
        }
      });
    }
  }

  deleteUat(uat: UAT): void {
    if (confirm(`Ești sigur că vrei să ștergi UAT-ul ${uat.denumire}? Această acțiune este ireversibilă.`)) {
      this.http.delete(`${this.apiUrl}/${uat.codSiruta}`).subscribe({
        next: () => {
          this.successMessage = 'UAT șters cu succes!';
          this.loadUats();
          if (this.viewingUat?.codSiruta === uat.codSiruta) this.closeViewUat();
          if (this.editingUat?.codSiruta === uat.codSiruta) this.closeEditUat();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'A apărut o eroare la ștergerea UAT-ului.';
        }
      });
    }
  }


}
