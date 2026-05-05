package com.sks.exception;

public class UserAlreadyExistException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;

	public UserAlreadyExistException() {};
	public UserAlreadyExistException(String message) {
		super();
		this.message = message;
	}
	

}
