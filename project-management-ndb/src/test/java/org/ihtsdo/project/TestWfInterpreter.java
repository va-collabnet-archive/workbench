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
package org.ihtsdo.project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

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
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.ihtsdo.project.workflow.model.TestXstream;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfPermission;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;

/**
 * The Class TestWfInterpreter.
 */
public class TestWfInterpreter extends TestCase {

	/** The wf definition. */
	private WorkflowDefinition wfDefinition;
	
	/** The kbase. */
	private KnowledgeBase kbase;
	
	/** The ksession. */
	private StatelessKnowledgeSession ksession;
	
	/** The actions. */
	private List<String> actions;
	
	/** The prep actions. */
	private List<String> prepActions;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
	this.wfDefinition = TestXstream.readWfDefinition(new File("sampleProcesses/testWfDefinition3.wfd"));;

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
	}
	
	/**
	 * Test next role.
	 */
	public void testNextRole(){
		WfInstance instance = new WfInstance();
		instance.setComponentId(UUID.randomUUID());
		WfState state7 = new WfState();
		state7.setId(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getUids().iterator().next());
		state7.setName("worklist item assigned");
	
		instance.setState(state7);
		instance.setWfDefinition(wfDefinition);
		//instance.setWorkList(UUID.randomUUID());
		WfPermission loopPermission = new WfPermission();
		loopPermission.setId(UUID.randomUUID());
		WfRole role=new WfRole("tsp translator one role",UUID.randomUUID());
		loopPermission.setRole(role);
		loopPermission.setHiearchyId(SNOMED.Concept.ROOT.getUids().iterator().next());

		ArrayList<String> actions = new ArrayList<String>();
		ksession.setGlobal("actions", actions);
		prepActions = new ArrayList<String>();
		ksession.setGlobal("prepActions", prepActions);

		ArrayList<Object> facts = new ArrayList<Object>();
		facts.add(instance);
		facts.add(loopPermission);
		ksession.execute(facts);

		if (actions.size() > 0) {
			List<WfRole> roles = new ArrayList<WfRole>();
			roles.add(role);
		}
	}
}
