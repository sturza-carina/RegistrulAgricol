import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class NotificareSuccesiuneService {
  private readonly apiUrl = '/api/succesiuni';

  constructor(private http: HttpClient) {}

  getAllNotificari(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  createNotificare(notificare: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, notificare);
  }

  getNotificariByCnp(cnp: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/defunct/${cnp}`);
  }

  updateStadiu(id: number, stadiu: string): Observable<any> {
    let params = new HttpParams().set('stadiu', stadiu);
    return this.http.patch<any>(`${this.apiUrl}/${id}/stadiu`, null, { params });
  }
}
