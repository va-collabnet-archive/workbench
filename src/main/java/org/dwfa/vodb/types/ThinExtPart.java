package org.dwfa.vodb.types;

import com.sleepycat.je.DatabaseEntry;

public class ThinExtPart {
   private int pathId;
   private int version;
   private int status;
   private DatabaseEntry extension;
   
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
   public DatabaseEntry getExtension() {
      return extension;
   }
   public void setExtension(DatabaseEntry extension) {
      this.extension = extension;
   }

}
