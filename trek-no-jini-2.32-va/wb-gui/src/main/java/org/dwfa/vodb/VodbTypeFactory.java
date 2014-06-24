/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.vodb;

import org.dwfa.tapi.TerminologyRuntimeException;

/**
 * Create a new concrete instance of an interface.
 * <p>
 * The created instance type will be determined from by removing the "I_" prefix
 * from the class name and changing the package to "org.dwfa.vodb.types".
 * <p>
 * eg. <code>org.dwfa.ace.api.ebr.I_<b>ThinExtByRefPartConcept</b></code> will
 * resolve to an instance of
 * <code>org.dwfa.vodb.types.<b>ThinExtByRefPartConcept</b></code>
 */
public class VodbTypeFactory {

    /**
     * May be overridden by extending implementations, for example to return a
     * mock class from a testing package.
     */
    protected static String IMPL_PACKAGE = "org.dwfa.vodb.types";

    public static <T> T create(Class<T> t) throws TerminologyRuntimeException {
        try {
            return resolve(t).newInstance();

        } catch (Exception e) {
            throw new TerminologyRuntimeException(e);
        }
    }

    protected static <T> Class<? extends T> resolve(Class<T> t) throws ClassNotFoundException {

        Class<?> targetClass = Class.forName(t.getName()
            .replaceFirst(t.getPackage().getName(), IMPL_PACKAGE)
            .replaceFirst("I_", ""));

        return targetClass.asSubclass(t);
    }
}
