import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../../core/auth/auth.service';
import { AlertRealtimeService } from '../../core/alerts/alert-realtime.service';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent implements OnInit, OnDestroy {
  private readonly auth = inject(AuthService);
  private readonly realtime = inject(AlertRealtimeService);

  protected readonly user = this.auth.currentUser;
  protected readonly liveCount = this.realtime.liveCount;

  ngOnInit(): void {
    this.realtime.connect();
  }

  ngOnDestroy(): void {
    this.realtime.disconnect();
  }

  protected logout(): void {
    this.realtime.disconnect();
    this.auth.logout();
  }
}
