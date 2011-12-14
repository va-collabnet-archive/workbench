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

public interface TerminologySnapshotDI extends TerminologyTransactionDI {
    
   PositionBI newPosition(PathBI path, long time) throws IOException;

   /**
    * 
    * @param ec
    * @return
    * @deprecated use getBuilder
    */
   @Deprecated
   TerminologyBuilderBI getAmender(EditCoordinate ec);

   TerminologyBuilderBI getBuilder(EditCoordinate ec);

   ComponentVersionBI getComponentVersion(Collection<UUID> uuids) throws IOException, ContradictionException;

   ComponentVersionBI getComponentVersion(ComponentContainerBI cc) throws IOException, ContradictionException;

   ComponentVersionBI getComponentVersion(int nid) throws IOException, ContradictionException;

   ComponentVersionBI getComponentVersion(UUID... uuids) throws IOException, ContradictionException;

   ConceptVersionBI getConceptForNid(int nid) throws IOException;

   ConceptVersionBI getConceptVersion(Collection<UUID> uuids) throws IOException;

   ConceptVersionBI getConceptVersion(ConceptContainerBI cc) throws IOException;

   ConceptVersionBI getConceptVersion(int cNid) throws IOException;

   ConceptVersionBI getConceptVersion(UUID... uuids) throws IOException;

   Map<Integer, ConceptVersionBI> getConceptVersions(NidBitSetBI cNids) throws IOException;

   int[] getPossibleChildren(int cNid) throws IOException;

   ViewCoordinate getViewCoordinate();
}
