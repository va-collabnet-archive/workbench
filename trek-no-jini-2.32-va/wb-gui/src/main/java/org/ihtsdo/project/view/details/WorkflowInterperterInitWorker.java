package org.ihtsdo.project.view.details;

import javax.swing.SwingWorker;

import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.workflow.api.WorkflowInterpreter;

public class WorkflowInterperterInitWorker extends SwingWorker<WorkflowInterpreter, Object[]> {

	private WorkList workList;
	/** The interpreter. */
	private boolean fresh;

	public WorkflowInterperterInitWorker(WorkList workList, boolean fresh) {
		super();
		this.workList = workList;
		this.fresh = fresh;
	}

	@Override
	protected WorkflowInterpreter doInBackground() throws Exception {
		WorkflowInterpreter interpreter;
		if(!fresh){
			interpreter = WorkflowInterpreter.createWorkflowInterpreter(workList.getWorkflowDefinition());
		}else{
			interpreter = WorkflowInterpreter.createFreshWorkflowInterpreter(workList.getWorkflowDefinition());
		}
		return interpreter;
	}

	@Override
	public void done() {
		try {
			get();
		} catch (Exception ignore) {
			//ignorAceLog.getAppLog().alertAndLogException(e);
		}
	}

}