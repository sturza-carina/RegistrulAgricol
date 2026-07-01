import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

import { LayoutComponent } from '../../components/layout/layout.component';
import { PageHeaderComponent } from '../../components/page-header/page-header.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';

@Component({
  selector: 'app-super-admin-dashboard',
  standalone: true,
  imports: [CommonModule, LayoutComponent, PageHeaderComponent, BreadcrumbsComponent],
  templateUrl: './super-admin-dashboard.component.html',
})
export class SuperAdminDashboardComponent implements OnInit {

  user: any = null;

  recentTenants: any[] = [];
  totalUsers: number = 0;

  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'Super Admin Dashboard', link: '/dashboard' }
  ];

  currentPage = 1;
  itemsPerPage = 5;

  get paginatedItems() {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    return this.recentTenants.slice(startIndex, startIndex + this.itemsPerPage);
  }

  get totalPages() {
    return Math.ceil(this.recentTenants.length / this.itemsPerPage) || 1;
  }

  nextPage() {
    if (this.currentPage < this.totalPages) this.currentPage++;
  }

  prevPage() {
    if (this.currentPage > 1) this.currentPage--;
  }

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
    this.loadTenants();
    this.loadUsers();
  }

  loadUsers(): void {
    this.http.get<any>('/api/users?page=0&size=1000').subscribe({
      next: (response) => {
        this.totalUsers = response.totalElements;
      },
      error: (err) => console.error('Failed to load users', err)
    });
  }

  loadTenants(): void {
    this.http.get<any[]>('/api/tenants').subscribe({
      next: (data) => {
        this.recentTenants = data.map(t => {
          // Generate initials and color based on name
          const initials = t.name.substring(0, 2).toUpperCase();
          const colors = ['#1a6b3c', '#0369a1', '#9333ea', '#15803d', '#6b7280', '#eab308', '#dc2626'];
          const color = colors[t.id % colors.length] || '#1a6b3c';
          
          return {
            name: t.name,
            initials: initials,
            color: color,
            schema: t.schemaName,
            date: new Date(t.createdAt).toLocaleDateString(),
            numUats: t.uatsCount || 1 // Backend will supply this, defaults to 1 for demo
          };
        });
        this.currentPage = 1;
      },
      error: (err) => console.error('Failed to load tenants', err)
    });
  }

  exportRecords(): void {
    // TODO: implement export
  }

  goToCreateTenant(): void {
    this.router.navigate(['/tenants'], { queryParams: { action: 'create' } });
  }

  goToCreateUat(): void {
    this.router.navigate(['/uats'], { queryParams: { action: 'create' } });
  }

  goToUats(): void {
    this.router.navigate(['/uats']);
  }

  goToCreateUser(): void {
    this.router.navigate(['/users'], { queryParams: { action: 'create' } });
  }

  goToUsers(): void {
    this.router.navigate(['/users']);
  }


}

