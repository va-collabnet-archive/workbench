package org.ihtsdo.workflow.refset.utilities;

import java.util.Comparator;
import java.util.logging.Level;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;

public class WfComparator 
{
    private static final WfComparator INSTANCE = new WfComparator();

    private WfComparator() {
    }
 
    public static WfComparator getInstance() {
        return INSTANCE;
    }
 

	public Comparator<ConceptVersionBI> createFsnComparer() {
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

	private class WorkflowFsnComparator implements Comparator<ConceptVersionBI> { 
		
		public int compare(ConceptVersionBI a, ConceptVersionBI b) {
			try {
				return a.getFullySpecifiedDescription().getText().toLowerCase().compareTo(
						b.getFullySpecifiedDescription().getText().toLowerCase());
			} catch (Exception e) {
				AceLog.getAppLog().log(Level.WARNING, "Couldn't Setup FSN Comparator", e);
			}
			
			return 0;
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

	public  Comparator<ConceptVersionBI> createPreferredTermComparer() {
		return new PreferredTermComparator();
	}

	public class PreferredTermComparator implements Comparator<ConceptVersionBI> { 
		public int compare(ConceptVersionBI a, ConceptVersionBI b) {
			try {
				return a.getPreferredDescription().getText().toLowerCase().compareTo(
						b.getPreferredDescription().getText().toLowerCase());
			} catch (Exception e) {
				AceLog.getAppLog().log(Level.WARNING, "Couldn't Setup PreferredTerm Comparator", e);
			}
			
			return 0;
		}
	}
}