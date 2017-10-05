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
package net.binarypaper.springbootframework.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents a Keycloak Oauth token
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// Lombok annotations
@Data
public class KeycloakToken {

    // Jackson annotations
    @JsonProperty("access_token")
    private String accessToken;

    // Jackson annotations
    @JsonProperty("expires_in")
    private Long expiresIn;

    // Jackson annotations
    @JsonProperty("refresh_expires_in")
    private Long refreshExpiresIn;

    // Jackson annotations
    @JsonProperty("refresh_token")
    private String refreshToken;

    // Jackson annotations
    @JsonProperty("token_type")
    private String tokenType;

    // Jackson annotations
    @JsonProperty("id_token")
    private String idToken;

    // Jackson annotations
    @JsonProperty("not-before-policy")
    private Long notBeforePolicy;

    // Jackson annotations
    @JsonProperty("session_state")
    private String sessionState;
}
