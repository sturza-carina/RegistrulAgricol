import { Component, OnInit, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AnimalService } from '../../services/animal.service';
import { AnimalIndividual, EfectivGrup } from '../../models/animal.model';
import { AuthService } from '../../services/auth.service';
import { LayoutComponent } from '../../components/layout/layout.component';
import { PageHeaderComponent } from '../../components/page-header/page-header.component';
import { GenericTableComponent, TableColumn, TableFilter, TableAction } from '../../components/generic-table/generic-table.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';
import { AnimalIndividualFormComponent } from '../animal-individual-form/animal-individual-form.component';
import { EfectivGrupFormComponent } from '../efectiv-grup-form/efectiv-grup-form.component';

@Component({
  selector: 'app-animal-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, LayoutComponent, PageHeaderComponent, GenericTableComponent, BreadcrumbsComponent, AnimalIndividualFormComponent, EfectivGrupFormComponent],
  templateUrl: './animal-list.component.html'
})
export class AnimalListComponent implements OnInit {
  activeTab: 'individual' | 'grup' = 'individual';
  individuals: AnimalIndividual[] = [];
  groups: EfectivGrup[] = [];
  user: any;
  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'Animale', link: '/animale' }
  ];

  @Input() gospodarieId?: number;

  showIndividualModal = false;
  editIndividualId?: number;

  showGroupModal = false;
  editGroupId?: number;

  individualsColumns: TableColumn[] = [
    { field: 'numarCrotal', header: 'Număr Crotal' },
    { field: 'specie', header: 'Specie', type: 'badge', badgeClasses: { 'BOVINE': 'registrar', 'OVINE': 'registrar', 'PORCINE': 'registrar' } },
    { field: 'rasa', header: 'Rasă', format: val => val || '-' },
    { field: 'sex', header: 'Sex', format: val => val === 'MASCULIN' ? 'Mascul' : 'Femelă' },
    { field: 'dataNastere', header: 'Data Naștere', type: 'date' },
    { field: 'greutateKg', header: 'Greutate (kg)', format: val => val ? val + ' kg' : '-' },
    { field: 'proprietar', header: 'Proprietar', format: val => this.getOwnerName(val) },
    { field: 'gospodarie.codGospodarie', header: 'Gospodărie', format: (val, row) => val || 'Detalii' },
    { field: 'stareActiva', header: 'Stare', type: 'badge', format: val => val ? 'Activ' : 'Inactiv', badgeClasses: { 'true': 'activ', 'false': 'inactiv' } }
  ];

  individualsFilters: TableFilter[] = [
    { field: 'search', label: 'Caută după crotal...', type: 'search', searchFields: ['numarCrotal'] },
    { field: 'specie', label: 'Specie', type: 'select', options: [{label: 'Bovine', value: 'BOVINE'}, {label: 'Ovine', value: 'OVINE'}, {label: 'Porcine', value: 'PORCINE'}, {label: 'Păsări', value: 'PASARI'}] },
    { field: 'stareActiva', label: 'Stare', type: 'select', options: [{label: 'Activ', value: true}, {label: 'Inactiv', value: false}] }
  ];

  individualsActions: TableAction[] = [
    { icon: 'history', tooltip: 'Istoric mișcări', action: (row) => this.router.navigate(['/animale/individual', row.id, 'istoric']) },
    { icon: 'edit', tooltip: 'Editare', action: (row) => this.openEditIndividual(row.id) },
    { icon: 'delete', tooltip: 'Șterge', action: (row) => this.deleteIndividual(row.id) }
  ];

  groupsColumns: TableColumn[] = [
    { field: 'id', header: 'ID', format: val => '#' + val },
    { field: 'specie', header: 'Specie', type: 'badge', badgeClasses: { 'BOVINE': 'admin', 'OVINE': 'admin', 'PORCINE': 'admin' } },
    { field: 'numarCapeteFamilii', header: 'Număr Capete / Familii', format: val => val + ' capete' },
    { field: 'proprietar', header: 'Proprietar', format: val => this.getOwnerName(val) },
    { field: 'gospodarie.codGospodarie', header: 'Gospodărie', format: (val, row) => val || 'Detalii' },
    { field: 'detalii', header: 'Detalii', format: val => val || '-' }
  ];

  groupsFilters: TableFilter[] = [
    { field: 'specie', label: 'Specie', type: 'select', options: [{label: 'Bovine', value: 'BOVINE'}, {label: 'Ovine', value: 'OVINE'}, {label: 'Porcine', value: 'PORCINE'}, {label: 'Păsări', value: 'PASARI'}, {label: 'Albine', value: 'ALBINE'}] }
  ];

  groupsActions: TableAction[] = [
    { icon: 'edit', tooltip: 'Editare', action: (row) => this.openEditGroup(row.id) },
    { icon: 'delete', tooltip: 'Șterge', action: (row) => this.deleteGroup(row.id) }
  ];

  constructor(
    private animalService: AnimalService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.user = this.authService.currentUserSubject.value;
    this.loadData();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['gospodarieId'] && !changes['gospodarieId'].firstChange) {
      this.loadData();
    }
  }

  loadData() {
    this.animalService.getAllIndividuals().subscribe({
      next: (data) => {
        let sorted = data.sort((a, b) => (b.id || 0) - (a.id || 0));
        this.individuals = this.gospodarieId ? sorted.filter(a => a.gospodarie?.id === this.gospodarieId) : sorted;
      },
      error: (err) => console.error('Error fetching individuals', err)
    });

    this.animalService.getAllGroups().subscribe({
      next: (data) => {
        let sorted = data.sort((a, b) => (b.id || 0) - (a.id || 0));
        this.groups = this.gospodarieId ? sorted.filter(g => g.gospodarie?.id === this.gospodarieId) : sorted;
      },
      error: (err) => console.error('Error fetching groups', err)
    });
  }

  setTab(tab: 'individual' | 'grup') {
    this.activeTab = tab;
  }

  getOwnerName(owner: any): string {
    if (!owner) return '-';
    if (owner.personType === 'PHYSICAL_PERSON') {
      return `${owner.firstName || ''} ${owner.lastName || ''}`.trim() || owner.username || '-';
    } else {
      return owner.companyName || '-';
    }
  }

  deleteIndividual(id: number) {
    if (confirm('Sigur doriți să ștergeți acest animal individual?')) {
      this.animalService.deleteIndividual(id).subscribe({
        next: () => {
          this.individuals = this.individuals.filter(i => i.id !== id);
        },
        error: (err) => console.error('Error deleting individual animal', err)
      });
    }
  }

  deleteGroup(id: number) {
    if (confirm('Sigur doriți să ștergeți acest grup de animale?')) {
      this.animalService.deleteGroup(id).subscribe({
        next: () => {
          this.groups = this.groups.filter(g => g.id !== id);
        },
        error: (err) => console.error('Error deleting group animals', err)
      });
    }
  }

  goToAddIndividual() {
    this.editIndividualId = undefined;
    this.showIndividualModal = true;
  }

  goToAddGroup() {
    this.editGroupId = undefined;
    this.showGroupModal = true;
  }

  openEditIndividual(id: number) {
    this.editIndividualId = id;
    this.showIndividualModal = true;
  }

  openEditGroup(id: number) {
    this.editGroupId = id;
    this.showGroupModal = true;
  }

  closeIndividualModal() {
    this.showIndividualModal = false;
    this.loadData();
  }

  closeGroupModal() {
    this.showGroupModal = false;
    this.loadData();
  }


}
