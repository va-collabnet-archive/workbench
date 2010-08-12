/*
 *  Copyright 2010 International Health Terminology Standards Development  *  Organisation..
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.dwfa.mojo.export;

import java.io.IOException;
import org.dwfa.tapi.TerminologyException;

/**
 *
 * @author Matthew Edwards
 */
public interface StatusChecker {
    /**
     * Checks if the Status equals the Concept.ACTIVE or Current
     * or is a child of Concept.ACTIVE
     *
     * @param statusNid int
     * @return boolean true if the statusNid is active
     * @throws TerminologyException DB error
     * @throws IOException DB error
     */
    boolean isActive(final int statusNid) throws IOException, TerminologyException;

    /**
     * Checks if the Status equals ACTIVE, Current, Pending Move, Concept Retired, Moved Elsewhere
     *
     * @param statusNid int
     * @return boolean true if the statusNid is active
     * @throws TerminologyException DB error
     * @throws IOException DB error
     */
    boolean isDescriptionActive(final int statusNid);

}
