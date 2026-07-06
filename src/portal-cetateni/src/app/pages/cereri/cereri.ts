import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { UATS } from '../../data/uats.data';
import { AuthService } from '../../services/auth.service';

import { ChangeDetectorRef } from '@angular/core';

export enum TipCerere {
  ELIBERARE_ATESTAT_PRODUCATOR = 'ELIBERARE_ATESTAT_PRODUCATOR',
  ELIBERARE_CARNET_COMERCIALIZARE = 'ELIBERARE_CARNET_COMERCIALIZARE',
  ADEVERINTA_ROL = 'ADEVERINTA_ROL',
  SESIZARE_AMBROZIA = 'SESIZARE_AMBROZIA',
  ADEVERINTA_AVIZ_CONSULTATIV = 'ADEVERINTA_AVIZ_CONSULTATIV',
  ADEVERINTA_TEREN_PF = 'ADEVERINTA_TEREN_PF',
  ADEVERINTA_TEREN_PJ = 'ADEVERINTA_TEREN_PJ',
  COPIE_REGISTRU_1959_1963 = 'COPIE_REGISTRU_1959_1963',
  EXTRAS_REGISTRU_PF = 'EXTRAS_REGISTRU_PF',
  EXTRAS_REGISTRU_PJ = 'EXTRAS_REGISTRU_PJ',
  ADEVERINTA_DETINERE_TEREN = 'ADEVERINTA_DETINERE_TEREN'
}

@Component({
  selector: 'app-cereri',
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './cereri.html',
  styleUrl: './cereri.css',
})
export class Cereri implements OnInit {
  TipCerere = TipCerere;
  step: number = 1;

  tipuriCereri = [
    { id: TipCerere.ELIBERARE_ATESTAT_PRODUCATOR, title: 'Cerere pentru atestat de producător', description: 'Cerere pentru eliberarea atestatului de producător agricol.' },
    { id: TipCerere.ELIBERARE_CARNET_COMERCIALIZARE, title: 'Cerere pentru carnet de comercializare', description: 'Cerere pentru eliberarea carnetului de comercializare a produselor.' },
    { id: TipCerere.ADEVERINTA_ROL, title: 'Cerere adeverință rol', description: 'Cerere pentru adeverință rol.' },
    { id: TipCerere.SESIZARE_AMBROZIA, title: 'Sesizare - terenuri infestate cu ambrozia', description: 'Sesizare referitoare la terenuri infestate cu buruiana ambrozia.' },
    { id: TipCerere.ADEVERINTA_AVIZ_CONSULTATIV, title: 'Cerere adeverință pentru obținerea avizului consultativ', description: 'Cerere pentru eliberare adeverință necesar obținerii avizului.' },
    { id: TipCerere.ADEVERINTA_TEREN_PF, title: 'Cerere adeverință teren înscris (Persoane Fizice)', description: 'adeverință pentru terenul înscris în registrul agricol (persoane fizice).' },
    { id: TipCerere.ADEVERINTA_TEREN_PJ, title: 'Cerere adeverință teren înscris (Persoane Juridice)', description: 'adeverință pentru terenul înscris în registrul agricol (persoane juridice).' },
    { id: TipCerere.COPIE_REGISTRU_1959_1963, title: 'Cerere copie registru (1959-1963)', description: 'Cerere copie din registrul agricol pentru perioada 1959-1963.' },
    { id: TipCerere.EXTRAS_REGISTRU_PF, title: 'Cerere extras registru 2020-2024 (PF)', description: 'Extras din registrul agricol pentru perioada 2020-2024 (persoane fizice).' },
    { id: TipCerere.EXTRAS_REGISTRU_PJ, title: 'Cerere extras registru 2020-2024 (PJ)', description: 'Extras din registrul agricol pentru perioada 2020-2024 (persoane juridice).' },
    { id: TipCerere.ADEVERINTA_DETINERE_TEREN, title: 'Cerere adeverință deținere/nedeținere teren', description: 'adeverință că deține sau nu deține teren agricol.' }
  ];

  selectedTipTitle: string = '';

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
    uatId: '',
    tipCerere: ''
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
  atestatStatus: any = null;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef, private authService: AuthService) {}

  ngOnInit() {
    this.toateLocalitatile = UATS;
    this.judete = [...new Set(UATS.filter(l => l && l.judet).map(l => l.judet))].sort((a: any, b: any) => a.localeCompare(b));
    this.isLoadingUats = false;

    this.authService.currentUser$.subscribe(user => {
      if (user && user.role === 'CETATEAN') {
        this.http.get<any>('/api/public/cetatean/me').subscribe({
          next: (cetatean) => {
            if (cetatean) {
              this.cerere.nume = [cetatean.nume, cetatean.prenume].filter(Boolean).join(' ');
              this.cerere.cnpCui = cetatean.cnp || '';
              this.cerere.telefon = cetatean.telefon || '';
              this.cerere.email = cetatean.email || '';
              
              if (cetatean.judet) {
                this.selectedJudet = cetatean.judet;
                this.onJudetChange();
              }
              if (cetatean.localitate) {
                const loc = this.localitati.find(l => l.denumire.toLowerCase() === cetatean.localitate.toLowerCase());
                if (loc) {
                  this.cerere.uatId = loc.id;
                }
              }
              
              this.domiciliuObj.strada = 'Str.';
              this.domiciliuObj.numeStrada = cetatean.strada || '';
              this.domiciliuObj.numar = cetatean.numar || '';
              this.domiciliuObj.bloc = cetatean.bloc || '';
              this.domiciliuObj.scara = cetatean.scara || '';
              this.domiciliuObj.etaj = cetatean.etaj || '';
              this.domiciliuObj.apartament = cetatean.apartament || '';
              
              this.cdr.detectChanges();
            }
          },
          error: (err) => console.error('Eroare la preluarea profilului', err)
        });
        
        // Fetch document status for preventing duplicate requests
        this.http.get<any>('/api/public/atestate/my-status').subscribe({
          next: (status) => {
            this.atestatStatus = status;
          },
          error: (err) => console.error('Error loading atestate status', err)
        });
      }
    });
  }

  selectTipCerere(tip: any) {
    if (tip.id === TipCerere.ELIBERARE_ATESTAT_PRODUCATOR && this.atestatStatus?.areAtestat) {
        alert("Dețineți deja un Atestat de Producător existent. Nu puteți trimite o altă cerere.");
        return;
    }
    if (tip.id === TipCerere.ELIBERARE_CARNET_COMERCIALIZARE && this.atestatStatus?.areCarnet) {
        alert("Dețineți deja un Carnet de Comercializare existent. Nu puteți trimite o altă cerere.");
        return;
    }
  
    this.cerere.tipCerere = tip.id;
    this.selectedTipTitle = tip.title;
    this.step = 2;
  }

  goBack() {
    this.step = 1;
    this.successCode = null;
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