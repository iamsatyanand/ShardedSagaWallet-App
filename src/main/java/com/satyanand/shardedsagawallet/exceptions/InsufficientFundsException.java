package com.satyanand.shardedsagawallet.exceptions;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(Long walletId) {
        super("Insufficient funds in wallet: " + walletId);
    }
}
