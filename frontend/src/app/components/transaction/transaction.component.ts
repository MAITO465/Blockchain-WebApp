import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TransactionService } from '../../services/transaction.service';
import { TransactionResponse } from '../../models/models';

@Component({
  selector: 'app-transaction',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-container">
      <div class="page-title">⇄ Transactions</div>

      <!-- Send form -->
      <div class="card">
        <h2>Send Bitcoin (TestNet3)</h2>
        <div class="info-msg">
          Make sure your wallet has TestNet3 funds and is synced before sending.
          Minimum fee is deducted automatically by BitcoinJ.
        </div>

        <div class="form-group">
          <label>Destination Address</label>
          <input
            type="text"
            [(ngModel)]="toAddress"
            placeholder="m... or n... or tb1... (TestNet3 address)"
          />
        </div>
        <div class="form-group">
          <label>Amount (satoshis)</label>
          <input
            type="number"
            [(ngModel)]="amountSatoshis"
            placeholder="e.g. 10000"
            min="1"
          />
          <div class="hint" *ngIf="amountSatoshis > 0">
            ≈ {{ amountSatoshis / 100000000 | number:'1.8-8' }} BTC
          </div>
        </div>

        <button class="btn-primary" (click)="send()" [disabled]="sending || !toAddress || amountSatoshis < 1">
          {{ sending ? 'Sending...' : '⇒ Send Transaction' }}
        </button>

        <div *ngIf="sendResult" class="success-msg" style="margin-top:16px">
          <strong>Transaction broadcast!</strong>
          <div class="mono" style="margin-top:8px">{{ sendResult.txHash }}</div>
        </div>
        <div *ngIf="sendError" class="error-msg" style="margin-top:16px">{{ sendError }}</div>
      </div>

      <!-- Transaction list -->
      <div class="card">
        <h2>Persisted Transactions</h2>
        <button class="btn-secondary" style="margin-bottom:16px" (click)="loadTransactions()">
          ↻ Refresh
        </button>
        <div *ngIf="loading" class="loading">Loading transactions...</div>
        <div *ngIf="!loading && transactions.length === 0" class="info-msg">
          No transactions persisted yet. Transactions appear here after Kafka consumer processes them.
        </div>
        <table *ngIf="!loading && transactions.length > 0">
          <thead>
            <tr>
              <th>Tx Hash</th>
              <th>To</th>
              <th>Amount</th>
              <th>Status</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let tx of transactions">
              <td><span class="mono" style="font-size:11px">{{ tx.txHash | slice:0:20 }}...</span></td>
              <td><span class="mono" style="font-size:11px">{{ tx.toAddress | slice:0:20 }}...</span></td>
              <td>{{ tx.amountSatoshis | number }} sat</td>
              <td><span class="badge badge-success">{{ tx.status }}</span></td>
              <td style="white-space:nowrap">{{ tx.createdAt | date:'short' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .hint {
      font-size: 12px;
      color: #a0aec0;
      margin-top: 4px;
    }
  `]
})
export class TransactionComponent implements OnInit {
  toAddress = '';
  amountSatoshis = 0;
  sending = false;
  sendResult: TransactionResponse | null = null;
  sendError = '';

  transactions: TransactionResponse[] = [];
  loading = false;

  constructor(private txService: TransactionService) {}

  ngOnInit(): void {
    this.loadTransactions();
  }

  send(): void {
    this.sending = true;
    this.sendResult = null;
    this.sendError = '';
    this.txService.sendTransaction({ toAddress: this.toAddress, amountSatoshis: this.amountSatoshis }).subscribe({
      next: (r) => {
        this.sendResult = r;
        this.sending = false;
        this.toAddress = '';
        this.amountSatoshis = 0;
        setTimeout(() => this.loadTransactions(), 2000);
      },
      error: (e) => {
        this.sendError = e.error?.message || 'Failed to send transaction';
        this.sending = false;
      }
    });
  }

  loadTransactions(): void {
    this.loading = true;
    this.txService.listTransactions().subscribe({
      next: (txs) => { this.transactions = txs; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }
}
