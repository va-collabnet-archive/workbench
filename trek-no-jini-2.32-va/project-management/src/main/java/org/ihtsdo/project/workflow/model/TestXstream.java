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
package org.ihtsdo.project.workflow.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.dwfa.ace.log.AceLog;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * The Class TestXstream.
 */
public class TestXstream {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {

//		WfState state0 = new WfState();
//		state0.setId(ArchitectonicAuxiliary.Concept.APPROVED_FOR_PUBLICATION_STATUS.getUids().iterator().next());
//		state0.setName(ArchitectonicAuxiliary.Concept.APPROVED_FOR_PUBLICATION_STATUS.name());
//		WfState state1 = new WfState();
//		state1.setId(ArchitectonicAuxiliary.Concept.REFERRED_TO_SME_STATUS.getUids().iterator().next());
//		state1.setName(ArchitectonicAuxiliary.Concept.REFERRED_TO_SME_STATUS.name());
//		WfState state2 = new WfState();
//		WfState state3 = new WfState();
//		state3.setId(ArchitectonicAuxiliary.Concept.REJECTED_BY_TPO_STATUS.getUids().iterator().next());
//		state3.setName(ArchitectonicAuxiliary.Concept.REJECTED_BY_TPO_STATUS.name());
//		WfState state4 = new WfState();
//		state4.setId(ArchitectonicAuxiliary.Concept.REJECTED_BY_TSP_STATUS.getUids().iterator().next());
//		state4.setName(ArchitectonicAuxiliary.Concept.REJECTED_BY_TSP_STATUS.name());
//		
//		WfState state5 = new WfState();
//		state5.setId(ArchitectonicAuxiliary.Concept.ESCALATED_TO_EDITORIAL_BOARD_STATUS.getUids().iterator().next());
//		state5.setName(ArchitectonicAuxiliary.Concept.ESCALATED_TO_EDITORIAL_BOARD_STATUS.name());
//		WfState state6 = new WfState();
//		state6.setId(ArchitectonicAuxiliary.Concept.TRANSLATED_STATUS.getUids().iterator().next());
//		state6.setName(ArchitectonicAuxiliary.Concept.TRANSLATED_STATUS.name());
//		WfState state7 = new WfState();
//		state7.setId(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getUids().iterator().next());
//		state7.setName(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.name());
//		WfState state8 = new WfState();
//		
//		WfState state9 = new WfState();
//		state9.setId(ArchitectonicAuxiliary.Concept.SME_FEEDBACK_COMPLETE_STATUS.getUids().iterator().next());
//		state9.setName(ArchitectonicAuxiliary.Concept.SME_FEEDBACK_COMPLETE_STATUS.name());
//		
//
//		WfState state10 = new WfState();
//		state10.setId(ArchitectonicAuxiliary.Concept.REVIEWED_BY_TSP_STATUS.getUids().iterator().next());
//		state10.setName(ArchitectonicAuxiliary.Concept.REVIEWED_BY_TSP_STATUS.name());
//		
//		WfRole role1 = new WfRole(ArchitectonicAuxiliary.Concept.TRANSLATION_EDITORIAL_BOARD_ROLE.name(), ArchitectonicAuxiliary.Concept.TRANSLATION_EDITORIAL_BOARD_ROLE.getUids().iterator().next());
//		WfRole role2 = new WfRole(ArchitectonicAuxiliary.Concept.TRANSLATION_SME_ROLE.name(), ArchitectonicAuxiliary.Concept.TRANSLATION_SME_ROLE.getUids().iterator().next());
//		WfRole role4 = new WfRole(ArchitectonicAuxiliary.Concept.TRANSLATION_TPO_REVIEWER_ROLE.name(), ArchitectonicAuxiliary.Concept.TRANSLATION_TPO_REVIEWER_ROLE.getUids().iterator().next());
//		WfRole role5 = new WfRole(ArchitectonicAuxiliary.Concept.TRANSLATION_TSP_REVIEWER_ROLE.name(), ArchitectonicAuxiliary.Concept.TRANSLATION_TSP_REVIEWER_ROLE.getUids().iterator().next());
//		WfRole role6 = new WfRole(ArchitectonicAuxiliary.Concept.TRANSLATOR_ONE_TSP_ROLE.name(), ArchitectonicAuxiliary.Concept.TRANSLATOR_ONE_TSP_ROLE.getUids().iterator().next());
//
//				WfAction action0 = new WfAction();
//				action0.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//				action0.setConsequence(state0);
//				action0.setId(UUID.randomUUID());
//				action0.setName("Approve");
//				WfAction action10 = new WfAction();
//				action10.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//				action10.setConsequence(state1);
//				action10.setId(UUID.randomUUID());
//				action10.setName("Consult to SME");
//				WfAction action1 = new WfAction();
//				action1.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//				action1.setConsequence(state2);
//				action1.setId(UUID.randomUUID());
//				action1.setName("Consult to Super SME");
//				WfAction action2 = new WfAction();
//				action2.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//				action2.setConsequence(state5);
//				action2.setId(UUID.randomUUID());
//				action2.setName("Escalate");
//				WfAction action3 = new WfAction();
//				action3.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//				action3.setConsequence(state4);
//				action3.setId(UUID.randomUUID());
//				action3.setName("Reject revision");
//				WfAction action4 = new WfAction();
//				action4.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//				action4.setConsequence(state4);
//				action4.setId(UUID.randomUUID());
//				action4.setName("Reject revision with stated reason");
//				WfAction action5 = new WfAction();
//				action5.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//				action5.setConsequence(state3);
//				action5.setId(UUID.randomUUID());
//				action5.setName("Reject translation with stated reason");
//				WfAction action6 = new WfAction();
//				action6.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//				action6.setConsequence(state9);
//				action6.setId(UUID.randomUUID());
//				action6.setName("Respond SME consultation");
//				WfAction action7 = new WfAction();
//				action7.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//				action7.setConsequence(state8);
//				action7.setId(UUID.randomUUID());
//				action7.setName("Respond Super SME consultation");
//				WfAction action8 = new WfAction();
//				action8.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//				action8.setConsequence(state10);
//				action8.setId(UUID.randomUUID());
//				action8.setName("Review");
//				WfAction action9 = new WfAction();
//				action9.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//				action9.setConsequence(state6);
//				action9.setId(UUID.randomUUID());
//				action9.setName("Translate");
//				
//				List<WfState> states = new ArrayList<WfState>();
//				states.add(state0);
//				states.add(state1);
//				states.add(state2);
//				states.add(state3);
//				states.add(state4);
//				states.add(state5);
//				states.add(state6);
//				states.add(state7);
//				states.add(state8);
//				states.add(state9);
//				states.add(state10);
//				List<WfRole> roles = new ArrayList<WfRole>();
//				roles.add(role1);
//				roles.add(role2);
//				roles.add(role3);
//				roles.add(role4);
//				roles.add(role5);
//				roles.add(role6);
//
//				Map<String,  WfAction> actions = new HashMap<String, WfAction>();
//				actions.put("Approve",action0);
//				actions.put("Consult to SME",action10);
//				actions.put("Consult to Super SME",action1);
//				actions.put("Escalate",action2);
//				actions.put("Reject revision",action3);
//				actions.put("Reject revision with stated reason",action4);
//				actions.put("Reject translation with stated reason",action5);
//				actions.put("Respond SME consultation",action6);
//				actions.put("Respond Super SME consultation",action7);
//				actions.put("Review",action8);
//				actions.put("Translate",action9);
//
//		WorkflowDefinition wfdf=new WorkflowDefinition();
//		wfdf.setName("testWfDefinition");
//		wfdf.setRoles(roles);
//		wfdf.setStates(states);
//		wfdf.setActions(actions);
//		
//		List <String> lstFName=new ArrayList<String>();
//		lstFName.add("drools-rules/test-dtable.xls");
//		wfdf.setXlsFileName(lstFName);
//		writeWfDefinition(wfdf);
//		WorkflowDefinition wfdf2=readWfDefinition(new File("sampleProcesses/"  +  wfdf.getName() + ".wfd"));
		
//		XStream xstream = new XStream(new JettisonMappedXmlDriver());
//		//xstream.setMode(XStream.NO_REFERENCES);
//		//xstream.alias("action", WfAction.class);
//		System.out.println("JSON Len: " + xstream.toXML(role2).length());
//		System.out.println(xstream.toXML(role2));
//
//		XStream xStream = new XStream(new DomDriver());
//		System.out.println("XML Len: " + xStream.toXML(role2).length());
//		System.out.println(xStream.toXML(role2));


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
			// TODO Auto-generated catch block
			AceLog.getAppLog().alertAndLogException(e);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			AceLog.getAppLog().alertAndLogException(e);
		}

	}


}
