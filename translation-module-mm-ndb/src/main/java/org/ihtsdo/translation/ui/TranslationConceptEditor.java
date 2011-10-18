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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.document.DocumentManager;
import org.ihtsdo.project.issue.manager.IssuesPanel;
import org.ihtsdo.project.issue.manager.IssuesView;
import org.ihtsdo.project.panel.PanelHelperFactory;
import org.ihtsdo.project.panel.TranslationHelperPanel;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.SimilarityMatchedItem;

/**
 * The Class TranslationConceptEditor.
 */
public class TranslationConceptEditor extends JPanel {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new translation concept editor.
	 * 
	 * @param concept the concept
	 * @param config the config
	 * @param sourceLangCode the source lang code
	 * @param targetLangCode the target lang code
	 */
	public TranslationConceptEditor(I_GetConceptData concept, I_ConfigAceFrame config, String sourceLangCode, String targetLangCode) {
		this.concept = concept;
		this.config = config;
		this.sourceLangCode = sourceLangCode;
		this.targetLangCode = targetLangCode;

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

		bDescIssue.setEnabled(false);
		DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
		try {
			comboBoxModel.addElement(
					Terms.get().getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
			comboBoxModel.addElement(
					Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
			comboBoxModel.addElement(
					Terms.get().getConcept(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids()));
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		comboBox1.setModel(comboBoxModel);
		comboBox1.setSelectedIndex(1);
		comboBox1.setVisible(false);

		tree1.setCellRenderer(new HtmlSafeTreeCellRenderer());
		button1.setEnabled(false);
		button2.setEnabled(false);
		button3.setEnabled(false);
		button4.setEnabled(true);
		button5.setEnabled(false);
		editorPane1.setEditable(false);

		populateTree();
	}

	/**
	 * Tree1 value changed.
	 * 
	 * @param e the e
	 */
	private void tree1ValueChanged(TreeSelectionEvent e) {
		updatePropertiesPanel();
	}

	/**
	 * Save action performed.
	 * 
	 * @param e the e
	 */
	private void saveActionPerformed(ActionEvent e) {
		try {
			if (descriptionInEditor != null) {
				LanguageUtil.persistEditedDescription(concept, descriptionInEditor, textField1.getText(), 
						descriptionInEditor.getTypeNid(), targetLangCode, config, checkBox1.isSelected(),
						ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			} else {
				I_GetConceptData typeConcept = (I_GetConceptData) comboBox1.getSelectedItem();
				LanguageUtil.persistEditedDescription(concept, null, textField1.getText(), 
						typeConcept.getConceptNid(), targetLangCode, config, checkBox1.isSelected(),
						ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			}
			clearForm();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		}

		populateTree();
	}

	/**
	 * Cancel action performed.
	 * 
	 * @param e the e
	 */
	private void cancelActionPerformed(ActionEvent e) {
		clearForm();
	}
	
	/**
	 * Clear form.
	 */
	private void clearForm(){
		descriptionInEditor = null;
		comboBox1.setVisible(false);
		label4.setVisible(true);
		label4.setText("");
		textField1.setText("");
		textField1.setEnabled(false);
		checkBox1.setSelected(false);
		panel2.revalidate();
		button1.setEnabled(false);
		button2.setEnabled(false);
		button3.setEnabled(false);
		button5.setEnabled(false);
		button4.setEnabled(true);
	}

	/**
	 * Spellcheck action performed.
	 * 
	 * @param e the e
	 */
	private void spellcheckActionPerformed(ActionEvent e) {
		textField1.setText(DocumentManager.spellcheckPhrase(textField1.getText(), null, targetLangCode));
	}

	/**
	 * Retire action performed.
	 * 
	 * @param e the e
	 */
	private void retireActionPerformed(ActionEvent e) {
		try {
			LanguageUtil.persistEditedDescription(concept, descriptionInEditor, descriptionInEditor.getText(), 
					descriptionInEditor.getTypeNid(), targetLangCode, config, descriptionInEditor.isInitialCaseSignificant(),
					ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		}
		populateTree();	
	}

	/**
	 * Check box1 item state changed.
	 * 
	 * @param e the e
	 */
	private void checkBox1ItemStateChanged(ItemEvent e) {
	}

	/**
	 * Adds the new action performed.
	 * 
	 * @param e the e
	 */
	private void addNewActionPerformed(ActionEvent e) {
		descriptionInEditor = null;
		label4.setText("");
		label4.setVisible(false);
		textField1.setText("");
		textField1.setEnabled(true);
		checkBox1.setSelected(false);
		panel2.revalidate();
		button1.setEnabled(true);
		button2.setEnabled(true);
		button3.setEnabled(true);
		button5.setEnabled(false);
		button4.setEnabled(false);	
		label4.setVisible(false);
		comboBox1.setVisible(true);
	}

	/**
	 * B add fsn action performed.
	 */
	private void bAddFSNActionPerformed() {
		I_GetConceptData concType;
		try {
			concType = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());

			LanguageUtil.persistEditedDescription(concept, null, targetPreferred.trim() + 
					" (" + LanguageUtil.getTargetEquivalentSemTag(sourceSemTag, sourceLangCode, targetLangCode).trim() + ")", 
					concType.getConceptNid(), targetLangCode, config, targetPreferredICS,
					ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		populateTree();
	}

	/**
	 * B cpt issue action performed.
	 */
	private void bCptIssueActionPerformed() {
		try {
			launchIssuePanel(concept.getUids().get(0).toString(),concept.getInitialText());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the component by class.
	 * 
	 * @param nameComp the name comp
	 * @param comps the comps
	 * 
	 * @return the component by class
	 */
	private Component getComponentByClass(String nameComp, Component[] comps){
		Component issueComp=null;
		for (int i=0 ;i<comps.length;i++){	
			System.out.println(comps[i].getClass().toString());
			if (comps[i].getClass().toString().equals(nameComp)){
				issueComp=comps[i];
				break;
			}
		}
		return issueComp;
	}

	/**
	 * Launch issue panel.
	 * 
	 * @param componentId the component id
	 * @param componentName the component name
	 */
	private void launchIssuePanel(String componentId,String componentName){
		TranslationHelperPanel thp;
		try {
			thp = PanelHelperFactory.getTranslationHelperPanel();

			JTabbedPane tp=thp.getTabbedPanel();
			int tabCount=tp.getTabCount();
			if (tp!=null){
				for (int i=0;i<tabCount;i++){
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.ISSUE_TAB_NAME)){
						tp.setSelectedIndex(i);
						thp.showTabbedPanel();

						IssuesPanel issPanel=(IssuesPanel)getComponentByClass("class org.dwfa.issue.manager.IssuesPanel",tp.getComponents());
						//if (issPanel!=null)
						issPanel.setComponentId(componentId,componentName);

						thp.showTabbedPanel();
						return;
					}
				}
			}
			IssuesPanel pmp=new IssuesPanel(
					Terms.get());
			tp.addTab(TranslationHelperPanel.ISSUE_TAB_NAME, pmp);
			tp.setSelectedIndex(tabCount);
			thp.showTabbedPanel();

			IssuesPanel issPanel=(IssuesPanel)getComponentByClass("class org.dwfa.issue.manager.IssuesPanel",tp.getComponents());
			//if (issPanel!=null)
			issPanel.setComponentId(componentId,componentName);
			return;
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * B desc issue action performed.
	 */
	private void bDescIssueActionPerformed() {
		try {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)  tree1.getLastSelectedPathComponent();

			if (node != null) {
				Object nodeInfo = node.getUserObject();
				DescriptionTreeNode descriptionNode = (DescriptionTreeNode)nodeInfo;
				if (descriptionNode.getType() == DescriptionTreeNode.DESCRIPTION_TYPE) {
					if (!descriptionNode.getDescription().getLang().equals(targetLangCode)) {

						I_TermFactory tf = Terms.get();
						launchIssuePanel(tf.getUids(descriptionNode.getDescription().getDescId()).iterator().next().toString(),
								descriptionNode.getDescription().getText());
					}
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * B view issue action performed.
	 */
	private void bViewIssueActionPerformed() {
		launchIssueViewPanel();
	}

	/**
	 * Launch issue view panel.
	 */
	private void launchIssueViewPanel() {
		TranslationHelperPanel thp;
		try {
			thp = PanelHelperFactory.getTranslationHelperPanel();

			JTabbedPane tp=thp.getTabbedPanel();
			int tabCount=tp.getTabCount();
			if (tp!=null){
				for (int i=0;i<tabCount;i++){
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.ISSUE_VIEW_TAB_NAME)){
						tp.removeTabAt(i);	
					}
				}
			}
			IssuesView issView=new IssuesView(concept,sourceLangCode);
			tp.addTab(TranslationHelperPanel.ISSUE_VIEW_TAB_NAME, issView);
			tp.setSelectedIndex(tp.getTabCount()-1);
			thp.showTabbedPanel();

			return;
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Button6 action performed.
	 * 
	 * @param e the e
	 */
	private void button6ActionPerformed(ActionEvent e) {
		LanguageUtil.openTranlationUI(concept, config, sourceLangCode, targetLangCode, LanguageUtil.SIMPLE_UI);
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		scrollPane1 = new JScrollPane();
		tree1 = new JTree();
		panel2 = new JPanel();
		label1 = new JLabel();
		panel5 = new JPanel();
		label4 = new JLabel();
		comboBox1 = new JComboBox();
		label2 = new JLabel();
		textField1 = new JTextField();
		label3 = new JLabel();
		checkBox1 = new JCheckBox();
		panel7 = new JPanel();
		bCptIssue = new JButton();
		bDescIssue = new JButton();
		bViewIssue = new JButton();
		panel6 = new JPanel();
		tabbedPane1 = new JTabbedPane();
		scrollPane2 = new JScrollPane();
		table1 = new JTable();
		scrollPane3 = new JScrollPane();
		table2 = new JTable();
		scrollPane4 = new JScrollPane();
		editorPane1 = new JEditorPane();
		panel4 = new JPanel();
		button6 = new JButton();
		panel3 = new JPanel();
		bAddFSN = new JButton();
		button3 = new JButton();
		button1 = new JButton();
		button5 = new JButton();
		button4 = new JButton();
		button2 = new JButton();
		label5 = new JLabel();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {255, 437, 200, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {149, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {325, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {147, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

			//======== scrollPane1 ========
			{

				//---- tree1 ----
				tree1.setVisibleRowCount(1);
				tree1.addTreeSelectionListener(new TreeSelectionListener() {
					@Override
					public void valueChanged(TreeSelectionEvent e) {
						tree1ValueChanged(e);
					}
				});
				scrollPane1.setViewportView(tree1);
			}
			panel1.add(scrollPane1, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 321, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0, 1.0E-4};

			//---- label1 ----
			label1.setText("Description Type");
			panel2.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//======== panel5 ========
			{
				panel5.setLayout(new FlowLayout(FlowLayout.LEFT));
				panel5.add(label4);
				panel5.add(comboBox1);
			}
			panel2.add(panel5, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//---- label2 ----
			label2.setText("Text");
			panel2.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- textField1 ----
			textField1.setEnabled(false);
			panel2.add(textField1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//---- label3 ----
			label3.setText("Is case significant");
			panel2.add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- checkBox1 ----
			checkBox1.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					checkBox1ItemStateChanged(e);
				}
			});
			panel2.add(checkBox1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//======== panel7 ========
			{
				panel7.setLayout(new GridBagLayout());
				((GridBagLayout)panel7.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel7.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel7.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
				((GridBagLayout)panel7.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

				//---- bCptIssue ----
				bCptIssue.setText("Add Concept Issue");
				bCptIssue.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						bCptIssueActionPerformed();
					}
				});
				panel7.add(bCptIssue, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- bDescIssue ----
				bDescIssue.setText("Add Description Issue");
				bDescIssue.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						bDescIssueActionPerformed();
					}
				});
				panel7.add(bDescIssue, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- bViewIssue ----
				bViewIssue.setText("View Issues");
				bViewIssue.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						bViewIssueActionPerformed();
					}
				});
				panel7.add(bViewIssue, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			panel2.add(panel7, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//======== panel6 ========
		{
			panel6.setLayout(new BoxLayout(panel6, BoxLayout.Y_AXIS));

			//======== tabbedPane1 ========
			{

				//======== scrollPane2 ========
				{

					//---- table1 ----
					table1.setPreferredScrollableViewportSize(new Dimension(180, 200));
					scrollPane2.setViewportView(table1);
				}
				tabbedPane1.addTab("Similarity", scrollPane2);


				//======== scrollPane3 ========
				{

					//---- table2 ----
					table2.setPreferredScrollableViewportSize(new Dimension(180, 200));
					scrollPane3.setViewportView(table2);
				}
				tabbedPane1.addTab("Translation Memory", scrollPane3);


				//======== scrollPane4 ========
				{

					//---- editorPane1 ----
					editorPane1.setContentType("text/html");
					scrollPane4.setViewportView(editorPane1);
				}
				tabbedPane1.addTab("Glossary", scrollPane4);

			}
			panel6.add(tabbedPane1);
		}
		add(panel6, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel4 ========
		{
			panel4.setLayout(new GridBagLayout());
			((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
			((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- button6 ----
			button6.setText("Switch to simple mode");
			button6.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button6ActionPerformed(e);
				}
			});
			panel4.add(button6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
		}
		add(panel4, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//======== panel3 ========
		{
			panel3.setLayout(new GridBagLayout());
			((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0};
			((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- bAddFSN ----
			bAddFSN.setText("Add FSN");
			bAddFSN.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					bAddFSNActionPerformed();
				}
			});
			panel3.add(bAddFSN, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button3 ----
			button3.setText("Spellcheck");
			button3.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					spellcheckActionPerformed(e);
				}
			});
			panel3.add(button3, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button1 ----
			button1.setText("Save");
			button1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					saveActionPerformed(e);
				}
			});
			panel3.add(button1, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button5 ----
			button5.setText("Retire");
			button5.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					retireActionPerformed(e);
				}
			});
			panel3.add(button5, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button4 ----
			button4.setText("Add new");
			button4.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					addNewActionPerformed(e);
				}
			});
			panel3.add(button4, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button2 ----
			button2.setText("Cancel");
			button2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cancelActionPerformed(e);
				}
			});
			panel3.add(button2, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel3, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- label5 ----
		label5.setText("Results");
		add(label5, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
			new Insets(0, 0, 5, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel1;
	private JScrollPane scrollPane1;
	private JTree tree1;
	private JPanel panel2;
	private JLabel label1;
	private JPanel panel5;
	private JLabel label4;
	private JComboBox comboBox1;
	private JLabel label2;
	private JTextField textField1;
	private JLabel label3;
	private JCheckBox checkBox1;
	private JPanel panel7;
	private JButton bCptIssue;
	private JButton bDescIssue;
	private JButton bViewIssue;
	private JPanel panel6;
	private JTabbedPane tabbedPane1;
	private JScrollPane scrollPane2;
	private JTable table1;
	private JScrollPane scrollPane3;
	private JTable table2;
	private JScrollPane scrollPane4;
	private JEditorPane editorPane1;
	private JPanel panel4;
	private JButton button6;
	private JPanel panel3;
	private JButton bAddFSN;
	private JButton button3;
	private JButton button1;
	private JButton button5;
	private JButton button4;
	private JButton button2;
	private JLabel label5;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	/** The descriptions. */
	private java.util.List<I_DescriptionTuple> descriptions;
	
	/** The concept. */
	private I_GetConceptData concept;
	
	/** The config. */
	private I_ConfigAceFrame config;
	
	/** The source lang code. */
	private String sourceLangCode;
	
	/** The target lang code. */
	private String targetLangCode;
	
	/** The description in editor. */
	private I_DescriptionTuple descriptionInEditor;
	
	/** The target preferred. */
	private String targetPreferred;
	
	/** The target preferred ics. */
	private boolean targetPreferredICS;
	
	/** The source sem tag. */
	private String sourceSemTag;

	/**
	 * Gets the concept.
	 * 
	 * @return the concept
	 */
	public I_GetConceptData getConcept() {
		return concept;
	}

	/**
	 * Sets the concept.
	 * 
	 * @param concept the new concept
	 */
	public void setConcept(I_GetConceptData concept) {
		this.concept = concept;
	}

	/**
	 * Gets the config.
	 * 
	 * @return the config
	 */
	public I_ConfigAceFrame getConfig() {
		return config;
	}

	/**
	 * Sets the config.
	 * 
	 * @param config the new config
	 */
	public void setConfig(I_ConfigAceFrame config) {
		this.config = config;
	}

	/**
	 * Gets the source lang code.
	 * 
	 * @return the source lang code
	 */
	public String getSourceLangCode() {
		return sourceLangCode;
	}

	/**
	 * Sets the source lang code.
	 * 
	 * @param sourceLangCode the new source lang code
	 */
	public void setSourceLangCode(String sourceLangCode) {
		this.sourceLangCode = sourceLangCode;
	}

	/**
	 * Gets the target lang code.
	 * 
	 * @return the target lang code
	 */
	public String getTargetLangCode() {
		return targetLangCode;
	}

	/**
	 * Sets the target lang code.
	 * 
	 * @param targetLangCode the new target lang code
	 */
	public void setTargetLangCode(String targetLangCode) {
		this.targetLangCode = targetLangCode;
	}

	/**
	 * Populate tree.
	 */
	@SuppressWarnings("unchecked")
	private void populateTree() {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(new DescriptionTreeNode("Concept: " + concept.toString(), 
				DescriptionTreeNode.LABEL_TYPE, null));
		DefaultMutableTreeNode fsn = new DefaultMutableTreeNode(new DescriptionTreeNode("Fully Specified Name", 
				DescriptionTreeNode.LABEL_TYPE, null));
		top.add(fsn);
		DefaultMutableTreeNode preferred = new DefaultMutableTreeNode(new DescriptionTreeNode("Preferred Term", 
				DescriptionTreeNode.LABEL_TYPE, null));
		top.add(preferred);
		DefaultMutableTreeNode synonym = new DefaultMutableTreeNode(new DescriptionTreeNode("Synonym", 
				DescriptionTreeNode.LABEL_TYPE, null));
		top.add(synonym);
		try {
			descriptions = (List<I_DescriptionTuple>) concept.getDescriptionTuples(config.getAllowedStatus(), 
					config.getDescTypes(), config.getViewPositionSetReadOnly(), 
					config.getPrecedence(), config.getConflictResolutionStrategy());
		} catch (IOException e) {
			e.printStackTrace();
		}
		boolean bHasPref =false;
		boolean bHasFSN =false;
		bAddFSN.setEnabled(true);
		I_TermFactory tf = Terms.get();
		for (I_DescriptionTuple description : descriptions) {
			if (description.getLang().equals(sourceLangCode) || description.getLang().equals(targetLangCode)) {
				try {
					if (description.getTypeNid() == tf.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids())) {
						fsn.add(new DefaultMutableTreeNode(new DescriptionTreeNode(null, DescriptionTreeNode.DESCRIPTION_TYPE, description)));
						if (description.getLang().equals(sourceLangCode)) {
							int semtagLocation = description.getText().lastIndexOf("(");
							if (semtagLocation == -1) semtagLocation = description.getText().length();

							int endParenthesis=description.getText().lastIndexOf(")");

							if (semtagLocation > -1 && semtagLocation<endParenthesis )
								sourceSemTag=description.getText().substring( semtagLocation + 1,endParenthesis);

							updateSimilarityTable(description.getText().substring(0, semtagLocation));
							updateTransMemoryTable(description.getText().substring(0, semtagLocation));
							updateGlossaryEnforcement(description.getText().substring(0, semtagLocation));
						}
						else{
							bHasFSN=true;
						}
					} else if (description.getTypeNid() == tf.uuidToNative(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids())) {
						preferred.add(new DefaultMutableTreeNode(new DescriptionTreeNode(null, DescriptionTreeNode.DESCRIPTION_TYPE, description)));
						if (description.getLang().equals(targetLangCode)){
							bHasPref = true;
							targetPreferred=description.getText();
							targetPreferredICS=description.isInitialCaseSignificant();
						}
					} else if (description.getTypeNid() == tf.uuidToNative(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids())) {
						synonym.add(new DefaultMutableTreeNode(new DescriptionTreeNode(null, DescriptionTreeNode.DESCRIPTION_TYPE, description)));
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (TerminologyException e) {
					e.printStackTrace();
				}
			}
		}

		DefaultTreeModel treeModel = new DefaultTreeModel(top);
		tree1.setModel(treeModel);
		for (int i = 0; i < tree1.getRowCount(); i++) {
			tree1.expandRow(i);
		}
		tree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree1.revalidate();
		scrollPane1.setMaximumSize(new Dimension(250,250));
		scrollPane1.revalidate();

		bAddFSN.setEnabled(!bHasFSN && bHasPref);
		comboBox1.setVisible(false);
		textField1.setVisible(true);
		textField1.setEnabled(false);
	}

	/**
	 * Update properties panel.
	 */
	private void updatePropertiesPanel() {

		DefaultMutableTreeNode node = (DefaultMutableTreeNode)  tree1.getLastSelectedPathComponent();

		if (node == null) {
			descriptionInEditor = null;
			label4.setText("");
			textField1.setText("");
			checkBox1.setSelected(false);
			panel2.revalidate();
			button1.setEnabled(false);
			button2.setEnabled(false);
			button3.setEnabled(false);
			button5.setEnabled(false);
			button4.setEnabled(true);
			label4.setVisible(true);
			comboBox1.setVisible(false);
			textField1.setEnabled(false);
		} else {
			Object nodeInfo = node.getUserObject();
			DescriptionTreeNode descriptionNode = (DescriptionTreeNode)nodeInfo;
			try {
				if (descriptionNode.getType() == DescriptionTreeNode.DESCRIPTION_TYPE) {
					if (descriptionNode.getDescription().getLang().equals(targetLangCode)) {
						textField1.setEnabled(true);
						bDescIssue.setEnabled(false);
						boolean update = false;
						if (descriptionInEditor == null) {
							update = true;
						} else {
							if (descriptionInEditor.getText().trim().equals(textField1.getText().trim()) && (
									descriptionInEditor.isInitialCaseSignificant() == checkBox1.isSelected())) {
								update = true;
							} else {
								Object[] options = {"Discard unsaved data", "Cancel and continue editing"};
								int n = JOptionPane.showOptionDialog(null,
										"Do you want to save the change you made to the term in the editor panel?",
										"Unsaved data",
										JOptionPane.YES_NO_OPTION,
										JOptionPane.WARNING_MESSAGE,
										null,     //do not use a custom Icon
										options,  //the titles of buttons
										options[1]); //default button title
								if (n == 0) {
									update = true;
								} else {
									update = false;
								}
							}
						}
						if (update) {
							descriptionInEditor = descriptionNode.getDescription();
							label4.setText(Terms.get().getConcept(descriptionInEditor.getTypeNid()).toString());
							textField1.setText(descriptionInEditor.getText().trim());
							checkBox1.setSelected(descriptionInEditor.isInitialCaseSignificant());
							panel2.revalidate();
							button1.setEnabled(true);
							button2.setEnabled(true);
							button3.setEnabled(true);
							button4.setEnabled(false);
							button5.setEnabled(true);
							label4.setVisible(true);
							comboBox1.setVisible(false);
						}
					}
					else{
						bDescIssue.setEnabled(true);
					}
				} else {
					// nothing
					bDescIssue.setEnabled(false);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (TerminologyException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Update similarity table.
	 * 
	 * @param query the query
	 */
	private void updateSimilarityTable(String query) {
		List<SimilarityMatchedItem> results = LanguageUtil.getSimilarityResults(query, sourceLangCode, targetLangCode, config);
		String[] columnNames = {"Source Text",
		"Target Text"};
		String[][] data = null;
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};
		for (SimilarityMatchedItem item : results) {
			tableModel.addRow(new String[] {item.getSourceText(),item.getTargetText()});
		}
		table1.setModel(tableModel);
		TableColumnModel cmodel = table1.getColumnModel(); 
		TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
		cmodel.getColumn(0).setCellRenderer(textAreaRenderer); 
		cmodel.getColumn(1).setCellRenderer(textAreaRenderer); 
		table1.revalidate();
	}
	
	/**
	 * Update trans memory table.
	 * 
	 * @param query the query
	 */
	private void updateTransMemoryTable(String query) {

		HashMap<String,String> results = DocumentManager.matchTranslationMemory(query);
		String[] columnNames = {"Pattern Text",
		"Translated to.."};
		String[][] data = null;
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};

		for (String key : results.keySet()) {
			tableModel.addRow(new String[] {key,results.get(key)});
		}
		table2.setModel(tableModel);
		TableColumnModel cmodel = table2.getColumnModel(); 
		if (results.size()>0){
			tabbedPane1.setTitleAt(1, "<html>Translation Memory<b><font color='red'>*</font></b></html>");
		}
		TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
		cmodel.getColumn(0).setCellRenderer(textAreaRenderer); 
		cmodel.getColumn(1).setCellRenderer(textAreaRenderer); 
		table2.revalidate();
	}

	/**
	 * Update glossary enforcement.
	 * 
	 * @param query the query
	 */
	private void updateGlossaryEnforcement(String query) {

		String results = DocumentManager.getInfoForTerm(query, config);
		if (!results.equals("")){
			tabbedPane1.setTitleAt(2, "<html>Glossary<b><font color='red'>*</font></b></html>");
		}
		editorPane1.setText(results);
		editorPane1.revalidate();
	}
}
