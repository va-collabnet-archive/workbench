package org.dwfa.vodb.types;


public abstract class ThinExtPart {
   private int pathId;
   private int version;
   private int status;
   
   public int getStatus() {
      return status;
   }
   public void setStatus(int idStatus) {
      this.status = idStatus;
   }
   public int getPathId() {
      return pathId;
   }
   public void setPathId(int pathId) {
      this.pathId = pathId;
   }
   public int getVersion() {
      return version;
   }
   public void setVersion(int version) {
      this.version = version;
   }

   @Override
   public boolean equals(Object obj) {
      ThinExtPart another = (ThinExtPart) obj;
      return ((pathId == another.pathId) &&
            (version == another.version) &&
            (status == another.status));
   }
   @Override
   public int hashCode() {
      return HashFunction.hashCode(new int[] {pathId, version, status});
   }

}
