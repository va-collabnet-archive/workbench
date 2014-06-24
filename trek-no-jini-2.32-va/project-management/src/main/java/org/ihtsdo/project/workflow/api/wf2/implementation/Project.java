package org.ihtsdo.project.workflow.api.wf2.implementation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.ProjectPermissionsAPI;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.model.WfMembership;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.workflow.api.ProjectBI;
import org.ihtsdo.tk.workflow.api.WfPermissionBI;
import org.ihtsdo.tk.workflow.api.WfProcessDefinitionBI;
import org.ihtsdo.tk.workflow.api.WfRoleBI;
import org.ihtsdo.tk.workflow.api.WfUserBI;
import org.ihtsdo.tk.workflow.api.WorkListBI;

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

	public ViewCoordinate getViewCoordinate() throws Exception {
		return Terms.get().getActiveAceFrameConfig().getViewCoordinate();
	}

	public EditCoordinate getEditCoordinate() throws Exception {
		return Terms.get().getActiveAceFrameConfig().getEditCoordinate();
	}

	@Override
	public Collection<WorkListBI> getWorkLists() throws Exception {
		List<WorkListBI> workLists = new ArrayList<WorkListBI>();
		for (WorkSet workSet : TerminologyProjectDAO.getAllWorkSetsForProject(project, Terms.get().getActiveAceFrameConfig())) {
			workLists.addAll(TerminologyProjectDAO.getAllWorklistForWorkset(workSet, Terms.get().getActiveAceFrameConfig()));
		}
		//workLists.addAll(TerminologyProjectDAO.getAllNacWorkLists(project, Terms.get().getActiveAceFrameConfig()));
		return workLists;
	}

	@Override
	public Collection<WfPermissionBI> getPermissions(WfUserBI user) throws Exception {
		WfComponentProvider prov = new WfComponentProvider();
		List<WfPermissionBI> permissions = new ArrayList<WfPermissionBI>();
		ProjectPermissionsAPI permApi = new ProjectPermissionsAPI(Terms.get().getActiveAceFrameConfig());
		Map<I_GetConceptData, Set<I_GetConceptData>> permMap = permApi.getMultiplePermissionsForUser(Terms.get().getConcept(user.getUuid()));

		for (I_GetConceptData roleConcept : permMap.keySet()) {
			for (I_GetConceptData roleOnCon : permMap.get(roleConcept)) {
				WfPermissionImpl loopPerm = new WfPermissionImpl(user, prov.roleConceptToWfRole(roleConcept), 
						roleOnCon.getPrimUuid(), project.getUids().iterator().next());
				permissions.add(loopPerm);
			}
		}

		return permissions;
	}

	@Override
	public Collection<WfPermissionBI> getPermissions() throws Exception {
		WfComponentProvider prov = new WfComponentProvider();
		List<WfPermissionBI> permissions = new ArrayList<WfPermissionBI>();
		ProjectPermissionsAPI permApi = new ProjectPermissionsAPI(Terms.get().getActiveAceFrameConfig());

		for (WfUser user : prov.getUsers()) {
			Map<I_GetConceptData, Set<I_GetConceptData>> permMap = permApi.getMultiplePermissionsForUser(Terms.get().getConcept(user.getUuid()));
			for (I_GetConceptData roleConcept : permMap.keySet()) {
				for (I_GetConceptData roleOnCon : permMap.get(roleConcept)) {
					WfPermissionBI loopPerm = new WfPermissionImpl(user, 
							prov.roleConceptToWfRole(roleConcept), 
							roleOnCon.getPrimUuid(), project.getUids().iterator().next());
					permissions.add(loopPerm);
				}
			}
		}

		return permissions;
	}

	@Override
	public String getDescription() {
		return "";
	}

	public EditCoordinate getPromotionCoordinate() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPermission(WfUserBI user,
			WfRoleBI role, UUID hierarchyUuid) throws Exception {
		WfComponentProvider prov = new WfComponentProvider();
		ProjectPermissionsAPI permApi = new ProjectPermissionsAPI(Terms.get().getActiveAceFrameConfig());
		I_TermFactory tf = Terms.get();
		permApi.addPermission(tf.getConcept(user.getUuid()),tf.getConcept(role.getUuid()), tf.getConcept(hierarchyUuid));
	}

	@Override
	public void createWorkList(WfProcessDefinitionBI definition, String name,
			Collection<WfPermissionBI> permissions) throws Exception {
		WfProcessDefinition pdef = (WfProcessDefinition) definition;
		ArrayList<WfMembership> oldStylePermissions = new ArrayList<WfMembership>();
		for (WfPermissionBI loopPermission : permissions) {
			oldStylePermissions.add(new WfMembership(UUID.randomUUID(), 
					(WfUser) loopPermission.getUser(), 
					(WfRole) loopPermission.getRole(), false));
		}
		
		TerminologyProjectDAO.createNewNacWorkList(project, pdef.getDefinition(), oldStylePermissions, name, 
				Terms.get().getActiveAceFrameConfig(), null);
		
	}

}
