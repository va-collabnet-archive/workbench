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
package org.ihtsdo.tk.dto.concept.component.refex.type_string;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.tk.dto.concept.UtfHelper;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.dto.RevisionHandling;

// TODO: Auto-generated Javadoc
/**
 * The Class TkRefsetStrMember.
 */
public class TkRefsetStrMember extends TkRefexAbstractMember<TkRefsetStrRevision> {

    /** The Constant serialVersionUID. */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /** The string1. */
    public String string1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new tk refset str member.
     */
    public TkRefsetStrMember() {
        super();
    }

    /**
     * Instantiates a new tk refset str member.
     *
     * @param refexChronicle the refex chronicle
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public TkRefsetStrMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexStringVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new tk refset str member.
     *
     * @param refexStringVersion the refex string version
     * @param revisionHandling the revision handling
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public TkRefsetStrMember(RefexStringVersionBI refexStringVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(refexStringVersion);
        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            this.string1 = refexStringVersion.getString1();
        } else {

            Collection<? extends RefexStringVersionBI> refexes = refexStringVersion.getVersions();
            int partCount = refexes.size();
            Iterator<? extends RefexStringVersionBI> itr = refexes.iterator();
            RefexStringVersionBI rv = itr.next();

            this.string1 = rv.getString1();

            if (partCount > 1) {
                revisions = new ArrayList<TkRefsetStrRevision>(partCount - 1);

                while (itr.hasNext()) {
                    rv = itr.next();
                    TkRefsetStrRevision rev = new TkRefsetStrRevision(rv);
                    if (rev.getTime() == this.time) {
                        // TODO this check can be removed after trek-95 change sets are no longer in production. 
                        this.string1 = rev.string1;
                    } else {
                        revisions.add(rev);
                    }
                }
            }
        }
    }

    /**
     * Instantiates a new tk refset str member.
     *
     * @param in the in
     * @param dataVersion the data version
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    public TkRefsetStrMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new tk refset str member.
     *
     * @param another the another
     * @param conversionMap the conversion map
     * @param offset the offset
     * @param mapAll the map all
     */
    public TkRefsetStrMember(TkRefsetStrMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.string1 = another.string1;
    }

    /**
     * Instantiates a new tk refset str member.
     *
     * @param refexStringVersion the refex string version
     * @param excludedNids the excluded nids
     * @param conversionMap the conversion map
     * @param offset the offset
     * @param mapAll the map all
     * @param viewCoordinate the view coordinate
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    public TkRefsetStrMember(RefexStringVersionBI refexStringVersion, NidBitSetBI excludedNids, Map<UUID, UUID> conversionMap,
            long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexStringVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);
        this.string1 = refexStringVersion.getString1();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>ERefsetStrMember</tt> object, and contains the same values, field by
     * field, as this <tt>ERefsetStrMember</tt>.
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

        if (TkRefsetStrMember.class.isAssignableFrom(obj.getClass())) {
            TkRefsetStrMember another = (TkRefsetStrMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
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
     * <code>ERefsetStrMember</code>.
     *
     * @return a hash code value for this <tt>ERefsetStrMember</tt>.
     */
    @Override
    public int hashCode() {
        return this.primordialUuid.hashCode();
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkRevision#makeConversion(java.util.Map, long, boolean)
     */
    @Override
    public TkRefsetStrMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefsetStrMember(this, conversionMap, offset, mapAll);
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember#readExternal(java.io.DataInput, int)
     */
    @Override
    public final void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        string1 = UtfHelper.readUtfV6(in, dataVersion);

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRefsetStrRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefsetStrRevision rev = new TkRefsetStrRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    string1 = rev.string1;
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
        buff.append(" str:");
        buff.append("'").append(this.string1).append("'");
        buff.append("; ");
        buff.append(super.toString());

        return buff.toString();
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember#writeExternal(java.io.DataOutput)
     */
    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        UtfHelper.writeUtf(out, string1);

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRefsetStrRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkComponent#getRevisionList()
     */
    @Override
    public List<TkRefsetStrRevision> getRevisionList() {
        return revisions;
    }

    /**
     * Gets the string1.
     *
     * @return the string1
     */
    public String getString1() {
        return string1;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember#getType()
     */
    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.STR;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the string1.
     *
     * @param string1 the new string1
     */
    public void setString1(String string1) {
        this.string1 = string1;
    }
}
