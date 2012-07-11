package org.ihtsdo.tk.dto.concept.component.refex.type_long;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.dto.RevisionHandling;
import org.ihtsdo.tk.dto.concept.component.refex.type_boolean.TkRefexBooleanRevision;

public class TkRefexLongMember extends TkRefexAbstractMember<TkRefexLongRevision> {

    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    public long long1;

    //~--- constructors --------------------------------------------------------
    public TkRefexLongMember() {
        super();
    }

    public TkRefexLongMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexLongVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    public TkRefexLongMember(RefexLongVersionBI refexLongVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(refexLongVersion);
        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            this.long1 = refexLongVersion.getLong1();
       } else {
            Collection<? extends RefexLongVersionBI> refexes = refexLongVersion.getVersions();
            int partCount = refexes.size();
            Iterator<? extends RefexLongVersionBI> itr = refexes.iterator();
            RefexLongVersionBI rv = itr.next();

            this.long1 = rv.getLong1();

            if (partCount > 1) {
                revisions = new ArrayList<TkRefexLongRevision>(partCount - 1);

                while (itr.hasNext()) {
                    rv = itr.next();
                    TkRefexLongRevision rev = new TkRefexLongRevision(rv);
                    if (rev.getTime() == this.time) {
                        // TODO this check can be removed after trek-95 change sets are no longer in production. 
                        this.long1 = rev.long1;
                    } else {
                        revisions.add(rev);
                    }
                }
            }
        }
    }
    

    public TkRefexLongMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRefexLongMember(TkRefexLongMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.long1 = another.long1;
    }

    public TkRefexLongMember(RefexLongVersionBI refexLongVersion, NidBitSetBI excludedNids,
            Map<UUID, UUID> conversionMap, long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexLongVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);
        this.long1 = refexLongVersion.getLong1();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>ERefsetLongMember</tt> object, and contains the same values, field by
     * field, as this <tt>ERefsetLongMember</tt>.
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

        if (TkRefexLongMember.class.isAssignableFrom(obj.getClass())) {
            TkRefexLongMember another = (TkRefexLongMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare long1
            if (this.long1 != another.long1) {
                return false;
            }

            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>ERefsetLongMember</code>.
     *
     * @return a hash code value for this <tt>ERefsetLongMember</tt>.
     */
    @Override
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    @Override
    public TkRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexLongMember(this, conversionMap, offset, mapAll);
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        long1 = in.readLong();

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRefexLongRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefexLongRevision rev = new TkRefexLongRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    long1 = rev.long1;
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
        buff.append(" long:");
        buff.append(this.long1);
        buff.append(" ");
        buff.append(super.toString());

        return buff.toString();
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(long1);

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRefexLongRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    public long getLong1() {
        return long1;
    }

    public List<TkRefexLongRevision> getRevisionList() {
        return revisions;
    }

    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.LONG;
    }

    //~--- set methods ---------------------------------------------------------
    public void setLong1(long long1) {
        this.long1 = long1;
    }
}
