package org.ihtsdo.tk.api.changeset;

import java.io.IOException;

import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

public interface ChangeSetGeneratorBI {

    public void open(NidSetBI commitStampNids) throws IOException;

    public void writeChanges(ConceptChronicleBI conceptChronicle, long time) throws IOException;
    
    public void setPolicy(ChangeSetGenerationPolicy changeSetGenerationPolicy);

    public void commit() throws IOException;

}
