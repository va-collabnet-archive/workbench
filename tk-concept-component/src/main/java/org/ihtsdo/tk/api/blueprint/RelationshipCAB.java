/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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

// TODO: Auto-generated Javadoc
/**
 * The Class RelationshipCAB.
 *
 * @author kec
 */
public class RelationshipCAB extends CreateOrAmendBlueprint {

    /** The Constant relSpecNamespace. */
    public static final UUID relSpecNamespace =
            UUID.fromString("16d79820-5289-11e0-b8af-0800200c9a66");
    
    /** The source uuid. */
    private UUID sourceUuid;
    
    /** The type uuid. */
    private UUID typeUuid;
    
    /** The dest uuid. */
    private UUID destUuid;
    
    /** The group. */
    private int group;
    
    /** The characteristic uuid. */
    private UUID characteristicUuid;
    
    /** The refinability uuid. */
    private UUID refinabilityUuid;

    /**
     * Instantiates a new relationship cab.
     *
     * @param sourceNid the source nid
     * @param typeNid the type nid
     * @param targetNid the target nid
     * @param group the group
     * @param relationshipType the relationship type
     * @throws IOException signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
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
     * Instantiates a new relationship cab.
     *
     * @param sourceUuid the source uuid
     * @param typeUuid the type uuid
     * @param targetUuid the target uuid
     * @param group the group
     * @param relationshipType the relationship type
     * @throws IOException signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    public RelationshipCAB(
            UUID sourceUuid, UUID typeUuid, UUID targetUuid, int group, TkRelationshipType relationshipType)
            throws IOException, InvalidCAB, ContradictionException {
        this(sourceUuid, typeUuid, targetUuid, group, null, relationshipType, null, null);
    }
    
    /**
     * Instantiates a new relationship cab.
     *
     * @param sourceNid the source nid
     * @param typeNid the type nid
     * @param targetNid the target nid
     * @param group the group
     * @param relationshipType the relationship type
     * @param relationshipVersion the relationship version
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    public RelationshipCAB(
            int sourceNid, int typeNid, int targetNid, int group, TkRelationshipType relationshipType,
            RelationshipVersionBI relationshipVersion, ViewCoordinate viewCoordinate)throws IOException, InvalidCAB, ContradictionException {
        this(Ts.get().getComponent(sourceNid).getPrimUuid(),
                Ts.get().getComponent(typeNid).getPrimUuid(),
                Ts.get().getComponent(targetNid).getPrimUuid(),
                group, null, relationshipType, relationshipVersion, viewCoordinate);
    }

    /**
     * Instantiates a new relationship cab.
     *
     * @param sourceUuid the source uuid
     * @param typeUuid the type uuid
     * @param targetUuid the target uuid
     * @param group the group
     * @param relationshipType the relationship type
     * @param relationshipVersion the relationship version
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    public RelationshipCAB(
            UUID sourceUuid, UUID typeUuid, UUID targetUuid, int group,
            TkRelationshipType relationshipType, RelationshipVersionBI relationshipVersion,
            ViewCoordinate viewCoordinate) throws IOException, InvalidCAB, ContradictionException {
        this(sourceUuid, typeUuid, targetUuid, group, null, relationshipType, relationshipVersion, viewCoordinate);
    }

    /**
     * Instantiates a new relationship cab.
     *
     * @param sourceUuid the source uuid
     * @param typeUuid the type uuid
     * @param targetUuid the target uuid
     * @param group the group
     * @param componentUuid the component uuid
     * @param relationshipType the relationship type
     * @param relationshipVersion the relationship version
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
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
    
    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint#recomputeUuid()
     */
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
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o){
        if(RelationshipCAB.class.isAssignableFrom(o.getClass())){
            RelationshipCAB another = (RelationshipCAB) o;
            if(this.sourceUuid.equals(another.sourceUuid) &&
              this.typeUuid.equals(another.typeUuid) &&
              this.destUuid.equals(another.destUuid) &&
              this.group == another.group){
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the characteristic uuid.
     *
     * @return the characteristic uuid
     */
    public UUID getCharacteristicUuid() {
        return characteristicUuid;
    }

    /**
     * Gets the target uuid.
     *
     * @return the target uuid
     */
    public UUID getTargetUuid() {
        return destUuid;
    }

    /**
     * Gets the group.
     *
     * @return the group
     */
    public int getGroup() {
        return group;
    }

    /**
     * Gets the refinability uuid.
     *
     * @return the refinability uuid
     */
    public UUID getRefinabilityUuid() {
        return refinabilityUuid;
    }

    /**
     * Gets the source uuid.
     *
     * @return the source uuid
     */
    public UUID getSourceUuid() {
        return sourceUuid;
    }

    /**
     * Gets the type uuid.
     *
     * @return the type uuid
     */
    public UUID getTypeUuid() {
        return typeUuid;
    }

    /**
     * Gets the characteristic nid.
     *
     * @return the characteristic nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public int getCharacteristicNid() throws IOException {
        return Ts.get().getNidForUuids(characteristicUuid);
    }

    /**
     * Gets the target nid.
     *
     * @return the target nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public int getTargetNid() throws IOException {
        return Ts.get().getNidForUuids(destUuid);
    }

    /**
     * Gets the refinability nid.
     *
     * @return the refinability nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public int getRefinabilityNid() throws IOException {
        return Ts.get().getNidForUuids(refinabilityUuid);
    }

    /**
     * Gets the source nid.
     *
     * @return the source nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public int getSourceNid() throws IOException {
        return Ts.get().getNidForUuids(sourceUuid);
    }

    /**
     * Gets the type nid.
     *
     * @return the type nid
     * @throws IOException signals that an I/O exception has occurred.
     */
    public int getTypeNid() throws IOException {
        return Ts.get().getNidForUuids(typeUuid);
    }
    
    /**
     * Sets the source uuid.
     *
     * @param sourceNewUuid the new source uuid
     */
    protected void setSourceUuid(UUID sourceNewUuid){
        this.sourceUuid = sourceNewUuid;
    }
    
    /**
     * Validate.
     *
     * @param relationshipVersion the relationship version
     * @return <code>true</code>, if successful
     * @throws IOException signals that an I/O exception has occurred.
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
