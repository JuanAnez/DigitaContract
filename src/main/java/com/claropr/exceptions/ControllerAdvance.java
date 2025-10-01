package com.claropr.exceptions;

import com.claropr.model.ApiResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ControllerAdvance extends ResponseEntityExceptionHandler {
  
  public ControllerAdvance() {
    System.out.println("ControllerAdvance: Constructor called - @RestControllerAdvice is being loaded");
  }
  @ExceptionHandler(value = BusinessLogicException.class)
  public ResponseEntity<Object> businessLogicException(
      BusinessLogicException businessLogicException) {
    
    System.out.println("ControllerAdvance: Handling BusinessLogicException - Status: " + businessLogicException.getStatusCode() + ", Message: " + businessLogicException.getMessage());
    
    HttpStatus httpStatus = HttpStatus.valueOf(businessLogicException.getStatusCode());

    String reasonPhrase = httpStatus.getReasonPhrase();

    ApiResponseDTO<String> apiResponseDTO =
        ApiResponseDTO.create(
            httpStatus.value(), reasonPhrase, businessLogicException.getMessage());

    return new ResponseEntity<>(apiResponseDTO, HttpStatus.OK);
  }
}
