import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Page } from '../../shared/models/page.model';
import {
  WatchedVehicle,
  WatchlistRequest,
  WatchlistUpdateRequest,
} from '../../shared/models/watchlist.model';

@Injectable({ providedIn: 'root' })
export class WatchlistService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/watchlist';

  list(page = 0, size = 20): Observable<Page<WatchedVehicle>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<WatchedVehicle>>(this.base, { params });
  }

  create(request: WatchlistRequest): Observable<WatchedVehicle> {
    return this.http.post<WatchedVehicle>(this.base, request);
  }

  update(id: number, request: WatchlistUpdateRequest): Observable<WatchedVehicle> {
    return this.http.put<WatchedVehicle>(`${this.base}/${id}`, request);
  }

  deactivate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  activate(id: number): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/activate`, {});
  }
}
