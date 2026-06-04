package com.bitcoin.testnet.exception;

public class BitcoinException extends RuntimeException {
    public BitcoinException(String message) {
        super(message);
    }
    public BitcoinException(String message, Throwable cause) {
        super(message, cause);
    }
}
