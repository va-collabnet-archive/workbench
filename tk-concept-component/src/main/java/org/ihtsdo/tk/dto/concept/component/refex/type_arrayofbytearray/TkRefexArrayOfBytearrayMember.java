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
package org.ihtsdo.tk.dto.concept.component.refex.type_arrayofbytearray;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayVersionBI;
import org.ihtsdo.tk.dto.RevisionHandling;
import org.ihtsdo.tk.dto.concept.component.refex.type_long.TkRefexLongRevision;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

// TODO: Auto-generated Javadoc
/**
 * The Class TkRefexArrayOfBytearrayMember.
 *
 * @author kec
 */
public class TkRefexArrayOfBytearrayMember extends TkRefexAbstractMember<TkRefexArrayOfByteArrayRevision> {

    /** The Constant serialVersionUID. */
    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    /** The array of byte array1. */
    public byte[][] arrayOfByteArray1;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new tk refex array of bytearray member.
     */
    public TkRefexArrayOfBytearrayMember() {
        super();
    }

    /**
     * Instantiates a new tk refex array of bytearray member.
     *
     * @param refexChronicle the refex chronicle
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public TkRefexArrayOfBytearrayMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexArrayOfBytearrayVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    /**
     * Instantiates a new tk refex array of bytearray member.
     *
     * @param refexArrayOfBytearrayVersion the refex array of bytearray version
     * @param revisionHandling the revision handling
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public TkRefexArrayOfBytearrayMember(RefexArrayOfBytearrayVersionBI refexArrayOfBytearrayVersion,
            RevisionHandling revisionHandling) throws IOException {
        super(refexArrayOfBytearrayVersion);
        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            this.arrayOfByteArray1 = refexArrayOfBytearrayVersion.getArrayOfByteArray();
        } else {
            Collection<? extends RefexArrayOfBytearrayVersionBI> refexes = refexArrayOfBytearrayVersion.getVersions();
            int partCount = refexes.size();
            Iterator<? extends RefexArrayOfBytearrayVersionBI> itr = refexes.iterator();
            RefexArrayOfBytearrayVersionBI rv = itr.next();

            this.arrayOfByteArray1 = rv.getArrayOfByteArray();

            if (partCount > 1) {
                revisions = new ArrayList<TkRefexArrayOfByteArrayRevision>(partCount - 1);

                while (itr.hasNext()) {
                    rv = itr.next();
                    TkRefexArrayOfByteArrayRevision rev = new TkRefexArrayOfByteArrayRevision(rv);
                    if (rev.getTime() == this.time) {
                        // TODO this check can be removed after trek-95 change sets are no longer in production. 
                        this.arrayOfByteArray1 = rev.arrayOfByteArray1;
                    } else {
                        revisions.add(rev);
                    }
                }
            }
        }
    }
    

    /**
     * Instantiates a new tk refex array of bytearray member.
     *
     * @param in the in
     * @param dataVersion the data version
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    public TkRefexArrayOfBytearrayMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    /**
     * Instantiates a new tk refex array of bytearray member.
     *
     * @param another the another
     * @param conversionMap the conversion map
     * @param offset the offset
     * @param mapAll the map all
     */
    public TkRefexArrayOfBytearrayMember(TkRefexArrayOfBytearrayMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.arrayOfByteArray1 = another.arrayOfByteArray1;
    }

    /**
     * Instantiates a new tk refex array of bytearray member.
     *
     * @param refexArrayOfBytearrayVersion the refex array of bytearray version
     * @param excludedNids the excluded nids
     * @param conversionMap the conversion map
     * @param offset the offset
     * @param mapAll the map all
     * @param viewCoordinate the view coordinate
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ContradictionException the contradiction exception
     */
    public TkRefexArrayOfBytearrayMember(RefexArrayOfBytearrayVersionBI refexArrayOfBytearrayVersion, NidBitSetBI excludedNids,
            Map<UUID, UUID> conversionMap, long offset, boolean mapAll, ViewCoordinate viewCoordinate)
            throws IOException, ContradictionException {
        super(refexArrayOfBytearrayVersion, excludedNids, conversionMap, offset, mapAll, viewCoordinate);
        this.arrayOfByteArray1 = refexArrayOfBytearrayVersion.getArrayOfByteArray();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>ERefsetLongMember</tt> object, and contains the same values, field by
     * field, as this <tt>ERefsetLongMember</tt>.
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

        if (TkRefexArrayOfBytearrayMember.class.isAssignableFrom(obj.getClass())) {
            TkRefexArrayOfBytearrayMember another = (TkRefexArrayOfBytearrayMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare byteArray
            if (!Arrays.deepEquals(this.arrayOfByteArray1, another.arrayOfByteArray1)) {
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

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkRevision#makeConversion(java.util.Map, long, boolean)
     */
    @Override
    public TkRefexArrayOfBytearrayMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexArrayOfBytearrayMember(this, conversionMap, offset, mapAll);
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember#readExternal(java.io.DataInput, int)
     */
    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        int arrayLength = in.readShort();
        this.arrayOfByteArray1 = new byte[arrayLength][];
        for (int i = 0; i < arrayLength; i++) {
            int byteArrayLength = in.readInt();
            this.arrayOfByteArray1[i] = new byte[byteArrayLength];
            in.readFully(this.arrayOfByteArray1[i]);
        }

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRefexArrayOfByteArrayRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefexArrayOfByteArrayRevision rev = new TkRefexArrayOfByteArrayRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    arrayOfByteArray1 = rev.arrayOfByteArray1;
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
        buff.append(" size: ");
        buff.append(this.arrayOfByteArray1.length);
        for (int i = 0; i < this.arrayOfByteArray1.length; i++) {
            buff.append(" ").append(i);
            buff.append(": ");
            buff.append(this.arrayOfByteArray1[i]);
        }
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
        out.writeShort(arrayOfByteArray1.length);
        for (byte[] bytes : arrayOfByteArray1) {
            out.writeInt(bytes.length);
            out.write(bytes);
        }
        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRefexArrayOfByteArrayRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.TkComponent#getRevisionList()
     */
    @Override
    public List<TkRefexArrayOfByteArrayRevision> getRevisionList() {
        return revisions;
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember#getType()
     */
    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.ARRAY_BYTEARRAY;
    }

    /**
     * Gets the array of byte array1.
     *
     * @return the array of byte array1
     */
    public byte[][] getArrayOfByteArray1() {
        return arrayOfByteArray1;
    }

    /**
     * Sets the array of byte array1.
     *
     * @param byteArray1 the new array of byte array1
     */
    public void setArrayOfByteArray1(byte[][] byteArray1) {
        this.arrayOfByteArray1 = byteArray1;
    }
}
