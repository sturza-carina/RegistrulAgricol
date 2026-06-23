import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { GospodarieService } from '../../services/gospodarie.service';
import { UatContextService } from '../../services/uat-context.service';
import { Uat } from '../../models/gospodarie.model';

import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';
import { GenericFormComponent } from '../../components/generic-form/generic-form.component';
import { FormConfig } from '../../components/generic-form/generic-form.models';

@Component({
  selector: 'app-gospodarie-form',
  standalone: true,
  imports: [CommonModule, RouterLink, SidebarComponent, BreadcrumbsComponent, GenericFormComponent],
  templateUrl: './gospodarie-form.component.html'
})
export class GospodarieFormComponent implements OnInit {
  isEditMode = false;
  gospodarieId: number | null = null;
  breadcrumbItems: BreadcrumbItem[] = [];
  isSaving = false;

  formInitialData: any = {
    tipGospodarie: 'INDIVIDUALA',
    activa: true
  };

  formConfig: FormConfig = {
    submitText: 'Salvează',
    cancelText: 'Anulează',
    sections: [
      {
        title: 'Informații Generale',
        fields: [
          { name: 'codGospodarie', label: 'Cod Gospodărie', type: 'text', required: true, width: 'half' },
          { name: 'tipGospodarie', label: 'Tip Gospodărie', type: 'select', required: true, width: 'half', options: [
            { label: 'Individuală', value: 'INDIVIDUALA' },
            { label: 'Colectivă', value: 'COLECTIVA' },
            { label: 'Asociație', value: 'ASOCIATIE' }
          ] },
          { name: 'uat', label: 'UAT', type: 'select', required: true, width: 'half', options: [], disabled: true },
          { name: 'activa', label: 'Activă', type: 'checkbox', required: false, width: 'half' }
        ]
      },
      {
        title: 'Adresă',
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
      }
    ]
  };

  constructor(
    private gospodarieService: GospodarieService,
    private uatContextService: UatContextService,
    private router: Router,
    private route: ActivatedRoute,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.loadUats();
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.isEditMode = true;
        this.gospodarieId = +id;
        this.loadGospodarie(this.gospodarieId);
      } else {
        const activeUat = this.uatContextService.getActiveUat();
        if (activeUat) {
          this.formInitialData = { ...this.formInitialData, uat: activeUat.id };
        }
      }
      this.updateBreadcrumbs();
    });
  }

  updateBreadcrumbs() {
    this.breadcrumbItems = [
      { label: 'Gospodării', link: '/gospodarii' },
      { label: this.isEditMode ? 'Editare' : 'Adăugare' }
    ];
    this.formConfig.submitText = this.isEditMode ? 'Actualizează' : 'Salvează';
  }

  loadUats() {
    this.http.get<Uat[]>('/api/uats').subscribe(data => {
      const uatField = this.formConfig.sections[0].fields.find(f => f.name === 'uat');
      if (uatField) {
        uatField.options = data.map(u => ({ label: u.denumire, value: u.id }));
        // Force reference update to trigger change detection for generic form if necessary
        this.formConfig = { ...this.formConfig };
      }
    });
  }

  loadGospodarie(id: number) {
    this.gospodarieService.getGospodarieById(id).subscribe(data => {
      const initData: any = { ...data };
      if (data.uat) {
        initData.uat = data.uat.id;
      }
      if (data.adresa) {
        initData.county = data.adresa.county;
        initData.localitate = data.adresa.localitate;
        initData.street = data.adresa.street;
        initData.streetNumber = data.adresa.streetNumber;
        initData.building = data.adresa.building;
        initData.staircase = data.adresa.staircase;
        initData.floor = data.adresa.floor;
        initData.apartmentNumber = data.adresa.apartmentNumber;
        initData.postalCode = data.adresa.postalCode;
      }
      this.formInitialData = initData;
    });
  }

  save(formData: any) {
    const payload = {
      codGospodarie: formData.codGospodarie,
      tipGospodarie: formData.tipGospodarie,
      activa: formData.activa,
      uat: { id: formData.uat },
      adresa: {
        county: formData.county,
        localitate: formData.localitate,
        street: formData.street,
        streetNumber: formData.streetNumber,
        building: formData.building,
        staircase: formData.staircase,
        floor: formData.floor ? Number(formData.floor) : undefined,
        apartmentNumber: formData.apartmentNumber ? Number(formData.apartmentNumber) : undefined,
        postalCode: formData.postalCode
      }
    };

    this.isSaving = true;

    if (this.isEditMode && this.gospodarieId) {
      this.gospodarieService.updateGospodarie(this.gospodarieId, payload as any).subscribe({
        next: () => {
          this.isSaving = false;
          this.router.navigate(['/gospodarii']);
        },
        error: (err) => {
          this.isSaving = false;
          console.error(err);
          alert('Eroare la salvare');
        }
      });
    } else {
      this.gospodarieService.createGospodarie(payload as any).subscribe({
        next: () => {
          this.isSaving = false;
          this.router.navigate(['/gospodarii']);
        },
        error: (err) => {
          this.isSaving = false;
          console.error(err);
          alert('Eroare la salvare');
        }
      });
    }
  }

  cancel() {
    this.router.navigate(['/gospodarii']);
  }
}
