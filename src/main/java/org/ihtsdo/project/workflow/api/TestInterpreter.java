package org.ihtsdo.project.workflow.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfMembership;
import org.ihtsdo.project.workflow.model.WfPermission;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;
import org.ihtsdo.project.workflow.model.actions.StubAction;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class TestInterpreter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//saveKB();
		//writeWfDefinition(getWfDefinition());
		
		WorkflowDefinition wfDef = readWfDefinition(new File("sampleProcesses/Workflow Canada 1.wfd"));
		
		WorkList workList = new WorkList();
		workList.setWorkflowDefinition(wfDef);
		workList.setWorkflowUserRoles(getWorkflowMembers(wfDef));
		
		WorkflowIntepreter wfInt = new WorkflowIntepreter(wfDef);
		
		for (WfState loopState : wfDef.getStates()) {
			for (WfMembership loopMember : workList.getWorkflowUserRoles()) {
				System.out.println("Testing for: " + loopState.getName() + 
						" and " + loopMember.getUser().getUsername() +
						" - Role: " + loopMember.getUser().getPermissions().iterator().next().getRoleName());
				
				WfInstance loopInstance = new WfInstance(UUID.randomUUID(), 
						wfDef, loopState, null, null);
				List<WfAction> resultActions = wfInt.getPossibleActions(loopInstance, loopMember.getUser());
				List<WfRole> nextRoles = wfInt.getNextRole(loopInstance, workList);
				WfUser nextUser = wfInt.getNextDestination(loopInstance, workList);
				
				System.out.println("");
				System.out.println("- Possible Actions: " + resultActions.size());
				for (WfAction loopAction : resultActions) {
					System.out.println("--- " + loopAction.getName());
				}
				System.out.println("");
				System.out.println("- Next Roles:" + nextRoles.size());
				for (WfRole loopRole : nextRoles) {
					System.out.println("--- " + loopRole.getName());
				}
				System.out.println("");
				System.out.print("- Next User: ");
				if (nextUser != null) {
					System.out.println(nextUser.getUsername() + " - " + nextUser.getPermissions().iterator().next().getRoleName());
				}
				System.out.println("");
				System.out.println("");
				
			}
		}
		

	}

	private static List<WfMembership> getWorkflowMembers(WorkflowDefinition wfDef) {
		List<WfMembership> members = new ArrayList<WfMembership>();
		int counter = 0;
		for (WfRole loopRole : wfDef.getRoles()) {
			counter++;
			WfUser loopuser = new WfUser("User " + counter, UUID.randomUUID());
			WfPermission loopPermission = new WfPermission(UUID.randomUUID(), 
					loopRole, UUID.randomUUID());
			List<WfPermission> listPerm = new ArrayList<WfPermission>();
			listPerm.add(loopPermission);
			loopuser.setPermissions(listPerm);
			
			members.add(new WfMembership(UUID.randomUUID(), 
					loopuser, loopRole, true));
		}
		return members;
	}

	public static void saveKB() {
		DecisionTableConfiguration dtableconfiguration =
			KnowledgeBuilderFactory.newDecisionTableConfiguration();
		dtableconfiguration.setInputType( DecisionTableInputType.XLS );

		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbase);

		Resource xlsRes = ResourceFactory.newFileResource("/Users/alo/Desktop/test-dtable.xls");
		kbuilder.add( xlsRes,
				ResourceType.DTABLE,
				dtableconfiguration );

		if ( kbuilder.hasErrors() ) {
			System.err.print( kbuilder.getErrors() );
		}

		kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

		try {
			ObjectOutput out = new ObjectOutputStream(new FileOutputStream("rules/workFlowCanada1.wfkb"));
			out.writeObject(kbase.getKnowledgePackages());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("KBase saved!");
	}
	
	public static WorkflowDefinition getWfDefinition() {
		WorkflowDefinition wfdf=new WorkflowDefinition();
		
		List<WfState> states = new ArrayList<WfState>();
		states.add(new WfState("Assigned", UUID.randomUUID()));
		states.add(new WfState("Responded by SME", UUID.randomUUID()));
		states.add(new WfState("Responded by Super SME", UUID.randomUUID()));
		states.add(new WfState("Consulted to SME", UUID.randomUUID()));
		states.add(new WfState("Consulted to Super SME", UUID.randomUUID()));
		states.add(new WfState("Reviewed", UUID.randomUUID()));
		states.add(new WfState("Revision rejected", UUID.randomUUID()));
		states.add(new WfState("Translated", UUID.randomUUID()));
		states.add(new WfState("Translation rejected", UUID.randomUUID()));
		
		List<WfRole> roles = new ArrayList<WfRole>();
		roles.add(new WfRole("Editorial Board", UUID.randomUUID()));
		roles.add(new WfRole("TSP Translator", UUID.randomUUID()));
		roles.add(new WfRole("SME", UUID.randomUUID()));
		roles.add(new WfRole("Super SME", UUID.randomUUID()));
		roles.add(new WfRole("TPO Reviewer", UUID.randomUUID()));
		roles.add(new WfRole("TSP Reviewer", UUID.randomUUID()));
		
		Map<String,StubAction> actions = new HashMap<String,StubAction>();
		actions.put("Approve", new StubAction("Approve"));
		actions.put("Reject revision with stated reason", new StubAction("Reject revision with stated reason"));
		actions.put("Consult to Super SME", new StubAction("Consult to Super SME"));
		actions.put("Translate", new StubAction("Translate"));
		actions.put("Respond SME consultation", new StubAction("Respond SME consultation"));
		actions.put("Respond Super SME consultation", new StubAction("Respond Super SME consultation"));
		actions.put("Reject revision", new StubAction("Reject revision"));
		actions.put("Escalate", new StubAction("Escalate"));
		actions.put("Reject translation with stated reason", new StubAction("Reject translation with stated reason"));
		actions.put("Review", new StubAction("Review"));
		actions.put("Consult to SME", new StubAction("Consult to SME"));
		
		wfdf.setName("Workflow Canada 1");
		wfdf.setRoles(roles);
		wfdf.setStates(states);
		wfdf.setActions(actions);
		wfdf.setStateTransitionKBFileName("workFlowCanada1.wfkb");
		
		return wfdf;
	}
	
	public static void writeWfDefinition(WorkflowDefinition wfDefinition){
		XStream xStream = new XStream(new DomDriver());
		FileOutputStream rfos;
		try {
			rfos = new FileOutputStream("sampleProcesses/" +  wfDefinition.getName() + ".wfd");
			OutputStreamWriter rosw = new OutputStreamWriter(rfos,"UTF-8");
			xStream.toXML(wfDefinition,rosw);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.out.println("Workflow Definition saved!");
	}
	
	public static WorkflowDefinition readWfDefinition(File file){
		XStream xStream = new XStream(new DomDriver());
		WorkflowDefinition wfDef=(WorkflowDefinition)xStream.fromXML(file);
		return wfDef;
	}

}
