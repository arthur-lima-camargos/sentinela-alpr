import { inject } from '@angular/core';
import {
  HttpContextToken,
  HttpErrorResponse,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest,
} from '@angular/common/http';
import { catchError, switchMap, throwError } from 'rxjs';

import { AuthService } from './auth.service';

const RETRIED = new HttpContextToken<boolean>(() => false);

function isAuthEndpoint(url: string): boolean {
  return url.includes('/api/v1/auth/');
}

function withBearer(req: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
  return req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);

  if (isAuthEndpoint(req.url)) {
    return next(req);
  }

  const token = auth.accessToken();
  const authReq = token ? withBearer(req, token) : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      const canRefresh =
        error.status === 401 && !!auth.refreshToken() && !req.context.get(RETRIED);
      if (canRefresh) {
        return retryAfterRefresh(req, next, auth);
      }
      return throwError(() => error);
    })
  );
};

function retryAfterRefresh(req: HttpRequest<unknown>, next: HttpHandlerFn, auth: AuthService) {
  return auth.refresh().pipe(
    switchMap((tokens) => {
      const retried = withBearer(req, tokens.accessToken);
      return next(retried.clone({ context: retried.context.set(RETRIED, true) }));
    }),
    catchError((error) => {
      auth.logout();
      return throwError(() => error);
    })
  );
}
