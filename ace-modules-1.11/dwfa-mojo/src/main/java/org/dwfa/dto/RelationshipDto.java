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
 * Relationship details for exporting.
 */
public class RelationshipDto extends ConceptDto {
    private UUID sourceId;
    private Map<UUID, Long> destinationId;
    private UUID typeId;
    private UUID characteristicTypeId;
    private UUID modifierId;
    private UUID RefinabilityId;
    private Character refinableCode;
    private Integer relationshipGroup;
    private Character characteristicTypeCode;

    /**
     * Bean constructor.
     */
    public RelationshipDto() {

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
    public Map<UUID, Long> getDestinationId() {
        return destinationId;
    }

    /**
     * @param destinationid the destinationid to set
     */
    public void setDestinationId(Map<UUID, Long> destinationid) {
        this.destinationId = destinationid;
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
     * @return the refinabilityId
     */
    public final UUID getRefinabilityId() {
        return RefinabilityId;
    }

    /**
     * @param refinabilityId the refinabilityId to set
     */
    public final void setRefinabilityId(UUID refinabilityId) {
        RefinabilityId = refinabilityId;
    }

    /**
     * @return the isRefinable
     */
    public Character getRefinable() {
        return refinableCode;
    }

    /**
     * @param isRefinable the isRefinable to set
     */
    public void setRefinable(Character refinableCode) {
        this.refinableCode = refinableCode;
    }

    /**
     * @return the relationshipGroupCode
     */
    public Integer getRelationshipGroup() {
        return relationshipGroup;
    }

    /**
     * @param relationshipGroupCode the relationshipGroupCode to set
     */
    public void setRelationshipGroup(Integer relationshipGroupCode) {
        this.relationshipGroup = relationshipGroupCode;
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
