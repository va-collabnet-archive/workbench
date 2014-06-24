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
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfMembership;
import org.ihtsdo.project.workflow.model.WfPermission;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * The Class TestInterpreter.
 */
public class TestInterpreter {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		//saveKB();
		writeWfDefinition(getWfDefinition());
		
		WorkflowDefinition wfDef = readWfDefinition(new File("sampleProcesses/WorkflowStore Canada 1.wfd"));
		
		WorkList workList = new WorkList();
		workList.setWorkflowDefinition(wfDef);
		workList.setWorkflowUserRoles(getWorkflowMembers(wfDef));
		
		WorkflowInterpreter wfInt = WorkflowInterpreter.createWorkflowInterpreter(wfDef);
		
		for (WfState loopState : wfDef.getStates()) {
			for (WfMembership loopMember : workList.getWorkflowUserRoles()) {
				System.out.println("Testing for: " + loopState.getName() + 
						" and " + loopMember.getUser().getUsername() +
						" - Role: " + loopMember.getUser().getPermissions().iterator().next().getRoleName());
				
				WfInstance loopInstance = new WfInstance(UUID.randomUUID(), 
						wfDef, loopState, null, null,0L);
				List<WfAction> resultActions = wfInt.getPossibleActions(loopInstance, loopMember.getUser());
				WfAction prepAction = wfInt.getPreparationAction(loopInstance, loopMember.getUser());
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
				System.out.print("- Prep action: ");
				if (prepAction != null) {
					System.out.println(prepAction.getName());
				}
				System.out.println("");
				System.out.println("");
				
			}
		}
		

	}

	/**
	 * Gets the workflow members.
	 *
	 * @param wfDef the wf def
	 * @return the workflow members
	 */
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

	/**
	 * Save kb.
	 */
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
			AceLog.getAppLog().alertAndLogException(e);
		}
		System.out.println("KBase saved!");
	}
	
	/**
	 * Gets the wf definition.
	 *
	 * @return the wf definition
	 */
	public static WorkflowDefinition getWfDefinition() {
		WorkflowDefinition wfdf=new WorkflowDefinition();
		
		List<WfState> states = new ArrayList<WfState>();
		states.add(new WfState("worklist item assigned", UUID.randomUUID()));
		states.add(new WfState("SME feedback complete status", UUID.randomUUID()));
		states.add(new WfState("Super SME feedback complete status", UUID.randomUUID()));
		states.add(new WfState("referred to SME status", UUID.randomUUID()));
		states.add(new WfState("referred to Super SME status", UUID.randomUUID()));
		states.add(new WfState("reviewed by TSP reviewer status", UUID.randomUUID()));
		states.add(new WfState("rejected by TPO reviewer status", UUID.randomUUID()));
		states.add(new WfState("translated status", UUID.randomUUID()));
		states.add(new WfState("rejected by TSP reviewer status", UUID.randomUUID()));
		states.add(new WfState("escalated to editorial board status", UUID.randomUUID()));
		
		List<WfRole> roles = new ArrayList<WfRole>();
		roles.add(new WfRole("translation editorial board role", UUID.randomUUID()));
		roles.add(new WfRole("tsp translator one role", UUID.randomUUID()));
		roles.add(new WfRole("translation sme role", UUID.randomUUID()));
		roles.add(new WfRole("translation super sme role", UUID.randomUUID()));
		roles.add(new WfRole("tpo reviewer role", UUID.randomUUID()));
		roles.add(new WfRole("tsp reviewer role", UUID.randomUUID()));
		
		Map<String,WfAction> actions = new HashMap<String,WfAction>();
		actions.put("Approve", new WfAction("Approve"));
		actions.put("Reject revision with stated reason", new WfAction("Reject revision with stated reason"));
		actions.put("Consult to Super SME", new WfAction("Consult to Super SME"));
		actions.put("Translate", new WfAction("Translate"));
		actions.put("Respond SME consultation", new WfAction("Respond SME consultation"));
		actions.put("Respond Super SME consultation", new WfAction("Respond Super SME consultation"));
		actions.put("Reject revision", new WfAction("Reject revision"));
		actions.put("Escalate", new WfAction("Escalate"));
		actions.put("Reject translation with stated reason", new WfAction("Reject translation with stated reason"));
		actions.put("Review", new WfAction("Review"));
		actions.put("Consult to SME", new WfAction("Consult to SME"));
		actions.put("Open Edit Translation Panel", new WfAction("Open Edit Translation Panel"));
		actions.put("Open Read Only Translation Panel", new WfAction("Open Read Only Translation Panel"));
		
		wfdf.setName("WorkflowStore Canada 1");
		wfdf.setRoles(roles);
		wfdf.setStates(states);
		wfdf.setActions(actions);
		//wfdf.getXlsFileName().add("rules/test-dtable.xls");
		wfdf.getXlsFileName().add("/Users/alo/Desktop/test-dtable.xls");
		
		return wfdf;
	}
	
	/**
	 * Write wf definition.
	 *
	 * @param wfDefinition the wf definition
	 */
	public static void writeWfDefinition(WorkflowDefinition wfDefinition){
		XStream xStream = new XStream(new DomDriver());
		FileOutputStream rfos;
		try {
			rfos = new FileOutputStream("sampleProcesses/" +  wfDefinition.getName() + ".wfd");
			OutputStreamWriter rosw = new OutputStreamWriter(rfos,"UTF-8");
			xStream.toXML(wfDefinition,rosw);
		} catch (FileNotFoundException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (UnsupportedEncodingException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		System.out.println("WorkflowStore Definition saved!");
	}
	
	/**
	 * Read wf definition.
	 *
	 * @param file the file
	 * @return the workflow definition
	 */
	public static WorkflowDefinition readWfDefinition(File file){
		XStream xStream = new XStream(new DomDriver());
		WorkflowDefinition wfDef=(WorkflowDefinition)xStream.fromXML(file);
		return wfDef;
	}

}
