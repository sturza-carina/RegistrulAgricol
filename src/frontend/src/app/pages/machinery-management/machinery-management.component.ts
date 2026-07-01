import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Machinery } from '../../models/machinery.model';
import { MachineryService } from '../../services/machinery.service';
import { GenericTableComponent, TableColumn, TableFilter, TableAction } from '../../components/generic-table/generic-table.component';
import { GenericFormComponent } from '../../components/generic-form/generic-form.component';
import { FormConfig } from '../../components/generic-form/generic-form.models';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-machinery-management',
  standalone: true,
  imports: [CommonModule, FormsModule, GenericTableComponent, GenericFormComponent],
  templateUrl: './machinery-management.component.html'
})
export class MachineryManagementComponent implements OnInit, OnChanges {
  @Input() gospodarieId!: number;

  machineryList: Machinery[] = [];
  isLoading = false;
  isSaving = false;
  showForm = false;
  editingId: number | null = null;

  formInitialData: any = {};
  formConfig: FormConfig = {
    submitText: 'Salvează',
    cancelText: 'Anulează',
    sections: [
      {
        fields: [
          { name: 'tipUtilaj', label: 'Tip Utilaj', type: 'select', required: true, width: 'half', options: [
            { label: 'Tractor', value: 'TRACTOR' },
            { label: 'Combină', value: 'COMBINA' },
            { label: 'Plug', value: 'PLUG' },
            { label: 'Remorcă', value: 'REMORCA' },
            { label: 'Altele', value: 'ALTELE' }
          ] },
          { name: 'marca', label: 'Marca', type: 'text', required: true, width: 'half', placeholder: 'ex. John Deere' },
          { name: 'model', label: 'Model', type: 'text', required: false, width: 'half', placeholder: 'ex. 6155M' },
          { name: 'anFabricatie', label: 'An Fabricație', type: 'number', required: false, width: 'half', min: 1900 },
          { name: 'numarInmatriculare', label: 'Număr Înmatriculare', type: 'text', required: false, width: 'full', placeholder: 'ex. TM 01 AGR' }
        ]
      }
    ]
  };

  columns: TableColumn[] = [
    { field: 'tipUtilaj', header: 'Tip Utilaj', type: 'badge', badgeClasses: { 'TRACTOR': 'admin', 'COMBINA': 'admin', 'PLUG': 'viewer', 'REMORCA': 'viewer', 'ALTELE': 'default' } },
    { field: 'marca', header: 'Marca', format: val => val || '-' },
    { field: 'model', header: 'Model', format: val => val || '-' },
    { field: 'anFabricatie', header: 'An Fabr.', format: val => val || '-' },
    { field: 'numarInmatriculare', header: 'Nr. Înmatriculare', format: val => val || '-' }
  ];

  filters: TableFilter[] = [
    { field: 'search', label: 'Caută după marcă sau model...', type: 'search', searchFields: ['marca', 'model', 'numarInmatriculare'] },
    { field: 'tipUtilaj', label: 'Tip Utilaj', type: 'select', options: [{label: 'Tractor', value: 'TRACTOR'}, {label: 'Combină', value: 'COMBINA'}, {label: 'Plug', value: 'PLUG'}, {label: 'Remorcă', value: 'REMORCA'}, {label: 'Altele', value: 'ALTELE'}] }
  ];

  actions: TableAction[] = [
    { icon: 'edit', tooltip: 'Editare', action: (row) => this.openEditForm(row) },
    { icon: 'delete', tooltip: 'Ștergere', action: (row) => this.deleteMachinery(row.id) }
  ];

  constructor(private machineryService: MachineryService, private toastService: ToastService) {}

  ngOnInit(): void {
    this.loadMachinery();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['gospodarieId'] && this.gospodarieId) {
      this.loadMachinery();
    }
  }

  loadMachinery(): void {
    if (!this.gospodarieId) {
      this.machineryList = [];
      return;
    }

    this.isLoading = true;
    this.machineryService.getMachineryByGospodarie(this.gospodarieId, 0, 1000).subscribe({
      next: (response) => {
        this.machineryList = response.content;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  openCreateForm(): void {
    this.editingId = null;
    this.formInitialData = { tipUtilaj: 'TRACTOR' };
    this.formConfig.submitText = 'Adaugă Utilaj';
    this.showForm = true;
  }

  openEditForm(item: Machinery): void {
    this.editingId = item.id ?? null;
    this.formInitialData = { ...item };
    this.formConfig.submitText = 'Salvează Modificările';
    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingId = null;
  }

  saveMachinery(formData: any): void {
    if (!this.gospodarieId) {
      return;
    }

    if (!formData.tipUtilaj || !formData.marca) {
      this.toastService.warning('Vă rugăm să completați tipul utilajului și marca.');
      return;
    }

    this.isSaving = true;
    const payload: Machinery = {
      ...formData,
      gospodarieId: this.gospodarieId,
      anFabricatie: formData.anFabricatie ? Number(formData.anFabricatie) : null
    };

    const request = this.editingId
      ? this.machineryService.update(this.editingId, payload)
      : this.machineryService.create(payload);

    request.subscribe({
      next: () => {
        this.isSaving = false;
        this.showForm = false;
        this.loadMachinery();
        this.editingId = null;
      },
      error: () => {
        this.isSaving = false;
      }
    });
  }

  deleteMachinery(id?: number): void {
    if (!id) {
      return;
    }

    const confirmed = window.confirm('Sigur doriți să ștergeți acest utilaj?');
    if (!confirmed) {
      return;
    }

    this.machineryService.delete(id).subscribe(() => {
      this.loadMachinery();
    });
  }
}
