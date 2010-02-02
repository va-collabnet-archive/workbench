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

import java.util.UUID;

/**
 *  Refset details for export
 */
public class ExtensionDto extends ConceptDto {
    private String value;
    private UUID memberId;
    private UUID concept1Id;
    private UUID concept2Id;
    private UUID concept3Id;

    public ExtensionDto() {

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
    public UUID getConcept1Id() {
        return concept1Id;
    }

    /**
     * @param concept1Id the concept1Id to set
     */
    public void setConcept1Id(UUID concept1Id) {
        this.concept1Id = concept1Id;
    }

    /**
     * @return the concept2Id
     */
    public UUID getConcept2Id() {
        return concept2Id;
    }

    /**
     * @param concept2Id the concept2Id to set
     */
    public void setConcept2Id(UUID concept2Id) {
        this.concept2Id = concept2Id;
    }

    /**
     * @return the concept3Id
     */
    public UUID getConcept3Id() {
        return concept3Id;
    }

    /**
     * @param concept3Id the concept3Id to set
     */
    public void setConcept3Id(UUID concept3Id) {
        this.concept3Id = concept3Id;
    }
}
