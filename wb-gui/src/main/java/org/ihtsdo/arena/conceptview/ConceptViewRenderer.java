package org.ihtsdo.arena.conceptview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.apache.commons.lang.WordUtils;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.batch.BatchMonitor;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.arena.WizardPanel;
import org.ihtsdo.arena.context.action.BpAction;
import org.ihtsdo.arena.context.action.BpActionFactory;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.view.details.WorkflowInterperterInitWorker;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.api.WorkflowInterpreter;
import org.ihtsdo.project.workflow.api.wf2.implementation.WfActivity;
import org.ihtsdo.project.workflow.api.wf2.implementation.WfProcessDefinition;
import org.ihtsdo.project.workflow.api.wf2.implementation.WorkflowStore;
import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfPermission;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.workflow.WorkflowHistoryJavaBeanBI;
import org.ihtsdo.tk.drools.facts.ConceptFact;
import org.ihtsdo.tk.drools.facts.Context;
import org.ihtsdo.tk.workflow.api.WfActivityBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;
import org.ihtsdo.tk.workflow.api.WfUserBI;
import org.ihtsdo.tk.workflow.api.WorkListBI;
import org.ihtsdo.util.swing.GuiUtil;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;
import org.intsdo.tk.drools.manager.DroolsExecutionManager;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import java.util.logging.Logger;

/**
 * @author Administrator
 * 
 */
public class ConceptViewRenderer extends JLayeredPane {
	// TODO: Remove Hardcoding and find better fix to issue of Pref-Term vs FSN
	// (THROUGHOUT FILE ie Override)

	final String CHANGED_WORKFLOW_STATE = "Changed";
	final String CHANGED_IN_BATCH_WORKFLOW_STATE = "Changed in batch";
	final String CONCEPT_HAVING_NO_PRIOR_WORKFLOW_STATE = "Concept having no prior";
	final String CONCEPT_NOT_PREVIOUSLY_EXISTING_WORKFLOW_STATE = "Concept not previously existing";
	final String NEW_WORKFLOW_STATE = "New";
	final String WORKFLOW_STATE_SUFFIX = " workflow state";
	final String WORKFLOW_ACTION_SUFFIX = " workflow action";
	private final JButton cancelButton;
	private final JButton commitButton;
	private final JButton acceptButton;
	private final JButton promoteButton;
	private final JButton conflictButton;
	private WfHxDetailsPanelHandler wfHxDetails;
	private final JLabel workflowStatusLabel;
	private static boolean capWorkflow = false;

	private class RendererComponentAdaptor extends ComponentAdapter implements AncestorListener {

		@Override
		public void componentMoved(ComponentEvent e) {
			if (settings.hideNavigator()) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						settings.showNavigator();
						settings.setNavigatorLocation();
					}
				});
			}

			if (wfHxDetails.isWfHxDetailsCurrenltyDisplayed()) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						wfHxDetails.setWfHxLocation();
					}
				});
			}
		}

		@Override
		public void componentResized(ComponentEvent e) {
			if (settings.hideNavigator()) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						settings.showNavigator();
						settings.setNavigatorLocation();
					}
				});
			}
			if (wfHxDetails.isWfHxDetailsCurrenltyDisplayed()) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						wfHxDetails.setWfHxLocation();
					}
				});
			}
			setDividerLocation();
                    try {
                        settings.getView().redoConceptViewLayout();
                    } catch (IOException ex) {
                        Logger.getLogger(ConceptViewRenderer.class.getName()).log(Level.SEVERE, null, ex);
                    }
		}

		@Override
		public void ancestorMoved(AncestorEvent event) {
			if (ConceptViewRenderer.this.getRootPane() != null && event.getAncestor() != ConceptViewRenderer.this.getRootPane().getParent()) {
				if (settings.hideNavigator()) {
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							settings.showNavigator();
						}
					});
				}
				if (wfHxDetails.isWfHxDetailsCurrenltyDisplayed()) {
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							wfHxDetails.setWfHxLocation();
							settings.setNavigatorLocation();
						}
					});
				}
			}
			setDividerLocation();
		}

		@Override
		public void ancestorRemoved(AncestorEvent event) {
			settings.hideNavigator();
			settings.hideAiNavigator();
			wfHxDetails.hideWfHxDetailsPanel();
			setWorkflowStatusLabel();
		}

		@Override
		public void ancestorAdded(AncestorEvent event) {
			settings.hideNavigator();
			settings.hideAiNavigator();
			wfHxDetails.hideWfHxDetailsPanel();
			setWorkflowStatusLabel(settings.getConcept());
		}
	}

	/**
     *
     */
	private static final long serialVersionUID = 2106746763664760745L;
	/**
     *
     */
	protected static ConceptViewRenderer dragSource = null;
	/**
     *
     */
	protected static int sourceRow = 0;
	/**
     *
     */
	protected mxCell cell;
	/**
     *
     */
	protected mxGraphComponent graphContainer;
	/**
     *
     */
	protected mxGraph graph;
	/**
     *
     */
	public ConceptView renderedComponent;
	private ConceptViewSettings settings;
	private ViewCoordinate viewCoord;
	public ConceptViewTitle title;
	private JSplitPane workflowPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private JPanel applicationWorkflowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
	private JPanel conceptWorkflowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
	private JScrollPane workflowScrollPane = new JScrollPane(workflowPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	private WizardPanel wizardPanel;
	private JScrollPane wizardScrollPane;
	private JScrollPane conceptScrollPane;
	private JToggleButton workflowToggleButton;
	private JToggleButton wfHxDetailsToggleButton;
	private JToggleButton oopsButton;
	private final static String advanceWorkflowActionPath = "migration-wf";
	private final String advanceWorkflowActionFile = "AdvanceWorkflow.bp";
	private JComponent historyPanel = new JPanel(new BorderLayout());

	public JComponent getHistoryPanel() {
		return historyPanel;
	}

	private JPanel conceptViewPanel = new JPanel(new BorderLayout());
	private Set<File> kbFiles = new HashSet<File>();
	private File wfBpFile;

	/**
     *
     */
	public ConceptViewRenderer(Object cellObj, final mxGraphComponent graphContainer, I_ConfigAceFrame config) {

		wizardPanel = new WizardPanel(new FlowLayout(FlowLayout.LEADING, 10, 10), this);
		wizardScrollPane = new JScrollPane(wizardPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.kbFiles.add(new File("drools-rules/ContextualConceptActionsPanel.drl"));
		if (new File("drools-rules/extras/ContextualConceptActionsPanelXtra.drl").exists()) {
			kbFiles.add(new File("drools-rules/extras/ContextualConceptActionsPanelXtra.drl"));
		}

		try {
			DroolsExecutionManager.setup(ConceptViewRenderer.class.getCanonicalName(), kbFiles);
		} catch (IOException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}
		applicationWorkflowPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		conceptWorkflowPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		this.cell = (mxCell) cellObj;
		this.graphContainer = graphContainer;
		this.graph = graphContainer.getGraph();
		this.settings = (ConceptViewSettings) this.cell.getValue();
		this.settings.setup(config, cell, graphContainer, graph, this);
		this.viewCoord = config.getViewCoordinate();
		wizardPanel.add(new JLabel("Wizard Panel"));
		setLayout(new BorderLayout());

		title = new ConceptViewTitle(graph, cell, settings);
		title.setCursor(Cursor.getDefaultCursor());

		add(title, BorderLayout.NORTH);

		conceptScrollPane = null;

		if (graph.getModel().getChildCount(cell) == 0) {
			renderedComponent = (ConceptView) settings.getComponent(config);
			conceptScrollPane = new JScrollPane();
		}

		if (conceptScrollPane != null) {
			historyPanel = renderedComponent.getPathCheckboxPanel();

			conceptViewPanel.add(historyPanel, BorderLayout.NORTH);
			conceptViewPanel.add(new JScrollPane(renderedComponent), BorderLayout.CENTER);
			add(conceptViewPanel, BorderLayout.CENTER);
			conceptScrollPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			conceptScrollPane.getViewport().setBackground(Color.WHITE);
			conceptScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			conceptScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			setOpaque(true);
			conceptScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

				@Override
				public void adjustmentValueChanged(AdjustmentEvent e) {
					graphContainer.refresh();
				}
			});
			conceptScrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.GRAY));
		}

		wfBpFile = new File("plugins" + File.separator + advanceWorkflowActionPath + File.separator + advanceWorkflowActionFile);
		capWorkflow = wfBpFile.exists();

		JPanel footerPanel = new JPanel();
		footerPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.insets = new Insets(0, 8, 0, 0);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;

		gbc.gridx++;
		workflowToggleButton = new JToggleButton(new ImageIcon(ACE.class.getResource("/16x16/plain/media_step_forward.png")));
		workflowToggleButton.setToolTipText("show context sensitive actions available for this concept...");
		workflowToggleButton.setSelected(false);

		workflowToggleButton.addActionListener(new ActionListener() {

			private Collection<WfActivityBI> activiteis;

			@Override
			public void actionPerformed(ActionEvent e) {
				JToggleButton button = (JToggleButton) e.getSource();
				if (button.isSelected()) {
					if (((e.getModifiers() & ActionEvent.SHIFT_MASK) > 0) || ((e.getModifiers() & ActionEvent.ALT_MASK) > 0)) {
						showWizardPanel();
					} else {
						conceptScrollPane.setVisible(false);
						remove(conceptViewPanel);
						applicationWorkflowPanel.removeAll();
						conceptWorkflowPanel.removeAll();
						workflowPanel.removeAll();
						setupWorkflow();
					}
				} else {
					showConceptPanel();
				}
			}

			private void setupWorkflow() {

				List<UUID> availableActions = null;
				Collection<? extends WorkflowHistoryJavaBeanBI> possibleActions = null;

				// Application Workflow
				Collection<Action> actions = getKbActions();
				for (Action a : actions) {
					applicationWorkflowPanel.add(new JButton(a));
				}

				// Advancing Workflow

				if (capWorkflow) {

					try {
						if (settings.getConcept() != null) {
							// Must hit DB b/c of casting of getConcept() from
							// I_TermComp to I_GCD
							I_GetConceptData concept = Terms.get().getConcept(settings.getConcept().getConceptNid());
							availableActions = WorkflowHelper.getAllAvailableWorkflowActionUids();
							possibleActions = WorkflowHelper.getAvailableWorkflowActions(concept.getVersion(viewCoord), viewCoord);

							capWorkflowSetup(capWorkflow, availableActions, wfBpFile, possibleActions);
							capOopsButton();

							if (!WorkflowHelper.isWorkflowCapabilityAvailable()) {
								WorkflowStore ws = new WorkflowStore();
								I_GetConceptData user = Terms.get().getActiveAceFrameConfig().getDbConfig().getUserConcept();
								WfComponentProvider wcp = new WfComponentProvider();
								WfUser wfUser = wcp.getUserByUUID(user.getPrimUuid());
								Collection<WfProcessInstanceBI> incompleteProcessInstances = ws.getIncompleteProcessInstances(concept.getPrimUuid());
								for (WfProcessInstanceBI wfProcessInstanceBI : incompleteProcessInstances) {
									separator();
									JPanel instancePanel = new JPanel();
									JPanel instancePanelRow1 = new JPanel();
									instancePanelRow1.setLayout(new FlowLayout());
									JPanel instancePanelRow2 = new JPanel();
									instancePanelRow2.setLayout(new FlowLayout());

									instancePanel.setLayout(new BoxLayout(instancePanel, BoxLayout.Y_AXIS));
									instancePanel.add(instancePanelRow1);
									instancePanel.add(instancePanelRow2);

									Collection<WfActivityBI> activities = ws.getActivities(wfProcessInstanceBI, wfUser);

									JComboBox<WfActivityBI> jcombo = new JComboBox<WfActivityBI>();
									Set<WfActivityBI> activiteisSet = new HashSet<WfActivityBI>();
									for (WfActivityBI wfActivityBI : activities) {
										activiteisSet.add(wfActivityBI);
									}
									for (WfActivityBI wfActivityBI2 : activiteisSet) {
										jcombo.addItem(wfActivityBI2);
									}
									JButton advanceWorkflowButton = new JButton("GO");
									advanceWorkflowButton.addActionListener(new WorkflowActionListener(jcombo, wfProcessInstanceBI));
									JLabel worklistName = new JLabel(WordUtils.capitalize(wfProcessInstanceBI.getWorkList().getName()) + ": ");
									JLabel stateName = new JLabel(WordUtils.capitalize(wfProcessInstanceBI.getState().getName()));
									instancePanelRow1.add(worklistName);
									instancePanelRow1.add(stateName);
									instancePanelRow2.add(jcombo);
									instancePanelRow2.add(advanceWorkflowButton);
									conceptWorkflowPanel.add(instancePanel);
								}
								separator();
							}
						}
					} catch (Exception e1) {
						AceLog.getAppLog().log(Level.WARNING, "Error in setting up Workflow with error: " + e1.getMessage());
					}

				} else {
					AceLog.getAppLog().log(Level.WARNING, "Unable to find AdvanceWorkflow.bp file at path specified: " + advanceWorkflowActionPath + File.separator + advanceWorkflowActionFile);
				}

				workflowPanel.setTopComponent(applicationWorkflowPanel);
				workflowPanel.setBottomComponent(conceptWorkflowPanel);
				workflowPanel.setOneTouchExpandable(false);
				workflowPanel.setContinuousLayout(true);
				workflowPanel.setPreferredSize(getPreferredSize());
				workflowPanel.setVisible(true);
				setDividerLocation();

				add(workflowScrollPane, BorderLayout.CENTER);
				// Populate here...
				conceptScrollPane.setVisible(false);
				GuiUtil.tickle(ConceptViewRenderer.this);

				if (capWorkflow) {

					final JButton overrideButton = new JButton();
					overrideButton.setText("Override mode");
					overrideButton.setAlignmentY(LEFT_ALIGNMENT);

					overrideButton.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							JButton selectedOverrideButton = ((JButton) e.getSource());

							try {
								TreeSet<? extends ConceptVersionBI> wfStates = Terms.get().getActiveAceFrameConfig().getWorkflowStates();
								SortedSet<String> possibilities = new TreeSet<String>();

								for (ConceptVersionBI con : wfStates) {
									if (!WorkflowHelper.isBeginWorkflowState(con) && !WorkflowHelper.isEndWorkflowState(con)) {
										possibilities.add(con.getDescriptionPreferred().getText());
									}
								}

								String[] actualPossibilities = new String[possibilities.size()];
								int counter = 0;
								for (String s : possibilities) {
									actualPossibilities[counter++] = s;
								}

								String s = (String) JOptionPane.showInputDialog(selectedOverrideButton.getParent(), "Select a workflow state:", "Override", JOptionPane.PLAIN_MESSAGE, new ImageIcon(BatchMonitor.class.getResource("/24x24/plain/flag_green.png")), actualPossibilities,
										actualPossibilities[0]);

								if ((s != null) && (s.length() > 0)) {
									ConceptVersionBI currConcept = WorkflowHelper.lookupState(s + WORKFLOW_STATE_SUFFIX, viewCoord);
									I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
									I_Work worker;

									if (config.getWorker().isExecuting()) {
										worker = config.getWorker().getTransactionIndependentClone();
									} else {
										worker = config.getWorker();
									}

									// TODO: REMOVE HARD CODING!!!
									// TODO: Override Action Handler. . . Not
									// sure how used
									worker.writeAttachment(ProcessAttachmentKeys.SELECTED_WORKFLOW_ACTION.name(), ArchitectonicAuxiliary.Concept.WORKFLOW_OVERRIDE_ACTION.getPrimoridalUid());

									I_GetConceptData selectedConcept = settings.getConcept();

									workflowToggleButton.doClick();

									if (selectedConcept != null) {
										updateOopsButton(selectedConcept);

										UUID selectedActionUid = (UUID) worker.readAttachement(ProcessAttachmentKeys.SELECTED_WORKFLOW_ACTION.name());
										WorkflowHistoryRefsetWriter writer = new WorkflowHistoryRefsetWriter();

										WorkflowHistoryJavaBean bean = new WorkflowHistoryJavaBean();

										bean.setConcept(selectedConcept.getUids().iterator().next());

										bean.setPath(Terms.get().nidToUuid(selectedConcept.getConAttrs().getPathNid()));
										bean.setModeler(WorkflowHelper.getCurrentModeler().getPrimUuid());
										bean.setFullySpecifiedName(WorkflowHelper.identifyFSN(selectedConcept.getConceptNid(), viewCoord));
										bean.setAction(selectedActionUid);
										bean.setState(currConcept.getPrimUuid());
										bean.setOverridden(true);
										bean.setAutoApproved(false);

										java.util.Date today = new java.util.Date();
										bean.setWorkflowTime(today.getTime());

										WorkflowHistoryJavaBean latestBean = WorkflowHelper.getLatestWfHxJavaBeanForConcept(selectedConcept);
										if (latestBean == null || WorkflowHelper.getAcceptAction().equals(latestBean.getAction())) {
											bean.setWorkflowId(UUID.randomUUID());
										} else {
											bean.setWorkflowId(latestBean.getWorkflowId());
										}

										// Override
										WorkflowHelper.setAdvancingWorkflowLock(true);
										writer.updateWorkflowHistory(bean);
										setWorkflowStatusLabel(selectedConcept);
										wfHxDetails.regenerateWfPanel(selectedConcept, true);

										WorkflowHelper.setAdvancingWorkflowLock(false);
									}
								}
							} catch (Exception e1) {
								AceLog.getAppLog().log(Level.WARNING, "Error in Executing Override in Workflow with error: " + e1.getMessage());
							}
						}
					});

					if (WorkflowHelper.isWorkflowCapabilityAvailable()) {
						if (settings.getConcept() != null) {
							conceptWorkflowPanel.add(overrideButton);
						}
					}
				}
			}

			private void separator() {
				JSeparator comp = new JSeparator();
				comp.setPreferredSize(new Dimension(500, 10));
				conceptWorkflowPanel.add(comp);
			}

			class WorkflowActionListener implements ActionListener {
				private JComboBox<WfActivityBI> combo;
				private WfProcessInstanceBI wfInstance;

				public WorkflowActionListener(JComboBox<WfActivityBI> combo, WfProcessInstanceBI wfInstance) {
					this.combo = combo;
					this.wfInstance = wfInstance;
				}

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						I_GetConceptData user = Terms.get().getActiveAceFrameConfig().getDbConfig().getUserConcept();
						WfComponentProvider wcp = new WfComponentProvider();
						WfUser wfUser = wcp.getUserByUUID(user.getPrimUuid());

						WfActivity action = (WfActivity) combo.getSelectedItem();

						action.perform(wfInstance);

						WorkflowInterpreter wi = WorkflowInterpreter.createWorkflowInterpreter(((WfProcessDefinition) wfInstance.getProcessDefinition()).getDefinition());
						WfUser nextDestination = wi.getNextDestination((WfInstance) wfInstance, (WorkList) wfInstance.getWorkList());

						if (nextDestination != null) {
							WfInstance.updateDestination((WfInstance) wfInstance, nextDestination);
						} else {
							I_GetConceptData component = Terms.get().getConcept(((WfInstance) wfInstance).getComponentId());
							component.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
						}

						WorkflowStore ws = new WorkflowStore();
						Collection<WfActivityBI> activities = ws.getActivities(wfInstance, wfUser);
						combo.removeAllItems();
						for (WfActivityBI wfActivityBI : activities) {
							combo.addItem(wfActivityBI);
						}
						if (settings.activityInstanceNav != null && settings.activityInstanceNav.isVisible()) {
							settings.activityInstanceNav.updateActivityInstances(Terms.get().getConcept(wfInstance.getComponentPrimUuid()));
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}

			private void capWorkflowSetup(boolean capWorkflow, Collection<UUID> availableActions, File wfBpFile, final Collection<? extends WorkflowHistoryJavaBeanBI> possibleActions) {

				try {
					if (capWorkflow) {
						BpActionFactory actionFactory = new BpActionFactory(settings.getConfig(), settings.getHost(), wizardPanel);

						for (final UUID action : availableActions) {
							I_GetConceptData actionConcept = Terms.get().getConcept(action);
							List<RelationshipVersionBI<?>> relList = WorkflowHelper.getWorkflowRelationship(actionConcept.getVersion(viewCoord), ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE);

							for (RelationshipVersionBI<?> rel : relList) {
								if (rel != null && rel.getTargetNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_USER_ACTION.getPrimoridalUid()).getConceptNid()) {
									JButton advanceWorkflowButton = new JButton();

									BpAction a = (BpAction) actionFactory.make(wfBpFile);
									a.getExtraAttachments().put(ProcessAttachmentKeys.SELECTED_WORKFLOW_ACTION.name(), WorkflowHelper.lookupAction(WorkflowHelper.identifyPrefTerm(actionConcept.getConceptNid(), viewCoord) + WORKFLOW_ACTION_SUFFIX, viewCoord).getPrimUuid());
									a.getExtraAttachments().put(ProcessAttachmentKeys.POSSIBLE_WF_ACTIONS_LIST.name(), possibleActions);

									advanceWorkflowButton.setAction(a);

									advanceWorkflowButton.setHorizontalTextPosition(SwingConstants.CENTER);
									advanceWorkflowButton.setVerticalTextPosition(SwingConstants.BOTTOM);
									advanceWorkflowButton.setText(WorkflowHelper.identifyPrefTerm(actionConcept.getConceptNid(), viewCoord));

									advanceWorkflowButton.addActionListener(new ActionListener() {

										@Override
										public void actionPerformed(ActionEvent e) {

											try {
												// TODO: Remove Hardcoding and
												// find better fix to issue of
												// Pref-Term vs FSN
												// As no available actions will
												// exist for null concept, this
												// check for completeness
												if (settings.getConcept() != null) {
													updateOopsButton(settings.getConcept());

													// Advance WF
													for (WorkflowHistoryJavaBeanBI bean : possibleActions) {
														if (bean.getAction().equals(action)) {
															// Commit done by
															// BP, just update
															// display
															setWorkflowStatusLabel(bean);
															wfHxDetails.regenerateWfData(bean);
															workflowToggleButton.doClick();
															break;
														}
													}
												}
											} catch (Exception e1) {
												AceLog.getAppLog().log(Level.WARNING, "Error Advancing Workflow with error: " + e1.getMessage());
												// As no available actions will
												// exist for null concept, this
												// check for completeness
												if (settings.getConcept() != null) {
													updateOopsButton(settings.getConcept());
												}
											}
										}
									});

									if (WorkflowHelper.isActiveAction(possibleActions, action)) {
										advanceWorkflowButton.setEnabled(true);
									} else {
										advanceWorkflowButton.setEnabled(false);
									}

									if (WorkflowHelper.isWorkflowCapabilityAvailable()) {
										conceptWorkflowPanel.add(advanceWorkflowButton);
									}
								}
							}
						}
					}
				} catch (Exception e) {
					AceLog.getAppLog().log(Level.WARNING, "Error in setting up Workflow with error: " + e.getMessage());
				}
			}

			private void capOopsButton() {
				oopsButton = new JToggleButton("Undo");
				oopsButton.setToolTipText("Undo last action on this concept");

				oopsButton.setHorizontalTextPosition(SwingConstants.CENTER);
				oopsButton.setVerticalTextPosition(SwingConstants.BOTTOM);

				oopsButton.setSelected(false);

				oopsButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {

						try {

							if (settings.getConcept() != null) {
								WorkflowHistoryJavaBean latestWfHxJavaBean = WorkflowHelper.getLatestWfHxJavaBeanForConcept(settings.getConcept());
								UUID latestModelerUUID = latestWfHxJavaBean.getModeler();
								UUID currentModelerUUID = WorkflowHelper.getCurrentModeler().getPrimUuid();
								boolean autoApproved = latestWfHxJavaBean.getAutoApproved();

								// See if state prevents retirement
								if (currentModelerUUID.equals(latestModelerUUID) && !autoApproved) {
									updateOopsButton(settings.getConcept());

									// Oops button
									WorkflowHelper.setAdvancingWorkflowLock(true);
									WorkflowHelper.retireWorkflowHistoryRow(latestWfHxJavaBean, viewCoord);
									setWorkflowStatusLabel(settings.getConcept());
									wfHxDetails.regenerateWfPanel(settings.getConcept(), true);

									workflowToggleButton.doClick();

									updateOopsButton(settings.getConcept());
									WorkflowHelper.setAdvancingWorkflowLock(false);
								}
							}
						} catch (Exception e1) {
							AceLog.getAppLog().log(Level.WARNING, "Error in performing Undo on Workflow with error: " + e1.getMessage());
						}
					}
				});

				updateOopsButton(settings.getConcept());
				if (settings.getConcept() != null) {
					if (WorkflowHelper.isWorkflowCapabilityAvailable()) {
						conceptWorkflowPanel.add(oopsButton);
					}
				}
			}
		});

		workflowToggleButton.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
		footerPanel.add(workflowToggleButton, gbc);
		workflowToggleButton.setCursor(Cursor.getDefaultCursor());

		gbc.gridx++;
		wfHxDetails = new WfHxDetailsPanelHandler(this, settings);
		wfHxDetailsToggleButton = new JToggleButton(new ImageIcon(ACE.class.getResource("/16x16/plain/workflow_history.png")));
		wfHxDetailsToggleButton.setToolTipText("show workflow history details for this concept...");
		wfHxDetailsToggleButton.setSelected(false);
		// wfHxDetailsToggleButton.setSelected(false);
		wfHxDetailsToggleButton.setVisible(true);
		wfHxDetailsToggleButton.setCursor(Cursor.getDefaultCursor());

		wfHxDetailsToggleButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JToggleButton button = (JToggleButton) e.getSource();
				if (button.isSelected()) {
					wfHxDetails.showWfHxDetailsPanel(settings.getConcept());
				} else {
					wfHxDetails.hideWfHxDetailsPanel();
				}
			}
		});

		wfHxDetailsToggleButton.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
		if (WorkflowHelper.isWorkflowCapabilityAvailable()) {
			footerPanel.add(wfHxDetailsToggleButton, gbc);
		}

		gbc.gridx++;

		workflowStatusLabel = new JLabel("");
		setWorkflowStatusLabel(settings.getConcept());
		if (WorkflowHelper.isWorkflowCapabilityAvailable()) {
			footerPanel.add(workflowStatusLabel, gbc);
		}

		gbc.gridx++;

		gbc.weightx = 1;
		JPanel fillerPanel = new JPanel();
		fillerPanel.setBackground(footerPanel.getBackground());

		footerPanel.add(fillerPanel, gbc);

		gbc.weightx = 0;

		gbc.gridx++;
		conflictButton = new JButton(new ImageIcon(DragPanelRel.class.getResource("/16x16/plain/warning.png")));
		conflictButton.setToolTipText("there are conflicts on the concept");
		conflictButton.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
		conflictButton.setVisible(false);
		conflictButton.setCursor(Cursor.getDefaultCursor());
		footerPanel.add(conflictButton, gbc);

		acceptButton = new JButton(new ImageIcon(ACE.class.getResource("/16x16/plain/stamp.png")));
		if (settings.isForAdjudication()) {
			gbc.gridx++;
			acceptButton.setToolTipText("accept concept as is");
			acceptButton.addActionListener(new AcceptActionListener(settings));
			acceptButton.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
			acceptButton.setCursor(Cursor.getDefaultCursor());
			footerPanel.add(acceptButton, gbc);
		}

		promoteButton = new JButton(new ImageIcon(ACE.class.getResource("/16x16/plain/navigate_check.png")));
		if (settings.isForPromotion()) {
			gbc.gridx++;
			promoteButton.setToolTipText("mark as ready for promotion");
			promoteButton.addActionListener(new PromoteActionListener(settings));
			promoteButton.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
			promoteButton.setCursor(Cursor.getDefaultCursor());
			footerPanel.add(promoteButton, gbc);
		}

		gbc.gridx++;
		cancelButton = new JButton(new ImageIcon(ACE.class.getResource("/16x16/plain/delete.png")));
		cancelButton.setToolTipText("cancel changes to concept");
		cancelButton.addActionListener(new CancelActionListener(settings));
		cancelButton.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
		cancelButton.setVisible(false);
		cancelButton.setCursor(Cursor.getDefaultCursor());
		footerPanel.add(cancelButton, gbc);

		gbc.gridx++;
		commitButton = new JButton(new ImageIcon(ACE.class.getResource("/16x16/plain/check.png")));
		commitButton.setToolTipText("commit changes to concept");
		commitButton.addActionListener(new CommitActionListener(settings));
		commitButton.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
		commitButton.setVisible(false);
		commitButton.setCursor(Cursor.getDefaultCursor());
		footerPanel.add(commitButton, gbc);

		gbc.gridx++;
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		footerPanel.add(settings.getResizeLabel(), gbc);
		footerPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.gray));
		footerPanel.setCursor(Cursor.getDefaultCursor());
		add(footerPanel, BorderLayout.SOUTH);

		setMinimumSize(new Dimension(40, 20));
		RendererComponentAdaptor rca = new RendererComponentAdaptor();
		addAncestorListener(rca);
		addComponentListener(rca);
		this.settings.addHostListener(new HostListener());
	}

	protected void updateLabel() {
		showConceptPanel();
		title.updateTitle();
	}

	private Collection<Action> getKbActions() {
		Collection<Action> actions = new ArrayList<Action>();

		try {
			ViewCoordinate coordinate = settings.getConfig().getViewCoordinate();
			Map<String, Object> globals = new HashMap<String, Object>();
			globals.put("vc", coordinate);
			globals.put("actions", actions);
			globals.put("actionFactory", new BpActionFactory(settings.getConfig(), settings.getHost(), wizardPanel));
			Collection<Object> facts = new ArrayList<Object>();
			ConceptVersionBI concept;
			if (settings.getConcept() != null) {
				concept = settings.getConcept().getVersion(coordinate);
			} else {
				concept = null;
			}
			facts.add(new ConceptFact(Context.FOCUS_CONCEPT, concept, coordinate));

			DroolsExecutionManager.fireAllRules(ConceptViewRenderer.class.getCanonicalName(), kbFiles, globals, facts, false);
		} catch (Throwable e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return actions;
	}

	public static ConceptViewRenderer getVertex(Component component) {
		while (component != null) {
			if (component instanceof ConceptViewRenderer) {
				return (ConceptViewRenderer) component;
			}
			component = component.getParent();
		}

		return null;
	}

	private class HostListener implements PropertyChangeListener {

		int lastNid = Integer.MAX_VALUE;

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getNewValue() == null) {
				updateLabel();
			} else {
				I_GetConceptData concept = (I_GetConceptData) evt.getNewValue();
				if (concept.getNid() != lastNid) {
					lastNid = concept.getNid();
					updateLabel();
				}

				if (capWorkflow) {
					setWorkflowStatusLabel(concept);
					if (wfHxDetails.isWfHxDetailsCurrenltyDisplayed()) {
						wfHxDetails.regenerateWfPanel(concept, false);
					} else {
						wfHxDetailsToggleButton.setSelected(false);
					}
				}
			}

			updateCancelAndCommit();
		}

	}

	protected void updateCancelAndCommit() {
		if (settings != null && cancelButton != null && commitButton != null) {
			if (settings.getConcept() != null && settings.getConcept().isUncommitted()) {
				cancelButton.setVisible(true);
				commitButton.setVisible(true);
				if (settings.isForAdjudication()) {
					acceptButton.setVisible(false);
				}
				if (settings.isForPromotion()) {
					promoteButton.setVisible(false);
				}
			} else {
				cancelButton.setVisible(false);
				commitButton.setVisible(false);
				if (settings.isForAdjudication()) {
					acceptButton.setVisible(true);
				}
				if (settings.isForPromotion()) {
					promoteButton.setVisible(true);
				}

				if (settings.getConcept() != null) {
					if (wfHxDetails.isWfHxDetailsCurrenltyDisplayed()) {
						wfHxDetails.regenerateWfPanel(settings.getConcept(), false);
					}

					setWorkflowStatusLabel(settings.getConcept());
				}
			}
		}
	}

	protected void showConflictIcon(boolean isShowing) {
		conflictButton.setVisible(isShowing);
		if (settings.isForAdjudication() && isShowing == false) {
			acceptButton.setVisible(false);
		}
	}

	private void setWorkflowStatusLabel(WorkflowHistoryJavaBeanBI hxBean) {
		try {
			if (hxBean != null) {
				ViewCoordinate coordinate = settings.getConfig().getViewCoordinate();

				StringBuilder str = new StringBuilder();
				str.append(hxBean.getStateForTitleBar(coordinate));
				str.append(": ");
				str.append(hxBean.getModelerForTitleBar(coordinate));

				workflowStatusLabel.setText(str.toString());
			}
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Error in identifying wf display values for arena");
		}

	}

	private void setWorkflowStatusLabel() {
		workflowStatusLabel.setText("");
	}

	private void setWorkflowStatusLabel(I_GetConceptData concept) {
		try {
			if (concept != null) {
				WorkflowHistoryJavaBean hxBean = WorkflowHelper.getLatestWfHxJavaBeanForConcept(concept);
				ViewCoordinate coordinate = settings.getConfig().getViewCoordinate();

				if (hxBean != null) {
					StringBuilder str = new StringBuilder();
					str.append(hxBean.getStateForTitleBar(coordinate));
					str.append(": ");
					str.append(hxBean.getModelerForTitleBar(coordinate));

					workflowStatusLabel.setText(str.toString());
					return;
				}
			}
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Error in identifying wf display values for arena");
		}

		workflowStatusLabel.setText("");
	}

	private void updateOopsButton(I_GetConceptData concept) {
		boolean enableOopsButton = true;
		try {
			if (concept != null) {
				TreeSet<WorkflowHistoryJavaBean> latestWfHxSet = WorkflowHelper.getLatestWfHxForConcept(concept);

				if (latestWfHxSet == null || latestWfHxSet.size() == 0) {
					// Only if have history
					enableOopsButton = false;
				} else {
					// Test if latest modeler action on concept is current
					// moderl
					UUID latestModelerUUID = latestWfHxSet.last().getModeler();
					UUID currentModelerUUID = WorkflowHelper.getCurrentModeler().getPrimUuid();

					if (!currentModelerUUID.equals(latestModelerUUID)) {
						enableOopsButton = false;
					} else {
						// Test if latest action is AutoApproval
						boolean islatestActionAutoApproved = latestWfHxSet.last().getAutoApproved();
						if (islatestActionAutoApproved) {
							enableOopsButton = false;
						} else {
							// Test if single value in workflow and it is a
							// Begin WF Action
							if (latestWfHxSet.size() == 1 && WorkflowHelper.isBeginWorkflowState(latestWfHxSet.last().getState())) {
								enableOopsButton = false;
							}
						}
					}
				}

				oopsButton.setEnabled(enableOopsButton);
			} else {
				oopsButton.setEnabled(false);
			}

		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Error in finding Undo-Button's State with error: " + e.getMessage());
		}
	}

	public void showConceptPanel() {
		wizardPanel.setVisible(false);
		remove(wizardScrollPane);
		workflowPanel.setVisible(false);
		remove(workflowScrollPane);
		conceptViewPanel.setVisible(true);
		workflowToggleButton.setSelected(false);
		workflowToggleButton.setVisible(true);
		wfHxDetailsToggleButton.setVisible(true);
		if (this.getIndexOf(conceptViewPanel) < 0) {
			add(conceptViewPanel, BorderLayout.CENTER);
		}

		conceptScrollPane.setVisible(true);
		// GuiUtil.tickle(ConceptViewRenderer.this);
	}

	public void showWizardPanel() {
		conceptViewPanel.setVisible(false);
		remove(conceptViewPanel);
		wizardPanel.setVisible(true);
		workflowPanel.setVisible(false);
		remove(workflowScrollPane);
		add(wizardScrollPane, BorderLayout.CENTER);
		workflowToggleButton.setVisible(false);
		wfHxDetailsToggleButton.setVisible(false);
		GuiUtil.tickle(ConceptViewRenderer.this);
	}

	public void updateWizardPanel() {
		if (workflowToggleButton.isSelected()) {
			showWizardPanel();
		}
	}

	private void setDividerLocation() {
		int dividerLocation = workflowPanel.getHeight() / 2;
		workflowPanel.setDividerLocation(dividerLocation);
	}

	public JScrollPane getConceptScrollPane() {
		return conceptScrollPane;
	}

	public WizardPanel getWizardPanel() {
		return wizardPanel;
	}
}
