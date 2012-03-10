/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.dto.concept.component.refset.array.bytearray;

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
import org.ihtsdo.tk.dto.concept.component.refset.Long.TkRefsetLongRevision;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;

/**
 *
 * @author kec
 */
public class TkRefsetArrayOfBytearrayMember extends TkRefsetAbstractMember<TkRefsetArrayByteArrayRevision> {

    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    public byte[][] arrayOfByteArray;

    //~--- constructors --------------------------------------------------------
    public TkRefsetArrayOfBytearrayMember() {
        super();
    }

    public TkRefsetArrayOfBytearrayMember(RefexChronicleBI another) throws IOException {
        this((RefexArrayOfBytearrayVersionBI) another.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

    public TkRefsetArrayOfBytearrayMember(RefexArrayOfBytearrayVersionBI another,
            RevisionHandling revisionHandling) throws IOException {
        super(another);
        if (revisionHandling == RevisionHandling.EXCLUDE_REVISIONS) {
            this.arrayOfByteArray = another.getArrayOfByteArray();
        } else {
            Collection<? extends RefexArrayOfBytearrayVersionBI> refexes = another.getVersions();
            int partCount = refexes.size();
            Iterator<? extends RefexArrayOfBytearrayVersionBI> itr = refexes.iterator();
            RefexArrayOfBytearrayVersionBI rv = itr.next();

            this.arrayOfByteArray = rv.getArrayOfByteArray();

            if (partCount > 1) {
                revisions = new ArrayList<TkRefsetArrayByteArrayRevision>(partCount - 1);

                while (itr.hasNext()) {
                    rv = itr.next();
                    TkRefsetArrayByteArrayRevision rev = new TkRefsetArrayByteArrayRevision(rv);
                    if (rev.getTime() == this.time) {
                        // TODO this check can be removed after trek-95 change sets are no longer in production. 
                        this.arrayOfByteArray = rev.arrayOfByteArray;
                    } else {
                        revisions.add(rev);
                    }
                }
            }
        }
    }
    

    public TkRefsetArrayOfBytearrayMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRefsetArrayOfBytearrayMember(TkRefsetArrayOfBytearrayMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.arrayOfByteArray = another.arrayOfByteArray;
    }

    public TkRefsetArrayOfBytearrayMember(RefexArrayOfBytearrayVersionBI another, NidBitSetBI exclusions,
            Map<UUID, UUID> conversionMap, long offset, boolean mapAll, ViewCoordinate vc)
            throws IOException, ContradictionException {
        super(another, exclusions, conversionMap, offset, mapAll, vc);
        this.arrayOfByteArray = another.getArrayOfByteArray();
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Compares this object to the specified object. The result is <tt>true</tt> if and only if the argument
     * is not <tt>null</tt>, is a <tt>ERefsetLongMember</tt> object, and contains the same values, field by
     * field, as this <tt>ERefsetLongMember</tt>.
     *
     * @param obj the object to compare with.
     * @return
     * <code>true</code> if the objects are the same;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (TkRefsetArrayOfBytearrayMember.class.isAssignableFrom(obj.getClass())) {
            TkRefsetArrayOfBytearrayMember another = (TkRefsetArrayOfBytearrayMember) obj;

            // =========================================================
            // Compare properties of 'this' class to the 'another' class
            // =========================================================
            // Compare byteArray
            if (!Arrays.deepEquals(this.arrayOfByteArray, another.arrayOfByteArray)) {
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

    @Override
    public TkRefsetArrayOfBytearrayMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefsetArrayOfBytearrayMember(this, conversionMap, offset, mapAll);
    }

    @Override
    public void readExternal(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super.readExternal(in, dataVersion);
        int arrayLength = in.readShort();
        this.arrayOfByteArray = new byte[arrayLength][];
        for (int i = 0; i < arrayLength; i++) {
            int byteArrayLength = in.readInt();
            this.arrayOfByteArray[i] = new byte[byteArrayLength];
            in.readFully(this.arrayOfByteArray[i]);
        }

        int versionSize = in.readInt();

        if (versionSize > 0) {
            revisions = new ArrayList<TkRefsetArrayByteArrayRevision>(versionSize);

            for (int i = 0; i < versionSize; i++) {
                TkRefsetArrayByteArrayRevision rev = new TkRefsetArrayByteArrayRevision(in, dataVersion);
                if (rev.getTime() == this.time) {
                    // TODO this check can be removed after trek-95 change sets are no longer in production. 
                    arrayOfByteArray = rev.arrayOfByteArray;
                } else {
                    revisions.add(rev);
                }
            }
        }
    }

    /**
     * Returns a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();

        buff.append(this.getClass().getSimpleName()).append(": ");
        buff.append(" size: ");
        buff.append(this.arrayOfByteArray.length);
        for (int i = 0; i < this.arrayOfByteArray.length; i++) {
            buff.append(" ").append(i);
            buff.append(": ");
            buff.append(this.arrayOfByteArray[i]);
        }
        buff.append(" ");
        buff.append(super.toString());

        return buff.toString();
    }

    @Override
    public void writeExternal(DataOutput out) throws IOException {
        super.writeExternal(out);
        out.writeShort(arrayOfByteArray.length);
        for (byte[] bytes : arrayOfByteArray) {
            out.writeInt(bytes.length);
            out.write(bytes);
        }
        if (revisions == null) {
            out.writeInt(0);
        } else {
            out.writeInt(revisions.size());

            for (TkRefsetArrayByteArrayRevision rmv : revisions) {
                rmv.writeExternal(out);
            }
        }
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public List<TkRefsetArrayByteArrayRevision> getRevisionList() {
        return revisions;
    }

    @Override
    public TK_REFSET_TYPE getType() {
        return TK_REFSET_TYPE.ARRAY_BYTEARRAY;
    }

    public byte[][] getArrayOfByteArray() {
        return arrayOfByteArray;
    }

    public void setArrayOfByteArray(byte[][] byteArray) {
        this.arrayOfByteArray = byteArray;
    }
}
