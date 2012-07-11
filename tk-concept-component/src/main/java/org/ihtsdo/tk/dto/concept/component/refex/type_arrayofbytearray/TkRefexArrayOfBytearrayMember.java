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

/**
 *
 * @author kec
 */
public class TkRefexArrayOfBytearrayMember extends TkRefexAbstractMember<TkRefexArrayOfByteArrayRevision> {

    public static final long serialVersionUID = 1;
    //~--- fields --------------------------------------------------------------
    public byte[][] arrayOfByteArray1;

    //~--- constructors --------------------------------------------------------
    public TkRefexArrayOfBytearrayMember() {
        super();
    }

    public TkRefexArrayOfBytearrayMember(RefexChronicleBI refexChronicle) throws IOException {
        this((RefexArrayOfBytearrayVersionBI) refexChronicle.getPrimordialVersion(), RevisionHandling.INCLUDE_REVISIONS);
    }

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
    

    public TkRefexArrayOfBytearrayMember(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super();
        readExternal(in, dataVersion);
    }

    public TkRefexArrayOfBytearrayMember(TkRefexArrayOfBytearrayMember another, Map<UUID, UUID> conversionMap, long offset,
            boolean mapAll) {
        super(another, conversionMap, offset, mapAll);
        this.arrayOfByteArray1 = another.arrayOfByteArray1;
    }

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
     * @return
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

    @Override
    public TkRefexArrayOfBytearrayMember makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
        return new TkRefexArrayOfBytearrayMember(this, conversionMap, offset, mapAll);
    }

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
    @Override
    public List<TkRefexArrayOfByteArrayRevision> getRevisionList() {
        return revisions;
    }

    @Override
    public TK_REFEX_TYPE getType() {
        return TK_REFEX_TYPE.ARRAY_BYTEARRAY;
    }

    public byte[][] getArrayOfByteArray1() {
        return arrayOfByteArray1;
    }

    public void setArrayOfByteArray1(byte[][] byteArray1) {
        this.arrayOfByteArray1 = byteArray1;
    }
}
