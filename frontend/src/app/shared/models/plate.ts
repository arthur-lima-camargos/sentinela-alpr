export const PLATE_REGEX = /^[A-Z]{3}[0-9][0-9A-Z][0-9]{2}$/;

export function normalizePlate(raw: string): string {
  return raw.trim().replace(/[-\s]/g, '').toUpperCase();
}
