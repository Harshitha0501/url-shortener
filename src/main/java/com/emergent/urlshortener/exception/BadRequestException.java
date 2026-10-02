package com.emergent.urlshortener.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
