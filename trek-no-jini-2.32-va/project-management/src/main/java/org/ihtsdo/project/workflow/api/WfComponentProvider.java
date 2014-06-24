/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.project.workflow.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ProjectPermissionsAPI;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.workflow.model.TestXstream;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfMembership;
import org.ihtsdo.project.workflow.model.WfPermission;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.binding.snomed.RefsetAux;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

/**
 * The Class WfComponentProvider.
 */
public class WfComponentProvider {

	/** The users cache. */
	public static Map<UUID, WfUser> usersCache = new HashMap<UUID, WfUser>();

	/** The states cache. */
	public static Map<UUID, WfState> statesCache = new HashMap<UUID, WfState>();

	/** The roles cache. */
	public static Map<UUID, WfRole> rolesCache = new HashMap<UUID, WfRole>();

	/**
	 * Gets the users.
	 * 
	 * @return the users
	 */
	public List<WfUser> getUsers() {
		List<WfUser> wfUsers = new ArrayList<WfUser>();
		try {
			I_GetConceptData roleParent = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER.getUids());

			I_IntSet allowedTypes = Terms.get().getActiveAceFrameConfig().getDestRelTypes();

			Set<? extends I_GetConceptData> allUsers = roleParent.getDestRelOrigins(Terms.get().getActiveAceFrameConfig().getAllowedStatus(), allowedTypes, Terms.get().getActiveAceFrameConfig().getViewPositionSetReadOnly(), Precedence.TIME, Terms.get().getActiveAceFrameConfig()
					.getConflictResolutionStrategy());

			for (I_GetConceptData user : allUsers) {

				I_ConceptAttributeVersioned attr = user.getConAttrs();
				if (TerminologyProjectDAO.isActive(attr.getStatusNid())) {
					wfUsers.add(new WfUser(user.toString(), user.getUids().iterator().next(), null));

					Set<? extends I_GetConceptData> allDescUsers = user.getDestRelOrigins(Terms.get().getActiveAceFrameConfig().getAllowedStatus(), allowedTypes, Terms.get().getActiveAceFrameConfig().getViewPositionSetReadOnly(), Precedence.TIME, Terms.get().getActiveAceFrameConfig()
							.getConflictResolutionStrategy());
					for (I_GetConceptData descUser : allDescUsers) {

						attr = descUser.getConAttrs();
						if (TerminologyProjectDAO.isActive(attr.getStatusNid())) {
							wfUsers.add(new WfUser(descUser.toString(), descUser.getUids().iterator().next(), null));

						}
					}
				}
			}

		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return wfUsers;
	}

	/**
	 * Gets the preferred term from auxiliary hier.
	 * 
	 * @param concept
	 *            the concept
	 * @return the preferred term from auxiliary hier
	 */
	private String getPreferredTermFromAuxiliaryHier(I_GetConceptData concept) {

		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();

			ConceptVersionBI conceptBi = ((ConceptChronicleBI) concept).getVersion(config.getViewCoordinate());

			for (DescriptionVersionBI<?> loopDesc : conceptBi.getDescriptionsActive()) {
				if (loopDesc.getTypeNid() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()) {
					return loopDesc.getText();
				} else if (isPreferredTerm(loopDesc, config.getViewCoordinate())) {
					return loopDesc.getText();
				}
			}
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (ContradictionException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return "";
	}

	/**
	 * Gets the user by uuid.
	 * 
	 * @param id
	 *            the id
	 * @return the user by uuid
	 */
	public WfUser getUserByUUID(UUID id) {
		WfUser wfUser = null;
		ConceptVersionBI userConcept;
		try {
			userConcept = Ts.get().getConceptVersion(Terms.get().getActiveAceFrameConfig().getViewCoordinate(), id);
			wfUser = new WfUser(userConcept.getDescriptionPreferred().getText(), userConcept.getPrimUuid(), null);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
		return wfUser;
	}

	/**
	 * Gets the roles.
	 * 
	 * @return the roles
	 */
	public List<WfRole> getRoles() {
		List<WfRole> returnRoles = new ArrayList<WfRole>();

		try {
			Set<I_GetConceptData> translationRoles = new HashSet<I_GetConceptData>();

			/* For Tranlslation Projects
			translationRoles = ProjectPermissionsAPI.getDescendants(translationRoles, Terms.get().getConcept(ArchitectonicAuxiliary.Concept.TRANSLATOR_ROLE.getUids()));

			for (I_GetConceptData role : translationRoles) {
				returnRoles.add(roleConceptToWfRole(role));
			}
			 */
			
			/* For International WF Edition
			Set<I_GetConceptData> wfRoles = new HashSet<I_GetConceptData>();
			wfRoles = ProjectPermissionsAPI.getDescendants(wfRoles, Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ROLES.getUids()));

			for (I_GetConceptData role : wfRoles) {
				returnRoles.add(roleConceptToWfRole(role));
			}
			*/

			// VA
			Set<I_GetConceptData> vaRoles = new HashSet<I_GetConceptData>();
			vaRoles = ProjectPermissionsAPI.getDescendants(vaRoles, Terms.get().getConcept(UUID.fromString("824308c5-1bdb-5f32-9558-faa51f650118")));

			for (I_GetConceptData role : vaRoles) {
				returnRoles.add(roleConceptToWfRole(role));
			}

			// JIF
			Set<I_GetConceptData> jifRoles = new HashSet<I_GetConceptData>();
			jifRoles = ProjectPermissionsAPI.getDescendants(jifRoles, Terms.get().getConcept(UUID.fromString("f6561a78-a48f-594f-89df-5f77a215d807")));

			for (I_GetConceptData role : jifRoles) {
				returnRoles.add(roleConceptToWfRole(role));
			}
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return returnRoles;

	}

	/**
	 * Gets the all states.
	 * 
	 * @return the all states
	 */
	public List<WfState> getAllStates() {
		return getStates();
	}

	/**
	 * Gets the wf instance.
	 * 
	 * @param instanceUUID
	 *            the instance uuid
	 * @return the wf instance
	 */
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
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return wfInstance;
	}

	/**
	 * Gets the permissions for user.
	 * 
	 * @param user
	 *            the user
	 * @return the permissions for user
	 */
	public List<WfPermission> getPermissionsForUser(WfUser user) {
		List<WfPermission> wfPermissions = new ArrayList<WfPermission>();
		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			ProjectPermissionsAPI permissionsApi = new ProjectPermissionsAPI(config);
			Map<I_GetConceptData, Set<I_GetConceptData>> permissions = permissionsApi.getMultiplePermissionsForUser(Terms.get().getConcept(user.getId()));
			
			for (I_GetConceptData loopRole : permissions.keySet()) {
				for (I_GetConceptData loopHierarchy : permissions.get(loopRole)) {
					WfPermission loopPerm = new WfPermission();
					loopPerm.setId(UUID.randomUUID());
					loopPerm.setRole(roleConceptToWfRole(Terms.get().getConcept(loopRole.getPrimUuid())));
					loopPerm.setHiearchyId(loopHierarchy.getPrimUuid());
					wfPermissions.add(loopPerm);
				}
			}
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return wfPermissions;
	}

	/**
	 * Gets the permissions for user.
	 * 
	 * @param user
	 *            the user
	 * @return the permissions for user
	 */
	public List<WfPermission> getPermissionsForUserInWorklist(WfUser user, WfInstance instance) {
		List<WfPermission> wfPermissions = new ArrayList<WfPermission>();
		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			ProjectPermissionsAPI permissionsApi = new ProjectPermissionsAPI(config);
			Map<I_GetConceptData, Set<I_GetConceptData>> allPermissions = permissionsApi.getMultiplePermissionsForUser(Terms.get().getConcept(user.getId()));
			Map<I_GetConceptData, Set<I_GetConceptData>> worklistPermissions = new HashMap<I_GetConceptData, Set<I_GetConceptData>>();
			
			List<WfMembership> userRoles = ((WorkList) instance.getWorkList()).getWorkflowUserRoles();
			List<WfRole> currentUserRoles = new ArrayList<WfRole>();
			
			for (WfMembership membership : userRoles) {
				if (membership.getUser().equals(user)) {
					currentUserRoles.add(membership.getRole());
				}
			}

			for (I_GetConceptData role : allPermissions.keySet()) {
				if (currentUserRoles.contains(roleConceptToWfRole(role))) {
					worklistPermissions.put(role, allPermissions.get(role));
				}
			}
			
			for (I_GetConceptData loopRole : worklistPermissions.keySet()) {
				for (I_GetConceptData loopHierarchy : worklistPermissions.get(loopRole)) {
					WfPermission loopPerm = new WfPermission();
					loopPerm.setId(UUID.randomUUID());
					loopPerm.setRole(roleConceptToWfRole(Terms.get().getConcept(loopRole.getPrimUuid())));
					loopPerm.setHiearchyId(loopHierarchy.getPrimUuid());
					wfPermissions.add(loopPerm);
				}
			}
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return wfPermissions;
	}

	/**
	 * Gets the permissions.
	 * 
	 * @return the permissions
	 */
	public List<WfPermission> getPermissions() {
		List<WfPermission> wfPermissions = new ArrayList<WfPermission>();

		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			ProjectPermissionsAPI permissionsApi = new ProjectPermissionsAPI(config);
			for (WfUser loopUser : getUsers()) {
				Map<I_GetConceptData, Set<I_GetConceptData>> permissions = permissionsApi.getMultiplePermissionsForUser(Terms.get().getConcept(loopUser.getId()));

				for (I_GetConceptData loopRole : permissions.keySet()) {
					for (I_GetConceptData loopHierarchy : permissions.get(loopRole)) {
						WfPermission loopPerm = new WfPermission();
						loopPerm.setId(UUID.randomUUID());
						loopPerm.setRole(roleConceptToWfRole(Terms.get().getConcept(loopRole.getPrimUuid())));
						loopPerm.setHiearchyId(loopHierarchy.getPrimUuid());
						wfPermissions.add(loopPerm);
					}
				}
			}
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return wfPermissions;
	}

	/**
	 * Gets the states.
	 * 
	 * @return the states
	 */
	public List<WfState> getStates() {
		List<WfState> returnStates = new ArrayList<WfState>();

		try {
			Set<I_GetConceptData> allStates = new HashSet<I_GetConceptData>();
			/* Translation States
			 * 	allStates.addAll(ProjectPermissionsAPI.getDescendants(allStates, Terms.get().getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_STATUS.getUids())));
			 */
			
			/* International Edition States
			allStates.addAll(ProjectPermissionsAPI.getDescendants(allStates, Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_STATES.getUids())));
			*/
			
			// VA
			allStates.addAll(ProjectPermissionsAPI.getDescendants(allStates, Terms.get().getConcept(UUID.fromString("041a7ea1-942d-58d2-b9df-ee33d684ae1b"))));
			// JIF
			allStates.addAll(ProjectPermissionsAPI.getDescendants(allStates, Terms.get().getConcept(UUID.fromString("4af7ffa0-dadd-541b-bdb3-25cda4f89810"))));
			
			for (I_GetConceptData state : allStates) {
				returnStates.add(statusConceptToWfState(state));
			}

			//returnStates.add(statusConceptToWfState(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getUids())));

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return returnStates;
	}

	/**
	 * Role concept to wf role.
	 * 
	 * @param role
	 *            the role
	 * @return the wf role
	 */
	public WfRole roleConceptToWfRole(I_GetConceptData role) {
		WfRole wfrole = WfComponentProvider.rolesCache.get(role.getPrimUuid());
		if (wfrole != null) {
			return wfrole;
		}
		wfrole = new WfRole(getPreferredTermFromAuxiliaryHier(role), role.getPrimUuid());
		WfComponentProvider.rolesCache.put(wfrole.getId(), wfrole);
		return wfrole;
	}

	/**
	 * User concept to wf user.
	 * 
	 * @param user
	 *            the user
	 * @return the wf user
	 */
	public WfUser userConceptToWfUser(I_GetConceptData user) {
		WfUser wfUser = WfComponentProvider.usersCache.get(user.getPrimUuid());
		if (wfUser != null) {
			return wfUser;
		}

		try {
			wfUser = new WfUser(user.getInitialText(), user.getPrimUuid(), new ArrayList<WfPermission>());
			wfUser.setPermissions(getPermissionsForUser(wfUser));
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		WfComponentProvider.usersCache.put(wfUser.getId(), wfUser);
		return wfUser;
	}

	/**
	 * Status concept to wf state.
	 * 
	 * @param status
	 *            the status
	 * @return the wf state
	 */
	public WfState statusConceptToWfState(I_GetConceptData status) {
		WfState state = WfComponentProvider.statesCache.get(status.getPrimUuid());
		if (state != null) {
			return state;
		}

		try {
			state = new WfState(status.getInitialText(), status.getPrimUuid());
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		WfComponentProvider.statesCache.put(state.getId(), state);
		return state;
	}

	/** The Constant END_FILE. */
	private static final String END_FILE = ".wfd";

	/**
	 * Gets the workflow definition files.
	 * 
	 * @return the workflow definition files
	 */
	public static List<File> getWorkflowDefinitionFiles() {
		File folder = new File("./sampleProcesses");
		return getWorkflowDefinitionFiles(folder);
	}

	/**
	 * Gets the workflow definition files.
	 * 
	 * @return the workflow definition files
	 */
	public static List<File> getWorkflowDefinitionFiles(File folder) {
		List<File> retFiles = loadFiles(folder, END_FILE);
		return retFiles;
	}

	/**
	 * Gets the workflow definitions.
	 * 
	 * @return the workflow definitions
	 */
	public static List<WorkflowDefinition> getWorkflowDefinitions(File folder) {
		List<WorkflowDefinition> definitions = new ArrayList<WorkflowDefinition>();
		List<File> retFiles = loadFiles(folder, END_FILE);

		for (File loopFile : retFiles) {
			definitions.add(TestXstream.readWfDefinition(loopFile));
		}

		return definitions;
	}

	/**
	 * Load files.
	 * 
	 * @param folder
	 *            the folder
	 * @param endFile
	 *            the end file
	 * @return the list
	 */
	private static List<File> loadFiles(File folder, String endFile) {
		File[] fileList = folder.listFiles();
		List<File> retFiles = new ArrayList<File>();
		for (File file : fileList) {
			if (file.isDirectory()) {
				// List<File> tmpFiles = loadFiles(file, endFile);
				// retFiles.addAll(tmpFiles);
			} else {
				if (!file.isHidden() && file.getName().toLowerCase().endsWith(endFile)) {
					retFiles.add(file);
				}
			}
		}
		return retFiles;
	}


    private boolean isPreferredTerm(DescriptionVersionBI<?> desc, ViewCoordinate vc) {
        boolean isPreferredTerm = false;
        try {
        	if (desc.getTypeNid() == SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid()) {
        		 Collection<? extends RefexChronicleBI<?>> annotations = desc.getAnnotations();
        		 
        		 for (RefexChronicleBI<?> annot : annotations) {
        			 // Is it in EN_US Refset?
        			 if (annot.getRefexNid() == RefsetAux.EN_US_REFEX.getLenient().getNid()) {
                        
        				 // Is it versionable?
        				 if (RefexVersionBI.class.isAssignableFrom(annot.getClass())) {
                            RefexVersionBI<?> rv = (RefexVersionBI<?>) annot;

                            // Is it a CidRefset Member?
                            if (RefexNidVersionBI.class.isAssignableFrom(rv.getClass())) {
                                int cnid = ((RefexNidVersionBI<?>) rv).getNid1();
                                
                                // Is the Cid Preferred?
                                if (cnid == SnomedMetadataRfx.getDESC_PREFERRED_NID()) {
                                    isPreferredTerm = true;
                                }
                            } else {
                                System.out.println("Can't convert: RefexCnidVersionBI:  " + rv);
                            }
                        } else {
                            System.out.println("Can't convert: RefexVersionBI:  " + annot);
                        }
                    }
                }
            }
        	
            return isPreferredTerm;
        } catch (IOException e) {
            return isPreferredTerm;
        }
    }
}
