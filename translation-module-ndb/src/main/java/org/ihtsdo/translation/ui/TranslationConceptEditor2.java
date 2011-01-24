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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.document.DocumentManager;
import org.ihtsdo.project.issue.manager.IssuesPanel;
import org.ihtsdo.project.issue.manager.IssuesView;
import org.ihtsdo.project.issuerepository.manager.ListObj;
import org.ihtsdo.project.panel.PanelHelperFactory;
import org.ihtsdo.project.panel.TranslationHelperPanel;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.SimilarityMatchedItem;

/**
 * The Class TranslationConceptEditor.
 */
public class TranslationConceptEditor2 extends JPanel {
	
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
	public TranslationConceptEditor2(I_GetConceptData concept, I_ConfigAceFrame config, String sourceLangCode, String targetLangCode) {
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
		mSpellChk.setEnabled(false);
		mAddDesc.setEnabled(true);
		mAddPref.setEnabled(true);
		button5.setEnabled(false);
		editorPane1.setEditable(false);
		bKeep.setEnabled(false);
		bReview.setEnabled(false);
		bEscalate.setEnabled(false);

		addListeners(table3);
		DefaultMutableTreeNode root=new DefaultMutableTreeNode();
		tree1.setModel(new DefaultTreeModel(root));
	//	populateTree();
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
						descriptionInEditor.getTypeNid(), targetLangCode, config, rbYes.isSelected(),
						ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			} else {
				I_GetConceptData typeConcept = (I_GetConceptData) comboBox1.getSelectedItem();
				LanguageUtil.persistEditedDescription(concept, null, textField1.getText(), 
						typeConcept.getConceptNid(), targetLangCode, config, rbYes.isSelected(),
						ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			}
			clearForm(false);
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
		clearForm(false);
	}
	
	/**
	 * Clear form.
	 */
	private void clearForm(boolean clearAll){
		descriptionInEditor = null;
		comboBox1.setVisible(false);
		label4.setVisible(true);
		label4.setText("");
		textField1.setText("");
		textField1.setEnabled(false);
		rbYes.setSelected(false);
		panel2.revalidate();
		button1.setEnabled(false);
		mSpellChk.setEnabled(false);
		button5.setEnabled(false);
		mAddDesc.setEnabled(true);
		mAddPref.setEnabled(true);
		if (clearAll){
			DefaultMutableTreeNode root=new DefaultMutableTreeNode();
			tree1.setModel(new DefaultTreeModel(root));
			table1.setModel(new DefaultTableModel());
			table2.setModel(new DefaultTableModel());
			editorPane1.setText("");
		}
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
		panel2.revalidate();
		button1.setEnabled(true);
		mSpellChk.setEnabled(true);
		button5.setEnabled(false);
		mAddDesc.setEnabled(true);
		mAddPref.setEnabled(true);
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
	 * Spellcheck action performed.
	 * 
	 * @param e the e
	 */
	private void mSpellChkActionPerformed() {

		textField1.setText(DocumentManager.spellcheckPhrase(textField1.getText(), null, targetLangCode));

	}

	private void mCloseActionPerformed() {
		AceFrameConfig config;
		try {
			config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();
			AceFrame ace=config.getAceFrame();
			JTabbedPane tp=ace.getCdePanel().getConceptTabs();
			if (tp!=null){
				int tabCount=tp.getTabCount();
				for (int i=0;i<tabCount;i++){
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.TRANSLATION_TAB_NAME)){
						tp.remove(i);
					}
					tp.repaint();
					tp.revalidate();

				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void mSwTSVActionPerformed() {
		LanguageUtil.openTranlationUI(concept, config, sourceLangCode, targetLangCode, LanguageUtil.SIMPLE_UI);
	}

	private void mAddPrefActionPerformed() {
		descriptionInEditor = null;
		label4.setText("");
		label4.setVisible(false);
		textField1.setText("");
		textField1.setEnabled(true);
		panel2.revalidate();
		button1.setEnabled(true);
		mSpellChk.setEnabled(true);
		button5.setEnabled(false);
		label4.setVisible(false);
		comboBox1.setVisible(true);
	}

	private void mAddDescActionPerformed() {
		descriptionInEditor = null;
		label4.setText("");
		label4.setVisible(false);
		textField1.setText("");
		textField1.setEnabled(true);
		panel2.revalidate();
		button1.setEnabled(true);
		mSpellChk.setEnabled(true);
		button5.setEnabled(false);
		label4.setVisible(false);
		comboBox1.setVisible(true);
	}

	private void mGetAssignActionPerformed() {
		I_IntSet allowedDestRelTypes =  Terms.get().newIntSet();
		try {
			allowedDestRelTypes.add(Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
		
			Set <I_GetConceptData>concepts=(Set<I_GetConceptData>)Terms.get().getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8")).getDestRelOrigins(
					config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(),
					config.getConflictResolutionStrategy());
		
		String[] columnNames = {"Concepts"};
		String[][] data = null;
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};
		for (I_GetConceptData item : concepts) {
			
			tableModel.addRow(new Object[] {new ListObj("C",item.getInitialText(),item)});
		}
		table3.setModel(tableModel);
		TableColumnModel cmodel = table3.getColumnModel(); 
		TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
		cmodel.getColumn(0).setCellRenderer(textAreaRenderer); 
		table3.revalidate();
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void bExecActionPerformed() {
		if (this.concept!=null){
			populateTree();
			bExec.setEnabled(false);
			table3.setEnabled(false);
			mGetAssign.setEnabled(false);
			mClose.setEnabled(false);
			bKeep.setEnabled(true);
			bReview.setEnabled(true);
			bEscalate.setEnabled(true);
		}
	}


	private void addListeners(JTable table){
		   SelectionListener listener = new SelectionListener(table);
		   table.getSelectionModel().addListSelectionListener(listener);
	}

	private void bKeepActionPerformed() {
		clearForm(true);

		bKeep.setEnabled(false);
		bReview.setEnabled(false);
		bEscalate.setEnabled(false);
		bExec.setEnabled(true);
		table3.setEnabled(true);
		mClose.setEnabled(true);
	}

	private void bReviewActionPerformed() {
		clearAndRemove();
	}
	
	private void clearAndRemove() {
		clearForm(true);

		bKeep.setEnabled(false);
		bReview.setEnabled(false);
		bEscalate.setEnabled(false);
		bExec.setEnabled(true);
		DefaultTableModel tModel=(DefaultTableModel) table3.getModel();
		for (int i=0;i<tModel.getRowCount();i++){
			I_GetConceptData conc=(I_GetConceptData)((ListObj)tModel.getValueAt(i, 0)).getAtrValue();
			if(conc.equals(this.concept)){
				tModel.removeRow(i);
				this.concept=null;
				break;
			}
		}
		table3.clearSelection();
		table3.setEnabled(true);
		table3.revalidate();
		mClose.setEnabled(true);
		
	}

	private void bEscalateActionPerformed() {
		clearAndRemove();
	}

	class SelectionListener implements ListSelectionListener {
        
        /** The table. */
        JTable table;
    
        // It is necessary to keep the table since it is not possible
        // to determine the table from the event's source
        /**
         * Instantiates a new selection listener.
         * 
         * @param table the table
         */
        SelectionListener(JTable table) {
            this.table = table;
        }
        
        /* (non-Javadoc)
         * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
         */
        public void valueChanged(ListSelectionEvent e) {
            // If cell selection is enabled, both row and column change events are fired
        	if (!(e.getSource() == table.getSelectionModel()
                  && table.getRowSelectionAllowed())) {
        		return;
            }
    
            if (e.getValueIsAdjusting()) {
                // The mouse button has not yet been released
            	return;
            }
            else{
        		int  first=table.getSelectedRow();
        		if (first>-1){
        			Object row=table.getModel().getValueAt(first, 0);
            		if (row!=null){
            			concept=(I_GetConceptData)((ListObj)row).getAtrValue();
            		}
            	}
            	
            }
        }

    }
	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		menuBar1 = new JMenuBar();
		menu5 = new JMenu();
		mGetAssign = new JMenuItem();
		mClose = new JMenuItem();
		menu1 = new JMenu();
		bAddFSN = new JMenuItem();
		mAddPref = new JMenuItem();
		mAddDesc = new JMenuItem();
		menu2 = new JMenu();
		bCptIssue = new JMenuItem();
		bDescIssue = new JMenuItem();
		bViewIssue = new JMenuItem();
		menu3 = new JMenu();
		mSpellChk = new JMenuItem();
		menu4 = new JMenu();
		mSwTSV = new JMenuItem();
		splitPane1 = new JSplitPane();
		panel11 = new JPanel();
		label9 = new JLabel();
		scrollPane6 = new JScrollPane();
		table3 = new JTable();
		panel8 = new JPanel();
		bExec = new JButton();
		panel10 = new JPanel();
		splitPane2 = new JSplitPane();
		panel9 = new JPanel();
		scrollPane1 = new JScrollPane();
		tree1 = new JTree();
		tabbedPane1 = new JTabbedPane();
		scrollPane2 = new JScrollPane();
		table1 = new JTable();
		scrollPane3 = new JScrollPane();
		table2 = new JTable();
		scrollPane4 = new JScrollPane();
		editorPane1 = new JEditorPane();
		panel2 = new JPanel();
		label1 = new JLabel();
		panel7 = new JPanel();
		label4 = new JLabel();
		comboBox1 = new JComboBox();
		label2 = new JLabel();
		textField1 = new JTextField();
		label3 = new JLabel();
		panel4 = new JPanel();
		label5 = new JLabel();
		rbYes = new JRadioButton();
		label6 = new JLabel();
		label7 = new JLabel();
		rbNo = new JRadioButton();
		panel1 = new JPanel();
		panel3 = new JPanel();
		button1 = new JButton();
		button5 = new JButton();
		panel6 = new JPanel();
		panel5 = new JPanel();
		label8 = new JLabel();
		bKeep = new JButton();
		bReview = new JButton();
		bEscalate = new JButton();

		//======== this ========
		setBackground(new Color(255, 153, 153));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 446, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {35, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

		//======== menuBar1 ========
		{

			//======== menu5 ========
			{
				menu5.setText("Translation");
				menu5.setFont(new Font("Verdana", Font.PLAIN, 14));

				//---- mGetAssign ----
				mGetAssign.setText("Get Assignments");
				mGetAssign.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mGetAssignActionPerformed();
						mGetAssignActionPerformed();
					}
				});
				menu5.add(mGetAssign);

				//---- mClose ----
				mClose.setText("Close");
				mClose.setMnemonic('C');
				mClose.setFont(new Font("Verdana", Font.PLAIN, 14));
				mClose.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mCloseActionPerformed();
					}
				});
				menu5.add(mClose);
			}
			menuBar1.add(menu5);

			//======== menu1 ========
			{
				menu1.setText("Edit");
				menu1.setFont(new Font("Verdana", Font.PLAIN, 14));

				//---- bAddFSN ----
				bAddFSN.setText("Add Concept FSN");
				bAddFSN.setFont(new Font("Verdana", Font.PLAIN, 14));
				bAddFSN.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						bAddFSNActionPerformed();
					}
				});
				menu1.add(bAddFSN);

				//---- mAddPref ----
				mAddPref.setText("Add Concept Preferred");
				mAddPref.setFont(new Font("Verdana", Font.PLAIN, 14));
				mAddPref.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mAddPrefActionPerformed();
					}
				});
				menu1.add(mAddPref);

				//---- mAddDesc ----
				mAddDesc.setText("Add Concept Description");
				mAddDesc.setFont(new Font("Verdana", Font.PLAIN, 14));
				mAddDesc.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mAddDescActionPerformed();
					}
				});
				menu1.add(mAddDesc);
			}
			menuBar1.add(menu1);

			//======== menu2 ========
			{
				menu2.setText("Issues");
				menu2.setFont(new Font("Verdana", Font.PLAIN, 14));

				//---- bCptIssue ----
				bCptIssue.setText("Add Concept Issue");
				bCptIssue.setFont(new Font("Verdana", Font.PLAIN, 14));
				bCptIssue.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						bCptIssueActionPerformed();
					}
				});
				menu2.add(bCptIssue);

				//---- bDescIssue ----
				bDescIssue.setText("Add Description Issue");
				bDescIssue.setFont(new Font("Verdana", Font.PLAIN, 14));
				bDescIssue.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						bDescIssueActionPerformed();
					}
				});
				menu2.add(bDescIssue);
				menu2.addSeparator();

				//---- bViewIssue ----
				bViewIssue.setText("View Issues");
				bViewIssue.setFont(new Font("Verdana", Font.PLAIN, 14));
				bViewIssue.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						bViewIssueActionPerformed();
					}
				});
				menu2.add(bViewIssue);
			}
			menuBar1.add(menu2);

			//======== menu3 ========
			{
				menu3.setText("Tools");
				menu3.setSelectedIcon(null);
				menu3.setFont(new Font("Verdana", Font.PLAIN, 14));

				//---- mSpellChk ----
				mSpellChk.setText("Spellcheck");
				mSpellChk.setFont(new Font("Verdana", Font.PLAIN, 14));
				mSpellChk.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mSpellChkActionPerformed();
					}
				});
				menu3.add(mSpellChk);
			}
			menuBar1.add(menu3);

			//======== menu4 ========
			{
				menu4.setText("View");
				menu4.setFont(new Font("Verdana", Font.PLAIN, 14));

				//---- mSwTSV ----
				mSwTSV.setText("Switch to simple view");
				mSwTSV.setFont(new Font("Verdana", Font.PLAIN, 14));
				mSwTSV.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mSwTSVActionPerformed();
					}
				});
				menu4.add(mSwTSV);
			}
			menuBar1.add(menu4);
		}
		add(menuBar1, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== splitPane1 ========
		{
			splitPane1.setToolTipText("Drag to resize");
			splitPane1.setBackground(new Color(255, 153, 153));

			//======== panel11 ========
			{
				panel11.setLayout(new GridBagLayout());
				((GridBagLayout)panel11.getLayout()).columnWidths = new int[] {0, 0};
				((GridBagLayout)panel11.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel11.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
				((GridBagLayout)panel11.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

				//---- label9 ----
				label9.setText("Assignments");
				panel11.add(label9, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== scrollPane6 ========
				{
					scrollPane6.setViewportView(table3);
				}
				panel11.add(scrollPane6, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel8 ========
				{
					panel8.setBackground(new Color(255, 153, 153));
					panel8.setLayout(new GridBagLayout());
					((GridBagLayout)panel8.getLayout()).columnWidths = new int[] {0, 0, 0};
					((GridBagLayout)panel8.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel8.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
					((GridBagLayout)panel8.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- bExec ----
					bExec.setText("Execute Assignment");
					bExec.setIcon(new ImageIcon("icons/Work.gif"));
					bExec.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							bExecActionPerformed();
						}
					});
					panel8.add(bExec, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel11.add(panel8, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			splitPane1.setLeftComponent(panel11);

			//======== panel10 ========
			{
				panel10.setLayout(new GridBagLayout());
				((GridBagLayout)panel10.getLayout()).columnWidths = new int[] {0, 0, 0};
				((GridBagLayout)panel10.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
				((GridBagLayout)panel10.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
				((GridBagLayout)panel10.getLayout()).rowWeights = new double[] {1.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

				//======== splitPane2 ========
				{
					splitPane2.setToolTipText("Drag to resize");

					//======== panel9 ========
					{
						panel9.setLayout(new GridBagLayout());
						((GridBagLayout)panel9.getLayout()).columnWidths = new int[] {0, 0};
						((GridBagLayout)panel9.getLayout()).rowHeights = new int[] {0, 0};
						((GridBagLayout)panel9.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
						((GridBagLayout)panel9.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

						//======== scrollPane1 ========
						{

							//---- tree1 ----
							tree1.setBackground(new Color(239, 235, 222));
							tree1.addTreeSelectionListener(new TreeSelectionListener() {
								@Override
								public void valueChanged(TreeSelectionEvent e) {
									tree1ValueChanged(e);
								}
							});
							scrollPane1.setViewportView(tree1);
						}
						panel9.add(scrollPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					splitPane2.setLeftComponent(panel9);

					//======== tabbedPane1 ========
					{
						tabbedPane1.setBorder(LineBorder.createBlackLineBorder());
						tabbedPane1.setFont(new Font("Verdana", Font.PLAIN, 13));

						//======== scrollPane2 ========
						{

							//---- table1 ----
							table1.setPreferredScrollableViewportSize(new Dimension(180, 200));
							table1.setBackground(new Color(239, 235, 222));
							table1.setFont(new Font("Verdana", Font.PLAIN, 12));
							scrollPane2.setViewportView(table1);
						}
						tabbedPane1.addTab("Similarity", scrollPane2);


						//======== scrollPane3 ========
						{

							//---- table2 ----
							table2.setPreferredScrollableViewportSize(new Dimension(180, 200));
							table2.setFont(new Font("Verdana", Font.PLAIN, 12));
							scrollPane3.setViewportView(table2);
						}
						tabbedPane1.addTab("Translation Memory", scrollPane3);


						//======== scrollPane4 ========
						{

							//---- editorPane1 ----
							editorPane1.setContentType("text/html");
							editorPane1.setFont(new Font("Verdana", Font.PLAIN, 13));
							scrollPane4.setViewportView(editorPane1);
						}
						tabbedPane1.addTab("Glossary", scrollPane4);

					}
					splitPane2.setRightComponent(tabbedPane1);
				}
				panel10.add(splitPane2, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel2 ========
				{
					panel2.setLayout(new GridBagLayout());
					((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 321, 0};
					((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
					((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

					//---- label1 ----
					label1.setText("Term type:");
					label1.setFont(new Font("Verdana", Font.PLAIN, 13));
					panel2.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(0, 10, 5, 5), 0, 0));

					//======== panel7 ========
					{
						panel7.setLayout(new FlowLayout(FlowLayout.LEFT));
						panel7.add(label4);
						panel7.add(comboBox1);
					}
					panel2.add(panel7, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label2 ----
					label2.setText("Term:");
					label2.setFont(new Font("Verdana", Font.PLAIN, 13));
					panel2.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(0, 10, 5, 5), 0, 0));

					//---- textField1 ----
					textField1.setEnabled(false);
					panel2.add(textField1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label3 ----
					label3.setText("Is case significant ?");
					label3.setFont(new Font("Verdana", Font.PLAIN, 13));
					panel2.add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(0, 10, 0, 5), 0, 0));

					//======== panel4 ========
					{
						panel4.setLayout(new GridBagLayout());
						((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0};
						((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0};
						((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
						((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

						//---- label5 ----
						label5.setText("Yes");
						panel4.add(label5, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));
						panel4.add(rbYes, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- label6 ----
						label6.setText("    ");
						panel4.add(label6, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- label7 ----
						label7.setText("No");
						panel4.add(label7, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- rbNo ----
						rbNo.setSelected(true);
						panel4.add(rbNo, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					panel2.add(panel4, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel10.add(panel2, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel1 ========
				{
					panel1.setBackground(new Color(255, 153, 153));
					panel1.setLayout(new GridBagLayout());
					((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0};
					((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
					((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
				}
				panel10.add(panel1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel3 ========
				{
					panel3.setBackground(new Color(255, 153, 153));
					panel3.setLayout(new GridBagLayout());
					((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- button1 ----
					button1.setText("Save");
					button1.setFont(new Font("Verdana", Font.PLAIN, 13));
					button1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							saveActionPerformed(e);
						}
					});
					panel3.add(button1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- button5 ----
					button5.setText("Retire");
					button5.setFont(new Font("Verdana", Font.PLAIN, 13));
					button5.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							retireActionPerformed(e);
						}
					});
					panel3.add(button5, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
				}
				panel10.add(panel3, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel6 ========
				{
					panel6.setLayout(new BoxLayout(panel6, BoxLayout.Y_AXIS));

					//======== panel5 ========
					{
						panel5.setBackground(new Color(255, 153, 153));
						panel5.setLayout(new GridBagLayout());
						((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
						((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0};
						((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
						((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

						//---- label8 ----
						label8.setText("Workflow actions:      ");
						panel5.add(label8, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- bKeep ----
						bKeep.setText("Keep in inbox");
						bKeep.setIcon(new ImageIcon("icons/cabinet.gif"));
						bKeep.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								bKeepActionPerformed();
							}
						});
						panel5.add(bKeep, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- bReview ----
						bReview.setText("Send to reviewer");
						bReview.setIcon(new ImageIcon("icons/reviewer.gif"));
						bReview.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								bReviewActionPerformed();
							}
						});
						panel5.add(bReview, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- bEscalate ----
						bEscalate.setText("Escalate");
						bEscalate.setIcon(new ImageIcon("icons/editor.gif"));
						bEscalate.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								bEscalateActionPerformed();
							}
						});
						panel5.add(bEscalate, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					panel6.add(panel5);
				}
				panel10.add(panel6, new GridBagConstraints(0, 3, 2, 2, 1.0, 1.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			splitPane1.setRightComponent(panel10);
		}
		add(splitPane1, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(rbYes);
		buttonGroup1.add(rbNo);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JMenuBar menuBar1;
	private JMenu menu5;
	private JMenuItem mGetAssign;
	private JMenuItem mClose;
	private JMenu menu1;
	private JMenuItem bAddFSN;
	private JMenuItem mAddPref;
	private JMenuItem mAddDesc;
	private JMenu menu2;
	private JMenuItem bCptIssue;
	private JMenuItem bDescIssue;
	private JMenuItem bViewIssue;
	private JMenu menu3;
	private JMenuItem mSpellChk;
	private JMenu menu4;
	private JMenuItem mSwTSV;
	private JSplitPane splitPane1;
	private JPanel panel11;
	private JLabel label9;
	private JScrollPane scrollPane6;
	private JTable table3;
	private JPanel panel8;
	private JButton bExec;
	private JPanel panel10;
	private JSplitPane splitPane2;
	private JPanel panel9;
	private JScrollPane scrollPane1;
	private JTree tree1;
	private JTabbedPane tabbedPane1;
	private JScrollPane scrollPane2;
	private JTable table1;
	private JScrollPane scrollPane3;
	private JTable table2;
	private JScrollPane scrollPane4;
	private JEditorPane editorPane1;
	private JPanel panel2;
	private JLabel label1;
	private JPanel panel7;
	private JLabel label4;
	private JComboBox comboBox1;
	private JLabel label2;
	private JTextField textField1;
	private JLabel label3;
	private JPanel panel4;
	private JLabel label5;
	private JRadioButton rbYes;
	private JLabel label6;
	private JLabel label7;
	private JRadioButton rbNo;
	private JPanel panel1;
	private JPanel panel3;
	private JButton button1;
	private JButton button5;
	private JPanel panel6;
	private JPanel panel5;
	private JLabel label8;
	private JButton bKeep;
	private JButton bReview;
	private JButton bEscalate;
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
//			ContextualizedDescription;
//			LanguageUtil.getContextualizedDescriptions(concept.getConceptId(), languageRefsetId, config.getAllowedStatus(), config.getDescTypes(), config.getViewPositionSetReadOnly(), true)
			
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
//			label4.setText("");
//			textField1.setText("");
//			checkBox1.setSelected(false);
//			panel2.revalidate();
//			button1.setEnabled(false);
			mSpellChk.setEnabled(false);
//			button5.setEnabled(false);
//			button4.setEnabled(true);
//			label4.setVisible(true);
//			comboBox1.setVisible(false);
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
									descriptionInEditor.isInitialCaseSignificant() == rbYes.isSelected())) {
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
//							descriptionInEditor = descriptionNode.getDescription();
							label4.setText(Terms.get().getConcept(descriptionInEditor.getTypeNid()).toString());
//							textField1.setText(descriptionInEditor.getText().trim());
							rbYes.setSelected(descriptionInEditor.isInitialCaseSignificant());
//							panel2.revalidate();
//							button1.setEnabled(true);
							mSpellChk.setEnabled(true);
//							button4.setEnabled(false);
//							button5.setEnabled(true);
//							label4.setVisible(true);
//							comboBox1.setVisible(false);
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
