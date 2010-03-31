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

import java.util.Map;
import java.util.UUID;

/**
 *  Refset details for export
 */
public class ExtensionDto extends ConceptDto implements Comparable<ExtensionDto> {
    private String value;
    private UUID memberId;
    private Map<UUID, Long> referencedConceptId;
    private Map<UUID, Long> concept1Id;
    private Map<UUID, Long> concept2Id;
    private Map<UUID, Long> concept3Id;
    private boolean isClinical;

    public ExtensionDto() {
        isClinical = true;
    }

    /**
     * @return the memberId
     */
    public UUID getMemberId() {
        return memberId;
    }

    /**
     * @param memberId the memberId to set
     */
    public void setMemberId(UUID memberId) {
        this.memberId = memberId;
    }

    /**
     * @return the referencedConceptId
     */
    public Map<UUID, Long> getReferencedConceptId() {
        return referencedConceptId;
    }

    /**
     * @param referencedConceptId the referencedConceptId to set
     */
    public void setReferencedConceptId(Map<UUID, Long> referencedConceptId) {
        this.referencedConceptId = referencedConceptId;
    }

    /**
     * @return the valueId
     */
    public String getValue() {
        return value;
    }

    /**
     * @param valueId the valueId to set
     */
    public void setValue(String valueId) {
        this.value = valueId;
    }

    /**
     * @return the concept1Id
     */
    public Map<UUID, Long> getConcept1Id() {
        return concept1Id;
    }

    /**
     * @param concept1Id the concept1Id to set
     */
    public void setConcept1Id(Map<UUID, Long> concept1Id) {
        this.concept1Id = concept1Id;
    }

    /**
     * @return the concept2Id
     */
    public Map<UUID, Long> getConcept2Id() {
        return concept2Id;
    }

    /**
     * @param concept2Id the concept2Id to set
     */
    public void setConcept2Id(Map<UUID, Long> concept2Id) {
        this.concept2Id = concept2Id;
    }

    /**
     * @return the concept3Id
     */
    public Map<UUID, Long> getConcept3Id() {
        return concept3Id;
    }

    /**
     * @param concept3Id the concept3Id to set
     */
    public void setConcept3Id(Map<UUID, Long> concept3Id) {
        this.concept3Id = concept3Id;
    }

    /**
     * @return the isClinical
     */
    public boolean isClinical() {
        return isClinical;
    }

    /**
     *
     * @param isClinicalToSet boolean
     */
    public void setIsClinical(boolean isClinicalToSet) {
        isClinical = isClinicalToSet;
    }

    @Override
    public int compareTo(ExtensionDto extensionDto) {
        if (this.getMemberId().equals(extensionDto.getMemberId())) {
            return 0;
        } else {
            return this.getMemberId().compareTo(extensionDto.getMemberId());
        }
    }
}
