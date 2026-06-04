package com.bitcoin.testnet.exception;

public class WalletNotReadyException extends BitcoinException {
    public WalletNotReadyException(String message) {
        super(message);
    }
}
