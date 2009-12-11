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

import java.io.IOException;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;


public abstract class ThinExtByRefPart implements I_ThinExtByRefPart, Comparable<I_ThinExtByRefPart> {
   private int pathId;
   private int version;
   private int status;
   
   public int getStatusId() {
	   return getStatus();
   }
/* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPart#getStatus()
    */
   public int getStatus() {
      return status;
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPart#setStatus(int)
    */
   public void setStatus(int idStatus) {
      this.status = idStatus;
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPart#getPathId()
    */
   public int getPathId() {
      return pathId;
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPart#setPathId(int)
    */
   public void setPathId(int pathId) {
      this.pathId = pathId;
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPart#getVersion()
    */
   public int getVersion() {
      return version;
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPart#setVersion(int)
    */
   public void setVersion(int version) {
      this.version = version;
   }

   @Override
   public boolean equals(Object obj) {
      ThinExtByRefPart another = (ThinExtByRefPart) obj;
      return ((pathId == another.pathId) &&
            (version == another.version) &&
            (status == another.status));
   }
   @Override
   public int hashCode() {
      return HashFunction.hashCode(new int[] {pathId, version, status});
   }
   
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPart#getUniversalPart()
    */
   public abstract UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException;

   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPart#duplicatePart()
    */
   public abstract I_ThinExtByRefPart duplicatePart();
   
   public ThinExtByRefPart(ThinExtByRefPart another) {
      super();
      this.pathId = another.pathId;
      this.version = another.version;
      this.status = another.status;
   }
   public ThinExtByRefPart() {
      super();
   }
   
   public String toString() {
       return this.getClass().getSimpleName() + " pathId: " + pathId + " version: " + version + " status: " + status;
   }
}
