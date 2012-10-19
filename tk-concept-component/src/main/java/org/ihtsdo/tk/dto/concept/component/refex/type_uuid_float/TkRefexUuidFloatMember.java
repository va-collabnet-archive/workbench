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
package org.ihtsdo.tk.dto.concept.component.refex.type_uuid_float;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_nid_float.RefexNidFloatVersionBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.dto.RevisionHandling;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid_string.TkRefexUuidUuidStringRevision;

/**
 * The Class TkRefexUuidFloatMember represents a uuid-float type refex member in
 * the eConcept format and contains methods specific for interacting with a
 * uuid-float type refex member. Further discussion of the eConcept format can
 * be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkRefexUuidFloatMember extends TkRefexAbstractMember<TkRefexUuidFloatRevision> {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The uuid associated with this TK Refex Uuid Float Member.
     */
    public UUID uuid1;
    /**
     * The float associated with this TK Refex Uuid Float Member.
     */
    public float float1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refex Uuid Float Member.
     */
    public TkRefexUuidFloatMember() {
        super();
    }

    /**
     * Instantiates a new TK Refex Uuid Float Member based on the
     * <code>refexChronicle</code>.
     *
     * @param refexChronicle the refex chronicle specifying how to construct
     * this TK Refex Uuid Float Member
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexUuidFloatMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexNidFloatVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new TK Refex Uuid Float Member based on the
     * <code>refexNidFloatVersion</code> and using the given
     * <code>revisionHandling</code>.
     *
     * @param refexNidFloatVersion the refex nid float version specifying how to
     * construct this TK Refex Uuid Float Member
     * @param revisionHandling specifying if addition versions should be
     * included or not
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexUuidFloatMember(RefexNidFloatVersionBI refexNidFloatVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(refexNidFloatVersion);
        TerminologyStoreDI ts = Ts.get();
        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            this.uuid1 = ts.getUuidPrimordialForNid(refexNidFloatVersion.getNid1());
            this.float1 = refexNidFloatVersion.getFloat1();
        } else {
            Collection<? extends RefexNidFloatVersionBI> refexes = refexNidFloatVersion.getVersions();
            int partCount = refexes.size();
            Iterator<? extends RefexNidFloatVersionBI> itr = refexes.iterator();
            RefexNidFloatVersionBI rv = itr.next();

            this.uuid1 = ts.getUuidPrimordialForNid(rv.getNid1());
            this.float1 = rv.getFloat1();

            if (partCount > 1) {
                revisions = new ArrayList<TkRefexUuidFloatRevision>(partCount - 1);

                while (itr.hasNext()) {
                    rv = itr.next();
                    TkRefexUuidFloatRevision rev = new TkRefexUuidFloatRevision(rv);
                    if (rev.getTime() == this.time) {
                        // TODO this check can be removed after trek-95 change sets are no longer in production. 
                        this.uuid1 = rev.uuid1;
                        this.float1 = rev.float1;
                    } else {
                        revisions.add(rev);
                    }
                }
            }
        }
    }

    /**
     * Instantiates a new TK Refex Uuid Float Member based on the specified data
     * input,
     * <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Refex Uuid
     * Float Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRefexUuidFloatMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refex Uuid Float Member based on
     * <code>another</code> TK Refex Uuid Float Member and allows for uuid
     * conversion.
     *
     * @param another the TK Refex Uuid Float Member specifying how to construct
     * this TK Refex Uuid Float Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Uuid Float Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid Float Member based on the conversion map
     */
    public TkRefexUuidFloatMember(TkRefexUuidFloatMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.uuid1 = conversionMap.get(another.uuid1);
            this.float1 = another.float1;
        } else {
            this.uuid1 = another.uuid1;
            this.float1 = another.float1;
        }
    }

    /**
     * Instantiates a new TK Refex Uuid Float Member based on a
     * <code>refexNidFloatVersion</code> and allows for uuid conversion. Can
     * exclude components based on their nid.
     *
     * @param refexNidFloatVersion the refex nid float version specifying how to
     * construct this TK Refex Uuid Float Member
     * @param excludedNids the nids in the specified component version to
     * exclude from this TK Refex Uuid Float Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Uuid Float Member
     * @param mapAll the map all
     * @param viewCoordinate the view coordinate specifying which version of the
     * components to use
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for the
     * given view coordinate
     */
    public TkRefexUuidFloatMember(RefexNidFloatVersionBI refexNidFloatVersion, NidBitSetBI excludedNids,
            Map<UUID, UUID> conversionMap, long offset, boolean mapAll,
            ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexNidFloatVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);

        if (mapAll) {
            this.uuid1 = conversionMap.get(Ts.get().getComponent(refexNidFloatVersion.getNid1()).getPrimUuid());
        } else {
            this.uuid1 = Ts.get().getComponent(refexNidFloatVersion.getNid1()).getPrimUuid();
        }

        this.float1 = refexNidFloatVersion.getFloat1();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetCidFloatMember</tt> object, and contains the same values,
     * field by field, as this <tt>ERefsetCidFloatMember</tt>.
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

        if (TkRefexUuidFloatMember.class.isAssignableFrom(obj.getClass())) {
            TkRefexUuidFloatMember another = (TkRefexUuidFloatMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare uuid1
            if (!this.uuid1.equals(another.uuid1)) {
                return false;
            }

            // Compare float1
            if (this.float1 != another.float1) {
                return false;
            }

            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>ERefsetCidFloatMember</code>.
     *
     * @return a hash code value for this <tt>ERefsetCidFloatMember</tt>.
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
     * TK Refex Uuid Float Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid Float Member based on the conversion map
     * @return the converted TK Refex Uuid Float Member
     */
    @Override
    public TkRefexUuidFloatMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexUuidFloatMember(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK
     * Refex Uuid Float Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        uuid1 = new UUID(in.readLong(), in.readLong());
        float1 = in.readFloat();

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRefexUuidFloatRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefexUuidFloatRevision rev = new TkRefexUuidFloatRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    uuid1 = rev.uuid1;
                    float1 = rev.float1;
                } else {
                    revisions.add(rev);
                }
            }
        }
    }

    /**
     * Returns a string representation of this TK Refex Uuid Float Member object.
     *
     * @return a string representation of this TK Refex Uuid Float Member object
     * including the concept represented by the uuid and the float.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" c1:");
        buff.append(informAboutUuid(this.uuid1));
        buff.append(" flt:");
        buff.append(this.float1);
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
        out.writeFloat(float1);

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRefexUuidFloatRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the uuid associated with this TK Refex Uuid Float Member.
     *
     * @return the uuid associated with this TK Refex Uuid Float Member.
     */
    public UUID getUuid1() {
        return uuid1;
    }

    /**
     * Gets the float associated with this TK Refex Uuid Float Member.
     *
     * @return the float associated with this TK Refex Uuid Float Member.
     */
    public float getFloat1() {
        return float1;
    }

    /**
     *
     * @return a list of revisions on this TK Refex Uuid Float Member
     */
    @Override
    public List<TkRefexUuidFloatRevision> getRevisionList() {
        return revisions;
    }

    /**
     *
     * @return a list of revisions on this TK Refex Uuid Float Member
     */
    @Override
    public List<TkRefexUuidFloatRevision> getRevisions() {
        return revisions;
    }

    /**
     * 
     * @return TK_REFEX_TYPE.CID_FLOAT
     */
    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.CID_FLOAT;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the uuid associated with this TK Refex Uuid Float Member.
     *
     * @param uuid1 the uuid associated with this TK Refex Uuid Float Member
     */
    public void setUuid1(UUID uuid1) {
        this.uuid1 = uuid1;
    }

    /**
     * Sets the float associated with this TK Refex Uuid Float Member.
     *
     * @param float1 the float associated with this TK Refex Uuid Float Member
     */
    public void setFloat1(float float1) {
        this.float1 = float1;
    }
}
