package org.ihtsdo.tk.api;

import org.ihtsdo.tk.api.concept.ConceptVersionBI;

public interface TerminologyTransactionDI {

	void addUncommitted(ConceptVersionBI concept);
	
	void commit();
	void cancel();
	
	void commit(ConceptVersionBI concept);
	void cancel(ConceptVersionBI concept);

}
