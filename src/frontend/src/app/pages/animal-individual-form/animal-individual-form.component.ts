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
import { AnimalIndividual, SpecieAnimal, SexAnimal } from '../../models/animal.model';
import { Gospodarie } from '../../models/gospodarie.model';
import { Persoana } from '../../models/persoana.model';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';
import { GenericFormComponent } from '../../components/generic-form/generic-form.component';
import { FormConfig, FormField } from '../../components/generic-form/generic-form.models';

@Component({
  selector: 'app-animal-individual-form',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent, RouterModule, BreadcrumbsComponent, GenericFormComponent],
  templateUrl: './animal-individual-form.component.html'
})
export class AnimalIndividualFormComponent implements OnInit, OnDestroy {
  @Input() isModal = false;
  @Input() inputGospodarieId?: number;
  @Input() editId?: number;
  @Output() closeForm = new EventEmitter<void>();

  isEditMode = false;
  animalId?: number;
  returnToGospodarieId?: number;
  gospodarieId?: number;

  user: any;
  activeUat: any;
  private destroy$ = new Subject<void>();

  // Form config
  formInitialData: any = {};
  formConfig: FormConfig = {
    submitText: $localize `:@@form_submit_save_animal:Salvare Animal`,
    cancelText: $localize `:@@form_cancel:Anulare`,
    sections: []
  };

  // Enum Options
  speciesOptions = Object.values(SpecieAnimal);
  sexOptions = Object.values(SexAnimal);

  // Dropdown lists
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
    private route: ActivatedRoute
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
        this.isEditMode = true;
        this.animalId = this.editId;
        this.loadAnimal(this.animalId);
      }
    } else {
      this.route.paramMap.subscribe(params => {
        const idParam = params.get('id');
        if (idParam) {
          this.isEditMode = true;
          this.animalId = +idParam;
          this.loadAnimal(this.animalId);
        }
      });
    }

    if (!this.isModal) {
      this.route.queryParams.subscribe(params => {
        if (params['gospodarieId']) {
          this.returnToGospodarieId = +params['gospodarieId'];
          if (!this.isEditMode) {
            this.gospodarieId = this.returnToGospodarieId;
          }
        }
        this.updateBreadcrumbs();
      });
    }
  }

  updateBreadcrumbs() {
    this.breadcrumbItems = [
      { label: $localize `:@@breadcrumb_animals:Animale`, link: '/animale' }
    ];
    if (this.returnToGospodarieId) {
      this.breadcrumbItems = [
        { label: $localize `:@@breadcrumb_households:Gospodării`, link: '/gospodarii' },
        { label: $localize `:@@breadcrumb_household_details:Detalii Gospodărie`, link: `/gospodarii/${this.returnToGospodarieId}`, queryParams: { tab: 'ANIMALS' } }
      ];
    }
    this.breadcrumbItems.push({ 
      label: this.isEditMode 
        ? $localize `:@@breadcrumb_edit_animal:Editare Animal` 
        : $localize `:@@breadcrumb_add_animal:Adăugare Animal` 
    });
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
    this.formConfig.submitText = this.isEditMode 
      ? $localize `:@@form_submit_save_changes:Salvează Modificările` 
      : $localize `:@@form_submit_add_animal:Adăugare Animal`;
    const assocFields: FormField[] = [
      { 
        name: 'gospodarieId', 
        label: $localize `:@@field_associated_household:Gospodărie Asociată`, 
        type: 'select', 
        required: true, 
        width: 'half', 
        placeholder: $localize `:@@placeholder_select_household:-- Selectează Gospodăria --`,
        options: this.gospodariiList.map(g => ({ label: `${g.codGospodarie} - ${g.adresa?.street || ''} ${g.adresa?.streetNumber || ''} (${g.adresa?.localitate || ''})`, value: g.id }))
      },
      { 
        name: 'proprietarId', 
        label: $localize `:@@field_owner_person:Proprietar (Persoană)`, 
        type: 'select', 
        required: true, 
        width: 'half', 
        placeholder: $localize `:@@placeholder_select_owner:-- Selectează Proprietar --`,
        options: this.personsList.map(p => ({ label: this.getPersonDisplayName(p), value: p.id }))
      }
    ];

    if (!this.isEditMode) {
      assocFields.push({ 
        name: 'stareActiva', 
        label: $localize `:@@field_active_state:Stare Activă (Prezent în exploatație)`, 
        type: 'checkbox', 
        required: false, 
        width: 'full' 
      });
    }

    this.formConfig.sections = [
      {
        title: $localize `:@@section_identification_details:Detalii Identificare`,
        fields: [
          { name: 'numarCrotal', label: $localize `:@@field_crotal_number:Număr Crotal (Identificare)`, type: 'text', required: true, placeholder: $localize `:@@placeholder_crotal_example:Ex: RO123456789`, width: 'half' },
          { name: 'specie', label: $localize `:@@field_specie:Specie`, type: 'select', required: true, width: 'half', options: this.speciesOptions.map(s => ({ label: s, value: s })) },
          { name: 'rasa', label: $localize `:@@field_rasa:Rasă`, type: 'text', required: false, width: 'half', placeholder: $localize `:@@placeholder_rasa_example:Ex: Bălțată Românească` },
          { name: 'sex', label: $localize `:@@field_sex:Sex`, type: 'select', required: true, width: 'half', options: this.sexOptions.map(s => ({ label: s, value: s })) },
          { name: 'dataNastere', label: $localize `:@@field_birth_date:Data Nașterii`, type: 'date', required: false, width: 'half' },
          { name: 'greutateKg', label: $localize `:@@field_weight:Greutate estimată (Kg)`, type: 'number', required: false, width: 'half', min: 0 }
        ]
      },
      {
        title: $localize `:@@section_association:Asociere`,
        fields: assocFields
      }
    ];

    // Setup initial data defaults
    if (!this.isEditMode && Object.keys(this.formInitialData).length === 0) {
      this.formInitialData = {
        specie: SpecieAnimal.BOVINE,
        sex: SexAnimal.FEMININ,
        stareActiva: true,
        gospodarieId: this.gospodarieId
      };
    }
  }

  loadAnimal(id: number) {
    this.animalService.getIndividualById(id).subscribe({
      next: (anim) => {
        this.formInitialData = {
          numarCrotal: anim.numarCrotal || '',
          specie: anim.specie,
          rasa: anim.rasa || '',
          sex: anim.sex,
          dataNastere: anim.dataNastere ? anim.dataNastere.substring(0, 10) : '',
          greutateKg: anim.greutateKg,
          stareActiva: anim.stareActiva,
          gospodarieId: anim.gospodarie?.id,
          proprietarId: anim.proprietar?.id
        };
      },
      error: (err) => console.error('Error loading animal', err)
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
      alert($localize `:@@alert_select_household_owner:Vă rugăm să selectați Gospodăria și Proprietarul.`);
      return;
    }

    const payload: AnimalIndividual = {
      id: this.animalId,
      numarCrotal: formData.numarCrotal,
      specie: formData.specie,
      rasa: formData.rasa,
      sex: formData.sex,
      dataNastere: formData.dataNastere ? formData.dataNastere : undefined,
      greutateKg: formData.greutateKg,
      stareActiva: this.isEditMode ? undefined : (formData.stareActiva !== undefined ? formData.stareActiva : true),
      gospodarieId: formData.gospodarieId,
      proprietarId: formData.proprietarId
    } as any;

    if (this.isEditMode && this.animalId) {
      this.animalService.updateIndividual(this.animalId, payload).subscribe({
        next: () => this.navigateBack(),
        error: (err) => {
          const errMsg = $localize `:@@alert_update_animal_error:Eroare la actualizarea animalului: `;
          alert(errMsg + (err.error?.message || err.message));
        }
      });
    } else {
      this.animalService.createIndividual(payload).subscribe({
        next: () => this.navigateBack(),
        error: (err) => {
          const errMsg = $localize `:@@alert_register_animal_error:Eroare la înregistrarea animalului: `;
          alert(errMsg + (err.error?.message || err.message));
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
