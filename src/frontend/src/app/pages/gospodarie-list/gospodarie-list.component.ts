import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject, combineLatest, filter } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { GospodarieService } from '../../services/gospodarie.service';
import { UatContextService } from '../../services/uat-context.service';
import { Gospodarie, Uat } from '../../models/gospodarie.model';
import { LayoutComponent } from '../../components/layout/layout.component';
import { PageHeaderComponent } from '../../components/page-header/page-header.component';
import { AuthService } from '../../services/auth.service';

import { GenericTableComponent, TableColumn, TableFilter, TableAction } from '../../components/generic-table/generic-table.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';
import { ActiveUatBannerComponent } from '../../components/active-uat-banner/active-uat-banner.component';

import { AppTranslatePipe } from '../../services/translate.pipe';

@Component({
  selector: 'app-gospodarie-list',
  standalone: true,
  imports: [CommonModule, RouterLink, LayoutComponent, PageHeaderComponent, FormsModule, GenericTableComponent, BreadcrumbsComponent, ActiveUatBannerComponent, AppTranslatePipe],
  templateUrl: './gospodarie-list.component.html'
})
export class GospodarieListComponent implements OnInit, OnDestroy {
  gospodarii: Gospodarie[] = [];
  user: any = null;
  selectedGospodarie: Gospodarie | null = null;
  activeUat: Uat | null = null;

  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'Gospodării', link: '/gospodarii' }
  ];

  columns: TableColumn[] = [
    { field: 'codGospodarie', header: 'Cod Gospodărie' },
    { field: 'tipGospodarie', header: 'Tip', type: 'badge', badgeClasses: { 'INDIVIDUALA': 'viewer', 'COLECTIVA': 'viewer', 'ASOCIATIE': 'viewer' } },
    { field: 'adresa.localitate', header: 'Localitate / Adresă', format: (val, row) => val || row.uat?.denumire || '-', subField: 'adresa.street' },
    { field: 'activa', header: 'Status', type: 'badge', format: val => val ? 'Activă' : 'Inactivă', badgeClasses: { 'true': 'admin', 'false': 'viewer' } }
  ];

  filters: TableFilter[] = [
    { field: 'search', label: 'Caută după adresă sau cod...', type: 'search', searchFields: ['codGospodarie', 'adresa.street', 'adresa.localitate', 'uat.denumire'] },
    { field: 'tipGospodarie', label: 'Tip Gospodărie', type: 'select', options: [{ label: 'Individuală', value: 'INDIVIDUALA' }, { label: 'Colectivă', value: 'COLECTIVA' }, { label: 'Asociație', value: 'ASOCIATIE' }] },
    { field: 'activa', label: 'Status', type: 'select', options: [{ label: 'Activă', value: true }, { label: 'Inactivă', value: false }] }
  ];

  actions: TableAction[] = [
    { icon: 'view', tooltip: 'Detalii', action: (row) => this.viewDetails(row.id) },
    { icon: 'delete', tooltip: 'Șterge', action: (row, event) => this.deleteRow(row.id, event) }
  ];

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

  currentPage: number = 1;
  pageSize: number = 6;
  totalPages: number = 1;
  currentSearch: string = '';
  currentTip: string = '';
  currentStatus: any = '';

  loadGospodarii(): void {
    const uatCode = this.user?.role === 'ROLE_SUPER_ADMIN'
      ? undefined
      : this.activeUat?.codSiruta;

    // Actually, GospodarieService currently only accepts uatCode, page, and size
    // It doesn't have backend filtering for search, tip, and status yet.
    // I should pass page and size, and implement filtering later if requested.
    this.gospodarieService.getAllGospodarii(uatCode, this.currentPage - 1, this.pageSize).subscribe({
      next: (response) => {
        this.totalPages = response.totalPages;
        this.gospodarii = response.content;
      },
      error: (err) => console.error(err)
    });
  }

  onPageChange(page: number) {
    this.currentPage = page;
    this.loadGospodarii();
  }

  onFilterChange(filters: Record<string, any>) {
    // Backend filtering not yet implemented for Gospodarie, so this just resets page
    this.currentPage = 1;
    this.loadGospodarii();
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



  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
