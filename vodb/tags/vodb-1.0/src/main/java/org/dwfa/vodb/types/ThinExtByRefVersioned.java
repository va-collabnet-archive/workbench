/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.vodb.types;

import java.util.ArrayList;
import java.util.List;

/**
 * @todo add version to vodb -> added as getProperty...
 * @todo add imported change set info to vodb, need to set theProperty...
 * 
 * @todo have change sets automatically increment as size increases over a certain size. 
 * Added increment to change set file name format. 
 * @todo add extension ability
 * 
 * @author kec
 *
 */
public class ThinExtByRefVersioned {
   
   private int refsetId;
   private int memberId;
   private int componentId;
   private int typeId; //Use an enumeration when reading/writing, and convert it to the corresponding concept nid...
   private List<ThinExtByRefPart> versions ;
   
   public ThinExtByRefVersioned(int refsetId, int memberId, int componentId, int typeId) {
      this(refsetId, memberId, componentId, typeId, 1);
   }
   
   public ThinExtByRefVersioned(int refsetId, int memberId, int componentId, int typeId, int partCount) {
      super();
      this.refsetId = refsetId;
      this.memberId = memberId;
      this.componentId = componentId;
      this.typeId = typeId;
      this.versions = new ArrayList<ThinExtByRefPart>(partCount);
   }
   
   public int getMemberId() {
      return memberId;
   }

   public int getComponentId() {
      return componentId;
   }

   public int getTypeId() {
      return typeId;
   }

   public List<? extends ThinExtByRefPart> getVersions() {
      return versions;
   }

   public int getRefsetId() {
      return refsetId;
   }
   @Override
   public boolean equals(Object obj) {
      ThinExtByRefVersioned another = (ThinExtByRefVersioned) obj;
      return ((refsetId == another.refsetId) &&
            (memberId == another.memberId) &&
            (componentId == another.componentId) &&
            (typeId == another.typeId) &&
            (versions.equals(another.versions)));
   }
   @Override
   public int hashCode() {
      return HashFunction.hashCode(new int[] {refsetId, memberId, componentId, typeId });
   }

   public void addVersion(ThinExtByRefPart part) {
      versions.add(part);
   }

   public void setRefsetId(int refsetId) {
      this.refsetId = refsetId;
   }

   public void setTypeId(int typeId) {
      this.typeId = typeId;
   }

}
