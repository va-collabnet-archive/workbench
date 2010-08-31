package org.ihtsdo.tk.dto.concept.component;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.dto.concept.component.identifier.IDENTIFIER_PART_TYPES;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierLong;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierString;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierUuid;

public abstract class TkComponent<V extends TkRevision> extends TkRevision implements I_AmComponent<V> {

    public static final long serialVersionUID = 1;

    private static final int dataVersion = 4;

    public UUID primordialUuid;

    public List<TkIdentifier> additionalIds;

    public List<V> revisions;

    public TkComponent() {
        super();
    }

    public TkComponent(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        int readDataVersion = in.readInt();
        if (readDataVersion != dataVersion) {
            throw new IOException("Unsupported dataVersion: " + readDataVersion + " dataVersion: " + dataVersion);
        }
        primordialUuid = new UUID(in.readLong(), in.readLong());
        short idVersionCount = in.readShort();
        if (idVersionCount > 0) {
            additionalIds = new ArrayList<TkIdentifier>(idVersionCount);
            for (int i = 0; i < idVersionCount; i++) {
                switch (IDENTIFIER_PART_TYPES.readType(in)) {
                case LONG:
                    additionalIds.add(new TkIdentifierLong(in, dataVersion));
                    break;
                case STRING:
                    additionalIds.add(new TkIdentifierString(in, dataVersion));
                    break;
                case UUID:
                    additionalIds.add(new TkIdentifierUuid(in, dataVersion));
                    break;
                default:
                    throw new UnsupportedOperationException();
                }
            }
        }
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(dataVersion);
        out.writeLong(primordialUuid.getMostSignificantBits());
        out.writeLong(primordialUuid.getLeastSignificantBits());
        if (additionalIds == null) {
            out.writeShort(0);
        } else {
            out.writeShort(additionalIds.size());
            for (TkIdentifier idv : additionalIds) {
                idv.getIdType().writeType(out);
                idv.writeExternal(out);
            }
        }
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.tk.dto.concept.component.I_AmComponent#getIdComponentCount()
	 */
    public int getIdComponentCount() {
        if (additionalIds == null) {
            return 1;
        }
        return additionalIds.size() + 1;
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.tk.dto.concept.component.I_AmComponent#getEIdentifiers()
	 */
    public List<TkIdentifier> getEIdentifiers() {
        List<TkIdentifier> ids;
        if (additionalIds != null) {
            ids = new ArrayList<TkIdentifier>(additionalIds.size() + 1);
            ids.addAll(additionalIds);
        } else {
            ids = new ArrayList<TkIdentifier>(1);
        }
        ids.add(new TkIdentifierUuid(this.primordialUuid));
        return ids;
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.tk.dto.concept.component.I_AmComponent#getUuids()
	 */
    public List<UUID> getUuids() {
        List<UUID> uuids = new ArrayList<UUID>();
        uuids.add(primordialUuid);
        if (additionalIds != null) {
            for (TkIdentifier idv : additionalIds) {
                if (TkIdentifierUuid.class.isAssignableFrom(idv.getClass())) {
                    uuids.add((UUID) idv.getDenotation());
                }
            }
        }
        return uuids;
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.tk.dto.concept.component.I_AmComponent#getVersionCount()
	 */
    public int getVersionCount() {
        List<? extends TkRevision> extraVersions = getRevisionList();
        if (extraVersions == null) {
            return 1;
        }
        return extraVersions.size() + 1;
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.tk.dto.concept.component.I_AmComponent#getRevisionList()
	 */
    public abstract List<? extends TkRevision> getRevisionList();

    /* (non-Javadoc)
	 * @see org.ihtsdo.tk.dto.concept.component.I_AmComponent#getPrimordialComponentUuid()
	 */
    public UUID getPrimordialComponentUuid() {
        return primordialUuid;
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.tk.dto.concept.component.I_AmComponent#getAdditionalIdComponents()
	 */
    public List<TkIdentifier> getAdditionalIdComponents() {
        return additionalIds;
    }

    public void setAdditionalIdComponents(List<TkIdentifier> additionalIdComponents) {
        this.additionalIds = additionalIdComponents;
    }

    /* (non-Javadoc)
	 * @see org.ihtsdo.tk.dto.concept.component.I_AmComponent#getRevisions()
	 */
    public List<V> getRevisions() {
        return revisions;
    }

    public void setRevisions(List<V> revisions) {
        this.revisions = revisions;
    }

    public void setPrimordialComponentUuid(UUID primordialComponentUuid) {
        this.primordialUuid = primordialComponentUuid;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(" primordialComponentUuid:");
        buff.append(this.primordialUuid);
        buff.append(" additionalIdComponents:");
        buff.append(this.additionalIds);
        buff.append(super.toString());
        buff.append(" Revisions:");
        buff.append(this.revisions);
        return buff.toString();
    }

    /**
     * Returns a hash code for this <code>EComponent</code>.
     * 
     * @return a hash code value for this <tt>EComponent</tt>.
     */
    public int hashCode() {
        return Arrays.hashCode(new int[] { getPrimordialComponentUuid().hashCode(), 
        		statusUuid.hashCode(), pathUuid.hashCode(), (int) time, (int) (time >>> 32) });
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EComponent</tt> object, and contains the same values, field by field, 
     * as this <tt>EComponent</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (TkComponent.class.isAssignableFrom(obj.getClass())) {
            TkComponent<?> another = (TkComponent<?>) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare primordialComponentUuid
            if (!this.primordialUuid.equals(another.primordialUuid)) {
                return false;
            }
            // Compare additionalIdComponents
            if (this.additionalIds == null) {
                if (another.additionalIds == null) { // Equal!
                } else if (another.additionalIds.size() == 0) { // Equal!
                } else {
                    return false;
                }
            } else if (!this.additionalIds.equals(another.additionalIds)) {
                return false;
            }
            // Compare extraVersions
            if (this.revisions == null) {
                if (another.revisions == null) { // Equal!
                } else if (another.revisions.size() == 0) { // Equal!
                } else {
                    return false;
                }
            } else if (!this.revisions.equals(another.revisions)) {
                return false;
            }
            // Compare their parents
            return super.equals(obj);
        }
        return false;
    }

}
