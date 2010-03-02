package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.tapi.TerminologyException;

public class EConceptAttributes extends EComponent<EConceptAttributesRevision> implements I_ConceptualizeExternally {
    public static final long serialVersionUID = 1;

    protected boolean defined;

    public EConceptAttributes() {
        super();
    }

    public EConceptAttributes(DataInput in) throws IOException, ClassNotFoundException {
        super();
        readExternal(in);
    }

    public EConceptAttributes(I_ConceptAttributeVersioned conceptAttributes) throws TerminologyException, IOException {
        super();
        convert(nidToIdentifier(conceptAttributes.getNid()));
        int partCount = conceptAttributes.getMutableParts().size();
        I_ConceptAttributePart part = conceptAttributes.getMutableParts().get(0);
        defined = part.isDefined();
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
        if (partCount > 1) {
            revisions = new ArrayList<EConceptAttributesRevision>(partCount - 1);
            for (int i = 1; i < partCount; i++) {
                revisions.add(new EConceptAttributesRevision(conceptAttributes.getMutableParts().get(i)));
            }
        }
    }

    public void readExternal(DataInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        defined = in.readBoolean();
        int versionCount = in.readInt();
        if (versionCount > 0) {
            revisions = new ArrayList<EConceptAttributesRevision>(versionCount);
            for (int i = 0; i < versionCount; i++) {
                revisions.add(new EConceptAttributesRevision(in));
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
            for (EConceptAttributesRevision cav : revisions) {
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

    public List<EConceptAttributesRevision> getRevisionList() {
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
        if (EConceptAttributes.class.isAssignableFrom(obj.getClass())) {
            EConceptAttributes another = (EConceptAttributes) obj;

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