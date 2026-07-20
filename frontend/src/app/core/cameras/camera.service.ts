import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Camera, CameraRequest, Page } from '../../shared/models/camera.model';

@Injectable({ providedIn: 'root' })
export class CameraService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/cameras';

  list(page = 0, size = 20): Observable<Page<Camera>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<Camera>>(this.base, { params });
  }

  create(request: CameraRequest): Observable<Camera> {
    return this.http.post<Camera>(this.base, request);
  }

  update(id: number, request: CameraRequest): Observable<Camera> {
    return this.http.put<Camera>(`${this.base}/${id}`, request);
  }

  deactivate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  activate(id: number): Observable<void> {
    return this.http.post<void>(`${this.base}/${id}/activate`, {});
  }
}
