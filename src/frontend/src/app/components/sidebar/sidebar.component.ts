import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html'
})
export class SidebarComponent implements OnInit {
  @Input() activeGospodarieId?: number;
  @Input() activeTab?: string;
  user: any = null;
  isImpersonating: boolean = false;

  constructor(
    private authService: AuthService,
    public router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser.subscribe(u => {
      this.user = u;
    });
    this.authService.impersonatedTenant.subscribe(tenantId => {
      this.isImpersonating = !!tenantId;
    });
  }

  stopImpersonating(): void {
    this.authService.stopImpersonation();
    this.router.navigate(['/uats']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
