import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WalletService } from '../../services/wallet.service';
import { AddressResponse } from '../../models/models';

@Component({
  selector: 'app-address',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-container">
      <div class="page-title">◎ Bitcoin Addresses</div>

      <!-- Generate address -->
      <div class="card">
        <h2>Generate New Address</h2>
        <p class="card-desc">
          Generate a fresh TestNet3 receiving address (P2PKH). Each address is derived
          from your HD wallet and stored in MongoDB.
        </p>
        <button class="btn-primary" (click)="generate()" [disabled]="generating">
          {{ generating ? 'Generating...' : '+ Generate Address' }}
        </button>

        <div *ngIf="newAddress" class="result-box">
          <label>New Address</label>
          <div class="mono">{{ newAddress.address }}</div>
          <div class="addr-meta">
            <span class="badge badge-success">{{ newAddress.network }}</span>
            <span class="addr-date">{{ newAddress.createdAt | date:'medium' }}</span>
          </div>
        </div>
        <div *ngIf="generateError" class="error-msg">{{ generateError }}</div>
      </div>

      <!-- Address list -->
      <div class="card">
        <h2>Generated Addresses</h2>
        <button class="btn-secondary" style="margin-bottom:16px" (click)="loadAddresses()">
          ↻ Refresh
        </button>
        <div *ngIf="loading" class="loading">Loading addresses...</div>
        <div *ngIf="!loading && addresses.length === 0" class="info-msg">
          No addresses generated yet. Click "Generate Address" above.
        </div>
        <table *ngIf="!loading && addresses.length > 0">
          <thead>
            <tr>
              <th>Address</th>
              <th>Network</th>
              <th>Created</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let addr of addresses">
              <td><span class="mono addr-cell">{{ addr.address }}</span></td>
              <td><span class="badge badge-success">{{ addr.network }}</span></td>
              <td>{{ addr.createdAt | date:'short' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .card-desc {
      color: #a0aec0;
      font-size: 14px;
      margin-bottom: 16px;
      line-height: 1.5;
    }
    .result-box {
      margin-top: 16px;
    }
    .addr-meta {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-top: 8px;
    }
    .addr-date {
      font-size: 12px;
      color: #a0aec0;
    }
    .addr-cell {
      font-size: 12px;
    }
  `]
})
export class AddressComponent implements OnInit {
  addresses: AddressResponse[] = [];
  newAddress: AddressResponse | null = null;
  loading = false;
  generating = false;
  generateError = '';

  constructor(private walletService: WalletService) {}

  ngOnInit(): void {
    this.loadAddresses();
  }

  loadAddresses(): void {
    this.loading = true;
    this.walletService.listAddresses().subscribe({
      next: (a) => { this.addresses = a; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  generate(): void {
    this.generating = true;
    this.generateError = '';
    this.newAddress = null;
    this.walletService.generateAddress().subscribe({
      next: (a) => {
        this.newAddress = a;
        this.generating = false;
        this.loadAddresses();
      },
      error: (e) => {
        this.generateError = e.error?.message || 'Failed to generate address';
        this.generating = false;
      }
    });
  }
}
