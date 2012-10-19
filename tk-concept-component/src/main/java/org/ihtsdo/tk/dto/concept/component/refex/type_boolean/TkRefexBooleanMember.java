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

/**
 * The Class TkRefexBooleanMember represents a boolean type refex member in the
 * eConcept format and contains methods specific for interacting with a boolean
 * type refex member. Further discussion of the eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkRefexBooleanMember extends TkRefexAbstractMember<TkRefexBooleanRevision> {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The boolean value associated with this TK Refex Boolean Member.
     */
    public boolean boolean1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refex Boolean Member.
     */
    public TkRefexBooleanMember() {
        super();
    }

    /**
     * Instantiates a new TK Refex Boolean Member based on the
     * <code>refexBooleanVersion</code> and using the given
     * <code>revisionHandling</code>.
     *
     * @param refexBooleanVersion the refex boolean version specifying how to
     * construct this TK Refex Boolean Member
     * @param revisionHandling specifying if addition versions should be
     * included or not
     * @throws IOException signals that an I/O exception has occurred
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
     * Instantiates a new TK Refex Boolean Member based on the
     * <code>refexChronicle</code>.
     *
     * @param refexChronicle the refex chronicle specifying how to construct
     * this TK Refex Boolean Member
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexBooleanMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexBooleanVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new TK Refex Boolean Member based on the specified data
     * input, <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Refex BooleanMember
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRefexBooleanMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refex Boolean Member based on
     * <code>another</code> TK Refex Boolean Member and allows for uuid
     * conversion.
     *
     * @param another the TK Refex Boolean Member specifying how to construct
     * this TK Refex Boolean Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Boolean Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Boolean Member based on the conversion map
     */
    public TkRefexBooleanMember(TkRefexBooleanMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.boolean1 = another.boolean1;
    }

    /**
     * Instantiates a new TK Refex Boolean Member based on a
     * <code>refexBooleanVersion</code> and allows for uuid conversion. Can
     * exclude components based on their nid.
     *
     * @param refexBooleanVersion the refex boolean version specifying how to
     * construct this TK Refex Boolean Member
     * @param excludedNids the nids in the specified component version to
     * exclude from this TK Refex Boolean Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Boolean Member
     * @param mapAll the map all
     * @param viewCoordinate the view coordinate specifying which version of the
     * components to use
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for the
     * given view coordinate
     */
    public TkRefexBooleanMember(RefexBooleanVersionBI refexBooleanVersion, NidBitSetBI excludedNids,
            Map<UUID, UUID> conversionMap, long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexBooleanVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);
        this.boolean1 = refexBooleanVersion.getBoolean1();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetBooleanMember</tt> object, and contains the same values, field
     * by field, as this <tt>ERefsetBooleanMember</tt>.
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

    /**
     *
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Boolean Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Boolean Member based on the conversion map
     * @return the converted TK Refex Boolean Member
     */
    @Override
    public TkRefexBooleanMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexBooleanMember(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK
     * Refex Boolean Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
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
     * Returns a string representation of this TK Refex Boolean Member object.
     *
     * @return a string representation of this TK Refex Boolean Member object
     * including the boolean value.
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

    /**
     *
     * @param out the data output object that writes to the external source
     * @throws IOException signals that an I/O exception has occurred
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
     * Gets the boolean associated with this TK Refex Boolean Member.
     *
     * @return the boolean associated with this TK Refex Boolean Member
     */
    public boolean getBoolean1() {
        return boolean1;
    }

    /**
     * 
     * @return a list of revisions on this TK Refex Boolean Member
     */
    public List<TkRefexBooleanRevision> getRevisionList() {
        return revisions;
    }

    /**
     * 
     * @return TK_REFEX_TYPE.BOOLEAN
     */
    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.BOOLEAN;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the boolean associated with this TK Refex Boolean Member.
     *
     * @param boolean1 the new boolean associated with this TK Refex Boolean Member
     */
    public void setBoolean1(boolean boolean1) {
        this.boolean1 = boolean1;
    }
}
