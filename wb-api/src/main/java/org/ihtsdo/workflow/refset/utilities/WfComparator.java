package org.ihtsdo.workflow.refset.utilities;

import java.io.IOException;
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
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;

public class WfComparator 
{
	private I_IntList descTypeList = null;
	private I_IntSet statusSet = null;
	private PositionSetReadOnly viewPos = null;

    private static final WfComparator INSTANCE = new WfComparator();

    private WfComparator() {
    }
 
    public static WfComparator getInstance() {
        return INSTANCE;
    }
 

	public Comparator<I_GetConceptData> createFsnComparer() {
		return new WorkflowFsnComparator();
	}

	public Comparator<WorkflowHistoryJavaBean> createWfHxJavaBeanComparer() {
		return new WfHxJavaBeanComparer();
	}

	public Comparator<I_GetConceptData> createWfHxConceptComparer() {
		return new WfComparator.WfHxConceptComparer();
	}

	
	
	private class WorkflowFsnComparator implements Comparator<I_GetConceptData> { 
		public int compare(I_GetConceptData a, I_GetConceptData b) {
			return WorkflowHelper.identifyFSN(a).compareTo(WorkflowHelper.identifyFSN(b));
		}
	}

	private class WfHxJavaBeanComparer implements Comparator<WorkflowHistoryJavaBean> {
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

	private class WfHxConceptComparer implements Comparator<I_GetConceptData> {
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

	public Comparator<WorkflowHistoryJavaBean> createWfHxFsnJavaBeanComparer() {
		return new WfHxFsnJavaBeanComparer();
	}

	public  Comparator<I_GetConceptData> createPreferredTermComparer() {
		return new PreferredTermComparator();
	}

	public Comparator<WorkflowHistoryJavaBean> createConceptWorkflowTimestampComparer() {
		return new ConceptWorkflowTimestampComparer();
	}

	public Comparator<String> createComponentStringTimestampComparer() {
		return new ComponentStringTimestampComparator();
	}

	public Comparator<WorkflowHistoryJavaBean> createTimestampComparer() {
		return new TimestampComparer();
	}

	public class ComponentStringTimestampComparator implements Comparator<String> { 
		public int compare(String a, String b) {
			// Reverse List
			return b.compareTo(a);
		}
	}

	public class PreferredTermComparator implements Comparator<I_GetConceptData> { 
		public int compare(I_GetConceptData a, I_GetConceptData b) {
			try {
				return getPreferredTerm(a).compareTo(getPreferredTerm(b));
			} catch (Exception e) {
				AceLog.getAppLog().log(Level.WARNING, "Couldn't Setup PreferredTermComparator", e);
			}
			
			return 0;
		}
	}
	
	public class WfHxFsnJavaBeanComparer implements Comparator<WorkflowHistoryJavaBean> {
		@Override
		public int compare(WorkflowHistoryJavaBean o1, WorkflowHistoryJavaBean o2) {
			return (o1.getFSN().compareTo(o2.getFSN()));
		}
	}
	
	public class ConceptWorkflowTimestampComparer implements Comparator<WorkflowHistoryJavaBean> {
		@Override
		public int compare(WorkflowHistoryJavaBean o1, WorkflowHistoryJavaBean o2) {
			if (o1.getConcept().compareTo(o2.getConcept()) != 0)
				return o1.getConcept().compareTo(o2.getConcept());
			else
			{
				if (o1.getWorkflowId().compareTo(o2.getWorkflowId()) != 0)
					return (o1.getWorkflowId().compareTo(o2.getWorkflowId()));
				else
					return (o1.getWorkflowTime().compareTo(o2.getWorkflowTime()));
			}
		}
	}

	public class TimestampComparer implements Comparator<WorkflowHistoryJavaBean> {
		@Override
		public int compare(WorkflowHistoryJavaBean o1, WorkflowHistoryJavaBean o2) {
			return (o2.getWorkflowTime().compareTo(o1.getWorkflowTime()));
		}
	}

	private String getPreferredTerm(I_GetConceptData conceptData) throws Exception {
        descTypeList = Terms.get().newIntList();
        descTypeList.add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());

        statusSet = Terms.get().newIntSet();
        statusSet.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
        statusSet.add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
        statusSet.add(ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.localize().getNid());
        statusSet.add(ArchitectonicAuxiliary.Concept.READY_TO_PROMOTE.localize().getNid());
        statusSet.add(ArchitectonicAuxiliary.Concept.PROMOTED.localize().getNid());

        viewPos = Terms.get().getActiveAceFrameConfig().getViewPositionSetReadOnly();

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