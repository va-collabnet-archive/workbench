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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.document.DocumentManager;
import org.ihtsdo.translation.LanguageUtil;

/**
 * The Class SimpleTranslationConceptEditor.
 */
public class SimpleTranslationConceptEditor extends JPanel {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 9071455913084344350L;
	
	/**
	 * Instantiates a new simple translation concept editor.
	 * 
	 * @param concept the concept
	 * @param config the config
	 * @param sourceLangCode the source lang code
	 * @param targetLangCode the target lang code
	 */
	public SimpleTranslationConceptEditor(I_GetConceptData concept, I_ConfigAceFrame config, String sourceLangCode, String targetLangCode) {
		this.concept = concept;
		this.config = config;
		this.sourceLangCode = sourceLangCode;
		this.targetLangCode = targetLangCode;
		preferredDescription = null;
		fsnDescription = null;

		try {
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}

		initComponents();
		
		// synonyms mode hidden
		radioButton3.setVisible(false);

		updateSourceTable(concept);
		radioButton1.setActionCommand("prefOnly");
		radioButton2.setActionCommand("fsnPref");
		radioButton3.setActionCommand("free");
		radioButton4.setActionCommand("switch");

		updateTargetComponents(concept);

		radioButton1.doClick();


	}

	/**
	 * Update source table.
	 * 
	 * @param concept the concept
	 */
	private void updateSourceTable(I_GetConceptData concept) {
		List <I_DescriptionTuple> descriptions = new ArrayList<I_DescriptionTuple>();
		List <I_DescriptionTuple> fsn = new ArrayList<I_DescriptionTuple>();
		List <I_DescriptionTuple> preferred = new ArrayList<I_DescriptionTuple>();
		List <I_DescriptionTuple> synonyms = new ArrayList<I_DescriptionTuple>();
		I_TermFactory tf = Terms.get();
		try {
			descriptions = (List<I_DescriptionTuple>) concept.getDescriptionTuples(config.getAllowedStatus(), 
					config.getDescTypes(), config.getViewPositionSetReadOnly(), 
					config.getPrecedence(), config.getConflictResolutionStrategy());
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (I_DescriptionTuple description : descriptions) {
			if (description.getLang().equals(sourceLangCode)) {
				try {
					if (description.getTypeId() == tf.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids())) {
						fsn.add(description);
					} else if (description.getTypeId() == tf.uuidToNative(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids())) {
						preferred.add(description);
					} else if (description.getTypeId() == tf.uuidToNative(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids())) {
						synonyms.add(description);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (TerminologyException e) {
					e.printStackTrace();
				}
			}
		}


		String[] columnNames = {"Type",
		"Source Text"};
		String[][] data = null;
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};
		for (I_DescriptionTuple loopDescription : fsn) {
			tableModel.addRow(new String[] {"FSN",loopDescription.getText()});
		}
		for (I_DescriptionTuple loopDescription : preferred) {
			tableModel.addRow(new String[] {"Preferred",loopDescription.getText()});
		}
		for (I_DescriptionTuple loopDescription : fsn) {
			tableModel.addRow(new String[] {"Synonym",loopDescription.getText()});
		}
		table1.setModel(tableModel);
		TableColumnModel cmodel = table1.getColumnModel(); 
		TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
		cmodel.getColumn(0).setCellRenderer(textAreaRenderer); 
		cmodel.getColumn(1).setCellRenderer(textAreaRenderer); 
		table1.revalidate();
	}

	/**
	 * Update target components.
	 * 
	 * @param concept the concept
	 */
	private void updateTargetComponents(I_GetConceptData concept) {
		List <I_DescriptionTuple> descriptions = new ArrayList<I_DescriptionTuple>();
		List <I_DescriptionTuple> fsn = new ArrayList<I_DescriptionTuple>();
		List <I_DescriptionTuple> preferred = new ArrayList<I_DescriptionTuple>();
		List <I_DescriptionTuple> synonyms = new ArrayList<I_DescriptionTuple>();
		try {
			descriptions = (List<I_DescriptionTuple>) concept.getDescriptionTuples(config.getAllowedStatus(), 
					config.getDescTypes(), config.getViewPositionSetReadOnly(), 
					config.getPrecedence(), config.getConflictResolutionStrategy());
		} catch (IOException e) {
			e.printStackTrace();
		}
		I_TermFactory tf = Terms.get();
		for (I_DescriptionTuple description : descriptions) {
			if (description.getLang().equals(targetLangCode)) {
				try {
					if (description.getTypeId() == tf.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids())) {
						fsn.add(description);
					} else if (description.getTypeId() == tf.uuidToNative(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids())) {
						preferred.add(description);
					} else if (description.getTypeId() == tf.uuidToNative(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids())) {
						synonyms.add(description);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (TerminologyException e) {
					e.printStackTrace();
				}
			}
		}


		String[] columnNames = {"Text"};
		String[][] data = null;
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
		for (I_DescriptionTuple loopDescription : fsn) {
			textField1.setText(loopDescription.getText());
			fsnDescription = loopDescription;
			checkBox1.setSelected(loopDescription.isInitialCaseSignificant());
		}
		for (I_DescriptionTuple loopDescription : preferred) {
			textField2.setText(loopDescription.getText());
			preferredDescription = loopDescription;
			checkBox2.setSelected(loopDescription.isInitialCaseSignificant());
		}
		for (I_DescriptionTuple loopDescription : fsn) {
			tableModel.addRow(new String[] {loopDescription.getText()});
		}
		table2.setModel(tableModel);
		TableColumnModel cmodel = table2.getColumnModel(); 
		TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
		cmodel.getColumn(0).setCellRenderer(textAreaRenderer); 
		table2.revalidate();
	}

	/**
	 * Option action performed.
	 * 
	 * @param e the e
	 */
	private void optionActionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("prefOnly")) {
			label1.setVisible(false);
			textField1.setVisible(false);
			checkBox1.setVisible(false);
			label2.setVisible(true);
			textField2.setVisible(true);
			checkBox2.setVisible(true);
			label3.setVisible(false);
			scrollPane2.setVisible(false);
			button1.setVisible(false);
		} else if (e.getActionCommand().equals("fsnPref")) {
			label1.setVisible(true);
			textField1.setVisible(true);
			checkBox1.setVisible(true);
			label2.setVisible(true);
			textField2.setVisible(true);
			checkBox2.setVisible(true);
			label3.setVisible(false);
			scrollPane2.setVisible(false);
			button1.setVisible(false);
		} else if (e.getActionCommand().equals("free")) {
			label1.setVisible(true);
			textField1.setVisible(true);
			checkBox1.setVisible(true);
			label2.setVisible(true);
			textField2.setVisible(true);
			checkBox2.setVisible(true);
			label3.setVisible(true);
			scrollPane2.setVisible(true);
			button1.setVisible(true);
		} else if (e.getActionCommand().equals("switch")) {
			LanguageUtil.openTranlationUI(concept, config, sourceLangCode, targetLangCode, LanguageUtil.ADVANCED_UI);
		}
	}

	/**
	 * Adds the synonym action performed.
	 * 
	 * @param e the e
	 */
	private void addSynonymActionPerformed(ActionEvent e) {
		DefaultTableModel model = (DefaultTableModel) table2.getModel();
		model.addRow(new String[] {"New description"});
		table2.setModel(model);
		table2.revalidate();
	}

	/**
	 * Save action performed.
	 * 
	 * @param e the e
	 */
	private void saveActionPerformed(ActionEvent e) {
		try {
			if (textField1.isVisible()) {
				if (fsnDescription != null) {
					LanguageUtil.persistEditedDescription(concept, fsnDescription, textField1.getText(), 
							fsnDescription.getTypeId(), targetLangCode, config, checkBox1.isSelected(),
							ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
				} else {
					LanguageUtil.persistEditedDescription(concept, null, textField1.getText(), 
							ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid(), 
							targetLangCode, config, checkBox1.isSelected(),
							ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
				}
			}

			if (textField2.isVisible()) {
				if (preferredDescription != null) {
					LanguageUtil.persistEditedDescription(concept, preferredDescription, textField2.getText(), 
							preferredDescription.getTypeId(), targetLangCode, config, checkBox2.isSelected(),
							ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
				} else {
					LanguageUtil.persistEditedDescription(concept, null, textField2.getText(), 
							ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid(), 
							targetLangCode, config, checkBox2.isSelected(),
							ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
				}
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		}
		updateTargetComponents(concept);
	}

	/**
	 * Spell action performed.
	 * 
	 * @param e the e
	 */
	private void spellActionPerformed(ActionEvent e) {
		textField1.setText(DocumentManager.spellcheckPhrase(textField1.getText(), null, targetLangCode));
		textField2.setText(DocumentManager.spellcheckPhrase(textField2.getText(), null, targetLangCode));
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel3 = new JPanel();
		label4 = new JLabel();
		scrollPane1 = new JScrollPane();
		table1 = new JTable();
		panel1 = new JPanel();
		label1 = new JLabel();
		textField1 = new JTextField();
		checkBox1 = new JCheckBox();
		label2 = new JLabel();
		textField2 = new JTextField();
		checkBox2 = new JCheckBox();
		label3 = new JLabel();
		scrollPane2 = new JScrollPane();
		table2 = new JTable();
		panel4 = new JPanel();
		button1 = new JButton();
		panel2 = new JPanel();
		label5 = new JLabel();
		radioButton1 = new JRadioButton();
		radioButton2 = new JRadioButton();
		radioButton3 = new JRadioButton();
		radioButton4 = new JRadioButton();
		panel5 = new JPanel();
		button2 = new JButton();
		button3 = new JButton();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0, 0.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0E-4};

		//======== panel3 ========
		{
			panel3.setLayout(new GridBagLayout());
			((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0, 0};
			((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
			((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

			//---- label4 ----
			label4.setText("Source Descriptions");
			panel3.add(label4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//======== scrollPane1 ========
			{
				scrollPane1.setViewportView(table1);
			}
			panel3.add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0E-4};

			//---- label1 ----
			label1.setText("FSN");
			panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- textField1 ----
			textField1.setColumns(40);
			panel1.add(textField1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- checkBox1 ----
			checkBox1.setText("ICS");
			panel1.add(checkBox1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//---- label2 ----
			label2.setText("Preferred Term");
			panel1.add(label2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- textField2 ----
			textField2.setColumns(40);
			panel1.add(textField2, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- checkBox2 ----
			checkBox2.setText("ICS");
			panel1.add(checkBox2, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//---- label3 ----
			label3.setText("Synonyms");
			panel1.add(label3, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//======== scrollPane2 ========
			{
				scrollPane2.setViewportView(table2);
			}
			panel1.add(scrollPane2, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//======== panel4 ========
			{
				panel4.setLayout(new GridBagLayout());
				((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0};
				((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};
				((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

				//---- button1 ----
				button1.setText("Add Synonym");
				button1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						addSynonymActionPerformed(e);
					}
				});
				panel4.add(button1, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			panel1.add(panel4, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));
		}
		add(panel1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

			//---- label5 ----
			label5.setText("Translation mode");
			panel2.add(label5, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 5, 0), 0, 0));

			//---- radioButton1 ----
			radioButton1.setText("Preferred term only");
			radioButton1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					optionActionPerformed(e);
				}
			});
			panel2.add(radioButton1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 5, 0), 0, 0));

			//---- radioButton2 ----
			radioButton2.setText("FSN & Preferred");
			radioButton2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					optionActionPerformed(e);
				}
			});
			panel2.add(radioButton2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 5, 0), 0, 0));

			//---- radioButton3 ----
			radioButton3.setText("Include synonyms");
			radioButton3.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					optionActionPerformed(e);
				}
			});
			panel2.add(radioButton3, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 5, 0), 0, 0));

			//---- radioButton4 ----
			radioButton4.setText("Swich to advanced mode");
			radioButton4.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					optionActionPerformed(e);
				}
			});
			panel2.add(radioButton4, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel5 ========
		{
			panel5.setLayout(new GridBagLayout());
			((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
			((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- button2 ----
			button2.setText("Save");
			button2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveActionPerformed(e);
				}
			});
			panel5.add(button2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button3 ----
			button3.setText("Spellcheck");
			button3.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					spellActionPerformed(e);
				}
			});
			panel5.add(button3, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
		}
		add(panel5, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 5), 0, 0));

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(radioButton1);
		buttonGroup1.add(radioButton2);
		buttonGroup1.add(radioButton3);
		buttonGroup1.add(radioButton4);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel3;
	private JLabel label4;
	private JScrollPane scrollPane1;
	private JTable table1;
	private JPanel panel1;
	private JLabel label1;
	private JTextField textField1;
	private JCheckBox checkBox1;
	private JLabel label2;
	private JTextField textField2;
	private JCheckBox checkBox2;
	private JLabel label3;
	private JScrollPane scrollPane2;
	private JTable table2;
	private JPanel panel4;
	private JButton button1;
	private JPanel panel2;
	private JLabel label5;
	private JRadioButton radioButton1;
	private JRadioButton radioButton2;
	private JRadioButton radioButton3;
	private JRadioButton radioButton4;
	private JPanel panel5;
	private JButton button2;
	private JButton button3;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	/** The concept. */
	private I_GetConceptData concept;
	
	/** The config. */
	private I_ConfigAceFrame config;
	
	/** The source lang code. */
	private String sourceLangCode;
	
	/** The target lang code. */
	private String targetLangCode;
	
	/** The fsn description. */
	private I_DescriptionTuple fsnDescription;
	
	/** The preferred description. */
	private I_DescriptionTuple preferredDescription;
}
