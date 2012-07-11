package org.ihtsdo.tk.api;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface TerminologySnapshotDI extends TerminologyDI {
    
   PositionBI newPosition(PathBI path, long time) throws IOException;

   /**
    * 
    * @param editCoordinate
    * @return
    * @deprecated use getBuilder
    */
   @Deprecated
   TerminologyBuilderBI getAmender(EditCoordinate editCoordinate);

   TerminologyBuilderBI getBuilder(EditCoordinate editCoordinate);

   ComponentVersionBI getComponentVersion(Collection<UUID> uuids) throws IOException, ContradictionException;

   ComponentVersionBI getComponentVersion(ComponentContainerBI componentContainer) throws IOException, ContradictionException;

   ComponentVersionBI getComponentVersion(int nid) throws IOException, ContradictionException;

   ComponentVersionBI getComponentVersion(UUID... uuids) throws IOException, ContradictionException;

   ConceptVersionBI getConceptForNid(int nid) throws IOException;

   ConceptVersionBI getConceptVersion(Collection<UUID> uuids) throws IOException;

   ConceptVersionBI getConceptVersion(ConceptContainerBI conceptContainer) throws IOException;

   ConceptVersionBI getConceptVersion(int conceptNid) throws IOException;

   ConceptVersionBI getConceptVersion(UUID... uuids) throws IOException;

   Map<Integer, ConceptVersionBI> getConceptVersions(NidBitSetBI conceptNids) throws IOException;

   int[] getPossibleChildren(int conceptNid) throws IOException;

   ViewCoordinate getViewCoordinate();
   
   int getConceptNidForNid(Integer nid) throws IOException;
}
