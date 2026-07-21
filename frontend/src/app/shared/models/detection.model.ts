export interface Detection {
  id: number;
  plate: string;
  cameraId: number;
  detectedAt: string;
}

export interface DetectionPage {
  content: Detection[];
  nextCursor: string | null;
}

export interface DetectionQuery {
  plate?: string | null;
  cameraId?: number | null;
  from?: string | null;
  to?: string | null;
  cursor?: string | null;
  size?: number;
}

export interface DetectionSummary {
  lastHour: number;
  last24h: number;
}
