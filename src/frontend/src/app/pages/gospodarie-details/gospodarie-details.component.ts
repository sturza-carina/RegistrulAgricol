import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { GospodarieService } from '../../services/gospodarie.service';
import { Gospodarie } from '../../models/gospodarie.model';
import { PersoanaService } from '../../services/persoana.service';
import { TerenService } from '../../services/teren.service';
import { Teren } from '../../models/teren.model';
import { ParcelaService } from '../../services/parcela.service';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';

@Component({
  selector: 'app-gospodarie-details',
  standalone: true,
  imports: [CommonModule, SidebarComponent, RouterModule],
  templateUrl: './gospodarie-details.component.html',
  styleUrls: ['./gospodarie-details.component.css']
})
export class GospodarieDetailsComponent implements OnInit {
  gospodarieId!: number;
  gospodarie: Gospodarie | null = null;
  persoane: any[] = [];
  terenuri: Teren[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private gospodarieService: GospodarieService,
    private persoanaService: PersoanaService,
    private terenService: TerenService,
    private parcelaService: ParcelaService
  ) {}

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.gospodarieId = +id;
        this.loadDetails();
      }
    });
  }

  loadDetails() {
    this.gospodarieService.getGospodarieById(this.gospodarieId).subscribe(data => {
      this.gospodarie = data;
    });

    this.terenService.getTerenByGospodarieId(this.gospodarieId).subscribe({
      next: (terenuri) => {
        this.terenuri = terenuri || [];
      },
      error: () => {
        this.terenuri = [];
      }
    });

    this.persoanaService.getPersonsByGospodarieId(this.gospodarieId).subscribe(data => this.persoane = data as any[]);
  }

  editGospodarie() {
    this.router.navigate(['/gospodarii/edit', this.gospodarieId]);
  }

  addPerson() {
    this.router.navigate(['/persoane/new'], { queryParams: { gospodarieId: this.gospodarieId } });
  }

  editPerson(id: number | undefined) {
    if (id) this.router.navigate(['/persoane/edit', id]);
  }

  viewMap() {
    this.router.navigate(['/harta'], { queryParams: { gospodarieId: this.gospodarieId } });
  }

  addTeren() {
    this.router.navigate(['/terenuri/new'], { queryParams: { gospodarieId: this.gospodarieId } });
  }

  viewTeren(teren: Teren) {
    this.router.navigate(['/harta'], { queryParams: { gospodarieId: this.gospodarieId, terenId: teren.id } });
  }

  editTeren(teren: Teren) {
    this.router.navigate(['/terenuri', teren.id, 'parcele'], { queryParams: { gospodarieId: this.gospodarieId } });
  }

  deleteTeren(teren: Teren) {
    if (!teren.id) return;
    if (!confirm(`Ștergeți terenul "${teren.denumire}"? Această acțiune este ireversibilă și va șterge și parcelele asociate.`)) return;
    this.terenService.deleteTeren(teren.id).subscribe({
      next: () => {
        this.terenuri = this.terenuri.filter(t => t.id !== teren.id);
      },
      error: () => alert('Eroare la ștergere teren.')
    });
  }
}
