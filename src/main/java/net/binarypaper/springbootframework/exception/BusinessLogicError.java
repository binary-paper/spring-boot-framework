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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Class to represent business logic errors in JSON format for REST responses
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// Jackson annotations
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({
    "errorCode",
    "message",
    "fieldErrors"
})
// Lombok annotations
@Getter
@Setter
@NoArgsConstructor
public class BusinessLogicError {

    // Jackson annotations
    @JsonProperty("error_code")
    // Swagger annotations
    @ApiModelProperty(
            value = "The error code of the error",
            example = "0002",
            readOnly = true,
            position = 1
    )
    private String errorCode;

    // Swagger annotations
    @ApiModelProperty(
            value = "The error message of the error",
            example = "The specified parent id is invalid.",
            readOnly = true,
            position = 2
    )
    private String message;

    // Jackson annotations
    @JsonProperty("field_errors")
    // Swagger annotations
    @ApiModelProperty(
            value = "The list of field va;idation errors",
            readOnly = true,
            position = 3
    )
    private List<FieldError> fieldErrors;

}
