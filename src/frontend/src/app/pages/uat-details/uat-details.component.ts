import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { LayoutComponent } from '../../components/layout/layout.component';
import { PageHeaderComponent } from '../../components/page-header/page-header.component';
import { BreadcrumbsComponent, BreadcrumbItem } from '../../components/breadcrumbs/breadcrumbs.component';

interface UAT {
  codSiruta: string;
  denumire: string;
  judet: string;
  tipUat: string;
  isActive: boolean;
  tenantId?: string;
}

@Component({
  selector: 'app-uat-details',
  standalone: true,
  imports: [CommonModule, LayoutComponent, PageHeaderComponent, BreadcrumbsComponent],
  templateUrl: './uat-details.component.html',
  styleUrls: ['./uat-details.component.css']
})
export class UatDetailsComponent implements OnInit, OnDestroy {
  codSiruta: string | null = null;
  uat: UAT | null = null;
  loadError = '';
  user: any = null;

  breadcrumbItems: BreadcrumbItem[] = [
    { label: 'UAT-uri', link: '/uats' }
  ];

  private subs = new Subscription();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.subs.add(
      this.authService.currentUser.subscribe(user => {
        if (!user) { this.router.navigate(['/login']); return; }
        this.user = user;
        
        this.subs.add(
          this.route.paramMap.subscribe(params => {
            this.codSiruta = params.get('id');
            if (this.codSiruta) {
              this.loadUat();
            }
          })
        );
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  get isSuperAdmin(): boolean { return this.user?.role === 'ROLE_SUPER_ADMIN'; }

  loadUat(): void {
    const url = this.isSuperAdmin ? '/api/uats' : '/api/uats/tenant';
    this.http.get<UAT[]>(url).subscribe({
      next: (list) => {
        const found = (list || []).find(u => u.codSiruta === this.codSiruta);
        if (found) {
          this.uat = found;
          this.breadcrumbItems = [
            { label: 'UAT-uri', link: '/uats' },
            { label: `Detalii UAT ${this.uat.denumire}` }
          ];
        } else {
          // Fallback if not found in the filtered lists, we might need a specific endpoint, 
          // but for this task, the standard list is sufficient since the user navigates from there.
          this.loadError = 'UAT-ul nu a fost găsit sau nu aveți acces la el.';
        }
      },
      error: () => {
        this.loadError = 'A apărut o eroare la încărcarea datelor.';
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/uats']);
  }
}
