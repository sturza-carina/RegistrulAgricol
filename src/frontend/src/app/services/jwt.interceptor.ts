import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  let user: any = null;
  authService.currentUser.subscribe(u => user = u).unsubscribe();

  let clonedReq = req;

  // Trimite token-ul indiferent dacă e local (user.token) sau Keycloak (user.token = access_token)
  if (user && user.token) {
    clonedReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${user.token}`,
        'X-Tenant-ID': authService.currentTenantId
      }
    });
  }

  return next(clonedReq);
};
