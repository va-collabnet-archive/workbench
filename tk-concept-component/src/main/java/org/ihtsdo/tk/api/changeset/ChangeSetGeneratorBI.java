package org.ihtsdo.tk.api.changeset;

import java.io.IOException;

import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

public interface ChangeSetGeneratorBI {

    public void open(NidSetBI commitSapNids) throws IOException;

    public void writeChanges(ConceptChronicleBI concept, long time) throws IOException;
    
    public void setPolicy(ChangeSetGenerationPolicy policy);

    public void commit() throws IOException;

}
