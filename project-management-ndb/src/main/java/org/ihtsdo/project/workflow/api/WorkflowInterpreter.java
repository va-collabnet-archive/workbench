package org.ihtsdo.project.workflow.api;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.DecisionTableConfiguration;
import org.drools.builder.DecisionTableInputType;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatelessKnowledgeSession;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.cement.SNOMED;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfMembership;
import org.ihtsdo.project.workflow.model.WfPermission;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;

public class WorkflowInterpreter {

	public static WorkflowInterpreter createWorkflowInterpreter(
			WorkflowDefinition workflowfDefinition) {
		if (hWfI.containsKey(workflowfDefinition.getName())){
			return hWfI.get(workflowfDefinition.getName()) ;
		}
			
		return new WorkflowInterpreter(workflowfDefinition);
	}

	private static HashMap<String,WorkflowInterpreter> hWfI=new HashMap<String,WorkflowInterpreter>();
	
	private static WorkflowDefinition wfDefinition;
	private KnowledgeBase kbase;
	private StatelessKnowledgeSession ksession;
	private List<String> actions;
	private List<String> prepActions;
	private WorkflowInterpreter(WorkflowDefinition wfDefinition) {
		super();
		this.wfDefinition = wfDefinition;
		
		if (kbase != null && ksession != null) {
			// kbase and ksession are singletons
		} else {
			// Crate knowledge base with decision table
			DecisionTableConfiguration dtableconfiguration =
				KnowledgeBuilderFactory.newDecisionTableConfiguration();
			dtableconfiguration.setInputType( DecisionTableInputType.XLS );

			kbase = KnowledgeBaseFactory.newKnowledgeBase();
			KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbase);

			for (String loopXls : wfDefinition.getXlsFileName()) {
				Resource xlsRes = ResourceFactory.newFileResource(loopXls);
				kbuilder.add( xlsRes,
						ResourceType.DTABLE,
						dtableconfiguration );
			}

			for (String loopDrl : wfDefinition.getDrlFileName()) {
				kbuilder.add(ResourceFactory.newFileResource(loopDrl), 
						ResourceType.DRL);
			}

			if ( kbuilder.hasErrors() ) {
				System.err.print( kbuilder.getErrors() );
			}

			kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
			ksession = kbase.newStatelessKnowledgeSession();
			hWfI.put(wfDefinition.getName(), this);
			
		}

	}

	public WorkflowDefinition getWfDefinition() {
		return wfDefinition;
	}

	public List<WfAction> getPossibleActions(WfInstance instance, WfUser user) {
		List<WfAction> possibleActions = new ArrayList<WfAction>();
		WfComponentProvider cp=new WfComponentProvider();

		//KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);

		actions = new ArrayList<String>();
		ksession.setGlobal("actions", actions);
		prepActions = new ArrayList<String>();
		ksession.setGlobal("prepActions", prepActions);
		ksession.setGlobal("kindOfComputer", new SimpleKindOfComputer());

		ArrayList<Object> facts = new ArrayList<Object>();
		facts.add(instance);
		facts.add(user);
		facts.addAll(cp.getPermissionsForUser(user));
		ksession.execute(facts);

		for (String returnedActionName : actions) {
			for (String loopActionName : wfDefinition.getActions().keySet()) {
				if (loopActionName.equals(returnedActionName)) {
					possibleActions.add(wfDefinition.getActions().get(loopActionName));
				}
			}
		}

		return possibleActions;
	}

	public WfAction getPreparationAction(WfInstance instance, WfUser user) {
		List<WfAction> candidatePrepActions = new ArrayList<WfAction>();
		WfComponentProvider cp=new WfComponentProvider();

		//KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);

		actions = new ArrayList<String>();
		ksession.setGlobal("actions", actions);
		prepActions = new ArrayList<String>();
		ksession.setGlobal("prepActions", prepActions);
		ksession.setGlobal("kindOfComputer", new SimpleKindOfComputer());

		ArrayList<Object> facts = new ArrayList<Object>();
		facts.add(instance);
		facts.add(user);
		facts.addAll(cp.getPermissionsForUser(user));
		ksession.execute(facts);

		for (String returnedActionName : prepActions) {
			for (String loopActionName : wfDefinition.getActions().keySet()) {
				if (loopActionName.equals(returnedActionName)) {
					candidatePrepActions.add(wfDefinition.getActions().get(loopActionName));
				}
			}
		}

		if (candidatePrepActions.isEmpty()) {
			return null;
		} else if (candidatePrepActions.size() ==  1) {
			return candidatePrepActions.iterator().next();
		} else if (candidatePrepActions.size() > 1) {
			// raise exception?
			return candidatePrepActions.iterator().next();
		} else {
			return null;
		}

	}

	public List<WfRole> getNextRole(WfInstance instance, WorkList workList) {
		List<WfRole> roles = new ArrayList<WfRole>();

		for (WfMembership loopMembership : workList.getWorkflowUserRoles()) {
			WfPermission loopPermission = new WfPermission();
			loopPermission.setId(UUID.randomUUID());
			loopPermission.setRole(loopMembership.getRole());
			loopPermission.setHiearchyId(SNOMED.Concept.ROOT.getUids().iterator().next());

			actions = new ArrayList<String>();
			ksession.setGlobal("actions", actions);
			prepActions = new ArrayList<String>();
			ksession.setGlobal("prepActions", prepActions);
			ksession.setGlobal("kindOfComputer", new SimpleKindOfComputer());

			ArrayList<Object> facts = new ArrayList<Object>();
			facts.add(instance);
			facts.add(loopPermission);
			ksession.execute(facts);

			if (actions.size() > 0) {
				roles.add(loopMembership.getRole());
			}

		}

		return roles;
	}

	public WfUser getNextDestination(WfInstance instance, WorkList workList) {
		List<WfRole> nextRoles = getNextRole(instance, workList);
		WfUser nextUser = null;

		for (WfMembership loopWfMember : workList.getWorkflowUserRoles()) {
			if (nextRoles.contains(loopWfMember.getRole())) {
				if (getPossibleActions(instance, loopWfMember.getUser()).size() > 0) {
					if (loopWfMember.isDefaultAssignment()) {
						nextUser = loopWfMember.getUser();
					} else if (nextUser==null){
						nextUser = loopWfMember.getUser();
					}
				}
			}
		}

		return nextUser;
	}

	public List<WfUser> getPossibleDestinations(WfInstance instance, WorkList workList) {
		List<WfUser> possibleUsers = new ArrayList<WfUser>();
		List<WfRole> nextRoles = getNextRole(instance, workList);

		for (WfMembership loopWfMember : workList.getWorkflowUserRoles()) {
			if (nextRoles.contains(loopWfMember.getRole())) {
				if (getPossibleActions(instance, loopWfMember.getUser()).size() > 0) {
					possibleUsers.add(loopWfMember.getUser());
				}
			}
		}

		return possibleUsers;
	}

	public void doAction(WfInstance instance, WfRole role,WfAction action, I_Work worker) {
		// TODO: decide if should check for action is possible
		try {
			BusinessProcess bp;
			ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(action.getBusinessProcess())));
			bp = (BusinessProcess) ois.readObject();
			bp.writeAttachment("WfInstance", instance);
			bp.writeAttachment("WfRole", role);
			bp.writeAttachment("consequenceState", action.getConsequence());
			final I_Work tworker;
			if (worker.isExecuting()) {
				tworker = worker.getTransactionIndependentClone();
				tworker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), Terms.get().getActiveAceFrameConfig());
			} else {
				tworker = worker;

			}

			tworker.execute(bp);

		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

}
