import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SendTransactionRequest, TransactionResponse } from '../models/models';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly base = '/api/transactions';

  constructor(private http: HttpClient) {}

  sendTransaction(request: SendTransactionRequest): Observable<TransactionResponse> {
    return this.http.post<TransactionResponse>(`${this.base}/send`, request);
  }

  listTransactions(): Observable<TransactionResponse[]> {
    return this.http.get<TransactionResponse[]>(this.base);
  }

  getByHash(txHash: string): Observable<TransactionResponse> {
    return this.http.get<TransactionResponse>(`${this.base}/${txHash}`);
  }
}
