package org.ihtsdo.workflow.refset.semHier;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetSearcher;



/* 
* @author Jesse Efron
* 
*/
public  class SemanticHierarchyRefsetSearcher extends WorkflowRefsetSearcher 
{
	private SemanticHierarchyRefsetReader reader = null;
	
	public SemanticHierarchyRefsetSearcher()
			throws TerminologyException, IOException 
	{
		super(semanticHierarchyConcept);
		reader = new SemanticHierarchyRefsetReader();
	}

	public String getConceptHierarchyTagFromEditorCategoryTags(ConceptVersionBI concept, Set<String> possibleTags) throws Exception {
		try {
			String semTag = WorkflowHelper.parseSemanticTag(concept);
		
			return findFirstTag(semTag, possibleTags);
		} catch (Exception e) {
			return null;
		}
	}

	private String findFirstTag(String semTag, Set<String> candidates) throws Exception {
		if (candidates.size() == 0)
			return "";
		else if (candidates.contains(semTag))
			return semTag;
		else
		{
			Set<String> newCandidates = new HashSet<String>();
			Set<String> parents = searchForParentTagBySemTag(semTag);

			for (String p : parents)
			{
				if (candidates.contains(p))
					return p;
				else
					newCandidates.add(p);
			}
			
			// If newCandidates is empty, then will return 
			return findFirstTag(semTag, newCandidates);
		}
	}
	
	private Set<String> searchForParentTagBySemTag(String tag) throws Exception 
	{
		List<? extends I_ExtendByRef> l = Terms.get().getRefsetExtensionsForComponent(refsetNid, reader.getRefCompConcept().getConceptNid());
		Set<String> results = new HashSet<String>();
		
		for (int i = 0; i < l.size(); i++)
		{
			I_ExtendByRefPartStr props = (I_ExtendByRefPartStr)l.get(i);
			
			String childTag =  reader.getChildSemanticTag(props.getStringValue());
			if (childTag.equalsIgnoreCase(tag))
			{
				String parentTag = reader.getParentSemanticTag(props.getStringValue());
				
				if (!parentTag.equals(childTag) &&
					!parentTag.equals("inactive concept") &&
					!parentTag.equals("SNOMED RT+CTV3"))
				{
					results.add(parentTag);					
				}
			}
		}
		
		return results;
	}

	public SortedSet<String> getAllSemanticTags() throws IOException {
		SortedSet<String> retSet = new TreeSet<String>();
		
		Collection<? extends I_ExtendByRef> rows = Terms.get().getRefsetExtensionMembers(refsetNid);
		
		for (I_ExtendByRef row : rows) {
			String childTag =  reader.getChildSemanticTag(((I_ExtendByRefPartStr)row).getStringValue());
			String parentTag = reader.getParentSemanticTag(((I_ExtendByRefPartStr)row).getStringValue());			

			retSet.add(childTag);
			retSet.add(parentTag);
		}

		return retSet;
	}
}
