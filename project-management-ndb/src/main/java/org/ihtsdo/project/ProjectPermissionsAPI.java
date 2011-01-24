package org.ihtsdo.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.Precedence;

public class ProjectPermissionsAPI {

	private I_ConfigAceFrame config;

	public ProjectPermissionsAPI(I_ConfigAceFrame config) {
		super();
		this.config = config;
	}

	public I_ConfigAceFrame getConfig() {
		return config;
	}

	public void setConfig(I_ConfigAceFrame config) {
		this.config = config;
	}

	public void addPermission(I_GetConceptData user, I_GetConceptData permission, I_GetConceptData domain
	) throws Exception {
		I_TermFactory tf = Terms.get();

		I_RelVersioned relVersioned = tf.newRelationship(UUID.randomUUID(), user, permission, domain, 
				tf.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()),
				tf.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()),
				tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

		tf.addUncommittedNoChecks(user);
		tf.commit();
	}

	public void removePermission(I_GetConceptData user, I_GetConceptData permission, I_GetConceptData domain
	) throws Exception {
		I_IntSet allowedTypes = Terms.get().newIntSet();
		allowedTypes.add(permission.getConceptNid());

		List<? extends I_RelTuple> relationships = user.getSourceRelTuples(
				config.getAllowedStatus(),
				allowedTypes,
				config.getViewPositionSetReadOnly(),
				Precedence.TIME,
				config.getConflictResolutionStrategy());
		for (I_RelTuple rel : relationships) {
			removePermission(rel);
		}

	}

	public void removePermission(I_RelTuple relationship) throws Exception {
		I_TermFactory tf = Terms.get();
		I_GetConceptData concept = tf.getConcept(relationship.getC1Id());

		for (PathBI editPath : config.getEditingPathSet()) {
			I_RelPart newPart = (I_RelPart) relationship.getMutablePart().makeAnalog(
					ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
					editPath.getConceptNid(),
					Long.MAX_VALUE);
			relationship.getFixedPart().addVersion(newPart);
			tf.addUncommittedNoChecks(concept);
			tf.commit();
		}

	}

	public boolean checkPermissionForHierarchy(I_GetConceptData user, I_GetConceptData target, 
			I_GetConceptData permission) throws IOException, TerminologyException {
		boolean permisionGranted = false;
		I_TermFactory tf = Terms.get();

		I_IntSet isaType = tf.newIntSet();
		isaType.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());

		I_IntSet allowedTypes = tf.newIntSet();
		allowedTypes.add(permission.getConceptNid());

		for (I_GetConceptData parent : permission.getSourceRelTargets(
				config.getAllowedStatus(), 
				allowedTypes, config.getViewPositionSetReadOnly(),
				Precedence.TIME, config.getConflictResolutionStrategy())) {
			allowedTypes.add(parent.getConceptNid());
		}

		List<? extends I_RelTuple> relationships = user.getSourceRelTuples(
				config.getAllowedStatus(), 
				allowedTypes, config.getViewPositionSetReadOnly(),
				Precedence.TIME, config.getConflictResolutionStrategy());
		for (I_RelTuple rel : relationships) {
			I_GetConceptData relC2 = tf.getConcept(rel.getC2Id());
			if (relC2.isParentOfOrEqualTo(target)) {
				permisionGranted = true;
			}
		}

		return permisionGranted;
	}

	public boolean checkPermissionForProject(I_GetConceptData user, I_GetConceptData target, 
			I_GetConceptData permission) throws IOException, TerminologyException {
		boolean permisionGranted = false;
		long latestVersion = Long.MIN_VALUE;

		I_IntSet isaType = Terms.get().newIntSet();
		isaType.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());

		I_IntSet allowedTypes = Terms.get().newIntSet();
		allowedTypes.add(permission.getConceptNid());

		for (I_GetConceptData parent : permission.getSourceRelTargets(
				config.getAllowedStatus(),
				isaType,
				config.getViewPositionSetReadOnly(),
				Precedence.TIME,
				config.getConflictResolutionStrategy())) {
			allowedTypes.add(parent.getConceptNid());
		}

		I_RelTuple lastestTuple = null;
		List<? extends I_RelTuple> relationships = user.getSourceRelTuples(
				config.getAllowedStatus(),
				allowedTypes,
				config.getViewPositionSetReadOnly(),
				Precedence.TIME,
				config.getConflictResolutionStrategy());
		for (I_RelTuple rel : relationships) {
			if (rel.getC2Id() == target.getConceptNid()) {
				if (rel.getTime() > latestVersion) {
					latestVersion = rel.getTime();
					lastestTuple = rel;
				}
			}
		}

		if (lastestTuple != null) {
			if (isActive(lastestTuple.getStatusNid())) {
				permisionGranted = true;
			}
		}

		return permisionGranted;
	}

	public Set<I_GetConceptData> getUsersForRole(I_GetConceptData role, I_GetConceptData project
	) throws IOException, TerminologyException {

		Set<I_GetConceptData> returnUsers = new HashSet<I_GetConceptData>();
		Set<I_GetConceptData> allUsers = getUsers();

		for (I_GetConceptData user : allUsers) {
			if (checkPermissionForProject(user, project, role)) {
				returnUsers.add(user);
			}
		}

		return returnUsers;
	}

	/**
	 * Calculates a set of valid users - a user is valid is they are a child of the User concept in the top hierarchy,
	 * and have a description of type "user inbox".
	 * 
	 * @return The set of valid users.
	 */
	public Set<I_GetConceptData> getUsers() {
		HashSet<I_GetConceptData> validUsers = new HashSet<I_GetConceptData>();
		try {
			I_GetConceptData userParent =
				Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER.getUids());

			I_IntSet allowedTypes = Terms.get().getActiveAceFrameConfig().getDestRelTypes();
			I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
			Set<Integer> currentStatuses = helper.getCurrentStatusIds();

			Set<? extends I_GetConceptData> allUsers = userParent.getDestRelOrigins(config.getAllowedStatus(),
					allowedTypes, config.getViewPositionSetReadOnly(), Precedence.TIME,
					config.getConflictResolutionStrategy());
			I_GetConceptData descriptionType =
				Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER_INBOX.getUids());
			I_IntSet descAllowedTypes = Terms.get().newIntSet();
			descAllowedTypes.add(descriptionType.getConceptNid());

			for (I_GetConceptData user : allUsers) {

				I_DescriptionTuple latestTuple = null;
				long latestVersion = Long.MIN_VALUE;

				List<? extends I_DescriptionTuple> descriptionResults =
					user.getDescriptionTuples(null, descAllowedTypes, Terms.get()
							.getActiveAceFrameConfig().getViewPositionSetReadOnly(),
							Precedence.TIME, config.getConflictResolutionStrategy());
				for (I_DescriptionTuple descriptionTuple : descriptionResults) {
					if (descriptionTuple.getTime() > latestVersion) {
						latestVersion = descriptionTuple.getTime();
						latestTuple = descriptionTuple;
					}
				}
				if (latestTuple != null) {
					for (int currentStatusId : currentStatuses) {
						if (latestTuple.getStatusNid() == currentStatusId) {
							validUsers.add(user);
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return validUsers;
	}

	public Set<String> getUsersInboxAddresses() {
		HashSet<String> inboxes = new HashSet<String>();
		try {
			I_GetConceptData userParent =
				Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER.getUids());

			I_IntSet allowedTypes = Terms.get().getActiveAceFrameConfig().getDestRelTypes();
			I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
			Set<Integer> currentStatuses = helper.getCurrentStatusIds();

			Set<? extends I_GetConceptData> allUsers = userParent.getDestRelOrigins(config.getAllowedStatus(),
					allowedTypes, config.getViewPositionSetReadOnly(), Precedence.TIME,
					config.getConflictResolutionStrategy());
			I_GetConceptData descriptionType =
				Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER_INBOX.getUids());
			I_IntSet descAllowedTypes = Terms.get().newIntSet();
			descAllowedTypes.add(descriptionType.getConceptNid());

			for (I_GetConceptData user : allUsers) {

				I_DescriptionTuple latestTuple = null;
				long latestVersion = Long.MIN_VALUE;

				List<? extends I_DescriptionTuple> descriptionResults =
					user.getDescriptionTuples(null, descAllowedTypes, Terms.get()
							.getActiveAceFrameConfig().getViewPositionSetReadOnly(),
							Precedence.TIME, config.getConflictResolutionStrategy());
				for (I_DescriptionTuple descriptionTuple : descriptionResults) {
					if (descriptionTuple.getTime() > latestVersion) {
						latestVersion = descriptionTuple.getTime();
						latestTuple = descriptionTuple;
					}
				}
				if (latestTuple != null) {
					for (int currentStatusId : currentStatuses) {
						if (latestTuple.getStatusNid() == currentStatusId) {
							inboxes.add(latestTuple.getText());
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return inboxes;
	}
	public Set<String> getUsersInboxAddressesForRole(I_GetConceptData role, I_GetConceptData project) {
		HashSet<String> inboxes = new HashSet<String>();
		try {
			I_GetConceptData userParent =
				Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER.getUids());
			
			I_IntSet allowedTypes = Terms.get().getActiveAceFrameConfig().getDestRelTypes();
			I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
			Set<Integer> currentStatuses = helper.getCurrentStatusIds();

			Set<? extends I_GetConceptData> allUsers = userParent.getDestRelOrigins(config.getAllowedStatus(),
					allowedTypes, config.getViewPositionSetReadOnly(), Precedence.TIME,
					config.getConflictResolutionStrategy());
			I_GetConceptData descriptionType =
				Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER_INBOX.getUids());
			I_IntSet descAllowedTypes = Terms.get().newIntSet();
			descAllowedTypes.add(descriptionType.getConceptNid());

			for (I_GetConceptData user : allUsers) {
				if (checkPermissionForProject(user, project, role)) {
					I_DescriptionTuple latestTuple = null;
					long latestVersion = Long.MIN_VALUE;

					List<? extends I_DescriptionTuple> descriptionResults =
						user.getDescriptionTuples(null, descAllowedTypes, Terms.get()
								.getActiveAceFrameConfig().getViewPositionSetReadOnly(),
								Precedence.TIME, config.getConflictResolutionStrategy());
					for (I_DescriptionTuple descriptionTuple : descriptionResults) {
						if (descriptionTuple.getTime() > latestVersion) {
							latestVersion = descriptionTuple.getTime();
							latestTuple = descriptionTuple;
						}
					}
					if (latestTuple != null) {
						for (int currentStatusId : currentStatuses) {
							if (latestTuple.getStatusNid() == currentStatusId) {
								inboxes.add(latestTuple.getText());
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return inboxes;
	}

	public boolean isActive(int statusId) {
		List<Integer> activeStatuses = new ArrayList<Integer>();
		try {
			activeStatuses.add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			activeStatuses.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			activeStatuses.add(ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return (activeStatuses.contains(statusId));
	}

	public boolean isInactive(int statusId) {
		List<Integer> inactiveStatuses = new ArrayList<Integer>();
		try {
			inactiveStatuses.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
			inactiveStatuses.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return (inactiveStatuses.contains(statusId));
	}

}
