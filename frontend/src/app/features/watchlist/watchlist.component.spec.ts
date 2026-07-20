import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { of, throwError } from 'rxjs';

import { WatchlistComponent } from './watchlist.component';
import { WatchlistService } from '../../core/watchlist/watchlist.service';
import { AuthService } from '../../core/auth/auth.service';
import { WatchedVehicle } from '../../shared/models/watchlist.model';
import { CurrentUser } from '../../core/auth/auth.models';

function pageOf(content: WatchedVehicle[]) {
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

const WV: WatchedVehicle = {
  id: 1,
  plate: 'ABC1D34',
  reason: 'THEFT',
  active: true,
  createdAt: '2026-01-01T00:00:00Z',
};

describe('WatchlistComponent', () => {
  let currentUser: ReturnType<typeof signal<CurrentUser | null>>;
  let serviceStub: {
    list: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
    update: ReturnType<typeof vi.fn>;
    deactivate: ReturnType<typeof vi.fn>;
    activate: ReturnType<typeof vi.fn>;
  };

  function buttonByText(el: HTMLElement, text: string): HTMLButtonElement | undefined {
    return [...el.querySelectorAll('button')].find((b) => b.textContent?.trim().includes(text));
  }

  function selectReason(el: HTMLElement, label: string) {
    const select = el.querySelector('.dialog select') as HTMLSelectElement;
    const option = [...select.options].find((o) => o.textContent?.trim() === label)!;
    select.value = option.value;
    select.dispatchEvent(new Event('change'));
  }

  function typePlate(el: HTMLElement, value: string) {
    const input = el.querySelector('.dialog input[type="text"]') as HTMLInputElement;
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  function create() {
    const fixture = TestBed.createComponent(WatchlistComponent);
    fixture.detectChanges();
    return fixture;
  }

  beforeEach(async () => {
    currentUser = signal<CurrentUser | null>({ login: 'admin', role: 'ADMIN' });
    serviceStub = {
      list: vi.fn(() => of(pageOf([WV]))),
      create: vi.fn(() => of({})),
      update: vi.fn(() => of({})),
      deactivate: vi.fn(() => of(void 0)),
      activate: vi.fn(() => of(void 0)),
    };

    await TestBed.configureTestingModule({
      imports: [WatchlistComponent],
      providers: [
        { provide: WatchlistService, useValue: serviceStub },
        { provide: AuthService, useValue: { currentUser } },
      ],
    }).compileComponents();
  });

  it('carrega e renderiza as placas no init', () => {
    const fixture = create();
    expect(serviceStub.list).toHaveBeenCalledWith(0);
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(1);
    expect(fixture.nativeElement.textContent).toContain('ABC1D34');
    expect(fixture.nativeElement.textContent).toContain('Furto');
  });

  it('mostra ações de escrita para ADMIN', () => {
    expect(create().nativeElement.textContent).toContain('Nova placa');
  });

  it('esconde ações de escrita para OPERATOR', () => {
    currentUser.set({ login: 'op', role: 'OPERATOR' });
    const el = create().nativeElement;
    expect(el.textContent).not.toContain('Nova placa');
    expect(el.querySelector('.table__actions')).toBeNull();
  });

  it('cria chamando o serviço com placa normalizada e motivo', () => {
    const fixture = create();
    const el: HTMLElement = fixture.nativeElement;

    buttonByText(el, 'Nova placa')!.click();
    fixture.detectChanges();

    typePlate(el, 'abc-1d34');
    selectReason(el, 'Roubo');
    fixture.detectChanges();

    buttonByText(el, 'Salvar')!.click();

    expect(serviceStub.create).toHaveBeenCalledTimes(1);
    expect(serviceStub.create.mock.calls[0][0]).toMatchObject({ plate: 'ABC1D34', reason: 'ROBBERY' });
  });

  it('não cria quando a placa é inválida', () => {
    const fixture = create();
    const el: HTMLElement = fixture.nativeElement;

    buttonByText(el, 'Nova placa')!.click();
    fixture.detectChanges();

    typePlate(el, 'XX');
    selectReason(el, 'Roubo');
    fixture.detectChanges();

    buttonByText(el, 'Salvar')!.click();

    expect(serviceStub.create).not.toHaveBeenCalled();
  });

  it('não cria quando falta o motivo', () => {
    const fixture = create();
    const el: HTMLElement = fixture.nativeElement;

    buttonByText(el, 'Nova placa')!.click();
    fixture.detectChanges();

    typePlate(el, 'ABC1D34');
    fixture.detectChanges();

    buttonByText(el, 'Salvar')!.click();

    expect(serviceStub.create).not.toHaveBeenCalled();
  });

  it('edita chamando update apenas com o novo motivo', () => {
    const fixture = create();
    const el: HTMLElement = fixture.nativeElement;

    buttonByText(el, 'Editar')!.click();
    fixture.detectChanges();

    selectReason(el, 'Procurado');
    fixture.detectChanges();

    buttonByText(el, 'Salvar')!.click();

    expect(serviceStub.update).toHaveBeenCalledWith(1, { reason: 'WANTED' });
    expect(serviceStub.create).not.toHaveBeenCalled();
  });

  it('reativa um monitoramento inativo chamando activate', () => {
    serviceStub.list = vi.fn(() => of(pageOf([{ ...WV, active: false }])));
    const el = create().nativeElement;

    buttonByText(el, 'Reativar')!.click();

    expect(serviceStub.activate).toHaveBeenCalledWith(1);
  });

  it('desativa após confirmação', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    const el = create().nativeElement;

    buttonByText(el, 'Desativar')!.click();

    expect(serviceStub.deactivate).toHaveBeenCalledWith(1);
  });

  it('não desativa quando a confirmação é cancelada', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false);
    const el = create().nativeElement;

    buttonByText(el, 'Desativar')!.click();

    expect(serviceStub.deactivate).not.toHaveBeenCalled();
  });

  it('mostra mensagem de erro quando a listagem falha', () => {
    serviceStub.list = vi.fn(() => throwError(() => ({ error: { detail: 'Falha ao carregar' } })));
    const el = create().nativeElement;

    expect(el.textContent).toContain('Falha ao carregar');
  });
});
