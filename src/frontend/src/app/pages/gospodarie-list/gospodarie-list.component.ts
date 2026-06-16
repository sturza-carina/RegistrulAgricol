import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject, combineLatest, filter } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { GospodarieService } from '../../services/gospodarie.service';
import { UatContextService } from '../../services/uat-context.service';
import { Gospodarie, Uat } from '../../models/gospodarie.model';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-gospodarie-list',
  standalone: true,
  imports: [CommonModule, RouterLink, SidebarComponent, FormsModule],
  templateUrl: './gospodarie-list.component.html',
  styleUrls: ['./gospodarie-list.component.css']
})
export class GospodarieListComponent implements OnInit, OnDestroy {
  gospodarii: Gospodarie[] = [];
  filteredGospodarii: Gospodarie[] = [];
  user: any = null;
  searchQuery = '';
  filterTipGospodarie: string = '';
  selectedGospodarie: Gospodarie | null = null;

  // UAT activ curent — folosit pentru banner si filtrare
  activeUat: Uat | null = null;

  private destroy$ = new Subject<void>();
  constructor(
    private gospodarieService: GospodarieService,
    private uatContext: UatContextService,
    private authService: AuthService,
    private router: Router
  ) {}


  ngOnInit(): void {
    this.user = this.authService.currentUserSubject.getValue();
    if (!this.user) {
      this.router.navigate(['/login']);
      return;
    }

    // super admin incarca imediat fara sa astepte UAT
    if (this.user.role === 'ROLE_SUPER_ADMIN') {
      this.loadGospodarii();
      return;
    }

    // ceilalti așteapta UAT-ul activ (ignora null-ul initial)
    this.uatContext.activeUat$
      .pipe(
        filter(uat => uat !== null),
        takeUntil(this.destroy$)
      )
      .subscribe(uat => {
        this.activeUat = uat;
        this.loadGospodarii();
      });
  }

  loadGospodarii(): void {
    const uatCode = this.user?.role === 'ROLE_SUPER_ADMIN'
      ? undefined
      : this.activeUat?.codSiruta;

    console.log('loadGospodarii cu uatCode:', uatCode);

    this.gospodarieService.getAllGospodarii(uatCode).subscribe({
      next: (data) => {
        this.gospodarii = data;
        this.applyFilters();
      },
      error: (err) => console.error(err)
    });
  }

  applyFilters(): void {
    const q = this.searchQuery.toLowerCase();
    this.filteredGospodarii = this.gospodarii.filter(g => {
      const matchSearch =
        g.codGospodarie.toLowerCase().includes(q) ||
        (g.adresa.localitate && g.adresa.localitate.toLowerCase().includes(q));
      const matchType = this.filterTipGospodarie
        ? g.tipGospodarie === this.filterTipGospodarie
        : true;
      return matchSearch && matchType;
    });

    if (
      this.selectedGospodarie &&
      !this.filteredGospodarii.find(g => g.id === this.selectedGospodarie?.id)
    ) {
      this.selectedGospodarie = null;
    }
  }

  goToCreate() {
    this.router.navigate(['/gospodarii/new']);
  }

  selectGospodarie(g: Gospodarie): void {
    this.selectedGospodarie = this.selectedGospodarie?.id === g.id ? null : g;
  }

  editSelected() {
    if (this.selectedGospodarie) {
      this.router.navigate(['/gospodarii/edit', this.selectedGospodarie.id]);
    }
  }

  deleteSelected(): void {
    if (
      this.selectedGospodarie?.id &&
      confirm('Sunteți sigur că doriți să ștergeți gospodăria selectată?')
    ) {
      this.gospodarieService.deleteGospodarie(this.selectedGospodarie.id).subscribe(() => {
        this.selectedGospodarie = null;
        this.loadGospodarii();
      });
    }
  }

  deleteRow(id: number | undefined, event: Event): void {
    event.stopPropagation();
    if (!id) return;
    if (confirm('Ești sigur că vrei să ștergi această gospodărie?')) {
      this.gospodarieService.deleteGospodarie(id).subscribe(() => {
        if (this.selectedGospodarie?.id === id) {
          this.selectedGospodarie = null;
        }
        this.loadGospodarii();
      });
    }
  }

  viewDetailsSelected() {
    if (this.selectedGospodarie) {
      this.router.navigate(['/gospodarii', this.selectedGospodarie.id]);
    }
  }

  viewDetails(id?: number) {
    if (id) this.router.navigate(['/gospodarii', id]);
  }

  editGospodarie(id?: number) {
    if (id) this.router.navigate(['/gospodarii/edit', id]);
  }

  addParcelaSelected(): void {
    if (this.selectedGospodarie) {
      this.router.navigate(['/harta'], {
        queryParams: { gospodarieId: this.selectedGospodarie.id }
      });
    }
  }

  addPersoanaSelected(): void {
    if (this.selectedGospodarie) {
      this.router.navigate(['/persoane/new'], {
        queryParams: { gospodarieId: this.selectedGospodarie.id }
      });
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
