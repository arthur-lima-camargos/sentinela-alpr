import { Injectable, inject, signal } from '@angular/core';
import { Subject } from 'rxjs';
import { Client } from '@stomp/stompjs';

import { AuthService } from '../auth/auth.service';
import { Alert } from '../../shared/models/alert.model';

const TOPIC = '/topic/alerts';

@Injectable({ providedIn: 'root' })
export class AlertRealtimeService {
  private readonly auth = inject(AuthService);

  private client?: Client;
  private readonly incoming = new Subject<Alert>();

  readonly messages$ = this.incoming.asObservable();

  readonly connected = signal(false);

  readonly liveCount = signal(0);

  connect(): void {
    if (this.client?.active) {
      return;
    }
    const client = new Client({
      brokerURL: this.brokerUrl(),
      reconnectDelay: 5000,
      beforeConnect: () => {
        const token = this.auth.accessToken();
        client.connectHeaders = token ? { Authorization: `Bearer ${token}` } : {};
      },
      onConnect: () => {
        this.connected.set(true);
        client.subscribe(TOPIC, (message) => {
          try {
            const alert = JSON.parse(message.body) as Alert;
            this.incoming.next(alert);
            if (alert.status === 'NEW') {
              this.liveCount.update((n) => n + 1);
            }
          } catch {
            return;
          }
        });
      },
      onWebSocketClose: () => this.connected.set(false),
      onStompError: () => this.connected.set(false),
    });
    this.client = client;
    client.activate();
  }

  disconnect(): void {
    this.liveCount.set(0);
    this.connected.set(false);
    void this.client?.deactivate();
    this.client = undefined;
  }

  resetLiveCount(): void {
    this.liveCount.set(0);
  }

  private brokerUrl(): string {
    const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
    return `${protocol}://${location.host}/ws`;
  }
}
