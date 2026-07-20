export interface Detection {
  id: number;
  plate: string;
  cameraId: number;
  detectedAt: string;
}

/** Página por keyset/cursor (forward-only): `nextCursor` nulo = fim. */
export interface DetectionPage {
  content: Detection[];
  nextCursor: string | null;
}

/** Filtros da consulta de passagens. Campos ausentes não são enviados. */
export interface DetectionQuery {
  plate?: string | null;
  cameraId?: number | null;
  from?: string | null; // Instant ISO-8601 (UTC)
  to?: string | null; // Instant ISO-8601 (UTC)
  cursor?: string | null;
  size?: number;
}
