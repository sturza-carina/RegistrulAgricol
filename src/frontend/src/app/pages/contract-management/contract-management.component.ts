import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { ContractUtilizareService, ContractUtilizare } from '../../services/contract-utilizare.service';

@Component({
  selector: 'app-contract-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, SidebarComponent],
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
  users: any[] = []; // System users for selection
  
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

  tipuriContract = ['ARENDA', 'COMODAT', 'CONCESIUNE', 'INCHIRIERE', 'ALTELE'];
  statusuriContract = ['ACTIV', 'EXPIRAT', 'REZILIAT', 'SUSPENDAT'];

  constructor(
    private router: Router,
    private authService: AuthService,
    private contractService: ContractUtilizareService,
    private http: HttpClient,
    private fb: FormBuilder
  ) {
    this.contractForm = this.fb.group({
      terenId: ['', [Validators.required]],
      locatorProprietarId: [''],
      locatorUtilizatorId: [''],
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
      this.users = [];
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

    // Load users
    this.http.get<any[]>('/api/users').subscribe({
      next: (data) => {
        this.users = data;
      },
      error: (err) => console.error('Eroare la încărcarea utilizatorilor', err)
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
    this.contractForm.reset({
      tipContract: 'ARENDA',
      statusContract: 'ACTIV',
      indexarePret: false,
      esteActiv: true
    });
    this.successMessage = '';
    this.errorMessage = '';
  }

  closeAddForm(): void {
    this.creatingContract = false;
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
    
    this.contractForm.patchValue({
      terenId: contract.teren?.id,
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
    });
    
    this.errorMessage = '';
    this.successMessage = '';
  }

  closeEditContract(): void {
    this.editingContract = null;
    this.errorMessage = '';
  }

  saveContract(): void {
    if (this.contractForm.invalid) {
      this.errorMessage = 'Vă rugăm să completați toate câmpurile obligatorii.';
      return;
    }

    const formVal = this.contractForm.value;
    const contractPayload: ContractUtilizare = {
      teren: { id: +formVal.terenId },
      locatorProprietar: formVal.locatorProprietarId ? { id: +formVal.locatorProprietarId } : null,
      locatorUtilizator: formVal.locatorUtilizatorId ? { id: +formVal.locatorUtilizatorId } : null,
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

  getUserDisplayName(userObj: any): string {
    if (!userObj) return '-';
    return userObj.nume ? `${userObj.nume} (${userObj.username})` : userObj.username;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
