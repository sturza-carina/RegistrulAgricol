import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

function homeForRole(role: string): string {
  if (role === 'ROLE_SUPER_ADMIN') return '/super-admin';
  if (role === 'ROLE_ADMIN') return '/tenant-admin';
  return '/gospodarii';
}

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const user = authService.currentUserSubject.value;
  if (!user) {
    return router.createUrlTree(['/login']);
  }

  const allowedRoles: string[] = route.data['roles'] ?? [];
  if (allowedRoles.length === 0 || allowedRoles.includes(user.role)) {
    return true;
  }

  return router.createUrlTree([homeForRole(user.role)]);
};
