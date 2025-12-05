package com.uniai.exception;

public class JsonProcessingException extends RuntimeException {
	public JsonProcessingException(String message) {
		super(message);
	}

	public JsonProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

}
