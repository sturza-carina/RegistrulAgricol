import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';


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
  imports: [CommonModule, ReactiveFormsModule, FormsModule, SidebarComponent],
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

  user: any = null;
  uats: UAT[] = [];
  filteredUats: UAT[] = [];
  uatForm: FormGroup;
  creatingUat = false;
  viewingUat: UAT | null = null;
  editingUat: UAT | null = null;

  searchTerm = '';
  tipUatFilter = 'Toate';
  loadError = '';
  successMessage = '';
  errorMessage = '';

  private apiUrl = '/api/uats';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private http: HttpClient,
    private fb: FormBuilder
  ) {
    this.uatForm = this.fb.group({
      codSiruta: ['', [Validators.required]],
      denumire:  ['', [Validators.required]],
      judet:     ['', [Validators.required]],
      tipUat:    ['Comună', [Validators.required]],
      isActive:  [true]
    });
  }

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => {
      if (!user) {
        this.router.navigate(['/login']);
      } else {
        this.user = user;
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
        this.applyFilter();
      },
      error: () => {
        this.uats = [];
        this.filteredUats = [];
        this.loadError = 'Nu s-au putut încărca UAT-urile. Verifică dacă backend-ul rulează.';
      }
    });
  }

  onSearch(event: any): void {
    this.searchTerm = event.target.value.toLowerCase();
    this.applyFilter();
  }

  applyFilter(): void {
    this.filteredUats = this.uats.filter(uat => {
      const matchesSearch = uat.denumire.toLowerCase().includes(this.searchTerm) || uat.judet.toLowerCase().includes(this.searchTerm);
      const matchesTip = this.tipUatFilter === 'Toate' || uat.tipUat === this.tipUatFilter;
      return matchesSearch && matchesTip;
    });
  }

  openAddForm(): void {
    this.creatingUat = true;
    this.viewingUat = null;
    this.editingUat = null;
    this.uatForm.reset({ tipUat: 'Comună', isActive: true });
    this.uatForm.get('codSiruta')?.enable();
    this.successMessage = '';
    this.errorMessage = '';
  }

  closeAddForm(): void {
    this.creatingUat = false;
    this.errorMessage = '';
    this.successMessage = '';
  }

  viewUat(uat: UAT): void {
    this.viewingUat = uat;
    this.creatingUat = false;
    this.editingUat = null;
    this.errorMessage = '';
    this.successMessage = '';
  }

  closeViewUat(): void {
    this.viewingUat = null;
  }

  editUat(uat: UAT): void {
    this.editingUat = { ...uat };
    this.viewingUat = null;
    this.creatingUat = false;
    this.uatForm.patchValue(uat);
    this.uatForm.get('codSiruta')?.disable();
    this.errorMessage = '';
    this.successMessage = '';
  }

  closeEditUat(): void {
    this.editingUat = null;
    this.errorMessage = '';
    this.successMessage = '';
  }

  manageUat(uat: UAT): void {
    if (!uat.tenantId) {
      alert('This UAT does not have an active tenant associated with it.');
      return;
    }
    this.authService.setImpersonation(uat.tenantId);
    this.router.navigate(['/gospodarii']);
  }

  saveUat(): void {
    if (this.uatForm.invalid) {
      this.errorMessage = 'Vă rugăm să completați toate câmpurile obligatorii.';
      return;
    }
    const uatData = this.uatForm.getRawValue();
    
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

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
