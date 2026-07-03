import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { UatContextService } from '../../services/uat-context.service';
import { Uat } from '../../models/gospodarie.model';

import { AppTranslatePipe } from '../../services/translate.pipe';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, AppTranslatePipe],
  templateUrl: './sidebar.component.html'
})
export class SidebarComponent implements OnInit, OnDestroy {
  @Input() activeGospodarieId?: number;
  @Input() activeTab?: string;

  user: any = null;
  isImpersonating: boolean = false;
  availableUats: Uat[] = [];
  activeUat: Uat | null = null;
  activeUatCode: string = '';

  currentLocale: string = 'ro';

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private uatContext: UatContextService,
    public router: Router
  ) {}

  ngOnInit(): void {
    this.currentLocale = window.location.pathname.startsWith('/en/') ? 'en' : 'ro';
    this.authService.currentUser
      .pipe(takeUntil(this.destroy$))
      .subscribe(u => { this.user = u; });

    this.authService.impersonatedTenant
      .pipe(takeUntil(this.destroy$))
      .subscribe(tenantId => { this.isImpersonating = !!tenantId; });

    this.uatContext.availableUats$
      .pipe(takeUntil(this.destroy$))
      .subscribe(uats => { this.availableUats = uats; });

    this.uatContext.activeUat$
      .pipe(takeUntil(this.destroy$))
      .subscribe(uat => {
        this.activeUat = uat;
        this.activeUatCode = uat?.codSiruta || '';
      });
  }

  onUatChange(codSiruta: string): void {
    if (codSiruta === this.activeUat?.codSiruta) return;
    const selected = this.availableUats.find(u => u.codSiruta === codSiruta);
    if (selected) {
      this.uatContext.setActiveUat(selected);
    }
  }

  stopImpersonating(): void {
    this.authService.stopImpersonation();
    this.router.navigate(['/uats']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  changeLanguage(locale: string): void {
    if (locale === this.currentLocale) return;
    document.cookie = `lang=${locale};path=/;max-age=31536000`;
    const path = window.location.pathname;
    if (path.startsWith('/ro/') || path.startsWith('/en/')) {
      const newPath = path.replace(/^\/(ro|en)/, `/${locale}`);
      window.location.href = window.location.origin + newPath + window.location.search + window.location.hash;
    } else {
      alert(`Language switched to ${locale.toUpperCase()}. In production, this will redirect to /${locale}/. In development, run the corresponding build config (e.g. npm run start -- --configuration=${locale}) to view.`);
      window.location.reload();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
