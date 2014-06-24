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
package org.ihtsdo.tk.dto.concept.component.relationship;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.ext.I_RelateExternally;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkComponent;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.dto.RevisionHandling;

/**
 * The Class TkRelationship relationship in the eConcept format and contains
 * methods general to interacting with a relationship. Further discussion of the
 * eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkRelationship extends TkComponent<TkRelationshipRevision> implements I_RelateExternally {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The uuid of the source concept.
     */
    public UUID c1Uuid;
    /**
     * The uuid of the target concept.
     */
    public UUID c2Uuid;
    /**
     * The uuid representing the relationship characteristic uuid.
     */
    public UUID characteristicUuid;
    /**
     * The uuid representing the relationship refinability uuid.
     */
    public UUID refinabilityUuid;
    /**
     * The int value representing the relationship group.
     */
    public int relGroup;
    /**
     * The uuid representing the relationship type.
     */
    public UUID typeUuid;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Relationship.
     */
    public TkRelationship() {
        super();
    }

    /**
     * Instantiates a new TK Relationship based on the
     * <code>relationshipChronicle</code>.
     *
     * @param relationshipChronicle the relationship chronicle how to construct
     * this TK Relationship
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRelationship(RelationshipChronicleBI relationshipChronicle) throws IOException {
        this(relationshipChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new TK Relationship based on the
     * <code>relationshipVersion</code> and using the given
     * <code>revisionHandling</code>.
     *
     * @param relationshipVersion the relationship version specifying how to
     * construct this TK Relationship
     * @param revisionHandling the revision handling specifying if addition
     * versions should be included or not
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRelationship(RelationshipVersionBI relationshipVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(relationshipVersion.getPrimordialVersion());
        TerminologyStoreDI ts = Ts.get();
        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            c1Uuid = ts.getUuidPrimordialForNid(relationshipVersion.getConceptNid());
            c2Uuid = ts.getUuidPrimordialForNid(relationshipVersion.getTargetNid());
            characteristicUuid = ts.getUuidPrimordialForNid(relationshipVersion.getCharacteristicNid());
            refinabilityUuid = ts.getUuidPrimordialForNid(relationshipVersion.getRefinabilityNid());
            relGroup = relationshipVersion.getGroup();
            typeUuid = ts.getUuidPrimordialForNid(relationshipVersion.getTypeNid());
            pathUuid = ts.getUuidPrimordialForNid(relationshipVersion.getPathNid());
            statusUuid = ts.getUuidPrimordialForNid(relationshipVersion.getStatusNid());
            time = relationshipVersion.getTime();
        } else {
            Collection<? extends RelationshipVersionBI> rels = relationshipVersion.getVersions();
            int partCount = rels.size();
            Iterator<? extends RelationshipVersionBI> relItr = rels.iterator();
            RelationshipVersionBI rv = relItr.next();

            c1Uuid = ts.getUuidPrimordialForNid(rv.getConceptNid());
            c2Uuid = ts.getUuidPrimordialForNid(rv.getTargetNid());
            characteristicUuid = ts.getUuidPrimordialForNid(rv.getCharacteristicNid());
            refinabilityUuid = ts.getUuidPrimordialForNid(rv.getRefinabilityNid());
            relGroup = rv.getGroup();
            typeUuid = ts.getUuidPrimordialForNid(rv.getTypeNid());
            pathUuid = ts.getUuidPrimordialForNid(rv.getPathNid());
            statusUuid = ts.getUuidPrimordialForNid(rv.getStatusNid());
            time = rv.getTime();

            if (partCount > 1) {
                revisions = new ArrayList<TkRelationshipRevision>(partCount - 1);

                while (relItr.hasNext()) {
                    rv = relItr.next();
                    revisions.add(new TkRelationshipRevision(rv));
                }
            }
        }
    }

    /**
     * Instantiates a new TK Relationship based on the specified data input,
     * <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Relationship
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRelationship(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Relationship based on
     * <code>another</code> TK Relationship and allows for uuid conversion.
     *
     * @param another the TK Relationship specifying how to construct this TK
     * Relationship
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Relationship
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Relationship based on the conversion map
     */
    public TkRelationship(TkRelationship another, Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.c1Uuid = conversionMap.get(another.c1Uuid);
            this.c2Uuid = conversionMap.get(another.c2Uuid);
            this.characteristicUuid = conversionMap.get(another.characteristicUuid);
            this.refinabilityUuid = conversionMap.get(another.refinabilityUuid);
            this.relGroup = another.relGroup;
            this.typeUuid = conversionMap.get(another.typeUuid);
        } else {
            this.c1Uuid = another.c1Uuid;
            this.c2Uuid = another.c2Uuid;
            this.characteristicUuid = another.characteristicUuid;
            this.refinabilityUuid = another.refinabilityUuid;
            this.relGroup = another.relGroup;
            this.typeUuid = another.typeUuid;
        }
    }

    /**
     * Instantiates a new TK Relationship based on a
     * <code>relationshipVersion</code> and allows for uuid conversion. Can
     * exclude components based on their nid.
     *
     * @param relationshipVersion the relationship version specifying how to
     * construct this TK Relationship
     * @param excludedNids the nids in the specified component version to
     * exclude from this TK Relationship
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Relationship
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Relationship based on the conversion map
     * @param viewCoordinate the view coordinate specifying which version of the
     * components to use
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version of a component is
     * found for the specified view coordinate
     */
    public TkRelationship(RelationshipVersionBI relationshipVersion, NidBitSetBI excludedNids,
            Map<UUID, UUID> conversionMap, long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(relationshipVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);

        if (mapAll) {
            this.c1Uuid =
                    conversionMap.get(Ts.get().getComponent(relationshipVersion.getSourceNid()).getPrimUuid());
            this.c2Uuid =
                    conversionMap.get(Ts.get().getComponent(relationshipVersion.getTargetNid()).getPrimUuid());
            this.characteristicUuid =
                    conversionMap.get(Ts.get().getComponent(relationshipVersion.getCharacteristicNid()).getPrimUuid());
            this.refinabilityUuid =
                    conversionMap.get(Ts.get().getComponent(relationshipVersion.getRefinabilityNid()).getPrimUuid());
            this.typeUuid = conversionMap.get(Ts.get().getComponent(relationshipVersion.getTypeNid()).getPrimUuid());
        } else {
            this.c1Uuid = Ts.get().getComponent(relationshipVersion.getSourceNid()).getPrimUuid();
            this.c2Uuid = Ts.get().getComponent(relationshipVersion.getTargetNid()).getPrimUuid();
            this.characteristicUuid = Ts.get().getComponent(relationshipVersion.getCharacteristicNid()).getPrimUuid();
            this.refinabilityUuid = Ts.get().getComponent(relationshipVersion.getRefinabilityNid()).getPrimUuid();
            this.typeUuid = Ts.get().getComponent(relationshipVersion.getTypeNid()).getPrimUuid();
        }

        this.relGroup = relationshipVersion.getGroup();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERelationship</tt> object, and contains the same values, field by
     * field, as this <tt>ERelationship</tt>.
     *
     * @param obj the object to compare with.
     * @return <code>true</code>, if successful <code>true</code> if the objects
     * are the same; <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (TkRelationship.class.isAssignableFrom(obj.getClass())) {
            TkRelationship another = (TkRelationship) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare c1Uuid
            if (!this.c1Uuid.equals(another.c1Uuid)) {
                return false;
            }

            // Compare c2Uuid
            if (!this.c2Uuid.equals(another.c2Uuid)) {
                return false;
            }

            // Compare characteristicUuid
            if (!this.characteristicUuid.equals(another.characteristicUuid)) {
                return false;
            }

            // Compare refinabilityUuid
            if (!this.refinabilityUuid.equals(another.refinabilityUuid)) {
                return false;
            }

            // Compare relGroup
            if (this.relGroup != another.relGroup) {
                return false;
            }

            // Compare typeUuid
            if (!this.typeUuid.equals(another.typeUuid)) {
                return false;
            }

            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>ERelationship</code>.
     *
     * @return a hash code value for this <tt>ERelationship</tt>.
     */
    @Override
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    /**
     *
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Relationship
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Relationship based on the conversion map
     * @return the converted TK Relationship
     */
    @Override
    public TkRelationship makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRelationship(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK Relationship
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        c1Uuid = new UUID(in.readLong(), in.readLong());
        c2Uuid = new UUID(in.readLong(), in.readLong());
        characteristicUuid = new UUID(in.readLong(), in.readLong());
        refinabilityUuid = new UUID(in.readLong(), in.readLong());
        relGroup = in.readInt();
        typeUuid = new UUID(in.readLong(), in.readLong());

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRelationshipRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                revisions.add(new TkRelationshipRevision(in, dataVersion));
            }
        }
    }

    /**
     * Returns a string representation of this TK Relationship object.
     *
     * @return a string representation of this TK Relationship object including
     * the source concept, relationship type, target concept, group number,
     * relationship characteristic, and relationship refinability.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" c1: ");
        buff.append(informAboutUuid(this.c1Uuid));
        buff.append(" type:");
        buff.append(informAboutUuid(this.typeUuid));
        buff.append(" c2: ");
        buff.append(informAboutUuid(this.c2Uuid));
        buff.append(" grp:");
        buff.append(this.relGroup);
        buff.append(" char: ");
        buff.append(informAboutUuid(this.characteristicUuid));
        buff.append(" ref: ");
        buff.append(informAboutUuid(this.refinabilityUuid));
        buff.append(" ");
        buff.append(super.toString());

        return buff.toString();
    }

    /**
     *
     * @param out the data output object that writes to the external source
     * @throws IOException signals that an I/O exception has occurred
     */
    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(c1Uuid.getMostSignificantBits());
        out.writeLong(c1Uuid.getLeastSignificantBits());
        out.writeLong(c2Uuid.getMostSignificantBits());
        out.writeLong(c2Uuid.getLeastSignificantBits());
        out.writeLong(characteristicUuid.getMostSignificantBits());
        out.writeLong(characteristicUuid.getLeastSignificantBits());
        out.writeLong(refinabilityUuid.getMostSignificantBits());
        out.writeLong(refinabilityUuid.getLeastSignificantBits());
        out.writeInt(relGroup);
        out.writeLong(typeUuid.getMostSignificantBits());
        out.writeLong(typeUuid.getLeastSignificantBits());

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRelationshipRevision erv : revisions) {
                erv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    /**
     *
     * @return the uuid of the source concept associated with this TK
     * Relationship
     */
    @Override
    public UUID getRelationshipSourceUuid() {
        return c1Uuid;
    }

    /**
     *
     * @return the uuid of the target concept associated with this TK
     * Relationship
     */
    @Override
    public UUID getRelationshipTargetUuid() {
        return c2Uuid;
    }

    /**
     *
     * @return the uuid of the relationship characteristic associated with this
     * TK Relationship
     */
    @Override
    public UUID getCharacteristicUuid() {
        return characteristicUuid;
    }

    /**
     *
     * @return the uuid of the relationship refinability associated with this TK
     * Relationship
     */
    @Override
    public UUID getRefinabilityUuid() {
        return refinabilityUuid;
    }

    /**
     *
     * @return the <code>int</code> representing the relationship group
     * associated with this TK Relationship
     */
    @Override
    public int getRelationshipGroup() {
        return relGroup;
    }

    /**
     *
     * @return a list of revisions on this TK Relationship
     */
    @Override
    public List<TkRelationshipRevision> getRevisionList() {
        return revisions;
    }

    /**
     *
     * @return the uuid of the relationship type associated with this TK
     * Relationship
     */
    @Override
    public UUID getTypeUuid() {
        return typeUuid;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the uuid of the source concept associated with this TK Relationship.
     *
     * @param c1Uuid the uuid of the source concept
     */
    public void setC1Uuid(UUID c1Uuid) {
        this.c1Uuid = c1Uuid;
    }

    /**
     * Sets the uuid of the target concept associated with this TK Relationship.
     *
     * @param c2Uuid the uuid of the target concept
     */
    public void setC2Uuid(UUID c2Uuid) {
        this.c2Uuid = c2Uuid;
    }

    /**
     * Sets the relationship characteristic uuid associated with this TK
     * Relationship.
     *
     * @param characteristicUuid the relationship characteristic uuid
     */
    public void setCharacteristicUuid(UUID characteristicUuid) {
        this.characteristicUuid = characteristicUuid;
    }

    /**
     * Sets the relationship refinability uuid associated with this TK
     * Relationship.
     *
     * @param refinabilityUuid the relationship refinability uuid
     */
    public void setRefinabilityUuid(UUID refinabilityUuid) {
        this.refinabilityUuid = refinabilityUuid;
    }

    /**
     * Sets the
     * <code>int</code> representing the relationship group associated with this
     * TK Relationship.
     *
     * @param relGroup the <code>int</code> representing the relationship group
     */
    public void setRelGroup(int relGroup) {
        this.relGroup = relGroup;
    }

    /**
     * Sets the relationship type uuid associated with this TK Relationship.
     *
     * @param typeUuid the relationship type uuid
     */
    public void setTypeUuid(UUID typeUuid) {
        this.typeUuid = typeUuid;
    }
}
