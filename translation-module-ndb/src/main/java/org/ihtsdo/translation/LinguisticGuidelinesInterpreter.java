package org.ihtsdo.translation;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
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
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.cement.SNOMED;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.workflow.api.SimpleKindOfComputer;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
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
public class LinguisticGuidelinesInterpreter {
	/** The h wf i. */
	private static HashMap<Integer, LinguisticGuidelinesInterpreter> hWfI = new HashMap<Integer, LinguisticGuidelinesInterpreter>();

	/** The wf definition. */
	private static I_GetConceptData concept;

	/** The kbase. */
	private KnowledgeBase kbase;

	/** The ksession. */
	private StatelessKnowledgeSession ksession;

	/** The actions. */
	private List<String> actions;

	/** The prep actions. */
	private List<String> prepActions;
	
	private static final String linguisticGuidelinesRulesFile = "drools-rules/linguistic-guidelines.xls";

	/**
	 * Instantiates a new workflow interpreter.
	 * 
	 * @param wfDefinition
	 *            the wf definition
	 */
	public LinguisticGuidelinesInterpreter(I_GetConceptData concept) {
		super();
		this.concept = concept;
			// kbase and ksession are singletons
		if (kbase == null || ksession == null) {
			// Crate knowledge base with decision table
			DecisionTableConfiguration dtableconfiguration = KnowledgeBuilderFactory.newDecisionTableConfiguration();
			dtableconfiguration.setInputType(DecisionTableInputType.XLS);

			kbase = KnowledgeBaseFactory.newKnowledgeBase();
			KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbase);

//			for (String loopXls : wfDefinition.getXlsFileName()) {
//				Resource xlsRes = ResourceFactory.newFileResource(loopXls);
//				kbuilder.add(xlsRes, ResourceType.DTABLE, dtableconfiguration);
//			}
//
//			for (String loopDrl : wfDefinition.getDrlFileName()) {
//				kbuilder.add(ResourceFactory.newFileResource(loopDrl), ResourceType.DRL);
//			}

			if (kbuilder.hasErrors()) {
				System.err.print(kbuilder.getErrors());
			}

			kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
			ksession = kbase.newStatelessKnowledgeSession();
//			hWfI.put(wfDefinition.getName(), this);

		}

	}

	public static LinguisticGuidelinesInterpreter createLinguisticGuidelinesInterpreter(I_GetConceptData concept) {
		try {
			if (hWfI.containsKey(concept.getInitialText())) {
				return hWfI.get(concept.getInitialText());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new LinguisticGuidelinesInterpreter(concept);
	}

	/**
	 * Gets the wf definition.
	 * 
	 * @return the wf definition
	 */
//	public WorkflowDefinition getWfDefinition() {
//		return wfDefinition;
//	}

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
		prepActions = new ArrayList<String>();
		ksession.setGlobal("prepActions", prepActions);
		ksession.setGlobal("kindOfComputer", new SimpleKindOfComputer());

		ArrayList<Object> facts = new ArrayList<Object>();
		facts.add(instance);
		facts.add(user);
		facts.addAll(cp.getPermissionsForUser(user));
		ksession.execute(facts);

//		for (String returnedActionName : actions) {
//			for (String loopActionName : wfDefinition.getActions().keySet()) {
//				if (loopActionName.equals(returnedActionName)) {
//					possibleActions.add(wfDefinition.getActions().get(loopActionName));
//				}
//			}
//		}

		return possibleActions;
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
		prepActions = new ArrayList<String>();
		ksession.setGlobal("prepActions", prepActions);
		ksession.setGlobal("kindOfComputer", new SimpleKindOfComputer());

		ArrayList<Object> facts = new ArrayList<Object>();
		facts.add(instance);
		facts.add(user);
		facts.addAll(cp.getPermissionsForUser(user));
		ksession.execute(facts);

//		for (String returnedActionName : prepActions) {
//			for (String loopActionName : wfDefinition.getActions().keySet()) {
//				if (loopActionName.equals(returnedActionName)) {
//					candidatePrepActions.add(wfDefinition.getActions().get(loopActionName));
//				}
//			}
//		}

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
	public void doAction(WfInstance instance, WfRole role, WfAction action, I_Work worker) {
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
