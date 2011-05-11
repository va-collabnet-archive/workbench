package org.ihtsdo.contradiction;

import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import javax.swing.JList;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.workflow.refset.utilities.WfComparator;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

public class ContradictionReporter {

	public void populateConflictsInListPanel(Set<Integer> nids)
	{
		try 
		{
			JList conceptList = Terms.get().getActiveAceFrameConfig().getBatchConceptList();
	        I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
	        model.clear();

	        TreeSet<I_GetConceptData> sortedConcepts = new TreeSet<I_GetConceptData>(WfComparator.getInstance().createFsnComparer());
	        for (Integer nid : nids)
	        {
	        	I_GetConceptData concept = Terms.get().getConcept(nid);
	        	sortedConcepts.add(concept);
	        }
	        
	        for (I_GetConceptData con : sortedConcepts)
	        {
	        	model.addElement(con);
	        }
        	
        	Terms.get().getActiveAceFrameConfig().showListView();
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.SEVERE, "Error in populating contradiction detection results in the list panel");
		}
	}
	public void printSingleConcepts(TreeSet<I_GetConceptData> singleConcepts) {
		System.out.println("\n\n*********Singles with count of: " + singleConcepts.size() + "*********");

		for (I_GetConceptData c : singleConcepts) {
			System.out.println(WorkflowHelper.identifyFSN(c));
	}
		System.out.println("*********End Of Singles*********\n\n\n\n");
	}
	
	public void printConflictConcepts(TreeSet<I_GetConceptData> conflictingConcepts) {
		System.out.println("\n\n*********Conflicts with count of: " + conflictingConcepts.size() + "*********");

		for (I_GetConceptData c : conflictingConcepts) {
			System.out.println(WorkflowHelper.identifyFSN(c));
		}
		System.out.println("*********End Of Conflicts*********\n\n\n\n");
	}

	public void printDuplicateNewConcepts(TreeSet<I_GetConceptData> conflictingDuplicateNewConcepts) {
		System.out.println("\n\n*********Conflictings With Same Values for different CompId with count of: " + conflictingDuplicateNewConcepts.size() + "*********");

		for (I_GetConceptData c : conflictingDuplicateNewConcepts) {
			System.out.println(WorkflowHelper.identifyFSN(c));
		}
		System.out.println("*********End Of Conflicts*********\n\n\n\n");
	}

	public void printDuplicateEditConcepts(TreeSet<I_GetConceptData> conflictingDuplicateEditConcepts) {
		System.out.println("\n\n*********Conflictings With Same Values for same CompIdwith count of: " + conflictingDuplicateEditConcepts.size() + "*********");

		for (I_GetConceptData c : conflictingDuplicateEditConcepts) {
			System.out.println(WorkflowHelper.identifyFSN(c));
		}
		System.out.println("*********End Of Conflicts*********\n\n\n\n");
	}

}
