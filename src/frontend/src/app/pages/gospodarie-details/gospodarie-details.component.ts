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
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { MachineryManagementComponent } from '../machinery-management/machinery-management.component';
import { CladireManagementComponent } from '../cladire-management/cladire-management.component';
import { PersonFormComponent } from '../persoana-form/persoana-form.component';

@Component({
  selector: 'app-gospodarie-details',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent, RouterModule, MachineryManagementComponent, CladireManagementComponent, PersonFormComponent],
  templateUrl: './gospodarie-details.component.html',
  styleUrls: ['./gospodarie-details.component.css']
})
export class GospodarieDetailsComponent implements OnInit {
  gospodarieId!: number;
  gospodarie: Gospodarie | null = null;
  persoane: any[] = [];
  terenuri: Teren[] = [];
  activeTab: string = 'GENERAL';

  showPersonModal = false;
  editPersonId?: number;

  searchMembriText: string = '';
  searchTerenuriText: string = '';
  selectedTipTeren: string = '';

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
    });

    this.terenService.getTerenByGospodarieId(this.gospodarieId).subscribe({
      next: (terenuri) => {
        this.terenuri = terenuri || [];
      },
      error: () => {
        this.terenuri = [];
      }
    });

    this.persoanaService.getPersonsByGospodarieId(this.gospodarieId).subscribe(data => this.persoane = data as any[]);
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
    this.router.navigate(['/terenuri/new'], { queryParams: { gospodarieId: this.gospodarieId } });
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

  get filteredPersoane() {
    if (!this.searchMembriText) return this.persoane;
    const lowerSearch = this.searchMembriText.toLowerCase();
    return this.persoane.filter(p => {
      const isLegal = p.personType === 'LEGAL_ENTITY';
      const name = isLegal ? p.companyName : `${p.firstName} ${p.lastName}`;
      const idCode = isLegal ? p.cui : p.cnp;
      
      return (name && name.toLowerCase().includes(lowerSearch)) ||
             (idCode && idCode.toLowerCase().includes(lowerSearch));
    });
  }

  get tipTerenOptions(): string[] {
    const types = new Set(this.terenuri.map(t => t.tipTeren).filter(t => t));
    return Array.from(types) as string[];
  }

  get filteredTerenuri() {
    let filtered = this.terenuri;
    
    if (this.selectedTipTeren) {
      filtered = filtered.filter(t => t.tipTeren === this.selectedTipTeren);
    }

    if (this.searchTerenuriText) {
      const lowerSearch = this.searchTerenuriText.toLowerCase();
      filtered = filtered.filter(t => {
        return (t.denumire && t.denumire.toLowerCase().includes(lowerSearch)) ||
               (t.tipTeren && t.tipTeren.toLowerCase().includes(lowerSearch));
      });
    }

    return filtered;
  }
}
