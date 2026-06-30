import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { GospodarieService } from '../../services/gospodarie.service';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { UatService } from '../../services/uat.service';
import { UatContextService } from '../../services/uat-context.service';
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
  @Input() isEmbedded = false;
  @Input() inputGospodarieId: number | null = null;
  
  isEditMode = false;
  gospodarieId: number | null = null;
  breadcrumbItems: BreadcrumbItem[] = [];
  isSaving = false;
  private tenantUats: any[] = [];

  judete: string[] = [];
  localitati: any[] = [];
  selectedJudet = '';

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
          {
            name: 'tipGospodarie', label: 'Tip Gospodărie', type: 'select', required: true, width: 'half', options: [
              { label: 'Individuală', value: 'INDIVIDUALA' },
              { label: 'Colectivă', value: 'COLECTIVA' },
              { label: 'Asociație', value: 'ASOCIATIE' }
            ]
          },
          { name: 'activa', label: 'Activă', type: 'checkbox', required: false, width: 'half' }
        ]
      },
      {
        title: 'Adresă',
        fields: [
          { name: 'county', label: 'Județ', type: 'select', required: true, width: 'half', options: [] },
          { name: 'uatId', label: 'Localitate (UAT)', type: 'select', required: true, width: 'half', options: [], disabled: true },
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
    private uatService: UatService,
    private uatContextService: UatContextService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit() {
    this.loadJudete();
    
    if (this.isEmbedded && this.inputGospodarieId) {
      this.isEditMode = true;
      this.gospodarieId = this.inputGospodarieId;
      this.loadGospodarie(this.gospodarieId);
      return;
    }

    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.isEditMode = true;
        this.gospodarieId = +id;
        this.loadGospodarie(this.gospodarieId);
      } else {
        const activeUat = this.uatContextService.getActiveUat();
        if (activeUat) {
          this.formInitialData = {
            ...this.formInitialData,
            county: activeUat.judet,
            uatId: String(activeUat.id)
          };
          this.onJudetChange(activeUat.judet, String(activeUat.id));
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

  shouldLockAddress(): boolean {
    return this.isEditMode || this.uatContextService.getActiveUat() !== null;
  }

  loadJudete() {
    this.uatService.getTenantUats().subscribe(data => {
      this.tenantUats = data;
      const uniqueCounties = Array.from(new Set(data.map(u => u.judet)));
      this.judete = uniqueCounties as string[];
      
      const countyField = this.formConfig.sections[1].fields.find(f => f.name === 'county');
      if (countyField) {
        countyField.options = uniqueCounties.map(j => ({ label: j as string, value: j as string }));
        countyField.disabled = this.shouldLockAddress();
        this.formConfig = { ...this.formConfig };
      }

      if (this.formInitialData && this.formInitialData.county) {
        this.onJudetChange(this.formInitialData.county, this.formInitialData.uatId);
      }
    });
  }

  // Interceptăm evenimentul când formularul generic detectează modificări în câmpuri
  // Notă: Dacă componenta vostră GenericFormComponent expune un eveniment (valueChanges) sau similar,
  // va trebui să legi această metodă în HTML.
  onJudetChange(judetName: string, selectedUatId: number | string | null = null) {
    this.selectedJudet = judetName;
    const uatField = this.formConfig.sections[1].fields.find(f => f.name === 'uatId');

    if (!uatField) return;

    if (!judetName) {
      uatField.options = [];
      uatField.disabled = true;
      this.formConfig = { ...this.formConfig };
      return;
    }

    const filteredUats = this.tenantUats.filter(u => u.judet === judetName);
    this.localitati = filteredUats;
    uatField.disabled = this.shouldLockAddress();
    uatField.options = filteredUats.map(l => ({ label: `${l.denumire} (${l.tipUat})`, value: String(l.id) }));

    if (selectedUatId) {
      this.formInitialData = { ...this.formInitialData, uatId: String(selectedUatId) };
    }
    this.formConfig = { ...this.formConfig };
  }

  onFieldValueChange(event: { fieldName: string, value: any }) {
    if (event.fieldName === 'county') {
      // Când se schimbă județul, curățăm uatId-ul vechi și încărcăm localitățile noi
      this.formInitialData = { ...this.formInitialData, county: event.value, uatId: null };
      this.onJudetChange(event.value);
    }
  }



  loadGospodarie(id: number) {
    this.gospodarieService.getGospodarieById(id).subscribe(data => {
      const county = data.uat?.judet ?? data.adresa?.county ?? '';
      const uatId = data.uat?.id ? String(data.uat.id) : null;

      const initData: any = {
        codGospodarie: data.codGospodarie,
        tipGospodarie: data.tipGospodarie,
        activa: data.activa,
        county: county,
        uatId: uatId
      };

      if (data.adresa) {
        initData.street = data.adresa.street;
        initData.streetNumber = data.adresa.streetNumber;
        initData.building = data.adresa.building;
        initData.staircase = data.adresa.staircase;
        initData.floor = data.adresa.floor;
        initData.apartmentNumber = data.adresa.apartmentNumber;
        initData.postalCode = data.adresa.postalCode;
      }

      this.formInitialData = initData;

      if (county && this.tenantUats.length > 0) {
        this.onJudetChange(county, uatId);
      }
    });
  }

  save(formData: any) {
    this.isSaving = true;
    const selectedUatId = formData.uatId ? Number(formData.uatId) : null;
    const selectedLocalitate = selectedUatId
      ? this.localitati.find(l => l.id === selectedUatId)
      : null;

    const payload = {
      codGospodarie: formData.codGospodarie,
      tipGospodarie: formData.tipGospodarie,
      activa: formData.activa,
      uatId: selectedUatId,
      uat: selectedUatId ? { id: selectedUatId } : null,
      adresa: {
        county: formData.county,
        localitate: selectedLocalitate ? selectedLocalitate.denumire : formData.localitate,
        street: formData.street,
        streetNumber: formData.streetNumber,
        building: formData.building,
        staircase: formData.staircase,
        floor: formData.floor ? Number(formData.floor) : undefined,
        apartmentNumber: formData.apartmentNumber ? Number(formData.apartmentNumber) : undefined,
        postalCode: formData.postalCode
      }
    };

    if (this.isEditMode && this.gospodarieId) {
      this.gospodarieService.updateGospodarie(this.gospodarieId, payload as any).subscribe({
        next: () => {
          this.isSaving = false;
          if (this.isEmbedded) {
            alert('Modificările au fost salvate cu succes.');
            this.loadGospodarie(this.gospodarieId!);
          } else {
            this.router.navigate(['/gospodarii']);
          }
        },
        error: (err) => {
          this.isSaving = false;
          console.error(err);
          alert('Eroare la salvarea modificărilor.');
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
          alert('Eroare la crearea gospodăriei.');
        }
      });
    }
  }

  cancel() {
    if (this.isEmbedded && this.gospodarieId) {
      this.loadGospodarie(this.gospodarieId);
    } else {
      this.router.navigate(['/gospodarii']);
    }
  }
}