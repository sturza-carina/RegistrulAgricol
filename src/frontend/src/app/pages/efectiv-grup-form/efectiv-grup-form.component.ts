import { Component, OnInit } from '@angular/core';
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

/**
 * Formular pentru înregistrarea efectivelor de grup (model snapshot ANSVSA).
 *
 * Semantică:
 *  - Route /animale/grup/new → creează un snapshot nou de efectiv
 *  - Route /animale/grup/:id/snapshot → adaugă un snapshot actualizat
 *    (numărul de capete s-a modificat; rândul vechi rămâne în istoric)
 *
 * Modul "editare" clasică NU mai există — modelul este append-only.
 */
@Component({
  selector: 'app-efectiv-grup-form',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent, RouterModule],
  templateUrl: './efectiv-grup-form.component.html'
})
export class EfectivGrupFormComponent implements OnInit {
  /** Snapshot mode: adăugăm un snapshot nou la un efectiv existent */
  isSnapshotMode = false;
  referenceGrupId?: number;

  // Form fields
  specie: SpecieAnimal = SpecieAnimal.OVINE;
  numarCapeteFamilii = 1;
  dataInregistrare: string = new Date().toISOString().substring(0, 10);
  detalii = '';
  gospodarieId?: number;
  proprietarId?: number;

  speciesOptions = Object.values(SpecieAnimal);
  gospodariiList: Gospodarie[] = [];
  personsList: Persoana[] = [];

  constructor(
    private animalService: AnimalService,
    private gospodarieService: GospodarieService,
    private persoanaService: PersoanaService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.loadDropdowns();

    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      if (idParam) {
        // Route /animale/grup/:id/snapshot → snapshot mode
        this.isSnapshotMode = true;
        this.referenceGrupId = +idParam;
        this.loadReferenceGroup(this.referenceGrupId);
      }
    });
  }

  loadDropdowns() {
    this.gospodarieService.getAllGospodarii().subscribe({
      next: (data) => this.gospodariiList = data,
      error: (err) => console.error('Error fetching households', err)
    });
    this.persoanaService.getAllPersons().subscribe({
      next: (data) => this.personsList = data,
      error: (err) => console.error('Error fetching persons', err)
    });
  }

  /** Preîncarcă gospodăria și proprietarul din snapshot-ul de referință */
  loadReferenceGroup(id: number) {
    this.animalService.getGroupById(id).subscribe({
      next: (g) => {
        this.specie = g.specie;
        this.numarCapeteFamilii = g.numarCapeteFamilii;
        this.detalii = '';  // detalii noi pentru noul snapshot
        this.gospodarieId = g.gospodarie?.id;
        this.proprietarId = g.proprietar?.id;
      },
      error: (err) => console.error('Error loading reference group', err)
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

  save() {
    if (!this.gospodarieId || !this.proprietarId) {
      alert('Vă rugăm să selectați Gospodăria și Proprietarul.');
      return;
    }

    const payload: EfectivGrup = {
      specie: this.specie,
      numarCapeteFamilii: this.numarCapeteFamilii,
      dataInregistrare: this.dataInregistrare,
      detalii: this.detalii || undefined,
      gospodarieId: this.gospodarieId,
      proprietarId: this.proprietarId
    } as any;

    if (this.isSnapshotMode && this.referenceGrupId) {
      // Adăugăm un snapshot nou la efectivul existent
      this.animalService.addGrupSnapshot(this.referenceGrupId, payload).subscribe({
        next: () => this.router.navigate(['/animale']),
        error: (err) => {
          alert('Eroare la adăugarea snapshot-ului: ' + (err.error?.message || err.message));
        }
      });
    } else {
      // Creăm un efectiv nou (primul snapshot)
      this.animalService.createGroup(payload).subscribe({
        next: () => this.router.navigate(['/animale']),
        error: (err) => {
          alert('Eroare la înregistrarea efectivului: ' + (err.error?.message || err.message));
        }
      });
    }
  }

  cancel() {
    this.router.navigate(['/animale']);
  }
}
