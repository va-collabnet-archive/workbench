package org.ihtsdo.tk.api;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Set;

public interface ComponentVersionBI extends ComponentBI {
   boolean sapIsInRange(int min, int max);

   String toUserString(TerminologySnapshotDI snapshot) throws IOException, ContraditionException;

   //~--- get methods ---------------------------------------------------------

   Set<Integer> getAllNidsForVersion() throws IOException;

   int getAuthorNid();

   ComponentChroncileBI getChronicle();

   int getPathNid();

   PositionBI getPosition() throws IOException;

   int getSapNid();

   int getStatusNid();

   long getTime();

   boolean isActive(NidSetBI allowedStatusNids) throws IOException;

   boolean isActive(ViewCoordinate vc) throws IOException;
   
   public boolean isUncommitted();
   
   /**
    *
    * @return  <code>true</code> if this version is stored in the read-only
    * database, rather than in the mutable database. <code>false</code> otherwise.
    */
   boolean isBaselineGeneration();
}
