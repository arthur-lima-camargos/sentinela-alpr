export interface LoginRequest {
  login: string;
  password: string;
}

export interface RefreshRequest {
  refreshToken: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: string;
}

export type Role = 'OPERATOR' | 'ADMIN';

export interface CurrentUser {
  login: string;
  role: Role;
}
