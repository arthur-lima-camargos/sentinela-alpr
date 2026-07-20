import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { CameraService } from './camera.service';

describe('CameraService', () => {
  let service: CameraService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CameraService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CameraService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('list envia page e size', () => {
    service.list(2, 10).subscribe();
    const req = http.expectOne((r) => r.url === '/api/v1/cameras');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('size')).toBe('10');
    req.flush({ content: [], totalElements: 0, totalPages: 0, number: 2, size: 10, first: true, last: true });
  });

  it('create faz POST no recurso base', () => {
    service.create({ name: 'Portal Norte', latitude: null, longitude: null, road: null }).subscribe();
    const req = http.expectOne('/api/v1/cameras');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.name).toBe('Portal Norte');
    req.flush({});
  });

  it('update faz PUT no id', () => {
    service.update(5, { name: 'Y', latitude: null, longitude: null, road: null }).subscribe();
    const req = http.expectOne('/api/v1/cameras/5');
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('deactivate faz DELETE no id', () => {
    service.deactivate(7).subscribe();
    const req = http.expectOne('/api/v1/cameras/7');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('activate faz POST em /{id}/activate', () => {
    service.activate(9).subscribe();
    const req = http.expectOne('/api/v1/cameras/9/activate');
    expect(req.request.method).toBe('POST');
    req.flush(null);
  });
});
