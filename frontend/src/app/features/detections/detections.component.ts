import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { CameraService } from '../../core/cameras/camera.service';
import { DetectionService } from '../../core/detections/detection.service';
import { Camera } from '../../shared/models/camera.model';
import { Detection, DetectionQuery } from '../../shared/models/detection.model';
import { normalizePlate } from '../../shared/models/plate';

const PAGE_SIZE = 20;

@Component({
  selector: 'app-detections',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './detections.component.html',
  styleUrl: './detections.component.scss',
})
export class DetectionsComponent implements OnInit {
  private readonly service = inject(DetectionService);
  private readonly cameraService = inject(CameraService);
  private readonly fb = inject(FormBuilder);

  protected readonly items = signal<Detection[]>([]);
  protected readonly nextCursor = signal<string | null>(null);
  protected readonly loading = signal(false);
  protected readonly loadingMore = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly cameras = signal<Camera[]>([]);
  private readonly cameraNames = computed(() => {
    const map = new Map<number, string>();
    for (const c of this.cameras()) {
      map.set(c.id, c.name);
    }
    return map;
  });

  protected readonly hasMore = computed(() => this.nextCursor() !== null);

  protected readonly form = this.fb.group({
    plate: [''],
    cameraId: [null as number | null],
    from: [''],
    to: [''],
  });

  ngOnInit(): void {
    this.loadCameras();
    this.search();
  }

  protected cameraName(id: number): string {
    return this.cameraNames().get(id) ?? `Câmera #${id}`;
  }

  protected search(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.query(this.buildQuery(null)).subscribe({
      next: (page) => {
        this.items.set(page.content);
        this.nextCursor.set(page.nextCursor);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(this.message(err));
        this.loading.set(false);
      },
    });
  }

  protected loadMore(): void {
    const cursor = this.nextCursor();
    if (cursor === null || this.loadingMore()) {
      return;
    }
    this.loadingMore.set(true);
    this.service.query(this.buildQuery(cursor)).subscribe({
      next: (page) => {
        this.items.update((current) => [...current, ...page.content]);
        this.nextCursor.set(page.nextCursor);
        this.loadingMore.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(this.message(err));
        this.loadingMore.set(false);
      },
    });
  }

  protected clear(): void {
    this.form.reset({ plate: '', cameraId: null, from: '', to: '' });
    this.search();
  }

  private buildQuery(cursor: string | null): DetectionQuery {
    const v = this.form.getRawValue();
    const plate = v.plate?.trim() ? normalizePlate(v.plate) : null;
    return {
      plate,
      cameraId: v.cameraId ?? null,
      from: this.toInstant(v.from),
      to: this.toInstant(v.to),
      cursor,
      size: PAGE_SIZE,
    };
  }

  private toInstant(local: string | null | undefined): string | null {
    if (!local) {
      return null;
    }
    const date = new Date(local);
    return isNaN(date.getTime()) ? null : date.toISOString();
  }

  private loadCameras(): void {
    this.cameraService.list(0, 1000).subscribe({
      next: (page) => this.cameras.set(page.content),
      error: () => this.cameras.set([]),
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
