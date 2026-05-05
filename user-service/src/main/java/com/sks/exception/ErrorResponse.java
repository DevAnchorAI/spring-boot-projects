package com.sks.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
	private int errorCode;
	private String message;
	
	public ErrorResponse(String message) {
		super();
		this.message = message;
	}
	

}
