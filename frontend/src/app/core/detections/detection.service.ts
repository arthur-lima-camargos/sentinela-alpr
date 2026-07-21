import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { DetectionPage, DetectionQuery, DetectionSummary } from '../../shared/models/detection.model';

@Injectable({ providedIn: 'root' })
export class DetectionService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/detections';

  query(q: DetectionQuery): Observable<DetectionPage> {
    let params = new HttpParams();
    if (q.plate) {
      params = params.set('plate', q.plate);
    }
    if (q.cameraId != null) {
      params = params.set('cameraId', q.cameraId);
    }
    if (q.from) {
      params = params.set('from', q.from);
    }
    if (q.to) {
      params = params.set('to', q.to);
    }
    if (q.cursor) {
      params = params.set('cursor', q.cursor);
    }
    if (q.size) {
      params = params.set('size', q.size);
    }
    return this.http.get<DetectionPage>(this.base, { params });
  }

  summary(): Observable<DetectionSummary> {
    return this.http.get<DetectionSummary>(`${this.base}/summary`);
  }
}
