import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrls: ['./register.css']
})
export class Register {
  formData = {
    nume: '',
    prenume: '',
    cnp: '',
    email: '',
    telefon: '',
    parola: '',
    confirmareParola: '',
    judet: '',
    localitate: '',
    strada: '',
    numar: '',
    bloc: '',
    scara: '',
    etaj: '',
    apartament: ''
  };

  error = '';
  successMsg = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) {
    if (this.authService.currentUserValue) {
      this.router.navigate(['/']);
    }
  }

  onSubmit() {
    this.error = '';
    
    // Basic validations
    if (!this.formData.nume || !this.formData.prenume || !this.formData.cnp || !this.formData.email || !this.formData.telefon || !this.formData.parola) {
      this.error = 'Toate datele personale sunt obligatorii.';
      return;
    }
    
    if (this.formData.cnp.length !== 13) {
      this.error = 'CNP-ul trebuie să aibă 13 caractere.';
      return;
    }

    if (this.formData.parola !== this.formData.confirmareParola) {
      this.error = 'Parolele nu coincid.';
      return;
    }

    if (!this.formData.judet || !this.formData.localitate || !this.formData.strada || !this.formData.numar) {
      this.error = 'Județul, Localitatea, Strada și Numărul sunt obligatorii.';
      return;
    }

    this.loading = true;

    this.authService.register(this.formData).subscribe({
      next: () => {
        this.successMsg = 'Contul a fost creat cu succes! Te redirecționăm...';
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (err) => {
        this.error = err.error || 'Eroare la înregistrare. Verifică datele introduse.';
        this.loading = false;
      }
    });
  }
}
