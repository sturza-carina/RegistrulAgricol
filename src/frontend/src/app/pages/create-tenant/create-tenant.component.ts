import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { TenantService } from '../../services/tenant.service';

@Component({
  selector: 'app-create-tenant',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './create-tenant.component.html',
})
export class CreateTenantComponent implements OnInit {

  user: any = null;
  isSubmitting = false;
  successMessage = '';
  errorMessage = '';

  form = {
    name: ''
  };

  constructor(
    private router: Router,
    private authService: AuthService,
    private tenantService: TenantService
  ) {}

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => {
      if (!user) {
        this.router.navigate(['/login']);
      } else {
        this.user = user;
      }
    });
  }

  goToDashboard(): void {
    this.router.navigate(['/super-admin']);
  }

  goToUsers(): void {
    this.router.navigate(['/user-management']);
  }

  goToSettings(): void {}

  goToTenants(): void {
    this.router.navigate(['/tenants']);
  }

  cancel(): void {
    this.router.navigate(['/super-admin']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  onCreateTenant(): void {
    if (!this.form.name) {
      this.errorMessage = 'Vă rugăm să introduceți numele tenantului.';
      return;
    }
    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.tenantService.createTenant(this.form.name).subscribe({
      next: (res: any) => {
        this.successMessage = `Tenantul "${res.name}" a fost creat cu succes!`;
        this.isSubmitting = false;
        setTimeout(() => this.router.navigate(['/super-admin']), 1500);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'A apărut o eroare la crearea tenantului.';
        this.isSubmitting = false;
      }
    });
  }
}
