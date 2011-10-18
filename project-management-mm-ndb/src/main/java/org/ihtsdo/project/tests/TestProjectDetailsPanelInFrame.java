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
package org.ihtsdo.project.tests;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.PartitionScheme;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.panel.details.ProjectDetailsPanel;

/**
 * The Class TestPanelInFrame.
 */
public class TestProjectDetailsPanelInFrame extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The vodb directory. */
	static File vodbDirectory;
	
	/** The read only. */
	static boolean readOnly = false;
	
	/** The cache size. */
	static Long cacheSize = Long.getLong("600000000");
	
	/** The db setup config. */
	static DatabaseSetupConfig dbSetupConfig;
	
	/** The config. */
	static I_ConfigAceFrame config;
	
	/** The tf. */
	static I_TermFactory tf;


	/**
	 * Instantiates a new test panel in frame.
	 * 
	 * @param concept the concept
	 * @param config the config
	 * @param langCode the lang code
	 * @param title the title
	 * @param editable the editable
	 */
	public TestProjectDetailsPanelInFrame(I_GetConceptData concept, I_ConfigAceFrame config, String langCode, 
			String title, boolean editable) {
	}

	/**
	 * Creates the and show gui.
	 */
	private static void createAndShowGUI() {
		try {
			vodbDirectory = new File("berkeley-db");
			dbSetupConfig = new DatabaseSetupConfig();
			System.out.println("Opening database");
			Terms.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
			tf = Terms.get();
			config = getTestConfig();
			tf.setActiveAceFrameConfig(config);
			
			List<TranslationProject> projects = TerminologyProjectDAO.getAllTranslationProjects(config);
			TranslationProject project = projects.iterator().next();
			
			List<WorkSet> workSets = project.getWorkSets(config);
			WorkSet workSet = workSets.iterator().next();
			PartitionScheme partitionScheme = workSet.getPartitionSchemes(config).iterator().next();
			//Partition partition = partitionScheme.getPartitions().iterator().next();
			//WorkList workList = partition.getWorkLists().iterator().next();
			
			JPanel panel = new ProjectDetailsPanel(project, config);
			//JPanel panel = new WorkSetDetailsPanel(workSet, config);
			//JPanel panel = new PartitionSchemeDetailsPanel(partitionScheme, config);
			//JPanel panel = new RefsetPartitionerPanel(partitionScheme, config);
			//JPanel panel = new PartitionDetailsPanel(partition, config);
			//JPanel panel = new WorkListDetailsPanel(workList, config, null);
			//Create and set up the window.
			JFrame frame = new JFrame("TestPanelInFrame");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			//Create and set up the content pane.
			
			panel.setOpaque(true); //content panes must be opaque
			frame.setContentPane(panel);

			//Display the window.
			frame.pack();
			frame.setVisible(true);
		}
		catch (Exception e) { e.printStackTrace(); }

		
	}

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
	
	private static I_ConfigAceFrame getTestConfig() {
		I_ConfigAceFrame config = null;
		try {
			config = tf.newAceFrameConfig();
			config.addViewPosition(tf.newPosition(
					tf.getPath(new UUID[] {UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66")}), 
					Integer.MAX_VALUE));
			config.addViewPosition(tf.newPosition(
					tf.getPath(new UUID[] {UUID.fromString("d638b00c-eda1-4f99-9440-5c22bf93f601")}), 
					Integer.MAX_VALUE));
			config.addEditingPath(tf.getPath(new UUID[] {UUID.fromString("d638b00c-eda1-4f99-9440-5c22bf93f601")}));
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
			config.setDefaultStatus(tf.getConcept((ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid())));
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return config;
	}
}
