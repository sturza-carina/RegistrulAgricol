import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AnimalIndividual, EfectivGrup, ProprietarAnimals } from '../models/animal.model';

@Injectable({
  providedIn: 'root'
})
export class AnimalService {
  private baseApiUrl = '/api/animals';

  constructor(private http: HttpClient) {}

  // --- AnimalIndividual CRUD ---

  getAllIndividuals(): Observable<AnimalIndividual[]> {
    return this.http.get<AnimalIndividual[]>(`${this.baseApiUrl}/individual`);
  }

  getIndividualById(id: number): Observable<AnimalIndividual> {
    return this.http.get<AnimalIndividual>(`${this.baseApiUrl}/individual/${id}`);
  }

  createIndividual(animal: AnimalIndividual): Observable<AnimalIndividual> {
    return this.http.post<AnimalIndividual>(`${this.baseApiUrl}/individual`, animal);
  }

  updateIndividual(id: number, animal: AnimalIndividual): Observable<AnimalIndividual> {
    return this.http.put<AnimalIndividual>(`${this.baseApiUrl}/individual/${id}`, animal);
  }

  deleteIndividual(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseApiUrl}/individual/${id}`);
  }

  // --- EfectivGrup CRUD ---

  getAllGroups(): Observable<EfectivGrup[]> {
    return this.http.get<EfectivGrup[]>(`${this.baseApiUrl}/grup`);
  }

  getGroupById(id: number): Observable<EfectivGrup> {
    return this.http.get<EfectivGrup>(`${this.baseApiUrl}/grup/${id}`);
  }

  createGroup(grup: EfectivGrup): Observable<EfectivGrup> {
    return this.http.post<EfectivGrup>(`${this.baseApiUrl}/grup`, grup);
  }

  updateGroup(id: number, grup: EfectivGrup): Observable<EfectivGrup> {
    return this.http.put<EfectivGrup>(`${this.baseApiUrl}/grup/${id}`, grup);
  }

  deleteGroup(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseApiUrl}/grup/${id}`);
  }

  // --- Combined query by proprietar ---

  getAnimalsByProprietar(proprietarId: number): Observable<ProprietarAnimals> {
    return this.http.get<ProprietarAnimals>(`${this.baseApiUrl}/proprietar/${proprietarId}`);
  }
}
