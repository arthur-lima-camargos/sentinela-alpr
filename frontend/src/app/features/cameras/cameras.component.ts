import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../../core/auth/auth.service';
import { CameraService } from '../../core/cameras/camera.service';
import { Camera, CameraRequest } from '../../shared/models/camera.model';

@Component({
  selector: 'app-cameras',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './cameras.component.html',
  styleUrl: './cameras.component.scss',
})
export class CamerasComponent implements OnInit {
  private readonly service = inject(CameraService);
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);

  protected readonly cameras = signal<Camera[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly page = signal(0);
  protected readonly totalPages = signal(0);
  protected readonly totalElements = signal(0);

  protected readonly formOpen = signal(false);
  protected readonly editingId = signal<number | null>(null);
  protected readonly saving = signal(false);
  protected readonly formError = signal<string | null>(null);

  protected readonly isAdmin = computed(() => this.auth.currentUser()?.role === 'ADMIN');

  protected readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    latitude: [null as number | null, [Validators.min(-90), Validators.max(90)]],
    longitude: [null as number | null, [Validators.min(-180), Validators.max(180)]],
    road: ['', [Validators.maxLength(120)]],
  });

  ngOnInit(): void {
    this.load(0);
  }

  protected load(page: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.list(page).subscribe({
      next: (res) => {
        this.cameras.set(res.content);
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

  protected openCreate(): void {
    this.editingId.set(null);
    this.formError.set(null);
    this.form.reset({ name: '', latitude: null, longitude: null, road: '' });
    this.formOpen.set(true);
  }

  protected openEdit(camera: Camera): void {
    this.editingId.set(camera.id);
    this.formError.set(null);
    this.form.reset({
      name: camera.name,
      latitude: camera.latitude,
      longitude: camera.longitude,
      road: camera.road ?? '',
    });
    this.formOpen.set(true);
  }

  protected closeForm(): void {
    this.formOpen.set(false);
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.formError.set(null);
    const id = this.editingId();
    const request = this.toRequest();
    const call = id === null ? this.service.create(request) : this.service.update(id, request);
    call.subscribe({
      next: () => {
        this.saving.set(false);
        this.formOpen.set(false);
        this.load(this.page());
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        this.formError.set(this.message(err));
      },
    });
  }

  protected deactivate(camera: Camera): void {
    if (!confirm(`Desativar a câmera "${camera.name}"?`)) {
      return;
    }
    this.service.deactivate(camera.id).subscribe({
      next: () => this.load(this.page()),
      error: (err: HttpErrorResponse) => this.error.set(this.message(err)),
    });
  }

  protected activate(camera: Camera): void {
    this.service.activate(camera.id).subscribe({
      next: () => this.load(this.page()),
      error: (err: HttpErrorResponse) => this.error.set(this.message(err)),
    });
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

  private toRequest(): CameraRequest {
    const v = this.form.getRawValue();
    const road = v.road?.trim();
    return {
      name: (v.name ?? '').trim(),
      latitude: v.latitude ?? null,
      longitude: v.longitude ?? null,
      road: road ? road : null,
    };
  }

  private message(err: HttpErrorResponse): string {
    const body = err.error as { detail?: string; title?: string } | string | null;
    if (body && typeof body === 'object') {
      return body.detail ?? body.title ?? 'Ocorreu um erro. Tente novamente.';
    }
    return 'Ocorreu um erro. Tente novamente.';
  }
}
