import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Alert, AlertStatus } from '../../shared/models/alert.model';
import { Page } from '../../shared/models/page.model';

@Injectable({ providedIn: 'root' })
export class AlertService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/alerts';

  list(status: AlertStatus | null = null, page = 0, size = 20): Observable<Page<Alert>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<Page<Alert>>(this.base, { params });
  }

  updateStatus(id: number, status: AlertStatus): Observable<Alert> {
    return this.http.patch<Alert>(`${this.base}/${id}`, { status });
  }
}
