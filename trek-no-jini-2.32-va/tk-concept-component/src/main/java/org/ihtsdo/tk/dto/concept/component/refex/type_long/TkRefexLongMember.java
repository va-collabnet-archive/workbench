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
package org.ihtsdo.tk.dto.concept.component.refex.type_long;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.dto.RevisionHandling;
import org.ihtsdo.tk.dto.concept.component.refex.type_boolean.TkRefexBooleanRevision;

/**
 * The Class TkRefexLongMember represents a long type refex member in the
 * eConcept format and contains methods specific for interacting with a long
 * type refex member. Further discussion of the eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkRefexLongMember extends TkRefexAbstractMember<TkRefexLongRevision> {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The long value associated with this TK Refex Long Member.
     */
    public long long1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refex Long Member.
     */
    public TkRefexLongMember() {
        super();
    }

    /**
     * Instantiates a new TK Refex Long Member based on the
     * <code>refexChronicle</code>.
     *
     * @param refexChronicle the refex chronicle specifying how to construct
     * this TK Refex Long Member
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexLongMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexLongVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new TK Refex Long Member based on the
     * <code>refexLongVersion</code> and using the given
     * <code>revisionHandling</code>.
     *
     * @param refexLongVersion the refex long version specifying how to
     * construct this TK Refex Long Member
     * @param revisionHandling specifying if addition versions should be
     * included or not
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexLongMember(RefexLongVersionBI refexLongVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(refexLongVersion);
        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            this.long1 = refexLongVersion.getLong1();
        } else {
            Collection<? extends RefexLongVersionBI> refexes = refexLongVersion.getVersions();
            int partCount = refexes.size();
            Iterator<? extends RefexLongVersionBI> itr = refexes.iterator();
            RefexLongVersionBI rv = itr.next();

            this.long1 = rv.getLong1();

            if (partCount > 1) {
                revisions = new ArrayList<TkRefexLongRevision>(partCount - 1);

                while (itr.hasNext()) {
                    rv = itr.next();
                    TkRefexLongRevision rev = new TkRefexLongRevision(rv);
                    if (rev.getTime() == this.time) {
                        // TODO this check can be removed after trek-95 change sets are no longer in production. 
                        this.long1 = rev.long1;
                    } else {
                        revisions.add(rev);
                    }
                }
            }
        }
    }

    /**
     * Instantiates a new TK Refex Long Member based on the specified data
     * input, <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Refex Long Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRefexLongMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refex Long Member based on
     * <code>another</code> TK Refex Long Member and allows for uuid
     * conversion.
     *
     * @param another the TK Refex Long Member specifying how to construct
     * this TK Refex Long Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Long Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK Refex Long Member based on the conversion map
     */
    public TkRefexLongMember(TkRefexLongMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.long1 = another.long1;
    }

    /**
     * Instantiates a new TK Refex Long Member based on a
     * <code>refexIntVersion</code> and allows for uuid conversion. Can
     * exclude components based on their nid.
     *
     * @param refexLongVersion the refex long version specifying how to
     * construct this TK Refex Long Member
     * @param excludedNids the nids in the specified component version to
     * exclude from this TK Refex Long Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Long Member
     * @param mapAll the map all
     * @param viewCoordinate the view coordinate specifying which version of the
     * components to use
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for the
     * given view coordinate
     */
    public TkRefexLongMember(RefexLongVersionBI refexLongVersion, NidBitSetBI excludedNids,
            Map<UUID, UUID> conversionMap, long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexLongVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);
        this.long1 = refexLongVersion.getLong1();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetLongMember</tt> object, and contains the same values, field by
     * field, as this <tt>ERefsetLongMember</tt>.
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

        if (TkRefexLongMember.class.isAssignableFrom(obj.getClass())) {
            TkRefexLongMember another = (TkRefexLongMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
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
     * <code>ERefsetLongMember</code>.
     *
     * @return a hash code value for this <tt>ERefsetLongMember</tt>.
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
     * TK Refex Long Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK Refex Long Member based on the conversion map
     * @return the converted TK Refex Long Member
     */
    @Override
    public TkRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexLongMember(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK Refex Long Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        long1 = in.readLong();

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRefexLongRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefexLongRevision rev = new TkRefexLongRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    long1 = rev.long1;
                } else {
                    revisions.add(rev);
                }
            }
        }
    }

    /**
     * Returns a string representation of this TK Refex Long Member object.
     *
     * @return a string representation of this TK Refex Long Member object
     * including the long value
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
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
        out.writeLong(long1);

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRefexLongRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the long associated with this TK Refex Long Member.
     *
     * @return the long associated with this TK Refex Long Member
     */
    public long getLong1() {
        return long1;
    }

    /**
     * 
     * @return a list of revisions on this TK Refex Long Member
     */
    public List<TkRefexLongRevision> getRevisionList() {
        return revisions;
    }

    /**
     * 
     * @return TK_REFEX_TYPE.LONG
     */
    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.LONG;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the long associated with this TK Refex Long Member.
     *
     * @param long1 the long associated with this TK Refex Long Member
     */
    public void setLong1(long long1) {
        this.long1 = long1;
    }
}
