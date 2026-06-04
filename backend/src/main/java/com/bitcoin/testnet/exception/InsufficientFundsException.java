package com.bitcoin.testnet.exception;

public class InsufficientFundsException extends BitcoinException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
