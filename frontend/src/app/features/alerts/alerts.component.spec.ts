import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { Subject, of, throwError } from 'rxjs';

import { AlertsComponent } from './alerts.component';
import { AlertService } from '../../core/alerts/alert.service';
import { AlertRealtimeService } from '../../core/alerts/alert-realtime.service';
import { WatchlistService } from '../../core/watchlist/watchlist.service';
import { Alert } from '../../shared/models/alert.model';
import { WatchedVehicle } from '../../shared/models/watchlist.model';

function alertPage(content: Alert[]) {
  return {
    content,
    totalElements: content.length,
    totalPages: 1,
    number: 0,
    size: 20,
    first: true,
    last: true,
  };
}

function wvPage(content: WatchedVehicle[]) {
  return { content, totalElements: content.length, totalPages: 1, number: 0, size: 1000, first: true, last: true };
}

const WV: WatchedVehicle = { id: 5, plate: 'ABC1D34', reason: 'ROBBERY', active: true, createdAt: '2026-01-01T00:00:00Z' };

const AL: Alert = {
  id: 10,
  plate: 'ABC1D34',
  detectionId: 1,
  watchedVehicleId: 5,
  detectedAt: '2026-01-01T10:00:00Z',
  status: 'NEW',
  createdAt: '2026-01-01T10:00:01Z',
};

describe('AlertsComponent', () => {
  let alertStub: { list: ReturnType<typeof vi.fn>; updateStatus: ReturnType<typeof vi.fn> };
  let watchStub: { list: ReturnType<typeof vi.fn> };
  let messages$: Subject<Alert>;
  let realtimeStub: {
    messages$: Subject<Alert>;
    connected: ReturnType<typeof signal<boolean>>;
    liveCount: ReturnType<typeof signal<number>>;
    resetLiveCount: ReturnType<typeof vi.fn>;
  };

  function buttonByText(el: HTMLElement, text: string): HTMLButtonElement | undefined {
    return [...el.querySelectorAll('button')].find((b) => b.textContent?.trim().includes(text));
  }

  function create() {
    const fixture = TestBed.createComponent(AlertsComponent);
    fixture.detectChanges();
    return fixture;
  }

  beforeEach(async () => {
    messages$ = new Subject<Alert>();
    alertStub = {
      list: vi.fn(() => of(alertPage([AL]))),
      updateStatus: vi.fn((id: number, status: 'NEW' | 'SEEN') => of({ ...AL, id, status })),
    };
    watchStub = { list: vi.fn(() => of(wvPage([WV]))) };
    realtimeStub = {
      messages$,
      connected: signal(true),
      liveCount: signal(0),
      resetLiveCount: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [AlertsComponent],
      providers: [
        { provide: AlertService, useValue: alertStub },
        { provide: WatchlistService, useValue: watchStub },
        { provide: AlertRealtimeService, useValue: realtimeStub },
      ],
    }).compileComponents();
  });

  it('carrega e renderiza os alertas, resolvendo o motivo', () => {
    const el = create().nativeElement;
    expect(alertStub.list).toHaveBeenCalledWith(null, 0);
    expect(el.querySelectorAll('tbody tr').length).toBe(1);
    expect(el.textContent).toContain('ABC1D34');
    expect(el.textContent).toContain('Roubo');
  });

  it('zera o contador ao vivo ao abrir a tela', () => {
    create();
    expect(realtimeStub.resetLiveCount).toHaveBeenCalled();
  });

  it('filtra por status ao clicar em Novos', () => {
    const fixture = create();
    buttonByText(fixture.nativeElement, 'Novos')!.click();
    expect(alertStub.list).toHaveBeenLastCalledWith('NEW', 0);
  });

  it('marca como visto chamando updateStatus e atualiza a linha', () => {
    const fixture = create();
    buttonByText(fixture.nativeElement, 'Marcar como visto')!.click();
    fixture.detectChanges();
    expect(alertStub.updateStatus).toHaveBeenCalledWith(10, 'SEEN');
    expect(buttonByText(fixture.nativeElement, 'Reabrir')).toBeTruthy();
  });

  it('insere ao vivo um alerta que chega pelo STOMP, no topo', () => {
    const fixture = create();
    const incoming: Alert = { ...AL, id: 11, plate: 'XYZ2A11' };

    messages$.next(incoming);
    fixture.detectChanges();

    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(2);
    expect(rows[0].textContent).toContain('XYZ2A11');
    expect(rows[0].classList.contains('row--live')).toBe(true);
  });

  it('não duplica um alerta ao vivo já presente na lista', () => {
    const fixture = create();
    messages$.next(AL); // mesmo id do que já veio do servidor
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(1);
  });

  it('ignora alerta ao vivo que não casa com o filtro atual', () => {
    const fixture = create();
    buttonByText(fixture.nativeElement, 'Vistos')!.click(); // filtro SEEN
    fixture.detectChanges();

    messages$.next({ ...AL, id: 12, status: 'NEW' }); // NEW não casa com SEEN
    fixture.detectChanges();

    const rows = [...fixture.nativeElement.querySelectorAll('tbody tr')];
    expect(rows.some((r) => r.textContent?.includes('id 12'))).toBe(false);
    expect(rows.length).toBe(1);
  });

  it('esconde a paginação quando há só uma página', () => {
    const el = create().nativeElement;
    expect(el.textContent).not.toContain('Página');
  });

  it('mostra a paginação quando há mais de uma página', () => {
    alertStub.list = vi.fn(() => of({ ...alertPage([AL]), totalPages: 3 }));
    const el = create().nativeElement;
    expect(el.textContent).toContain('Página 1 de 3');
  });

  it('mostra mensagem de erro quando a listagem falha', () => {
    alertStub.list = vi.fn(() => throwError(() => ({ error: { detail: 'Falha nos alertas' } })));
    const el = create().nativeElement;
    expect(el.textContent).toContain('Falha nos alertas');
  });
});
