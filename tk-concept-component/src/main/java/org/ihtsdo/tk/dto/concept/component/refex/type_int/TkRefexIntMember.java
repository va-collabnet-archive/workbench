package org.ihtsdo.tk.dto.concept.component.refex.type_int;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.dto.RevisionHandling;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_string.TkRefexUuidStringRevision;

public class TkRefexIntMember extends TkRefexAbstractMember<TkRefexIntRevision> {

    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    public int int1;

    //~--- constructors --------------------------------------------------------
    public TkRefexIntMember() {
        super();
    }

    public TkRefexIntMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexIntVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    public TkRefexIntMember(RefexIntVersionBI refexIntVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(refexIntVersion);
        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            this.int1 = refexIntVersion.getInt1();
        } else {
            Collection<? extends RefexIntVersionBI> refexes = refexIntVersion.getVersions();
            int partCount = refexes.size();
            Iterator<? extends RefexIntVersionBI> itr = refexes.iterator();
            RefexIntVersionBI rv = itr.next();

            this.int1 = rv.getInt1();

            if (partCount > 1) {
                revisions = new ArrayList<TkRefexIntRevision>(partCount - 1);

                while (itr.hasNext()) {
                    rv = itr.next();
                    TkRefexIntRevision rev = new TkRefexIntRevision(rv);
                    if (rev.getTime() == this.time) {
                        // TODO this check can be removed after trek-95 change sets are no longer in production. 
                        this.int1 = rev.int1;
                    } else {
                        revisions.add(rev);
                    }
                }
            }
        }
    }

    public TkRefexIntMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRefexIntMember(TkRefexIntMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.int1 = another.int1;
    }

    public TkRefexIntMember(RefexIntVersionBI refexIntVersion, NidBitSetBI excludedNids, Map<UUID, UUID> conversionMap,
            long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexIntVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);
        this.int1 = refexIntVersion.getInt1();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>ERefsetIntMember</tt> object, and contains the same values, field by
     * field, as this <tt>ERefsetIntMember</tt>.
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

        if (TkRefexIntMember.class.isAssignableFrom(obj.getClass())) {
            TkRefexIntMember another = (TkRefexIntMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare int1
            if (this.int1 != another.int1) {
                return false;
            }

            // Compare extraVersions
            if (this.revisions == null) {
                if (another.revisions == null) {             // Equal!
                } else if (another.revisions.isEmpty()) {    // Equal!
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

    /**
     * Returns a hash code for this
     * <code>ERefsetIntMember</code>.
     *
     * @return a hash code value for this <tt>ERefsetIntMember</tt>.
     */
    @Override
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    @Override
    public TkRefexIntMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexIntMember(this, conversionMap, offset, mapAll);
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        int1 = in.readInt();

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRefexIntRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefexIntRevision rev = new TkRefexIntRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    int1 = rev.int1;
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
        buff.append(" int: ");
        buff.append(this.int1);
        buff.append(" ");
        buff.append(super.toString());

        return buff.toString();
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(int1);

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRefexIntRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    public int getInt1() {
        return int1;
    }

    @Override
    public List<TkRefexIntRevision> getRevisionList() {
        return revisions;
    }

    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.INT;
    }

    //~--- set methods ---------------------------------------------------------
    public void setInt1(int int1) {
        this.int1 = int1;
    }
}
