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
 * Description details for exporting
 */
public class DescriptionDto extends ConceptDto {
    private UUID descriptionId;
    private UUID typeId;
    private UUID rf2TypeId;
    private UUID languageId;
    private UUID caseSignificanceId;
    private String description;
    private Character initialCapitalStatusCode;
    private Character descriptionTypeCode;
    private String languageCode;

    /**
     * Bean constructor.
     */
    public DescriptionDto() {

    }

    /**
     * @return the descriptionId
     */
    public UUID getDescriptionId() {
        return descriptionId;
    }

    /**
     * @param descriptionId the descriptionId to set
     */
    public void setDescriptionId(UUID descriptionId) {
        this.descriptionId = descriptionId;
    }

    /**
     * @return the typeId
     */
    public UUID getTypeId() {
        return typeId;
    }

    /**
     * @param typeId the typeId to set
     */
    public void setTypeId(UUID typeId) {
        this.typeId = typeId;
    }

    /**
     * @return the rf2TypeId
     */
    public final UUID getRf2TypeId() {
        return rf2TypeId;
    }

    /**
     * @param rf2TypeId the rf2TypeId to set
     */
    public final void setRf2TypeId(UUID rf2TypeId) {
        this.rf2TypeId = rf2TypeId;
    }

    /**
     * @return the languageId
     */
    public UUID getLanguageId() {
        return languageId;
    }

    /**
     * @param languageId the languageId to set
     */
    public void setLanguageId(UUID languageId) {
        this.languageId = languageId;
    }

    /**
     * @return the caseSignificanceId
     */
    public UUID getCaseSignificanceId() {
        return caseSignificanceId;
    }

    /**
     * @param caseSignificanceId the caseSignificanceId to set
     */
    public void setCaseSignificanceId(UUID caseSignificanceId) {
        this.caseSignificanceId = caseSignificanceId;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the initialCapitalStatusCode
     */
    public Character getInitialCapitalStatusCode() {
        return initialCapitalStatusCode;
    }

    /**
     * @param initialCapitalStatusCode the initialCapitalStatusCode to set
     */
    public void setInitialCapitalStatusCode(Character initialCapitalStatusCode) {
        this.initialCapitalStatusCode = initialCapitalStatusCode;
    }

    /**
     * @return the descriptionTypeCode
     */
    public Character getDescriptionTypeCode() {
        return descriptionTypeCode;
    }

    /**
     * @param descriptionTypeCode the descriptionTypeCode to set
     */
    public void setDescriptionTypeCode(Character descriptionTypeCode) {
        this.descriptionTypeCode = descriptionTypeCode;
    }

    /**
     * @return the languageCode
     */
    public String getLanguageCode() {
        return languageCode;
    }

    /**
     * @param languageCode the languageCode to set
     */
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
}
