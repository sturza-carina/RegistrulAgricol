import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Machinery } from '../../models/machinery.model';
import { MachineryService } from '../../services/machinery.service';

@Component({
  selector: 'app-machinery-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './machinery-management.component.html',
  styleUrls: ['./machinery-management.component.css']
})
export class MachineryManagementComponent implements OnInit, OnChanges {
  @Input() gospodarieId!: number;

  machineryList: Machinery[] = [];
  isLoading = false;
  isSaving = false;
  showForm = false;
  editingId: number | null = null;

  tipUtilajOptions = ['TRACTOR', 'COMBINA', 'PLUG', 'REMORCA', 'ALTELE'];

  machineryForm: Machinery = this.createEmptyForm();

  constructor(private machineryService: MachineryService) {}

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
    this.machineryService.getMachineryByGospodarie(this.gospodarieId).subscribe({
      next: (data) => {
        this.machineryList = data;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  openCreateForm(): void {
    this.editingId = null;
    this.machineryForm = this.createEmptyForm();
    this.showForm = true;
  }

  openEditForm(item: Machinery): void {
    this.editingId = item.id ?? null;
    this.machineryForm = {
      ...item,
      gospodarieId: this.gospodarieId,
      anFabricatie: item.anFabricatie ?? null
    };
    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingId = null;
    this.machineryForm = this.createEmptyForm();
  }

  saveMachinery(): void {
    if (!this.gospodarieId) {
      return;
    }

    this.isSaving = true;
    const payload: Machinery = {
      ...this.machineryForm,
      gospodarieId: this.gospodarieId,
      anFabricatie: this.machineryForm.anFabricatie ? Number(this.machineryForm.anFabricatie) : null
    };

    const request = this.editingId
      ? this.machineryService.update(this.editingId, payload)
      : this.machineryService.create(payload);

    request.subscribe({
      next: () => {
        this.isSaving = false;
        this.showForm = false;
        this.loadMachinery();
        this.machineryForm = this.createEmptyForm();
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

  private createEmptyForm(): Machinery {
    return {
      tipUtilaj: 'TRACTOR',
      marca: '',
      model: '',
      anFabricatie: null,
      numarInmatriculare: '',
      gospodarieId: this.gospodarieId ?? 0
    };
  }
}
