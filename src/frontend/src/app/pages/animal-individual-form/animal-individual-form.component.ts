import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { AnimalService } from '../../services/animal.service';
import { GospodarieService } from '../../services/gospodarie.service';
import { PersoanaService } from '../../services/persoana.service';
import { AnimalIndividual, SpecieAnimal, SexAnimal } from '../../models/animal.model';
import { Gospodarie } from '../../models/gospodarie.model';
import { Persoana } from '../../models/persoana.model';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';

@Component({
  selector: 'app-animal-individual-form',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent, RouterModule],
  templateUrl: './animal-individual-form.component.html',
  styleUrls: ['./animal-individual-form.component.css']
})
export class AnimalIndividualFormComponent implements OnInit {
  isEditMode = false;
  animalId?: number;

  // Form fields
  numarCrotal = '';
  specie: SpecieAnimal = SpecieAnimal.BOVINE;
  rasa = '';
  sex: SexAnimal = SexAnimal.FEMININ;
  dataNastere = '';
  greutateKg?: number;
  stareActiva = true;
  gospodarieId?: number;
  proprietarId?: number;

  // Enum Options
  speciesOptions = Object.values(SpecieAnimal);
  sexOptions = Object.values(SexAnimal);

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
        this.animalId = +idParam;
        this.loadAnimal(this.animalId);
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

  loadAnimal(id: number) {
    this.animalService.getIndividualById(id).subscribe({
      next: (anim) => {
        this.numarCrotal = anim.numarCrotal || '';
        this.specie = anim.specie;
        this.rasa = anim.rasa || '';
        this.sex = anim.sex;
        this.dataNastere = anim.dataNastere ? anim.dataNastere.substring(0, 10) : '';
        this.greutateKg = anim.greutateKg;
        this.stareActiva = anim.stareActiva;
        this.gospodarieId = anim.gospodarie?.id;
        this.proprietarId = anim.proprietar?.id;
      },
      error: (err) => console.error('Error loading animal', err)
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

    const payload: AnimalIndividual = {
      id: this.animalId,
      numarCrotal: this.numarCrotal || undefined,
      specie: this.specie,
      rasa: this.rasa || undefined,
      sex: this.sex,
      dataNastere: this.dataNastere ? this.dataNastere : undefined,
      greutateKg: this.greutateKg,
      stareActiva: true, // Server ignores this on update; set true on create
      gospodarieId: this.gospodarieId,
      proprietarId: this.proprietarId
    } as any;

    if (this.isEditMode && this.animalId) {
      this.animalService.updateIndividual(this.animalId, payload).subscribe({
        next: () => this.router.navigate(['/animale']),
        error: (err) => {
          alert('Eroare la actualizarea animalului: ' + (err.error?.message || err.message));
        }
      });
    } else {
      this.animalService.createIndividual(payload).subscribe({
        next: () => this.router.navigate(['/animale']),
        error: (err) => {
          alert('Eroare la înregistrarea animalului: ' + (err.error?.message || err.message));
        }
      });
    }
  }

  cancel() {
    this.router.navigate(['/animale']);
  }
}
