package com.urlshortner.exception;

public class ShortCodeAlreadyExistsException extends RuntimeException {
    private static final long serialVersionUID = 1L;

	public ShortCodeAlreadyExistsException(String message) {
        super(message);
    }
}
