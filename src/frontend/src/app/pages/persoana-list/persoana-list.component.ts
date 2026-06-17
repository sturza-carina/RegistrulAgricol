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
  suggestions: string[] = [];
  showSuggestions = false;

  currentPage = 1;
  itemsPerPage = 5;

  get paginatedItems() {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    return this.filteredPersons.slice(startIndex, startIndex + this.itemsPerPage);
  }

  get totalPages() {
    return Math.ceil(this.filteredPersons.length / this.itemsPerPage) || 1;
  }

  nextPage() {
    if (this.currentPage < this.totalPages) this.currentPage++;
  }

  prevPage() {
    if (this.currentPage > 1) this.currentPage--;
  }

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
        this.currentPage = 1;
      },
      error: (err) => console.error('Error fetching persoane', err)
    });
  }

  applyFilters() {
    this.loadPersons();
  }

  onSearchInput(event: any): void {
    this.searchQuery = event.target.value;
    this.updateSuggestions();
    this.loadPersons();
  }

  updateSuggestions(): void {
    const term = this.searchQuery.toLowerCase().trim();
    if (!term || this.persoane.length === 0) {
      this.suggestions = [];
      this.showSuggestions = false;
      return;
    }
    const names = this.persoane.map(p => this.getPersonName(p));
    const matched = names.filter(n => n.toLowerCase().startsWith(term));
    this.suggestions = [...new Set(matched)].slice(0, 8);
    this.showSuggestions = this.suggestions.length > 0;
  }

  selectSuggestion(suggestion: string): void {
    this.searchQuery = suggestion;
    this.showSuggestions = false;
    this.suggestions = [];
    this.loadPersons();
  }

  hideSuggestions(): void {
    setTimeout(() => { this.showSuggestions = false; }, 150);
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

