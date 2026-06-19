import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AnimalService } from '../../services/animal.service';
import { AnimalIndividual, EvenimentAnimal, TipEvenimentAnimal } from '../../models/animal.model';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';

@Component({
  selector: 'app-animal-timeline',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, SidebarComponent, BreadcrumbsComponent],
  templateUrl: './animal-timeline.component.html',
  styleUrls: ['./animal-timeline.component.css']
})
export class AnimalTimelineComponent implements OnInit {
  animalId!: number;
  animal?: AnimalIndividual;
  timeline: EvenimentAnimal[] = [];
  isLoading = true;
  errorMsg = '';
  breadcrumbItems: BreadcrumbItem[] = [];

  // Modal state
  showModal = false;
  isSaving = false;
  modalError = '';

  // Form fields
  tipEveniment: TipEvenimentAnimal = TipEvenimentAnimal.NASTERE;
  dataEveniment: string = new Date().toISOString().substring(0, 10);
  detalii = '';

  tipuriEveniment = Object.values(TipEvenimentAnimal);

  /** Map pentru afișare prietenoasă a tipurilor de eveniment */
  readonly tipLabels: Record<TipEvenimentAnimal, string> = {
    [TipEvenimentAnimal.NASTERE]:            'Naștere',
    [TipEvenimentAnimal.CUMPARARE]:          'Cumpărare',
    [TipEvenimentAnimal.TRANSFER_INTRARE]:   'Transfer Intrare',
    [TipEvenimentAnimal.VANZARE]:            'Vânzare',
    [TipEvenimentAnimal.SACRIFICARE_PROPRIE]:'Sacrificare Proprie',
    [TipEvenimentAnimal.MOARTE]:             'Moarte',
    [TipEvenimentAnimal.UCIDERE_FOCAR]:      'Ucidere Focar',
    [TipEvenimentAnimal.DISPARITIE]:         'Dispariție'
  };

  /** Culori pentru fiecare tip de eveniment (pentru dot-ul din timeline) */
  readonly tipColors: Record<TipEvenimentAnimal, string> = {
    [TipEvenimentAnimal.NASTERE]:            '#22c55e',
    [TipEvenimentAnimal.CUMPARARE]:          '#3b82f6',
    [TipEvenimentAnimal.TRANSFER_INTRARE]:   '#8b5cf6',
    [TipEvenimentAnimal.VANZARE]:            '#f59e0b',
    [TipEvenimentAnimal.SACRIFICARE_PROPRIE]:'#f97316',
    [TipEvenimentAnimal.MOARTE]:             '#ef4444',
    [TipEvenimentAnimal.UCIDERE_FOCAR]:      '#dc2626',
    [TipEvenimentAnimal.DISPARITIE]:         '#6b7280'
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private animalService: AnimalService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.animalId = +id;
        this.loadAnimal();
        this.loadTimeline();
      }
    });
  }

  loadAnimal(): void {
    this.animalService.getIndividualById(this.animalId).subscribe({
      next: (a) => {
        this.animal = a;
        this.updateBreadcrumbs();
      },
      error: () => this.errorMsg = 'Animalul nu a putut fi încărcat.'
    });
  }

  updateBreadcrumbs() {
    this.breadcrumbItems = [
      { label: 'Animale', link: '/animale' },
      { label: `Istoric (${this.animal?.numarCrotal || '...'})` }
    ];
  }

  loadTimeline(): void {
    this.isLoading = true;
    this.animalService.getTimeline(this.animalId).subscribe({
      next: (events) => {
        this.timeline = events;
        this.isLoading = false;
      },
      error: () => {
        this.errorMsg = 'Istoricul nu a putut fi încărcat.';
        this.isLoading = false;
      }
    });
  }

  openModal(): void {
    this.modalError = '';
    this.tipEveniment = TipEvenimentAnimal.NASTERE;
    this.dataEveniment = new Date().toISOString().substring(0, 10);
    this.detalii = '';
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.isSaving = false;
    this.modalError = '';
  }

  saveEveniment(): void {
    this.isSaving = true;
    this.modalError = '';

    const payload: EvenimentAnimal = {
      tipEveniment: this.tipEveniment,
      dataEveniment: this.dataEveniment,
      detalii: this.detalii || undefined
    };

    this.animalService.adaugaEveniment(this.animalId, payload).subscribe({
      next: () => {
        this.closeModal();
        this.loadAnimal();   // reîncarcă starea animalului (poate deveni inactiv)
        this.loadTimeline(); // reîncarcă timeline-ul
      },
      error: (err) => {
        this.isSaving = false;
        this.modalError = err.error?.message || err.error || 'Eroare la salvarea evenimentului.';
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/animale']);
  }

  getTipLabel(tip: TipEvenimentAnimal): string {
    return this.tipLabels[tip] ?? tip;
  }

  getTipColor(tip: TipEvenimentAnimal): string {
    return this.tipColors[tip] ?? '#6b7280';
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    return d.toLocaleDateString('ro-RO', { day: '2-digit', month: 'long', year: 'numeric' });
  }
}
