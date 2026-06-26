import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { GospodarieService } from '../../services/gospodarie.service';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { UatService } from '../../services/uat.service';
import { UatContextService } from '../../services/uat-context.service';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';

@Component({
  selector: 'app-gospodarie-form',
  standalone: true,
  imports: [CommonModule, RouterLink, SidebarComponent, BreadcrumbsComponent, ReactiveFormsModule],
  templateUrl: './gospodarie-form.component.html'
})
export class GospodarieFormComponent implements OnInit {
  isEditMode = false;
  gospodarieId: number | null = null;
  judete: string[] = [];
  localitati: any[] = [];
  selectedJudet = '';
  isSaving = false;
  gospodarieForm: FormGroup;
  breadcrumbItems: BreadcrumbItem[] = [];

  constructor(
    private gospodarieService: GospodarieService,
    private uatService: UatService,
    private uatContextService: UatContextService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.gospodarieForm = this.fb.group({
      codGospodarie: ['', Validators.required],
      tipGospodarie: ['INDIVIDUALA', Validators.required],
      activa: [true],
      uatId: [null, Validators.required],
      adresa: this.fb.group({
        county: [''],
        localitate: [''],
        street: [''],
        streetNumber: [''],
        building: [''],
        staircase: [''],
        floor: [''],
        apartmentNumber: [''],
        postalCode: ['']
      })
    });
  }

  ngOnInit() {
    this.loadJudete();
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.isEditMode = true;
        this.gospodarieId = +id;
        this.loadGospodarie(this.gospodarieId);
      } else {
        const activeUat = this.uatContextService.getActiveUat();
        if (activeUat) {
          const county = activeUat.judet;
          this.gospodarieForm.patchValue({
            uatId: activeUat.id,
            adresa: {
              county: county,
              localitate: activeUat.denumire
            }
          });
          if (county) {
            this.onJudetChange({ target: { value: county } }, activeUat.id);
          }
        }
      }
      this.updateBreadcrumbs();
    });
  }

  updateBreadcrumbs() {
    this.breadcrumbItems = [
      { label: 'Gospodării', link: '/gospodarii' },
      { label: this.isEditMode ? 'Editare' : 'Adăugare' }
    ];
  }

  loadJudete() {
    this.uatService.getJudete().subscribe(data => {
      this.judete = data;
    });
  }

  loadGospodarie(id: number) {
    this.gospodarieService.getGospodarieById(id).subscribe(data => {
      const county = data.uat?.judet ?? data.adresa?.county ?? '';

      this.gospodarieForm.patchValue({
        codGospodarie: data.codGospodarie,
        tipGospodarie: data.tipGospodarie,
        activa: data.activa,
        uatId: data.uat?.id ?? null,
        adresa: {
          county,
          localitate: data.adresa?.localitate ?? data.uat?.denumire ?? '',
          street: data.adresa?.street ?? '',
          streetNumber: data.adresa?.streetNumber ?? '',
          building: data.adresa?.building ?? '',
          staircase: data.adresa?.staircase ?? '',
          floor: data.adresa?.floor ?? '',
          apartmentNumber: data.adresa?.apartmentNumber ?? '',
          postalCode: data.adresa?.postalCode ?? ''
        }
      });

      if (county) {
        this.onJudetChange({ target: { value: county } }, data.uat?.id ?? null);
      }
    });
  }

  onJudetChange(event: any, selectedUatId: number | null = null) {
    const judet = event?.target?.value ?? '';
    this.selectedJudet = judet;
    this.localitati = [];

    this.gospodarieForm.get('uatId')?.reset();
    this.gospodarieForm.get('adresa.county')?.setValue(judet);
    this.gospodarieForm.get('adresa.localitate')?.reset();

    if (!judet) {
      return;
    }

    this.uatService.getLocalitatiByJudet(judet).subscribe(data => {
      this.localitati = data;

      if (selectedUatId) {
        this.gospodarieForm.get('uatId')?.setValue(selectedUatId);
        const selectedLocalitate = this.localitati.find(localitate => localitate.id === selectedUatId);
        if (selectedLocalitate) {
          this.gospodarieForm.get('adresa.localitate')?.setValue(selectedLocalitate.denumire);
        }
      }
    });
  }

  onSubmit() {
    if (this.gospodarieForm.invalid) return;
    const formData = this.gospodarieForm.value;
    this.isSaving = true;

    const selectedUatId = formData.uatId ? Number(formData.uatId) : null;
    const selectedLocalitate = selectedUatId
      ? this.localitati.find(localitate => localitate.id === selectedUatId)
      : null;

    if (selectedLocalitate && formData.adresa) {
      formData.adresa.localitate = selectedLocalitate.denumire;
    }

    const payload = {
      codGospodarie: formData.codGospodarie,
      tipGospodarie: formData.tipGospodarie,
      activa: formData.activa,
      uatId: selectedUatId,
      uat: selectedUatId ? { id: selectedUatId } : null,
      adresa: {
        county: formData.adresa.county,
        localitate: formData.adresa.localitate,
        street: formData.adresa.street,
        streetNumber: formData.adresa.streetNumber,
        building: formData.adresa.building,
        staircase: formData.adresa.staircase,
        floor: formData.adresa.floor ? Number(formData.adresa.floor) : undefined,
        apartmentNumber: formData.adresa.apartmentNumber ? Number(formData.adresa.apartmentNumber) : undefined,
        postalCode: formData.adresa.postalCode
      }
    };

    if (this.isEditMode && this.gospodarieId) {
      this.gospodarieService.updateGospodarie(this.gospodarieId, payload as any).subscribe({
        next: () => {
          this.isSaving = false;
          this.router.navigate(['/gospodarii']);
        },
        error: (err) => {
          this.isSaving = false;
          console.error(err);
          alert('Eroare la salvare');
        }
      });
    } else {
      this.gospodarieService.createGospodarie(payload as any).subscribe({
        next: () => {
          this.isSaving = false;
          this.router.navigate(['/gospodarii']);
        },
        error: (err) => {
          this.isSaving = false;
          console.error(err);
          alert('Eroare la salvare');
        }
      });
    }
  }

  cancel() {
    this.router.navigate(['/gospodarii']);
  }
}
