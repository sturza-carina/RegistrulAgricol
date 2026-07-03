import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Cereri } from './pages/cereri/cereri';
import { Stadiu } from './pages/stadiu/stadiu';
import { ContulMeu } from './pages/contul-meu/contul-meu';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'cereri', component: Cereri },
  { path: 'stadiu', component: Stadiu },
  { path: 'contul-meu', component: ContulMeu },
  { path: '**', redirectTo: '' }
];
