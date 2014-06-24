/*
 * Created by JFormDesigner on Mon Mar 05 17:48:05 GMT-03:00 2012
 */

package org.ihtsdo.translation.ui.translation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.document.DocumentsSearchPanel;
import org.ihtsdo.issue.IssueRepoRegistration;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;
import org.ihtsdo.project.I_ContextualizeDescription;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.project.view.PanelHelperFactory;
import org.ihtsdo.project.view.TranslationHelperPanel;
import org.ihtsdo.project.view.details.WorklistMemberLogPanel;
import org.ihtsdo.project.view.event.EventMediator;
import org.ihtsdo.project.view.event.GenericEvent.EventType;
import org.ihtsdo.project.view.issue.IssuesListPanel2;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.api.WorkflowInterpreter;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfPermission;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.ui.ConfigTranslationModule;
import org.ihtsdo.translation.ui.ConfigTranslationModule.EditorMode;
import org.ihtsdo.translation.ui.HierarchyNavigator;
import org.ihtsdo.translation.ui.SimilarityPanel;
import org.ihtsdo.translation.ui.TranslationWlstMemberLogPanel;
import org.ihtsdo.translation.ui.event.ClearAllPanelEvent;
import org.ihtsdo.translation.ui.event.ClearAllPanelEventHandler;
import org.ihtsdo.translation.ui.event.HistoryEvent;
import org.ihtsdo.translation.ui.event.HistoryEventHandler;
import org.ihtsdo.translation.ui.event.LogEvent;
import org.ihtsdo.translation.ui.event.LogEventHandler;
import org.ihtsdo.translation.ui.event.SearchDocumentEvent;
import org.ihtsdo.translation.ui.event.SearchDocumentEventHandler;
import org.ihtsdo.translation.ui.event.SendToConceptViewerR1Event;
import org.ihtsdo.translation.ui.event.SendToConceptViewerR1EventHandler;
import org.ihtsdo.translation.ui.event.UpdateSimilarityEvent;
import org.ihtsdo.translation.ui.event.UpdateSimilarityEventHandler;
import org.ihtsdo.translation.ui.event.UpdateTargetDescriptionTableEvent;
import org.ihtsdo.translation.ui.event.UpdateTargetDescriptionTableEventHandler;

/**
 * @author Guillermo Reynoso
 */
public class TranslationView extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5758313303998420551L;
	private TranslationProject project;
	private WorkListMember worklistMember;
	private ConfigTranslationModule translConfig;
	public WorkflowDefinition workflowDefinition;
	public WorkflowInterpreter workflowInterpreter;
	public WfComponentProvider componentProvider;
	public WfRole wfRole;
	public TranslationProject translationProject;
	private UpdateUIWorker updateUiWorker;
	private I_ConfigAceFrame config;
	private LanguageMembershipRefset targetLangRefset;
	private boolean readOnlyMode;
	private TranslationTermTableReport result;

	public TranslationView() {
		initComponents();
		initCustomComponents();
		Ts.get().removeTermChangeListener(TranslationTermChangeListener.getInstance());
		Ts.get().addTermChangeListener(TranslationTermChangeListener.getInstance());
	}

	private void initCustomComponents() {
		try {
			config = Terms.get().getActiveAceFrameConfig();
		} catch (Exception e) {
			e.printStackTrace();
		}
		suscribeToEvents();
		label10.setIcon(IconUtilities.helpIcon);
		label10.setText("");
		hierarchyNavigator1.setContainerPanel(tabbedPane2);
	}

	/**
	 * Update ui.
	 * 
	 * @param instance
	 *            the instance
	 * @param readOnlyMode
	 *            the read only mode
	 * @param outbox
	 * @throws TerminologyException
	 * @throws IOException
	 */
	public void updateUi(WfInstance instance, boolean readOnlyMode, boolean outbox) throws IOException, TerminologyException {
		this.readOnlyMode = readOnlyMode;
		if (updateUiWorker != null && !updateUiWorker.isDone()) {
			updateUiWorker.cancel(true);
			updateUiWorker = null;
		}
		updateUiWorker = new UpdateUIWorker(instance, readOnlyMode, outbox);
		updateUiWorker.execute();
	}

	public void clearAll() {
		descriptionPanel1.clearForm();
		sourceTable.clearAll();
		targetTable.clearAll();
		ConceptVersionBI c = null;
		hierarchyNavigator1.setFocusConcept(c);
		conceptDetailsPanel1.updateDetailsTree(c);
		translationPanelMenu1.updateTranslationPanelMenue(true, translConfig);
		comments.clearComments();
	}

	private void updateUi(TranslationProject project, WorkListMember worklistMember, I_GetConceptData roleConcept, boolean readOnlyMode, boolean isOutbox) {
		try {
			HashMap<UUID, EditorMode> roles = translConfig.getTranslatorRoles();
			if (wfRole != null) {
				EditorMode editorMode = roles.get(wfRole.getId());
				if (editorMode != null && editorMode.equals(EditorMode.READ_ONLY)) {
					this.readOnlyMode = true;
				} else {
					this.readOnlyMode = true && readOnlyMode;
				}
			} else {
				this.readOnlyMode = true && readOnlyMode;
			}

			this.worklistMember = worklistMember;
			this.project = project;
			if (descriptionPanel1 == null) {
				descriptionPanel1 = new DescriptionPanel(translConfig);
			}
			descriptionPanel1.setTranslConfig(translConfig);
			descriptionPanel1.setReadOnlyMode(true);
			splitPane4.setBottomComponent(descriptionPanel1);
			descriptionPanel1.updateDescriptionPanel(worklistMember, this.readOnlyMode, translConfig, isOutbox);

			targetLangRefset = new LanguageMembershipRefset(project.getTargetLanguageRefset(), config);

			// Unfortunately source table must be populated before target table.
			if (sourceTable == null) {
				sourceTable = new LanguageTermPanel(true);
			}
			splitPane2.setTopComponent(sourceTable);
			result = sourceTable.populateTable(project, worklistMember, true, translConfig, null, this.readOnlyMode);

			if (targetTable == null) {
				targetTable = new LanguageTermPanel(false);
				splitPane4.setTopComponent(targetTable);
			}
			targetTable.populateTable(project, worklistMember, false, translConfig, result, this.readOnlyMode);

			hierarchyNavigator1.setFocusConcept(Ts.get().getConceptVersion(config.getViewCoordinate(), worklistMember.getConcept().getConceptNid()));
			conceptDetailsPanel1.updateDetailsTree(worklistMember);
			comments.updateCommentsPanel(roleConcept, targetLangRefset, worklistMember);
			if (isOutbox) {
				comments.setReadOnlyMode(true);
			} else {
				comments.setReadOnlyMode(false);
			}
			translationPanelMenu1.updateTranslationPanelMenue(this.readOnlyMode, translConfig);
			if (translationProject.getProjectIssueRepo() != null) {
				issues.loadIssues(worklistMember.getConcept(), project, this.readOnlyMode, config);
			}
			Ts.get().removeTermChangeListener(TranslationTermChangeListener.getInstance());
			Ts.get().addTermChangeListener(TranslationTermChangeListener.getInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateSimilarityTable(I_ContextualizeDescription sourceFsnConcept, I_ContextualizeDescription sourcePreferredConcept, I_GetConceptData concept, List<Integer> sourceIds, int targetId) {
		/** update similarity table */
		SimilarityPanel similPanel = getSimilarityPanel();
		similPanel.updateTabs(sourceFsnConcept, sourcePreferredConcept, concept, sourceIds, targetId, translationProject, worklistMember);
		int scount = similPanel.getSimilarityHitsCount();
		if (scount > 0) {
			label14.setForeground(Color.red);
		} else {
			label14.setForeground(Color.black);
		}
		label14.setText("S:" + scount);
		label14.revalidate();

		int tmcount = similPanel.getTransMemoryHitsCount();
		if (tmcount > 0) {
			label15.setForeground(Color.red);
		} else {
			label15.setForeground(Color.black);
		}
		label15.setText("TM:" + tmcount);
		label15.revalidate();

		int lgcount = similPanel.getLingGuidelinesHitsCount();
		if (lgcount > 0) {
			label16.setForeground(Color.red);
		} else {
			label16.setForeground(Color.black);
		}
		label16.setText("LG:" + lgcount);
		label16.revalidate();
	}

	/**
	 * Gets the similarity panel.
	 * 
	 * @return the similarity panel
	 */
	private SimilarityPanel getSimilarityPanel() {

		AceFrameConfig config;
		try {
			config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();

			AceFrame ace = config.getAceFrame();
			JTabbedPane tp = ace.getCdePanel().getLeftTabs();
			if (tp != null) {
				int tabCount = tp.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.SIMILARITY_TAB_NAME)) {
						return (SimilarityPanel) tp.getComponentAt(i);
					}
				}
				SimilarityPanel uiPanel = new SimilarityPanel();

				tp.addTab(TranslationHelperPanel.SIMILARITY_TAB_NAME, uiPanel);
				return uiPanel;
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void suscribeToEvents() {
		EventMediator mediator = EventMediator.getInstance();
		mediator.suscribe(EventType.UPDATE_TARGET_DESCRIPTION_TABLE, new UpdateTargetDescriptionTableEventHandler<UpdateTargetDescriptionTableEvent>(this) {
			@Override
			public void handleEvent(UpdateTargetDescriptionTableEvent event) {
				try {
					if (targetTable == null) {
						targetTable = new LanguageTermPanel(false);
					}
					targetTable.populateTable(project, worklistMember, false, translConfig, result, readOnlyMode);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		mediator.suscribe(EventType.SEND_TO_CONCEPT_VIEWER_R1, new SendToConceptViewerR1EventHandler<SendToConceptViewerR1Event>(this) {
			@Override
			public void handleEvent(SendToConceptViewerR1Event event) {
				try {
					I_HostConceptPlugins viewer = config.getConceptViewer(1);
					viewer.setTermComponent(worklistMember.getConcept());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		mediator.suscribe(EventType.UPDATE_SIMILARITY_EVENT, new UpdateSimilarityEventHandler<UpdateSimilarityEvent>(this) {
			@Override
			public void handleEvent(UpdateSimilarityEvent event) {
				updateSimilarityTable(event.getSourceFsnConcept(), event.getSourcePreferredConcept(), event.getConcept(), event.getSourceIds(), event.getTargetId());
			}
		});

		mediator.suscribe(EventType.HISTORY_EVENT, new HistoryEventHandler<HistoryEvent>(this) {
			@Override
			public void handleEvent(HistoryEvent event) {
				TranslationHelperPanel thp;
				try {
					thp = PanelHelperFactory.getTranslationHelperPanel();
					JTabbedPane tp = thp.getTabbedPanel();
					if (tp != null) {
						I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
						IssueRepository repo = null;
						if (translationProject.getProjectIssueRepo() != null) {
							repo = IssueRepositoryDAO.getIssueRepository(translationProject.getProjectIssueRepo());
						}
						IssueRepoRegistration regis = null;
						WorklistMemberLogPanel wmlpanel = null;
						if (repo != null) {
							regis = IssueRepositoryDAO.getRepositoryRegistration(repo.getUuid(), config);
						}
						int tabCount = tp.getTabCount();
						for (int i = 0; i < tabCount; i++) {
							if (tp.getTitleAt(i).equals(TranslationHelperPanel.CONCEPT_VERSIONS_TAB_NAME)) {
								tp.setSelectedIndex(i);
								wmlpanel = (WorklistMemberLogPanel) tp.getComponentAt(i);
								wmlpanel.showMemberChanges(worklistMember, translationProject, repo, regis);
								thp.showTabbedPanel();
								return;
							}
						}
						wmlpanel = new WorklistMemberLogPanel();
						tp.addTab(TranslationHelperPanel.CONCEPT_VERSIONS_TAB_NAME, wmlpanel);
						tp.setSelectedIndex(tp.getTabCount() - 1);
						thp.showTabbedPanel();
						wmlpanel.showMemberChanges(worklistMember, translationProject, repo, regis);
					}
				} catch (TerminologyException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		mediator.suscribe(EventType.LOG_EVENT, new LogEventHandler<LogEvent>(this) {
			@Override
			public void handleEvent(LogEvent event) {
				try {
					TranslationWlstMemberLogPanel panel = getTranslMemberLogPanel();

					I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
					IssueRepository repo = null;
					if (translationProject.getProjectIssueRepo() != null) {
						repo = IssueRepositoryDAO.getIssueRepository(translationProject.getProjectIssueRepo());
					}
					IssueRepoRegistration regis = null;

					if (repo != null) {
						regis = IssueRepositoryDAO.getRepositoryRegistration(repo.getUuid(), config);
					}
					panel.showMemberChanges(worklistMember, translationProject, repo, regis);

				} catch (TerminologyException e) {

					e.printStackTrace();
				} catch (IOException e) {

					e.printStackTrace();
				} catch (Exception e) {

					e.printStackTrace();
				}
			}

			/**
			 * Gets the transl member log panel.
			 * 
			 * @return the transl member log panel
			 */
			private TranslationWlstMemberLogPanel getTranslMemberLogPanel() {
				AceFrameConfig config;
				try {
					config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();

					AceFrame ace = config.getAceFrame();
					JTabbedPane tp = ace.getCdePanel().getLeftTabs();
					if (tp != null) {
						int tabCount = tp.getTabCount();
						for (int i = 0; i < tabCount; i++) {
							if (tp.getTitleAt(i).equals(TranslationHelperPanel.MEMBER_LOG_TAB_NAME)) {
								return (TranslationWlstMemberLogPanel) tp.getComponentAt(i);
							}
						}
						TranslationWlstMemberLogPanel uiPanel = new TranslationWlstMemberLogPanel();

						tp.addTab(TranslationHelperPanel.MEMBER_LOG_TAB_NAME, uiPanel);
						return uiPanel;
					}
				} catch (TerminologyException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;

			}
		});

		mediator.suscribe(EventType.SEARCH_DOCUMENT_EVENT, new SearchDocumentEventHandler<SearchDocumentEvent>(this) {
			@Override
			public void handleEvent(SearchDocumentEvent event) {
				try {
					TranslationHelperPanel thp = PanelHelperFactory.getTranslationHelperPanel();
					JTabbedPane tp = thp.getTabbedPanel();
					if (tp != null) {
						int tabCount = tp.getTabCount();
						for (int i = 0; i < tabCount; i++) {
							if (tp.getTitleAt(i).equals(TranslationHelperPanel.SEARCH_DOCS_TAB_NAME)) {
								tp.setSelectedIndex(i);
								thp.showTabbedPanel();
								return;
							}
						}
						JPanel panel = new JPanel();
						panel.setLayout(new BorderLayout());
						panel.add(new DocumentsSearchPanel(""), BorderLayout.CENTER);
						tp.addTab(TranslationHelperPanel.SEARCH_DOCS_TAB_NAME, panel);
						tp.setSelectedIndex(tp.getTabCount() - 1);
						thp.showTabbedPanel();
					}
				} catch (TerminologyException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		mediator.suscribe(EventType.CLEAR_ALL, new ClearAllPanelEventHandler<ClearAllPanelEvent>(this) {
			@Override
			public void handleEvent(ClearAllPanelEvent event) {
				clearAll();
			}
		});
	}

	private void label10MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("TRANSLATION_UI");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Gets the translation project config.
	 * 
	 * @return the translation project config
	 */
	private ConfigTranslationModule getTranslationProjectConfig() {
		ConfigTranslationModule translProjConfig = null;
		if (this.translationProject != null) {
			translProjConfig = LanguageUtil.getDefaultTranslationConfig(this.translationProject);
		}

		if (translProjConfig == null) {
			return translConfig;
		}
		translProjConfig.setColumnsDisplayedInInbox(translConfig.getColumnsDisplayedInInbox());
		translProjConfig.setAutoOpenNextInboxItem(translConfig.isAutoOpenNextInboxItem());
		translProjConfig.setSourceTreeComponents(translConfig.getSourceTreeComponents());
		translProjConfig.setTargetTreeComponents(translConfig.getTargetTreeComponents());
		translProjConfig.setSelectedPrefTermDefault(translConfig.getSelectedPrefTermDefault());
		return translProjConfig;
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		splitPane1 = new JSplitPane();
		splitPane2 = new JSplitPane();
		sourceTable = new LanguageTermPanel();
		commentsAndIssuesContainer = new JPanel();
		tabbedPane1 = new JTabbedPane();
		comments = new CommentsManagerPanel();
		issues = new IssuesListPanel2();
		splitPane3 = new JSplitPane();
		splitPane4 = new JSplitPane();
		targetTable = new LanguageTermPanel();
		descriptionPanel1 = new DescriptionPanel();
		cdAndHierarcyContainer = new JPanel();
		tabbedPane2 = new JTabbedPane();
		conceptDetailsPanel1 = new ConceptDetailsPanel();
		hierarchyNavigator1 = new HierarchyNavigator();
		panel2 = new JPanel();
		translationPanelMenu1 = new TranslationPanelMenu();
		label14 = new JLabel();
		label15 = new JLabel();
		label16 = new JLabel();
		label10 = new JLabel();

		// ======== this ========
		setLayout(new BorderLayout(5, 5));

		// ======== splitPane1 ========
		{
			splitPane1.setDividerSize(5);
			splitPane1.setResizeWeight(0.5);

			// ======== splitPane2 ========
			{
				splitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
				splitPane2.setResizeWeight(0.5);
				splitPane2.setDividerSize(15);
				splitPane2.setDividerLocation(300);
				splitPane2.setOneTouchExpandable(true);
				splitPane2.setTopComponent(sourceTable);

				// ======== commentsAndIssuesContainer ========
				{
					commentsAndIssuesContainer.setLayout(new GridBagLayout());
					((GridBagLayout) commentsAndIssuesContainer.getLayout()).columnWidths = new int[] { 0, 0 };
					((GridBagLayout) commentsAndIssuesContainer.getLayout()).rowHeights = new int[] { 17, 0, 0 };
					((GridBagLayout) commentsAndIssuesContainer.getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
					((GridBagLayout) commentsAndIssuesContainer.getLayout()).rowWeights = new double[] { 0.0, 1.0, 1.0E-4 };

					// ======== tabbedPane1 ========
					{
						tabbedPane1.setMinimumSize(new Dimension(15, 15));
						tabbedPane1.setPreferredSize(new Dimension(15, 15));
						tabbedPane1.addTab("Comments", comments);

						tabbedPane1.addTab("Issues", issues);

					}
					commentsAndIssuesContainer.add(tabbedPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				}
				splitPane2.setBottomComponent(commentsAndIssuesContainer);
			}
			splitPane1.setLeftComponent(splitPane2);

			// ======== splitPane3 ========
			{
				splitPane3.setOrientation(JSplitPane.VERTICAL_SPLIT);
				splitPane3.setDividerSize(15);
				splitPane3.setDividerLocation(500);
				splitPane3.setResizeWeight(0.5);
				splitPane3.setOneTouchExpandable(true);

				// ======== splitPane4 ========
				{
					splitPane4.setOrientation(JSplitPane.VERTICAL_SPLIT);
					splitPane4.setResizeWeight(0.2);
					splitPane4.setTopComponent(targetTable);

					// ---- descriptionPanel1 ----
					descriptionPanel1.setMinimumSize(new Dimension(66, 66));
					descriptionPanel1.setPreferredSize(new Dimension(66, 66));
					splitPane4.setBottomComponent(descriptionPanel1);
				}
				splitPane3.setTopComponent(splitPane4);

				// ======== cdAndHierarcyContainer ========
				{
					cdAndHierarcyContainer.setLayout(new GridBagLayout());
					((GridBagLayout) cdAndHierarcyContainer.getLayout()).columnWidths = new int[] { 0, 0 };
					((GridBagLayout) cdAndHierarcyContainer.getLayout()).rowHeights = new int[] { 0, 0, 0 };
					((GridBagLayout) cdAndHierarcyContainer.getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
					((GridBagLayout) cdAndHierarcyContainer.getLayout()).rowWeights = new double[] { 0.0, 1.0, 1.0E-4 };

					// ======== tabbedPane2 ========
					{
						tabbedPane2.setMinimumSize(new Dimension(50, 50));
						tabbedPane2.setPreferredSize(new Dimension(50, 50));
						tabbedPane2.addTab("Concept Details", conceptDetailsPanel1);

						tabbedPane2.addTab("Hierarchy", hierarchyNavigator1);

					}
					cdAndHierarcyContainer.add(tabbedPane2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				}
				splitPane3.setBottomComponent(cdAndHierarcyContainer);
			}
			splitPane1.setRightComponent(splitPane3);
		}
		add(splitPane1, BorderLayout.CENTER);

		// ======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout) panel2.getLayout()).columnWidths = new int[] { 0, 0, 0, 0, 0, 0 };
			((GridBagLayout) panel2.getLayout()).rowHeights = new int[] { 0, 0 };
			((GridBagLayout) panel2.getLayout()).columnWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4 };
			((GridBagLayout) panel2.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };
			panel2.add(translationPanelMenu1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- label14 ----
			label14.setText("S:-");
			label14.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel2.add(label14, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- label15 ----
			label15.setText("TM:-");
			label15.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel2.add(label15, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- label16 ----
			label16.setText("LG:-");
			label16.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel2.add(label16, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- label10 ----
			label10.setText("text");
			label10.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					label10MouseClicked(e);
				}
			});
			panel2.add(label10, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, BorderLayout.NORTH);
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JSplitPane splitPane1;
	private JSplitPane splitPane2;
	private LanguageTermPanel sourceTable;
	private JPanel commentsAndIssuesContainer;
	private JTabbedPane tabbedPane1;
	private CommentsManagerPanel comments;
	private IssuesListPanel2 issues;
	private JSplitPane splitPane3;
	private JSplitPane splitPane4;
	private LanguageTermPanel targetTable;
	private DescriptionPanel descriptionPanel1;
	private JPanel cdAndHierarcyContainer;
	private JTabbedPane tabbedPane2;
	private ConceptDetailsPanel conceptDetailsPanel1;
	private HierarchyNavigator hierarchyNavigator1;
	private JPanel panel2;
	private TranslationPanelMenu translationPanelMenu1;
	private JLabel label14;
	private JLabel label15;
	private JLabel label16;
	private JLabel label10;

	// JFormDesigner - End of variables declaration //GEN-END:variables

	class UpdateUIWorker extends SwingWorker<String, String> {
		private WfInstance instance;
		private boolean readOnlyMode;
		private boolean outbox;

		public UpdateUIWorker(WfInstance instance, boolean readOnlyMode, boolean outbox) {
			super();
			this.instance = instance;
			this.readOnlyMode = readOnlyMode;
			this.outbox = outbox;
		}

		@Override
		protected String doInBackground() throws Exception {
			// initializeMemonicKeys();
			// setReadOnlyMode(this.readOnlyMode);
			I_ConfigAceFrame config;
			try {
				workflowDefinition = instance.getWfDefinition();

				config = Terms.get().getActiveAceFrameConfig();
				WorkList workList = instance.getWorkList();
				workflowInterpreter = WorkflowInterpreter.createWorkflowInterpreter(workList.getWorkflowDefinition());
				List<WfRole> roles = workflowInterpreter.getNextRole(instance, workList);
				componentProvider = new WfComponentProvider();

				WfUser user = componentProvider.userConceptToWfUser(config.getDbConfig().getUserConcept());
				List<WfPermission> perms = componentProvider.getPermissionsForUser(user);
				WfRole userRole = null;
				boolean bExists = false;
				I_GetConceptData roleConcept = null;
				for (WfRole role : roles) {
					for (WfPermission perm : perms) {
						if (role.toString().equals(perm.getRole().toString())) {
							userRole = role;
							bExists = true;
							break;
						}
					}
					if (bExists)
						break;
				}
				if (bExists) {
					roleConcept = Terms.get().getConcept(userRole.getId());
				}
				wfRole = userRole;
				translationProject = (TranslationProject) TerminologyProjectDAO.getProjectForWorklist(workList, config);
				translConfig = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
				translConfig = getTranslationProjectConfig();
				I_GetConceptData component = Terms.get().getConcept(instance.getComponentId());
				WorkListMember workListMember = TerminologyProjectDAO.getWorkListMember(component, workList, config);
				updateUi(translationProject, workListMember, roleConcept, readOnlyMode, outbox);
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return "";
		}

	}

	public boolean checkUncommited() {
		// TODO Auto-generated method stub
		return false;
	}

}
