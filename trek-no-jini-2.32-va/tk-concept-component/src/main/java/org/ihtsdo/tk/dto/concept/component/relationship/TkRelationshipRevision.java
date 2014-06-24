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
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.Map;
import java.util.UUID;

/**
 * The Class TkRelationshipRevision represents a version of a relationship in
 * the eConcept format and contains methods for interacting with a version of a
 * relationship. Further discussion of the eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkRelationshipRevision extends TkRevision {

    /**
     * The Constant serialVersionUID.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The uuid representing the relationship characteristic uuid.
     */
    public UUID characteristicUuid;
    /**
     * The int value representing the relationship group.
     */
    public int group;
    /**
     * The uuid representing the relationship refinability uuid.
     */
    public UUID refinabilityUuid;
    /**
     * The uuid representing the relationship type.
     */
    public UUID typeUuid;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Relationship Revision.
     */
    public TkRelationshipRevision() {
        super();
    }

    /**
     * Instantiates a new TK Relationship Revision based on the
     * <code>relationshipVersion</code>.
     *
     * @param relationshipVersion the relationship version specifying how to
     * construct this TK Relationship Revision
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRelationshipRevision(RelationshipVersionBI relationshipVersion) throws IOException {
        super(relationshipVersion);
        TerminologyStoreDI ts = Ts.get();

        characteristicUuid = ts.getUuidPrimordialForNid(relationshipVersion.getCharacteristicNid());
        refinabilityUuid = ts.getUuidPrimordialForNid(relationshipVersion.getRefinabilityNid());
        group = relationshipVersion.getGroup();
        typeUuid = ts.getUuidPrimordialForNid(relationshipVersion.getTypeNid());
    }

    /**
     * Instantiates a new TK Relationship Revision based on the specified data
     * input,
     * <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Relationship
     * Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRelationshipRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Relationship Revision based on
     * <code>another</code> TK Relationship Revision and allows for uuid
     * conversion.
     *
     * @param another the TK Relationship Revision specifying how to construct
     * this TK Relationship Revision
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Relationship Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Relationship Revision based on the conversion map
     */
    public TkRelationshipRevision(TkRelationshipRevision another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.characteristicUuid = conversionMap.get(another.characteristicUuid);
            this.refinabilityUuid = conversionMap.get(another.refinabilityUuid);
            this.group = another.group;
            this.typeUuid = conversionMap.get(another.typeUuid);
        } else {
            this.characteristicUuid = another.characteristicUuid;
            this.refinabilityUuid = another.refinabilityUuid;
            this.group = another.group;
            this.typeUuid = another.typeUuid;
        }
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERelationshipVersion</tt> object, and contains the same values, field
     * by field, as this <tt>ERelationshipVersion</tt>.
     *
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; <code>false</code>
     * otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (TkRelationshipRevision.class.isAssignableFrom(obj.getClass())) {
            TkRelationshipRevision another = (TkRelationshipRevision) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare characteristicUuid
            if (!this.characteristicUuid.equals(another.characteristicUuid)) {
                return false;
            }

            // Compare refinabilityUuid
            if (!this.refinabilityUuid.equals(another.refinabilityUuid)) {
                return false;
            }

            // Compare group
            if (this.group != another.group) {
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
     *
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Relationship Revision
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Relationship Revision based on the conversion map
     * @return the converted TK Relationship Revision
     */
    @Override
    public TkRelationshipRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRelationshipRevision(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK Relationship
     * Revision
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        characteristicUuid = new UUID(in.readLong(), in.readLong());
        refinabilityUuid = new UUID(in.readLong(), in.readLong());
        group = in.readInt();
        typeUuid = new UUID(in.readLong(), in.readLong());
    }

    /**
     * Returns a string representation of this TK Relationship Revision object.
     *
     * @return a string representation of this TK Relationship Revision object
     * including the relationship type, group number, relationship
     * characteristic, and relationship refinability.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" type:");
        buff.append(informAboutUuid(this.typeUuid));
        buff.append(" grp:");
        buff.append(this.group);
        buff.append(" char:");
        buff.append(this.characteristicUuid);
        buff.append(" ref:");
        buff.append(this.refinabilityUuid);
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
        out.writeLong(characteristicUuid.getMostSignificantBits());
        out.writeLong(characteristicUuid.getLeastSignificantBits());
        out.writeLong(refinabilityUuid.getMostSignificantBits());
        out.writeLong(refinabilityUuid.getLeastSignificantBits());
        out.writeInt(group);
        out.writeLong(typeUuid.getMostSignificantBits());
        out.writeLong(typeUuid.getLeastSignificantBits());
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the relationship characteristic uuid associated with this TK
     * Relationship Revision.
     *
     * @return the relationship characteristic uuid
     */
    public UUID getCharacteristicUuid() {
        return characteristicUuid;
    }

    /**
     * Gets the
     * <code>int</cod> represtenting the relationship group associated with this TK
     * Relationship Revision.
     *
     * @return the <code>int</cod> represtenting the relationship group
     */
    public int getGroup() {
        return group;
    }

    /**
     * Gets the
     * <code>int</cod> represtenting the relationship group associated with this TK
     * Relationship Revision.
     *
     * @return the <code>int</cod> represtenting the relationship group
     */
    public int getRelGroup() {
        return group;
    }

    /**
     * Gets the relationship refinability uuid associated with this TK
     * Relationship Revision.
     *
     * @return the relationship refinability uuid
     */
    public UUID getRefinabilityUuid() {
        return refinabilityUuid;
    }

    /**
     * Gets the relationship type uuid associated with this TK
     * Relationship Revision.
     *
     * @return the relationship type uuid
     */
    public UUID getTypeUuid() {
        return typeUuid;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the relationship characteristic uuid associated with this TK
     * Relationship Revision.
     *
     * @param characteristicUuid the relationship characteristic uuid
     */
    public void setCharacteristicUuid(UUID characteristicUuid) {
        this.characteristicUuid = characteristicUuid;
    }

    /**
     * Sets the relationship refinability uuid associated with this TK
     * Relationship Revision.
     *
     * @param refinabilityUuid the relationship refinability uuid
     */
    public void setRefinabilityUuid(UUID refinabilityUuid) {
        this.refinabilityUuid = refinabilityUuid;
    }

    /**
     * Sets the
     * <code>int</code> representing the relationship group associated with this
     * TK Relationship Revision.
     *
     * @param relGroup the <code>int</code> representing the relationship group
     */
    public void setRelGroup(int relGroup) {
        this.group = relGroup;
    }

    /**
     * Sets the relationship type uuid associated with this TK Relationship Revision.
     *
     * @param typeUuid the relationship type uuid
     */
    public void setTypeUuid(UUID typeUuid) {
        this.typeUuid = typeUuid;
    }
}
