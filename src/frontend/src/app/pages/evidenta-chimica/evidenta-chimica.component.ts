import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

// Services
import { AuthService } from '../../services/auth.service';
import { ParcelaService } from '../../services/parcela.service';
import { CatalogPppService } from '../../services/catalog-ppp.service';
import { CatalogIngrasaminteService } from '../../services/catalog-ingrasaminte.service';
import { TratamentFitosanitarService } from '../../services/tratament-fitosanitar.service';
import { FertilizareService } from '../../services/fertilizare.service';
import { FilaParceleiService } from '../../services/fila-parcelei.service';

// Models
import { Parcela } from '../../models/parcela.model';
import { CatalogPpp } from '../../models/catalog-ppp.model';
import { CatalogIngrasaminte } from '../../models/catalog-ingrasaminte.model';
import { TratamentFitosanitar } from '../../models/tratament-fitosanitar.model';
import { Fertilizare } from '../../models/fertilizare.model';
import { FilaParcelei } from '../../models/fila-parcelei.model';

// Layout components
import { LayoutComponent } from '../../components/layout/layout.component';
import { PageHeaderComponent } from '../../components/page-header/page-header.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';
import { AppTranslatePipe } from '../../services/translate.pipe';

@Component({
  selector: 'app-evidenta-chimica',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, LayoutComponent, PageHeaderComponent, BreadcrumbsComponent, AppTranslatePipe],
  templateUrl: './evidenta-chimica.component.html',
  styleUrls: ['./evidenta-chimica.component.css']
})
export class EvidentaChimicaComponent implements OnInit, OnDestroy {
  activeTab: 'tratamente' | 'fertilizari' | 'cataloage' | 'fila' = 'tratamente';
  catalogSubTab: 'ppp' | 'ingrasaminte' = 'ppp';
  
  // User & context
  user: any;
  isAdmin = false;
  private destroy$ = new Subject<void>();

  // Breadcrumbs
  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'Tratamente & Fertilizări', link: '/evidenta-chimica' }
  ];

  // Data lists
  parcele: Parcela[] = [];
  pppProducts: CatalogPpp[] = [];
  fertilizerProducts: CatalogIngrasaminte[] = [];
  dropdownPppProducts: CatalogPpp[] = [];
  dropdownFertilizerProducts: CatalogIngrasaminte[] = [];
  tratamente: TratamentFitosanitar[] = [];
  fertilizari: Fertilizare[] = [];
  filaInterventii: FilaParcelei[] = [];

  // Fila parcelei filters
  selectedFilaParcelaId: number | null = null;
  selectedFilaAn: number = new Date().getFullYear();

  // Pagination states
  tratamentePage = 0;
  tratamenteTotal = 0;
  fertilizariPage = 0;
  fertilizariTotal = 0;
  pppPage = 0;
  pppTotal = 0;
  ingrasamintePage = 0;
  ingrasaminteTotal = 0;
  pageSize = 10;

  // Search queries
  pppSearchQuery = '';
  ingrasaminteSearchQuery = '';

  // Form Modals
  showPesticideModal = false;
  showFertilizareModal = false;
  showPppCatalogModal = false;
  showIngrasamantCatalogModal = false;

  // Form Models
  currentPesticide: TratamentFitosanitar = this.initPesticideForm();
  currentFertilizare: Fertilizare = this.initFertilizareForm();
  currentCatalogPpp: CatalogPpp = this.initCatalogPppForm();
  currentCatalogIngrasamant: CatalogIngrasaminte = this.initCatalogIngrasamantForm();

  // Validation Warnings
  showOverdoseWarning = false;
  showWinterWarning = false;
  winterWarningMessage = '';
  nitrateLimitErrorMessage = '';
  errorMessage = '';

  // Selected details for autocompletes
  selectedPppProduct: CatalogPpp | null = null;
  selectedIngrasamantProduct: CatalogIngrasaminte | null = null;

  constructor(
    private authService: AuthService,
    private parcelaService: ParcelaService,
    private catalogPppService: CatalogPppService,
    private catalogIngrasaminteService: CatalogIngrasaminteService,
    private tratamentFitosanitarService: TratamentFitosanitarService,
    private fertilizareService: FertilizareService,
    private filaParceleiService: FilaParceleiService
  ) {}

  ngOnInit(): void {
    this.authService.currentUser
      .pipe(takeUntil(this.destroy$))
      .subscribe((u: any) => {
        this.user = u;
        this.isAdmin = u?.role === 'ROLE_ADMIN' || u?.role === 'ROLE_SUPER_ADMIN';
      });

    this.loadParcele();
    this.loadTratamente();
    this.loadFertilizari();
    this.loadPppCatalog();
    this.loadIngrasaminteCatalog();
    this.loadDropdownCatalogs();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // --- INITIALIZERS ---
  initPesticideForm(): TratamentFitosanitar {
    return {
      dataEfectuarii: new Date().toISOString().substring(0, 16), // datetime-local format
      fenofaza: 'Aparitia primelor frunze',
      parcelaId: 0,
      agentDaunator: '',
      catalogPppId: 0,
      dozaUtilizata: 0,
      suprafataTratata: 0,
      responsabil: '',
      semnaturaElectronica: '',
      dataIncepereRecoltare: '',
      documentDareConsum: '',
      justificareSupradozaj: ''
    };
  }

  initFertilizareForm(): Fertilizare {
    return {
      dataAplicarii: new Date().toISOString().substring(0, 10), // date format
      parcelaId: 0,
      catalogIngrasaminteId: 0,
      cantitateBruta: 0,
      unitateMasura: 'kg/ha'
    };
  }

  initCatalogPppForm(): CatalogPpp {
    return {
      denumireComerciala: '',
      tip: 'Fungicid',
      daunatorVizat: '',
      dozaOmologata: 0,
      timpPauza: 0
    };
  }

  initCatalogIngrasamantForm(): CatalogIngrasaminte {
    return {
      denumire: '',
      tip: 'Chimic',
      procentAzot: 0,
      procentFosfor: 0,
      procentPotasiu: 0
    };
  }

  // --- DATA LOADING ---
  loadParcele(): void {
    this.parcelaService.getAllParcele(0, 100).subscribe(res => {
      this.parcele = res.content || [];
      if (this.parcele.length > 0) {
        this.selectedFilaParcelaId = this.parcele[0].id || null;
        this.loadFilaParcelei();
      }
    });
  }

  loadTratamente(): void {
    this.tratamentFitosanitarService.getTratamente(undefined, this.tratamentePage, this.pageSize)
      .subscribe(res => {
        this.tratamente = res.content || [];
        this.tratamenteTotal = res.totalElements || 0;
      });
  }

  loadFertilizari(): void {
    this.fertilizareService.getFertilizari(undefined, this.fertilizariPage, this.pageSize)
      .subscribe(res => {
        this.fertilizari = res.content || [];
        this.fertilizariTotal = res.totalElements || 0;
      });
  }

  loadPppCatalog(): void {
    this.catalogPppService.getCatalog(this.pppSearchQuery, this.pppPage, this.pageSize)
      .subscribe(res => {
        this.pppProducts = res.content || [];
        this.pppTotal = res.totalElements || 0;
      });
  }

  loadIngrasaminteCatalog(): void {
    this.catalogIngrasaminteService.getCatalog(this.ingrasaminteSearchQuery, this.ingrasamintePage, this.pageSize)
      .subscribe(res => {
        this.fertilizerProducts = res.content || [];
        this.ingrasaminteTotal = res.totalElements || 0;
      });
  }

  loadDropdownCatalogs(): void {
    this.catalogPppService.getCatalog('', 0, 1000)
      .subscribe(res => {
        this.dropdownPppProducts = res.content || [];
        console.log('[loadDropdownCatalogs] Loaded PPPs:', this.dropdownPppProducts);
      });
    this.catalogIngrasaminteService.getCatalog('', 0, 1000)
      .subscribe(res => {
        this.dropdownFertilizerProducts = res.content || [];
        console.log('[loadDropdownCatalogs] Loaded Fertilizers:', this.dropdownFertilizerProducts);
      });
  }

  loadFilaParcelei(): void {
    if (!this.selectedFilaParcelaId) return;
    this.filaParceleiService.getFilaParcelei(this.selectedFilaParcelaId, this.selectedFilaAn)
      .subscribe(res => {
        this.filaInterventii = res || [];
      });
  }

  // --- ACTIONS ---
  onTabChange(tab: 'tratamente' | 'fertilizari' | 'cataloage' | 'fila'): void {
    this.activeTab = tab;
    this.errorMessage = '';
  }

  onCatalogSubTabChange(subTab: 'ppp' | 'ingrasaminte'): void {
    this.catalogSubTab = subTab;
  }

  searchPpp(): void {
    this.pppPage = 0;
    this.loadPppCatalog();
  }

  searchIngrasaminte(): void {
    this.ingrasamintePage = 0;
    this.loadIngrasaminteCatalog();
  }

  // --- TRATAMENTE FITOSANITARE MODAL / FORMS ---
  openAddPesticideModal(): void {
    this.currentPesticide = this.initPesticideForm();
    this.selectedPppProduct = null;
    this.showOverdoseWarning = false;
    this.errorMessage = '';
    this.showPesticideModal = true;
  }

  onPppSelected(): void {
    const id = Number(this.currentPesticide.catalogPppId);
    this.selectedPppProduct = this.dropdownPppProducts.find(p => p.id === id) || null;
    console.log('[onPppSelected] selectedProduct:', this.selectedPppProduct);
    this.checkOverdosage();
  }

  checkOverdosage(newVal?: number): void {
    const val = newVal !== undefined ? newVal : this.currentPesticide.dozaUtilizata;
    console.log('[checkOverdosage] dozaUtilizata:', val, 'product:', this.selectedPppProduct);
    if (this.selectedPppProduct && val !== undefined && val !== null) {
      const utilized = Number(val);
      const allowed = Number(this.selectedPppProduct.dozaOmologata);
      this.showOverdoseWarning = utilized > allowed;
      console.log('[checkOverdosage] utilized:', utilized, 'allowed:', allowed, 'warning:', this.showOverdoseWarning);
    } else {
      this.showOverdoseWarning = false;
    }
  }

  get totalFitosanitarQty(): number {
    return (this.currentPesticide.suprafataTratata || 0) * (this.currentPesticide.dozaUtilizata || 0);
  }

  savePesticide(): void {
    this.errorMessage = '';
    if (this.showOverdoseWarning && (!this.currentPesticide.justificareSupradozaj || !this.currentPesticide.justificareSupradozaj.trim())) {
      this.errorMessage = 'Doza utilizată depășește doza omologată. Vă rugăm să introduceți o justificare pentru a înregistra supradozajul.';
      return;
    }

    if (this.currentPesticide.semnaturaElectronica) {
      this.currentPesticide.semnaturaElectronica = `Semnat electronic de ${this.currentPesticide.responsabil} la data de ${new Date().toLocaleDateString()}`;
    }

    const action = this.currentPesticide.id
      ? this.tratamentFitosanitarService.update(this.currentPesticide.id, this.currentPesticide)
      : this.tratamentFitosanitarService.create(this.currentPesticide);

    action.subscribe({
      next: () => {
        this.showPesticideModal = false;
        this.loadTratamente();
        this.loadFilaParcelei();
      },
      error: err => {
        this.errorMessage = err.error || 'A apărut o eroare la salvarea tratamentului fitosanitar.';
      }
    });
  }

  deleteTratament(id: number): void {
    if (confirm('Sunteți sigur că doriți să ștergeți acest tratament fitosanitar?')) {
      this.tratamentFitosanitarService.delete(id).subscribe(() => {
        this.loadTratamente();
        this.loadFilaParcelei();
      });
    }
  }

  downloadANFRegistru(): void {
    this.tratamentFitosanitarService.downloadPdf().subscribe(blob => {
      const link = document.createElement('a');
      link.href = window.URL.createObjectURL(blob);
      link.download = `registru_fitosanitar_anf_${new Date().getFullYear()}.pdf`;
      link.click();
    });
  }

  // --- FERTILIZARI MODAL / FORMS ---
  openAddFertilizareModal(): void {
    this.currentFertilizare = this.initFertilizareForm();
    this.selectedIngrasamantProduct = null;
    this.showWinterWarning = false;
    this.nitrateLimitErrorMessage = '';
    this.errorMessage = '';
    this.showFertilizareModal = true;
  }

  onIngrasamantSelected(): void {
    const id = Number(this.currentFertilizare.catalogIngrasaminteId);
    this.selectedIngrasamantProduct = this.dropdownFertilizerProducts.find(i => i.id === id) || null;
  }

  get calculatedActiveN(): number {
    if (!this.selectedIngrasamantProduct || !this.currentFertilizare.cantitateBruta) return 0;
    const factor = this.currentFertilizare.unitateMasura === 'tone/ha' ? 1000 : 1;
    return this.currentFertilizare.cantitateBruta * factor * (this.selectedIngrasamantProduct.procentAzot / 100);
  }

  get calculatedActiveP(): number {
    if (!this.selectedIngrasamantProduct || !this.currentFertilizare.cantitateBruta) return 0;
    const factor = this.currentFertilizare.unitateMasura === 'tone/ha' ? 1000 : 1;
    return this.currentFertilizare.cantitateBruta * factor * (this.selectedIngrasamantProduct.procentFosfor / 100);
  }

  get calculatedActiveK(): number {
    if (!this.selectedIngrasamantProduct || !this.currentFertilizare.cantitateBruta) return 0;
    const factor = this.currentFertilizare.unitateMasura === 'tone/ha' ? 1000 : 1;
    return this.currentFertilizare.cantitateBruta * factor * (this.selectedIngrasamantProduct.procentPotasiu / 100);
  }

  saveFertilizare(force: boolean = false): void {
    this.errorMessage = '';
    this.nitrateLimitErrorMessage = '';

    this.fertilizareService.create(this.currentFertilizare, force).subscribe({
      next: () => {
        this.showFertilizareModal = false;
        this.showWinterWarning = false;
        this.loadFertilizari();
        this.loadFilaParcelei();
      },
      error: err => {
        const errorText = err.error || '';
        if (errorText.includes('Avertisment Perioadă de Interdicție')) {
          this.winterWarningMessage = errorText;
          this.showWinterWarning = true;
        } else if (errorText.includes('Limita Directivei Nitraților Depășită')) {
          this.nitrateLimitErrorMessage = errorText;
        } else {
          this.errorMessage = errorText || 'A apărut o eroare la salvarea fertilizării.';
        }
      }
    });
  }

  deleteFertilizare(id: number): void {
    if (confirm('Sunteți sigur că doriți să ștergeți această înregistrare de fertilizare?')) {
      this.fertilizareService.delete(id).subscribe(() => {
        this.loadFertilizari();
        this.loadFilaParcelei();
      });
    }
  }

  // --- CATALOG MANAGEMENT (PPP) ---
  openAddPppCatalogModal(): void {
    this.currentCatalogPpp = this.initCatalogPppForm();
    this.errorMessage = '';
    this.showPppCatalogModal = true;
  }

  savePppCatalogItem(): void {
    const action = this.currentCatalogPpp.id
      ? this.catalogPppService.update(this.currentCatalogPpp.id, this.currentCatalogPpp)
      : this.catalogPppService.create(this.currentCatalogPpp);

    action.subscribe({
      next: () => {
        this.showPppCatalogModal = false;
        this.loadPppCatalog();
        this.loadDropdownCatalogs();
      },
      error: err => {
        this.errorMessage = err.error || 'A apărut o eroare la salvarea catalogului PPP.';
      }
    });
  }

  deletePppCatalogItem(id: number): void {
    if (confirm('Sunteți sigur că doriți să ștergeți acest produs din catalog?')) {
      this.catalogPppService.delete(id).subscribe(() => {
        this.loadPppCatalog();
        this.loadDropdownCatalogs();
      });
    }
  }

  // --- CATALOG MANAGEMENT (FERTILIZANTE) ---
  openAddIngrasamantCatalogModal(): void {
    this.currentCatalogIngrasamant = this.initCatalogIngrasamantForm();
    this.errorMessage = '';
    this.showIngrasamantCatalogModal = true;
  }

  saveIngrasamantCatalogItem(): void {
    const action = this.currentCatalogIngrasamant.id
      ? this.catalogIngrasaminteService.update(this.currentCatalogIngrasamant.id, this.currentCatalogIngrasamant)
      : this.catalogIngrasaminteService.create(this.currentCatalogIngrasamant);

    action.subscribe({
      next: () => {
        this.showIngrasamantCatalogModal = false;
        this.loadIngrasaminteCatalog();
        this.loadDropdownCatalogs();
      },
      error: err => {
        this.errorMessage = err.error || 'A apărut o eroare la salvarea catalogului de îngrășăminte.';
      }
    });
  }

  deleteIngrasamantCatalogItem(id: number): void {
    if (confirm('Sunteți sigur că doriți să ștergeți acest îngrășământ din catalog?')) {
      this.catalogIngrasaminteService.delete(id).subscribe(() => {
        this.loadIngrasaminteCatalog();
        this.loadDropdownCatalogs();
      });
    }
  }
}
