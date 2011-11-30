package org.ihtsdo.project.workflow.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;

public class WorkflowInterpreter {
	
	WorkflowDefinition workflowDefinition;
	
	public WorkflowInterpreter(WorkflowDefinition workflowDefinition) {
		super();
		this.workflowDefinition = workflowDefinition;
	}

	public List<WfAction> getPossibleActions(WfInstance instance, WfUser user) {
		List<WfAction> possibleActions = new ArrayList<WfAction>();
		for (WfAction loopAction : workflowDefinition.getActions()) {
			if (checkPermissionForAction(instance, user, loopAction)) {
				possibleActions.add(loopAction);
			}
		}
		return possibleActions;
	}
	
	public WfInstance doAction(WfInstance instance, WfAction action, WfUser user) throws Exception {
		return null;
	}

	public WfInstance revertLastAction(WfInstance instance, WfUser user) throws Exception {
		return null;
	}
	
	public boolean checkPermissionForAction(WfInstance instance, WfUser user, WfAction action) {
		boolean result = false;
		List<WfRole> roles = filterRolesForComponent(instance.getComponentId(), user);
		//if (CollectionUtils.containsAny(action.getPermissions(),roles) &&
				//action.getInitialStates().contains(instance.getState())) {
			result = true;
//		}
		return result;
	}

	private List<WfRole> filterRolesForComponent(UUID componentId, WfUser user) {
		// TODO implement hierarchy filter for component
		//return new ArrayList<WfRole>(user.getRoles().values());
		return null;
	}

	public WorkflowDefinition getWorkflowDefinition() {
		return workflowDefinition;
	}

	public void setWorkflowDefinition(WorkflowDefinition workflowDefinition) {
		this.workflowDefinition = workflowDefinition;
	}

}
