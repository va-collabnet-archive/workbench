package org.ihtsdo.tk.api;

import java.io.File;
import java.io.IOException;

import org.ihtsdo.tk.api.amend.TerminologyAmendmentBI;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public interface TerminologyTransactionDI {

	void addUncommitted(ConceptChronicleBI cc) throws IOException;
	void addUncommitted(ConceptVersionBI cv) throws IOException;
	
	void commit() throws IOException;
	void cancel() throws IOException;
	
	void commit(ConceptChronicleBI cc) throws IOException;
	void cancel(ConceptChronicleBI cc) throws IOException;

	void commit(ConceptVersionBI cv) throws IOException;
	void cancel(ConceptVersionBI cv) throws IOException;

    void addChangeSetGenerator(String key, ChangeSetGeneratorBI writer);
    void removeChangeSetGenerator(String key);
   
    ChangeSetGeneratorBI createDtoChangeSetGenerator(File changeSetFileName, 
   			File changeSetTempFileName, 
   			ChangeSetGenerationPolicy policy);
   
}
