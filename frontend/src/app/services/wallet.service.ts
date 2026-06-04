import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AddressBalanceResponse, AddressResponse, BalanceResponse } from '../models/models';

@Injectable({ providedIn: 'root' })
export class WalletService {
  private readonly base = '/api/wallet';

  constructor(private http: HttpClient) {}

  generateAddress(): Observable<AddressResponse> {
    return this.http.post<AddressResponse>(`${this.base}/address`, {});
  }

  getBalance(): Observable<BalanceResponse> {
    return this.http.get<BalanceResponse>(`${this.base}/balance`);
  }

  listAddresses(): Observable<AddressResponse[]> {
    return this.http.get<AddressResponse[]>(`${this.base}/addresses`);
  }

  getAddressBalance(address: string): Observable<AddressBalanceResponse> {
    return this.http.get<AddressBalanceResponse>(`${this.base}/address/${address}/balance`);
  }
}
