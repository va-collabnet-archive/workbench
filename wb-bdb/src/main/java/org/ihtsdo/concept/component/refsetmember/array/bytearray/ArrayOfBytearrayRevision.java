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
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayAnalogBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_array_of_bytearray.TkRefexArrayOfByteArrayRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_array_of_bytearray.TkRefexArrayOfBytearrayMember;
import org.ihtsdo.tk.uuid.UuidT5Generator;

/**
 *
 * @author kec
 */
public class ArrayOfBytearrayRevision extends RefsetRevision<ArrayOfBytearrayRevision, ArrayOfBytearrayMember>
        implements RefexArrayOfBytearrayAnalogBI<ArrayOfBytearrayRevision>  {

   private byte[][] arrayOfByteArray;

    @Override
    public byte[][] getArrayOfByteArray() {
        return arrayOfByteArray;
    }

    @Override
    public void setArrayOfByteArray(byte[][] arrayOfByteArray) {
        this.arrayOfByteArray = arrayOfByteArray;
        modified();
    }

    
   //~--- constructors --------------------------------------------------------

   public ArrayOfBytearrayRevision() {
      super();
   }

   protected ArrayOfBytearrayRevision(int statusAtPositionNid, ArrayOfBytearrayMember primoridalMember) {
      super(statusAtPositionNid, primoridalMember);
      this.arrayOfByteArray = primoridalMember.getArrayOfByteArray();
   }

   public ArrayOfBytearrayRevision(TkRefexArrayOfByteArrayRevision eVersion, ArrayOfBytearrayMember booleanMember) {
      super(eVersion, booleanMember);
      this.arrayOfByteArray = eVersion.getArrayOfByteArray1();
   }

   public ArrayOfBytearrayRevision(TupleInput in, ArrayOfBytearrayMember primoridalMember) {
      super(in, primoridalMember);
      int arrayLength = in.readShort();
      this.arrayOfByteArray = new byte[arrayLength][];
      for (int i = 0; i < arrayLength; i++) {
          int byteArrayLength = in.readInt();
          this.arrayOfByteArray[i] = new byte[byteArrayLength];
          in.read(this.arrayOfByteArray[i], 0, byteArrayLength);
      }
   }

   protected ArrayOfBytearrayRevision(int statusNid, long time, int authorNid,
           int moduleNid, int pathNid, ArrayOfBytearrayMember primoridalMember) {
      super(statusNid, time, authorNid, moduleNid, pathNid, primoridalMember);
      this.arrayOfByteArray = primoridalMember.getArrayOfByteArray();
   }

   protected ArrayOfBytearrayRevision(int statusNid, long time, int authorNid,
           int moduleNid, int pathNid, ArrayOfBytearrayRevision another) {
      super(statusNid, time, authorNid, moduleNid, pathNid, another.primordialComponent);
      this.arrayOfByteArray = another.getArrayOfByteArray();
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected void addRefsetTypeNids(Set<Integer> allNids) {

      // ;
   }

    @Override
   protected void addSpecProperties(RefexCAB rcs) {
      rcs.with(RefexCAB.RefexProperty.ARRAY_BYTEARRAY, getArrayOfByteArray());
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (ArrayOfBytearrayRevision.class.isAssignableFrom(obj.getClass())) {
         ArrayOfBytearrayRevision another = (ArrayOfBytearrayRevision) obj;

         return (Arrays.deepEquals(arrayOfByteArray, another.getArrayOfByteArray())) && super.equals(obj);
      }

      return false;
   }

   @Override
   public ArrayOfBytearrayRevision makeAnalog() {
      return new ArrayOfBytearrayRevision(getStatusNid(), getTime(), getAuthorNid(),
              getModuleNid(), getPathNid(),  this);
   }
   
   @Override
   public ArrayOfBytearrayRevision makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      if ((this.getTime() == time) && (this.getPathNid() == pathNid)) {
         this.setStatusNid(statusNid);
         this.setAuthorNid(authorNid);
         this.setModuleNid(moduleNid);

         return this;
      }

      ArrayOfBytearrayRevision newR = new ArrayOfBytearrayRevision(statusNid, time,
              authorNid, moduleNid, pathNid,this);

      primordialComponent.addRevision(newR);

      return newR;
   }

   @Override
   public I_ExtendByRefPart<ArrayOfBytearrayRevision> makePromotionPart(PathBI promotionPath, int authorNid) {

      // TODO
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean readyToWriteRefsetRevision() {
      return true;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      StringBuilder buff = new StringBuilder();
      buff.append(" size: ");
      buff.append(this.arrayOfByteArray.length);
      for (int i = 0; i < this.arrayOfByteArray.length; i++) {
        buff.append(" ").append(i);
        buff.append(": ");
        if (this.arrayOfByteArray[i].length == 16){
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
   protected void writeFieldsToBdb(TupleOutput out) {
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
   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {

      // TODO
      throw new UnsupportedOperationException();
   }

   @Override
   public ArrayIntList getVariableVersionNids() {
      ArrayIntList variableNids = new ArrayIntList(2);

      return variableNids;
   }

   @Override
   public ArrayOfBytearrayMember.Version getVersion(ViewCoordinate c) throws ContradictionException {
      return (ArrayOfBytearrayMember.Version) ((ArrayOfBytearrayMember) primordialComponent).getVersion(c);
   }

   @Override
   public Collection<ArrayOfBytearrayMember.Version> getVersions() {
      return ((ArrayOfBytearrayMember) primordialComponent).getVersions();
   }

   @Override
   public Collection<? extends RefexVersionBI<ArrayOfBytearrayRevision>> getVersions(ViewCoordinate c) {
      return ((ArrayOfBytearrayMember) primordialComponent).getVersions(c);
   }

   //~--- set methods ---------------------------------------------------------
}
