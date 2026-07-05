import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {TipDocument} from '../models/document.model';
import {SpecieRef} from '../models/specie-ref.model';

@Injectable({ providedIn: 'root' })
export class LookupService {
  private base = '/api/lookup';

  constructor(private http: HttpClient) {}

  getTipuriSol(): Observable<string[]> {
    return this.http.get<string[]>(`${this.base}/tip-sol`);
  }

  getCategoriiFolosinta(): Observable<string[]> {
    return this.http.get<string[]>(`${this.base}/categorie-folosinta`);
  }

  getTipuriSursaApa(): Observable<string[]> {
    return this.http.get<string[]>(`${this.base}/tip-sursa-apa`);
  }

  getTipuriDocument(): Observable<TipDocument[]> {
    return this.http.get<TipDocument[]>(`${this.base}/tip-document`);
  }

  getSpeciiPomi(): Observable<SpecieRef[]> {
    return this.http.get<SpecieRef[]>(`${this.base}/specii-pomi`);
  }
}
