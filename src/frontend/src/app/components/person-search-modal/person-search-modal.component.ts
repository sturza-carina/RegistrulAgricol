import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Persoana } from '../../models/persoana.model';

import { AppTranslatePipe } from '../../services/translate.pipe';

@Component({
  selector: 'app-person-search-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, AppTranslatePipe],
  templateUrl: './person-search-modal.component.html',
  styleUrls: ['./person-search-modal.component.css']
})
export class PersonSearchModalComponent implements OnInit {
  @Input() persoane: Persoana[] = [];
  @Output() personSelected = new EventEmitter<Persoana>();
  @Output() modalClosed = new EventEmitter<void>();

  searchTerm = '';
  filteredPersons: Persoana[] = [];

  ngOnInit(): void {
    this.filteredPersons = this.persoane;
  }

  onSearch(event: any): void {
    this.searchTerm = event.target.value.toLowerCase();
    this.applyFilter();
  }

  applyFilter(): void {
    if (!this.searchTerm) {
      this.filteredPersons = this.persoane;
      return;
    }

    this.filteredPersons = this.persoane.filter(p => {
      // Search by CNP
      const cnp = (p as any).cnp || '';
      if (cnp.toLowerCase().includes(this.searchTerm)) {
        return true;
      }

      // Search by name (for physical persons)
      const firstName = (p as any).firstName || '';
      const lastName = (p as any).lastName || '';
      if (firstName.toLowerCase().includes(this.searchTerm) || lastName.toLowerCase().includes(this.searchTerm)) {
        return true;
      }

      // Search by company name (for legal entities)
      const companyName = (p as any).companyName || '';
      if (companyName.toLowerCase().includes(this.searchTerm)) {
        return true;
      }

      // Search by CUI (for legal entities)
      const cui = (p as any).cui || '';
      if (cui.toLowerCase().includes(this.searchTerm)) {
        return true;
      }

      return false;
    });
  }

  selectPerson(person: Persoana): void {
    this.personSelected.emit(person);
    this.closeModal();
  }

  closeModal(): void {
    this.modalClosed.emit();
  }

  getPersonDisplayName(person: Persoana): string {
    if (person.personType === 'PHYSICAL_PERSON') {
      const physical = person as { id?: number; firstName?: string; lastName?: string; cnp?: string };
      const name = [physical.firstName, physical.lastName].filter(Boolean).join(' ') || `Persoană #${person.id}`;
      const cnp = physical.cnp ? ` (CNP: ${physical.cnp})` : '';
      return name + cnp;
    }

    if (person.personType === 'LEGAL_ENTITY') {
      const legal = person as { id?: number; companyName?: string; cui?: string };
      const name = legal.companyName || `Persoană #${person.id}`;
      const cui = legal.cui ? ` (CUI: ${legal.cui})` : '';
      return name + cui;
    }

    return `Persoană #${person.id}`;
  }
}

