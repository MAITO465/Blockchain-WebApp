import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WalletService } from '../../services/wallet.service';
import { BalanceResponse } from '../../models/models';

@Component({
  selector: 'app-balance',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-container">
      <div class="page-title">◈ Wallet Balance</div>

      <div class="info-msg">
        Balance shown is for the entire wallet (all addresses combined).
        Per-address balance is not available in SPV mode without an external block explorer.
      </div>

      <div class="card">
        <h2>Total Balance</h2>
        <button class="btn-secondary" style="margin-bottom:16px" (click)="loadBalance()">
          ↻ Refresh
        </button>

        <div *ngIf="loading" class="loading">Loading balance...</div>
        <div *ngIf="!loading && balance">
          <div class="balance-big">{{ balance.friendlyAmount }}</div>
          <div class="balance-sats">{{ balance.satoshis | number }} satoshis</div>

          <div class="sync-status">
            <div class="sync-row">
              <span class="sync-label">Sync status</span>
              <span class="badge" [class]="balance.synced ? 'badge-success' : 'badge-warning'">
                {{ balance.synced ? 'Synced' : 'Syncing...' }}
              </span>
            </div>
            <div class="sync-row">
              <span class="sync-label">Best chain height</span>
              <span class="sync-value">{{ balance.bestChainHeight | number }}</span>
            </div>
            <div class="sync-row">
              <span class="sync-label">Network</span>
              <span class="badge badge-success">{{ balance.network }}</span>
            </div>
          </div>

          <div *ngIf="!balance.synced" class="info-msg" style="margin-top:16px">
            The wallet is still syncing with TestNet3. Balance may not be accurate yet.
            The first synchronization downloads all block headers and may take several minutes.
          </div>
        </div>
        <div *ngIf="!loading && error" class="error-msg">{{ error }}</div>
      </div>

      <div class="card">
        <h2>Get TestNet Coins</h2>
        <p class="desc">
          TestNet coins have no real value. You can get free TestNet3 coins from a faucet.
          First, generate an address, then paste it in one of these faucets:
        </p>
        <ul class="faucet-list">
          <li>coinfaucet.eu/en/btc-testnet</li>
          <li>testnet-faucet.com</li>
          <li>bitcoinfaucet.uo1.net</li>
        </ul>
        <p class="desc">After sending, wait for at least 1 confirmation (~10 minutes on TestNet).</p>
      </div>
    </div>
  `,
  styles: [`
    .balance-big {
      font-size: 48px;
      font-weight: 700;
      color: #f7931a;
      margin-bottom: 8px;
    }
    .balance-sats {
      font-size: 16px;
      color: #a0aec0;
      margin-bottom: 24px;
    }
    .sync-status {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
    .sync-row {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .sync-label {
      font-size: 13px;
      color: #a0aec0;
      min-width: 140px;
    }
    .sync-value {
      font-size: 14px;
      color: #e0e0e0;
      font-family: monospace;
    }
    .desc {
      color: #a0aec0;
      font-size: 14px;
      line-height: 1.6;
      margin-bottom: 12px;
    }
    .faucet-list {
      list-style: none;
      padding: 0;
      margin-bottom: 12px;
    }
    .faucet-list li {
      font-family: monospace;
      color: #f7931a;
      padding: 4px 0;
    }
  `]
})
export class BalanceComponent implements OnInit {
  balance: BalanceResponse | null = null;
  loading = false;
  error = '';

  constructor(private walletService: WalletService) {}

  ngOnInit(): void {
    this.loadBalance();
  }

  loadBalance(): void {
    this.loading = true;
    this.error = '';
    this.walletService.getBalance().subscribe({
      next: (b) => { this.balance = b; this.loading = false; },
      error: (e) => { this.error = e.error?.message || 'Failed to load balance'; this.loading = false; }
    });
  }
}
