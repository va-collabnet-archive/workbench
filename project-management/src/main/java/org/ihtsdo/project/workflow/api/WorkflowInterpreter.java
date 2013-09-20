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
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.cement.SNOMED;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfMembership;
import org.ihtsdo.project.workflow.model.WfPermission;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;

/**
 * The Class WorkflowInterpreter.
 */
public class WorkflowInterpreter {

	private static final String BATCH_OPERATION = "Batch operation";

	/**
	 * Creates the workflow interpreter.
	 * 
	 * @param workflowfDefinition
	 *            the workflowf definition
	 * @return the workflow interpreter
	 */
	public static WorkflowInterpreter createWorkflowInterpreter(WorkflowDefinition workflowfDefinition) {
		if (hWfI.containsKey(workflowfDefinition.getName())) {
			return hWfI.get(workflowfDefinition.getName());
		}

		return new WorkflowInterpreter(workflowfDefinition);
	}
	
	/**
	 * Creates the workflow interpreter.
	 * 
	 * @param workflowfDefinition
	 *            the workflowf definition
	 * @return the workflow interpreter
	 */
	public static WorkflowInterpreter createFreshWorkflowInterpreter(WorkflowDefinition workflowfDefinition) {
		return new WorkflowInterpreter(workflowfDefinition);
	}

	/** The h wf i. */
	private static HashMap<String, WorkflowInterpreter> hWfI = new HashMap<String, WorkflowInterpreter>();

	/** The wf definition. */
	private WorkflowDefinition wfDefinition;

	/** The kbase. */
	private KnowledgeBase kbase;

	/** The ksession. */
	private StatelessKnowledgeSession ksession;

	/** The actions. */
	private List<String> actions;

	/** The automatic actions. */
	private List<String> autoActions;

	/** The prep actions. */
	private List<String> prepActions;

	/**
	 * Instantiates a new workflow interpreter.
	 * 
	 * @param wfDefinition
	 *            the wf definition
	 */
	private WorkflowInterpreter(WorkflowDefinition wfDefinition) {
		super();
		this.wfDefinition = wfDefinition;

		if (kbase != null && ksession != null) {
			// kbase and ksession are singletons
		} else {
			// Crate knowledge base with decision table
			DecisionTableConfiguration dtableconfiguration = KnowledgeBuilderFactory.newDecisionTableConfiguration();
			dtableconfiguration.setInputType(DecisionTableInputType.XLS);

			kbase = KnowledgeBaseFactory.newKnowledgeBase();
			KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbase);

			for (String loopXls : wfDefinition.getXlsFileName()) {
				Resource xlsRes = ResourceFactory.newFileResource(loopXls);
				kbuilder.add(xlsRes, ResourceType.DTABLE, dtableconfiguration);
			}

			for (String loopDrl : wfDefinition.getDrlFileName()) {
				kbuilder.add(ResourceFactory.newFileResource(loopDrl), ResourceType.DRL);
			}

			if (kbuilder.hasErrors()) {
				System.err.print(kbuilder.getErrors());
			}

			kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
			ksession = kbase.newStatelessKnowledgeSession();
			hWfI.put(wfDefinition.getName(), this);

		}

	}

	/**
	 * Gets the wf definition.
	 * 
	 * @return the wf definition
	 */
	public WorkflowDefinition getWfDefinition() {
		return wfDefinition;
	}

	/**
	 * Gets the possible actions.
	 * 
	 * @param instance
	 *            the instance
	 * @param user
	 *            the user
	 * @return the possible actions
	 */
	public List<WfAction> getPossibleActionsInWorklist(WfInstance instance, WfUser user) {
		List<WfAction> possibleActions = new ArrayList<WfAction>();
		WfComponentProvider cp = new WfComponentProvider();

		// KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);

		actions = new ArrayList<String>();
		ksession.setGlobal("actions", actions);
		autoActions = new ArrayList<String>();
		ksession.setGlobal("autoActions", autoActions);
		prepActions = new ArrayList<String>();
		ksession.setGlobal("prepActions", prepActions);
		ksession.setGlobal("kindOfComputer", new SimpleKindOfComputer());

		ArrayList<Object> facts = new ArrayList<Object>();
		facts.add(instance);
		facts.add(user);
		facts.addAll(cp.getPermissionsForUserInWorklist(user, instance));
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
	/**
	 * Gets the possible actions.
	 * 
	 * @param instance
	 *            the instance
	 * @param user
	 *            the user
	 * @return the possible actions
	 */
	public List<WfAction> getPossibleActions(WfInstance instance, WfUser user) {
		List<WfAction> possibleActions = new ArrayList<WfAction>();
		WfComponentProvider cp = new WfComponentProvider();

		// KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);

		actions = new ArrayList<String>();
		ksession.setGlobal("actions", actions);
		autoActions = new ArrayList<String>();
		ksession.setGlobal("autoActions", autoActions);
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

	public List<WfAction> getAutomaticActions(WfInstance instance, WfUser user) {
		List<WfAction> automaticActions = new ArrayList<WfAction>();
		WfComponentProvider cp = new WfComponentProvider();

		// KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);

		actions = new ArrayList<String>();
		ksession.setGlobal("actions", actions);
		autoActions = new ArrayList<String>();
		ksession.setGlobal("autoActions", autoActions);
		prepActions = new ArrayList<String>();
		ksession.setGlobal("prepActions", prepActions);
		ksession.setGlobal("kindOfComputer", new SimpleKindOfComputer());

		ArrayList<Object> facts = new ArrayList<Object>();
		facts.add(instance);
		facts.add(user);
		facts.addAll(cp.getPermissionsForUserInWorklist(user, instance));
		ksession.execute(facts);

		for (String returnedActionName : autoActions) {
			for (String loopActionName : wfDefinition.getActions().keySet()) {
				if (loopActionName.equals(returnedActionName)) {
					automaticActions.add(wfDefinition.getActions().get(loopActionName));
				}
			}
		}

		return automaticActions;
	}

	/**
	 * Gets the preparation action.
	 * 
	 * @param instance
	 *            the instance
	 * @param user
	 *            the user
	 * @return the preparation action
	 */
	public WfAction getPreparationAction(WfInstance instance, WfUser user) {
		List<WfAction> candidatePrepActions = new ArrayList<WfAction>();
		WfComponentProvider cp = new WfComponentProvider();

		// KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);

		actions = new ArrayList<String>();
		ksession.setGlobal("actions", actions);
		autoActions = new ArrayList<String>();
		ksession.setGlobal("autoActions", autoActions);
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
		} else if (candidatePrepActions.size() == 1) {
			return candidatePrepActions.iterator().next();
		} else if (candidatePrepActions.size() > 1) {
			// raise exception?
			return candidatePrepActions.iterator().next();
		} else {
			return null;
		}

	}

	/**
	 * Gets the next role.
	 * 
	 * @param instance
	 *            the instance
	 * @param workList
	 *            the work list
	 * @return the next role
	 */
	public List<WfRole> getNextRole(WfInstance instance, WorkList workList) {
		List<WfRole> roles = new ArrayList<WfRole>();

		for (WfMembership loopMembership : workList.getWorkflowUserRoles()) {
			WfPermission loopPermission = new WfPermission();
			loopPermission.setId(UUID.randomUUID());
			loopPermission.setRole(loopMembership.getRole());
			loopPermission.setHiearchyId(SNOMED.Concept.ROOT.getUids().iterator().next());

			actions = new ArrayList<String>();
			ksession.setGlobal("actions", actions);
			autoActions = new ArrayList<String>();
			ksession.setGlobal("autoActions", autoActions);
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

	/**
	 * Gets the next destination.
	 * 
	 * @param instance
	 *            the instance
	 * @param workList
	 *            the work list
	 * @return the next destination
	 */
	public WfUser getNextDestination(WfInstance instance, WorkList workList) {
		List<WfRole> nextRoles = getNextRole(instance, workList);
		WfUser nextUser = null;

		for (WfMembership loopWfMember : workList.getWorkflowUserRoles()) {
			if (nextRoles.contains(loopWfMember.getRole())) {
				if (getPossibleActions(instance, loopWfMember.getUser()).size() > 0) {
					if (loopWfMember.isDefaultAssignment()) {
						nextUser = loopWfMember.getUser();
					} else if (nextUser == null) {
						nextUser = loopWfMember.getUser();
					}
				}
			}
		}

		return nextUser;
	}

	/**
	 * Gets the possible destinations.
	 * 
	 * @param instance
	 *            the instance
	 * @param workList
	 *            the work list
	 * @return the possible destinations
	 */
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

	/**
	 * Do action.
	 * 
	 * @param instance
	 *            the instance
	 * @param role
	 *            the role
	 * @param action
	 *            the action
	 * @param worker
	 *            the worker
	 */
	public static boolean doAction(WfInstance instance, WfRole role, WfAction action, I_Work worker) {
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
			if (bp.getExitCondition().equals(Condition.ITEM_CANCELED)) {
				return false;
			}
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
			return false;
		}
		return true;
	}

	public static boolean doActionInBatch(WfInstance instance, WfRole role, WfAction action, MasterWorker worker) {
		try {
			BusinessProcess bp;
			ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(action.getBusinessProcess())));
			bp = (BusinessProcess) ois.readObject();
			bp.writeAttachment("WfInstance", instance);
			bp.writeAttachment("WfRole", role);
			bp.writeAttachment("BatchMessage", BATCH_OPERATION);
			bp.writeAttachment("consequenceState", action.getConsequence());
			final I_Work tworker;
			if (worker.isExecuting()) {
				tworker = worker.getTransactionIndependentClone();
				tworker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), Terms.get().getActiveAceFrameConfig());
			} else {
				tworker = worker;

			}

			tworker.execute(bp);
			if (bp.getExitCondition().equals(Condition.ITEM_CANCELED)) {
				return false;
			}
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
			return false;
		}
		return true;
	}

}
