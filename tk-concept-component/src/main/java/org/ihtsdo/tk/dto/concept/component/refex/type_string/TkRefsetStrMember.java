package org.ihtsdo.tk.dto.concept.component.refex.type_string;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.tk.dto.concept.UtfHelper;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.dto.RevisionHandling;

public class TkRefsetStrMember extends TkRefexAbstractMember<TkRefsetStrRevision> {

    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    public String string1;

    //~--- constructors --------------------------------------------------------
    public TkRefsetStrMember() {
        super();
    }

    public TkRefsetStrMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexStringVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    public TkRefsetStrMember(RefexStringVersionBI refexStringVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(refexStringVersion);
        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            this.string1 = refexStringVersion.getString1();
        } else {

            Collection<? extends RefexStringVersionBI> refexes = refexStringVersion.getVersions();
            int partCount = refexes.size();
            Iterator<? extends RefexStringVersionBI> itr = refexes.iterator();
            RefexStringVersionBI rv = itr.next();

            this.string1 = rv.getString1();

            if (partCount > 1) {
                revisions = new ArrayList<TkRefsetStrRevision>(partCount - 1);

                while (itr.hasNext()) {
                    rv = itr.next();
                    TkRefsetStrRevision rev = new TkRefsetStrRevision(rv);
                    if (rev.getTime() == this.time) {
                        // TODO this check can be removed after trek-95 change sets are no longer in production. 
                        this.string1 = rev.string1;
                    } else {
                        revisions.add(rev);
                    }
                }
            }
        }
    }

    public TkRefsetStrMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRefsetStrMember(TkRefsetStrMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.string1 = another.string1;
    }

    public TkRefsetStrMember(RefexStringVersionBI refexStringVersion, NidBitSetBI excludedNids, Map<UUID, UUID> conversionMap,
            long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexStringVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);
        this.string1 = refexStringVersion.getString1();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>ERefsetStrMember</tt> object, and contains the same values, field by
     * field, as this <tt>ERefsetStrMember</tt>.
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

        if (TkRefsetStrMember.class.isAssignableFrom(obj.getClass())) {
            TkRefsetStrMember another = (TkRefsetStrMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare string1
            if (!this.string1.equals(another.string1)) {
                return false;
            }

            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>ERefsetStrMember</code>.
     *
     * @return a hash code value for this <tt>ERefsetStrMember</tt>.
     */
    @Override
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    @Override
    public TkRefsetStrMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefsetStrMember(this, conversionMap, offset, mapAll);
    }

    @Override
    public final void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        string1 = UtfHelper.readUtfV6(in, dataVersion);

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRefsetStrRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefsetStrRevision rev = new TkRefsetStrRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    string1 = rev.string1;
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
        buff.append(" str:");
        buff.append("'").append(this.string1).append("'");
        buff.append("; ");
        buff.append(super.toString());

        return buff.toString();
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        UtfHelper.writeUtf(out, string1);

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRefsetStrRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public List<TkRefsetStrRevision> getRevisionList() {
        return revisions;
    }

    public String getString1() {
        return string1;
    }

    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.STR;
    }

    //~--- set methods ---------------------------------------------------------
    public void setString1(String string1) {
        this.string1 = string1;
    }
}
