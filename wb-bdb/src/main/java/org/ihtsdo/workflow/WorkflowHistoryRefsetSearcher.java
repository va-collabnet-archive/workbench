package org.ihtsdo.workflow; 

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefset;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;
import org.ihtsdo.workflow.refset.utilities.WorkflowRefsetSearcher;


/* 
* @author Jesse Efron
* 
*/
public class WorkflowHistoryRefsetSearcher extends WorkflowRefsetSearcher {

	private int currentStatusNid = 0;

	public WorkflowHistoryRefsetSearcher()
	{
		try { 
			refset = new WorkflowHistoryRefset();
			setRefsetName(refset.getRefsetName());
			setRefsetId(refset.getRefsetId());
        	
			currentStatusNid = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getPrimoridalUid());
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error creating Workflow History Refset Searcher with error: " + e.getMessage());
		}
	}

	public int getTotalCount() {
		try {
			return Terms.get().getRefsetExtensionMembers(refsetId).size();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}
	public WorkflowHistoryJavaBean getLatestBeanForWorkflowId(int conceptNid, UUID workflowId) {
		long currentTime = 0;
		WorkflowHistoryJavaBean lastBean = null;
		
		try {
			for (I_ExtendByRef row : Terms.get().getRefsetExtensionsForComponent(getRefsetId(), conceptNid)) {
				
				int idx = row.getTuples().size() - 1;
				if (idx >= 0) {
					if (row.getTuples().get(idx).getStatusNid() == currentStatusNid) {
						WorkflowHistoryJavaBean currentBean = WorkflowHelper.createWfHxJavaBean(row);
						
						if (currentBean.getWorkflowId().equals(workflowId) &&
							currentTime < currentBean.getWorkflowTime()) {
							currentTime = currentBean.getWorkflowTime();
							lastBean = currentBean; 
						}
					}
				}			
			}
			
			// Return Latest Bean of Workflow Set
			return lastBean;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public static void listWorkflowHistory() throws NumberFormatException, IOException, TerminologyException 
	{
		Writer outputFile = new OutputStreamWriter(new FileOutputStream("C:\\Users\\jefron\\Desktop\\wb-bundle\\log\\Output.txt"));
		int counter = 0;
		WorkflowHistoryRefset refset = new WorkflowHistoryRefset();
		for (I_ExtendByRef row : Terms.get().getRefsetExtensionMembers(refset.getRefsetId())) 
		{
			WorkflowHistoryJavaBean bean = WorkflowHelper.createWfHxJavaBean(row);
			System.out.println("\n\nBean #: " + counter++ + " = " + bean.toString());
			outputFile.write("\n\nBean #: " + counter++ + " = " + bean.toString());
		}
		outputFile.flush();
		outputFile.close();
	}
}

