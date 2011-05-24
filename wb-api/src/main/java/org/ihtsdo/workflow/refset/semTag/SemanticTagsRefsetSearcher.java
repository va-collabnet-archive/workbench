package org.ihtsdo.workflow.refset.semTag;

import java.io.IOException;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.semHier.SemanticAreaHierarchyRefset;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetSearcher;



/* 
* @author Jesse Efron
* 
*/
public  class SemanticTagsRefsetSearcher extends WorkflowRefsetSearcher 
{
	public SemanticTagsRefsetSearcher()
			throws TerminologyException, IOException 
	{
		refset = new SemanticTagsRefset();
		
		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId());
	}

	public SortedSet<String> getAllSemanticTags() throws IOException, NumberFormatException, TerminologyException {
		SortedSet<String> retSet = new TreeSet<String>();
		Collection<? extends I_ExtendByRef> allTags = Terms.get().getRefsetExtensionMembers(refsetId);
		
		for (I_ExtendByRef row : allTags) {
			String semTag = ((SemanticTagsRefset)refset).getSemanticTag(((I_ExtendByRefPartStr)row).getStringValue());
		}
		
		return retSet;
	}
}
