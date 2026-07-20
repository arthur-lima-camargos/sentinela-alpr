import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';

import { AlertService } from '../../core/alerts/alert.service';
import { AlertRealtimeService } from '../../core/alerts/alert-realtime.service';
import { WatchlistService } from '../../core/watchlist/watchlist.service';
import { Alert, AlertStatus } from '../../shared/models/alert.model';
import { WatchReason, watchReasonLabel } from '../../shared/models/watchlist.model';

@Component({
  selector: 'app-alerts',
  imports: [DatePipe],
  templateUrl: './alerts.component.html',
  styleUrl: './alerts.component.scss',
})
export class AlertsComponent implements OnInit {
  private readonly service = inject(AlertService);
  private readonly realtime = inject(AlertRealtimeService);
  private readonly watchlist = inject(WatchlistService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly items = signal<Alert[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly page = signal(0);
  protected readonly totalPages = signal(0);
  protected readonly totalElements = signal(0);

  protected readonly statusFilter = signal<AlertStatus | null>(null);
  protected readonly liveIds = signal<Set<number>>(new Set());

  private readonly reasons = signal<Map<number, WatchReason>>(new Map());

  protected readonly connected = this.realtime.connected;

  ngOnInit(): void {
    this.realtime.resetLiveCount();
    this.loadReasons();
    this.load(0);
    this.realtime.messages$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((a) => this.onLive(a));
  }

  protected reasonFor(watchedVehicleId: number): string {
    const reason = this.reasons().get(watchedVehicleId);
    return reason ? watchReasonLabel(reason) : '—';
  }

  protected isLive(id: number): boolean {
    return this.liveIds().has(id);
  }

  protected setFilter(status: AlertStatus | null): void {
    if (status === this.statusFilter()) {
      return;
    }
    this.statusFilter.set(status);
    this.load(0);
  }

  protected load(page: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.liveIds.set(new Set());
    this.service.list(this.statusFilter(), page).subscribe({
      next: (res) => {
        this.items.set(res.content);
        this.page.set(res.number);
        this.totalPages.set(res.totalPages);
        this.totalElements.set(res.totalElements);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(this.message(err));
        this.loading.set(false);
      },
    });
  }

  protected markSeen(alert: Alert): void {
    this.changeStatus(alert, 'SEEN');
  }

  protected reopen(alert: Alert): void {
    this.changeStatus(alert, 'NEW');
  }

  protected prev(): void {
    if (this.page() > 0) {
      this.load(this.page() - 1);
    }
  }

  protected next(): void {
    if (this.page() + 1 < this.totalPages()) {
      this.load(this.page() + 1);
    }
  }

  private changeStatus(alert: Alert, status: AlertStatus): void {
    this.service.updateStatus(alert.id, status).subscribe({
      next: (updated) => this.items.update((list) => list.map((a) => (a.id === updated.id ? updated : a))),
      error: (err: HttpErrorResponse) => this.error.set(this.message(err)),
    });
  }

  private onLive(alert: Alert): void {
    const filter = this.statusFilter();
    if (filter !== null && alert.status !== filter) {
      return;
    }
    let added = false;
    this.items.update((list) => {
      if (list.some((a) => a.id === alert.id)) {
        return list;
      }
      added = true;
      return [alert, ...list];
    });
    if (added) {
      this.liveIds.update((s) => new Set(s).add(alert.id));
      this.totalElements.update((n) => n + 1);
    }
  }

  private loadReasons(): void {
    this.watchlist.list(0, 1000).subscribe({
      next: (res) => this.reasons.set(new Map(res.content.map((w) => [w.id, w.reason]))),
      error: () => this.reasons.set(new Map()),
    });
  }

  private message(err: HttpErrorResponse): string {
    const body = err.error as { detail?: string; title?: string } | string | null;
    if (body && typeof body === 'object') {
      return body.detail ?? body.title ?? 'Ocorreu um erro. Tente novamente.';
    }
    return 'Ocorreu um erro. Tente novamente.';
  }
}
