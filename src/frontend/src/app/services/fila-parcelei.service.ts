import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FilaParcelei } from '../models/fila-parcelei.model';

@Injectable({
  providedIn: 'root'
})
export class FilaParceleiService {

  constructor(private http: HttpClient) { }

  getFilaParcelei(parcelaId: number, anAgricol?: number): Observable<FilaParcelei[]> {
    let params = new HttpParams();
    if (anAgricol) {
      params = params.set('anAgricol', anAgricol.toString());
    }
    return this.http.get<FilaParcelei[]>(`/api/parcele/${parcelaId}/fila`, { params });
  }
}
