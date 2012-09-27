package org.ihtsdo.workflow.refset.utilities;

import java.util.Comparator;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
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
 
	public Comparator<WorkflowHistoryJavaBean> createWfHxJavaBeanComparer() {
		return new WfHxJavaBeanComparer();
	}

	public Comparator<WorkflowHistoryJavaBean> createWfHxLatestFirstTimeComparer() {
		return new WfHxLatestFirstTimeComparer();
	}

	public Comparator<WorkflowHistoryJavaBean> createWfHxEarliestFirstTimeComparer() {
		return new WfHxEarliestFirstTimeComparer();
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

	
	// FSN
	public Comparator<ConceptVersionBI> createVersionedFsnComparer() {
		return new VersionedFsnComparator();
	}

	public Comparator<I_GetConceptData> createFsnComparer() {
		return new FsnComparator();
	}

	private class VersionedFsnComparator implements Comparator<ConceptVersionBI> { 
		
		public int compare(ConceptVersionBI a, ConceptVersionBI b) {
			try {
				return a.getDescriptionFullySpecified().getText().toLowerCase().compareTo(
						b.getDescriptionFullySpecified().getText().toLowerCase());
			} catch (Exception e) {
				AceLog.getAppLog().log(Level.WARNING, "Couldn't Setup FSN Comparator", e);
			}
			
			return 0;
		}
	}

	public class FsnComparator implements Comparator<I_GetConceptData> { 
		public int compare(I_GetConceptData a, I_GetConceptData b) {
			try {
				return WorkflowHelper.getFsn(a).toLowerCase().compareTo(
						WorkflowHelper.getFsn(b).toLowerCase());
			} catch (Exception e) {
				AceLog.getAppLog().log(Level.WARNING, "Couldn't Setup PreferredTerm Comparator", e);
			}
			
			return 0;
		}
	}

	
	// Pref Term
	public  Comparator<ConceptVersionBI> createVersionedPreferredTermComparer() {
		return new VersionedPreferredTermComparator();
	}

	public  Comparator<I_GetConceptData> createPreferredTermComparer() {
		return new PreferredTermComparator();
	}

	public class VersionedPreferredTermComparator implements Comparator<ConceptVersionBI> { 
		public int compare(ConceptVersionBI a, ConceptVersionBI b) {
			try {
				return a.getDescriptionPreferred().getText().toLowerCase().compareTo(
						b.getDescriptionPreferred().getText().toLowerCase());
			} catch (Exception e) {
				AceLog.getAppLog().log(Level.WARNING, "Couldn't Setup PreferredTerm Comparator", e);
			}
			
			return 0;
		}
	}
	
	public class PreferredTermComparator implements Comparator<I_GetConceptData> { 
		public int compare(I_GetConceptData a, I_GetConceptData b) {
			try {
				return WorkflowHelper.getPrefTerm(a).toLowerCase().compareTo(
						WorkflowHelper.getPrefTerm(b).toLowerCase());
			} catch (Exception e) {
				AceLog.getAppLog().log(Level.WARNING, "Couldn't Setup PreferredTerm Comparator", e);
			}
			
			return 0;
		}
	}
}