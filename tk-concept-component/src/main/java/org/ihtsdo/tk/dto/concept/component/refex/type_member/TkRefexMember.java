package org.ihtsdo.tk.dto.concept.component.refex.type_member;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_member.RefexMemberVersionBI;
import org.ihtsdo.tk.dto.RevisionHandling;

public class TkRefexMember extends TkRefexAbstractMember<TkRefexRevision> {

    public static final long serialVersionUID = 1;

    //~--- constructors --------------------------------------------------------
    public TkRefexMember() {
        super();
    }

    public TkRefexMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRefexMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexMemberVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    public TkRefexMember(RefexMemberVersionBI refexMemberVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(refexMemberVersion);
        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            //
        } else {

            Collection<? extends RefexMemberVersionBI> refexes = refexMemberVersion.getVersions();
            int partCount = refexes.size();
            Iterator<? extends RefexMemberVersionBI> itr = refexes.iterator();
            RefexMemberVersionBI rv = itr.next();

            if (partCount > 1) {
                revisions = new ArrayList<TkRefexRevision>(partCount - 1);

                while (itr.hasNext()) {
                    rv = itr.next();
                    TkRefexRevision rev = new TkRefexRevision(rv);
                    if (rev.getTime() == this.time) {
                        // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    } else {
                        revisions.add(rev);
                    }
                }
            }
        }
    }

    public TkRefexMember(TkRefexMember another, Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
    }

    public TkRefexMember(RefexVersionBI refexVersion, NidBitSetBI excludedNids, Map<UUID, UUID> conversionMap,
            long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>ERefsetMember</tt> object, and contains the same values, field by field,
     * as this <tt>ERefsetMember</tt>.
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

        if (TkRefexMember.class.isAssignableFrom(obj.getClass())) {

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>ERefsetMember</code>.
     *
     * @return a hash code value for this <tt>ERefsetMember</tt>.
     */
    @Override
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    @Override
    public TkRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexMember(this, conversionMap, offset, mapAll);
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRefexRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefexRevision rev = new TkRefexRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
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
        buff.append(" ");
        buff.append(super.toString());

        return buff.toString();
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRefexRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    public List<TkRefexRevision> getRevisionList() {
        return revisions;
    }

    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.MEMBER;
    }
}
