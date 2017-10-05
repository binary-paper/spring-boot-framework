/*
 * Copyright 2017 <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.binarypaper.springbootframework.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javax.validation.ConstraintViolation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Rest Exception Handler class to convert application exceptions to JSON
 * responses
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// Spring annotations
@ControllerAdvice
public class RestExceptionHandler {

    private static final ResourceBundle ERROR_MESSAGES = ResourceBundle.getBundle("ErrorMessages");

    // Spring annotations
    @ExceptionHandler({BusinessLogicException.class})
    protected ResponseEntity<Object> handleBusinessLogicException(BusinessLogicException businessLogicException) {
        BusinessLogicError businessLogicError = new BusinessLogicError();
        // Set the error code and message
        if (businessLogicException.getErrorCode() != null) {
            businessLogicError.setErrorCode(businessLogicException.getErrorCode());
            businessLogicError.setMessage(ERROR_MESSAGES.getString(businessLogicException.getErrorCode()));
        }
        // Set the field constraint validation errors
        Set<ConstraintViolation<?>> constraintViolations = businessLogicException.getConstraintViolations();
        if (constraintViolations != null) {
            List<FieldError> fieldErrors = new ArrayList<>();
            for (ConstraintViolation constraintViolation : constraintViolations) {
                FieldError fieldError = new FieldError();
                fieldError.setField(constraintViolation.getPropertyPath().toString());
                fieldError.setMessage(constraintViolation.getMessage());
                fieldErrors.add(fieldError);
            }
            businessLogicError.setFieldErrors(fieldErrors);
        }
        // Set the content type header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(businessLogicError, headers, businessLogicException.getHttpStatus());
    }

    // Spring annotations
    @ExceptionHandler({MethodArgumentNotValidException.class})
    protected ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        BusinessLogicError businessLogicError = new BusinessLogicError();
        // Set the field constraint validation errors
        if (exception.getBindingResult() != null) {
            List<FieldError> fieldErrors = new ArrayList<>();
            for (org.springframework.validation.FieldError springFieldError : exception.getBindingResult().getFieldErrors()) {
                FieldError fieldError = new FieldError();
                fieldError.setField(springFieldError.getField());
                fieldError.setMessage(springFieldError.getDefaultMessage());
                fieldErrors.add(fieldError);
            }
            businessLogicError.setFieldErrors(fieldErrors);
        } else {
            businessLogicError.setMessage(exception.getMessage());
            if (exception.getParameter() != null) {
                businessLogicError.setErrorCode(exception.getParameter().getParameterName());
            }
        }
        // Set the content type header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(businessLogicError, headers, HttpStatus.BAD_REQUEST);
    }
}
