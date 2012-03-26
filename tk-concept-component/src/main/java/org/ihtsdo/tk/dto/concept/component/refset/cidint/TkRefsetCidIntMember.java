package org.ihtsdo.tk.dto.concept.component.refset.cidint;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.type_cnid_int.RefexCnidIntVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.dto.RevisionHandling;
import org.ihtsdo.tk.dto.concept.component.refset.cidflt.TkRefsetCidFloatRevision;

public class TkRefsetCidIntMember extends TkRefsetAbstractMember<TkRefsetCidIntRevision> {

    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    public UUID c1Uuid;
    public int intValue;

    //~--- constructors --------------------------------------------------------
    public TkRefsetCidIntMember(RefexChronicleBI another) throws IOException {
        this((RefexCnidIntVersionBI) another.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    public TkRefsetCidIntMember(RefexCnidIntVersionBI another,
            RevisionHandling revisionHandling) throws IOException {
        super(another);
        TerminologyStoreDI ts = Ts.get();
        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            this.c1Uuid = ts.getUuidPrimordialForNid(another.getCnid1());
            this.intValue = another.getInt1();
        } else {
            Collection<? extends RefexCnidIntVersionBI> refexes = another.getVersions();
            int partCount = refexes.size();
            Iterator<? extends RefexCnidIntVersionBI> relItr = refexes.iterator();
            RefexCnidIntVersionBI rv = relItr.next();

            this.c1Uuid = ts.getUuidPrimordialForNid(rv.getCnid1());
            this.intValue = rv.getInt1();

            if (partCount > 1) {
                revisions = new ArrayList<TkRefsetCidIntRevision>(partCount - 1);

                while (relItr.hasNext()) {
                    rv = relItr.next();
                    TkRefsetCidIntRevision rev = new TkRefsetCidIntRevision(rv);
                    if (rev.getTime() == this.time) {
                        // TODO this check can be removed after trek-95 change sets are no longer in production. 
                        this.c1Uuid = rev.c1Uuid;
                        this.intValue = rev.intValue;
                    } else {
                        revisions.add(rev);
                    }
                }
            }
        }
    }

    public TkRefsetCidIntMember() {
        super();
    }

    public TkRefsetCidIntMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRefsetCidIntMember(TkRefsetCidIntMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.c1Uuid = conversionMap.get(another.c1Uuid);
            this.intValue = another.intValue;
        } else {
            this.c1Uuid = another.c1Uuid;
            this.intValue = another.intValue;
        }
    }

    public TkRefsetCidIntMember(RefexCnidIntVersionBI another, NidBitSetBI exclusions,
            Map<UUID, UUID> conversionMap, long offset, boolean mapAll, ViewCoordinate vc)
            throws IOException, ContradictionException {
        super(another, exclusions, conversionMap, offset, mapAll, vc);

        if (mapAll) {
            this.c1Uuid = conversionMap.get(Ts.get().getComponent(another.getCnid1()).getPrimUuid());
        } else {
            this.c1Uuid = Ts.get().getComponent(another.getCnid1()).getPrimUuid();
        }

        this.intValue = another.getInt1();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>ERefsetCidIntMember</tt> object, and contains the same values, field by
     * field, as this <tt>ERefsetCidIntMember</tt>.
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

        if (TkRefsetCidIntMember.class.isAssignableFrom(obj.getClass())) {
            TkRefsetCidIntMember another = (TkRefsetCidIntMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare c1Uuid
            if (!this.c1Uuid.equals(another.c1Uuid)) {
                return false;
            }

            // Compare intValue
            if (this.intValue != another.intValue) {
                return false;
            }

            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>ERefsetCidIntMember</code>.
     *
     * @return a hash code value for this <tt>ERefsetCidIntMember</tt>.
     */
    @Override
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    @Override
    public TkRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefsetCidIntMember(this, conversionMap, offset, mapAll);
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        c1Uuid = new UUID(in.readLong(), in.readLong());
        intValue = in.readInt();

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRefsetCidIntRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefsetCidIntRevision rev = new TkRefsetCidIntRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    c1Uuid = rev.c1Uuid;
                    intValue = rev.intValue;
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
        buff.append(" c1:");
        buff.append(informAboutUuid(this.c1Uuid));
        buff.append(" int:");
        buff.append(this.intValue);
        buff.append(" ");
        buff.append(super.toString());

        return buff.toString();
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(c1Uuid.getMostSignificantBits());
        out.writeLong(c1Uuid.getLeastSignificantBits());
        out.writeInt(intValue);

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRefsetCidIntRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    public UUID getC1Uuid() {
        return c1Uuid;
    }

    public int getIntValue() {
        return intValue;
    }

    @Override
    public List<TkRefsetCidIntRevision> getRevisionList() {
        return revisions;
    }

    @Override
    public TK_REFSET_TYPE getType() {
        return TK_REFSET_TYPE.CID_INT;
    }

    //~--- set methods ---------------------------------------------------------
    public void setC1Uuid(UUID c1Uuid) {
        this.c1Uuid = c1Uuid;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }
}