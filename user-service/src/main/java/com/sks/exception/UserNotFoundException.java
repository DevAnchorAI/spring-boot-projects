package com.sks.exception;

public class UserNotFoundException extends RuntimeException {
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
private String message;

public UserNotFoundException() {};
public UserNotFoundException(String message) {
	super();
	this.message = message;
}

}
