/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.tk.query.parts;

/**
 *
 * The Enumeration <code>TOKENS</code> represents types of query statements.
 */
public enum TOKENS {
    /**
     * Represents added relationship.
     */
    ADDED_RELATIONSHIP,
    /**
     * Represents changed relationship status.
     */
    CHANGED_RELATIONSHIP_STATUS,
    /**
     * Represents relationship characteristic.
     */
    REL_CHARACTERISTIC,
    /**
     * Represents relationship status.
     */
    REL_STATUS,
    /**
     * Represents added description.
     */
    ADDED_DESCRIPTION,
    /**
     * Represents changed description status.
     */
    CHANGED_DESCRIPTION_STATUS,
    /**
     * Represents description status.
     */
    DESC_STATUS,
    /**
     * Represents changed concept definition.
     */
    CHANGED_DEFINED,
    /**
     * Represented changed relationship group.
     */
    CHANGED_REL_GROUP;
}
