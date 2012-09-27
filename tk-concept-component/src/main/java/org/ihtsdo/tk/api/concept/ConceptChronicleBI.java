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
package org.ihtsdo.tk.api.concept;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.group.RelationshipGroupVersionBI;
import org.ihtsdo.tk.contradiction.FoundContradictionVersions;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Interface ConceptChronicleBI.
 */
public interface ConceptChronicleBI extends ComponentChronicleBI<ConceptVersionBI> {
   
   /**
    * Cancel.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   void cancel() throws IOException;

   /**
    * Commit.
    *
    * @param changeSetGenerationPolicy the change set generation policy
    * @param changeSetGenerationThreadingPolicy the change set generation threading policy
    * @return true, if successful
    * @throws IOException Signals that an I/O exception has occurred.
    */
   boolean commit(ChangeSetGenerationPolicy changeSetGenerationPolicy,
                  ChangeSetGenerationThreadingPolicy changeSetGenerationThreadingPolicy)
           throws IOException;
   
   /**
    * Commit.
    *
    * @param changeSetGenerationPolicy the change set generation policy
    * @param changeSetGenerationThreadingPolicy the change set generation threading policy
    * @param writeAdjudication the write adjudication
    * @return true, if successful
    * @throws IOException Signals that an I/O exception has occurred.
    */
   boolean commit(ChangeSetGenerationPolicy changeSetGenerationPolicy,
            ChangeSetGenerationThreadingPolicy changeSetGenerationThreadingPolicy,
            boolean writeAdjudication)
            throws IOException;

   /**
    * Returns a longer - more complete - string representation of the chronicle.
    * Useful for diagnostic purposes.
    *
    * @return the string
    */
   String toLongString();

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the concept attributes.
    *
    * @return the concept attributes
    * @throws IOException Signals that an I/O exception has occurred.
    */
   ConceptAttributeChronicleBI getConceptAttributes() throws IOException;

   /**
    * Gets the refset member active for component.
    *
    * @param viewCoordinate the view coordinate
    * @param componentNid the component nid
    * @return the refset member active for component
    * @throws IOException Signals that an I/O exception has occurred.
    */
   RefexVersionBI<?> getRefsetMemberActiveForComponent(ViewCoordinate viewCoordinate, int componentNid)
           throws IOException;

   /**
    * Gets the refset members active.
    *
    * @param viewCoordinate the view coordinate
    * @return the refset members active
    * @throws IOException Signals that an I/O exception has occurred.
    */
   Collection<? extends RefexVersionBI<?>> getRefsetMembersActive(ViewCoordinate viewCoordinate) throws IOException;

   /**
    * Retrieves tuples matching the specified view coordinate.
    *
    * @param viewCoordinate the view coordinate
    * @param cutoffTime the cutoff time
    * @return List of matching tuples
    * @throws IOException Signals that an I/O exception has occurred.
    */
   public Collection<? extends RefexVersionBI<?>> getRefsetMembersActive(ViewCoordinate viewCoordinate, Long cutoffTime)
           throws IOException;

   /**
    * Gets the descriptions.
    *
    * @return the descriptions
    * @throws IOException Signals that an I/O exception has occurred.
    */
   Collection<? extends DescriptionChronicleBI> getDescriptions() throws IOException;

   /**
    * Gets the last modification sequence.
    *
    * @return the last modification sequence
    */
   long getLastModificationSequence();

   /**
    * Gets the media.
    *
    * @return the media
    * @throws IOException Signals that an I/O exception has occurred.
    */
   Collection<? extends MediaChronicleBI> getMedia() throws IOException;

   /**
    * Gets the refset member for component.
    *
    * @param componentNid the component nid
    * @return the refset member for component
    * @throws IOException Signals that an I/O exception has occurred.
    */
   RefexChronicleBI<?> getRefsetMemberForComponent(int componentNid) throws IOException;
   
   /**
    * Returns refset members identified by this concept.
    *
    * @return the refset members
    * @throws IOException Signals that an I/O exception has occurred.
    */
   Collection<? extends RefexChronicleBI<?>> getRefsetMembers() throws IOException;

   /**
    * Gets the relationship groups.
    *
    * @param viewCoordinate the view coordinate
    * @return the relationship groups
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws ContradictionException the contradiction exception
    */
   Collection<? extends RelationshipGroupVersionBI> getRelationshipGroups(ViewCoordinate viewCoordinate)
           throws IOException, ContradictionException;

   /**
    * Gets the relationships target.
    *
    * @return the relationships target
    * @throws IOException Signals that an I/O exception has occurred.
    */
   Collection<? extends RelationshipChronicleBI> getRelationshipsTarget() throws IOException;

   /**
    * Gets the relationships source.
    *
    * @return the relationships source
    * @throws IOException Signals that an I/O exception has occurred.
    */
   Collection<? extends RelationshipChronicleBI> getRelationshipsSource() throws IOException;

   /**
    * Gets the versions in contradiction.
    *
    * @param viewCoordinate the view coordinate
    * @return the versions in contradiction
    */
   FoundContradictionVersions getVersionsInContradiction(ViewCoordinate viewCoordinate);
   
   /**
    * Gets the all nids for stamps.
    *
    * @param stampNids the stamp nids
    * @return the all nids for stamps
    * @throws IOException Signals that an I/O exception has occurred.
    */
   Set<Integer> getAllNidsForStamps(Set<Integer> stampNids) throws IOException;

   /**
    * Checks for refset member active for component.
    *
    * @param viewCoordinate the view coordinate
    * @param componentNid the component nid
    * @return true, if successful
    * @throws IOException Signals that an I/O exception has occurred.
    */
   boolean hasRefsetMemberActiveForComponent(ViewCoordinate viewCoordinate, int componentNid) throws IOException;

   /**
    * Checks if is annotation style refex.
    *
    * @return true, if is annotation style refex
    * @throws IOException Signals that an I/O exception has occurred.
    */
   boolean isAnnotationStyleRefex() throws IOException;
   
   /**
    * Checks if is annotation index.
    *
    * @return true, if is annotation index
    * @throws IOException Signals that an I/O exception has occurred.
    */
   boolean isAnnotationIndex() throws IOException;

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the annotation style refex.
    *
    * @param annotationSyleRefex the new annotation style refex
    */
   void setAnnotationStyleRefex(boolean annotationSyleRefex);
   
   /**
    * Sets the annotation index.
    *
    * @param annotationIndex the new annotation index
    * @throws IOException Signals that an I/O exception has occurred.
    */
   void setAnnotationIndex(boolean annotationIndex) throws IOException;
   
   /**
    * Gets the all nids.
    *
    * @return the all nids
    * @throws IOException Signals that an I/O exception has occurred.
    */
   Collection<Integer> getAllNids() throws IOException;
}
