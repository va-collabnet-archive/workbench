package org.ihtsdo.workflow.refset.semhier;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.utilities.RefsetSearcherUtility;



/* 
* @author Jesse Efron
* 
*/
public  class SemanticAreaHierarchyRefsetSearcher extends RefsetSearcherUtility 
{
	public SemanticAreaHierarchyRefsetSearcher()
			throws TerminologyException, IOException 
	{
		refset = new SemanticAreaHierarchyRefset();
		
		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId());
	}

	public String searchForMostDetailedTags(I_GetConceptData con, Set<String> possibleTags) throws Exception {
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
    	Collection<I_DescriptionVersioned> c = (Collection<I_DescriptionVersioned>) con.getDescriptions();
    	
    	Iterator<I_DescriptionVersioned> itr = c.iterator();
   		
   		while (itr.hasNext())
   		{
   	   		I_TermFactory tf = Terms.get();
   	   		I_DescriptionVersioned v = (I_DescriptionVersioned)itr.next();

   	   		for (I_DescriptionTuple tuple : v.getTuples())
	   		{
	   			if (tuple.getTypeId() == tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()).getNid())
   				{
	   				String s = tuple.getText();
	   				int startIndex = s.lastIndexOf('(');
	   				int endIndex = s.lastIndexOf(')');
	   				
	   				if (startIndex < 0 || endIndex < 0)
		   				System.out.println("S = " + s);
	   				else
	   					return s.substring(startIndex + 1, endIndex);
   				}
	   		}
   		}

   		return "";
	}

	public Set<String> searchForParentTagBySemTag(String tag) throws Exception 
	{
		List<I_ExtendByRefPartStr> l = helper.getAllCurrentRefsetExtensions(refsetId, ((SemanticAreaHierarchyRefset)refset).getSemanticTagParentRelationship().getConceptId());
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
