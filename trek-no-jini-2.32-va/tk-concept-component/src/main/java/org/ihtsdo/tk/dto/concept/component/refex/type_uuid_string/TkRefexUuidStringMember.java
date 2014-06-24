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
package org.ihtsdo.tk.dto.concept.component.refex.type_uuid_string;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_nid_string.RefexNidStringVersionBI;
import org.ihtsdo.tk.dto.concept.UtfHelper;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.dto.RevisionHandling;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_long.TkRefexUuidLongRevision;

/**
 * The Class TkRefexUuidStringMember represents a uuid-string type refex member
 * in the eConcept format and contains methods specific for interacting with a
 * uuid-string type refex member. Further discussion of the eConcept format can
 * be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkRefexUuidStringMember extends TkRefexAbstractMember<TkRefexUuidStringRevision> {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The uuid associated with this TK Refex Uuid String Member.
     */
    public UUID uuid1;
    /**
     * The string associated with this TK Refex Uuid String Member.
     */
    public String string1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refex Uuid String Member.
     */
    public TkRefexUuidStringMember() {
        super();
    }

    /**
     * Instantiates a new TK Refex Uuid String Member based on the
     * <code>refexChronicle</code>.
     *
     * @param refexChronicle the refex chronicle specifying how to construct
     * this TK Refex Uuid String Member
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexUuidStringMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexNidStringVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new TK Refex Uuid String Member based on the
     * <code>refexNidStringVersion</code> and using the given
     * <code>revisionHandling</code>.
     *
     * @param refexNidStringVersion the refex nid string version specifying how to
     * construct this TK Refex Uuid String Member
     * @param revisionHandling specifying if addition versions should be
     * included or not
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexUuidStringMember(RefexNidStringVersionBI refexNidStringVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(refexNidStringVersion);

        TerminologyStoreDI ts = Ts.get();
        Collection<? extends RefexNidStringVersionBI> refexes = refexNidStringVersion.getVersions();
        int partCount = refexes.size();
        Iterator<? extends RefexNidStringVersionBI> itr = refexes.iterator();
        RefexNidStringVersionBI rv = itr.next();

        this.uuid1 = ts.getUuidPrimordialForNid(rv.getNid1());
        this.string1 = rv.getString1();

        if (partCount > 1) {
            revisions = new ArrayList<TkRefexUuidStringRevision>(partCount - 1);

            while (itr.hasNext()) {
                rv = itr.next();
                TkRefexUuidStringRevision rev = new TkRefexUuidStringRevision(rv);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    this.uuid1 = rev.uuid1;
                    this.string1 = rev.string1;
                } else {
                    revisions.add(rev);
                }
            }
        }
    }

    /**
     * Instantiates a new TK Refex Uuid String Member based on the specified data
     * input, <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Refex Uuid
     * String Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRefexUuidStringMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refex Uuid String Member based on
     * <code>another</code> TK Refex Uuid String Member and allows for uuid
     * conversion.
     *
     * @param another the TK Refex Uuid String Member specifying how to construct
     * this TK Refex Uuid String Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Uuid String Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid String Member based on the conversion map
     */
    public TkRefexUuidStringMember(TkRefexUuidStringMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.uuid1 = conversionMap.get(another.uuid1);
            this.string1 = another.string1;
        } else {
            this.uuid1 = another.uuid1;
            this.string1 = another.string1;
        }
    }

    /**
     * Instantiates a new TK Refex Uuid String Member based on a
     * <code>refexNidStringVersion</code> and allows for uuid conversion. Can
     * exclude components based on their nid.
     *
     * @param refexNidStringVersion the refex nid string version specifying how to
     * construct this TK Refex Uuid String Member
     * @param excludedNids the nids in the specified component version to
     * exclude from this TK Refex Uuid String Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Uuid String Member
     * @param mapAll the map all
     * @param viewCoordinate the view coordinate specifying which version of the
     * components to use
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for the
     * given view coordinate
     */
    public TkRefexUuidStringMember(RefexNidStringVersionBI refexNidStringVersion, NidBitSetBI excludedNids,
            Map<UUID, UUID> conversionMap, long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexNidStringVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);

        if (mapAll) {
            this.uuid1 = conversionMap.get(Ts.get().getComponent(refexNidStringVersion.getNid1()).getPrimUuid());
        } else {
            this.uuid1 = Ts.get().getComponent(refexNidStringVersion.getNid1()).getPrimUuid();
        }

        this.string1 = refexNidStringVersion.getString1();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetCidStrMember</tt> object, and contains the same values, field
     * by field, as this <tt>ERefsetCidStrMember</tt>.
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

        if (TkRefexUuidStringMember.class.isAssignableFrom(obj.getClass())) {
            TkRefexUuidStringMember another = (TkRefexUuidStringMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare uuid1
            if (!this.uuid1.equals(another.uuid1)) {
                return false;
            }

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
     * <code>ERefsetCidStrMember</code>.
     *
     * @return a hash code value for this <tt>ERefsetCidStrMember</tt>.
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
     * TK Refex Uuid String Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid String Member based on the conversion map
     * @return the converted TK Refex Uuid String Member
     */
    @Override
    public TkRefexUuidStringMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexUuidStringMember(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK
     * Refex Uuid String Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public final void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        uuid1 = new UUID(in.readLong(), in.readLong());
        string1 = UtfHelper.readUtfV7(in, dataVersion);

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRefexUuidStringRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefexUuidStringRevision rev = new TkRefexUuidStringRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    uuid1 = rev.uuid1;
                    string1 = rev.string1;
                } else {
                    revisions.add(rev);
                }
            }
        }
    }

    /**
     * Returns a string representation of this TK Refex Uuid String Member object.
     *
     * @return a string representation of this TK Refex Uuid String Member object
     * including the concept represented by the uuid and the string.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" c1:");
        buff.append(informAboutUuid(this.uuid1));
        buff.append(" str:");
        buff.append("'").append(this.string1).append("'");
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
        UtfHelper.writeUtf(out, string1);

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRefexUuidStringRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the uuid associated with this TK Refex Uuid String Member.
     *
     * @return the uuid associated with this TK Refex Uuid String Member
     */
    public UUID getUuid1() {
        return uuid1;
    }

    /**
     * 
     * @return a list of revisions on this TK Refex Uuid String Member
     */
    @Override
    public List<TkRefexUuidStringRevision> getRevisionList() {
        return revisions;
    }

    /**
     * 
     * @return a list of revisions on this TK Refex Uuid String Member
     */
    @Override
    public List<TkRefexUuidStringRevision> getRevisions() {
        return revisions;
    }

    /**
     * Gets the string associated with this TK Refex Uuid String Member.
     *
     * @return the string associated with this TK Refex Uuid String Member
     */
    public String getString1() {
        return string1;
    }

    /**
     * 
     * @return TK_REFEX_TYPE.CID_STR
     */
    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.CID_STR;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the uuid associated with this TK Refex Uuid String Member.
     *
     * @param uuid1 the uuid associated with this TK Refex Uuid String Member
     */
    public void setUuid1(UUID uuid1) {
        this.uuid1 = uuid1;
    }

    /**
     * Sets the string associated with this TK Refex Uuid String Member.
     *
     * @param string1 the new string associated with this TK Refex Uuid String Member
     */
    public void setString1(String string1) {
        this.string1 = string1;
    }
}
