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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Class to represent field errors in JSON format for REST responses
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// Jackson annotations
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "field",
    "message"
})
// Lombok annotations
@Getter
@Setter
public class FieldError {

    // Swagger annotations
    @ApiModelProperty(
            value = "The name of the field with the validation error",
            example = "lookup_list_name",
            readOnly = true,
            position = 1
    )
    private String field;
    
    // Swagger annotations
    @ApiModelProperty(
            value = "The validation error message",
            example = "The lookup_list_name is required.",
            readOnly = true,
            position = 2
    )
    private String message;
}
