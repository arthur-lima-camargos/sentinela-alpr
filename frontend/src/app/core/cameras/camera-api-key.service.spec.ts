import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { CameraApiKeyService } from './camera-api-key.service';

describe('CameraApiKeyService', () => {
  let service: CameraApiKeyService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CameraApiKeyService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CameraApiKeyService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('list faz GET nas chaves da câmera', () => {
    service.list(3).subscribe();
    const req = http.expectOne('/api/v1/cameras/3/api-keys');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('issue faz POST para emitir uma chave', () => {
    service.issue(3).subscribe();
    const req = http.expectOne('/api/v1/cameras/3/api-keys');
    expect(req.request.method).toBe('POST');
    req.flush({ id: 1, cameraId: 3, apiKey: 'alpr_secret', keyPrefix: 'alpr_ab', createdAt: '2026-01-01T00:00:00Z' });
  });

  it('revoke faz DELETE na chave', () => {
    service.revoke(3, 9).subscribe();
    const req = http.expectOne('/api/v1/cameras/3/api-keys/9');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
