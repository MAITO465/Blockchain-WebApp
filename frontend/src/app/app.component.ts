import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="app-layout">
      <nav class="sidebar">
        <div class="sidebar-header">
          <div class="logo">₿</div>
          <div class="logo-text">
            <div class="logo-title">Bitcoin</div>
            <div class="logo-subtitle">TestNet3 Client</div>
          </div>
        </div>
        <ul class="nav-links">
          <li>
            <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}">
              <span class="nav-icon">⊞</span> Dashboard
            </a>
          </li>
          <li>
            <a routerLink="/addresses" routerLinkActive="active">
              <span class="nav-icon">◎</span> Addresses
            </a>
          </li>
          <li>
            <a routerLink="/balance" routerLinkActive="active">
              <span class="nav-icon">◈</span> Balance
            </a>
          </li>
          <li>
            <a routerLink="/transactions" routerLinkActive="active">
              <span class="nav-icon">⇄</span> Transactions
            </a>
          </li>
          <li>
            <a routerLink="/messages" routerLinkActive="active">
              <span class="nav-icon">✎</span> Sign / Verify
            </a>
          </li>
        </ul>
        <div class="sidebar-footer">
          <span class="network-badge">TestNet3</span>
        </div>
      </nav>
      <main class="main-content">
        <router-outlet />
      </main>
    </div>
  `,
  styles: [`
    .app-layout {
      display: flex;
      min-height: 100vh;
    }
    .sidebar {
      width: 220px;
      background: #0d1117;
      border-right: 1px solid #2d3748;
      display: flex;
      flex-direction: column;
      position: fixed;
      top: 0;
      left: 0;
      height: 100vh;
    }
    .sidebar-header {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 24px 16px;
      border-bottom: 1px solid #2d3748;
    }
    .logo {
      font-size: 32px;
      color: #f7931a;
      line-height: 1;
    }
    .logo-title {
      font-size: 16px;
      font-weight: 700;
      color: #f7931a;
    }
    .logo-subtitle {
      font-size: 11px;
      color: #a0aec0;
    }
    .nav-links {
      list-style: none;
      padding: 16px 8px;
      flex: 1;
    }
    .nav-links li a {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 10px 12px;
      border-radius: 8px;
      color: #a0aec0;
      font-size: 14px;
      font-weight: 500;
      transition: background 0.15s, color 0.15s;
    }
    .nav-links li a:hover {
      background: #1a1f2e;
      color: #e0e0e0;
      text-decoration: none;
    }
    .nav-links li a.active {
      background: #f7931a22;
      color: #f7931a;
    }
    .nav-icon {
      font-size: 16px;
      width: 20px;
      text-align: center;
    }
    .sidebar-footer {
      padding: 16px;
      border-top: 1px solid #2d3748;
    }
    .network-badge {
      display: inline-block;
      background: #1a472a;
      color: #68d391;
      padding: 4px 10px;
      border-radius: 12px;
      font-size: 11px;
      font-weight: 600;
    }
    .main-content {
      flex: 1;
      margin-left: 220px;
      min-height: 100vh;
      background: #0f1117;
    }
  `]
})
export class AppComponent {}
