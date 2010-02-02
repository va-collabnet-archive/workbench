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
package org.dwfa.maven.sctid;

import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;

public final class SctIdValidator {
    /** Class logger */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /** Signlton instance. */
    private static SctIdValidator instance;

    private SctIdValidator() {
    }

    public static SctIdValidator getInstance() {
        if (instance == null) {
            instance = new SctIdValidator();
        }
        return instance;
    }

    /**
     * Tests if the sctid is correct for the namespace and type.
     *
     * @param sctId String
     * @param namespace NAMESPACE
     * @param type TYPE
     * @return true if a valid sctid.
     */
    public boolean isValidSctId(String sctId, NAMESPACE namespace, TYPE type) {
        boolean isValid = true;

        if (!getSctIdNamespace(sctId).equals(namespace)) {
            logger.severe("Invalid sctid " + sctId + " for namespace " + namespace);
            isValid = false;
        }
        if (!getSctIdType(sctId).equals(type)) {
            logger.severe("Invalid sctid " + sctId + " for type " + type);
            isValid = false;
        }

        return isValid;
    }

    public boolean isValidSctId(String sctId) {
        boolean isValid = true;

        try{
            getSctIdNamespace(sctId);
            getSctIdType(sctId);
        } catch (Throwable e) {
            isValid = false;
        }

        return isValid;
    }
    /**
     * Gets the namespace from the sctid.
     *
     * @param sctId String SctId
     * @return NAMESPACE
     */
    public NAMESPACE getSctIdNamespace(String sctId) {
        int namespace = 0;

        if (!sctId.substring(sctId.length() - 3, sctId.length() - 2).equals(NAMESPACE.SNOMED_META_DATA.getDigits())) {
            namespace = Integer.parseInt(sctId.substring(sctId.length() - 10, sctId.length() - 2));
        }

        return NAMESPACE.fromString("" + namespace);
    }

    /**
     * Get the TYPE from the sctid string
     *
     * Type for our purposes is the second last number ie 0=concept
     * 1=description etc.
     *
     * @param sctId String
     * @return TYPE
     * @throws SQLException
     * @throws NoSuchElementException
     */
    public TYPE getSctIdType(String sctId) {
        int type = Integer.parseInt(sctId.substring(sctId.length() - 2, sctId.length() - 1));

        return TYPE.fromString("" + type);
    }
}
