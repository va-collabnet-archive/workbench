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
package org.ihtsdo.concept.component.refsetmember.array.bytearray;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.*;
import org.apache.commons.collections.primitives.ArrayIntList;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.RevisionSet;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayAnalogBI;
import org.ihtsdo.tk.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayVersionBI;
import org.ihtsdo.tk.dto.RevisionHandling;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_array_of_bytearray.TkRefexArrayOfByteArrayRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_array_of_bytearray.TkRefexArrayOfBytearrayMember;
import org.ihtsdo.tk.hash.Hashcode;
import org.ihtsdo.tk.uuid.UuidT5Generator;

/**
 *
 * @author kec
 */
public class ArrayOfBytearrayMember extends RefsetMember<ArrayOfBytearrayRevision, ArrayOfBytearrayMember>
        implements RefexArrayOfBytearrayAnalogBI<ArrayOfBytearrayRevision> {

    private static VersionComputer<RefsetMember<ArrayOfBytearrayRevision, ArrayOfBytearrayMember>.Version> computer =
            new VersionComputer<>();
    //~--- fields --------------------------------------------------------------
    private byte[][] arrayOfByteArray;

    @Override
    public byte[][] getArrayOfByteArray() {
        return arrayOfByteArray;
    }

    @Override
    public void setArrayOfByteArray(byte[][] byteArray) {
        this.arrayOfByteArray = byteArray;
        modified();
    }


    //~--- constructors --------------------------------------------------------
    public ArrayOfBytearrayMember() {
        super();
    }

    public ArrayOfBytearrayMember(int enclosingConceptNid, TupleInput input) throws IOException {
        super(enclosingConceptNid, input);
    }

    public ArrayOfBytearrayMember(TkRefexArrayOfBytearrayMember refsetMember, int enclosingConceptNid) throws IOException {
        super(refsetMember, enclosingConceptNid);
        arrayOfByteArray = refsetMember.getArrayOfByteArray1();

        if (refsetMember.getRevisionList() != null) {
            revisions = new RevisionSet(primordialSapNid);

            for (TkRefexArrayOfByteArrayRevision eVersion : refsetMember.getRevisionList()) {
                revisions.add(new ArrayOfBytearrayRevision(eVersion, this));
            }
        }
    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected void addRefsetTypeNids(Set<Integer> allNids) {
        // ;
    }
 
    @Override
    protected void addSpecProperties(RefexCAB rcs) {
        rcs.with(RefexCAB.RefexProperty.ARRAY_BYTEARRAY, arrayOfByteArray);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (RefexArrayOfBytearrayVersionBI.class.equals(obj.getClass())) {
            RefexArrayOfBytearrayVersionBI another = (RefexArrayOfBytearrayVersionBI) obj;

            return this.nid == another.getNid();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Hashcode.compute(new int[]{nid});
    }

    @Override
    public ArrayOfBytearrayRevision makeAnalog() {
        ArrayOfBytearrayRevision newR = new ArrayOfBytearrayRevision(getStatusNid(), getTime(),
                getAuthorNid(), getModuleNid(), getPathNid(), this);

        return newR;
    }
    
    @Override
    public ArrayOfBytearrayRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
        ArrayOfBytearrayRevision newR = new ArrayOfBytearrayRevision(statusNid, time,
                authorNid, moduleNid, pathNid, this);

        addRevision(newR);

        return newR;
    }

    @Override
    protected boolean refexFieldsEqual(ConceptComponent<ArrayOfBytearrayRevision, ArrayOfBytearrayMember> obj) {
        if (ArrayOfBytearrayMember.class.isAssignableFrom(obj.getClass())) {
            ArrayOfBytearrayMember another = (ArrayOfBytearrayMember) obj;

            return Arrays.deepEquals(this.arrayOfByteArray, another.arrayOfByteArray);
        }

        return false;
    }

    @Override
    public boolean refexFieldsEqual(RefexVersionBI another) {
        if(RefexArrayOfBytearrayVersionBI.class.isAssignableFrom(another.getClass())){
            RefexArrayOfBytearrayVersionBI bv = (RefexArrayOfBytearrayVersionBI) another;
            return Arrays.deepEquals(this.arrayOfByteArray, bv.getArrayOfByteArray());
        }
        return false;
    }

    @Override
    protected void readMemberFields(TupleInput in) {
      int arrayLength = in.readShort();
      this.arrayOfByteArray = new byte[arrayLength][];
      for (int i = 0; i < arrayLength; i++) {
          int byteArrayLength = in.readInt();
          this.arrayOfByteArray[i] = new byte[byteArrayLength];
          in.read(this.arrayOfByteArray[i], 0, byteArrayLength);
      }
    }

    @Override
    protected final ArrayOfBytearrayRevision readMemberRevision(TupleInput input) {
        return new ArrayOfBytearrayRevision(input, this);
    }

    @Override
    public boolean readyToWriteRefsetMember() {
        return true;
    }

    /*
     *  (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
     buff.append("AOBA size: ");
      buff.append(this.arrayOfByteArray.length);
      for (int i = 0; i < this.arrayOfByteArray.length; i++) {
        buff.append(" ").append(i);
        buff.append(": ");
        if(this.arrayOfByteArray[i].length == 16){
            buff.append(UuidT5Generator.getUuidFromRawBytes(this.arrayOfByteArray[i]));
        }else{
            buff.append(this.arrayOfByteArray[i]);
        }
        
      }
      buff.append(" ");
      buff.append(super.toString());

        return buff.toString();
    }

    @Override
    protected void writeMember(TupleOutput out) {
     out.writeShort(arrayOfByteArray.length);
      for (byte[] bytes: arrayOfByteArray) {
        out.writeInt(bytes.length);  
        out.write(bytes);
      }
    }

    //~--- get methods ---------------------------------------------------------

    @Override
    public TkRefexAbstractMember<?> getTkRefsetMemberActiveOnly(ViewCoordinate vc, NidBitSetBI exclusionSet,
            Map<UUID, UUID> conversionMap)
            throws ContradictionException, IOException {
        return new TkRefexArrayOfBytearrayMember(this, exclusionSet, conversionMap, 0, true, vc);
    }

    @Override
    protected TK_REFEX_TYPE getTkRefsetType() {
        return TK_REFEX_TYPE.ARRAY_BYTEARRAY;
    }

    @Override
    public int getTypeId() {
        return EConcept.REFSET_TYPES.ARRAY_OF_BYTEARRAY.getTypeNid();
    }

    @Override
    public int getTypeNid() {
        return EConcept.REFSET_TYPES.ARRAY_OF_BYTEARRAY.getTypeNid();
    }

    @Override
    protected ArrayIntList getVariableVersionNids() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected VersionComputer<RefsetMember<ArrayOfBytearrayRevision, ArrayOfBytearrayMember>.Version> getVersionComputer() {
        return computer;
    }

    @Override
    public List<ArrayOfBytearrayMember.Version> getVersions() {
        if (versions == null) {
            int count = 1;

            if (revisions != null) {
                count = count + revisions.size();
            }

            ArrayList<ArrayOfBytearrayMember.Version> list = new ArrayList<>(count);

            if (getTime() != Long.MIN_VALUE) {
                list.add(new ArrayOfBytearrayMember.Version(this));
            }

            if (revisions != null) {
                for (ArrayOfBytearrayRevision br : revisions) {
                    if (br.getTime() != Long.MIN_VALUE) {
                        list.add(new ArrayOfBytearrayMember.Version(br));
                    }
                }
            }

            versions = list;
        }

        return (List<ArrayOfBytearrayMember.Version>) versions;
    }

    //~--- set methods ---------------------------------------------------------

    //~--- inner classes -------------------------------------------------------
    public class Version extends RefsetMember<ArrayOfBytearrayRevision, ArrayOfBytearrayMember>.Version
            implements RefexArrayOfBytearrayAnalogBI<ArrayOfBytearrayRevision> {

        private Version(RefexArrayOfBytearrayAnalogBI<ArrayOfBytearrayRevision> cv) {
            super(cv);
        }

        //~--- methods ----------------------------------------------------------
        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public int compareTo(RefexVersionBI o) {
            if (RefexArrayOfBytearrayVersionBI.class.isAssignableFrom(o.getClass())) {
                RefexArrayOfBytearrayVersionBI another = (RefexArrayOfBytearrayVersionBI) o;

                if (!Arrays.deepEquals(arrayOfByteArray, another.getArrayOfByteArray())) {
                    return Arrays.deepToString(arrayOfByteArray).compareTo(Arrays.deepToString(another.getArrayOfByteArray()));
                } 
            }

            return super.compareTo(o);
        }

        @Override
        public int hashCodeOfParts() {
            return Arrays.deepHashCode(arrayOfByteArray);
        }

        //~--- get methods ------------------------------------------------------
        @Override
        public byte[][] getArrayOfByteArray() {
            return getCv().getArrayOfByteArray();
        }

        RefexArrayOfBytearrayAnalogBI<ArrayOfBytearrayRevision> getCv() {
            return (RefexArrayOfBytearrayAnalogBI<ArrayOfBytearrayRevision>) cv;
        }

        @Override
        public TkRefexArrayOfBytearrayMember getERefsetMember() throws IOException {
            return new TkRefexArrayOfBytearrayMember(this, RevisionHandling.EXCLUDE_REVISIONS);
        }

        @Override
        public TkRefexArrayOfByteArrayRevision getERefsetRevision() throws IOException {
            return new TkRefexArrayOfByteArrayRevision(this);
        }

        @Override
        public ArrayIntList getVariableVersionNids() {
            return new ArrayIntList();
        }

        //~--- set methods ------------------------------------------------------
        @Override
        public void setArrayOfByteArray(byte[][] arrayOfByteArray) throws PropertyVetoException {
            getCv().setArrayOfByteArray(arrayOfByteArray);
        }
    }
}
