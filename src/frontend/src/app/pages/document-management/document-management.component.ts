import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GenericTableComponent, TableColumn, TableAction } from '../../components/generic-table/generic-table.component';
import { DocumentService } from '../../services/document.service';
import { LookupService } from '../../services/lookup.service';
import { TipDocument } from '../../models/document.model';

@Component({
  selector: 'app-document-management',
  standalone: true,
  imports: [CommonModule, FormsModule, GenericTableComponent],
  templateUrl: './document-management.component.html'
})
export class DocumentManagementComponent implements OnInit {
  @Input() gospodarieId!: number;

  documente: any[] = [];
  tipuriDocument: any[] = [];
  isLoading = false;
  isUploading = false;
  showUploadForm = false;

  selectedFile: File | null = null;
  uploadData: any = {
    tipDocumentId: null,
    dataEmitere: null,
    dataExpirare: null,
    observatii: ''
  };

  columns: TableColumn[] = [
    { field: 'numeFisier', header: 'Nume Fișier' },
    { field: 'tipDocumentDenumire', header: 'Tip Document' },
    { field: 'dataEmitere', header: 'Data Emitere', type: 'date' },
    { field: 'dataExpirare', header: 'Data Expirare', type: 'date' }
  ];

  actions: TableAction[] = [
    { icon: 'view', tooltip: 'Descarcă', action: (row) => this.downloadDocument(row) },
    { icon: 'delete', tooltip: 'Șterge', action: (row) => this.deleteDocument(row) }
  ];

  constructor(
    private documentService: DocumentService,
    private lookupService: LookupService
  ) {}

  ngOnInit() {
    this.loadTipuriDocument();
    this.lookupService.getTipuriDocument().subscribe();
  }

  loadTipuriDocument() {
    this.lookupService.getTipuriDocument().subscribe(data => {
      this.tipuriDocument = data;
      this.loadDocumente();
    });
  }

  loadDocumente() {
    this.isLoading = true;
    this.documentService.getDocumentsByGospodarie(this.gospodarieId, 0, 100).subscribe({
      next: (res) => {
        this.documente = res.content.map(doc => ({
          ...doc,
          tipDocumentDenumire: this.tipuriDocument.find(t => t.id === doc.tipDocumentId)?.denumire || '-'
        }));
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  openUploadForm() {
    this.uploadData = { tipDocumentId: null, dataEmitere: null, dataExpirare: null, observatii: '' };
    this.selectedFile = null;
    this.showUploadForm = true;
  }

  cancelUpload() {
    this.showUploadForm = false;
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
  }

  submitUpload() {
    if (!this.selectedFile || !this.uploadData.tipDocumentId) {
      alert('Selectați tipul documentului și un fișier.');
      return;
    }

    this.isUploading = true;
    const payload = { ...this.uploadData, gospodarieId: this.gospodarieId };

    this.documentService.uploadDocument(payload, this.selectedFile).subscribe({
      next: () => {
        this.isUploading = false;
        this.showUploadForm = false;
        this.loadDocumente();
      },
      error: () => {
        this.isUploading = false;
        alert('Eroare la încărcarea documentului.');
      }
    });
  }

  downloadDocument(row: any) {
    this.documentService.downloadDocument(row.id, this.gospodarieId).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = row.numeFisier;
      a.click();
      window.URL.revokeObjectURL(url);
    });
  }

  deleteDocument(row: any) {
    if (!confirm(`Ștergeți documentul "${row.numeFisier}"?`)) return;
    this.documentService.deleteDocument(row.id, this.gospodarieId).subscribe({
      next: () => this.loadDocumente(),
      error: () => alert('Eroare la ștergerea documentului.')
    });
  }
}
