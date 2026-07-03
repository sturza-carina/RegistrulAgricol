import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { UATS } from '../../data/uats.data';

import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-cereri',
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './cereri.html',
  styleUrl: './cereri.css',
})
export class Cereri implements OnInit {
  toateLocalitatile: any[] = [];
  judete: string[] = [];
  localitati: any[] = [];
  selectedJudet: string = '';
  
  cerere: any = {
    nume: '',
    cnpCui: '',
    domiciliu: '',
    telefon: '',
    email: '',
    numarCarteFunciara: '',
    numarCadastral: '',
    uatId: ''
  };

  domiciliuObj: any = {
    strada: '',
    numeStrada: '',
    numar: '',
    bloc: '',
    scara: '',
    etaj: '',
    apartament: ''
  };

  isSubmitting = false;
  successCode: string | null = null;
  isLoadingUats = true;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.toateLocalitatile = UATS;
    this.judete = [...new Set(UATS.filter(l => l && l.judet).map(l => l.judet))].sort((a: any, b: any) => a.localeCompare(b));
    this.isLoadingUats = false;
  }

  onJudetChange() {
    this.cerere.uatId = '';
    this.localitati = [];
    if (this.selectedJudet) {
      this.localitati = this.toateLocalitatile
        .filter(l => l && l.judet === this.selectedJudet)
        .sort((a, b) => (a.denumire || '').localeCompare(b.denumire || ''));
    }
  }

  submitCerere() {
    this.isSubmitting = true;
    
    // Construct domiciliu
    const localitateName = this.localitati.find(l => l.id == this.cerere.uatId)?.denumire || '';
    let addressParts = [
      `Județ ${this.selectedJudet}`,
      `Localitate ${localitateName}`
    ];
    
    if (this.domiciliuObj.strada || this.domiciliuObj.numeStrada) {
      addressParts.push(`${this.domiciliuObj.strada} ${this.domiciliuObj.numeStrada}`.trim());
    }
    if (this.domiciliuObj.numar) addressParts.push(`Nr. ${this.domiciliuObj.numar}`);
    if (this.domiciliuObj.bloc) addressParts.push(`Bl. ${this.domiciliuObj.bloc}`);
    if (this.domiciliuObj.scara) addressParts.push(`Sc. ${this.domiciliuObj.scara}`);
    if (this.domiciliuObj.etaj) addressParts.push(`Et. ${this.domiciliuObj.etaj}`);
    if (this.domiciliuObj.apartament) addressParts.push(`Ap. ${this.domiciliuObj.apartament}`);
    
    this.cerere.domiciliu = addressParts.join(', ');

    this.cdr.detectChanges();
    this.http.post<any>('/api/public/cereri', this.cerere).subscribe({
      next: (res) => {
        if (res && res.codCerere) {
          this.successCode = res.codCerere;
        } else {
          alert('Cererea a fost trimisă, dar nu am putut prelua codul de confirmare.');
        }
        this.isSubmitting = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.isSubmitting = false;
        this.cdr.detectChanges();
        alert('A apărut o eroare la trimiterea cererii. Vă rugăm să încercați din nou.');
      }
    });
  }
}
