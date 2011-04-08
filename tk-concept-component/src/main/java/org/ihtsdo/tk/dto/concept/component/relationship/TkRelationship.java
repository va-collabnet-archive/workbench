package org.ihtsdo.tk.dto.concept.component.relationship;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.api.ext.I_RelateExternally;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.TkComponent;

public class TkRelationship extends TkComponent<TkRelationshipRevision> implements I_RelateExternally {

    public static final long serialVersionUID = 1;

    public UUID c1Uuid;
    public UUID c2Uuid;
    public UUID characteristicUuid;
    public UUID refinabilityUuid;
    public int relGroup;
    public UUID typeUuid;

    public TkRelationship(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRelationship() {
        super();
    }

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
        	TkConcept.checkListInt(revisions.size());
            out.writeInt(revisions.size());
            for (TkRelationshipRevision erv : revisions) {
                erv.writeExternal(out);
            }
        }
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.tk.concept.component.relationship.I_RelateExternally#getC1Uuid()
	 */
    public UUID getC1Uuid() {
        return c1Uuid;
    }

    public void setC1Uuid(UUID c1Uuid) {
        this.c1Uuid = c1Uuid;
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.tk.concept.component.relationship.I_RelateExternally#getC2Uuid()
	 */
    public UUID getC2Uuid() {
        return c2Uuid;
    }

    public void setC2Uuid(UUID c2Uuid) {
        this.c2Uuid = c2Uuid;
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.tk.concept.component.relationship.I_RelateExternally#getCharacteristicUuid()
	 */
    public UUID getCharacteristicUuid() {
        return characteristicUuid;
    }

    public void setCharacteristicUuid(UUID characteristicUuid) {
        this.characteristicUuid = characteristicUuid;
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.tk.concept.component.relationship.I_RelateExternally#getRefinabilityUuid()
	 */
    public UUID getRefinabilityUuid() {
        return refinabilityUuid;
    }

    public void setRefinabilityUuid(UUID refinabilityUuid) {
        this.refinabilityUuid = refinabilityUuid;
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.tk.concept.component.relationship.I_RelateExternally#getRelGroup()
	 */
    public int getRelGroup() {
        return relGroup;
    }

    public void setRelGroup(int relGroup) {
        this.relGroup = relGroup;
    }

    public List<TkRelationshipRevision> getRevisionList() {
        return revisions;
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.tk.concept.component.relationship.I_RelateExternally#getTypeUuid()
	 */
    public UUID getTypeUuid() {
        return typeUuid;
    }

    public void setTypeUuid(UUID typeUuid) {
        this.typeUuid = typeUuid;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append(" c1Uuid:");
        buff.append(this.c1Uuid);
        buff.append(" c2Uuid:");
        buff.append(this.c2Uuid);
        buff.append(" characteristicUuid:");
        buff.append(this.characteristicUuid);
        buff.append(" refinabilityUuid:");
        buff.append(this.refinabilityUuid);
        buff.append(" relGroup:");
        buff.append(this.relGroup);
        buff.append(" typeUuid:");
        buff.append(this.typeUuid);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }

    /**
     * Returns a hash code for this <code>ERelationship</code>.
     * 
     * @return a hash code value for this <tt>ERelationship</tt>.
     */
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERelationship</tt> object, and contains the same values, 
     * field by field, as this <tt>ERelationship</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
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
}
