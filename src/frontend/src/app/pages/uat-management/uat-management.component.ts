import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';


interface UAT {
  codSiruta: string;
  denumire: string;
  judet: string;
  tipUat: string;
  isActive: boolean;
}

@Component({
  selector: 'app-uat-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
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
  isEditMode = false;
  showModal = false;
  showDeleteConfirm = false;
  selectedUat: UAT | null = null;
  searchTerm = '';
  loadError = '';

  private apiUrl = '/api/uats';

  constructor(
    private router: Router,
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
  }

  // Navigation
  goToTenants(): void    { this.router.navigate(['/super-admin']); }
  goToCreateTenant(): void { this.router.navigate(['/tenants/new']); }
  goToUsers(): void      { this.router.navigate(['/user-management']); }
  goToUats(): void       { this.router.navigate(['/uats']); }
  goToSettings(): void   { }
  logout(): void         { this.authService.logout(); this.router.navigate(['/login']); }

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
    this.filteredUats = this.uats.filter(uat =>
      uat.denumire.toLowerCase().includes(this.searchTerm) ||
      uat.judet.toLowerCase().includes(this.searchTerm)
    );
  }

  openAddModal(): void {
    this.isEditMode = false;
    this.uatForm.reset({ tipUat: 'Comună', isActive: true });
    this.uatForm.get('codSiruta')?.enable();
    this.showModal = true;
  }

  openEditModal(uat: UAT): void {
    this.isEditMode = true;
    this.selectedUat = uat;
    this.uatForm.patchValue(uat);
    this.uatForm.get('codSiruta')?.disable();
    this.showModal = true;
  }

  closeModal(): void { this.showModal = false; this.selectedUat = null; }

  // saveUat(): void {
  //   if (this.uatForm.invalid) return;
  //   const uatData = this.uatForm.getRawValue();
  //   if (this.isEditMode) {
  //     this.http.put(`${this.apiUrl}/${uatData.codSiruta}`, uatData).subscribe(() => {
  //       this.loadUats(); this.closeModal();
  //     });
  //   } else {
  //     this.http.post(this.apiUrl, uatData).subscribe(() => {
  //       this.loadUats(); this.closeModal();
  //     });
  //   }
  // }
  saveUat(): void {
    if (this.uatForm.invalid) return;
    const uatData = this.uatForm.getRawValue();
    if (this.isEditMode) {
      this.http.put(`${this.apiUrl}/${uatData.codSiruta}`, uatData).subscribe({
        next: () => { this.loadUats(); this.closeModal(); },
        error: (err) => console.error('Update failed:', err)
      });
    } else {
      this.http.post(this.apiUrl, uatData).subscribe({
        next: () => { this.loadUats(); this.closeModal(); },
        error: (err) => console.error('Create failed:', err)
      });
    }
  }

  confirmDelete(uat: UAT): void { this.selectedUat = uat; this.showDeleteConfirm = true; }

  // deleteUat(): void {
  //   if (this.selectedUat) {
  //     this.http.delete(`${this.apiUrl}/${this.selectedUat.codSiruta}`).subscribe(() => {
  //       this.loadUats();
  //       this.showDeleteConfirm = false;
  //       this.selectedUat = null;
  //     });
  //   }
  // }
  deleteUat(): void {
    if (this.selectedUat) {
      this.http.delete(`${this.apiUrl}/${this.selectedUat.codSiruta}`).subscribe({
        next: () => {
          this.loadUats();
          this.showDeleteConfirm = false;
          this.selectedUat = null;
        },
        error: (err) => console.error('Delete failed:', err)
      });
    }
  }
}
