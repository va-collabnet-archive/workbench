package org.ihtsdo.project.workflow.api.wf2.implementation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.project.workflow.model.WorkflowDefinition;
import org.ihtsdo.project.workflow2.WfActivityBI;
import org.ihtsdo.project.workflow2.WfProcessDefinitionBI;
import org.ihtsdo.project.workflow2.WfRoleBI;
import org.ihtsdo.project.workflow2.WfStateBI;

public class WfProcessDefinition implements WfProcessDefinitionBI {
	
	WorkflowDefinition definition;

	public WfProcessDefinition(WorkflowDefinition definition) {
		this.definition = definition;
	}

	@Override
	public String getName() {
		return definition.getName();
	}

	@Override
	public UUID getUuid() throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<WfRoleBI> getRoles() {
		List<WfRoleBI> roles = new ArrayList<WfRoleBI>();
		roles.addAll(definition.getRoles());
		return roles;
	}

	@Override
	public Collection<WfStateBI> getStates() {
		List<WfStateBI> states = new ArrayList<WfStateBI>();
		states.addAll(definition.getStates());
		return states;
	}

	@Override
	public Collection<WfActivityBI> getActivities() {
		List<WfActivityBI> activities = new ArrayList<WfActivityBI>();
		activities.addAll(definition.getActions().values());
		return activities;
	}

	@Override
	public Object getLogic() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean logicIsValid() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCompleteState(WfStateBI state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPromoteState(WfStateBI state) {
		// TODO Auto-generated method stub
		return false;
	}

}
