import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MessageService } from '../../services/message.service';
import { SignatureResponse, VerifyResponse } from '../../models/models';

@Component({
  selector: 'app-message',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-container">
      <div class="page-title">✎ Sign & Verify Messages</div>

      <!-- Sign section -->
      <div class="card">
        <h2>Sign Message</h2>
        <div class="info-msg">
          Sign a message using the private key of a wallet address.
          Only P2PKH (legacy) addresses are supported for Bitcoin message signing.
        </div>

        <div class="form-group">
          <label>Wallet Address (P2PKH)</label>
          <input type="text" [(ngModel)]="signAddress" placeholder="m... or n... TestNet3 address" />
        </div>
        <div class="form-group">
          <label>Message</label>
          <textarea [(ngModel)]="signMessage" placeholder="Enter message to sign..."></textarea>
        </div>

        <button class="btn-primary" (click)="sign()" [disabled]="signing || !signAddress || !signMessage">
          {{ signing ? 'Signing...' : '✎ Sign Message' }}
        </button>

        <div *ngIf="signResult" class="result-section">
          <h3>Signature</h3>
          <div class="mono">{{ signResult.signature }}</div>
          <div class="copy-hint">Copy this signature to share or verify.</div>
        </div>
        <div *ngIf="signError" class="error-msg" style="margin-top:16px">{{ signError }}</div>
      </div>

      <!-- Verify section -->
      <div class="card">
        <h2>Verify Message</h2>
        <div class="info-msg">
          Verify that a message was signed by the private key of a given Bitcoin address.
        </div>

        <div class="form-group">
          <label>Address</label>
          <input type="text" [(ngModel)]="verifyAddress" placeholder="Bitcoin P2PKH address" />
        </div>
        <div class="form-group">
          <label>Original Message</label>
          <textarea [(ngModel)]="verifyMessage" placeholder="The original message..."></textarea>
        </div>
        <div class="form-group">
          <label>Signature (base64)</label>
          <textarea [(ngModel)]="verifySignature" placeholder="Paste signature here..."></textarea>
        </div>

        <button class="btn-primary" (click)="verify()"
          [disabled]="verifying || !verifyAddress || !verifyMessage || !verifySignature">
          {{ verifying ? 'Verifying...' : '✓ Verify Signature' }}
        </button>

        <div *ngIf="verifyResult !== null" class="result-section">
          <div [class]="verifyResult.valid ? 'success-msg' : 'error-msg'" style="margin-top:16px">
            <strong>{{ verifyResult.valid ? '✓ Valid Signature' : '✗ Invalid Signature' }}</strong>
            <div>{{ verifyResult.message }}</div>
          </div>
        </div>
        <div *ngIf="verifyError" class="error-msg" style="margin-top:16px">{{ verifyError }}</div>
      </div>
    </div>
  `,
  styles: [`
    .result-section {
      margin-top: 16px;
    }
    .result-section h3 {
      margin-bottom: 8px;
    }
    .copy-hint {
      font-size: 12px;
      color: #a0aec0;
      margin-top: 8px;
    }
  `]
})
export class MessageComponent {
  // Sign
  signAddress = '';
  signMessage = '';
  signing = false;
  signResult: SignatureResponse | null = null;
  signError = '';

  // Verify
  verifyAddress = '';
  verifyMessage = '';
  verifySignature = '';
  verifying = false;
  verifyResult: VerifyResponse | null = null;
  verifyError = '';

  constructor(private messageService: MessageService) {}

  sign(): void {
    this.signing = true;
    this.signResult = null;
    this.signError = '';
    this.messageService.signMessage({ address: this.signAddress, message: this.signMessage }).subscribe({
      next: (r) => { this.signResult = r; this.signing = false; },
      error: (e) => { this.signError = e.error?.message || 'Failed to sign message'; this.signing = false; }
    });
  }

  verify(): void {
    this.verifying = true;
    this.verifyResult = null;
    this.verifyError = '';
    this.messageService.verifyMessage({
      address: this.verifyAddress,
      message: this.verifyMessage,
      signature: this.verifySignature
    }).subscribe({
      next: (r) => { this.verifyResult = r; this.verifying = false; },
      error: (e) => { this.verifyError = e.error?.message || 'Failed to verify'; this.verifying = false; }
    });
  }
}
