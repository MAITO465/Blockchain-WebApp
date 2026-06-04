import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { WalletService } from '../../services/wallet.service';
import { TransactionService } from '../../services/transaction.service';
import { BalanceResponse, TransactionResponse } from '../../models/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page-container">
      <div class="page-title">⊞ Dashboard</div>

      <div class="info-msg">
        Connected to Bitcoin <strong>TestNet3</strong>.
        TestNet coins have no real value. SPV sync may take a few minutes on first start.
      </div>

      <!-- Balance card -->
      <div class="card">
        <h2>◈ Wallet Balance</h2>
        <div *ngIf="loadingBalance" class="loading">Loading balance...</div>
        <div *ngIf="!loadingBalance && balance">
          <div class="balance-amount">{{ balance.friendlyAmount }}</div>
          <div class="balance-meta">
            <span class="badge" [class]="balance.synced ? 'badge-success' : 'badge-warning'">
              {{ balance.synced ? 'Synced' : 'Syncing...' }}
            </span>
            <span class="chain-height">Block height: {{ balance.bestChainHeight }}</span>
          </div>
        </div>
        <div *ngIf="!loadingBalance && balanceError" class="error-msg">{{ balanceError }}</div>
      </div>

      <!-- Recent transactions card -->
      <div class="card">
        <h2>⇄ Recent Transactions</h2>
        <div *ngIf="loadingTx" class="loading">Loading transactions...</div>
        <div *ngIf="!loadingTx && recentTx.length === 0" class="info-msg">
          No transactions yet. <a routerLink="/transactions">Send your first transaction</a>.
        </div>
        <table *ngIf="!loadingTx && recentTx.length > 0">
          <thead>
            <tr>
              <th>Tx Hash</th>
              <th>To</th>
              <th>Amount</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let tx of recentTx">
              <td><span class="mono short-hash">{{ tx.txHash | slice:0:16 }}...</span></td>
              <td><span class="mono short-hash">{{ tx.toAddress | slice:0:16 }}...</span></td>
              <td>{{ tx.amountSatoshis | number }} sat</td>
              <td><span class="badge badge-success">{{ tx.status }}</span></td>
            </tr>
          </tbody>
        </table>
        <div class="card-actions" *ngIf="recentTx.length > 0">
          <a routerLink="/transactions">View all transactions →</a>
        </div>
      </div>

      <!-- Quick actions -->
      <div class="card">
        <h2>Quick Actions</h2>
        <div class="quick-actions">
          <a routerLink="/addresses" class="action-btn">Generate Address</a>
          <a routerLink="/transactions" class="action-btn">Send Transaction</a>
          <a routerLink="/messages" class="action-btn">Sign Message</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .balance-amount {
      font-size: 36px;
      font-weight: 700;
      color: #f7931a;
      margin-bottom: 12px;
    }
    .balance-meta {
      display: flex;
      align-items: center;
      gap: 16px;
    }
    .chain-height {
      font-size: 13px;
      color: #a0aec0;
    }
    .card-actions {
      margin-top: 12px;
      text-align: right;
    }
    .short-hash {
      font-size: 12px;
    }
    .quick-actions {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
    }
    .action-btn {
      display: inline-block;
      background: #1e2533;
      border: 1px solid #f7931a44;
      color: #f7931a;
      padding: 10px 18px;
      border-radius: 8px;
      font-size: 14px;
      font-weight: 600;
      text-decoration: none;
      transition: background 0.2s;
    }
    .action-btn:hover {
      background: #f7931a22;
      text-decoration: none;
    }
  `]
})
export class DashboardComponent implements OnInit {
  balance: BalanceResponse | null = null;
  recentTx: TransactionResponse[] = [];
  loadingBalance = true;
  loadingTx = true;
  balanceError = '';

  constructor(
    private walletService: WalletService,
    private txService: TransactionService
  ) {}

  ngOnInit(): void {
    this.walletService.getBalance().subscribe({
      next: (b) => { this.balance = b; this.loadingBalance = false; },
      error: (e) => { this.balanceError = e.error?.message || 'Failed to load balance'; this.loadingBalance = false; }
    });

    this.txService.listTransactions().subscribe({
      next: (txs) => { this.recentTx = txs.slice(0, 5); this.loadingTx = false; },
      error: () => { this.loadingTx = false; }
    });
  }
}
