package org.ihtsdo.workflow.refset.semHier;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetSearcher;



/* 
* @author Jesse Efron
* 
*/
public  class SemanticAreaHierarchyRefsetSearcher extends WorkflowRefsetSearcher 
{
	private static int currentStatusNid = 0;
	private static int fullySpecifiedTermDescriptionTypeNid = 0;
	
	public SemanticAreaHierarchyRefsetSearcher()
			throws TerminologyException, IOException 
	{
		refset = new SemanticAreaHierarchyRefset();
		
		currentStatusNid = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getNid();	
		fullySpecifiedTermDescriptionTypeNid = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()).getNid();	

		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId());
	}

	public String getConceptHierarchyTagFromEditorCategoryTags(I_GetConceptData con, Set<String> possibleTags) throws Exception {
		try {
			String semTag = getSemanticTag(con);
		
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
			
			return findFirstTag(semTag, newCandidates);
		}
	}
	
	private String getSemanticTag(I_GetConceptData con) throws IOException, TerminologyException {
    	
   		I_IntList descType = Terms.get().newIntList();
   		descType.add(fullySpecifiedTermDescriptionTypeNid);
   		I_DescriptionTuple tuple = con.getDescTuple(descType, Terms.get().getActiveAceFrameConfig());

		String s = tuple.getText();
		int startIndex = s.lastIndexOf('(');
		int endIndex = s.lastIndexOf(')');
		
		return s.substring(startIndex + 1, endIndex);
	}

	private Set<String> searchForParentTagBySemTag(String tag) throws Exception 
	{
		List<I_ExtendByRefPartStr> l = helper.getAllCurrentRefsetExtensions(refsetId, ((SemanticAreaHierarchyRefset)refset).getSemanticTagParentRelationship().getConceptNid());
		Set<String> results = new HashSet<String>();
		
		for (int i = 0; i < l.size(); i++)
		{
			I_ExtendByRefPartStr props = (I_ExtendByRefPartStr)l.get(i);
			
			String childTag =  ((SemanticAreaHierarchyRefset)refset).getChildSemanticTag(props.getStringValue());
			if (childTag.equalsIgnoreCase(tag))
			{
				String parentTag = ((SemanticAreaHierarchyRefset)refset).getParentSemanticTag(props.getStringValue());
				
				if (!parentTag.equals(childTag) &&
					!parentTag.equals("inactive_concept") &&
					!parentTag.equals("SNOMED_RT+CTV3"))
				{
					results.add(parentTag);					
				}
			}
		}
		
		return results;
	}
}
