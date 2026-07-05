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
import { ActiveUatBannerComponent } from '../../components/active-uat-banner/active-uat-banner.component';

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
    BreadcrumbsComponent,
    ActiveUatBannerComponent
  ],
  templateUrl: './contract-management.component.html',
  styleUrls: ['./contract-management.component.scss']
})
export class ContractManagementComponent implements OnInit {
  user: any = null;
  selectedTenantId: string = '';
  activeUat: any = null;

  contracts: ContractUtilizare[] = [];
  parcele: any[] = [];
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
    { field: 'numarContract', header: 'Nr. Contract', type: 'text', format: (val, row) => `${val} (Semnat: ${row.dataSemnare || '-'})${row.semnatElectronic ? ' ✓ Semnat electronic' : ''}` },
    { field: 'tipContract', header: 'Tip', type: 'badge' },
    { field: 'parcela.denumire', header: 'Parcela Asoc.', type: 'text', format: (val) => val || '-' },
    { field: 'dataInceput', header: 'Valabilitate', type: 'text', format: (val, row) => `${row.dataInceput || 'N/A'} → ${row.dataSfarsit || 'Nedefinit'}` },
    { field: 'pretArendaRonAn', header: 'Preț Arendă An', type: 'text', format: (val, row) => val ? `${val} RON` : (row.pretArendaGrauKgHa ? `${row.pretArendaGrauKgHa} Kg Grâu/Ha` : '-') },
    { field: 'statusContract', header: 'Status', type: 'badge', badgeClasses: { 'ACTIV': 'active', 'EXPIRAT': 'archived', 'REZILIAT': 'archived', 'SUSPENDAT': 'archived' } }
  ];

  filters: TableFilter[] = [
    { field: 'search', label: 'Căutare', type: 'search', searchFields: ['numarContract', 'parcela.denumire'] },
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
          if (uat) {
            this.activeUat = uat;
            this.selectedTenantId = this.authService.currentTenantId;
            this.loadTenantData();
          } else {
            this.activeUat = null;
            this.selectedTenantId = '';
            this.contracts = [];
            this.parcele = [];
            this.persoane = [];
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

    const activeUat = this.uatContextService.getActiveUat();
    const uatCode = activeUat ? activeUat.codSiruta : undefined;

    this.contractService.getAllContracts(uatCode, 0, 1000).subscribe({
      next: (response) => {
        this.contracts = response.content;
        this.buildFormConfig();
      },
      error: (err) => {
        this.contracts = [];
        this.loadError = 'Nu s-au putut încărca contractele. ' + (err.error?.message || err.error || err.message || JSON.stringify(err));
      }
    });

    this.http.get<any>('/api/parcele', { params: { size: '1000' } }).subscribe({
      next: (data) => {
        this.parcele = data.content || data;
        this.buildFormConfig();
      },
      error: (err) => console.error('Eroare parcele', err)
    });

    this.persoanaService.getAllPersons('', '', 0, 1000).subscribe({
      next: (response) => {
        this.persoane = response.content;
        this.buildFormConfig();
      },
      error: (err) => console.error('Eroare persoane', err)
    });
  }

  buildFormConfig(): void {
    const parcelaOptions = this.parcele.map(p => ({ label: `${p.denumire} (ID: ${p.id})`, value: p.id }));
    const persoanaOptions = [{ label: '-- Niciun selectat --', value: '' }, ...this.persoane.map(p => ({ label: this.getPersonDisplayName(p), value: p.id }))];

    this.formConfig = {
      sections: [
        {
          title: 'Informații Generale',
          icon: '📝',
          fields: [
            { name: 'numarContract', label: 'Număr Contract', type: 'text', required: true, width: 'half' },
            { name: 'tipContract', label: 'Tip Contract', type: 'select', required: true, width: 'half', options: this.tipuriContract.map(t => ({label: t, value: t})) },
            { name: 'parcelaId', label: 'Parcela Asociată', type: 'select', required: true, width: 'full', options: [{ label: '-- Alegeți Parcela --', value: '' }, ...parcelaOptions] }
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
      parcelaId: '',
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
      parcelaId: contract.parcela?.id || '',
      locatorProprietarId: contract.locatorProprietar?.id || '',
      locatorUtilizatorId: contract.locatorUtilizator?.id,
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
      parcelaId: +formVal.parcelaId,
      locatorProprietarId: formVal.locatorProprietarId ? +formVal.locatorProprietarId : null,
      locatorUtilizatorId: formVal.locatorUtilizatorId ? +formVal.locatorUtilizatorId : null,
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

  signingContract: ContractUtilizare | null = null;
  private isDrawingSemnatura = false;
  private lastSemnaturaX = 0;
  private lastSemnaturaY = 0;

  startSemnare(contract: ContractUtilizare): void {
    if (!contract.id) return;
    this.errorMessage = '';
    this.successMessage = '';
    this.signingContract = contract;
    setTimeout(() => this.clearSignatureCanvas(), 0);
  }

  cancelSemnare(): void {
    this.signingContract = null;
  }

  private getSignatureCanvas(): HTMLCanvasElement | null {
    return document.getElementById('signatureCanvas') as HTMLCanvasElement | null;
  }

  clearSignatureCanvas(): void {
    const canvas = this.getSignatureCanvas();
    const ctx = canvas?.getContext('2d');
    if (canvas && ctx) {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
    }
  }

  onSignaturePointerDown(event: PointerEvent): void {
    const canvas = event.target as HTMLCanvasElement;
    const rect = canvas.getBoundingClientRect();
    this.isDrawingSemnatura = true;
    this.lastSemnaturaX = event.clientX - rect.left;
    this.lastSemnaturaY = event.clientY - rect.top;
  }

  onSignaturePointerMove(event: PointerEvent): void {
    if (!this.isDrawingSemnatura) return;
    const canvas = event.target as HTMLCanvasElement;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    const rect = canvas.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    ctx.strokeStyle = '#1e293b';
    ctx.lineWidth = 2;
    ctx.lineCap = 'round';
    ctx.beginPath();
    ctx.moveTo(this.lastSemnaturaX, this.lastSemnaturaY);
    ctx.lineTo(x, y);
    ctx.stroke();
    this.lastSemnaturaX = x;
    this.lastSemnaturaY = y;
  }

  onSignaturePointerUp(): void {
    this.isDrawingSemnatura = false;
  }

  confirmSemnare(): void {
    const canvas = this.getSignatureCanvas();
    const contractId = this.signingContract?.id;
    if (!canvas || !contractId) return;

    const semnaturaImagineBase64 = canvas.toDataURL('image/png');
    this.errorMessage = '';
    this.successMessage = '';
    this.contractService.semneazaContract(contractId, semnaturaImagineBase64).subscribe({
      next: (updated) => {
        this.successMessage = 'Contractul a fost semnat electronic cu succes!';
        this.signingContract = null;
        this.loadTenantData();
        if (this.viewingContract?.id === contractId) {
          this.viewingContract = updated;
        }
      },
      error: (err) => {
        this.errorMessage = err.error?.message || err.error || 'A apărut o eroare la semnarea electronică a contractului.';
        this.signingContract = null;
      }
    });
  }

  downloadDocumentSemnat(contract: ContractUtilizare): void {
    if (!contract.id) return;
    this.contractService.downloadDocumentSemnat(contract.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `contract-${contract.numarContract}-semnat.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => {
        this.errorMessage = 'Nu s-a putut descărca documentul semnat.';
      }
    });
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
