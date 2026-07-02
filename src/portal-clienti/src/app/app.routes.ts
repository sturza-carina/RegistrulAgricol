import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Cereri } from './pages/cereri/cereri';
import { Stadiu } from './pages/stadiu/stadiu';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'cereri', component: Cereri },
  { path: 'stadiu', component: Stadiu },
  { path: '**', redirectTo: '' }
];
