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
package org.ihtsdo.tk.dto.concept.component.refex.type_uuid;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
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
import org.ihtsdo.tk.dto.concept.component.refex.type_arrayofbytearray.TkRefexArrayOfByteArrayRevision;

// TODO: Auto-generated Javadoc
/**
 * The Class TkRefexUuidMember.
 */
public class TkRefexUuidMember extends TkRefexAbstractMember<TkRefexUuidRevision> {

    /** The Constant serialVersionUID. */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /** The uuid1. */
    public UUID uuid1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new tk refex uuid member.
     *
     * @param refexChronicle the refex chronicle
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexUuidMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexNidVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new tk refex uuid member.
     *
     * @param refexNidVersion the refex nid version
     * @param revisionHandling the revision handling
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexUuidMember(RefexNidVersionBI refexNidVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(refexNidVersion);

        TerminologyStoreDI ts = Ts.get();
        Collection<? extends RefexNidVersionBI> refexes = refexNidVersion.getVersions();
        int partCount = refexes.size();
        Iterator<? extends RefexNidVersionBI> itr = refexes.iterator();
        RefexNidVersionBI rv = itr.next();

        this.uuid1 = ts.getUuidPrimordialForNid(rv.getNid1());

        if (partCount > 1) {
            revisions = new ArrayList<TkRefexUuidRevision>(partCount - 1);

            while (itr.hasNext()) {
                rv = itr.next();
                TkRefexUuidRevision rev = new TkRefexUuidRevision(rv);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    this.uuid1 = rev.uuid1;
                } else {
                    revisions.add(rev);
                }
            }
        }
    }

    /**
     * Instantiates a new tk refex uuid member.
     */
    public TkRefexUuidMember() {
        super();
    }

    /**
     * Instantiates a new tk refex uuid member.
     *
     * @param in the in
     * @param dataVersion the data version
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException the class not found exception
     */
    public TkRefexUuidMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new tk refex uuid member.
     *
     * @param another the another
     * @param conversionMap the conversion map
     * @param offset the offset
     * @param mapAll the map all
     */
    public TkRefexUuidMember(TkRefexUuidMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.uuid1 = conversionMap.get(another.uuid1);
        } else {
            this.uuid1 = another.uuid1;
        }
    }

    /**
     * Instantiates a new tk refex uuid member.
     *
     * @param refexNidVersion the refex nid version
     * @param excludedNids the excluded nids
     * @param conversionMap the conversion map
     * @param offset the offset
     * @param mapAll the map all
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException the contradiction exception
     */
    public TkRefexUuidMember(RefexNidVersionBI refexNidVersion, NidBitSetBI excludedNids,
            Map<UUID, UUID> conversionMap, long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexNidVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);

        if (mapAll) {
            this.uuid1 = conversionMap.get(Ts.get().getComponent(refexNidVersion.getNid1()).getPrimUuid());
        } else {
            this.uuid1 = Ts.get().getComponent(refexNidVersion.getNid1()).getPrimUuid();
        }
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>ERefsetCidMember</tt> object, and contains the same values, field by
     * field, as this <tt>ERefsetCidMember</tt>.
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

        if (TkRefexUuidMember.class.isAssignableFrom(obj.getClass())) {
            TkRefexUuidMember another = (TkRefexUuidMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare setUuid1
            if (!this.uuid1.equals(another.uuid1)) {
                return false;
            }

            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>ERefsetCidMember</code>.
     *
     * @return a hash code value for this <tt>ERefsetCidMember</tt>.
     */
    @Override
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkRevision#makeConversion(java.util.Map, long, boolean)
     */
    @Override
    public TkRefexUuidMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexUuidMember(this, conversionMap, offset, mapAll);
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember#readExternal(java.io.DataInput, int)
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        uuid1 = new UUID(in.readLong(), in.readLong());

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRefexUuidRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefexUuidRevision rev = new TkRefexUuidRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    uuid1 = rev.uuid1;
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

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRefexUuidRevision rmv : revisions) {
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

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkComponent#getRevisionList()
     */
    @Override
    public List<TkRefexUuidRevision> getRevisionList() {
        return revisions;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember#getType()
     */
    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.CID;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the uuid1.
     *
     * @param uuid1 the new uuid1
     */
    public void setUuid1(UUID uuid1) {
        this.uuid1 = uuid1;
    }
}
