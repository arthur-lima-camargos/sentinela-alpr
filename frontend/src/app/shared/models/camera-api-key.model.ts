export interface ApiKey {
  id: number;
  cameraId: number;
  keyPrefix: string;
  active: boolean;
  createdAt: string;
  revokedAt: string | null;
}

export interface IssuedApiKey {
  id: number;
  cameraId: number;
  apiKey: string;
  keyPrefix: string;
  createdAt: string;
}
