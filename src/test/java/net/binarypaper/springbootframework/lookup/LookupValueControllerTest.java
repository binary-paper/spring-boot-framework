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
package net.binarypaper.springbootframework.lookup;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import net.binarypaper.springbootframework.entity.AuditRevision;
import net.binarypaper.springbootframework.exception.BusinessLogicError;
import net.binarypaper.springbootframework.exception.FieldError;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

/**
 * Integration test for the LookupValueController.
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// Spring annotations
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest
@TestPropertySource(locations = "/application-test.properties")
@AutoConfigureMockMvc
public class LookupValueControllerTest {

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

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void test01() throws Exception {
        // Call a REST method without passing the KEYCLOAK_TOKEN
        mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/lookup-list-name/vehicle-make")
                        .contentType(MediaType.APPLICATION_JSON)
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
        mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/lookup-list-name/vehicle-make")
                        .header("Authorization", "Bearer " + accessToken.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void test03() throws Exception {
        // Add vehicle-make Ford
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle-make");
        lookupValue.setDisplayValue("Ford");
        lookupValue.setActive(Boolean.TRUE);
        String jsonRequest = objectMapper.writerWithView(LookupValue.View.Add.class)
                .writeValueAsString(lookupValue);
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .post("/lookup-values")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        lookupValue = objectMapper.readerWithView(LookupValue.View.All.class)
                .forType(LookupValue.class)
                .readValue(jsonResponse);
        Assert.assertEquals(1L, lookupValue.getId().longValue());
        Assert.assertEquals(0L, lookupValue.getVersion().longValue());
    }

    @Test
    public void test04() throws Exception {
        // Add vehicle-make VW
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle-make");
        lookupValue.setDisplayValue("VW");
        lookupValue.setActive(Boolean.TRUE);
        String jsonRequest = objectMapper.writerWithView(LookupValue.View.Add.class)
                .writeValueAsString(lookupValue);
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .post("/lookup-values")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        lookupValue = objectMapper.readerWithView(LookupValue.View.All.class)
                .forType(LookupValue.class)
                .readValue(jsonResponse);
        Assert.assertEquals(2L, lookupValue.getId().longValue());
        Assert.assertEquals(0L, lookupValue.getVersion().longValue());
    }

    @Test
    public void test05() throws Exception {
        // Add vehicle-model Focus under Ford
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle-model");
        lookupValue.setDisplayValue("Focus");
        lookupValue.setActive(Boolean.TRUE);
        lookupValue.setParentId(1L);
        String jsonRequest = objectMapper.writerWithView(LookupValue.View.Add.class)
                .writeValueAsString(lookupValue);
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .post("/lookup-values")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .param("parent-id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        lookupValue = objectMapper.readerWithView(LookupValue.View.All.class)
                .forType(LookupValue.class)
                .readValue(jsonResponse);
        Assert.assertEquals(3L, lookupValue.getId().longValue());
        Assert.assertEquals(0L, lookupValue.getVersion().longValue());
    }

    @Test
    public void test06() throws Exception {
        // Add vehicle-model Escort under Ford that is inactive
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle-model");
        lookupValue.setDisplayValue("Escort");
        lookupValue.setActive(Boolean.FALSE);
        lookupValue.setParentId(1L);
        String jsonRequest = objectMapper.writerWithView(LookupValue.View.Add.class)
                .writeValueAsString(lookupValue);
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .post("/lookup-values")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .param("parent-id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        lookupValue = objectMapper.readerWithView(LookupValue.View.All.class)
                .forType(LookupValue.class)
                .readValue(jsonResponse);
        Assert.assertEquals(4L, lookupValue.getId().longValue());
        Assert.assertEquals(0L, lookupValue.getVersion().longValue());
        Assert.assertEquals(Boolean.FALSE, lookupValue.getActive());
    }

    @Test
    public void test07() throws Exception {
        // Add vehicle-model Sierra under Ford that is active
        // and has an effective from and effective to date
        Date effectiveFrom = (new GregorianCalendar(2016, Calendar.JANUARY, 1)).getTime();
        Date effectiveTo = (new GregorianCalendar(2016, Calendar.DECEMBER, 31)).getTime();
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle-model");
        lookupValue.setDisplayValue("Sierra");
        lookupValue.setActive(Boolean.TRUE);
        lookupValue.setEffectiveFrom(effectiveFrom);
        lookupValue.setEffectiveTo(effectiveTo);
        lookupValue.setParentId(1L);
        String jsonRequest = objectMapper.writerWithView(LookupValue.View.Add.class)
                .writeValueAsString(lookupValue);
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .post("/lookup-values")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .param("parent-id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        lookupValue = objectMapper.readerWithView(LookupValue.View.All.class)
                .forType(LookupValue.class)
                .readValue(jsonResponse);
        Assert.assertEquals(5L, lookupValue.getId().longValue());
        Assert.assertEquals(0L, lookupValue.getVersion().longValue());
        Assert.assertEquals(Boolean.TRUE, lookupValue.getActive());
        Assert.assertEquals(effectiveFrom, lookupValue.getEffectiveFrom());
        Assert.assertEquals(effectiveTo, lookupValue.getEffectiveTo());
    }

    @Test
    public void test08() throws Exception {
        // Add vehicle-model Polo under VW
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle-model");
        lookupValue.setDisplayValue("Polo");
        lookupValue.setActive(Boolean.TRUE);
        lookupValue.setParentId(2L);
        String jsonRequest = objectMapper.writerWithView(LookupValue.View.Add.class)
                .writeValueAsString(lookupValue);
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .post("/lookup-values")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .param("parent-id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        lookupValue = objectMapper.readerWithView(LookupValue.View.All.class)
                .forType(LookupValue.class)
                .readValue(jsonResponse);
        Assert.assertEquals(6L, lookupValue.getId().longValue());
        Assert.assertEquals(0L, lookupValue.getVersion().longValue());
    }

    @Test
    public void test09() throws Exception {
        // Add a lookup value with a parentId in the query param, but no parentId in the request body
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle-model");
        lookupValue.setDisplayValue("Lazer");
        lookupValue.setActive(Boolean.TRUE);
        String jsonRequest = objectMapper.writerWithView(LookupValue.View.Add.class)
                .writeValueAsString(lookupValue);
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .post("/lookup-values")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .param("parent-id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("0001", businessLogicError.getErrorCode());
        Assert.assertEquals("The parent lookup value id in the URL does not match the parent id in the request body", businessLogicError.getMessage());
    }

    @Test
    public void test10() throws Exception {
        // Add a lookup value with a parentId in the query param, but a different parentId in the request body
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle-model");
        lookupValue.setDisplayValue("Lazer");
        lookupValue.setActive(Boolean.TRUE);
        lookupValue.setParentId(1L);
        String jsonRequest = objectMapper.writerWithView(LookupValue.View.Add.class)
                .writeValueAsString(lookupValue);
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .post("/lookup-values")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .param("parent-id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("0001", businessLogicError.getErrorCode());
        Assert.assertEquals("The parent lookup value id in the URL does not match the parent id in the request body", businessLogicError.getMessage());
    }

    @Test
    public void test11() throws Exception {
        // Add a lookup value with a parentId in the request body, but no parentId in the query param
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle-model");
        lookupValue.setDisplayValue("Lazer");
        lookupValue.setActive(Boolean.TRUE);
        lookupValue.setParentId(1L);
        String jsonRequest = objectMapper.writerWithView(LookupValue.View.Add.class)
                .writeValueAsString(lookupValue);
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .post("/lookup-values")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("0001", businessLogicError.getErrorCode());
        Assert.assertEquals("The parent lookup value id in the URL does not match the parent id in the request body", businessLogicError.getMessage());
    }

    @Test
    public void test12() throws Exception {
        // Add a lookup value with an invalid parentId in the query param and request body
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle-model");
        lookupValue.setDisplayValue("Lazer");
        lookupValue.setActive(Boolean.TRUE);
        lookupValue.setParentId(100L);
        String jsonRequest = objectMapper.writerWithView(LookupValue.View.Add.class)
                .writeValueAsString(lookupValue);
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .post("/lookup-values")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .param("parent-id", "100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("0002", businessLogicError.getErrorCode());
        Assert.assertEquals("The specified parent id is invalid", businessLogicError.getMessage());
    }

    @Test
    public void test13() throws Exception {
        // Add a lookup value with the same lookup list name as its parent
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle-make");
        lookupValue.setDisplayValue("Toyota");
        lookupValue.setActive(Boolean.TRUE);
        lookupValue.setParentId(1L);
        String jsonRequest = objectMapper.writerWithView(LookupValue.View.Add.class)
                .writeValueAsString(lookupValue);
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .post("/lookup-values")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .param("parent-id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("0003", businessLogicError.getErrorCode());
        Assert.assertEquals("A lookup value may not have the same lookup list name as its parent lookup value", businessLogicError.getMessage());
    }

    @Test
    public void test14() throws Exception {
        // Add a lookup value without a parentId that would violate the UC_LOOKUP_LIST_VALUE unique constraint
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle-make");
        lookupValue.setDisplayValue("Ford");
        lookupValue.setActive(Boolean.TRUE);
        String jsonRequest = objectMapper.writerWithView(LookupValue.View.Add.class)
                .writeValueAsString(lookupValue);
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .post("/lookup-values")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("0004", businessLogicError.getErrorCode());
        Assert.assertEquals("The combination of the lookup list name, display value and parent lookup list must be unique", businessLogicError.getMessage());
    }

    @Test
    public void test15() throws Exception {
        // Add a lookup value with a parentId that would violate the UC_LOOKUP_LIST_VALUE unique constraint
        LookupValue lookupValue = new LookupValue();
        lookupValue.setLookupListName("vehicle-model");
        lookupValue.setDisplayValue("Focus");
        lookupValue.setActive(Boolean.TRUE);
        lookupValue.setParentId(1L);
        String jsonRequest = objectMapper.writerWithView(LookupValue.View.Add.class)
                .writeValueAsString(lookupValue);
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .post("/lookup-values")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .param("parent-id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("0004", businessLogicError.getErrorCode());
        Assert.assertEquals("The combination of the lookup list name, display value and parent lookup list must be unique", businessLogicError.getMessage());
    }

    @Test
    public void test16() throws Exception {
        // Add a lookup value constraint violation errors
        LookupValue lookupValue = new LookupValue();
        lookupValue.setDisplayValue("Toyota");
        lookupValue.setActive(Boolean.TRUE);
        String jsonRequest = objectMapper.writerWithView(LookupValue.View.Add.class)
                .writeValueAsString(lookupValue);
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .post("/lookup-values")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertNull(businessLogicError.getErrorCode());
        Assert.assertNull(businessLogicError.getMessage());
        List<FieldError> fieldErrors = businessLogicError.getFieldErrors();
        Assert.assertEquals(1, fieldErrors.size());
        Assert.assertEquals("lookupListName", fieldErrors.get(0).getField());
        Assert.assertEquals("The lookup list name must be specified", fieldErrors.get(0).getMessage());
    }

    @Test
    public void test17() throws Exception {
        // Get a list of lookup values where no content will be found
        mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/lookup-list-name/invalid-name")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void test18() throws Exception {
        // Get a list of lookup values with an invalid effective date
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/lookup-list-name/vehicle-make")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .param("effective-date", "2016-01")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("0007", businessLogicError.getErrorCode());
        Assert.assertEquals("The effective date is invalid", businessLogicError.getMessage());
    }

    @Test
    public void test19() throws Exception {
        // Get list of vehicle make
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/lookup-list-name/vehicle-make")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<LookupValue> lookupValues = objectMapper.readerWithView(LookupValue.View.List.class)
                .forType(objectMapper.getTypeFactory().constructCollectionType(List.class, LookupValue.class))
                .readValue(jsonResponse);
        Assert.assertEquals(2, lookupValues.size());
        LookupValue lookupValue = lookupValues.get(0);
        Assert.assertEquals(1L, lookupValue.getId().longValue());
        Assert.assertEquals("Ford", lookupValue.getDisplayValue());
        lookupValue = lookupValues.get(1);
        Assert.assertEquals(2L, lookupValue.getId().longValue());
        Assert.assertEquals("VW", lookupValue.getDisplayValue());
    }

    @Test
    public void test20() throws Exception {
        // Get list of vehicle model
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/lookup-list-name/vehicle-model")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<LookupValue> lookupValues = objectMapper.readerWithView(LookupValue.View.List.class)
                .forType(objectMapper.getTypeFactory().constructCollectionType(List.class, LookupValue.class))
                .readValue(jsonResponse);
        Assert.assertEquals(4, lookupValues.size());
    }

    @Test
    public void test21() throws Exception {
        // Get list of vehicle model filtering by parentId 1 (Ford)
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/lookup-list-name/vehicle-model")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .param("parent-id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<LookupValue> lookupValues = objectMapper.readerWithView(LookupValue.View.List.class)
                .forType(objectMapper.getTypeFactory().constructCollectionType(List.class, LookupValue.class))
                .readValue(jsonResponse);
        Assert.assertEquals(3, lookupValues.size());
    }

    @Test
    public void test22() throws Exception {
        // Get list of vehicle model filtering by parentId 1 (Ford)
        // and active = TRUE
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/lookup-list-name/vehicle-model")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .param("parent-id", "1")
                        .param("active", "true")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<LookupValue> lookupValues = objectMapper.readerWithView(LookupValue.View.List.class)
                .forType(objectMapper.getTypeFactory().constructCollectionType(List.class, LookupValue.class))
                .readValue(jsonResponse);
        Assert.assertEquals(2, lookupValues.size());
    }

    @Test
    public void test23() throws Exception {
        // Get list of vehicle model filtering by parentId 1 (Ford)
        // and active = TRUE and effectiveDate = 2016-01-01
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/lookup-list-name/vehicle-model")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .param("parent-id", "1")
                        .param("active", "true")
                        .param("effective-date", "2016-01-01")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<LookupValue> lookupValues = objectMapper.readerWithView(LookupValue.View.List.class)
                .forType(objectMapper.getTypeFactory().constructCollectionType(List.class, LookupValue.class))
                .readValue(jsonResponse);
        Assert.assertEquals(2, lookupValues.size());
    }

    @Test
    public void test24() throws Exception {
        // Get list of vehicle model filtering by parentId 1 (Ford)
        // and active = TRUE and effectiveDate = 2015-12-31
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/lookup-list-name/vehicle-model")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .param("parent-id", "1")
                        .param("active", "true")
                        .param("effective-date", "2015-12-31")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<LookupValue> lookupValues = objectMapper.readerWithView(LookupValue.View.List.class)
                .forType(objectMapper.getTypeFactory().constructCollectionType(List.class, LookupValue.class))
                .readValue(jsonResponse);
        Assert.assertEquals(1, lookupValues.size());
    }

    @Test
    public void test25() throws Exception {
        // Get list of vehicle model filtering by parentId 1 (Ford)
        // and active = TRUE and effectiveDate = 2016-12-31
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/lookup-list-name/vehicle-model")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .param("parent-id", "1")
                        .param("active", "true")
                        .param("effective-date", "2016-12-31")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<LookupValue> lookupValues = objectMapper.readerWithView(LookupValue.View.List.class)
                .forType(objectMapper.getTypeFactory().constructCollectionType(List.class, LookupValue.class))
                .readValue(jsonResponse);
        Assert.assertEquals(2, lookupValues.size());
    }

    @Test
    public void test26() throws Exception {
        // Get list of vehicle model filtering by parentId 1 (Ford)
        // and active = TRUE and effectiveDate = 2017-01-01
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/lookup-list-name/vehicle-model")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .param("parent-id", "1")
                        .param("active", "true")
                        .param("effective-date", "2017-01-01")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<LookupValue> lookupValues = objectMapper.readerWithView(LookupValue.View.List.class)
                .forType(objectMapper.getTypeFactory().constructCollectionType(List.class, LookupValue.class))
                .readValue(jsonResponse);
        Assert.assertEquals(1, lookupValues.size());
    }

    @Test
    public void test27() throws Exception {
        // Get list of vehicle model filtering by parentId 2 (VW)
        // and active = TRUE
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/lookup-list-name/vehicle-model")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .param("parent-id", "2")
                        .param("active", "true")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<LookupValue> lookupValues = objectMapper.readerWithView(LookupValue.View.List.class)
                .forType(objectMapper.getTypeFactory().constructCollectionType(List.class, LookupValue.class))
                .readValue(jsonResponse);
        Assert.assertEquals(1, lookupValues.size());
    }

    @Test
    public void test28() throws Exception {
        // Get list of vehicle model filtering by parentId 2 (VW)
        // and active = FALSE
        mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/lookup-list-name/vehicle-model")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .param("parent-id", "2")
                        .param("active", "false")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void test29() throws Exception {
        // Get vehicle make Ford by id 1
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/1")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        LookupValue lookupValue = objectMapper.readerWithView(LookupValue.View.All.class)
                .forType(LookupValue.class)
                .readValue(jsonResponse);
        Assert.assertEquals(1L, lookupValue.getId().longValue());
        Assert.assertEquals("vehicle-make", lookupValue.getLookupListName());
        Assert.assertEquals("Ford", lookupValue.getDisplayValue());
    }

    @Test
    public void test30() throws Exception {
        // Get lookup value with invalid id
        mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/7")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void test31() throws Exception {
        // Update Lookup value Polo
        Date effectiveFrom = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
        Date effectiveTo = new GregorianCalendar(2016, Calendar.DECEMBER, 31).getTime();
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/6")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        LookupValue lookupValue = objectMapper.readerWithView(LookupValue.View.All.class)
                .forType(LookupValue.class)
                .readValue(jsonResponse);
        lookupValue.setActive(Boolean.FALSE);
        lookupValue.setEffectiveFrom(effectiveFrom);
        lookupValue.setEffectiveTo(effectiveTo);
        String jsonRequest = objectMapper.writerWithView(LookupValue.View.Edit.class)
                .writeValueAsString(lookupValue);
        jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .put("/lookup-values/6")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(MockMvcResultMatchers.status().isAccepted())
                .andReturn()
                .getResponse()
                .getContentAsString();
        lookupValue = objectMapper.readerWithView(LookupValue.View.All.class)
                .forType(LookupValue.class)
                .readValue(jsonResponse);
        Assert.assertEquals(1L, lookupValue.getVersion().longValue());
        Assert.assertEquals(Boolean.FALSE, lookupValue.getActive());
        Assert.assertEquals(effectiveFrom, lookupValue.getEffectiveFrom());
        Assert.assertEquals(effectiveTo, lookupValue.getEffectiveTo());
    }

    @Test
    public void test32() throws Exception {
        // Update lookup value with invalid id
        LookupValue lookupValue = new LookupValue();
        lookupValue.setId(7L);
        lookupValue.setVersion(0L);
        lookupValue.setActive(Boolean.TRUE);
        lookupValue.setLookupListName("some-lookup-list");
        lookupValue.setDisplayValue("Some lookup list value");
        String jsonRequest = objectMapper.writerWithView(LookupValue.View.Edit.class
        )
                .writeValueAsString(lookupValue);
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .put("/lookup-values/7")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("0006", businessLogicError.getErrorCode());
        Assert.assertEquals("The lookup value id is invalid", businessLogicError.getMessage());
    }

    @Test
    public void test33() throws Exception {
        // Update lookjup value with id in path not matching id in body
        LookupValue lookupValue = new LookupValue();
        lookupValue.setId(7L);
        lookupValue.setVersion(0L);
        lookupValue.setActive(Boolean.TRUE);
        lookupValue.setLookupListName("some-lookup-list");
        lookupValue.setDisplayValue("Some lookup list value");
        String jsonRequest = objectMapper.writerWithView(LookupValue.View.Edit.class)
                .writeValueAsString(lookupValue);
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .put("/lookup-values/8")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("0005", businessLogicError.getErrorCode());
        Assert.assertEquals("The lookup value id in the URL does not match the id in the request body", businessLogicError.getMessage());
    }

    @Test
    public void test34() throws Exception {
        // Update lookup value with parent id null and create a unique constraint violation
        // Get Vehicle make VW
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/2")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        LookupValue lookupValue = objectMapper.readerWithView(LookupValue.View.All.class)
                .forType(LookupValue.class)
                .readValue(jsonResponse);
        // Change display value VW to Ford
        lookupValue.setDisplayValue("Ford");
        String jsonRequest = objectMapper.writerWithView(LookupValue.View.Edit.class)
                .writeValueAsString(lookupValue);
        jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .put("/lookup-values/2")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("0004", businessLogicError.getErrorCode());
        Assert.assertEquals("The combination of the lookup list name, display value and parent lookup list must be unique", businessLogicError.getMessage());
    }

    @Test
    public void test35() throws Exception {
        // Update lookup value with parent id not null and create a unique constraint violation
        // Get Vehicle make Escort
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/4")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        LookupValue lookupValue = objectMapper.readerWithView(LookupValue.View.All.class)
                .forType(LookupValue.class)
                .readValue(jsonResponse);
        // Change display value Escort to Focus
        lookupValue.setDisplayValue("Focus");
        String jsonRequest = objectMapper.writerWithView(LookupValue.View.Edit.class)
                .writeValueAsString(lookupValue);
        jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .put("/lookup-values/4")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("0004", businessLogicError.getErrorCode());
        Assert.assertEquals("The combination of the lookup list name, display value and parent lookup list must be unique", businessLogicError.getMessage());
    }

    @Test
    public void test36() throws Exception {
        // Update lookup value and create a concurrency issue
        // Get Vehicle make Ford
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/1")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        LookupValue lookupValue = objectMapper.readerWithView(LookupValue.View.All.class)
                .forType(LookupValue.class)
                .readValue(jsonResponse);
        // Change the version from 0 to 1
        Assert.assertEquals(0L, lookupValue.getVersion().longValue());
        lookupValue.setVersion(1L);
        lookupValue.setDisplayValue("Ford updated");
        String jsonRequest = objectMapper.writerWithView(LookupValue.View.Edit.class)
                .writeValueAsString(lookupValue);
        jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .put("/lookup-values/1")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("F001", businessLogicError.getErrorCode());
        Assert.assertEquals("The entity has been updated since it has been retrieved", businessLogicError.getMessage());
    }

    @Test
    public void test37() throws Exception {
        // Update lookup value without changing any of the updateable fields
        // Get Vehicle make Ford
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/1")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        LookupValue lookupValue = objectMapper.readerWithView(LookupValue.View.All.class)
                .forType(LookupValue.class)
                .readValue(jsonResponse);
        // Change the lookup list name which is not updatable from vehicle-make to vehicle-make_updated
        Assert.assertEquals("vehicle-make", lookupValue.getLookupListName());
        lookupValue.setLookupListName("vehicle-make_updated");
        String jsonRequest = objectMapper.writerWithView(LookupValue.View.Edit.class
        )
                .writeValueAsString(lookupValue);
        jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .put("/lookup-values/1")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("F002", businessLogicError.getErrorCode());
        Assert.assertEquals("None of the updatable fields were updated", businessLogicError.getMessage());
    }

    @Test
    public void test38() throws Exception {
        // Delete lookup value Polo
        // Get Vehicle make Ford
        mvc.perform(
                MockMvcRequestBuilders
                        .delete("/lookup-values/6")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void test39() throws Exception {
        // Delete lookup value with an invalid id
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .delete("/lookup-values/7")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("0006", businessLogicError.getErrorCode());
        Assert.assertEquals("The lookup value id is invalid", businessLogicError.getMessage());
    }

    @Test
    public void test40() throws Exception {
        // Delete lookup value with children
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .delete("/lookup-values/1")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("0008", businessLogicError.getErrorCode());
        Assert.assertEquals("A lookup value cannot be deleted if it has child lookup values", businessLogicError.getMessage());
    }

    @Test
    public void test41() throws Exception {
        // Get lookup value revisions for lookup value Polo
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/6/revisions")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<LookupValue> lookupValues = objectMapper.readerWithView(AuditRevision.class)
                .forType(objectMapper.getTypeFactory().constructCollectionType(List.class, LookupValue.class))
                .readValue(jsonResponse);
        Assert.assertEquals(3, lookupValues.size());
        // Get the first lookup value revision
        LookupValue lookupValue = lookupValues.get(0);
        Assert.assertEquals("ADD", lookupValue.getRevision().getRevisionType());
        Assert.assertEquals("test", lookupValue.getRevision().getUserName());
        Assert.assertEquals(Boolean.TRUE, lookupValue.getActive());
        Assert.assertEquals(null, lookupValue.getEffectiveFrom());
        Assert.assertEquals(null, lookupValue.getEffectiveTo());
        // Get the second lookup value revision
        lookupValue = lookupValues.get(1);
        Assert.assertEquals("MOD", lookupValue.getRevision().getRevisionType());
        Assert.assertEquals("test", lookupValue.getRevision().getUserName());
        Assert.assertEquals(Boolean.FALSE, lookupValue.getActive());
        Date effectiveFrom = new GregorianCalendar(2016, Calendar.JANUARY, 1).getTime();
        Date effectiveTo = new GregorianCalendar(2016, Calendar.DECEMBER, 31).getTime();
        Assert.assertEquals(effectiveFrom, lookupValue.getEffectiveFrom());
        Assert.assertEquals(effectiveTo, lookupValue.getEffectiveTo());
        // Get the third lookup value revision
        lookupValue = lookupValues.get(2);
        Assert.assertEquals("DEL", lookupValue.getRevision().getRevisionType());
        Assert.assertEquals("test", lookupValue.getRevision().getUserName());
        Assert.assertEquals(null, lookupValue.getActive());
        Assert.assertEquals(null, lookupValue.getEffectiveFrom());
        Assert.assertEquals(null, lookupValue.getEffectiveTo());
    }

    @Test
    public void test42() throws Exception {
        // Get lookup value revisions for an invalid lookup value id
        mvc.perform(
                MockMvcRequestBuilders
                        .get("/lookup-values/7/revisions")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void test43() throws Exception {
        // Upload a valid CSV file
        File csvFile = new File("src/test/resources/CsvUploadValid.csv");
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .fileUpload("/lookup-values/csv-upload")
                        .file("file", FileUtils.readFileToByteArray(csvFile))
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<LookupValue> lookupValues = objectMapper.readerWithView(LookupValue.View.List.class)
                .forType(objectMapper.getTypeFactory().constructCollectionType(List.class, LookupValue.class))
                .readValue(jsonResponse);
        Assert.assertEquals(12, lookupValues.size());
    }

    @Test
    public void test44() throws Exception {
        // Upload a CSV file without any file parts
        mvc.perform(
                MockMvcRequestBuilders
                        .fileUpload("/lookup-values/csv-upload")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    public void test45() throws Exception {
        // Upload a CSV file with an invalid part
        File csvFile = new File("src/test/resources/CsvUploadValid.csv");
        mvc.perform(
                MockMvcRequestBuilders
                        .fileUpload("/lookup-values/csv-upload")
                        .file("invalidPart", FileUtils.readFileToByteArray(csvFile))
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    public void test46() throws Exception {
        // Upload a CSV file with lookup list name same as parent
        File csvFile = new File("src/test/resources/CsvUploadLookupListNameSameAsParent.csv");
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .fileUpload("/lookup-values/csv-upload")
                        .file("file", FileUtils.readFileToByteArray(csvFile))
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("0010", businessLogicError.getErrorCode());
        Assert.assertEquals("One of the lookup values in the csv file has the same lookup list name as its parent lookup value", businessLogicError.getMessage());
    }

    @Test
    public void test47() throws Exception {
        // Upload a CSV file with duplicate lookup values
        File csvFile = new File("src/test/resources/CsvUploadValid.csv");
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .fileUpload("/lookup-values/csv-upload")
                        .file("file", FileUtils.readFileToByteArray(csvFile))
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("0011", businessLogicError.getErrorCode());
        Assert.assertEquals("At least one of the lookup values in the CSV file already exists", businessLogicError.getMessage());
    }

    @Test
    public void test48() throws Exception {
        // Upload a CSV file with an invalid effective date for a lookup values
        File csvFile = new File("src/test/resources/CsvUploadInvalidDate.csv");
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .fileUpload("/lookup-values/csv-upload")
                        .file("file", FileUtils.readFileToByteArray(csvFile))
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("0013", businessLogicError.getErrorCode());
        Assert.assertEquals("The uploaded CSV file contains an EFFECTIVE_FROM or EFFECTIVE_TO date that is not in the format yyyy-MM-dd", businessLogicError.getMessage());
    }

    @Test
    public void test49() throws Exception {
        // Upload a CSV file with an invalid parent for a lookup values
        File csvFile = new File("src/test/resources/CsvUploadInvalidParent.csv");
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .fileUpload("/lookup-values/csv-upload")
                        .file("file", FileUtils.readFileToByteArray(csvFile))
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("0014", businessLogicError.getErrorCode());
        Assert.assertEquals("One of the records in the CSV file contains a reference to a parent lookup value that does not exist", businessLogicError.getMessage());
    }

    @Test
    public void test50() throws Exception {
        // Upload a CSV file with an ambiguous parent for a lookup values
        File csvFile = new File("src/test/resources/CsvUploadAmbiguousParent.csv");
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .fileUpload("/lookup-values/csv-upload")
                        .file("file", FileUtils.readFileToByteArray(csvFile))
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("0015", businessLogicError.getErrorCode());
        Assert.assertEquals("One of the records in the CSV file contains a reference to a parent lookup value that is ambiguous", businessLogicError.getMessage());
    }

    @Test
    public void test51() throws Exception {
        // Upload a CSV file with an incorrect header
        File csvFile = new File("src/test/resources/CsvUploadIncorrectHeader.csv");
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .fileUpload("/lookup-values/csv-upload")
                        .file("file", FileUtils.readFileToByteArray(csvFile))
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertEquals("0016", businessLogicError.getErrorCode());
        Assert.assertEquals("The CSV file headers are invalid", businessLogicError.getMessage());
    }

    @Test
    public void test52() throws Exception {
        // Upload a CSV file with a bean validation constraint violation
        File csvFile = new File("src/test/resources/CsvUploadConstraintViolation.csv");
        String jsonResponse = mvc.perform(
                MockMvcRequestBuilders
                        .fileUpload("/lookup-values/csv-upload")
                        .file("file", FileUtils.readFileToByteArray(csvFile))
                        .header("Authorization", "Bearer " + ACCESS_TOKEN.getToken())
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();
        BusinessLogicError businessLogicError = objectMapper.readValue(jsonResponse, BusinessLogicError.class);
        Assert.assertNull(businessLogicError.getErrorCode());
        Assert.assertNull(businessLogicError.getMessage());
        List<FieldError> fieldErrors = businessLogicError.getFieldErrors();
        Assert.assertEquals(1, fieldErrors.size());
        Assert.assertEquals("lookupListName", fieldErrors.get(0).getField());
        Assert.assertEquals("The lookup list name must be between 3 and 100 characters long", fieldErrors.get(0).getMessage());
    }

}
