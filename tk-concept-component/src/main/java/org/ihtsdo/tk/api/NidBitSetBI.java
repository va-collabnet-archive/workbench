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
package org.ihtsdo.tk.api;

// TODO: Auto-generated Javadoc
/**
 * The Interface NidBitSetBI.
 */
public interface NidBitSetBI {
   
   /**
    * And.
    *
    * @param other the other
    */
   public void and(NidBitSetBI other);

   /**
    * And not.
    *
    * @param other the other
    */
   void andNot(NidBitSetBI other);

   /**
    * Cardinality.
    *
    * @return number of set bits.
    */
   public int cardinality();

   /**
    * Clear.
    */
   public void clear();
   
   /**
    * Sets the all.
    */
   public void setAll();

   /**
    * Iterator.
    *
    * @return the native id bit set itr bi
    */
   public NidBitSetItrBI iterator();

   /**
    * Or.
    *
    * @param other the other
    */
   public void or(NidBitSetBI other);

   /**
    * Total bits.
    *
    * @return the int
    */
   public int totalBits();

   /**
    * Union.
    *
    * @param other the other
    */
   void union(NidBitSetBI other);

   /**
    * Xor.
    *
    * @param other the other
    */
   void xor(NidBitSetBI other);

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks if is member.
    *
    * @param nid the nid
    * @return true, if is member
    */
   public boolean isMember(int nid);

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the member.
    *
    * @param nid the new member
    */
   public void setMember(int nid);

   /**
    * Sets the not member.
    *
    * @param nid the new not member
    */
   public void setNotMember(int nid);
}
