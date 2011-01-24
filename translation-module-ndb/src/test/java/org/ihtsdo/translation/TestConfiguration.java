/**
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
package org.ihtsdo.translation;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import junit.framework.TestCase;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.api.cs.ChangeSetWriterThreading;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.BdbTermFactory;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.translation.ui.ConfigTranslationModule;
import org.ihtsdo.translation.ui.ConfigTranslationModule.EditorMode;
import org.ihtsdo.translation.ui.ConfigTranslationModule.FsnGenerationStrategy;
import org.ihtsdo.translation.ui.ConfigTranslationModule.IcsGenerationStrategy;
import org.ihtsdo.translation.ui.ConfigTranslationModule.InboxColumn;
import org.ihtsdo.translation.ui.ConfigTranslationModule.PreferredTermDefault;
import org.ihtsdo.translation.ui.ConfigTranslationModule.TreeComponent;
import org.ihtsdo.translation.ui.config.ConfigDialog;
import org.ihtsdo.translation.ui.config.EditorModePanel;

/**
 * The Class TestContextualizedDescriptions.
 */
public class TestConfiguration extends TestCase {

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
		System.out.println("Deleting test fixture");
		deleteDirectory(new File("berkeley-db"));
		System.out.println("Creating test fixture");
		copyDirectory(new File("src/test/java/org/ihtsdo/translation/berkeley-db"), new File("berkeley-db"));
		vodbDirectory = new File("berkeley-db");
		dbSetupConfig = new DatabaseSetupConfig();
		System.out.println("Opening database");
		Terms.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
		tf = Terms.get();
		config = getTestConfig();
		tf.setActiveAceFrameConfig(config);
	}

	/**
	 * Test state full.
	 */
	private void testStateFull() {
		try {
			
			//TODO: Testear la nueva configuracion agregada.!
			
			Date timeStamp = new Date();
			
			ConfigTranslationModule confTrans = new ConfigTranslationModule();
			
			assertNull(confTrans.getColumnsDisplayedInInbox());
			assertNull(confTrans.getSelectedEditorMode());
			assertNull(confTrans.getSelectedFsnGenStrategy());
			assertNull(confTrans.getSelectedPrefTermDefault());
			assertNull(confTrans.getSourceTreeComponents());
			assertNull(confTrans.getTargetTreeComponents());
			assertNull(confTrans.getSelectedIcsGenerationStrategy());

			assertFalse(confTrans.isAutoOpenNextInboxItem());
			
			//printConfig(confTrans);
			
			createConfiguration(confTrans);
			
			//printConfig(confTrans);
			
			ConfigTranslationModule confTrans2 = new ConfigTranslationModule();
			confTrans2 = (ConfigTranslationModule)config.getDbConfig().getProperty("TRANSLATION_CONFIG");
			
			printConfig(confTrans2);
			
			assertNotNull(confTrans2.getColumnsDisplayedInInbox());
			assertNotNull(confTrans2.getSelectedEditorMode());
			assertNotNull(confTrans2.getSelectedFsnGenStrategy());
			assertNotNull(confTrans2.getSelectedPrefTermDefault());
			assertNotNull(confTrans2.getSourceTreeComponents());
			assertNotNull(confTrans2.getTargetTreeComponents());
			assertNotNull(confTrans2.getSelectedIcsGenerationStrategy());

			assertTrue(confTrans2.isAutoOpenNextInboxItem());
			
			
			
			
			// Esto graba la configuracion
			//Terms.get().setActiveAceFrameConfig(arg0)
			
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		ConfigTranslationModule confTrans;
		ConfigTranslationModule confTrans2; 
		try {
			confTrans = LanguageUtil.getTranslationConfig(config);
			confTrans.setSelectedEditorMode(EditorMode.PREFERRED_TERM_EDITOR);
			EditorModePanel editorMode = new EditorModePanel(config,confTrans);

			Component[] components = editorMode.getComponents();
			//Finds the Full editor raddio button and selects it
			for (Component component : components) {
				if(component instanceof JPanel){
					Component[] subComp = ((JPanel) component).getComponents();
					for (Component component2 : subComp) {
						if(component2 instanceof JRadioButton){
							JRadioButton button = (JRadioButton)component2;
							System.out.println(button.getText());
							if(button.getText().equals(EditorMode.FULL_EDITOR.toString())){
								button.doClick();
							}
						}
					}
				}
			}
			
			//Configuration instance changed
			assertEquals(confTrans.getSelectedEditorMode(), EditorMode.FULL_EDITOR);
			
			//Gets the configuration again
			confTrans2 = LanguageUtil.getTranslationConfig(config);
			//It should not be Full editor mod yet
			assertFalse(confTrans2.getSelectedEditorMode().equals(EditorMode.FULL_EDITOR.toString()));
			
			//Finds the jbutton and clicks it
			for (Component component : components) {
				if(component instanceof JPanel){
					Component[] subComp = ((JPanel) component).getComponents();
					for (Component component2 : subComp) {
						if(component2 instanceof JButton){
							JButton button = (JButton)component2;
							button.doClick();
						}
					}
				}
			}
			
			//After the click we get the configuration and it should be changed.
			confTrans2 = LanguageUtil.getTranslationConfig(config);
			assertTrue(confTrans2.getSelectedEditorMode().toString().equals(EditorMode.FULL_EDITOR.toString()));
			
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	public void testConfigDialog(){
		
		
		ConfigTranslationModule confTrans = new ConfigTranslationModule();
		createConfiguration(confTrans);
		
		ConfigDialog panle = new ConfigDialog(config,confTrans, true,null);
		Thread th = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(40000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		th.start();
		try {
			th.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	private void createConfiguration(ConfigTranslationModule confTrans){
		confTrans.setAutoOpenNextInboxItem(true);
		
		LinkedHashSet<InboxColumn> inboxCol = new LinkedHashSet<InboxColumn>();
		inboxCol.add(InboxColumn.SOURCE_PREFERRED);
		inboxCol.add(InboxColumn.STATUS);
		confTrans.setColumnsDisplayedInInbox(inboxCol);
		
		LinkedHashSet<TreeComponent> sourceTreeComponents = new LinkedHashSet<TreeComponent>();
		sourceTreeComponents.add(TreeComponent.FSN);
		sourceTreeComponents.add(TreeComponent.SYNONYM);
		confTrans.setSourceTreeComponents(sourceTreeComponents);
		
		LinkedHashSet<TreeComponent> targetTreeComponents = new LinkedHashSet<TreeComponent>();
		targetTreeComponents.add(TreeComponent.FSN);
		targetTreeComponents.add(TreeComponent.PREFERRED);
		confTrans.setTargetTreeComponents(targetTreeComponents);
		
		confTrans.setSelectedEditorMode(EditorMode.PREFERRED_TERM_EDITOR);
		confTrans.setSelectedFsnGenStrategy(FsnGenerationStrategy.LINK_SOURCE_LANGUAGE);
		confTrans.setSelectedIcsGenerationStrategy(IcsGenerationStrategy.NONE);
		confTrans.setSelectedPrefTermDefault(PreferredTermDefault.SOURCE);
		
		try {
			config.getDbConfig().setProperty("TRANSLATION_CONFIG", confTrans);
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void printConfig(ConfigTranslationModule confTrans){
		System.out.println("\nSelected editor mode.");
		System.out.println("\t" + confTrans.getSelectedEditorMode());
		
		System.out.println("Fully specified name generation strategy.");
		System.out.println("\t" + confTrans.getSelectedFsnGenStrategy());
		
		System.out.println("Selected Preferd term default.");
		System.out.println("\t" + confTrans.getSelectedPrefTermDefault());
		
		System.out.println("Selected ICS generation strategy.");
		System.out.println("\t" + confTrans.getSelectedIcsGenerationStrategy());
		
		System.out.println("Auto open next inbox item.");
		System.out.println("\t" +  confTrans.isAutoOpenNextInboxItem());

		if(confTrans.getColumnsDisplayedInInbox() != null){
			System.out.println("Columns displayd in inbox.");
			for (InboxColumn col : confTrans.getColumnsDisplayedInInbox()) {
				System.out.println("\t" + col.getEditorClass());
			}
		}
		
		if(confTrans.getSourceTreeComponents() != null){
			System.out.println("Source tree components.");
			for (TreeComponent sourceTreeComp : confTrans.getSourceTreeComponents()) {
				System.out.println("\t" + sourceTreeComp);
			}
		}
		
		if(confTrans.getTargetTreeComponents() != null){
			System.out.println("Target tree components.");
			for (TreeComponent targeTreeComp : confTrans.getTargetTreeComponents()) {
				System.out.println("\t" + targeTreeComp);
			}
		}
		
	}

	private I_ConfigAceFrame getTestConfig() {
		I_ConfigAceFrame config = null;
		try {
			config = tf.newAceFrameConfig();
			BdbTermFactory tf2 = (BdbTermFactory) tf;
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

			I_ConfigAceDb newDbProfile = tf2.newAceDbConfig();
			newDbProfile.setUsername("username");
			newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
			newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
			newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
			newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
			config.setDbConfig(newDbProfile);

			config.setPrecedence(Precedence.TIME);

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return config;
	}

	// If targetLocation does not exist, it will be created.
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
}
