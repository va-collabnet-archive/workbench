package org.ihtsdo.tk.api;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Set;
import org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;

public interface ComponentVersionBI extends ComponentBI, VersionPointBI {
   boolean stampIsInRange(int minStampNid, int maxStampNid);

   String toUserString(TerminologySnapshotDI terminologySnapshot) throws IOException, ContradictionException;

   //~--- get methods ---------------------------------------------------------

   Set<Integer> getAllNidsForVersion() throws IOException;

   int getAuthorNid();
   
   int getModuleNid();

   ComponentChronicleBI getChronicle();

   PositionBI getPosition() throws IOException;

   int getStampNid();

   int getStatusNid();

   boolean isActive(NidSetBI allowedStatusNids) throws IOException;

   boolean isActive(ViewCoordinate viewCoordinate) throws IOException;
   
   public boolean isUncommitted();
   
   /**
    *
    * @return  <code>true</code> if this version is stored in the read-only
    * database, rather than in the mutable database. <code>false</code> otherwise.
    */
   boolean isBaselineGeneration();
   
   /**
    *
    * @param viewCoordinate1 ViewCoordinate of the first version
    * 
    * @param viewCoordinate2 ViewCoordinate of the second version
    * 
    * @param compareAuthoring Set to <code>true</code> to compare the author and path of the 
    * versions. Otherwise <code>false</code> to disregard author and path.
    * 
    * @return <code>true</code> if the versions are equal. <code>false</code> otherwise.
    */
   boolean versionsEqual(ViewCoordinate viewCoordinate1, ViewCoordinate viewCoordinate2, Boolean compareAuthoring);
   
   CreateOrAmendBlueprint makeBlueprint(ViewCoordinate viewCoordinate) 
           throws IOException, ContradictionException, InvalidCAB;
}
