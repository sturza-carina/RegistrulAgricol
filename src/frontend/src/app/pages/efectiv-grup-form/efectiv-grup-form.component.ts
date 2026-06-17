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

@Component({
  selector: 'app-efectiv-grup-form',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent, RouterModule],
  templateUrl: './efectiv-grup-form.component.html'
})
export class EfectivGrupFormComponent implements OnInit {
  isEditMode = false;
  grupId?: number;

  // Form fields
  specie: SpecieAnimal = SpecieAnimal.OVINE;
  numarCapeteFamilii = 1;
  detalii = '';
  gospodarieId?: number;
  proprietarId?: number;

  // Enum Options
  speciesOptions = Object.values(SpecieAnimal);

  // Dropdown lists
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
        this.isEditMode = true;
        this.grupId = +idParam;
        this.loadGroup(this.grupId);
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

  loadGroup(id: number) {
    this.animalService.getGroupById(id).subscribe({
      next: (g) => {
        this.specie = g.specie;
        this.numarCapeteFamilii = g.numarCapeteFamilii;
        this.detalii = g.detalii || '';
        this.gospodarieId = g.gospodarie?.id;
        this.proprietarId = g.proprietar?.id;
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

  save() {
    if (!this.gospodarieId || !this.proprietarId) {
      alert('Vă rugăm să selectați Gospodăria și Proprietarul.');
      return;
    }

    const payload: EfectivGrup = {
      id: this.grupId,
      specie: this.specie,
      numarCapeteFamilii: this.numarCapeteFamilii,
      detalii: this.detalii,
      gospodarieId: this.gospodarieId,
      proprietarId: this.proprietarId
    } as any;

    if (this.isEditMode && this.grupId) {
      this.animalService.updateGroup(this.grupId, payload).subscribe({
        next: () => this.router.navigate(['/animale']),
        error: (err) => {
          alert('Eroare la actualizarea grupului: ' + (err.error?.message || err.message));
        }
      });
    } else {
      this.animalService.createGroup(payload).subscribe({
        next: () => this.router.navigate(['/animale']),
        error: (err) => {
          alert('Eroare la înregistrarea grupului: ' + (err.error?.message || err.message));
        }
      });
    }
  }

  cancel() {
    this.router.navigate(['/animale']);
  }
}
