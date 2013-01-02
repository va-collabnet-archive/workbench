package org.ihtsdo.project.workflow.api.wf2.implementation;

import java.util.Collection;
import java.util.UUID;

import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.workflow2.ProjectBI;
import org.ihtsdo.project.workflow2.WfPermissionBI;
import org.ihtsdo.project.workflow2.WfUserBI;
import org.ihtsdo.project.workflow2.WorkListBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public class Project implements ProjectBI {
	
	I_TerminologyProject project;

	public Project(I_TerminologyProject project) {
		this.project = project;
	}

	@Override
	public String getName() {
		return project.getName();
	}

	@Override
	public UUID getUuid() {
		return project.getUids().iterator().next();
	}

	@Override
	public ViewCoordinate getViewCoordinate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EditCoordinate getEditCoordinate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<WorkListBI> getWorkLists() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<WfPermissionBI> getPermissions(WfUserBI user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<WfPermissionBI> getPermissions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

}
