package org.ihtsdo.project.workflow.api.wf2.implementation;

import java.util.UUID;

import org.ihtsdo.tk.workflow.api.WfPermissionBI;
import org.ihtsdo.tk.workflow.api.WfRoleBI;
import org.ihtsdo.tk.workflow.api.WfUserBI;

public class WfPermissionImpl implements WfPermissionBI {

	WfUserBI user;
	WfRoleBI role;
	UUID hierarchyParent;
	UUID projectId;

	public WfPermissionImpl(WfUserBI user, WfRoleBI role, UUID hierarchyParent, UUID projectId) {
		this.user = user;
		this.role = role;
		this.hierarchyParent = hierarchyParent;
		this.projectId = projectId;
	}

	@Override
	public WfUserBI getUser() {
		return user;
	}

	@Override
	public WfRoleBI getRole() {
		return role;
	}

	@Override
	public UUID getHierarchyParent() {
		return hierarchyParent;
	}

	@Override
	public UUID getProject() {
		return projectId;
	}

}
