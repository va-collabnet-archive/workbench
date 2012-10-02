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

// TODO: Auto-generated Javadoc
/**
 * The Class TkRelationship.
 */
public class TkRelationship extends TkComponent<TkRelationshipRevision> implements I_RelateExternally {

    /** The Constant serialVersionUID. */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /** The c1 uuid. */
    public UUID c1Uuid;
    
    /** The c2 uuid. */
    public UUID c2Uuid;
    
    /** The characteristic uuid. */
    public UUID characteristicUuid;
    
    /** The refinability uuid. */
    public UUID refinabilityUuid;
    
    /** The rel group. */
    public int relGroup;
    
    /** The type uuid. */
    public UUID typeUuid;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new tk relationship.
     */
    public TkRelationship() {
        super();
    }

    /**
     * Instantiates a new tk relationship.
     *
     * @param relationshipChronicle the relationship chronicle
     * @throws IOException signals that an I/O exception has occurred.
     */
    public TkRelationship(RelationshipChronicleBI relationshipChronicle) throws IOException {
        this(relationshipChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new tk relationship.
     *
     * @param relationshipVersion the relationship version
     * @param revisionHandling the revision handling
     * @throws IOException signals that an I/O exception has occurred.
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
     * Instantiates a new tk relationship.
     *
     * @param in the in
     * @param dataVersion the data version
     * @throws IOException signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    public TkRelationship(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new tk relationship.
     *
     * @param another the another
     * @param conversionMap the conversion map
     * @param offset the offset
     * @param mapAll the map all
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
     * Instantiates a new tk relationship.
     *
     * @param relationshipVersion the relationship version
     * @param excludedNids the excluded nids
     * @param conversionMap the conversion map
     * @param offset the offset
     * @param mapAll the map all
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
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
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>ERelationship</tt> object, and contains the same values, field by field,
     * as this <tt>ERelationship</tt>.
     *
     * @param obj the object to compare with.
     * @return <code>true</code>, if successful
     * <code>true</code> if the objects are the same;
     * <code>false</code> otherwise.
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

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkRevision#makeConversion(java.util.Map, long, boolean)
     */
    @Override
    public TkRelationship makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRelationship(this, conversionMap, offset, mapAll);
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkComponent#readExternal(java.io.DataInput, int)
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
     * Returns a string representation of the object.
     *
     * @return the string
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

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkComponent#writeExternal(java.io.DataOutput)
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

    /*
     * (non-Javadoc) @see org.ihtsdo.tk.concept.component.relationship.I_RelateExternally#getRelationshipSourceUuid()
     */
    @Override
    public UUID getRelationshipSourceUuid() {
        return c1Uuid;
    }

    /*
     * (non-Javadoc) @see org.ihtsdo.tk.concept.component.relationship.I_RelateExternally#getRelationshipTargetUuid()
     */
    @Override
    public UUID getRelationshipTargetUuid() {
        return c2Uuid;
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.tk.concept.component.relationship.I_RelateExternally#getCharacteristicUuid()
     */
    @Override
    public UUID getCharacteristicUuid() {
        return characteristicUuid;
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.tk.concept.component.relationship.I_RelateExternally#getRefinabilityUuid()
     */
    @Override
    public UUID getRefinabilityUuid() {
        return refinabilityUuid;
    }

    /*
     * (non-Javadoc) @see org.ihtsdo.tk.concept.component.relationship.I_RelateExternally#getRelationshipGroup()
     */
    @Override
    public int getRelationshipGroup() {
        return relGroup;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkComponent#getRevisionList()
     */
    @Override
    public List<TkRelationshipRevision> getRevisionList() {
        return revisions;
    }

    /*
     * (non-Javadoc) @see org.ihtsdo.tk.concept.component.relationship.I_RelateExternally#getTypeUuid()
     */
    @Override
    public UUID getTypeUuid() {
        return typeUuid;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the c1 uuid.
     *
     * @param c1Uuid the new c1 uuid
     */
    public void setC1Uuid(UUID c1Uuid) {
        this.c1Uuid = c1Uuid;
    }

    /**
     * Sets the c2 uuid.
     *
     * @param c2Uuid the new c2 uuid
     */
    public void setC2Uuid(UUID c2Uuid) {
        this.c2Uuid = c2Uuid;
    }

    /**
     * Sets the characteristic uuid.
     *
     * @param characteristicUuid the new characteristic uuid
     */
    public void setCharacteristicUuid(UUID characteristicUuid) {
        this.characteristicUuid = characteristicUuid;
    }

    /**
     * Sets the refinability uuid.
     *
     * @param refinabilityUuid the new refinability uuid
     */
    public void setRefinabilityUuid(UUID refinabilityUuid) {
        this.refinabilityUuid = refinabilityUuid;
    }

    /**
     * Sets the rel group.
     *
     * @param relGroup the new rel group
     */
    public void setRelGroup(int relGroup) {
        this.relGroup = relGroup;
    }

    /**
     * Sets the type uuid.
     *
     * @param typeUuid the new type uuid
     */
    public void setTypeUuid(UUID typeUuid) {
        this.typeUuid = typeUuid;
    }
}
