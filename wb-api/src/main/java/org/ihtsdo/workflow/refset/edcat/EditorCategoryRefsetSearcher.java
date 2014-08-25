package org.ihtsdo.workflow.refset.edcat;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
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
	private static int activeRf1 = 0;
	private static int activeRf2 = 0;

	public EditorCategoryRefsetSearcher()
			throws TerminologyException, IOException
	{
		super(editorCategoryConcept);
		reader = new EditorCategoryRefsetReader();
		
		activeRf1 = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
		activeRf2 = Terms.get().uuidToNative(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()[0]);
	}

	public boolean isAutomaticApprovalAvailable(ConceptVersionBI modeler, ViewCoordinate vc) throws NumberFormatException, TerminologyException, IOException, Exception {
		// Get Editor Categories
		for (String prop : searchForEditorCategoryListByModeler(modeler))
		{
			I_GetConceptData val = reader.getEditorCategory(prop);

			List<RelationshipVersionBI<?>> relList = WorkflowHelper.getWorkflowRelationship(val.getVersion(vc), ArchitectonicAuxiliary.Concept.WORKFLOW_ROLE_VALUE);

			for (RelationshipVersionBI<?> rel : relList)
			{
				if (rel != null &&
					rel.getTargetNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_AUTOMOTAIC_APPROVAL.getPrimoridalUid()).getConceptNid())
						return true;
			}
		}

		return false;
	}
	public ConceptVersionBI searchForCategoryForConceptByModeler(ConceptVersionBI modeler, ConceptVersionBI concept, ViewCoordinate vc) throws Exception
	{
		// Get Editor Categories
		Set<String> currentModelerPropertySet = searchForEditorCategoryListByModeler(modeler);

		if (currentModelerPropertySet.isEmpty()) {
            return null;
        } else if (currentModelerPropertySet.size() == 1) {
			String editorCategory = WorkflowHelper.identifyPrefTerm(reader.getEditorCategory(currentModelerPropertySet.iterator().next()).getConceptNid(), vc);
			return WorkflowHelper.lookupRoles(editorCategory, vc);
		}
		else
		{
			SemanticHierarchyRefsetSearcher searcher = new SemanticHierarchyRefsetSearcher();

			// Transform editor category list into a map of tags-to-categories
			Map<String, ConceptVersionBI> hierarchyToCategoryMap = getHierarchyToCategoryMap(currentModelerPropertySet, vc);

			// Find Tag
			String tag = searcher.getConceptHierarchyTagFromEditorCategoryTags(concept, hierarchyToCategoryMap.keySet());

			// Find category
			return identifyModelerCategoryFromTag(hierarchyToCategoryMap, tag);
		}

	}

	private ConceptVersionBI identifyModelerCategoryFromTag(Map<String, ConceptVersionBI> hierarchyToCategoryMap, String tag) throws Exception {
		ConceptVersionBI category = null;


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
	private Map<String, ConceptVersionBI> getHierarchyToCategoryMap(Set<String> set, ViewCoordinate vc) throws Exception {
		Map<String, ConceptVersionBI> results = new HashMap<String, ConceptVersionBI>();

		for (String props : set) {
			String key = reader.getSemanticTag(props);
			I_GetConceptData val = reader.getEditorCategory(props);
			results.put(key, val.getVersion(vc));
		}

		return results;
	}

	private Set<String> searchForEditorCategoryListByModeler(ConceptVersionBI modeler) throws Exception
	{
	    if (modeler == null) {
	       return new HashSet<String>();
	    }
		List<? extends I_ExtendByRef> l = Terms.get().getRefsetExtensionsForComponent(refsetNid, modeler.getNid());
		Set<String> results = new HashSet<String>();

		for (int i = 0; i < l.size(); i++)
		{
			I_ExtendByRefPartStr<?> props = (I_ExtendByRefPartStr<?>)l.get(i);
			results.add(props.getString1Value());
		}

		return results;
	}
	
	public ConceptVersionBI searchForCategoryByModelerAndTag(ConceptVersionBI modeler, String tag, ViewCoordinate vc) throws Exception {
		List<? extends I_ExtendByRef> allMembers = Terms.get().getRefsetExtensionsForComponent(refsetNid, modeler.getNid());

		for (int i = 0; i < allMembers.size(); i++)
		{
			// Identify latest version of extension
            I_ExtendByRefPart latestPart = null;
            for (I_ExtendByRefPart part : allMembers.get(i).getMutableParts()) {
                if ((latestPart == null) || (part.getTime() >= latestPart.getTime())) {
                    latestPart = part;
                }
            }

			I_ExtendByRefPartStr<?> version = (I_ExtendByRefPartStr<?>)latestPart;

			// If has same modeler & semantic tag combination already in refset, ensure is active.  
			// If so, return existing category
			if (reader.getSemanticTag(version.getString1Value()).equalsIgnoreCase(tag)) {
				if (version.getStatusNid() == activeRf1 || version.getStatusNid() == activeRf2) {
					return reader.getEditorCategory(version.getString1Value()).getVersion(vc);
				}
			}
		}
		
		return null;
	}
}
