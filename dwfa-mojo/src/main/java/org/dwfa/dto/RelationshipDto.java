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
 * Relationship details for exporting.
 */
public class RelationshipDto extends ConceptDto {
    private UUID sourceId;
    private UUID destinationId;
    private UUID relationshipGroupId;
    private UUID typeId;
    private UUID characteristicTypeId;
    private UUID modifierId;
    private boolean isRefinable;
    private Character relationshipGroupCode;
    private Character characteristicTypeCode;

    /**
     * Bean constructor.
     */
    public RelationshipDto() {

    }

    /**
     * @return the relationshipId
     */
    public UUID getRelationshipId() {
        return getConceptId();
    }

    /**
     * @param relationshipId the relationshipId to set
     */
    public void setRelationshipId(UUID relationshipId) {
        setConceptId(relationshipId);
    }

    /**
     * @return the sourceId
     */
    public UUID getSourceId() {
        return sourceId;
    }

    /**
     * @param sourceId the sourceId to set
     */
    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
    }

    /**
     * @return the destinationid
     */
    public UUID getDestinationId() {
        return destinationId;
    }

    /**
     * @param destinationid the destinationid to set
     */
    public void setDestinationId(UUID destinationid) {
        this.destinationId = destinationid;
    }

    /**
     * @return the relationshipGroupId
     */
    public UUID getRelationshipGroupId() {
        return relationshipGroupId;
    }

    /**
     * @param relationshipGroupId the relationshipGroupId to set
     */
    public void setRelationshipGroupId(UUID relationshipGroupId) {
        this.relationshipGroupId = relationshipGroupId;
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
     * @return the characteristicTypeId
     */
    public UUID getCharacteristicTypeId() {
        return characteristicTypeId;
    }

    /**
     * @param characteristicTypeId the characteristicTypeId to set
     */
    public void setCharacteristicTypeId(UUID characteristicTypeId) {
        this.characteristicTypeId = characteristicTypeId;
    }

    /**
     * @return the modifierId
     */
    public UUID getModifierId() {
        return modifierId;
    }

    /**
     * @param modifierId the modifierId to set
     */
    public void setModifierId(UUID modifierId) {
        this.modifierId = modifierId;
    }

    /**
     * @return the isRefinable
     */
    public boolean isRefinable() {
        return isRefinable;
    }

    /**
     * @param isRefinable the isRefinable to set
     */
    public void setRefinable(boolean isRefinable) {
        this.isRefinable = isRefinable;
    }

    /**
     * @return the relationshipGroupCode
     */
    public Character getRelationshipGroupCode() {
        return relationshipGroupCode;
    }

    /**
     * @param relationshipGroupCode the relationshipGroupCode to set
     */
    public void setRelationshipGroupCode(Character relationshipGroupCode) {
        this.relationshipGroupCode = relationshipGroupCode;
    }

    /**
     * @return the characteristicTypeCode
     */
    public Character getCharacteristicTypeCode() {
        return characteristicTypeCode;
    }

    /**
     * @param characteristicTypeCode the characteristicTypeCode to set
     */
    public void setCharacteristicTypeCode(Character characteristicTypeCode) {
        this.characteristicTypeCode = characteristicTypeCode;
    }
}
