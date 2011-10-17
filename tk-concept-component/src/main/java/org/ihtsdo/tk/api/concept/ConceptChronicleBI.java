package org.ihtsdo.tk.api.concept;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;
import org.ihtsdo.tk.api.conattr.ConAttrChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;
import org.ihtsdo.tk.contradiction.FoundContradictionVersions;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;

public interface ConceptChronicleBI extends ComponentChroncileBI<ConceptVersionBI> {
   void cancel() throws IOException;

   boolean commit(ChangeSetGenerationPolicy changeSetPolicy,
                  ChangeSetGenerationThreadingPolicy changeSetWriterThreading)
           throws IOException;

   /**
    * Returns a longer - more complete - string representation of the chronicle.
    * Useful for diagnostic purposes.
    *
    * @return
    */
   String toLongString();

   //~--- get methods ---------------------------------------------------------

   ConAttrChronicleBI getConAttrs() throws IOException;

   RefexVersionBI<?> getCurrentRefsetMemberForComponent(ViewCoordinate vc, int componentNid)
           throws IOException;

   Collection<? extends RefexVersionBI<?>> getCurrentRefsetMembers(ViewCoordinate vc) throws IOException;

   /**
     * Retrieves tuples matching the specified view coordinate
     * 
     * @param cuttoffTime
     *          cutoff time to match tuples, tuples with a time greater than
     *          cutoff will no be returned
     * @return List of matching tuples
     * @throws TerminologyException
     */
   public Collection<? extends RefexVersionBI<?>> getCurrentRefsetMembers(ViewCoordinate vc, Long cutoffTime)
           throws IOException;

   Collection<? extends DescriptionChronicleBI> getDescs() throws IOException;

   long getLastModificationSequence();

   Collection<? extends MediaChronicleBI> getMedia() throws IOException;

   RefexChronicleBI<?> getRefsetMemberForComponent(int componentNid) throws IOException;

   Collection<? extends RefexChronicleBI<?>> getRefsetMembers() throws IOException;

   Collection<? extends RelGroupVersionBI> getRelGroups(ViewCoordinate vc)
           throws IOException, ContraditionException;

   Collection<? extends RelationshipChronicleBI> getRelsIncoming() throws IOException;

   Collection<? extends RelationshipChronicleBI> getRelsOutgoing() throws IOException;

   FoundContradictionVersions getVersionsInContradiction(ViewCoordinate vc);

   boolean hasCurrentRefsetMemberForComponent(ViewCoordinate vc, int componentNid) throws IOException;

   boolean isAnnotationStyleRefex() throws IOException;

   //~--- set methods ---------------------------------------------------------

   void setAnnotationStyleRefex(boolean annotationSyleRefex);
}
