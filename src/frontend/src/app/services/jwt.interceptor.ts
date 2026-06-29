import { HttpInterceptorFn } from '@angular/common/http';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  // Add withCredentials: true to send the HttpOnly cookie automatically
  const clonedReq = req.clone({
    withCredentials: true
  });

  return next(clonedReq);
};

