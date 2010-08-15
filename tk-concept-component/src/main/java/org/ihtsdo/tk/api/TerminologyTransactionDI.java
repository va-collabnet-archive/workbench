package org.ihtsdo.tk.api;

import java.io.IOException;

import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;

public interface TerminologyTransactionDI {

	void addUncommitted(ConceptChronicleBI cc) throws IOException;
	void addUncommitted(ConceptVersionBI cv) throws IOException;
	
	void commit() throws IOException;
	void cancel() throws IOException;
	
	void commit(ConceptChronicleBI cc) throws IOException;
	void cancel(ConceptChronicleBI cc) throws IOException;

	void commit(ConceptVersionBI cv) throws IOException;
	void cancel(ConceptVersionBI cv) throws IOException;

}
