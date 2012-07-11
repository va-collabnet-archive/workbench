/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.api.blueprint;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipType;
import org.ihtsdo.tk.uuid.UuidT5Generator;

/**
 *
 * @author kec
 */
public class RelationshipCAB extends CreateOrAmendBlueprint {

    public static final UUID relSpecNamespace =
            UUID.fromString("16d79820-5289-11e0-b8af-0800200c9a66");
    private UUID sourceUuid;
    private UUID typeUuid;
    private UUID destUuid;
    private int group;
    private UUID characteristicUuid;
    private UUID refinabilityUuid;

    public RelationshipCAB(
            int sourceNid, int typeNid, int targetNid, int group, TkRelationshipType relationshipType)
            throws IOException, InvalidCAB, ContradictionException {
        this(Ts.get().getComponent(sourceNid).getPrimUuid(),
                Ts.get().getComponent(typeNid).getPrimUuid(),
                Ts.get().getComponent(targetNid).getPrimUuid(),
                group, null, relationshipType, null, null);
    }

    public RelationshipCAB(
            UUID sourceUuid, UUID typeUuid, UUID targetUuid, int group, TkRelationshipType relationshipType)
            throws IOException, InvalidCAB, ContradictionException {
        this(sourceUuid, typeUuid, targetUuid, group, null, relationshipType, null, null);
    }
    
    public RelationshipCAB(
            int sourceNid, int typeNid, int targetNid, int group, TkRelationshipType relationshipType,
            RelationshipVersionBI relationshipVersion, ViewCoordinate viewCoordinate)throws IOException, InvalidCAB, ContradictionException {
        this(Ts.get().getComponent(sourceNid).getPrimUuid(),
                Ts.get().getComponent(typeNid).getPrimUuid(),
                Ts.get().getComponent(targetNid).getPrimUuid(),
                group, null, relationshipType, relationshipVersion, viewCoordinate);
    }

    public RelationshipCAB(
            UUID sourceUuid, UUID typeUuid, UUID targetUuid, int group,
            TkRelationshipType relationshipType, RelationshipVersionBI relationshipVersion,
            ViewCoordinate viewCoordinate) throws IOException, InvalidCAB, ContradictionException {
        this(sourceUuid, typeUuid, targetUuid, group, null, relationshipType, relationshipVersion, viewCoordinate);
    }

    public RelationshipCAB(
            UUID sourceUuid, UUID typeUuid, UUID targetUuid, int group,
            UUID componentUuid, TkRelationshipType relationshipType, RelationshipVersionBI relationshipVersion,
            ViewCoordinate viewCoordinate) throws IOException, InvalidCAB, ContradictionException {
        super(componentUuid, relationshipVersion, viewCoordinate);
        assert sourceUuid != null;
        assert typeUuid != null;
        assert targetUuid != null;
        assert relationshipType != null;
        this.sourceUuid = sourceUuid;
        this.typeUuid = typeUuid;
        this.destUuid = targetUuid;
        this.group = group;
        TerminologyStoreDI ts = Ts.get();

        switch (relationshipType) {
            case STATED_HIERARCHY:
                characteristicUuid =
                        ts.getUuidsForNid(SnomedMetadataRfx.getREL_CH_STATED_RELATIONSHIP_NID()).get(0);
                refinabilityUuid =
                        ts.getUuidsForNid(SnomedMetadataRfx.getREL_NOT_REFINABLE_NID()).get(0);
                break;
            case STATED_ROLE:
                characteristicUuid =
                        ts.getUuidsForNid(SnomedMetadataRfx.getREL_CH_STATED_RELATIONSHIP_NID()).get(0);
                refinabilityUuid =
                        ts.getUuidsForNid(SnomedMetadataRfx.getREL_OPTIONAL_REFINABILITY_NID()).get(0);
                break;
            case INFERRED_HIERARCY:
                characteristicUuid =
                        ts.getUuidsForNid(SnomedMetadataRfx.getREL_CH_INFERRED_RELATIONSHIP_NID()).get(0);
                refinabilityUuid =
                        ts.getUuidsForNid(SnomedMetadataRfx.getREL_NOT_REFINABLE_NID()).get(0);
                break;
            case QUALIFIER:
                characteristicUuid =
                        ts.getUuidsForNid(SnomedMetadataRfx.getREL_CH_QUALIFIER_CHARACTERISTIC_NID()).get(0);
                refinabilityUuid =
                        ts.getUuidsForNid(SnomedMetadataRfx.getREL_MANDATORY_REFINABILITY_NID()).get(0);
                break;
            case INFERRED_ROLE:
                characteristicUuid =
                        ts.getUuidsForNid(SnomedMetadataRfx.getREL_CH_INFERRED_RELATIONSHIP_NID()).get(0);
                refinabilityUuid =
                        ts.getUuidsForNid(SnomedMetadataRfx.getREL_OPTIONAL_REFINABILITY_NID()).get(0);
                break;
            case HISTORIC:
                characteristicUuid =
                        ts.getUuidsForNid(SnomedMetadataRfx.getREL_HISTORY_HISTORIC_NID()).get(0);
                refinabilityUuid =
                        ts.getUuidsForNid(SnomedMetadataRfx.getREL_NOT_REFINABLE_NID()).get(0);
                break;
        }
        if (getComponentUuid() == null) {
            try {
                recomputeUuid();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (InvalidCAB ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    @Override
    public void recomputeUuid() throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException{
        setComponentUuid(UuidT5Generator.get(relSpecNamespace,
                        getPrimoridalUuidString(sourceUuid)
                        + getPrimoridalUuidString(typeUuid)
                        + getPrimoridalUuidString(destUuid)
                        + group));
        for(RefexCAB annotBp: getAnnotationBlueprints()){
            annotBp.setReferencedComponentUuid(getComponentUuid());
            annotBp.recomputeUuid();
        }
    }

    public UUID getCharacteristicUuid() {
        return characteristicUuid;
    }

    public UUID getTargetUuid() {
        return destUuid;
    }

    public int getGroup() {
        return group;
    }

    public UUID getRefinabilityUuid() {
        return refinabilityUuid;
    }

    public UUID getSourceUuid() {
        return sourceUuid;
    }

    public UUID getTypeUuid() {
        return typeUuid;
    }

    public int getCharacteristicNid() throws IOException {
        return Ts.get().getNidForUuids(characteristicUuid);
    }

    public int getTargetNid() throws IOException {
        return Ts.get().getNidForUuids(destUuid);
    }

    public int getRefinabilityNid() throws IOException {
        return Ts.get().getNidForUuids(refinabilityUuid);
    }

    public int getSourceNid() throws IOException {
        return Ts.get().getNidForUuids(sourceUuid);
    }

    public int getTypeNid() throws IOException {
        return Ts.get().getNidForUuids(typeUuid);
    }
    
    protected void setSourceUuid(UUID sourceNewUuid){
        this.sourceUuid = sourceNewUuid;
    }
    public boolean validate(RelationshipVersionBI relationshipVersion) throws IOException {
        if (relationshipVersion.getStatusNid() != getStatusNid()) {
            return false;
        }
        if (relationshipVersion.getNid() != getComponentNid()) {
            return false;
        }
        if (relationshipVersion.getConceptNid() != getSourceNid()) {
            return false;
        }
        if (relationshipVersion.getTypeNid() != getTypeNid()) {
            return false;
        }
        if (relationshipVersion.getRefinabilityNid() != getRefinabilityNid()) {
            return false;
        }
        if (relationshipVersion.getCharacteristicNid() != getCharacteristicNid()) {
            return false;
        }
        if (relationshipVersion.getTargetNid() != getTargetNid()) {
            return false;
        }
        return true;
    }
}
