import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Cladire } from '../../models/cladire.model';
import { CladireService } from '../../services/cladire.service';
import { GenericTableComponent, TableColumn, TableAction } from '../../components/generic-table/generic-table.component';
import { GenericFormComponent } from '../../components/generic-form/generic-form.component';
import { FormConfig } from '../../components/generic-form/generic-form.models';

@Component({
  selector: 'app-cladire-management',
  standalone: true,
  imports: [CommonModule, FormsModule, GenericTableComponent, GenericFormComponent],
  templateUrl: './cladire-management.component.html'
})
export class CladireManagementComponent implements OnInit {
  @Input() gospodarieId!: number;
  cladiri: Cladire[] = [];
  
  showModal = false;
  isEditing = false;
  currentCladire: Partial<Cladire> = {};

  formInitialData: any = {};
  formConfig: FormConfig = {
    submitText: 'Salvează',
    cancelText: 'Anulează',
    sections: [
      {
        fields: [
          { name: 'destinatie', label: 'Destinație', type: 'select', required: true, width: 'half', options: [
            { label: 'LOCUINTA', value: 'LOCUINTA' },
            { label: 'ANEXA', value: 'ANEXA' },
            { label: 'ALTELE', value: 'ALTELE' }
          ] },
          { name: 'suprafataConstruita', label: 'Suprafață Construită (mp)', type: 'number', required: true, width: 'half', min: 0 },
          { name: 'suprafataDesfasurata', label: 'Suprafață Desfășurată (mp)', type: 'number', required: false, width: 'half', min: 0 },
          { name: 'anTerminare', label: 'Anul Terminării', type: 'number', required: false, width: 'half' },
          { name: 'materiale', label: 'Materiale folosite (zidărie, acoperiș)', type: 'text', required: false, width: 'full' },
          { name: 'adresaSauParcela', label: 'Adresă / Parcelă aferentă', type: 'text', required: false, width: 'full' },
          { name: 'zonaImpozitare', label: 'Zona Impozitare', type: 'text', required: false, width: 'half' },
          { name: 'observatii', label: 'Observații', type: 'textarea', required: false, width: 'full' }
        ]
      }
    ]
  };

  columns: TableColumn[] = [
    { field: 'destinatie', header: 'Destinație', type: 'badge', badgeClasses: { 'LOCUINTA': 'activ', 'ANEXA': 'viewer', 'ALTELE': 'default' } },
    { field: 'suprafataConstruita', header: 'Suprafață Construită (mp)', format: val => val ? val + ' mp' : '-' },
    { field: 'anTerminare', header: 'An Terminare', format: val => val || '-' },
    { field: 'materiale', header: 'Materiale', format: val => val || '-' },
    { field: 'adresaSauParcela', header: 'Adresă / Parcelă', format: val => val || '-' }
  ];

  actions: TableAction[] = [
    { icon: 'edit', tooltip: 'Editare', action: (row) => this.openEditModal(row) },
    { icon: 'delete', tooltip: 'Ștergere', action: (row) => this.deleteCladire(row.id) }
  ];

  constructor(private cladireService: CladireService) {}

  ngOnInit(): void {
    if (this.gospodarieId) {
      this.loadCladiri();
    }
  }

  loadCladiri() {
    this.cladireService.getCladiri(this.gospodarieId, 0, 1000).subscribe({
      next: (response) => {
        this.cladiri = response.content.sort((a, b) => (b.id || 0) - (a.id || 0));
      },
      error: (err) => console.error('Failed to load cladiri', err)
    });
  }

  openAddModal() {
    this.isEditing = false;
    this.currentCladire = {
      gospodarieId: this.gospodarieId
    };
    this.formInitialData = {};
    this.formConfig.submitText = 'Adaugă Clădire';
    this.showModal = true;
  }

  openEditModal(cladire: Cladire) {
    this.isEditing = true;
    this.currentCladire = { ...cladire };
    this.formInitialData = { ...cladire };
    this.formConfig.submitText = 'Salvează Modificările';
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.currentCladire = {};
  }

  saveCladire(formData: any) {
    if (!formData.destinatie || !formData.suprafataConstruita) {
      alert('Vă rugăm să completați destinația și suprafața construită.');
      return;
    }

    const payload = { ...this.currentCladire, ...formData };

    if (this.isEditing && this.currentCladire.id) {
      this.cladireService.updateCladire(this.gospodarieId, this.currentCladire.id, payload as Cladire)
        .subscribe({
          next: () => {
            this.loadCladiri();
            this.closeModal();
          },
          error: (err) => console.error('Failed to update cladire', err)
        });
    } else {
      this.cladireService.createCladire(this.gospodarieId, payload as Cladire)
        .subscribe({
          next: () => {
            this.loadCladiri();
            this.closeModal();
          },
          error: (err) => console.error('Failed to create cladire', err)
        });
    }
  }

  deleteCladire(id: number | undefined) {
    if (!id) return;
    if (confirm('Sunteți sigur că doriți să ștergeți această clădire?')) {
      this.cladireService.deleteCladire(this.gospodarieId, id).subscribe({
        next: () => this.loadCladiri(),
        error: (err) => console.error('Failed to delete cladire', err)
      });
    }
  }
}
