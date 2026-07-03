import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Cereri } from './pages/cereri/cereri';
import { Stadiu } from './pages/stadiu/stadiu';
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';
import { CereriMele } from './pages/cereri-mele/cereri-mele';
import { AuthGuard } from './guards/auth.guard';
import { ContulMeu } from './pages/contul-meu/contul-meu';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'cereri', component: Cereri, canActivate: [AuthGuard] },
  { path: 'cereri-mele', component: CereriMele, canActivate: [AuthGuard] },
  { path: 'stadiu', component: Stadiu },
  { path: 'contul-meu', component: ContulMeu },
  { path: '**', redirectTo: '' }
];
