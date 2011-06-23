package org.ihtsdo.workflow.refset.utilities;

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

	public Comparator<WorkflowHistoryJavaBean> createWfHxLatestFirstTimeComparer() {
		return new WfHxLatestFirstTimeComparer();
	}

	public Comparator<WorkflowHistoryJavaBean> createWfHxEarliestFirstTimeComparer() {
		return new WfHxEarliestFirstTimeComparer();
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

	private class WfHxEarliestFirstTimeComparer implements Comparator<WorkflowHistoryJavaBean> {
		@Override
		public int compare(WorkflowHistoryJavaBean o1, WorkflowHistoryJavaBean o2) {
			return o1.getWorkflowTime().compareTo(o2.getWorkflowTime());
		}
	}

	private class WfHxLatestFirstTimeComparer implements Comparator<WorkflowHistoryJavaBean> {
		@Override
		public int compare(WorkflowHistoryJavaBean o1, WorkflowHistoryJavaBean o2) {
			// Reverse List
			return o2.getWorkflowTime().compareTo(o1.getWorkflowTime());
		}
	}

	public  Comparator<I_GetConceptData> createPreferredTermComparer() {
		return new PreferredTermComparator();
	}

	public class PreferredTermComparator implements Comparator<I_GetConceptData> { 
		public int compare(I_GetConceptData a, I_GetConceptData b) {
			try {
				return WorkflowHelper.getPreferredTerm(a).compareTo(WorkflowHelper.getPreferredTerm(b));
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

	public Comparator<? super I_GetConceptData> createPreferredTermComparerNoCase() {
		return new PreferredTermComparatorNoCase();
	}

	// If no Pref Term found, use 
	public class PreferredTermComparatorNoCase implements Comparator<I_GetConceptData> { 
		public int compare(I_GetConceptData a, I_GetConceptData b) {
			try {
				String aStr = WorkflowHelper.getPreferredTerm(a).toLowerCase();
				if (aStr == null || aStr.length() == 0) {
					aStr = WorkflowHelper.getFsnTerm(a).toLowerCase();
				}
				
				String bStr = WorkflowHelper.getPreferredTerm(b).toLowerCase();
				if (bStr == null || aStr.length() == 0) {
					bStr = WorkflowHelper.getFsnTerm(b).toLowerCase();
				}

				return aStr.compareTo(bStr);
			} catch (Exception e) {
				AceLog.getAppLog().log(Level.WARNING, "Couldn't Setup PreferredTermComparator", e);
			}
			
			return 0;
		}
	}
}