package org.ihtsdo.tk.dto.concept.component.refset.Boolean;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.dto.RevisionHandling;
import org.ihtsdo.tk.dto.concept.component.refset.str.TkRefsetStrRevision;

public class TkRefsetBooleanMember extends TkRefsetAbstractMember<TkRefsetBooleanRevision> {

    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    public boolean booleanValue;

    //~--- constructors --------------------------------------------------------
    public TkRefsetBooleanMember() {
        super();
    }

    public TkRefsetBooleanMember(RefexBooleanVersionBI another,
            RevisionHandling revisionHandling) throws IOException {
        super(another);

        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            this.booleanValue = another.getBoolean1();
        } else {
            Collection<? extends RefexBooleanVersionBI> refexes = another.getVersions();
            int partCount = refexes.size();
            Iterator<? extends RefexBooleanVersionBI> itr = refexes.iterator();
            RefexBooleanVersionBI rv = itr.next();

            this.booleanValue = rv.getBoolean1();

            if (partCount > 1) {
                revisions = new ArrayList<TkRefsetBooleanRevision>(partCount - 1);

                while (itr.hasNext()) {
                    rv = itr.next();
                    TkRefsetBooleanRevision rev = new TkRefsetBooleanRevision(rv);
                    if (rev.getTime() == this.time) {
                        // TODO this check can be removed after trek-95 change sets are no longer in production. 
                        this.booleanValue = rev.booleanValue;
                    } else {
                        revisions.add(rev);
                    }
                }
            }
        }
    }

    public TkRefsetBooleanMember(RefexChronicleBI another) throws IOException {
        this((RefexBooleanVersionBI) another.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    public TkRefsetBooleanMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRefsetBooleanMember(TkRefsetBooleanMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.booleanValue = another.booleanValue;
    }

    public TkRefsetBooleanMember(RefexBooleanVersionBI another, NidBitSetBI exclusions,
            Map<UUID, UUID> conversionMap, long offset, boolean mapAll, ViewCoordinate vc)
            throws IOException, ContradictionException {
        super(another, exclusions, conversionMap, offset, mapAll, vc);
        this.booleanValue = another.getBoolean1();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>ERefsetBooleanMember</tt> object, and contains the same values, field by
     * field, as this <tt>ERefsetBooleanMember</tt>.
     *
     * @param obj the object to compare with.
     * @return
     * <code>true</code> if the objects are the same;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (TkRefsetBooleanMember.class.isAssignableFrom(obj.getClass())) {
            TkRefsetBooleanMember another = (TkRefsetBooleanMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare booleanValue
            if (this.booleanValue != another.booleanValue) {
                return false;
            }

            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>ERefsetBooleanMember</code>.
     *
     * @return a hash code value for this <tt>ERefsetBooleanMember</tt>.
     */
    @Override
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    @Override
    public TkRefsetBooleanMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefsetBooleanMember(this, conversionMap, offset, mapAll);
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        booleanValue = in.readBoolean();

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRefsetBooleanRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefsetBooleanRevision rev = new TkRefsetBooleanRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    booleanValue = rev.booleanValue;
                } else {
                    revisions.add(rev);
                }
            }
        }
    }

    /**
     * Returns a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(this.booleanValue);
        buff.append(" ");
        buff.append(super.toString());

        return buff.toString();
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeBoolean(booleanValue);

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRefsetBooleanRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    public boolean getBooleanValue() {
        return booleanValue;
    }

    public List<TkRefsetBooleanRevision> getRevisionList() {
        return revisions;
    }

    @Override
    public TK_REFSET_TYPE getType() {
        return TK_REFSET_TYPE.BOOLEAN;
    }

    //~--- set methods ---------------------------------------------------------
    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }
}