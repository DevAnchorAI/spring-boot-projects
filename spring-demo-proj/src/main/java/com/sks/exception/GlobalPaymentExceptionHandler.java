package com.sks.exception;

import com.sks.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalPaymentExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFoundException(AccountNotFoundException exception, HttpServletRequest request){
        return buildResponse(HttpStatus.NOT_FOUND,exception.getMessage(),request.getRequestURI());
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse>  handleInsufficientBalanceException(InsufficientBalanceException exception,HttpServletRequest request){
        return buildResponse(HttpStatus.CONFLICT,exception.getMessage(),request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse>  handleException(IllegalArgumentException exception,HttpServletRequest request){

        return buildResponse(HttpStatus.BAD_REQUEST,exception.getMessage(),request.getRequestURI());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>  handleException(Exception exception,HttpServletRequest request){

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,exception.getMessage(),request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildResponse( HttpStatus status, String message, String path) {
        ErrorResponse error = new ErrorResponse( Instant.now(), status.value(), status.getReasonPhrase(), message, path );
        return ResponseEntity.status(status).body(error);
    }
}
