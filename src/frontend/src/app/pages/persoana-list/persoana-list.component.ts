import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { PersoanaService } from '../../services/persoana.service';
import { Persoana, PersoanaFizica, PersoanaJuridica } from '../../models/persoana.model';
import { AuthService } from '../../services/auth.service';
import { UatContextService } from '../../services/uat-context.service';

import { LayoutComponent } from '../../components/layout/layout.component';
import { PageHeaderComponent } from '../../components/page-header/page-header.component';
import { GenericTableComponent, TableColumn, TableFilter, TableAction } from '../../components/generic-table/generic-table.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';
import { PersonFormComponent } from '../persoana-form/persoana-form.component';
import { ActiveUatBannerComponent } from '../../components/active-uat-banner/active-uat-banner.component';

@Component({
  selector: 'app-persoana-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, LayoutComponent, PageHeaderComponent, GenericTableComponent, BreadcrumbsComponent, PersonFormComponent, ActiveUatBannerComponent],
  templateUrl: './persoana-list.component.html'
})
export class PersonListComponent implements OnInit {
  user: any = null;
  persoane: any[] = [];
  activeUat: any = null;


  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'Persoane', link: '/persoane' }
  ];

  showPersonModal = false;
  editPersonId?: number;

  columns: TableColumn[] = [
    { field: 'displayName', header: 'Nume / Denumire', type: 'avatar', subField: 'displayHandle' },
    { field: 'personType', header: 'Tip Persoană', type: 'badge', format: val => val === 'PHYSICAL_PERSON' ? 'Persoană Fizică' : 'Persoană Juridică', badgeClasses: { 'PHYSICAL_PERSON': 'viewer', 'LEGAL_ENTITY': 'admin' } },
    { field: 'displayIdentifier', header: 'CNP / CUI', format: val => val || '-' },
    { field: 'judet', header: 'Județ', format: val => val || '-' },
    { field: 'localitate', header: 'Localitate', format: val => val || '-' }
  ];

  filters: TableFilter[] = [
    { field: 'search', label: 'Caută după nume, CNP/CUI...', type: 'search', searchFields: ['displayName', 'displayIdentifier'] },
    { field: 'personType', label: 'Tip Persoană', type: 'select', options: [{label: 'Persoană Fizică', value: 'PHYSICAL_PERSON'}, {label: 'Persoană Juridică', value: 'LEGAL_ENTITY'}] }
  ];

  actions: TableAction[] = [
    { icon: 'edit', tooltip: 'Editare', action: (row) => this.editPerson(row.raw) },
    { icon: 'delete', tooltip: 'Ștergere', action: (row) => this.deletePerson(row.raw) }
  ];

  constructor(
    private persoanaService: PersoanaService,
    public authService: AuthService,
    private router: Router,
    public uatContextService: UatContextService
  ) {}

  ngOnInit() {
    this.authService.currentUser.subscribe(user => {
      this.user = user;
      if (!user) {
        this.router.navigate(['/login']);
        return;
      }
      this.uatContextService.activeUat$.subscribe(uat => {
        this.activeUat = uat;
        if (uat) {
          this.loadPersons();
        } else {
          this.persoane = [];
        }
      });
    });
  }

  loadPersons() {
    // we fetch all and let generic table filter, so pass empty strings to api if it requires
    this.persoanaService.getAllPersons('', '').subscribe({
      next: (data) => {
        this.persoane = data.map(p => ({
           raw: p,
           displayName: this.getPersonName(p),
           displayHandle: this.getPersonTypeHandle(p),
           displayIdentifier: this.getPersonIdentifier(p),
           personType: p.personType,
           judet: p.adresa?.county || '',
           localitate: p.adresa?.localitate || '',
           initials: this.getPersonName(p).substring(0, 1).toUpperCase(),
           avatarBg: p.personType === 'PHYSICAL_PERSON' ? '#3b82f6' : '#8b5cf6'
        })).sort((a, b) => (b.raw.id || 0) - (a.raw.id || 0));
      },
      error: (err) => console.error('Error fetching persoane', err)
    });
  }

  getPersonTypeHandle(p: Persoana): string {
    return p.personType === 'PHYSICAL_PERSON' ? 'Persoană Fizică' : 'Persoană Juridică';
  }

  getPersonName(persoana: Persoana): string {
    if (persoana.personType === 'PHYSICAL_PERSON') {
      const p = persoana as PersoanaFizica;
      return `${p.firstName} ${p.lastName}`;
    } else {
      const l = persoana as PersoanaJuridica;
      return l.companyName;
    }
  }

  getPersonIdentifier(persoana: Persoana): string {
    if (persoana.personType === 'PHYSICAL_PERSON') {
      return (persoana as PersoanaFizica).cnp || 'N/A';
    } else {
      return (persoana as PersoanaJuridica).cui || 'N/A';
    }
  }

  goToCreatePerson() {
    if (this.authService.currentUserSubject.value?.role === 'ROLE_SUPER_ADMIN' && !this.uatContextService.getActiveUat()?.tenantId) {
      alert('Vă rugăm să selectați mai întâi un UAT.');
      return;
    }
    this.editPersonId = undefined;
    this.showPersonModal = true;
  }

  editPerson(persoana: Persoana) {
    this.editPersonId = persoana.id;
    this.showPersonModal = true;
  }

  closePersonModal() {
    this.showPersonModal = false;
    this.loadPersons();
  }

  deletePerson(persoana: Persoana) {
    if (confirm('Are you sure you want to delete this persoana?')) {
      this.persoanaService.deletePerson(persoana.id!).subscribe({
        next: () => this.loadPersons(),
        error: (err) => console.error('Error deleting persoana', err)
      });
    }
  }

}

