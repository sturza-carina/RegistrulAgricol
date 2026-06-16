import { Component, OnInit, OnDestroy} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { UatContextService } from '../../services/uat-context.service';
import {Uat} from '../../models/gospodarie.model';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './sidebar.component.html'
})
export class SidebarComponent implements OnInit, OnDestroy {
  user: any = null;
  isImpersonating: boolean = false;
  availableUats: Uat[] = [];
  activeUat: Uat | null = null;
  activeUatCode: string = '';

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private uatContext: UatContextService,
    private router: Router
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
      .subscribe(uats => {
        console.log('availableUats$ emis in sidebar:', uats.length, uats.map(u => u.codSiruta));
        this.availableUats = uats;
      });

    this.uatContext.activeUat$
      .pipe(takeUntil(this.destroy$))
      .subscribe(uat => {
        this.activeUat = uat;
        this.activeUatCode = uat?.codSiruta || '';  // sincronizezi string-ul pentru ngModel
      });
  }

  onUatChange(codSiruta: string): void {
    // ignoră dacă e același UAT — previne loop-uri
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
