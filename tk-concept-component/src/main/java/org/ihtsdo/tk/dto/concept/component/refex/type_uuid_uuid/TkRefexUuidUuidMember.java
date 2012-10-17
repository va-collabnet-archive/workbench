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
package org.ihtsdo.tk.dto.concept.component.refex.type_uuid_uuid;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_nid_nid.RefexNidNidVersionBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

//~--- JDK imports ------------------------------------------------------------

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.*;
import org.ihtsdo.tk.dto.RevisionHandling;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidRevision;

/**
 * The Class TkRefexUuidUuidMember represents a uuid-uuid type refex member in
 * the eConcept format and contains methods specific for interacting with a
 * uuid-uuid type refex member. Further discussion of the eConcept format can be
 * found on
 * <code>TkConcept</code>.
 *
 * @see TkConcept
 */
public class TkRefexUuidUuidMember extends TkRefexAbstractMember<TkRefsetUuidUuidRevision> {

    /**
     * The Constant serialVersionUID, used to prevent the class from computing
     * its own serialVersionUID based on a hash of all the method signatures.
     */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /**
     * The first uuid associated with this TK Refex Uuid Uuid Member.
     */
    public UUID uuid1;
    /**
     * The second uuid associated with this TK Refex Uuid Uuid Member.
     */
    public UUID uuid2;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new TK Refex Uuid Uuid Member.
     */
    public TkRefexUuidUuidMember() {
        super();
    }

    /**
     * Instantiates a new TK Refex Uuid Uuid Member based on the
     * <code>refexChronicle</code>.
     *
     * @param refexChronicle the refex chronicle specifying how to construct
     * this TK Refex Uuid Uuid Member
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexUuidUuidMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexNidNidVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new TK Refex Uuid Uuid Member based on the
     * <code>refexNidNidVersion</code> and using the given
     * <code>revisionHandling</code>.
     *
     * @param refexNidNidVersion the refex nid nid version specifying how to
     * construct this TK Refex Uuid Uuid Member
     * @param revisionHandling specifying if addition versions should be
     * included or not
     * @throws IOException signals that an I/O exception has occurred
     */
    public TkRefexUuidUuidMember(RefexNidNidVersionBI refexNidNidVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(refexNidNidVersion);
        TerminologyStoreDI ts = Ts.get();
        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            this.uuid1 = ts.getUuidPrimordialForNid(refexNidNidVersion.getNid1());
            this.uuid2 = ts.getUuidPrimordialForNid(refexNidNidVersion.getNid2());
        } else {
            Collection<? extends RefexNidNidVersionBI> rels = refexNidNidVersion.getVersions();
            int partCount = rels.size();
            Iterator<? extends RefexNidNidVersionBI> relItr = rels.iterator();
            RefexNidNidVersionBI rv = relItr.next();

            this.uuid1 = ts.getUuidPrimordialForNid(rv.getNid1());
            this.uuid2 = ts.getUuidPrimordialForNid(rv.getNid2());

            if (partCount > 1) {
                revisions = new ArrayList<TkRefsetUuidUuidRevision>(partCount - 1);

                while (relItr.hasNext()) {
                    rv = relItr.next();
                    TkRefsetUuidUuidRevision rev = new TkRefsetUuidUuidRevision(rv);
                    if (rev.getTime() == this.time) {
                        // TODO this check can be removed after trek-95 change sets are no longer in production. 
                        this.uuid1 = rev.uuid1;
                        this.uuid2 = rev.uuid2;
                    } else {
                        revisions.add(rev);
                    }
                }
            }
        }
    }

    /**
     * Instantiates a new TK Refex Uuid Uuid Member based on the specified data
     * input, <code>in</code>.
     *
     * @param in the data input specifying how to construct this TK Refex Uuid
     * Uuid Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    public TkRefexUuidUuidMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new TK Refex Uuid Uuid Member based on
     * <code>another</code> TK Refex Uuid Uuid Member and allows for uuid
     * conversion.
     *
     * @param another the TK Refex Uuid Uuid Member specifying how to construct
     * this TK Refex Uuid Uuid Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Uuid Uuid Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid Uuid Member based on the conversion map
     */
    public TkRefexUuidUuidMember(TkRefexUuidUuidMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);

        if (mapAll) {
            this.uuid1 = conversionMap.get(another.uuid1);
            this.uuid2 = conversionMap.get(another.uuid2);
        } else {
            this.uuid1 = another.uuid1;
            this.uuid2 = another.uuid2;
        }
    }

    /**
     * Instantiates a new TK Refex Uuid Uuid Member based on a
     * <code>refexNidNidVersion</code> and allows for uuid conversion. Can
     * exclude components based on their nid.
     *
     * @param refexNidNidVersion the refex nid nid version specifying how to
     * construct this TK Refex Uuid Uuid Member
     * @param excludedNids the nids in the specified component version to
     * exclude from this TK Refex Uuid Uuid Member
     * @param conversionMap the map for converting from one set of uuids to
     * another
     * @param offset the offset to be applied to the time associated with this
     * TK Refex Uuid Uuid Member
     * @param mapAll the map all
     * @param viewCoordinate the view coordinate specifying which version of the
     * components to use
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for the
     * given view coordinate
     */
    public TkRefexUuidUuidMember(RefexNidNidVersionBI refexNidNidVersion, NidBitSetBI excludedNids,
            Map<UUID, UUID> conversionMap, long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexNidNidVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);

        if (mapAll) {
            this.uuid1 = conversionMap.get(Ts.get().getComponent(refexNidNidVersion.getNid1()).getPrimUuid());
            this.uuid2 = conversionMap.get(Ts.get().getComponent(refexNidNidVersion.getNid2()).getPrimUuid());
        } else {
            this.uuid1 = Ts.get().getComponent(refexNidNidVersion.getNid1()).getPrimUuid();
            this.uuid2 = Ts.get().getComponent(refexNidNidVersion.getNid2()).getPrimUuid();
        }
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt>
     * if and only if the argument is not <tt>null</tt>, is a
     * <tt>ERefsetCidCidMember</tt> object, and contains the same values, field
     * by field, as this <tt>ERefsetCidCidMember</tt>.
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

        if (TkRefexUuidUuidMember.class.isAssignableFrom(obj.getClass())) {
            TkRefexUuidUuidMember another = (TkRefexUuidUuidMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare uuid1
            if (!this.uuid1.equals(another.uuid1)) {
                return false;
            }

            // Compare uuid2
            if (!this.uuid2.equals(another.uuid2)) {
                return false;
            }

            // Compare their parents
            return super.equals(obj);
        }

        return false;
    }

    /**
     * Returns a hash code for this
     * <code>ERefsetCidCidMember</code>.
     *
     * @return a hash code value for this <tt>ERefsetCidCidMember</tt>.
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
     * TK Refex Uuid Uuid Member
     * @param mapAll set to <code>true</code> to map all the uuids in this TK
     * Refex Uuid Uuid Member based on the conversion map
     * @return the converted TK Refex Uuid Uuid Member
     */
    @Override
    public TkRefexUuidUuidMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexUuidUuidMember(this, conversionMap, offset, mapAll);
    }

    /**
     *
     * @param in the data input specifying how to construct this TK
     * Refex Uuid Uuid Member
     * @param dataVersion the data version of the external source
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        uuid1 = new UUID(in.readLong(), in.readLong());
        uuid2 = new UUID(in.readLong(), in.readLong());

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRefsetUuidUuidRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefsetUuidUuidRevision rev = new TkRefsetUuidUuidRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    uuid1 = rev.uuid1;
                    uuid2 = rev.uuid2;
                } else {
                    revisions.add(rev);
                }
            }
        }
    }

    /**
     * Returns a string representation of this TK Refex Uuid Uuid Member object.
     *
     * @return a string representation of this TK Refex Uuid Uuid Member object
     * including the concepts represented by the uuids.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" c1:");
        buff.append(informAboutUuid(this.uuid1));
        buff.append(" c2:");
        buff.append(informAboutUuid(this.uuid2));
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
        out.writeLong(uuid2.getMostSignificantBits());
        out.writeLong(uuid2.getLeastSignificantBits());

        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRefsetUuidUuidRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the first uuid associated with this TK Refex Uuid Uuid Member.
     *
     * @return the first uuid associated with this TK Refex Uuid Uuid Member
     */
    public UUID getUuid1() {
        return uuid1;
    }

    /**
     * Gets the second uuid associated with this TK Refex Uuid Uuid Member.
     *
     * @return the second uuid associated with this TK Refex Uuid Uuid Member
     */
    public UUID getUuid2() {
        return uuid2;
    }

    /**
     * 
     * @return a list of revisions on this TK Refex Uuid Uuid Member
     */
    @Override
    public List<TkRefsetUuidUuidRevision> getRevisionList() {
        return revisions;
    }

    /**
     * 
     * @return TK_REFEX_TYPE.CID_CID
     */
    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.CID_CID;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the first uuid associated with this TK Refex Uuid Uuid Member.
     *
     * @param uuid1 the first uuid associated with this TK Refex Uuid Uuid Member
     */
    public void setUuid1(UUID uuid1) {
        this.uuid1 = uuid1;
    }

    /**
     * Sets the second uuid associated with this TK Refex Uuid Uuid Member.
     *
     * @param uuid2 the second uuid associated with this TK Refex Uuid Uuid Member
     */
    public void setUuid2(UUID uuid2) {
        this.uuid2 = uuid2;
    }
}
