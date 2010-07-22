package org.ihtsdo.tk.concept.component.relationship;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.tk.concept.component.TkRevision;

public class TkRelationshipRevision extends TkRevision {

    public static final long serialVersionUID = 1;

    protected UUID characteristicUuid;
    protected UUID refinabilityUuid;
    protected int group;
    protected UUID typeUuid;

    public TkRelationshipRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRelationshipRevision() {
        super();
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        characteristicUuid = new UUID(in.readLong(), in.readLong());
        refinabilityUuid = new UUID(in.readLong(), in.readLong());
        group = in.readInt();
        typeUuid = new UUID(in.readLong(), in.readLong());
    }

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

    public UUID getCharacteristicUuid() {
        return characteristicUuid;
    }

    public void setCharacteristicUuid(UUID characteristicUuid) {
        this.characteristicUuid = characteristicUuid;
    }

    public UUID getRefinabilityUuid() {
        return refinabilityUuid;
    }

    public void setRefinabilityUuid(UUID refinabilityUuid) {
        this.refinabilityUuid = refinabilityUuid;
    }

    public int getGroup() {
        return group;
    }

    public void setRelGroup(int relGroup) {
        this.group = relGroup;
    }

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
        buff.append(" characteristicUuid:");
        buff.append(this.characteristicUuid);
        buff.append(" refinabilityUuid:");
        buff.append(this.refinabilityUuid);
        buff.append(" group:");
        buff.append(this.group);
        buff.append(" typeUuid:");
        buff.append(this.typeUuid);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERelationshipVersion</tt> object, and contains the same values, 
     * field by field, as this <tt>ERelationshipVersion</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
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
}
