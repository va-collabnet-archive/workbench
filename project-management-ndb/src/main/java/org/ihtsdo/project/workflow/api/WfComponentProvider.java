package org.ihtsdo.project.workflow.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ProjectPermissionsAPI;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfPermission;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;

public class WfComponentProvider {

	public static Map<UUID, WfUser> usersCache = new HashMap<UUID, WfUser>();
	public static Map<UUID, WfState> statesCache = new HashMap<UUID, WfState>();
	public static Map<UUID, WfRole> rolesCache = new HashMap<UUID, WfRole>();

	public List<WfUser> getUsers() {
		List<WfUser> wfUsers = new ArrayList<WfUser>();
		try {
			I_GetConceptData roleParent = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER.getUids());

			I_IntSet allowedTypes = Terms.get().getActiveAceFrameConfig().getDestRelTypes();
			I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
			Set<Integer> currentStatuses = helper.getCurrentStatusIds();

			Set<? extends I_GetConceptData> allUsers = roleParent.getDestRelOrigins(Terms.get().getActiveAceFrameConfig().getAllowedStatus(), allowedTypes, Terms.get().getActiveAceFrameConfig()
					.getViewPositionSetReadOnly(), Precedence.TIME, Terms.get().getActiveAceFrameConfig().getConflictResolutionStrategy());

			for (I_GetConceptData user : allUsers) {

				I_ConceptAttributeVersioned attr = user.getConceptAttributes();
				if (TerminologyProjectDAO.isActive(attr.getStatusNid())) {
					wfUsers.add(new WfUser(user.toUserString(), user.getUids().iterator().next(), null));

					Set<? extends I_GetConceptData> allDescUsers = user.getDestRelOrigins(Terms.get().getActiveAceFrameConfig().getAllowedStatus(), allowedTypes, Terms.get().getActiveAceFrameConfig()
							.getViewPositionSetReadOnly(), Precedence.TIME, Terms.get().getActiveAceFrameConfig().getConflictResolutionStrategy());
					for (I_GetConceptData descUser : allDescUsers) {

						attr = descUser.getConceptAttributes();
						if (TerminologyProjectDAO.isActive(attr.getStatusNid())) {
							wfUsers.add(new WfUser(descUser.toUserString(), descUser.getUids().iterator().next(), null));

						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return wfUsers;
	}

	private String getPreferredTermFromAuxiliaryHier(I_GetConceptData concept) {

		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			
			ConceptVersionBI conceptBi = ((ConceptChronicleBI) concept).getVersion(config.getViewCoordinate());
			
			for (DescriptionVersionBI loopDesc : conceptBi.getDescsActive()) {
				if (loopDesc.getTypeNid() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()) {
					return loopDesc.getText();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
		return "";
	}

	public WfUser getUserByUUID(UUID id) {
		WfUser wfUser = null;
		try {
			I_GetConceptData roleParent = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.IHTSDO.getUids());

			I_IntSet allowedTypes = Terms.get().getActiveAceFrameConfig().getDestRelTypes();
			I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
			Set<Integer> currentStatuses = helper.getCurrentStatusIds();

			Set<? extends I_GetConceptData> allUsers = roleParent.getDestRelOrigins(Terms.get().getActiveAceFrameConfig().getAllowedStatus(), allowedTypes, Terms.get().getActiveAceFrameConfig()
					.getViewPositionSetReadOnly(), Precedence.TIME, Terms.get().getActiveAceFrameConfig().getConflictResolutionStrategy());

			for (I_GetConceptData user : allUsers) {
				I_ConceptAttributeVersioned attr = user.getConceptAttributes();
				if (TerminologyProjectDAO.isActive(attr.getStatusNid()) && user.getUids().contains(id)) {
					wfUser = new WfUser();
					wfUser = new WfUser(attr.toUserString(), user.getUids().iterator().next(), null);
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return wfUser;
	}

	public List<WfRole> getRoles() {
		List<WfRole> returnRoles = new ArrayList<WfRole>();

		try {
			Set<I_GetConceptData> allRoles = new HashSet<I_GetConceptData>();
			allRoles = ProjectPermissionsAPI.getDescendants(allRoles, Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER_ROLE.getUids()));

			for (I_GetConceptData role : allRoles) {
				returnRoles.add(roleConceptToWfRole(role));
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return returnRoles;

	}

	public List<WfState> getAllStates() {
		return getStates();
	}

	public static WfInstance getWfInstance(UUID instanceUUID) {
		WfInstance wfInstance = null;
		try {
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);
			for (I_TerminologyProject i_TerminologyProject : projects) {
				List<WorkSet> worksets = TerminologyProjectDAO.getAllWorkSetsForProject(i_TerminologyProject, config);
				for (WorkSet workSet : worksets) {
					List<WorkList> worklists = TerminologyProjectDAO.getAllWorklistForWorkset(workSet, config);
					for (WorkList workList : worklists) {
						wfInstance = TerminologyProjectDAO.getWorkListMember(Terms.get().getConcept(instanceUUID), workList, config).getWfInstance();
						if (wfInstance != null) {
							break;
						}
					}
					if (wfInstance != null) {
						break;
					}
				}
				if (wfInstance != null) {
					break;
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return wfInstance;
	}

	public List<WfPermission> getPermissionsForUser(WfUser user) {
		List<WfPermission> wfPermissions = new ArrayList<WfPermission>();
		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			ProjectPermissionsAPI permissionsApi = new ProjectPermissionsAPI(config);
			Map<I_GetConceptData, I_GetConceptData> permissions = permissionsApi.getPermissionsForUser(Terms.get().getConcept(user.getId()));
			for (I_GetConceptData loopRole : permissions.keySet()) {
				I_GetConceptData loopHierarchy = permissions.get(loopRole);
				WfPermission loopPerm = new WfPermission();
				loopPerm.setId(UUID.randomUUID());
				loopPerm.setRole(roleConceptToWfRole(Terms.get().getConcept(loopRole.getPrimUuid())));
				loopPerm.setHiearchyId(loopHierarchy.getPrimUuid());
				wfPermissions.add(loopPerm);
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return wfPermissions;
	}

	public List<WfPermission> getPermissions() {
		List<WfPermission> wfPermissions = new ArrayList<WfPermission>();

		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			ProjectPermissionsAPI permissionsApi = new ProjectPermissionsAPI(config);
			for (WfUser loopUser : getUsers()) {
				Map<I_GetConceptData, I_GetConceptData> permissions = permissionsApi.getPermissionsForUser(Terms.get().getConcept(loopUser.getId()));

				for (I_GetConceptData loopRole : permissions.keySet()) {
					I_GetConceptData loopHierarchy = permissions.get(loopRole);
					WfPermission loopPerm = new WfPermission();
					loopPerm.setId(UUID.randomUUID());
					loopPerm.setRole(roleConceptToWfRole(Terms.get().getConcept(loopRole.getPrimUuid())));
					loopPerm.setHiearchyId(loopHierarchy.getPrimUuid());
					wfPermissions.add(loopPerm);
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return wfPermissions;
	}

	public List<WfState> getStates() {
		List<WfState> returnStates = new ArrayList<WfState>();

		try {
			Set<I_GetConceptData> allStates = new HashSet<I_GetConceptData>();
			allStates = ProjectPermissionsAPI.getDescendants(allStates, Terms.get().getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_STATUS.getUids()));

			for (I_GetConceptData state : allStates) {
				returnStates.add(statusConceptToWfState(state));
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return returnStates;
	}

	public WfRole roleConceptToWfRole(I_GetConceptData role) {
		WfRole wfrole = WfComponentProvider.rolesCache.get(role.getPrimUuid());
		if (wfrole != null) {
			return wfrole;
		}
		wfrole = new WfRole(getPreferredTermFromAuxiliaryHier(role), role.getPrimUuid());
		WfComponentProvider.rolesCache.put(wfrole.getId(), wfrole);
		return wfrole;
	}

	public WfUser userConceptToWfUser(I_GetConceptData user) {
		WfUser wfUser = WfComponentProvider.usersCache.get(user.getPrimUuid());
		if (wfUser != null) {
			return wfUser;
		}

		try {
			wfUser = new WfUser(user.getInitialText(), user.getPrimUuid(), new ArrayList<WfPermission>());
			wfUser.setPermissions(getPermissionsForUser(wfUser));
		} catch (IOException e) {
			e.printStackTrace();
		}

		WfComponentProvider.usersCache.put(wfUser.getId(), wfUser);
		return wfUser;
	}

	public WfState statusConceptToWfState(I_GetConceptData status) {
		WfState state = WfComponentProvider.statesCache.get(status.getPrimUuid());
		if (state != null) {
			return state;
		}

		try {
			state = new WfState(status.getInitialText(), status.getPrimUuid());
		} catch (IOException e) {
			e.printStackTrace();
		}
		WfComponentProvider.statesCache.put(state.getId(), state);
		return state;
	}

	private static final String END_FILE = ".wfd";

	public static List<File> getWorkflowDefinitionFiles() {
		File folder = new File("./sampleProcesses");
		List<File> retFiles = loadFiles(folder, END_FILE);
		return retFiles;
	}

	private static List<File> loadFiles(File folder, String endFile) {
		File[] fileList = folder.listFiles();
		List<File> retFiles = new ArrayList<File>();
		for (File file : fileList) {
			if (file.isDirectory()) {
				List<File> tmpFiles = loadFiles(file, endFile);
				retFiles.addAll(tmpFiles);
			} else {
				if (!file.isHidden() && file.getName().toLowerCase().endsWith(endFile)) {
					retFiles.add(file);
				}
			}
		}
		return retFiles;
	}

}
