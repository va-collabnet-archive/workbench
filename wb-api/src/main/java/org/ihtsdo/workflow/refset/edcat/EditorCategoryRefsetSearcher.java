package org.ihtsdo.workflow.refset.edcat;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.semHier.SemanticAreaHierarchyRefsetSearcher;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetSearcher;



/* 
* @author Jesse Efron
* 
*/
public  class EditorCategoryRefsetSearcher extends WorkflowRefsetSearcher 
{
	public EditorCategoryRefsetSearcher()
			throws TerminologyException, IOException 
	{
		refset = new EditorCategoryRefset();
		
		setRefsetName(refset.getRefsetName());
		setRefsetId(refset.getRefsetId());
	}
	
	public boolean isAutomaticApprovalAvailable(I_GetConceptData modeler) throws NumberFormatException, TerminologyException, IOException, Exception {
		// Get Editor Categories
		for (String prop : searchForEditorCategoryListByModeler(modeler))
		{
			I_GetConceptData val = ((EditorCategoryRefset)refset).getEditorCategory(prop);
			
			List<? extends I_RelTuple> relList = WorkflowHelper.getWorkflowRelationship(val, ArchitectonicAuxiliary.Concept.WORKFLOW_ROLE_VALUE);
			
			for (I_RelTuple rel : relList)
			{
				if (rel != null &&
					rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_AUTOMOTAIC_APPROVAL.getPrimoridalUid()).getConceptNid())
						return true;
			}
		}
		
		return false;
	}
	public I_GetConceptData searchForCategoryForConceptByModeler(I_GetConceptData modeler, I_GetConceptData con) throws Exception
	{
		// Get Editor Categories
		Set<String> currentModelerPropertySet = searchForEditorCategoryListByModeler(modeler);
		
		if (currentModelerPropertySet.isEmpty()) {
                    return null;
                } else if (currentModelerPropertySet.size() == 1) {
			// Only Category, must be all
			String role = ((EditorCategoryRefset)refset).getSemanticTag(currentModelerPropertySet.iterator().next());
			
			String defaultRoleString = "Clinical editor role All";
			if (!role.equalsIgnoreCase("all"))
				throw new Exception("Must be All Tag if only single result");
			else
				return WorkflowHelper.lookupRoles(defaultRoleString);
		}
		else
		{
			SemanticAreaHierarchyRefsetSearcher searcher = new SemanticAreaHierarchyRefsetSearcher();

			// Create map of tags-to-categories
			Map<String, I_GetConceptData> hierarchyToCategoryMap = getHierarchyToCategoryMap(currentModelerPropertySet); 
			
			// Find Tag
			String tag = searcher.getConceptHierarchyTagFromEditorCategoryTags(con, hierarchyToCategoryMap.keySet());
			
			// Find category
			return identifyModelerCategoryFromTag(modeler, tag);
		}
		
	}

	private I_GetConceptData identifyModelerCategoryFromTag(I_GetConceptData modeler, String tag) throws Exception {
		I_GetConceptData category = null;
		List<I_ExtendByRefPartStr> l = helper.getAllCurrentRefsetExtensions(refsetId, modeler.getNid());
		
		for (int i = 0; i < l.size(); i++)
		{
			I_ExtendByRefPartStr props = (I_ExtendByRefPartStr)l.get(i);
			String key = ((EditorCategoryRefset)refset).getSemanticTag(props.getStringValue());
			
			if (key.equalsIgnoreCase(tag))
			{
				return ((EditorCategoryRefset)refset).getEditorCategory(props.getStringValue());
			}
			else if (key.equals("all"))
			{
				category = ((EditorCategoryRefset)refset).getEditorCategory(props.getStringValue());
			}
		}

		return category;
	}
	private Map<String, I_GetConceptData> getHierarchyToCategoryMap(Set<String> set) throws Exception {
		Map<String, I_GetConceptData> results = new HashMap<String, I_GetConceptData>();
		
		for (String props : set) {
			String key = ((EditorCategoryRefset)refset).getSemanticTag(props);
			I_GetConceptData val = ((EditorCategoryRefset)refset).getEditorCategory(props);
			results.put(key, val);
		}
	
		if (!results.containsKey("all"))
			throw new Exception("Multiple Hierarchies must have \"ANY\" Category");
		
		return results;
	}
	
	private Set<String> searchForEditorCategoryListByModeler(I_GetConceptData modeler) throws Exception 
	{
      if (modeler == null) {
         return new HashSet<String>();
      }
		List<I_ExtendByRefPartStr> l = helper.getAllCurrentRefsetExtensions(refsetId, modeler.getNid());
		Set<String> results = new HashSet<String>();
		
		for (int i = 0; i < l.size(); i++)
		{
			I_ExtendByRefPartStr props = (I_ExtendByRefPartStr)l.get(i);
			results.add(props.getStringValue());
		}
		
		return results;
	}
}
