package org.ihtsdo.workflow.refset.edcat;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.WorkflowRefset;
import org.ihtsdo.workflow.refset.semHier.SemanticHierarchyRefsetSearcher;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetSearcher;



/*
* @author Jesse Efron
*
*/
public  class EditorCategoryRefsetSearcher extends WorkflowRefsetSearcher
{
	EditorCategoryRefsetReader reader = null;
	
	public EditorCategoryRefsetSearcher()
			throws TerminologyException, IOException
	{
		super(editorCategoryConcept);
		reader = new EditorCategoryRefsetReader();
	}

	public boolean isAutomaticApprovalAvailable(I_GetConceptData modeler) throws NumberFormatException, TerminologyException, IOException, Exception {
		// Get Editor Categories
		for (String prop : searchForEditorCategoryListByModeler(modeler))
		{
			I_GetConceptData val = reader.getEditorCategory(prop);

			List<I_RelVersioned> relList = WorkflowHelper.getWorkflowRelationship(val, ArchitectonicAuxiliary.Concept.WORKFLOW_ROLE_VALUE);

			for (I_RelVersioned rel : relList)
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
			String editorCategory = reader.getEditorCategory(currentModelerPropertySet.iterator().next()).getInitialText();
			return WorkflowHelper.lookupRoles(editorCategory);
		}
		else
		{
			SemanticHierarchyRefsetSearcher searcher = new SemanticHierarchyRefsetSearcher();

			// Transform editor category list into a map of tags-to-categories
			Map<String, I_GetConceptData> hierarchyToCategoryMap = getHierarchyToCategoryMap(currentModelerPropertySet);

			// Find Tag
			String tag = searcher.getConceptHierarchyTagFromEditorCategoryTags(con, hierarchyToCategoryMap.keySet());

			// Find category
			return identifyModelerCategoryFromTag(hierarchyToCategoryMap, tag);
		}

	}

	private I_GetConceptData identifyModelerCategoryFromTag(Map<String, I_GetConceptData> hierarchyToCategoryMap, String tag) throws Exception {
		I_GetConceptData category = null;


		for (String key : hierarchyToCategoryMap.keySet()) {
			if (key.equalsIgnoreCase(tag))
			{
				return hierarchyToCategoryMap.get(key);
			}
			else if (key.startsWith("SNOMED CT Concept") || key.equalsIgnoreCase("all"))
			{
				category = hierarchyToCategoryMap.get(key);
			}
		}

		return category;
	}
	private Map<String, I_GetConceptData> getHierarchyToCategoryMap(Set<String> set) throws Exception {
		Map<String, I_GetConceptData> results = new HashMap<String, I_GetConceptData>();

		for (String props : set) {
			String key = reader.getSemanticTag(props);
			I_GetConceptData val = reader.getEditorCategory(props);
			results.put(key, val);
		}

		return results;
	}

	private Set<String> searchForEditorCategoryListByModeler(I_GetConceptData modeler) throws Exception
	{
      if (modeler == null) {
         return new HashSet<String>();
      }
		List<? extends I_ExtendByRef> l = Terms.get().getRefsetExtensionsForComponent(refsetNid, modeler.getNid());
		Set<String> results = new HashSet<String>();

		for (int i = 0; i < l.size(); i++)
		{
			I_ExtendByRefPartStr props = (I_ExtendByRefPartStr)l.get(i);
			results.add(props.getStringValue());
		}

		return results;
	}
}
