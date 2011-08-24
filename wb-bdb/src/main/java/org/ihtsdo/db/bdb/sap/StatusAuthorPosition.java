
/**
 *
 */
package org.ihtsdo.db.bdb.sap;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.hash.Hashcode;

public class StatusAuthorPosition implements Comparable<StatusAuthorPosition> {
   private int  authorNid;
   private int  pathNid;
   private int  statusNid;
   private long time;

   //~--- constructors --------------------------------------------------------

   StatusAuthorPosition(int statusNid, int authorNid, int pathNid, long time) {
      super();
      this.statusNid = statusNid;
      this.authorNid = authorNid;
      this.pathNid   = pathNid;
      this.time      = time;
      assert time != 0;
      assert statusNid != 0;
      assert pathNid != 0;
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
      return Hashcode.compute(new int[] { authorNid, statusNid, pathNid, (int) time });
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

   public long getTime() {
      return time;
   }

   //~--- set methods ---------------------------------------------------------

   public void setAuthorNid(int authorNid) {
      this.authorNid = authorNid;
   }
}
