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
package org.ihtsdo.translation.ui;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 * The Class TranslationMultifunctionPanel.
 */
public class TranslationMultifunctionPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The fields map. */
	private HashMap<JTextField,I_DescriptionVersioned> fieldsMap = new HashMap<JTextField,I_DescriptionVersioned>();

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
	 * Instantiates a new translation multifunction panel.
	 * 
	 * @param concept the concept
	 * @param config the config
	 * @param langCode the lang code
	 * @param title the title
	 * @param editable the editable
	 */
	@SuppressWarnings("unchecked")
	public TranslationMultifunctionPanel(I_GetConceptData concept, I_ConfigAceFrame config, String langCode, 
			String title, boolean editable) {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		tf = LocalVersionedTerminology.get();
		
		JLabel titleLabel = new JLabel("<html><body><b>" + title);
		this.add(titleLabel);
		this.add(Box.createRigidArea(new Dimension(10,10)));

		this.add(new JLabel("FullySpecifiedName"));

		JTextField fsn = new JTextField(30);
		fsn.setEditable(editable);
		this.add(fsn);
		fieldsMap.put(fsn, null);

		this.add(new JLabel("Preferred Term"));

		JTextField preferred = new JTextField(30);
		this.add(preferred);
		preferred.setEditable(editable);
		fieldsMap.put(preferred, null);

		this.add(new JLabel("Synonyms"));

		List<I_DescriptionTuple> descriptions = new ArrayList<I_DescriptionTuple>();
		try {
			descriptions = (List<I_DescriptionTuple>) concept.getDescriptionTuples(
					config.getAllowedStatus(), config.getDescTypes(), config.getViewPositionSetReadOnly(), 
					config.getPrecedence(), config.getConflictResolutionStrategy());
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (I_DescriptionTuple description : descriptions) {
			if (description.getLang().equals(langCode)) {
				try {
					if (description.getTypeId() == tf.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids())) {
						fsn.setText(description.getText());
						fieldsMap.put(fsn, description.getDescVersioned());
					} else if (description.getTypeId() == tf.uuidToNative(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids())) {
						preferred.setText(description.getText());
						fieldsMap.put(preferred, description.getDescVersioned());
					} else if (description.getTypeId() == tf.uuidToNative(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids())) {
						JTextField loopSynonym = new JTextField(description.getText(),30);
						loopSynonym.setEditable(editable);
						this.add(loopSynonym);
						fieldsMap.put(loopSynonym, description.getDescVersioned());
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (TerminologyException e) {
					e.printStackTrace();
				}
			}
		}
		
		if (editable) {
			JTextField addtionalSynonym1 = new JTextField(30);
			this.add(addtionalSynonym1);
			fieldsMap.put(addtionalSynonym1, null);
			JTextField addtionalSynonym2 = new JTextField(30);
			this.add(addtionalSynonym2);
			fieldsMap.put(addtionalSynonym2, null);
			JTextField addtionalSynonym3 = new JTextField(30);
			this.add(addtionalSynonym3);
			fieldsMap.put(addtionalSynonym3, null);
		}
		
		this.add(Box.createVerticalGlue());


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
		JFrame frame = new JFrame("DictionaryIndexPanel");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create and set up the content pane.
		TranslationMultifunctionPanel newContentPane = new TranslationMultifunctionPanel(sampleConcept, config, "es", "Source Language", true);
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

	/**
	 * Gets the fields map.
	 * 
	 * @return the fields map
	 */
	public HashMap<JTextField, I_DescriptionVersioned> getFieldsMap() {
		return fieldsMap;
	}

}
