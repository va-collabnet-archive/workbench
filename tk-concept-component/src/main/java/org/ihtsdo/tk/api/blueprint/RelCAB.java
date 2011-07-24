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
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelType;
import org.ihtsdo.tk.example.binding.TermAux;
import org.ihtsdo.tk.uuid.UuidT5Generator;

/**
 *
 * @author kec
 */
public class RelCAB extends CreateOrAmendBlueprint {

    public static final UUID relSpecNamespace =
            UUID.fromString("16d79820-5289-11e0-b8af-0800200c9a66");
    private UUID sourceUuid;
    private UUID typeUuid;
    private UUID destUuid;
    private int group;
    private UUID characteristicUuid;
    private UUID refinabilityUuid;

    public RelCAB(
            int sourceNid, int typeNid, int destNid, int group, TkRelType type)
            throws IOException {
        this(Ts.get().getComponent(sourceNid).getPrimUuid(),
                Ts.get().getComponent(typeNid).getPrimUuid(),
                Ts.get().getComponent(destNid).getPrimUuid(),
                group, null, type);
    }

    public RelCAB(
            UUID sourceUuid, UUID typeUuid, UUID destUuid, int group, TkRelType type)
            throws IOException {
        this(sourceUuid, typeUuid, destUuid, group, null, type);
    }

    public RelCAB(
            UUID sourceUuid, UUID typeUuid, UUID destUuid, int group,
            UUID componentUuid, TkRelType type) throws IOException {
        super(componentUuid);
        assert sourceUuid != null;
        assert typeUuid != null;
        assert destUuid != null;
        assert type != null;
        this.sourceUuid = sourceUuid;
        this.typeUuid = typeUuid;
        this.destUuid = destUuid;
        this.group = group;
        TerminologyStoreDI ts = Ts.get();

        switch (type) {
            case STATED_HIERARCHY:
                characteristicUuid =
                        ts.getUuidsForNid(SnomedMetadataRfx.getREL_CH_STATED_RELATIONSHIP_NID()).get(0);
                refinabilityUuid =
                        ts.getUuidsForNid(SnomedMetadataRfx.getREL_NOT_REFINABLE_NID()).get(0);
                break;
            case STATED_ROLE:
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
                        ts.getUuidsForNid(SnomedMetadataRfx.getREL_OPTIONAL_REFINABILITY_NID()).get(0);
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
                setComponentUuid(UuidT5Generator.get(relSpecNamespace,
                        getPrimoridalUuidStr(sourceUuid)
                        + getPrimoridalUuidStr(typeUuid)
                        + getPrimoridalUuidStr(destUuid)
                        + group));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (InvalidCAB ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public UUID getCharacteristicUuid() {
        return characteristicUuid;
    }

    public UUID getDestUuid() {
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

    public int getDestNid() throws IOException {
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
    
    public boolean validate(RelationshipVersionBI version) throws IOException {
        if (version.getStatusNid() != getStatusNid()) {
            return false;
        }
        if (version.getNid() != getComponentNid()) {
            return false;
        }
        if (version.getConceptNid() != getSourceNid()) {
            return false;
        }
        if (version.getTypeNid() != getTypeNid()) {
            return false;
        }
        if (version.getRefinabilityNid() != getRefinabilityNid()) {
            return false;
        }
        if (version.getCharacteristicNid() != getCharacteristicNid()) {
            return false;
        }
        if (version.getDestinationNid() != getDestNid()) {
            return false;
        }
        return true;
    }
}
