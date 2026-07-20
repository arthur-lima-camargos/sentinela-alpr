export interface Camera {
  id: number;
  name: string;
  latitude: number | null;
  longitude: number | null;
  road: string | null;
  active: boolean;
  createdAt: string;
}

export interface CameraRequest {
  name: string;
  latitude: number | null;
  longitude: number | null;
  road: string | null;
}

export type { Page } from './page.model';
