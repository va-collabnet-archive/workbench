package org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid_uuid;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_nid_nid_nid.RefexNidNidNidVersionBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.dto.RevisionHandling;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid.TkRefsetUuidUuidRevision;

public class TkRefexUuidUuidUuidMember extends TkRefexAbstractMember<TkRefexUuidUuidUuidRevision> {

    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    public UUID uuid1;
    public UUID uuid2;
    public UUID uuid3;

    //~--- constructors --------------------------------------------------------
    public TkRefexUuidUuidUuidMember() {
        super();
    }

    public TkRefexUuidUuidUuidMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexNidNidNidVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    public TkRefexUuidUuidUuidMember(RefexNidNidNidVersionBI refexNidNidNidVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(refexNidNidNidVersion);
        TerminologyStoreDI ts = Ts.get();
        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            this.uuid1 = ts.getUuidPrimordialForNid(refexNidNidNidVersion.getNid1());
            this.uuid2 = ts.getUuidPrimordialForNid(refexNidNidNidVersion.getNid2());
            this.uuid3 = ts.getUuidPrimordialForNid(refexNidNidNidVersion.getNid3());
        } else {
            Collection<? extends RefexNidNidNidVersionBI> refexes = refexNidNidNidVersion.getVersions();
            int partCount = refexes.size();
            Iterator<? extends RefexNidNidNidVersionBI> itr = refexes.iterator();
            RefexNidNidNidVersionBI rv = itr.next();

            this.uuid1 = ts.getUuidPrimordialForNid(rv.getNid1());
            this.uuid2 = ts.getUuidPrimordialForNid(rv.getNid2());
            this.uuid3 = ts.getUuidPrimordialForNid(rv.getNid3());

            if (partCount > 1) {
                revisions = new ArrayList<TkRefexUuidUuidUuidRevision>(partCount - 1);

                while (itr.hasNext()) {
                    rv = itr.next();
                    TkRefexUuidUuidUuidRevision rev = new TkRefexUuidUuidUuidRevision(rv);
                    if (rev.getTime() == this.time) {
                        // TODO this check can be removed after trek-95 change sets are no longer in production. 
                        this.uuid1 = rev.uuid1;
                        this.uuid2 = rev.uuid2;
                        this.uuid3 = rev.uuid3;
                    } else {
                        revisions.add(rev);
                    }
                }
            }
        }
    }

    public TkRefexUuidUuidUuidMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRefexUuidUuidUuidMember(TkRefexUuidUuidUuidMember another, Map<UUID, UUID> conversionMap,
            long offset, boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.uuid1 = conversionMap.get(another.uuid1);
            this.uuid2 = conversionMap.get(another.uuid2);
            this.uuid3 = conversionMap.get(another.uuid3);
        } else {
            this.uuid1 = another.uuid1;
            this.uuid2 = another.uuid2;
            this.uuid3 = another.uuid3;
        }
    }

    public TkRefexUuidUuidUuidMember(RefexNidNidNidVersionBI refexNidNidNidVersion, NidBitSetBI excludedNids,
            Map<UUID, UUID> conversionMap, long offset, boolean mapAll,
            ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexNidNidNidVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);

        if (mapAll) {
            this.uuid1 = conversionMap.get(Ts.get().getComponent(refexNidNidNidVersion.getNid1()).getPrimUuid());
            this.uuid2 = conversionMap.get(Ts.get().getComponent(refexNidNidNidVersion.getNid2()).getPrimUuid());
            this.uuid3 = conversionMap.get(Ts.get().getComponent(refexNidNidNidVersion.getNid3()).getPrimUuid());
        } else {
            this.uuid1 = Ts.get().getComponent(refexNidNidNidVersion.getNid1()).getPrimUuid();
            this.uuid2 = Ts.get().getComponent(refexNidNidNidVersion.getNid2()).getPrimUuid();
            this.uuid3 = Ts.get().getComponent(refexNidNidNidVersion.getNid3()).getPrimUuid();
        }
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>ERefsetCidCidCidMember</tt> object, and contains the same values, field
     * by field, as this <tt>ERefsetCidCidCidMember</tt>.
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

        if (TkRefexUuidUuidUuidMember.class.isAssignableFrom(obj.getClass())) {
            TkRefexUuidUuidUuidMember another = (TkRefexUuidUuidUuidMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare uuid1
            if (!this.uuid1.equals(another.uuid1)) {
                return false;
            }

            // Compare uuid2
            if (!this.uuid2.equals(another.uuid2)) {
                return false;
            }

            // Compare uuid3
            if (!this.uuid3.equals(another.uuid3)) {
                return false;
            }

            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>ERefsetCidCidCidMember</code>.
     *
     * @return a hash code value for this <tt>ERefsetCidCidCidMember</tt>.
     */
    @Override
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    @Override
    public TkRefexUuidUuidUuidMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexUuidUuidUuidMember(this, conversionMap, offset, mapAll);
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        uuid1 = new UUID(in.readLong(), in.readLong());
        uuid2 = new UUID(in.readLong(), in.readLong());
        uuid3 = new UUID(in.readLong(), in.readLong());

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRefexUuidUuidUuidRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefexUuidUuidUuidRevision rev = new TkRefexUuidUuidUuidRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    uuid1 = rev.uuid1;
                    uuid2 = rev.uuid2;
                    uuid3 = rev.uuid3;
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
        buff.append(informAboutUuid(this.uuid1));
        buff.append(" c2:");
        buff.append(informAboutUuid(this.uuid2));
        buff.append(" c3:");
        buff.append(informAboutUuid(this.uuid3));
        buff.append(" ");
        buff.append(super.toString());

        return buff.toString();
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(uuid1.getMostSignificantBits());
        out.writeLong(uuid1.getLeastSignificantBits());
        out.writeLong(uuid2.getMostSignificantBits());
        out.writeLong(uuid2.getLeastSignificantBits());
        out.writeLong(uuid3.getMostSignificantBits());
        out.writeLong(uuid3.getLeastSignificantBits());

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRefexUuidUuidUuidRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    public UUID getUuid1() {
        return uuid1;
    }

    public UUID getUuid2() {
        return uuid2;
    }

    public UUID getUuid3() {
        return uuid3;
    }

    @Override
    public List<TkRefexUuidUuidUuidRevision> getRevisionList() {
        return revisions;
    }

    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.CID_CID_CID;
    }

    //~--- set methods ---------------------------------------------------------
    public void setUuid1(UUID uuid1) {
        this.uuid1 = uuid1;
    }

    public void setUuid2(UUID uuid2) {
        this.uuid2 = uuid2;
    }

    public void setUuid3(UUID uuid3) {
        this.uuid3 = uuid3;
    }
}
