package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public class ERefsetCidMember extends ERefsetMember<ERefsetCidRevision> {

    public static final long serialVersionUID = 1;

    protected UUID c1Uuid;

    public ERefsetCidMember(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    public ERefsetCidMember(I_ExtendByRef m) throws TerminologyException, IOException {
        if (I_Identify.class.isAssignableFrom(m.getClass())) {
            convert((I_Identify) m);
        } else {
            convert(nidToIdentifier(m.getMemberId()));
        }
        int partCount = m.getMutableParts().size();
        refsetUuid = nidToUuid(m.getRefsetId());
        componentUuid = nidToUuid(m.getComponentId());

        I_ExtendByRefPartCid part = (I_ExtendByRefPartCid) m.getMutableParts().get(0);
        c1Uuid = nidToUuid(part.getC1id());
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
        if (partCount > 1) {
            revisions = new ArrayList<ERefsetCidRevision>(partCount - 1);
            for (int i = 1; i < partCount; i++) {
                revisions.add(new ERefsetCidRevision((I_ExtendByRefPartCid) m.getMutableParts().get(i)));
            }
        }
    }

    public ERefsetCidMember() {
        super();
    }

    public ERefsetCidMember(I_ExtendByRefVersion m, I_Identify id) throws TerminologyException, IOException {
        convert(id);
        refsetUuid = nidToUuid(m.getRefsetId());
        componentUuid = nidToUuid(m.getComponentId());
        I_ExtendByRefPartCid part = (I_ExtendByRefPartCid) m.getMutablePart();
        c1Uuid = nidToUuid(part.getC1id());
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
	}

	@Override
    public void readExternal(DataInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        c1Uuid = new UUID(in.readLong(), in.readLong());
        int versionSize = in.readInt();
        if (versionSize > 0) {
            revisions = new ArrayList<ERefsetCidRevision>(versionSize);
            for (int i = 0; i < versionSize; i++) {
                revisions.add(new ERefsetCidRevision(in));
            }
        }
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(c1Uuid.getMostSignificantBits());
        out.writeLong(c1Uuid.getLeastSignificantBits());
        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());
            for (ERefsetCidRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    @Override
    public REFSET_TYPES getType() {
        return REFSET_TYPES.CID;
    }

    public List<ERefsetCidRevision> getRevisionList() {
        return revisions;
    }

    public UUID getC1Uuid() {
        return c1Uuid;
    }

    public void setC1Uuid(UUID c1Uuid) {
        this.c1Uuid = c1Uuid;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append(" c1Uuid:");
        buff.append(this.c1Uuid);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }

    /**
     * Returns a hash code for this <code>ERefsetCidMember</code>.
     * 
     * @return a hash code value for this <tt>ERefsetCidMember</tt>.
     */
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetCidMember</tt> object, and contains the same values, field by field, 
     * as this <tt>ERefsetCidMember</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (ERefsetCidMember.class.isAssignableFrom(obj.getClass())) {
            ERefsetCidMember another = (ERefsetCidMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare c1Uuid
            if (!this.c1Uuid.equals(another.c1Uuid)) {
                return false;
            }
            // Compare their parents
            return super.equals(obj);
        }
        return false;
    }

}
