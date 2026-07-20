import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { DetectionsComponent } from './detections.component';
import { DetectionService } from '../../core/detections/detection.service';
import { CameraService } from '../../core/cameras/camera.service';
import { Detection, DetectionPage } from '../../shared/models/detection.model';
import { Camera } from '../../shared/models/camera.model';

function detPage(content: Detection[], nextCursor: string | null): DetectionPage {
  return { content, nextCursor };
}

function camPage(content: Camera[]) {
  return {
    content,
    totalElements: content.length,
    totalPages: 1,
    number: 0,
    size: 1000,
    first: true,
    last: true,
  };
}

const CAM: Camera = {
  id: 5,
  name: 'Portal Norte',
  latitude: null,
  longitude: null,
  road: null,
  active: true,
  createdAt: '2026-01-01T00:00:00Z',
};

const D1: Detection = { id: 1, plate: 'ABC1D34', cameraId: 5, detectedAt: '2026-01-01T10:00:00Z' };
const D2: Detection = { id: 2, plate: 'XYZ2A11', cameraId: 99, detectedAt: '2026-01-01T09:00:00Z' };

describe('DetectionsComponent', () => {
  let detStub: { query: ReturnType<typeof vi.fn> };
  let camStub: { list: ReturnType<typeof vi.fn> };

  function buttonByText(el: HTMLElement, text: string): HTMLButtonElement | undefined {
    return [...el.querySelectorAll('button')].find((b) => b.textContent?.trim().includes(text));
  }

  function create() {
    const fixture = TestBed.createComponent(DetectionsComponent);
    fixture.detectChanges();
    return fixture;
  }

  beforeEach(async () => {
    detStub = { query: vi.fn(() => of(detPage([D1], 'cursor1'))) };
    camStub = { list: vi.fn(() => of(camPage([CAM]))) };

    await TestBed.configureTestingModule({
      imports: [DetectionsComponent],
      providers: [
        { provide: DetectionService, useValue: detStub },
        { provide: CameraService, useValue: camStub },
      ],
    }).compileComponents();
  });

  it('carrega câmeras e passagens no init', () => {
    const fixture = create();
    expect(camStub.list).toHaveBeenCalled();
    expect(detStub.query).toHaveBeenCalledTimes(1);
    expect(detStub.query.mock.calls[0][0]).toMatchObject({ cursor: null, size: 20 });
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(1);
    expect(fixture.nativeElement.textContent).toContain('ABC1D34');
  });

  it('resolve o nome da câmera pela lista carregada', () => {
    const el = create().nativeElement;
    expect(el.textContent).toContain('Portal Norte');
  });

  it('mostra "Câmera #id" quando a câmera não está na lista', () => {
    detStub.query = vi.fn(() => of(detPage([D2], null)));
    const el = create().nativeElement;
    expect(el.textContent).toContain('Câmera #99');
  });

  it('busca aplicando os filtros com placa normalizada e câmera', () => {
    const fixture = create();
    const el: HTMLElement = fixture.nativeElement;

    const plate = el.querySelector('.filters input[type="text"]') as HTMLInputElement;
    plate.value = 'abc-1d34';
    plate.dispatchEvent(new Event('input'));

    const select = el.querySelector('.filters select') as HTMLSelectElement;
    select.value = [...select.options].find((o) => o.textContent?.trim() === 'Portal Norte')!.value;
    select.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    buttonByText(el, 'Buscar')!.click();

    const last = detStub.query.mock.calls.at(-1)![0];
    expect(last).toMatchObject({ plate: 'ABC1D34', cameraId: 5, cursor: null });
  });

  it('carregar mais anexa a próxima leva usando o cursor', () => {
    detStub.query = vi
      .fn()
      .mockReturnValueOnce(of(detPage([D1], 'cursor1')))
      .mockReturnValueOnce(of(detPage([D2], null)));
    const fixture = create();
    const el: HTMLElement = fixture.nativeElement;

    buttonByText(el, 'Carregar mais')!.click();
    fixture.detectChanges();

    expect(detStub.query.mock.calls[1][0]).toMatchObject({ cursor: 'cursor1' });
    expect(el.querySelectorAll('tbody tr').length).toBe(2);
    expect(el.textContent).toContain('Fim dos resultados');
  });

  it('não mostra "Carregar mais" quando não há próximo cursor', () => {
    detStub.query = vi.fn(() => of(detPage([D1], null)));
    const el = create().nativeElement;
    expect(buttonByText(el, 'Carregar mais')).toBeUndefined();
    expect(el.textContent).toContain('Fim dos resultados');
  });

  it('limpar reseta os filtros e busca de novo', () => {
    const fixture = create();
    const el: HTMLElement = fixture.nativeElement;

    const plate = el.querySelector('.filters input[type="text"]') as HTMLInputElement;
    plate.value = 'ABC1D34';
    plate.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    buttonByText(el, 'Limpar')!.click();

    expect(plate.value).toBe('');
    const last = detStub.query.mock.calls.at(-1)![0];
    expect(last).toMatchObject({ plate: null });
  });

  it('mostra mensagem de erro quando a busca falha', () => {
    detStub.query = vi.fn(() => throwError(() => ({ error: { detail: 'Falha na consulta' } })));
    const el = create().nativeElement;
    expect(el.textContent).toContain('Falha na consulta');
  });
});
