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

/**
 * The Class TkRefsetStrMember represents a string type refex member in the
 * eConcept format and contains methods specific for interacting with a refex
 * member. Further discussion of the eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkRefsetStrMember extends TkRefexAbstractMember<TkRefsetStrRevision> {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The string value associated with this TK Refset String Member.
     */
    public String string1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refset String Member.
     */
    public TkRefsetStrMember() {
        super();
    }

    /**
     * Instantiates a new TK Refset String Member based on the
     * <code>refexChronicle</code>.
     *
     * @param refexChronicle the refex chronicle specifying how to construct
     * this TK Refset String Member
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefsetStrMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexStringVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new TK Refset String Member based on the
     * <code>refexStringVersion</code> and using the given
     * <code>revisionHandling</code>.
     *
     * @param refexStringVersion the refex string version specifying how to
     * construct this TK Refset String Member
     * @param revisionHandling specifying if addition versions should be
     * included or not
     * @throws IOException signals that an I/O exception has occurred
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
     * Instantiates a new TK Refset String Member based on the specified data
     * input, <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Refset String Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRefsetStrMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refset String Member based on
     * <code>another</code> TK Refset String Member and allows for uuid
     * conversion.
     *
     * @param another the TK Refset String Member specifying how to construct
     * this TK Refset String Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refset String Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK Refset String Member based on the conversion map
     */
    public TkRefsetStrMember(TkRefsetStrMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.string1 = another.string1;
    }

    /**
     * Instantiates a new TK Refset String Member based on a
     * <code>refexIntVersion</code> and allows for uuid conversion. Can
     * exclude components based on their nid.
     *
     * @param refexStringVersion the refex string version specifying how to
     * construct this TK Refset String Member
     * @param excludedNids the nids in the specified component version to
     * exclude from this TK Refset String Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refset String Member
     * @param mapAll the map all
     * @param viewCoordinate the view coordinate specifying which version of the
     * components to use
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for the
     * given view coordinate
     */
    public TkRefsetStrMember(RefexStringVersionBI refexStringVersion, NidBitSetBI excludedNids, Map<UUID, UUID> conversionMap,
            long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexStringVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);
        this.string1 = refexStringVersion.getString1();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetStrMember</tt> object, and contains the same values, field by
     * field, as this <tt>ERefsetStrMember</tt>.
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

    /**
     *
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refset String Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK Refset String Member based on the conversion map
     * @return the converted TK Refset String Member
     */
    @Override
    public TkRefsetStrMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefsetStrMember(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK Refset String Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
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
     * Returns a string representation of this TK Refset String Member object.
     *
     * @return a string representation of this TK Refset String Member object
     * including the string value
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

    /**
     *
     * @param out the data output object that writes to the external source
     * @throws IOException signals that an I/O exception has occurred
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
    /**
     * 
     * @return a list of revisions on this TK Refset String Member
     */
    @Override
    public List<TkRefsetStrRevision> getRevisionList() {
        return revisions;
    }

    /**
     * Gets the string associated with this TK Refset String Member.
     *
     * @return the string associated with this TK Refset String Member
     */
    public String getString1() {
        return string1;
    }

    /**
     * 
     * @return TK_REFEX_TYPE.STR
     */
    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.STR;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the string associated with this TK Refset String Member.
     *
     * @param string1 associated with this TK Refset String Member
     */
    public void setString1(String string1) {
        this.string1 = string1;
    }
}
