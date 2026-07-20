import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { WatchlistService } from './watchlist.service';

describe('WatchlistService', () => {
  let service: WatchlistService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [WatchlistService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(WatchlistService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('list envia page e size', () => {
    service.list(3, 15).subscribe();
    const req = http.expectOne((r) => r.url === '/api/v1/watchlist');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('3');
    expect(req.request.params.get('size')).toBe('15');
    req.flush({ content: [], totalElements: 0, totalPages: 0, number: 3, size: 15, first: true, last: true });
  });

  it('create faz POST no recurso base', () => {
    service.create({ plate: 'ABC1D34', reason: 'THEFT', active: true }).subscribe();
    const req = http.expectOne('/api/v1/watchlist');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.plate).toBe('ABC1D34');
    req.flush({});
  });

  it('update faz PUT no id enviando só o motivo', () => {
    service.update(5, { reason: 'WANTED' }).subscribe();
    const req = http.expectOne('/api/v1/watchlist/5');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ reason: 'WANTED' });
    req.flush({});
  });

  it('deactivate faz DELETE no id', () => {
    service.deactivate(7).subscribe();
    const req = http.expectOne('/api/v1/watchlist/7');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('activate faz POST em /{id}/activate', () => {
    service.activate(9).subscribe();
    const req = http.expectOne('/api/v1/watchlist/9/activate');
    expect(req.request.method).toBe('POST');
    req.flush(null);
  });
});
