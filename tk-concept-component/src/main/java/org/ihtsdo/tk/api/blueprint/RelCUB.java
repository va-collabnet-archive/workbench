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
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelType;
import org.ihtsdo.tk.example.binding.TermAux;
import org.ihtsdo.tk.uuid.UuidT5Generator;

/**
 *
 * @author kec
 */
public class RelCUB extends CreateOrUpdateBlueprint {

    public static final UUID relSpecNamespace =
            UUID.fromString("16d79820-5289-11e0-b8af-0800200c9a66");
    private UUID componentUuid;
    private UUID sourceUuid;
    private UUID typeUuid;
    private UUID destUuid;
    private int group;
    private UUID characteristicUuid;
    private UUID refinabilityUuid;

    public RelCUB(
            int sourceNid, int typeNid, int destNid, int group, TkRelType type)
            throws IOException {
        this(Ts.get().getComponent(sourceNid).getPrimUuid(),
                Ts.get().getComponent(typeNid).getPrimUuid(),
                Ts.get().getComponent(destNid).getPrimUuid(),
                group, null, type);
    }

    public RelCUB(
            UUID sourceUuid, UUID typeUuid, UUID destUuid, int group, TkRelType type)
            throws IOException {
        this(sourceUuid, typeUuid, destUuid, group, null, type);
    }

    public RelCUB(
            UUID sourceUuid, UUID typeUuid, UUID destUuid, int group,
            UUID componentUuid, TkRelType type) throws IOException {

        this.componentUuid = componentUuid;
        this.sourceUuid = sourceUuid;
        this.typeUuid = typeUuid;
        this.destUuid = destUuid;
        this.group = group;

        switch (type) {
            case STATED_HIERARCHY:
                characteristicUuid =
                        TermAux.REL_STATED_CHAR.getStrict(Ts.get().getMetadataVC()).getPrimUuid();
                refinabilityUuid =
                        TermAux.REL_NOT_REFINABLE.getStrict(Ts.get().getMetadataVC()).getPrimUuid();
                break;
            case STATED_ROLE:
                characteristicUuid =
                        TermAux.REL_STATED_CHAR.getStrict(Ts.get().getMetadataVC()).getPrimUuid();
                refinabilityUuid =
                        TermAux.REL_OPTIONALLY_REFINABLE.getStrict(Ts.get().getMetadataVC()).getPrimUuid();
                break;
            case INFERRED_HIERARCY:
                characteristicUuid =
                        TermAux.REL_INFERED_CHAR.getStrict(Ts.get().getMetadataVC()).getPrimUuid();
                refinabilityUuid =
                        TermAux.REL_NOT_REFINABLE.getStrict(Ts.get().getMetadataVC()).getPrimUuid();
                break;
            case QUALIFIER:
                characteristicUuid =
                        TermAux.REL_QUALIFIER_CHAR.getStrict(Ts.get().getMetadataVC()).getPrimUuid();
                refinabilityUuid =
                        TermAux.REL_OPTIONALLY_REFINABLE.getStrict(Ts.get().getMetadataVC()).getPrimUuid();
                break;
            case INFERRED_ROLE:
                characteristicUuid =
                        TermAux.REL_INFERED_CHAR.getStrict(Ts.get().getMetadataVC()).getPrimUuid();
                refinabilityUuid =
                        TermAux.REL_OPTIONALLY_REFINABLE.getStrict(Ts.get().getMetadataVC()).getPrimUuid();
                break;
            case HISTORIC:
                characteristicUuid =
                        TermAux.REL_HISTORIC.getStrict(Ts.get().getMetadataVC()).getPrimUuid();
                refinabilityUuid =
                        TermAux.REL_NOT_REFINABLE.getStrict(Ts.get().getMetadataVC()).getPrimUuid();
                break;
        }
        if (componentUuid == null) {
            try {
                componentUuid = UuidT5Generator.get(relSpecNamespace,
                        getPrimoridalUuidStr(sourceUuid)
                        + getPrimoridalUuidStr(typeUuid)
                        + getPrimoridalUuidStr(destUuid)
                        + group);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (InvalidCUB ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public UUID getCharacteristicUuid() {
        return characteristicUuid;
    }

    public UUID getComponentUuid() {
        return componentUuid;
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

    public int getComponentNid() throws IOException {
        return Ts.get().getNidForUuids(componentUuid);
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
}
