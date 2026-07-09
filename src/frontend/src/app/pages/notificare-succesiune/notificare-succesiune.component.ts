import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NotificareSuccesiuneService } from '../../services/notificare-succesiune.service';
import { PersoanaService } from '../../services/persoana.service';
import { AppTranslatePipe } from '../../services/translate.pipe';

@Component({
  selector: 'app-notificare-succesiune',
  standalone: true,
  imports: [CommonModule, FormsModule, AppTranslatePipe],
  templateUrl: './notificare-succesiune.component.html'
})
export class NotificareSuccesiuneComponent implements OnInit {
  // Formular state
  defunctId: number | null = null;
  dataDecesului: string = '';
  numarCertificatDeces: string = '';
  numeNotarSpnBin: string = '';
  numarAdresaOficiala: string = '';
  dataTrimitere: string = '';
  observatii: string = '';

  isSaving: boolean = false;
  successMessage: string = '';
  errorMessage: string = '';

  // Selectare persoană autocomplete
  personSearchTerm: string = '';
  personSearchResults: any[] = [];
  selectedPerson: any = null;
  showPersonDropdown: boolean = false;
  isSearchingPersons: boolean = false;

  // Căutare dosar prin Blind Index (CNP clar)
  searchCnp: string = '';
  searchResults: any[] = [];
  isSearchingDosar: boolean = false;
  hasSearched: boolean = false;

  // Notari pre-definiți în dropdown
  notariPredefiniti: string[] = [
    'SPN Ionescu și Asociații - București',
    'BIN Popescu Mihai - Cluj-Napoca',
    'SPN Dorobanțu - Craiova',
    'BIN Radu Elena - Timișoara',
    'BIN Vasilescu Vasile - Iași'
  ];

  constructor(
    private notificareSuccesiuneService: NotificareSuccesiuneService,
    private persoanaService: PersoanaService
  ) {}

  ngOnInit(): void {
    // Încărcăm inițial toate notificările dacă utilizatorul dorește o listare globală la deschidere
    this.loadAllNotificari();
  }

  // Căutare autocomplete persoană
  onPersonSearchInput(): void {
    if (this.personSearchTerm.trim().length < 2) {
      this.personSearchResults = [];
      this.showPersonDropdown = false;
      return;
    }

    this.isSearchingPersons = true;
    this.persoanaService.getAllPersons(this.personSearchTerm, 'PHYSICAL_PERSON', 0, 10).subscribe({
      next: (response) => {
        this.personSearchResults = response.content || [];
        this.showPersonDropdown = this.personSearchResults.length > 0;
        this.isSearchingPersons = false;
      },
      error: () => {
        this.isSearchingPersons = false;
      }
    });
  }

  selectPerson(person: any): void {
    this.selectedPerson = person;
    this.defunctId = person.id;
    this.personSearchTerm = `${person.lastName} ${person.firstName} (${person.cnp || 'CNP Criptat'})`;
    this.showPersonDropdown = false;
  }

  // Înregistrare notificare
  submitForm(): void {
    if (!this.defunctId) {
      this.errorMessage = 'Vă rugăm să selectați o persoană fizică din listă.';
      return;
    }
    if (!this.numarAdresaOficiala || !this.dataTrimitere) {
      this.errorMessage = 'Numărul adresei oficiale și data trimiterii sunt obligatorii.';
      return;
    }

    this.isSaving = true;
    this.successMessage = '';
    this.errorMessage = '';

    const payload = {
      defunctId: this.defunctId,
      numeNotarSpnBin: this.numeNotarSpnBin,
      numarAdresaOficiala: this.numarAdresaOficiala,
      dataTrimitere: this.dataTrimitere,
      stadiuNotificare: 'TRIMIS',
      observatii: this.observatii,
      dataDecesului: this.dataDecesului || null,
      numarCertificatDeces: this.numarCertificatDeces || null
    };

    this.notificareSuccesiuneService.createNotificare(payload).subscribe({
      next: (created) => {
        this.isSaving = false;
        this.successMessage = 'Notificarea de succesiune a fost înregistrată cu succes în registrul de Cluj! Defunctul a fost marcat automat ca fiind decedat.';
        this.resetForm();
        this.loadAllNotificari();
      },
      error: (err) => {
        this.isSaving = false;
        this.errorMessage = err.error?.message || 'Eroare la înregistrarea notificării. Verificați datele introduse.';
      }
    });
  }

  resetForm(): void {
    this.defunctId = null;
    this.selectedPerson = null;
    this.personSearchTerm = '';
    this.dataDecesului = '';
    this.numarCertificatDeces = '';
    this.numeNotarSpnBin = '';
    this.numarAdresaOficiala = '';
    this.dataTrimitere = '';
    this.observatii = '';
  }

  // Căutare dosare pe bază de Blind Index
  searchDosar(): void {
    if (!this.searchCnp || this.searchCnp.trim().length !== 13) {
      alert('Vă rugăm să introduceți un CNP valid de 13 cifre pentru căutare.');
      return;
    }

    this.isSearchingDosar = true;
    this.hasSearched = true;
    this.notificareSuccesiuneService.getNotificariByCnp(this.searchCnp.trim()).subscribe({
      next: (data) => {
        this.searchResults = data || [];
        this.isSearchingDosar = false;
      },
      error: () => {
        this.searchResults = [];
        this.isSearchingDosar = false;
        alert('Eroare la efectuarea căutării dosarului.');
      }
    });
  }

  // Modificare stadiu notificare
  changeStadiu(id: number, noulStadiu: string): void {
    if (!id || !noulStadiu) return;

    this.notificareSuccesiuneService.updateStadiu(id, noulStadiu).subscribe({
      next: (updated) => {
        // Actualizăm stadiul în lista de rezultate
        const idx = this.searchResults.findIndex(n => n.id === id);
        if (idx !== -1) {
          this.searchResults[idx] = updated;
        }
        alert(`Stadiul notificării a fost actualizat cu succes în: ${noulStadiu}`);
      },
      error: () => {
        alert('Eroare la actualizarea stadiului notificării.');
      }
    });
  }

  loadAllNotificari(): void {
    this.notificareSuccesiuneService.getAllNotificari().subscribe({
      next: (data) => {
        this.searchResults = data || [];
      }
    });
  }
}
