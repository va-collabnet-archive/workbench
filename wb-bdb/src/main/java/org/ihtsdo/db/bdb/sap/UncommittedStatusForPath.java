package org.ihtsdo.db.bdb.sap;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.hash.Hashcode;

public class UncommittedStatusForPath {
   public int pathNid;
   public int statusNid;

   //~--- constructors --------------------------------------------------------

   public UncommittedStatusForPath(int statusNid, int pathNid) {
      super();
      this.statusNid = statusNid;
      this.pathNid   = pathNid;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof UncommittedStatusForPath) {
         UncommittedStatusForPath other = (UncommittedStatusForPath) obj;

         if ((statusNid == other.statusNid) && (pathNid == other.pathNid)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public int hashCode() {
      return Hashcode.compute(new int[] { statusNid, pathNid });
   }
}
