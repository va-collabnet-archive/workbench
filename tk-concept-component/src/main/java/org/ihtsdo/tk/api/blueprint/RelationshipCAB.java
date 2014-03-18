/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
 * The Class RelationshipCAB contains methods for creating a relationship
 * blueprint. This blueprint can be constructed into a type of
 * <code>RelationshipChronicleBI</code>. This is the preferred method for
 * updating or creating new descriptions.
 *
 * @see TerminologyBuilderBI
 * @see RelationshipChronicleBI
 *
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

    /**
     * Instantiates a new relationship blueprint using nids values.
     *
     * @param sourceNid the nid of the source concept
     * @param typeNid the nid of the relationship type
     * @param targetNid the nid of the target concept
     * @param group the int representing the relationship group, set to 0 if
     * relationship is not grouped
     * @param relationshipType the tk relationship type specifying the
     * relationship characteristic type. This value is used to determine the
     * relationship refinability type.
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * view coordinate
     */
    public RelationshipCAB(
            int sourceNid, int typeNid, int targetNid, int group, TkRelationshipType relationshipType)
            throws IOException, InvalidCAB, ContradictionException {
        this(Ts.get().getComponent(sourceNid).getPrimUuid(),
                Ts.get().getComponent(typeNid).getPrimUuid(),
                Ts.get().getComponent(targetNid).getPrimUuid(),
                group, null, relationshipType, null, null);
    }

    /**
     * Instantiates a new relationship blueprint using uuids values.
     *
     * @param sourceUuid the uuid of the source concept
     * @param typeUuid the uuid of the relationship type
     * @param targetUuid the uuid of the target concept
     * @param group the int representing the relationship group, set to 0 if
     * relationship is not grouped
     * @param relationshipType the tk relationship type specifying the
     * relationship characteristic type. This value is used to determine the
     * relationship refinability type.
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * view coordinate
     */
    public RelationshipCAB(
            UUID sourceUuid, UUID typeUuid, UUID targetUuid, int group, TkRelationshipType relationshipType)
            throws IOException, InvalidCAB, ContradictionException {
        this(sourceUuid, typeUuid, targetUuid, group, null, relationshipType, null, null);
    }

    /**
     * Instantiates a new relationship blueprint using nid values and a given
     * <code>relationshipVersion</code>.
     *
     * @param sourceNid the nid of the source concept
     * @param typeNid the nid of the relationship type
     * @param targetNid the nid of the target concept
     * @param group the int representing the relationship group, set to 0 if
     * relationship is not grouped
     * @param relationshipType the tk relationship type specifying the
     * relationship characteristic type. This value is used to determine the
     * relationship refinability type.
     * @param relationshipVersion the relationship version to use as a pattern
     * @param viewCoordinate the view coordinate specifying which versions are
     * active and inactive
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * view coordinate
     */
    public RelationshipCAB(
            int sourceNid, int typeNid, int targetNid, int group, TkRelationshipType relationshipType,
            RelationshipVersionBI relationshipVersion, ViewCoordinate viewCoordinate) throws IOException, InvalidCAB, ContradictionException {
        this(Ts.get().getComponent(sourceNid).getPrimUuid(),
                Ts.get().getComponent(typeNid).getPrimUuid(),
                Ts.get().getComponent(targetNid).getPrimUuid(),
                group, null, relationshipType, relationshipVersion, viewCoordinate);
    }

    /**
     * Instantiates a new relationship blueprint using uuid values and a given
     * <code>relationshipVersion</code>.
     *
     * @param sourceUuid the uuid of the source concept
     * @param typeUuid the uuid of the relationship type
     * @param targetUuid the uuid of the target concept
     * @param group the int representing the relationship group, set to 0 if
     * relationship is not grouped
     * @param relationshipType the tk relationship type specifying the
     * relationship characteristic type. This value is used to determine the
     * relationship refinability type.
     * @param relationshipVersion the relationship version to use as a pattern
     * @param viewCoordinate the view coordinate specifying which versions are
     * active and inactive
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * view coordinate
     */
    public RelationshipCAB(
            UUID sourceUuid, UUID typeUuid, UUID targetUuid, int group,
            TkRelationshipType relationshipType, RelationshipVersionBI relationshipVersion,
            ViewCoordinate viewCoordinate) throws IOException, InvalidCAB, ContradictionException {
        this(sourceUuid, typeUuid, targetUuid, group, null, relationshipType, relationshipVersion, viewCoordinate);
    }

    /**
     * Instantiates a new relationship blueprint using uuid values and a given
     * <code>relationshipVersion</code>. Can specify the uuid to be associated
     * with the new relationship.
     *
     * @param sourceUuid the uuid of the source concept
     * @param typeUuid the uuid of the relationship type
     * @param targetUuid the uuid of the target concept
     * @param group the int representing the relationship group, set to 0 if
     * relationship is not grouped
     * @param componentUuid the uuid representing the new relationship
     * @param relationshipType the tk relationship type specifying the
     * relationship characteristic type. This value is used to determine the
     * relationship refinability type.
     * @param relationshipVersion the relationship version to use as a pattern
     * @param viewCoordinate the view coordinate specifying which versions are
     * active and inactive
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * view coordinate
     */
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

    /**
     * Computes the uuid for this relationship based on the source concept uuid,
     * relationship type uuid, and target concept uuid. Recomputes the uuids for
     * dependent annotations.
     *
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception
     * has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding
     * exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is found for a
     * give position or view coordinate
     */
    @Override
    public void recomputeUuid() throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException, InvalidCAB, ContradictionException {
        setComponentUuid(UuidT5Generator.get(relSpecNamespace,
                getPrimoridalUuidString(sourceUuid)
                + getPrimoridalUuidString(typeUuid)
                + getPrimoridalUuidString(destUuid)
                + group));
        for (RefexCAB annotBp : getAnnotationBlueprints()) {
            annotBp.setReferencedComponentUuid(getComponentUuid());
            annotBp.recomputeUuid();
        }
    }

    /**
     * Checks if this relationship blueprint is equal to the given object.
     * Compares source concept, relationship type, target concept, and group
     * number.
     *
     * @param o the other relationship blueprint to check for equality
     * @return <code>true</code> if the specified relationship blueprint is
     * equal to this relationship blueprint
     */
    @Override
    public boolean equals(Object o) {
        if (RelationshipCAB.class.isAssignableFrom(o.getClass())) {
            RelationshipCAB another = (RelationshipCAB) o;
            if (this.sourceUuid.equals(another.sourceUuid)
                    && this.typeUuid.equals(another.typeUuid)
                    && this.destUuid.equals(another.destUuid)
                    && this.group == another.group) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the uuid of the relationship characteristic.
     *
     * @return the relationship characteristic uuid
     */
    public UUID getCharacteristicUuid() {
        return characteristicUuid;
    }

    /**
     * Gets the uuid of the target concept.
     *
     * @return the target concept uuid
     */
    public UUID getTargetUuid() {
        return destUuid;
    }

    /**
     * Gets the int representing the grouping for this relationship blueprint. 0
     * indicates the relationship is not part of a group.
     *
     * @return the group number
     */
    public int getGroup() {
        return group;
    }

    /**
     * Gets the uuid of the relationship refinability.
     *
     * @return the refinability uuid
     */
    public UUID getRefinabilityUuid() {
        return refinabilityUuid;
    }

    /**
     * Gets the uuid of the source concept.
     *
     * @return the source concept uuid
     */
    public UUID getSourceUuid() {
        return sourceUuid;
    }

    /**
     * Gets the uuid of the relationship type.
     *
     * @return the relationship type uuid
     */
    public UUID getTypeUuid() {
        return typeUuid;
    }

    /**
     * Gets the nid of the relationship characteristic.
     *
     * @return the relationship characteristic nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getCharacteristicNid() throws IOException {
        return Ts.get().getNidForUuids(characteristicUuid);
    }

    /**
     * Gets the nid of the target concept.
     *
     * @return the target concept nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getTargetNid() throws IOException {
        return Ts.get().getNidForUuids(destUuid);
    }

    /**
     * Gets the nid of the relationship refinability.
     *
     * @return the relationship refinability nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getRefinabilityNid() throws IOException {
        return Ts.get().getNidForUuids(refinabilityUuid);
    }

    /**
     * Gets the nid of the source concept.
     *
     * @return the source concept nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getSourceNid() throws IOException {
        return Ts.get().getNidForUuids(sourceUuid);
    }

    /**
     * Gets the nid of the relationship type.
     *
     * @return the relationship type nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getTypeNid() throws IOException {
        return Ts.get().getNidForUuids(typeUuid);
    }

    /**
     * Sets the uuid of the source concept.
     *
     * @param sourceNewUuid the uuid of the source concept
     */
    protected void setSourceUuid(UUID sourceNewUuid) {
        this.sourceUuid = sourceNewUuid;
    }

    /**
     * Validates this relationship blueprint's fields against the given
     * <code>relationhipVersion</code>. Compares the status nid, relationship
     * nid, source concept nid, relationship type nid, relationship refinability
     * nid, relationship characteristic nid, and relationship target nid.
     *
     * @param relationshipVersion the relationship version to use for validation
     * @return <code>true</code>, if this relationship blueprint's fields are
     * equal to the specified relationship version
     * @throws IOException signals that an I/O exception has occurred
     */
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
