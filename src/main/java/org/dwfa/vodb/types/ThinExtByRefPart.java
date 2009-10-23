package org.dwfa.vodb.types;

import java.io.IOException;
import java.util.Date;

import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;


public abstract class ThinExtByRefPart implements I_ThinExtByRefPart, Comparable<I_ThinExtByRefPart> {
   private int pathId;
   private int version;
   private int status;
   
/* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPart#getStatus()
    */
   @Deprecated
   public int getStatus() {
      return status;
   }
   
   /* (non-Javadoc)
    * @see org.dwfa.ace.api.I_AmPart#getStatusId()
    */
   public int getStatusId() {
	   return getStatus();
   }   
   
   /* (non-Javadoc)
    * @see org.dwfa.vodb.types.I_ThinExtByRefPart#setStatus(int)
    */
   @Deprecated
   public void setStatus(int idStatus) {
      this.status = idStatus;
   }
   
   /* (non-Javadoc)
    * @see org.dwfa.ace.api.I_AmPart#setStatusId(int)
    */
   public void setStatusId(int statusId) {	
	   setStatus(statusId);
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
	   try {
		   StringBuffer buff = new StringBuffer();
		   buff.append(this.getClass().getSimpleName());
		   buff.append(" path: ");
		   buff.append(LocalVersionedTerminology.get().getConcept(pathId).toString());
		   buff.append(" version: ");
		   buff.append(new Date(LocalVersionedTerminology.get().convertToThickVersion(version)));
		   buff.append(" status: ");
		   buff.append(LocalVersionedTerminology.get().getConcept(status).toString());
		   
		   return buff.toString();
	   } catch (Exception e) {
		AceLog.getAppLog().alertAndLogException(e);
	   }
       return this.getClass().getSimpleName() + " pathId: " + pathId + " version: " + version + " status: " + status;
   }

   @Deprecated
   public I_ThinExtByRefPart duplicatePart() {
	   return duplicate();
   }
}


