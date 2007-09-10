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
