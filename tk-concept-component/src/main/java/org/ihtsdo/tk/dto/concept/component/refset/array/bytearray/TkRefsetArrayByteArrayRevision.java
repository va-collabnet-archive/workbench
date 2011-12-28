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
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import org.ihtsdo.tk.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

/**
 *
 * @author kec
 */
public class TkRefsetArrayByteArrayRevision extends TkRevision {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public byte[][] arrayOfByteArray;

   //~--- constructors --------------------------------------------------------

   public TkRefsetArrayByteArrayRevision() {
      super();
   }

   public TkRefsetArrayByteArrayRevision(RefexArrayOfBytearrayVersionBI another) throws IOException {
      super(another);
      this.arrayOfByteArray = another.getArrayOfByteArray();
   }

   public TkRefsetArrayByteArrayRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TkRefsetArrayByteArrayRevision(TkRefsetArrayByteArrayRevision another, Map<UUID, UUID> conversionMap, long offset,
                               boolean mapAll) {
      super(another, conversionMap, offset, mapAll);
      this.arrayOfByteArray = another.arrayOfByteArray;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Compares this object to the specified object. The result is <tt>true</tt>
    * if and only if the argument is not <tt>null</tt>, is a
    * <tt>ERefsetLongVersion</tt> object, and contains the same values, field by field,
    * as this <tt>ERefsetLongVersion</tt>.
    *
    * @param obj the object to compare with.
    * @return <code>true</code> if the objects are the same;
    *         <code>false</code> otherwise.
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (TkRefsetArrayByteArrayRevision.class.isAssignableFrom(obj.getClass())) {
         TkRefsetArrayByteArrayRevision another = (TkRefsetArrayByteArrayRevision) obj;

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

    @Override
    public int hashCode() {
        return super.hashCode();
    }

   @Override
   public TkRefsetArrayByteArrayRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkRefsetArrayByteArrayRevision(this, conversionMap, offset, mapAll);
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
      for (byte[] bytes: arrayOfByteArray) {
        out.writeInt(bytes.length);  
        out.write(bytes);
      }
   }

    public byte[][] getArrayOfByteArray() {
        return arrayOfByteArray;
    }

    public void setArrayOfByteArray(byte[][] byteArray) {
        this.arrayOfByteArray = byteArray;
    }
}

