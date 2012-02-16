/*
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

package org.ihtsdo.project.panel.details;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.DefaultTableModel;

import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.issue.IssueRepoRegistration;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;
import org.ihtsdo.project.FileLink;
import org.ihtsdo.project.FileLinkAPI;
import org.ihtsdo.project.ProjectPermissionsAPI;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.panel.ExportTabPanel;
import org.ihtsdo.project.panel.TranslationHelperPanel;
import org.ihtsdo.project.panel.dnd.GetAndSetIssueRepo;
import org.ihtsdo.project.panel.dnd.I_UpdateRepository;
import org.ihtsdo.project.panel.dnd.ListDragGestureListenerWithImage;
import org.ihtsdo.project.panel.dnd.ObjectTransferHandler;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.api.WorkflowDefinitionManager;
import org.ihtsdo.project.workflow.model.WfMembership;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;
import org.ihtsdo.project.workflow.wizard.WizardLauncher;

/**
 * The Class ProjectDetailsPanel.
 *
 * @author Guillermo Reynoso
 */
public class ProjectDetailsPanel extends JPanel {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The Constant TARGET_LIST_NAME. */
	public final static String TARGET_LIST_NAME ="targetLanguageList";
	
	/** The project. */
	private TranslationProject project;
	
	/** The config. */
	private I_ConfigAceFrame config;
	
	/** The list1 model. */
	private DefaultListModel list1Model;
	
	/** The list2 model. */
	private DefaultListModel list2Model;
	
	/** The list4 model. */
	private DefaultListModel list4Model;
	
	/** The list5 model. */
	private DefaultListModel list5Model;
	
	/** The list6 model. */
	private DefaultListModel list6Model;
	
	/** The list7 model. */
	private DefaultListModel list7Model;
	
	/** The utw business process. */
	private BusinessProcess utwBusinessProcess;
	//	private TermComponentLabel targetLanguageLabel;
	/** The source repo. */
	private IssueRepository sourceRepo;
	
	/** The project repo. */
	private IssueRepository projectRepo;

	/** The Concept dn d handler. */
	ObjectTransferHandler ConceptDnDHandler ;
	
	/** The list8 model. */
	private DefaultListModel list8Model;
	
	/** The release candidate list model. */
	private DefaultListModel releaseCandidateListModel;
	
	/** The module id model. */
	private DefaultListModel moduleIdModel;

	private WorkflowDefinition workflowDefinition;

	private String noName;

	/**
	 * Instantiates a new project details panel.
	 *
	 * @param project the project
	 * @param config the config
	 */
	public ProjectDetailsPanel(TranslationProject project, I_ConfigAceFrame config) {
		initComponents();
		this.project = project;
		this.config = config;
		I_TermFactory tf = Terms.get();
		try {
			label18.setIcon(IconUtilities.helpIcon);
			label18.setText("");
			label38.setIcon(IconUtilities.helpIcon);
			label38.setText("");
			issuesHelpLbl.setIcon(IconUtilities.helpIcon);
			issuesHelpLbl.setText("");
			ExportTabPanel expPanel=new ExportTabPanel(project);
			expPanel.revalidate();
			tabbedPane1.addTab("Export Target Language", expPanel);
//			FileLinkAPI flApi = new FileLinkAPI(config);
//			FileLink link1 = new FileLink(new File("sampleProcesses/TranslationWorkflow.bp"), 
//					tf.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_BUSINESS_PROCESS_CATEGORY.getUids()));
//			flApi.putLinkInConfig(link1);
//			FileLink link2 = new FileLink(new File("sampleProcesses/MaintenanceWorkflow.bp"), 
//					tf.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_BUSINESS_PROCESS_CATEGORY.getUids()));
//			flApi.putLinkInConfig(link2);
//			FileLink link3 = new FileLink(new File("sampleProcesses/IsolatedEdit.bp"), 
//					tf.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_BUSINESS_PROCESS_CATEGORY.getUids()));
//			flApi.putLinkInConfig(link3);
//			FileLink link4 = new FileLink(new File("sampleProcesses/TranslationWorkflowCa.bp"), 
//					tf.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_BUSINESS_PROCESS_CATEGORY.getUids()));
//			flApi.putLinkInConfig(link4);
//			FileLink link5 = new FileLink(new File("sampleProcesses/TranslationWorkflowCaFastTrack.bp"), 
//					tf.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_BUSINESS_PROCESS_CATEGORY.getUids()));
//			flApi.putLinkInConfig(link5);
//			FileLink link6 = new FileLink(new File("sampleProcesses/TranslationWorkflowDk.bp"), 
//					tf.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_BUSINESS_PROCESS_CATEGORY.getUids()));
//			flApi.putLinkInConfig(link6);
			
			textField1.setText(project.getName());
			button3.setEnabled(false);

			namespaceTextField.setText(project.getNamespaceRefset());
			
			list4Model = new DefaultListModel();
			List<I_GetConceptData> exclusionRefsets = project.getExclusionRefsets();
			Collections.sort(exclusionRefsets,
					new Comparator<I_GetConceptData>()
					{
						public int compare(I_GetConceptData f1, I_GetConceptData f2)
						{
							return f1.toString().compareTo(f2.toString());
						}
					});
			for (I_GetConceptData exclusionRefset : exclusionRefsets) {
				list4Model.addElement(exclusionRefset);
			}
			list4.setModel(list4Model);
			ConceptDnDHandler =	new ObjectTransferHandler(this.config,null);
			list4.setTransferHandler(ConceptDnDHandler);
			list4.validate();

			list5Model = new DefaultListModel();
			List<I_GetConceptData> linkedRefsets = project.getCommonRefsets();
			Collections.sort(linkedRefsets,
					new Comparator<I_GetConceptData>()
					{
						public int compare(I_GetConceptData f1, I_GetConceptData f2)
						{
							return f1.toString().compareTo(f2.toString());
						}
					});
			for (I_GetConceptData linkedRefset : linkedRefsets) {
				list5Model.addElement(linkedRefset);
			}
			list5.setModel(list5Model);
			list5.setTransferHandler(ConceptDnDHandler);

			list5.validate();

			List<I_GetConceptData> sourceLanguageRefsets = project.getSourceLanguageRefsets();
			Collections.sort(sourceLanguageRefsets,
					new Comparator<I_GetConceptData>()
					{
						public int compare(I_GetConceptData f1, I_GetConceptData f2)
						{
							return f1.toString().compareTo(f2.toString());
						}
					});
			list6Model = new DefaultListModel();
			ListDataListener listDataListener = new ListDataListener() {
				public void contentsChanged(ListDataEvent listDataEvent) {
					//appendEvent(listDataEvent);
				}

				public void intervalAdded(ListDataEvent listDataEvent) {
					appendEvent(listDataEvent);
				}

				public void intervalRemoved(ListDataEvent listDataEvent) {
					//appendEvent(listDataEvent);
				}

				private void appendEvent(ListDataEvent listDataEvent) {
					int index = listDataEvent.getIndex0();
					try {
						if(((ListModel)listDataEvent.getSource()).equals(list6Model)){
							I_GetConceptData addedRfst = (I_GetConceptData)list6Model.get(index);
							if(!LanguageMembershipRefset.validateAsLanguageRefset(addedRfst.getConceptNid(), ProjectDetailsPanel.this.config)){
								list6Model.remove(index);
								JOptionPane.showMessageDialog(ProjectDetailsPanel.this,
										"The selected Source Language refset is not valid or is empty.", 
										"Warning", JOptionPane.WARNING_MESSAGE);
							}
						}else if(((ListModel)listDataEvent.getSource()).equals(list8Model)){
							I_GetConceptData addedRfst = (I_GetConceptData)list8Model.get(index);
							if(!LanguageMembershipRefset.validateAsLanguageRefset(addedRfst.getConceptNid(), ProjectDetailsPanel.this.config)){
								list8Model.remove(index);
								JOptionPane.showMessageDialog(ProjectDetailsPanel.this,
										"The selected Target Language refset is not valid or is empty.", 
										"Warning", JOptionPane.WARNING_MESSAGE);
							}
						}else if(((ListModel)listDataEvent.getSource()).equals(releaseCandidateListModel)){
							I_GetConceptData addedRfst = (I_GetConceptData)releaseCandidateListModel.get(index);
							if(!validateAsPathRefset(addedRfst.getConceptNid(), ProjectDetailsPanel.this.config)){
								releaseCandidateListModel.remove(index);
								JOptionPane.showMessageDialog(ProjectDetailsPanel.this,
										"The selected refset is not a valid Path", 
										"Warning", JOptionPane.WARNING_MESSAGE);
							} else {
								int releasePathNid = 0;
								try {
									releasePathNid = ProjectDetailsPanel.this.project.getReleasePath().getNid();
								} catch (Exception e) {
									e.printStackTrace();
								}
								if(addedRfst.getNid() != releasePathNid){
									button3.setEnabled(true);
								}else{
									button3.setEnabled(false);
								}
							}
						}else if(((ListModel)listDataEvent.getSource()).equals(moduleIdModel)){
							I_GetConceptData addedRfst = (I_GetConceptData)moduleIdModel.get(index);
							if(!validateAsModuleRefset(addedRfst.getConceptNid(), ProjectDetailsPanel.this.config)){
								moduleIdModel.remove(index);
								JOptionPane.showMessageDialog(ProjectDetailsPanel.this,
										"The selected refset is not a valid Moudle", 
										"Warning", JOptionPane.WARNING_MESSAGE);
							}else {
								int modulId = 0;
								try {
									modulId = ProjectDetailsPanel.this.project.getModuleIdRefset().getNid();
								} catch (Exception e) {
									e.printStackTrace();
								}
								if(addedRfst.getNid() != modulId){
									button3.setEnabled(true);
								}else{
									button3.setEnabled(false);
								}
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (TerminologyException e) {
						e.printStackTrace();
					}
				}
			};

			list6Model.addListDataListener(listDataListener);

			for (I_GetConceptData sourceLanguageRefset : sourceLanguageRefsets) {
				list6Model.addElement(sourceLanguageRefset);
			}
			list6.setModel(list6Model);
			list6.setTransferHandler(ConceptDnDHandler);
			list6.validate();

			updateList7Content();

			updateList1Content();

			list8Model = new DefaultListModel();
			list8Model.addListDataListener(listDataListener);
			I_GetConceptData targetLanguageRefset = project.getTargetLanguageRefset();
			if (targetLanguageRefset!=null){
				list8Model.addElement(targetLanguageRefset);
			}
			list8.setName(TARGET_LIST_NAME);
			list8.setModel(list8Model);
			list8.setTransferHandler(ConceptDnDHandler);
			list8.validate();

			releaseCandidateListModel = new DefaultListModel();
			releaseCandidateList.setModel(releaseCandidateListModel);
			releaseCandidateListModel.addListDataListener(listDataListener);
			I_GetConceptData releaseCandidatePath = project.getReleasePath();
			if(releaseCandidatePath != null){
				releaseCandidateListModel.addElement(releaseCandidatePath);
			}
			releaseCandidateList.setName(TARGET_LIST_NAME);
			releaseCandidateList.setTransferHandler(ConceptDnDHandler);

			moduleIdModel = new DefaultListModel();
			moduleIdList.setModel(moduleIdModel);
			moduleIdModel.addListDataListener(listDataListener);
			I_GetConceptData moduleIdRefset = project.getModuleIdRefset();
			if(moduleIdRefset != null){
				moduleIdModel.addElement(moduleIdRefset);
			}
			moduleIdList.setName(TARGET_LIST_NAME);
			moduleIdList.setTransferHandler(ConceptDnDHandler);
			
			//			targetLanguageLabel = new TermComponentLabel();
			//			targetLanguageLabel.setTermComponent(project.getTargetLanguageRefset());
			//			targetLanguageLabel.setAlignmentX(LEFT_ALIGNMENT);
			//			panel3.add(targetLanguageLabel);
			//			panel3.validate();
			
			updateIssuePanel();

//			FileLinkAPI fileLinkApi = new FileLinkAPI(config);
//			List<FileLink> wfFiles = fileLinkApi.getLinksForCategory(tf.getConcept(
//					ArchitectonicAuxiliary.Concept.TRANSLATION_BUSINESS_PROCESS_CATEGORY.getUids()));
//			for (FileLink loopLink : wfFiles) {
//				comboBox1.addItem(loopLink);
//			}
			
			ProjectPermissionsAPI permissionsApi = new ProjectPermissionsAPI(config);
			boolean isProjectManager = permissionsApi.checkPermissionForProject(
					config.getDbConfig().getUserConcept(), 
					tf.getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.localize().getNid()),
					tf.getConcept(ArchitectonicAuxiliary.Concept.PROJECT_MANAGER_ROLE.localize().getNid()));

			if (!isProjectManager) {
				button2.setVisible(false);
				button3.setVisible(false);
				button4.setVisible(false);
				button5.setVisible(false);
				button6.setVisible(false);
				textField1.setEditable(false);
				list4.setEnabled(false);
				list5.setEnabled(false);
				list6.setEnabled(false);
				list8.setEnabled(false);
				//				targetLanguageLabel.setEnabled(false);
			}

			boolean isWorkSetManager = permissionsApi.checkPermissionForProject(
					config.getDbConfig().getUserConcept(), 
					tf.getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.localize().getNid()),
					tf.getConcept(ArchitectonicAuxiliary.Concept.WORKSET_MANAGER_ROLE.localize().getNid()));

			if (!isWorkSetManager) {
				button1.setVisible(false);
				button7.setVisible(false);
			}
			  DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(list2, DnDConstants.ACTION_MOVE,
			            new ListDragGestureListenerWithImage(new DragSourceListener(){

							@Override
							public void dragDropEnd(DragSourceDropEvent dsde) {
								// TODO Auto-generated method stub
								
							}

							@Override
							public void dragEnter(DragSourceDragEvent dsde) {
								// TODO Auto-generated method stub
								
							}

							@Override
							public void dragExit(DragSourceEvent dse) {
								// TODO Auto-generated method stub
								
							}

							@Override
							public void dragOver(DragSourceDragEvent dsde) {
								// TODO Auto-generated method stub
								
							}

							@Override
							public void dropActionChanged(
									DragSourceDragEvent dsde) {
								// TODO Auto-generated method stub
								
							}},list2,config));
			textField3.setTransferHandler(new ObjectTransferHandler(config, new GetAndSetIssueRepo(new UpdateRepositoryData("SOURCE") )));
			textField6.setTransferHandler(new ObjectTransferHandler(config, new GetAndSetIssueRepo(new UpdateRepositoryData("PROJECT") )));
				
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					e.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	/**
	 * Validate as path refset.
	 *
	 * @param pathRefsetId the path refset id
	 * @param config the config
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public static boolean validateAsPathRefset(int pathRefsetId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		I_TermFactory tf = Terms.get();
		I_GetConceptData languageRefsetConcept = tf.getConcept(pathRefsetId);
		I_GetConceptData pathRefset = tf.getConcept(ArchitectonicAuxiliary.Concept.PATH.localize().getNid());
		I_IntSet allowedTypes = tf.newIntSet();
		//allowedTypes.add(ArchitectonicAuxiliary.Concept.HAS_RELEASE_PATH_REFSET_ATTRIBUTE.localize().getNid());
		allowedTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
		
		return pathRefset.isParentOf(languageRefsetConcept, config.getAllowedStatus(), config.getDestRelTypes(), config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
	}
	
	/**
	 * Validate as module refset.
	 *
	 * @param moduleRefsetId the module refset id
	 * @param config the config
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 */
	public static boolean validateAsModuleRefset(int moduleRefsetId, I_ConfigAceFrame config) throws IOException, TerminologyException {
		return false;
//		I_TermFactory tf = Terms.get();
//		I_GetConceptData languageRefsetConcept = tf.getConcept(languageRefsetId);
//		I_GetConceptData refsetTypeConcept = tf.getConcept(
//				RefsetAuxiliary.Concept.LANGUAGE_ENUMERATION_EXTENSION.getUids());
//		Set<? extends I_GetConceptData> refsetTypes = getSourceRelTarget(languageRefsetConcept, config, 
//				RefsetAuxiliary.Concept.REFSET_TYPE_REL.localize().getNid());
//		boolean isValid = false;
//		for (I_GetConceptData refsetType : refsetTypes) {
//			if (refsetType.getConceptNid() == refsetTypeConcept.getConceptNid()) {
//				isValid = true;
//			}
//		}
//		return isValid;
	}
	
	/**
	 * The Class UpdateRepositoryData.
	 */
	public class UpdateRepositoryData implements I_UpdateRepository{
		
		/** The source. */
		String source;
		
		/**
		 * Instantiates a new update repository data.
		 *
		 * @param source the source
		 */
		public UpdateRepositoryData(String source){
			this.source=source;
		}
		
		/* (non-Javadoc)
		 * @see org.ihtsdo.project.panel.dnd.I_UpdateRepository#update(org.ihtsdo.issue.issuerepository.IssueRepository)
		 */
		@Override
		public void update(IssueRepository issueRepository) {
			if (source.equals("PROJECT")){
				textField6.setText(issueRepository.getName());
				projectRepo=issueRepository;
				try {
					updateProjectRepoInfo();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}
			textField3.setText(issueRepository.getName());
			sourceRepo=issueRepository;
			try {
				updateSourceRepoInfo();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			
		}
		
	}
	
	/**
	 * Update source repo info.
	 *
	 * @throws Exception the exception
	 */
	private void updateSourceRepoInfo() throws Exception {
			if (sourceRepo != null) {
				textField3.setText(sourceRepo.getName());
				label24.setText(sourceRepo.getRepositoryId());
				label25.setText(sourceRepo.getUrl());
				IssueRepoRegistration sourceRepoReg = IssueRepositoryDAO.getRepositoryRegistration(
						sourceRepo.getUuid(), config);
				if (sourceRepoReg != null) {
					textField4.setText(sourceRepoReg.getUserId());
					textField5.setText(sourceRepoReg.getPassword());
				}
			}
	}
	
	/**
	 * Update project repo info.
	 *
	 * @throws Exception the exception
	 */
	private void updateProjectRepoInfo() throws Exception {
		if (projectRepo != null) {
			textField6.setText(projectRepo.getName());
			label31.setText(projectRepo.getRepositoryId());
			label33.setText(projectRepo.getUrl());
			IssueRepoRegistration projectRepoReg = IssueRepositoryDAO.getRepositoryRegistration(
					projectRepo.getUuid(), config);
			if (projectRepoReg != null) {
				textField7.setText(projectRepoReg.getUserId());
				textField8.setText(projectRepoReg.getPassword());
			}
		}
	}

	/**
	 * Update issue panel.
	 */
	private void updateIssuePanel() {
		updateList2();
		try {			
			if(project.getSourceIssueRepo() != null){
				sourceRepo = IssueRepositoryDAO.getIssueRepository(project.getSourceIssueRepo());
				updateSourceRepoInfo();
			}
			if(project.getProjectIssueRepo() != null){
				projectRepo = IssueRepositoryDAO.getIssueRepository(project.getProjectIssueRepo());
				updateProjectRepoInfo();
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
	 * Update list2.
	 */
	private void updateList2() {
		// List of repositories
		list2Model = new DefaultListModel();
		try {
			List<IssueRepository> repositories = IssueRepositoryDAO.getAllIssueRepository(config);
			Collections.sort(repositories,
					new Comparator<IssueRepository>()
					{
				public int compare(IssueRepository f1, IssueRepository f2)
				{
					return f1.toString().compareTo(f2.toString());
				}
					});
			for (IssueRepository repo : repositories) {
				list2Model.addElement(repo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		list2.setModel(list2Model);
		list2.validate();
	}

	/**
	 * Button13 action performed.
	 *
	 * @param e the e
	 */
	private void button13ActionPerformed(ActionEvent e) {
		// save issue repo info
		I_TermFactory tf = Terms.get();
		try {
			if (sourceRepo!= null && (project.getSourceIssueRepo() == null || 
					project.getSourceIssueRepo().getConceptNid() != sourceRepo.getConceptId())) {
				project.setSourceIssueRepo(tf.getConcept(sourceRepo.getConceptId()));
			}
			if (projectRepo!= null && (project.getProjectIssueRepo() == null || 
					project.getProjectIssueRepo().getConceptNid() != projectRepo.getConceptId())) {
				project.setProjectIssueRepo(tf.getConcept(projectRepo.getConceptId()));
			}

			if (sourceRepo!= null) {
				String passwordField5 = new String(textField5.getPassword());
				if (textField4.getText() != null && passwordField5 != null) {
					if (!textField4.getText().isEmpty() && !passwordField5.isEmpty()) {
						IssueRepoRegistration currentSourceRepoReg = 
							IssueRepositoryDAO.getRepositoryRegistration(sourceRepo.getUuid(), config);
						if (currentSourceRepoReg == null) {
							currentSourceRepoReg = new IssueRepoRegistration(sourceRepo.getUuid(), textField4.getText(),
									passwordField5,"");
							IssueRepositoryDAO.addRepositoryToProfile(currentSourceRepoReg);
						} else {
							if (!textField4.getText().equals(currentSourceRepoReg.getUserId()) || 
									!passwordField5.equals(currentSourceRepoReg.getPassword())) {
								currentSourceRepoReg.setUserId(textField4.getText());
								currentSourceRepoReg.setPassword(passwordField5);
								IssueRepositoryDAO.addRepositoryToProfile(currentSourceRepoReg);
							}
						}
					}
				}
			}

			if (projectRepo!= null) {
				String passwordField8 = new String(textField8.getPassword());
				if (textField7.getText() != null && passwordField8 != null) {
					if (!textField7.getText().isEmpty() && !passwordField8.isEmpty()) {
						IssueRepoRegistration currentProjectRepoReg = 
							IssueRepositoryDAO.getRepositoryRegistration(projectRepo.getUuid(), config);
						if (currentProjectRepoReg == null) {
							currentProjectRepoReg = new IssueRepoRegistration(projectRepo.getUuid(), textField7.getText(),
									passwordField8,"");
							IssueRepositoryDAO.addRepositoryToProfile(currentProjectRepoReg);
						} else {
							if (!textField7.getText().equals(currentProjectRepoReg.getUserId()) || 
									!passwordField8.equals(currentProjectRepoReg.getPassword())) {
								currentProjectRepoReg.setUserId(textField7.getText());
								currentProjectRepoReg.setPassword(passwordField8);
								IssueRepositoryDAO.addRepositoryToProfile(currentProjectRepoReg);
							}
						}
					}
				}
			}

		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		JOptionPane.showMessageDialog(this,
				"Project Issue Repository configuration saved!", 
				"Message", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Update list1 content.
	 */
	private void updateList1Content() {
		list1Model = new DefaultListModel();
		List<WorkList> utwWorklists = TerminologyProjectDAO.getAllNacWorkLists(
				project, config);
		if (!utwWorklists.isEmpty()) {
			Collections.sort(utwWorklists, new Comparator<WorkList>() {
				public int compare(WorkList f1, WorkList f2) {
					return f1.toString().compareTo(f2.toString());
				}
			});

			for (WorkList workList : utwWorklists) {
				list1Model.addElement(workList);
			}
			list1.setModel(list1Model);
			list1.validate();
		}
	}

	/**
	 * Update list7 content.
	 */
	private void updateList7Content() {
		list7Model = new DefaultListModel();
		List<WorkSet> worksets = project.getWorkSets(config);
		Collections.sort(worksets,
				new Comparator<WorkSet>()
				{
			public int compare(WorkSet f1, WorkSet f2)
			{
				return f1.toString().compareTo(f2.toString());
			}
				});

		for (WorkSet workSet : worksets) {
			list7Model.addElement(workSet);
		}
		list7.setModel(list7Model);
		list7.validate();
	}

	/**
	 * Text field1 key typed.
	 *
	 * @param e the e
	 */
	private void textField1KeyTyped(KeyEvent e) {
		if (textField1.getText().equals(project.getName())) {
			button3.setEnabled(false);
		} else {
			button3.setEnabled(true);
		}
	}

	/**
	 * Button3 action performed.
	 *
	 * @param e the e
	 */
	private void button3ActionPerformed(ActionEvent e) {
		try {
			project.setName(textField1.getText());
			project.setModuleIdRefset((I_GetConceptData) (moduleIdModel.isEmpty() ? null : moduleIdModel.get(0)));
			project.setReleasePathRefset((I_GetConceptData) (releaseCandidateListModel.isEmpty() ? null : releaseCandidateListModel.get(0)));
			String namespaceText = namespaceTextField.getText();
			if(!namespaceText.trim().equals("")){
				try{
					if(namespaceText.length() == 7){
						Integer.valueOf(namespaceText);
					}else{
						JOptionPane.showMessageDialog(ProjectDetailsPanel.this,
								"Namespace wont be saved, it must be a 7 digit number.", 
								"Warning", JOptionPane.WARNING_MESSAGE);
					}
					project.setNamespaceRefset(namespaceText);
				}catch (Exception nfx) {
					JOptionPane.showMessageDialog(ProjectDetailsPanel.this,
							"Namespace wont be saved, it must be a 7 digit number.", 
							"Warning", JOptionPane.WARNING_MESSAGE);
				}
			}
			
			TerminologyProjectDAO.updateTranslationProjectMetadata(project, config);
			Terms.get().commit();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		button3.setEnabled(false);
		TranslationHelperPanel.refreshProjectPanelNode(config);
		JOptionPane.showMessageDialog(this, "Project saved!", "Message", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Button4 action performed.
	 *
	 * @param e the e
	 */
	private void button4ActionPerformed(ActionEvent e) {
		try {
			ListModel currentModel = list4.getModel();
			List<I_GetConceptData> currentExclusions = project.getExclusionRefsets();
			List<Integer> exclusionIds = new ArrayList<Integer>();
			for (I_GetConceptData exclusion : currentExclusions) {
				exclusionIds.add(exclusion.getConceptNid());
			}
			List<Integer> newExclusionIds = new ArrayList<Integer>();
			for (int i = 0;i < currentModel.getSize();i++) {
				I_GetConceptData listItem = (I_GetConceptData) currentModel.getElementAt(i);
				newExclusionIds.add(listItem.getConceptNid());
				if (!exclusionIds.contains(listItem.getConceptNid())) {
					TerminologyProjectDAO.addRefsetAsExclusion(project, listItem, config);
				}
			}
			Terms.get().commit();

			for (I_GetConceptData exclusion : currentExclusions) {
				if (!newExclusionIds.contains(exclusion.getConceptNid())) {
					TerminologyProjectDAO.removeRefsetAsExclusion(project, exclusion, config);
				}
			}
			Terms.get().commit();
			TranslationHelperPanel.refreshProjectPanelNode(config);
			JOptionPane.showMessageDialog(this,
					"Project saved!", 
					"Message", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(this,
					e1.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}

	}

	/**
	 * List4 key typed.
	 *
	 * @param e the e
	 */
	private void list4KeyTyped(KeyEvent e) {
		//		System.out.println(e.getKeyCode());
		//		System.out.println(e.getKeyChar());
		//		System.out.println();
		//		System.out.println(e.getKeyText(e.getKeyCode()));
		String keyChar = String.valueOf(e.getKeyChar());
		//		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
		if ("d".equals(keyChar)) {
			removeSelectedList4Items();
		}
	}

	/**
	 * Removes the selected list4 items.
	 */
	private void removeSelectedList4Items() {
		if(list4.getSelectedIndices().length > 0) {
			int[] tmp = list4.getSelectedIndices();
			int[] selectedIndices = list4.getSelectedIndices();

			for (int i = tmp.length-1; i >=0; i--) {
				selectedIndices = list4.getSelectedIndices();
				list4Model.removeElementAt(selectedIndices[i]);
			} // end-for
		} //
	}

	/**
	 * List5 key typed.
	 *
	 * @param e the e
	 */
	private void list5KeyTyped(KeyEvent e) {
		String keyChar = String.valueOf(e.getKeyChar());
		if ("d".equals(keyChar)) {
			removeSelectedList5Items();
		}
	}

	/**
	 * Removes the selected list5 items.
	 */
	private void removeSelectedList5Items() {
		if(list5.getSelectedIndices().length > 0) {
			int[] tmp = list5.getSelectedIndices();
			int[] selectedIndices = list5.getSelectedIndices();

			for (int i = tmp.length-1; i >=0; i--) {
				selectedIndices = list5.getSelectedIndices();
				list5Model.removeElementAt(selectedIndices[i]);
			} // end-for
		} //
	}

	/**
	 * Button5 action performed.
	 *
	 * @param e the e
	 */
	private void button5ActionPerformed(ActionEvent e) {
		try {
			ListModel currentModel = list5.getModel();
			List<I_GetConceptData> currentCommons = project.getCommonRefsets();
			List<Integer> commonIds = new ArrayList<Integer>();
			for (I_GetConceptData common : currentCommons) {
				commonIds.add(common.getConceptNid());
			}
			List<Integer> newCommonIds = new ArrayList<Integer>();
			for (int i = 0;i < currentModel.getSize();i++) {
				I_GetConceptData listItem = (I_GetConceptData) currentModel.getElementAt(i);
				newCommonIds.add(listItem.getConceptNid());
				if (!commonIds.contains(listItem.getConceptNid())) {
					TerminologyProjectDAO.addRefsetAsCommon(project, listItem, config);
				}
			}
			Terms.get().commit();

			for (I_GetConceptData common : currentCommons) {
				if (!newCommonIds.contains(common.getConceptNid())) {
					TerminologyProjectDAO.removeRefsetAsCommon(project, common, config);
				}
			}
			Terms.get().commit();
			TranslationHelperPanel.refreshProjectPanelNode(config);
			JOptionPane.showMessageDialog(this,
					"Project saved!", 
					"Message", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(this,
					e1.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}
	}

	/**
	 * List6 key typed.
	 *
	 * @param e the e
	 */
	private void list6KeyTyped(KeyEvent e) {
		String keyChar = String.valueOf(e.getKeyChar());
		if ("d".equals(keyChar)) {
			removeSelectedList6Items();
		}
	}

	/**
	 * List8 key typed.
	 *
	 * @param e the e
	 */
	private void list8KeyTyped(KeyEvent e) {
		String keyChar = String.valueOf(e.getKeyChar());
		if ("d".equals(keyChar)) {
			removeSelectedList8Items();
		}
	}

	/**
	 * Removes the selected list6 items.
	 */
	private void removeSelectedList6Items() {
		if(list6.getSelectedIndices().length > 0) {
			int[] tmp = list6.getSelectedIndices();
			int[] selectedIndices = list6.getSelectedIndices();

			for (int i = tmp.length-1; i >=0; i--) {
				selectedIndices = list6.getSelectedIndices();
				list6Model.removeElementAt(selectedIndices[i]);
			} // end-for
		} //
	}
	
	/**
	 * Removes the selected list8 items.
	 */
	private void removeSelectedList8Items() {
		if(list8.getSelectedIndices().length > 0) {
			int[] tmp = list8.getSelectedIndices();
			int[] selectedIndices = list8.getSelectedIndices();

			for (int i = tmp.length-1; i >=0; i--) {
				selectedIndices = list8.getSelectedIndices();
				list8Model.removeElementAt(selectedIndices[i]);
			} // end-for
		} //
	}

	/**
	 * Button6 action performed.
	 *
	 * @param e the e
	 */
	private void button6ActionPerformed(ActionEvent e) {
		try {
			if(list6Model.getSize() > 0 && list8Model.getSize() > 0){
				
				ListModel currentModel = list6.getModel();
				List<I_GetConceptData> currentSources = project.getSourceLanguageRefsets();
				List<Integer> sourcesIds = new ArrayList<Integer>();
				for (I_GetConceptData source : currentSources) {
					sourcesIds.add(source.getConceptNid());
				}
				List<Integer> newSourceIds = new ArrayList<Integer>();
				for (int i = 0;i < currentModel.getSize();i++) {
					I_GetConceptData listItem = (I_GetConceptData) currentModel.getElementAt(i);
					newSourceIds.add(listItem.getConceptNid());
					if (!sourcesIds.contains(listItem.getConceptNid())) {
						TerminologyProjectDAO.addRefsetAsSourceLanguage(project, listItem, config);
					}
				}
				Terms.get().commit();
				
				for (I_GetConceptData source : currentSources) {
					if (!newSourceIds.contains(source.getConceptNid())) {
						TerminologyProjectDAO.removeRefsetAsSourceLanguage(project, source, config);
					}
				}
				Terms.get().commit();
				
				//			I_GetConceptData currentSources = project.getSourceLanguageRefsets();
				//			if (targetLanguageLabel.getTermComponent() != null) {
				//				if (project.getTargetLanguageRefset() == null) {
				//					I_GetConceptData selectedTargetRefset = 
				//						Terms.get().getConcept(targetLanguageLabel.getTermComponent().getNid());
				//
				//					project.setTargetLanguageRefset(selectedTargetRefset);
				//				} else if (targetLanguageLabel.getTermComponent().getNid() != project.getTargetLanguageRefset().getConceptId()) {
				//					I_GetConceptData selectedTargetRefset = 
				//						Terms.get().getConcept(targetLanguageLabel.getTermComponent().getNid());
				//
				//					project..setTargetLanguageRefset(selectedTargetRefset);
				//				}
				//			}
				
				currentModel = list8.getModel();
				if (currentModel.getSize()>0){
					project.setTargetLanguageRefset((I_GetConceptData)currentModel.getElementAt(0));
				}else{
					project.setTargetLanguageRefset(null);
				}
				
				Terms.get().commit();
				
				TranslationHelperPanel.refreshProjectPanelNode(config);
				JOptionPane.showMessageDialog(this,
						"Project saved!", 
						"Message", JOptionPane.INFORMATION_MESSAGE);
			}else{
				if(list6Model.getSize() > 0 && list8Model.getSize() <= 0){
					JOptionPane.showMessageDialog(this,
							"Please define the target language.", 
							"Message", JOptionPane.INFORMATION_MESSAGE);
				}else if(list6Model.getSize() <= 0 && list8Model.getSize() > 0){
					JOptionPane.showMessageDialog(this,
							"Please define the source language.", 
							"Message", JOptionPane.INFORMATION_MESSAGE);
				}else if(list6Model.getSize() <= 0 && list8Model.getSize() <= 0){
					JOptionPane.showMessageDialog(this,
							"Please define the source and target languages.", 
							"Message", JOptionPane.INFORMATION_MESSAGE);
				}
			}

		} catch (Exception e3) {
			JOptionPane.showMessageDialog(this,
					e3.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			e3.printStackTrace();
		}
	}

	/**
	 * Button2 action performed.
	 *
	 * @param e the e
	 */
	private void button2ActionPerformed(ActionEvent e) {
		// retire project
		int n = JOptionPane.showConfirmDialog(
				this,
				"Would you like to retire the project?",
				"Confirmation",
				JOptionPane.YES_NO_OPTION);

		//System.out.println("Would you like to retire the project? " + n);

		if (n==0) {
			try {
				TerminologyProjectDAO.retireProject(project, config);
				Terms.get().commit();
				TranslationHelperPanel.refreshProjectPanelParentNode(config);
				TranslationHelperPanel.closeProjectDetailsTab(config);
			} catch (Exception e3) {
				JOptionPane.showMessageDialog(this,
						e3.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE);
				e3.printStackTrace();
			}
		}
	}

	/**
	 * Button1 action performed.
	 *
	 * @param e the e
	 */
	private void button1ActionPerformed(ActionEvent e) {
		// create workset

		try {
			if (project.getSourceLanguageRefsets().isEmpty() || project.getTargetLanguageRefset() == null) {
				JOptionPane.showMessageDialog(this,
						"<html><body> Warning. Source or Target languages <br>are not saved in the language tab.", 
						"Message", JOptionPane.WARNING_MESSAGE);
			}
		} catch (HeadlessException e2) {
			e2.printStackTrace();
		} catch (TerminologyException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		String workSetName = JOptionPane.showInputDialog(null, "Enter WorkSet Name : ", 
				"", 1);
		if (workSetName != null) {
			if(TerminologyProjectDAO.createNewWorkSet(workSetName, project.getUids().iterator().next(), config) != null){
				try {
					Terms.get().commit();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				updateList7Content();
				JOptionPane.showMessageDialog(this,
						"WorkSet created!", 
						"Message", JOptionPane.INFORMATION_MESSAGE);
				
				SwingUtilities.invokeLater(new Runnable(){
					public void run(){
						TranslationHelperPanel.refreshProjectPanelNode(config);
					}
				});
			}

		}
	}

	/**
	 * The Class PopUpList4.
	 */
	class PopUpList4 extends JPopupMenu implements ActionListener {
		
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;
		
		/** The an item. */
		JMenuItem anItem;
		
		/**
		 * Instantiates a new pop up list4.
		 */
		public PopUpList4(){
			anItem = new JMenuItem("Remove selected items");
			anItem.addActionListener(this);
			add(anItem);
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			removeSelectedList4Items();
		}
	}

	/**
	 * The Class PopUpList5.
	 */
	class PopUpList5 extends JPopupMenu implements ActionListener {
		
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;
		
		/** The an item. */
		JMenuItem anItem;
		
		/**
		 * Instantiates a new pop up list5.
		 */
		public PopUpList5(){
			anItem = new JMenuItem("Remove selected items");
			anItem.addActionListener(this);
			add(anItem);
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			removeSelectedList5Items();
		}
	}

	/**
	 * The Class PopUpList6.
	 */
	class PopUpList6 extends JPopupMenu implements ActionListener {
		
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;
		
		/** The an item. */
		JMenuItem anItem;
		
		/**
		 * Instantiates a new pop up list6.
		 */
		public PopUpList6(){
			anItem = new JMenuItem("Remove selected items");
			anItem.addActionListener(this);
			add(anItem);
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			removeSelectedList6Items();
		}
	}
	
	/**
	 * The Class PopUpList8.
	 */
	class PopUpList8 extends JPopupMenu implements ActionListener {
		
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;
		
		/** The an item. */
		JMenuItem anItem;
		
		/**
		 * Instantiates a new pop up list8.
		 */
		public PopUpList8(){
			anItem = new JMenuItem("Remove selected items");
			anItem.addActionListener(this);
			add(anItem);
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			removeSelectedList8Items();
		}
	}

	/**
	 * List4 mouse pressed.
	 *
	 * @param e the e
	 */
	private void list4MousePressed(MouseEvent e) {
		if (e.isPopupTrigger())
			doList4Pop(e);
	}

	/**
	 * List4 mouse released.
	 *
	 * @param e the e
	 */
	private void list4MouseReleased(MouseEvent e) {
		if (e.isPopupTrigger())
			doList4Pop(e);
	}

	/**
	 * Do list4 pop.
	 *
	 * @param e the e
	 */
	private void doList4Pop(MouseEvent e){
		PopUpList4 menu = new PopUpList4();
		menu.show(e.getComponent(), e.getX(), e.getY());
	}

	/**
	 * List5 mouse pressed.
	 *
	 * @param e the e
	 */
	private void list5MousePressed(MouseEvent e) {
		if (e.isPopupTrigger())
			doList5Pop(e);
	}

	/**
	 * List5 mouse released.
	 *
	 * @param e the e
	 */
	private void list5MouseReleased(MouseEvent e) {
		if (e.isPopupTrigger())
			doList5Pop(e);
	}

	/**
	 * Do list5 pop.
	 *
	 * @param e the e
	 */
	private void doList5Pop(MouseEvent e){
		PopUpList5 menu = new PopUpList5();
		menu.show(e.getComponent(), e.getX(), e.getY());
	}

	/**
	 * List6 mouse pressed.
	 *
	 * @param e the e
	 */
	private void list6MousePressed(MouseEvent e) {
		if (e.isPopupTrigger())
			doList6Pop(e);
	}
	
	/**
	 * List8 mouse pressed.
	 *
	 * @param e the e
	 */
	private void list8MousePressed(MouseEvent e) {
		if (e.isPopupTrigger())
			doList8Pop(e);
	}


	/**
	 * List6 mouse released.
	 *
	 * @param e the e
	 */
	private void list6MouseReleased(MouseEvent e) {
		if (e.isPopupTrigger())
			doList6Pop(e);
	}
	
	/**
	 * List8 mouse released.
	 *
	 * @param e the e
	 */
	private void list8MouseReleased(MouseEvent e) {
		if (e.isPopupTrigger())
			doList8Pop(e);
	}

	/**
	 * Do list6 pop.
	 *
	 * @param e the e
	 */
	private void doList6Pop(MouseEvent e){
		PopUpList6 menu = new PopUpList6();
		menu.show(e.getComponent(), e.getX(), e.getY());
	}
	
	/**
	 * Do list8 pop.
	 *
	 * @param e the e
	 */
	private void doList8Pop(MouseEvent e){
		PopUpList8 menu = new PopUpList8();
		menu.show(e.getComponent(), e.getX(), e.getY());
	}

	/**
	 * Button8 action performed.
	 *
	 * @param e the e
	 */
	private void button8ActionPerformed(ActionEvent e) {
		removeSelectedList4Items();
	}

	/**
	 * Button9 action performed.
	 *
	 * @param e the e
	 */
	private void button9ActionPerformed(ActionEvent e) {
		removeSelectedList5Items();
	}

	/**
	 * Button10 action performed.
	 *
	 * @param e the e
	 */
	private void button10ActionPerformed(ActionEvent e) {
		removeSelectedList6Items();
		if (list6Model.size() == 0) {
			JOptionPane.showMessageDialog(this,
					"Warning, source languages list is empty.", 
					"Warning", JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Button12 action performed.
	 *
	 * @param e the e
	 */
	private void button12ActionPerformed(ActionEvent e) {
		// generate worklist
		WfComponentProvider wcp = new WfComponentProvider();
		List<WfUser> users = wcp.getUsers();
		WizardLauncher wl = new WizardLauncher();
		wl.launchWfWizard(users);
		HashMap<String, Object> hsRes = wl.getResult();
		List<WfRole> roles = null;
		noName = "no name " + UUID.randomUUID().toString();
		workflowDefinition = null;
		final ArrayList<WfMembership> workflowUserRoles = new ArrayList<WfMembership>();
		
		for (String key : hsRes.keySet()) {
			Object val = hsRes.get(key);
			if (key.equals("WDS")) {
				workflowDefinition = WorkflowDefinitionManager.readWfDefinition(((File) val).getName());
				roles = workflowDefinition.getRoles();

			}

			if (key.equals("WORKLIST_NAME")) {
				noName = (String) val;
			}
			if (key.equals("roles")) {
				roles= wcp.getRoles();
				users= wcp.getUsers();
				DefaultTableModel model = (DefaultTableModel) hsRes.get(key);
				for (int j=1; j<model.getColumnCount();j+=2){
					WfRole role=null;
					for (WfRole wfRole : roles) {
						if(wfRole.getName().equals(model.getColumnName(j))){
							role=wfRole;
							break;
						}
					}
					for (int i = 0; i < model.getRowCount(); i++) {
						Boolean sel = (Boolean) model.getValueAt(i, j);
						if (sel) {
							Boolean def = (Boolean) model.getValueAt(i, j+1);
							WfUser user= null; 
							for (WfUser wfUser : users) {
								if(wfUser.getId().equals(((WfUser)model.getValueAt(i, 0)).getId())){
									user=wfUser;
									break;
								}
							}
							WfMembership workflowUserRole = new WfMembership(UUID.randomUUID(), user, role, def);
							workflowUserRoles.add(workflowUserRole);
						}
					}
				}
			}
		}
		
		final I_ShowActivity activity = Terms.get().newActivityPanel(true, config, "<html>Generating Worklist from partition", true);
		activity.setIndeterminate(true);
		final Long startTime = System.currentTimeMillis();
		activity.update();
		final SwingWorker<String, String> worker = new SwingWorker<String, String>() {
			@Override
			protected String doInBackground() throws Exception {

				try {
					TerminologyProjectDAO.createNewNacWorkList(project, workflowDefinition, 
							workflowUserRoles, noName, config, activity);
				} catch (Exception e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(ProjectDetailsPanel.this, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}

				updateList1Content();
				
				TranslationHelperPanel.refreshProjectPanelNode(config);
				JOptionPane.showMessageDialog(ProjectDetailsPanel.this, "Worklist created...", "Project Manager", JOptionPane.INFORMATION_MESSAGE);
				return null;
			}

			@Override
			protected void done() {
				try {
					get();
					long endTime = System.currentTimeMillis();

					long elapsed = endTime - startTime;
					String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);

					activity.setProgressInfoUpper("Worklist created...");
					activity.setProgressInfoLower("Elapsed: " + elapsedStr);
					activity.complete();

				} catch (CancellationException ce) {
					activity.setProgressInfoLower("Canceled");
					try {
						activity.complete();
					} catch (ComputationCanceled e) {
						activity.setProgressInfoLower("Canceled");
					}
				} catch (Exception e){
					activity.setProgressInfoLower("Canceled with error");
					e.printStackTrace();
				}
			}

		};
		worker.execute();
		activity.addStopActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				worker.cancel(true);
			}
		});
		try {
			ActivityViewer.addActivity(activity);
		} catch (InterruptedException i1) {
			// thread canceled, cancel db changes
			try {
				Terms.get().cancel();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (Exception e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}
//		utwBusinessProcess = null;
//		File selectedFileLink = (File) comboBox1.getSelectedItem();
//		
//		if (selectedFileLink != null) {
//			WorkflowDefinition wDef= WorkflowDefinitionManager.readWfDefinition(selectedFileLink.getAbsolutePath());
//		}
//		
//		if (textField2 == null || textField2.getText().isEmpty() || utwBusinessProcess == null) {
//			JOptionPane.showMessageDialog(this,
//					"Missing data, complete name and choose bp file...", 
//					"Error", JOptionPane.ERROR_MESSAGE);
//		} else {
//			try {
//				TerminologyProjectDAO.createNewNacWorkList(textField2.getText(), wDef, project, config);
//			} catch (TerminologyException e1) {
//				JOptionPane.showMessageDialog(this,
//						"Error, check logs", 
//						"Error", JOptionPane.ERROR_MESSAGE);
//				e1.printStackTrace();
//			} catch (IOException e1) {
//				JOptionPane.showMessageDialog(this,
//						"Error, check logs", 
//						"Error", JOptionPane.ERROR_MESSAGE);
//				e1.printStackTrace();
//			} catch (Exception e2) {
//				JOptionPane.showMessageDialog(this,
//						"Error, check logs", 
//						"Error", JOptionPane.ERROR_MESSAGE);
//				e2.printStackTrace();
//			}
//			textField2.setText("");
//			utwBusinessProcess = null;

//		}
	}

	/**
	 * Button14 action performed.
	 */
	private void button14ActionPerformed() {
		JFrame frame =  new JFrame("New Issue Repository");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		CreateIssuerepositoryPanel ipanel=new CreateIssuerepositoryPanel();
		frame.getContentPane().setLayout(new BorderLayout(10,10));
		frame.getContentPane().add(ipanel);
		frame.addWindowListener(new WindowListener (){

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosed(WindowEvent e) {
				updateList2();
			}

			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}});
		frame.setSize(new Dimension(360,200));
		frame.setVisible(true);
	}

	/**
	 * Label18 mouse clicked.
	 *
	 * @param e the e
	 */
	private void label18MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("PROJECT_DETAILS");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Issues help lbl mouse clicked.
	 *
	 * @param e the e
	 */
	private void issuesHelpLblMouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("ISSUES_HELP");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Label38 mouse clicked.
	 *
	 * @param e the e
	 */
	private void label38MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("MAINTENANCE_WORKFLOW");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	private void namespaceTextFieldKeyTyped(KeyEvent e) {
		String namespaceRefset = null;
		try {
			namespaceRefset = project.getNamespaceRefset();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (namespaceRefset != null && namespaceTextField.getText().equals(namespaceRefset)) {
			button3.setEnabled(false);
		} else {
			button3.setEnabled(true);
		}
	}


	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		tabbedPane1 = new JTabbedPane();
		panel0 = new JPanel();
		panel1 = new JPanel();
		label1 = new JLabel();
		panel2 = new JPanel();
		label2 = new JLabel();
		textField1 = new JTextField();
		label41 = new JLabel();
		namespaceTextField = new JTextField();
		separator1 = new JSeparator();
		label39 = new JLabel();
		releaseCandidateList = new JList();
		label40 = new JLabel();
		moduleIdList = new JList();
		panel4 = new JPanel();
		button1 = new JButton();
		button2 = new JButton();
		panel9 = new JPanel();
		label18 = new JLabel();
		label7 = new JLabel();
		panel12 = new JPanel();
		button3 = new JButton();
		panel5 = new JPanel();
		label3 = new JLabel();
		scrollPane4 = new JScrollPane();
		list4 = new JList();
		panel8 = new JPanel();
		label8 = new JLabel();
		panel13 = new JPanel();
		panel19 = new JPanel();
		button4 = new JButton();
		button8 = new JButton();
		panel6 = new JPanel();
		label4 = new JLabel();
		scrollPane5 = new JScrollPane();
		list5 = new JList();
		panel10 = new JPanel();
		label9 = new JLabel();
		panel14 = new JPanel();
		panel20 = new JPanel();
		button5 = new JButton();
		button9 = new JButton();
		panel7 = new JPanel();
		label5 = new JLabel();
		scrollPane6 = new JScrollPane();
		panel22 = new JPanel();
		list6 = new JList();
		panel23 = new JPanel();
		button10 = new JButton();
		vSpacer1 = new JPanel(null);
		label6 = new JLabel();
		list8 = new JList();
		panel24 = new JPanel();
		panel11 = new JPanel();
		label10 = new JLabel();
		panel15 = new JPanel();
		button6 = new JButton();
		panel16 = new JPanel();
		label11 = new JLabel();
		scrollPane7 = new JScrollPane();
		list7 = new JList();
		panel17 = new JPanel();
		label12 = new JLabel();
		panel18 = new JPanel();
		button7 = new JButton();
		panel3 = new JPanel();
		label14 = new JLabel();
		label38 = new JLabel();
		scrollPane1 = new JScrollPane();
		list1 = new JList();
		panel21 = new JPanel();
		label13 = new JLabel();
		panel25 = new JPanel();
		button12 = new JButton();
		panel26 = new JPanel();
		panel27 = new JPanel();
		issuesHelpLbl = new JLabel();
		label19 = new JLabel();
		label20 = new JLabel();
		panel28 = new JPanel();
		label21 = new JLabel();
		textField3 = new JTextField();
		label22 = new JLabel();
		label24 = new JLabel();
		label23 = new JLabel();
		label25 = new JLabel();
		label26 = new JLabel();
		label27 = new JLabel();
		textField4 = new JTextField();
		label28 = new JLabel();
		textField5 = new JPasswordField();
		panel29 = new JPanel();
		label29 = new JLabel();
		textField6 = new JTextField();
		label30 = new JLabel();
		label31 = new JLabel();
		label32 = new JLabel();
		label33 = new JLabel();
		label34 = new JLabel();
		label35 = new JLabel();
		textField7 = new JTextField();
		label36 = new JLabel();
		textField8 = new JPasswordField();
		panel31 = new JPanel();
		label37 = new JLabel();
		scrollPane2 = new JScrollPane();
		list2 = new JList();
		panel32 = new JPanel();
		button14 = new JButton();
		panel30 = new JPanel();
		button13 = new JButton();

		//======== this ========
		setBackground(new Color(238, 238, 238));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

		//======== tabbedPane1 ========
		{

			//======== panel0 ========
			{
				panel0.setLayout(new GridBagLayout());
				((GridBagLayout)panel0.getLayout()).columnWidths = new int[] {226, 230, 0};
				((GridBagLayout)panel0.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel0.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
				((GridBagLayout)panel0.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

				//======== panel1 ========
				{
					panel1.setLayout(new GridBagLayout());
					((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0};
					((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
					((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

					//---- label1 ----
					label1.setText("Translation project details");
					label1.setFont(new Font("Lucida Grande", Font.BOLD, 14));
					panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//======== panel2 ========
					{
						panel2.setLayout(new GridBagLayout());
						((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {127, 307, 0};
						((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 14, 35, 30, 0};
						((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
						((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

						//---- label2 ----
						label2.setText("Name");
						panel2.add(label2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- textField1 ----
						textField1.addKeyListener(new KeyAdapter() {
							@Override
							public void keyTyped(KeyEvent e) {
								textField1KeyTyped(e);
							}
						});
						panel2.add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//---- label41 ----
						label41.setText("Namespace");
						panel2.add(label41, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- namespaceTextField ----
						namespaceTextField.addKeyListener(new KeyAdapter() {
							@Override
							public void keyTyped(KeyEvent e) {
								namespaceTextFieldKeyTyped(e);
							}
						});
						panel2.add(namespaceTextField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));
						panel2.add(separator1, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//---- label39 ----
						label39.setText("<html>Release candidate<br>path");
						panel2.add(label39, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
							GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- releaseCandidateList ----
						releaseCandidateList.setVisibleRowCount(1);
						releaseCandidateList.setBorder(new EtchedBorder());
						panel2.add(releaseCandidateList, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//---- label40 ----
						label40.setText("<html>Module");
						panel2.add(label40, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
							GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- moduleIdList ----
						moduleIdList.setBorder(new EtchedBorder());
						moduleIdList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						panel2.add(moduleIdList, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					panel1.add(panel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//======== panel4 ========
					{
						panel4.setLayout(new GridBagLayout());
						((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {0, 0, 0};
						((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0};
						((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
						((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

						//---- button1 ----
						button1.setText("Create new WorkSet");
						button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
						button1.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								button1ActionPerformed(e);
							}
						});
						panel4.add(button1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- button2 ----
						button2.setText("Retire project");
						button2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
						button2.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								button2ActionPerformed(e);
							}
						});
						panel4.add(button2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					panel1.add(panel4, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel0.add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//======== panel9 ========
				{
					panel9.setLayout(new GridBagLayout());
					((GridBagLayout)panel9.getLayout()).columnWidths = new int[] {235, 0, 0};
					((GridBagLayout)panel9.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
					((GridBagLayout)panel9.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
					((GridBagLayout)panel9.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};

					//---- label18 ----
					label18.setText("text");
					label18.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							label18MouseClicked(e);
						}
					});
					panel9.add(label18, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label7 ----
					label7.setText("<html><body>\nEnter translation project name<br><br>\n\nPress \u2018New Workset\u2019  for creating a new workset for this project<br><br>\n\nPress \u2018Save\u2019 for persisting changes<br><br>\n\nPress \u2018Retire project\u2019  to retire this project. Project needs to be empty or retiring will not succeed\n</html>");
					label7.setBackground(new Color(238, 238, 238));
					panel9.add(label7, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
						GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 5, 0), 0, 0));

					//======== panel12 ========
					{
						panel12.setLayout(new GridBagLayout());
						((GridBagLayout)panel12.getLayout()).columnWidths = new int[] {0, 0};
						((GridBagLayout)panel12.getLayout()).rowHeights = new int[] {0, 0};
						((GridBagLayout)panel12.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
						((GridBagLayout)panel12.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

						//---- button3 ----
						button3.setText("Save");
						button3.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
						button3.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								button3ActionPerformed(e);
							}
						});
						panel12.add(button3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					panel9.add(panel12, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 0, 5), 0, 0));
				}
				panel0.add(panel9, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			tabbedPane1.addTab("Project", panel0);


			//======== panel5 ========
			{
				panel5.setLayout(new GridBagLayout());
				((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {413, 0, 0};
				((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
				((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

				//---- label3 ----
				label3.setText("Exclusion Refsets");
				panel5.add(label3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== scrollPane4 ========
				{

					//---- list4 ----
					list4.addKeyListener(new KeyAdapter() {
						@Override
						public void keyTyped(KeyEvent e) {
							list4KeyTyped(e);
						}
					});
					list4.addMouseListener(new MouseAdapter() {
						@Override
						public void mousePressed(MouseEvent e) {
							list4MousePressed(e);
						}
						@Override
						public void mouseReleased(MouseEvent e) {
							list4MouseReleased(e);
						}
					});
					scrollPane4.setViewportView(list4);
				}
				panel5.add(scrollPane4, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel8 ========
				{
					panel8.setLayout(new GridBagLayout());
					((GridBagLayout)panel8.getLayout()).columnWidths = new int[] {230, 0};
					((GridBagLayout)panel8.getLayout()).rowHeights = new int[] {0, 0, 0};
					((GridBagLayout)panel8.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
					((GridBagLayout)panel8.getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0E-4};

					//---- label8 ----
					label8.setText("<html> <body>\nDrag and drop a new refset for adding a new exclusion<br><br>\n\nPress \u2018Save\u2019 for persisting changes<br><br>\n\nSelect a refset and type \u2018d\u2019 for removing it<br><br>\n</html>");
					panel8.add(label8, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 5, 0), 0, 0));

					//======== panel13 ========
					{
						panel13.setLayout(new GridBagLayout());
						((GridBagLayout)panel13.getLayout()).columnWidths = new int[] {0, 0};
						((GridBagLayout)panel13.getLayout()).rowHeights = new int[] {0, 0};
						((GridBagLayout)panel13.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
						((GridBagLayout)panel13.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
					}
					panel8.add(panel13, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel5.add(panel8, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel19 ========
				{
					panel19.setLayout(new GridBagLayout());
					((GridBagLayout)panel19.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel19.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel19.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel19.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- button4 ----
					button4.setText("Save");
					button4.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button4.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button4ActionPerformed(e);
						}
					});
					panel19.add(button4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- button8 ----
					button8.setText("Remove selected refsets");
					button8.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button8.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button8ActionPerformed(e);
						}
					});
					panel19.add(button8, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel5.add(panel19, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));
			}
			tabbedPane1.addTab("Exclusion Refsets", panel5);


			//======== panel6 ========
			{
				panel6.setLayout(new GridBagLayout());
				((GridBagLayout)panel6.getLayout()).columnWidths = new int[] {459, 165, 0};
				((GridBagLayout)panel6.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel6.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
				((GridBagLayout)panel6.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

				//---- label4 ----
				label4.setText("Linked Refsets");
				panel6.add(label4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== scrollPane5 ========
				{

					//---- list5 ----
					list5.addKeyListener(new KeyAdapter() {
						@Override
						public void keyTyped(KeyEvent e) {
							list5KeyTyped(e);
						}
					});
					list5.addMouseListener(new MouseAdapter() {
						@Override
						public void mousePressed(MouseEvent e) {
							list5MousePressed(e);
						}
						@Override
						public void mouseReleased(MouseEvent e) {
							list5MouseReleased(e);
						}
					});
					scrollPane5.setViewportView(list5);
				}
				panel6.add(scrollPane5, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel10 ========
				{
					panel10.setLayout(new GridBagLayout());
					((GridBagLayout)panel10.getLayout()).columnWidths = new int[] {230, 0};
					((GridBagLayout)panel10.getLayout()).rowHeights = new int[] {0, 0, 0};
					((GridBagLayout)panel10.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
					((GridBagLayout)panel10.getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0E-4};

					//---- label9 ----
					label9.setText("<html><body>\nDrag and drop a new refset for adding a new linked refset<br><br>\n\nPress \u2018Save\u2019 for persisting changes<br><br>\n\nSelect a refset and type \u2018d\u2019 for removing it\n</html>");
					panel10.add(label9, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 5, 0), 0, 0));

					//======== panel14 ========
					{
						panel14.setLayout(new GridBagLayout());
						((GridBagLayout)panel14.getLayout()).columnWidths = new int[] {0, 0};
						((GridBagLayout)panel14.getLayout()).rowHeights = new int[] {0, 0};
						((GridBagLayout)panel14.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
						((GridBagLayout)panel14.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
					}
					panel10.add(panel14, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel6.add(panel10, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel20 ========
				{
					panel20.setLayout(new GridBagLayout());
					((GridBagLayout)panel20.getLayout()).columnWidths = new int[] {0, 0, 18, 0};
					((GridBagLayout)panel20.getLayout()).rowHeights = new int[] {20, 0};
					((GridBagLayout)panel20.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel20.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- button5 ----
					button5.setText("Save");
					button5.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button5.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button5ActionPerformed(e);
						}
					});
					panel20.add(button5, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- button9 ----
					button9.setText("Remove selected refsets");
					button9.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button9.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button9ActionPerformed(e);
						}
					});
					panel20.add(button9, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel6.add(panel20, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));
			}
			tabbedPane1.addTab("Linked Refsets", panel6);


			//======== panel7 ========
			{
				panel7.setLayout(new GridBagLayout());
				((GridBagLayout)panel7.getLayout()).columnWidths = new int[] {371, 260, 0};
				((GridBagLayout)panel7.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel7.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
				((GridBagLayout)panel7.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

				//---- label5 ----
				label5.setText("Source Languages");
				panel7.add(label5, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== scrollPane6 ========
				{

					//======== panel22 ========
					{
						panel22.setLayout(new GridBagLayout());
						((GridBagLayout)panel22.getLayout()).columnWidths = new int[] {0, 0};
						((GridBagLayout)panel22.getLayout()).rowHeights = new int[] {84, 22, 79, 0, 84, 0, 0};
						((GridBagLayout)panel22.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
						((GridBagLayout)panel22.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

						//---- list6 ----
						list6.setVisibleRowCount(3);
						list6.setBorder(new MatteBorder(1, 1, 1, 1, Color.black));
						list6.addKeyListener(new KeyAdapter() {
							@Override
							public void keyTyped(KeyEvent e) {
								list6KeyTyped(e);
							}
						});
						list6.addMouseListener(new MouseAdapter() {
							@Override
							public void mousePressed(MouseEvent e) {
								list6MousePressed(e);
							}
							@Override
							public void mouseReleased(MouseEvent e) {
								list6MouseReleased(e);
							}
						});
						panel22.add(list6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));

						//======== panel23 ========
						{
							panel23.setLayout(new GridBagLayout());
							((GridBagLayout)panel23.getLayout()).columnWidths = new int[] {0, 0, 0};
							((GridBagLayout)panel23.getLayout()).rowHeights = new int[] {22, 0};
							((GridBagLayout)panel23.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
							((GridBagLayout)panel23.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

							//---- button10 ----
							button10.setText("Remove selected refsets");
							button10.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
							button10.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									button10ActionPerformed(e);
								}
							});
							panel23.add(button10, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 0, 0), 0, 0));
						}
						panel22.add(panel23, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
						panel22.add(vSpacer1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));

						//---- label6 ----
						label6.setText("Target language");
						panel22.add(label6, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));

						//---- list8 ----
						list8.setVisibleRowCount(3);
						list8.setBorder(LineBorder.createBlackLineBorder());
						panel22.add(list8, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));

						//======== panel24 ========
						{
							panel24.setLayout(new GridBagLayout());
							((GridBagLayout)panel24.getLayout()).columnWidths = new int[] {0, 0, 0};
							((GridBagLayout)panel24.getLayout()).rowHeights = new int[] {22, 0};
							((GridBagLayout)panel24.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
							((GridBagLayout)panel24.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
						}
						panel22.add(panel24, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					scrollPane6.setViewportView(panel22);
				}
				panel7.add(scrollPane6, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel11 ========
				{
					panel11.setLayout(new GridBagLayout());
					((GridBagLayout)panel11.getLayout()).columnWidths = new int[] {241, 0};
					((GridBagLayout)panel11.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel11.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
					((GridBagLayout)panel11.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

					//---- label10 ----
					label10.setText("<html><body> Drag and drop one or more language refsets from the language refset list into the source language panel for selecting it(them) as the source language(s)<br><br>  Drag and drop a language refset from the language refset list into the target language panel for selecting it as the target language<br><br>  Press \u2018Save\u201d for persisting changes<br><br>  Select a source language and type \u2018d\u2019 for removing it<br><br>  Target language may be changed by selecting another language refset to replace it </html>");
					label10.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					panel11.add(label10, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel7.add(panel11, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel15 ========
				{
					panel15.setLayout(new GridBagLayout());
					((GridBagLayout)panel15.getLayout()).columnWidths = new int[] {0, 0};
					((GridBagLayout)panel15.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel15.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
					((GridBagLayout)panel15.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- button6 ----
					button6.setText("Save");
					button6.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button6.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button6ActionPerformed(e);
						}
					});
					panel15.add(button6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel7.add(panel15, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			tabbedPane1.addTab("Languages", panel7);


			//======== panel16 ========
			{
				panel16.setLayout(new GridBagLayout());
				((GridBagLayout)panel16.getLayout()).columnWidths = new int[] {406, 0, 0};
				((GridBagLayout)panel16.getLayout()).rowHeights = new int[] {0, 0, 0};
				((GridBagLayout)panel16.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
				((GridBagLayout)panel16.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

				//---- label11 ----
				label11.setText("WorkSets");
				panel16.add(label11, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== scrollPane7 ========
				{

					//---- list7 ----
					list7.addKeyListener(new KeyAdapter() {
						@Override
						public void keyTyped(KeyEvent e) {
							list6KeyTyped(e);
						}
					});
					scrollPane7.setViewportView(list7);
				}
				panel16.add(scrollPane7, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//======== panel17 ========
				{
					panel17.setLayout(new GridBagLayout());
					((GridBagLayout)panel17.getLayout()).columnWidths = new int[] {230, 0};
					((GridBagLayout)panel17.getLayout()).rowHeights = new int[] {0, 0, 0};
					((GridBagLayout)panel17.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
					((GridBagLayout)panel17.getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0E-4};

					//---- label12 ----
					label12.setText("<html>\n<body>\nThe list of worksets is displayed as new worksets are created<br>\n</html>");
					panel17.add(label12, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 5, 0), 0, 0));

					//======== panel18 ========
					{
						panel18.setLayout(new GridBagLayout());
						((GridBagLayout)panel18.getLayout()).columnWidths = new int[] {0, 0, 0};
						((GridBagLayout)panel18.getLayout()).rowHeights = new int[] {0, 0};
						((GridBagLayout)panel18.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
						((GridBagLayout)panel18.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

						//---- button7 ----
						button7.setText("Create new WorkSet");
						button7.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
						button7.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								button1ActionPerformed(e);
							}
						});
						panel18.add(button7, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));
					}
					panel17.add(panel18, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel16.add(panel17, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			tabbedPane1.addTab("WorkSets", panel16);


			//======== panel3 ========
			{
				panel3.setLayout(new GridBagLayout());
				((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {543, 177, 0};
				((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
				((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

				//---- label14 ----
				label14.setText("Worklists for maintenance work");
				panel3.add(label14, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label38 ----
				label38.setText("text");
				label38.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						label38MouseClicked(e);
					}
				});
				panel3.add(label38, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== scrollPane1 ========
				{
					scrollPane1.setViewportView(list1);
				}
				panel3.add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel21 ========
				{
					panel21.setLayout(new GridBagLayout());
					((GridBagLayout)panel21.getLayout()).columnWidths = new int[] {0, 0};
					((GridBagLayout)panel21.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel21.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
					((GridBagLayout)panel21.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

					//---- label13 ----
					label13.setText("<html><body>Maintenance worklists are used to send any concept into a translation workflow without the requirement of creating a new workset. Many different workflows can be configured.");
					panel21.add(label13, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));
				}
				panel3.add(panel21, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel25 ========
				{
					panel25.setLayout(new GridBagLayout());
					((GridBagLayout)panel25.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel25.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
					((GridBagLayout)panel25.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
					((GridBagLayout)panel25.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

					//---- button12 ----
					button12.setText("Create new WorkList for maintenance work");
					button12.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button12.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button12ActionPerformed(e);
						}
					});
					panel25.add(button12, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//======== panel26 ========
					{
						panel26.setLayout(new GridBagLayout());
						((GridBagLayout)panel26.getLayout()).columnWidths = new int[] {0, 0, 0};
						((GridBagLayout)panel26.getLayout()).rowHeights = new int[] {0, 0};
						((GridBagLayout)panel26.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
						((GridBagLayout)panel26.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
					}
					panel25.add(panel26, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
				}
				panel3.add(panel25, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));
			}
			tabbedPane1.addTab("Maintenance Workflows", panel3);


			//======== panel27 ========
			{
				panel27.setLayout(new GridBagLayout());
				((GridBagLayout)panel27.getLayout()).columnWidths = new int[] {305, 305, 0, 0};
				((GridBagLayout)panel27.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
				((GridBagLayout)panel27.getLayout()).columnWeights = new double[] {1.0, 1.0, 0.0, 1.0E-4};
				((GridBagLayout)panel27.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0, 0.0, 1.0E-4};

				//---- issuesHelpLbl ----
				issuesHelpLbl.setText("text");
				issuesHelpLbl.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						issuesHelpLblMouseClicked(e);
					}
				});
				panel27.add(issuesHelpLbl, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 5, 0), 0, 0));

				//---- label19 ----
				label19.setText("Source Defects Issue Repository");
				label19.setFont(new Font("Lucida Grande", Font.BOLD, 13));
				panel27.add(label19, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label20 ----
				label20.setText("Project Issue repository");
				label20.setFont(new Font("Lucida Grande", Font.BOLD, 13));
				panel27.add(label20, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel28 ========
				{
					panel28.setLayout(new GridBagLayout());
					((GridBagLayout)panel28.getLayout()).columnWidths = new int[] {0, 0, 0};
					((GridBagLayout)panel28.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
					((GridBagLayout)panel28.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
					((GridBagLayout)panel28.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

					//---- label21 ----
					label21.setText("Repository concept:");
					panel28.add(label21, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- textField3 ----
					textField3.setEditable(false);
					panel28.add(textField3, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label22 ----
					label22.setText("Repository ID: ");
					panel28.add(label22, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label24 ----
					label24.setText("id");
					panel28.add(label24, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label23 ----
					label23.setText("URL: ");
					panel28.add(label23, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label25 ----
					label25.setText("url");
					panel28.add(label25, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label26 ----
					label26.setText("Credentials:");
					panel28.add(label26, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label27 ----
					label27.setText("Username: ");
					panel28.add(label27, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 5, 5), 0, 0));
					panel28.add(textField4, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label28 ----
					label28.setText("Password: ");
					panel28.add(label28, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 0, 5), 0, 0));
					panel28.add(textField5, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel27.add(panel28, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel29 ========
				{
					panel29.setLayout(new GridBagLayout());
					((GridBagLayout)panel29.getLayout()).columnWidths = new int[] {0, 0, 0};
					((GridBagLayout)panel29.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
					((GridBagLayout)panel29.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
					((GridBagLayout)panel29.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

					//---- label29 ----
					label29.setText("Repository concept:");
					panel29.add(label29, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- textField6 ----
					textField6.setEditable(false);
					panel29.add(textField6, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label30 ----
					label30.setText("Repository ID: ");
					panel29.add(label30, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label31 ----
					label31.setText("id");
					panel29.add(label31, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label32 ----
					label32.setText("URL: ");
					panel29.add(label32, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label33 ----
					label33.setText("url");
					panel29.add(label33, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label34 ----
					label34.setText("Credentials:");
					panel29.add(label34, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label35 ----
					label35.setText("Username: ");
					panel29.add(label35, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 5, 5), 0, 0));
					panel29.add(textField7, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label36 ----
					label36.setText("Password: ");
					panel29.add(label36, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 0, 5), 0, 0));
					panel29.add(textField8, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel27.add(panel29, new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel31 ========
				{
					panel31.setLayout(new GridBagLayout());
					((GridBagLayout)panel31.getLayout()).columnWidths = new int[] {0, 0};
					((GridBagLayout)panel31.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
					((GridBagLayout)panel31.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
					((GridBagLayout)panel31.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};

					//---- label37 ----
					label37.setText("Available repositories (Drag&Drop to fields above)");
					panel31.add(label37, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//======== scrollPane2 ========
					{
						scrollPane2.setViewportView(list2);
					}
					panel31.add(scrollPane2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//======== panel32 ========
					{
						panel32.setLayout(new GridBagLayout());
						((GridBagLayout)panel32.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
						((GridBagLayout)panel32.getLayout()).rowHeights = new int[] {0, 0};
						((GridBagLayout)panel32.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
						((GridBagLayout)panel32.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

						//---- button14 ----
						button14.setText("Create a new repository");
						button14.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
						button14.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								button14ActionPerformed();
							}
						});
						panel32.add(button14, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					panel31.add(panel32, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel27.add(panel31, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel30 ========
				{
					panel30.setLayout(new GridBagLayout());
					((GridBagLayout)panel30.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel30.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel30.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel30.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- button13 ----
					button13.setText("Save");
					button13.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button13.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button13ActionPerformed(e);
						}
					});
					panel30.add(button13, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel27.add(panel30, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 0, 5), 0, 0));
			}
			tabbedPane1.addTab("Issues", panel27);

		}
		add(tabbedPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JTabbedPane tabbedPane1;
	private JPanel panel0;
	private JPanel panel1;
	private JLabel label1;
	private JPanel panel2;
	private JLabel label2;
	private JTextField textField1;
	private JLabel label41;
	private JTextField namespaceTextField;
	private JSeparator separator1;
	private JLabel label39;
	private JList releaseCandidateList;
	private JLabel label40;
	private JList moduleIdList;
	private JPanel panel4;
	private JButton button1;
	private JButton button2;
	private JPanel panel9;
	private JLabel label18;
	private JLabel label7;
	private JPanel panel12;
	private JButton button3;
	private JPanel panel5;
	private JLabel label3;
	private JScrollPane scrollPane4;
	private JList list4;
	private JPanel panel8;
	private JLabel label8;
	private JPanel panel13;
	private JPanel panel19;
	private JButton button4;
	private JButton button8;
	private JPanel panel6;
	private JLabel label4;
	private JScrollPane scrollPane5;
	private JList list5;
	private JPanel panel10;
	private JLabel label9;
	private JPanel panel14;
	private JPanel panel20;
	private JButton button5;
	private JButton button9;
	private JPanel panel7;
	private JLabel label5;
	private JScrollPane scrollPane6;
	private JPanel panel22;
	private JList list6;
	private JPanel panel23;
	private JButton button10;
	private JPanel vSpacer1;
	private JLabel label6;
	private JList list8;
	private JPanel panel24;
	private JPanel panel11;
	private JLabel label10;
	private JPanel panel15;
	private JButton button6;
	private JPanel panel16;
	private JLabel label11;
	private JScrollPane scrollPane7;
	private JList list7;
	private JPanel panel17;
	private JLabel label12;
	private JPanel panel18;
	private JButton button7;
	private JPanel panel3;
	private JLabel label14;
	private JLabel label38;
	private JScrollPane scrollPane1;
	private JList list1;
	private JPanel panel21;
	private JLabel label13;
	private JPanel panel25;
	private JButton button12;
	private JPanel panel26;
	private JPanel panel27;
	private JLabel issuesHelpLbl;
	private JLabel label19;
	private JLabel label20;
	private JPanel panel28;
	private JLabel label21;
	private JTextField textField3;
	private JLabel label22;
	private JLabel label24;
	private JLabel label23;
	private JLabel label25;
	private JLabel label26;
	private JLabel label27;
	private JTextField textField4;
	private JLabel label28;
	private JPasswordField textField5;
	private JPanel panel29;
	private JLabel label29;
	private JTextField textField6;
	private JLabel label30;
	private JLabel label31;
	private JLabel label32;
	private JLabel label33;
	private JLabel label34;
	private JLabel label35;
	private JTextField textField7;
	private JLabel label36;
	private JPasswordField textField8;
	private JPanel panel31;
	private JLabel label37;
	private JScrollPane scrollPane2;
	private JList list2;
	private JPanel panel32;
	private JButton button14;
	private JPanel panel30;
	private JButton button13;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
