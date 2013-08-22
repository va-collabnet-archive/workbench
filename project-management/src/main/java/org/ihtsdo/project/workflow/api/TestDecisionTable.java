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

import java.util.ArrayList;
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
import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfPermission;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfUser;

/**
 * The Class TestDecisionTable.
 */
public class TestDecisionTable {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		// Crate knowledge base with decision table
		DecisionTableConfiguration dtableconfiguration =
			KnowledgeBuilderFactory.newDecisionTableConfiguration();
		dtableconfiguration.setInputType( DecisionTableInputType.XLS );

		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbase);

		//		Resource xlsRes = ResourceFactory.newClassPathResource( "/Users/alo/Desktop/test-dtable.xls",
		//				TestDecisionTable.class );
		Resource xlsRes = ResourceFactory.newFileResource("/Users/alo/Desktop/test-dtable.xls");
		kbuilder.add( xlsRes,
				ResourceType.DTABLE,
				dtableconfiguration );

		if ( kbuilder.hasErrors() ) {
			System.err.print( kbuilder.getErrors() );
		}

		kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
		StatelessKnowledgeSession ksession = kbase.newStatelessKnowledgeSession();

		// Prepare test data
		WfState escalated = new WfState("Escalated", UUID.randomUUID());
		WfState delivered = new WfState("Delivered", UUID.randomUUID());
		WfState translated = new WfState("Translated", UUID.randomUUID());

		WfRole editorialBoard = new WfRole("Editorial Board", UUID.randomUUID());
		WfRole tspTranslator = new WfRole("TSP Translator", UUID.randomUUID());
		WfRole tspReviewer = new WfRole("TSP Reviewer", UUID.randomUUID());

		WfUser johnTspTrans = new WfUser("John TSP Trans", UUID.randomUUID());
		johnTspTrans.getPermissions().add(new WfPermission(UUID.randomUUID(), tspTranslator, UUID.randomUUID()));
		WfUser janeTspRev = new WfUser("Jane TSP Rev", UUID.randomUUID());
		janeTspRev.getPermissions().add(new WfPermission(UUID.randomUUID(), tspReviewer, UUID.randomUUID()));
		WfUser martinEB = new WfUser("Martin E B", UUID.randomUUID());
		martinEB.getPermissions().add(new WfPermission(UUID.randomUUID(), editorialBoard, UUID.randomUUID()));

		WfInstance translatedInstance = new WfInstance();
		translatedInstance.setState(translated);
		WfInstance deliveredInstance = new WfInstance();
		deliveredInstance.setState(delivered);
		WfInstance escalatedInstance = new WfInstance();
		escalatedInstance.setState(escalated);
		WfInstance escalatedInstanceNotAllowedHier = new WfInstance();
		escalatedInstanceNotAllowedHier.setState(escalated);
		escalatedInstanceNotAllowedHier.setComponentId(UUID.fromString("d0bdc410-1ad1-11e1-bddb-0800200c9a66"));

		WfAction approve = new WfAction() {
			public WfInstance doAction(WfInstance instance) throws Exception {
				return null;
			}
		};
		approve.setName("Approve");
		approve.setId(UUID.randomUUID());

		WfAction translate = new WfAction() {
			public WfInstance doAction(WfInstance instance) throws Exception {
				return null;
			}
		};
		translate.setName("Translate");
		translate.setId(UUID.randomUUID());

		WfAction review = new WfAction() {
			public WfInstance doAction(WfInstance instance) throws Exception {
				return null;
			}
		};
		review.setName("Review");
		review.setId(UUID.randomUUID());

		// Insert facts and run
		//KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
		List<String> actions = new ArrayList<String>();
		ksession.setGlobal("actions", actions);
		ksession.setGlobal("kindOfComputer", new SimpleKindOfComputer());
		
		System.out.println("MartinEB - Escalated");
		ArrayList facts = new ArrayList();
		facts.add(escalatedInstance);
		facts.add(martinEB);
		facts.addAll(martinEB.getPermissions());
		ksession.execute( facts );
		
		System.out.println("Actions size: " + actions.size());
		for (String action : actions) {
			System.out.println("- " + action);
		}
		System.out.println();
		
		actions = new ArrayList<String>();
		ksession.setGlobal("actions", actions);
		
		System.out.println("MartinEB - Escalated with not allowed hiearchy");
		facts = new ArrayList();
		facts.add(escalatedInstanceNotAllowedHier);
		facts.add(martinEB);
		facts.addAll(martinEB.getPermissions());
		ksession.execute( facts );
		
		System.out.println("Actions size: " + actions.size());
		for (String action : actions) {
			System.out.println("- " + action);
		}
		System.out.println();
		
		actions = new ArrayList<String>();
		ksession.setGlobal("actions", actions);
		
		System.out.println("MartinEB - delivered");
		facts = new ArrayList();
		facts.add(deliveredInstance);
		facts.add(martinEB);
		facts.addAll(martinEB.getPermissions());
		ksession.execute( facts );
		
		System.out.println("Actions size: " + actions.size());
		for (String action : actions) {
			System.out.println("- " + action);
		}
		System.out.println();
		
		actions = new ArrayList<String>();
		ksession.setGlobal("actions", actions);
		
		System.out.println("John TSP T - delivered");
		facts = new ArrayList();
		facts.add(deliveredInstance);
		facts.add(johnTspTrans);
		facts.addAll(johnTspTrans.getPermissions());
		ksession.execute( facts );
		
		System.out.println("Actions size: " + actions.size());
		for (String action : actions) {
			System.out.println("- " + action);
		}
		System.out.println();
		
		actions = new ArrayList<String>();
		ksession.setGlobal("actions", actions);
		
		System.out.println("Jane TSP R - translated");
		facts = new ArrayList();
		facts.add(translatedInstance);
		facts.add(janeTspRev);
		facts.addAll(janeTspRev.getPermissions());
		ksession.execute( facts );

		System.out.println("Actions size: " + actions.size());
		for (String action : actions) {
			System.out.println("- " + action);
		}
		System.out.println();

	}

}
