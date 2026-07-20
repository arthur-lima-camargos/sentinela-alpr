import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { AlertService } from './alert.service';

describe('AlertService', () => {
  let service: AlertService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AlertService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AlertService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('list sem status envia apenas page e size', () => {
    service.list().subscribe();
    const req = http.expectOne((r) => r.url === '/api/v1/alerts');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('20');
    expect(req.request.params.has('status')).toBe(false);
    req.flush({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 20, first: true, last: true });
  });

  it('list com status inclui o filtro', () => {
    service.list('NEW', 1, 10).subscribe();
    const req = http.expectOne((r) => r.url === '/api/v1/alerts');
    expect(req.request.params.get('status')).toBe('NEW');
    expect(req.request.params.get('page')).toBe('1');
    req.flush({ content: [], totalElements: 0, totalPages: 0, number: 1, size: 10, first: true, last: true });
  });

  it('updateStatus faz PATCH no id com o novo status', () => {
    service.updateStatus(7, 'SEEN').subscribe();
    const req = http.expectOne('/api/v1/alerts/7');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ status: 'SEEN' });
    req.flush({});
  });
});
