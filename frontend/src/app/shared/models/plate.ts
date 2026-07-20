/**
 * Formato de placa aceito (Mercosul e antigo, já normalizado):
 * espelha `PlateNormalizer.PLATE_REGEX` do backend.
 */
export const PLATE_REGEX = /^[A-Z]{3}[0-9][0-9A-Z][0-9]{2}$/;

/** Normaliza a placa como o backend: caixa alta, sem traços/espaços. */
export function normalizePlate(raw: string): string {
  return raw.trim().replace(/[-\s]/g, '').toUpperCase();
}
