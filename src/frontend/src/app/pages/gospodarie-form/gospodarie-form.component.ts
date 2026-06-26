import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { GospodarieService } from '../../services/gospodarie.service';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { UatService } from '../../services/uat.service';

@Component({
  selector: 'app-gospodarie-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, SidebarComponent],
  templateUrl: './gospodarie-form.component.html',
  styleUrls: ['./gospodarie-form.component.css']
})
export class GospodarieFormComponent implements OnInit {
  gospodarieForm: FormGroup;
  isEditMode = false;
  gospodarieId: number | null = null;
  judete: string[] = [];
  localitati: any[] = [];
  selectedJudet = '';
  user: any = null;

  constructor(
    private fb: FormBuilder,
    private gospodarieService: GospodarieService,
    private uatService: UatService,
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
      }
    });
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
    if (this.gospodarieForm.invalid) {
      this.gospodarieForm.markAllAsTouched();
      return;
    }

    const formData = this.gospodarieForm.value;
    
    // Sanitize numeric fields that might be empty strings to avoid Java Jackson parse errors
    if (formData.adresa) {
      if (formData.adresa.floor === '') formData.adresa.floor = null;
      if (formData.adresa.apartmentNumber === '') formData.adresa.apartmentNumber = null;
    }

    const selectedUatId = formData.uatId ? Number(formData.uatId) : null;
    const selectedLocalitate = selectedUatId
      ? this.localitati.find(localitate => localitate.id === selectedUatId)
      : null;

    if (selectedLocalitate && formData.adresa) {
      formData.adresa.localitate = selectedLocalitate.denumire;
    }

    const payload = {
      ...formData,
      uatId: selectedUatId,
      uat: selectedUatId ? { id: selectedUatId } : null
    };

    if (this.isEditMode && this.gospodarieId) {
      this.gospodarieService.updateGospodarie(this.gospodarieId, payload).subscribe({
        next: () => this.router.navigate(['/gospodarii']),
        error: (err) => {
          console.error(err);
          alert('Eroare la salvare');
        }
      });
    } else {
      this.gospodarieService.createGospodarie(payload).subscribe({
        next: () => this.router.navigate(['/gospodarii']),
        error: (err) => {
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
