
package org.ihtsdo.workflow.refset.history;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.I_ConfigAceFrame.LANGUAGE_SORT_PREF;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.WorkflowRefset;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;



/* 
* @author Jesse Efron
* 
*/
public class WorkflowHistoryRefset extends WorkflowRefset  {
	private static I_IntList descTypeList = null;
	private static I_IntSet statusSet = null;
	private static PositionSetReadOnly viewPos = null;
	
	public WorkflowHistoryRefset() throws IOException, TerminologyException {
		super (RefsetAuxiliary.Concept.WORKFLOW_HISTORY.localize().getNid(),
			RefsetAuxiliary.Concept.WORKFLOW_HISTORY.toString());
		}

	public Collection<UUID> getRefsetUids() throws TerminologyException, IOException {
		// TODO Auto-generated method stub
		return RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getUids();
	}

	// Workflow ID Only a UUID (No Concept)
	public UUID getWorkflowId(String props) {
		return getUUID("workflowId", props);
	}


	// I_GetConceptData values where appropriate
	public I_GetConceptData getUseCase(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("useCase", props);
	}
	public I_GetConceptData getState(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("state", props);
	}
	public I_GetConceptData getAction(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("action", props);
	}
	public I_GetConceptData getPath(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("path", props);
	}
	public I_GetConceptData getModeler(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("modeler", props);
	}
	public String getFSN(String props) throws NumberFormatException, TerminologyException, IOException {
		return getProp("fsn", props);
	}
	public Long getRefsetColumnTimeStamp(String props) throws NumberFormatException, TerminologyException, IOException {
		return Long.parseLong(getProp("refsetColumnTimeStamp", props));
	}
	public Long getTimeStamp(String props) throws NumberFormatException, TerminologyException, IOException {
		return Long.parseLong(getProp("timeStamp", props));
	}
	
	// UUID values where appropriate
	public UUID getUseCaseUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("useCase", props);
	}
	public UUID getStateUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("state", props);
	}
	public UUID getActionUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("action", props);
	}
	public UUID getPathUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("path", props);
	}
	public UUID getModelerUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("modeler", props);
	}
	
	public boolean getAutoApproved(String props) throws NumberFormatException, TerminologyException, IOException {
		
		try {

			String key = "autoApproved";
			String prop = getProp(key, props);
		
			if (prop.equals("true"))
				return true;
			else 
				return false;

		} catch (IndexOutOfBoundsException ioob) {
			return false;
		}
	}
	
	public boolean getOverridden(String props) throws NumberFormatException, TerminologyException, IOException {
		
		try {

			String key = "overridden";
			String prop = getProp(key, props);
		
			if (prop.equals("true"))
				return true;
			else 
				return false;

		} catch (IndexOutOfBoundsException ioob) {
			return false;
		}
	}
	

	public static Comparator<WorkflowHistoryJavaBean> createWfHxJavaBeanComparer() {
		return new WfHxJavaBeanComparer();
	}
	

	
	public static Comparator<I_GetConceptData> createWfHxConceptComparer() {
		return new WfHxConceptComparer();
	}

	public static Comparator<WorkflowHistoryJavaBean> createWfHxFsnJavaBeanComparer() {
		return new WfHxFsnJavaBeanComparer();
	}

	public static Comparator<I_GetConceptData> createWorkflowFsnComparer() {
		return new WorkflowFsnComparator();
	}

	public static Comparator<I_GetConceptData> createPreferredTermComparer() {
		return new PreferredTermComparator();
	}
	
	public static Comparator<WorkflowHistoryJavaBean> createWfIdTimeStampComparer() {
		return new WfHxWfIdTimeStampComparer();
	}

	public static class WorkflowFsnComparator implements Comparator<I_GetConceptData> { 
		public int compare(I_GetConceptData a, I_GetConceptData b) {
			return WorkflowHelper.identifyFSN(a).compareTo(WorkflowHelper.identifyFSN(b));
		}
	}

	private static class PreferredTermComparator implements Comparator<I_GetConceptData> { 
		public int compare(I_GetConceptData a, I_GetConceptData b) {
			try {
				return getPreferredTerm(a).compareTo(getPreferredTerm(b));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return 0;
		}
	}


	
	private static class WfHxJavaBeanComparer implements Comparator<WorkflowHistoryJavaBean> {
		@Override
		public int compare(WorkflowHistoryJavaBean o1, WorkflowHistoryJavaBean o2) {
			
//			if (o1.equals(o2))
//				try {
//					System.out.println("O1: " + o1);
//					System.out.println("\n\n\n02: " + o2);
//					throw new Exception("Duplicate WF Histories Exist");
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			
			if (o2.getConceptId().compareTo(o1.getConceptId()) != 0)
				return o2.getConceptId().compareTo(o1.getConceptId());
			//else if (o2.getWorkflowId().compareTo(o1.getWorkflowId()) != 0)
			//	return o2.getWorkflowId().compareTo(o1.getWorkflowId());
			else if (o2.getRefsetColumnTimeStamp().compareTo(o1.getRefsetColumnTimeStamp()) != 0)
				return o2.getRefsetColumnTimeStamp().compareTo(o1.getRefsetColumnTimeStamp());
			else 
				return -1;
		}
	}
	 

	private static class WfHxFsnJavaBeanComparer implements Comparator<WorkflowHistoryJavaBean> {
		@Override
		public int compare(WorkflowHistoryJavaBean o1, WorkflowHistoryJavaBean o2) {
			return (o1.getFSN().compareTo(o2.getFSN()));
		}
	}
	
			
			
	 

	private static class WfHxConceptComparer implements Comparator<I_GetConceptData> {
		@Override
		public int compare(I_GetConceptData o1, I_GetConceptData o2) {
			try {
				return (o1.getInitialText().toLowerCase().compareTo(o2.getInitialText().toLowerCase()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return -1;
		}
	}

	private static class WfHxWfIdTimeStampComparer implements Comparator<WorkflowHistoryJavaBean> {
		@Override
		public int compare(WorkflowHistoryJavaBean o1, WorkflowHistoryJavaBean o2) {
			if (o1.getWorkflowId().compareTo(o2.getWorkflowId()) != 0)
				return (o1.getWorkflowId().compareTo(o2.getWorkflowId()));
			else
				return (o1.getRefsetColumnTimeStamp().compareTo(o2.getRefsetColumnTimeStamp()));
		}
	}

	private static String getPreferredTerm(I_GetConceptData conceptData) throws Exception {
		
		if (descTypeList == null)
		{
	        descTypeList = Terms.get().newIntList();
	        descTypeList.add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
		}
		
		if (statusSet == null)
		{
	        statusSet = Terms.get().newIntSet();
	        statusSet.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
	        statusSet.add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
	        statusSet.add(ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.localize().getNid());
	        statusSet.add(ArchitectonicAuxiliary.Concept.READY_TO_PROMOTE.localize().getNid());
	        statusSet.add(ArchitectonicAuxiliary.Concept.PROMOTED.localize().getNid());
		}
		
		if (statusSet == null)
		{
	        viewPos = Terms.get().getActiveAceFrameConfig().getViewPositionSetReadOnly();
		}
		
        I_DescriptionTuple descTuple =
                conceptData.getDescTuple(descTypeList, null, statusSet, viewPos,
                    LANGUAGE_SORT_PREF.TYPE_B4_LANG, null, null);
        
        if (descTuple == null) {
            UUID conceptUuid = conceptData.getUids().iterator().next();
            throw new TerminologyException("Unable to obtain preferred term for concept " + conceptUuid.toString());
        }

        return descTuple.getText();
    }


}