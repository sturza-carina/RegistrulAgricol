import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ContractUtilizareService, ContractUtilizare, ContractUtilizareRequest } from '../../services/contract-utilizare.service';
import { PersoanaService } from '../../services/persoana.service';
import { Persoana } from '../../models/persoana.model';
import { UatContextService } from '../../services/uat-context.service';

import { GenericTableComponent, TableColumn, TableFilter, TableAction } from '../../components/generic-table/generic-table.component';
import { GenericFormComponent } from '../../components/generic-form/generic-form.component';
import { FormConfig } from '../../components/generic-form/generic-form.models';
import { PageHeaderComponent } from '../../components/page-header/page-header.component';
import { LayoutComponent } from '../../components/layout/layout.component';
import { BreadcrumbsComponent } from '../../components/breadcrumbs/breadcrumbs.component';

@Component({
  selector: 'app-contract-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    GenericTableComponent,
    GenericFormComponent,
    PageHeaderComponent,
    LayoutComponent,
    BreadcrumbsComponent
  ],
  templateUrl: './contract-management.component.html',
  styleUrls: ['./contract-management.component.scss']
})
export class ContractManagementComponent implements OnInit {
  user: any = null;
  selectedTenantId: string = '';

  contracts: ContractUtilizare[] = [];
  terenuri: any[] = [];
  persoane: Persoana[] = [];

  creatingContract = false;
  editingContract: ContractUtilizare | null = null;
  viewingContract: ContractUtilizare | null = null;

  successMessage = '';
  errorMessage = '';
  loadError = '';

  tipuriContract = ['ARENDA', 'COMODAT', 'CONCESIUNE', 'INCHIRIERE', 'ALTELE'];
  statusuriContract = ['ACTIV', 'EXPIRAT', 'REZILIAT', 'SUSPENDAT'];

  columns: TableColumn[] = [
    { field: 'numarContract', header: 'Nr. Contract', type: 'text', format: (val, row) => `${val} (Semnat: ${row.dataSemnare || '-'})` },
    { field: 'tipContract', header: 'Tip', type: 'badge' },
    { field: 'teren.denumire', header: 'Teren Asoc.', type: 'text', format: (val) => val || '-' },
    { field: 'dataInceput', header: 'Valabilitate', type: 'text', format: (val, row) => `${row.dataInceput || 'N/A'} → ${row.dataSfarsit || 'Nedefinit'}` },
    { field: 'pretArendaRonAn', header: 'Preț Arendă An', type: 'text', format: (val, row) => val ? `${val} RON` : (row.pretArendaGrauKgHa ? `${row.pretArendaGrauKgHa} Kg Grâu/Ha` : '-') },
    { field: 'statusContract', header: 'Status', type: 'badge', badgeClasses: { 'ACTIV': 'active', 'EXPIRAT': 'archived', 'REZILIAT': 'archived', 'SUSPENDAT': 'archived' } }
  ];

  filters: TableFilter[] = [
    { field: 'search', label: 'Căutare', type: 'search', searchFields: ['numarContract', 'teren.denumire'] },
    { field: 'tipContract', label: 'Tip Contract', type: 'select', options: [{label: 'Toate', value: ''}, ...this.tipuriContract.map(t => ({label: t, value: t}))] },
    { field: 'statusContract', label: 'Status', type: 'select', options: [{label: 'Toate', value: ''}, ...this.statusuriContract.map(s => ({label: s, value: s}))] }
  ];

  actions: TableAction[] = [
    { icon: 'view', tooltip: 'Detalii', action: (row) => this.viewContract(row) },
    { icon: 'edit', tooltip: 'Editare', action: (row) => this.editContract(row) },
    { icon: 'delete', tooltip: 'Ștergere', action: (row) => this.deleteContract(row) }
  ];

  formConfig!: FormConfig;
  formInitialData: any = {};

  breadcrumbItems = [
    { label: 'Acasă', link: '/' },
    { label: 'Contracte Utilizare', link: '/contracte' }
  ];

  constructor(
    private router: Router,
    private authService: AuthService,
    private contractService: ContractUtilizareService,
    private http: HttpClient,
    private persoanaService: PersoanaService,
    private uatContextService: UatContextService
  ) {}

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => {
      if (!user) {
        this.router.navigate(['/login']);
      } else {
        this.user = user;
        this.uatContextService.activeUat$.subscribe(uat => {
          if (uat && uat.tenantId) {
            if (this.user.role === 'ROLE_SUPER_ADMIN') {
              this.authService.setImpersonation(uat.tenantId).subscribe(() => {
                this.selectedTenantId = uat.tenantId;
                this.loadTenantData();
              });
            } else {
              this.selectedTenantId = uat.tenantId;
              this.loadTenantData();
            }
          } else {
            if (this.user.role === 'ROLE_SUPER_ADMIN') {
              this.authService.stopImpersonation().subscribe(() => {
                this.selectedTenantId = '';
                this.contracts = [];
                this.terenuri = [];
                this.persoane = [];
              });
            } else {
              this.selectedTenantId = '';
              this.contracts = [];
              this.terenuri = [];
              this.persoane = [];
            }
          }
        });
      }
    });
    this.buildFormConfig();
  }



  loadTenantData(): void {
    this.loadError = '';
    this.successMessage = '';
    this.errorMessage = '';

    this.contractService.getAllContracts().subscribe({
      next: (data) => {
        this.contracts = data;
        this.buildFormConfig();
      },
      error: (err) => {
        this.contracts = [];
        this.loadError = 'Nu s-au putut încărca contractele. ' + (err.error?.message || err.error || err.message || JSON.stringify(err));
      }
    });

    this.http.get<any[]>('/api/terenuri').subscribe({
      next: (data) => {
        this.terenuri = data;
        this.buildFormConfig();
      },
      error: (err) => console.error('Eroare terenuri', err)
    });

    this.persoanaService.getAllPersons().subscribe({
      next: (data) => {
        this.persoane = data;
        this.buildFormConfig();
      },
      error: (err) => console.error('Eroare persoane', err)
    });
  }

  buildFormConfig(): void {
    const terenOptions = this.terenuri.map(t => ({ label: `${t.denumire} (ID: ${t.id})`, value: t.id }));
    const persoanaOptions = [{ label: '-- Niciun selectat --', value: '' }, ...this.persoane.map(p => ({ label: this.getPersonDisplayName(p), value: p.id }))];

    this.formConfig = {
      sections: [
        {
          title: 'Informații Generale',
          icon: '📝',
          fields: [
            { name: 'numarContract', label: 'Număr Contract', type: 'text', required: true, width: 'half' },
            { name: 'tipContract', label: 'Tip Contract', type: 'select', required: true, width: 'half', options: this.tipuriContract.map(t => ({label: t, value: t})) },
            { name: 'terenId', label: 'Teren Asociat', type: 'select', required: true, width: 'full', options: [{ label: '-- Alegeți Teren --', value: '' }, ...terenOptions] }
          ]
        },
        {
          title: 'Părți Contractuale',
          icon: '👥',
          fields: [
            { name: 'locatorProprietarId', label: 'Proprietar Locator (Persoană)', type: 'select', width: 'half', options: persoanaOptions },
            { name: 'locatorUtilizatorId', label: 'Utilizator Locatar (Persoană)', type: 'select', width: 'half', options: persoanaOptions }
          ]
        },
        {
          title: 'Perioadă și Valabilitate',
          icon: '📅',
          fields: [
            { name: 'dataSemnare', label: 'Data Semnare', type: 'date', width: 'third' },
            { name: 'dataInceput', label: 'Dată Început', type: 'date', width: 'third' },
            { name: 'dataSfarsit', label: 'Dată Sfârșit', type: 'date', width: 'third' }
          ]
        },
        {
          title: 'Detalii Financiare',
          icon: '💰',
          fields: [
            { name: 'pretArendaRonAn', label: 'Arendă RON / An', type: 'number', width: 'half' },
            { name: 'pretArendaGrauKgHa', label: 'Arendă Grâu (Kg/Ha)', type: 'number', width: 'half' },
            { name: 'indexarePret', label: 'Indexare Preț', type: 'checkbox', width: 'full' }
          ]
        },
        {
          title: 'Status Contract',
          icon: '⚙️',
          fields: [
            { name: 'statusContract', label: 'Status Contract', type: 'select', required: true, width: 'half', options: this.statusuriContract.map(s => ({label: s, value: s})) },
            { name: 'esteActiv', label: 'Contract Activ (Valabil)', type: 'checkbox', width: 'half' },
            { name: 'motivIncetare', label: 'Motiv Încetare / Reziliere', type: 'textarea', width: 'full', showIf: (val) => val?.statusContract !== 'ACTIV' }
          ]
        }
      ],
      submitText: 'Salvează Contract',
      cancelText: 'Anulare'
    };
  }

  getPersonDisplayName(person: any): string {
    if (!person) return '-';
    if (person.personType === 'PHYSICAL_PERSON') {
      return [person.firstName, person.lastName].filter(Boolean).join(' ') || `Persoană #${person.id}`;
    }
    if (person.personType === 'LEGAL_ENTITY') {
      return person.companyName || `Persoană #${person.id}`;
    }
    return `Persoană #${person.id}`;
  }

  openAddForm(): void {
    if (!this.selectedTenantId) {
      alert('Vă rugăm să selectați mai întâi un Tenant/UAT.');
      return;
    }
    this.creatingContract = true;
    this.viewingContract = null;
    this.editingContract = null;
    this.formInitialData = {
      tipContract: 'ARENDA',
      statusContract: 'ACTIV',
      indexarePret: false,
      esteActiv: true,
      terenId: '',
      locatorProprietarId: '',
      locatorUtilizatorId: ''
    };
    this.successMessage = '';
    this.errorMessage = '';
    this.updateBreadcrumbs();
  }

  closeForm(): void {
    this.creatingContract = false;
    this.editingContract = null;
    this.errorMessage = '';
    this.updateBreadcrumbs();
  }

  viewContract(contract: ContractUtilizare): void {
    this.viewingContract = contract;
    this.creatingContract = false;
    this.editingContract = null;
    this.errorMessage = '';
    this.successMessage = '';
    this.updateBreadcrumbs();
  }

  closeViewContract(): void {
    this.viewingContract = null;
    this.updateBreadcrumbs();
  }

  editContract(contract: ContractUtilizare): void {
    this.editingContract = contract;
    this.viewingContract = null;
    this.creatingContract = false;

    this.formInitialData = {
      terenId: contract.teren?.id || '',
      locatorProprietarId: contract.locatorProprietar?.id || '',
      locatorUtilizatorId: contract.locatorUtilizator?.id || '',
      tipContract: contract.tipContract,
      numarContract: contract.numarContract,
      dataSemnare: contract.dataSemnare,
      dataInceput: contract.dataInceput,
      dataSfarsit: contract.dataSfarsit,
      pretArendaRonAn: contract.pretArendaRonAn,
      pretArendaGrauKgHa: contract.pretArendaGrauKgHa,
      indexarePret: contract.indexarePret,
      statusContract: contract.statusContract,
      motivIncetare: contract.motivIncetare,
      esteActiv: contract.esteActiv
    };

    this.errorMessage = '';
    this.successMessage = '';
    this.updateBreadcrumbs();
  }

  saveContract(formVal: any): void {
    const contractPayload: ContractUtilizareRequest = {
      terenId: +formVal.terenId,
      locatorProprietarId: formVal.locatorProprietarId ? +formVal.locatorProprietarId : null,
      locatorUtilizatorId: formVal.locatorUtilizatorId ? +formVal.locatorUtilizatorId : null,
      utilizatorOperareId: null,
      tipContract: formVal.tipContract,
      numarContract: formVal.numarContract,
      dataSemnare: formVal.dataSemnare || null,
      dataInceput: formVal.dataInceput || null,
      dataSfarsit: formVal.dataSfarsit || null,
      pretArendaRonAn: formVal.pretArendaRonAn ? +formVal.pretArendaRonAn : null,
      pretArendaGrauKgHa: formVal.pretArendaGrauKgHa ? +formVal.pretArendaGrauKgHa : null,
      indexarePret: formVal.indexarePret,
      statusContract: formVal.statusContract,
      motivIncetare: formVal.motivIncetare || null,
      esteActiv: formVal.esteActiv
    };

    if (this.editingContract && this.editingContract.id) {
      this.contractService.updateContract(this.editingContract.id, contractPayload).subscribe({
        next: () => {
          this.successMessage = 'Contractul a fost actualizat cu succes!';
          this.loadTenantData();
          this.closeForm();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || err.error || 'A apărut o eroare la actualizarea contractului.';
        }
      });
    } else {
      this.contractService.createContract(contractPayload).subscribe({
        next: () => {
          this.successMessage = 'Contractul a fost înregistrat cu succes!';
          this.loadTenantData();
          this.closeForm();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || err.error || 'A apărut o eroare la crearea contractului.';
        }
      });
    }
  }

  deleteContract(contract: ContractUtilizare): void {
    if (contract.id && confirm(`Sunteți sigur că doriți să ștergeți contractul nr. ${contract.numarContract}?`)) {
      this.contractService.deleteContract(contract.id).subscribe({
        next: () => {
          this.successMessage = 'Contractul a fost șters cu succes!';
          this.loadTenantData();
          if (this.viewingContract?.id === contract.id) this.closeViewContract();
          if (this.editingContract?.id === contract.id) this.closeForm();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || err.error || 'A apărut o eroare la ștergerea contractului.';
        }
      });
    }
  }

  updateBreadcrumbs(): void {
    this.breadcrumbItems = [
      { label: 'Acasă', link: '/' },
      { label: 'Contracte Utilizare', link: '/contracte' }
    ];
    
    if (this.creatingContract) {
      this.breadcrumbItems.push({ label: 'Creare Contract', link: '' });
    } else if (this.editingContract) {
      this.breadcrumbItems.push({ label: `Editare: ${this.editingContract.numarContract}`, link: '' });
    } else if (this.viewingContract) {
      this.breadcrumbItems.push({ label: `Detalii: ${this.viewingContract.numarContract}`, link: '' });
    }
  }
}
