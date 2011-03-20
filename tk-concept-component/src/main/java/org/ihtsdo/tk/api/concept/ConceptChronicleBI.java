package org.ihtsdo.tk.api.concept;

import java.io.IOException;
import java.util.Collection;

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
import org.ihtsdo.tk.api.relationship.group.RelGroupChronicleBI;

public interface ConceptChronicleBI extends
        ComponentChroncileBI<ConceptVersionBI> {

    ConAttrChronicleBI getConAttrs() throws IOException;

    Collection<? extends DescriptionChronicleBI> getDescs() throws IOException;

    Collection<? extends RelationshipChronicleBI> getRelsOutgoing() throws IOException;

    Collection<? extends RelationshipChronicleBI> getRelsIncoming() throws IOException;

    Collection<? extends MediaChronicleBI> getMedia() throws IOException;

    Collection<? extends RelGroupChronicleBI> getRelGroups() throws IOException, ContraditionException;

    boolean isAnnotationStyleRefex() throws IOException;

    void setAnnotationStyleRefex(boolean annotationSyleRefex);

    Collection<? extends RefexChronicleBI<?>> getRefsetMembers()
            throws IOException;

    Collection<? extends RefexVersionBI<?>> getCurrentRefsetMembers(ViewCoordinate vc)
            throws IOException;

    void commit(ChangeSetGenerationPolicy changeSetPolicy,
            ChangeSetGenerationThreadingPolicy changeSetWriterThreading) throws IOException;

    void cancel() throws IOException;
    
    ConceptVersionBI getVersion(ViewCoordinate vc);
    
   /**
    * Returns a longer - more complete - string representation of the chronicle. 
    * Useful for diagnostic purposes.
    *
    * @return
    */
   public String toLongString();

}
