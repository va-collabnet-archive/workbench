
/**
 *
 */
package org.ihtsdo.db.bdb.sap;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.hash.Hashcode;

public class StatusAuthorPosition implements Comparable<StatusAuthorPosition> {
   public int   hashCode = Integer.MAX_VALUE;
   private int  authorNid;
   private int  pathNid;
   private int  statusNid;
   private int moduleNid;
   private long time;

   //~--- constructors --------------------------------------------------------

   StatusAuthorPosition(int statusNid, long time, int authorNid, int moduleNid, int pathNid) {
      super();
      this.statusNid = statusNid;
      this.authorNid = authorNid;
      this.pathNid   = pathNid;
      this.moduleNid = moduleNid;
      this.time      = time;
      
      assert time != 0: "s: " + statusNid + " t: " + time + " a: " + authorNid + " " + " m: " + moduleNid + " p: " + pathNid;
      assert statusNid != 0: "s: " + statusNid + " t: " + time + " a: " + authorNid + " " + " m: " + moduleNid + " p: " + pathNid;
      assert pathNid != 0: "s: " + statusNid + " t: " + time + " a: " + authorNid + " " + " m: " + moduleNid + " p: " + pathNid;
      assert moduleNid != 0: "s: " + statusNid + " t: " + time + " a: " + authorNid + " " + " m: " + moduleNid + " p: " + pathNid;
      assert authorNid != 0: "s: " + statusNid + " t: " + time + " a: " + authorNid + " " + " m: " + moduleNid + " p: " + pathNid;

   }

   //~--- methods -------------------------------------------------------------

   @Override
   public int compareTo(StatusAuthorPosition o) {
      if (this.time > o.time) {
         return 1;
      }

      if (this.time < o.time) {
         return -1;
      }

      if (this.statusNid != o.statusNid) {
         return this.statusNid - o.statusNid;
      }

      if (this.authorNid != o.authorNid) {
         return this.authorNid - o.authorNid;
      }
      
      if (this.moduleNid != o.moduleNid) {
         return this.moduleNid - o.moduleNid;
      }

      return this.pathNid - o.pathNid;
   }

   @Override
   public boolean equals(Object obj) {
      if (StatusAuthorPosition.class.isAssignableFrom(obj.getClass())) {
         return compareTo((StatusAuthorPosition) obj) == 0;
      }

      return false;
   }

   @Override
   public int hashCode() {
      if (hashCode == Integer.MAX_VALUE) {
         hashCode = Hashcode.compute(new int[] { authorNid, statusNid, pathNid, (int) time });
      }

      return hashCode;
   }

   //~--- get methods ---------------------------------------------------------

   public int getAuthorNid() {
      return authorNid;
   }

   public int getPathNid() {
      return pathNid;
   }

   public int getStatusNid() {
      return statusNid;
   }
   
   public int getModuleNid() {
      return moduleNid;
   }

   public long getTime() {
      return time;
   }
}
