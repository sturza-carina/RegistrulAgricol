import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LayoutComponent } from '../../components/layout/layout.component';
import { AppTranslatePipe } from '../../services/translate.pipe';
import { RaportStatisticService } from '../../services/raport-statistic.service';
import { UatContextService } from '../../services/uat-context.service';
import { RecoltareService } from '../../services/recoltare.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-statistici',
  standalone: true,
  imports: [CommonModule, FormsModule, LayoutComponent, AppTranslatePipe],
  templateUrl: './statistici.component.html',
  styleUrl: './statistici.component.css'
})
export class StatisticiComponent implements OnInit, OnDestroy {
  private uatSubscription: Subscription | null = null;
  activeUatCode: string | undefined = undefined;

  selectedYear: number = 2026;
  availableYears: number[] = [2024, 2025, 2026, 2027, 2028];
  
  activeTab: 'VEGETAL' | 'ZOOTEHNIC' | 'UTILAJE' = 'VEGETAL';
  loading: boolean = false;
  errorMessage: string | null = null;

  // Statistical data retrieved from backend
  data: any = {
    culturi: [],
    categoriiFolosinta: [],
    animaleIndividuale: [],
    efectiveGrup: [],
    utilaje: []
  };

  // Stat summaries for widgets
  totals: any = {
    suprafataVegetalHa: 0,
    culturiCount: 0,
    categoriiCount: 0,
    capeteIndividuale: 0,
    capeteGrup: 0,
    utilajeCount: 0
  };

  recoltariCentralizator: any[] = [];

  constructor(
    private statisticiService: RaportStatisticService,
    private uatContextService: UatContextService,
    private recoltareService: RecoltareService
  ) {}

  ngOnInit(): void {
    this.uatSubscription = this.uatContextService.activeUat$.subscribe(uat => {
      this.activeUatCode = uat?.codSiruta;
      this.fetchData();
    });
  }

  ngOnDestroy(): void {
    if (this.uatSubscription) {
      this.uatSubscription.unsubscribe();
    }
  }

  onYearChange(): void {
    this.fetchData();
  }

  setTab(tab: 'VEGETAL' | 'ZOOTEHNIC' | 'UTILAJE'): void {
    this.activeTab = tab;
  }

  fetchData(): void {
    this.loading = true;
    this.errorMessage = null;
    this.statisticiService.getComplet(this.selectedYear, this.activeUatCode).subscribe({
      next: (res) => {
        this.data = res || {
          culturi: [],
          categoriiFolosinta: [],
          animaleIndividuale: [],
          efectiveGrup: [],
          utilaje: []
        };
        this.calculateSummaries();
        this.fetchCentralizatorRecoltari();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching statistics:', err);
        this.errorMessage = 'A apărut o eroare la încărcarea datelor statistice. Vă rugăm să reîncercați.';
        this.loading = false;
      }
    });
  }

  fetchCentralizatorRecoltari(): void {
    this.recoltareService.getCentralizator(this.selectedYear).subscribe({
      next: (res) => {
        this.recoltariCentralizator = res || [];
      },
      error: (err) => {
        console.error('Error fetching yield statistics:', err);
      }
    });
  }

  calculateSummaries(): void {
    // 1. Vegetal summaries
    const totalSuprafataCulturi = (this.data.culturi || []).reduce((acc: number, c: any) => acc + (c.suprafataTotalaHa || 0), 0);
    const totalSuprafataCategorii = (this.data.categoriiFolosinta || []).reduce((acc: number, c: any) => acc + (c.suprafataTotalaHa || 0), 0);
    this.totals.suprafataVegetalHa = totalSuprafataCulturi + totalSuprafataCategorii;
    this.totals.culturiCount = (this.data.culturi || []).length;
    this.totals.categoriiCount = (this.data.categoriiFolosinta || []).length;

    // 2. Zootehnic summaries
    this.totals.capeteIndividuale = (this.data.animaleIndividuale || []).reduce((acc: number, a: any) => acc + (a.totalCapete || 0), 0);
    this.totals.capeteGrup = (this.data.efectiveGrup || []).reduce((acc: number, g: any) => acc + (g.totalCapete || 0), 0);

    // 3. Utilaje summaries
    this.totals.utilajeCount = (this.data.utilaje || []).reduce((acc: number, u: any) => acc + (u.totalUnitati || 0), 0);
  }

  exportVegetal(): void {
    this.loading = true;
    this.statisticiService.exportVegetal(this.selectedYear, this.activeUatCode).subscribe({
      next: (blob) => {
        this.downloadBlob(blob, `centralizator_vegetal_${this.selectedYear}.xlsx`);
        this.loading = false;
      },
      error: (err) => {
        console.error('Error exporting vegetal statistics:', err);
        alert('Eroare la descărcarea centralizatorului vegetal.');
        this.loading = false;
      }
    });
  }

  exportZootehnic(): void {
    this.loading = true;
    this.statisticiService.exportZootehnic(this.activeUatCode).subscribe({
      next: (blob) => {
        this.downloadBlob(blob, 'centralizator_zootehnic.xlsx');
        this.loading = false;
      },
      error: (err) => {
        console.error('Error exporting zootehnic statistics:', err);
        alert('Eroare la descărcarea centralizatorului zootehnic.');
        this.loading = false;
      }
    });
  }

  exportUtilaje(): void {
    this.loading = true;
    this.statisticiService.exportUtilaje(this.activeUatCode).subscribe({
      next: (blob) => {
        this.downloadBlob(blob, 'centralizator_utilaje.xlsx');
        this.loading = false;
      },
      error: (err) => {
        console.error('Error exporting machinery statistics:', err);
        alert('Eroare la descărcarea centralizatorului de utilaje.');
        this.loading = false;
      }
    });
  }

  private downloadBlob(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    window.URL.revokeObjectURL(url);
  }
}
