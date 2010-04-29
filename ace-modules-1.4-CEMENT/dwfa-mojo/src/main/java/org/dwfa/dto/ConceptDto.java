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
package org.dwfa.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Concept details for exporting.
 */
public class ConceptDto extends BaseConceptDto implements Concept {
    private Map<UUID, Long> conceptId;
    private String fullySpecifiedName;
    private String ctv3Id;
    private String snomedId;
    private boolean isPrimative;
    private List<IdentifierDto> identifierDtos;

    /**
     * Bean constructor.
     */
    public ConceptDto() {
        identifierDtos = new ArrayList<IdentifierDto>();
    }

    /**
     * @return the conceptId
     */
    public Map<UUID, Long> getConceptId() {
        return conceptId;
    }

    /**
     * @param conceptId the conceptId to set
     */
    public void setConceptId(Map<UUID, Long> conceptId) {
        this.conceptId = conceptId;
    }

    /**
     * @return the fullySpecifiedName
     */
    public String getFullySpecifiedName() {
        return fullySpecifiedName;
    }

    /**
     * @param fullySpecifiedName the fullySpecifiedName to set
     */
    public void setFullySpecifiedName(String fullySpecifiedName) {
        this.fullySpecifiedName = fullySpecifiedName;
    }

    /**
     * @return the ctv3Id
     */
    public String getCtv3Id() {
        return ctv3Id;
    }

    /**
     * @param ctv3Id the ctv3Id to set
     */
    public void setCtv3Id(String ctv3Id) {
        this.ctv3Id = ctv3Id;
    }

    /**
     * @return the snomedId
     */
    public String getSnomedId() {
        return snomedId;
    }

    /**
     * @param snomedId the snomedId to set
     */
    public void setSnomedId(String snomedId) {
        this.snomedId = snomedId;
    }

    /**
     * @return the isPrimative
     */
    public boolean isPrimative() {
        return isPrimative;
    }

    /**
     * @param isPrimative the isPrimative to set
     */
    public void setPrimative(boolean isPrimative) {
        this.isPrimative = isPrimative;
    }

    /**
     * @return the identifierDtos
     */
    public List<IdentifierDto> getIdentifierDtos() {
        return identifierDtos;
    }

    /**
     * @param identifierDtos the identifierDtos to set
     */
    public void setIdentifierDtos(List<IdentifierDto> identifierDtos) {
        this.identifierDtos = identifierDtos;
    }

    /**
     * Is this concept active (new or existing) or a inactive concept that was previously released.
     *
     * @return true if this concept is active or a inactive concept that was previously released.
     */
    public boolean isNewActiveOrRetiringLive() {
        return ((!this.isActive() && this.isLive()) || this.isActive());
    }
}
