/** Estados do alerta — espelha o enum `AlertStatus` do backend. */
export type AlertStatus = 'NEW' | 'SEEN';

export interface Alert {
  id: number;
  plate: string;
  detectionId: number;
  watchedVehicleId: number;
  detectedAt: string;
  status: AlertStatus;
  createdAt: string;
}
