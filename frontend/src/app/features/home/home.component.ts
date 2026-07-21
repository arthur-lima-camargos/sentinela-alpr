import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';

import { AlertService } from '../../core/alerts/alert.service';
import { CameraService } from '../../core/cameras/camera.service';
import { DetectionService } from '../../core/detections/detection.service';
import { WatchlistService } from '../../core/watchlist/watchlist.service';
import { AlertRealtimeService } from '../../core/alerts/alert-realtime.service';
import { AlertSummary } from '../../shared/models/alert.model';
import { CameraSummary } from '../../shared/models/camera.model';
import { DetectionSummary } from '../../shared/models/detection.model';
import { WatchlistSummary } from '../../shared/models/watchlist.model';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent implements OnInit {
  private readonly alerts = inject(AlertService);
  private readonly cameras = inject(CameraService);
  private readonly detections = inject(DetectionService);
  private readonly watchlist = inject(WatchlistService);
  private readonly realtime = inject(AlertRealtimeService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly alertSummary = signal<AlertSummary | null>(null);
  protected readonly detectionSummary = signal<DetectionSummary | null>(null);
  protected readonly cameraSummary = signal<CameraSummary | null>(null);
  protected readonly watchlistSummary = signal<WatchlistSummary | null>(null);

  protected readonly connected = this.realtime.connected;
  protected readonly skeleton = [1, 2, 3, 4];

  protected readonly pendingAlerts = computed(() => this.alertSummary()?.newCount ?? 0);

  ngOnInit(): void {
    this.load();
    this.realtime.messages$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((alert) => {
      if (alert.status === 'NEW') {
        this.alertSummary.update((s) => (s ? { ...s, newCount: s.newCount + 1 } : s));
      }
    });
  }

  protected load(): void {
    this.loading.set(true);
    this.error.set(null);
    forkJoin({
      alerts: this.alerts.summary(),
      detections: this.detections.summary(),
      cameras: this.cameras.summary(),
      watchlist: this.watchlist.summary(),
    }).subscribe({
      next: (res) => {
        this.alertSummary.set(res.alerts);
        this.detectionSummary.set(res.detections);
        this.cameraSummary.set(res.cameras);
        this.watchlistSummary.set(res.watchlist);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(this.message(err));
        this.loading.set(false);
      },
    });
  }

  private message(err: HttpErrorResponse): string {
    const body = err.error as { detail?: string; title?: string } | string | null;
    if (body && typeof body === 'object') {
      return body.detail ?? body.title ?? 'Não foi possível carregar o painel.';
    }
    return 'Não foi possível carregar o painel.';
  }
}
