import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { SuperAdminDashboardComponent } from './pages/super-admin-dashboard/super-admin-dashboard.component';
import { TenantAdminDashboardComponent } from './pages/tenant-admin-dashboard/tenant-admin-dashboard.component';
import { UserManagementComponent } from './pages/user-management/user-management.component';
import { CreateTenantComponent } from './pages/create-tenant/create-tenant.component';
import { TenantsComponent } from './pages/tenants/tenants.component';

export const routes: Routes = [
  { path: 'login',          component: LoginComponent },
  { path: 'dashboard',      component: DashboardComponent },
  { path: 'super-admin',    component: SuperAdminDashboardComponent },
  { path: 'tenant-admin',   component: TenantAdminDashboardComponent },
  { path: 'user-management',component: UserManagementComponent },
  { path: 'tenants',        component: TenantsComponent },
  { path: 'tenants/new',    component: CreateTenantComponent },
  { path: '',               redirectTo: '/login', pathMatch: 'full' }
];
