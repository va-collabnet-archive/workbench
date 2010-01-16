package org.ihtsdo.etypes;

import java.io.IOException;
import java.io.DataInput;
import java.io.DataOutput;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;

public class EConceptAttributesVersion extends EVersion implements I_ConceptualizeExternally {
    public static final long serialVersionUID = 1;

    protected boolean defined;

    public EConceptAttributesVersion(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    public EConceptAttributesVersion(I_ConceptAttributePart part) throws TerminologyException, IOException {
        defined = part.isDefined();
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
    }

    public EConceptAttributesVersion() {
        super();
    }

    @Override
    public void readExternal(DataInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        defined = in.readBoolean();
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeBoolean(defined);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ihtsdo.etypes.I_ConceptualizeExternally#isDefined()
     */
    public boolean isDefined() {
        return defined;
    }

    public void setDefined(boolean defined) {
        this.defined = defined;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();

        buff.append(this.getClass().getSimpleName() + ": ");
        buff.append(super.toString());

        buff.append(" defined:");
        buff.append(this.defined);
        buff.append("; ");

        return buff.toString();
    }
    
    /**
     * Returns a hash code for this <code>EConceptAttributesVersion</code>.
     * 
     * @return a hash code value for this <tt>EConceptAttributesVersion</tt>.
     */
    public int hashCode() {
        return HashFunction.hashCode(new int[] { statusUuid.hashCode(), pathUuid.hashCode(), (int) time });
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EConceptAttributesVersion</tt> object, and contains the same values, field by field, 
     * as this <tt>EConceptAttributesVersion</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (EConceptAttributesVersion.class.isAssignableFrom(obj.getClass())) {
            EConceptAttributesVersion another = (EConceptAttributesVersion) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare defined
            if (this.defined != another.defined) {
                return false;
            }
            // Compare their parents
            return super.equals(obj);
        }
        return false;
    }
}
