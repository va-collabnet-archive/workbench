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

/**
 * Container for all the details for a component, that is the core details plus all concept extensions.
 */
public class ComponentDto {
    private List<ConceptDto> conceptDtos = new ArrayList<ConceptDto>(0);
    private List<DescriptionDto> descriptionDtos = new ArrayList<DescriptionDto>(0);
    private List<RelationshipDto> relationshipDos = new ArrayList<RelationshipDto>(0);
    private List<ExtensionDto> conceptExtensionDtos = new ArrayList<ExtensionDto>(0);
    private List<ExtensionDto> descriptionExtensionDtos = new ArrayList<ExtensionDto>(0);
    private List<ExtensionDto> relationshipExtensionDtos = new ArrayList<ExtensionDto>(0);

    /**
     * Bean constructor.
     */
    public ComponentDto() {

    }

    /**
     * @return the conceptDto
     */
    public List<ConceptDto> getConceptDtos() {
        return conceptDtos;
    }

    /**
     * @param conceptDto the conceptDto to set
     */
    public void setConceptDto(List<ConceptDto> conceptDtos) {
        this.conceptDtos = conceptDtos;
    }

    /**
     * @return the descriptionDtos
     */
    public List<DescriptionDto> getDescriptionDtos() {
        return descriptionDtos;
    }

    /**
     * @param descriptionDtos the descriptionDtos to set
     */
    public void setDescriptionDtos(List<DescriptionDto> descriptionDtos) {
        this.descriptionDtos = descriptionDtos;
    }

    /**
     * @return the relationshipDos
     */
    public List<RelationshipDto> getRelationshipDtos() {
        return relationshipDos;
    }

    /**
     * @param relationshipDos the relationshipDos to set
     */
    public void setRelationshipDtos(List<RelationshipDto> relationshipDos) {
        this.relationshipDos = relationshipDos;
    }

    /**
     * @return the conceptExtensionDtos
     */
    public List<ExtensionDto> getConceptExtensionDtos() {
        return conceptExtensionDtos;
    }

    /**
     * @param conceptExtensionDtos the conceptExtensionDtos to set
     */
    public void setConceptExtensionDtos(List<ExtensionDto> conceptExtensionDtos) {
        this.conceptExtensionDtos = conceptExtensionDtos;
    }

    /**
     * @return the descriptionExtensionDtos
     */
    public List<ExtensionDto> getDescriptionExtensionDtos() {
        return descriptionExtensionDtos;
    }

    /**
     * @param descriptionExtensionDtos the descriptionExtensionDtos to set
     */
    public void setDescriptionExtensionDtos(List<ExtensionDto> descriptionExtensionDtos) {
        this.descriptionExtensionDtos = descriptionExtensionDtos;
    }

    /**
     * @return the relationshipExtensionDtos
     */
    public List<ExtensionDto> getRelationshipExtensionDtos() {
        return relationshipExtensionDtos;
    }

    /**
     * @param relationshipExtensionDtos the relationshipExtensionDtos to set
     */
    public void setRelationshipExtensionDtos(List<ExtensionDto> relationshipExtensionDtos) {
        this.relationshipExtensionDtos = relationshipExtensionDtos;
    }
}
