import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

import { SidebarComponent } from '../../components/sidebar/sidebar.component';

@Component({
  selector: 'app-super-admin-dashboard',
  standalone: true,
  imports: [CommonModule, SidebarComponent],
  templateUrl: './super-admin-dashboard.component.html',
})
export class SuperAdminDashboardComponent implements OnInit {

  user: any = null;

  recentTenants = [
    { name: 'AgriGroup S.A.',       initials: 'AG', color: '#1a6b3c', schema: 'agri_group_schema',      date: 'Oct 24, 2023', status: 'ACTIVE' },
    { name: 'TerraForma Solutions',  initials: 'TF', color: '#0369a1', schema: 'terraform_sol_v2',       date: 'Oct 22, 2023', status: 'ACTIVE' },
    { name: 'BioVineyard Ltd.',      initials: 'BV', color: '#9333ea', schema: 'biovine_master',         date: 'Oct 19, 2023', status: 'PENDING' },
    { name: 'GreenHorizon Coop',     initials: 'GH', color: '#15803d', schema: 'green_horizon_prod',     date: 'Oct 15, 2023', status: 'ACTIVE' },
    { name: 'SoilMaster Analytics',  initials: 'SM', color: '#6b7280', schema: 'soil_master_analytical', date: 'Oct 12, 2023', status: 'ARCHIVED' },
  ];

  constructor(
    private router: Router,
    private authService: AuthService
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

  exportRecords(): void {
    console.log('Export records clicked');
    // TODO: implement export
  }

  goToCreateTenant(): void {
    this.router.navigate(['/tenants/new']);
  }

  goToUsers(): void {
    this.router.navigate(['/user-management']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}

