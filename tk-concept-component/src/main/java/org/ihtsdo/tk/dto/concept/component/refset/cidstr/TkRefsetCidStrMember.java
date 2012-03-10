package org.ihtsdo.tk.dto.concept.component.refset.cidstr;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_cnid_str.RefexCnidStrVersionBI;
import org.ihtsdo.tk.dto.concept.UtfHelper;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.dto.RevisionHandling;
import org.ihtsdo.tk.dto.concept.component.refset.cidlong.TkRefsetCidLongRevision;

public class TkRefsetCidStrMember extends TkRefsetAbstractMember<TkRefsetCidStrRevision> {

    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    public UUID c1Uuid;
    public String strValue;

    //~--- constructors --------------------------------------------------------
    public TkRefsetCidStrMember() {
        super();
    }

    public TkRefsetCidStrMember(RefexChronicleBI another) throws IOException {
        this((RefexCnidStrVersionBI) another.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    public TkRefsetCidStrMember(RefexCnidStrVersionBI another,
            RevisionHandling revisionHandling) throws IOException {
        super(another);

        TerminologyStoreDI ts = Ts.get();
        Collection<? extends RefexCnidStrVersionBI> refexes = another.getVersions();
        int partCount = refexes.size();
        Iterator<? extends RefexCnidStrVersionBI> itr = refexes.iterator();
        RefexCnidStrVersionBI rv = itr.next();

        this.c1Uuid = ts.getUuidPrimordialForNid(rv.getCnid1());
        this.strValue = rv.getStr1();

        if (partCount > 1) {
            revisions = new ArrayList<TkRefsetCidStrRevision>(partCount - 1);

            while (itr.hasNext()) {
                rv = itr.next();
                TkRefsetCidStrRevision rev = new TkRefsetCidStrRevision(rv);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    this.c1Uuid = rev.c1Uuid;
                    this.strValue = rev.strValue;
                } else {
                    revisions.add(rev);
                }
            }
        }
    }

    public TkRefsetCidStrMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRefsetCidStrMember(TkRefsetCidStrMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.c1Uuid = conversionMap.get(another.c1Uuid);
            this.strValue = another.strValue;
        } else {
            this.c1Uuid = another.c1Uuid;
            this.strValue = another.strValue;
        }
    }

    public TkRefsetCidStrMember(RefexCnidStrVersionBI another, NidBitSetBI exclusions,
            Map<UUID, UUID> conversionMap, long offset, boolean mapAll, ViewCoordinate vc)
            throws IOException, ContradictionException {
        super(another, exclusions, conversionMap, offset, mapAll, vc);

        if (mapAll) {
            this.c1Uuid = conversionMap.get(Ts.get().getComponent(another.getCnid1()).getPrimUuid());
        } else {
            this.c1Uuid = Ts.get().getComponent(another.getCnid1()).getPrimUuid();
        }

        this.strValue = another.getStr1();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>ERefsetCidStrMember</tt> object, and contains the same values, field by
     * field, as this <tt>ERefsetCidStrMember</tt>.
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

        if (TkRefsetCidStrMember.class.isAssignableFrom(obj.getClass())) {
            TkRefsetCidStrMember another = (TkRefsetCidStrMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare c1Uuid
            if (!this.c1Uuid.equals(another.c1Uuid)) {
                return false;
            }

            // Compare strValue
            if (!this.strValue.equals(another.strValue)) {
                return false;
            }

            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>ERefsetCidStrMember</code>.
     *
     * @return a hash code value for this <tt>ERefsetCidStrMember</tt>.
     */
    @Override
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    @Override
    public TkRefsetCidStrMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefsetCidStrMember(this, conversionMap, offset, mapAll);
    }

    @Override
    public final void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        c1Uuid = new UUID(in.readLong(), in.readLong());
        strValue = UtfHelper.readUtfV7(in, dataVersion);

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRefsetCidStrRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefsetCidStrRevision rev = new TkRefsetCidStrRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    c1Uuid = rev.c1Uuid;
                    strValue = rev.strValue;
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
        buff.append(" str:");
        buff.append("'").append(this.strValue).append("'");
        buff.append(" ");
        buff.append(super.toString());

        return buff.toString();
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(c1Uuid.getMostSignificantBits());
        out.writeLong(c1Uuid.getLeastSignificantBits());
        UtfHelper.writeUtf(out, strValue);

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRefsetCidStrRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    public UUID getC1Uuid() {
        return c1Uuid;
    }

    @Override
    public List<TkRefsetCidStrRevision> getRevisionList() {
        return revisions;
    }

    @Override
    public List<TkRefsetCidStrRevision> getRevisions() {
        return revisions;
    }

    public String getStrValue() {
        return strValue;
    }

    @Override
    public TK_REFSET_TYPE getType() {
        return TK_REFSET_TYPE.CID_STR;
    }

    //~--- set methods ---------------------------------------------------------
    public void setC1Uuid(UUID c1Uuid) {
        this.c1Uuid = c1Uuid;
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }
}
