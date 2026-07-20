import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApiKey, IssuedApiKey } from '../../shared/models/camera-api-key.model';

@Injectable({ providedIn: 'root' })
export class CameraApiKeyService {
  private readonly http = inject(HttpClient);

  private base(cameraId: number): string {
    return `/api/v1/cameras/${cameraId}/api-keys`;
  }

  list(cameraId: number): Observable<ApiKey[]> {
    return this.http.get<ApiKey[]>(this.base(cameraId));
  }

  issue(cameraId: number): Observable<IssuedApiKey> {
    return this.http.post<IssuedApiKey>(this.base(cameraId), {});
  }

  revoke(cameraId: number, keyId: number): Observable<void> {
    return this.http.delete<void>(`${this.base(cameraId)}/${keyId}`);
  }
}
