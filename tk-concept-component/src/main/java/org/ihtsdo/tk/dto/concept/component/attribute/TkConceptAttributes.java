package org.ihtsdo.tk.dto.concept.component.attribute;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ihtsdo.tk.api.ext.I_ConceptualizeExternally;
import org.ihtsdo.tk.dto.concept.component.TkComponent;

public class TkConceptAttributes extends TkComponent<TkConceptAttributesRevision> implements I_ConceptualizeExternally {
    public static final long serialVersionUID = 1;

    public boolean defined;

    public TkConceptAttributes() {
        super();
    }

    public TkConceptAttributes(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        defined = in.readBoolean();
        int versionCount = in.readInt();
        if (versionCount > 0) {
            revisions = new ArrayList<TkConceptAttributesRevision>(versionCount);
            for (int i = 0; i < versionCount; i++) {
                revisions.add(new TkConceptAttributesRevision(in, dataVersion));
            }
        }
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeBoolean(defined);
        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());
            for (TkConceptAttributesRevision cav : revisions) {
                cav.writeExternal(out);
            }
        }
    }

    public boolean isDefined() {
        return defined;
    }

    public void setDefined(boolean defined) {
        this.defined = defined;
    }

    public List<TkConceptAttributesRevision> getRevisionList() {
        return revisions;
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
     * Returns a hash code for this <code>EConceptAttributes</code>.
     * 
     * @return a hash code value for this <tt>EConceptAttributes</tt>.
     */
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>EConceptAttributes</tt> object, and contains the same values, field by field, 
     * as this <tt>EConceptAttributes</tt>.
     * 
     * @param obj the object to compare with.
     * @return <code>true</code> if the objects are the same; 
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (TkConceptAttributes.class.isAssignableFrom(obj.getClass())) {
            TkConceptAttributes another = (TkConceptAttributes) obj;

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