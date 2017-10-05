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
package net.binarypaper.springbootframework;

import static com.google.common.collect.Lists.newArrayList;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.AuthorizationCodeGrant;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.OAuth;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.Tag;
import springfox.documentation.service.TokenEndpoint;
import springfox.documentation.service.TokenRequestEndpoint;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// Spring annotations
@Configuration
// Swagger annotations
@EnableSwagger2
public class SwaggerConfig {

    /**
     * The name of the Basic Authentication scheme
     */
    public static final String O_AUTH_2 = "OAuth2";

    @Value("${project.version}")
    private String projectVersion;

    @Bean
    public Docket swaggerApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("net.binarypaper.springbootframework"))
                .paths(PathSelectors.regex("/.*"))
                .build()
                .apiInfo(apiInfo())
                .tags(new Tag("Lookup Values", "A lookup value REST resource"),
                        new Tag("Render Notification", "A render notification REST resource"))
                .securitySchemes(newArrayList(securitySchema()))
                .securityContexts(newArrayList(securityContext()));
    }

    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
                "SpringBootFramework REST API",
                "REST API documentation",
                projectVersion,
                "",
                new Contact("Willy Gadney", "", "willy.gadney@binarypaper.net"),
                "Apache License Version 2.0",
                "http://www.apache.org/licenses/LICENSE-2.0",
                vendorExtensions());
        return apiInfo;
    }

    private List<VendorExtension> vendorExtensions() {
        ArrayList<VendorExtension> vendorExtensions = new ArrayList<>();
        return vendorExtensions;
    }

    private OAuth securitySchema() {
//        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        TokenRequestEndpoint tokenRequestEndpoint = new TokenRequestEndpoint("http://localhost:8180/auth/realms/demo/protocol/openid-connect/auth", "swagger-ui", null);
        TokenEndpoint tokenEndpoint = new TokenEndpoint("http://localhost:8180/auth/realms/demo/protocol/openid-connect/token", "access_token");
        AuthorizationCodeGrant authorizationCodeGrant = new AuthorizationCodeGrant(tokenRequestEndpoint, tokenEndpoint);
        return new OAuth(O_AUTH_2, newArrayList(), newArrayList(authorizationCodeGrant));
//        return new OAuth(O_AUTH_2, newArrayList(authorizationScope), newArrayList(grantType));
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .build();
    }

    List<SecurityReference> defaultAuth() {
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[0];
//        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
//        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
//        authorizationScopes[0] = authorizationScope;
        return newArrayList(new SecurityReference(O_AUTH_2, authorizationScopes));
    }

    /**
     * A bean to configure the values that will be returned at the
     * /swagger-resources/configuration/security rest endpoint to configure the
     * Swagger UI frontend of SpringFox
     *
     * @return The security configuration for swagger UI
     */
    @Bean
    SecurityConfiguration security() {
        return new SecurityConfiguration(
                "swagger-ui", /*client ID*/
                "", /*client secret*/
                "demo", /*realm*/
                "your-app-name", /*app name*/
                "", /*api key*/
                ApiKeyVehicle.HEADER, /*api key vehicle*/
                "", /*api key name*/
                " " /*scope separator*/);
    }
}
