package com.bitcoin.testnet.exception;

public class InvalidAddressException extends BitcoinException {
    public InvalidAddressException(String message) {
        super(message);
    }
}
