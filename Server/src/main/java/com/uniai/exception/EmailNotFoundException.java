package com.uniai.exception;

public class EmailNotFoundException extends RuntimeException {
	public EmailNotFoundException() {
		super("Email not found!");
	}

}
