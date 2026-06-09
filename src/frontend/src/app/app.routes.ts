import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { SuperAdminDashboardComponent } from './pages/super-admin-dashboard/super-admin-dashboard.component';
import { TenantAdminDashboardComponent } from './pages/tenant-admin-dashboard/tenant-admin-dashboard.component';
import { UserManagementComponent } from './pages/user-management/user-management.component';
import { CreateTenantComponent } from './pages/create-tenant/create-tenant.component';
import { PersonListComponent } from './pages/person-list/person-list.component';
import { PersonFormComponent } from './pages/person-form/person-form.component';

export const routes: Routes = [
  { path: 'login',          component: LoginComponent },
  { path: 'dashboard',      component: DashboardComponent },
  { path: 'super-admin',    component: SuperAdminDashboardComponent },
  { path: 'tenant-admin',   component: TenantAdminDashboardComponent },
  { path: 'user-management',component: UserManagementComponent },
  { path: 'tenants/new',    component: CreateTenantComponent },
  { path: 'persons',        component: PersonListComponent },
  { path: 'persons/new',    component: PersonFormComponent },
  { path: 'persons/edit/:id', component: PersonFormComponent },
  { path: '',               redirectTo: '/login', pathMatch: 'full' }
];
