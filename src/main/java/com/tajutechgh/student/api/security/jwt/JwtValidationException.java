package com.tajutechgh.student.api.security.jwt;

@SuppressWarnings("serial")
public class JwtValidationException extends Exception {

	public JwtValidationException(String message, Throwable cause) {
		
		super(message, cause);
	}
}
