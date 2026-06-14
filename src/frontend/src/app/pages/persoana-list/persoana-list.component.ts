import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { PersoanaService } from '../../services/persoana.service';
import { Persoana, PersoanaFizica, PersoanaJuridica } from '../../models/persoana.model';
import { AuthService } from '../../services/auth.service';

import { SidebarComponent } from '../../components/sidebar/sidebar.component';

@Component({
  selector: 'app-persoana-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, SidebarComponent],
  templateUrl: './persoana-list.component.html',
  styleUrls: ['./persoana-list.component.css']
})
export class PersonListComponent implements OnInit {
  persoane: Persoana[] = [];
  filteredPersons: Persoana[] = [];
  user: any;
  
  searchQuery: string = '';
  typeFilter: string = 'Toate Tipurile';

  constructor(
    private persoanaService: PersoanaService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.user = this.authService.currentUserSubject.value;
    this.loadPersons();
  }

  loadPersons() {
    let filterType = '';
    if (this.typeFilter === 'Persoană Fizică') filterType = 'PHYSICAL_PERSON';
    if (this.typeFilter === 'Persoană Juridică') filterType = 'LEGAL_ENTITY';

    this.persoanaService.getAllPersons(this.searchQuery, filterType).subscribe({
      next: (data) => {
        this.persoane = data;
        this.filteredPersons = data;
      },
      error: (err) => console.error('Error fetching persoane', err)
    });
  }

  applyFilters() {
    this.loadPersons();
  }

  clearFilters() {
    this.searchQuery = '';
    this.typeFilter = 'Toate Tipurile';
    this.loadPersons();
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
    this.router.navigate(['/persoane/new']);
  }

  editPerson(persoana: Persoana) {
    this.router.navigate(['/persoane/edit', persoana.id]);
  }

  deletePerson(persoana: Persoana) {
    if (confirm('Are you sure you want to delete this persoana?')) {
      this.persoanaService.deletePerson(persoana.id!).subscribe({
        next: () => this.loadPersons(),
        error: (err) => console.error('Error deleting persoana', err)
      });
    }
  }
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}

