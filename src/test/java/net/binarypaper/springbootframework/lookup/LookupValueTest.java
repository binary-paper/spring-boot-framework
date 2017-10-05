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
package net.binarypaper.springbootframework.lookup;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for the LookupValue entity class.
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
public class LookupValueTest {

    private static Validator validator;
    private LookupValue lookupValue;

    //<editor-fold defaultstate="collapsed" desc="Test Initialization">
    @BeforeClass
    public static void setUpClass() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        lookupValue = new LookupValue();
        lookupValue.setId(1L);
        lookupValue.setLookupListName("vehicle-make");
        lookupValue.setDisplayValue("Ford");
        lookupValue.setActive(Boolean.TRUE);
        LookupValue child = new LookupValue();
        child.setId(2L);
        child.setLookupListName("vehicle-model");
        child.setDisplayValue("Escort");
        child.setActive(Boolean.TRUE);
        lookupValue.addChild(child);
    }

    @After
    public void tearDown() {
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Bean Validation Tests">
    @Test
    public void validLookupValue() {
        Set<ConstraintViolation<LookupValue>> violations = validator.validate(lookupValue);
        Assert.assertEquals(0, violations.size());
    }

    @Test
    public void lookupListNameNotNull() {
        lookupValue.setLookupListName(null);
        Set<ConstraintViolation<LookupValue>> violations = validator.validate(lookupValue);
        Assert.assertEquals(1, violations.size());
        Assert.assertEquals("The lookup list name must be specified",
                violations.iterator().next().getMessage());
    }

    @Test
    public void lookupListNameSizeMin() {
        lookupValue.setLookupListName("12");
        Set<ConstraintViolation<LookupValue>> violations = validator.validate(lookupValue);
        Assert.assertEquals(1, violations.size());
        Assert.assertEquals("The lookup list name must be between 3 and 100 characters long",
                violations.iterator().next().getMessage());
    }

    @Test
    public void lookupListNameSizeMax() {
        lookupValue.setLookupListName("12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901");
        Set<ConstraintViolation<LookupValue>> violations = validator.validate(lookupValue);
        Assert.assertEquals(1, violations.size());
        Assert.assertEquals("The lookup list name must be between 3 and 100 characters long",
                violations.iterator().next().getMessage());
    }

    @Test
    public void lookupListNamePattern() {
        lookupValue.setLookupListName("123#");
        Set<ConstraintViolation<LookupValue>> violations = validator.validate(lookupValue);
        Assert.assertEquals(1, violations.size());
        Assert.assertEquals("The lookup list name may only contain the characters a-z, A-Z, 0-9 and a hyphen (-)",
                violations.iterator().next().getMessage());
    }

    @Test
    public void displayValueNotNull() {
        lookupValue.setDisplayValue(null);
        Set<ConstraintViolation<LookupValue>> violations = validator.validate(lookupValue);
        Assert.assertEquals(1, violations.size());
        Assert.assertEquals("The display value must be specified",
                violations.iterator().next().getMessage());
    }

    @Test
    public void activeNotNull() {
        lookupValue.setActive(null);
        Set<ConstraintViolation<LookupValue>> violations = validator.validate(lookupValue);
        Assert.assertEquals(1, violations.size());
        Assert.assertEquals("The active status must be specified",
                violations.iterator().next().getMessage());
    }

    @Test
    public void effectiveDateValid() {
        Calendar effectiveFrom = new GregorianCalendar(2017, 7, 20);
        lookupValue.setEffectiveFrom(effectiveFrom.getTime());
        Calendar effectiveTo = new GregorianCalendar(2016, 7, 19);
        lookupValue.setEffectiveTo(effectiveTo.getTime());
        Set<ConstraintViolation<LookupValue>> violations = validator.validate(lookupValue);
        Assert.assertEquals(1, violations.size());
        Assert.assertEquals("The effective to date must be after the effective from date",
                violations.iterator().next().getMessage());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Business Method Tests">
    @Test
    public void addChild() {
        LookupValue child = new LookupValue();
        child.setId(2L);
        child.setLookupListName("vehicle-model");
        child.setDisplayValue("Focus");
        child.setActive(Boolean.TRUE);
        lookupValue.addChild(child);
        Set<ConstraintViolation<LookupValue>> violations = validator.validate(lookupValue);
        Assert.assertEquals(0, violations.size());
        Assert.assertEquals(lookupValue, child.getParent());
        Assert.assertEquals(2, lookupValue.getChildren().size());
        Assert.assertEquals(child, lookupValue.getChildren().get(1));
    }

    @Test
    public void removeChild() {
        LookupValue child = lookupValue.getChildren().get(0);
        lookupValue.removeChild(child);
        Set<ConstraintViolation<LookupValue>> violations = validator.validate(lookupValue);
        Assert.assertEquals(0, violations.size());
        Assert.assertNull(child.getParent());
        Assert.assertEquals(0, lookupValue.getChildren().size());
    }
    //</editor-fold>
}
