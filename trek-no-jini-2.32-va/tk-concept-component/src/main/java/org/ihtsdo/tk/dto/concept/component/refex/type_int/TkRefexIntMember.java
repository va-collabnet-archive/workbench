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
package org.ihtsdo.tk.dto.concept.component.refex.type_int;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.dto.RevisionHandling;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_string.TkRefexUuidStringRevision;

/**
 * The Class TkRefexIntMember represents a int type refex member in the eConcept
 * format and contains methods specific for interacting with a int type
 * refex member. Further discussion of the eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkRefexIntMember extends TkRefexAbstractMember<TkRefexIntRevision> {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The int value associated with this TK Refex Int Member.
     */
    public int int1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refex Int Member.
     */
    public TkRefexIntMember() {
        super();
    }

    /**
     * Instantiates a new TK Refex Int Member based on the
     * <code>refexChronicle</code>.
     *
     * @param refexChronicle the refex chronicle specifying how to construct
     * this TK Refex Int Member
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexIntMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexIntVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new TK Refex Int Member based on the
     * <code>refexIntVersion</code> and using the given
     * <code>revisionHandling</code>.
     *
     * @param refexIntVersion the refex int version specifying how to
     * construct this TK Refex Int Member
     * @param revisionHandling specifying if addition versions should be
     * included or not
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexIntMember(RefexIntVersionBI refexIntVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(refexIntVersion);
        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            this.int1 = refexIntVersion.getInt1();
        } else {
            Collection<? extends RefexIntVersionBI> refexes = refexIntVersion.getVersions();
            int partCount = refexes.size();
            Iterator<? extends RefexIntVersionBI> itr = refexes.iterator();
            RefexIntVersionBI rv = itr.next();

            this.int1 = rv.getInt1();

            if (partCount > 1) {
                revisions = new ArrayList<TkRefexIntRevision>(partCount - 1);

                while (itr.hasNext()) {
                    rv = itr.next();
                    TkRefexIntRevision rev = new TkRefexIntRevision(rv);
                    if (rev.getTime() == this.time) {
                        // TODO this check can be removed after trek-95 change sets are no longer in production. 
                        this.int1 = rev.int1;
                    } else {
                        revisions.add(rev);
                    }
                }
            }
        }
    }

    /**
     * Instantiates a new TK Refex Int Member based on the specified data
     * input, <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Refex Boolean Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRefexIntMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refex Int Member based on
     * <code>another</code> TK Refex Int Member and allows for uuid
     * conversion.
     *
     * @param another the TK Refex Int Member specifying how to construct
     * this TK Refex Int Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Int Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK Refex Int Member based on the conversion map
     */
    public TkRefexIntMember(TkRefexIntMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.int1 = another.int1;
    }

    /**
     * Instantiates a new TK Refex Int Member based on a
     * <code>refexIntVersion</code> and allows for uuid conversion. Can
     * exclude components based on their nid.
     *
     * @param refexIntVersion the refex int version specifying how to
     * construct this TK Refex Int Member
     * @param excludedNids the nids in the specified component version to
     * exclude from this TK Refex Int Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Int Member
     * @param mapAll the map all
     * @param viewCoordinate the view coordinate specifying which version of the
     * components to use
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for the
     * given view coordinate
     */
    public TkRefexIntMember(RefexIntVersionBI refexIntVersion, NidBitSetBI excludedNids, Map<UUID, UUID> conversionMap,
            long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexIntVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);
        this.int1 = refexIntVersion.getInt1();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetIntMember</tt> object, and contains the same values, field by
     * field, as this <tt>ERefsetIntMember</tt>.
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

        if (TkRefexIntMember.class.isAssignableFrom(obj.getClass())) {
            TkRefexIntMember another = (TkRefexIntMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare int1
            if (this.int1 != another.int1) {
                return false;
            }

            // Compare extraVersions
            if (this.revisions == null) {
                if (another.revisions == null) {             // Equal!
                } else if (another.revisions.isEmpty()) {    // Equal!
                } else {
                    return false;
                }
            } else if (!this.revisions.equals(another.revisions)) {
                return false;
            }

            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>ERefsetIntMember</code>.
     *
     * @return a hash code value for this <tt>ERefsetIntMember</tt>.
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
     * TK Refex Int Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK Refex Int Member based on the conversion map
     * @return the converted TK Refex Int Member
     */
    @Override
    public TkRefexIntMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexIntMember(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK Refex Int Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        int1 = in.readInt();

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRefexIntRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefexIntRevision rev = new TkRefexIntRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    int1 = rev.int1;
                } else {
                    revisions.add(rev);
                }
            }
        }
    }

    /**
     * Returns a string representation of this TK Refex Int Member object.
     *
     * @return a string representation of this TK Refex Int Member object
     * including the int value
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" int: ");
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
        out.writeInt(int1);

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRefexIntRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the int associated with this TK Refex Int Member.
     *
     * @return the int associated with this TK Refex Int Member
     */
    public int getInt1() {
        return int1;
    }

    /**
     * 
     * @return a list of revisions on this TK Refex Int Member
     */
    @Override
    public List<TkRefexIntRevision> getRevisionList() {
        return revisions;
    }

    /**
     * 
     * @return TK_REFEX_TYPE.BOOLEAN
     */
    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.INT;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the int associated with this TK Refex Int Member.
     *
     * @param int1 the int associated with this TK Refex Int Member.
     */
    public void setInt1(int int1) {
        this.int1 = int1;
    }
}
