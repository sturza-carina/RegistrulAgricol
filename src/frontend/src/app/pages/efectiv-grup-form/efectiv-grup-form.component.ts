import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { AnimalService } from '../../services/animal.service';
import { GospodarieService } from '../../services/gospodarie.service';
import { PersoanaService } from '../../services/persoana.service';
import { EfectivGrup, SpecieAnimal } from '../../models/animal.model';
import { Gospodarie } from '../../models/gospodarie.model';
import { Persoana } from '../../models/persoana.model';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';
import { GenericFormComponent } from '../../components/generic-form/generic-form.component';
import { FormConfig } from '../../components/generic-form/generic-form.models';

@Component({
  selector: 'app-efectiv-grup-form',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent, RouterModule, BreadcrumbsComponent, GenericFormComponent],
  templateUrl: './efectiv-grup-form.component.html'
})
export class EfectivGrupFormComponent implements OnInit {
  @Input() isModal = false;
  @Input() inputGospodarieId?: number;
  @Input() editId?: number;
  @Output() closeForm = new EventEmitter<void>();

  isEditMode = false;
  efectivId?: number;
  returnToGospodarieId?: number;
  gospodarieId?: number;

  // Form config
  formInitialData: any = {};
  formConfig: FormConfig = {
    submitText: 'Salvare Efectiv',
    cancelText: 'Anulare',
    sections: []
  };

  // Enum Options
  speciesOptions = Object.values(SpecieAnimal);

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
        this.efectivId = this.editId;
        this.loadGroup(this.efectivId);
      }
    } else {
      this.route.paramMap.subscribe(params => {
        const idParam = params.get('id');
        if (idParam) {
          this.isEditMode = true;
          this.efectivId = +idParam;
          this.loadGroup(this.efectivId);
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
      { label: 'Gospodării', link: '/gospodarii' }
    ];
    if (this.returnToGospodarieId) {
      this.breadcrumbItems = [
        { label: 'Gospodării', link: '/gospodarii' },
        { label: 'Detalii Gospodărie', link: `/gospodarii/${this.returnToGospodarieId}`, queryParams: { tab: 'ANIMALS' } }
      ];
    }
    this.breadcrumbItems.push({ label: this.isEditMode ? 'Editare Efectiv' : 'Adăugare Efectiv' });
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
    this.formConfig.submitText = this.isEditMode ? 'Salvează Modificările' : 'Adăugare Efectiv';
    this.formConfig.sections = [
      {
        title: 'Detalii Efectiv / Grup',
        fields: [
          { name: 'specie', label: 'Specie', type: 'select', required: true, width: 'half', options: this.speciesOptions.map(s => ({ label: s, value: s })) },
          { name: 'numarCapeteFamilii', label: 'Număr Capete / Familii Albine', type: 'number', required: true, width: 'half', min: 1 },
          { name: 'detalii', label: 'Detalii Suplimentare', type: 'textarea', required: false, width: 'full', placeholder: 'Ex: Tineret ovin etc.' }
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
          }
        ]
      }
    ];

    if (!this.isEditMode && Object.keys(this.formInitialData).length === 0) {
      this.formInitialData = {
        specie: SpecieAnimal.OVINE,
        numarCapeteFamilii: 1,
        gospodarieId: this.gospodarieId
      };
    }
  }

  loadGroup(id: number) {
    this.animalService.getGroupById(id).subscribe({
      next: (g) => {
        this.formInitialData = {
          specie: g.specie,
          numarCapeteFamilii: g.numarCapeteFamilii,
          detalii: g.detalii || '',
          gospodarieId: g.gospodarie?.id,
          proprietarId: g.proprietar?.id
        };
      },
      error: (err) => console.error('Error loading group flock', err)
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

    const payload: EfectivGrup = {
      id: this.efectivId,
      specie: formData.specie,
      numarCapeteFamilii: formData.numarCapeteFamilii,
      detalii: formData.detalii,
      gospodarieId: formData.gospodarieId,
      proprietarId: formData.proprietarId
    } as any;

    if (this.isEditMode && this.efectivId) {
      this.animalService.updateGroup(this.efectivId, payload).subscribe({
        next: () => this.navigateBack(),
        error: (err) => {
          alert('Eroare la actualizarea grupului: ' + (err.error?.message || err.message));
        }
      });
    } else {
      this.animalService.createGroup(payload).subscribe({
        next: () => this.navigateBack(),
        error: (err) => {
          alert('Eroare la înregistrarea grupului: ' + (err.error?.message || err.message));
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
