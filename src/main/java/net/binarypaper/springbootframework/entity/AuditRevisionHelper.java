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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import lombok.extern.java.Log;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;

/**
 * Helper class to perform some common tasks on audit revisions of a database
 * entity class
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 * @param <T> The entity class that has audit revisions
 */
// Lombok annotations
@Log
public class AuditRevisionHelper<T> {

    private final Class<T> entityClass;
    private final String revisionSetMethodName;

    public AuditRevisionHelper(Class<T> entityClass) {
        this.entityClass = entityClass;
        revisionSetMethodName = getAuditRevisionSetMethod();
        if (revisionSetMethodName == null) {
            throw new RuntimeException("The entity class " + entityClass.getName()
                    + " does not have a set method to set an AuditRevision");
        }
    }

    private String getAuditRevisionSetMethod() {
        // Get all the methods of the entity class
        Method[] methods = entityClass.getDeclaredMethods();
        // Loop through the array of methods
        for (Method method : methods) {
            // Get all the data types of the method input parameters
            Class[] parameterTypes = method.getParameterTypes();
            // Only consider methods with 1 input parameter
            if (parameterTypes.length == 1) {
                if (parameterTypes[0].getName().equals(AuditRevision.class.getName())) {
                    return method.getName();
                }
            }
        }
        return null;
    }

    public List<T> getAllAuditRevisions(EntityManager em, Object id) {
        AuditReader reader = AuditReaderFactory.get(em);
        @SuppressWarnings("unchecked")
        List<Object[]> revisions = (List<Object[]>) reader
                .createQuery()
                .forRevisionsOfEntity(entityClass, false, true)
                .add(AuditEntity.id().eq(id))
                .getResultList();
        List<T> entityList = new ArrayList<>();
        for (Object[] revision : revisions) {
            @SuppressWarnings("unchecked")
            T entity = (T) revision[0];
            AuditRevision dbAuditRevision = (AuditRevision) revision[1];
            dbAuditRevision.setRevisionType(revision[2].toString());
            AuditRevision auditRevision = new AuditRevision();
            auditRevision.setRev(dbAuditRevision.getRev());
            auditRevision.setRevisionTimestamp(dbAuditRevision.getRevisionTimestamp());
            auditRevision.setRevisionType(revision[2].toString());
            auditRevision.setUserName(dbAuditRevision.getUserName());
            // Call the revisionMethodName method using reflection in order to
            // set the auditRevision on the entity class
            try {
                Method revisionMethod = entityClass.getMethod(revisionSetMethodName, AuditRevision.class);
                revisionMethod.invoke(entity, auditRevision);
            } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                log.severe(ex.getMessage());
                throw new RuntimeException("The set method of the entity class " + entityClass.getName()
                        + " method to set an AuditRevision could not be called");
            }
            entityList.add(entity);
        }
        return entityList;
    }
}
