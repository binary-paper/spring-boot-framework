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
package net.binarypaper.springbootframework.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Base64;
import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import net.binarypaper.springbootframework.email.EmailMessage;
import net.binarypaper.springbootframework.exception.BusinessLogicError;
import net.binarypaper.springbootframework.exception.BusinessLogicException;
import net.binarypaper.springbootframework.render.FreeMarkerRenderService;
import net.binarypaper.springbootframework.SwaggerConfig;

/**
 * Render Notification REST Web Service
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// Spring annotations
@RestController
@RequestMapping(path = "render-notification")
// Security annotations
@RolesAllowed("render-notifications")
// Swagger annotations
@Api(tags = {"Render Notification"}, authorizations = {
    @Authorization(value = SwaggerConfig.O_AUTH_2)
})
// Lombok annotations
@Log
public class RenderNotificationController {

    @Autowired
    private FreeMarkerRenderService freeMarkerRenderService;

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // Spring annotations
    @PostMapping(path = "{templateName:.+}",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.TEXT_HTML_VALUE, MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE})
    // Swagger annotations
    @ApiOperation(value = "Render a document using the specified template name data",
            notes = "<p>Render a document using the specified template name data</p>"
            + "<p>The REST service can output a rendered document in HTML or plain text format</p>"
            + "<p>The REST service can consume input data in JSON or XML format</p>"
            + "<p>The rendered document can optionally be emailed if a to email addess and subject line is specified</p>",
            response = String.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "The input data is invalid", response = BusinessLogicError.class)
        ,@ApiResponse(code = 403, message = "Not authorized to call the api")
        ,@ApiResponse(code = 404, message = "The template name is invalid", response = BusinessLogicError.class)
        ,@ApiResponse(code = 406, message = "The input data content type (application/xml or application/json) does not match the content type of the specified template")
    })
    public ResponseEntity renderDocument(
            @PathVariable("templateName")
            @ApiParam(value = "The name of the template file to use for rendering", required = true)
            final String templateName,
            @RequestHeader("content-type")
            @ApiParam(hidden = true)
            final String contentType,
            @RequestParam(name = "email_to", required = false)
            @ApiParam(value = "The comma separated list of email addresses")
            final String emailTo,
            @RequestParam(name = "email_subject", required = false)
            @ApiParam(value = "The email subject of the email")
            final String emailSubject,
            @RequestBody String data,
            @ApiIgnore Principal principal) throws UnsupportedEncodingException {
        // Render the output document
        String renderedDocument = freeMarkerRenderService.render(servletContext, templateName, contentType, data);
        if ((emailTo != null) && (emailSubject != null)) {
            String userName = principal.getName();
            EmailMessage emailMessage = new EmailMessage();
            emailMessage.setUserName(userName);
            emailMessage.setToAddress(emailTo);
            emailMessage.setSubject(emailSubject);
            String base64Body = Base64.getEncoder().encodeToString(renderedDocument.getBytes("utf-8"));
            emailMessage.setBody(base64Body);
            try {
                jmsTemplate.convertAndSend("email_queue", objectMapper.writeValueAsString(emailMessage));
            } catch (JsonProcessingException ex) {
                throw new BusinessLogicException("FMR9");
            }
        }
        return ResponseEntity.ok(renderedDocument);
    }

}
