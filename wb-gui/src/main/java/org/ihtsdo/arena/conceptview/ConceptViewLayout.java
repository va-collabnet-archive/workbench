/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.dwfa.ace.ACE;
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
import org.ihtsdo.tk.spec.DescriptionSpec;
import org.ihtsdo.tk.spec.RelSpec;
import org.ihtsdo.arena.conceptview.ConceptView.PanelSection;
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
import org.ihtsdo.tk.spec.SpecBI;
import org.ihtsdo.tk.spec.SpecFactory;
import org.ihtsdo.util.swing.GuiUtil;

/**
 *
 * @author kec
 */
public class ConceptViewLayout extends SwingWorker<Map<SpecBI, Integer>, Object> {

    public boolean stop = false;
    private ConceptView cView;
    private Set<Integer> saps;
    private Set<PositionBI> positions;
    private Set<PathBI> paths;
    private List<? extends I_RelTuple> statedRels;
    private List<? extends I_RelTuple> inactiveStatedRels;
    private List<? extends I_RelTuple> inferredRels;
    private List<? extends I_RelTuple> inactiveInferredRels;
    private ConceptVersionBI cv;
    private Collection<? extends RefexVersionBI<?>> memberRefsets;
    private ViewCoordinate coordinate;
    private Collection<RelGroupVersionBI> statedRelGroups;
    private Collection<RelGroupVersionBI> inferredRelGroups;
    private List<? extends I_DescriptionTuple> descriptions;
    private List<? extends I_DescriptionTuple> inactiveDescriptions;
    private I_GetConceptData layoutConcept;
    private List<DragPanelDescription> inactiveDescriptionPanels;
    private List<DragPanelDescription> activeDescriptionPanels;
    private List<DragPanelRel> inactiveStatedRelPanels;
    private List<DragPanelRel> activeStatedRelPanels;
    private List<DragPanelRel> inactiveInferredRelPanels;
    private List<DragPanelRel> activeInferredRelPanels;
    protected AtomicReference<Collection<DragPanel.DropPanelActionManager>> dpamsRef;
    private CollapsePanel cpd;
    private CollapsePanel cpr;
    private ConceptViewSettings settings;

    public ConceptViewSettings getSettings() {
        return settings;
    }
    private I_ConfigAceFrame config;
    private PanelsChangedActionListener pcal;
    // 
    private Map<PositionBI, Collection<ComponentVersionDragPanel<?>>> positionPanelMap =
            new ConcurrentHashMap<PositionBI, Collection<ComponentVersionDragPanel<?>>>();

    public Map<PositionBI, Collection<ComponentVersionDragPanel<?>>> getPositionPanelMap() {
        return positionPanelMap;
    }
    private Map<PathBI, Integer> pathRowMap = new ConcurrentHashMap<PathBI, Integer>();

    public Map<PathBI, Integer> getPathRowMap() {
        return pathRowMap;
    }
    private TreeSet<PositionBI> positionOrderedSet = new TreeSet(new PositionComparator());
    private Collection<DragPanel> seperatorComponents = new ConcurrentSkipListSet<DragPanel>(new Comparator<DragPanel>() {

        @Override
        public int compare(DragPanel o1, DragPanel o2) {
            return o1.getId() - o2.getId();
        }
    });

    public Collection<DragPanel> getSeperatorComponents() {
        return seperatorComponents;
    }
    private Map<PanelSection, CollapsePanelPrefs> prefMap;
    private JPanel historyPanel = new JPanel(new GridBagLayout());
    private Map<Integer, JCheckBox> rowToPathCheckMap = new ConcurrentHashMap<Integer, JCheckBox>();
    private CountDownLatch latch = new CountDownLatch(6);
    private Lock dramsLock = new ReentrantLock();

    public Collection<DragPanel.DropPanelActionManager> getDropPanelActionManagers() {
        if (dpamsRef == null) {
            dramsLock.lock();
            try {
                if (dpamsRef == null) {
                    AtomicReference<Collection<DragPanel.DropPanelActionManager>> tempRef =
                            new AtomicReference<Collection<DragPanel.DropPanelActionManager>>();
                    tempRef.compareAndSet(null, new ConcurrentSkipListSet<DragPanel.DropPanelActionManager>());
                    dpamsRef = tempRef;
                }
            } finally {
                dramsLock.unlock();
            }
        }
        return dpamsRef.get();
    }

    public Map<Integer, JCheckBox> getRowToPathCheckMap() {
        return rowToPathCheckMap;
    }

    public ConceptViewLayout(ConceptView conceptView,
            I_GetConceptData layoutConcept) {
        super();
        this.cView = conceptView;
        this.layoutConcept = layoutConcept;
        this.settings = conceptView.getSettings();
        this.pcal = new PanelsChangedActionListener(settings);
        this.config = conceptView.getConfig();
        this.prefMap = conceptView.getPrefMap();
    }

    public ActionListener getPanelsChangedActionListener() {
        return pcal;
    }

    @Override
    protected Map<SpecBI, Integer> doInBackground() throws Exception {
        //TODO move all layout to background thread, and return a complete panel...

        if (stop) {
            return null;
        }
        if (layoutConcept != null) {
            cpd = new CollapsePanel("descriptions:", settings,
                    prefMap.get(PanelSection.DESC), PanelSection.DESC);
            cpr = new CollapsePanel("relationships:", settings,
                    prefMap.get(PanelSection.REL), PanelSection.REL);

            if (stop) {
                return null;
            }
            coordinate = new ViewCoordinate(config.getViewCoordinate());
            coordinate.setRelAssertionType(RelAssertionType.STATED);
            saps = layoutConcept.getAllSapNids();
            positions = Ts.get().getPositionSet(saps);
            paths = Ts.get().getPathSetFromPositionSet(positions);
            positionOrderedSet.addAll(positions);
            if (stop) {
                return null;
            }

            coordinate.setRelAssertionType(RelAssertionType.STATED);
            statedRels = layoutConcept.getSourceRelTuples(config.getAllowedStatus(),
                    null, config.getViewPositionSetReadOnly(),
                    config.getPrecedence(), config.getConflictResolutionStrategy(),
                    coordinate.getClassifierNid(), coordinate.getRelAssertionType());
            if (stop) {
                return null;
            }
            activeStatedRelPanels = new ArrayList<DragPanelRel>(statedRels.size());
            setupRels(latch, statedRels,
                    activeStatedRelPanels,
                    cpr, false);
            if (stop) {
                return null;
            }
            inactiveStatedRels = layoutConcept.getSourceRelTuples(null,
                    null, config.getViewPositionSetReadOnly(),
                    config.getPrecedence(), config.getConflictResolutionStrategy(),
                    coordinate.getClassifierNid(), coordinate.getRelAssertionType());
            if (stop) {
                return null;
            }
            inactiveStatedRels.removeAll(statedRels);
            inactiveStatedRelPanels = new ArrayList<DragPanelRel>(inactiveStatedRels.size());
            if (stop) {
                return null;
            }
            setupRels(latch, inactiveStatedRels,
                    inactiveStatedRelPanels,
                    cpr, false);
            statedRelGroups = (Collection<RelGroupVersionBI>) Ts.get().getConceptVersion(coordinate, layoutConcept.getNid()).getRelGroups();
            if (stop) {
                return null;
            }

            coordinate.setRelAssertionType(RelAssertionType.INFERRED);
            inferredRels = layoutConcept.getSourceRelTuples(config.getAllowedStatus(),
                    null, config.getViewPositionSetReadOnly(),
                    config.getPrecedence(), config.getConflictResolutionStrategy(),
                    coordinate.getClassifierNid(), coordinate.getRelAssertionType());
            activeInferredRelPanels = new ArrayList<DragPanelRel>(inferredRels.size());
            if (stop) {
                return null;
            }
            setupRels(latch, inferredRels,
                    activeInferredRelPanels,
                    cpr, true);
            inactiveInferredRels = layoutConcept.getSourceRelTuples(null,
                    null, config.getViewPositionSetReadOnly(),
                    config.getPrecedence(), config.getConflictResolutionStrategy(),
                    coordinate.getClassifierNid(), coordinate.getRelAssertionType());
            if (stop) {
                return null;
            }
            inactiveInferredRels.removeAll(inferredRels);
            inactiveInferredRelPanels = new ArrayList<DragPanelRel>(inactiveInferredRels.size());
            setupRels(latch, inactiveInferredRels,
                    inactiveInferredRelPanels,
                    cpr, true);
            if (stop) {
                return null;
            }
            inferredRelGroups = (Collection<RelGroupVersionBI>) Ts.get().getConceptVersion(coordinate, layoutConcept.getNid()).getRelGroups();


            cv = Ts.get().getConceptVersion(
                    config.getViewCoordinate(), layoutConcept.getNid());
            //get refsets
            if (stop) {
                return null;
            }
            memberRefsets = cv.getCurrentRefsetMembers();



            // Get active descriptions
            descriptions = layoutConcept.getDescriptionTuples(config.getAllowedStatus(),
                    null, config.getViewPositionSetReadOnly(),
                    config.getPrecedence(), config.getConflictResolutionStrategy());
            activeDescriptionPanels = new ArrayList<DragPanelDescription>(descriptions.size());
            if (stop) {
                return null;
            }
            setupDescriptions(latch, descriptions, activeDescriptionPanels, cpd);

            // get all descriptions
            List<? extends I_DescriptionTuple> tempDescList = layoutConcept.getDescriptionTuples(null,
                    null, config.getViewPositionSetReadOnly(),
                    config.getPrecedence(), config.getConflictResolutionStrategy());

            if (stop) {
                return null;
            }
            tempDescList.removeAll(descriptions);

            inactiveDescriptions = new ArrayList<I_DescriptionTuple>(tempDescList);
            inactiveDescriptionPanels = new ArrayList<DragPanelDescription>(inactiveDescriptions.size());
            setupDescriptions(latch, inactiveDescriptions, inactiveDescriptionPanels, cpd);
            if (stop) {
                return null;
            }

            latch.await();


        }
        if (stop) {
            return null;
        }
        return cView.getKb().setConcept(layoutConcept);
    }

    @Override
    protected void done() {
        if (stop) {
            return;
        }
        try {
            if (stop) {
                return;
            }
            Map<SpecBI, Integer> templates = get();
            cView.removeAll();
            cView.setLayout(new GridBagLayout());
            if (layoutConcept != null) {
                if (stop) {
                    return;
                }
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
                    cView.add(historyPanel, gbc);
                    gbc.gridy++;
                    gbc.anchor = GridBagConstraints.NORTHWEST;
                    if (stop) {
                        return;
                    }

                    CollapsePanel cpe = new CollapsePanel("concept:", settings,
                            prefMap.get(PanelSection.CONCEPT), PanelSection.CONCEPT);
                    cpe.addPanelsChangedActionListener(pcal);
                    cView.add(cpe, gbc);
                    gbc.gridy++;
                    I_TermFactory tf = Terms.get();
                    ConAttrAnalogBI cav =
                            (ConAttrAnalogBI) cv.getConAttrsActive();
                    if (cav == null) {
                        cav = (ConAttrAnalogBI) cv.getConAttrs().getVersion(coordinate.getVcWithAllStatusValues());
                    }
                    if (stop) {
                        return;
                    }
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
                    cView.add(cac, gbc);
                    gbc.gridy++;
                    if (stop) {
                        return;
                    }


                    if (memberRefsets != null) {
                        for (RefexVersionBI<?> extn : memberRefsets) {
                            if (stop) {
                                return;
                            }
                            int refsetNid = extn.getCollectionNid();
                            List<? extends I_ExtendByRefPart> currentRefsets =
                                    tf.getRefsetHelper(config).
                                    getAllCurrentRefsetExtensions(refsetNid, layoutConcept.getConceptNid());
                            for (I_ExtendByRefPart cr : currentRefsets) {
                                DragPanelExtension ce =
                                        new DragPanelExtension(this, cpe, extn);
                                seperatorComponents.add(ce);
                                cpe.addToggleComponent(ce);
                                cView.add(ce, gbc);
                                cpe.getRefexPanels().add(ce);
                                gbc.gridy++;
                                cpe.setAlertCount(cpe.alertCount += ce.getAlertSubpanelCount());
                                cpe.setRefexCount(cpe.refexCount += ce.getRefexSubpanelCount());
                                cpe.setHistoryCount(cpe.historyCount += ce.getHistorySubpanelCount());
                                cpe.setTemplateCount(cpe.templateCount += ce.getTemplateSubpanelCount());
                            }
                        }
                    }

                    cpd.addPanelsChangedActionListener(pcal);
                    cpd.setAlertCount(0);
                    cpd.setRefexCount(0);
                    cpd.setHistoryCount(inactiveDescriptions.size());
                    cpd.setTemplateCount(0);
                    cView.add(cpd, gbc);
                    gbc.gridy++;
                    for (DragPanelDescription dc : activeDescriptionPanels) {
                        if (stop) {
                            return;
                        }
                        seperatorComponents.add(dc);
                        cpd.addToggleComponent(dc);
                        cView.add(dc, gbc);
                        gbc.gridy++;
                        cpd.setAlertCount(cpd.alertCount += dc.getAlertSubpanelCount());
                        cpd.setRefexCount(cpd.refexCount += dc.getRefexSubpanelCount());
                        cpd.setHistoryCount(cpd.historyCount += dc.getHistorySubpanelCount());
                        cpd.setTemplateCount(cpd.templateCount += dc.getTemplateSubpanelCount());
                    }

                    boolean descHistoryIsShown = cpd.isShown(ComponentVersionDragPanel.SubPanelTypes.HISTORY);
                    for (DragPanelDescription dc : inactiveDescriptionPanels) {
                        if (stop) {
                            return;
                        }
                        seperatorComponents.add(dc);
                        dc.setVisible(descHistoryIsShown);
                        cpd.getInactiveComponentPanels().add(dc);
                        cpd.getRetiredPanels().add(dc);
                        cpd.addToggleComponent(dc);
                        cView.add(dc, gbc);
                        gbc.gridy++;
                        cpd.setAlertCount(cpd.alertCount += dc.getAlertSubpanelCount());
                        cpd.setRefexCount(cpd.refexCount += dc.getRefexSubpanelCount());
                        cpd.setHistoryCount(cpd.historyCount += dc.getHistorySubpanelCount());
                        cpd.setTemplateCount(cpd.templateCount += dc.getTemplateSubpanelCount());
                    }


                    cpr.addPanelsChangedActionListener(pcal);
                    cpr.setAlertCount(0);
                    cpr.setRefexCount(0);
                    cpr.setHistoryCount(0);
                    if (settings.showInferred()) {
                        cpr.setHistoryCount(cpr.historyCount + inactiveInferredRels.size());
                    }
                    if (settings.showStated()) {
                        cpr.setHistoryCount(cpr.historyCount + inactiveStatedRels.size());
                    }
                    boolean relHistoryIsShown = cpr.isShown(ComponentVersionDragPanel.SubPanelTypes.HISTORY);
                    cpr.setTemplateCount(0);
                    boolean cprAdded = false;
                    if (settings.showStated()) {
                        for (DragPanelRel rc : activeStatedRelPanels) {
                            if (stop) {
                                return;
                            }

                            if (!cprAdded) {
                                cView.add(cpr, gbc);
                                gbc.gridy++;
                                cprAdded = true;
                            }
                            seperatorComponents.add(rc);

                            cpr.addToggleComponent(rc);
                            cView.add(rc, gbc);
                            gbc.gridy++;
                            cpr.setAlertCount(cpr.alertCount += rc.getAlertSubpanelCount());
                            cpr.setRefexCount(cpr.refexCount += rc.getRefexSubpanelCount());
                            cpr.setHistoryCount(cpr.historyCount += rc.getHistorySubpanelCount());
                            cpr.setTemplateCount(cpr.templateCount += rc.getTemplateSubpanelCount());

                        }
                        for (DragPanelRel rc : inactiveStatedRelPanels) {
                            if (stop) {
                                return;
                            }
                            if (!cprAdded) {
                                cView.add(cpr, gbc);
                                gbc.gridy++;
                                cprAdded = true;
                            }
                            rc.setVisible(relHistoryIsShown);
                            seperatorComponents.add(rc);
                            cpr.addToggleComponent(rc);
                            cpr.getInactiveComponentPanels().add(rc);
                            cpr.getRetiredPanels().add(rc);
                            cView.add(rc, gbc);
                            gbc.gridy++;
                            cpr.setAlertCount(cpr.alertCount += rc.getAlertSubpanelCount());
                            cpr.setRefexCount(cpr.refexCount += rc.getRefexSubpanelCount());
                            cpr.setHistoryCount(cpr.historyCount += rc.getHistorySubpanelCount());
                            cpr.setTemplateCount(cpr.templateCount += rc.getTemplateSubpanelCount());

                        }

                    }
                    if (settings.showInferred()) {
                        for (DragPanelRel rc : activeInferredRelPanels) {
                            if (stop) {
                                return;
                            }
                            if (!cprAdded) {
                                cView.add(cpr, gbc);
                                gbc.gridy++;
                                cprAdded = true;
                            }

                            seperatorComponents.add(rc);

                            cpr.addToggleComponent(rc);
                            cView.add(rc, gbc);
                            gbc.gridy++;
                            cpr.setAlertCount(cpr.alertCount += rc.getAlertSubpanelCount());
                            cpr.setRefexCount(cpr.refexCount += rc.getRefexSubpanelCount());
                            cpr.setHistoryCount(cpr.historyCount += rc.getHistorySubpanelCount());
                            cpr.setTemplateCount(cpr.templateCount += rc.getTemplateSubpanelCount());

                        }
                        for (DragPanelRel rc : inactiveInferredRelPanels) {
                            if (stop) {
                                return;
                            }

                            if (!cprAdded) {
                                cView.add(cpr, gbc);
                                gbc.gridy++;
                                cprAdded = true;
                            }

                            rc.setVisible(relHistoryIsShown);
                            seperatorComponents.add(rc);

                            cpr.addToggleComponent(rc);
                            cpr.getInactiveComponentPanels().add(rc);
                            cView.add(rc, gbc);
                            gbc.gridy++;
                            cpr.setAlertCount(cpr.alertCount += rc.getAlertSubpanelCount());
                            cpr.setRefexCount(cpr.refexCount += rc.getRefexSubpanelCount());
                            cpr.setHistoryCount(cpr.historyCount += rc.getHistorySubpanelCount());
                            cpr.setTemplateCount(cpr.templateCount += rc.getTemplateSubpanelCount());

                        }
                    }

                    try {
                        if (settings.showStated()) {
                            if (stop) {
                                return;
                            }
                            addRelGroups(statedRelGroups, cprAdded, cpr, gbc);
                        }
                        if (settings.showInferred()) {
                            if (stop) {
                                return;
                            }
                            addRelGroups(inferredRelGroups, cprAdded, cpr, gbc);
                        }
                    } catch (ContraditionException e) {
                        AceLog.getAppLog().alertAndLogException(e);
                    }

                    if (templates.size() > 0) {
                        if (stop) {
                            return;
                        }
                        CollapsePanel cptemplate =
                                new CollapsePanel("aggregate extras", settings,
                                prefMap.get(PanelSection.EXTRAS), PanelSection.EXTRAS);
                        cptemplate.addPanelsChangedActionListener(pcal);
                        cptemplate.setTemplateCount(templates.size());
                        cptemplate.setRefexCount(0);
                        cptemplate.setHistoryCount(0);
                        cptemplate.setAlertCount(0);
                        cView.add(cptemplate, gbc);
                        gbc.gridy++;
                        for (Entry<SpecBI, Integer> entry : templates.entrySet()) {
                            if (stop) {
                                return;
                            }
                            Class<?> entryClass = entry.getKey().getClass();
                            if (RelSpec.class.isAssignableFrom(entryClass)) {
                                RelSpec spec = (RelSpec) entry.getKey();
                                DragPanelRelTemplate template = getRelTemplate(spec);
                                cptemplate.addToggleComponent(template);
                                cView.add(template, gbc);
                                cptemplate.getTemplatePanels().add(template);
                                gbc.gridy++;
                                cptemplate.setTemplateCount(cptemplate.templateCount++);
                            } else if (DescriptionSpec.class.isAssignableFrom(entryClass)) {
                                DescriptionSpec spec = (DescriptionSpec) entry.getKey();
                                DragPanelDescTemplate template = getDescTemplate(spec);
                                cptemplate.addToggleComponent(template);
                                cView.add(template, gbc);
                                cptemplate.getTemplatePanels().add(template);
                                gbc.gridy++;
                                cptemplate.setTemplateCount(cptemplate.templateCount++);
                            }
                        }
                    }
                    gbc.weighty = 1;
                    cView.add(new JPanel(), gbc);
                } catch (IOException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                } catch (TerminologyException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
            if (stop) {
                return;
            }
            setupHistoryPane();
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        if (stop) {
            return;
        }
        settings.getConfig().addPropertyChangeListener("commit", pcal);
        GuiUtil.tickle(cView);
        if (settings.getNavigator() != null) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (stop) {
                        return;
                    }
                    settings.getNavigator().updateHistoryPanel();

                }
            });
        }
    }

    public JPanel getHistoryPanel() {
        return historyPanel;
    }

    private void addRelGroups(Collection<RelGroupVersionBI> relGroups,
            boolean cprAdded, CollapsePanel cpr,
            GridBagConstraints gbc) throws IOException, TerminologyException, ContraditionException {
        for (RelGroupVersionBI rg : relGroups) {
            if (stop) {
                return;
            }
            Collection<? extends RelationshipVersionBI> rgRels =
                    rg.getCurrentRels(); //TODO getCurrentRels
            if (!rgRels.isEmpty()) {
                if (!cprAdded) {
                    cView.add(cpr, gbc);
                    gbc.gridy++;
                    cprAdded = true;
                }

                DragPanelRelGroup rgc = getRelGroupComponent(rg, cpr);
                seperatorComponents.add(rgc);
                cView.add(rgc, gbc);
                gbc.gridy++;
                cpr.setAlertCount(cpr.alertCount += rgc.getAlertSubpanelCount());
                cpr.setRefexCount(cpr.refexCount += rgc.getRefexSubpanelCount());
                cpr.setHistoryCount(cpr.historyCount += rgc.getHistorySubpanelCount());
                cpr.setTemplateCount(cpr.templateCount += rgc.getTemplateSubpanelCount());
            }
        }
    }

    public class RefreshHistoryViewListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            if (stop) {
                return;
            }
            settings.getNavigator().updateHistoryPanel();
        }
    }

    private void setupHistoryPane() throws IOException, ContraditionException {
        if (paths == null) {
            return;
        }
        int row = 0;
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
            if (stop) {
                return;
            }
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
            pathCheck.setVisible(cView.isHistoryShown());
            historyPanel.add(pathCheck, gbc);
            gbc.gridy++;
            row++;
        }
    }

    private class SetupDescriptions implements Runnable {

        CountDownLatch latch;
        Collection<? extends I_DescriptionTuple> descs;
        Collection<DragPanelDescription> panels;
        CollapsePanel cp;

        public SetupDescriptions(CountDownLatch latch,
                Collection<? extends I_DescriptionTuple> descs,
                Collection<DragPanelDescription> panels,
                CollapsePanel cp) {
            this.latch = latch;
            this.descs = descs;
            this.panels = panels;
            this.cp = cp;
        }

        @Override
        public void run() {
            for (I_DescriptionTuple desc : descs) {
                if (stop) {
                    return;
                }
                DragPanelDescription dc;
                try {
                    dc = getDescComponent(desc, cp);
                    panels.add(dc);
                } catch (TerminologyException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                } catch (IOException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            }
            latch.countDown();
        }
    }

    private void setupDescriptions(CountDownLatch latch,
            Collection<? extends I_DescriptionTuple> descs,
            Collection<DragPanelDescription> panels,
            CollapsePanel cp) throws TerminologyException, IOException {
        if (stop) {
            return;
        }
        SetupDescriptions setterUpper = new SetupDescriptions(latch, descs, panels, cp);
        ACE.threadPool.execute(setterUpper);
    }

    public DragPanelDescription getDescComponent(DescriptionAnalogBI desc,
            CollapsePanel parentCollapsePanel)
            throws TerminologyException, IOException {
        DragPanelDescription dragDescPanel =
                new DragPanelDescription(new GridBagLayout(), this,
                parentCollapsePanel, desc);
        addToPositionPanelMap(dragDescPanel);
        return dragDescPanel;
    }

    private class SetupRels implements Runnable {

        CountDownLatch latch;
        Collection<? extends I_RelTuple> rels;
        Collection<DragPanelRel> panels;
        CollapsePanel cp;
        boolean inferred;

        public SetupRels(CountDownLatch latch,
                Collection<? extends I_RelTuple> rels,
                Collection<DragPanelRel> panels,
                CollapsePanel cp,
                boolean inferred) {
            this.latch = latch;
            this.rels = rels;
            this.panels = panels;
            this.cp = cp;
            this.inferred = inferred;
        }

        @Override
        public void run() {
            for (I_RelTuple r : rels) {
                if (stop) {
                    return;
                }
                DragPanelRel rc;
                try {
                    if (r.getGroup() == 0) {
                        rc = getRelComponent(r, cp, inferred);
                        panels.add(rc);
                    }
                } catch (TerminologyException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                } catch (IOException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            }
            latch.countDown();
        }
    }

    private void setupRels(CountDownLatch latch,
            Collection<? extends I_RelTuple> rels,
            Collection<DragPanelRel> panels,
            CollapsePanel cp,
            boolean inferred) throws TerminologyException, IOException {
        SetupRels setterUpper = new SetupRels(latch, rels, panels, cp, inferred);
        if (stop) {
            return;
        }
        ACE.threadPool.execute(setterUpper);

    }

    public DragPanelRel getRelComponent(RelationshipVersionBI r,
            CollapsePanel parentCollapsePanel, boolean inferred)
            throws TerminologyException, IOException {
        DragPanelRel relPanel = new DragPanelRel(new GridBagLayout(), this,
                parentCollapsePanel, r, inferred);
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

    public DragPanelRelGroup getRelGroupComponent(RelGroupVersionBI group,
            CollapsePanel parentCollapsePanel)
            throws TerminologyException, IOException, ContraditionException {
        DragPanelRelGroup relGroupPanel =
                new DragPanelRelGroup(new GridBagLayout(), this,
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
        JButton actionMenuButton = relGroupPanel.getActionMenuButton();
        (new GetActionsSwingWorker(actionMenuButton, group)).execute();
        CollapsePanel cprg = new CollapsePanel("group:", settings,
                new CollapsePanelPrefs(prefMap.get(PanelSection.REL_GRP)),
                PanelSection.REL_GRP, actionMenuButton);
        cprg.setAlertCount(0);
        cprg.setRefexCount(0);
        cprg.setHistoryCount(0);
        cprg.setTemplateCount(0);
        relGroupPanel.add(cprg, gbc);
        gbc.gridy++;

        HashSet<Integer> activeRelIds = new HashSet();
        for (RelationshipVersionBI rv : group.getCurrentRels()) {
            activeRelIds.add(rv.getNid());
            DragPanelRel dpr = getRelComponent(rv, parentCollapsePanel, rv.isInferred());
            cprg.addToggleComponent(dpr);
            dpr.setInGroup(true);
            relGroupPanel.add(dpr, gbc);
            gbc.gridy++;
            cprg.setAlertCount(cprg.alertCount += dpr.getAlertSubpanelCount());
            cprg.setRefexCount(cprg.refexCount += dpr.getRefexSubpanelCount());
            cprg.setHistoryCount(cprg.historyCount += dpr.getHistorySubpanelCount());
            cprg.setTemplateCount(cprg.templateCount += dpr.getTemplateSubpanelCount());
        }
        boolean relHistoryIsShown = cpr.isShown(ComponentVersionDragPanel.SubPanelTypes.HISTORY);
        for (RelationshipVersionBI rv : group.getAllRels()) {
            if (!activeRelIds.contains(rv.getNid())) {
                DragPanelRel dpr = getRelComponent(rv, parentCollapsePanel, rv.isInferred());
                dpr.setVisible(relHistoryIsShown);
                cprg.addToggleComponent(dpr);
                dpr.setInGroup(true);
                relGroupPanel.add(dpr, gbc);
                parentCollapsePanel.getHistoryPanels().add(dpr);
                parentCollapsePanel.getInactiveComponentPanels().add(dpr);
                gbc.gridy++;
                cprg.setAlertCount(cprg.alertCount += dpr.getAlertSubpanelCount());
                cprg.setRefexCount(cprg.refexCount += dpr.getRefexSubpanelCount());
                cprg.setHistoryCount(cprg.historyCount += dpr.getHistorySubpanelCount());
                cprg.setTemplateCount(cprg.templateCount += dpr.getTemplateSubpanelCount());
            }
        }

        return relGroupPanel;

    }

    private class GetActionsSwingWorker extends SwingWorker<Collection<Action>, Collection<Action>> {

        JButton actionMenuButton;
        RelGroupVersionBI group;

        public GetActionsSwingWorker(JButton actionMenuButton,
                RelGroupVersionBI group) {
            this.actionMenuButton = actionMenuButton;
            this.group = group;
        }

        @Override
        protected Collection<Action> doInBackground() throws Exception {
            if (stop) {
                return null;
            }
            return cView.getKbActions(group);
        }

        @Override
        protected void done() {
            try {
                if (stop) {
                    return;
                }
                Collection<Action> actions = get();
                actionMenuButton.addActionListener(new DoDynamicPopup(actions));
                if (actions == null || actions.isEmpty()) {
                    actionMenuButton.setVisible(false);
                }
            } catch (InterruptedException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            } catch (ExecutionException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    public DragPanelConceptAttributes getConAttrComponent(ConAttrAnalogBI conAttr,
            CollapsePanel parentCollapsePanel)
            throws TerminologyException, IOException {
        DragPanelConceptAttributes dragConAttrPanel =
                new DragPanelConceptAttributes(new GridBagLayout(), this,
                parentCollapsePanel, conAttr);
        addToPositionPanelMap(dragConAttrPanel);
        return dragConAttrPanel;
    }

    public DragPanelDescTemplate getDescTemplate(final DescriptionSpec desc)
            throws TerminologyException, IOException {
        DragPanelDescTemplate descPanel =
                new DragPanelDescTemplate(new GridBagLayout(), this, desc);
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
                getLabel(desc.getDescTypeSpec().getStrict(config.getViewCoordinate()).getNid(), true);
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
                new UpdateTextTemplateDocumentListener(textPane, desc, config));
        return descPanel;
    }

    public DragPanelRelTemplate getRelTemplate(final RelSpec spec)
            throws TerminologyException, IOException {
        DragPanelRelTemplate relPanel =
                new DragPanelRelTemplate(new GridBagLayout(), this, spec);
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
                spec.getRelTypeSpec().getStrict(coordinate).getNid(), true);
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
                spec.getDestinationSpec().getStrict(coordinate).getNid(), true);
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

    private void addToPositionPanelMap(ComponentVersionDragPanel<?> panel) throws IOException {
        ComponentVersionBI componentVersion = panel.getComponentVersion();
        if (componentVersion == null) {
            return;
        }
        PositionBI position = componentVersion.getPosition();
        Collection<ComponentVersionDragPanel<?>> panels =
                positionPanelMap.get(position);
        if (panels == null) {
            panels = new ConcurrentSkipListSet<ComponentVersionDragPanel<?>>(new Comparator<ComponentVersionDragPanel<?>>() {

                @Override
                public int compare(ComponentVersionDragPanel<?> o1, ComponentVersionDragPanel<?> o2) {
                    return o1.getId() - o2.getId();
                }
            });
            positionPanelMap.put(position, panels);
        }
        panels.add(panel);
    }

    private static class UpdateTextTemplateDocumentListener
            implements DocumentListener, ActionListener {

        FixedWidthJEditorPane editorPane;
        DescriptionSpec desc;
        Timer t;
        I_GetConceptData c;
        boolean update = false;
        I_ConfigAceFrame config;

        public UpdateTextTemplateDocumentListener(FixedWidthJEditorPane editorPane,
                DescriptionSpec desc, I_ConfigAceFrame config) throws TerminologyException, IOException {
            super();
            this.editorPane = editorPane;
            this.desc = desc;
            this.config = config;
            t = new Timer(1000, this);
            t.start();
            c = Terms.get().getConcept(desc.getConceptSpec().getStrict(config.getViewCoordinate()).getNid());
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

    public void stop() {
        this.stop = true;
        settings.getConfig().removePropertyChangeListener("commit", pcal);
        while (latch.getCount() > 0) {
            latch.countDown();
        }
        for (DragPanel.DropPanelActionManager dpam : getDropPanelActionManagers()) {
            dpam.removeReferences();
        }
        getDropPanelActionManagers().clear();
    }

    public TreeSet<PositionBI> getPositionOrderedSet() {

        TreeSet positionSet = new TreeSet(new PositionComparator());
        if (positionOrderedSet != null) {
            positionSet.addAll(positionOrderedSet);
        }
        return positionSet;
    }
}
