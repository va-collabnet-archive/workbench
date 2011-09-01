package org.ihtsdo.db.bdb.sap;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.hash.Hashcode;

public class UncommittedStatusForPath {
   public int hashCode = Integer.MAX_VALUE;
   public int authorNid;
   public int pathNid;
   public int statusNid;

   //~--- constructors --------------------------------------------------------

   public UncommittedStatusForPath(int statusNid, int authorNid, int pathNid) {
      super();
      this.statusNid = statusNid;
      this.authorNid = authorNid;
      this.pathNid   = pathNid;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof UncommittedStatusForPath) {
         UncommittedStatusForPath other = (UncommittedStatusForPath) obj;

         if ((statusNid == other.statusNid) && (authorNid == other.authorNid) && (pathNid == other.pathNid)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public int hashCode() {
      if (hashCode == Integer.MAX_VALUE) {
         hashCode = Hashcode.compute(new int[] { statusNid, authorNid, pathNid });
      }

      return hashCode;
   }
}
