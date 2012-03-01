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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.LineBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
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
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.issue.manager.IssuesListPanel2;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.panel.PanelHelperFactory;
import org.ihtsdo.project.panel.TranslationHelperPanel;
import org.ihtsdo.project.panel.details.WorklistMemberLogPanel;
import org.ihtsdo.project.refset.CommentsRefset;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.SimilarityMatchedItem;
import org.ihtsdo.translation.TreeEditorObjectWrapper;
import org.ihtsdo.translation.ui.ConfigTranslationModule.TreeComponent;
import org.ihtsdo.translation.ui.translation.CommentPanel;
import org.ihtsdo.translation.ui.translation.NewCommentPanel;
 
 /**
  * The Class TranslationConceptEditor5.
  */
 public class TranslationConceptEditor5 extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The Constant HEADER_SEPARATOR. */
	private static final String HEADER_SEPARATOR = " // ";
	
	/** The Constant COMMENT_HEADER_SEP. */
	private static final String COMMENT_HEADER_SEP = ": -";
	
	/** The translation project. */
	private TranslationProject translationProject;
	
	/** The synonym. */
	private I_GetConceptData synonym;
	
	/** The fsn. */
	private I_GetConceptData fsn;
	
	/** The preferred. */
	private I_GetConceptData preferred;
	
	/** The source ids. */
	private List<Integer> sourceIds;
	
	/** The target id. */
	private int targetId;
	
	/** The not acceptable. */
	private I_GetConceptData notAcceptable;
	
	/** The acceptable. */
	private I_GetConceptData acceptable;
	
	/** The current. */
	private I_GetConceptData current;
	
	/** The source lang refsets. */
	private Set<LanguageMembershipRefset> sourceLangRefsets;
	
	/** The target lang refset. */
	private LanguageMembershipRefset targetLangRefset;
	
	/** The formatter. */
	private SimpleDateFormat formatter;
	
	/** The description. */
	private I_GetConceptData description;
	
	/** The inactive. */
	private I_GetConceptData inactive;
	
	/** The active. */
	private I_GetConceptData active;
	
	/** The retired. */
	private I_GetConceptData retired;
	
	/** The issue list panel. */
	private IssuesListPanel2 issueListPanel;
	
	/** The transl config. */
	private ConfigTranslationModule translConfig;
	
	/** The assigned mnemo. */
	private String assignedMnemo;
	
	/** The read only mode. */
	private boolean readOnlyMode;
	
	/**
	 * Instantiates a new translation concept editor.
	 *
	 */
	public TranslationConceptEditor5() {
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
			//			translConfig=LanguageUtil.getTranslationConfig(config);
			//			if (translConfig==null){
			//				setDefaultConfigValues(translConfig);
			//			}
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
			definingChar=ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.localize().getNid();
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}

		initComponents();
		
		refineCheckBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if(!refineCheckBox.isSelected()){
					refinePanel.setVisible(false);
					refinePanel.validate();
				}else{
					refinePanel.setVisible(true);
					refinePanel.validate();
				}
			}
		});
		
		searchButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				searchButtonActionPreformed(e);
			}
		});
		
		formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		//		bDescIssue.setEnabled(false);
		DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
		comboBoxModel.addElement(fsn);
		comboBoxModel.addElement(description);

		comboBox1.setModel(comboBoxModel);
		comboBox1.setSelectedIndex(1);
		comboBox1.setEnabled(false);

		DefaultComboBoxModel comboBoxModel2 = new DefaultComboBoxModel();
		comboBoxModel2.addElement(preferred);
		comboBoxModel2.addElement(acceptable);
		comboBoxModel2.addElement(notAcceptable);

		cmbAccep.setModel(comboBoxModel2);
		cmbAccep.setSelectedIndex(1);
		cmbAccep.setEnabled(false);
		
		rbYes.setEnabled(false);
		rbNo.setEnabled(false);
		rbAct.setEnabled(false);
		rbInact.setEnabled(false);

		//		tree1.setCellRenderer(new HtmlSafeTreeCellRenderer());

//		tree1.setCellRenderer(new IconRenderer());
//		tree1.setRootVisible(true);
//		tree1.setShowsRootHandles(false);
//
//		tree2.setCellRenderer(new IconRenderer());
//		tree2.setRootVisible(true);
//		tree2.setShowsRootHandles(false);

		tree3.setCellRenderer(new DetailsIconRenderer());
		tree3.setRootVisible(true);
		tree3.setShowsRootHandles(false);

		setByCode=false;
		saveDesc.setEnabled(false);
		mSpellChk.setEnabled(false);
		mAddDesc.setEnabled(true);
		mAddPref.setEnabled(true);
		button5.setEnabled(false);
		bKeep.setEnabled(false);
		bReview.setEnabled(false);
		bEscalate.setEnabled(false);
		label4.setVisible(false);

//		DefaultMutableTreeNode sourceRoot=new DefaultMutableTreeNode();
//		tree1.setModel(new DefaultTreeModel(sourceRoot));
//
//		DefaultMutableTreeNode targetRoot=new DefaultMutableTreeNode();
//		tree2.setModel(new DefaultTreeModel(targetRoot));
		tabSou.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabTar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabSou.setModel(new DefaultTableModel());
		tabTar.setModel(new DefaultTableModel());
		tabTar.getSelectionModel().addListSelectionListener(new SelectionListener(tabTar));
		tabTar.setUpdateSelectionOnSort(false);
		tabSou.setUpdateSelectionOnSort(false);
		DefaultMutableTreeNode detailsRoot=new DefaultMutableTreeNode();
		tree3.setModel(new DefaultTreeModel(detailsRoot));

		ToolTipManager.sharedInstance().registerComponent(tree3);
		createIssuePanel();
		setMnemoInit();

    	refTable.setContentType("text/html");
    	refTable.setEditable(false);
    	refTable.setOpaque(false);
		//	populateTree();

	}

	/**
	 * The Enum TableSourceColumn.
	 */
	enum TableSourceColumn{
		  
  		/** The LANGUAGE. */
  		LANGUAGE("Language"), 
 /** The TER m_ type. */
 TERM_TYPE("Term type"), 
 /** The ACCEPTABILITY. */
 ACCEPTABILITY("Acceptability"), 
 /** The ICS. */
 ICS("ICS"),
/** The TERM. */
TERM("Term");

		/** The column name. */
		private final String columnName;
	
		/**
		 * Instantiates a new table source column.
		 *
		 * @param name the name
		 */
		private TableSourceColumn(String name) {
			this.columnName = name;
		}
	
		/**
		 * Gets the column name.
		 *
		 * @return the column name
		 */
		public String getColumnName() {
			return this.columnName;
		}
	}

	/**
	 * The Enum TableTargetColumn.
	 */
	enum TableTargetColumn{
		 
 		/** The LANGUAGE. */
 		LANGUAGE("Language"), 
 /** The TER m_ type. */
 TERM_TYPE("Term type"), 
 /** The ACCEPTABILITY. */
 ACCEPTABILITY("Acceptability"), 
 /** The ICS. */
 ICS("ICS"),
/** The TERM. */
TERM("Term");

		/** The column name. */
		private final String columnName;
	
		/**
		 * Instantiates a new table target column.
		 *
		 * @param name the name
		 */
		private TableTargetColumn(String name) {
			this.columnName = name;
		}
	
		/**
		 * Gets the column name.
		 *
		 * @return the column name
		 */
		public String getColumnName() {
			return this.columnName;
		}
	}

	/**
	 * Search button action preformed.
	 *
	 * @param e the e
	 */
	private void searchButtonActionPreformed(ActionEvent e){
		String query = searchTextField.getText();
		if(!query.trim().equals("")){
			updateSimilarityTable(query);
		}
	}
	
	/**
	 * Gets the translation project config.
	 *
	 * @return the translation project config
	 */
	private ConfigTranslationModule getTranslationProjectConfig(){
		ConfigTranslationModule translProjConfig=null;
		if (this.translationProject!=null)
			translProjConfig=LanguageUtil.getDefaultTranslationConfig(this.translationProject);

		if (translProjConfig==null){
			return translConfig;
		}
		translProjConfig.setColumnsDisplayedInInbox(translConfig.getColumnsDisplayedInInbox());
		translProjConfig.setAutoOpenNextInboxItem(translConfig.isAutoOpenNextInboxItem());
		translProjConfig.setSourceTreeComponents(translConfig.getSourceTreeComponents());
		translProjConfig.setTargetTreeComponents(translConfig.getTargetTreeComponents());
		return translProjConfig;
	}

	/**
	 * Sets the mnemo init.
	 */
	private void setMnemoInit(){
		assignedMnemo="FPDHIAVUMGOL";
	}

	/**
	 * Unload data.
	 */
	public void unloadData(){
		verifySavePending();
		clearForm(true);
	}
	
	/**
	 * Verify save pending.
	 */
	private void verifySavePending() {
		boolean bPendTerm=false;
		if (descriptionInEditor!=null ){
			if (!(descriptionInEditor.getText().trim().equals(textField1.getText().trim()) && (
					descriptionInEditor.isInitialCaseSignificant() == rbYes.isSelected())
					&& descriptionInEditor.getAcceptabilityId()== ((I_GetConceptData)cmbAccep.getSelectedItem()).getConceptNid()
					&& ((descriptionInEditor.getExtensionStatusId()==active.getConceptNid()
							&& rbAct.isSelected()) 
							||(descriptionInEditor.getExtensionStatusId()!=active.getConceptNid()
									&& !rbAct.isSelected()) )
									&& ((descriptionInEditor.getTypeId()==fsn.getConceptNid() && fsn.equals((I_GetConceptData)comboBox1.getSelectedItem()) )
											|| (descriptionInEditor.getTypeId()!=fsn.getConceptNid() && !fsn.equals((I_GetConceptData)comboBox1.getSelectedItem()))))) {
				bPendTerm=true;
			}
		}else{
			if (!textField1.getText().trim().equals("")){
				bPendTerm=true;
			}
		}
		if (bPendTerm ){
			Object[] options = {"Discard unsaved data", "Save"};
			int n = JOptionPane.showOptionDialog(null,
					"Do you want to save the change you made to the term in the editor panel?",
					"Unsaved data",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null,     //do not use a custom Icon
					options,  //the titles of buttons
					options[1]); //default button title
			if (n == 0) {
				return;
			} else {
				if (bPendTerm)
					saveDescActionPerformed();
			}
		}

	}
	
	/**
	 * Sets the read only mode.
	 *
	 * @param readOnly the new read only mode
	 */
	private void setReadOnlyMode(boolean readOnly){
		this.readOnlyMode=readOnly;
		textField1.setEnabled(!readOnly);
		comboBox1.setEnabled(!readOnly);
		cmbAccep.setEnabled(!readOnly);
		saveDesc.setEnabled(!readOnly);
		button5.setEnabled(!readOnly);
		bAddFSN.setEnabled(!readOnly);
		mAddPref.setEnabled(!readOnly);
		mAddDesc.setEnabled(!readOnly);
		mSpellChk.setEnabled(!readOnly);
		rbYes.setEnabled(!readOnly);
		rbNo.setEnabled(!readOnly);
		rbAct.setEnabled(!readOnly);
		rbInact.setEnabled(!readOnly);

	}
	
	/**
	 * Clear form.
	 *
	 * @param clearAll the clear all
	 */
	synchronized
	private void clearForm(boolean clearAll){
		descriptionInEditor = null;
		comboBox1.setEnabled(false);
		cmbAccep.setEnabled(false);
		//		label4.setVisible(true);
		//		label4.setText("");
		textField1.setText("");
		textField1.setEnabled(false);
		rbYes.setSelected(false);
		panel2.revalidate();
		mSpellChk.setEnabled(false);
		//button5.setEnabled(false);
		mAddDesc.setEnabled(true);
		mAddPref.setEnabled(true);
		//mClose.setEnabled(true);
		rbYes.setEnabled(false);
		rbInact.setEnabled(false);
		rbNo.setEnabled(false);
		rbAct.setEnabled(false);
		
		if (clearAll){
			saveDesc.setEnabled(false);
//			DefaultMutableTreeNode root=new DefaultMutableTreeNode();
//			tree1.setModel(new DefaultTreeModel(root));
//			DefaultMutableTreeNode root2=new DefaultMutableTreeNode();
//			tree2.setModel(new DefaultTreeModel(root2));

			tabSou.setModel(new DefaultTableModel());
			tabTar.setModel(new DefaultTableModel());
			DefaultMutableTreeNode root3=new DefaultMutableTreeNode();
			tree3.setModel(new DefaultTreeModel(root3));
			table1.setModel(new DefaultTableModel());
			table2.setModel(new DefaultTableModel());
			this.translationProject=null;
			this.concept=null;
			if (issueListPanel!=null){
				issueListPanel.loadIssues(null,null,null);
			}
			tabbedPane3.setSelectedIndex(0);
			clearComments();
			clearLingGuidelines();
			clearTransMemory();
			clearSimilarities();
			setMnemoInit();
		}
	}

	/**
	 * Retire action performed.
	 * 
	 * @param e the e
	 */
	private void retireActionPerformed(ActionEvent e) {
		clearForm(false);
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
	 * Spellcheck action performed.
	 *
	 */
	private void mSpellChkActionPerformed() {
		AceFrameConfig config;
		try {
			config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();
			textField1.setText(DocumentManager.spellcheckPhrase(textField1.getText(), null, targetLangRefset.getLangCode(config)));
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * M close action performed.
	 */
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
						tp.repaint();
						tp.revalidate();
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
	 * M add pref action performed.
	 */
	private void mAddPrefActionPerformed() {
		descriptionInEditor = null;
		//		label4.setText("");
		//		label4.setVisible(false);
		textField1.setText("");
		textField1.setEnabled(true);
		panel2.revalidate();
		saveDesc.setEnabled(true);
		mSpellChk.setEnabled(true);
		button5.setEnabled(true);
		comboBox1.setEnabled(true);
		comboBox1.setSelectedItem(description);
		cmbAccep.setEnabled(true);
		cmbAccep.setSelectedItem(preferred);
		rbNo.setEnabled(true);
		rbAct.setEnabled(true);
		rbYes.setEnabled(true);
		rbInact.setEnabled(true);
	}

	/**
	 * M add desc action performed.
	 */
	private void mAddDescActionPerformed() {
		descriptionInEditor = null;
		//		label4.setText("");
		//		label4.setVisible(false);
		textField1.setText("");
		textField1.setEnabled(true);
		panel2.revalidate();
		saveDesc.setEnabled(true);
		mSpellChk.setEnabled(true);
		button5.setEnabled(true);
		comboBox1.setEditable(true);
		comboBox1.setSelectedItem(description);
		cmbAccep.setEditable(true);
		cmbAccep.setSelectedItem(acceptable);
		rbNo.setSelected(true);
		rbAct.setSelected(true);
		rbInact.setEnabled(true);
		rbYes.setEnabled(true);
	}



	/**
	 * B keep action performed.
	 */
	private void bKeepActionPerformed() {
		clearForm(true);

		bKeep.setEnabled(false);
		bReview.setEnabled(false);
		bEscalate.setEnabled(false);
		//		bExec.setEnabled(true);
		//		table3.setEnabled(true);
		mClose.setEnabled(true);
	}

	/**
	 * B review action performed.
	 */
	private void bReviewActionPerformed() {
		//		clearAndRemove();
	}



	/**
	 * B escalate action performed.
	 */
	private void bEscalateActionPerformed() {
		//		clearAndRemove();
	}

	/**
	 * Rb fsn action performed.
	 *
	 * @param e the e
	 */
	private void rbFSNActionPerformed(ActionEvent e) {
		updateSimilarityTable(sourceFSN);
		searchTextField.setText(sourceFSN);
	}

	/**
	 * Rb pref action performed.
	 *
	 * @param e the e
	 */
	private void rbPrefActionPerformed(ActionEvent e) {
		updateSimilarityTable(sourceFSN);
		searchTextField.setText(sourceFSN);
	}

	/**
	 * Radio button2 action performed.
	 *
	 * @param e the e
	 */
	private void radioButton2ActionPerformed(ActionEvent e) {
		updateSimilarityTable(sourceFSN);
		searchTextField.setText(sourceFSN);
	}

	/**
	 * Save comment.
	 *
	 * @param comment the comment
	 */
	private void saveComment(String comment) {
		I_ConfigAceFrame config=null ;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			CommentsRefset commRefset=targetLangRefset.getCommentsRefset(config);
			String fullName= config.getDbConfig().getFullName();
			commRefset.addComment(this.concept.getConceptNid(), role.toString() + HEADER_SEPARATOR + fullName + COMMENT_HEADER_SEP + comment);
			Terms.get().commit();

		} catch (TerminologyException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		getPreviousComments();
		getWebReferences();

		try {
			populateTargetTree();
		} catch (Exception e1) {
			
			e1.printStackTrace();
		}
	}

	/**
	 * Save desc action performed.
	 */
	private void saveDescActionPerformed() {

		I_ConfigAceFrame config=null ;	

		try {

			config = Terms.get().getActiveAceFrameConfig();
			ConfigTranslationModule confTransMod = LanguageUtil.getTranslationConfig(config);
			System.out.println(confTransMod.isEnableSpellChecker());
			if(confTransMod.isEnableSpellChecker()){
				textField1.setText(DocumentManager.spellcheckPhrase(textField1.getText(), null,targetLangRefset.getLangCode(config)));
			}

			if (descriptionInEditor == null && !textField1.getText().trim().equals("") && rbAct.isSelected() && !((I_GetConceptData)cmbAccep.getSelectedItem()).equals(notAcceptable)) {
				descriptionInEditor=(ContextualizedDescription) ContextualizedDescription.createNewContextualizedDescription(concept.getConceptNid(), targetId, targetLangRefset.getLangCode(config));

				//				if (((I_GetConceptData)comboBox1.getSelectedItem()).equals(notAcceptable)){
				//					
				//				}
			}
			if (descriptionInEditor != null ){ 
				descriptionInEditor.setText(textField1.getText());
				descriptionInEditor.setInitialCaseSignificant(rbYes.isSelected());

				//set description type like RF1
				if (((I_GetConceptData)comboBox1.getSelectedItem()).equals(description)){
					if ((((I_GetConceptData)cmbAccep.getSelectedItem()).equals(preferred))){
						descriptionInEditor.setTypeId(preferred.getConceptNid());
					}else if ((((I_GetConceptData)cmbAccep.getSelectedItem()).equals(acceptable))){
						descriptionInEditor.setTypeId(synonym.getConceptNid());
					}
				}else{
					descriptionInEditor.setTypeId(fsn.getConceptNid());
				}
				//if some is wrong then all to retire 
				if ((((I_GetConceptData)cmbAccep.getSelectedItem()).equals(notAcceptable)) || (rbInact.isSelected()) ){
					descriptionInEditor.setExtensionStatusId(inactive.getConceptNid());
					descriptionInEditor.setDescriptionStatusId(retired.getConceptNid());
					descriptionInEditor.setAcceptabilityId(notAcceptable.getConceptNid());

				}else{

					//if all current
					descriptionInEditor.setAcceptabilityId(((I_GetConceptData)cmbAccep.getSelectedItem()).getConceptNid());
					descriptionInEditor.setDescriptionStatusId(current.getConceptNid());
					descriptionInEditor.setExtensionStatusId(active.getConceptNid());
				}

				descriptionInEditor.persistChanges();
				ContextualizedDescription fsnDesc= (ContextualizedDescription) LanguageUtil.generateFSN(concept, sourceLangRefsets.iterator().next(), targetLangRefset, translationProject, config);
				if (fsnDesc!=null){
					fsnDesc.persistChanges();
				}
			}

			clearForm(false);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (Exception e1) {
			
			e1.printStackTrace();
		}

		try {
			populateTargetTree();
		} catch (Exception e1) {
			
			e1.printStackTrace();
		}
	}

	/**
	 * This ancestor removed.
	 */
	private void thisAncestorRemoved() {
		verifySavePending();
	}

	/**
	 * M hist action performed.
	 */
	private void mHistActionPerformed() {
		org.ihtsdo.project.panel.TranslationHelperPanel thp;
		try {
			thp = PanelHelperFactory.getTranslationHelperPanel();
			JTabbedPane tp=thp.getTabbedPanel();
			if (tp!=null){
				I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
				List<UUID> repoUUID=translConfig.getProjectIssuesRepositoryIds();
				IssueRepository repo=null;
				IssueRepoRegistration regis=null;
				WorklistMemberLogPanel wmlpanel=null;
				if (repoUUID!=null && repoUUID.size()>0){
					repo= IssueRepositoryDAO.getIssueRepository(Terms.get().getConcept(repoUUID)); 

					regis=IssueRepositoryDAO.getRepositoryRegistration(repo.getUuid(), config);
				}
				int tabCount=tp.getTabCount();
				for (int i=0;i<tabCount;i++){
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.CONCEPT_VERSIONS_TAB_NAME)){
						tp.setSelectedIndex(i);
						wmlpanel=(WorklistMemberLogPanel)tp.getComponentAt(i);
						wmlpanel.showMemberChanges(this.worklistMember,this.translationProject,repo, regis) ;
						thp.showTabbedPanel();
						return;
					}
				}
				wmlpanel = new WorklistMemberLogPanel();
				wmlpanel.showMemberChanges(this.worklistMember,this.translationProject,repo, regis) ;

				tp.addTab(TranslationHelperPanel.CONCEPT_VERSIONS_TAB_NAME, wmlpanel);
				tp.setSelectedIndex(tp.getTabCount()-1);
				thp.showTabbedPanel();
			}
		} catch (TerminologyException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

	/**
	 * B add coment action performed.
	 */
	private void bAddComentActionPerformed() {
		showNewCommentPanel();
	}

	/**
	 * Show new comment panel.
	 */
	public void showNewCommentPanel() {

		NewCommentPanel cPanel;
		cPanel = new NewCommentPanel();


		int action =
			JOptionPane.showOptionDialog(null, cPanel, "Enter new comment",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

		this.requestFocus();

		if (action == JOptionPane.CANCEL_OPTION) {
			return ;
		} 
		if (cPanel.getNewComment().trim().equals("")){
			message("Cannot add a blank comment.");
			return;
		} 
		saveComment(cPanel.getNewComment().trim());
	}

	/**
	 * Message.
	 * 
	 * @param string the string
	 */
	private void message(String string) {

         JOptionPane.showOptionDialog(   
        		this,   
                string,   
                "Information", JOptionPane.DEFAULT_OPTION,   
                JOptionPane.INFORMATION_MESSAGE, null, null,   
                null );   
	}


	
	

	/**
	 * Ref table hyperlink update.
	 *
	 * @param hle the hle
	 */
	private void refTableHyperlinkUpdate(HyperlinkEvent hle) {
		if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {  
			System.out.println("Opening: " + hle.getURL());  
			System.out.println("Path: " +  hle.getURL().getHost() + hle.getURL().getPath());  
			try {
				Desktop desktop = null;
				if (Desktop.isDesktopSupported()) {
					desktop = Desktop.getDesktop();
//						String absoluteUrl = hle.getURL().getProtocol() + "://" + new File(".").getAbsolutePath();
//						absoluteUrl = absoluteUrl.substring(0, absoluteUrl.length() -1);
//						absoluteUrl = absoluteUrl + hle.getURL().getHost().replace(" ", "%20");
//						absoluteUrl = absoluteUrl + hle.getURL().getPath().replace(" ", "%20");
//						absoluteUrl = absoluteUrl.trim() + "#search=" + queryField.getText().trim().replace(" ", "%20") + "";
						
//						System.out.println("URL: " + absoluteUrl);
						desktop.browse(new URI(hle.getURL().toString()));
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}

		}  
	}

	/**
	 * Tbl comm mouse clicked.
	 *
	 * @param e the e
	 */
	private void tblCommMouseClicked(MouseEvent e) {
		if (e.getClickCount()==2){
			viewComment();
		}
	}

	/**
	 * View comment.
	 */
	private void viewComment() {
		int row=tblComm.getSelectedRow();
		if (row>-1){
			CommentPanel cp=new CommentPanel();
			String comm=(String)tblComm.getValueAt(row, 0);
			comm=comm.replace(htmlHeader,"");
			comm=comm.replace(htmlFooter, "");
			comm=comm.replace(endP, "");
			String[] arrComm=comm.split(COMMENT_HEADER_SEP);
			String header=arrComm[0];
			String[] headerComp=header.split(HEADER_SEPARATOR);
			String from="";
			String role="";
			String date="";
			String source="";
			if (headerComp.length>0){
				source=headerComp[0];
				if (headerComp.length>1){
					date=headerComp[1];
					if (headerComp.length>2){
						role=headerComp[2];
						if (headerComp.length>3){
							from=headerComp[3];
						}else{
							from=headerComp[2];
						}	
					}else{
						from=headerComp[0];
					}	
				}else{
					from=headerComp[0];
				}
			}
//			int sepLen=HEADER_SEPARATOR.length();
//			if (header.length()>0){
//				int toIndex=0;
//				toIndex=header.indexOf(HEADER_SEPARATOR, toIndex);
//				if (toIndex>-1){
//					source= getTextFromHeader(header,toIndex);
//					toIndex= header.indexOf(HEADER_SEPARATOR, toIndex + sepLen);
//					if (toIndex>-1){
//						date= getTextFromHeader(header,toIndex);
//						from=header.substring(toIndex + sepLen);
//						toIndex= header.indexOf(HEADER_SEPARATOR, toIndex +sepLen);
//						if (toIndex>-1){
//							role= getTextFromHeader(header,toIndex);
//							toIndex= header.indexOf(HEADER_SEPARATOR, toIndex + sepLen);
//							from=header.substring(toIndex + sepLen);
//						}
//					}
//				}
//			}else{
//				from =header;
//			}
//			
			cp.setFrom(from);
			cp.setRole(role);
			cp.setDate(date);
			cp.setSource(source);
			int index=comm.indexOf(COMMENT_HEADER_SEP);
			cp.setComment(comm.substring(index + COMMENT_HEADER_SEP.length()));
	
			JOptionPane.showMessageDialog (null, cp, "Comment",
						 JOptionPane.INFORMATION_MESSAGE);

			this.requestFocus();

		}
		
	}

	/**
	 * Gets the text from header.
	 *
	 * @param header the header
	 * @param toIndex the to index
	 * @return the text from header
	 */
	private String getTextFromHeader(String header, int toIndex) {
		
		String tmp=header.substring(0,toIndex);
		int lastInd=tmp.lastIndexOf(HEADER_SEPARATOR);
		return tmp.substring(lastInd + HEADER_SEPARATOR.length());
		
		
	}

	/**
	 * Expand button action performed.
	 *
	 * @param e the e
	 */
	private void expandButtonActionPerformed(ActionEvent e) {
		JPanel similarityPanel = (JPanel)tabbedPane1.getSelectedComponent();
		expandButton.setVisible(false);
		final JDialog similarityDialog = new JDialog();
		similarityDialog.setContentPane(similarityPanel);
		similarityDialog.setModal(true);
		similarityDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		similarityDialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				expandButton.setVisible(true);
				tabbedPane1.insertTab("Similarity", null, similarityDialog.getContentPane(), "Similarity", 0);
				tabbedPane1.setSelectedIndex(0);
				similarityDialog.dispose();
			}
		});
		similarityDialog.setSize(new Dimension(550,400));
		Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension ventana = similarityDialog.getSize();
        similarityDialog.setLocation(
            (pantalla.width - ventana.width) / 2,
            (pantalla.height - ventana.height) / 2);

		similarityDialog.setVisible(true);
	}


	/**
	 * The listener interface for receiving selection events.
	 * The class that is interested in processing a selection
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addSelectionListener<code> method. When
	 * the selection event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see SelectionEvent
	 */
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
					int rowModel=table.convertRowIndexToModel(first);
					ContextualizedDescription descrpt=(ContextualizedDescription) table.getModel().getValueAt(rowModel, TableTargetColumn.TERM.ordinal());
					
					if (descrpt!=null && !setByCode){
						updatePropertiesPanel( descrpt,rowModel);
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
		mClose = new JMenuItem();
		menu1 = new JMenu();
		bAddFSN = new JMenuItem();
		mAddPref = new JMenuItem();
		mAddDesc = new JMenuItem();
		menu3 = new JMenu();
		mSpellChk = new JMenuItem();
		menu2 = new JMenu();
		mHist = new JMenuItem();
		panel10 = new JPanel();
		splitPane3 = new JSplitPane();
		splitPane2 = new JSplitPane();
		panel9 = new JPanel();
		label9 = new JLabel();
		scrollPane1 = new JScrollPane();
		tabSou = new ZebraJTable();
		tabbedPane3 = new JTabbedPane();
		scrollPane7 = new JScrollPane();
		tree3 = new JTree();
		hierarchyNavigator1 = new HierarchyNavigator();
		panel8 = new JPanel();
		label11 = new JLabel();
		scrollPane6 = new JScrollPane();
		tabTar = new ZebraJTable();
		panel2 = new JPanel();
		label2 = new JLabel();
		scrollPane5 = new JScrollPane();
		textField1 = new JTextArea();
		label1 = new JLabel();
		panel7 = new JPanel();
		label4 = new JLabel();
		comboBox1 = new JComboBox();
		panel5 = new JPanel();
		label5 = new JLabel();
		cmbAccep = new JComboBox();
		label3 = new JLabel();
		panel4 = new JPanel();
		rbYes = new JRadioButton();
		label6 = new JLabel();
		rbNo = new JRadioButton();
		panel14 = new JPanel();
		label7 = new JLabel();
		rbAct = new JRadioButton();
		rbInact = new JRadioButton();
		panel3 = new JPanel();
		saveDesc = new JButton();
		button5 = new JButton();
		panel6 = new JPanel();
		buttonPanel = new JPanel();
		label8 = new JLabel();
		bKeep = new JButton();
		bReview = new JButton();
		bEscalate = new JButton();
		splitPane1 = new JSplitPane();
		tabbedPane1 = new JTabbedPane();
		panel12 = new JPanel();
		refinePanel = new JPanel();
		searchTextField = new JTextField();
		searchButton = new JButton();
		scrollPane2 = new JScrollPane();
		table1 = new ZebraJTable();
		panel13 = new JPanel();
		rbFSN = new JRadioButton();
		rbPref = new JRadioButton();
		radioButton2 = new JRadioButton();
		refineCheckBox = new JCheckBox();
		expandButton = new JButton();
		scrollPane3 = new JScrollPane();
		table2 = new ZebraJTable();
		panel15 = new JPanel();
		scrollPane4 = new JScrollPane();
		editorPane1 = new JEditorPane();
		tabbedPane2 = new JTabbedPane();
		panel16 = new JPanel();
		panel17 = new JPanel();
		bAddComent = new JButton();
		scrollPane9 = new JScrollPane();
		tblComm = new ZebraJTable();
		scrollPane8 = new JScrollPane();
		refTable = new JEditorPane();
		panel11 = new JPanel();

		//======== this ========
		setBackground(new Color(238, 238, 238));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {35, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

		//======== menuBar1 ========
		{

			//======== menu5 ========
			{
				menu5.setText("Translation");
				menu5.setFont(new Font("Verdana", Font.PLAIN, 14));

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
				bAddFSN.setMnemonic('F');
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
				mAddPref.setMnemonic('P');
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
				mAddDesc.setMnemonic('D');
				mAddDesc.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mAddDescActionPerformed();
					}
				});
				menu1.add(mAddDesc);
			}
			menuBar1.add(menu1);

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

			//======== menu2 ========
			{
				menu2.setText("View");

				//---- mHist ----
				mHist.setText("History");
				mHist.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mHistActionPerformed();
					}
				});
				menu2.add(mHist);
			}
			menuBar1.add(menu2);
		}
		add(menuBar1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

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
					splitPane2.setBackground(new Color(238, 238, 238));
					splitPane2.setResizeWeight(1.0);

					//======== panel9 ========
					{
						panel9.setBackground(new Color(238, 238, 238));
						panel9.setLayout(new GridBagLayout());
						((GridBagLayout)panel9.getLayout()).columnWidths = new int[] {0, 0};
						((GridBagLayout)panel9.getLayout()).rowHeights = new int[] {20, 0, 0, 0};
						((GridBagLayout)panel9.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
						((GridBagLayout)panel9.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0, 1.0E-4};

						//---- label9 ----
						label9.setText("Source Language");
						panel9.add(label9, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//======== scrollPane1 ========
						{

							//---- tabSou ----
							tabSou.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
							tabSou.setBorder(LineBorder.createBlackLineBorder());
							tabSou.setAutoCreateRowSorter(true);
							scrollPane1.setViewportView(tabSou);
						}
						panel9.add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//======== tabbedPane3 ========
						{

							//======== scrollPane7 ========
							{

								//---- tree3 ----
								tree3.setVisibleRowCount(4);
								scrollPane7.setViewportView(tree3);
							}
							tabbedPane3.addTab("Concept Details", scrollPane7);

							tabbedPane3.addTab("Hierarchy", hierarchyNavigator1);
							tabbedPane3.setMnemonicAt(1, 'H');
						}
						panel9.add(tabbedPane3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					splitPane2.setLeftComponent(panel9);

					//======== panel8 ========
					{
						panel8.setBackground(new Color(238, 238, 238));
						panel8.setLayout(new GridBagLayout());
						((GridBagLayout)panel8.getLayout()).columnWidths = new int[] {0, 0};
						((GridBagLayout)panel8.getLayout()).rowHeights = new int[] {0, 105, 0, 34, 0};
						((GridBagLayout)panel8.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
						((GridBagLayout)panel8.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 0.0, 1.0E-4};

						//---- label11 ----
						label11.setText("Target Language");
						label11.setBackground(new Color(238, 238, 238));
						panel8.add(label11, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//======== scrollPane6 ========
						{

							//---- tabTar ----
							tabTar.setAutoCreateRowSorter(true);
							tabTar.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
							tabTar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							tabTar.setBorder(LineBorder.createBlackLineBorder());
							scrollPane6.setViewportView(tabTar);
						}
						panel8.add(scrollPane6, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//======== panel2 ========
						{
							panel2.setBorder(LineBorder.createBlackLineBorder());
							panel2.setBackground(new Color(238, 238, 238));
							panel2.setLayout(new GridBagLayout());
							((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 5, 0};
							((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {13, 26, 23, 23, 33, 0, 0, 0};
							((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 1.0, 0.0, 1.0E-4};
							((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

							//---- label2 ----
							label2.setText("Term:");
							label2.setFont(new Font("Verdana", Font.PLAIN, 13));
							panel2.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
								new Insets(0, 10, 5, 5), 0, 0));

							//======== scrollPane5 ========
							{

								//---- textField1 ----
								textField1.setRows(2);
								scrollPane5.setViewportView(textField1);
							}
							panel2.add(scrollPane5, new GridBagConstraints(1, 1, 3, 3, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 5), 0, 0));

							//---- label1 ----
							label1.setText("Term Type:");
							label1.setFont(new Font("Verdana", Font.PLAIN, 13));
							panel2.add(label1, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
								new Insets(0, 10, 5, 5), 0, 0));

							//======== panel7 ========
							{
								panel7.setBackground(new Color(238, 238, 238));
								panel7.setLayout(new FlowLayout(FlowLayout.LEFT));
								panel7.add(label4);
								panel7.add(comboBox1);
							}
							panel2.add(panel7, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
								new Insets(0, 0, 5, 5), 0, 0));

							//======== panel5 ========
							{
								panel5.setBackground(new Color(238, 238, 238));
								panel5.setLayout(new GridBagLayout());
								((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {15, 0, 0, 0};
								((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0};
								((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
								((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

								//---- label5 ----
								label5.setText("Acceptability:");
								panel5.add(label5, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 0, 5), 0, 0));
								panel5.add(cmbAccep, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 0, 0), 0, 0));
							}
							panel2.add(panel5, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
								new Insets(0, 0, 5, 5), 0, 0));

							//---- label3 ----
							label3.setText("Is case significant ?");
							label3.setFont(new Font("Verdana", Font.PLAIN, 13));
							panel2.add(label3, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
								new Insets(0, 10, 5, 5), 0, 0));

							//======== panel4 ========
							{
								panel4.setBackground(new Color(238, 238, 238));
								panel4.setLayout(new GridBagLayout());
								((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0};
								((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0};
								((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
								((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

								//---- rbYes ----
								rbYes.setText("Yes");
								rbYes.setBackground(new Color(238, 238, 238));
								panel4.add(rbYes, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
									new Insets(0, 0, 0, 5), 0, 0));

								//---- label6 ----
								label6.setText("    ");
								panel4.add(label6, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
									new Insets(0, 0, 0, 5), 0, 0));

								//---- rbNo ----
								rbNo.setSelected(true);
								rbNo.setText("No");
								rbNo.setBackground(new Color(238, 238, 238));
								panel4.add(rbNo, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
									new Insets(0, 0, 0, 0), 0, 0));
							}
							panel2.add(panel4, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
								new Insets(0, 0, 5, 5), 0, 0));

							//======== panel14 ========
							{
								panel14.setBackground(new Color(238, 238, 238));
								panel14.setLayout(new GridBagLayout());
								((GridBagLayout)panel14.getLayout()).columnWidths = new int[] {15, 0, 0, 0, 0, 0, 0};
								((GridBagLayout)panel14.getLayout()).rowHeights = new int[] {0, 0};
								((GridBagLayout)panel14.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
								((GridBagLayout)panel14.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

								//---- label7 ----
								label7.setText("Status:");
								panel14.add(label7, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
									new Insets(0, 0, 0, 5), 0, 0));

								//---- rbAct ----
								rbAct.setText("Active");
								rbAct.setSelected(true);
								rbAct.setBackground(new Color(238, 238, 238));
								panel14.add(rbAct, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
									new Insets(0, 0, 0, 5), 0, 0));

								//---- rbInact ----
								rbInact.setText("Inactive");
								rbInact.setBackground(new Color(238, 238, 238));
								panel14.add(rbInact, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
									new Insets(0, 0, 0, 0), 0, 0));
							}
							panel2.add(panel14, new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
								new Insets(0, 0, 5, 5), 0, 0));

							//======== panel3 ========
							{
								panel3.setBackground(new Color(238, 238, 238));
								panel3.setLayout(new GridBagLayout());
								((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
								((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
								((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
								((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

								//---- saveDesc ----
								saveDesc.setText("Save");
								saveDesc.setFont(new Font("Verdana", Font.PLAIN, 13));
								saveDesc.setMnemonic('A');
								saveDesc.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										saveDescActionPerformed();
									}
								});
								panel3.add(saveDesc, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 0, 5), 0, 0));

								//---- button5 ----
								button5.setText("Cancel");
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
							panel2.add(panel3, new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0,
								GridBagConstraints.EAST, GridBagConstraints.NONE,
								new Insets(0, 0, 0, 5), 0, 0));
						}
						panel8.add(panel2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//======== panel6 ========
						{
							panel6.setBorder(LineBorder.createBlackLineBorder());
							panel6.setLayout(new BoxLayout(panel6, BoxLayout.Y_AXIS));

							//======== buttonPanel ========
							{
								buttonPanel.setBackground(new Color(238, 238, 238));
								buttonPanel.setLayout(new GridBagLayout());
								((GridBagLayout)buttonPanel.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
								((GridBagLayout)buttonPanel.getLayout()).rowHeights = new int[] {0, 0};
								((GridBagLayout)buttonPanel.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
								((GridBagLayout)buttonPanel.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

								//---- label8 ----
								label8.setText("Workflow actions:      ");
								buttonPanel.add(label8, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
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
								buttonPanel.add(bKeep, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
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
								buttonPanel.add(bReview, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
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
								buttonPanel.add(bEscalate, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
									new Insets(0, 0, 0, 0), 0, 0));
							}
							panel6.add(buttonPanel);
						}
						panel8.add(panel6, new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					splitPane2.setRightComponent(panel8);
				}
				splitPane3.setTopComponent(splitPane2);

				//======== splitPane1 ========
				{
					splitPane1.setBackground(new Color(238, 238, 238));
					splitPane1.setResizeWeight(1.0);
					splitPane1.setToolTipText("Drag to resize");

					//======== tabbedPane1 ========
					{
						tabbedPane1.setFont(new Font("Verdana", Font.PLAIN, 13));

						//======== panel12 ========
						{
							panel12.setBackground(new Color(238, 238, 238));
							panel12.setLayout(new GridBagLayout());
							((GridBagLayout)panel12.getLayout()).columnWidths = new int[] {0, 0};
							((GridBagLayout)panel12.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
							((GridBagLayout)panel12.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
							((GridBagLayout)panel12.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

							//======== refinePanel ========
							{
								refinePanel.setBackground(new Color(238, 238, 238));
								refinePanel.setLayout(new GridBagLayout());
								((GridBagLayout)refinePanel.getLayout()).columnWidths = new int[] {233, 0, 68, 0};
								((GridBagLayout)refinePanel.getLayout()).rowHeights = new int[] {0, 0};
								((GridBagLayout)refinePanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 1.0E-4};
								((GridBagLayout)refinePanel.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
								refinePanel.setVisible(false);
								refinePanel.add(searchTextField, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 0, 5), 0, 0));

								//---- searchButton ----
								searchButton.setAction(null);
								searchButton.setText("Search");
								refinePanel.add(searchButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 0, 5), 0, 0));
							}
							panel12.add(refinePanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 0), 0, 0));

							//======== scrollPane2 ========
							{

								//---- table1 ----
								table1.setPreferredScrollableViewportSize(new Dimension(180, 200));
								table1.setBackground(new Color(239, 235, 222));
								table1.setFont(new Font("Verdana", Font.PLAIN, 12));
								scrollPane2.setViewportView(table1);
							}
							panel12.add(scrollPane2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 0), 0, 0));

							//======== panel13 ========
							{
								panel13.setBackground(new Color(238, 238, 238));
								panel13.setLayout(new GridBagLayout());
								((GridBagLayout)panel13.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0};
								((GridBagLayout)panel13.getLayout()).rowHeights = new int[] {0, 0};
								((GridBagLayout)panel13.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};
								((GridBagLayout)panel13.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

								//---- rbFSN ----
								rbFSN.setText("FSN");
								rbFSN.setSelected(true);
								rbFSN.setBackground(new Color(238, 238, 238));
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
								rbPref.setBackground(new Color(238, 238, 238));
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
								radioButton2.setBackground(new Color(238, 238, 238));
								radioButton2.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										radioButton2ActionPerformed(e);
									}
								});
								panel13.add(radioButton2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 0, 5), 0, 0));

								//---- refineCheckBox ----
								refineCheckBox.setText("Refine");
								panel13.add(refineCheckBox, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 0, 5), 0, 0));

								//---- expandButton ----
								expandButton.setText("Expand");
								expandButton.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										expandButtonActionPerformed(e);
									}
								});
								panel13.add(expandButton, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
									new Insets(0, 0, 0, 0), 0, 0));
							}
							panel12.add(panel13, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 0, 0), 0, 0));
						}
						tabbedPane1.addTab("Similarity", panel12);
						tabbedPane1.setMnemonicAt(0, 'L');

						//======== scrollPane3 ========
						{

							//---- table2 ----
							table2.setPreferredScrollableViewportSize(new Dimension(180, 200));
							table2.setFont(new Font("Verdana", Font.PLAIN, 12));
							scrollPane3.setViewportView(table2);
						}
						tabbedPane1.addTab("Translation Memory", scrollPane3);
						tabbedPane1.setMnemonicAt(1, 'M');

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
						tabbedPane1.setMnemonicAt(2, 'G');
					}
					splitPane1.setLeftComponent(tabbedPane1);

					//======== tabbedPane2 ========
					{

						//======== panel16 ========
						{
							panel16.setLayout(new GridBagLayout());
							((GridBagLayout)panel16.getLayout()).columnWidths = new int[] {0, 0};
							((GridBagLayout)panel16.getLayout()).rowHeights = new int[] {0, 0, 0};
							((GridBagLayout)panel16.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
							((GridBagLayout)panel16.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

							//======== panel17 ========
							{
								panel17.setLayout(new GridBagLayout());
								((GridBagLayout)panel17.getLayout()).columnWidths = new int[] {0, 0, 0};
								((GridBagLayout)panel17.getLayout()).rowHeights = new int[] {0, 0};
								((GridBagLayout)panel17.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
								((GridBagLayout)panel17.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

								//---- bAddComent ----
								bAddComent.setText("Add Comment");
								bAddComent.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										bAddComentActionPerformed();
									}
								});
								panel17.add(bAddComent, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 0, 5), 0, 0));
							}
							panel16.add(panel17, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 0), 0, 0));

							//======== scrollPane9 ========
							{

								//---- tblComm ----
								tblComm.addMouseListener(new MouseAdapter() {
									@Override
									public void mouseClicked(MouseEvent e) {
										tblCommMouseClicked(e);
									}
								});
								scrollPane9.setViewportView(tblComm);
							}
							panel16.add(scrollPane9, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 0, 0), 0, 0));
						}
						tabbedPane2.addTab("Comments", panel16);


						//======== scrollPane8 ========
						{

							//---- refTable ----
							refTable.setEditable(false);
							refTable.addHyperlinkListener(new HyperlinkListener() {
								@Override
								public void hyperlinkUpdate(HyperlinkEvent e) {
									refTableHyperlinkUpdate(e);
								}
							});
							scrollPane8.setViewportView(refTable);
						}
						tabbedPane2.addTab("Web references", scrollPane8);


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
						tabbedPane2.setMnemonicAt(2, 'U');
					}
					splitPane1.setRightComponent(tabbedPane2);
				}
				splitPane3.setBottomComponent(splitPane1);
			}
			panel10.add(splitPane3, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel10, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(rbYes);
		buttonGroup1.add(rbNo);

		//---- buttonGroup3 ----
		ButtonGroup buttonGroup3 = new ButtonGroup();
		buttonGroup3.add(rbAct);
		buttonGroup3.add(rbInact);

		//---- buttonGroup2 ----
		ButtonGroup buttonGroup2 = new ButtonGroup();
		buttonGroup2.add(rbFSN);
		buttonGroup2.add(rbPref);
		buttonGroup2.add(radioButton2);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	/** The menu bar1. */
	private JMenuBar menuBar1;
	
	/** The menu5. */
	private JMenu menu5;
	
	/** The m close. */
	private JMenuItem mClose;
	
	/** The menu1. */
	private JMenu menu1;
	
	/** The b add fsn. */
	private JMenuItem bAddFSN;
	
	/** The m add pref. */
	private JMenuItem mAddPref;
	
	/** The m add desc. */
	private JMenuItem mAddDesc;
	
	/** The menu3. */
	private JMenu menu3;
	
	/** The m spell chk. */
	private JMenuItem mSpellChk;
	
	/** The menu2. */
	private JMenu menu2;
	
	/** The m hist. */
	private JMenuItem mHist;
	
	/** The panel10. */
	private JPanel panel10;
	
	/** The split pane3. */
	private JSplitPane splitPane3;
	
	/** The split pane2. */
	private JSplitPane splitPane2;
	
	/** The panel9. */
	private JPanel panel9;
	
	/** The label9. */
	private JLabel label9;
	
	/** The scroll pane1. */
	private JScrollPane scrollPane1;
	
	/** The tab sou. */
	private ZebraJTable tabSou;
	
	/** The tabbed pane3. */
	private JTabbedPane tabbedPane3;
	
	/** The scroll pane7. */
	private JScrollPane scrollPane7;
	
	/** The tree3. */
	private JTree tree3;
	
	/** The hierarchy navigator1. */
	private HierarchyNavigator hierarchyNavigator1;
	
	/** The panel8. */
	private JPanel panel8;
	
	/** The label11. */
	private JLabel label11;
	
	/** The scroll pane6. */
	private JScrollPane scrollPane6;
	
	/** The tab tar. */
	private ZebraJTable tabTar;
	
	/** The panel2. */
	private JPanel panel2;
	
	/** The label2. */
	private JLabel label2;
	
	/** The scroll pane5. */
	private JScrollPane scrollPane5;
	
	/** The text field1. */
	private JTextArea textField1;
	
	/** The label1. */
	private JLabel label1;
	
	/** The panel7. */
	private JPanel panel7;
	
	/** The label4. */
	private JLabel label4;
	
	/** The combo box1. */
	private JComboBox comboBox1;
	
	/** The panel5. */
	private JPanel panel5;
	
	/** The label5. */
	private JLabel label5;
	
	/** The cmb accep. */
	private JComboBox cmbAccep;
	
	/** The label3. */
	private JLabel label3;
	
	/** The panel4. */
	private JPanel panel4;
	
	/** The rb yes. */
	private JRadioButton rbYes;
	
	/** The label6. */
	private JLabel label6;
	
	/** The rb no. */
	private JRadioButton rbNo;
	
	/** The panel14. */
	private JPanel panel14;
	
	/** The label7. */
	private JLabel label7;
	
	/** The rb act. */
	private JRadioButton rbAct;
	
	/** The rb inact. */
	private JRadioButton rbInact;
	
	/** The panel3. */
	private JPanel panel3;
	
	/** The save desc. */
	private JButton saveDesc;
	
	/** The button5. */
	private JButton button5;
	
	/** The panel6. */
	private JPanel panel6;
	
	/** The button panel. */
	private JPanel buttonPanel;
	
	/** The label8. */
	private JLabel label8;
	
	/** The b keep. */
	private JButton bKeep;
	
	/** The b review. */
	private JButton bReview;
	
	/** The b escalate. */
	private JButton bEscalate;
	
	/** The split pane1. */
	private JSplitPane splitPane1;
	
	/** The tabbed pane1. */
	private JTabbedPane tabbedPane1;
	
	/** The panel12. */
	private JPanel panel12;
	
	/** The refine panel. */
	private JPanel refinePanel;
	
	/** The search text field. */
	private JTextField searchTextField;
	
	/** The search button. */
	private JButton searchButton;
	
	/** The scroll pane2. */
	private JScrollPane scrollPane2;
	
	/** The table1. */
	private ZebraJTable table1;
	
	/** The panel13. */
	private JPanel panel13;
	
	/** The rb fsn. */
	private JRadioButton rbFSN;
	
	/** The rb pref. */
	private JRadioButton rbPref;
	
	/** The radio button2. */
	private JRadioButton radioButton2;
	
	/** The refine check box. */
	private JCheckBox refineCheckBox;
	
	/** The expand button. */
	private JButton expandButton;
	
	/** The scroll pane3. */
	private JScrollPane scrollPane3;
	
	/** The table2. */
	private ZebraJTable table2;
	
	/** The panel15. */
	private JPanel panel15;
	
	/** The scroll pane4. */
	private JScrollPane scrollPane4;
	
	/** The editor pane1. */
	private JEditorPane editorPane1;
	
	/** The tabbed pane2. */
	private JTabbedPane tabbedPane2;
	
	/** The panel16. */
	private JPanel panel16;
	
	/** The panel17. */
	private JPanel panel17;
	
	/** The b add coment. */
	private JButton bAddComent;
	
	/** The scroll pane9. */
	private JScrollPane scrollPane9;
	
	/** The tbl comm. */
	private ZebraJTable tblComm;
	
	/** The scroll pane8. */
	private JScrollPane scrollPane8;
	
	/** The ref table. */
	private JEditorPane refTable;
	
	/** The panel11. */
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
	
	/** The defining char. */
	private int definingChar=-1;
	
	/** The source fsn. */
	private String sourceFSN;
	
	/** The worklist member. */
	private WorkListMember worklistMember;
	
	/** The set by code. */
	private boolean setByCode;
	
	/** The source ics. */
	private boolean sourceICS;
	
	/** The keep ii class. */
	private I_KeepTaskInInbox keepIIClass;
	
	/** The unloaded. */
	private boolean unloaded;
	
	/** The role. */
	private I_GetConceptData role;
	
	/** The editing row. */
	private Integer editingRow;
	
	/** The target preferred row. */
	private Integer targetPreferredRow;
	
	/** The html footer. */
	private String htmlFooter="</body></html>";
	
	/** The html header. */
	private String htmlHeader="<html><body><font style='color:blue'>";
	
	/** The end p. */
	private String endP= "</font>";
	
	/** The scrollp. */
	private JScrollPane scrollp;

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
	 *
	 * @throws Exception the exception
	 */
	private void populateSourceTree() throws Exception {
//		DefaultMutableTreeNode top = null;
		//		translConfig=LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
		int maxTermWidth=0;
		LinkedHashSet<TreeComponent> sourceCom = translConfig.getSourceTreeComponents();
		Object[][] data=null;
		String[] columnNames = new String[TableSourceColumn.values().length];
		for (int i=0;i<TableSourceColumn.values().length;i++){
			columnNames[i]=TableSourceColumn.values()[i].getColumnName();
		}
		
		DefaultTableModel model=new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};
		List<Object[]> tmpRows=new ArrayList<Object[]>();
		if (concept != null) {
			try {
//				top = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toString(), TreeEditorObjectWrapper.CONCEPT, concept));
				for (I_GetConceptData langRefset: this.translationProject.getSourceLanguageRefsets()){
					List<ContextualizedDescription> descriptions = LanguageUtil.getContextualizedDescriptions(
							concept.getConceptNid(), langRefset.getConceptNid(), true);

					
//					DefaultMutableTreeNode groupLang = new DefaultMutableTreeNode(
//							new TreeEditorObjectWrapper(langRefset.getInitialText(), TreeEditorObjectWrapper.FOLDER,langRefset ));

//					List<DefaultMutableTreeNode> nodesToAdd = new ArrayList<DefaultMutableTreeNode>();

					boolean bSourceFSN = false;
					boolean bNewNode=false;
					for (I_ContextualizeDescription description : descriptions) {
						if (description.getLanguageExtension()!=null && description.getLanguageRefsetId()==langRefset.getConceptNid()){
							bNewNode=false;
							Object[] rowClass=new Object[2];
							Object[] row=new Object[TableSourceColumn.values().length];
							Object[] termType_Status=new Object[2];
							row[TableSourceColumn.TERM.ordinal()]=description;
							row[TableSourceColumn.LANGUAGE.ordinal()]=description.getLang();
							row[TableSourceColumn.ICS.ordinal()]=description.isInitialCaseSignificant();
							
							if (description.getAcceptabilityId()==notAcceptable.getConceptNid() ||
									description.getExtensionStatusId()==inactive.getConceptNid() ||
									description.getDescriptionStatusId()==retired.getConceptNid()){
								if ( sourceCom.contains(ConfigTranslationModule.TreeComponent.RETIRED)){
									rowClass[0]=TreeEditorObjectWrapper.NOTACCEPTABLE;
									row[TableSourceColumn.ACCEPTABILITY.ordinal()]=notAcceptable;
//									row[TableSourceColumn.STATUS.ordinal()]=inactive;
									termType_Status[1]=inactive;
									if (description.getTypeId() == fsn.getConceptNid()){ 
//										row[TableSourceColumn.TERM_TYPE.ordinal()]=fsn;
										termType_Status[0]=fsn;
									}else{
//										row[TableSourceColumn.TERM_TYPE.ordinal()]=this.description;
										termType_Status[0]=this.description;
									}
									row[TableSourceColumn.TERM_TYPE.ordinal()]=termType_Status;
									bNewNode=true;
								}
							} else if (description.getTypeId() == fsn.getConceptNid()) {
								if ( sourceCom.contains(ConfigTranslationModule.TreeComponent.FSN)){
									rowClass[0]=TreeEditorObjectWrapper.FSNDESCRIPTION;
									row[TableSourceColumn.ACCEPTABILITY.ordinal()]=preferred;
									termType_Status[0]=fsn;
									termType_Status[1]=active;
									row[TableSourceColumn.TERM_TYPE.ordinal()]=termType_Status;
									
									bNewNode=true;
								}
								int semtagLocation = description.getText().lastIndexOf("(");
								if (semtagLocation == -1) semtagLocation = description.getText().length();

								int endParenthesis=description.getText().lastIndexOf(")");

								if (semtagLocation > -1 && semtagLocation<endParenthesis )
									sourceSemTag=description.getText().substring( semtagLocation + 1,endParenthesis);
								//saveDesc.setEnabled(true);
								if (!bSourceFSN){
									bSourceFSN=true;
									sourceFSN=description.getText().substring(0, semtagLocation);
//									Runnable simil = new Runnable() {
//										public void run() {
											updateSimilarityTable(sourceFSN);
//										}
//									};
//									simil.run();
//									Runnable tMemo = new Runnable() {
//										public void run() {
											updateTransMemoryTable(sourceFSN);
//										}
//									};
//									tMemo.run();
//									Runnable gloss = new Runnable() {
//										public void run() {
//											//											updateGlossaryEnforcement(sourceFSN);
//										}
//									};
//									gloss.run();
								}
							} else if (description.getAcceptabilityId() == acceptable.getConceptNid()
									&& sourceCom.contains(ConfigTranslationModule.TreeComponent.SYNONYM)) {
								rowClass[0]=TreeEditorObjectWrapper.SYNONYMN;
								row[TableSourceColumn.ACCEPTABILITY.ordinal()]=acceptable;
								termType_Status[0]=this.description;
								termType_Status[1]=active;
								row[TableSourceColumn.TERM_TYPE.ordinal()]=termType_Status;
								bNewNode=true;
							} else if (description.getAcceptabilityId() == preferred.getConceptNid()
									&& sourceCom.contains(ConfigTranslationModule.TreeComponent.PREFERRED)) {
								rowClass[0]=TreeEditorObjectWrapper.PREFERRED;
								row[TableSourceColumn.ACCEPTABILITY.ordinal()]=preferred;
								termType_Status[0]=this.description;
								termType_Status[1]=active;
								row[TableSourceColumn.TERM_TYPE.ordinal()]=termType_Status;
								bNewNode=true;
							}else if ( sourceCom.contains(ConfigTranslationModule.TreeComponent.RETIRED)){
								rowClass[0]=TreeEditorObjectWrapper.SYNONYMN;
								row[TableSourceColumn.ACCEPTABILITY.ordinal()]=notAcceptable;
								termType_Status[0]=this.description;
								termType_Status[1]=inactive;
								row[TableSourceColumn.TERM_TYPE.ordinal()]=termType_Status;
								bNewNode=true;
							}
							if (bNewNode){
								rowClass[1]=row;
								tmpRows.add(rowClass);
							}
						}
					}
					Font fontTmp = tabSou.getFont();
					FontMetrics fontMetrics=new FontMetrics(fontTmp){};
					Rectangle2D bounds;
					for (ConfigTranslationModule.TreeComponent tComp:sourceCom){

						switch (tComp){
						case FSN:
							for (Object[] rowClass : tmpRows) {
								if ((Integer)rowClass[0] == TreeEditorObjectWrapper.FSNDESCRIPTION) {
									Object[] row=(Object[]) rowClass[1];
									bounds = fontMetrics.getStringBounds(row[TableSourceColumn.TERM.ordinal()].toString(), null);  
							        int tmpWidth = (int) bounds.getWidth();  
									if (maxTermWidth< tmpWidth){
										maxTermWidth=tmpWidth;
									}
									model.addRow(row);
								}
							}
							break;
						case PREFERRED:

							for (Object[] rowClass : tmpRows) {
								if ((Integer)rowClass[0] == TreeEditorObjectWrapper.PREFERRED) {
									Object[] row=(Object[]) rowClass[1];
									bounds = fontMetrics.getStringBounds(row[TableSourceColumn.TERM.ordinal()].toString(), null);  
							        int tmpWidth = (int) bounds.getWidth();  
									if (maxTermWidth< tmpWidth){
										maxTermWidth=tmpWidth;
									}
									model.addRow(row);
								}
							}
							break;
						case SYNONYM:
							for (Object[] rowClass : tmpRows) {
								if ((Integer)rowClass[0] == TreeEditorObjectWrapper.SYNONYMN) {
									Object[] row=(Object[]) rowClass[1];
									bounds = fontMetrics.getStringBounds(row[TableSourceColumn.TERM.ordinal()].toString(), null);  
							        int tmpWidth = (int) bounds.getWidth();  
									if (maxTermWidth< tmpWidth){
										maxTermWidth=tmpWidth;
									}
									model.addRow(row);
								}
							}
							break;
						case RETIRED:
							for (Object[] rowClass : tmpRows) {
								if ((Integer)rowClass[0] == TreeEditorObjectWrapper.NOTACCEPTABLE) {
									Object[] row=(Object[]) rowClass[1];
									bounds = fontMetrics.getStringBounds(row[TableSourceColumn.TERM.ordinal()].toString(), null);  
							        int tmpWidth = (int) bounds.getWidth();  
									if (maxTermWidth< tmpWidth){
										maxTermWidth=tmpWidth;
									}
									model.addRow(row);
								}
							}
							break;
						}
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (TerminologyException e) {
				e.printStackTrace();
			}
		}
		
		tabSou.setModel(model);
		if(maxTermWidth==0)
			maxTermWidth=200;
		else
			maxTermWidth+=10;
		TableColumnModel cmodel = tabSou.getColumnModel(); 
		TermRenderer textAreaRenderer = new TermRenderer();
		cmodel.getColumn(TableSourceColumn.TERM.ordinal()).setCellRenderer(textAreaRenderer); 
		cmodel.getColumn(TableSourceColumn.TERM_TYPE.ordinal()).setCellRenderer(new TermTypeIconRenderer()); 
		cmodel.getColumn(TableSourceColumn.ACCEPTABILITY.ordinal()).setCellRenderer(new AcceptabilityIconRenderer()); 
		cmodel.getColumn(TableSourceColumn.LANGUAGE.ordinal()).setCellRenderer(new LanguageIconRenderer()); 
		cmodel.getColumn(TableSourceColumn.ICS.ordinal()).setCellRenderer(new ICSIconRenderer()); 

//		double widthAvai = panel9.getPreferredSize().getWidth();
//		int termColAvai=(int) (widthAvai-100);
//		if (termColAvai>maxTermWidth)
//			cmodel.getColumn(TableSourceColumn.TERM.ordinal()).setPreferredWidth(termColAvai); 
//		else
//			cmodel.getColumn(TableSourceColumn.TERM.ordinal()).setPreferredWidth(maxTermWidth); 
		cmodel.getColumn(TableSourceColumn.TERM.ordinal()).setMinWidth(maxTermWidth);
		
		cmodel.getColumn(TableSourceColumn.TERM_TYPE.ordinal()).setMaxWidth(48); 
		cmodel.getColumn(TableSourceColumn.ACCEPTABILITY.ordinal()).setMaxWidth(48); 
		cmodel.getColumn(TableSourceColumn.ICS.ordinal()).setMaxWidth(48); 
		cmodel.getColumn(TableSourceColumn.LANGUAGE.ordinal()).setMaxWidth(48); 
		
		tabSou.setRowHeight(24);
		tabSou.setUpdateSelectionOnSort(true);
		tabSou.revalidate();		

	}


	/**
	 * Populate target tree.
	 *
	 * @throws Exception the exception
	 */
	private void populateTargetTree() throws Exception {
		I_TermFactory tf = Terms.get();
//		DefaultMutableTreeNode top = null;
		boolean bHasPref =false;
		boolean bHasFSN =false;
		bAddFSN.setEnabled(true);
		targetPreferred="";
		targetPreferredICS=false;
		int authId;
		String authorColName="";
		int authorColPos =-1;
		int authorAdj=0;
		int maxTermWidth=0;
		HashMap<Integer,String> hashAuthId=new HashMap<Integer,String>();
		//		translConfig=LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
		LinkedHashSet<TreeComponent> targetCom = translConfig.getTargetTreeComponents();

		if (translConfig.getTargetTreeComponents().contains(ConfigTranslationModule.TreeComponent.AUTHOR_PATH)){
			authorAdj = 1;
			authorColPos = TableTargetColumn.values().length;
			authorColName = "Author";
		}
		Object[][] data=null;
		String[] columnNames = new String[TableTargetColumn.values().length + authorAdj];
		for (int i=0;i<TableTargetColumn.values().length;i++){
			columnNames[i]=TableTargetColumn.values()[i].getColumnName();
		}
		if (authorAdj==1){
			columnNames[authorColPos]=authorColName;
		}
		DefaultTableModel model=new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};
		List<Object[]> tmpRows=new ArrayList<Object[]>();
		if (concept != null) {
			try {
//				top = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toString(), TreeEditorObjectWrapper.CONCEPT, concept));
				I_GetConceptData langRefset=this.translationProject.getTargetLanguageRefset();
				if (langRefset == null) {
					JOptionPane.showMessageDialog(new JDialog(), "Target language refset cannot be retrieved\nCheck project details", 
							"Error", JOptionPane.ERROR_MESSAGE);
					throw new Exception("Target language refset cannot be retrieved.");
				}
				List<ContextualizedDescription> descriptions = LanguageUtil.getContextualizedDescriptions(
						concept.getConceptNid(), langRefset.getConceptNid(), true);

//				DefaultMutableTreeNode groupLang = new DefaultMutableTreeNode(
//						new TreeEditorObjectWrapper(langRefset.getInitialText(), TreeEditorObjectWrapper.FOLDER,langRefset ));

//				List<DefaultMutableTreeNode> nodesToAdd = new ArrayList<DefaultMutableTreeNode>();

				boolean bNewNode=false;
				for (I_ContextualizeDescription description : descriptions) {
					if (description.getLanguageExtension()!=null && description.getLanguageRefsetId()==langRefset.getConceptNid()){
//						DefaultMutableTreeNode descriptionNode = null;
						bNewNode=false;
						Object[] row=new Object[TableTargetColumn.values().length];

						Object[] rowClass=new Object[2];
						Object[] termType_Status=new Object[2];
						row[TableTargetColumn.TERM.ordinal()]=description;
						row[TableTargetColumn.LANGUAGE.ordinal()]=description.getLang();
						row[TableTargetColumn.ICS.ordinal()]=description.isInitialCaseSignificant();
						
						if (description.getAcceptabilityId()==notAcceptable.getConceptNid() ||
								description.getExtensionStatusId()==inactive.getConceptNid() ||
								description.getDescriptionStatusId()==retired.getConceptNid()){
							if(targetCom.contains(ConfigTranslationModule.TreeComponent.RETIRED)){
								rowClass[0]=TreeEditorObjectWrapper.NOTACCEPTABLE;
								row[TableTargetColumn.ACCEPTABILITY.ordinal()]=notAcceptable;
								termType_Status[1]=inactive;
								if (description.getTypeId() == fsn.getConceptNid()){ 
									termType_Status[0]=fsn;
								}else{
									termType_Status[0]=this.description;
								}
								row[TableSourceColumn.TERM_TYPE.ordinal()]=termType_Status;
								bNewNode=true;
							}
						} else if (description.getTypeId() == fsn.getConceptNid()){
							if (targetCom.contains(ConfigTranslationModule.TreeComponent.FSN)) {
								rowClass[0]=TreeEditorObjectWrapper.FSNDESCRIPTION;
								row[TableSourceColumn.ACCEPTABILITY.ordinal()]=preferred;
								termType_Status[0]=fsn;
								termType_Status[1]=active;
								row[TableSourceColumn.TERM_TYPE.ordinal()]=termType_Status;
								bNewNode=true;
								bNewNode=true;
							}
							bHasFSN=true;
						} else if (description.getAcceptabilityId() == acceptable.getConceptNid()
								&& targetCom.contains(ConfigTranslationModule.TreeComponent.SYNONYM)) {
							rowClass[0]=TreeEditorObjectWrapper.SYNONYMN;
							row[TableSourceColumn.ACCEPTABILITY.ordinal()]=acceptable;
							termType_Status[0]=this.description;
							termType_Status[1]=active;
							row[TableSourceColumn.TERM_TYPE.ordinal()]=termType_Status;
							bNewNode=true;
						} else if (description.getAcceptabilityId() == preferred.getConceptNid()
								&& targetCom.contains(ConfigTranslationModule.TreeComponent.PREFERRED)) {
							rowClass[0]=TreeEditorObjectWrapper.PREFERRED;
							row[TableSourceColumn.ACCEPTABILITY.ordinal()]=preferred;
							termType_Status[0]=this.description;
							termType_Status[1]=active;
							row[TableSourceColumn.TERM_TYPE.ordinal()]=termType_Status;
							bHasPref = true;
							targetPreferred=description.getText();
							targetPreferredICS=description.isInitialCaseSignificant();

							bNewNode=true;
						}else if (targetCom.contains(ConfigTranslationModule.TreeComponent.RETIRED)){
							rowClass[0]=TreeEditorObjectWrapper.SYNONYMN;
							row[TableSourceColumn.ACCEPTABILITY.ordinal()]=notAcceptable;
							termType_Status[0]=this.description;
							termType_Status[1]=inactive;
							row[TableSourceColumn.TERM_TYPE.ordinal()]=termType_Status;
							bNewNode=true;
						}
						if (bNewNode){
							if (translConfig.getTargetTreeComponents().contains(ConfigTranslationModule.TreeComponent.AUTHOR_PATH)){

								authId=description.getDescriptionVersioned().getLastTuple().getAuthorNid();
								String userConcept="";
								if (hashAuthId.containsKey(authId))
									userConcept=hashAuthId.get(authId);
								else{
									I_GetConceptData conc= tf.getConcept(authId);
									if (conc!=null){
										userConcept=conc.toString();
										hashAuthId.put(authId,userConcept);
									}
								}
								if (!userConcept.equals("")){
									row[authorColPos]=userConcept;

								}

							}
							rowClass[1]=row;
							tmpRows.add(rowClass);
						}
					}
				}
				Font fontTmp = tabTar.getFont();
				FontMetrics fontMetrics=new FontMetrics(fontTmp){};
				Rectangle2D bounds;
				for (ConfigTranslationModule.TreeComponent tComp:targetCom){

						switch (tComp){
						case FSN:
							for (Object[] rowClass : tmpRows) {
								if ((Integer)rowClass[0] == TreeEditorObjectWrapper.FSNDESCRIPTION) {
									Object[] row=(Object[]) rowClass[1];
									bounds = fontMetrics.getStringBounds(row[TableTargetColumn.TERM.ordinal()].toString(), null);  
							        int tmpWidth = (int) bounds.getWidth();  
									if (maxTermWidth< tmpWidth){
										maxTermWidth=tmpWidth;
									}
									model.addRow(row);
								}
							}
							break;
						case PREFERRED:
							for (Object[] rowClass : tmpRows) {
								if ((Integer)rowClass[0] == TreeEditorObjectWrapper.PREFERRED) {
									Object[] row=(Object[]) rowClass[1];
									bounds = fontMetrics.getStringBounds(row[TableTargetColumn.TERM.ordinal()].toString(), null);  
							        int tmpWidth = (int) bounds.getWidth();  
									if (maxTermWidth< tmpWidth){
										maxTermWidth=tmpWidth;
									}
									model.addRow(row);
									targetPreferredRow= model.getRowCount()-1;
								}
							}
							break;
						case SYNONYM:
							for (Object[] rowClass : tmpRows) {
								if ((Integer)rowClass[0] == TreeEditorObjectWrapper.SYNONYMN) {
									Object[] row=(Object[]) rowClass[1];
									bounds = fontMetrics.getStringBounds(row[TableTargetColumn.TERM.ordinal()].toString(), null);  
							        int tmpWidth = (int) bounds.getWidth();  
									if (maxTermWidth< tmpWidth){
										maxTermWidth=tmpWidth;
									}
									model.addRow(row);
								}
							}
							break;
						case RETIRED:
							for (Object[] rowClass : tmpRows) {
								if ((Integer)rowClass[0] == TreeEditorObjectWrapper.NOTACCEPTABLE) {
									Object[] row=(Object[]) rowClass[1];
									bounds = fontMetrics.getStringBounds(row[TableTargetColumn.TERM.ordinal()].toString(), null);  
							        int tmpWidth = (int) bounds.getWidth();  
									if (maxTermWidth< tmpWidth){
										maxTermWidth=tmpWidth;
									}
									model.addRow(row);
								}
							}
							break;
						}
				}


			} catch (IOException e) {
				e.printStackTrace();
			} catch (TerminologyException e) {
				e.printStackTrace();
			}
		}

		tabTar.setModel(model);
		if(maxTermWidth==0)
			maxTermWidth=200;
		else
			maxTermWidth+=10;
		TableColumnModel cmodel = tabTar.getColumnModel(); 
		TermRenderer textAreaRenderer = new TermRenderer();
		cmodel.getColumn(TableTargetColumn.TERM.ordinal()).setCellRenderer(textAreaRenderer); 
		cmodel.getColumn(TableTargetColumn.TERM_TYPE.ordinal()).setCellRenderer(new TermTypeIconRenderer()); 
		cmodel.getColumn(TableTargetColumn.ACCEPTABILITY.ordinal()).setCellRenderer(new AcceptabilityIconRenderer()); 
		cmodel.getColumn(TableTargetColumn.LANGUAGE.ordinal()).setCellRenderer(new LanguageIconRenderer()); 
		cmodel.getColumn(TableTargetColumn.ICS.ordinal()).setCellRenderer(new ICSIconRenderer()); 

//		double widthAvai = panel8.getPreferredSize().getWidth();

//		double widthAdj=0;
//		if (authorAdj==1){
//			widthAdj=cmodel.getColumn(authorColPos).getWidth();
//		}
//		int termColAvai=(int) (widthAvai-100-widthAdj);
//		if (termColAvai>maxTermWidth)
//			cmodel.getColumn(TableTargetColumn.TERM.ordinal()).setPreferredWidth(termColAvai); 
////		if (cmodel.getColumn(TableTargetColumn.TERM.ordinal()).getWidth()<maxTermWidth)
//		else
//			cmodel.getColumn(TableTargetColumn.TERM.ordinal()).setPreferredWidth(maxTermWidth); 
		cmodel.getColumn(TableTargetColumn.TERM.ordinal()).setMinWidth(maxTermWidth);
		cmodel.getColumn(TableTargetColumn.TERM_TYPE.ordinal()).setMaxWidth(48); 
		cmodel.getColumn(TableTargetColumn.ACCEPTABILITY.ordinal()).setMaxWidth(48); 
		cmodel.getColumn(TableTargetColumn.ICS.ordinal()).setMaxWidth(48); 
		cmodel.getColumn(TableTargetColumn.LANGUAGE.ordinal()).setMaxWidth(48); 
		tabTar.setRowHeight(24);
		tabTar.setUpdateSelectionOnSort(true);
		tabTar.revalidate();

		bAddFSN.setEnabled(!bHasFSN && bHasPref);
		comboBox1.setEnabled(false);
		textField1.setVisible(true);
		textField1.setEnabled(false);
	}

	/**
	 * Gets the max term width.
	 *
	 * @param font the font
	 * @param string the string
	 * @return the max term width
	 */
	private int getMaxTermWidth(Font font, String string) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Populate details tree.
	 *
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unchecked")
	private void populateDetailsTree() throws Exception {
		I_TermFactory tf = Terms.get();
		DefaultMutableTreeNode top = null;
		if (concept != null) {
			try {
				I_ConfigAceFrame config = tf.getActiveAceFrameConfig();

//				top = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toString(), IconUtilities.CONCEPT, concept));
				I_ConceptAttributeTuple attributes = null;
				attributes = concept.getConceptAttributeTuples(config.getPrecedence(), 
						config.getConflictResolutionStrategy()).iterator().next();

		//				String definedOrPrimitive = "";
//				DefaultMutableTreeNode idNode=null;
				String statusName = tf.getConcept(attributes.getStatusId()).toString();
				if (statusName.equalsIgnoreCase("retired") || statusName.equalsIgnoreCase("inactive")){
					top = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toString(), IconUtilities.INACTIVE, concept));
				}else if (attributes.isDefined()) {
//					definedOrPrimitive = "fully defined";
					top = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toString(), IconUtilities.DEFINED, concept));
				
//					idNode= new DefaultMutableTreeNode(
//							new TreeEditorObjectWrapper(definedOrPrimitive, IconUtilities.DEFINED, attributes.getMutablePart()));
//					
				} else {
					top = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toString(), IconUtilities.PRIMITIVE, concept));
//					definedOrPrimitive = "primitive";
//					idNode= new DefaultMutableTreeNode(
//							new TreeEditorObjectWrapper(definedOrPrimitive, IconUtilities.PRIMITIVE, attributes.getMutablePart()));
//					
				}
//				top.add(idNode);

//				String statusName = tf.getConcept(attributes.getStatusId()).toString();
//				DefaultMutableTreeNode statusNode =null;
//				if (statusName.equalsIgnoreCase("retired") || statusName.equalsIgnoreCase("inactive")){
//					statusNode = new DefaultMutableTreeNode(
//							new TreeEditorObjectWrapper(statusName, IconUtilities.INACTIVE, attributes.getMutablePart()));
//				}else{
//					statusNode = new DefaultMutableTreeNode(
//							new TreeEditorObjectWrapper(statusName, IconUtilities.ATTRIBUTE, attributes.getMutablePart()));
//				}
//				top.add(statusNode);

				List<I_RelTuple> relationships = (List<I_RelTuple>) concept.getSourceRelTuples(null, null, config.getViewPositionSetReadOnly(),
						config.getPrecedence(), config.getConflictResolutionStrategy());
				//				List<I_RelVersioned> relationships2 = (List<I_RelVersioned>) concept.getDestRels();

				List<DefaultMutableTreeNode> nodesToAdd = new ArrayList<DefaultMutableTreeNode>();

				HashMap<Integer,List<DefaultMutableTreeNode>> mapGroup= new HashMap<Integer,List<DefaultMutableTreeNode>>() ;
				List<DefaultMutableTreeNode> roleList=new ArrayList<DefaultMutableTreeNode>();
				int group=0;
				for (I_RelTuple relationship : relationships) {
					I_GetConceptData targetConcept = tf.getConcept(relationship.getC2Id());
					I_GetConceptData typeConcept = tf.getConcept(relationship.getTypeId());
					String label = typeConcept + ": " + targetConcept;

					if ((relationship.getTypeId() == ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid())
							|| (typeConcept.toString().equalsIgnoreCase("is a") )) {
						attributes = targetConcept.getConceptAttributeTuples(config.getPrecedence(), 
								config.getConflictResolutionStrategy()).iterator().next();
						DefaultMutableTreeNode supertypeNode=null;
						statusName = tf.getConcept(attributes.getStatusId()).toString();
						if (statusName.equalsIgnoreCase("retired") || statusName.equalsIgnoreCase("inactive")){
							supertypeNode = new DefaultMutableTreeNode(
									new TreeEditorObjectWrapper(label, IconUtilities.INACTIVE_PARENT, relationship.getMutablePart()));
							
						}else if (attributes.isDefined()) {
							supertypeNode = new DefaultMutableTreeNode(
								new TreeEditorObjectWrapper(label, IconUtilities.DEFINED_PARENT, relationship.getMutablePart()));
						}else{
							supertypeNode = new DefaultMutableTreeNode(
									new TreeEditorObjectWrapper(label, IconUtilities.PRIMITIVE_PARENT, relationship.getMutablePart()));
							
						}
						nodesToAdd.add(supertypeNode);
					} else {
						if (relationship.getGroup()==0){
							if (relationship.getCharacteristicId()==definingChar){
								DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(
										new TreeEditorObjectWrapper(label, IconUtilities.ROLE, relationship.getMutablePart()));
								nodesToAdd.add(roleNode);
							}
							else{
								DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(
										new TreeEditorObjectWrapper(label, IconUtilities.ASSOCIATION, relationship.getMutablePart()));
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
									new TreeEditorObjectWrapper(label, IconUtilities.ROLE, relationship.getMutablePart())));
							mapGroup.put(group, roleList);
						}
					}
				}



				for (DefaultMutableTreeNode loopNode : nodesToAdd) {
					TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
					if (nodeObject.getType() == IconUtilities.DEFINED_PARENT || nodeObject.getType() == IconUtilities.PRIMITIVE_PARENT || nodeObject.getType() == IconUtilities.INACTIVE_PARENT) {
						top.add(loopNode);
					}
				}

				for (DefaultMutableTreeNode loopNode : nodesToAdd) {
					TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
					if (nodeObject.getType() == IconUtilities.ROLE) {
						top.add(loopNode);
					}
				}
				for (int key:mapGroup.keySet()){
					List<DefaultMutableTreeNode> lRoles=(List<DefaultMutableTreeNode>)mapGroup.get(key);
					DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(
							new TreeEditorObjectWrapper("Group:" + key, IconUtilities.ROLEGROUP,lRoles ));
					for (DefaultMutableTreeNode rNode: lRoles){
						groupNode.add(rNode);
					}
					top.add(groupNode);
				}
				for (DefaultMutableTreeNode loopNode : nodesToAdd) {
					TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
					if (nodeObject.getType() == IconUtilities.ASSOCIATION) {
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

	/**
	 * The Class TermTypeIconRenderer.
	 */
	class TermTypeIconRenderer extends DefaultTableCellRenderer {

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {

			JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			Object[] termType_status=(Object[])value;
			String termType=termType_status[0].toString();
			String status=termType_status[1].toString();
			label.setIcon(IconUtilities.getIconForTermType_Status(termType, status));
			label.setText("");
			label.setToolTipText(status + " " + termType );
			label.setHorizontalAlignment(CENTER);

			return label;
		}

	}
	
	/**
	 * The Class AcceptabilityIconRenderer.
	 */
	class AcceptabilityIconRenderer extends DefaultTableCellRenderer {

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {

			JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			label.setIcon (IconUtilities.getIconForAcceptability(value.toString()));
			label.setText("");
			label.setToolTipText(value.toString() );
			label.setHorizontalAlignment(CENTER);
			return label;
		}

	}

	/**
	 * The Class TermRenderer.
	 */
	class TermRenderer extends DefaultTableCellRenderer {

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {

			JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			label.setToolTipText(value.toString() );
			return label;
		}

	}
	
	/**
	 * The Class LanguageIconRenderer.
	 */
	class LanguageIconRenderer extends DefaultTableCellRenderer {

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {

			JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			label.setIcon(IconUtilities.getIconForLanguage(value.toString()));
			label.setText("");
			label.setToolTipText(value.toString() );
			label.setHorizontalAlignment(CENTER);

			return label;
		}

	}

	/**
	 * The Class ICSIconRenderer.
	 */
	class ICSIconRenderer extends DefaultTableCellRenderer {

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {

			JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			label.setIcon(IconUtilities.getIconForICS((Boolean)value));
			label.setText("");
			label.setToolTipText(value.toString() );
			label.setHorizontalAlignment(CENTER);

			return label;
		}

	}
	
	/**
	 * Update properties panel.
	 *
	 * @param descrpt the descrpt
	 * @param rowModel the row model
	 */
	private void updatePropertiesPanel(ContextualizedDescription descrpt, int rowModel) {
		boolean update = false;

//		DefaultMutableTreeNode node = (DefaultMutableTreeNode)  tree2.getLastSelectedPathComponent();

		//		try {
		//			translConfig=LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
		//		} catch (IOException e1) {
		//			e1.printStackTrace();
		//		} catch (TerminologyException e1) {
		//			e1.printStackTrace();
		//		}
//		if (node != null) {
//			TreeEditorObjectWrapper nodeWrp = (TreeEditorObjectWrapper)node.getUserObject();
//
//			Object obj=nodeWrp.getUserObject();
//			if (obj instanceof ContextualizedDescription){
				if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.PREFERRED_TERM_EDITOR)){
					if (descrpt.getTypeId()!=preferred.getConceptNid()){
						if (editingRow!=null){
							setByCode=true;
							int selrow=tabTar.convertRowIndexToView(editingRow);
							tabTar.setRowSelectionInterval(selrow,selrow);
							setByCode=false;
						}
						return;
					}
				}
				if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.SYNONYMS_EDITOR)){
					if (descrpt.getTypeId()!=synonym.getConceptNid()){
						if (editingRow!=null){
							setByCode=true;
							int selrow=tabTar.convertRowIndexToView(editingRow);
							tabTar.setRowSelectionInterval(selrow,selrow);
							setByCode=false;
						}
						return;
					}
				}
				if (descriptionInEditor == null) {
					update = true;
				} else {
					if (descriptionInEditor.getText().trim().equals(textField1.getText().trim()) && (
							descriptionInEditor.isInitialCaseSignificant() == rbYes.isSelected())
							&& descriptionInEditor.getAcceptabilityId()== ((I_GetConceptData)cmbAccep.getSelectedItem()).getConceptNid()
							&& ((descriptionInEditor.getExtensionStatusId()==active.getConceptNid()
									&& rbAct.isSelected()) 
									||(descriptionInEditor.getExtensionStatusId()!=active.getConceptNid()
											&& !rbAct.isSelected()) )
											&& ((descriptionInEditor.getTypeId()==fsn.getConceptNid() && fsn.equals((I_GetConceptData)comboBox1.getSelectedItem()) )
													|| (descriptionInEditor.getTypeId()!=fsn.getConceptNid() && !fsn.equals((I_GetConceptData)comboBox1.getSelectedItem())))) {
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
							if (editingRow!=null){
								setByCode=true;
								int selrow=tabTar.convertRowIndexToView(editingRow);
								tabTar.setRowSelectionInterval(selrow,selrow);
								setByCode=false;
							}
						}
					}
				}

				if (update) {

					if (descrpt == null) {
						descriptionInEditor = null;
						//			label4.setText("");
						textField1.setText("");
						rbYes.setSelected(false);
						panel2.revalidate();
						//			saveDesc.setEnabled(false);
						mSpellChk.setEnabled(false);
						//			button5.setEnabled(false);
						mAddPref.setEnabled(false);
						mAddDesc.setEnabled(true);
						//label4.setVisible(true);
						comboBox1.setEnabled(true);
						rbYes.setEnabled(true);
						rbNo.setEnabled(true);
						rbAct.setEnabled(true);
						rbInact.setEnabled(true);
						cmbAccep.setEnabled(true);
						textField1.setEnabled(false);
					} else {
						editingRow=rowModel;
						if (descrpt.getLanguageRefsetId()==targetId){
							if (descrpt.getTypeId()==fsn.getConceptNid() ||
									descrpt.getTypeId()==preferred.getConceptNid()  ||
									descrpt.getTypeId()==synonym.getConceptNid() ) {

								try {
									if (fsn.getConceptNid()== descrpt.getTypeId()){
										comboBox1.setSelectedItem(fsn);
									}else{
										comboBox1.setSelectedItem(description);
									}
									cmbAccep.setSelectedItem(Terms.get().getConcept(descrpt.getAcceptabilityId()));
								} catch (TerminologyException ex) {
									ex.printStackTrace();
								} catch (IOException ex) {
									ex.printStackTrace();
								}
								textField1.setEnabled(true);
								//						bDescIssue.setEnabled(false);
								descriptionInEditor = descrpt;
								//	label4.setText(Terms.get().getConcept(descriptionInEditor.getTypeId()).toString());
								textField1.setText(descriptionInEditor.getText().trim());
								if (descriptionInEditor.isInitialCaseSignificant()) 
									rbYes.setSelected(true);
								else
									rbNo.setSelected(true);
								rbAct.setSelected(descriptionInEditor.getExtensionStatusId()==active.getConceptNid());
								panel2.revalidate();
								saveDesc.setEnabled(true);
								mSpellChk.setEnabled(true);
								mAddPref.setEnabled(false);
								//								mAddDesc.setEnabled(false);
								button5.setEnabled(true);
								//	label4.setVisible(true);
								comboBox1.setEnabled(true);
								cmbAccep.setEnabled(true);
								rbAct.setEnabled(true);
								rbInact.setEnabled(true);
								rbYes.setEnabled(true);
								rbNo.setEnabled(true);
							}
						}
					}
				}
//			}
//		}
	}
	/**
	 * Update similarity table.
	 * 
	 * @param query the query
	 */
	private void updateSimilarityTable(String query) {
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

		List<SimilarityMatchedItem> results = LanguageUtil.getSimilarityResults(query, sourceIds, targetId, types,null);
		String[] columnNames = {"Source Text",
		"Target Text"};
		String[][] data = null;
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};

		if (results.isEmpty()) {
			tableModel.addRow(new String[] {query,"No matches found"});
		} else {
			for (SimilarityMatchedItem item : results) {
				tableModel.addRow(new String[] {item.getSourceText(),item.getTargetText()});
			}
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

		if (results.isEmpty()) {
			tableModel.addRow(new String[] {"No results found","No results found"});
			tabbedPane1.setTitleAt(1, "<html>Translation Memory</html>");
		} else {
			for (String key : results.keySet()) {
				tableModel.addRow(new String[] {key,results.get(key)});
			}
			tabbedPane1.setTitleAt(1, "<html>Translation Memory<b><font color='red'>*</font></b></html>");
		}
		table2.setModel(tableModel);
		TableColumnModel cmodel = table2.getColumnModel(); 
		TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
		cmodel.getColumn(0).setCellRenderer(textAreaRenderer); 
		cmodel.getColumn(1).setCellRenderer(textAreaRenderer); 
		table2.revalidate();
	}

	/**
	 * Update ui.
	 *
	 * @param translationProject the translation project
	 * @param workListMember the work list member
	 * @param role the role
	 */
	public void updateUI(TranslationProject translationProject,WorkListMember workListMember, I_GetConceptData role){
	//	clearForm(true);
		try {
			this.translationProject=translationProject;
			this.role=role;
			translConfig=LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
//			if (translConfig.isProjectDefaultConfiguration())
			translConfig=getTranslationProjectConfig();
			this.concept=workListMember.getConcept();
			this.worklistMember=workListMember;
			sourceLangRefsets=new HashSet<LanguageMembershipRefset>();
			sourceIds=new ArrayList<Integer>();
			targetId=-1;
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
			hierarchyNavigator1.setFocusConcept(concept);

			sourceICS=LanguageUtil.getDefaultICS(concept, sourceLangRefsets.iterator().next(), targetLangRefset, config);
			if(sourceICS)
				rbYes.setSelected(true);
			else
				rbNo.setSelected(true);

			if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.PREFERRED_TERM_EDITOR)){
				if (targetPreferred.equals("") ){
					mAddPrefActionPerformed();
					String pref=LanguageUtil.getDefaultPreferredTermText(concept, sourceLangRefsets.iterator().next(), targetLangRefset, config);
					textField1.setText(pref);

				}
				else{
					if (targetPreferredRow!=null){
						tabTar.setRowSelectionInterval(targetPreferredRow, targetPreferredRow);
					}
				}
				bAddFSN.setEnabled(false);
				mAddDesc.setEnabled(false);
				mAddPref.setEnabled(true);
			}else if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.SYNONYMS_EDITOR)){
				mAddDescActionPerformed();
				bAddFSN.setEnabled(false);
				mAddDesc.setEnabled(true);
				mAddPref.setEnabled(false);
			}
			getPreviousComments();
			getWebReferences();
			if (translationProject.getProjectIssueRepo()!=null){
//			Object flag=config.getDbConfig().getProperty(TranslationHelperPanel.EXTERNAL_ISSUES_ACTIVE_PROPERTY_FLAG);
//			if (flag!=null && flag.toString().equals("true")){
				tabbedPane2.setTitleAt(2, "<html>Issues</html>");
				Thread appthr=new Thread(){
					synchronized
					public void run(){
						SwingUtilities.invokeLater(new Runnable(){
							synchronized
							@Override
							public void run() {
								loadIssues();

							}


						});
					}

				};
				appthr.start();
			}else{
				tabbedPane2.setTitleAt(2, "<html>Issues <font><style size=1>(Inactive)</style></font></html>");

			}

			mClose.setEnabled(false);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	/**
	 * Gets the web references.
	 *
	 * @return the web references
	 */
	private void getWebReferences() {
		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			HashMap<URL, String>urls=new HashMap<URL, String>();

			StringBuffer sb=new StringBuffer("");
			sb.append("<html><body>");
			int urlCount=0;
			if (targetLangRefset!=null){
				urls=targetLangRefset.getCommentsRefset(config).getUrls(this.concept.getConceptNid());	
				urlCount=urls.size();
				for (URL url:urls.keySet()) {
					sb.append("<a href=\"");
					sb.append( url.toString() );
					sb.append("\">");
					sb.append(url.toString());
					sb.append("</a><br>");
				}
			}
			urls=TerminologyProjectDAO.getWorkList(Terms.get().getConcept(worklistMember.getWorkListUUID()), config).getCommentsRefset(config).getUrls(this.concept.getConceptNid());

			urlCount+=urls.size();
			for (URL url:urls.keySet()) {
				sb.append("<a href=\"");
				sb.append( url.toString() );
				sb.append("\">");
				sb.append(url.toString());
				sb.append("</a><br>");
			}
			sb.append("</body></html>");
			
			refTable.setText(sb.toString());
			if (urlCount>0){
				tabbedPane2.setTitleAt(1, "<html>Web references <b><font color='red'>(" + urlCount + ")</font></b></html>");
			}else {
				tabbedPane2.setTitleAt(1, "<html>Web references (0)</font></b></html>");
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	/**
	 * Load issues.
	 */
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
			IssueRepository repo= IssueRepositoryDAO.getIssueRepository(translationProject.getProjectIssueRepo()); 
			IssueRepoRegistration regis;
			regis=IssueRepositoryDAO.getRepositoryRegistration(repo.getUuid(), config);
			if (regis!=null && regis.getUserId()!= null && regis.getPassword()!=null){
				issueListPanel.loadIssues(concept,repo,regis);
			}
//			List<UUID> repoUUID=translConfig.getProjectIssuesRepositoryIds();
//			if (repoUUID!=null && repoUUID.size()>0){
//				IssueRepository repo= IssueRepositoryDAO.getIssueRepository(Terms.get().getConcept(repoUUID)); 
//				IssueRepoRegistration regis;
//				regis=IssueRepositoryDAO.getRepositoryRegistration(repo.getUuid(), config);
//				if (regis!=null && regis.getUserId()!= null && regis.getPassword()!=null){
//					issueListPanel.loadIssues(concept,repo,regis);
//				}
//			}
		} catch (TerminologyException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

	/**
	 * Creates the issue panel.
	 */
	private void createIssuePanel() {
		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			if (config==null)
				issueListPanel=new IssuesListPanel2(false);
			else
				issueListPanel=new IssuesListPanel2(config,false);

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

	/**
	 * Clear comments.
	 */
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
		tabbedPane2.setTitleAt(0, "<html>Comments</font></b></html>");
	}
	
	/**
	 * Clear similarities.
	 */
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

	/**
	 * Clear ling guidelines.
	 */
	private void clearLingGuidelines(){
		tabbedPane1.setTitleAt(2, "<html>Linguistic Guidelines</html>");
		editorPane1.setText("");
		editorPane1.revalidate();
	}

	/**
	 * Clear trans memory.
	 */
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

	/**
	 * Gets the previous comments.
	 *
	 * @return the previous comments
	 */
	private void getPreviousComments() {
		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			List<String>comments=new ArrayList<String>();
			String[] columnNames = {"Comment"};
			String[][] data = null;
			DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
				private static final long serialVersionUID = 1L;

				public boolean isCellEditable(int x, int y) {
					return false;
				}
			};
			if (targetLangRefset!=null){
				comments.addAll(targetLangRefset.getCommentsRefset(config).getComments(this.concept.getConceptNid()).values());
				for (int i=comments.size()-1;i>-1;i--) {
					tableModel.addRow(new Object[] {formatComment(comments.get(i),"Language refset")});
				}
			}
			comments.addAll(TerminologyProjectDAO.getWorkList(Terms.get().getConcept(worklistMember.getWorkListUUID()), config).getCommentsRefset(config).getComments(this.concept.getConceptNid()).values());
			
			for (int i=comments.size()-1;i>-1;i--) {
				tableModel.addRow(new Object[] {formatComment(comments.get(i),"Workflow")});
			}

			tblComm.setModel(tableModel);
			TableColumnModel cmodel = tblComm.getColumnModel(); 
			EditorPaneRenderer textAreaRenderer = new EditorPaneRenderer();
			cmodel.getColumn(0).setCellRenderer(textAreaRenderer); 
			tblComm.setRowHeight(48);
			tblComm.revalidate();
			if (tblComm.getRowCount()>0){
				tabbedPane2.setTitleAt(0, "<html>Comments <b><font color='red'>(" + tblComm.getRowCount() + ")</font></b></html>");
			}else {
				tabbedPane2.setTitleAt(0, "<html>Comments (0)</font></b></html>");
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Format comment.
	 *
	 * @param comment the comment
	 * @param source the source
	 * @return the string
	 */
	private String formatComment(String comment,String source) {
		long thickVer = Terms.get().convertToThickVersion(Integer.parseInt(comment.substring(comment.trim().lastIndexOf(" ") +1)));
		String strDate = formatter.format(thickVer);
		String tmp=comment.substring(0,comment.lastIndexOf(" - Time:"));
		if (tmp.indexOf(COMMENT_HEADER_SEP)>-1){
			tmp= tmp.replace(COMMENT_HEADER_SEP,endP + COMMENT_HEADER_SEP) + htmlFooter;
			return     htmlHeader + source + HEADER_SEPARATOR  + strDate + 
			HEADER_SEPARATOR + tmp ;
		}
		return htmlHeader + source + HEADER_SEPARATOR  + strDate + COMMENT_HEADER_SEP + tmp;

	}

	/**
	 * Sets the workflow buttons.
	 *
	 * @param buttons the new workflow buttons
	 */
	public void setWorkflowButtons(List<Component> buttons){
		removeWorkflowButtons();
		addWorkflowButtons(buttons);
	}

	/**
	 * Adds the workflow buttons.
	 *
	 * @param buttons the buttons
	 */
	private void addWorkflowButtons(List<Component> buttons) {
		
		int columnsCount=(buttons.size() * 2) + 3;
		if (scrollp!=null)
			panel6.remove(scrollp);
		else if (buttonPanel!=null)
			panel6.remove(buttonPanel);
		buttonPanel=new JPanel();
		buttonPanel.setBackground(new Color(238, 238, 238));
		buttonPanel.setLayout(new GridBagLayout());
		
		int col = 0;
		int row = 0;
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = col;
		c.gridy = row;
		JLabel iconLabel = new JLabel("");
		iconLabel.setIcon(IconUtilities.media_step_forwardIcon);
		buttonPanel.add(iconLabel, c);
		col++;
		
		for (Component loopComponent : buttons) {
			if (col == 4) {
				col = 1;
				row++;
			}
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = col;
			c.gridy = row;
			buttonPanel.add(loopComponent, c);
			col++;
		}
		
//		((GridBagLayout)buttonPanel.getLayout()).columnWidths = new int[columnsCount] ;
//		((GridBagLayout)buttonPanel.getLayout()).rowHeights = new int[] {0, 0};
//		((GridBagLayout)buttonPanel.getLayout()).columnWeights = new double[columnsCount] ;
//
//		((GridBagLayout)buttonPanel.getLayout()).columnWeights[0]=1.0;
//		for (int i=1;i<columnsCount-1;i++){
//			((GridBagLayout)buttonPanel.getLayout()).columnWeights[i]=0.0;
//		}
//		((GridBagLayout)buttonPanel.getLayout()).columnWeights[columnsCount-1]=1.0E-4;
//
//		((GridBagLayout)buttonPanel.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
//
//		//---- label8 ----
//		label8=new JLabel();
//		label8.setText("Workflow actions:      ");
//		buttonPanel.add(label8, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
//				GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
//				new Insets(0, 0, 0, 5), 0, 0));
//		for (int i=0;i<buttons.size();i++){
//			Component btton=buttons.get(i);
//			setButtonMnemo(btton);
//			buttonPanel.add(btton, new GridBagConstraints((i+1) * 2 , 0, 1, 1, 0.0, 0.0,
//					GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
//					new Insets(0, 0, 0, 5), 0, 0));
//		}
		scrollp=new JScrollPane();
		scrollp.setViewportView(buttonPanel);
		panel6.add(scrollp);
		panel6.revalidate();
		panel6.repaint();
	}

	/**
	 * Sets the button mnemo.
	 *
	 * @param btton the new button mnemo
	 */
	private void setButtonMnemo(Component btton) {
		if (btton instanceof JButton){
			String buttName=((JButton)btton).getText();
			for (int i=0;i<buttName.length();i++){
				String MnemChar=buttName.substring(i,i + 1).toUpperCase();
				if (!MnemChar.equals(" ") && assignedMnemo.indexOf(MnemChar)<0){
					assignedMnemo=assignedMnemo + MnemChar;
					((JButton)btton).setMnemonic(MnemChar.charAt(0));
					break;
				}
			}
		}
	}

	/**
	 * Removes the workflow buttons.
	 */
	public void removeWorkflowButtons() {
		if (buttonPanel!=null){
			for (int i=buttonPanel.getComponentCount()-1;i>-1;i--){

				Component comp=buttonPanel.getComponent(i);
				if (comp instanceof JButton){
					for (ActionListener aListener :((JButton)comp).getActionListeners()){
						((JButton)comp).removeActionListener(aListener);
					}
				}
				buttonPanel.remove(comp);
				comp=null;
			}
		}

	}

	/**
	 * Update glossary enforcement.
	 * 
	 * @param query the query
	 */
	private void updateGlossaryEnforcement(String query) {
//
//		try {
//			String results = LanguageUtil.getLinguisticGuidelines(concept);
//			if (!results.isEmpty()){
//				tabbedPane1.setTitleAt(2, "<html>Linguistic Guidelines<b><font color='red'>*</font></b></html>");
//			}
//			else{
//				tabbedPane1.setTitleAt(2, "<html>Linguistic Guidelines</html>");
//			}
//			editorPane1.setText(results);
//			editorPane1.revalidate();
//		} catch (TerminologyException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * Gets the source lang refsets.
	 *
	 * @return the source lang refsets
	 */
	public Set<LanguageMembershipRefset> getSourceLangRefsets() {
		return sourceLangRefsets;
	}

	/**
	 * Sets the source lang refsets.
	 *
	 * @param sourceLangRefsets the new source lang refsets
	 */
	public void setSourceLangRefsets(Set<LanguageMembershipRefset> sourceLangRefsets) {
		this.sourceLangRefsets = sourceLangRefsets;
		sourceIds=new ArrayList<Integer>();
		for (LanguageMembershipRefset sourceRef:sourceLangRefsets){
			sourceIds.add(sourceRef.getRefsetId());
		}
	}

	/**
	 * Gets the target lang refset.
	 *
	 * @return the target lang refset
	 */
	public LanguageMembershipRefset getTargetLangRefset() {
		return targetLangRefset;
	}

	/**
	 * Sets the target lang refset.
	 *
	 * @param targetLangRefset the new target lang refset
	 */
	public void setTargetLangRefset(LanguageMembershipRefset targetLangRefset) {
		this.targetLangRefset = targetLangRefset;
		targetId=targetLangRefset.getRefsetId();
	}
	
	/**
	 * Autokeep in inbox.
	 */
	public void AutokeepInInbox(){
		if (this.keepIIClass!=null){
			this.unloaded=false;
			this.keepIIClass.KeepInInbox();
		}
	}
	
	/**
	 * Sets the auto keep function.
	 *
	 * @param thisAutoKeep the new auto keep function
	 */
	public void setAutoKeepFunction(I_KeepTaskInInbox thisAutoKeep) {
		this.keepIIClass=thisAutoKeep;

	}

	/**
	 * Sets the unloaded.
	 *
	 * @param b the new unloaded
	 */
	public void setUnloaded(boolean b) {
		this.unloaded=b;

	}

	/**
	 * Gets the unloaded.
	 *
	 * @return the unloaded
	 */
	public boolean getUnloaded() {
		return this.unloaded;
	}
}