import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PaginatedResponse } from '../models/paginated-response.model';

export interface DocumentEntity {
  id?: number;
  gospodarieId?: number;
  tipDocumentId: number;
  numeFisier: string;
  mimeType?: string;
  dimensiuneKb?: number;
  dataEmitere?: string | null;
  dataExpirare?: string | null;
  observatii?: string | null;
  esteActiv?: boolean;
}

export interface DocumentUploadRequest {
  gospodarieId: number;
  tipDocumentId: number;
  dataEmitere?: string | null;
  dataExpirare?: string | null;
  observatii?: string | null;
}

export interface TipDocument {
  id: number;
  cod: string;
  denumire: string;
  descriere?: string;
}

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private apiUrl = '/api/documente';
  private tipuriUrl = '/api/tipuri-document';

  constructor(private http: HttpClient) {}

  getAllDocuments(uatCode?: string, page: number = 0, size: number = 20): Observable<PaginatedResponse<DocumentEntity>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (uatCode) {
      params = params.set('uatCode', uatCode);
    }
    return this.http.get<PaginatedResponse<DocumentEntity>>(this.apiUrl, { params });
  }

  getDocumentsByGospodarie(gospodarieId: number, page: number = 0, size: number = 20): Observable<PaginatedResponse<DocumentEntity>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PaginatedResponse<DocumentEntity>>(`${this.apiUrl}/gospodarie/${gospodarieId}`, { params });
  }

  uploadDocument(data: DocumentUploadRequest, file: File): Observable<DocumentEntity> {
    const formData = new FormData();
    formData.append('document', new Blob([JSON.stringify(data)], { type: 'application/json' }));
    formData.append('file', file);
    return this.http.post<DocumentEntity>(this.apiUrl, formData);
  }

  downloadDocument(id: number, gospodarieId: number): Observable<Blob> {
    const params = new HttpParams().set('gospodarieId', gospodarieId.toString());
    return this.http.get(`${this.apiUrl}/${id}/download`, { params, responseType: 'blob' });
  }

  deleteDocument(id: number, gospodarieId: number): Observable<void> {
    const params = new HttpParams().set('gospodarieId', gospodarieId.toString());
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { params });
  }
}
