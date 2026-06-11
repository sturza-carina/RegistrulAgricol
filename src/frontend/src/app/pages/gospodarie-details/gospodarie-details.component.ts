import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { GospodarieService } from '../../services/gospodarie.service';
import { Gospodarie } from '../../models/gospodarie.model';
import { PersoanaService } from '../../services/persoana.service';
import { Persoana, PersoanaFizica, PersoanaJuridica } from '../../models/persoana.model';
import { TerenService } from '../../services/teren.service';
import { Teren } from '../../models/teren.model';
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
  teren: Teren | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private gospodarieService: GospodarieService,
    private persoanaService: PersoanaService,
    private terenService: TerenService
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
    this.gospodarieService.getGospodarieById(this.gospodarieId).subscribe(data => this.gospodarie = data);
    this.persoanaService.getPersonsByGospodarieId(this.gospodarieId).subscribe(data => this.persoane = data as any[]);
    this.terenService.getTerenByGospodarieId(this.gospodarieId).subscribe(data => this.teren = data);
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

  viewTeren(id: number | undefined) {
    if (id) this.router.navigate(['/harta'], { queryParams: { gospodarieId: this.gospodarieId, terenId: id } });
  }
}
