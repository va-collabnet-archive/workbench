/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_nid_nid.RefexNidNidVersionBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.dto.RevisionHandling;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidRevision;

// TODO: Auto-generated Javadoc
/**
 * The Class TkRefexUuidUuidMember.
 */
public class TkRefexUuidUuidMember extends TkRefexAbstractMember<TkRefsetUuidUuidRevision> {

    /** The Constant serialVersionUID. */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /** The uuid1. */
    public UUID uuid1;
    
    /** The uuid2. */
    public UUID uuid2;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new tk refex uuid uuid member.
     */
    public TkRefexUuidUuidMember() {
        super();
    }

    /**
     * Instantiates a new tk refex uuid uuid member.
     *
     * @param refexChronicle the refex chronicle
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexUuidUuidMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexNidNidVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new tk refex uuid uuid member.
     *
     * @param refexNidNidVersion the refex nid nid version
     * @param revisionHandling the revision handling
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexUuidUuidMember(RefexNidNidVersionBI refexNidNidVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(refexNidNidVersion);
        TerminologyStoreDI ts = Ts.get();
        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            this.uuid1 = ts.getUuidPrimordialForNid(refexNidNidVersion.getNid1());
            this.uuid2 = ts.getUuidPrimordialForNid(refexNidNidVersion.getNid2());
        } else {
            Collection<? extends RefexNidNidVersionBI> rels = refexNidNidVersion.getVersions();
            int partCount = rels.size();
            Iterator<? extends RefexNidNidVersionBI> relItr = rels.iterator();
            RefexNidNidVersionBI rv = relItr.next();

            this.uuid1 = ts.getUuidPrimordialForNid(rv.getNid1());
            this.uuid2 = ts.getUuidPrimordialForNid(rv.getNid2());

            if (partCount > 1) {
                revisions = new ArrayList<TkRefsetUuidUuidRevision>(partCount - 1);

                while (relItr.hasNext()) {
                    rv = relItr.next();
                    TkRefsetUuidUuidRevision rev = new TkRefsetUuidUuidRevision(rv);
                    if (rev.getTime() == this.time) {
                        // TODO this check can be removed after trek-95 change sets are no longer in production. 
                        this.uuid1 = rev.uuid1;
                        this.uuid2 = rev.uuid2;
                    } else {
                        revisions.add(rev);
                    }
                }
            }
        }
    }

    /**
     * Instantiates a new tk refex uuid uuid member.
     *
     * @param in the in
     * @param dataVersion the data version
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException the class not found exception
     */
    public TkRefexUuidUuidMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new tk refex uuid uuid member.
     *
     * @param another the another
     * @param conversionMap the conversion map
     * @param offset the offset
     * @param mapAll the map all
     */
    public TkRefexUuidUuidMember(TkRefexUuidUuidMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.uuid1 = conversionMap.get(another.uuid1);
            this.uuid2 = conversionMap.get(another.uuid2);
        } else {
            this.uuid1 = another.uuid1;
            this.uuid2 = another.uuid2;
        }
    }

    /**
     * Instantiates a new tk refex uuid uuid member.
     *
     * @param refexNidNidVersion the refex nid nid version
     * @param excludedNids the excluded nids
     * @param conversionMap the conversion map
     * @param offset the offset
     * @param mapAll the map all
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException the contradiction exception
     */
    public TkRefexUuidUuidMember(RefexNidNidVersionBI refexNidNidVersion, NidBitSetBI excludedNids,
            Map<UUID, UUID> conversionMap, long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexNidNidVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);

        if (mapAll) {
            this.uuid1 = conversionMap.get(Ts.get().getComponent(refexNidNidVersion.getNid1()).getPrimUuid());
            this.uuid2 = conversionMap.get(Ts.get().getComponent(refexNidNidVersion.getNid2()).getPrimUuid());
        } else {
            this.uuid1 = Ts.get().getComponent(refexNidNidVersion.getNid1()).getPrimUuid();
            this.uuid2 = Ts.get().getComponent(refexNidNidVersion.getNid2()).getPrimUuid();
        }
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>ERefsetCidCidMember</tt> object, and contains the same values, field by
     * field, as this <tt>ERefsetCidCidMember</tt>.
     *
     * @param obj the object to compare with.
     * @return <code>true</code>, if successful
     * <code>true</code> if the objects are the same;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (TkRefexUuidUuidMember.class.isAssignableFrom(obj.getClass())) {
            TkRefexUuidUuidMember another = (TkRefexUuidUuidMember) obj;

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

            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>ERefsetCidCidMember</code>.
     *
     * @return a hash code value for this <tt>ERefsetCidCidMember</tt>.
     */
    @Override
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkRevision#makeConversion(java.util.Map, long, boolean)
     */
    @Override
    public TkRefexUuidUuidMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexUuidUuidMember(this, conversionMap, offset, mapAll);
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember#readExternal(java.io.DataInput, int)
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        uuid1 = new UUID(in.readLong(), in.readLong());
        uuid2 = new UUID(in.readLong(), in.readLong());

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRefsetUuidUuidRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefsetUuidUuidRevision rev = new TkRefsetUuidUuidRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    uuid1 = rev.uuid1;
                    uuid2 = rev.uuid2;
                } else {
                    revisions.add(rev);
                }
            }
        }
    }

    /**
     * Returns a string representation of the object.
     *
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" c1:");
        buff.append(informAboutUuid(this.uuid1));
        buff.append(" c2:");
        buff.append(informAboutUuid(this.uuid2));
        buff.append(" ");
        buff.append(super.toString());

        return buff.toString();
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember#writeExternal(java.io.DataOutput)
     */
    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(uuid1.getMostSignificantBits());
        out.writeLong(uuid1.getLeastSignificantBits());
        out.writeLong(uuid2.getMostSignificantBits());
        out.writeLong(uuid2.getLeastSignificantBits());

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRefsetUuidUuidRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the uuid1.
     *
     * @return the uuid1
     */
    public UUID getUuid1() {
        return uuid1;
    }

    /**
     * Gets the uuid2.
     *
     * @return the uuid2
     */
    public UUID getUuid2() {
        return uuid2;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkComponent#getRevisionList()
     */
    @Override
    public List<TkRefsetUuidUuidRevision> getRevisionList() {
        return revisions;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember#getType()
     */
    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.CID_CID;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the uuid1.
     *
     * @param c1Uuid the new uuid1
     */
    public void setUuid1(UUID c1Uuid) {
        this.uuid1 = c1Uuid;
    }

    /**
     * Sets the uuid2.
     *
     * @param c2Uuid the new uuid2
     */
    public void setUuid2(UUID c2Uuid) {
        this.uuid2 = c2Uuid;
    }
}
