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
package net.binarypaper.springbootframework.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import javax.mail.internet.MimeMessage;
import net.binarypaper.springbootframework.exception.BusinessLogicError;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * Integration test for the RenderNotificationController.
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// Spring annotations
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest
@TestPropertySource(locations = "/application-test.properties")
@AutoConfigureMockMvc
public class RenderNotificationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String KEYCLOAK_SERVER_URL = "http://localhost:8180/auth";
    private static final String REALM = "demo";
    private static final String CLIENT_ID = "swagger-ui";
    private static final String USER_NAME = "test";
    private static final String PASSOWRD = "test";
    private static AccessTokenResponse ACCESS_TOKEN;

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.ALL);

    @BeforeClass
    public static void setUpClass() {
        ACCESS_TOKEN = Keycloak
                .getInstance(KEYCLOAK_SERVER_URL, REALM, USER_NAME, PASSOWRD, CLIENT_ID)
                .tokenManager()
                .getAccessToken();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Test
    public void test01() throws Exception {
        // Call a REST method without passing the KEYCLOAK_TOKEN
        String inputJson = "{'firstName': 'Albert', 'surname': 'Einstein'}";
        mvc.perform(
                MockMvcRequestBuilders
                        .post("/render-notification/BasicJSON.html")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_HTML)
                        .content(inputJson)
        )
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void test02() throws Exception {
        AccessTokenResponse accessToken = Keycloak
                .getInstance(KEYCLOAK_SERVER_URL, REALM, "noroles", "noroles", CLIENT_ID)
                .tokenManager()
                .getAccessToken();
        // Call a REST method with a user that has no roles
        String inputJson = "{'firstName': 'Albert', 'surname': 'Einstein'}";
        mvc.perform(
                MockMvcRequestBuilders
                        .post("/render-notification/BasicJSON.html")
                        .header("Authorization", "Bearer " + accessToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_HTML)
                        .content(inputJson)
        )
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void test03() throws Exception {
        // Render JSON to HTML
        String inputJson = "{'firstName': 'Albert', 'surname': 'Einstein'}";
        String outputHTML = mvc.perform(
                MockMvcRequestBuilders
                        .post("/render-notification/BasicJSON.html")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_HTML)
                        .content(inputJson)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assert.assertTrue(outputHTML.contains("Hi Albert Einstein,"));
    }

    @Test
    public void test04() throws Exception {
        // Render XML to HTML
        String inputXml = "<root><firstName>Isaac</firstName><surname>Newton</surname></root>";
        String outputHTML = mvc.perform(
                MockMvcRequestBuilders
                        .post("/render-notification/BasicXML.html")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_XML)
                        .accept(MediaType.TEXT_HTML)
                        .content(inputXml)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assert.assertTrue(outputHTML.contains("Hi Isaac Newton,"));
    }

    @Test
    public void test05() throws Exception {
        // Render JSON to Plain Text
        String inputJson = "{'firstName': 'Thomas', 'surname': 'Edison'}";
        String outputHTML = mvc.perform(
                MockMvcRequestBuilders
                        .post("/render-notification/BasicJSON.txt")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_PLAIN)
                        .content(inputJson)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assert.assertTrue(outputHTML.contains("Hello Thomas Edison,"));
    }

    @Test
    public void test06() throws Exception {
        // Render XML to Plain Text
        String inputXml = "<root><firstName>Nikola</firstName><surname>Tesla</surname></root>";
        String outputHTML = mvc.perform(
                MockMvcRequestBuilders
                        .post("/render-notification/BasicXML.txt")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_XML)
                        .accept(MediaType.TEXT_PLAIN)
                        .content(inputXml)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assert.assertTrue(outputHTML.contains("Hello Nikola Tesla,"));
    }

    @Test
    public void test07() throws Exception {
        // Render invalid template
        String inputJson = "{'firstName': 'Edwin', 'surname': 'Hubble'}";
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .post("/render-notification/InvalidTemplate.html")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(inputJson)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("FMR2", businessLogicError.getErrorCode());
        Assert.assertEquals("The FreeMarker template could not be parsed", businessLogicError.getMessage());
    }

    @Test
    public void test08() throws Exception {
        // Render with required surname element is not provided in the input JSON
        String inputJson = "{'firstName': 'Albert'}";
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .post("/render-notification/BasicJSON.html")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(inputJson)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("FMR3", businessLogicError.getErrorCode());
        Assert.assertEquals("The template contains a reference to a data element that is not provided", businessLogicError.getMessage());
    }

    @Test
    public void test09() throws Exception {
        // Render with an invalid template name
        String inputJson = "{'firstName': 'Albert', 'surname': 'Einstein'}";
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .post("/render-notification/InvalidTemplateName.html")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(inputJson)
        )
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("FMR4", businessLogicError.getErrorCode());
        Assert.assertEquals("The template name is invalid", businessLogicError.getMessage());
    }

    @Test
    public void test10() throws Exception {
        // Render with invalid JSON will cause FreeMarker runtime error
        String inputJson = "Invalid JSON";
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .post("/render-notification/BasicJSON.html")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(inputJson)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("FMR5", businessLogicError.getErrorCode());
        Assert.assertTrue(businessLogicError.getMessage().startsWith("A FreeMarker runtime error occurred."));
    }

    @Test
    public void test11() throws Exception {
        // Render with invalid XML will cause a SAXException
        String inputXml = "Invalid XML";
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .post("/render-notification/BasicXML.html")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_XML)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(inputXml)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("FMR6", businessLogicError.getErrorCode());
        Assert.assertEquals("The input XML data is invalid", businessLogicError.getMessage());
    }

    @Test
    public void test12() throws Exception {
        // Render JSON to HTML and Email
        String inputJson = "{'firstName': 'Johannes', 'surname': 'Kepler'}";
        String outputHTML = mvc.perform(
                MockMvcRequestBuilders
                        .post("/render-notification/BasicJSON.html")
                        .param("email_to", "test@example.com")
                        .param("email_subject", "Test JSON to HTML Email")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_HTML)
                        .content(inputJson)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assert.assertTrue(outputHTML.contains("Hi Johannes Kepler,"));
        // Wait for max 5s for 1 email to arrive
        // WaitForIncomingEmail() is useful if you're sending stuff asynchronously in a separate thread
        Assert.assertTrue(greenMail.waitForIncomingEmail(5000, 1));
        MimeMessage[] emails = greenMail.getReceivedMessages();
        Assert.assertEquals(1, emails.length);
        Assert.assertEquals("Test JSON to HTML Email", emails[0].getSubject());
        Assert.assertTrue(GreenMailUtil.getBody(emails[0]).contains("Hi Johannes Kepler,"));
    }

    @Test
    public void test13() throws Exception {
        // Render XML to HTML and Email
        String inputXml = "<root><firstName>Galileo</firstName><surname>Galilei</surname></root>";
        String outputHTML = mvc.perform(
                MockMvcRequestBuilders
                        .post("/render-notification/BasicXML.html")
                        .param("email_to", "test@example.com")
                        .param("email_subject", "Test XML to HTML Email")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_XML)
                        .accept(MediaType.TEXT_HTML)
                        .content(inputXml)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assert.assertTrue(outputHTML.contains("Hi Galileo Galilei,"));
        // Wait for max 5s for 1 email to arrive
        // WaitForIncomingEmail() is useful if you're sending stuff asynchronously in a separate thread
        Assert.assertTrue(greenMail.waitForIncomingEmail(5000, 1));
        MimeMessage[] emails = greenMail.getReceivedMessages();
        Assert.assertEquals(1, emails.length);
        Assert.assertEquals("Test XML to HTML Email", emails[0].getSubject());
        Assert.assertTrue(GreenMailUtil.getBody(emails[0]).contains("Hi Galileo Galilei,"));
    }

    @Test
    public void test14() throws Exception {
        // Render JSON to Plain Text and Email
        String inputJson = "{'firstName': 'Charles', 'surname': 'Darwin'}";
        String outputHTML = mvc.perform(
                MockMvcRequestBuilders
                        .post("/render-notification/BasicJSON.txt")
                        .param("email_to", "test@example.com")
                        .param("email_subject", "Test JSON to Plain Text Email")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_PLAIN)
                        .content(inputJson)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assert.assertTrue(outputHTML.contains("Hello Charles Darwin,"));
        // Wait for max 5s for 1 email to arrive
        // WaitForIncomingEmail() is useful if you're sending stuff asynchronously in a separate thread
        Assert.assertTrue(greenMail.waitForIncomingEmail(5000, 1));
        MimeMessage[] emails = greenMail.getReceivedMessages();
        Assert.assertEquals(1, emails.length);
        Assert.assertEquals("Test JSON to Plain Text Email", emails[0].getSubject());
        Assert.assertTrue(GreenMailUtil.getBody(emails[0]).contains("Hello Charles Darwin,"));
    }

    @Test
    public void test15() throws Exception {
        // Render XML to Plain Text and Email
        String inputXml = "<root><firstName>Marie</firstName><surname>Curie</surname></root>";
        String outputHTML = mvc.perform(
                MockMvcRequestBuilders
                        .post("/render-notification/BasicXML.txt")
                        .param("email_to", "test@example.com")
                        .param("email_subject", "Test XML to Plain Text Email")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_XML)
                        .accept(MediaType.TEXT_PLAIN)
                        .content(inputXml)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Assert.assertTrue(outputHTML.contains("Hello Marie Curie,"));
        // Wait for max 5s for 1 email to arrive
        // WaitForIncomingEmail() is useful if you're sending stuff asynchronously in a separate thread
        Assert.assertTrue(greenMail.waitForIncomingEmail(5000, 1));
        MimeMessage[] emails = greenMail.getReceivedMessages();
        Assert.assertEquals(1, emails.length);
        Assert.assertEquals("Test XML to Plain Text Email", emails[0].getSubject());
        Assert.assertTrue(GreenMailUtil.getBody(emails[0]).contains("Hello Marie Curie,"));
    }

}
