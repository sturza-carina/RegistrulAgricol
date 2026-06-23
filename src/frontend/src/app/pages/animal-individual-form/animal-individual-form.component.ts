import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { AnimalService } from '../../services/animal.service';
import { GospodarieService } from '../../services/gospodarie.service';
import { PersoanaService } from '../../services/persoana.service';
import { AnimalIndividual, SpecieAnimal, SexAnimal } from '../../models/animal.model';
import { Gospodarie } from '../../models/gospodarie.model';
import { Persoana } from '../../models/persoana.model';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';
import { GenericFormComponent } from '../../components/generic-form/generic-form.component';
import { FormConfig } from '../../components/generic-form/generic-form.models';

@Component({
  selector: 'app-animal-individual-form',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent, RouterModule, BreadcrumbsComponent, GenericFormComponent],
  templateUrl: './animal-individual-form.component.html'
})
export class AnimalIndividualFormComponent implements OnInit {
  @Input() isModal = false;
  @Input() inputGospodarieId?: number;
  @Input() editId?: number;
  @Output() closeForm = new EventEmitter<void>();

  isEditMode = false;
  animalId?: number;
  returnToGospodarieId?: number;
  gospodarieId?: number;

  // Form config
  formInitialData: any = {};
  formConfig: FormConfig = {
    submitText: 'Salvare Animal',
    cancelText: 'Anulare',
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
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.loadDropdowns();

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
      { label: 'Animale', link: '/animale' }
    ];
    if (this.returnToGospodarieId) {
      this.breadcrumbItems = [
        { label: 'Gospodării', link: '/gospodarii' },
        { label: 'Detalii Gospodărie', link: `/gospodarii/${this.returnToGospodarieId}`, queryParams: { tab: 'ANIMALS' } }
      ];
    }
    this.breadcrumbItems.push({ label: this.isEditMode ? 'Editare Animal' : 'Adăugare Animal' });
  }

  loadDropdowns() {
    this.gospodarieService.getAllGospodarii().subscribe({
      next: (data) => {
        this.gospodariiList = data;
        this.updateFormConfig();
      },
      error: (err) => console.error('Error fetching households', err)
    });

    this.persoanaService.getAllPersons().subscribe({
      next: (data) => {
        this.personsList = data;
        this.updateFormConfig();
      },
      error: (err) => console.error('Error fetching persons', err)
    });
  }

  updateFormConfig() {
    this.formConfig.submitText = this.isEditMode ? 'Salvează Modificările' : 'Adăugare Animal';
    this.formConfig.sections = [
      {
        title: 'Detalii Identificare',
        fields: [
          { name: 'numarCrotal', label: 'Număr Crotal (Identificare)', type: 'text', required: true, placeholder: 'Ex: RO123456789', width: 'half' },
          { name: 'specie', label: 'Specie', type: 'select', required: true, width: 'half', options: this.speciesOptions.map(s => ({ label: s, value: s })) },
          { name: 'rasa', label: 'Rasă', type: 'text', required: false, width: 'half', placeholder: 'Ex: Bălțată Românească' },
          { name: 'sex', label: 'Sex', type: 'select', required: true, width: 'half', options: this.sexOptions.map(s => ({ label: s, value: s })) },
          { name: 'dataNastere', label: 'Data Nașterii', type: 'date', required: false, width: 'half' },
          { name: 'greutateKg', label: 'Greutate estimată (Kg)', type: 'number', required: false, width: 'half', min: 0 }
        ]
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
          },
          { name: 'stareActiva', label: 'Stare Activă (Prezent în exploatație)', type: 'checkbox', required: false, width: 'full' }
        ]
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
      alert('Vă rugăm să selectați Gospodăria și Proprietarul.');
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
      stareActiva: formData.stareActiva,
      gospodarieId: formData.gospodarieId,
      proprietarId: formData.proprietarId
    } as any;

    if (this.isEditMode && this.animalId) {
      this.animalService.updateIndividual(this.animalId, payload).subscribe({
        next: () => this.navigateBack(),
        error: (err) => {
          alert('Eroare la actualizarea animalului: ' + (err.error?.message || err.message));
        }
      });
    } else {
      this.animalService.createIndividual(payload).subscribe({
        next: () => this.navigateBack(),
        error: (err) => {
          alert('Eroare la înregistrarea animalului: ' + (err.error?.message || err.message));
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
}
