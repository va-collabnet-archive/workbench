package org.ihtsdo.concept;

public interface I_ProcessConceptData {

	public boolean continueWork() throws Exception;

	public void processConceptData(Concept concept) throws Exception;

}