import { Routes } from '@angular/router';

import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./shared/shell/shell.component').then((m) => m.ShellComponent),
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/home/home.component').then((m) => m.HomeComponent),
      },
      {
        path: 'cameras',
        loadComponent: () =>
          import('./features/cameras/cameras.component').then((m) => m.CamerasComponent),
      },
      {
        path: 'watchlist',
        loadComponent: () =>
          import('./features/watchlist/watchlist.component').then((m) => m.WatchlistComponent),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
