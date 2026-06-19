import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { PersoanaService } from '../../services/persoana.service';
import { Persoana, PersoanaFizica, PersoanaJuridica } from '../../models/persoana.model';

import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';
import { GenericFormComponent } from '../../components/generic-form/generic-form.component';
import { FormConfig } from '../../components/generic-form/generic-form.models';

@Component({
  selector: 'app-persoana-form',
  standalone: true,
  imports: [CommonModule, SidebarComponent, RouterModule, BreadcrumbsComponent, GenericFormComponent],
  templateUrl: './persoana-form.component.html'
})
export class PersonFormComponent implements OnInit {
  @Input() isModal = false;
  @Input() personId?: number;
  @Input() set editId(val: number | undefined) {
    if (val) {
      this.isEditMode = true;
      this.personId = val;
      this.loadPerson(val);
    }
  }
  @Output() closeForm = new EventEmitter<void>();

  isEditMode = false;
  isSaving = false;
  breadcrumbItems: BreadcrumbItem[] = [];
  @Input() gospodarieId?: number;

  formInitialData: any = {
    personType: 'PHYSICAL_PERSON'
  };

  formConfig: FormConfig = {
    submitText: 'Adaugă',
    cancelText: 'Anulează',
    sections: [
      {
        title: 'Informații Generale',
        fields: [
          { name: 'personType', label: 'Tip Entitate', type: 'select', required: true, width: 'full', options: [
            { label: 'Persoană Fizică', value: 'PHYSICAL_PERSON' },
            { label: 'Persoană Juridică', value: 'LEGAL_ENTITY' }
          ] },
          
          // Physical Person Fields
          { name: 'firstName', label: 'Prenume', type: 'text', required: true, width: 'half', showIf: (m) => m.personType === 'PHYSICAL_PERSON' },
          { name: 'lastName', label: 'Nume', type: 'text', required: true, width: 'half', showIf: (m) => m.personType === 'PHYSICAL_PERSON' },
          { name: 'cnp', label: 'CNP', type: 'text', required: true, width: 'half', showIf: (m) => m.personType === 'PHYSICAL_PERSON' },
          { name: 'dateOfBirth', label: 'Data Nașterii', type: 'date', required: false, width: 'half', showIf: (m) => m.personType === 'PHYSICAL_PERSON' },
          { name: 'isHeadOfHousehold', label: 'Cap Gospodărie', type: 'checkbox', required: false, width: 'full', showIf: (m) => m.personType === 'PHYSICAL_PERSON' },

          // Legal Entity Fields
          { name: 'companyName', label: 'Denumire Companie', type: 'text', required: true, width: 'half', showIf: (m) => m.personType === 'LEGAL_ENTITY' },
          { name: 'cui', label: 'CUI', type: 'text', required: true, width: 'half', showIf: (m) => m.personType === 'LEGAL_ENTITY' },
          { name: 'registrationNumber', label: 'Număr de Înregistrare', type: 'text', required: false, width: 'half', placeholder: 'J.../...', showIf: (m) => m.personType === 'LEGAL_ENTITY' },
          { name: 'legalRepresentative', label: 'Reprezentant Legal', type: 'text', required: false, width: 'half', showIf: (m) => m.personType === 'LEGAL_ENTITY' }
        ]
      },
      {
        title: 'Detalii Adresă',
        fields: [
          { name: 'county', label: 'Județ', type: 'text', required: false, width: 'half' },
          { name: 'localitate', label: 'Localitate', type: 'text', required: false, width: 'half' },
          { name: 'street', label: 'Stradă', type: 'text', required: false, width: 'half' },
          { name: 'streetNumber', label: 'Număr', type: 'text', required: false, width: 'half' },
          { name: 'building', label: 'Bloc', type: 'text', required: false, width: 'half' },
          { name: 'staircase', label: 'Scară', type: 'text', required: false, width: 'half' },
          { name: 'floor', label: 'Etaj', type: 'number', required: false, width: 'half' },
          { name: 'apartmentNumber', label: 'Apartament', type: 'number', required: false, width: 'half' },
          { name: 'postalCode', label: 'Cod Poștal', type: 'text', required: false, width: 'half' }
        ]
      },
      {
        title: 'Contact și Registru Agricol',
        fields: [
          { name: 'phoneNumber', label: 'Număr de Telefon', type: 'text', required: false, width: 'half' },
          { name: 'email', label: 'Adresă Email', type: 'email', required: false, width: 'half' },
          { name: 'registerVolume', label: 'Volum Registru', type: 'text', required: false, width: 'half' },
          { name: 'registerPosition', label: 'Poziție Registru', type: 'text', required: false, width: 'half' },
          { name: 'notes', label: 'Note', type: 'textarea', required: false, width: 'full' }
        ]
      }
    ]
  };

  constructor(
    private persoanaService: PersoanaService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    if (!this.isModal) {
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
        this.updateBreadcrumbs();
      });
    }
  }

  updateBreadcrumbs() {
    this.breadcrumbItems = [
      { label: 'Persoane', link: '/persoane' },
      { label: this.isEditMode ? 'Editare' : 'Adăugare persoană' }
    ];
    this.formConfig.submitText = this.isEditMode ? 'Actualizează' : 'Adaugă';
  }

  loadPerson(id: number) {
    this.persoanaService.getPersonById(id).subscribe({
      next: (persoana) => {
        const data: any = { ...persoana };

        // Flatten address
        if (persoana.address) {
          data.county = persoana.address.county;
          data.localitate = persoana.address.localitate;
          data.street = persoana.address.street;
          data.streetNumber = persoana.address.streetNumber;
          data.building = persoana.address.building;
          data.staircase = persoana.address.staircase;
          data.floor = persoana.address.floor;
          data.apartmentNumber = persoana.address.apartmentNumber;
          data.postalCode = persoana.address.postalCode;
        }

        if (persoana.personType === 'PHYSICAL_PERSON') {
          const p = persoana as PersoanaFizica;
          if (p.dateOfBirth) {
            data.dateOfBirth = p.dateOfBirth.substring(0, 10);
          }
        }

        // Disable personType in edit mode
        this.formConfig.sections[0].fields[0].disabled = true;

        this.formInitialData = data;
        this.updateBreadcrumbs();
      },
      error: (err) => console.error('Error loading persoana', err)
    });
  }

  save(formData: any) {
    const address = {
      county: formData.county,
      localitate: formData.localitate,
      street: formData.street,
      streetNumber: formData.streetNumber,
      building: formData.building,
      staircase: formData.staircase,
      floor: formData.floor,
      apartmentNumber: formData.apartmentNumber,
      postalCode: formData.postalCode
    };

    let gospodarieObj = this.gospodarieId ? { id: this.gospodarieId } : undefined;
    let payload: any = {
      personType: formData.personType,
      address,
      phoneNumber: formData.phoneNumber,
      email: formData.email,
      registerVolume: formData.registerVolume,
      registerPosition: formData.registerPosition,
      notes: formData.notes,
      gospodarie: gospodarieObj
    };

    if (formData.personType === 'PHYSICAL_PERSON') {
      payload = {
        ...payload,
        firstName: formData.firstName,
        lastName: formData.lastName,
        cnp: formData.cnp,
        dateOfBirth: formData.dateOfBirth,
        isHeadOfHousehold: formData.isHeadOfHousehold
      } as PersoanaFizica;
    } else {
      payload = {
        ...payload,
        companyName: formData.companyName,
        cui: formData.cui,
        registrationNumber: formData.registrationNumber,
        legalRepresentative: formData.legalRepresentative
      } as PersoanaJuridica;
    }

    this.isSaving = true;

    if (this.isEditMode && this.personId) {
      this.persoanaService.updatePerson(this.personId, payload).subscribe({
        next: () => {
          this.isSaving = false;
          if (this.isModal) this.closeForm.emit();
          else this.router.navigate(['/persoane']);
        },
        error: (err) => {
          this.isSaving = false;
          const msg = err.error?.message || err.message;
          alert('Error updating persoana: ' + msg);
        }
      });
    } else {
      this.persoanaService.createPerson(payload).subscribe({
        next: () => {
          this.isSaving = false;
          if (this.isModal) this.closeForm.emit();
          else this.router.navigate(['/persoane']);
        },
        error: (err) => {
          this.isSaving = false;
          const msg = err.error?.message || err.message;
          alert('Error creating persoana: ' + msg);
        }
      });
    }
  }

  cancel() {
    if (this.isModal) this.closeForm.emit();
    else this.router.navigate(['/persoane']);
  }
}
