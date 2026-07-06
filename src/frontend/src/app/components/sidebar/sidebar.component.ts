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

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private uatContext: UatContextService,
    public router: Router
  ) {}

  ngOnInit(): void {
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

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
