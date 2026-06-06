import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService, JwtResponse } from '../services/auth.service';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  user: JwtResponse | null = null;
  activeTab: string = 'overview';
  tenants: any[] = [];

  // Form Fields
  uatName: string = '';
  sirutaCode: string = '';
  successMessage: string = '';
  errorMessage: string = '';
  isSubmitting: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.authService.currentUser.subscribe(user => {
      if (!user) {
        this.router.navigate(['/login']);
      } else {
        this.user = user;
        if (this.user.role === 'ROLE_SUPER_ADMIN') {
          this.loadTenants();
        }
      }
    });
  }

  loadTenants() {
    this.http.get<any[]>('/api/tenants').subscribe({
      next: (data) => {
        this.tenants = data;
      },
      error: (err) => {
        console.error('Failed to load tenants', err);
      }
    });
  }

  setTab(tab: string) {
    this.activeTab = tab;
    this.successMessage = '';
    this.errorMessage = '';
    if (tab === 'tenants') {
      this.loadTenants();
    }
  }

  createUat() {
    if (!this.uatName || !this.sirutaCode) {
      this.errorMessage = 'Please fill out all fields.';
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.http.post('/api/tenants', {
      sirutaCode: this.sirutaCode,
      name: this.uatName
    }).subscribe({
      next: (res: any) => {
        this.successMessage = `UAT "${res.name}" successfully created with schema "${res.schemaName}"!`;
        this.uatName = '';
        this.sirutaCode = '';
        this.isSubmitting = false;
        this.loadTenants();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'An error occurred while creating the UAT.';
        this.isSubmitting = false;
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
