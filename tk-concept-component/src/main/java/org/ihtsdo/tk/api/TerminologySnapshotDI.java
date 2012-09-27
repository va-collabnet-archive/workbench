/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

// TODO: Auto-generated Javadoc
/**
 * The Interface TerminologySnapshotDI.
 */
public interface TerminologySnapshotDI extends TerminologyDI {
    
   /**
    * New position.
    *
    * @param path the path
    * @param time the time
    * @return the position bi
    * @throws IOException Signals that an I/O exception has occurred.
    */
   PositionBI newPosition(PathBI path, long time) throws IOException;

   /**
    * Gets the amender.
    *
    * @param editCoordinate the edit coordinate
    * @return the amender
    * @deprecated use getBuilder
    */
   @Deprecated
   TerminologyBuilderBI getAmender(EditCoordinate editCoordinate);

   /**
    * Gets the builder.
    *
    * @param editCoordinate the edit coordinate
    * @return the builder
    */
   TerminologyBuilderBI getBuilder(EditCoordinate editCoordinate);

   /**
    * Gets the component version.
    *
    * @param uuids the uuids
    * @return the component version
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws ContradictionException the contradiction exception
    */
   ComponentVersionBI getComponentVersion(Collection<UUID> uuids) throws IOException, ContradictionException;

   /**
    * Gets the component version.
    *
    * @param componentContainer the component container
    * @return the component version
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws ContradictionException the contradiction exception
    */
   ComponentVersionBI getComponentVersion(ComponentContainerBI componentContainer) throws IOException, ContradictionException;

   /**
    * Gets the component version.
    *
    * @param nid the nid
    * @return the component version
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws ContradictionException the contradiction exception
    */
   ComponentVersionBI getComponentVersion(int nid) throws IOException, ContradictionException;

   /**
    * Gets the component version.
    *
    * @param uuids the uuids
    * @return the component version
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws ContradictionException the contradiction exception
    */
   ComponentVersionBI getComponentVersion(UUID... uuids) throws IOException, ContradictionException;

   /**
    * Gets the concept for nid.
    *
    * @param nid the nid
    * @return the concept for nid
    * @throws IOException Signals that an I/O exception has occurred.
    */
   ConceptVersionBI getConceptForNid(int nid) throws IOException;

   /**
    * Gets the concept version.
    *
    * @param uuids the uuids
    * @return the concept version
    * @throws IOException Signals that an I/O exception has occurred.
    */
   ConceptVersionBI getConceptVersion(Collection<UUID> uuids) throws IOException;

   /**
    * Gets the concept version.
    *
    * @param conceptContainer the concept container
    * @return the concept version
    * @throws IOException Signals that an I/O exception has occurred.
    */
   ConceptVersionBI getConceptVersion(ConceptContainerBI conceptContainer) throws IOException;

   /**
    * Gets the concept version.
    *
    * @param conceptNid the concept nid
    * @return the concept version
    * @throws IOException Signals that an I/O exception has occurred.
    */
   ConceptVersionBI getConceptVersion(int conceptNid) throws IOException;

   /**
    * Gets the concept version.
    *
    * @param uuids the uuids
    * @return the concept version
    * @throws IOException Signals that an I/O exception has occurred.
    */
   ConceptVersionBI getConceptVersion(UUID... uuids) throws IOException;

   /**
    * Gets the concept versions.
    *
    * @param conceptNids the concept nids
    * @return the concept versions
    * @throws IOException Signals that an I/O exception has occurred.
    */
   Map<Integer, ConceptVersionBI> getConceptVersions(NidBitSetBI conceptNids) throws IOException;

   /**
    * Gets the possible children.
    *
    * @param conceptNid the concept nid
    * @return the possible children
    * @throws IOException Signals that an I/O exception has occurred.
    */
   int[] getPossibleChildren(int conceptNid) throws IOException;

   /**
    * Gets the view coordinate.
    *
    * @return the view coordinate
    */
   ViewCoordinate getViewCoordinate();
   
   /**
    * Gets the concept nid for nid.
    *
    * @param nid the nid
    * @return the concept nid for nid
    * @throws IOException Signals that an I/O exception has occurred.
    */
   int getConceptNidForNid(Integer nid) throws IOException;
   
   /**
    * Checks if is kind of.
    *
    * @param childNid the child nid
    * @param parentNid the parent nid
    * @return true, if is kind of
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws ContradictionException the contradiction exception
    */
   boolean isKindOf(int childNid, int parentNid) throws IOException, ContradictionException;
}
