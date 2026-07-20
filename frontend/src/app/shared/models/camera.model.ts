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

/** Envelope de paginação por offset do Spring Data (`Page<T>`). */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}
