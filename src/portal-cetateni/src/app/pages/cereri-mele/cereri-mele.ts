import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { WebsocketService } from '../../services/websocket.service';
import { ChangeDetectorRef } from '@angular/core';

export interface Cerere {
  id: number;
  codCerere: string;
  status: string;
  nume: string;
  createdAt: string;
  numarCadastral?: string;
  numarCarteFunciara?: string;
}

@Component({
  selector: 'app-cereri-mele',
  imports: [CommonModule, RouterModule],
  templateUrl: './cereri-mele.html',
  styleUrl: './cereri-mele.css'
})
export class CereriMele implements OnInit {
  cereri: Cerere[] = [];
  filteredCereri: Cerere[] = [];
  loading = true;
  error = '';
  selectedFilter: string = 'ALL'; // ALL, PENDING, ACCEPTED, DECLINED
  selectedCerereId: number | null = null;
  currentPage = 1;
  itemsPerPage = 6;

  constructor(private http: HttpClient, private wsService: WebsocketService, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.fetchCereri();
    
    this.wsService.cerereUpdates.subscribe(update => {
      if (update) {
        const index = this.cereri.findIndex(c => c.codCerere === update.codCerere);
        if (index !== -1) {
          this.cereri[index].status = update.status;
          this.applyFilter();
          this.cdr.detectChanges();
        } else {
          // New cerere update that wasn't in our list yet, fetch again
          this.fetchCereri();
        }
      }
    });
  }

  fetchCereri() {
    this.loading = true;
    this.cdr.detectChanges();
    this.http.get<Cerere[]>('/api/public/cereri/my-cereri').subscribe({
      next: (data) => {
        try {
          const processed = data.map(c => {
            if (c.createdAt && Array.isArray(c.createdAt)) {
              const arr = c.createdAt as number[];
              c.createdAt = new Date(arr[0], arr[1] - 1, arr[2], arr[3] || 0, arr[4] || 0, arr[5] || 0).toISOString();
            }
            return c;
          });
          this.cereri = processed.sort((a, b) => {
            const timeA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
            const timeB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
            return timeB - timeA;
          });
          this.applyFilter();
        } catch (e) {
          console.error("Eroare la procesarea cererilor", e);
        }
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.error = 'Nu am putut prelua cererile.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  applyFilter() {
    if (this.selectedFilter === 'ALL') {
      this.filteredCereri = [...this.cereri];
    } else {
      this.filteredCereri = this.cereri.filter(c => c.status === this.selectedFilter);
    }
    this.currentPage = 1; // reset page on filter change
  }

  setFilter(filter: string) {
    this.selectedFilter = filter;
    this.applyFilter();
  }

  toggleDetails(id: number) {
    if (this.selectedCerereId === id) {
      this.selectedCerereId = null;
    } else {
      this.selectedCerereId = id;
    }
  }

  get paginatedCereri(): Cerere[] {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    return this.filteredCereri.slice(start, start + this.itemsPerPage);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredCereri.length / this.itemsPerPage) || 1;
  }

  setPage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.selectedCerereId = null;
    }
  }

  getStatusClass(status: string): string {
    switch(status) {
      case 'PENDING': return 'status-pending';
      case 'ACCEPTED': return 'status-approved';
      case 'DECLINED': return 'status-rejected';
      default: return '';
    }
  }

  getStatusLabel(status: string): string {
    switch(status) {
      case 'PENDING': return 'În așteptare';
      case 'ACCEPTED': return 'Acceptată';
      case 'DECLINED': return 'Refuzată';
      default: return status;
    }
  }

  downloadPdf(cerere: Cerere) {
    this.http.get(`/api/public/cereri/${cerere.id}/pdf`, { responseType: 'blob' }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        window.open(url, '_blank');
      },
      error: (err) => {
        console.error('Failed to download PDF', err);
        alert('Eroare la descărcarea documentului.');
      }
    });
  }
}

