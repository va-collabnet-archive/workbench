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
package org.dwfa.ace.refset;

public class ConceptRefsetInclusionDetails {

    private int conceptId;
    private int inclusionTypeId;
    private int inclusionReasonId;
    private int distance;

    public int getConceptId() {
        return conceptId;
    }

    public void setConceptId(int conceptId) {
        this.conceptId = conceptId;
    }

    public int getInclusionTypeId() {
        return inclusionTypeId;
    }

    public void setInclusionTypeId(int inclusionTypeId) {
        this.inclusionTypeId = inclusionTypeId;
    }

    public int getInclusionReasonId() {
        return inclusionReasonId;
    }

    public void setInclusionReasonId(int inclusionReasonId) {
        this.inclusionReasonId = inclusionReasonId;
    }

    public ConceptRefsetInclusionDetails(int conceptId, int inclusionTypeId, int inclusionReasonId, int distance) {
        super();
        this.conceptId = conceptId;
        this.inclusionTypeId = inclusionTypeId;
        this.inclusionReasonId = inclusionReasonId;
        this.distance = distance;

    }

    @Override
    public String toString() {
        return "" + conceptId;
    }

    @Override
    public int hashCode() {
        return conceptId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ConceptRefsetInclusionDetails)) {
            return false;
        } else {
            ConceptRefsetInclusionDetails c = (ConceptRefsetInclusionDetails) obj;
            if (c.getConceptId() == conceptId) {
                return true;
            } else {
                return false;
            }
        }
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

}
