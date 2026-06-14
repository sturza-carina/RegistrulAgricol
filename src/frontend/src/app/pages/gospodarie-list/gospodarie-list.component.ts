import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { GospodarieService } from '../../services/gospodarie.service';
import { Gospodarie } from '../../models/gospodarie.model';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { AuthService } from '../../services/auth.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-gospodarie-list',
  standalone: true,
  imports: [CommonModule, RouterLink, SidebarComponent, FormsModule],
  templateUrl: './gospodarie-list.component.html',
  styleUrls: ['./gospodarie-list.component.css']
})
export class GospodarieListComponent implements OnInit {
  gospodarii: Gospodarie[] = [];
  filteredGospodarii: Gospodarie[] = [];
  user: any = null;
  searchQuery = '';

  selectedGospodarie: Gospodarie | null = null;

  constructor(
    private gospodarieService: GospodarieService,
    private authService: AuthService,
    private router: Router
  ) {}

  filterTipGospodarie: string = '';

  ngOnInit() {
    this.authService.currentUser.subscribe(user => {
      if (!user) {
        this.router.navigate(['/login']);
      } else {
        this.user = user;
        this.loadGospodarii();
      }
    });
  }

  loadGospodarii() {
    this.gospodarieService.getAllGospodarii().subscribe({
      next: (data) => {
        this.gospodarii = data;
        this.applyFilters();
      },
      error: (err) => console.error(err)
    });
  }

  applyFilters() {
    const q = this.searchQuery.toLowerCase();
    this.filteredGospodarii = this.gospodarii.filter(g => {
      const matchSearch = g.codGospodarie.toLowerCase().includes(q) ||
        (g.adresa.localitate && g.adresa.localitate.toLowerCase().includes(q));
      
      const matchType = this.filterTipGospodarie ? g.tipGospodarie === this.filterTipGospodarie : true;

      return matchSearch && matchType;
    });
    if (this.selectedGospodarie && !this.filteredGospodarii.find(g => g.id === this.selectedGospodarie?.id)) {
      this.selectedGospodarie = null;
    }
  }

  goToCreate() {
    this.router.navigate(['/gospodarii/new']);
  }

  selectGospodarie(g: Gospodarie) {
    if (this.selectedGospodarie?.id === g.id) {
      this.selectedGospodarie = null; // deselect
    } else {
      this.selectedGospodarie = g;
    }
  }

  editSelected() {
    if (this.selectedGospodarie) {
      this.router.navigate(['/gospodarii/edit', this.selectedGospodarie.id]);
    }
  }

  deleteSelected() {
    if (this.selectedGospodarie?.id && confirm('Sunteți sigur că doriți să ștergeți gospodăria selectată?')) {
      this.gospodarieService.deleteGospodarie(this.selectedGospodarie.id).subscribe(() => {
        this.selectedGospodarie = null;
        this.loadGospodarii();
      });
    }
  }

  deleteRow(id: number | undefined, event: Event) {
    event.stopPropagation();
    if (!id) return;
    if (confirm('Ești sigur că vrei să ștergi această gospodărie?')) {
      this.gospodarieService.deleteGospodarie(id).subscribe(() => {
        if (this.selectedGospodarie?.id === id) {
          this.selectedGospodarie = null;
        }
        this.loadGospodarii();
      });
    }
  }

  viewDetailsSelected() {
    if (this.selectedGospodarie) {
      this.router.navigate(['/gospodarii', this.selectedGospodarie.id]);
    }
  }

  viewDetails(id?: number) {
    if (id) this.router.navigate(['/gospodarii', id]);
  }

  editGospodarie(id?: number) {
    if (id) this.router.navigate(['/gospodarii/edit', id]);
  }

  manageParcele(id?: number) {
    if (id) this.router.navigate(['/harta'], { queryParams: { gospodarieId: id } });
  }

  addParcelaSelected() {
    if (this.selectedGospodarie) {
      this.router.navigate(['/harta'], { queryParams: { gospodarieId: this.selectedGospodarie.id } });
    }
  }

  addPersoanaSelected() {
    if (this.selectedGospodarie) {
      this.router.navigate(['/persoane/new'], { queryParams: { gospodarieId: this.selectedGospodarie.id } });
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
