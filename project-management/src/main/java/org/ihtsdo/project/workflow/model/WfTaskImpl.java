package org.ihtsdo.project.workflow.model;

import java.util.Collection;
import java.util.LinkedList;

import org.ihtsdo.project.workflow2.WfActivityBI;
import org.ihtsdo.project.workflow2.WfProcessDefinitionBI;
import org.ihtsdo.project.workflow2.WfHistoryEntryBI;
import org.ihtsdo.project.workflow2.WfRoleBI;
import org.ihtsdo.project.workflow2.WfStateBI;
import org.ihtsdo.project.workflow2.WfProcessInstanceBI;
import org.ihtsdo.project.workflow2.WfUserBI;
import org.ihtsdo.project.workflow2.WorkListBI;
import org.ihtsdo.tk.api.ComponentVersionBI;

public class WfTaskImpl implements WfProcessInstanceBI {
	
	WfInstance instance;

	public WfTaskImpl(WfInstance instance) {
		this.instance = instance;
	}

	@Override
	public ComponentVersionBI getComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorkListBI getWorkList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WfProcessDefinitionBI getWorkflowDefinition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WfStateBI getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setState(WfStateBI state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public WfUserBI getAssignedUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAssignedUser(WfUserBI user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<WfActivityBI> getActions(WfUserBI user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<WfActivityBI> getActions(WfRoleBI role) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<WfActivityBI> getActions(Collection<WfRoleBI> roles) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<WfActivityBI> getActionsForOverrideMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<WfHistoryEntryBI> getHistory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getDueDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getCreationDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getPriority() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCompleted() {
		// TODO Auto-generated method stub
		return false;
	}

}
