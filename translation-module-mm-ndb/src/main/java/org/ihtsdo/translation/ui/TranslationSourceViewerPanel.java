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
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 * The Class TranslationSourceViewerPanel.
 */
public class TranslationSourceViewerPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The table model. */
	DefaultTableModel tableModel;
	
	/** The table. */
	JTable table;

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
	 * Instantiates a new translation source viewer panel.
	 * 
	 * @param concept the concept
	 * @param config the config
	 */
	@SuppressWarnings("unchecked")
	public TranslationSourceViewerPanel(I_GetConceptData concept, I_ConfigAceFrame config) {
		this.setLayout(new BorderLayout());

		String[] columnNames = {"Description", "Type"};
		String[][] data = null;
		tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};

		table = new JTable(tableModel);
		List<I_DescriptionTuple> descriptions = new ArrayList<I_DescriptionTuple>();
		try {
			descriptions = (List<I_DescriptionTuple>) concept.getDescriptionTuples(config.getAllowedStatus(), 
					config.getDescTypes(), config.getViewPositionSetReadOnly(), config.getPrecedence(),
					config.getConflictResolutionStrategy());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (I_DescriptionTuple description : descriptions) {
			try {
				tableModel.addRow(new String[] {description.getText(), tf.getConcept(description.getTypeId()).toString()});
			} catch (TerminologyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		JScrollPane scrollPane = new JScrollPane(table);
		table.setGridColor(Color.GRAY);

		this.add(scrollPane, BorderLayout.CENTER);

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
			tf.setActiveAceFrameConfig(config);

			sampleConcept = tf.getConcept(new UUID[] {UUID.fromString("4be3f62e-28d5-3bb4-a424-9aa7856a1790")});
		}
		catch (Exception e) { e.printStackTrace(); }

		//Create and set up the window.
		JFrame frame = new JFrame("DictionaryIndexPanel");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create and set up the content pane.
		TranslationSourceViewerPanel newContentPane = new TranslationSourceViewerPanel(sampleConcept, config);
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
