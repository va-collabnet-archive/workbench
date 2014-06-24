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
package org.ihtsdo.tk.dto.concept.component.refex.type_member;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_member.RefexMemberVersionBI;
import org.ihtsdo.tk.dto.RevisionHandling;

/**
 * The Class TkRefexMember represents a refex member in the eConcept format and
 * contains methods specific for interacting with a refex member. Further
 * discussion of the eConcept format can be found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkRefexMember extends TkRefexAbstractMember<TkRefexRevision> {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refex Member.
     */
    public TkRefexMember() {
        super();
    }

    /**
     * Instantiates a new TK Refex Member based on the specified data
     * input, <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Refex Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRefexMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refex Member based on the
     * <code>refexChronicle</code>.
     *
     * @param refexChronicle the refex chronicle specifying how to construct
     * this TK Refex Member
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexMemberVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new TK Refex Member based on the
     * <code>refexMemberVersion</code> and using the given
     * <code>revisionHandling</code>.
     *
     * @param refexMemberVersion the refex member version specifying how to
     * construct this TK Refex Member
     * @param revisionHandling specifying if addition versions should be
     * included or not
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexMember(RefexMemberVersionBI refexMemberVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(refexMemberVersion);
        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            //
        } else {

            Collection<? extends RefexMemberVersionBI> refexes = refexMemberVersion.getVersions();
            int partCount = refexes.size();
            Iterator<? extends RefexMemberVersionBI> itr = refexes.iterator();
            RefexMemberVersionBI rv = itr.next();

            if (partCount > 1) {
                revisions = new ArrayList<TkRefexRevision>(partCount - 1);

                while (itr.hasNext()) {
                    rv = itr.next();
                    TkRefexRevision rev = new TkRefexRevision(rv);
                    if (rev.getTime() == this.time) {
                        // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    } else {
                        revisions.add(rev);
                    }
                }
            }
        }
    }

    /**
     * Instantiates a new TK Refex Member based on
     * <code>another</code> TK Refex Member and allows for uuid
     * conversion.
     *
     * @param another the TK Refex Member specifying how to construct
     * this TK Refex Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK Refex Member based on the conversion map
     */
    public TkRefexMember(TkRefexMember another, Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
    }

    /**
     * Instantiates a new TK Refex Member based on a
     * <code>refexIntVersion</code> and allows for uuid conversion. Can
     * exclude components based on their nid.
     *
     * @param refexVersion the refex version specifying how to
     * construct this TK Refex Member
     * @param excludedNids the nids in the specified component version to
     * exclude from this TK Refex Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Member
     * @param mapAll the map all
     * @param viewCoordinate the view coordinate specifying which version of the
     * components to use
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for the
     * given view coordinate
     */
    public TkRefexMember(RefexVersionBI refexVersion, NidBitSetBI excludedNids, Map<UUID, UUID> conversionMap,
            long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetMember</tt> object, and contains the same values, field by
     * field, as this <tt>ERefsetMember</tt>.
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

        if (TkRefexMember.class.isAssignableFrom(obj.getClass())) {

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>ERefsetMember</code>.
     *
     * @return a hash code value for this <tt>ERefsetMember</tt>.
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
     * TK Refex Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK Refex Member based on the conversion map
     * @return the converted TK Refex Member
     */
    @Override
    public TkRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexMember(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK Refex Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRefexRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefexRevision rev = new TkRefexRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                } else {
                    revisions.add(rev);
                }
            }
        }
    }

    /**
     * Returns a string representation of this TK Refex Member object.
     *
     * @return a string representation of this TK Refex Member object
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
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

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRefexRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * 
     * @return a list of revisions on this TK Refex Member
     */
    public List<TkRefexRevision> getRevisionList() {
        return revisions;
    }

    /**
     * 
     * @return TK_REFEX_TYPE.MEMBER
     */
    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.MEMBER;
    }
}
