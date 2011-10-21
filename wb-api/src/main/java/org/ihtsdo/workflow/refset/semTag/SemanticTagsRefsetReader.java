package org.ihtsdo.workflow.refset.semTag;

import java.io.IOException;

import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetReader;



/*
* @author Jesse Efron
*
*/
public  class SemanticTagsRefsetReader extends WorkflowRefsetReader
{
	public SemanticTagsRefsetReader() throws TerminologyException, IOException
	{
		super(semanticTagConcept);
	}


	public String getSemanticTag(String props) throws NumberFormatException, TerminologyException, IOException {
		return getProp("semTag", props);
	}

	public String getSemanticTagUUID(String props) throws NumberFormatException, TerminologyException, IOException {
		return getProp("uuid", props);
	}
}
