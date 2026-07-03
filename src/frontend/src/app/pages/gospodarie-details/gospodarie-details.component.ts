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
import { CarteFunciaraService } from '../../services/carte-funciara.service';
import { CarteFunciara } from '../../models/carte-funciara.model';
import { LayoutComponent } from '../../components/layout/layout.component';
import { PageHeaderComponent } from '../../components/page-header/page-header.component';
import { MachineryManagementComponent } from '../machinery-management/machinery-management.component';
import { CladireManagementComponent } from '../cladire-management/cladire-management.component';
import { PersonFormComponent } from '../persoana-form/persoana-form.component';
import { AnimalListComponent } from '../animal-list/animal-list.component';
import { TerenFormComponent } from '../teren-form/teren-form.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';
import { GenericTableComponent, TableColumn, TableFilter, TableAction } from '../../components/generic-table/generic-table.component';
import { DocumentManagementComponent } from '../document-management/document-management.component';
import { GospodarieFormComponent } from '../gospodarie-form/gospodarie-form.component';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-gospodarie-details',
  standalone: true,
  imports: [CommonModule, FormsModule, LayoutComponent,
    PageHeaderComponent, RouterModule, MachineryManagementComponent,
    CladireManagementComponent, PersonFormComponent, AnimalListComponent,
    BreadcrumbsComponent, GenericTableComponent, TerenFormComponent,
    DocumentManagementComponent, GospodarieFormComponent
  ],
  templateUrl: './gospodarie-details.component.html',
  styleUrls: ['./gospodarie-details.component.css']
})
export class GospodarieDetailsComponent implements OnInit {
  gospodarieId!: number;
  gospodarie: Gospodarie | null = null;
  persoane: any[] = [];
  terenuri: Teren[] = [];
  activeTab: string = 'GENERAL';

  // Carte Funciara
  carteFunciaraRows: CarteFunciara[] = [];
  carteFunciaraLoading = false;
  editingCfId: number | null = null;
  editCfForm: { numarCf: string; numarTopografic: string } = { numarCf: '', numarTopografic: '' };

  breadcrumbItems: BreadcrumbItem[] = [];

  showPersonModal = false;
  editPersonId?: number;

  showTerenModal = false;

  isAddingMember = false;
  toatePersoanele: any[] = [];

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

  toatePersoaneleActions: TableAction[] = [
    { icon: 'add', tooltip: 'Adaugă Membru', action: (row) => this.addExistingPersonToGospodarie(row.id) }
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
    private parcelaService: ParcelaService,
    private carteFunciaraService: CarteFunciaraService
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
      this.isAddingMember = false; // Reset sub-view on tab change
      this.updateBreadcrumbs();
      if (this.activeTab === 'CARTE_FUNCIARA') {
        this.loadCarteFunciara();
      }
    });
  }

  loadDetails() {
    this.gospodarieService.getGospodarieById(this.gospodarieId).subscribe(data => {
      this.gospodarie = data;
      this.updateBreadcrumbs();
    });

    this.terenService.getTerenByGospodarieId(this.gospodarieId, 0, 1000).subscribe({
      next: (response) => {
        this.terenuri = response.content;
        this.updateTerenuriFilters();
      },
      error: () => {
        this.terenuri = [];
      }
    });

    this.persoanaService.getPersonsByGospodarieId(this.gospodarieId, 0, 100).subscribe(res => this.persoane = res.content);
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
    let tabName = '';
    switch (this.activeTab) {
      case 'GENERAL': tabName = 'General'; break;
      case 'TERENURI': tabName = 'Terenuri Agricole'; break;
      case 'BUILDINGS': tabName = 'Clădiri'; break;
      case 'MEMBERS': tabName = 'Membri'; break;
      case 'ANIMALS': tabName = 'Animale'; break;
      case 'MACHINERY': tabName = 'Utilaje'; break;
      case 'DOCUMENTS': tabName = 'Documente'; break;
      case 'CARTE_FUNCIARA': tabName = 'Carte Funciară'; break;
      default: tabName = this.activeTab;
    }

    this.breadcrumbItems = [
      { label: 'Gospodării', link: '/gospodarii' },
      { label: `Detalii - ${this.gospodarie?.codGospodarie || ''}` }
    ];

    if (tabName) {
      this.breadcrumbItems.push({ label: tabName });
    }

    if (this.isAddingMember) {
      this.breadcrumbItems.push({ label: 'Adaugă membrii' });
    }
  }

  addPerson() {
    this.isAddingMember = true;
    this.updateBreadcrumbs();
    this.loadToatePersoanele();
  }

  cancelAddPerson() {
    this.isAddingMember = false;
    this.updateBreadcrumbs();
  }

  loadToatePersoanele() {
    this.persoanaService.getAllPersons('', '', 0, 1000).subscribe(response => {
      const data = response.content;
      // Filter out persons that are already members
      const existingMemberIds = new Set(this.persoane.map(p => p.id));
      this.toatePersoanele = (data as any[]).filter(p => !existingMemberIds.has(p.id));
    });
  }

  addExistingPersonToGospodarie(persoanaId: number) {
    this.persoanaService.addPersonToGospodarie(persoanaId, this.gospodarieId).subscribe({
      next: () => {
        this.isAddingMember = false;
        this.loadDetails(); // Refresh members list and close sub-view
      },
      error: () => alert('Eroare la adăugarea persoanei în gospodărie.')
    });
  }

  createNewPerson() {
    this.editPersonId = undefined;
    this.showPersonModal = true;
  }

  editPerson(id: number) {
    this.editPersonId = id;
    this.showPersonModal = true;
  }

  closePersonModal() {
    this.showPersonModal = false;
    if (this.isAddingMember) {
      // If we just created a new person while in adding mode, ideally we want to select them.
      // But for now, we can just reload the "toatePersoanele" list to let the user select them.
      this.loadToatePersoanele();
    } else {
      this.loadDetails(); // Refresh members list
    }
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

  // --- Carte Funciara ---

  loadCarteFunciara() {
    if (this.terenuri.length === 0) {
      // Terenuri might not be loaded yet if we navigate directly to CF tab
      this.carteFunciaraLoading = true;
      this.terenService.getTerenByGospodarieId(this.gospodarieId, 0, 1000).subscribe({
        next: (response) => {
          this.terenuri = response.content;
          this.fetchCfForTerenuri();
        },
        error: () => {
          this.terenuri = [];
          this.carteFunciaraLoading = false;
        }
      });
    } else {
      this.fetchCfForTerenuri();
    }
  }

  private fetchCfForTerenuri() {
    if (this.terenuri.length === 0) {
      this.carteFunciaraRows = [];
      this.carteFunciaraLoading = false;
      return;
    }
    this.carteFunciaraLoading = true;
    const requests = this.terenuri
      .filter(t => t.id != null)
      .map(t =>
        this.carteFunciaraService.getByTerenId(t.id!).pipe(
          catchError(() => of(null))
        )
      );

    forkJoin(requests).subscribe(results => {
      this.carteFunciaraRows = results
        .map((cf, idx) => {
          if (!cf) return null;
          const teren = this.terenuri[idx];
          return { ...cf, terenDenumire: teren?.denumire, terenId: teren?.id } as CarteFunciara;
        })
        .filter((cf): cf is CarteFunciara => cf !== null);
      this.carteFunciaraLoading = false;
    });
  }

  startEditCf(cf: CarteFunciara) {
    this.editingCfId = cf.id ?? null;
    this.editCfForm = {
      numarCf: cf.numarCf ?? '',
      numarTopografic: cf.numarTopografic ?? ''
    };
  }

  cancelEditCf() {
    this.editingCfId = null;
  }

  saveCf(cf: CarteFunciara) {
    if (!cf.id) return;
    this.carteFunciaraService.update(cf.id, {
      numarCf: this.editCfForm.numarCf || undefined,
      numarTopografic: this.editCfForm.numarTopografic || undefined
    }).subscribe({
      next: (updated) => {
        const idx = this.carteFunciaraRows.findIndex(r => r.id === cf.id);
        if (idx !== -1) {
          this.carteFunciaraRows[idx] = {
            ...this.carteFunciaraRows[idx],
            numarCf: updated.numarCf,
            numarTopografic: updated.numarTopografic
          };
        }
        this.editingCfId = null;
      },
      error: () => alert('Eroare la salvarea datelor Cărții Funciare.')
    });
  }

}
