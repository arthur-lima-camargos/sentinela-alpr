import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { provideRouter } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';

import { HomeComponent } from './home.component';
import { AlertService } from '../../core/alerts/alert.service';
import { CameraService } from '../../core/cameras/camera.service';
import { DetectionService } from '../../core/detections/detection.service';
import { WatchlistService } from '../../core/watchlist/watchlist.service';
import { AlertRealtimeService } from '../../core/alerts/alert-realtime.service';
import { Alert, AlertSummary } from '../../shared/models/alert.model';
import { CameraSummary } from '../../shared/models/camera.model';
import { DetectionSummary } from '../../shared/models/detection.model';
import { WatchlistSummary } from '../../shared/models/watchlist.model';

const ALERTS: AlertSummary = { newCount: 3, seenCount: 7 };
const DETECTIONS: DetectionSummary = { lastHour: 12, last24h: 340 };
const CAMERAS: CameraSummary = { active: 5, inactive: 2 };
const WATCHLIST: WatchlistSummary = { active: 9 };

describe('HomeComponent', () => {
  let alertStub: { summary: ReturnType<typeof vi.fn> };
  let cameraStub: { summary: ReturnType<typeof vi.fn> };
  let detectionStub: { summary: ReturnType<typeof vi.fn> };
  let watchlistStub: { summary: ReturnType<typeof vi.fn> };
  let messages$: Subject<Alert>;
  let realtimeStub: { messages$: Subject<Alert>; connected: ReturnType<typeof signal<boolean>> };

  function create() {
    const fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();
    return fixture;
  }

  function card(el: HTMLElement, rotulo: string): HTMLElement | undefined {
    return [...el.querySelectorAll<HTMLElement>('.cartao')].find((c) =>
      c.querySelector('.cartao__rotulo')?.textContent?.includes(rotulo),
    );
  }

  beforeEach(async () => {
    messages$ = new Subject<Alert>();
    alertStub = { summary: vi.fn(() => of(ALERTS)) };
    cameraStub = { summary: vi.fn(() => of(CAMERAS)) };
    detectionStub = { summary: vi.fn(() => of(DETECTIONS)) };
    watchlistStub = { summary: vi.fn(() => of(WATCHLIST)) };
    realtimeStub = { messages$, connected: signal(true) };

    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [
        provideRouter([]),
        { provide: AlertService, useValue: alertStub },
        { provide: CameraService, useValue: cameraStub },
        { provide: DetectionService, useValue: detectionStub },
        { provide: WatchlistService, useValue: watchlistStub },
        { provide: AlertRealtimeService, useValue: realtimeStub },
      ],
    }).compileComponents();
  });

  it('carrega e renderiza os quatro cartões com as métricas', () => {
    const el = create().nativeElement as HTMLElement;
    expect(alertStub.summary).toHaveBeenCalled();
    expect(card(el, 'Alertas pendentes')!.querySelector('.cartao__numero')!.textContent).toContain('3');
    expect(card(el, 'Alertas pendentes')!.textContent).toContain('7 já tratados');
    expect(card(el, 'Passagens')!.querySelector('.cartao__numero')!.textContent).toContain('340');
    expect(card(el, 'Passagens')!.textContent).toContain('12 na última hora');
    expect(card(el, 'Câmeras')!.querySelector('.cartao__numero')!.textContent).toContain('5');
    expect(card(el, 'Câmeras')!.textContent).toContain('2 inativas');
    expect(card(el, 'monitorados')!.querySelector('.cartao__numero')!.textContent).toContain('9');
  });

  it('destaca como crítico o cartão de alertas quando há pendentes', () => {
    const el = create().nativeElement as HTMLElement;
    expect(card(el, 'Alertas pendentes')!.classList.contains('cartao--critico')).toBe(true);
  });

  it('não destaca o cartão de alertas quando não há pendentes', () => {
    alertStub.summary = vi.fn(() => of({ newCount: 0, seenCount: 4 }));
    const el = create().nativeElement as HTMLElement;
    expect(card(el, 'Alertas pendentes')!.classList.contains('cartao--critico')).toBe(false);
  });

  it('incrementa os pendentes ao receber um alerta NEW pelo STOMP', () => {
    const fixture = create();
    messages$.next({
      id: 99,
      plate: 'XYZ2A11',
      detectionId: 1,
      watchedVehicleId: 1,
      detectedAt: '2026-01-01T00:00:00Z',
      status: 'NEW',
      createdAt: '2026-01-01T00:00:01Z',
    });
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    expect(card(el, 'Alertas pendentes')!.querySelector('.cartao__numero')!.textContent).toContain('4');
  });

  it('mostra mensagem de erro e permite tentar de novo quando o carregamento falha', () => {
    detectionStub.summary = vi.fn(() => throwError(() => ({ error: { detail: 'Falha no painel' } })));
    const fixture = create();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain('Falha no painel');

    detectionStub.summary = vi.fn(() => of(DETECTIONS));
    [...el.querySelectorAll('button')].find((b) => b.textContent?.includes('Tentar de novo'))!.click();
    fixture.detectChanges();
    expect(card(el, 'Passagens')!.querySelector('.cartao__numero')!.textContent).toContain('340');
  });
});
