package org.ihtsdo.tk.dto.concept.component.refex.type_uuid_long;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.type_nid_long.RefexNidLongVersionBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.dto.RevisionHandling;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_int.TkRefexUuidIntRevision;

public class TkRefexUuidLongMember extends TkRefexAbstractMember<TkRefexUuidLongRevision> {

    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    public UUID uuid1;
    public List<TkRefexUuidLongRevision> extraVersions;
    public long long1;

    //~--- constructors --------------------------------------------------------
    public TkRefexUuidLongMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexNidLongVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    public TkRefexUuidLongMember(RefexNidLongVersionBI refexNidLongVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(refexNidLongVersion);
        TerminologyStoreDI ts = Ts.get();
        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            this.uuid1 = ts.getUuidPrimordialForNid(refexNidLongVersion.getNid1());
            this.long1 = refexNidLongVersion.getLong1();
        } else {
            Collection<? extends RefexNidLongVersionBI> refexes = refexNidLongVersion.getVersions();
            int partCount = refexes.size();
            Iterator<? extends RefexNidLongVersionBI> itr = refexes.iterator();
            RefexNidLongVersionBI rv = itr.next();

            this.uuid1 = ts.getUuidPrimordialForNid(rv.getNid1());
            this.long1 = rv.getLong1();

            if (partCount > 1) {
                revisions = new ArrayList<TkRefexUuidLongRevision>(partCount - 1);

                while (itr.hasNext()) {
                    rv = itr.next();
                    TkRefexUuidLongRevision rev = new TkRefexUuidLongRevision(rv);
                    if (rev.getTime() == this.time) {
                        // TODO this check can be removed after trek-95 change sets are no longer in production. 
                        this.uuid1 = rev.uuid1;
                        this.long1 = rev.long1;
                    } else {
                        revisions.add(rev);
                    }
                }
            }
        }
    }

    public TkRefexUuidLongMember() {
        super();
    }

    public TkRefexUuidLongMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRefexUuidLongMember(TkRefexUuidLongMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.uuid1 = conversionMap.get(another.uuid1);
            this.long1 = another.long1;
        } else {
            this.uuid1 = another.uuid1;
            this.long1 = another.long1;
        }
    }

    public TkRefexUuidLongMember(RefexNidLongVersionBI refexNidLongVersion, NidBitSetBI excludedNids,
            Map<UUID, UUID> conversionMap, long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexNidLongVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);

        if (mapAll) {
            this.uuid1 = conversionMap.get(Ts.get().getComponent(refexNidLongVersion.getNid1()).getPrimUuid());
        } else {
            this.uuid1 = Ts.get().getComponent(refexNidLongVersion.getNid1()).getPrimUuid();
        }

        this.long1 = refexNidLongVersion.getLong1();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>ERefsetCidLongMember</tt> object, and contains the same values, field by
     * field, as this <tt>ERefsetCidLongMember</tt>.
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

        if (TkRefexUuidLongMember.class.isAssignableFrom(obj.getClass())) {
            TkRefexUuidLongMember another = (TkRefexUuidLongMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare uuid1
            if (!this.uuid1.equals(another.uuid1)) {
                return false;
            }

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
     * <code>ERefsetCidLongMember</code>.
     *
     * @return a hash code value for this <tt>ERefsetCidLongMember</tt>.
     */
    @Override
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    @Override
    public TkRefexUuidLongMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexUuidLongMember(this, conversionMap, offset, mapAll);
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        uuid1 = new UUID(in.readLong(), in.readLong());
        long1 = in.readLong();

        int versionSize = in.readInt();

        if (versionSize > 0) {
            extraVersions = new ArrayList<TkRefexUuidLongRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefexUuidLongRevision rev = new TkRefexUuidLongRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    uuid1 = rev.uuid1;
                    long1 = rev.long1;
                } else {
                    extraVersions.add(rev);
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
        buff.append(" c1: ");
        buff.append(informAboutUuid(this.uuid1));
        buff.append(" long:");
        buff.append(this.long1);
        buff.append(" ");
        buff.append(super.toString());

        return buff.toString();
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(uuid1.getMostSignificantBits());
        out.writeLong(uuid1.getLeastSignificantBits());
        out.writeLong(long1);

        if (extraVersions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(extraVersions.size());

            for (TkRefexUuidLongRevision rmv : extraVersions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    public UUID getUuid1() {
        return uuid1;
    }

    public long getLong1() {
        return long1;
    }

    @Override
    public List<TkRefexUuidLongRevision> getRevisionList() {
        return extraVersions;
    }

    @Override
    public List<TkRefexUuidLongRevision> getRevisions() {
        return extraVersions;
    }

    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.CID_LONG;
    }

    //~--- set methods ---------------------------------------------------------
    public void setUuid1(UUID uuid1) {
        this.uuid1 = uuid1;
    }

    public void setLong1(long long1) {
        this.long1 = long1;
    }
}
