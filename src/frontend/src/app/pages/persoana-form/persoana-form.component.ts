import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { PersoanaService } from '../../services/persoana.service';
import { Persoana, PersoanaFizica, PersoanaJuridica } from '../../models/persoana.model';

import { SidebarComponent } from '../../components/sidebar/sidebar.component';

@Component({
  selector: 'app-persoana-form',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent, RouterModule],
  templateUrl: './persoana-form.component.html'
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

  // Adresa fields
  county: string = '';
  localitate: string = '';
  street: string = '';
  streetNumber: string = '';
  building: string = '';
  staircase: string = '';
  floor?: number;
  apartmentNumber?: number;
  postalCode: string = '';

  // PersoanaFizica fields
  firstName: string = '';
  lastName: string = '';
  cnp: string = '';
  dateOfBirth: string = '';
  isHeadOfHousehold: boolean = false;

  // PersoanaJuridica fields
  companyName: string = '';
  cui: string = '';
  registrationNumber: string = '';
  legalRepresentative: string = '';

  gospodarieId?: number;

  constructor(
    private persoanaService: PersoanaService,
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

    this.route.queryParams.subscribe(params => {
      if (params['gospodarieId']) {
        this.gospodarieId = +params['gospodarieId'];
      }
    });
  }

  loadPerson(id: number) {
    this.persoanaService.getPersonById(id).subscribe({
      next: (persoana) => {
        this.personType = persoana.personType;
        this.phoneNumber = persoana.phoneNumber || '';
        this.email = persoana.email || '';
        this.registerVolume = persoana.registerVolume || '';
        this.registerPosition = persoana.registerPosition || '';
        this.notes = persoana.notes || '';

        if (persoana.address) {
          this.county = persoana.address.county || '';
          this.localitate = persoana.address.localitate || '';
          this.street = persoana.address.street || '';
          this.streetNumber = persoana.address.streetNumber || '';
          this.building = persoana.address.building || '';
          this.staircase = persoana.address.staircase || '';
          this.floor = persoana.address.floor;
          this.apartmentNumber = persoana.address.apartmentNumber;
          this.postalCode = persoana.address.postalCode || '';
        }

        if (persoana.personType === 'PHYSICAL_PERSON') {
          const p = persoana as PersoanaFizica;
          this.firstName = p.firstName || '';
          this.lastName = p.lastName || '';
          this.cnp = p.cnp || '';
          this.dateOfBirth = p.dateOfBirth ? p.dateOfBirth.substring(0,10) : '';
          this.isHeadOfHousehold = p.isHeadOfHousehold || false;
        } else {
          const l = persoana as PersoanaJuridica;
          this.companyName = l.companyName || '';
          this.cui = l.cui || '';
          this.registrationNumber = l.registrationNumber || '';
          this.legalRepresentative = l.legalRepresentative || '';
        }
      },
      error: (err) => console.error('Error loading persoana', err)
    });
  }

  save() {
    const address = {
      county: this.county,
      localitate: this.localitate,
      street: this.street,
      streetNumber: this.streetNumber,
      building: this.building,
      staircase: this.staircase,
      floor: this.floor,
      apartmentNumber: this.apartmentNumber,
      postalCode: this.postalCode
    };

    let gospodarieObj = this.gospodarieId ? { id: this.gospodarieId } : undefined;
    let payload: any;

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
        notes: this.notes,
        gospodarie: gospodarieObj
      } as PersoanaFizica;
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
        notes: this.notes,
        gospodarie: gospodarieObj
      } as PersoanaJuridica;
    }

    if (this.isEditMode && this.personId) {
      this.persoanaService.updatePerson(this.personId, payload).subscribe({
        next: () => this.router.navigate(['/persoane']),
        error: (err) => {
          const msg = err.error?.message || err.message;
          alert('Error updating persoana: ' + msg);
        }
      });
    } else {
      this.persoanaService.createPerson(payload).subscribe({
        next: () => this.router.navigate(['/persoane']),
        error: (err) => {
          const msg = err.error?.message || err.message;
          alert('Error creating persoana: ' + msg);
        }
      });
    }
  }

  cancel() {
    this.router.navigate(['/persoane']);
  }
}


