export type WatchReason = 'ROBBERY' | 'THEFT' | 'WANTED' | 'SUSPECT';

export const WATCH_REASONS: ReadonlyArray<{ value: WatchReason; label: string }> = [
  { value: 'ROBBERY', label: 'Roubo' },
  { value: 'THEFT', label: 'Furto' },
  { value: 'WANTED', label: 'Procurado' },
  { value: 'SUSPECT', label: 'Suspeito' },
];

export function watchReasonLabel(reason: WatchReason): string {
  return WATCH_REASONS.find((r) => r.value === reason)?.label ?? reason;
}

export const PLATE_REGEX = /^[A-Z]{3}[0-9][0-9A-Z][0-9]{2}$/;

export function normalizePlate(raw: string): string {
  return raw.trim().replace(/[-\s]/g, '').toUpperCase();
}

export interface WatchedVehicle {
  id: number;
  plate: string;
  reason: WatchReason;
  active: boolean;
  createdAt: string;
}

export interface WatchlistRequest {
  plate: string;
  reason: WatchReason;
  active: boolean;
}

export interface WatchlistUpdateRequest {
  reason: WatchReason;
}
