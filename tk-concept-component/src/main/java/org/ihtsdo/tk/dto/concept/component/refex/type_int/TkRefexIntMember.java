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

// TODO: Auto-generated Javadoc
/**
 * The Class TkRefexIntMember.
 */
public class TkRefexIntMember extends TkRefexAbstractMember<TkRefexIntRevision> {

    /** The Constant serialVersionUID. */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /** The int1. */
    public int int1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new tk refex int member.
     */
    public TkRefexIntMember() {
        super();
    }

    /**
     * Instantiates a new tk refex int member.
     *
     * @param refexChronicle the refex chronicle
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexIntMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexIntVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new tk refex int member.
     *
     * @param refexIntVersion the refex int version
     * @param revisionHandling the revision handling
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
     * Instantiates a new tk refex int member.
     *
     * @param in the in
     * @param dataVersion the data version
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException the class not found exception
     */
    public TkRefexIntMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new tk refex int member.
     *
     * @param another the another
     * @param conversionMap the conversion map
     * @param offset the offset
     * @param mapAll the map all
     */
    public TkRefexIntMember(TkRefexIntMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.int1 = another.int1;
    }

    /**
     * Instantiates a new tk refex int member.
     *
     * @param refexIntVersion the refex int version
     * @param excludedNids the excluded nids
     * @param conversionMap the conversion map
     * @param offset the offset
     * @param mapAll the map all
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException the contradiction exception
     */
    public TkRefexIntMember(RefexIntVersionBI refexIntVersion, NidBitSetBI excludedNids, Map<UUID, UUID> conversionMap,
            long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexIntVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);
        this.int1 = refexIntVersion.getInt1();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>ERefsetIntMember</tt> object, and contains the same values, field by
     * field, as this <tt>ERefsetIntMember</tt>.
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

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkRevision#makeConversion(java.util.Map, long, boolean)
     */
    @Override
    public TkRefexIntMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexIntMember(this, conversionMap, offset, mapAll);
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember#readExternal(java.io.DataInput, int)
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
     * Returns a string representation of the object.
     *
     * @return the string
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

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember#writeExternal(java.io.DataOutput)
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
     * Gets the int1.
     *
     * @return the int1
     */
    public int getInt1() {
        return int1;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkComponent#getRevisionList()
     */
    @Override
    public List<TkRefexIntRevision> getRevisionList() {
        return revisions;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember#getType()
     */
    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.INT;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the int1.
     *
     * @param int1 the new int1
     */
    public void setInt1(int int1) {
        this.int1 = int1;
    }
}
