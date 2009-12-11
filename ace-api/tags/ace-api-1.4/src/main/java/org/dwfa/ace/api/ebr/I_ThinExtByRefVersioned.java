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
package org.dwfa.ace.api.ebr;

import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;

public interface I_ThinExtByRefVersioned {

   public int getMemberId();

   public int getComponentId();

   public int getTypeId();

   public List<? extends I_ThinExtByRefPart> getVersions();

   public int getRefsetId();

   public void addVersion(I_ThinExtByRefPart part);

   public void setRefsetId(int refsetId);

   public void setTypeId(int typeId);

   /*
    * (non-Javadoc)
    * 
    * @see org.dwfa.vodb.types.I_RelVersioned#addTuples(org.dwfa.ace.IntSet,
    *      org.dwfa.ace.IntSet, java.util.Set, java.util.List, boolean)
    */
   public void addTuples(I_IntSet allowedStatus, Set<I_Position> positions,
         List<I_ThinExtByRefTuple> returnTuples, boolean addUncommitted);

}
