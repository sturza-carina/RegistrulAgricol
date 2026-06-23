import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AnimalService } from '../../services/animal.service';
import {
  AnimalIndividual, EvenimentAnimal, TipEvenimentAnimal,
  TransferRequest
} from '../../models/animal.model';
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

  // --- Modal de adăugare eveniment ---
  showModal = false;
  isSaving = false;
  modalError = '';
  tipEveniment: TipEvenimentAnimal = TipEvenimentAnimal.NASTERE;
  dataEveniment: string = new Date().toISOString().substring(0, 10);
  detalii = '';
  tipuriEveniment = Object.values(TipEvenimentAnimal);

  // --- Modal de transfer cross-tenant ---
  showTransferModal = false;
  isTransferring = false;
  transferError = '';
  transferDestinatarTenantId = '';
  transferDestinatarCodSiruta = '';
  transferDestinatarGospodarieId?: number;
  transferDestinatarProprietarId?: number;
  transferDetalii = '';
  transferSuccess: string | null = null;

  /** Etichetele prietenoase pentru tipurile de eveniment (română) */
  readonly tipLabels: Record<TipEvenimentAnimal, string> = {
    [TipEvenimentAnimal.NASTERE]:             'Naștere',
    [TipEvenimentAnimal.CUMPARARE]:           'Cumpărare',
    [TipEvenimentAnimal.TRANSFER_INTRARE]:    'Transfer Intrare',
    [TipEvenimentAnimal.TRATAMENT_VETERINAR]: 'Tratament Veterinar',
    [TipEvenimentAnimal.VANZARE]:             'Vânzare',
    [TipEvenimentAnimal.SACRIFICARE_PROPRIE]: 'Sacrificare Proprie',
    [TipEvenimentAnimal.MOARTE]:              'Moarte',
    [TipEvenimentAnimal.UCIDERE_FOCAR]:       'Ucidere Focar',
    [TipEvenimentAnimal.DISPARITIE]:          'Dispariție'
  };

  /** Culori semantice per tip de eveniment (pentru dot-ul din timeline) */
  readonly tipColors: Record<TipEvenimentAnimal, string> = {
    [TipEvenimentAnimal.NASTERE]:             '#22c55e',
    [TipEvenimentAnimal.CUMPARARE]:           '#3b82f6',
    [TipEvenimentAnimal.TRANSFER_INTRARE]:    '#8b5cf6',
    [TipEvenimentAnimal.TRATAMENT_VETERINAR]: '#06b6d4',  // cyan — medical
    [TipEvenimentAnimal.VANZARE]:             '#f59e0b',
    [TipEvenimentAnimal.SACRIFICARE_PROPRIE]: '#f97316',
    [TipEvenimentAnimal.MOARTE]:              '#ef4444',
    [TipEvenimentAnimal.UCIDERE_FOCAR]:       '#dc2626',
    [TipEvenimentAnimal.DISPARITIE]:          '#6b7280'
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
      next: (events) => { this.timeline = events; this.isLoading = false; },
      error: () => { this.errorMsg = 'Istoricul nu a putut fi încărcat.'; this.isLoading = false; }
    });
  }

  // ---- Modal eveniment ----

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
        this.loadAnimal();
        this.loadTimeline();
      },
      error: (err) => {
        this.isSaving = false;
        this.modalError = err.error?.message || err.error || 'Eroare la salvarea evenimentului.';
      }
    });
  }

  // ---- Modal transfer cross-tenant ----

  uatsList: any[] = [];
  gospodariiList: any[] = [];
  personsList: any[] = [];

  openTransferModal(): void {
    this.transferError = '';
    this.transferSuccess = null;
    this.transferDestinatarTenantId = '';
    this.transferDestinatarCodSiruta = '';
    this.transferDestinatarGospodarieId = undefined;
    this.transferDestinatarProprietarId = undefined;
    this.transferDetalii = '';
    this.gospodariiList = [];
    this.personsList = [];
    this.animalService.getAllUats().subscribe({
      next: (data) => {
        this.uatsList = data.filter(u => u.tenantId);
        if (this.uatsList.length > 0) {
          this.transferDestinatarCodSiruta = this.uatsList[0].codSiruta;
          this.onUatTransferChange(this.transferDestinatarCodSiruta);
        }
      },
      error: () => {
        this.transferError = 'Eroare la încărcarea listei de UAT-uri destinație.';
      }
    });
    this.showTransferModal = true;
  }

  onUatTransferChange(codSiruta: string): void {
    this.transferDestinatarCodSiruta = codSiruta;
    this.transferDestinatarGospodarieId = undefined;
    this.transferDestinatarProprietarId = undefined;
    this.gospodariiList = [];
    this.personsList = [];
    if (!codSiruta) return;

    const selectedUat = this.uatsList.find(u => u.codSiruta === codSiruta);
    if (!selectedUat) return;

    this.transferDestinatarTenantId = selectedUat.tenantId;

    this.animalService.getGospodariiByTenant(selectedUat.tenantId, codSiruta).subscribe({
      next: (data) => {
        this.gospodariiList = data;
        if (this.gospodariiList.length > 0) {
          this.transferDestinatarGospodarieId = this.gospodariiList[0].id;
        }
      },
      error: () => {
        this.transferError = 'Eroare la încărcarea gospodăriilor din UAT destinatar.';
      }
    });

    this.animalService.getPersonsByTenant(selectedUat.tenantId).subscribe({
      next: (data) => {
        this.personsList = data;
        if (this.personsList.length > 0) {
          this.transferDestinatarProprietarId = this.personsList[0].id;
        }
      },
      error: () => {
        this.transferError = 'Eroare la încărcarea persoanelor din UAT destinatar.';
      }
    });
  }

  closeTransferModal(): void {
    this.showTransferModal = false;
    this.isTransferring = false;
    this.transferError = '';
  }

  confirmTransfer(): void {
    if (!this.transferDestinatarTenantId || !this.transferDestinatarGospodarieId || !this.transferDestinatarProprietarId) {
      this.transferError = 'Completați toate câmpurile obligatorii (Tenant, Gospodărie, Proprietar).';
      return;
    }
    this.isTransferring = true;
    this.transferError = '';

    const request: TransferRequest = {
      destinatarTenantId: this.transferDestinatarTenantId,
      destinatarGospodarieId: this.transferDestinatarGospodarieId!,
      destinatarProprietarId: this.transferDestinatarProprietarId!,
      detaliiTransfer: this.transferDetalii || undefined
    };

    this.animalService.transferAnimal(this.animalId, request).subscribe({
      next: (resp) => {
        this.isTransferring = false;
        this.transferSuccess = `Transfer finalizat! ID animal în tenant destinatar: #${resp.newAnimalId}`;
        // Reîncărcăm animalul (devine inactiv după transfer)
        this.loadAnimal();
        this.loadTimeline();
        // Închidem modalul după 2 secunde
        setTimeout(() => this.closeTransferModal(), 2500);
      },
      error: (err) => {
        this.isTransferring = false;
        this.transferError = err.error?.message || 'Transferul a eșuat. Verificați datele destinatarului.';
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
