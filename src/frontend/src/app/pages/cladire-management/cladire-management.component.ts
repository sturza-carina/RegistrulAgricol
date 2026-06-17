import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Cladire } from '../../models/cladire.model';
import { CladireService } from '../../services/cladire.service';

@Component({
  selector: 'app-cladire-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cladire-management.component.html',
  styleUrl: './cladire-management.component.css'
})
export class CladireManagementComponent implements OnInit {
  @Input() gospodarieId!: number;
  cladiri: Cladire[] = [];
  
  showModal = false;
  isEditing = false;
  currentCladire: Partial<Cladire> = {};

  currentPage = 1;
  itemsPerPage = 5;

  get paginatedItems() {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    return this.cladiri.slice(startIndex, startIndex + this.itemsPerPage);
  }

  get totalPages() {
    return Math.ceil(this.cladiri.length / this.itemsPerPage) || 1;
  }

  nextPage() {
    if (this.currentPage < this.totalPages) this.currentPage++;
  }

  prevPage() {
    if (this.currentPage > 1) this.currentPage--;
  }

  constructor(private cladireService: CladireService) {}

  ngOnInit(): void {
    if (this.gospodarieId) {
      this.loadCladiri();
    }
  }

  loadCladiri() {
    this.cladireService.getCladiri(this.gospodarieId).subscribe({
      next: (data) => {
        this.cladiri = data;
        this.currentPage = 1;
      },
      error: (err) => console.error('Failed to load cladiri', err)
    });
  }

  openAddModal() {
    this.isEditing = false;
    this.currentCladire = {
      gospodarieId: this.gospodarieId
    };
    this.showModal = true;
  }

  openEditModal(cladire: Cladire) {
    this.isEditing = true;
    this.currentCladire = { ...cladire };
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.currentCladire = {};
  }

  saveCladire() {
    if (!this.currentCladire.destinatie || !this.currentCladire.suprafataConstruita) {
      alert('Vă rugăm să completați destinația și suprafața construită.');
      return;
    }

    if (this.isEditing && this.currentCladire.id) {
      this.cladireService.updateCladire(this.gospodarieId, this.currentCladire.id, this.currentCladire as Cladire)
        .subscribe({
          next: () => {
            this.loadCladiri();
            this.closeModal();
          },
          error: (err) => console.error('Failed to update cladire', err)
        });
    } else {
      this.cladireService.createCladire(this.gospodarieId, this.currentCladire as Cladire)
        .subscribe({
          next: () => {
            this.loadCladiri();
            this.closeModal();
          },
          error: (err) => console.error('Failed to create cladire', err)
        });
    }
  }

  deleteCladire(id: number | undefined) {
    if (!id) return;
    if (confirm('Sunteți sigur că doriți să ștergeți această clădire?')) {
      this.cladireService.deleteCladire(this.gospodarieId, id).subscribe({
        next: () => this.loadCladiri(),
        error: (err) => console.error('Failed to delete cladire', err)
      });
    }
  }
}
