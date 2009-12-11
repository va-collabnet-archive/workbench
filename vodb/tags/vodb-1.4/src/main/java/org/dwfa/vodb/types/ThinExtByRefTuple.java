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
import java.util.List;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;

public class ThinExtByRefTuple implements I_ThinExtByRefTuple {
   I_ThinExtByRefPart part;
   I_ThinExtByRefVersioned core;
   public ThinExtByRefTuple(I_ThinExtByRefVersioned core, I_ThinExtByRefPart part) {
      super();
      this.part = part;
      this.core = core;
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#getPathId()
    */
   public int getPathId() {
      return part.getPathId();
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#getStatus()
    */
   public int getStatus() {
      return part.getStatus();
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#getVersion()
    */
   public int getVersion() {
      return part.getVersion();
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#setPathId(int)
    */
   public void setPathId(int pathId) {
      part.setPathId(pathId);
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#setStatus(int)
    */
   public void setStatus(int idStatus) {
      part.setStatus(idStatus);
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#setVersion(int)
    */
   public void setVersion(int version) {
      part.setVersion(version);
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#addVersion(org.dwfa.vodb.types.ThinExtByRefPart)
    */
   public void addVersion(I_ThinExtByRefPart part) {
      core.addVersion(part);
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#getComponentId()
    */
   public int getComponentId() {
      return core.getComponentId();
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#getMemberId()
    */
   public int getMemberId() {
      return core.getMemberId();
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#getRefsetId()
    */
   public int getRefsetId() {
      return core.getRefsetId();
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#getTypeId()
    */
   public int getTypeId() {
      return core.getTypeId();
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#getVersions()
    */
   public List<? extends I_ThinExtByRefPart> getVersions() {
      return core.getVersions();
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#getCore()
    */
   public I_ThinExtByRefVersioned getCore() {
      return core;
   }
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefTuple#getPart()
    */
   public I_ThinExtByRefPart getPart() {
      return part;
   }
   
   @Override
   public String toString() {
       return "ThinExtByRefVersioned refsetId: " + core.getRefsetId() + " memberId: " + core.getMemberId() + 
           " componentId: " + core.getComponentId() + " typeId: " + core.getTypeId() + " version: " + part;
   }
   public I_ThinExtByRefPart duplicatePart() {
      return part.duplicatePart();
   }
   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {
      return part.getUniversalPart();
   }

}
