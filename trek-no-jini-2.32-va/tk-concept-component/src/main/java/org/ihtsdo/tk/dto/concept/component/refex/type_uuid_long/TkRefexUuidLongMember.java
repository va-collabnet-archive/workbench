/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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

/**
 * The Class TkRefexUuidLongMember represents a uuid-long type refex member in
 * the eConcept format and contains methods specific for interacting with a
 * uuid-long type refex member. Further discussion of the eConcept format can be
 * found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkRefexUuidLongMember extends TkRefexAbstractMember<TkRefexUuidLongRevision> {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The uuid associated with this TK Refex Uuid Long Member.
     */
    public UUID uuid1;
    /**
     * The extra versions associated with this TK Refex Uuid Long Member.
     */
    public List<TkRefexUuidLongRevision> extraVersions;
    /**
     * The long associated with this TK Refex Uuid Long Member.
     */
    public long long1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refex Uuid Long Member based on the
     * <code>refexChronicle</code>.
     *
     * @param refexChronicle the refex chronicle specifying how to construct
     * this TK Refex Uuid Long Member
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexUuidLongMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexNidLongVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new TK Refex Uuid Long Member based on the
     * <code>refexNidLongVersion</code> and using the given
     * <code>revisionHandling</code>.
     *
     * @param refexNidLongVersion the refex nid long version specifying how to
     * construct this TK Refex Uuid Long Member
     * @param revisionHandling specifying if addition versions should be
     * included or not
     * @throws IOException signals that an I/O exception has occurred
     */
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

    /**
     * Instantiates a new TK Refex Uuid Long Member.
     */
    public TkRefexUuidLongMember() {
        super();
    }

    /**
     * Instantiates a new TK Refex Uuid Long Member based on the specified data
     * input, <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Refex Uuid
     * Long Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRefexUuidLongMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refex Uuid Long Member based on
     * <code>another</code> TK Refex Uuid Long Member and allows for uuid
     * conversion.
     *
     * @param another the TK Refex Uuid Long Member specifying how to construct
     * this TK Refex Uuid Long Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Uuid Long Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid Long Member based on the conversion map
     */
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

    /**
     * Instantiates a new TK Refex Uuid Long Member based on a
     * <code>refexNidLongVersion</code> and allows for uuid conversion. Can
     * exclude components based on their nid.
     *
     * @param refexNidLongVersion the refex nid long version specifying how to
     * construct this TK Refex Uuid Long Member
     * @param excludedNids the nids in the specified component version to
     * exclude from this TK Refex Uuid Long Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Uuid Long Member
     * @param mapAll the map all
     * @param viewCoordinate the view coordinate specifying which version of the
     * components to use
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for the
     * given view coordinate
     */
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
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetCidLongMember</tt> object, and contains the same values, field
     * by field, as this <tt>ERefsetCidLongMember</tt>.
     *
     * @param obj the object to compare with.
     * @return <code>true</code>, if successful <code>true</code> if the objects
     * are the same; <code>false</code> otherwise.
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

    /**
     *
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Uuid Long Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid Long Member based on the conversion map
     * @return the converted TK Refex Uuid Long Member
     */
    @Override
    public TkRefexUuidLongMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexUuidLongMember(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK
     * Refex Uuid Long Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
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
     * Returns a string representation of this TK Refex Uuid Long Member object.
     *
     * @return a string representation of this TK Refex Uuid Long Member object
     * including the concept represented by the uuid and the long.
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

    /**
     *
     * @param out the data output object that writes to the external source
     * @throws IOException signals that an I/O exception has occurred
     */
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
    /**
     * Gets the uuid associated with this TK Refex Uuid Long Member.
     *
     * @return the uuid associated with this TK Refex Uuid Long Member
     */
    public UUID getUuid1() {
        return uuid1;
    }

    /**
     * Gets the long associated with this TK Refex Uuid Long Member.
     *
     * @return the long associated with this TK Refex Uuid Long Member
     */
    public long getLong1() {
        return long1;
    }

    /**
     * 
     * @return a list of revisions on this TK Refex Uuid Long Member
     */
    @Override
    public List<TkRefexUuidLongRevision> getRevisionList() {
        return extraVersions;
    }

    /**
     * 
     * @return a list of revisions on this TK Refex Uuid Int Member
     */
    @Override
    public List<TkRefexUuidLongRevision> getRevisions() {
        return extraVersions;
    }

    /**
     * 
     * @return TK_REFEX_TYPE.CID_LONG
     */
    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.CID_LONG;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the uuid associated with this TK Refex Uuid Long Member.
     *
     * @param uuid1 the uuid associated with this TK Refex Uuid Long Member
     */
    public void setUuid1(UUID uuid1) {
        this.uuid1 = uuid1;
    }

    /**
     * Sets the long associated with this TK Refex Uuid Long Member.
     *
     * @param long1 the long associated with this TK Refex Uuid Long Member
     */
    public void setLong1(long long1) {
        this.long1 = long1;
    }
}
