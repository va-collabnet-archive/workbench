package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public class ERefsetStrMember extends ERefsetMember<ERefsetStrRevision> {

    public static final long serialVersionUID = 1;

    protected String strValue;

    public ERefsetStrMember(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    public ERefsetStrMember(I_ExtendByRef m) throws TerminologyException, IOException {
        if (I_Identify.class.isAssignableFrom(m.getClass())) {
            convert((I_Identify) m);
        } else {
            convert(nidToIdentifier(m.getMemberId()));
        }
        int partCount = m.getMutableParts().size();
        refsetUuid = nidToUuid(m.getRefsetId());
        componentUuid = nidToUuid(m.getComponentId());

        I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) m.getMutableParts().get(0);
        strValue = part.getStringValue();
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
        if (partCount > 1) {
            revisions = new ArrayList<ERefsetStrRevision>(partCount - 1);
            for (int i = 1; i < partCount; i++) {
                revisions.add(new ERefsetStrRevision((I_ExtendByRefPartStr) m.getMutableParts().get(i)));
            }
        }
    }

    public ERefsetStrMember() {
        super();
    }

    public ERefsetStrMember(I_ExtendByRefVersion m) throws TerminologyException, IOException {
        if (I_Identify.class.isAssignableFrom(m.getClass())) {
            convert((I_Identify) m);
        } else {
            convert(nidToIdentifier(m.getMemberId()));
        }
        refsetUuid = nidToUuid(m.getRefsetId());
        componentUuid = nidToUuid(m.getComponentId());
        I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) m.getMutablePart();
        strValue = part.getStringValue();
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
	}

	@Override
    public void readExternal(DataInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        strValue = in.readUTF();
        int versionSize = in.readInt();
        if (versionSize > 0) {
            revisions = new ArrayList<ERefsetStrRevision>(versionSize);
            for (int i = 0; i < versionSize; i++) {
                revisions.add(new ERefsetStrRevision(in));
            }
        }
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        if (strValue == null) {
            
        }
        out.writeUTF(strValue);
        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());
            for (ERefsetStrRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    @Override
    public REFSET_TYPES getType() {
        return REFSET_TYPES.STR;
    }

    public List<ERefsetStrRevision> getRevisionList() {
        return revisions;
    }

    public String getStrValue() {
        return strValue;
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append(" strValue:");
        buff.append("'" + this.strValue + "'");
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
    }
    
    /**
     * Returns a hash code for this <code>ERefsetStrMember</code>.
     * 
     * @return a hash code value for this <tt>ERefsetStrMember</tt>.
     */
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetStrMember</tt> object, and contains the same values, field by field, 
     * as this <tt>ERefsetStrMember</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (ERefsetStrMember.class.isAssignableFrom(obj.getClass())) {
            ERefsetStrMember another = (ERefsetStrMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare strValue
            if (!this.strValue.equals(another.strValue)) {
                return false;
            }            
            // Compare their parents
            return super.equals(obj);
        }
        return false;
    }

}
