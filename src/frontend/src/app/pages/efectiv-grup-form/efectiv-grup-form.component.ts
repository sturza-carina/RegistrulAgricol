import { Component, OnInit, Input, Output, EventEmitter, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { UatContextService } from '../../services/uat-context.service';
import { AnimalService } from '../../services/animal.service';
import { GospodarieService } from '../../services/gospodarie.service';
import { PersoanaService } from '../../services/persoana.service';
import { EfectivGrup, SpecieAnimal } from '../../models/animal.model';
import { Gospodarie } from '../../models/gospodarie.model';
import { Persoana } from '../../models/persoana.model';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';
import { GenericFormComponent } from '../../components/generic-form/generic-form.component';
import { FormConfig, FormField } from '../../components/generic-form/generic-form.models';
import { ToastService } from '../../services/toast.service';

/**
 * Formular pentru înregistrarea efectivelor de grup (model snapshot ANSVSA).
 *
 * Semantică:
 *  - Route /animale/grup/new → creează un snapshot nou de efectiv
 *  - Route /animale/grup/:id/snapshot → adaugă un snapshot actualizat
 *    (numărul de capete s-a modificat; rândul vechi rămâne în istoric)
 *
 * Modul "editare" clasică NU mai există — modelul este append-only.
 */
@Component({
  selector: 'app-efectiv-grup-form',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent, RouterModule, BreadcrumbsComponent, GenericFormComponent],
  templateUrl: './efectiv-grup-form.component.html'
})
export class EfectivGrupFormComponent implements OnInit, OnDestroy {
  @Input() isModal = false;
  @Input() inputGospodarieId?: number;
  @Input() editId?: number;
  @Output() closeForm = new EventEmitter<void>();

  isSnapshotMode = false;
  referenceGrupId?: number;
  returnToGospodarieId?: number;
  gospodarieId?: number;

  user: any;
  activeUat: any;
  private destroy$ = new Subject<void>();

  // Form config
  formInitialData: any = {};
  formConfig: FormConfig = {
    submitText: 'Salvare Efectiv',
    cancelText: 'Anulare',
    sections: []
  };

  speciesOptions = Object.values(SpecieAnimal);
  gospodariiList: Gospodarie[] = [];
  personsList: Persoana[] = [];
  breadcrumbItems: BreadcrumbItem[] = [];

  constructor(
    private animalService: AnimalService,
    private gospodarieService: GospodarieService,
    private persoanaService: PersoanaService,
    private authService: AuthService,
    private uatContextService: UatContextService,
    private router: Router,
    private route: ActivatedRoute,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.user = this.authService.currentUserSubject.value;

    if (this.user?.role === 'ROLE_SUPER_ADMIN') {
      this.loadDropdowns();
    } else {
      this.uatContextService.activeUat$
        .pipe(takeUntil(this.destroy$))
        .subscribe(uat => {
          this.activeUat = uat;
          this.loadDropdowns();
        });
    }

    if (this.isModal) {
      if (this.inputGospodarieId) {
        this.gospodarieId = this.inputGospodarieId;
        this.returnToGospodarieId = this.inputGospodarieId;
      }
      if (this.editId) {
        this.isSnapshotMode = true;
        this.referenceGrupId = this.editId;
        this.loadReferenceGroup(this.referenceGrupId);
      }
    } else {
      this.route.paramMap.subscribe(params => {
        const idParam = params.get('id');
        if (idParam) {
          this.isSnapshotMode = true;
          this.referenceGrupId = +idParam;
          this.loadReferenceGroup(this.referenceGrupId);
        }
      });
    }

    if (!this.isModal) {
      this.route.queryParams.subscribe(params => {
        if (params['gospodarieId']) {
          this.returnToGospodarieId = +params['gospodarieId'];
          this.gospodarieId = this.returnToGospodarieId;
        }
        this.updateBreadcrumbs();
      });
    }
  }

  updateBreadcrumbs() {
    this.breadcrumbItems = [
      { label: 'Gospodării', link: '/gospodarii' }
    ];
    if (this.returnToGospodarieId) {
      this.breadcrumbItems = [
        { label: 'Gospodării', link: '/gospodarii' },
        { label: 'Detalii Gospodărie', link: `/gospodarii/${this.returnToGospodarieId}`, queryParams: { tab: 'ANIMALS' } }
      ];
    }
    this.breadcrumbItems.push({ label: this.isSnapshotMode ? 'Actualizare Efectiv' : 'Adăugare Efectiv' });
  }

  loadDropdowns() {
    const uatCode = this.user?.role === 'ROLE_SUPER_ADMIN' ? undefined : this.activeUat?.codSiruta;
    this.gospodarieService.getAllGospodarii(uatCode, 0, 1000).subscribe({
      next: (response) => {
        this.gospodariiList = response.content;
        this.updateFormConfig();
      },
      error: (err) => console.error('Error fetching households', err)
    });
    this.persoanaService.getAllPersons('', '', 0, 1000).subscribe({
      next: (response) => {
        this.personsList = response.content;
        this.updateFormConfig();
      },
      error: (err) => console.error('Error fetching persons', err)
    });
  }

  updateFormConfig() {
    this.formConfig.submitText = this.isSnapshotMode ? 'Salvează Snapshot' : 'Adăugare Efectiv';
    const detailFields: FormField[] = [
      { name: 'specie', label: 'Specie', type: 'select', required: true, width: 'half', options: this.speciesOptions.map(s => ({ label: s, value: s })) },
      { name: 'numarCapeteFamilii', label: 'Număr Capete / Familii Albine', type: 'number', required: true, width: 'half', min: 1 }
    ];

    if (this.isSnapshotMode) {
      detailFields[0].width = 'third';
      detailFields[1].width = 'third';
      detailFields.push({ name: 'dataInregistrare', label: 'Data Înregistrării', type: 'date', required: true, width: 'third' });
    }

    detailFields.push({ name: 'detalii', label: 'Detalii Suplimentare', type: 'textarea', required: false, width: 'full', placeholder: 'Ex: Tineret ovin etc.' });

    this.formConfig.sections = [
      {
        title: 'Detalii Efectiv / Grup',
        fields: detailFields
      },
      {
        title: 'Asociere',
        fields: [
          { 
            name: 'gospodarieId', label: 'Gospodărie Asociată', type: 'select', required: true, width: 'half', placeholder: '-- Selectează Gospodăria --',
            options: this.gospodariiList.map(g => ({ label: `${g.codGospodarie} - ${g.adresa?.street || ''} ${g.adresa?.streetNumber || ''} (${g.adresa?.localitate || ''})`, value: g.id }))
          },
          { 
            name: 'proprietarId', label: 'Proprietar (Persoană)', type: 'select', required: true, width: 'half', placeholder: '-- Selectează Proprietar --',
            options: this.personsList.map(p => ({ label: this.getPersonDisplayName(p), value: p.id }))
          }
        ]
      }
    ];

    if (!this.isSnapshotMode && Object.keys(this.formInitialData).length === 0) {
      this.formInitialData = {
        specie: SpecieAnimal.OVINE,
        numarCapeteFamilii: 1,
        gospodarieId: this.gospodarieId
      };
    }
  }

  loadReferenceGroup(id: number) {
    this.animalService.getGroupById(id).subscribe({
      next: (g) => {
        this.formInitialData = {
          specie: g.specie,
          numarCapeteFamilii: g.numarCapeteFamilii,
          dataInregistrare: new Date().toISOString().substring(0, 10),
          detalii: '',
          gospodarieId: g.gospodarie?.id,
          proprietarId: g.proprietar?.id
        };
        this.gospodarieId = g.gospodarie?.id;
        this.updateFormConfig();
      },
      error: (err) => console.error('Error loading reference group', err)
    });
  }

  getPersonDisplayName(p: Persoana): string {
    if (p.personType === 'PHYSICAL_PERSON') {
      const pf = p as any;
      return `${pf.firstName || ''} ${pf.lastName || ''} (${pf.cnp || 'N/A'})`.trim();
    } else {
      const pj = p as any;
      return `${pj.companyName || ''} (${pj.cui || 'N/A'})`.trim();
    }
  }

  save(formData: any) {
    if (!formData.gospodarieId || !formData.proprietarId) {
      this.toastService.warning('Vă rugăm să selectați Gospodăria și Proprietarul.');
      return;
    }

    const payload: EfectivGrup = {
      specie: formData.specie,
      numarCapeteFamilii: formData.numarCapeteFamilii,
      dataInregistrare: formData.dataInregistrare ? formData.dataInregistrare : new Date().toISOString().substring(0, 10),
      detalii: formData.detalii || undefined,
      gospodarieId: formData.gospodarieId,
      proprietarId: formData.proprietarId
    } as any;

    if (this.isSnapshotMode && this.referenceGrupId) {
      // Adăugăm un snapshot nou la efectivul existent
      this.animalService.addGrupSnapshot(this.referenceGrupId, payload).subscribe({
        next: () => this.navigateBack(),
        error: (err) => {
          this.toastService.error('Eroare la adăugarea snapshot-ului: ' + (err.error?.message || err.message));
        }
      });
    } else {
      // Creăm un efectiv nou (primul snapshot)
      this.animalService.createGroup(payload).subscribe({
        next: () => this.navigateBack(),
        error: (err) => {
          this.toastService.error('Eroare la înregistrarea efectivului: ' + (err.error?.message || err.message));
        }
      });
    }
  }

  cancel() {
    this.navigateBack();
  }

  private navigateBack() {
    if (this.isModal) {
      this.closeForm.emit();
    } else if (this.returnToGospodarieId) {
      this.router.navigate(['/gospodarii', this.returnToGospodarieId], { queryParams: { tab: 'ANIMALS' } });
    } else {
      this.router.navigate(['/animale']);
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
