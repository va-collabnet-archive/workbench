package org.ihtsdo.workflow; 

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.utilities.WfComparator;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetSearcher;


/* 
* @author Jesse Efron
* 
*/
public class WorkflowHistoryRefsetSearcher extends WorkflowRefsetSearcher {
    private static SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private int adjudicationPath = 0;
	public WorkflowHistoryRefsetSearcher() throws TerminologyException, IOException
	{
		super(workflowHistoryConcept);
		adjudicationPath = Terms.get().uuidToNative(UUID.fromString("7dfa494a-abde-5bc0-b1e3-2563519130a2"));
	}

	public boolean isInitialized() {
		return refsetConcept != null;
	}

	public WorkflowHistoryJavaBean getLatestBeanForWorkflowId(int conceptNid, UUID workflowId) {
		long currentTime = 0;
		WorkflowHistoryJavaBean lastBean = null;
		
		try {
			I_GetConceptData con = Terms.get().getConcept(conceptNid);
			TreeSet<WorkflowHistoryJavaBean> beans = WorkflowHelper.getWfHxMembersAsBeans(con);
			for (WorkflowHistoryJavaBean bean : beans ) {
				if (bean.getWorkflowId().equals(workflowId) &&
					currentTime < bean.getWorkflowTime()) {
					currentTime = bean.getWorkflowTime();
					lastBean = bean; 
				}
			}
			
			// Return Latest Bean of Workflow Set
			return lastBean;
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Unable to access Workflow History Refset members with error: " + e.getMessage());
		}
		
		return null;
	}

	public TreeSet<WorkflowHistoryJavaBean> getAllHistoryForWorkflowId(I_GetConceptData concept, UUID wfId) {
		TreeSet<WorkflowHistoryJavaBean> retSet = new TreeSet<WorkflowHistoryJavaBean>(WfComparator.getInstance().createWfHxEarliestFirstTimeComparer());

		try {
			TreeSet<WorkflowHistoryJavaBean> beans = WorkflowHelper.getWfHxMembersAsBeans(concept);
			for (WorkflowHistoryJavaBean bean : beans) {
				if (bean.getWorkflowId().equals(wfId)) {
					retSet.add(bean);
				}
				
			}
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Unable to access Workflow History Refset members with error: " + e.getMessage());
		}
		
		return retSet;
	}
	
	/* 
	 * From WorkflowHelper
	 * 
	 	public final static int workflowIdPosition = 0;									// 0
    	public final static int conceptIdPosition = workflowIdPosition + 1;				// 1
	    public final static int modelerPosition = conceptIdPosition + 1;				// 2
	    public final static int actionPosition = modelerPosition + 1;					// 3
	    public final static int statePosition = actionPosition + 1;						// 4
	    public final static int fsnPosition = statePosition + 1;						// 5
	    public final static int refsetColumnTimeStampPosition = fsnPosition + 1;		// 6
	    public final static int timeStampPosition = refsetColumnTimeStampPosition + 1;	// 7

    	public final static int numberOfColumns = timeStampPosition + 1;				// 8
	 *
	 */
	public String getMemberWfHxForDatabaseImport(int memberNid) throws IOException, TerminologyException {
		StringBuffer retStr = new StringBuffer();
		String[] columns = new String[WorkflowHelper.numberOfColumns];
		
		I_ExtendByRef member = Terms.get().getExtension(memberNid);
		if (member.getRefsetId() != getRefsetNid()) {
			return "";
		}

		
		long devWfTime = 0;
		boolean adjudicationPathFound = false;
		
		for (I_ExtendByRefVersion<?> tuple : member.getTuples()) {
			WorkflowHistoryJavaBean bean = WorkflowHelper.populateWorkflowHistoryJavaBean(tuple);

			if (tuple.getPathNid() == adjudicationPath) {
				adjudicationPathFound = true;
			} else {
				devWfTime = bean.getWorkflowTime();
			}
		}

		int counter = 0;
		for (I_ExtendByRefVersion<?> tuple : member.getTuples()) {
			if (adjudicationPathFound && tuple.getPathNid() != adjudicationPath) {
				continue;
			}
			
			WorkflowHistoryJavaBean bean = WorkflowHelper.populateWorkflowHistoryJavaBean(tuple);

			// UUIDs for WfId & Concept
			columns[WorkflowHelper.workflowIdPosition] = bean.getWorkflowId().toString();
			columns[WorkflowHelper.conceptIdPosition] = bean.getConcept().toString();
			
			// Pref Term for modeler's login
			columns[WorkflowHelper.modelerPosition] = WorkflowHelper.getPrefTerm(Terms.get().getConcept(bean.getModeler()));
			
			// FSN for state & action
			columns[WorkflowHelper.actionPosition] = WorkflowHelper.getFsn(Terms.get().getConcept(bean.getAction()));
			columns[WorkflowHelper.statePosition] = WorkflowHelper.getFsn(Terms.get().getConcept(bean.getState()));
			
			// FSN of concept
			columns[WorkflowHelper.fsnPosition] = bean.getFullySpecifiedName().toString();
			
			// Timestamps in 'yyyy-mm-dd hh:mm:ss' format
			if (devWfTime > 0) {
				columns[WorkflowHelper.refsetColumnTimeStampPosition] = dateParser.format(devWfTime);
			} else {
				columns[WorkflowHelper.refsetColumnTimeStampPosition] = dateParser.format(bean.getWorkflowTime());
			}
			columns[WorkflowHelper.timeStampPosition] = dateParser.format(bean.getEffectiveTime());
					 
			// Write columns to return string tab-delimited
			for (int i = 0; i < WorkflowHelper.numberOfColumns; i++) {
				if (columns[i] == null || columns[i].length() == 0) {
					throw new IOException("For memberId: " + memberNid + " unable to calculate column #" + i); 
				} else {
					retStr.append(columns[i]);
					retStr.append("\t");
				}
			}
		}
			 
		retStr.append("\r\n");

		return retStr.toString().trim();
	}
}