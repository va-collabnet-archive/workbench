package org.ihtsdo.project.workflow.api.wf2.implementation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.ihtsdo.project.ProjectPermissionsAPI;
import org.ihtsdo.project.TerminologyProjectAuxiliary;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.api.WorkflowDefinitionManager;
import org.ihtsdo.project.workflow.api.WorkflowInterpreter;
import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;
import org.ihtsdo.project.workflow2.ProjectBI;
import org.ihtsdo.project.workflow2.WfActivityBI;
import org.ihtsdo.project.workflow2.WfFilterBI;
import org.ihtsdo.project.workflow2.WfProcessDefinitionBI;
import org.ihtsdo.project.workflow2.WfProcessInstanceBI;
import org.ihtsdo.project.workflow2.WfRoleBI;
import org.ihtsdo.project.workflow2.WfStateBI;
import org.ihtsdo.project.workflow2.WfUserBI;
import org.ihtsdo.project.workflow2.WorkListBI;
import org.ihtsdo.project.workflow2.WorkflowStoreBI;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;

public class WorkflowStore implements WorkflowStoreBI {

	TerminologyStoreDI ts;
	ConceptChronicleBI worklistsRoot;
	WfComponentProvider wfComponentProvider;
	I_ConfigAceFrame config;
	ProjectPermissionsAPI permissionsApi;

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
	
	@Override
	public WfProcessInstanceBI getProcessInstance(
			WorkListBI workList, UUID componentUuid) throws Exception {
		
		for (WfProcessInstanceBI loopInstance : workList.getInstances()) {
			if (loopInstance.getComponentPrimUuid().equals(componentUuid)) {
				return loopInstance;
			}
		}
		
		return null;
	}

	@Override
	public Collection<WfProcessInstanceBI> getProcessInstances(UUID componentUuid) throws Exception {
		Collection<WfProcessInstanceBI> instances = new ArrayList<WfProcessInstanceBI>();
		ConceptChronicleBI concept = Ts.get().getConcept(componentUuid);
		Collection<? extends RefexVersionBI<?>> annotations = concept.getAnnotationsActive(config.getViewCoordinate());
		I_TermFactory tf = Terms.get();
		for (RefexVersionBI loopAnnot : annotations) {
			I_GetConceptData refset = tf.getConcept(loopAnnot.getRefexNid());
			if (ts.isKindOf(refset.getConceptNid(), worklistsRoot.getConceptNid(), 
					config.getViewCoordinate())) {
				I_IntSet allowedTypes = tf.newIntSet(); 
				allowedTypes.add(RefsetAuxiliary.Concept.PROMOTION_REL.localize().getNid());
				Set<? extends I_GetConceptData> sources = refset.getDestRelOrigins(config.getAllowedStatus(), 
						allowedTypes, 
						config.getViewPositionSetReadOnly(), 
						config.getPrecedence(), 
						config.getConflictResolutionStrategy());

				if (!sources.isEmpty()) {
					I_GetConceptData wRefset = sources.iterator().next();
					WorkList wlist = TerminologyProjectDAO.getWorkList(wRefset, config);
					if (wlist != null) {
						instances.add(TerminologyProjectDAO.getWorkListMember(
								tf.getConcept(concept.getNid()), wlist, config).getWfInstance());
					}
				}

			}
		}
		return instances;
	}

	@Override
	public Collection<WfProcessInstanceBI> searchWorkflow(Collection<WfFilterBI> filters) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<WfUserBI> getAllUsers() {
		List<WfUserBI> users = new ArrayList<WfUserBI>();
		users.addAll(wfComponentProvider.getUsers());
		return users;
	}

	@Override
	public Collection<WfStateBI> getAllStates() {
		List<WfStateBI> states = new ArrayList<WfStateBI>();
		states.addAll(wfComponentProvider.getAllStates());
		return states;
	}

	@Override
	public Collection<WfActivityBI> getAllActivities() {
		//TODO: Implement actions as an external component
		List<WfActivityBI> activities = new ArrayList<WfActivityBI>();
		return activities;
	}

	@Override
	public Collection<WfRoleBI> getAllRoles() {
		List<WfRoleBI> roles = new ArrayList<WfRoleBI>();
		roles.addAll(wfComponentProvider.getRoles());
		return roles;
	}

	@Override
	public Collection<WfProcessDefinitionBI> getAllProcessDefinitions() {
		List<WfProcessDefinitionBI> definitions = new ArrayList<WfProcessDefinitionBI>();
		for (File loopWfDefFile : WfComponentProvider.getWorkflowDefinitionFiles()) {
			WorkflowDefinition loopWfDef = WorkflowDefinitionManager.readWfDefinition(loopWfDefFile.getAbsolutePath());
			definitions.add(new WfProcessDefinition(loopWfDef));
		}
		return definitions;
	}

	@Override
	public Collection<ProjectBI> getAllProjects() throws Exception {
		List<ProjectBI> projects = new ArrayList<ProjectBI>();
		for (I_TerminologyProject loopProject : TerminologyProjectDAO.getAllProjects(config)) {
			projects.add(new Project(loopProject));
		}
		return projects;
	}

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

	@Override
	public ProjectBI createProject(String name) throws Exception {
		return new Project(TerminologyProjectDAO.createNewTranslationProject(name, config));
	}

	@Override
	public ProjectBI createTranslationProject(String name) throws Exception {
		return new Project(TerminologyProjectDAO.createNewTerminologyProject(name, config));
	}

	@Override
	public Collection<WfActivityBI> getActivities(WfProcessInstanceBI instance,
			WfUserBI user) throws Exception {
		
		List<WfActivityBI> activities = new ArrayList<WfActivityBI>();
		
		WorkflowDefinition oldStyleDefinition = ((WfProcessDefinition) instance.getProcessDefinition()).getDefinition();
		WorkflowInterpreter interpreter = WorkflowInterpreter.createWorkflowInterpreter(oldStyleDefinition);
		
		List<WfAction> possibleActions = interpreter.getPossibleActions((WfInstance) instance, (WfUser) user);
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

}
