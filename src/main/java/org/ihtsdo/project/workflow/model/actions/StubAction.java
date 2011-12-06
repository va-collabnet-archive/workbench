package org.ihtsdo.project.workflow.model.actions;

import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfInstance;

public class StubAction extends WfAction {

	public StubAction() {
		// TODO Auto-generated constructor stub
	}
	
	public StubAction(String name) {
		this.setName(name);
	}

	@Override
	public WfInstance doAction(WfInstance instance) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
