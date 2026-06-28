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
import { ContractManagementComponent } from './pages/contract-management/contract-management.component';
import { AnimalListComponent } from './pages/animal-list/animal-list.component';
import { AnimalIndividualFormComponent } from './pages/animal-individual-form/animal-individual-form.component';
import { EfectivGrupFormComponent } from './pages/efectiv-grup-form/efectiv-grup-form.component';
import { AnimalTimelineComponent } from './pages/animal-timeline/animal-timeline.component';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },

  // ROLE_SUPER_ADMIN only
  { path: 'super-admin', component: SuperAdminDashboardComponent, canActivate: [authGuard, roleGuard], data: { roles: ['ROLE_SUPER_ADMIN'] } },
  { path: 'tenants',     component: CreateTenantComponent,        canActivate: [authGuard, roleGuard], data: { roles: ['ROLE_SUPER_ADMIN'] } },

  // ROLE_ADMIN or ROLE_SUPER_ADMIN
  { path: 'tenant-admin',    component: TenantAdminDashboardComponent, canActivate: [authGuard, roleGuard], data: { roles: ['ROLE_ADMIN', 'ROLE_SUPER_ADMIN'] } },
  { path: 'user-management', component: UserManagementComponent,       canActivate: [authGuard, roleGuard], data: { roles: ['ROLE_ADMIN', 'ROLE_SUPER_ADMIN'] } },
  { path: 'uats',            component: UatManagementComponent,        canActivate: [authGuard, roleGuard], data: { roles: ['ROLE_ADMIN', 'ROLE_SUPER_ADMIN'] } },

  // Any authenticated user
  { path: 'dashboard',                         component: DashboardComponent,           canActivate: [authGuard] },
  { path: 'persoane',                          component: PersonListComponent,           canActivate: [authGuard] },
  { path: 'persoane/new',                      component: PersonFormComponent,           canActivate: [authGuard] },
  { path: 'persoane/edit/:id',                 component: PersonFormComponent,           canActivate: [authGuard] },
  { path: 'contracte',                         component: ContractManagementComponent,   canActivate: [authGuard] },
  { path: 'gospodarii',                        component: GospodarieListComponent,       canActivate: [authGuard] },
  { path: 'gospodarii/new',                    component: GospodarieFormComponent,       canActivate: [authGuard] },
  { path: 'gospodarii/edit/:id',               component: GospodarieFormComponent,       canActivate: [authGuard] },
  { path: 'gospodarii/:id',                    component: GospodarieDetailsComponent,    canActivate: [authGuard] },
  { path: 'terenuri/new',                      component: TerenFormComponent,            canActivate: [authGuard] },
  { path: 'terenuri/:id/parcele',              component: TerenParceleComponent,         canActivate: [authGuard] },
  { path: 'animale',                           component: AnimalListComponent,           canActivate: [authGuard] },
  { path: 'animale/individual/new',            component: AnimalIndividualFormComponent, canActivate: [authGuard] },
  { path: 'animale/individual/edit/:id',       component: AnimalIndividualFormComponent, canActivate: [authGuard] },
  { path: 'animale/grup/new',                  component: EfectivGrupFormComponent,      canActivate: [authGuard] },
  { path: 'animale/grup/:id/snapshot',         component: EfectivGrupFormComponent,      canActivate: [authGuard] },
  { path: 'animale/individual/:id/istoric',    component: AnimalTimelineComponent,       canActivate: [authGuard] },
  { path: 'harta',                             component: ParcelaMapComponent,           canActivate: [authGuard] },
  { path: 'google-harta',                      component: GoogleMapComponent,            canActivate: [authGuard] },

  { path: '', redirectTo: '/login', pathMatch: 'full' }
];
