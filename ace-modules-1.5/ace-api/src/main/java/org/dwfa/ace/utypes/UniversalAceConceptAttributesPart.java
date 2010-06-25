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
package org.dwfa.ace.utypes;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

public class UniversalAceConceptAttributesPart implements Serializable, I_VersionComponent {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private Collection<UUID> pathId;
    private long time;
    private Collection<UUID> conceptStatus;
    private boolean defined;

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ConceptAttributePart#getPathId()
     */
    public Collection<UUID> getPathId() {
        return pathId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ConceptAttributePart#setPathId(int)
     */
    public void setPathId(Collection<UUID> pathId) {
        this.pathId = pathId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ConceptAttributePart#getConceptStatus()
     */
    public Collection<UUID> getConceptStatus() {
        return conceptStatus;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ConceptAttributePart#setConceptStatus(int)
     */
    public void setConceptStatus(Collection<UUID> conceptStatus) {
        this.conceptStatus = conceptStatus;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ConceptAttributePart#isDefined()
     */
    public boolean isDefined() {
        return defined;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ConceptAttributePart#setDefined(boolean)
     */
    public void setDefined(boolean defined) {
        this.defined = defined;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ConceptAttributePart#getVersion()
     */
    public long getTime() {
        return time;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_ConceptAttributePart#setVersion(int)
     */
    public void setTime(long version) {
        this.time = version;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " defined: " + defined + " status: " + conceptStatus + " path: "
            + pathId + " time: " + time + " (" + new Date(time) + ")";
    }

    public Collection<UUID> getStatusId() {
        return getConceptStatus();
    }

    public void setStatusId(Collection<UUID> status) {
        setConceptStatus(status);
    }

}
