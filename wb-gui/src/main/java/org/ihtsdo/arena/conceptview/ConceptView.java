package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.ScrollablePanel;
import org.ihtsdo.arena.context.action.DropActionPanel;
import org.ihtsdo.arena.drools.EditPanelKb;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.conattr.ConAttrAnalogBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;
import org.ihtsdo.tk.drools.facts.Context;
import org.ihtsdo.tk.drools.facts.FactFactory;
import org.ihtsdo.tk.spec.DescriptionSpec;
import org.ihtsdo.tk.spec.RelSpec;
import org.ihtsdo.tk.spec.SpecBI;
import org.ihtsdo.tk.spec.SpecFactory;
import org.ihtsdo.util.swing.GuiUtil;
import org.intsdo.tk.drools.manager.DroolsExecutionManager;

public class ConceptView extends JPanel {

    private final ConceptViewRenderer cvRenderer;

    private class PanelsChangedActionListener
            implements ActionListener, ChangeListener, PropertyChangeListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            doUpdateLater();
        }

        private void doUpdateLater() {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            settings.getNavigator().updateHistoryPanel();
                        }
                    });
                }
            });
        }

        @Override
        public void stateChanged(ChangeEvent ce) {
            doUpdateLater();
        }

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            doUpdateLater();
        }
    }

    private void setupPrefMap() {
        prefMap.put(PanelSection.CONCEPT, new CollapsePanelPrefs());
        prefMap.put(PanelSection.DESC, new CollapsePanelPrefs());
        prefMap.put(PanelSection.REL, new CollapsePanelPrefs());
        prefMap.put(PanelSection.REL_GRP, new CollapsePanelPrefs());
        prefMap.put(PanelSection.EXTRAS, new CollapsePanelPrefs());


    }

    public class LayoutConceptWorker extends SwingWorker<Map<SpecBI, Integer>, Boolean> {

        private Set<Integer> saps;
        private Set<PositionBI> positions;
        private Set<PathBI> paths;
        private List<? extends I_RelTuple> rels;
        private ConceptVersionBI cv;
        private Collection<? extends RefexVersionBI<?>> memberRefsets;
        private ViewCoordinate coordinate;
        private Collection<? extends RelGroupVersionBI> relGroups;
        private List<? extends I_DescriptionTuple> descriptions;
        private List<? extends I_DescriptionTuple> inactiveDescriptions;
        private I_GetConceptData layoutConcept;

        public LayoutConceptWorker() {
            super();
        }

        @Override
        protected Map<SpecBI, Integer> doInBackground() throws Exception {
            //TODO move all layout to background thread, and return a complete panel...
            positionPanelMap.clear();
            positionOrderedSet.clear();
            seperatorComponents.clear();
            pathRowMap.clear();
            layoutConcept = concept;
            if (layoutConcept != null) {
                coordinate = new ViewCoordinate(config.getViewCoordinate());
                coordinate.setRelAssertionType(RelAssertionType.STATED);
                saps = layoutConcept.getAllSapNids();
                positions = Ts.get().getPositionSet(saps);
                paths = Ts.get().getPathSetFromPositionSet(positions);
                positionOrderedSet.addAll(positions);
                if (settings.getRelAssertionType() != RelAssertionType.INFERRED) {
                    rels = layoutConcept.getSourceRelTuples(config.getAllowedStatus(),
                            null, config.getViewPositionSetReadOnly(),
                            config.getPrecedence(), config.getConflictResolutionStrategy(),
                            coordinate.getClassifierNid(), coordinate.getRelAssertionType());
                    if (settings.getRelAssertionType() == RelAssertionType.INFERRED_THEN_STATED) {
                        coordinate.setRelAssertionType(RelAssertionType.INFERRED);
                        List infRels = layoutConcept.getSourceRelTuples(config.getAllowedStatus(),
                                null, config.getViewPositionSetReadOnly(),
                                config.getPrecedence(), config.getConflictResolutionStrategy(),
                                coordinate.getClassifierNid(), coordinate.getRelAssertionType());
                        rels.addAll(infRels);
                    }

                } else {
                    coordinate.setRelAssertionType(RelAssertionType.INFERRED);
                    rels = layoutConcept.getSourceRelTuples(config.getAllowedStatus(),
                            null, config.getViewPositionSetReadOnly(),
                            config.getPrecedence(), config.getConflictResolutionStrategy(),
                            coordinate.getClassifierNid(), coordinate.getRelAssertionType());
                }
                cv = Ts.get().getConceptVersion(
                        config.getViewCoordinate(), layoutConcept.getNid());
                //get refsets
                memberRefsets = cv.getCurrentRefsetMembers();
                relGroups = Ts.get().getConceptVersion(coordinate, layoutConcept.getNid()).getRelGroups();
                descriptions = layoutConcept.getDescriptionTuples(config.getAllowedStatus(),
                        null, config.getViewPositionSetReadOnly(),
                        config.getPrecedence(), config.getConflictResolutionStrategy());
                List<? extends I_DescriptionTuple> tempDescList = layoutConcept.getDescriptionTuples(null,
                        null, config.getViewPositionSetReadOnly(),
                        config.getPrecedence(), config.getConflictResolutionStrategy());
                HashSet<I_DescriptionTuple> descSet =
                        new HashSet<I_DescriptionTuple>(tempDescList);
                tempDescList.removeAll(descriptions);
                inactiveDescriptions = new ArrayList<I_DescriptionTuple>(tempDescList);

            }
            return kb.setConcept(layoutConcept);
        }

        @Override
        protected void done() {
            try {
                Map<SpecBI, Integer> templates = get();
                removeAll();
                setLayout(new GridBagLayout());
                if (layoutConcept != null) {
                    try {
                        GridBagConstraints gbc = new GridBagConstraints();
                        gbc.weightx = 1;
                        gbc.weighty = 0;
                        gbc.anchor = GridBagConstraints.NORTHWEST;
                        gbc.fill = GridBagConstraints.BOTH;
                        gbc.gridheight = 1;
                        gbc.gridwidth = 1;
                        gbc.gridx = 1;
                        gbc.gridy = 0;
                        setupHistoryPane();
                        gbc.anchor = GridBagConstraints.NORTHWEST;

                        CollapsePanel cpe = new CollapsePanel("concept:", settings,
                                prefMap.get(PanelSection.CONCEPT), PanelSection.CONCEPT);
                        cpe.addPanelsChangedActionListener(pcal);
                        add(cpe, gbc);
                        gbc.gridy++;
                        I_TermFactory tf = Terms.get();
                        ConAttrAnalogBI cav =
                                (ConAttrAnalogBI) cv.getConAttrsActive();
                        DragPanelConceptAttributes cac = getConAttrComponent(
                                (ConAttrAnalogBI) cav, cpe);
                        seperatorComponents.add(cac);

                        cpe.addToggleComponent(cac);
                        cpe.setAlertCount(0);
                        if (cav == null || cav.getCurrentRefexes(coordinate) == null) {
                            cpe.setRefexCount(0);
                        } else {
                            cpe.setRefexCount(cav.getCurrentRefexes(coordinate).size());
                        }
                        cpe.setHistoryCount(cac.getHistorySubpanelCount());
                        cpe.setTemplateCount(0);
                        add(cac, gbc);
                        gbc.gridy++;


                        if (memberRefsets != null) {
                            for (RefexVersionBI<?> extn : memberRefsets) {
                                int refsetNid = extn.getCollectionNid();
                                List<? extends I_ExtendByRefPart> currentRefsets =
                                        tf.getRefsetHelper(config).
                                        getAllCurrentRefsetExtensions(refsetNid, layoutConcept.getConceptNid());
                                for (I_ExtendByRefPart cr : currentRefsets) {
                                    DragPanelExtension ce =
                                            new DragPanelExtension(settings, cpe, extn);
                                    seperatorComponents.add(ce);
                                    cpe.addToggleComponent(ce);
                                    add(ce, gbc);
                                    cpe.getRefexPanels().add(ce);
                                    gbc.gridy++;
                                    cpe.setAlertCount(cpe.alertCount += ce.getAlertSubpanelCount());
                                    cpe.setRefexCount(cpe.refexCount += ce.getRefexSubpanelCount());
                                    cpe.setHistoryCount(cpe.historyCount += ce.getHistorySubpanelCount());
                                    cpe.setTemplateCount(cpe.templateCount += ce.getTemplateSubpanelCount());
                                }
                            }
                        }

                        CollapsePanel cpd = new CollapsePanel("descriptions:", settings,
                                prefMap.get(PanelSection.DESC), PanelSection.DESC);
                        cpd.addPanelsChangedActionListener(pcal);
                        cpd.setAlertCount(0);
                        cpd.setRefexCount(0);
                        cpd.setHistoryCount(1);
                        cpd.setTemplateCount(0);
                        add(cpd, gbc);
                        gbc.gridy++;
                        for (I_DescriptionTuple desc : descriptions) {
                            DragPanelDescription dc = getDescComponent(desc, cpd);
                            seperatorComponents.add(dc);

                            cpd.addToggleComponent(dc);
                            add(dc, gbc);
                            gbc.gridy++;
                            cpd.setAlertCount(cpd.alertCount += dc.getAlertSubpanelCount());
                            cpd.setRefexCount(cpd.refexCount += dc.getRefexSubpanelCount());
                            cpd.setHistoryCount(cpd.historyCount += dc.getHistorySubpanelCount());
                            cpd.setTemplateCount(cpd.templateCount += dc.getTemplateSubpanelCount());
                        }

                        boolean historyIsShown = cpd.isShown(ComponentVersionDragPanel.SubPanelTypes.HISTORY);
                        for (I_DescriptionTuple desc : inactiveDescriptions) {
                            DragPanelDescription dc = getDescComponent(desc, cpd);
                            seperatorComponents.add(dc);
                            dc.setVisible(historyIsShown);
                            cpd.addToggleComponent(dc);
                            cpd.getRetiredPanels().add(dc);
                            add(dc, gbc);
                            gbc.gridy++;
                            cpd.setAlertCount(cpd.alertCount += dc.getAlertSubpanelCount());
                            cpd.setRefexCount(cpd.refexCount += dc.getRefexSubpanelCount());
                            cpd.setHistoryCount(cpd.historyCount += dc.getHistorySubpanelCount());
                            cpd.setTemplateCount(cpd.templateCount += dc.getTemplateSubpanelCount());
                        }


                        CollapsePanel cpr = new CollapsePanel("relationships:", settings,
                                prefMap.get(PanelSection.REL), PanelSection.REL);
                        cpr.addPanelsChangedActionListener(pcal);
                        cpr.setAlertCount(0);
                        cpr.setRefexCount(0);
                        cpr.setHistoryCount(0);
                        cpr.setTemplateCount(0);
                        boolean cprAdded = false;
                        for (I_RelTuple r : rels) {
                            if (r.getGroup() == 0) {
                                if (!cprAdded) {
                                    add(cpr, gbc);
                                    gbc.gridy++;
                                    cprAdded = true;
                                }
                                DragPanelRel rc = getRelComponent(r, cpr);
                                seperatorComponents.add(rc);

                                cpr.addToggleComponent(rc);
                                add(rc, gbc);
                                gbc.gridy++;
                                cpr.setAlertCount(cpr.alertCount += rc.getAlertSubpanelCount());
                                cpr.setRefexCount(cpr.refexCount += rc.getRefexSubpanelCount());
                                cpr.setHistoryCount(cpr.historyCount += rc.getHistorySubpanelCount());
                                cpr.setTemplateCount(cpr.templateCount += rc.getTemplateSubpanelCount());
                            }
                        }

                        try {
                            for (RelGroupVersionBI r : relGroups) {
                                Collection<? extends RelationshipVersionBI> currentRels =
                                        r.getCurrentRels(); //TODO getCurrentRels
                                if (!currentRels.isEmpty()) {
                                    if (!cprAdded) {
                                        add(cpr, gbc);
                                        gbc.gridy++;
                                        cprAdded = true;
                                    }
                                    DragPanelRelGroup rgc = getRelGroupComponent(r, cpr);
                                    seperatorComponents.add(rgc);
                                    cpr.addToggleComponent(rgc);
                                    add(rgc, gbc);
                                    gbc.gridy++;
                                    cpr.setAlertCount(cpr.alertCount += rgc.getAlertSubpanelCount());
                                    cpr.setRefexCount(cpr.refexCount += rgc.getRefexSubpanelCount());
                                    cpr.setHistoryCount(cpr.historyCount += rgc.getHistorySubpanelCount());
                                    cpr.setTemplateCount(cpr.templateCount += rgc.getTemplateSubpanelCount());
                                }
                            }
                        } catch (ContraditionException e) {
                            AceLog.getAppLog().alertAndLogException(e);
                        }

                        if (templates.size() > 0) {
                            CollapsePanel cptemplate =
                                    new CollapsePanel("aggregate extras", settings,
                                    prefMap.get(PanelSection.EXTRAS), PanelSection.EXTRAS);
                            cptemplate.addPanelsChangedActionListener(pcal);
                            cptemplate.setTemplateCount(templates.size());
                            cptemplate.setRefexCount(0);
                            cptemplate.setHistoryCount(0);
                            cptemplate.setAlertCount(0);
                            add(cptemplate, gbc);
                            gbc.gridy++;
                            for (Entry<SpecBI, Integer> entry : templates.entrySet()) {
                                Class<?> entryClass = entry.getKey().getClass();
                                if (RelSpec.class.isAssignableFrom(entryClass)) {
                                    RelSpec spec = (RelSpec) entry.getKey();
                                    DragPanelRelTemplate template = getRelTemplate(spec);
                                    cptemplate.addToggleComponent(template);
                                    add(template, gbc);
                                    cptemplate.getTemplatePanels().add(template);
                                    gbc.gridy++;
                                    cptemplate.setTemplateCount(cptemplate.templateCount++);
                                } else if (DescriptionSpec.class.isAssignableFrom(entryClass)) {
                                    DescriptionSpec spec = (DescriptionSpec) entry.getKey();
                                    DragPanelDescTemplate template = getDescTemplate(spec);
                                    cptemplate.addToggleComponent(template);
                                    add(template, gbc);
                                    cptemplate.getTemplatePanels().add(template);
                                    gbc.gridy++;
                                    cptemplate.setTemplateCount(cptemplate.templateCount++);
                                }
                            }
                        }
                        gbc.weighty = 1;
                        add(new JPanel(), gbc);
                    } catch (IOException e) {
                        AceLog.getAppLog().alertAndLogException(e);
                    } catch (TerminologyException e) {
                        AceLog.getAppLog().alertAndLogException(e);
                    }
                }
            } catch (Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
            GuiUtil.tickle(ConceptView.this);
            if (settings.getNavigator() != null
                    && settings.getNavigatorButton().isSelected()) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        settings.getNavigator().updateHistoryPanel();
                    }
                });
            }
        }

        public class RefreshHistoryViewListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent ae) {
                settings.getNavigator().updateHistoryPanel();
            }
        }

        private void setupHistoryPane() throws IOException, ContraditionException {
            int row = 0;
            getHistoryPanel().removeAll();
            getHistoryPanel().setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.weightx = 1;
            gbc.weighty = 0;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridheight = 1;
            gbc.gridwidth = 1;
            gbc.gridx = 1;
            gbc.gridy = 0;
            for (PathBI path : paths) {
                pathRowMap.put(path, row);
                ConceptVersionBI pathVersion =
                        Ts.get().getConceptVersion(coordinate, path.getConceptNid());
                JCheckBox pathCheck = getJCheckBox();
                pathCheck.addActionListener(new RefreshHistoryViewListener());

                if (settings.getNavigator().getDropSide()
                        == ConceptViewSettings.SIDE.LEFT) {
                    gbc.anchor = GridBagConstraints.NORTHWEST;
                    pathCheck.setHorizontalTextPosition(SwingConstants.RIGHT);
                    pathCheck.setHorizontalAlignment(SwingConstants.LEFT);
                } else {
                    gbc.anchor = GridBagConstraints.NORTHEAST;
                    pathCheck.setHorizontalTextPosition(SwingConstants.LEFT);
                    pathCheck.setHorizontalAlignment(SwingConstants.RIGHT);
                }
                if (pathVersion.getPreferredDescription() != null) {
                    pathCheck.setText(pathVersion.getPreferredDescription().getText());
                } else {
                    pathCheck.setText(pathVersion.toString());
                }
                pathCheck.setSelected(true);
                rowToPathCheckMap.put(row, pathCheck);
                pathCheck.setVisible(ConceptView.this.historyShown);
                getHistoryPanel().add(pathCheck, gbc);
                gbc.gridy++;
                row++;
            }
        }
    }
    private Object lastThingBeingDropped;

    public void setupDrop(Object thingBeingDropped) {

        if (thingBeingDropped != null) {
            if (lastThingBeingDropped == null
                    || thingBeingDropped.equals(lastThingBeingDropped) == false) {
                lastThingBeingDropped = thingBeingDropped;
                actionList.clear();
                actionList.addAll(getKbActions(thingBeingDropped));
                dropComponents.clear();
                if (actionList.size() > -1) {
                    for (Action a : actionList) {
                        try {
                            dropComponents.add(new DropActionPanel(a));
                        } catch (TooManyListenersException e) {
                            AceLog.getAppLog().alertAndLogException(e);
                        }
                    }
                }
            }
        } else {
            System.out.println("Changing to null");
            actionList.clear();
            dropComponents.clear();


        }

    }

    public class DropPanelActionManager implements ActionListener, I_DispatchDragStatus {

        private Timer timer;
        private boolean dragging = false;
        private JComponent dropPanel = new JLabel("dropPanel");
        private boolean panelAdded = false;
        private JScrollPane sfpScroller;
        private boolean gridLayout = true;
        private JPanel sfp;
        private Collection<JComponent> addedDropComponents = new ArrayList<JComponent>();

        public DropPanelActionManager() {
            super();
            new DropPanelProxy(this);
            timer = new Timer(50, this);
        }
        /* (non-Javadoc)
         * @see org.ihtsdo.arena.conceptview.I_DispatchDragStatus#dragStarted()
         */

        @Override
        public void dragStarted() {

            LayoutManager layout = new FlowLayout(FlowLayout.LEADING, 5, 5);
            sfp = new ScrollablePanel(layout);
            if (gridLayout) {
                layout = new GridLayout(0, 1, 5, 5);
                sfp = new JPanel(layout);
            }

            sfp.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            dropPanel = new JPanel(new GridLayout(1, 1));
            sfpScroller = new JScrollPane(sfp);
            sfpScroller.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            sfpScroller.setAutoscrolls(true);
            sfpScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            sfpScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            dropPanel.add(sfpScroller);
            dragging = true;
            timer.start();
        }
        /* (non-Javadoc)
         * @see org.ihtsdo.arena.conceptview.I_DispatchDragStatus#dragFinished()
         */

        @Override
        public void dragFinished() {
            timer.stop();
            dragging = false;
            setDragPanelVisible(false);
            dropPanel = null;
            addedDropComponents.clear();
            actionList.clear();
            dropComponents.clear();
            lastThingBeingDropped = null;
        }
        /* (non-Javadoc)
         * @see org.ihtsdo.arena.conceptview.I_DispatchDragStatus#actionPerformed(java.awt.event.ActionEvent)
         */

        @Override
        public void actionPerformed(ActionEvent e) {
            if (dragging) {
                if (addedDropComponents.equals(dropComponents) == false) {
                    sfp.removeAll();
                    System.out.println("Concept changing drop components.");
                    addedDropComponents = new ArrayList<JComponent>();
                    for (JComponent c : dropComponents) {
                        addedDropComponents.add(c);
                        sfp.add(c);
                    }
                }
                if (addedDropComponents.isEmpty()) {
                    return;
                }
                Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
                Point mouseLocationForConceptView = mouseLocation.getLocation();
                SwingUtilities.convertPointFromScreen(mouseLocationForConceptView,
                        ConceptView.this.getParent());
                if (ConceptView.this.getParent().contains(mouseLocationForConceptView)) {
                    setDragPanelVisible(true);
                } else {
                    Point mouseLocationForDropPanel = mouseLocation.getLocation();
                    SwingUtilities.convertPointFromScreen(mouseLocationForDropPanel, dropPanel);
                    if (mouseLocationForDropPanel.y <= 0) {
                        mouseLocationForDropPanel.y = mouseLocationForDropPanel.y + 10;
                    } else if (mouseLocationForDropPanel.y >= dropPanel.getHeight()) {
                        mouseLocationForDropPanel.y = mouseLocationForDropPanel.y - 10;
                    }
                    if (dropPanel.contains(mouseLocationForDropPanel) && panelAdded) {
                        if (mouseLocationForDropPanel.y < 10) {
                            BoundedRangeModel scrollerModel = sfpScroller.getVerticalScrollBar().getModel();
                            scrollerModel.setExtent(1);
                            if (scrollerModel.getValue() < scrollerModel.getMaximum()) {
                                scrollerModel.setValue(scrollerModel.getValue() - 5);
                            }
                        } else if (dropPanel.getHeight() - mouseLocationForDropPanel.y < 10) {
                            BoundedRangeModel scrollerModel = sfpScroller.getVerticalScrollBar().getModel();
                            scrollerModel.setExtent(1);
                            if (scrollerModel.getValue() < scrollerModel.getMaximum()) {
                                scrollerModel.setValue(scrollerModel.getValue() + 5);
                            }
                        }
                    } else {
                        setDragPanelVisible(false);
                    }

                }
            } else {
                setDragPanelVisible(false);
            }
        }

        private void setDragPanelVisible(boolean visible) {
            if (visible) {
                if (ConceptView.this.isVisible()) {
                    if (!panelAdded) {
                        panelAdded = true;
                        Point loc = ConceptView.this.getParent().getLocation();
                        JLayeredPane rootLayers = ConceptView.this.getRootPane().getLayeredPane();
                        rootLayers.add(dropPanel, JLayeredPane.PALETTE_LAYER);
                        loc = SwingUtilities.convertPoint(ConceptView.this.getParent(), loc, rootLayers);
                        dropPanel.setSize(sfp.getPreferredSize().width + 4, ConceptView.this.getParent().getHeight());
                        dropPanel.setLocation(loc.x - dropPanel.getWidth(), loc.y);
                        dropPanel.setVisible(true);
                        dropPanel.setBorder(BorderFactory.createLineBorder(Color.gray));
                    }
                }
            } else {
                if (panelAdded) {
                    panelAdded = false;
                    JLayeredPane rootLayers = ConceptView.this.getRootPane().getLayeredPane();
                    dropPanel.setVisible(false);
                    rootLayers.remove(dropPanel);
                }
            }
        }
    }

    private class UpdateTextTemplateDocumentListener
            implements DocumentListener, ActionListener {

        FixedWidthJEditorPane editorPane;
        DescriptionSpec desc;
        Timer t;
        I_GetConceptData c;
        boolean update = false;

        public UpdateTextTemplateDocumentListener(FixedWidthJEditorPane editorPane,
                DescriptionSpec desc) throws TerminologyException, IOException {
            super();
            this.editorPane = editorPane;
            this.desc = desc;
            t = new Timer(1000, this);
            t.start();
            c = Terms.get().getConcept(desc.getConceptSpec().get(config.getViewCoordinate()).getNid());
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            update = true;
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update = true;
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update = true;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (update) {
                update = false;
                desc.setDescText(editorPane.extractText());
            }
        }
    }
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private I_ConfigAceFrame config;
    private ConceptViewSettings settings;
    private EditPanelKb kb;
    private I_DispatchDragStatus dropPanelMgr = new DropPanelActionManager();
    private Collection<Action> actionList =
            Collections.synchronizedCollection(new ArrayList<Action>());
    private I_GetConceptData concept;

    public enum PanelSection {

        CONCEPT,
        DESC, REL, REL_GRP, EXTRAS
    };
    private Map<PanelSection, CollapsePanelPrefs> prefMap =
            new EnumMap<PanelSection, CollapsePanelPrefs>(PanelSection.class);
    private TreeSet<PositionBI> positionOrderedSet = new TreeSet(new PositionComparator());
    private Map<PathBI, Integer> pathRowMap = new ConcurrentHashMap<PathBI, Integer>();
    private PanelsChangedActionListener pcal = new PanelsChangedActionListener();

    public ActionListener getPanelsChangedActionListener() {
        return pcal;
    }

    public I_GetConceptData getConcept() {
        return concept;
    }

    public void setConcept(I_GetConceptData concept) {
        this.concept = concept;
    }
    private Collection<JComponent> dropComponents =
            Collections.synchronizedList(new ArrayList<JComponent>());
    private boolean historyShown = false;
    private Map<PositionBI, Collection<ComponentVersionDragPanel<?>>> positionPanelMap =
            new ConcurrentHashMap<PositionBI, Collection<ComponentVersionDragPanel<?>>>();
    private Map<Integer, JCheckBox> rowToPathCheckMap = new ConcurrentHashMap<Integer, JCheckBox>();
    private List<JComponent> seperatorComponents = new ArrayList<JComponent>();
    private Set<File> kbFiles = new HashSet<File>();

    public ConceptView(I_ConfigAceFrame config,
            ConceptViewSettings settings, ConceptViewRenderer cvRenderer) {
        super();
        this.config = config;
        this.settings = settings;
        this.cvRenderer = cvRenderer;
        kbFiles.add(new File("drools-rules/ContextualDropActions.drl"));

        kb = new EditPanelKb(config);
        addCommitListener(settings);
        settings.getConfig().addPropertyChangeListener("commit", pcal);
        setupPrefMap();
    }

    public Map<Integer, JCheckBox> getRowToPathCheckMap() {
        return rowToPathCheckMap;
    }

    public Map<PositionBI, Collection<ComponentVersionDragPanel<?>>> getPositionPanelMap() {
        return positionPanelMap;
    }

    private void addToPositionPanelMap(ComponentVersionDragPanel<?> panel) throws IOException {
        ComponentVersionBI cv = panel.getComponentVersion();
        if (cv == null) {
            return;
        }
        PositionBI position = cv.getPosition();
        Collection<ComponentVersionDragPanel<?>> panels =
                positionPanelMap.get(position);
        if (panels == null) {
            panels = new HashSet<ComponentVersionDragPanel<?>>();
            positionPanelMap.put(position, panels);
        }
        panels.add(panel);
    }

    public void addHostListener(PropertyChangeListener l) {
        if (settings != null && settings.getHost() != null) {
            settings.getHost().addPropertyChangeListener("termComponent", l);
        }
    }

    public JPanel getHistoryPanel() {
        return cvRenderer.getHistoryPanel();
    }

    public boolean isHistoryShown() {
        return historyShown;
    }

    public void setHistoryShown(boolean historyShown) {
        if (historyShown != this.historyShown) {
            this.historyShown = historyShown;
            for (JComponent hc : rowToPathCheckMap.values()) {
                hc.setVisible(historyShown);
            }
            if (historyShown) {
                if (settings.getNavigator() != null) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            settings.getNavigator().updateHistoryPanel();
                        }
                    });
                }
            }
        }
    }

    private void addCommitListener(ConceptViewSettings settings) {
        settings.getConfig().addPropertyChangeListener("commit",
                new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        layoutConcept(ConceptView.this.concept);
                    }
                });
    }

    public void layoutConcept(I_GetConceptData concept) {
        removeAll();
        this.concept = concept;
        (new LayoutConceptWorker()).execute();
    }

    public DragPanelRelGroup getRelGroupComponent(RelGroupVersionBI group,
            CollapsePanel parentCollapsePanel)
            throws TerminologyException, IOException, ContraditionException {
        DragPanelRelGroup relGroupPanel =
                new DragPanelRelGroup(new GridBagLayout(), settings,
                parentCollapsePanel, group);
        relGroupPanel.setupDrag(group);
        relGroupPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        JLabel relGroupLabel = getJLabel(" ");
        relGroupLabel.setBackground(Color.GREEN);
        relGroupLabel.setOpaque(true);
        relGroupPanel.setDropPopupInset(relGroupLabel.getPreferredSize().width);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = group.getRels().size() + 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        relGroupPanel.add(relGroupLabel, gbc);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.gridheight = 1;
        CollapsePanel cprg = new CollapsePanel("group:", settings,
                new CollapsePanelPrefs(prefMap.get(PanelSection.REL_GRP)),
                PanelSection.REL_GRP, relGroupPanel.getActionMenuButton(getKbActions(group)));
        cprg.setAlertCount(0);
        cprg.setRefexCount(0);
        cprg.setHistoryCount(0);
        cprg.setTemplateCount(0);
        relGroupPanel.add(cprg, gbc);
        gbc.gridy++;
        for (RelationshipVersionBI r : group.getCurrentRels()) { //TODO getCurrentRels
            DragPanelRel dpr = getRelComponent(r, parentCollapsePanel);
            cprg.addToggleComponent(dpr);
            dpr.setInGroup(true);
            relGroupPanel.add(dpr, gbc);
            gbc.gridy++;
            cprg.setAlertCount(cprg.alertCount += dpr.getAlertSubpanelCount());
            cprg.setRefexCount(cprg.refexCount += dpr.getRefexSubpanelCount());
            cprg.setHistoryCount(cprg.historyCount += dpr.getHistorySubpanelCount());
            cprg.setTemplateCount(cprg.templateCount += dpr.getTemplateSubpanelCount());
        }

        return relGroupPanel;

    }

    public DragPanelConceptAttributes getConAttrComponent(ConAttrAnalogBI conAttr,
            CollapsePanel parentCollapsePanel)
            throws TerminologyException, IOException {
        DragPanelConceptAttributes dragConAttrPanel =
                new DragPanelConceptAttributes(new GridBagLayout(), settings,
                parentCollapsePanel, conAttr);
        addToPositionPanelMap(dragConAttrPanel);
        return dragConAttrPanel;
    }

    public DragPanelDescription getDescComponent(DescriptionAnalogBI desc,
            CollapsePanel parentCollapsePanel)
            throws TerminologyException, IOException {
        DragPanelDescription dragDescPanel =
                new DragPanelDescription(new GridBagLayout(), settings,
                parentCollapsePanel, desc);
        addToPositionPanelMap(dragDescPanel);
        return dragDescPanel;
    }

    public DragPanelDescTemplate getDescTemplate(final DescriptionSpec desc)
            throws TerminologyException, IOException {
        DragPanelDescTemplate descPanel =
                new DragPanelDescTemplate(new GridBagLayout(), settings, desc);
        descPanel.setupDrag(desc);
        descPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        JLabel descLabel = getJLabel("T");
        descLabel.setBackground(Color.ORANGE);
        descLabel.setOpaque(true);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        descPanel.add(descLabel, gbc);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx++;
        TermComponentLabel typeLabel =
                getLabel(desc.getDescTypeSpec().get(config.getViewCoordinate()).getNid(), true);
        descPanel.add(typeLabel, gbc);
        typeLabel.addPropertyChangeListener("termComponent",
                new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        try {
                            desc.setDescTypeSpec(SpecFactory.get(
                                    (I_GetConceptData) evt.getNewValue(),
                                    config.getViewCoordinate()));
                        } catch (IOException ex) {
                            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE,
                                    null, ex);
                        }
                    }
                });

        gbc.gridx++;
        descPanel.add(new JSeparator(SwingConstants.VERTICAL), gbc);
        gbc.weightx = 1;
        gbc.gridx++;
        FixedWidthJEditorPane textPane = new FixedWidthJEditorPane();
        textPane.setEditable(true);
        textPane.setOpaque(false);

        textPane.setFont(textPane.getFont().deriveFont(settings.getFontSize()));
        textPane.setText(desc.getDescText());
        descPanel.add(textPane, gbc);
        textPane.getDocument().addDocumentListener(
                new UpdateTextTemplateDocumentListener(textPane, desc));
        return descPanel;
    }

    public DragPanelRelTemplate getRelTemplate(final RelSpec spec)
            throws TerminologyException, IOException {
        ViewCoordinate coordinate = config.getViewCoordinate();
        DragPanelRelTemplate relPanel =
                new DragPanelRelTemplate(new GridBagLayout(), settings, spec);
        relPanel.setupDrag(spec);
        relPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        JLabel relLabel = getJLabel("T");
        relLabel.setBackground(Color.YELLOW);
        relLabel.setOpaque(true);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        relPanel.add(relLabel, gbc);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx++;
        TermComponentLabel typeLabel = getLabel(
                spec.getRelTypeSpec().get(coordinate).getNid(), true);
        relPanel.add(typeLabel, gbc);
        typeLabel.addPropertyChangeListener("termComponent",
                new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        try {
                            spec.setRelTypeSpec(SpecFactory.get((I_GetConceptData) evt.getNewValue(),
                                    config.getViewCoordinate()));
                        } catch (IOException ex) {
                            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
        gbc.gridx++;
        relPanel.add(new JSeparator(SwingConstants.VERTICAL), gbc);
        gbc.weightx = 1;
        gbc.gridx++;
        TermComponentLabel destLabel = getLabel(
                spec.getDestinationSpec().get(coordinate).getNid(), true);
        relPanel.add(destLabel, gbc);
        destLabel.addPropertyChangeListener("termComponent",
                new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        try {
                            spec.setDestinationSpec(SpecFactory.get(
                                    (I_GetConceptData) evt.getNewValue(),
                                    config.getViewCoordinate()));
                        } catch (IOException ex) {
                            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
        return relPanel;
    }

    public DragPanelRel getRelComponent(RelationshipVersionBI r,
            CollapsePanel parentCollapsePanel)
            throws TerminologyException, IOException {
        DragPanelRel relPanel = new DragPanelRel(new GridBagLayout(), settings,
                parentCollapsePanel, r);
        addToPositionPanelMap(relPanel);


        return relPanel;
    }

    public JLabel getJLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(settings.getFontSize()));
        l.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
        return l;
    }

    JCheckBox getJCheckBox() {
        JCheckBox check = new JCheckBox();
        check.setFont(check.getFont().deriveFont(settings.getFontSize()));
        check.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
        return check;
    }

    private TermComponentLabel getLabel(int nid, boolean canDrop)
            throws TerminologyException, IOException {
        TermComponentLabel termLabel = new TermComponentLabel();
        termLabel.setLineWrapEnabled(true);
        termLabel.getDropTarget().setActive(canDrop);
        termLabel.setFixedWidth(100);
        termLabel.setFont(termLabel.getFont().deriveFont(settings.getFontSize()));
        termLabel.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
        termLabel.setTermComponent(Terms.get().getConcept(nid));
        return termLabel;
    }

    private Collection<Action> getKbActions(Object thingToDrop) {
        ArrayList<Action> actions = new ArrayList<Action>();
        try {

            if (I_GetConceptData.class.isAssignableFrom(thingToDrop.getClass())) {
                I_GetConceptData conceptToDrop = (I_GetConceptData) thingToDrop;
                thingToDrop = Ts.get().getConceptVersion(config.getViewCoordinate(),
                        conceptToDrop.getConceptNid());
            }

            if (ComponentVersionBI.class.isAssignableFrom(thingToDrop.getClass())
                    || SpecBI.class.isAssignableFrom(thingToDrop.getClass())) {

                Map<String, Object> globals = new HashMap<String, Object>();
                globals.put("actions", actions);
                globals.put("vc", config.getViewCoordinate());

                Collection<Object> facts = new ArrayList<Object>();
                facts.add(FactFactory.get(
                        Context.DROP_OBJECT, thingToDrop, config.getViewCoordinate()));
                facts.add(FactFactory.get(Context.DROP_TARGET,
                        Ts.get().getConceptVersion(
                        config.getViewCoordinate(), concept.getNid()),
                        config.getViewCoordinate()));

                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().fine("dropTarget: " + concept);
                    AceLog.getAppLog().fine("thingToDrop: " + thingToDrop);
                }


                DroolsExecutionManager.fireAllRules(
                        ConceptView.class.getCanonicalName(),
                        kbFiles,
                        globals,
                        facts,
                        false);

            }
        } catch (Throwable e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return actions;
    }

    public Map<PathBI, Integer> getPathRowMap() {
        return pathRowMap;
    }

    public TreeSet<PositionBI> getPositionOrderedSet() {

        TreeSet positionSet = new TreeSet(new PositionComparator());
        if (positionOrderedSet != null) {
            positionSet.addAll(positionOrderedSet);
        }
        return positionSet;
    }

    static class PositionComparator implements Comparator<PositionBI> {

        public PositionComparator() {
        }

        @Override
        public int compare(PositionBI t, PositionBI t1) {
            if (t.getTime() != t1.getTime()) {
                if (t.getTime() > t1.getTime()) {
                    return 1;
                }
                return -1;
            }
            return t.getPath().getConceptNid() - t1.getPath().getConceptNid();
        }
    }

    public ConceptViewRenderer getCvRenderer() {
        return cvRenderer;
    }

    public I_ConfigAceFrame getConfig() {
        return config;
    }

    public List<JComponent> getSeperatorComponents() {
        return seperatorComponents;
    }
}
