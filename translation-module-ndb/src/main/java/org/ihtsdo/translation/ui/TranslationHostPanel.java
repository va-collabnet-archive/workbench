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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.UUID;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
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
import org.ihtsdo.document.DocumentManager;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.SimilarityMatchedItem;
import org.ihtsdo.translation.SimilarityResultsPanel;

/**
 * The Class TranslationHostPanel.
 */
public class TranslationHostPanel extends JPanel implements FocusListener, ActionListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The source panel. */
	TranslationMultifunctionPanel sourcePanel;

	/** The target panel. */
	TranslationMultifunctionPanel targetPanel;

	/** The similarity panel. */
	JPanel similarityPanel;

	/** The concept. */
	I_GetConceptData concept;

	/** The source lang code. */
	String sourceLangCode;

	/** The target lang code. */
	String targetLangCode;

	/** The info column. */
	JPanel infoColumn;

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
	 * Instantiates a new translation host panel.
	 * 
	 * @param concept the concept
	 * @param config the config
	 * @param sourceLangCode the source lang code
	 * @param targetLangCode the target lang code
	 */
	public TranslationHostPanel(I_GetConceptData concept, I_ConfigAceFrame config, String sourceLangCode, String targetLangCode) {

		try {
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TerminologyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		TranslationHostPanel.config = config;
		this.sourceLangCode = sourceLangCode;
		this.targetLangCode = targetLangCode;
		this.concept = concept;
		this.setLayout(new BorderLayout());

		Container topContainer = new Container();
		topContainer.setLayout(new FlowLayout());

		Container middleContainer = new Container();
		middleContainer.setLayout(new FlowLayout());

		Container sourceColumn = new Container();
		sourceColumn.setLayout(new BoxLayout(sourceColumn, BoxLayout.Y_AXIS));

		Container targetColumn = new Container();
		targetColumn.setLayout(new BoxLayout(targetColumn, BoxLayout.Y_AXIS));

		infoColumn = new JPanel();
		infoColumn.setLayout(new BoxLayout(infoColumn, BoxLayout.Y_AXIS));

		topContainer.add(new JLabel("Translation window, some tollbar will probably be here...."));
		JButton checkSpellButton = new JButton("Check spelling");
		checkSpellButton.addActionListener(this);
		checkSpellButton.setActionCommand("spelling");
		topContainer.add(checkSpellButton);
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		saveButton.setActionCommand("save");
		topContainer.add(saveButton);

		infoColumn.add(new JLabel("Information"));
		infoColumn.add(new JTextArea("...."));

		sourcePanel = new TranslationMultifunctionPanel(concept, config, "en", "Source: English", false);
		sourcePanel.setName("sourcePanel");
		targetPanel = new TranslationMultifunctionPanel(concept, config, "es", "Target: Spanish", true);
		targetPanel.setName("targetPanel");

		for (JTextField field : sourcePanel.getFieldsMap().keySet()) {
			field.addFocusListener(this);
			try {
				if (sourcePanel.getFieldsMap().get(field) != null) {
					I_DescriptionVersioned<?> desc = sourcePanel.getFieldsMap().get(field);
					I_DescriptionTuple tuple = desc.getTuples(
							config.getConflictResolutionStrategy()).iterator().next();
					if (tuple.getTypeId() == ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid()) {
						updateSimilarityPanel(field.getText());
					}
				}
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (JTextField field : targetPanel.getFieldsMap().keySet()) {
			field.addFocusListener(this);
		}


		sourceColumn.add(sourcePanel);
		targetColumn.add(targetPanel);

		sourceColumn.add(Box.createVerticalGlue());
		targetColumn.add(Box.createVerticalGlue());

		middleContainer.add(sourceColumn);
		middleContainer.add(targetColumn);

		this.add(topContainer, BorderLayout.PAGE_START);
		this.add(middleContainer, BorderLayout.CENTER);
		this.add(infoColumn, BorderLayout.PAGE_END);

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
		JFrame frame = new JFrame("TranslationPanel");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create and set up the content pane.
		TranslationHostPanel newContentPane = new TranslationHostPanel(sampleConcept, config, "en", "es");
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

	/* (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	public void focusGained(FocusEvent e) {
		if (e.getComponent().getParent().getName().equals("sourcePanel")) {
			JTextField focusedField = (JTextField)e.getComponent();
			updateSimilarityPanel(focusedField.getText());
		} 

	}

	/* (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	public void focusLost(FocusEvent e) {
		/*if (e.getComponent().getParent().getName().equals("targetPanel")) {
			JTextField focusedField = (JTextField)e.getComponent();
			DocumentManager.spellcheckPhrase(focusedField.getText(), null);
		}*/
	}

	/**
	 * Update similarity panel.
	 * 
	 * @param text the text
	 */
	public void updateSimilarityPanel(String text) {
		List<SimilarityMatchedItem> results = LanguageUtil.getSimilarityResults(text, 
				sourceLangCode, targetLangCode, config);
		similarityPanel = new SimilarityResultsPanel(text, sourceLangCode, targetLangCode, results, config);
		Component[] components = infoColumn.getComponents();
		for (int i = 0; i < components.length; i++) {
			infoColumn.remove(components[i]);
		}
		infoColumn.add(similarityPanel);
		infoColumn.revalidate();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if ("spelling".equals(e.getActionCommand()) || "save".equals(e.getActionCommand())) {
			for (JTextField field : targetPanel.getFieldsMap().keySet()) {
				if (field.getText() != null) {
					field.setText(DocumentManager.spellcheckPhrase(field.getText(), null, targetLangCode));
				}
			}
		} if ("save".equals(e.getActionCommand())) {
			for (JTextField field : targetPanel.getFieldsMap().keySet()) {
				if (field.getText() != null) {
					if (targetPanel.getFieldsMap().get(field) != null) {
						I_DescriptionTuple tuple = null;
						try {
							I_DescriptionVersioned<?> desc = targetPanel.getFieldsMap().get(field);
							tuple = desc.getTuples(
									config.getConflictResolutionStrategy()).iterator().next();
						} catch (TerminologyException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						LanguageUtil.persistEditedDescription(concept, tuple.getDescId(), field.getText(), 
								tuple.getTypeId(), targetLangCode, config, true, tuple.getStatusId());
					} else {
						try {
							LanguageUtil.persistEditedDescription(concept, Integer.MAX_VALUE, field.getText(), 
									ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid(), targetLangCode, config, 
									true, ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (TerminologyException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
		}
	}
}
