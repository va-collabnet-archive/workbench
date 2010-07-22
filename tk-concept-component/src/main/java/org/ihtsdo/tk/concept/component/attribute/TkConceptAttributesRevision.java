package org.ihtsdo.tk.concept.component.attribute;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.ihtsdo.tk.I_ConceptualizeExternally;
import org.ihtsdo.tk.concept.component.TkRevision;

public class TkConceptAttributesRevision extends TkRevision implements I_ConceptualizeExternally {
    public static final long serialVersionUID = 1;

    protected boolean defined;

    public TkConceptAttributesRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkConceptAttributesRevision() {
        super();
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
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
        buff.append(" defined:");
        buff.append(this.defined);
        buff.append("; ");
        buff.append(super.toString());
        return buff.toString();
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
        if (TkConceptAttributesRevision.class.isAssignableFrom(obj.getClass())) {
            TkConceptAttributesRevision another = (TkConceptAttributesRevision) obj;

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
