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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.drools.KnowledgeBase;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.batch.BatchMonitor;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.WizardPanel;
import org.ihtsdo.arena.context.action.BpActionFactory;
import org.ihtsdo.arena.drools.EditPanelKb;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.workflow.WorkflowHandlerBI;
import org.ihtsdo.tk.api.workflow.WorkflowHistoryJavaBeanBI;
import org.ihtsdo.util.swing.GuiUtil;
import org.ihtsdo.workflow.WorkflowHandler;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetSearcher;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

/**
 * @author Administrator
 *
 */
public class ConceptViewRenderer extends JLayeredPane {

    final String CHANGED_WORKFLOW_STATE = "Changed workflow state";
    final String CHANGED_IN_BATCH_WORKFLOW_STATE = "Changed in batch workflow state";
    final String CONCEPT_HAVING_NO_PRIOR_WORKFLOW_STATE = "Concept having no prior workflow state";
    final String CONCEPT_NOT_PREVIOUSLY_EXISTING_WORKFLOW_STATE = "Concept not previously existing workflow state";
    final String NEW_WORKFLOW_STATE = "New workflow state";
    final String WORKFLOW_STATE_SUFFIX = " workflow state";

    private class RendererComponentAdaptor extends ComponentAdapter implements AncestorListener {

        @Override
        public void componentMoved(ComponentEvent e) {
            settings.hideNavigator();
        }

        @Override
        public void componentResized(ComponentEvent e) {
            settings.hideNavigator();
            setDividerLocation();
        }

        @Override
        public void ancestorMoved(AncestorEvent event) {
            settings.hideNavigator();
        }

        @Override
        public void ancestorRemoved(AncestorEvent event) {
            settings.hideNavigator();
        }

        @Override
        public void ancestorAdded(AncestorEvent event) {
            settings.hideNavigator();
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
    public JComponent renderedComponent;
    private ConceptViewSettings settings;
    private ConceptViewTitle title;
    private JSplitPane workflowPanel =
            new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    private JPanel applicationWorkflowPanel =
            new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    private JPanel conceptWorkflowPanel =
            new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    private JScrollPane workflowScrollPane = new JScrollPane(workflowPanel,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    private WizardPanel wizardPanel;
    private JScrollPane wizardScrollPane;
    private KnowledgeBase contextualConceptActionsKBase;
    private JScrollPane scrollPane;
    private JToggleButton workflowToggleButton;
    private JToggleButton oopsButton;
    private final static String advanceWorkflowActionPath = "migration-wf";
    private final String advanceWorkflowActionFile = "AdvanceWorkflow.bp";
    private JPanel historyPanel = new JPanel(new BorderLayout());

    public JPanel getHistoryPanel() {
        return historyPanel;
    }
    private JPanel conceptViewPanel = new JPanel(new BorderLayout());

    /**
     *
     */
    public ConceptViewRenderer(Object cellObj,
            final mxGraphComponent graphContainer, ACE ace) {

        wizardPanel =
                new WizardPanel(new FlowLayout(FlowLayout.LEADING, 10, 10), this);
        wizardScrollPane = new JScrollPane(wizardPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        try {
            contextualConceptActionsKBase = EditPanelKb.setupKb(
                    new File("drools-rules/ContextualConceptActionsPanel.drl"));
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
        applicationWorkflowPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        conceptWorkflowPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        this.cell = (mxCell) cellObj;
        this.graphContainer = graphContainer;
        this.graph = graphContainer.getGraph();
        this.settings = (ConceptViewSettings) this.cell.getValue();
        this.settings.setup(ace, cell, graphContainer, graph, this);
        wizardPanel.add(new JLabel("Wizard Panel"));
        setLayout(new BorderLayout());

        title = new ConceptViewTitle(graph, cell, settings);

        add(title, BorderLayout.NORTH);

        scrollPane = null;

        if (graph.getModel().getChildCount(cell) == 0) {
            renderedComponent = settings.getComponent(ace.getAceFrameConfig());
            if (JScrollPane.class.isAssignableFrom(renderedComponent.getClass())) {
                scrollPane = (JScrollPane) renderedComponent;
            } else {
                scrollPane = new JScrollPane(renderedComponent);
            }
        }

        if (scrollPane != null) {
            conceptViewPanel.add(historyPanel, BorderLayout.NORTH);
            conceptViewPanel.add(scrollPane, BorderLayout.CENTER);
            add(conceptViewPanel, BorderLayout.CENTER);
            scrollPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            scrollPane.getViewport().setBackground(Color.WHITE);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            setOpaque(true);
            scrollPane.getVerticalScrollBar().addAdjustmentListener(
                    new AdjustmentListener() {

                        @Override
                        public void adjustmentValueChanged(AdjustmentEvent e) {
                            graphContainer.refresh();
                        }
                    });
            scrollPane.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.GRAY));
        }


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
        workflowToggleButton = new JToggleButton(
                new ImageIcon(ACE.class.getResource("/16x16/plain/media_step_forward.png")));
        workflowToggleButton.setToolTipText("show context sensitive actions available for this concept...");
        workflowToggleButton.setSelected(false);

        workflowToggleButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JToggleButton button = (JToggleButton) e.getSource();
                if (button.isSelected()) {
                    if (((e.getModifiers() & ActionEvent.SHIFT_MASK) > 0)
                            || ((e.getModifiers() & ActionEvent.ALT_MASK) > 0)) {
                        showWizardPanel();
                    } else {
                        scrollPane.setVisible(false);
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
                Collection<Action> actions = getKbActions();
                for (Action a : actions) {
                    applicationWorkflowPanel.add(new JButton(a));
                }

                workflowPanel.setTopComponent(applicationWorkflowPanel);

                WorkflowHandlerBI wfHandler = new WorkflowHandler();
                Collection<? extends WorkflowHistoryJavaBeanBI> possibleActions = null;
                Collection<UUID> availableActions = null;
                try {
                    ViewCoordinate coordinate = settings.getConfig().getViewCoordinate();
                    ConceptVersionBI concept = Ts.get().getConceptVersion(coordinate, settings.getConcept().getPrimUuid());
                    availableActions = wfHandler.getAllAvailableWorkflowActionUids();
                    possibleActions = wfHandler.getAvailableWorkflowActions(concept);
                } catch (IOException e1) {
                    AceLog.getAppLog().alertAndLogException(e1);
                } catch (ContraditionException e2) {
                    AceLog.getAppLog().alertAndLogException(e2);
                }


                File wfBpFile = new File(advanceWorkflowActionPath
                        + File.separator + advanceWorkflowActionFile);
                boolean capWorkflow = wfBpFile.exists();
                if (capWorkflow) {
                    capWorkflowSetup(capWorkflow, availableActions, wfBpFile,
                            wfHandler, possibleActions);
                    capOopsButton();
                } else {
                    AceLog.getAppLog().log(Level.SEVERE,
                            "Unable to find AdvanceWorkflow.bp file at path specified: "
                            + advanceWorkflowActionPath + File.separator
                            + advanceWorkflowActionFile, new FileNotFoundException());
                }

                workflowPanel.setBottomComponent(conceptWorkflowPanel);
                workflowPanel.setOneTouchExpandable(false);
                workflowPanel.setContinuousLayout(true);
                workflowPanel.setPreferredSize(getPreferredSize());
                workflowPanel.setVisible(true);
                setDividerLocation();

                add(workflowScrollPane, BorderLayout.CENTER);
                // Populate here...
                scrollPane.setVisible(false);
                GuiUtil.tickle(ConceptViewRenderer.this);

                if (capWorkflow) {

                    final JButton override = new JButton();
                    override.setText("Override mode");
                    override.setAlignmentY(LEFT_ALIGNMENT);

                    override.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {

                            JButton override = ((JButton) e.getSource());

                            TreeSet wfStates = WorkflowHelper.returnWorkflowStates();
                            Iterator i = wfStates.iterator();

                            int j = 0;

                            String[] possibilities = new String[wfStates.size()];

                            while (i.hasNext()) {
                                String currCon = i.next().toString();
                                if (currCon.equals(CHANGED_WORKFLOW_STATE)
                                        || currCon.equals(CHANGED_IN_BATCH_WORKFLOW_STATE)
                                        || currCon.equals(CONCEPT_HAVING_NO_PRIOR_WORKFLOW_STATE)
                                        || currCon.equals(CONCEPT_NOT_PREVIOUSLY_EXISTING_WORKFLOW_STATE)
                                        || currCon.equals(NEW_WORKFLOW_STATE)) {
                                    i.remove();
                                } else {
                                    possibilities[j++] = currCon.replaceAll(WORKFLOW_STATE_SUFFIX, "");
                                }
                            }

                            possibilities = Arrays.copyOf(possibilities, wfStates.size());


                            String s = (String) JOptionPane.showInputDialog(override.getParent(), "Select a workflow state:",
                                    "Override",
                                    JOptionPane.PLAIN_MESSAGE,
                                    new ImageIcon(BatchMonitor.class.getResource("/24x24/plain/flag_green.png")),
                                    possibilities,
                                    possibilities[0]);

                            if ((s != null) && (s.length() > 0)) {

                                I_GetConceptData currConcept = null;

                                for (Object c : wfStates) {
                                    if (c.toString().equals(s + WORKFLOW_STATE_SUFFIX)) {
                                        currConcept = (I_GetConceptData) c;
                                    }
                                }

                                try {

                                    I_ConfigAceFrame config;

                                    config = Terms.get().getActiveAceFrameConfig();

                                    I_Work worker;

                                    if (config.getWorker().isExecuting()) {
                                        worker = config.getWorker().getTransactionIndependentClone();
                                    } else {
                                        worker = config.getWorker();
                                    }

                                    String action = getActionFromState(s);

                                    worker.writeAttachment(ProcessAttachmentKeys.SELECTED_WORKFLOW_ACTION.name(), WorkflowHelper.lookupAction(action).getPrimUuid());

                                    I_GetConceptData selectedConcept = settings.getConcept();

                                    workflowToggleButton.doClick();
                                    updateOopsButton(selectedConcept);

                                    UUID selectedActionUid = (UUID) worker.readAttachement(ProcessAttachmentKeys.SELECTED_WORKFLOW_ACTION.name());
                                    WorkflowHistoryRefsetWriter writer = new WorkflowHistoryRefsetWriter();

                                    WorkflowHistoryRefsetSearcher searcher = new WorkflowHistoryRefsetSearcher();
                                    WorkflowHistoryJavaBean bean = new WorkflowHistoryJavaBean();

                                    bean.setPath(Terms.get().nidToUuid(selectedConcept.getConceptAttributes().getPathNid()));
                                    bean.setModeler(WorkflowHelper.getCurrentModeler().getPrimUuid());
                                    bean.setConcept(selectedConcept.getUids().iterator().next());
                                    bean.setFSN(WorkflowHelper.identifyFSN(selectedConcept));
                                    java.util.Date today = new java.util.Date();
                                    bean.setWorkflowTime(today.getTime());

                                    WorkflowHistoryJavaBean latestBean = searcher.getLatestWfHxJavaBeanForConcept(selectedConcept);

                                    if (!isConceptInCurrentWorkflow(latestBean)) {
                                        bean.setWorkflowId(UUID.randomUUID());
                                    } else {
                                        bean.setWorkflowId(latestBean.getWorkflowId());
                                    }

                                    bean.setOverridden(true);
                                    bean.setAction(selectedActionUid);
                                    bean.setState(currConcept.getPrimUuid());
                                    writer.updateWorkflowHistory(bean);

                                } catch (IOException e2) {
                                    AceLog.getAppLog().alertAndLogException(e2);
                                } catch (TerminologyException e2) {
                                    AceLog.getAppLog().alertAndLogException(e2);
                                } catch (Exception e1) {
                                    AceLog.getAppLog().alertAndLogException(e1);
                                }
                            }
                        }
                    });

                    conceptWorkflowPanel.add(override);
                }
            }

            private boolean isConceptInCurrentWorkflow(WorkflowHistoryJavaBean latestBean) throws Exception {
                if (latestBean == null) {
                    return false;
                } else {
                    I_GetConceptData action = Terms.get().getConcept(latestBean.getAction());

                    List<I_RelVersioned> relList = WorkflowHelper.getWorkflowRelationship(action, ArchitectonicAuxiliary.Concept.WORKFLOW_ACTION_VALUE);

                    for (I_RelVersioned rel : relList) {
                        if (rel != null
                                && rel.getC2Id() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKFLOW_ACCEPT_ACTION.getPrimoridalUid()).getConceptNid()) {
                            return false;
                        }
                    }
                }

                return true;
            }

            private void capWorkflowSetup(boolean capWorkflow,
                    Collection<UUID> availableActions, File wfBpFile,
                    WorkflowHandlerBI wfHandler,
                    Collection<? extends WorkflowHistoryJavaBeanBI> possibleActions) {
                if (capWorkflow) {
                    try {
                        if (capWorkflow) {
                            BpActionFactory actionFactory =
                                    new BpActionFactory(settings.getConfig(),
                                    settings.getHost(), wizardPanel);
                            for (UUID action : availableActions) {

                                JButton actionButton = new JButton();

                                Action a = actionFactory.make(wfBpFile);
                                actionButton.setAction(a);

                                actionButton.setHorizontalTextPosition(SwingConstants.CENTER);
                                actionButton.setVerticalTextPosition(SwingConstants.BOTTOM);
                                actionButton.setText(Terms.get().getConcept(action).getInitialText());

                                actionButton.addActionListener(new ActionListener() {

                                    @Override
                                    public void actionPerformed(ActionEvent e) {

                                        try {
                                            final I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

                                            // Get Worker
                                            I_Work worker;
                                            if (config.getWorker().isExecuting()) {
                                                worker = config.getWorker().getTransactionIndependentClone();
                                            } else {
                                                worker = config.getWorker();
                                            }

                                            worker.writeAttachment(ProcessAttachmentKeys.SELECTED_WORKFLOW_ACTION.name(), WorkflowHelper.lookupAction(e.getActionCommand()).getPrimUuid());
                                            workflowToggleButton.doClick();
                                            updateOopsButton(settings.getConcept());

                                        } catch (Exception e1) {
                                            AceLog.getAppLog().alertAndLogException(e1);
                                        }

                                    }
                                });

                                if (wfHandler.isActiveAction(possibleActions, action)) {
                                    actionButton.setEnabled(true);
                                } else {
                                    actionButton.setEnabled(false);
                                }

                                conceptWorkflowPanel.add(actionButton);
                            }
                        }

                    } catch (TerminologyException e2) {
                        AceLog.getAppLog().alertAndLogException(e2);
                    } catch (IOException e2) {
                        AceLog.getAppLog().alertAndLogException(e2);
                    }
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
                                WorkflowHistoryRefsetSearcher searcher =
                                        new WorkflowHistoryRefsetSearcher();
                                WorkflowHistoryJavaBean latestWfHxJavaBean =
                                        searcher.getLatestWfHxJavaBeanForConcept(settings.getConcept());
                                UUID latestModelerUUID =
                                        latestWfHxJavaBean.getModeler();
                                UUID currentModelerUUID =
                                        WorkflowHelper.getCurrentModeler().getPrimUuid();
                                boolean autoApproved = latestWfHxJavaBean.getAutoApproved();

                                // See if state prevents retirement
                                if (currentModelerUUID.equals(latestModelerUUID) && !autoApproved) {
                                    WorkflowHelper.retireWorkflowHistoryRow(latestWfHxJavaBean);
                                    updateOopsButton(settings.getConcept());
                                    workflowToggleButton.doClick();
                                }
                            } else {
                                throw new Exception("Must have a concept to reverse workflow on");
                            }

                        } catch (TerminologyException e1) {
                            AceLog.getAppLog().alertAndLogException(e1);
                        } catch (IOException e1) {
                            AceLog.getAppLog().alertAndLogException(e1);
                        } catch (Exception e1) {
                            AceLog.getAppLog().alertAndLogException(e1);
                        }
                    }
                });

                updateOopsButton(settings.getConcept());
                conceptWorkflowPanel.add(oopsButton);
            }
        });

        workflowToggleButton.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        footerPanel.add(workflowToggleButton, gbc);


        gbc.gridx++;

        gbc.weightx = 1;
        JPanel fillerPanel = new JPanel();
        fillerPanel.setBackground(footerPanel.getBackground());

        footerPanel.add(fillerPanel, gbc);

        gbc.weightx = 0;
        gbc.gridx++;
        JButton cancelButton = new JButton(new ImageIcon(
                ACE.class.getResource("/16x16/plain/delete.png")));
        cancelButton.setToolTipText("cancel changes to concept");
        cancelButton.addActionListener(new CancelActionListener(settings));
        cancelButton.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        footerPanel.add(cancelButton, gbc);

        gbc.gridx++;
        JButton commitButton = new JButton(new ImageIcon(
                ACE.class.getResource("/16x16/plain/check.png")));
        commitButton.setToolTipText("commit changes to concept");
        commitButton.addActionListener(new CommitActionListener(settings));
        commitButton.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        footerPanel.add(commitButton, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        footerPanel.add(settings.getResizeLabel(), gbc);
        footerPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.gray));
        add(footerPanel, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(40, 20));
        RendererComponentAdaptor rca = new RendererComponentAdaptor();
        addAncestorListener(rca);
        addComponentListener(rca);
        this.settings.addHostListener(new HostListener());
    }

    private void updateLabel() {
        title.updateTitle();
    }

    private Collection<Action> getKbActions() {
        Collection<Action> actions = new ArrayList<Action>();

        try {
            StatefulKnowledgeSession ksession =
                    contextualConceptActionsKBase.newStatefulKnowledgeSession();
            boolean uselogger = false;

            KnowledgeRuntimeLogger logger = null;
            if (uselogger) {
                logger = KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
            }
            try {

                ViewCoordinate coordinate = settings.getConfig().getViewCoordinate();
                ksession.setGlobal("vc", coordinate);
                ksession.setGlobal("actions", actions);
                ksession.setGlobal("actionFactory", new BpActionFactory(
                        settings.getConfig(),
                        settings.getHost(), wizardPanel));
                if (settings.getConcept() != null) {
                    ksession.insert(Ts.get().getConceptVersion(coordinate,
                            settings.getConcept().getNid()));
                } else {
                    ksession.insert("null concept");
                }
                ksession.fireAllRules();
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } finally {
                if (logger != null) {
                    logger.close();
                }
            }
        } catch (Throwable e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return actions;
    }

    private String getActionFromState(String state) {

        if (state.equals("Approved")) {
            return "Accept";
        }
        if (state.equals("Escalated")) {
            return "Escalate";
        }
        if (state.equals("For Chief Terminologist review")) {
            return "Chief Terminologist review";
        }
        if (state.equals("For discussion")) {
            return "Discuss";
        }
        if (state.equals("For review")) {
            return "Review";
        }

        return "";

    }

    /**
     *
     */
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

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            updateLabel();
        }
    }

    private void updateOopsButton(I_GetConceptData concept) {
        try {
            WorkflowHistoryRefsetSearcher searcher =
                    new WorkflowHistoryRefsetSearcher();

            WorkflowHistoryJavaBean latestWfHxJavaBean =
                    searcher.getLatestWfHxJavaBeanForConcept(concept);

            if (latestWfHxJavaBean == null
                    || WorkflowHelper.isBeginWorkflowAction(Terms.get().getConcept(latestWfHxJavaBean.getAction()))) {
                oopsButton.setEnabled(false);
            } else {
                oopsButton.setEnabled(true);
            }
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

    public void showConceptPanel() {
        wizardPanel.setVisible(false);
        remove(wizardScrollPane);
        workflowPanel.setVisible(false);
        remove(workflowScrollPane);
        
        add(conceptViewPanel, BorderLayout.CENTER);
        scrollPane.setVisible(true);
    }

    public void showWizardPanel() {
        conceptViewPanel.setVisible(false);
        remove(conceptViewPanel);
        wizardPanel.setVisible(true);
        add(wizardScrollPane, BorderLayout.CENTER);
        GuiUtil.tickle(ConceptViewRenderer.this);
    }

    private void setDividerLocation() {
        int dividerLocation = workflowPanel.getHeight() / 2;
        workflowPanel.setDividerLocation(dividerLocation);
    }
}
