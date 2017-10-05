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

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import lombok.extern.java.Log;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import springfox.documentation.annotations.ApiIgnore;
import net.binarypaper.springbootframework.entity.AuditRevision;
import net.binarypaper.springbootframework.entity.DatedEntity;
import net.binarypaper.springbootframework.exception.BusinessLogicException;
import net.binarypaper.springbootframework.entity.AuditRevisionHelper;
import net.binarypaper.springbootframework.entity.PersistenceHelper;
import net.binarypaper.springbootframework.exception.BusinessLogicError;
import net.binarypaper.springbootframework.SwaggerConfig;

/**
 * A REST Web Service controller for managing Lookup Values
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// Spring annotations
@RestController
@RequestMapping(path = "lookup-values", produces = {MediaType.APPLICATION_JSON_VALUE})
// Security annotations
@RolesAllowed("view-lookup-values")
// Swagger annotations
@Api(tags = {"Lookup Values"}, authorizations = {
    @Authorization(value = SwaggerConfig.O_AUTH_2)
})
// Lombok annotations
@Log
public class LookupValueController {

    @PersistenceContext
    private EntityManager em;

    // Spring annotations
    @PostMapping
    @Transactional
    // Security annotations
    @RolesAllowed("manage-lookup-values")
    // Jackson annotations
    @JsonView(LookupValue.View.All.class)
    // Swagger annotations
    @ApiOperation(value = "Add a lookup value",
            notes = "Add a lookup value",
            code = 201,
            response = LookupValue.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "The input data is invalid", response = BusinessLogicError.class)
    })
    public ResponseEntity<LookupValue> addLookupValue(
            @RequestParam(value = "parent-id", required = false)
            @ApiParam(value = "The id of the parent lookup value") Long parentId,
            @RequestBody
            @JsonView(LookupValue.View.Add.class) LookupValue lookupValue,
            @ApiIgnore Principal principal) {
        if (parentId != null) {
            if (!parentId.equals(lookupValue.getParentId())) {
                throw new BusinessLogicException("0001");
            }
            LookupValue parent = em.find(LookupValue.class, parentId);
            if (parent == null) {
                throw new BusinessLogicException("0002");
            }
            if (lookupValue.getLookupListName().equals(parent.getLookupListName())) {
                throw new BusinessLogicException("0003");
            }
            lookupValue.setParent(parent);
            parent.getChildren().add(lookupValue);
        } else {
            // If the parent id in the query string is null,
            // but a parent id is specified in the lookupList
            if (lookupValue.getParentId() != null) {
                throw new BusinessLogicException("0001");
            }
        }
        PersistenceHelper<LookupValue> persistenceHelper = new PersistenceHelper<>(LookupValue.class, em, principal);
        persistenceHelper.addConstraintValidation("UC_LOOKUP_LIST_VALUE", "0004");
        lookupValue = persistenceHelper.persistEntity(lookupValue);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(lookupValue.getId())
                .toUri();
        return ResponseEntity.created(location).body(lookupValue);
    }

    // Spring annotations
    @GetMapping("lookup-list-name/{lookup-list-name}")
    // Jackson annotations
    @JsonView(LookupValue.View.List.class)
    // Swagger annotations
    @ApiOperation(value = "Get all lookup values for the lookup list name",
            notes = "Get all lookup values for the lookup list name",
            code = 200,
            responseContainer = "List",
            response = LookupValue.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "No lookup values to return")
        ,@ApiResponse(code = 400, message = "The input data is invalid", response = BusinessLogicError.class)
    })
    public ResponseEntity<List<LookupValue>> getLookupValuesByLookupListName(
            @PathVariable("lookup-list-name")
            @ApiParam(value = "The name of the lookup list", required = true)
            final String lookupListName,
            @RequestParam(name = "parent-id", required = false)
            @ApiParam(value = "The id of the parent lookup value")
            final Long parentId,
            @RequestParam(name = "active", required = false)
            @ApiParam(value = "The active status of the lookup value")
            final Boolean active,
            @RequestParam(name = "effective-date", required = false)
            @ApiParam(value = "The effective date in the format yyyy-MM-dd by which lookup values will be filtered")
            final String effectiveDateString) {
        TypedQuery<LookupValue> query;
        if (parentId == null) {
            query = em.createNamedQuery("LookupValue.findByLookupListName", LookupValue.class);
        } else {
            query = em.createNamedQuery("LookupValue.findByLookupListNameAndParentId", LookupValue.class);
            query.setParameter("parentId", parentId);
        }
        query.setParameter("lookupListName", lookupListName);
        List<LookupValue> lookupValues = query.getResultList();
        lookupValues = LookupValue.filterByActiveStatus(lookupValues, active);
        if (effectiveDateString != null) {
            Date effectiveDate = DatedEntity.parseDate(effectiveDateString, "0007");
            lookupValues = LookupValue.filterByEffectiveDate(lookupValues, effectiveDate);
        }
        if (lookupValues.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(lookupValues);
    }

    // Spring annotations
    @GetMapping("{lookup-value-id}")
    // Jackson annotations
    @JsonView(LookupValue.View.All.class)
    // Swagger annotations
    @ApiOperation(value = "Get a lookup value by id",
            notes = "Get a lookup value by id",
            code = 200,
            response = LookupValue.class
    )
    public ResponseEntity<LookupValue> getLookupValueById(
            @PathVariable("lookup-value-id")
            @ApiParam(value = "The id of the lookup value", required = true)
            final Long lookupValueId) {
        LookupValue lookupValue = em.find(LookupValue.class, lookupValueId);
        if (lookupValue == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(lookupValue);
    }

    // Spring annotations
    @PutMapping(path = "{lookup-value-id}")
    @Transactional
    // Security annotations
    @RolesAllowed("manage-lookup-values")
    // Jackson annotations
    @JsonView({LookupValue.View.All.class})
    // Swagger annotations
    @ApiOperation(value = "Update lookup value",
            notes = "Update lookup value",
            code = 202,
            response = LookupValue.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "The input data is invalid", response = BusinessLogicError.class)
    })
    public ResponseEntity<LookupValue> updateLookupValue(
            @PathVariable("lookup-value-id")
            @ApiParam(value = "The id of the lookup value", required = true)
            final Long lookupValueId,
            @RequestBody
            @JsonView(LookupValue.View.Edit.class) LookupValue lookupValue,
            @ApiIgnore Principal principal) {
        if (!lookupValueId.equals(lookupValue.getId())) {
            throw new BusinessLogicException("0005");
        }
        LookupValue fromDB = em.find(LookupValue.class, lookupValue.getId());
        if (fromDB == null) {
            throw new BusinessLogicException("0006");
        }
        PersistenceHelper<LookupValue> persistenceHelper = new PersistenceHelper<>(LookupValue.class, em, principal);
        persistenceHelper.addConstraintValidation("UC_LOOKUP_LIST_VALUE", "0004");
        fromDB = persistenceHelper.updateEntity(fromDB, lookupValue);
        return ResponseEntity.accepted().body(fromDB);
    }

    // Spring annotations
    @DeleteMapping("{lookup-value-id}")
    @Transactional
    // Security annotations
    @RolesAllowed("delete-lookup-values")
    // Swagger annotations
    @ApiOperation(value = "Delete a lookup value by id",
            notes = "Delete a lookup value by id",
            code = 200,
            response = String.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "The lookup value could not be deleted", response = BusinessLogicError.class)
        ,@ApiResponse(code = 404, message = "No lookup value to remove")
    })
    public ResponseEntity deleteLookupValueById(
            @PathVariable("lookup-value-id")
            @ApiParam(value = "The id of the lookup value")
            final Long lookupValueId,
            @ApiIgnore Principal principal) {
        LookupValue lookupValue = em.find(LookupValue.class, lookupValueId);
        if (lookupValue == null) {
            throw new BusinessLogicException("0006");
        }
        if (lookupValue.getParent() != null) {
            lookupValue.getParent().removeChild(lookupValue);
        }
        PersistenceHelper<LookupValue> persistenceHelper = new PersistenceHelper<>(LookupValue.class, em, principal);
        persistenceHelper.addConstraintValidation("FK_LOOKUP_VALUE_PARENT", "0008");
        persistenceHelper.deleteEntity(lookupValue);
        return ResponseEntity.ok().build();
    }

    // Spring annotations
    @GetMapping("{lookup-value-id}/revisions")
    // Security annotations
    @RolesAllowed("view-audit-revisions")
    // Jackson annotations
    @JsonView(AuditRevision.class)
    // Swagger annotations
    @ApiOperation(value = "Get a list of audit database revisions for a given lookup value id",
            notes = "Get a list of audit database revisions for a given lookup value id",
            code = 200,
            responseContainer = "List",
            response = LookupValue.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "No lookup value revisions to return")
    })
    public ResponseEntity getLookupValueRevisions(
            @PathVariable("lookup-value-id")
            @ApiParam(value = "The id of the lookup value", required = true)
            final Long lookupValueId) {
        AuditRevisionHelper<LookupValue> auditRevisionHelper = new AuditRevisionHelper<>(LookupValue.class);
        List<LookupValue> revisions = auditRevisionHelper.getAllAuditRevisions(em, lookupValueId);
        if (revisions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(revisions);
    }

    // Spring annotations
    @PostMapping(path = "/csv-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    // Security annotations
    @RolesAllowed("manage-lookup-values")
    // Jackson annotations
    @JsonView(LookupValue.View.List.class)
    // Swagger annotations
    @ApiOperation(value = "Upload a CSV file containing lookup values to be added",
            notes = "Upload a CSV file containing lookup values to be added. "
            + "The header record of the CSV file should be: "
            + "LOOKUP-LIST-NAME, DISPLAY-VALUE, ACTIVE, EFFECTIVE-FROM, EFFECTIVE-TO, PARENT-LOOKUP-LIST-NAME, PARENT-DISPLAY-VALUE",
            code = 200,
            responseContainer = "List",
            response = LookupValue.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "The uploaded file is invalid", response = BusinessLogicError.class)
    })
    public ResponseEntity uploadCsvFile(
            @RequestPart("file")
            @ApiParam(value = "The CSV file to upload", type = "file", required = true) MultipartFile file,
            @ApiIgnore Principal principal) {
        List<LookupValue> lookupValues = new ArrayList<>();
        if (file.isEmpty()) {
            throw new BusinessLogicException("0009");
        }
        try {
            // Read the file bytes with Apache Commons CSV
            Reader reader = new InputStreamReader(file.getInputStream());
            Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(reader);
            for (CSVRecord record : records) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                LookupValue lookupValue = new LookupValue();
                lookupValue.setLookupListName(record.get("LOOKUP-LIST-NAME").trim());
                lookupValue.setDisplayValue(record.get("DISPLAY-VALUE").trim());
                lookupValue.setActive(Boolean.parseBoolean(record.get("ACTIVE").trim()));
                if (!record.get("EFFECTIVE-FROM").trim().isEmpty()) {
                    Date effectiveFromDate = dateFormat.parse(record.get("EFFECTIVE-FROM").trim());
                    lookupValue.setEffectiveFrom(effectiveFromDate);
                }
                if (!record.get("EFFECTIVE-TO").trim().isEmpty()) {
                    Date effectiveToDate = dateFormat.parse(record.get("EFFECTIVE-TO").trim());
                    lookupValue.setEffectiveTo(effectiveToDate);
                }
                if (!record.get("PARENT-LOOKUP-LIST-NAME").trim().isEmpty()
                        && !record.get("PARENT-DISPLAY-VALUE").trim().isEmpty()) {
                    TypedQuery<LookupValue> query = em.createNamedQuery("LookupValue.findByLookupListNameAndDisplayValue", LookupValue.class);
                    query.setParameter("lookupListName", record.get("PARENT-LOOKUP-LIST-NAME").trim());
                    query.setParameter("displayValue", record.get("PARENT-DISPLAY-VALUE").trim());
                    LookupValue parent = query.getSingleResult();
                    if (lookupValue.getLookupListName().equals(parent.getLookupListName())) {
                        throw new BusinessLogicException("0010");
                    }
                    parent.addChild(lookupValue);
                }
                PersistenceHelper<LookupValue> persistenceHelper = new PersistenceHelper<>(LookupValue.class, em, principal);
                persistenceHelper.addConstraintValidation("UC_LOOKUP_LIST_VALUE", "0011");
                lookupValue = persistenceHelper.persistEntity(lookupValue);
                lookupValues.add(lookupValue);
            }
            reader.close();
        } catch (IOException ex) {
            throw new BusinessLogicException("0012");
        } catch (ParseException ex) {
            throw new BusinessLogicException("0013");
        } catch (NoResultException ex) {
            throw new BusinessLogicException("0014");
        } catch (NonUniqueResultException ex) {
            throw new BusinessLogicException("0015");
        } catch (IllegalArgumentException ex) {
            throw new BusinessLogicException("0016");
        }
        return ResponseEntity.ok(lookupValues);
    }
}
