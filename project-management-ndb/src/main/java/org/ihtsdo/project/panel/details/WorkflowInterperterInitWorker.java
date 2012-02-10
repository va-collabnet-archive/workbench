package org.ihtsdo.project.panel.details;

import javax.swing.SwingWorker;

import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.workflow.api.WorkflowInterpreter;

public class WorkflowInterperterInitWorker extends SwingWorker<WorkflowInterpreter, Object[]> {

	private WorkList workList;
	/** The interpreter. */
	private WorkflowInterpreter interpreter;

	public WorkflowInterperterInitWorker(WorkList workList) {
		super();
		this.workList = workList;
	}

	@Override
	protected WorkflowInterpreter doInBackground() throws Exception {
		WorkflowInterpreter interpreter = WorkflowInterpreter.createWorkflowInterpreter(workList.getWorkflowDefinition());
		return interpreter;
	}

	@Override
	public void done() {
		try {
			this.interpreter = get();
		} catch (Exception ignore) {
			ignore.printStackTrace();
		}
	}

}