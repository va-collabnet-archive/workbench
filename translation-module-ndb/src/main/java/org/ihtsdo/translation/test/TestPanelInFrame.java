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
package org.ihtsdo.translation.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.translation.ui.TranslationConceptEditor;

/**
 * The Class TestPanelInFrame.
 */
public class TestPanelInFrame extends JPanel {

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
	public TestPanelInFrame(I_GetConceptData concept, I_ConfigAceFrame config, String langCode, 
			String title, boolean editable) {
	}

	/**
	 * Creates the and show gui.
	 */
	private static void createAndShowGUI() {
		I_GetConceptData sampleConcept = null;
		try {
			vodbDirectory = new File("berkeley-db");
			dbSetupConfig = new DatabaseSetupConfig();
			LocalVersionedTerminology.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
			tf = LocalVersionedTerminology.get();
			//FileInputStream fin = new FileInputStream("/Users/alo/Documents/TermMed/Eclipse/MrcmMaven/miniSct-ide-sa2/src/main/profiles/standalone/standalone.ace");
			FileInputStream fin = new FileInputStream("config.dat");
			ObjectInputStream ois = new ObjectInputStream(fin);
			config = (I_ConfigAceFrame) ois.readObject();
			ois.close();
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
			config.getViewPositionSet().add(tf.newPosition(tf.getPath(new UUID[] {UUID.fromString("d2f8b990-82b9-11de-8a39-0800200c9a66")}), 
					Integer.MAX_VALUE));
			tf.setActiveAceFrameConfig(config);

			sampleConcept = tf.getConcept(new UUID[] {UUID.fromString("4be3f62e-28d5-3bb4-a424-9aa7856a1790")});
		}
		catch (Exception e) { e.printStackTrace(); }

		//Create and set up the window.
		JFrame frame = new JFrame("TestPanelInFrame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create and set up the content pane.
		TranslationConceptEditor newContentPane = new TranslationConceptEditor(sampleConcept, config, "en", "es");
		newContentPane.setOpaque(true); //content panes must be opaque
		frame.setContentPane(newContentPane);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
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
}
