package org.ihtsdo.project.workflow.api.wf2.implementation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.ProjectPermissionsAPI;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow2.ProjectBI;
import org.ihtsdo.project.workflow2.WfPermissionBI;
import org.ihtsdo.project.workflow2.WfRoleBI;
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
	public ViewCoordinate getViewCoordinate() throws Exception {
		return Terms.get().getActiveAceFrameConfig().getViewCoordinate();
	}

	@Override
	public EditCoordinate getEditCoordinate() throws Exception {
		return Terms.get().getActiveAceFrameConfig().getEditCoordinate();
	}

	@Override
	public Collection<WorkListBI> getWorkLists() throws Exception {
		List<WorkListBI> workLists = new ArrayList<WorkListBI>();
		for (WorkSet workSet : TerminologyProjectDAO.getAllWorkSetsForProject(project, Terms.get().getActiveAceFrameConfig())) {
			workLists.addAll(TerminologyProjectDAO.getAllWorklistForWorkset(workSet, Terms.get().getActiveAceFrameConfig()));
		}
		return workLists;
	}

	@Override
	public Collection<WfPermissionBI> getPermissions(WfUserBI user) throws Exception {
		WfComponentProvider prov = new WfComponentProvider();
		List<WfPermissionBI> permissions = new ArrayList<WfPermissionBI>();
		ProjectPermissionsAPI permApi = new ProjectPermissionsAPI(Terms.get().getActiveAceFrameConfig());
		Map<I_GetConceptData, I_GetConceptData> permMap = permApi.getPermissionsForUser(Terms.get().getConcept(user.getUuid()));

		for (I_GetConceptData roleConcept : permMap.keySet()) {
			WfPermission loopPerm = new WfPermission(user, prov.roleConceptToWfRole(roleConcept), 
					permMap.get(roleConcept).getPrimUuid(), project.getUids().iterator().next());
			permissions.add(loopPerm);
		}

		return permissions;
	}

	@Override
	public Collection<WfPermissionBI> getPermissions() throws Exception {
		WfComponentProvider prov = new WfComponentProvider();
		List<WfPermissionBI> permissions = new ArrayList<WfPermissionBI>();
		ProjectPermissionsAPI permApi = new ProjectPermissionsAPI(Terms.get().getActiveAceFrameConfig());

		for (I_GetConceptData user : permApi.getUsers()) {
			Map<I_GetConceptData, I_GetConceptData> permMap = permApi.getPermissionsForUser(Terms.get().getConcept(user.getPrimUuid()));

			for (I_GetConceptData roleConcept : permMap.keySet()) {
				WfPermission loopPerm = new WfPermission(prov.userConceptToWfUser(user), 
						prov.roleConceptToWfRole(roleConcept), 
						permMap.get(roleConcept).getPrimUuid(), project.getUids().iterator().next());
				permissions.add(loopPerm);
			}
		}

		return permissions;
	}

	@Override
	public String getDescription() {
		return "";
	}

	class WfPermission implements WfPermissionBI {

		WfUserBI user;
		WfRoleBI role;
		UUID hierarchyParent;
		UUID projectId;

		public WfPermission(WfUserBI user, WfRoleBI role, UUID hierarchyParent, UUID projectId) {
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

}
