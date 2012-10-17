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
package org.ihtsdo.tk.dto.concept.component.refex.type_uuid_int;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.type_nid_int.RefexNidIntVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
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
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_float.TkRefexUuidFloatRevision;

/**
 * The Class TkRefexUuidIntMember represents a uuid-int type refex member in the
 * eConcept format and contains methods specific for interacting with a uuid-int
 * type refex member. Further discussion of the eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkRefexUuidIntMember extends TkRefexAbstractMember<TkRefexUuidIntRevision> {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The uuid associated with this TK Refex Uuid Int Member.
     */
    public UUID uuid1;
    /**
     * The int associated with this TK Refex Uuid Int Member.
     */
    public int int1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refex Uuid Int Member based on the
     * <code>refexChronicle</code>.
     *
     * @param refexChronicle the refex chronicle specifying how to construct
     * this TK Refex Uuid Int Member
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexUuidIntMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexNidIntVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new TK Refex Uuid Int Member based on the
     * <code>refexNidIntVersion</code> and using the given
     * <code>revisionHandling</code>.
     *
     * @param refexNidIntVersion the refex nid int version specifying how to
     * construct this TK Refex Uuid Int Member
     * @param revisionHandling specifying if addition versions should be
     * included or not
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexUuidIntMember(RefexNidIntVersionBI refexNidIntVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(refexNidIntVersion);
        TerminologyStoreDI ts = Ts.get();
        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            this.uuid1 = ts.getUuidPrimordialForNid(refexNidIntVersion.getNid1());
            this.int1 = refexNidIntVersion.getInt1();
        } else {
            Collection<? extends RefexNidIntVersionBI> refexes = refexNidIntVersion.getVersions();
            int partCount = refexes.size();
            Iterator<? extends RefexNidIntVersionBI> relItr = refexes.iterator();
            RefexNidIntVersionBI rv = relItr.next();

            this.uuid1 = ts.getUuidPrimordialForNid(rv.getNid1());
            this.int1 = rv.getInt1();

            if (partCount > 1) {
                revisions = new ArrayList<TkRefexUuidIntRevision>(partCount - 1);

                while (relItr.hasNext()) {
                    rv = relItr.next();
                    TkRefexUuidIntRevision rev = new TkRefexUuidIntRevision(rv);
                    if (rev.getTime() == this.time) {
                        // TODO this check can be removed after trek-95 change sets are no longer in production. 
                        this.uuid1 = rev.uuid1;
                        this.int1 = rev.int1;
                    } else {
                        revisions.add(rev);
                    }
                }
            }
        }
    }

    /**
     * Instantiates a new TK Refex Uuid Int Member.
     */
    public TkRefexUuidIntMember() {
        super();
    }

    /**
     * Instantiates a new TK Refex Uuid Int Member based on the specified data
     * input,
     * <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Refex Uuid
     * Int Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRefexUuidIntMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refex Uuid Int Member based on
     * <code>another</code> TK Refex Uuid Int Member and allows for uuid
     * conversion.
     *
     * @param another the TK Refex Uuid Int Member specifying how to construct
     * this TK Refex Uuid Int Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Uuid Int Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid Int Member based on the conversion map
     */
    public TkRefexUuidIntMember(TkRefexUuidIntMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.uuid1 = conversionMap.get(another.uuid1);
            this.int1 = another.int1;
        } else {
            this.uuid1 = another.uuid1;
            this.int1 = another.int1;
        }
    }

    /**
     * Instantiates a new TK Refex Uuid Int Member based on a
     * <code>refexNidIntVersion</code> and allows for uuid conversion. Can
     * exclude components based on their nid.
     *
     * @param refexNidIntVersion the refex nid float version specifying how to
     * construct this TK Refex Uuid Int Member
     * @param excludedNids the nids in the specified component version to
     * exclude from this TK Refex Uuid Int Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Uuid Int Member
     * @param mapAll the map all
     * @param viewCoordinate the view coordinate specifying which version of the
     * components to use
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for the
     * given view coordinate
     */
    public TkRefexUuidIntMember(RefexNidIntVersionBI refexNidIntVersion, NidBitSetBI excludedNids,
            Map<UUID, UUID> conversionMap, long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexNidIntVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);

        if (mapAll) {
            this.uuid1 = conversionMap.get(Ts.get().getComponent(refexNidIntVersion.getNid1()).getPrimUuid());
        } else {
            this.uuid1 = Ts.get().getComponent(refexNidIntVersion.getNid1()).getPrimUuid();
        }

        this.int1 = refexNidIntVersion.getInt1();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetCidIntMember</tt> object, and contains the same values, field
     * by field, as this <tt>ERefsetCidIntMember</tt>.
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

        if (TkRefexUuidIntMember.class.isAssignableFrom(obj.getClass())) {
            TkRefexUuidIntMember another = (TkRefexUuidIntMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare uuid1
            if (!this.uuid1.equals(another.uuid1)) {
                return false;
            }

            // Compare int1
            if (this.int1 != another.int1) {
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

    /**
     *
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Uuid Int Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid Int Member based on the conversion map
     * @return the converted TK Refex Uuid Int Member
     */
    @Override
    public TkRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexUuidIntMember(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK
     * Refex Uuid Int Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        uuid1 = new UUID(in.readLong(), in.readLong());
        int1 = in.readInt();

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRefexUuidIntRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefexUuidIntRevision rev = new TkRefexUuidIntRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    uuid1 = rev.uuid1;
                    int1 = rev.int1;
                } else {
                    revisions.add(rev);
                }
            }
        }
    }

    /**
     * Returns a string representation of this TK Refex Uuid Int Member object.
     *
     * @return a string representation of this TK Refex Uuid Int Member object
     * including the concept represented by the uuid and the integer.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" c1:");
        buff.append(informAboutUuid(this.uuid1));
        buff.append(" int:");
        buff.append(this.int1);
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
        out.writeInt(int1);

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRefexUuidIntRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the uuid associated with this TK Refex Uuid Int Member.
     *
     * @return the uuid associated with this TK Refex Uuid Int Member
     */
    public UUID getUuid1() {
        return uuid1;
    }

    /**
     * Gets the int associated with this TK Refex Uuid Int Member.
     *
     * @return the int associated with this TK Refex Uuid Int Member
     */
    public int getInt1() {
        return int1;
    }

    /**
     * 
     * @return a list of revisions on this TK Refex Uuid Int Member
     */
    @Override
    public List<TkRefexUuidIntRevision> getRevisionList() {
        return revisions;
    }

    /**
     * 
     * @return TK_REFEX_TYPE.CID_INT
     */
    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.CID_INT;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the uuid associated with this TK Refex Uuid Int Member.
     *
     * @param uuid1 the uuid associated with this TK Refex Uuid Int Member
     */
    public void setUuid1(UUID uuid1) {
        this.uuid1 = uuid1;
    }

    /**
     * Sets the int associated with this TK Refex Uuid Int Member.
     *
     * @param int1 the int associated with this TK Refex Uuid Int Member
     */
    public void setInt1(int int1) {
        this.int1 = int1;
    }
}
