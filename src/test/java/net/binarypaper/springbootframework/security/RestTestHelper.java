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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

/**
 * Helper class used to by Arquillian REST integration tests
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// Lombok annotations
@Log
public class RestTestHelper {

    /**
     * Returns a Keycloak authentication token that will be used by REST service
     * calls for the authentication header
     *
     * @param serverUrl The base server URL of the Keycloak server
     * @param realm The Keycloak realm name
     * @param clientId The Keycloak client id
     * @param userName The Keycloak user name
     * @param password the Keycloak user password
     * @return A Keycloak authentication token
     */
    public static KeycloakToken getKeycloakToken(String serverUrl, String realm, String clientId, String userName, String password) {
        try {
            String tokenURL = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost(tokenURL);
            // Add HTTP headers
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setHeader("Accept", "application/json");
            // Add post form content
            List<NameValuePair> formParameters = new ArrayList<>();
            formParameters.add(new BasicNameValuePair("grant_type", "password"));
            formParameters.add(new BasicNameValuePair("client_id", clientId));
            formParameters.add(new BasicNameValuePair("username", userName));
            formParameters.add(new BasicNameValuePair("password", password));
            httpPost.setEntity(new UrlEncodedFormEntity(formParameters, Charset.defaultCharset()));
            // Excecute the http post
            HttpResponse response = httpClient.execute(httpPost);
            // Read the http response result
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            // Convert the response JSON to KeycloakToken object
            ObjectMapper objectMapper = new ObjectMapper();
            KeycloakToken keycloakToken = objectMapper.readValue(result.toString(), KeycloakToken.class);
            return keycloakToken;
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Log out a specified Keycloak authentication token
     *
     * @param keycloakToken The Keycloak token to log out
     * @param serverUrl The base server URL of the Keycloak server
     * @param realm The Keycloak realm name
     * @param clientId The Keycloak client id
     */
    public static void logoutKeycloakToken(KeycloakToken keycloakToken, String serverUrl, String realm, String clientId) {
        try {
            String logoutURL = serverUrl + "/realms/" + realm + "/protocol/openid-connect/logout?session_state=" + keycloakToken.getSessionState();
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost httpPost = new HttpPost(logoutURL);
            // Add HTTP headers
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + keycloakToken.getAccessToken());
            // Add post form content
            List<NameValuePair> formParameters = new ArrayList<>();
            formParameters.add(new BasicNameValuePair("client_id", clientId));
            formParameters.add(new BasicNameValuePair("refresh_token", keycloakToken.getRefreshToken()));
            httpPost.setEntity(new UrlEncodedFormEntity(formParameters, Charset.defaultCharset()));
            // Excecute the http post
            httpClient.execute(httpPost);
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }

}
