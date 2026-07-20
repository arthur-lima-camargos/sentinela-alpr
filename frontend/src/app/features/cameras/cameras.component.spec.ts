import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { of, throwError } from 'rxjs';

import { CamerasComponent } from './cameras.component';
import { CameraService } from '../../core/cameras/camera.service';
import { CameraApiKeyService } from '../../core/cameras/camera-api-key.service';
import { AuthService } from '../../core/auth/auth.service';
import { Camera } from '../../shared/models/camera.model';
import { ApiKey } from '../../shared/models/camera-api-key.model';
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

const KEY: ApiKey = {
  id: 7,
  cameraId: 1,
  keyPrefix: 'alpr_ab12',
  active: true,
  createdAt: '2026-01-01T00:00:00Z',
  revokedAt: null,
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
  let apiKeyStub: {
    list: ReturnType<typeof vi.fn>;
    issue: ReturnType<typeof vi.fn>;
    revoke: ReturnType<typeof vi.fn>;
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
    apiKeyStub = {
      list: vi.fn(() => of([KEY])),
      issue: vi.fn(() => of({ id: 8, cameraId: 1, apiKey: 'alpr_fullsecret', keyPrefix: 'alpr_cd34', createdAt: '2026-01-01T00:00:00Z' })),
      revoke: vi.fn(() => of(void 0)),
    };

    await TestBed.configureTestingModule({
      imports: [CamerasComponent],
      providers: [
        { provide: CameraService, useValue: serviceStub },
        { provide: CameraApiKeyService, useValue: apiKeyStub },
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

  it('abre o modal de chaves e lista as chaves da câmera', () => {
    const fixture = create();
    const el: HTMLElement = fixture.nativeElement;

    buttonByText(el, 'Chaves')!.click();
    fixture.detectChanges();

    expect(apiKeyStub.list).toHaveBeenCalledWith(1);
    expect(el.textContent).toContain('Chaves de API — Portal Norte');
    expect(el.textContent).toContain('alpr_ab12');
  });

  it('emite uma nova chave e mostra o segredo uma única vez', () => {
    const fixture = create();
    const el: HTMLElement = fixture.nativeElement;

    buttonByText(el, 'Chaves')!.click();
    fixture.detectChanges();
    buttonByText(el, 'Emitir nova chave')!.click();
    fixture.detectChanges();

    expect(apiKeyStub.issue).toHaveBeenCalledWith(1);
    expect(el.textContent).toContain('alpr_fullsecret');
    expect(el.textContent).toContain('não será exibida novamente');
  });

  it('revoga uma chave após confirmação', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    const fixture = create();
    const el: HTMLElement = fixture.nativeElement;

    buttonByText(el, 'Chaves')!.click();
    fixture.detectChanges();
    buttonByText(el, 'Revogar')!.click();

    expect(apiKeyStub.revoke).toHaveBeenCalledWith(1, 7);
  });
});
