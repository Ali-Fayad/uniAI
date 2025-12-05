package com.uniai.exception;

public class InvalidTokenException extends RuntimeException {
	public InvalidTokenException() {
		super("Invalid token!");
	}

}
