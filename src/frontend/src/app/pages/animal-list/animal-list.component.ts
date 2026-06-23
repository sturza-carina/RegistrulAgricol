import { Component, OnInit, Input, OnChanges, SimpleChanges, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AnimalService } from '../../services/animal.service';
import { AnimalIndividual, EfectivGrup } from '../../models/animal.model';
import { AuthService } from '../../services/auth.service';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { UatContextService } from '../../services/uat-context.service';
import { Uat } from '../../models/gospodarie.model';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-animal-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, SidebarComponent],
  templateUrl: './animal-list.component.html',
  styleUrls: ['./animal-list.component.css']
})
export class AnimalListComponent implements OnInit, OnChanges, OnDestroy {
  activeTab: 'individual' | 'grup' = 'individual';
  individuals: AnimalIndividual[] = [];
  groups: EfectivGrup[] = [];
  user: any;
  activeUat: Uat | null = null;
  private destroy$ = new Subject<void>();

  @Input() gospodarieId?: number;

  constructor(
    private animalService: AnimalService,
    private authService: AuthService,
    private uatContext: UatContextService,
    private router: Router
  ) {}

  ngOnInit() {
    this.user = this.authService.currentUserSubject.value;
    this.uatContext.activeUat$
      .pipe(takeUntil(this.destroy$))
      .subscribe(uat => {
        this.activeUat = uat;
        this.loadData();
      });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['gospodarieId'] && !changes['gospodarieId'].firstChange) {
      this.loadData();
    }
  }

  loadData() {
    this.animalService.getAllIndividuals().subscribe({
      next: (data) => {
        if (this.gospodarieId) {
          this.individuals = data.filter(a => a.gospodarie?.id === this.gospodarieId);
        } else if (this.activeUat) {
          this.individuals = data.filter(a => a.gospodarie?.uat?.codSiruta === this.activeUat?.codSiruta);
        } else {
          this.individuals = data;
        }
      },
      error: (err) => console.error('Error fetching individuals', err)
    });

    this.animalService.getAllGroups().subscribe({
      next: (data) => {
        if (this.gospodarieId) {
          this.groups = data.filter(g => g.gospodarie?.id === this.gospodarieId);
        } else if (this.activeUat) {
          this.groups = data.filter(g => g.gospodarie?.uat?.codSiruta === this.activeUat?.codSiruta);
        } else {
          this.groups = data;
        }
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

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
