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

import java.util.UUID;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.mojo.PositionDescriptor;

/**
 *
 * @author Matthew Edwards
 */
public abstract class AbstractOriginProcessor implements OriginProcessor {

    protected final I_TermFactory termFactory = LocalVersionedTerminology.get();
    /** The active concept. */
    protected final I_GetConceptData currentConcept;
    /**The release position.*/
    protected final PositionDescriptor releasePosition;
    /**Origins to be Exported.*/
    protected final PositionDescriptor[] originsForExport;

    public AbstractOriginProcessor(I_GetConceptData currentConcept, PositionDescriptor releasePosition, PositionDescriptor[] originsForExport) {
        this.currentConcept = currentConcept;
        this.releasePosition = releasePosition;
        this.originsForExport = originsForExport;
    }

    /**
     * Is the path UUID in the excluded path list
     *
     * @param uuid UUID
     * @return true if and excluded Path UUID
     */
    protected final boolean isExcludedPath(final UUID uuid, final PositionDescriptor[] excludedPositions) {
        boolean excluded = false;
        for (PositionDescriptor positionDescriptor : excludedPositions) {
            if (positionDescriptor.getPath().getUuid().equals(uuid.toString())) {
                excluded = true;
                break;
            }
        }
        return excluded;
    }
}
