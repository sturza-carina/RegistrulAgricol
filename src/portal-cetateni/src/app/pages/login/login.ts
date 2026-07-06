import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login {
  email = '';
  parola = '';
  error = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) {
    if (this.authService.currentUserValue) {
      this.router.navigate(['/']);
    }
  }

  onSubmit() {
    if (!this.email || !this.parola) {
      this.error = 'Completează ambele câmpuri.';
      return;
    }
    
    this.loading = true;
    this.error = '';

    this.authService.login(this.email, this.parola).subscribe({
      next: () => {
        const returnUrl = this.router.routerState.snapshot.root.queryParams['returnUrl'] || '/';
        this.router.navigateByUrl(returnUrl);
      },
      error: (err) => {
        if (err.status === 401) {
          this.error = 'Date de autentificare incorecte.';
        } else if (err.status === 0 || err.status >= 500) {
          this.error = 'Eroare de conexiune cu serverul. Te rugăm să încerci din nou.';
        } else {
          this.error = 'A apărut o eroare neașteptată.';
        }
        this.loading = false;
      }
    });
  }
}
