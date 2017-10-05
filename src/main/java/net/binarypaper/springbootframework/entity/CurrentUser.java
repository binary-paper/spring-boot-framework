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

import java.security.Principal;

/**
 * Utility class used to pass the logged in user principal from the JAX-RS REST
 * resource to the Hibernate Envers RevisionListerer using a ThreadLocal
 * variable.
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
public class CurrentUser {

    private static final ThreadLocal<Principal> PRINCIPAL = new ThreadLocal<>();

    public static void setPrincipal(Principal principal) {
        PRINCIPAL.set(principal);
    }

    public static Principal getPrincipal() {
        return PRINCIPAL.get();
    }
}
