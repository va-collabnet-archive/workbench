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
package org.dwfa.maven.transform;

import java.util.UUID;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.maven.Transform;
import org.dwfa.maven.transform.AbstractTransform;

/**
 * Transforms a constant to a NID.
 * 
 */
public class UuidFromAceAuxillary extends AbstractTransform {

    /**
     * The param which needs to be mapped to a UUID.
     */
    public String param;

    /**
     * Transforms a constant to a UUID.
     * 
     * @param input The input string (corresponds to one field in a
     *            table/file).
     * @return String A string containing the result of the transformation.
     * @throws Exception Throws any exception caused by execution of the
     *             transform.
     */
    public final String transform(final String input) throws Exception {
        ArchitectonicAuxiliary.Concept c = ArchitectonicAuxiliary.Concept.valueOf(param);
        UUID uid = c.getUids().iterator().next();
        if (getChainedTransform() != null) {
            return setLastTransform(getChainedTransform().transform(uid.toString()));
        } else {
            return setLastTransform(uid.toString());
        }

    }

    /**
     * Sets up the transform.
     * 
     * @param transformer Reference to caller of this transform.
     */
    public final void setupImpl(final Transform transformer) {
    }
}
