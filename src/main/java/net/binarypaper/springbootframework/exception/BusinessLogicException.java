/*
 * Copyright 2016 <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>.
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

import java.util.Set;
import javax.validation.ConstraintViolation;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * A generic Business Logic Exception intended to be used by the business layer
 * of the application.
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
public class BusinessLogicException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * The unique error code of the error
     *
     * @return The error code
     */
    // Lombok annotations
    @Getter
    private final String errorCode;

    /**
     * The HTTP status code to use for the REST response
     *
     * @return The HTTP status
     */
    // Lombok annotations
    @Getter
    private final HttpStatus httpStatus;

    /**
     * An object containing Spring validation errors
     *
     * @return The error code
     */
    // Lombok annotations
    @Getter
    private Set<ConstraintViolation<?>> constraintViolations;

    /**
     * Constructs an instance of <code>BusinessLogicException</code> with the
     * specified error code.
     *
     * @param errorCode The error code of the error
     */
    public BusinessLogicException(String errorCode) {
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    /**
     * Constructs an instance of <code>BusinessLogicException</code> with the
     * specified error code.
     *
     * @param errorCode The error code of the error
     * @param httpStatus The HTTP status of the error
     */
    public BusinessLogicException(String errorCode, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    /**
     * Constructs an instance of <code>BusinessLogicException</code> with the
     * specified Bean Validation constraint validations.
     *
     * @param constraintViolations The set of Bean Validation constraint
     * validations
     */
    public BusinessLogicException(Set<ConstraintViolation<?>> constraintViolations) {
        this.errorCode = null;
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.constraintViolations = constraintViolations;
    }

}
