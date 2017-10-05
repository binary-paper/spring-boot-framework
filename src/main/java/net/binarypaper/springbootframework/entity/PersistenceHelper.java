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
package net.binarypaper.springbootframework.entity;

import java.lang.reflect.Field;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Version;
import javax.validation.ConstraintViolationException;
import lombok.extern.java.Log;
import net.binarypaper.springbootframework.exception.BusinessLogicException;

/**
 * Helper class used by the EJB layer to perform common persistence tasks
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 * @param <T> The entity class being updated
 */
// Lombok annotations
@Log
public class PersistenceHelper<T> {

    private final Class<T> entityClass;
    private final EntityManager em;
    private final HashMap<String, String> constraintsToValidate = new HashMap<>();
    private final List<Field> fields = new LinkedList<>();
    private Field versionField;

    /**
     * Instantiate a new PersistenceHelper of type entityClass in order to
     * perform persistence tasks for the entity class.
     *
     * @param entityClass The entity class being persisted
     * @param em The entity manager to use
     * @param principal The user principal of the currently logged in user
     */
    public PersistenceHelper(Class<T> entityClass, EntityManager em, Principal principal) {
        this.entityClass = entityClass;
        this.em = em;
        CurrentUser.setPrincipal(principal);
    }

    /**
     * Add a database unique constraint to validate when persisting or merging a
     * database entity.
     * <p>
     * A unique constraint name may not be associated with more than one error
     * code.
     *
     * @param constraintName The name of the database unique constraint
     * @param errorMessage The error message to return if the unique constraint
     * is violated
     */
    public void addConstraintValidation(String constraintName, String errorMessage) {
        if (constraintsToValidate.containsKey(constraintName)) {
            throw new RuntimeException("The constraint " + constraintName
                    + " has already been added");
        }
        constraintsToValidate.put(constraintName, errorMessage);
    }

    /**
     * Persist the entity to the database using the specified entity manager
     *
     * @param entity the entity to persist
     * @return The persisted entity
     */
    public T persistEntity(T entity) {
        try {
            em.persist(entity);
            em.flush();
            return entity;
        } catch (ConstraintViolationException ex) {
            // Handle bean validation constraint violation exceptions by wrapping them
            // in a BusinessLogicException, which is a checked exception
            throw new BusinessLogicException(ex.getConstraintViolations());
        } catch (PersistenceException ex) {
            throw handlePersistenceException(ex);
        }
    }

    /**
     * Persist the database entity to the database using the specified entity
     * manager.
     * <p>
     * The updatable fields will be retriever from the update entity and set in
     * the database entity and the update of the database entity will only occur
     * if one or more updatable fields have been updated.
     *
     * @param databaseEntity The database entity to update
     * @param updateEntity The update entity containing the field changes for
     * the update
     * @return The updated database entity
     */
    public T updateEntity(T databaseEntity, T updateEntity) {
        if (fields.isEmpty()) {
            getAllFields(fields, entityClass);
            for (Field field : fields) {
                Version versionAnnotation = field.getAnnotation(Version.class);
                if (versionAnnotation != null) {
                    versionField = field;
                    versionField.setAccessible(true);
                }
            }
        }
        if ((versionField != null) && (!versionsMatch(databaseEntity, updateEntity))) {
            throw new BusinessLogicException("F001");
        }
        // Modify the changed attributes
        for (Field field : fields) {
            Updatable updatable = field.getAnnotation(Updatable.class);
            if (updatable != null) {
                try {
                    field.setAccessible(true);
                    Object updateFieldValue = field.get(updateEntity);
                    field.set(databaseEntity, updateFieldValue);
                } catch (SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                    log.severe(ex.getMessage());
                    throw new RuntimeException("The entity class " + entityClass.getName()
                            + "." + field.getName() + " could not be updated");
                }
            }
        }
        try {
            databaseEntity = em.merge(databaseEntity);
            em.flush();
        } catch (ConstraintViolationException ex) {
            // Handle bean validation constraint violation exceptions by wrapping them
            // in a BusinessLogicException, which is a checked exception
            throw new BusinessLogicException(ex.getConstraintViolations());
        } catch (PersistenceException ex) {
            throw handlePersistenceException(ex);
        }
        if ((versionField != null) && (versionsMatch(databaseEntity, updateEntity))) {
            throw new BusinessLogicException("F002");
        }
        return databaseEntity;
    }

    private List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass());
        }
        return fields;
    }

    private boolean versionsMatch(T databaseEntity, T updateEntity) {
        try {
            Object databaseVersion = versionField.get(databaseEntity);
            Object updateVersion = versionField.get(updateEntity);
            return databaseVersion.equals(updateVersion);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Delete the database entity from the database using the specified primary
     * key.
     *
     * @param entity The entity to delete
     */
    public void deleteEntity(Object entity) {
        try {
            em.remove(entity);
            em.flush();
        } catch (PersistenceException ex) {
            throw handlePersistenceException(ex);
        }
    }

    private BusinessLogicException handlePersistenceException(PersistenceException ex) {
        // Handle database constraint violations by throwing a
        // BusinessLogicException with the correct error code
        for (Throwable t = ex.getCause(); t != null; t = t.getCause()) {
            Set<String> constraintNames = constraintsToValidate.keySet();
            for (String constraintName : constraintNames) {
                if (t.getMessage().contains(constraintName)) {
                    String errorMessage = constraintsToValidate.get(constraintName);
                    return new BusinessLogicException(errorMessage);
                }
            }
        }
        // If the persistence exception could not be handled
        throw ex;
    }

    /**
     * Find an entity by its primary key, but using the specified entity graph
     * for database query optimization and to prevent errors when JSON
     * serialization of lazily loaded objects occurs.
     *
     * @param primaryKey The primary key of the entity to find
     * @param entityGraphName The name of the @NamedEntityGraph to use
     * @return
     * @throws BusinessLogicException
     */
    public T findEntityWithNamedEntityGraph(Object primaryKey, String entityGraphName) throws BusinessLogicException {
        EntityGraph entityGraph = em.getEntityGraph(entityGraphName);
        Map<String, Object> hints = new HashMap<>();
        hints.put("javax.persistence.loadgraph", entityGraph);
        return em.find(entityClass, primaryKey, hints);
    }
}
