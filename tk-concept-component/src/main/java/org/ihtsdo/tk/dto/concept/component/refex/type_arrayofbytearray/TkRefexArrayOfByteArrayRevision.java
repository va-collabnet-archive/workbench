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
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import org.ihtsdo.tk.api.refex.type_array_of_bytearray.RefexArrayOfBytearrayVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkRevision;

/**
 *
 * @author kec
 */
public class TkRefexArrayOfByteArrayRevision extends TkRevision {
   public static final long serialVersionUID = 1;

   //~--- fields --------------------------------------------------------------

   public byte[][] arrayOfByteArray1;

   //~--- constructors --------------------------------------------------------

   public TkRefexArrayOfByteArrayRevision() {
      super();
   }

   public TkRefexArrayOfByteArrayRevision(RefexArrayOfBytearrayVersionBI refexArrayOfBytearrayVersion) throws IOException {
      super(refexArrayOfBytearrayVersion);
      this.arrayOfByteArray1 = refexArrayOfBytearrayVersion.getArrayOfByteArray();
   }

   public TkRefexArrayOfByteArrayRevision(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
      super();
      readExternal(in, dataVersion);
   }

   public TkRefexArrayOfByteArrayRevision(TkRefexArrayOfByteArrayRevision another, Map<UUID, UUID> conversionMap, long offset,
                               boolean mapAll) {
      super(another, conversionMap, offset, mapAll);
      this.arrayOfByteArray1 = another.arrayOfByteArray1;
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

      if (TkRefexArrayOfByteArrayRevision.class.isAssignableFrom(obj.getClass())) {
         TkRefexArrayOfByteArrayRevision another = (TkRefexArrayOfByteArrayRevision) obj;

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

    @Override
    public int hashCode() {
        return super.hashCode();
    }

   @Override
   public TkRefexArrayOfByteArrayRevision makeConversion(Map<UUID, UUID> conversionMap, long offset, boolean mapAll) {
      return new TkRefexArrayOfByteArrayRevision(this, conversionMap, offset, mapAll);
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
      for (byte[] bytes: arrayOfByteArray1) {
        out.writeInt(bytes.length);  
        out.write(bytes);
      }
   }

    public byte[][] getArrayOfByteArray1() {
        return arrayOfByteArray1;
    }

    public void setArrayOfByteArray1(byte[][] byteArray1) {
        this.arrayOfByteArray1 = byteArray1;
    }
}

