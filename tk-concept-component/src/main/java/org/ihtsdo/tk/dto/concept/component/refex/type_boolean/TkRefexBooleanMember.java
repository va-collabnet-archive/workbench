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
package org.ihtsdo.tk.dto.concept.component.refex.type_boolean;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.dto.RevisionHandling;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrRevision;

// TODO: Auto-generated Javadoc
/**
 * The Class TkRefexBooleanMember.
 */
public class TkRefexBooleanMember extends TkRefexAbstractMember<TkRefexBooleanRevision> {

    /** The Constant serialVersionUID. */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /** The boolean1. */
    public boolean boolean1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new tk refex boolean member.
     */
    public TkRefexBooleanMember() {
        super();
    }

    /**
     * Instantiates a new tk refex boolean member.
     *
     * @param refexBooleanVersion the refex boolean version
     * @param revisionHandling the revision handling
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public TkRefexBooleanMember(RefexBooleanVersionBI refexBooleanVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(refexBooleanVersion);

        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            this.boolean1 = refexBooleanVersion.getBoolean1();
        } else {
            Collection<? extends RefexBooleanVersionBI> refexes = refexBooleanVersion.getVersions();
            int partCount = refexes.size();
            Iterator<? extends RefexBooleanVersionBI> itr = refexes.iterator();
            RefexBooleanVersionBI rv = itr.next();

            this.boolean1 = rv.getBoolean1();

            if (partCount > 1) {
                revisions = new ArrayList<TkRefexBooleanRevision>(partCount - 1);

                while (itr.hasNext()) {
                    rv = itr.next();
                    TkRefexBooleanRevision rev = new TkRefexBooleanRevision(rv);
                    if (rev.getTime() == this.time) {
                        // TODO this check can be removed after trek-95 change sets are no longer in production. 
                        this.boolean1 = rev.boolean1;
                    } else {
                        revisions.add(rev);
                    }
                }
            }
        }
    }

    /**
     * Instantiates a new tk refex boolean member.
     *
     * @param refexChronicle the refex chronicle
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public TkRefexBooleanMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexBooleanVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new tk refex boolean member.
     *
     * @param in the in
     * @param dataVersion the data version
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    public TkRefexBooleanMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new tk refex boolean member.
     *
     * @param another the another
     * @param conversionMap the conversion map
     * @param offset the offset
     * @param mapAll the map all
     */
    public TkRefexBooleanMember(TkRefexBooleanMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.boolean1 = another.boolean1;
    }

    /**
     * Instantiates a new tk refex boolean member.
     *
     * @param refexBooleanVersion the refex boolean version
     * @param excludedNids the excluded nids
     * @param conversionMap the conversion map
     * @param offset the offset
     * @param mapAll the map all
     * @param viewCoordinate the view coordinate
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    public TkRefexBooleanMember(RefexBooleanVersionBI refexBooleanVersion, NidBitSetBI excludedNids,
            Map<UUID, UUID> conversionMap, long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexBooleanVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);
        this.boolean1 = refexBooleanVersion.getBoolean1();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>ERefsetBooleanMember</tt> object, and contains the same values, field by
     * field, as this <tt>ERefsetBooleanMember</tt>.
     *
     * @param obj the object to compare with.
     * @return true, if successful
     * <code>true</code> if the objects are the same;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (TkRefexBooleanMember.class.isAssignableFrom(obj.getClass())) {
            TkRefexBooleanMember another = (TkRefexBooleanMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare boolean1
            if (this.boolean1 != another.boolean1) {
                return false;
            }

            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>ERefsetBooleanMember</code>.
     *
     * @return a hash code value for this <tt>ERefsetBooleanMember</tt>.
     */
    @Override
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkRevision#makeConversion(java.util.Map, long, boolean)
     */
    @Override
    public TkRefexBooleanMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexBooleanMember(this, conversionMap, offset, mapAll);
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember#readExternal(java.io.DataInput, int)
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        boolean1 = in.readBoolean();

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRefexBooleanRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefexBooleanRevision rev = new TkRefexBooleanRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    boolean1 = rev.boolean1;
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
        buff.append(this.boolean1);
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
        out.writeBoolean(boolean1);

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRefexBooleanRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the boolean1.
     *
     * @return the boolean1
     */
    public boolean getBoolean1() {
        return boolean1;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkComponent#getRevisionList()
     */
    public List<TkRefexBooleanRevision> getRevisionList() {
        return revisions;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember#getType()
     */
    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.BOOLEAN;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the boolean1.
     *
     * @param boolean1 the new boolean1
     */
    public void setBoolean1(boolean boolean1) {
        this.boolean1 = boolean1;
    }
}
