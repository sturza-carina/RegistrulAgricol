import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { HttpClient } from '@angular/common/http';

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
    orgName: '',
    schemaName: '',
    domain: '',
    region: '',
    contactName: '',
    contactEmail: '',
    status: 'pending',
  };

  constructor(
    private router: Router,
    private authService: AuthService,
    private http: HttpClient
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
    this.router.navigate(['/tenants/new']);
  }

  cancel(): void {
    this.router.navigate(['/super-admin']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  onCreateTenant(): void {
    if (!this.form.orgName || !this.form.schemaName) {
      this.errorMessage = 'Please fill out all required fields.';
      return;
    }
    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.http.post('/api/tenants', {
      name: this.form.orgName,
      sirutaCode: this.form.schemaName,
    }).subscribe({
      next: (res: any) => {
        this.successMessage = `Tenant "${res.name}" created successfully!`;
        this.isSubmitting = false;
        setTimeout(() => this.router.navigate(['/super-admin']), 1500);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'An error occurred while creating the tenant.';
        this.isSubmitting = false;
      }
    });
  }
}
