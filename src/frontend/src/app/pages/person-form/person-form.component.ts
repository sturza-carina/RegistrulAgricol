import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { PersonService } from '../../services/person.service';
import { Person, PhysicalPerson, LegalEntity } from '../../models/person.model';

@Component({
  selector: 'app-person-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './person-form.component.html'
})
export class PersonFormComponent implements OnInit {
  isEditMode = false;
  personId?: number;

  // Form State
  personType: 'PHYSICAL_PERSON' | 'LEGAL_ENTITY' = 'PHYSICAL_PERSON';
  
  // Common fields
  phoneNumber: string = '';
  email: string = '';
  registerVolume: string = '';
  registerPosition: string = '';
  notes: string = '';

  // Address fields
  county: string = '';
  locality: string = '';
  street: string = '';
  streetNumber: string = '';
  building: string = '';
  staircase: string = '';
  floor?: number;
  apartmentNumber?: number;
  postalCode: string = '';

  // PhysicalPerson fields
  firstName: string = '';
  lastName: string = '';
  cnp: string = '';
  dateOfBirth: string = '';
  isHeadOfHousehold: boolean = false;

  // LegalEntity fields
  companyName: string = '';
  cui: string = '';
  registrationNumber: string = '';
  legalRepresentative: string = '';

  constructor(
    private personService: PersonService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      if (idParam) {
        this.isEditMode = true;
        this.personId = +idParam;
        this.loadPerson(this.personId);
      }
    });
  }

  loadPerson(id: number) {
    this.personService.getPersonById(id).subscribe({
      next: (person) => {
        this.personType = person.personType;
        this.phoneNumber = person.phoneNumber || '';
        this.email = person.email || '';
        this.registerVolume = person.registerVolume || '';
        this.registerPosition = person.registerPosition || '';
        this.notes = person.notes || '';

        if (person.address) {
          this.county = person.address.county || '';
          this.locality = person.address.locality || '';
          this.street = person.address.street || '';
          this.streetNumber = person.address.streetNumber || '';
          this.building = person.address.building || '';
          this.staircase = person.address.staircase || '';
          this.floor = person.address.floor;
          this.apartmentNumber = person.address.apartmentNumber;
          this.postalCode = person.address.postalCode || '';
        }

        if (person.personType === 'PHYSICAL_PERSON') {
          const p = person as PhysicalPerson;
          this.firstName = p.firstName || '';
          this.lastName = p.lastName || '';
          this.cnp = p.cnp || '';
          this.dateOfBirth = p.dateOfBirth ? p.dateOfBirth.substring(0,10) : '';
          this.isHeadOfHousehold = p.isHeadOfHousehold || false;
        } else {
          const l = person as LegalEntity;
          this.companyName = l.companyName || '';
          this.cui = l.cui || '';
          this.registrationNumber = l.registrationNumber || '';
          this.legalRepresentative = l.legalRepresentative || '';
        }
      },
      error: (err) => console.error('Error loading person', err)
    });
  }

  save() {
    const address = {
      county: this.county,
      locality: this.locality,
      street: this.street,
      streetNumber: this.streetNumber,
      building: this.building,
      staircase: this.staircase,
      floor: this.floor,
      apartmentNumber: this.apartmentNumber,
      postalCode: this.postalCode
    };

    let payload: Person;

    if (this.personType === 'PHYSICAL_PERSON') {
      payload = {
        personType: 'PHYSICAL_PERSON',
        firstName: this.firstName,
        lastName: this.lastName,
        cnp: this.cnp,
        dateOfBirth: this.dateOfBirth,
        isHeadOfHousehold: this.isHeadOfHousehold,
        address,
        phoneNumber: this.phoneNumber,
        email: this.email,
        registerVolume: this.registerVolume,
        registerPosition: this.registerPosition,
        notes: this.notes
      } as PhysicalPerson;
    } else {
      payload = {
        personType: 'LEGAL_ENTITY',
        companyName: this.companyName,
        cui: this.cui,
        registrationNumber: this.registrationNumber,
        legalRepresentative: this.legalRepresentative,
        address,
        phoneNumber: this.phoneNumber,
        email: this.email,
        registerVolume: this.registerVolume,
        registerPosition: this.registerPosition,
        notes: this.notes
      } as LegalEntity;
    }

    if (this.isEditMode && this.personId) {
      this.personService.updatePerson(this.personId, payload).subscribe({
        next: () => this.router.navigate(['/persons']),
        error: (err) => {
          const msg = err.error?.message || err.message;
          alert('Error updating person: ' + msg);
        }
      });
    } else {
      this.personService.createPerson(payload).subscribe({
        next: () => this.router.navigate(['/persons']),
        error: (err) => {
          const msg = err.error?.message || err.message;
          alert('Error creating person: ' + msg);
        }
      });
    }
  }

  cancel() {
    this.router.navigate(['/persons']);
  }
}
