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
package org.ihtsdo.project.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.JFrame;

import junit.framework.TestCase;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;
import org.ihtsdo.tk.api.Precedence;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.junit.Ignore;
import org.junit.Test;

/**
 * The Class TestWorkflowDefinitionPanel.
 */
public class TestWorkflowDefinitionPanel extends TestCase {
	/** The vodb directory. */
	File vodbDirectory;

	/** The read only. */
	boolean readOnly = false;

	/** The cache size. */
	Long cacheSize = Long.getLong("600000000");

	/** The db setup config. */
	DatabaseSetupConfig dbSetupConfig;

	/** The config. */
	I_ConfigAceFrame config;

	/** The tf. */
	I_TermFactory tf;

	/** The new project concept. */
	I_GetConceptData newProjectConcept;

	/** The allowed statuses with retired. */
	I_IntSet allowedStatusesWithRetired;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
//		System.out.println("Deleting test fixture");
//		deleteDirectory(new File("berkeley-db"));
//		System.out.println("Creating test fixture");
////		copyDirectory(new File("/Users/termmed/Desktop/wb-bundle/berkeley-db"), new File("berkeley-db"));
//		vodbDirectory = new File("berkeley-db");
//		dbSetupConfig = new DatabaseSetupConfig();
//		System.out.println("Opening database");
////		Terms.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
//		tf = (I_ImplementTermFactory) Terms.get();
//		config = getTestConfig();
////		tf.setActiveAceFrameConfig(config);
	}

	/**
	 * Test create workflow definition.
	 */
    @Ignore("Ignored becuase setup has user specific class paths...") @Test
	public void testCreateWorkflowDefinition(){

//		WfState state0 = new WfState();
//		state0.setId(ArchitectonicAuxiliary.Concept.APPROVED_FOR_PUBLICATION_STATUS.getUids().iterator().next());
//		try {
//			state0.setName(getAAPreferredTerm(tf.getConcept(ArchitectonicAuxiliary.Concept.APPROVED_FOR_PUBLICATION_STATUS.getUids())));
//
//			WfState state1 = new WfState();
//			state1.setId(ArchitectonicAuxiliary.Concept.REFERRED_TO_SME_STATUS.getUids().iterator().next());
//			state1.setName(getAAPreferredTerm(tf.getConcept(ArchitectonicAuxiliary.Concept.REFERRED_TO_SME_STATUS.getUids())));
//			WfState state2 = new WfState();
//			state2.setId(ArchitectonicAuxiliary.Concept.REFERRED_TO_SME_STATUS.getUids().iterator().next());
//			state2.setName(getAAPreferredTerm(tf.getConcept(ArchitectonicAuxiliary.Concept.REFERRED_TO_SME_STATUS.getUids())));
//			WfState state3 = new WfState();
//			state3.setId(ArchitectonicAuxiliary.Concept.REJECTED_BY_TPO_STATUS.getUids().iterator().next());
//			state3.setName(getAAPreferredTerm(tf.getConcept(ArchitectonicAuxiliary.Concept.REJECTED_BY_TPO_STATUS.getUids())));
//			WfState state4 = new WfState();
//			state4.setId(ArchitectonicAuxiliary.Concept.REJECTED_BY_TSP_STATUS.getUids().iterator().next());
//			state4.setName(getAAPreferredTerm(tf.getConcept(ArchitectonicAuxiliary.Concept.REJECTED_BY_TSP_STATUS.getUids())));
//
//			WfState state5 = new WfState();
//			state5.setId(ArchitectonicAuxiliary.Concept.ESCALATED_TO_EDITORIAL_BOARD_STATUS.getUids().iterator().next());
//			state5.setName(getAAPreferredTerm(tf.getConcept(ArchitectonicAuxiliary.Concept.ESCALATED_TO_EDITORIAL_BOARD_STATUS.getUids())));
//			WfState state6 = new WfState();
//			state6.setId(ArchitectonicAuxiliary.Concept.TRANSLATED_STATUS.getUids().iterator().next());
//			state6.setName(getAAPreferredTerm(tf.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATED_STATUS.getUids())));
//			WfState state7 = new WfState();
//			state7.setId(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getUids().iterator().next());
//			state7.setName(getAAPreferredTerm(tf.getConcept(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getUids())));
//			WfState state8 = new WfState();
//			state8.setId(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getUids().iterator().next());
//			state8.setName(getAAPreferredTerm(tf.getConcept(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getUids())));
//
//			WfState state9 = new WfState();
//			state9.setId(ArchitectonicAuxiliary.Concept.SME_FEEDBACK_COMPLETE_STATUS.getUids().iterator().next());
//			state9.setName(getAAPreferredTerm(tf.getConcept(ArchitectonicAuxiliary.Concept.SME_FEEDBACK_COMPLETE_STATUS.getUids())));
//
//
//			WfState state10 = new WfState();
//			state10.setId(ArchitectonicAuxiliary.Concept.REVIEWED_BY_TSP_STATUS.getUids().iterator().next());
//			state10.setName(getAAPreferredTerm(tf.getConcept(ArchitectonicAuxiliary.Concept.REVIEWED_BY_TSP_STATUS.getUids())));
//
//			WfRole role1 = new WfRole(getAAPreferredTerm(tf.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_EDITORIAL_BOARD_ROLE.getUids())), ArchitectonicAuxiliary.Concept.TRANSLATION_EDITORIAL_BOARD_ROLE.getUids().iterator().next());
//			WfRole role2 = new WfRole(getAAPreferredTerm(tf.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_SME_ROLE.getUids())), ArchitectonicAuxiliary.Concept.TRANSLATION_SME_ROLE.getUids().iterator().next());
//			WfRole role3 = new WfRole(getAAPreferredTerm(tf.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_SME_ROLE.getUids())), ArchitectonicAuxiliary.Concept.TRANSLATION_SME_ROLE.getUids().iterator().next());
//			WfRole role4 = new WfRole(getAAPreferredTerm(tf.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_TPO_REVIEWER_ROLE.getUids())), ArchitectonicAuxiliary.Concept.TRANSLATION_TPO_REVIEWER_ROLE.getUids().iterator().next());
//			WfRole role5 = new WfRole(getAAPreferredTerm(tf.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_TSP_REVIEWER_ROLE.getUids())), ArchitectonicAuxiliary.Concept.TRANSLATION_TSP_REVIEWER_ROLE.getUids().iterator().next());
//			WfRole role6 = new WfRole(getAAPreferredTerm(tf.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATOR_ONE_TSP_ROLE.getUids())), ArchitectonicAuxiliary.Concept.TRANSLATOR_ONE_TSP_ROLE.getUids().iterator().next());
//
//			WfAction action0 = new WfAction();
//			action0.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//			action0.setConsequence(state0);
//			action0.setId(UUID.randomUUID());
//			action0.setName("Approve");
//			WfAction action10 = new WfAction();
//			action10.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//			action10.setConsequence(state1);
//			action10.setId(UUID.randomUUID());
//			action10.setName("Consult to SME");
//			WfAction action1 = new WfAction();
//			action1.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//			action1.setConsequence(state2);
//			action1.setId(UUID.randomUUID());
//			action1.setName("Consult to Super SME");
//			WfAction action2 = new WfAction();
//			action2.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//			action2.setConsequence(state5);
//			action2.setId(UUID.randomUUID());
//			action2.setName("Escalate");
//			WfAction action3 = new WfAction();
//			action3.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//			action3.setConsequence(state4);
//			action3.setId(UUID.randomUUID());
//			action3.setName("Reject revision");
//			WfAction action4 = new WfAction();
//			action4.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//			action4.setConsequence(state4);
//			action4.setId(UUID.randomUUID());
//			action4.setName("Reject revision with stated reason");
//			WfAction action5 = new WfAction();
//			action5.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//			action5.setConsequence(state3);
//			action5.setId(UUID.randomUUID());
//			action5.setName("Reject translation with stated reason");
//			WfAction action6 = new WfAction();
//			action6.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//			action6.setConsequence(state9);
//			action6.setId(UUID.randomUUID());
//			action6.setName("Respond SME consultation");
//			WfAction action7 = new WfAction();
//			action7.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//			action7.setConsequence(state8);
//			action7.setId(UUID.randomUUID());
//			action7.setName("Respond Super SME consultation");
//			WfAction action8 = new WfAction();
//			action8.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//			action8.setConsequence(state10);
//			action8.setId(UUID.randomUUID());
//			action8.setName("Review");
//			WfAction action9 = new WfAction();
//			action9.setBusinessProcess(new File("sampleProcess/dummyaction.bp"));
//			action9.setConsequence(state6);
//			action9.setId(UUID.randomUUID());
//			action9.setName("Translate");
//
//			List<WfState> states = new ArrayList<WfState>();
//			states.add(state0);
//			states.add(state1);
//			states.add(state2);
//			states.add(state3);
//			states.add(state4);
//			states.add(state5);
//			states.add(state6);
//			states.add(state7);
//			states.add(state8);
//			states.add(state9);
//			states.add(state10);
//			List<WfRole> roles = new ArrayList<WfRole>();
//			roles.add(role1);
//			roles.add(role2);
//			roles.add(role3);
//			roles.add(role4);
//			roles.add(role5);
//			roles.add(role6);
//
//			Map<String,  WfAction> actions = new HashMap<String, WfAction>();
//			actions.put("Approve",action0);
//			actions.put("Consult to SME",action10);
//			actions.put("Consult to Super SME",action1);
//			actions.put("Escalate",action2);
//			actions.put("Reject revision",action3);
//			actions.put("Reject revision with stated reason",action4);
//			actions.put("Reject translation with stated reason",action5);
//			actions.put("Respond SME consultation",action6);
//			actions.put("Respond Super SME consultation",action7);
//			actions.put("Review",action8);
//			actions.put("Translate",action9);
//
//			WorkflowDefinition wfdf=new WorkflowDefinition();
//			wfdf.setName("testWfDefinition3");
//			wfdf.setRoles(roles);
//			wfdf.setStates(states);
//			wfdf.setActions(actions);
//
//			List <String> lstFName=new ArrayList<String>();
//			lstFName.add("drools-rules/test-dtable.xls");
//			wfdf.setXlsFileName(lstFName);
//			writeWfDefinition(wfdf);
//		} catch (TerminologyException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		JFrame frame= new JFrame("lalalalala");
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setSize(600,400);
//		WorkflowDefinitionPanel panel= new WorkflowDefinitionPanel();
//		frame.add(panel);
//		frame.pack();
//		frame.setVisible(true);
//		while(true);
		
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
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * Gets the aA preferred term.
	 *
	 * @param concept the concept
	 * @return the aA preferred term
	 */
	@SuppressWarnings("rawtypes")
	private String getAAPreferredTerm(I_GetConceptData concept){

		List<? extends I_DescriptionTuple> descTuples;
		try {
			descTuples = concept.getDescriptionTuples(
					config.getAllowedStatus(), 
					(config.getDescTypes().getSetValues().length == 0)?null:config.getDescTypes(), 
							config.getViewPositionSetReadOnly(), 
							Precedence.TIME, config.getConflictResolutionStrategy());

			for (I_DescriptionTuple tuple : descTuples) {
				if (tuple.getTypeNid() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()) {
					return  tuple.getText();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * Gets the test config.
	 *
	 * @return the test config
	 */
	private I_ConfigAceFrame getTestConfig() {
		I_ConfigAceFrame config = null;
		try {
			config = tf.newAceFrameConfig();
			config.addViewPosition(tf.newPosition(
					tf.getPath(new UUID[] {UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66")}), 
					Integer.MAX_VALUE));
			config.addViewPosition(tf.newPosition(
					tf.getPath(new UUID[] {UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")}), 
					Integer.MAX_VALUE));
			config.addEditingPath(tf.getPath(new UUID[] {UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")}));
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
			config.setDefaultStatus(tf.getConcept((ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid())));
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());

			//			I_ConfigAceDb newDbProfile = tf.newAceDbConfig();
			//	        newDbProfile.setUsername("username");
			//	        newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
			//	        newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
			//	        newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
			//	        newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
			//	        config.setDbConfig(newDbProfile);

			config.setPrecedence(Precedence.TIME);

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return config;
	}

	// If targetLocation does not exist, it will be created.
	/**
	 * Copy directory.
	 *
	 * @param sourceLocation the source location
	 * @param targetLocation the target location
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void copyDirectory(File sourceLocation , File targetLocation)
	throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i=0; i<children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]),
						new File(targetLocation, children[i]));
			}
		} else {

			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	/**
	 * Delete directory.
	 *
	 * @param path the path
	 * @return true, if successful
	 */
	public boolean deleteDirectory(File path) {
		if( path.exists() ) {
			File[] files = path.listFiles();
			for(int i=0; i<files.length; i++) {
				if(files[i].isDirectory()) {
					deleteDirectory(files[i]);
				}
				else {
					files[i].delete();
				}
			}
		}
		return( path.delete() );
	}

	/**
	 * Sleep.
	 *
	 * @param n the n
	 */
	public static void sleep(int n){
		long t0, t1;
		t0 =  System.currentTimeMillis();
		do{
			t1 = System.currentTimeMillis();
		}
		while ((t1 - t0) < (n * 1000));
	}
}
