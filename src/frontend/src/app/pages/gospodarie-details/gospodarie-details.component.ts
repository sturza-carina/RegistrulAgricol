import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { GospodarieService } from '../../services/gospodarie.service';
import { Gospodarie } from '../../models/gospodarie.model';
import { PersoanaService } from '../../services/persoana.service';
import { TerenService } from '../../services/teren.service';
import { Teren } from '../../models/teren.model';
import { ParcelaService } from '../../services/parcela.service';
import { LayoutComponent } from '../../components/layout/layout.component';
import { PageHeaderComponent } from '../../components/page-header/page-header.component';
import { MachineryManagementComponent } from '../machinery-management/machinery-management.component';
import { CladireManagementComponent } from '../cladire-management/cladire-management.component';
import { PersonFormComponent } from '../persoana-form/persoana-form.component';
import { AnimalListComponent } from '../animal-list/animal-list.component';
import { TerenFormComponent } from '../teren-form/teren-form.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';
import { GenericTableComponent, TableColumn, TableFilter, TableAction } from '../../components/generic-table/generic-table.component';

@Component({
  selector: 'app-gospodarie-details',
  standalone: true,
  imports: [CommonModule, FormsModule, LayoutComponent, PageHeaderComponent, RouterModule, MachineryManagementComponent, CladireManagementComponent, PersonFormComponent, AnimalListComponent, BreadcrumbsComponent, GenericTableComponent, TerenFormComponent],
  templateUrl: './gospodarie-details.component.html',
  styleUrls: ['./gospodarie-details.component.css']
})
export class GospodarieDetailsComponent implements OnInit {
  gospodarieId!: number;
  gospodarie: Gospodarie | null = null;
  persoane: any[] = [];
  terenuri: Teren[] = [];
  activeTab: string = 'GENERAL';

  breadcrumbItems: BreadcrumbItem[] = [];

  showPersonModal = false;
  editPersonId?: number;
  
  showTerenModal = false;

  searchMembriText: string = '';
  searchTerenuriText: string = '';
  selectedTipTeren: string = '';

  membriColumns: TableColumn[] = [
    { field: 'nameDisplay', header: 'Nume / Denumire', format: (val, row) => row.personType === 'LEGAL_ENTITY' ? row.companyName : `${row.firstName} ${row.lastName}` },
    { field: 'idDisplay', header: 'CNP / CUI', format: (val, row) => row.personType === 'LEGAL_ENTITY' ? row.cui : row.cnp },
    { field: 'isHeadOfHousehold', header: 'Rol', type: 'badge', format: val => val ? 'Cap Gospodărie' : 'Membru', badgeClasses: { 'true': 'user', 'false': 'viewer' } }
  ];

  membriFilters: TableFilter[] = [
    { field: 'search', label: 'Caută membru...', type: 'search', searchFields: ['firstName', 'lastName', 'companyName', 'cnp', 'cui'] }
  ];

  membriActions: TableAction[] = [
    { icon: 'edit', tooltip: 'Detalii / Editare', action: (row) => this.editPerson(row.id) }
  ];

  terenuriColumns: TableColumn[] = [
    { field: 'denumire', header: 'Denumire', format: val => val || '-' },
    { field: 'tipTeren', header: 'Tip Teren', type: 'badge', format: val => val || 'Nespecificat', badgeClasses: {} },
    { field: 'mapat', header: 'Geometrie', type: 'badge', format: (val, row) => (row.polygon || row.stereo70Coordinates) ? 'Mapat' : 'Nemapat', badgeClasses: { 'Mapat': 'activ', 'Nemapat': 'inactiv' } }
  ];

  terenuriFilters: TableFilter[] = [
    { field: 'search', label: 'Caută teren...', type: 'search', searchFields: ['denumire', 'tipTeren'] },
    { field: 'tipTeren', label: 'Tip Teren', type: 'select', options: [] } // Will be populated dynamically
  ];

  terenuriActions: TableAction[] = [
    { icon: 'edit', tooltip: 'Editare Parcele', action: (row) => this.editTeren(row) },
    { icon: 'delete', tooltip: 'Ștergere Teren', action: (row) => this.deleteTeren(row) }
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private gospodarieService: GospodarieService,
    private persoanaService: PersoanaService,
    private terenService: TerenService,
    private parcelaService: ParcelaService
  ) {}

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.gospodarieId = +id;
        this.loadDetails();
      }
    });

    this.route.queryParams.subscribe(params => {
      if (params['tab']) {
        this.activeTab = params['tab'];
      } else {
        this.activeTab = 'GENERAL';
      }
    });
  }

  loadDetails() {
    this.gospodarieService.getGospodarieById(this.gospodarieId).subscribe(data => {
      this.gospodarie = data;
      this.updateBreadcrumbs();
    });

    this.terenService.getTerenByGospodarieId(this.gospodarieId).subscribe({
      next: (terenuri) => {
        this.terenuri = (terenuri || []).sort((a, b) => (b.id || 0) - (a.id || 0));
        this.updateTerenuriFilters();
      },
      error: () => {
        this.terenuri = [];
      }
    });

    this.persoanaService.getPersonsByGospodarieId(this.gospodarieId).subscribe(data => this.persoane = data as any[]);
  }

  updateTerenuriFilters() {
    const types = new Set(this.terenuri.map(t => t.tipTeren).filter(t => t));
    const typeOptions = Array.from(types).map(t => ({ label: t as string, value: t as string }));
    const tipTerenFilter = this.terenuriFilters.find(f => f.field === 'tipTeren');
    if (tipTerenFilter) {
      tipTerenFilter.options = typeOptions;
    }
  }

  updateBreadcrumbs() {
    this.breadcrumbItems = [
      { label: 'Gospodării', link: '/gospodarii' },
      { label: this.gospodarie?.codGospodarie || 'Detalii' }
    ];
  }

  editGospodarie() {
    this.router.navigate(['/gospodarii/edit', this.gospodarieId]);
  }

  addPerson() {
    this.editPersonId = undefined;
    this.showPersonModal = true;
  }

  editPerson(id: number) {
    this.editPersonId = id;
    this.showPersonModal = true;
  }

  closePersonModal() {
    this.showPersonModal = false;
    this.loadDetails(); // Refresh members list
  }

  viewMap() {
    this.router.navigate(['/harta'], { queryParams: { gospodarieId: this.gospodarieId } });
  }

  addTeren() {
    this.showTerenModal = true;
  }
  
  closeTerenModal() {
    this.showTerenModal = false;
    this.loadDetails(); // Refresh list after adding
  }

  viewTeren(teren: Teren) {
    this.router.navigate(['/harta'], { queryParams: { gospodarieId: this.gospodarieId, terenId: teren.id } });
  }

  editTeren(teren: Teren) {
    this.router.navigate(['/terenuri', teren.id, 'parcele'], { queryParams: { gospodarieId: this.gospodarieId } });
  }

  deleteTeren(teren: Teren) {
    if (!teren.id) return;
    if (!confirm(`Ștergeți terenul "${teren.denumire}"? Această acțiune este ireversibilă și va șterge și parcelele asociate.`)) return;
    this.terenService.deleteTeren(teren.id).subscribe({
      next: () => {
        this.terenuri = this.terenuri.filter(t => t.id !== teren.id);
      },
      error: () => alert('Eroare la ștergere teren.')
    });
  }


}
