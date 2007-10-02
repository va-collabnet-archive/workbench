package org.dwfa.vodb.types;

import java.io.IOException;

import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;


public abstract class ThinExtByRefPart {
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
      ThinExtByRefPart another = (ThinExtByRefPart) obj;
      return ((pathId == another.pathId) &&
            (version == another.version) &&
            (status == another.status));
   }
   @Override
   public int hashCode() {
      return HashFunction.hashCode(new int[] {pathId, version, status});
   }
   
   public abstract UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException;

   public abstract ThinExtByRefPart duplicatePart();
   
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
