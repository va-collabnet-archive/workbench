
package org.ihtsdo.workflow.refset.history;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.I_ConfigAceFrame.LANGUAGE_SORT_PREF;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.ComponentVersionBI;
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
		super (RefsetAuxiliary.Concept.WORKFLOW_HISTORY);
	}

	public Collection<UUID> getRefsetUids() throws TerminologyException, IOException {
		return RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getUids();
	}

	
	// Workflow ID Only a UUID (No Concept)
	public UUID getWorkflowId(String props) {
		return getUUID("workflowId", props);
	}

	// I_GetConceptData values where appropriate
	public I_GetConceptData getConcept(String props) throws NumberFormatException, TerminologyException, IOException {
		return getConcept("concept", props);
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
	public Long getWorkflowTime(String props) throws NumberFormatException, TerminologyException, IOException {
		return Long.parseLong(getProp("workflowTime", props));
	}
	public Long getEffectiveTime(String props) throws NumberFormatException, TerminologyException, IOException {
		return Long.parseLong(getProp("effectiveTime", props));
	}
	public UUID getConceptUid(String props) throws NumberFormatException, TerminologyException, IOException {
		return getUUID("concept", props);
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

	public static Comparator<ComponentVersionBI> createComponentTimestampComparer() {
		return new ComponentTimestampComparator();
	}

	public static Comparator<WorkflowHistoryJavaBean> createWfIdTimeStampComparer() {
		return new WfHxWfIdTimeStampComparer();
	}

	public static class ComponentTimestampComparator implements Comparator<ComponentVersionBI> { 
		public int compare(ComponentVersionBI a, ComponentVersionBI b) {
			if (a.getTime() > b.getTime())
				return 1;
			else
				return -1;
		}
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
				AceLog.getAppLog().log(Level.WARNING, "Couldn't Setup PreferredTermComparator", e);
			}
			
			return 0;
		}
	}


	
	private static class WfHxJavaBeanComparer implements Comparator<WorkflowHistoryJavaBean> {
		@Override
		public int compare(WorkflowHistoryJavaBean o1, WorkflowHistoryJavaBean o2) {
			if (o1.getConcept().compareTo(o2.getConcept()) != 0)
				return o1.getConcept().compareTo(o2.getConcept());
			else if (o1.getWorkflowTime().compareTo(o2.getWorkflowTime()) != 0)
				return o1.getWorkflowTime().compareTo(o2.getWorkflowTime());
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
				AceLog.getAppLog().log(Level.WARNING, "Failure in Compare Routine", e);
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
				return (o1.getWorkflowTime().compareTo(o2.getWorkflowTime()));
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