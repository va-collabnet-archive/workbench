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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.document.DocumentManager;
import org.ihtsdo.issue.IssueRepoRegistration;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.I_ContextualizeDescription;
import org.ihtsdo.project.issue.manager.IssuesListPanel2;
import org.ihtsdo.project.issue.manager.IssuesPanel;
import org.ihtsdo.project.issue.manager.IssuesView;
import org.ihtsdo.project.issue.manager.TextAreaRenderer;
import org.ihtsdo.project.issuerepository.manager.ListObj;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.panel.PanelHelperFactory;
import org.ihtsdo.project.panel.TranslationHelperPanel;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.SimilarityMatchedItem;
import org.ihtsdo.translation.TreeEditorObjectWrapper;

/**
 * The Class TranslationConceptEditor.
 */
public class TranslationConceptViewer extends JPanel {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	private TranslationProject translationProject;
	private I_GetConceptData synonym;
	private I_GetConceptData fsn;
	private I_GetConceptData preferred;
	private List<Integer> sourceIds;
	private int targetId;
	private I_GetConceptData notAcceptable;
	private I_GetConceptData acceptable;
	private I_GetConceptData current;
	private Set<LanguageMembershipRefset> sourceLangRefsets;
	private LanguageMembershipRefset targetLangRefset;
	private SimpleDateFormat formatter;
	private I_GetConceptData description;
	private I_GetConceptData inactive;
	private I_GetConceptData active;
	private I_GetConceptData retired;
	private IssuesListPanel2 issueListPanel;
	private ConfigTranslationModule translConfig;

	public TranslationConceptViewer() {
		sourceIds=new ArrayList<Integer>();
//		if (sourceLangRefsets!=null)
//			for (LanguageMembershipRefset sourceRef:sourceLangRefsets){
//				sourceIds.add(sourceRef.getRefsetId());
//			}
//		if (targetLangRefset!=null)
//			targetId=targetLangRefset.getRefsetId();
		I_ConfigAceFrame config=null;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			translConfig=LanguageUtil.getTranslationConfig(config);
			synonym = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids());
			fsn = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
			preferred =  Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
			
			//TODO review!! previously was DESCRIPTION_DESCRIPTION_TYPE
			description =  Terms.get().getConcept(ArchitectonicAuxiliary.Concept.DESCRIPTION_TYPE.getUids());
			
			notAcceptable =  Terms.get().getConcept(ArchitectonicAuxiliary.Concept.NOT_ACCEPTABLE.getUids());
			inactive =Terms.get().getConcept(ArchitectonicAuxiliary.Concept.INACTIVE.getUids());
			retired =Terms.get().getConcept(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
			acceptable =  Terms.get().getConcept(ArchitectonicAuxiliary.Concept.ACCEPTABLE.getUids());
			current =  Terms.get().getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
			active=Terms.get().getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
			definingChar=ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.localize().getNid();
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}

		initComponents();
		formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

//		bDescIssue.setEnabled(false);
		DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
		comboBoxModel.addElement(fsn);
		comboBoxModel.addElement(description);

		DefaultComboBoxModel comboBoxModel2 = new DefaultComboBoxModel();
		comboBoxModel2.addElement(preferred);
		comboBoxModel2.addElement(acceptable);
		comboBoxModel2.addElement(notAcceptable);

//		tree1.setCellRenderer(new HtmlSafeTreeCellRenderer());

		tree1.setCellRenderer(new IconRenderer());
		tree1.setRootVisible(true);
		tree1.setShowsRootHandles(false);

		tree2.setCellRenderer(new IconRenderer());
		tree2.setRootVisible(true);
		tree2.setShowsRootHandles(false);

		tree3.setCellRenderer(new IconRenderer());
		tree3.setRootVisible(true);
		tree3.setShowsRootHandles(false);
		
		setByCode=false;

		DefaultMutableTreeNode sourceRoot=new DefaultMutableTreeNode();
		tree1.setModel(new DefaultTreeModel(sourceRoot));

		DefaultMutableTreeNode targetRoot=new DefaultMutableTreeNode();
		tree2.setModel(new DefaultTreeModel(targetRoot));

		DefaultMutableTreeNode detailsRoot=new DefaultMutableTreeNode();
		tree3.setModel(new DefaultTreeModel(detailsRoot));
		createIssuePanel();

	//	populateTree();
	}

	/**
	 * Tree1 value changed.
	 * 
	 * @param e the e
	 */
	private void tree1ValueChanged(TreeSelectionEvent e) {
	}

	/**
	 * Cancel action performed.
	 * 
	 * @param e the e
	 */
	private void cancelActionPerformed(ActionEvent e) {
		clearForm(false);
	}
	
	public void unloadData(){
		verifySavePending();
		clearForm(true);
	}
	private void verifySavePending() {
	}

	/**
	 * Clear form.
	 */
	private void clearForm(boolean clearAll){
		descriptionInEditor = null;
		if (clearAll){
			DefaultMutableTreeNode root=new DefaultMutableTreeNode();
			tree1.setModel(new DefaultTreeModel(root));
			DefaultMutableTreeNode root2=new DefaultMutableTreeNode();
			tree2.setModel(new DefaultTreeModel(root2));
			DefaultMutableTreeNode root3=new DefaultMutableTreeNode();
			tree3.setModel(new DefaultTreeModel(root3));
			table1.setModel(new DefaultTableModel());
			table2.setModel(new DefaultTableModel());
			this.translationProject=null;
			this.concept=null;
			if (issueListPanel!=null){
				issueListPanel.loadIssues(null,null,null);
			}
			clearComments();
			clearLingGuidelines();
			clearTransMemory();
			clearSimilarities();
		}
	}

	/**
	 * Retire action performed.
	 * 
	 * @param e the e
	 */
	private void retireActionPerformed(ActionEvent e) {
		clearForm(false);
//		try {
//			descriptionInEditor.setAcceptabilityId(notAcceptable.getConceptId());
//			descriptionInEditor.persistChanges();
//			clearForm(false);
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		} catch (TerminologyException e1) {
//			e1.printStackTrace();
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
//		try {
//			populateTargetTree();
//			getPreviousComments();
//			SwingUtilities.invokeLater(new Runnable() {
//				public void run() {
//		            Timer timer = new Timer(1100, new setInboxPanelFocus());
//		            timer.setRepeats(false);
//		            timer.start();
//				}
//
//			});
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}	
	}


	/**
	 * Adds the new action performed.
	 * 
	 * @param e the e
	 */
	private void addNewActionPerformed(ActionEvent e) {
		descriptionInEditor = null;
	}

	/**
	 * B add fsn action performed.
	 */
	private void bAddFSNActionPerformed() {
		try {

			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			descriptionInEditor=(ContextualizedDescription) ContextualizedDescription.createNewContextualizedDescription(concept.getConceptNid(), targetId, targetLangRefset.getLangCode(config));
			descriptionInEditor.setText(targetPreferred.trim() + 
					" (" + LanguageUtil.getTargetEquivalentSemTag(sourceSemTag, sourceLangRefsets.iterator().next().getLangCode(config) , targetLangRefset.getLangCode(config)) + ")");
			descriptionInEditor.setInitialCaseSignificant(targetPreferredICS);
			descriptionInEditor.setAcceptabilityId(preferred.getConceptNid());
			descriptionInEditor.setTypeId(fsn.getConceptNid());
			descriptionInEditor.setDescriptionStatusId(current.getConceptNid());
			descriptionInEditor.setExtensionStatusId(active.getConceptNid());
			descriptionInEditor.persistChanges();
			clearForm(false);
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			populateTargetTree();
			getPreviousComments();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)  tree1.getLastSelectedPathComponent();

		if (node != null) {
			Object nodeInfo = node.getUserObject();
			ContextualizedDescription descriptionNode = (ContextualizedDescription) ((TreeEditorObjectWrapper)nodeInfo).getUserObject();
			if (descriptionNode.getLanguageRefsetId()==targetId){
				if (descriptionNode.getTypeId()==fsn.getConceptNid() ||
						descriptionNode.getTypeId()==preferred.getConceptNid()  ||
						descriptionNode.getTypeId()==synonym.getConceptNid() ) {

					launchIssuePanel(descriptionNode.getUuids().iterator().next().toString(),
							descriptionNode.getText());
				}
			}
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
		try {
			AceFrameConfig config = (AceFrameConfig)Terms.get().getActiveAceFrameConfig();
			AceFrame ace=config.getAceFrame();
			JTabbedPane tp=ace.getCdePanel().getConceptTabs();
			if (tp!=null){
				int tabCount=tp.getTabCount();
				for (int i=0;i<tabCount;i++){
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.ISSUE_VIEW_TAB_NAME)){
						tp.removeTabAt(i);	
					}
				}

				I_ConfigAceFrame iconfig = Terms.get().getActiveAceFrameConfig();
				IssuesView issView=new IssuesView(concept,sourceLangRefsets.iterator().next().getLangCode(iconfig));
				tp.addTab(TranslationHelperPanel.ISSUE_VIEW_TAB_NAME, issView);
				tp.setSelectedIndex(tp.getTabCount()-1);
				tp.revalidate();
				tp.repaint();
			}

			return;
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	private void mSwTSVActionPerformed() {
//		LanguageUtil.openTranlationUI(concept, config, sourceLangCode, targetLangCode, LanguageUtil.SIMPLE_UI);
	}



	private void addListeners(JTable table){
		   SelectionListener listener = new SelectionListener(table);
		   table.getSelectionModel().addListSelectionListener(listener);
	}



	private void rbFSNActionPerformed(ActionEvent e) {
		updateSimilarityTable(sourceFSN);
	}

	private void rbPrefActionPerformed(ActionEvent e) {
		updateSimilarityTable(sourceFSN);
	}

	private void radioButton2ActionPerformed(ActionEvent e) {
		updateSimilarityTable(sourceFSN);
	}

	private void thisAncestorRemoved() {
		verifySavePending();
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
		panel10 = new JPanel();
		splitPane3 = new JSplitPane();
		splitPane2 = new JSplitPane();
		panel9 = new JPanel();
		label9 = new JLabel();
		scrollPane1 = new JScrollPane();
		tree1 = new JTree();
		label10 = new JLabel();
		scrollPane7 = new JScrollPane();
		tree3 = new JTree();
		panel8 = new JPanel();
		label11 = new JLabel();
		scrollPane6 = new JScrollPane();
		tree2 = new JTree();
		splitPane1 = new JSplitPane();
		tabbedPane1 = new JTabbedPane();
		panel12 = new JPanel();
		scrollPane2 = new JScrollPane();
		table1 = new ZebraJTable();
		panel13 = new JPanel();
		rbFSN = new JRadioButton();
		rbPref = new JRadioButton();
		radioButton2 = new JRadioButton();
		scrollPane3 = new JScrollPane();
		table2 = new JTable();
		panel15 = new JPanel();
		scrollPane4 = new JScrollPane();
		editorPane1 = new JEditorPane();
		tabbedPane2 = new JTabbedPane();
		scrollPane9 = new JScrollPane();
		tblComm = new JTable();
		panel11 = new JPanel();

		//======== this ========
		setBackground(new Color(220, 233, 249));
		addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorMoved(AncestorEvent e) {}
			@Override
			public void ancestorAdded(AncestorEvent e) {}
			@Override
			public void ancestorRemoved(AncestorEvent e) {
				thisAncestorRemoved();
			}
		});
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

		//======== panel10 ========
		{
			panel10.setLayout(new GridBagLayout());
			((GridBagLayout)panel10.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)panel10.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel10.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
			((GridBagLayout)panel10.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

			//======== splitPane3 ========
			{
				splitPane3.setOrientation(JSplitPane.VERTICAL_SPLIT);
				splitPane3.setOneTouchExpandable(true);
				splitPane3.setResizeWeight(1.0);

				//======== splitPane2 ========
				{
					splitPane2.setToolTipText("Drag to resize");
					splitPane2.setBackground(new Color(220, 233, 249));
					splitPane2.setResizeWeight(1.0);

					//======== panel9 ========
					{
						panel9.setBackground(new Color(220, 233, 249));
						panel9.setLayout(new GridBagLayout());
						((GridBagLayout)panel9.getLayout()).columnWidths = new int[] {0, 0};
						((GridBagLayout)panel9.getLayout()).rowHeights = new int[] {20, 0, 0, 0, 0};
						((GridBagLayout)panel9.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
						((GridBagLayout)panel9.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0, 1.0E-4};

						//---- label9 ----
						label9.setText("Source Language");
						panel9.add(label9, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//======== scrollPane1 ========
						{

							//---- tree1 ----
							tree1.setVisibleRowCount(8);
							tree1.addTreeSelectionListener(new TreeSelectionListener() {
								@Override
								public void valueChanged(TreeSelectionEvent e) {
									tree1ValueChanged(e);
								}
							});
							scrollPane1.setViewportView(tree1);
						}
						panel9.add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//---- label10 ----
						label10.setText("Concept Details");
						panel9.add(label10, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//======== scrollPane7 ========
						{

							//---- tree3 ----
							tree3.setVisibleRowCount(8);
							scrollPane7.setViewportView(tree3);
						}
						panel9.add(scrollPane7, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					splitPane2.setLeftComponent(panel9);

					//======== panel8 ========
					{
						panel8.setBackground(new Color(220, 233, 249));
						panel8.setLayout(new GridBagLayout());
						((GridBagLayout)panel8.getLayout()).columnWidths = new int[] {0, 0};
						((GridBagLayout)panel8.getLayout()).rowHeights = new int[] {0, 150, 0};
						((GridBagLayout)panel8.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
						((GridBagLayout)panel8.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

						//---- label11 ----
						label11.setText("Target Language");
						panel8.add(label11, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//======== scrollPane6 ========
						{

							//---- tree2 ----
							tree2.setVisibleRowCount(12);
							scrollPane6.setViewportView(tree2);
						}
						panel8.add(scrollPane6, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					splitPane2.setRightComponent(panel8);
				}
				splitPane3.setTopComponent(splitPane2);

				//======== splitPane1 ========
				{
					splitPane1.setBackground(new Color(220, 233, 249));
					splitPane1.setResizeWeight(1.0);
					splitPane1.setToolTipText("Drag to resize");

					//======== tabbedPane1 ========
					{
						tabbedPane1.setFont(new Font("Verdana", Font.PLAIN, 13));

						//======== panel12 ========
						{
							panel12.setBackground(new Color(220, 233, 249));
							panel12.setLayout(new GridBagLayout());
							((GridBagLayout)panel12.getLayout()).columnWidths = new int[] {0, 0};
							((GridBagLayout)panel12.getLayout()).rowHeights = new int[] {0, 0, 0};
							((GridBagLayout)panel12.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
							((GridBagLayout)panel12.getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0E-4};

							//======== scrollPane2 ========
							{

								//---- table1 ----
								table1.setPreferredScrollableViewportSize(new Dimension(180, 200));
								table1.setBackground(new Color(239, 235, 222));
								table1.setFont(new Font("Verdana", Font.PLAIN, 12));
								scrollPane2.setViewportView(table1);
							}
							panel12.add(scrollPane2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 0), 0, 0));

							//======== panel13 ========
							{
								panel13.setBackground(new Color(220, 233, 249));
								panel13.setLayout(new GridBagLayout());
								((GridBagLayout)panel13.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
								((GridBagLayout)panel13.getLayout()).rowHeights = new int[] {0, 0};
								((GridBagLayout)panel13.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
								((GridBagLayout)panel13.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

								//---- rbFSN ----
								rbFSN.setText("FSN");
								rbFSN.setSelected(true);
								rbFSN.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										rbFSNActionPerformed(e);
									}
								});
								panel13.add(rbFSN, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 0, 5), 0, 0));

								//---- rbPref ----
								rbPref.setText("Preferred");
								rbPref.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										rbPrefActionPerformed(e);
									}
								});
								panel13.add(rbPref, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 0, 5), 0, 0));

								//---- radioButton2 ----
								radioButton2.setText("Both");
								radioButton2.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										radioButton2ActionPerformed(e);
									}
								});
								panel13.add(radioButton2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 0, 5), 0, 0));
							}
							panel12.add(panel13, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 0, 0), 0, 0));
						}
						tabbedPane1.addTab("Similarity", panel12);


						//======== scrollPane3 ========
						{

							//---- table2 ----
							table2.setPreferredScrollableViewportSize(new Dimension(180, 200));
							table2.setFont(new Font("Verdana", Font.PLAIN, 12));
							scrollPane3.setViewportView(table2);
						}
						tabbedPane1.addTab("Translation Memory", scrollPane3);


						//======== panel15 ========
						{
							panel15.setLayout(new GridBagLayout());
							((GridBagLayout)panel15.getLayout()).columnWidths = new int[] {0, 0};
							((GridBagLayout)panel15.getLayout()).rowHeights = new int[] {0, 0};
							((GridBagLayout)panel15.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
							((GridBagLayout)panel15.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

							//======== scrollPane4 ========
							{

								//---- editorPane1 ----
								editorPane1.setContentType("text/html");
								scrollPane4.setViewportView(editorPane1);
							}
							panel15.add(scrollPane4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 0, 0), 0, 0));
						}
						tabbedPane1.addTab("Linguistic Guidelines", panel15);

					}
					splitPane1.setLeftComponent(tabbedPane1);

					//======== tabbedPane2 ========
					{

						//======== scrollPane9 ========
						{
							scrollPane9.setViewportView(tblComm);
						}
						tabbedPane2.addTab("Previous Comments", scrollPane9);


						//======== panel11 ========
						{
							panel11.setBackground(Color.white);
							panel11.setLayout(new GridBagLayout());
							((GridBagLayout)panel11.getLayout()).columnWidths = new int[] {0, 0};
							((GridBagLayout)panel11.getLayout()).rowHeights = new int[] {0, 0};
							((GridBagLayout)panel11.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
							((GridBagLayout)panel11.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};
						}
						tabbedPane2.addTab("Issues", panel11);

					}
					splitPane1.setRightComponent(tabbedPane2);
				}
				splitPane3.setBottomComponent(splitPane1);
			}
			panel10.add(splitPane3, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel10, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));

		//---- buttonGroup2 ----
		ButtonGroup buttonGroup2 = new ButtonGroup();
		buttonGroup2.add(rbFSN);
		buttonGroup2.add(rbPref);
		buttonGroup2.add(radioButton2);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel10;
	private JSplitPane splitPane3;
	private JSplitPane splitPane2;
	private JPanel panel9;
	private JLabel label9;
	private JScrollPane scrollPane1;
	private JTree tree1;
	private JLabel label10;
	private JScrollPane scrollPane7;
	private JTree tree3;
	private JPanel panel8;
	private JLabel label11;
	private JScrollPane scrollPane6;
	private JTree tree2;
	private JSplitPane splitPane1;
	private JTabbedPane tabbedPane1;
	private JPanel panel12;
	private JScrollPane scrollPane2;
	private ZebraJTable table1;
	private JPanel panel13;
	private JRadioButton rbFSN;
	private JRadioButton rbPref;
	private JRadioButton radioButton2;
	private JScrollPane scrollPane3;
	private JTable table2;
	private JPanel panel15;
	private JScrollPane scrollPane4;
	private JEditorPane editorPane1;
	private JTabbedPane tabbedPane2;
	private JScrollPane scrollPane9;
	private JTable tblComm;
	private JPanel panel11;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	
	/** The concept. */
	private I_GetConceptData concept;
	
//	
//	/** The source lang code. */
//	private String sourceLangCode;
//	
//	/** The target lang code. */
//	private String targetLangCode;
//	
	/** The description in editor. */
	private ContextualizedDescription descriptionInEditor;
	
	/** The target preferred. */
	private String targetPreferred;
	
	/** The target preferred ics. */
	private boolean targetPreferredICS;
	
	/** The source sem tag. */
	private String sourceSemTag;
	private int definingChar=-1;
	private String sourceFSN;
	private WorkListMember worklistMember;
	private Object editingPath;
	private boolean setByCode;

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
	 * Populate tree.
	 */
	@SuppressWarnings("unchecked")
	private void populateSourceTree() throws Exception {
		I_TermFactory tf = Terms.get();
		DefaultMutableTreeNode top = null;
		if (concept != null) {
			try {
				top = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toString(), TreeEditorObjectWrapper.CONCEPT, concept));
				for (I_GetConceptData langRefset: this.translationProject.getSourceLanguageRefsets()){
					List<ContextualizedDescription> descriptions = LanguageUtil.getContextualizedDescriptions(
							concept.getConceptNid(), langRefset.getConceptNid(), true);

					DefaultMutableTreeNode groupLang = new DefaultMutableTreeNode(
							new TreeEditorObjectWrapper(langRefset.getInitialText(), TreeEditorObjectWrapper.FOLDER,langRefset ));

					List<DefaultMutableTreeNode> nodesToAdd = new ArrayList<DefaultMutableTreeNode>();
					boolean bSourceFSN = false;
					for (I_ContextualizeDescription description : descriptions) {
						if (description.getLanguageExtension()!=null && description.getLanguageRefsetId()==langRefset.getConceptNid()){
							DefaultMutableTreeNode descriptionNode = null;

							if (description.getAcceptabilityId()==notAcceptable.getConceptNid() ||
									description.getExtensionStatusId()==inactive.getConceptNid() ||
										description.getDescriptionStatusId()==retired.getConceptNid()){

								descriptionNode = new DefaultMutableTreeNode(
										new TreeEditorObjectWrapper("Not Acceptable:" + description.getText(), TreeEditorObjectWrapper.NOTACCEPTABLE , description));
								
							} else if (description.getTypeId() == fsn.getConceptNid()) {
								descriptionNode = new DefaultMutableTreeNode(
										new TreeEditorObjectWrapper("FSN:" + description.getText(), TreeEditorObjectWrapper.FSNDESCRIPTION, description));

								int semtagLocation = description.getText().lastIndexOf("(");
								if (semtagLocation == -1) semtagLocation = description.getText().length();

								int endParenthesis=description.getText().lastIndexOf(")");

								if (semtagLocation > -1 && semtagLocation<endParenthesis )
									sourceSemTag=description.getText().substring( semtagLocation + 1,endParenthesis);
								if (!bSourceFSN){
									bSourceFSN=true;
									sourceFSN=description.getText().substring(0, semtagLocation);
									Runnable simil = new Runnable() {
										public void run() {
											updateSimilarityTable(sourceFSN);
										}
									};
									simil.run();
									Runnable tMemo = new Runnable() {
										public void run() {
											updateTransMemoryTable(sourceFSN);
										}
									};
									tMemo.run();
									Runnable gloss = new Runnable() {
										public void run() {
											updateGlossaryEnforcement(sourceFSN);
										}
									};
									gloss.run();
								}
							} else if (description.getAcceptabilityId() == acceptable.getConceptNid()) {
								descriptionNode = new DefaultMutableTreeNode(
										new TreeEditorObjectWrapper("Acceptable:" + description.getText(), TreeEditorObjectWrapper.SYNONYMN, description));
							} else if (description.getAcceptabilityId() == preferred.getConceptNid()) {
								descriptionNode = new DefaultMutableTreeNode(
										new TreeEditorObjectWrapper("Preferred:" + description.getText(), TreeEditorObjectWrapper.PREFERRED , description));
							}else{
								descriptionNode = new DefaultMutableTreeNode(
										new TreeEditorObjectWrapper("Not Acceptable:" + description.getText(), TreeEditorObjectWrapper.NOTACCEPTABLE , description));
								
							}

							I_GetConceptData descriptionStatusConcept = tf.getConcept(description.getDescriptionStatusId());
							descriptionNode.add(new DefaultMutableTreeNode(
									new TreeEditorObjectWrapper(descriptionStatusConcept.toString(), TreeEditorObjectWrapper.DESCRIPTIONINFO, description)));
							descriptionNode.add(new DefaultMutableTreeNode(
									new TreeEditorObjectWrapper("Is case significant: " + description.isInitialCaseSignificant(), TreeEditorObjectWrapper.DESCRIPTIONINFO, description)));
							nodesToAdd.add(descriptionNode);
						}
					}
					for (DefaultMutableTreeNode loopNode : nodesToAdd) {
						TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
						if (nodeObject.getType() == TreeEditorObjectWrapper.FSNDESCRIPTION) {
							groupLang.add(loopNode);
						}
					}

					for (DefaultMutableTreeNode loopNode : nodesToAdd) {
						TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
						if (nodeObject.getType() == TreeEditorObjectWrapper.PREFERRED) {
							groupLang.add(loopNode);
						}
					}

					for (DefaultMutableTreeNode loopNode : nodesToAdd) {
						TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
						if (nodeObject.getType() == TreeEditorObjectWrapper.SYNONYMN) {
							groupLang.add(loopNode);
						}
					}

					for (DefaultMutableTreeNode loopNode : nodesToAdd) {
						TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
						if (nodeObject.getType() == TreeEditorObjectWrapper.NOTACCEPTABLE) {
							groupLang.add(loopNode);
						}
					}
					top.add(groupLang);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (TerminologyException e) {
				e.printStackTrace();
			}
		}

		DefaultTreeModel treeModel = new DefaultTreeModel(top);

		tree1.setModel(treeModel);
		for (int i =0 ;i<tree1.getRowCount();i++){
			TreePath tp=tree1.getPathForRow(i);
			if (tp.getPath().length==2)
				tree1.expandRow(i);
		}
		tree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree1.revalidate();

	}


	private void populateTargetTree() throws Exception {
		I_TermFactory tf = Terms.get();
		DefaultMutableTreeNode top = null;
		boolean bHasPref =false;
		boolean bHasFSN =false;
		if (concept != null) {
			try {
				top = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toString(), TreeEditorObjectWrapper.CONCEPT, concept));
				I_GetConceptData langRefset=this.translationProject.getTargetLanguageRefset();
				List<ContextualizedDescription> descriptions = LanguageUtil.getContextualizedDescriptions(
						concept.getConceptNid(), langRefset.getConceptNid(), true);

				DefaultMutableTreeNode groupLang = new DefaultMutableTreeNode(
						new TreeEditorObjectWrapper(langRefset.getInitialText(), TreeEditorObjectWrapper.FOLDER,langRefset ));

				List<DefaultMutableTreeNode> nodesToAdd = new ArrayList<DefaultMutableTreeNode>();

				for (I_ContextualizeDescription description : descriptions) {
					if (description.getLanguageExtension()!=null && description.getLanguageRefsetId()==langRefset.getConceptNid()){
						DefaultMutableTreeNode descriptionNode = null;

						if (description.getAcceptabilityId()==notAcceptable.getConceptNid() ||
								description.getExtensionStatusId()==inactive.getConceptNid() ||
									description.getDescriptionStatusId()==retired.getConceptNid()){

							descriptionNode = new DefaultMutableTreeNode(
									new TreeEditorObjectWrapper("Not Acceptable:" + description.getText(), TreeEditorObjectWrapper.NOTACCEPTABLE , description));
							
						} else if (description.getTypeId() == fsn.getConceptNid()) {
							descriptionNode = new DefaultMutableTreeNode(
									new TreeEditorObjectWrapper("FSN:" + description.getText(), TreeEditorObjectWrapper.FSNDESCRIPTION, description));
							bHasFSN=true;

						} else if (description.getAcceptabilityId() == acceptable.getConceptNid()) {
							descriptionNode = new DefaultMutableTreeNode(
									new TreeEditorObjectWrapper("Acceptable:" + description.getText(), TreeEditorObjectWrapper.SYNONYMN, description));
						} else if (description.getAcceptabilityId() == preferred.getConceptNid()) {
							descriptionNode = new DefaultMutableTreeNode(
									new TreeEditorObjectWrapper("Preferred:" + description.getText(), TreeEditorObjectWrapper.PREFERRED , description));
							bHasPref = true;
							targetPreferred=description.getText();
							targetPreferredICS=description.isInitialCaseSignificant();
						}else{
							descriptionNode = new DefaultMutableTreeNode(
									new TreeEditorObjectWrapper("Not Acceptable:" + description.getText(), TreeEditorObjectWrapper.NOTACCEPTABLE , description));
							
						}

						I_GetConceptData descriptionStatusConcept = tf.getConcept(description.getDescriptionStatusId());
						descriptionNode.add(new DefaultMutableTreeNode(
								new TreeEditorObjectWrapper(descriptionStatusConcept.toString(), TreeEditorObjectWrapper.DESCRIPTIONINFO, description)));
						descriptionNode.add(new DefaultMutableTreeNode(
								new TreeEditorObjectWrapper("Is case significant: " + description.isInitialCaseSignificant(), TreeEditorObjectWrapper.DESCRIPTIONINFO, description)));
						nodesToAdd.add(descriptionNode);
					}
				}
				for (DefaultMutableTreeNode loopNode : nodesToAdd) {
					TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
					if (nodeObject.getType() == TreeEditorObjectWrapper.FSNDESCRIPTION) {
						groupLang.add(loopNode);
					}
				}

				for (DefaultMutableTreeNode loopNode : nodesToAdd) {
					TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
					if (nodeObject.getType() == TreeEditorObjectWrapper.PREFERRED) {
						groupLang.add(loopNode);
					}
				}

				for (DefaultMutableTreeNode loopNode : nodesToAdd) {
					TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
					if (nodeObject.getType() == TreeEditorObjectWrapper.SYNONYMN) {
						groupLang.add(loopNode);
					}
				}

				for (DefaultMutableTreeNode loopNode : nodesToAdd) {
					TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
					if (nodeObject.getType() == TreeEditorObjectWrapper.NOTACCEPTABLE) {
						groupLang.add(loopNode);
					}
				}
				top.add(groupLang);
				String status=Terms.get().getConcept(worklistMember.getActivityStatus()).toString();
				DefaultMutableTreeNode wListMembStatNode = new DefaultMutableTreeNode(
						new TreeEditorObjectWrapper("Worklist member status: " + status, TreeEditorObjectWrapper.RELATIONSHIPINFO , null));
				top.add(wListMembStatNode);

			} catch (IOException e) {
				e.printStackTrace();
			} catch (TerminologyException e) {
				e.printStackTrace();
			}
		}

		DefaultTreeModel treeModel = new DefaultTreeModel(top);

		tree2.setModel(treeModel);
		for (int i =0 ;i<tree2.getRowCount();i++){
			TreePath tp=tree2.getPathForRow(i);
			if (tp.getPath().length==2)
				tree2.expandRow(i);
		}
		tree2.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree2.revalidate();

	}

	private void populateDetailsTree() throws Exception {
		I_TermFactory tf = Terms.get();
		DefaultMutableTreeNode top = null;
		if (concept != null) {
			try {
				//TODO add config as parameter
				I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
				
				top = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toString(), TreeEditorObjectWrapper.CONCEPT, concept));
				I_ConceptAttributeTuple attributes = null;
				attributes = concept.getConceptAttributeTuples(config.getPrecedence(), 
						config.getConflictResolutionStrategy()).iterator().next();
				String definedOrPrimitive = "";
				if (attributes.isDefined()) {
					definedOrPrimitive = "fully defined";
				} else {
					definedOrPrimitive = "primitive";
				}
				DefaultMutableTreeNode idNode = new DefaultMutableTreeNode(
						new TreeEditorObjectWrapper(definedOrPrimitive, TreeEditorObjectWrapper.ATTRIBUTE, attributes.getMutablePart()));
				top.add(idNode);

				String statusName = tf.getConcept(attributes.getStatusNid()).toString();
				DefaultMutableTreeNode statusNode = new DefaultMutableTreeNode(
						new TreeEditorObjectWrapper(statusName, TreeEditorObjectWrapper.ATTRIBUTE, attributes.getMutablePart()));
				top.add(statusNode);

				List<I_RelTuple> relationships = (List<I_RelTuple>) concept.getSourceRelTuples(null, null, config.getViewPositionSetReadOnly(),
						config.getPrecedence(), config.getConflictResolutionStrategy());
//				List<I_RelVersioned> relationships2 = (List<I_RelVersioned>) concept.getDestRels();

				List<DefaultMutableTreeNode> nodesToAdd = new ArrayList<DefaultMutableTreeNode>();

				HashMap<Integer,List<DefaultMutableTreeNode>> mapGroup= new HashMap<Integer,List<DefaultMutableTreeNode>>() ;
				List<DefaultMutableTreeNode> roleList=new ArrayList<DefaultMutableTreeNode>();
				int group=0;
				for (I_RelTuple relationship : relationships) {
					I_GetConceptData targetConcept = tf.getConcept(relationship.getC2Id());
					I_GetConceptData typeConcept = tf.getConcept(relationship.getTypeNid());
					String label = typeConcept + ": " + targetConcept;

					if (relationship.getTypeNid() == ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid()) {
						DefaultMutableTreeNode supertypeNode = new DefaultMutableTreeNode(
								new TreeEditorObjectWrapper(label, TreeEditorObjectWrapper.SUPERTYPE, relationship.getMutablePart()));
						nodesToAdd.add(supertypeNode);
					} else {
						if (relationship.getGroup()==0){
							if (relationship.getCharacteristicId()==definingChar){
								DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(
										new TreeEditorObjectWrapper(label, TreeEditorObjectWrapper.ROLE, relationship.getMutablePart()));
								nodesToAdd.add(roleNode);
							}
							else{
								DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(
										new TreeEditorObjectWrapper(label, TreeEditorObjectWrapper.ASSOCIATION, relationship.getMutablePart()));
								nodesToAdd.add(roleNode);
							}
						}
						else
						{
							group = relationship.getGroup();
							if (mapGroup.containsKey(group)){
								roleList=mapGroup.get(group);
							}
							else{
								roleList=new ArrayList<DefaultMutableTreeNode>();
							}

							roleList.add(new DefaultMutableTreeNode(
									new TreeEditorObjectWrapper(label, TreeEditorObjectWrapper.ROLE, relationship.getMutablePart())));
							mapGroup.put(group, roleList);
						}
					}
				}

			

				for (DefaultMutableTreeNode loopNode : nodesToAdd) {
					TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
					if (nodeObject.getType() == TreeEditorObjectWrapper.SUPERTYPE) {
						top.add(loopNode);
					}
				}

				for (DefaultMutableTreeNode loopNode : nodesToAdd) {
					TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
					if (nodeObject.getType() == TreeEditorObjectWrapper.ROLE) {
						top.add(loopNode);
					}
				}
				for (int key:mapGroup.keySet()){
					List<DefaultMutableTreeNode> lRoles=(List<DefaultMutableTreeNode>)mapGroup.get(key);
					DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(
							new TreeEditorObjectWrapper("Group:" + key, TreeEditorObjectWrapper.ROLEGROUP,lRoles ));
					for (DefaultMutableTreeNode rNode: lRoles){
						groupNode.add(rNode);
					}
					top.add(groupNode);
				}
				for (DefaultMutableTreeNode loopNode : nodesToAdd) {
					TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
					if (nodeObject.getType() == TreeEditorObjectWrapper.ASSOCIATION) {
						top.add(loopNode);
					}
				}


			} catch (IOException e) {
				e.printStackTrace();
			} catch (TerminologyException e) {
				e.printStackTrace();
			}
			DefaultTreeModel treeModel = new DefaultTreeModel(top);

			tree3.setModel(treeModel);

			for (int i = 0; i < tree3.getRowCount(); i++) {
				tree3.expandRow(i);
			}
			tree3.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree3.revalidate();
		}
	}
	
	class IconRenderer extends DefaultTreeCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		Icon redIcon;
		Icon attributeIcon;
		Icon orangeIcon;
		Icon descriptionIcon;
		Icon fsnIcon;
		Icon blackIcon;
		Icon preferredIcon;
		Icon roleGroupIcon;
		Icon folder;
		Icon associationIcon;
		Icon notAcceptIcon;

		public IconRenderer() {
			redIcon = new ImageIcon("icons/91.png");
			attributeIcon = new ImageIcon("icons/ConceptStatus.gif");
			orangeIcon = new ImageIcon("icons/90.png");
			descriptionIcon = new ImageIcon("icons/Description.gif");
			fsnIcon = new ImageIcon("icons/Name.gif");
			blackIcon = new ImageIcon("icons/85.png");
			preferredIcon= new ImageIcon("icons/Preferred.gif");
			roleGroupIcon=new ImageIcon("icons/rolegroup.gif");
			folder=new ImageIcon("icons/folder.png");
			associationIcon=new ImageIcon("icons/Association.gif");
			notAcceptIcon=new ImageIcon("icons/NotAccept.gif");
		}

		@Override
		public Component getTreeCellRendererComponent(
				JTree tree,
				Object value,
				boolean sel,
				boolean expanded,
				boolean leaf,
				int row,
				boolean hasFocus) {

			super.getTreeCellRendererComponent(
					tree, value, sel,
					expanded, leaf, row,
					hasFocus);
			DefaultMutableTreeNode node =  (DefaultMutableTreeNode)value;
			if (node!=null && (node.getUserObject() instanceof TreeEditorObjectWrapper)){
				TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) node.getUserObject();
				switch (nodeObject.getType()) {
				case 3: setIcon(attributeIcon); break;
				case 4: setIcon(fsnIcon); break;
				case 5: setIcon(preferredIcon); break;
				case 6: setIcon(orangeIcon); break;
				case 7: setIcon(redIcon); break;
				case 8: setIcon(descriptionIcon); break;
				case 10: setIcon(descriptionIcon); break;
				case 11: setIcon(roleGroupIcon); break;
				case 12: setIcon(folder); break;
				case 13: setIcon(associationIcon); break;
				case 14: setIcon(notAcceptIcon); break;
				default: setIcon(blackIcon); break;
				}
			}else{
				setIcon(blackIcon); 
			}
			return this;
		}

	}

	/**
	 * Update similarity table.
	 * 
	 * @param query the query
	 */
	private void updateSimilarityTable(String query) {
		// TODO fix language parameters
		List<Integer> types= new ArrayList<Integer>();
		if (rbFSN.isSelected())
			types.add(fsn.getConceptNid());
		else
			if (rbPref.isSelected())
				types.add(preferred.getConceptNid());
			else{
				types.add(fsn.getConceptNid());
				types.add(preferred.getConceptNid());
			}

		List<SimilarityMatchedItem> results = LanguageUtil.getSimilarityResults(query, sourceIds, targetId, types);
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
		// TODO fix language parameters

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
		}else{
			tabbedPane1.setTitleAt(1, "<html>Translation Memory</html>");
		}
		TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
		cmodel.getColumn(0).setCellRenderer(textAreaRenderer); 
		cmodel.getColumn(1).setCellRenderer(textAreaRenderer); 
		table2.revalidate();
	}

	public void updateUI(TranslationProject translationProject,WorkListMember workListMember){
		clearForm(true);
		this.translationProject=translationProject;
		this.concept=workListMember.getConcept();
		this.worklistMember=workListMember;
		sourceLangRefsets=new HashSet<LanguageMembershipRefset>();
		sourceIds=new ArrayList<Integer>();
		targetId=-1;
		try {
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			List<I_GetConceptData> sourceLangConcepts = translationProject.getSourceLanguageRefsets();
			if (sourceLangConcepts!=null){
				for (I_GetConceptData sourceLangConcept:sourceLangConcepts){
					sourceLangRefsets.add(new LanguageMembershipRefset(sourceLangConcept,config));
					sourceIds.add(sourceLangConcept.getConceptNid());
				}
			}
			I_GetConceptData targetLangConcept = translationProject.getTargetLanguageRefset();
			if (targetLangConcept!=null){
				targetLangRefset=new LanguageMembershipRefset(targetLangConcept,config);
				targetId=targetLangConcept.getConceptNid();
			}
			populateSourceTree();
			populateTargetTree();
			populateDetailsTree();
			getPreviousComments();
			Object flag=config.getDbConfig().getProperty(TranslationHelperPanel.EXTERNAL_ISSUES_ACTIVE_PROPERTY_FLAG);
			if (flag!=null && flag.toString().equals("true")){
				tabbedPane2.setTitleAt(1, "<html>Issues</html>");
				Thread appthr=new Thread(){
					public void run(){
						SwingUtilities.invokeLater(new Runnable(){

							@Override
							public void run() {
								loadIssues();

							}


						});
					}

				};
				appthr.start();
			}else{
				tabbedPane2.setTitleAt(1, "<html>Issues <font><style size=1>(Inactive)</style></font></html>");
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

	protected void loadIssues() {
		if (issueListPanel==null){
			createIssuePanel();
		}
		if (issueListPanel==null){
			return;
		}
		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			List<UUID> repoUUID=translConfig.getProjectIssuesRepositoryIds();
			if (repoUUID!=null && repoUUID.size()>0){
				IssueRepository repo= IssueRepositoryDAO.getIssueRepository(Terms.get().getConcept(repoUUID)); 
				IssueRepoRegistration regis;
				regis=IssueRepositoryDAO.getRepositoryRegistration(repo.getUuid(), config);
				if (regis!=null && regis.getUserId()!= null && regis.getPassword()!=null){
					issueListPanel.loadIssues(concept,repo,regis);
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createIssuePanel() {
		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			if (config==null)
				issueListPanel=new IssuesListPanel2();
			else
				issueListPanel=new IssuesListPanel2(config);
			
			panel11.add(issueListPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}
	
	private void clearComments(){
		String[] columnNames = {"Comment"};
		String[][] data = null;
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};

		tblComm.setModel(tableModel);
		tblComm.revalidate();
		tabbedPane2.setTitleAt(0, "<html>Previous Comments</font></b></html>");
	}
	private void clearSimilarities(){
		String[] columnNames = {"Source Text",
		"Target Text"};
		String[][] data = null;
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};
		table1.setModel(tableModel);
		table1.revalidate();
	}

	private void clearLingGuidelines(){
		tabbedPane1.setTitleAt(2, "<html>Linguistic Guidelines</html>");
		editorPane1.setText("");
		editorPane1.revalidate();
	}

	private void clearTransMemory(){
		String[] columnNames = {"Pattern Text",
		"Translated to.."};
		String[][] data = null;
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};

		table2.setModel(tableModel);
		tabbedPane1.setTitleAt(1, "<html>Translation Memory</html>");
		table2.revalidate();
	}

	private void getPreviousComments() {
		if (targetLangRefset!=null){
			I_ConfigAceFrame config;
			try {
				config = Terms.get().getActiveAceFrameConfig();
				List<String>comments=targetLangRefset.getCommentsRefset(config).getComments(this.concept.getConceptNid());
				String[] columnNames = {"Comment"};
				String[][] data = null;
				DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
					private static final long serialVersionUID = 1L;

					public boolean isCellEditable(int x, int y) {
						return false;
					}
				};
				for (int i=comments.size()-1;i>-1;i--) {
					tableModel.addRow(new Object[] {formatComment(comments.get(i))});
				}

				tblComm.setModel(tableModel);
				TableColumnModel cmodel = tblComm.getColumnModel(); 
				TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
				cmodel.getColumn(0).setCellRenderer(textAreaRenderer); 
				tblComm.revalidate();
				if (tblComm.getRowCount()>0){
					tabbedPane2.setTitleAt(0, "<html>Previous Comments <b><font color='red'>(" + tblComm.getRowCount() + ")</font></b></html>");
				}else {
					tabbedPane2.setTitleAt(0, "<html>Previous Comments (0)</font></b></html>");
				}
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String formatComment(String comment) {
		long thickVer = Terms.get().convertToThickVersion(Integer.parseInt(comment.substring(comment.trim().lastIndexOf(" ") +1)));
		String strDate = formatter.format(thickVer);
		return  "<"  + strDate + ">" + comment.substring(0,comment.lastIndexOf(" - Time:")) ;
		
	}
	
	/**
	 * Update glossary enforcement.
	 * 
	 * @param query the query
	 */
	private void updateGlossaryEnforcement(String query) {

		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			String results = DocumentManager.getInfoForTerm(query,Terms.get().getConcept(ArchitectonicAuxiliary.Concept.LINGUISTIC_GUIDELINES_ROOT.getUids()), config);
			if (!results.equals("")){
				tabbedPane1.setTitleAt(2, "<html>Linguistic Guidelines<b><font color='red'>*</font></b></html>");
			}
			else{
				tabbedPane1.setTitleAt(2, "<html>Linguistic Guidelines</html>");
			}
			editorPane1.setText(results);
			editorPane1.revalidate();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Set<LanguageMembershipRefset> getSourceLangRefsets() {
		return sourceLangRefsets;
	}

	public void setSourceLangRefsets(Set<LanguageMembershipRefset> sourceLangRefsets) {
		this.sourceLangRefsets = sourceLangRefsets;
		sourceIds=new ArrayList<Integer>();
		for (LanguageMembershipRefset sourceRef:sourceLangRefsets){
			sourceIds.add(sourceRef.getRefsetId());
		}
	}

	public LanguageMembershipRefset getTargetLangRefset() {
		return targetLangRefset;
	}

	public void setTargetLangRefset(LanguageMembershipRefset targetLangRefset) {
		this.targetLangRefset = targetLangRefset;
		targetId=targetLangRefset.getRefsetId();
	}
}
