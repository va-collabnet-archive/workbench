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

public interface ConceptChronicleBI extends ComponentChronicleBI<ConceptVersionBI> {
   void cancel() throws IOException;

   boolean commit(ChangeSetGenerationPolicy changeSetGenerationPolicy,
                  ChangeSetGenerationThreadingPolicy changeSetGenerationThreadingPolicy)
           throws IOException;
   
   boolean commit(ChangeSetGenerationPolicy changeSetGenerationPolicy,
            ChangeSetGenerationThreadingPolicy changeSetGenerationThreadingPolicy,
            boolean writeAdjudication)
            throws IOException;

   /**
    * Returns a longer - more complete - string representation of the chronicle.
    * Useful for diagnostic purposes.
    *
    * @return
    */
   String toLongString();

   //~--- get methods ---------------------------------------------------------

   ConceptAttributeChronicleBI getConceptAttributes() throws IOException;

   RefexVersionBI<?> getRefsetMemberActiveForComponent(ViewCoordinate viewCoordinate, int componentNid)
           throws IOException;

   Collection<? extends RefexVersionBI<?>> getRefsetMembersActive(ViewCoordinate viewCoordinate) throws IOException;

   /**
     * Retrieves tuples matching the specified view coordinate
     * 
     * @param cuttoffTime
     *          cutoff time to match tuples, tuples with a time greater than
     *          cutoff will no be returned
     * @return List of matching tuples
     * @throws TerminologyException
     */
   public Collection<? extends RefexVersionBI<?>> getRefsetMembersActive(ViewCoordinate viewCoordinate, Long cutoffTime)
           throws IOException;

   Collection<? extends DescriptionChronicleBI> getDescriptions() throws IOException;

   long getLastModificationSequence();

   Collection<? extends MediaChronicleBI> getMedia() throws IOException;

   RefexChronicleBI<?> getRefsetMemberForComponent(int componentNid) throws IOException;
   /**
    * Returns refset members identified by this concept.
    * @return
    * @throws IOException 
    */
   Collection<? extends RefexChronicleBI<?>> getRefsetMembers() throws IOException;

   Collection<? extends RelationshipGroupVersionBI> getRelationshipGroups(ViewCoordinate viewCoordinate)
           throws IOException, ContradictionException;

   Collection<? extends RelationshipChronicleBI> getRelationshipsTarget() throws IOException;

   Collection<? extends RelationshipChronicleBI> getRelationshipsSource() throws IOException;

   FoundContradictionVersions getVersionsInContradiction(ViewCoordinate viewCoordinate);
   
   Set<Integer> getAllNidsForStamps(Set<Integer> stampNids) throws IOException;

   boolean hasRefsetMemberActiveForComponent(ViewCoordinate viewCoordinate, int componentNid) throws IOException;

   boolean isAnnotationStyleRefex() throws IOException;

   //~--- set methods ---------------------------------------------------------

   void setAnnotationStyleRefex(boolean annotationSyleRefex);
   
   Collection<Integer> getAllNids() throws IOException;
}
