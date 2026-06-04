export interface AddressResponse {
  id: string;
  address: string;
  network: string;
  createdAt: string;
}

export interface BalanceResponse {
  satoshis: number;
  friendlyAmount: string;
  network: string;
  synced: boolean;
  bestChainHeight: number;
}

export interface TransactionResponse {
  id: string;
  eventId: string;
  txHash: string;
  fromAddress: string;
  toAddress: string;
  amountSatoshis: number;
  status: string;
  network: string;
  createdAt: string;
}

export interface SignatureResponse {
  address: string;
  message: string;
  signature: string;
}

export interface VerifyResponse {
  valid: boolean;
  message: string;
  address: string;
}

export interface SendTransactionRequest {
  toAddress: string;
  amountSatoshis: number;
}

export interface SignMessageRequest {
  address: string;
  message: string;
}

export interface VerifyMessageRequest {
  address: string;
  message: string;
  signature: string;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}
