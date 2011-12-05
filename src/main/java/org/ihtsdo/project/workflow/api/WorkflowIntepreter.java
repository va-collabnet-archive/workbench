package org.ihtsdo.project.workflow.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.definition.KnowledgePackage;
import org.drools.runtime.StatelessKnowledgeSession;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfMembership;
import org.ihtsdo.project.workflow.model.WfPermission;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

public class WorkflowIntepreter {

	private WorkflowDefinition wfDefinition;
	private StatelessKnowledgeSession ksession;
	private List<String> actions;

	public WorkflowIntepreter(WorkflowDefinition wfDefinition) {
		super();
		this.wfDefinition = wfDefinition;

		File serializedKbFile = new File("rules/" + wfDefinition.getStateTransitionKBFileName());

		if (serializedKbFile.exists()) {
			try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(serializedKbFile));
				// The input stream might contain an individual
				// package or a collection.
				Collection<KnowledgePackage> kpkgs = (Collection<KnowledgePackage>)in.readObject();
				in.close();
				KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
				kbase.addKnowledgePackages(kpkgs);
				actions = new ArrayList<String>();
				ksession.setGlobal("actions", actions);
				ksession.setGlobal("kindOfComputer", new SimpleKindOfComputer());
				ksession = kbase.newStatelessKnowledgeSession();
			} catch (StreamCorruptedException e0) {
				serializedKbFile.delete();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

	public WorkflowDefinition getWfDefinition() {
		return wfDefinition;
	}

	public List<WfAction> getPossibleActions(WfInstance instance, WfUser user) {
		List<WfAction> possibleActions = new ArrayList<WfAction>();

		actions = new ArrayList<String>();
		ksession.setGlobal("actions", actions);

		ArrayList<Object> facts = new ArrayList<Object>();
		facts.add(instance);
		facts.add(user);
		facts.addAll(user.getPermissions());
		ksession.execute(facts);

		//TODO: convert string to actions

		return possibleActions;
	}

	public List<WfRole> getNextRole(WfInstance instance, WorkList workList) {
		List<WfRole> roles = new ArrayList<WfRole>();

		for (WfMembership loopMembership : workList.getWorkflowMembers()) {
			WfPermission loopPermission = new WfPermission();
			loopPermission.setId(UUID.randomUUID());
			loopPermission.setRole(loopMembership.getRole());
			loopPermission.setHiearchyId(SNOMED.Concept.ROOT.getUids().iterator().next());

			actions = new ArrayList<String>();
			ksession.setGlobal("actions", actions);

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

		for (WfMembership loopWfMember : workList.getWorkflowMembers()) {
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

}
