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

import java.util.List;

public class ThinExtByRefTuple {
   ThinExtByRefPart part;
   ThinExtByRefVersioned core;
   public ThinExtByRefTuple(ThinExtByRefVersioned core, ThinExtByRefPart part) {
      super();
      this.part = part;
      this.core = core;
   }
   public int getPathId() {
      return part.getPathId();
   }
   public int getStatus() {
      return part.getStatus();
   }
   public int getVersion() {
      return part.getVersion();
   }
   public void setPathId(int pathId) {
      part.setPathId(pathId);
   }
   public void setStatus(int idStatus) {
      part.setStatus(idStatus);
   }
   public void setVersion(int version) {
      part.setVersion(version);
   }
   public void addVersion(ThinExtByRefPart part) {
      core.addVersion(part);
   }
   public int getComponentId() {
      return core.getComponentId();
   }
   public int getMemberId() {
      return core.getMemberId();
   }
   public int getRefsetId() {
      return core.getRefsetId();
   }
   public int getTypeId() {
      return core.getTypeId();
   }
   public List<? extends ThinExtByRefPart> getVersions() {
      return core.getVersions();
   }
   public ThinExtByRefVersioned getCore() {
      return core;
   }
   public ThinExtByRefPart getPart() {
      return part;
   }
}
