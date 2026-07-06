import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CarteFunciara } from '../models/carte-funciara.model';

@Injectable({
  providedIn: 'root'
})
export class CarteFunciaraService {
  private apiUrl = '/api/carti-funciare';

  constructor(private http: HttpClient) {}

  getByTerenId(terenId: number): Observable<CarteFunciara> {
    return this.http.get<CarteFunciara>(`${this.apiUrl}/teren/${terenId}`);
  }

  update(id: number, dto: { numarCf?: string; numarTopografic?: string }): Observable<CarteFunciara> {
    return this.http.put<CarteFunciara>(`${this.apiUrl}/${id}`, dto);
  }
}
