import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { SuperAdminDashboardComponent } from './pages/super-admin-dashboard/super-admin-dashboard.component';
import { TenantAdminDashboardComponent } from './pages/tenant-admin-dashboard/tenant-admin-dashboard.component';
import { UserManagementComponent } from './pages/user-management/user-management.component';
import { CreateTenantComponent } from './pages/create-tenant/create-tenant.component';
import { PersonListComponent } from './pages/persoana-list/persoana-list.component';
import { PersonFormComponent } from './pages/persoana-form/persoana-form.component';
import { UatManagementComponent } from './pages/uat-management/uat-management.component';
import { ParcelaMapComponent } from './components/parcela-map/parcela-map.component';
import { GospodarieListComponent } from './pages/gospodarie-list/gospodarie-list.component';
import { GospodarieFormComponent } from './pages/gospodarie-form/gospodarie-form.component';
import { GospodarieDetailsComponent } from './pages/gospodarie-details/gospodarie-details.component';
import { TerenFormComponent } from './pages/teren-form/teren-form.component';
import { TerenParceleComponent } from './pages/teren-parcele/teren-parcele.component';
import { GoogleMapComponent } from './components/google-map/google-map.component';

export const routes: Routes = [
  { path: 'login',          component: LoginComponent },
  { path: 'dashboard',      component: DashboardComponent },
  { path: 'super-admin',    component: SuperAdminDashboardComponent },
  { path: 'tenant-admin',   component: TenantAdminDashboardComponent },
  { path: 'user-management',component: UserManagementComponent },
  { path: 'tenants',        component: CreateTenantComponent },
  { path: 'persoane',        component: PersonListComponent },
  { path: 'persoane/new',    component: PersonFormComponent },
  { path: 'persoane/edit/:id', component: PersonFormComponent },
  { path: 'uats',           component: UatManagementComponent },
  { path: 'gospodarii',     component: GospodarieListComponent },
  { path: 'gospodarii/new', component: GospodarieFormComponent },
  { path: 'gospodarii/edit/:id', component: GospodarieFormComponent },
  { path: 'gospodarii/:id', component: GospodarieDetailsComponent },
  { path: 'terenuri/new',   component: TerenFormComponent },
  { path: 'terenuri/:id/parcele', component: TerenParceleComponent },
  { path: 'harta',          component: ParcelaMapComponent },
  { path: 'google-harta',   component: GoogleMapComponent },
  { path: '',               redirectTo: '/login', pathMatch: 'full' }
];


