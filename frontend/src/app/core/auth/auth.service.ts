import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, finalize, shareReplay, tap, throwError } from 'rxjs';

import { CurrentUser, LoginRequest, Role, TokenResponse } from './auth.models';

const ACCESS_KEY = 'alpr.accessToken';
const REFRESH_KEY = 'alpr.refreshToken';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly accessTokenSig = signal<string | null>(localStorage.getItem(ACCESS_KEY));
  private readonly refreshTokenSig = signal<string | null>(localStorage.getItem(REFRESH_KEY));

  private refresh$?: Observable<TokenResponse>;

  readonly currentUser = computed<CurrentUser | null>(() => decodeUser(this.accessTokenSig()));
  readonly isAuthenticated = computed(() => this.currentUser() !== null);

  accessToken(): string | null {
    return this.accessTokenSig();
  }

  refreshToken(): string | null {
    return this.refreshTokenSig();
  }

  login(credentials: LoginRequest): Observable<TokenResponse> {
    return this.http.post<TokenResponse>('/api/v1/auth/login', credentials).pipe(
      tap((tokens) => this.store(tokens))
    );
  }

  refresh(): Observable<TokenResponse> {
    if (!this.refresh$) {
      const token = this.refreshTokenSig();
      if (!token) {
        return throwError(() => new Error('Sem refresh token'));
      }
      this.refresh$ = this.http
        .post<TokenResponse>('/api/v1/auth/refresh', { refreshToken: token })
        .pipe(
          tap((tokens) => this.store(tokens)),
          shareReplay(1),
          finalize(() => (this.refresh$ = undefined))
        );
    }
    return this.refresh$;
  }

  logout(): void {
    this.clear();
    this.router.navigate(['/login']);
  }

  private store(tokens: TokenResponse): void {
    localStorage.setItem(ACCESS_KEY, tokens.accessToken);
    localStorage.setItem(REFRESH_KEY, tokens.refreshToken);
    this.accessTokenSig.set(tokens.accessToken);
    this.refreshTokenSig.set(tokens.refreshToken);
  }

  private clear(): void {
    localStorage.removeItem(ACCESS_KEY);
    localStorage.removeItem(REFRESH_KEY);
    this.accessTokenSig.set(null);
    this.refreshTokenSig.set(null);
  }
}

function decodeUser(token: string | null): CurrentUser | null {
  if (!token) {
    return null;
  }
  try {
    const payload = token.split('.')[1];
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    const claims = JSON.parse(json) as { sub?: string; role?: Role };
    if (!claims.sub || !claims.role) {
      return null;
    }
    return { login: claims.sub, role: claims.role };
  } catch {
    return null;
  }
}
