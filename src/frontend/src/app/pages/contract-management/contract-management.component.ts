import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { PersonSearchModalComponent } from '../../components/person-search-modal/person-search-modal.component';
import { ContractUtilizareService, ContractUtilizare, ContractUtilizareRequest, PersoanaRef } from '../../services/contract-utilizare.service';
import { PersoanaService } from '../../services/persoana.service';
import { Persoana } from '../../models/persoana.model';

@Component({
  selector: 'app-contract-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, SidebarComponent, PersonSearchModalComponent],
  templateUrl: './contract-management.component.html',
  styleUrls: ['./contract-management.component.scss']
})
export class ContractManagementComponent implements OnInit {
  user: any = null;
  tenants: any[] = [];
  selectedTenantId: string = '';

  contracts: ContractUtilizare[] = [];
  filteredContracts: ContractUtilizare[] = [];
  terenuri: any[] = [];
  persoane: Persoana[] = [];

  contractForm: FormGroup;
  creatingContract = false;
  editingContract: ContractUtilizare | null = null;
  viewingContract: ContractUtilizare | null = null;

  searchTerm = '';
  tipContractFilter = 'Toate';
  statusContractFilter = 'Toate';

  successMessage = '';
  errorMessage = '';
  loadError = '';

  // Modal state
  showLocatorProprietarModal = false;
  showLocatorUtilizatorModal = false;
  selectedLocatorProprietar: Persoana | PersoanaRef | null = null;
  selectedLocatorUtilizator: Persoana | PersoanaRef | null = null;

  tipuriContract = ['ARENDA', 'COMODAT', 'CONCESIUNE', 'INCHIRIERE', 'ALTELE'];
  statusuriContract = ['ACTIV', 'EXPIRAT', 'REZILIAT', 'SUSPENDAT'];

  constructor(
    private router: Router,
    private authService: AuthService,
    private contractService: ContractUtilizareService,
    private http: HttpClient,
    private persoanaService: PersoanaService,
    private fb: FormBuilder
  ) {
    this.contractForm = this.fb.group({
      terenId: ['', [Validators.required]],
      locatorProprietarId: [''],
      locatorUtilizatorId: [''],
      utilizatorOperareId: [''],
      tipContract: ['ARENDA', [Validators.required]],
      numarContract: ['', [Validators.required]],
      dataSemnare: [''],
      dataInceput: [''],
      dataSfarsit: [''],
      pretArendaRonAn: [''],
      pretArendaGrauKgHa: [''],
      indexarePret: [false],
      statusContract: ['ACTIV', [Validators.required]],
      motivIncetare: [''],
      esteActiv: [true]
    });
  }

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => {
      if (!user) {
        this.router.navigate(['/login']);
      } else {
        this.user = user;
        if (this.user.role === 'ROLE_SUPER_ADMIN') {
          this.loadTenants();
        }

        // If already impersonating a tenant, set it
        const activeTenant = this.authService.currentTenantId;
        if (activeTenant && activeTenant !== 'public') {
          this.selectedTenantId = activeTenant;
          this.loadTenantData();
        }
      }
    });
  }

  loadTenants(): void {
    this.http.get<any[]>('/api/tenants').subscribe({
      next: (data) => {
        this.tenants = data;
      },
      error: (err) => {
        console.error('Eroare la încărcarea tenantilor', err);
      }
    });
  }

  onTenantChange(): void {
    if (this.selectedTenantId) {
      this.authService.setImpersonation(this.selectedTenantId);
      this.loadTenantData();
    } else {
      this.authService.stopImpersonation();
      this.contracts = [];
      this.filteredContracts = [];
      this.terenuri = [];
      this.persoane = [];
    }
  }

  loadTenantData(): void {
    this.loadError = '';
    this.successMessage = '';
    this.errorMessage = '';

    // Load contracts
    this.contractService.getAllContracts().subscribe({
      next: (data) => {
        this.contracts = data;
        this.applyFilter();
      },
      error: (err) => {
        this.contracts = [];
        this.filteredContracts = [];
        this.loadError = 'Nu s-au putut încărca contractele pentru tenantul selectat.';
      }
    });

    // Load land records (terenuri)
    this.http.get<any[]>('/api/terenuri').subscribe({
      next: (data) => {
        this.terenuri = data;
      },
      error: (err) => console.error('Eroare la încărcarea terenurilor', err)
    });

    // Load persons
    this.persoanaService.getAllPersons().subscribe({
      next: (data) => {
        this.persoane = data;
      },
      error: (err) => console.error('Eroare la încărcarea persoanelor', err)
    });
  }

  onSearch(event: any): void {
    this.searchTerm = event.target.value.toLowerCase();
    this.applyFilter();
  }

  applyFilter(): void {
    this.filteredContracts = this.contracts.filter(c => {
      const matchesSearch = c.numarContract.toLowerCase().includes(this.searchTerm) ||
                            (c.teren?.denumire || '').toLowerCase().includes(this.searchTerm);
      const matchesTip = this.tipContractFilter === 'Toate' || c.tipContract === this.tipContractFilter;
      const matchesStatus = this.statusContractFilter === 'Toate' || c.statusContract === this.statusContractFilter;
      return matchesSearch && matchesTip && matchesStatus;
    });
  }

  openAddForm(): void {
    if (!this.selectedTenantId) {
      alert('Vă rugăm să selectați mai întâi un Tenant/UAT.');
      return;
    }
    this.creatingContract = true;
    this.viewingContract = null;
    this.editingContract = null;
    this.selectedLocatorProprietar = null;
    this.selectedLocatorUtilizator = null;
    this.contractForm.reset({
      tipContract: 'ARENDA',
      statusContract: 'ACTIV',
      indexarePret: false,
      esteActiv: true,
      utilizatorOperareId: this.user?.id || ''
    });
    this.successMessage = '';
    this.errorMessage = '';
  }

  closeAddForm(): void {
    this.creatingContract = false;
    this.selectedLocatorProprietar = null;
    this.selectedLocatorUtilizator = null;
    this.errorMessage = '';
  }

  viewContract(contract: ContractUtilizare): void {
    this.viewingContract = contract;
    this.creatingContract = false;
    this.editingContract = null;
    this.errorMessage = '';
    this.successMessage = '';
  }

  closeViewContract(): void {
    this.viewingContract = null;
  }

  editContract(contract: ContractUtilizare): void {
    this.editingContract = contract;
    this.viewingContract = null;
    this.creatingContract = false;

    // Set selected persons if they exist
    if (contract.locatorProprietar) {
      this.selectedLocatorProprietar = contract.locatorProprietar;
    }
    if (contract.locatorUtilizator) {
      this.selectedLocatorUtilizator = contract.locatorUtilizator;
    }

    this.contractForm.patchValue({
      terenId: contract.teren?.id,
      locatorProprietarId: contract.locatorProprietar?.id || '',
      locatorUtilizatorId: contract.locatorUtilizator?.id || '',
      utilizatorOperareId: contract.utilizatorOperare?.id || '',
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
    });

    this.errorMessage = '';
    this.successMessage = '';
  }

  closeEditContract(): void {
    this.editingContract = null;
    this.selectedLocatorProprietar = null;
    this.selectedLocatorUtilizator = null;
    this.errorMessage = '';
  }

  saveContract(): void {
    if (this.contractForm.invalid) {
      this.errorMessage = 'Vă rugăm să completați toate câmpurile obligatorii.';
      return;
    }

    const formVal = this.contractForm.value;
    const contractPayload: ContractUtilizareRequest = {
      terenId: +formVal.terenId,
      locatorProprietarId: formVal.locatorProprietarId ? +formVal.locatorProprietarId : null,
      locatorUtilizatorId: formVal.locatorUtilizatorId ? +formVal.locatorUtilizatorId : null,
      utilizatorOperareId: formVal.utilizatorOperareId ? +formVal.utilizatorOperareId : null,
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
          this.closeEditContract();
        },
        error: (err) => {
          this.errorMessage = err.error || 'A apărut o eroare la actualizarea contractului.';
        }
      });
    } else {
      this.contractService.createContract(contractPayload).subscribe({
        next: () => {
          this.successMessage = 'Contractul a fost înregistrat cu succes!';
          this.loadTenantData();
          this.closeAddForm();
        },
        error: (err) => {
          this.errorMessage = err.error || 'A apărut o eroare la crearea contractului.';
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
          if (this.editingContract?.id === contract.id) this.closeEditContract();
        },
        error: (err) => {
          this.errorMessage = err.error || 'A apărut o eroare la ștergerea contractului.';
        }
      });
    }
  }

  getPersonDisplayName(person: Persoana | PersoanaRef | null | undefined): string {
    if (!person) return '-';

    if (person.personType === 'PHYSICAL_PERSON') {
      const physical = person as { id?: number; firstName?: string; lastName?: string };
      return [physical.firstName, physical.lastName].filter(Boolean).join(' ') || `Persoană #${person.id}`;
    }

    if (person.personType === 'LEGAL_ENTITY') {
      const legal = person as { id?: number; companyName?: string };
      return legal.companyName || `Persoană #${person.id}`;
    }

    return `Persoană #${person.id}`;
  }

  // Modal methods for Locator Proprietar
  openLocatorProprietarModal(): void {
    this.showLocatorProprietarModal = true;
  }

  closeLocatorProprietarModal(): void {
    this.showLocatorProprietarModal = false;
  }

  selectLocatorProprietar(person: Persoana): void {
    this.selectedLocatorProprietar = person;
    this.contractForm.patchValue({
      locatorProprietarId: person.id
    });
    this.closeLocatorProprietarModal();
  }

  // Modal methods for Locator Utilizator
  openLocatorUtilizatorModal(): void {
    this.showLocatorUtilizatorModal = true;
  }

  closeLocatorUtilizatorModal(): void {
    this.showLocatorUtilizatorModal = false;
  }

  selectLocatorUtilizator(person: Persoana): void {
    this.selectedLocatorUtilizator = person;
    this.contractForm.patchValue({
      locatorUtilizatorId: person.id
    });
    this.closeLocatorUtilizatorModal();
  }

  clearLocatorProprietar(): void {
    this.selectedLocatorProprietar = null;
    this.contractForm.patchValue({
      locatorProprietarId: ''
    });
  }

  clearLocatorUtilizator(): void {
    this.selectedLocatorUtilizator = null;
    this.contractForm.patchValue({
      locatorUtilizatorId: ''
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
