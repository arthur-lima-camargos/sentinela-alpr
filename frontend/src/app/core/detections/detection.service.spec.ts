import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { DetectionService } from './detection.service';

describe('DetectionService', () => {
  let service: DetectionService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DetectionService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(DetectionService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envia apenas os filtros presentes', () => {
    service.query({ plate: 'ABC1D34', size: 20 }).subscribe();
    const req = http.expectOne((r) => r.url === '/api/v1/detections');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('plate')).toBe('ABC1D34');
    expect(req.request.params.get('size')).toBe('20');
    expect(req.request.params.has('cameraId')).toBe(false);
    expect(req.request.params.has('cursor')).toBe(false);
    req.flush({ content: [], nextCursor: null });
  });

  it('inclui cameraId, período e cursor quando informados', () => {
    service
      .query({ cameraId: 5, from: '2026-01-01T00:00:00.000Z', to: '2026-01-02T00:00:00.000Z', cursor: 'abc' })
      .subscribe();
    const req = http.expectOne((r) => r.url === '/api/v1/detections');
    expect(req.request.params.get('cameraId')).toBe('5');
    expect(req.request.params.get('from')).toBe('2026-01-01T00:00:00.000Z');
    expect(req.request.params.get('to')).toBe('2026-01-02T00:00:00.000Z');
    expect(req.request.params.get('cursor')).toBe('abc');
    req.flush({ content: [], nextCursor: null });
  });

  it('não envia plate vazia', () => {
    service.query({ plate: null }).subscribe();
    const req = http.expectOne((r) => r.url === '/api/v1/detections');
    expect(req.request.params.has('plate')).toBe(false);
    req.flush({ content: [], nextCursor: null });
  });
});
