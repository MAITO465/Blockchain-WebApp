import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SignMessageRequest, SignatureResponse, VerifyMessageRequest, VerifyResponse } from '../models/models';

@Injectable({ providedIn: 'root' })
export class MessageService {
  private readonly base = '/api/messages';

  constructor(private http: HttpClient) {}

  signMessage(request: SignMessageRequest): Observable<SignatureResponse> {
    return this.http.post<SignatureResponse>(`${this.base}/sign`, request);
  }

  verifyMessage(request: VerifyMessageRequest): Observable<VerifyResponse> {
    return this.http.post<VerifyResponse>(`${this.base}/verify`, request);
  }
}
