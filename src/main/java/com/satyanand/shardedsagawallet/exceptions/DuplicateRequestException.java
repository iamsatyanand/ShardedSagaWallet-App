package com.satyanand.shardedsagawallet.exceptions;

public class DuplicateRequestException extends RuntimeException {
    public DuplicateRequestException(String idempotencyKey) {
        super("Duplicate request detected for key: " + idempotencyKey);
    }
}
