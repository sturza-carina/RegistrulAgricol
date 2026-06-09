import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PersonService } from '../../services/person.service';
import { Person, PhysicalPerson, LegalEntity } from '../../models/person.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-person-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './person-list.component.html',
  styleUrls: ['./person-list.component.css']
})
export class PersonListComponent implements OnInit {
  persons: Person[] = [];
  filteredPersons: Person[] = [];
  user: any;
  
  searchQuery: string = '';
  typeFilter: string = 'All Types';

  constructor(
    private personService: PersonService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.user = this.authService.currentUserSubject.value;
    this.loadPersons();
  }

  loadPersons() {
    let filterType = '';
    if (this.typeFilter === 'Physical Person') filterType = 'PHYSICAL_PERSON';
    if (this.typeFilter === 'Legal Entity') filterType = 'LEGAL_ENTITY';

    this.personService.getAllPersons(this.searchQuery, filterType).subscribe({
      next: (data) => {
        this.persons = data;
        this.filteredPersons = data;
      },
      error: (err) => console.error('Error fetching persons', err)
    });
  }

  applyFilters() {
    this.loadPersons();
  }

  clearFilters() {
    this.searchQuery = '';
    this.typeFilter = 'All Types';
    this.loadPersons();
  }

  getPersonName(person: Person): string {
    if (person.personType === 'PHYSICAL_PERSON') {
      const p = person as PhysicalPerson;
      return `${p.firstName} ${p.lastName}`;
    } else {
      const l = person as LegalEntity;
      return l.companyName;
    }
  }

  getPersonIdentifier(person: Person): string {
    if (person.personType === 'PHYSICAL_PERSON') {
      return (person as PhysicalPerson).cnp || 'N/A';
    } else {
      return (person as LegalEntity).cui || 'N/A';
    }
  }

  goToCreatePerson() {
    this.router.navigate(['/persons/new']);
  }

  editPerson(person: Person) {
    this.router.navigate(['/persons/edit', person.id]);
  }

  deletePerson(person: Person) {
    if (confirm('Are you sure you want to delete this person?')) {
      this.personService.deletePerson(person.id!).subscribe({
        next: () => this.loadPersons(),
        error: (err) => console.error('Error deleting person', err)
      });
    }
  }

  // Navigation
  goToDashboard() {
    const role = this.user?.role || '';
    if (role === 'ROLE_SUPER_ADMIN') this.router.navigate(['/super-admin']);
    else if (role === 'ROLE_ADMIN') this.router.navigate(['/tenant-admin']);
    else this.router.navigate(['/dashboard']);
  }

  goToUsers() {
    this.router.navigate(['/user-management']);
  }
  
  goToPersons() {
    this.router.navigate(['/persons']);
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
