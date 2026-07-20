import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../../core/auth/auth.service';
import { WatchlistService } from '../../core/watchlist/watchlist.service';
import {
  PLATE_REGEX,
  WATCH_REASONS,
  WatchReason,
  WatchedVehicle,
  normalizePlate,
  watchReasonLabel,
} from '../../shared/models/watchlist.model';

function plateValidator(control: AbstractControl): ValidationErrors | null {
  const value = (control.value as string | null) ?? '';
  if (value.trim() === '') {
    return null; 
  }
  return PLATE_REGEX.test(normalizePlate(value)) ? null : { plate: true };
}

@Component({
  selector: 'app-watchlist',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './watchlist.component.html',
  styleUrl: './watchlist.component.scss',
})
export class WatchlistComponent implements OnInit {
  private readonly service = inject(WatchlistService);
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);

  protected readonly reasons = WATCH_REASONS;
  protected readonly reasonLabel = watchReasonLabel;

  protected readonly items = signal<WatchedVehicle[]>([]);
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
    plate: ['', [Validators.required, plateValidator]],
    reason: [null as WatchReason | null, [Validators.required]],
    active: [true],
  });

  ngOnInit(): void {
    this.load(0);
  }

  protected load(page: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.list(page).subscribe({
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

  protected openCreate(): void {
    this.editingId.set(null);
    this.formError.set(null);
    this.form.reset({ plate: '', reason: null, active: true });
    this.form.controls.plate.enable();
    this.formOpen.set(true);
  }

  protected openEdit(item: WatchedVehicle): void {
    this.editingId.set(item.id);
    this.formError.set(null);
    this.form.reset({ plate: item.plate, reason: item.reason, active: item.active });
    this.form.controls.plate.disable();
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
    const v = this.form.getRawValue();
    const reason = v.reason as WatchReason;
    const call =
      id === null
        ? this.service.create({ plate: normalizePlate(v.plate ?? ''), reason, active: v.active ?? true })
        : this.service.update(id, { reason });
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

  protected deactivate(item: WatchedVehicle): void {
    if (!confirm(`Desativar o monitoramento da placa ${item.plate}?`)) {
      return;
    }
    this.service.deactivate(item.id).subscribe({
      next: () => this.load(this.page()),
      error: (err: HttpErrorResponse) => this.error.set(this.message(err)),
    });
  }

  protected activate(item: WatchedVehicle): void {
    this.service.activate(item.id).subscribe({
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

  private message(err: HttpErrorResponse): string {
    const body = err.error as { detail?: string; title?: string } | string | null;
    if (body && typeof body === 'object') {
      return body.detail ?? body.title ?? 'An error occurs. Try again.';
    }
    return 'An error occurs. Try again.';
  }
}
