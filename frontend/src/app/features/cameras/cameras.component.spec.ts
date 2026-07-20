import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { of, throwError } from 'rxjs';

import { CamerasComponent } from './cameras.component';
import { CameraService } from '../../core/cameras/camera.service';
import { AuthService } from '../../core/auth/auth.service';
import { Camera } from '../../shared/models/camera.model';
import { CurrentUser } from '../../core/auth/auth.models';

function pageOf(content: Camera[]) {
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

const CAM: Camera = {
  id: 1,
  name: 'Portal Norte',
  latitude: null,
  longitude: null,
  road: 'BR-101',
  active: true,
  createdAt: '2026-01-01T00:00:00Z',
};

describe('CamerasComponent', () => {
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

  function create() {
    const fixture = TestBed.createComponent(CamerasComponent);
    fixture.detectChanges();
    return fixture;
  }

  beforeEach(async () => {
    currentUser = signal<CurrentUser | null>({ login: 'admin', role: 'ADMIN' });
    serviceStub = {
      list: vi.fn(() => of(pageOf([CAM]))),
      create: vi.fn(() => of({})),
      update: vi.fn(() => of({})),
      deactivate: vi.fn(() => of(void 0)),
      activate: vi.fn(() => of(void 0)),
    };

    await TestBed.configureTestingModule({
      imports: [CamerasComponent],
      providers: [
        { provide: CameraService, useValue: serviceStub },
        { provide: AuthService, useValue: { currentUser } },
      ],
    }).compileComponents();
  });

  it('carrega e renderiza as câmeras no init', () => {
    const fixture = create();
    expect(serviceStub.list).toHaveBeenCalledWith(0);
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(1);
    expect(fixture.nativeElement.textContent).toContain('Portal Norte');
  });

  it('mostra ações de escrita para ADMIN', () => {
    expect(create().nativeElement.textContent).toContain('Nova câmera');
  });

  it('esconde ações de escrita para OPERATOR', () => {
    currentUser.set({ login: 'op', role: 'OPERATOR' });
    const el = create().nativeElement;
    expect(el.textContent).not.toContain('Nova câmera');
    expect(el.querySelector('.table__actions')).toBeNull();
  });

  it('cria chamando o serviço com o payload quando válido', () => {
    const fixture = create();
    const el: HTMLElement = fixture.nativeElement;

    buttonByText(el, 'Nova câmera')!.click();
    fixture.detectChanges();

    const nameInput = el.querySelectorAll('.dialog input')[0] as HTMLInputElement;
    nameInput.value = 'Cam Nova';
    nameInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    buttonByText(el, 'Salvar')!.click();

    expect(serviceStub.create).toHaveBeenCalledTimes(1);
    expect(serviceStub.create.mock.calls[0][0]).toMatchObject({ name: 'Cam Nova' });
  });

  it('não cria quando o nome está vazio', () => {
    const fixture = create();
    const el: HTMLElement = fixture.nativeElement;

    buttonByText(el, 'Nova câmera')!.click();
    fixture.detectChanges();
    buttonByText(el, 'Salvar')!.click();

    expect(serviceStub.create).not.toHaveBeenCalled();
  });

  it('edita chamando update com o id da câmera', () => {
    const fixture = create();
    const el: HTMLElement = fixture.nativeElement;

    buttonByText(el, 'Editar')!.click();
    fixture.detectChanges();
    buttonByText(el, 'Salvar')!.click();

    expect(serviceStub.update).toHaveBeenCalledWith(1, expect.objectContaining({ name: 'Portal Norte' }));
  });

  it('reativa uma câmera inativa chamando activate', () => {
    serviceStub.list = vi.fn(() => of(pageOf([{ ...CAM, active: false }])));
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
