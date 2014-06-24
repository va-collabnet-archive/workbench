/*
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.project.workflow.api.wf2.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.IdentifierSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.helper.report.ReportingHelper;
import org.ihtsdo.project.ProjectPermissionsAPI;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.filter.WfProjectFilter;
import org.ihtsdo.project.filter.WfWorklistFilter;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.api.WorkflowDefinitionManager;
import org.ihtsdo.project.workflow.api.WorkflowInterpreter;
import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfMembership;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidList;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.workflow.api.ProjectBI;
import org.ihtsdo.tk.workflow.api.WfActivityBI;
import org.ihtsdo.tk.workflow.api.WfFilterBI;
import org.ihtsdo.tk.workflow.api.WfProcessDefinitionBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;
import org.ihtsdo.tk.workflow.api.WfRoleBI;
import org.ihtsdo.tk.workflow.api.WfStateBI;
import org.ihtsdo.tk.workflow.api.WfUserBI;
import org.ihtsdo.tk.workflow.api.WorkListBI;
import org.ihtsdo.tk.workflow.api.WorkflowStoreBI;
import org.ihtsdo.tk.workflow.api.ProjectBI.ProjectType;

/**
 * <describe the purpose of this class> <br>
 * <br>
 * Use: <describe its use if not obvious, otherwise remove>.
 */
public class WorkflowStore implements WorkflowStoreBI {

	/** The ts. */
	TerminologyStoreDI ts;

	/** The worklists root. */
	ConceptChronicleBI worklistsRoot;

	/** The wf component provider. */
	WfComponentProvider wfComponentProvider;

	/** The config. */
	I_ConfigAceFrame config;

	/** The permissions api. */
	ProjectPermissionsAPI permissionsApi;

	/**
	 * Instantiates a new workflow store.
	 */
	public WorkflowStore() {
		ts = Ts.get();
		wfComponentProvider = new WfComponentProvider();
		try {
			worklistsRoot = ts.getConcept(UUID.fromString("2facb3a8-6829-314a-9798-ed006930ca18"));
			config = Terms.get().getActiveAceFrameConfig();
			permissionsApi = new ProjectPermissionsAPI(config);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.tk.workflow.api.WorkflowStoreBI#getProcessInstance(org.ihtsdo
	 * .tk.workflow.api.WorkListBI, java.util.UUID)
	 */
	@Override
	public WfProcessInstanceBI getProcessInstance(WorkListBI workList, UUID componentUuid) throws Exception {

		for (WfProcessInstanceBI loopInstance : workList.getInstances()) {
			if (loopInstance.getComponentPrimUuid().equals(componentUuid)) {
				return loopInstance;
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.tk.workflow.api.WorkflowStoreBI#getProcessInstances(java.util
	 * .UUID)
	 */
	@Override
	public Collection<WfProcessInstanceBI> getProcessInstances(UUID componentUuid) throws Exception {
		Collection<WfProcessInstanceBI> instances = new ArrayList<WfProcessInstanceBI>();
		ConceptChronicleBI concept = Ts.get().getConcept(componentUuid);
		Collection<? extends RefexVersionBI<?>> annotations = concept.getAnnotationsActive(config.getViewCoordinate());
		I_TermFactory tf = Terms.get();
		for (RefexVersionBI loopAnnot : annotations) {
			I_GetConceptData refset = tf.getConcept(loopAnnot.getRefexNid());
			if (ts.isKindOf(refset.getConceptNid(), worklistsRoot.getConceptNid(), config.getViewCoordinate())) {
				I_IntSet allowedTypes = tf.newIntSet();
				allowedTypes.add(RefsetAuxiliary.Concept.PROMOTION_REL.localize().getNid());
				Set<? extends I_GetConceptData> sources = refset.getDestRelOrigins(config.getAllowedStatus(), allowedTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());

				if (!sources.isEmpty()) {
					I_GetConceptData wRefset = sources.iterator().next();
					WorkList wlist = TerminologyProjectDAO.getWorkList(wRefset, config);
					if (wlist != null) {
						instances.add(TerminologyProjectDAO.getWorkListMember(tf.getConcept(concept.getNid()), wlist, config).getWfInstance());
					}
				}

			}
		}
		return instances;
	}

	/**
	 * Gets the {@link Collection} representing the process instances.
	 * 
	 * @param concept
	 *            the concept
	 * @return the {@link Collection}
	 * @throws Exception
	 *             the exception
	 */
	public Collection<WfProcessInstanceBI> getProcessInstances(ConceptChronicleBI concept) throws Exception {
		Collection<WfProcessInstanceBI> instances = new ArrayList<WfProcessInstanceBI>();
		Collection<? extends RefexVersionBI<?>> annotations = concept.getAnnotationsActive(config.getViewCoordinate());
		I_TermFactory tf = Terms.get();
		for (RefexVersionBI loopAnnot : annotations) {
			I_GetConceptData refset = tf.getConcept(loopAnnot.getRefexNid());
			if (ts.isKindOf(refset.getConceptNid(), worklistsRoot.getConceptNid(), config.getViewCoordinate())) {
				I_IntSet allowedTypes = tf.newIntSet();
				allowedTypes.add(RefsetAuxiliary.Concept.PROMOTION_REL.localize().getNid());
				Set<? extends I_GetConceptData> sources = refset.getDestRelOrigins(config.getAllowedStatus(), allowedTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());

				if (!sources.isEmpty()) {
					I_GetConceptData wRefset = sources.iterator().next();
					WorkList wlist = TerminologyProjectDAO.getWorkList(wRefset, config);
					if (wlist != null) {
						instances.add(TerminologyProjectDAO.getWorkListMember(tf.getConcept(concept.getNid()), wlist, config).getWfInstance());
					}
				}

			}
		}
		return instances;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.tk.workflow.api.WorkflowStoreBI#searchWorkflow(java.util.
	 * Collection)
	 */
	@Override
	public Collection<WfProcessInstanceBI> searchWorkflow(Collection<WfFilterBI> filters) throws Exception {
		boolean worklistOrProjectFilter = isProjectFilter(filters);
		Collection<WfProcessInstanceBI> result = new ArrayList<WfProcessInstanceBI>();
		if (worklistOrProjectFilter) {
			getInstancesForProjectFilter(filters, result);
		} else {
			WfInstanceSearcher instanceSearchWorker = new WfInstanceSearcher(filters, null, new CancelSearch());
			instanceSearchWorker.execute();
			try {
				return instanceSearchWorker.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * Checks if is project filter.
	 * 
	 * @param filters
	 *            the filters
	 * @return true, if is project filter
	 */
	private boolean isProjectFilter(Collection<WfFilterBI> filters) {
		boolean worklistOrProjectFilter = false;
		for (WfFilterBI filter : filters) {
			if (filter instanceof WfWorklistFilter || filter instanceof WfProjectFilter) {
				worklistOrProjectFilter = true;
			}
		}
		return worklistOrProjectFilter;
	}

	/**
	 * Asynchronously searches workflow instances to workflow instance
	 * container.
	 * 
	 * @param filters
	 *            for workflow instances.
	 * @param wfinstanceCont
	 *            WfInstanceContainer used to add Asynchronously the filtered
	 *            instances.
	 * @param propertyChangeListener
	 *            Progress listener to update search progress.
	 * @param keepSearching
	 *            the keep searching
	 * @return whatever.
	 * @throws Exception
	 *             the exception
	 */
	public Collection<WfProcessInstanceBI> searchWorkflow(Collection<WfFilterBI> filters, WfInstanceContainer wfinstanceCont, PropertyChangeListener propertyChangeListener, CancelSearch keepSearching) throws Exception {
		boolean worklistOrProjectFilter = isProjectFilter(filters);

		if (worklistOrProjectFilter) {
			Collection<WfProcessInstanceBI> result = new ArrayList<WfProcessInstanceBI>();
			getInstancesForProjectFilter(filters, result);
			keepSearching.cancel(true);
			return result;
		} else {
			WfInstanceSearcher instanceSearchWorker = new WfInstanceSearcher(filters, wfinstanceCont, keepSearching);
			instanceSearchWorker.addPropertyChangeListener(propertyChangeListener);
			instanceSearchWorker.execute();
		}
		return null;
	}

	/**
	 * Gets the {@link void} representing the instances for project filter.
	 * 
	 * @param filters
	 *            the filters
	 * @param result
	 *            the result
	 * @return the {@link void}
	 * @throws TerminologyException
	 *             the terminology exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws Exception
	 *             the exception
	 */
	private void getInstancesForProjectFilter(Collection<WfFilterBI> filters, Collection<WfProcessInstanceBI> result) throws TerminologyException, IOException, Exception {
		Set<WfProcessInstanceBI> projectSet = new HashSet<WfProcessInstanceBI>();
		Set<WfProcessInstanceBI> worklistSet = new HashSet<WfProcessInstanceBI>();

		Set<UUID> projects = new HashSet<UUID>();
		for (WfFilterBI wfFilterBI : filters) {
			if (wfFilterBI instanceof WfProjectFilter) {
				projects.add(((WfProjectFilter) wfFilterBI).getProjectUUID());
			}
		}

		Set<UUID> wlUuids = new HashSet<UUID>();
		for (WfFilterBI wfFilterBI : filters) {
			if (wfFilterBI instanceof WfWorklistFilter) {
				wlUuids.add(((WfWorklistFilter) wfFilterBI).getWorklistUUID());
			}
		}
		for (UUID wlUuid : wlUuids) {
			WorkListBI worklist = getWorklist(wlUuid);
			Collection<WfProcessInstanceBI> instances = worklist.getInstances();
			for (WfProcessInstanceBI wfProcessInstanceBI : instances) {
				boolean passed = true;
				for (WfFilterBI filter : filters) {
					if (!filter.evaluateInstance(wfProcessInstanceBI)) {
						passed = false;
					}
				}
				if (passed) {
					worklistSet.add(wfProcessInstanceBI);
				}

			}
		}

		for (UUID uuid : projects) {
			ProjectBI project = getProject(uuid);
			Collection<WorkListBI> worklists = project.getWorkLists();
			for (WorkListBI workListBI : worklists) {
				Collection<WfProcessInstanceBI> instances = workListBI.getInstances();
				for (WfProcessInstanceBI wfProcessInstanceBI : instances) {
					boolean passed = true;
					for (WfFilterBI filter : filters) {
						if (!filter.evaluateInstance(wfProcessInstanceBI)) {
							passed = false;
						}
					}
					if (passed) {
						projectSet.add(wfProcessInstanceBI);
					}
				}
			}
		}

		if (worklistSet.isEmpty()) {
			result.addAll(projectSet);
		} else if (projectSet.isEmpty()) {
			result.addAll(worklistSet);
		} else {
			for (WfProcessInstanceBI projectInstance : projectSet) {
				if (worklistSet.contains(projectInstance)) {
					result.add(projectInstance);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.tk.workflow.api.WorkflowStoreBI#getAllUsers()
	 */
	@Override
	public Collection<WfUserBI> getAllUsers() {
		List<WfUserBI> users = new ArrayList<WfUserBI>();
		users.addAll(wfComponentProvider.getUsers());
		return users;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.tk.workflow.api.WorkflowStoreBI#getAllStates()
	 */
	@Override
	public Collection<WfStateBI> getAllStates() {
		List<WfStateBI> states = new ArrayList<WfStateBI>();
		states.addAll(wfComponentProvider.getAllStates());
		return states;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.tk.workflow.api.WorkflowStoreBI#getAllActivities()
	 */
	@Override
	public Collection<WfActivityBI> getAllActivities() {
		// TODO: Implement actions as an external component
		List<WfActivityBI> activities = new ArrayList<WfActivityBI>();
		return activities;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.tk.workflow.api.WorkflowStoreBI#getAllRoles()
	 */
	@Override
	public Collection<WfRoleBI> getAllRoles() {
		List<WfRoleBI> roles = new ArrayList<WfRoleBI>();
		roles.addAll(wfComponentProvider.getRoles());
		return roles;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.tk.workflow.api.WorkflowStoreBI#getAllProcessDefinitions()
	 */
	@Override
	public Collection<WfProcessDefinitionBI> getAllProcessDefinitions() {
		List<WfProcessDefinitionBI> definitions = new ArrayList<WfProcessDefinitionBI>();
		for (File loopWfDefFile : WfComponentProvider.getWorkflowDefinitionFiles()) {
			WorkflowDefinition loopWfDef = WorkflowDefinitionManager.readWfDefinition(loopWfDefFile.getAbsolutePath());
			definitions.add(new WfProcessDefinition(loopWfDef));
		}
		return definitions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.tk.workflow.api.WorkflowStoreBI#getAllProjects()
	 */
	@Override
	public Collection<ProjectBI> getAllProjects() throws Exception {
		List<ProjectBI> projects = new ArrayList<ProjectBI>();
		for (I_TerminologyProject loopProject : TerminologyProjectDAO.getAllProjects(config)) {
			projects.add(new Project(loopProject));
		}
		return projects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.tk.workflow.api.WorkflowStoreBI#getActiveProcessInstances(
	 * java.util.UUID)
	 */
	@Override
	public Collection<WfProcessInstanceBI> getActiveProcessInstances(UUID componentUuid) throws Exception {
		Collection<WfProcessInstanceBI> instances = new ArrayList<WfProcessInstanceBI>();
		for (WfProcessInstanceBI loopInstance : getProcessInstances(componentUuid)) {
			if (loopInstance.isActive()) {
				instances.add(loopInstance);
			}
		}
		return instances;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.tk.workflow.api.WorkflowStoreBI#getIncompleteProcessInstances
	 * (java.util.UUID)
	 */
	@Override
	public Collection<WfProcessInstanceBI> getIncompleteProcessInstances(UUID componentUuid) throws Exception {
		Collection<WfProcessInstanceBI> instances = new ArrayList<WfProcessInstanceBI>();
		for (WfProcessInstanceBI loopInstance : getProcessInstances(componentUuid)) {
			if (loopInstance.isActive() && !loopInstance.isCompleted()) {
				instances.add(loopInstance);
			}
		}
		return instances;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.tk.workflow.api.WorkflowStoreBI#createProject(java.lang.String
	 * , org.ihtsdo.tk.workflow.api.ProjectBI.ProjectType)
	 */
	@Override
	public ProjectBI createProject(String name, ProjectBI.ProjectType type) throws Exception {
		if (type.equals(ProjectType.TRANSLATION)) {
			return new Project(TerminologyProjectDAO.createNewTranslationProject(name, config));
		} else if (type.equals(ProjectType.TERMINOLOGY)) {
			return new Project(TerminologyProjectDAO.createNewTerminologyProject(name, config));
		} else if (type.equals(ProjectType.MAPPING)) {
			return new Project(TerminologyProjectDAO.createNewMappingProject(name, config));
		} else {
			return null;
		}
	}

	/**
	 * Creates the translation project.
	 * 
	 * @param name
	 *            the name
	 * @return the project bi
	 * @throws Exception
	 *             the exception
	 */
	public ProjectBI createTranslationProject(String name) throws Exception {
		return new Project(TerminologyProjectDAO.createNewTerminologyProject(name, config));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.tk.workflow.api.WorkflowStoreBI#getActivities(org.ihtsdo.tk
	 * .workflow.api.WfProcessInstanceBI, org.ihtsdo.tk.workflow.api.WfUserBI)
	 */
	@Override
	public Collection<WfActivityBI> getActivities(WfProcessInstanceBI instance, WfUserBI user) throws Exception {

		List<WfActivityBI> activities = new ArrayList<WfActivityBI>();
		
		if (!((WorkList) instance.getWorkList()).getUsers().contains(user)) {
			return activities;
		}
		
		WorkflowDefinition oldStyleDefinition = ((WfProcessDefinition) instance.getProcessDefinition()).getDefinition();
		WorkflowInterpreter interpreter = WorkflowInterpreter.createWorkflowInterpreter(oldStyleDefinition);

		List<WfAction> possibleActions = interpreter.getPossibleActionsInWorklist((WfInstance) instance, (WfUser) user);
		List<WfAction> autoActions = interpreter.getAutomaticActions((WfInstance) instance, (WfUser) user);
		
 		for (WfAction loopAction : possibleActions) {
			WfActivity loopActivity = new WfActivity(loopAction);
			loopActivity.setAutomatic(false);
			activities.add(loopActivity);
		}

		for (WfAction loopAction : autoActions) {
			WfActivity loopActivity = new WfActivity(loopAction);
			loopActivity.setAutomatic(true);
			activities.add(loopActivity);
		}
		

		return activities;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.tk.workflow.api.WorkflowStoreBI#getWorklist(java.util.UUID)
	 */
	@Override
	public WorkListBI getWorklist(UUID worklistUuid) throws Exception {
		return TerminologyProjectDAO.getWorkList(Terms.get().getConcept(worklistUuid), config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.tk.workflow.api.WorkflowStoreBI#getProject(java.util.UUID)
	 */
	@Override
	public ProjectBI getProject(UUID projectUuid) throws Exception {
		return new Project(TerminologyProjectDAO.getProject(Terms.get().getConcept(projectUuid), config));
	}

	/**
	 * Send all changes in range to workflow.
	 * 
	 * @param startTime
	 *            the time and date, in the form MM/dd/yy HH:mm:ss
	 * @param endTime
	 *            the time and date, in the form MM/dd/yy HH:mm:ss
	 * @throws Exception
	 */
	public void sendAllChangesInTimeRangeToDefaultWorkflow(String startTime, String endTime) throws Exception {
		WorkflowInitiator initiator = new WorkflowInitiator();

		System.out.println("Starting evaluation: " + startTime + " to " +  endTime);
		NidList changedNids = ReportingHelper.getChangedConceptNids(startTime, endTime);
		System.out.println("Changed Nids: " + changedNids.size());
		Iterator<Integer> nidsIterator = changedNids.iterator();
//		ConceptChronicleBI rootConcept = Ts.get().getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));
		Set<ConceptChronicleBI> nonRootConcepts = new HashSet<ConceptChronicleBI>();
		nonRootConcepts.add(Ts.get().getConcept(UUID.fromString("1c698388-c309-3dfa-96f0-86248753fac5")));
		nonRootConcepts.add(Ts.get().getConcept(UUID.fromString("f4d2fabc-7e96-3b3a-a348-ae867ba74029")));

		int total = changedNids.size();
		int count = 0;
		while (nidsIterator.hasNext()) {
			count++;
			I_RepresentIdSet nidsToEvaluate = new IdentifierSet();
			Integer loopNid = nidsIterator.next();
			ConceptChronicleBI loopConcept = Ts.get().getConcept(loopNid);
//			boolean isKindOfSnomed = Ts.get().isKindOf(loopConcept.getConceptNid(), rootConcept.getConceptNid(), config.getViewCoordinate());
			
			boolean nonRootConceptModification = false;
			for (ConceptChronicleBI nonRoot : nonRootConcepts) {
				if (Ts.get().isKindOf(loopConcept.getConceptNid(), nonRoot.getConceptNid(), config.getViewCoordinate())) {
					nonRootConceptModification = true;
				}
			}

			if (nonRootConceptModification) {
				System.out.print("Sending: " + loopNid);
				System.out.println(" - Concept: " + loopConcept.toString());
				nidsToEvaluate.setMember(loopNid);
				initiator.evaluateForWorkflowInitiation(new PropertyChangeEvent(this, "diff match", null, nidsToEvaluate));
				System.out.println("Concept finished." + (total-count) + " more to go...");
				Thread.sleep(1000);
			} else {
				System.out.print("Skipping: " + loopNid);
				System.out.println(" - Concept: " + loopConcept.toString() + " | " + (total-count) + " more to go...");
			}
		}
		System.out.print("Workflow initiation finished.");
	}
}
