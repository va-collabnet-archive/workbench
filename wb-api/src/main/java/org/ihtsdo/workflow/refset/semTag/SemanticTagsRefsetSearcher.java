package org.ihtsdo.workflow.refset.semTag;

import java.io.IOException;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetSearcher;



/* 
* @author Jesse Efron
* 
*/
public  class SemanticTagsRefsetSearcher extends WorkflowRefsetSearcher 
{
	private SemanticTagsRefsetReader reader;

	public SemanticTagsRefsetSearcher()
			throws TerminologyException, IOException 
	{
		super(semanticTagConcept);
		reader  = new SemanticTagsRefsetReader();
	}

	public SortedSet<String> getAllSemanticTags() throws IOException, NumberFormatException, TerminologyException {
		SortedSet<String> retSet = new TreeSet<String>();
		Collection<? extends I_ExtendByRef> allTags = Terms.get().getRefsetExtensionMembers(refsetNid);
		
		for (I_ExtendByRef row : allTags) {
			String semTag = reader.getSemanticTag(((I_ExtendByRefPartStr)row).getStringValue());
		}
		
		return retSet;
	}
}
