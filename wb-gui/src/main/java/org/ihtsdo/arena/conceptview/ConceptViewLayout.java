
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.conceptview;

//~--- non-JDK imports --------------------------------------------------------
import org.dwfa.ace.ACE;
import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.TermComponentLabel.LabelText;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.arena.conceptview.ConceptView.PanelSection;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeAnalogBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelationshipGroupVersionBI;
import org.ihtsdo.tk.spec.DescriptionSpec;
import org.ihtsdo.tk.spec.RelationshipSpec;
import org.ihtsdo.tk.spec.SpecBI;
import org.ihtsdo.tk.spec.SpecFactory;
import org.ihtsdo.util.swing.GuiUtil;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import java.util.*;
import java.util.Map.Entry;
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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.helper.bdb.MultiEditorContradictionCase;
import org.ihtsdo.helper.bdb.MultiEditorContradictionDetector;
import org.ihtsdo.tk.api.*;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.binding.snomed.RefsetAux;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

/**
 *
 * @author kec
 * @author akf
 */
public class ConceptViewLayout extends SwingWorker<Map<SpecBI, Integer>, Object> {
    
    public boolean stop = false;
    private Collection<DragPanel> seperatorComponents =
            new ConcurrentSkipListSet<>(new Comparator<DragPanel>() {
        @Override
        public int compare(DragPanel o1, DragPanel o2) {
            return o1.getId() - o2.getId();
        }
    });
    private Map<Integer, JCheckBox> rowToPathCheckMap = new ConcurrentHashMap<>();
    //
    private Map<PositionBI, Collection<DragPanelComponentVersion<?>>> positionPanelMap =
            new ConcurrentHashMap<>();
    private TreeSet<PositionBI> positionOrderedSet =
            new TreeSet(new PositionComparator());
    private Map<PathBI, Integer> pathRowMap = new ConcurrentHashMap<>();
    private CountDownLatch latch = new CountDownLatch(6);
    private Lock dramsLock = new ReentrantLock();
    private Map<Integer, Collection<Integer>> componentCountForConflict = new TreeMap<>();
    private List<DragPanelDescription> activeDescriptionPanels;
    private List<DragPanelRel> activeInferredRelPanels;
    private List<DragPanelRel> activeShortNormRelPanels = new ArrayList<>();
    private List<DragPanelRel> activeLongNormRelPanels = new ArrayList<>();
    private HashSet<RelationshipVersionBI> duplicateRels;
    private List<DragPanelRel> activeStatedRelPanels;
    protected ConceptView cView;
    private I_ConfigAceFrame config;
    private ViewCoordinate statedCoordinate;
    private ViewCoordinate inferredCoordinate;
    private CollapsePanel cpd;
    private CollapsePanel cpr;
    private ConceptVersionBI cv;
    private List<? extends I_DescriptionTuple> descriptions;
    protected AtomicReference<Collection<DragPanel.DropPanelActionManager>> dpamsRef;
    private List<DragPanelDescription> inactiveDescriptionPanels;
    private List<? extends I_DescriptionTuple> inactiveDescriptions;
    private List<DragPanelRel> inactiveInferredRelPanels;
    private List<? extends I_RelTuple> inactiveInferredRels;
    private List<DragPanelRel> inactiveStatedRelPanels;
    private List<? extends I_RelTuple> inactiveStatedRels;
    private Collection<RelationshipGroupVersionBI> inferredRelGroups;
    private List<? extends I_RelTuple> inferredRels;
    private I_GetConceptData layoutConcept;
    private Collection<? extends RefexVersionBI<?>> memberRefsets;
    private Set<PathBI> paths;
    private PanelsChangedActionListener pcal;
    private Set<PositionBI> positions;
    private Map<PanelSection, CollapsePanelPrefs> prefMap;
    private Set<Integer> stamps;
    private ConceptViewSettings settings;
    private Collection<RelationshipGroupVersionBI> statedRelGroups = new ArrayList<>();
    private Collection<RelationshipGroupVersionBI> shortNormRelGroups = new HashSet<>();
    private Collection<RelationshipGroupVersionBI> longNormRelGroups = new ArrayList<>();
    private List<? extends I_RelTuple> statedRels;
    private JPanel conceptPanel;
    private Set<Integer> sapsForConflict = new HashSet<>();

    //~--- constructors --------------------------------------------------------
    public ConceptViewLayout(ConceptView conceptView, I_GetConceptData layoutConcept) throws IOException {
        super();
        
        if ((layoutConcept != null) && (layoutConcept.getNid() != 0)
                && (Ts.get().getConceptNidForNid(layoutConcept.getNid()) == layoutConcept.getNid())
                && !layoutConcept.isCanceled()) {
            this.layoutConcept = layoutConcept;
        } else {
            this.layoutConcept = null;
        }
        
        this.cView = conceptView;
        this.settings = conceptView.getSettings();
        this.pcal = new PanelsChangedActionListener(settings);
        this.config = conceptView.getConfig();
        this.prefMap = conceptView.getPrefMap();
    }

    //~--- methods -------------------------------------------------------------
    private void addRelGroups(Collection<RelationshipGroupVersionBI> relGroups, boolean cprAdded, CollapsePanel cpr,
            GridBagConstraints gbc)
            throws IOException, TerminologyException, ContradictionException {
        int currentRgRels = 0;
        boolean relHistoryIsShown = cpr.isShown(DragPanelComponentVersion.SubPanelTypes.HISTORY);
        
        for (RelationshipGroupVersionBI rg : relGroups) {
            if (stop) {
                return;
            }
            
            Collection<? extends RelationshipChronicleBI> rgRels = rg.getRelationships();
            
            if (rgRels.size() == rg.getRelationshipsActiveAllVersions().size()) {
                if (!rgRels.isEmpty()) {
                    if (!cprAdded) {
                        conceptPanel.add(cpr, gbc);
                        gbc.gridy++;
                        cprAdded = true;
                    }
                    
                    DragPanelRelGroup rgc = getRelGroupComponent(rg, cpr);
                    
                    seperatorComponents.add(rgc);
                    conceptPanel.add(rgc, gbc);
                    gbc.gridy++;
                    cpr.setAlertCount(cpr.alertCount += rgc.getAlertSubpanelCount());
                    cpr.setRefexCount(cpr.refexCount += rgc.getRefexSubpanelCount());
                    cpr.setHistoryCount(cpr.historyCount += rgc.getHistorySubpanelCount());
                    cpr.setTemplateCount(cpr.templateCount += rgc.getTemplateSubpanelCount());
                }
            } else if (rg.getRelationshipsActiveAllVersions().isEmpty()) {
                if (!rgRels.isEmpty()) {
                    if (!cprAdded) {
                        conceptPanel.add(cpr, gbc);
                        gbc.gridy++;
                        cprAdded = true;
                    }
                    
                    DragPanelRelGroup rgc = getRelGroupComponent(rg, cpr);
                    
                    cpr.getInactiveComponentPanels().add(rgc);
                    rgc.setVisible(relHistoryIsShown);
                    seperatorComponents.add(rgc);
                    conceptPanel.add(rgc, gbc);
                    gbc.gridy++;
                    cpr.setAlertCount(cpr.alertCount += rgc.getAlertSubpanelCount());
                    cpr.setRefexCount(cpr.refexCount += rgc.getRefexSubpanelCount());
                    cpr.setHistoryCount(cpr.historyCount += rgc.getHistorySubpanelCount());
                    cpr.setTemplateCount(cpr.templateCount += rgc.getTemplateSubpanelCount());
                }
            } else {
                if (!rgRels.isEmpty()) {
                    if (!cprAdded) {
                        conceptPanel.add(cpr, gbc);
                        gbc.gridy++;
                        cprAdded = true;
                    }
                    
                    DragPanelRelGroup rgc = getRelGroupComponent(rg, cpr);
                    
                    seperatorComponents.add(rgc);
                    conceptPanel.add(rgc, gbc);
                    gbc.gridy++;
                    cpr.setAlertCount(cpr.alertCount += rgc.getAlertSubpanelCount());
                    cpr.setRefexCount(cpr.refexCount += rgc.getRefexSubpanelCount());
                    cpr.setHistoryCount(cpr.historyCount += rgc.getHistorySubpanelCount());
                    cpr.setTemplateCount(cpr.templateCount += rgc.getTemplateSubpanelCount());
                }
            }
        }
    }
    
    private void addToPositionPanelMap(DragPanelComponentVersion<?> panel) throws IOException {
        ComponentVersionBI componentVersion = panel.getComponentVersion();
        
        if (componentVersion == null) {
            return;
        }
        
        PositionBI position = componentVersion.getPosition();
        Collection<DragPanelComponentVersion<?>> panels = positionPanelMap.get(position);
        
        if (panels == null) {
            panels = new ConcurrentSkipListSet<>(
                    new Comparator<DragPanelComponentVersion<?>>() {
                        @Override
                        public int compare(DragPanelComponentVersion<?> o1, DragPanelComponentVersion<?> o2) {
                            return o1.getId() - o2.getId();
                        }
                    });
            positionPanelMap.put(position, panels);
        }
        
        panels.add(panel);
    }
    
    @Override
    protected Map<SpecBI, Integer> doInBackground() throws Exception {
        componentCountForConflict.clear();
        
        if (stop) {
            return null;
        }
        
        if (layoutConcept != null) {
            cpd = new CollapsePanel("descriptions:", settings, prefMap.get(PanelSection.DESC),
                    PanelSection.DESC);
            cpr = new CollapsePanel("relationships:", settings, prefMap.get(PanelSection.REL), PanelSection.REL);
            
            if (stop) {
                return null;
            }
            
            updateUncommitted();
            statedCoordinate = new ViewCoordinate(config.getViewCoordinate());
            statedCoordinate.setRelationshipAssertionType(RelAssertionType.STATED);
            inferredCoordinate = new ViewCoordinate(config.getViewCoordinate());
            inferredCoordinate.setRelationshipAssertionType(RelAssertionType.INFERRED);
            stamps = layoutConcept.getAllStampNids();
            positions = Ts.get().getPositionSet(stamps);
            paths = Ts.get().getPathSetFromPositionSet(positions);
            positionOrderedSet.addAll(positions);
            
            if (stop) {
                return null;
            }
            
            statedCoordinate.setRelationshipAssertionType(RelAssertionType.STATED);
            statedRels = layoutConcept.getSourceRelTuples(config.getAllowedStatus(), null,
                    config.getViewPositionSetReadOnly(), config.getPrecedence(),
                    config.getConflictResolutionStrategy(), statedCoordinate.getClassifierNid(),
                    statedCoordinate.getRelationshipAssertionType());
            removeContradictions(statedRels);
            
            if (stop) {
                return null;
            }

            //find contradictions
            NidBitSetBI nidSet = Ts.get().getEmptyNidSet();
            nidSet.setMember(layoutConcept.getConceptNid());
            int commitRecRefsetNid = Ts.get().getNidForUuids(RefsetAuxiliary.Concept.COMMIT_RECORD.getUids());
            int adjRecRefsetNid = Ts.get().getNidForUuids(RefsetAuxiliary.Concept.ADJUDICATION_RECORD.getUids());
            List<MultiEditorContradictionCase> cases = new ArrayList<>();
            MultiEditorContradictionDetector mecd;
            mecd = new MultiEditorContradictionDetector(commitRecRefsetNid,
                    adjRecRefsetNid,
                    config.getViewCoordinate(),
                    cases, null,
                    nidSet,
                    true, true);
            Ts.get().iterateConceptDataInParallel(mecd);
            for (MultiEditorContradictionCase contCase : cases) {
                sapsForConflict = contCase.getStamps();
            }

            // REPORT COMPONENTS WITH MISSING COMMIT RECORDS
            if (mecd.hasComponentsMissingCommitRecord()) {
                StringBuilder sb = new StringBuilder();
                sb.append("\r\n**** COMPONENTS MISSING COMMITRECORDS ****");
                sb.append("\r\n[MultiEditorContradictionDetectionMojo] MISSING COMMITRECORDS LIST\r\n");
                sb.append(mecd.toStringMissingCommitRecords());
                sb.append("\r\n");
                AceLog.getAppLog().log(Level.WARNING, sb.toString());
            }
            
            activeStatedRelPanels = new ArrayList<>(statedRels.size());
            setupRels(latch, statedRels, activeStatedRelPanels, cpr, false);
             
            if (stop) {
                return null;
            }
            
            inactiveStatedRels = layoutConcept.getSourceRelTuples(null, null,
                    config.getViewPositionSetReadOnly(), config.getPrecedence(),
                    config.getConflictResolutionStrategy(), statedCoordinate.getClassifierNid(),
                    statedCoordinate.getRelationshipAssertionType());
            
            if (stop) {
                return null;
            }
            
            inactiveStatedRels.removeAll(statedRels);
            removeContradictions(inactiveStatedRels);
            inactiveStatedRelPanels = new ArrayList<>(inactiveStatedRels.size());
            
            if (stop) {
                return null;
            }
            
            setupRels(latch, inactiveStatedRels, inactiveStatedRelPanels, cpr, false);
            statedRelGroups = (Collection<RelationshipGroupVersionBI>) Ts.get().getConceptVersion(statedCoordinate,
                    layoutConcept.getNid()).getRelationshipGroups();
            
            if (stop) {
                return null;
            }
            
            statedCoordinate.setRelationshipAssertionType(RelAssertionType.INFERRED);
            inferredRels = layoutConcept.getSourceRelTuples(config.getAllowedStatus(), null,
                    config.getViewPositionSetReadOnly(), config.getPrecedence(),
                    config.getConflictResolutionStrategy(), statedCoordinate.getClassifierNid(),
                    statedCoordinate.getRelationshipAssertionType());
            List<ComponentVersionBI> extra = new ArrayList<>();
            for (I_RelTuple inferredRel : inferredRels) {
                for (I_RelTuple statedRel : statedRels) {
                    if (inferredRel.getPrimUuid().equals(statedRel.getPrimUuid())) {
                        extra.add(inferredRel);
                    }
                }
            }
            inferredRels.removeAll(extra);
            extra.clear();
            removeContradictions(inferredRels);
            activeInferredRelPanels = new ArrayList<>(inferredRels.size());
            
            if (stop) {
                return null;
            }
            
            setupRels(latch, inferredRels, activeInferredRelPanels, cpr, true);
            inactiveInferredRels = layoutConcept.getSourceRelTuples(null, null,
                    config.getViewPositionSetReadOnly(), config.getPrecedence(),
                    config.getConflictResolutionStrategy(), statedCoordinate.getClassifierNid(),
                    statedCoordinate.getRelationshipAssertionType());
            
            if (stop) {
                return null;
            }
            
            inactiveInferredRels.removeAll(inferredRels);
            for (I_RelTuple inferredRel : inactiveInferredRels) {
                for (I_RelTuple statedRel : statedRels) {
                    if (inferredRel.getPrimUuid().equals(statedRel.getPrimUuid())) {
                        extra.add(inferredRel);
                    }
                }
            }
            inactiveInferredRels.removeAll(extra);
            removeContradictions(inactiveInferredRels);
            inactiveInferredRelPanels = new ArrayList<>(inactiveInferredRels.size());
            setupRels(latch, inactiveInferredRels, inactiveInferredRelPanels, cpr, true);
            
            if (stop) {
                return null;
            }
            
            inferredRelGroups = (Collection<RelationshipGroupVersionBI>) Ts.get().getConceptVersion(statedCoordinate,
                    layoutConcept.getNid()).getRelationshipGroups();
            cv = Ts.get().getConceptVersion(config.getViewCoordinate(), layoutConcept.getNid());

            // get refsets
            if (stop) {
                return null;
            }
            
            memberRefsets = cv.getRefsetMembersActive();

            // Get active descriptions
            descriptions = layoutConcept.getDescriptionTuples(config.getAllowedStatus(), null,
                    config.getViewPositionSetReadOnly(), config.getPrecedence(),
                    config.getConflictResolutionStrategy());
            removeContradictions(descriptions);
            activeDescriptionPanels = new ArrayList<>(descriptions.size());
            
            if (stop) {
                return null;
            }
            
            setupDescriptions(latch, descriptions, activeDescriptionPanels, cpd);

            // get all descriptions
            List<? extends I_DescriptionTuple> tempDescList = layoutConcept.getDescriptionTuples(null, null,
                    config.getViewPositionSetReadOnly(),
                    config.getPrecedence(),
                    config.getConflictResolutionStrategy());
            
            if (stop) {
                return null;
            }
            
            tempDescList.removeAll(descriptions);
            inactiveDescriptions = new ArrayList<>(tempDescList);
            removeContradictions(inactiveDescriptions);
            inactiveDescriptionPanels = new ArrayList<>(inactiveDescriptions.size());
            setupDescriptions(latch, inactiveDescriptions, inactiveDescriptionPanels, cpd);
            
            if (stop) {
                return null;
            }
            
            latch.await();
        }
        
        if (stop) {
            return null;
        }
        Map<SpecBI, Integer> templates = cView.getKb().setConcept(layoutConcept, settings);
        
        return templates;
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
            
            cView.getCvRenderer().updateLabel();
            cView.getCvRenderer().updateCancelAndCommit();
            if (!sapsForConflict.isEmpty()) {
                cView.getCvRenderer().showConflictIcon(true);
            } else {
                cView.getCvRenderer().showConflictIcon(false);
            }
            
            Map<SpecBI, Integer> templates = get();
            
            cView.removeAll();
            cView.setLayout(new GridBagLayout());
            
            if (layoutConcept != null) {
                if (stop) {
                    return;
                }
                
                
                try {
                    GridBagConstraints topGbc = new GridBagConstraints();
                    
                    topGbc.weightx = 1;
                    topGbc.weighty = 0;
                    topGbc.anchor = GridBagConstraints.NORTHWEST;
                    topGbc.fill = GridBagConstraints.BOTH;
                    topGbc.gridheight = 1;
                    topGbc.gridwidth = 1;
                    topGbc.gridx = 1;
                    topGbc.gridy = 0;
                    //cView.add(pathCheckboxPanel, topGbc);
                    topGbc.gridy++;
                    topGbc.anchor = GridBagConstraints.NORTHWEST;
                    topGbc.weightx = 1;
                    topGbc.weighty = 0;
                    topGbc.fill = GridBagConstraints.BOTH;
                    conceptPanel = new JPanel(new GridBagLayout());
                    cView.add(conceptPanel, topGbc);
                    
                    topGbc.gridy++;
                    topGbc.weighty = 1;
                    JPanel filler = new JPanel();
                    filler.setBackground(Color.LIGHT_GRAY);
                    
                    cView.add(filler, topGbc);
                    
                    if (stop) {
                        return;
                    }
                    
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.weightx = 1;
                    gbc.weighty = 0;
                    gbc.anchor = GridBagConstraints.NORTHWEST;
                    gbc.fill = GridBagConstraints.BOTH;
                    gbc.gridheight = 1;
                    gbc.gridwidth = 1;
                    gbc.gridx = 0;
                    gbc.gridy = 0;
                    
                    CollapsePanel cpe = new CollapsePanel("concept:", settings, prefMap.get(PanelSection.CONCEPT),
                            PanelSection.CONCEPT);
                    
                    cpe.addPanelsChangedActionListener(pcal);
                    conceptPanel.add(cpe, gbc);
                    gbc.gridy++;
                    
                    I_TermFactory tf = Terms.get();
                    ConceptAttributeAnalogBI cav = null;
                    try {
                        cav = (ConceptAttributeAnalogBI) cv.getConceptAttributesActive();
                    } catch (ContradictionException ex) {
                        Collection<? extends ComponentVersionBI> versions = cv.getConceptAttributes().getVersions(cv.getViewCoordinate());
                        if (!versions.isEmpty()) {
                            removeContradictions(versions);
                            cav = (ConceptAttributeAnalogBI) versions.iterator().next();
                        }
                    }
                    if (cav == null) {
                        cav = (ConceptAttributeAnalogBI) cv.getConceptAttributes().getVersion(statedCoordinate.getViewCoordinateWithAllStatusValues());
                    }
                    DragPanelConceptAttributes cac;
                    if (cav == null) {
                        if (cv.getChronicle().getConceptAttributes() != null) {
                            cav = (ConceptAttributeAnalogBI) cv.getChronicle().getConceptAttributes().getPrimordialVersion();
                            cac = getConAttrComponent((ConceptAttributeAnalogBI) cav, cpe);
                            cac.showConflicts(sapsForConflict);
                            
                            seperatorComponents.add(cac);
                            cpe.addToggleComponent(cac);
                        } else {
                            cac = getConAttrComponent((ConceptAttributeAnalogBI) cav, cpe);
                        }
                    } else {
                        cac = getConAttrComponent((ConceptAttributeAnalogBI) cav, cpe);
                        
                        setShowConflicts(cav, cac);
                        seperatorComponents.add(cac);
                        cpe.addToggleComponent(cac);
                    }
                    
                    if (stop) {
                        return;
                    }
                    
                    
                    cpe.setAlertCount(0);
                    
                    if ((cav == null) || (cav.getRefexesActive(statedCoordinate) == null)) {
                        cpe.setRefexCount(0);
                    } else {
                        cpe.setRefexCount(cav.getRefexesActive(statedCoordinate).size());
                    }
                    
                    cpe.setHistoryCount(cac.getHistorySubpanelCount());
                    cpe.setTemplateCount(0);
                    conceptPanel.add(cac, gbc);
                    gbc.gridy++;
                    
                    if (stop) {
                        return;
                    }
                    
                    if (memberRefsets != null) {
                        boolean displayRefsetMembersInArena = false;
                        
                        if (displayRefsetMembersInArena) {
                            for (RefexVersionBI<?> extn : memberRefsets) {
                                if (stop) {
                                    return;
                                }
                                
                                int refsetNid = extn.getRefexNid();
                                for (RefexVersionBI cr : layoutConcept.getRefexMembersActive(statedCoordinate, refsetNid)) {
                                    DragPanelExtension ce = new DragPanelExtension(this, cpe, extn);
                                    setShowConflicts(ce.getComponentVersion(), ce);
                                    seperatorComponents.add(ce);
                                    cpe.addToggleComponent(ce);
                                    conceptPanel.add(ce, gbc);
                                    cpe.getRefexPanels().add(ce);
                                    gbc.gridy++;
                                    cpe.setAlertCount(cpe.alertCount += ce.getAlertSubpanelCount());
                                    cpe.setRefexCount(cpe.refexCount += ce.getRefexSubpanelCount());
                                    cpe.setHistoryCount(cpe.historyCount += ce.getHistorySubpanelCount());
                                    cpe.setTemplateCount(cpe.templateCount += ce.getTemplateSubpanelCount());
                                }
                            }
                        }
                    }
                    
                    cpd.addPanelsChangedActionListener(pcal);
                    cpd.setAlertCount(0);
                    cpd.setRefexCount(0);
                    cpd.setHistoryCount(inactiveDescriptions.size());
                    cpd.setTemplateCount(0);
                    conceptPanel.add(cpd, gbc);
                    gbc.gridy++;
                    
                    sortDescriptions();
                    
                    for (DragPanelDescription dc : activeDescriptionPanels) {
                        if (stop) {
                            return;
                        }
                        
                        setShowConflicts(dc.getComponentVersion(), dc);
                        seperatorComponents.add(dc);
                        cpd.addToggleComponent(dc);
                        conceptPanel.add(dc, gbc);
                        gbc.gridy++;
                        cpd.setAlertCount(cpd.alertCount += dc.getAlertSubpanelCount());
                        cpd.setRefexCount(cpd.refexCount += dc.getRefexSubpanelCount());
                        cpd.setHistoryCount(cpd.historyCount += dc.getHistorySubpanelCount());
                        cpd.setTemplateCount(cpd.templateCount += dc.getTemplateSubpanelCount());
                        if(dc.getThingToDrag().isUncommitted() && cView.focus){
                                dc.getTextPane().requestFocusInWindow();
                                cView.focus = false;
                        }
                    }
                    
                    boolean descHistoryIsShown = cpd.isShown(DragPanelComponentVersion.SubPanelTypes.HISTORY);
                    
                    for (DragPanelDescription dc : inactiveDescriptionPanels) {
                        if (stop) {
                            return;
                        }
                        
                        setShowConflicts(dc.getComponentVersion(), dc);
                        seperatorComponents.add(dc);
                        dc.setVisible(descHistoryIsShown);
                        cpd.getInactiveComponentPanels().add(dc);
                        cpd.getRetiredPanels().add(dc);
                        cpd.addToggleComponent(dc);
                        conceptPanel.add(dc, gbc);
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
                    
                    if(settings.getRelAssertionType().equals(RelAssertionType.SHORT_NORMAL_FORM)){
                        HashSet<RelationshipVersionBI> primitives = getPrimitives(Ts.get().getConceptVersion(inferredCoordinate, cv.getConceptNid()));
                        HashSet<RelationshipVersionBI> uniquePrimitives = removeDuplicatePrimitives(primitives);
                        HashSet<RelationshipVersionBI> proximalPrimitives = getProximalPrimitives(uniquePrimitives);
                        HashSet<RelationshipVersionBI> roleRelationships = getRoleRelationships(cv.getConceptNid());
                        if(!roleRelationships.isEmpty()){
                            shortNormRelGroups = new ArrayList<>();
                            HashSet<RelationshipVersionBI> roleRelsToDisplay = findShortNormRoleRelsToDisplay(roleRelationships, proximalPrimitives);
                            for (RelationshipVersionBI rel : roleRelsToDisplay) {
                                if (rel.getGroup() == 0) {
                                    DragPanelRel dragPanel = getRelComponent(rel, cpe, true);
                                    activeShortNormRelPanels.add(dragPanel);
                                } else {
                                    int group = rel.getGroup();
                                    boolean add = true;
                                    for (RelationshipGroupVersionBI rg : shortNormRelGroups) {
                                        if (rg.getRelationshipGroupNumber() == group) {
                                            add = false;
                                        }
                                    }
                                    if (add) {
                                        for (RelationshipGroupVersionBI relGroup : (Ts.get().getConceptVersion(inferredCoordinate, cv.getConceptNid())).getRelationshipGroups()) {
                                            if (relGroup.getRelationshipGroupNumber() == group) {
                                                if (relGroup.getRelationshipsActive().iterator().next().getCharacteristicNid()
                                                        == SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid()) {
                                                    shortNormRelGroups.add(relGroup);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        for(RelationshipVersionBI rel : proximalPrimitives){
                            DragPanelRel dragPanel = getRelComponent(rel, cpe, true);
                            activeShortNormRelPanels.add(dragPanel);
                        }
                        
                    }
                    
                    if (settings.getRelAssertionType().equals(RelAssertionType.LONG_NORMAL_FORM)) {
                        HashSet<RelationshipVersionBI> primitives = getPrimitives(Ts.get().getConceptVersion(inferredCoordinate, cv.getConceptNid()));
                        HashSet<RelationshipVersionBI> uniquePrimitives = removeDuplicatePrimitives(primitives);
                        HashSet<RelationshipVersionBI> proximalPrimitives = getProximalPrimitives(uniquePrimitives);
                        HashSet<RelationshipVersionBI> inferredRoleRels = getRoleRelationships(cv.getConceptNid());
                        Collection<? extends RelationshipVersionBI> rels = cv.getRelationshipsOutgoingActive();
                        HashSet<RelationshipVersionBI> statedRoleRels = new HashSet<>();
                        for (RelationshipVersionBI r : rels) {
                            if (r.getTypeNid() != Snomed.IS_A.getLenient().getNid()
                                    && r.getCharacteristicNid() == SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getNid()) {
                                statedRoleRels.add(r);
                            }
                        }
                        longNormRelGroups = new ArrayList<>();
                        HashSet<RelationshipVersionBI> statedInferredRels = findStatedInferredLongNormRoleRels(statedRoleRels, inferredRoleRels);
                        for (RelationshipVersionBI rel : inferredRoleRels){
                            if (rel.getGroup() == 0) {
                                DragPanelRel dragPanel = getRelComponent(rel, cpe, true);
                                if (statedInferredRels.contains(rel)) {
                                    dragPanel.setInheritedDuplicate();
                                }
                                activeLongNormRelPanels.add(dragPanel);
                            } else {
                                int group = rel.getGroup();
                                boolean add = true;
                                for (RelationshipGroupVersionBI rg : longNormRelGroups) {
                                   if (rg.getRelationshipGroupNumber() == group) {
                                        add = false;
                                    }
                                }
                                if (add) {
                                    for (RelationshipGroupVersionBI relGroup : (Ts.get().getConceptVersion(inferredCoordinate, cv.getConceptNid())).getRelationshipGroups()) {
                                        if (relGroup.getRelationshipGroupNumber() == group) {
                                            if (relGroup.getRelationshipsActive().iterator().next().getCharacteristicNid()
                                                    == SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid()) {
                                                longNormRelGroups.add(relGroup);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        for (RelationshipVersionBI rel : proximalPrimitives) {
                            DragPanelRel dragPanel = getRelComponent(rel, cpe, true);
                            activeLongNormRelPanels.add(dragPanel);
                        }
                    }
                    
                    boolean relHistoryIsShown = cpr.isShown(DragPanelComponentVersion.SubPanelTypes.HISTORY);
                    
                    cpr.setTemplateCount(0);
                    
                    boolean cprAdded = false;
                    
                    if (settings.getRelAssertionType().equals(RelAssertionType.SHORT_NORMAL_FORM)) {
                        for (DragPanelRel rc : activeShortNormRelPanels) {
                            if (stop) {
                                return;
                            }

                            if (!cprAdded) {
                                conceptPanel.add(cpr, gbc);
                                gbc.gridy++;
                                cprAdded = true;
                            }

                            setShowConflicts(rc.getComponentVersion(), rc);
                            seperatorComponents.add(rc);
                            cpr.addToggleComponent(rc);
                            conceptPanel.add(rc, gbc);
                            gbc.gridy++;
                            cpr.setAlertCount(cpr.alertCount += rc.getAlertSubpanelCount());
                            cpr.setRefexCount(cpr.refexCount += rc.getRefexSubpanelCount());
                            cpr.setHistoryCount(cpr.historyCount += rc.getHistorySubpanelCount());
                            cpr.setTemplateCount(cpr.templateCount += rc.getTemplateSubpanelCount());
                        }
                    } else if (settings.getRelAssertionType().equals(RelAssertionType.LONG_NORMAL_FORM)) {
                        for (DragPanelRel rc : activeLongNormRelPanels) {
                            if (stop) {
                                return;
                            }

                            if (!cprAdded) {
                                conceptPanel.add(cpr, gbc);
                                gbc.gridy++;
                                cprAdded = true;
                            }

                            setShowConflicts(rc.getComponentVersion(), rc);
                            seperatorComponents.add(rc);
                            cpr.addToggleComponent(rc);
                            conceptPanel.add(rc, gbc);
                            gbc.gridy++;
                            cpr.setAlertCount(cpr.alertCount += rc.getAlertSubpanelCount());
                            cpr.setRefexCount(cpr.refexCount += rc.getRefexSubpanelCount());
                            cpr.setHistoryCount(cpr.historyCount += rc.getHistorySubpanelCount());
                            cpr.setTemplateCount(cpr.templateCount += rc.getTemplateSubpanelCount());
                        }
                    } else {
                        if (settings.showStated()) {
                            for (DragPanelRel rc : activeStatedRelPanels) {
                                if (stop) {
                                    return;
                                }

                                if (!cprAdded) {
                                    conceptPanel.add(cpr, gbc);
                                    gbc.gridy++;
                                    cprAdded = true;
                                }

                                setShowConflicts(rc.getComponentVersion(), rc);
                                seperatorComponents.add(rc);
                                cpr.addToggleComponent(rc);
                                conceptPanel.add(rc, gbc);
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
                                    conceptPanel.add(cpr, gbc);
                                    gbc.gridy++;
                                    cprAdded = true;
                                }

                                setShowConflicts(rc.getComponentVersion(), rc);
                                rc.setVisible(relHistoryIsShown);
                                seperatorComponents.add(rc);
                                cpr.addToggleComponent(rc);
                                cpr.getInactiveComponentPanels().add(rc);
                                cpr.getRetiredPanels().add(rc);
                                conceptPanel.add(rc, gbc);
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
                                    conceptPanel.add(cpr, gbc);
                                    gbc.gridy++;
                                    cprAdded = true;
                                }

                                setShowConflicts(rc.getComponentVersion(), rc);
                                seperatorComponents.add(rc);
                                cpr.addToggleComponent(rc);
                                conceptPanel.add(rc, gbc);
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
                                    conceptPanel.add(cpr, gbc);
                                    gbc.gridy++;
                                    cprAdded = true;
                                }

                                setShowConflicts(rc.getComponentVersion(), rc);
                                rc.setVisible(relHistoryIsShown);
                                seperatorComponents.add(rc);
                                cpr.addToggleComponent(rc);
                                cpr.getInactiveComponentPanels().add(rc);
                                conceptPanel.add(rc, gbc);
                                gbc.gridy++;
                                cpr.setAlertCount(cpr.alertCount += rc.getAlertSubpanelCount());
                                cpr.setRefexCount(cpr.refexCount += rc.getRefexSubpanelCount());
                                cpr.setHistoryCount(cpr.historyCount += rc.getHistorySubpanelCount());
                                cpr.setTemplateCount(cpr.templateCount += rc.getTemplateSubpanelCount());
                            }
                        }
                    }
                    try {
                        if (settings.getRelAssertionType().equals(RelAssertionType.SHORT_NORMAL_FORM)) {
                            if (stop) {
                                return;
                            }

                            addRelGroups(shortNormRelGroups, cprAdded, cpr, gbc);
                        } else if (settings.getRelAssertionType().equals(RelAssertionType.LONG_NORMAL_FORM)) {
                            if (stop) {
                                return;
                            }

                            addRelGroups(longNormRelGroups, cprAdded, cpr, gbc);
                        }else {
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
                        }
                            
                    } catch (ContradictionException e) {
                        AceLog.getAppLog().alertAndLogException(e);
                    }
                    
                    if (templates.size() > 0) {
                        if (stop) {
                            return;
                        }
                        
                        CollapsePanel cptemplate = new CollapsePanel("aggregate extras", settings,
                                prefMap.get(PanelSection.EXTRAS), PanelSection.EXTRAS);
                        
                        cptemplate.addPanelsChangedActionListener(pcal);
                        cptemplate.setTemplateCount(templates.size());
                        cptemplate.setRefexCount(0);
                        cptemplate.setHistoryCount(0);
                        cptemplate.setAlertCount(0);
                        conceptPanel.add(cptemplate, gbc);
                        gbc.gridy++;
                        
                        for (Entry<SpecBI, Integer> entry : templates.entrySet()) {
                            if (stop) {
                                return;
                            }
                            
                            Class<?> entryClass = entry.getKey().getClass();
                            
                            if (RelationshipSpec.class.isAssignableFrom(entryClass)) {
                                RelationshipSpec spec = (RelationshipSpec) entry.getKey();
                                DragPanelRelTemplate template = getRelTemplate(spec);
                                
                                cptemplate.addToggleComponent(template);
                                conceptPanel.add(template, gbc);
                                cptemplate.getTemplatePanels().add(template);
                                gbc.gridy++;
                                cptemplate.setTemplateCount(cptemplate.templateCount++);
                            } else if (DescriptionSpec.class.isAssignableFrom(entryClass)) {
                                DescriptionSpec spec = (DescriptionSpec) entry.getKey();
                                DragPanelDescTemplate template = getDescTemplate(spec);
                                
                                cptemplate.addToggleComponent(template);
                                conceptPanel.add(template, gbc);
                                cptemplate.getTemplatePanels().add(template);
                                gbc.gridy++;
                                cptemplate.setTemplateCount(cptemplate.templateCount++);
                            }
                        }
                    }
                    
                    gbc.weighty = 1;
                    JPanel filler2 = new JPanel();
                    filler2.setBackground(Color.LIGHT_GRAY);
                    conceptPanel.add(filler2, gbc);
                } catch (IOException | TerminologyException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            } else {
                cView.getCvRenderer().title.setTitleEmpty();
            }
            
            if (stop) {
                return;
            }
            
            updateUncommitted();
            
            if (!settings.isNavigatorSetup()) {
                settings.getNavigator();
            }
            setupPathCheckboxPane();
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        
        if (stop) {
            return;
        }
        
        settings.getConfig().addPropertyChangeListener("commit", pcal);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (settings.getNavigator().getHistoryPanel() != null) {
                        settings.getNavigator().getHistoryPanel().updateHistoryLayout();
                    }
                } catch (IOException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            }
        });
        for(DragPanelDescription panel : activeDescriptionPanels){      
            if(System.getProperty(DragPanelDescription.FOCUSED_COMPONENT).equals(panel.getTextPane().getName())){
                boolean requestFocusInWindow = panel.getTextPane().requestFocusInWindow();
                panel.getTextPane().setCaretPosition(Integer.parseInt(System.getProperty(DragPanelDescription.FOCUSED_CARET)));
            }
        }
    }
    
    private HashSet<RelationshipVersionBI> findProximalPrimitveOLD(Collection<? extends RelationshipVersionBI> rel) throws IOException, ContradictionException{
        HashSet<RelationshipVersionBI> parentRels = new HashSet<>();
        for(RelationshipVersionBI r : rel){
            if(r.getCharacteristicNid() == SnomedMetadataRfx.getREL_CH_INFERRED_RELATIONSHIP_NID() 
                    && r.getTypeNid() == Snomed.IS_A.getLenient().getNid()){
                if (!Ts.get().getConcept(r.getTargetNid()).getConceptAttributes().getVersion(statedCoordinate).isDefined()) {
                    parentRels.add(r);
                } 
            }
        }
        if(!parentRels.isEmpty()){
            return parentRels;
        }else{
        //move up another level
            for(RelationshipVersionBI r : rel){
                if(r.getCharacteristicNid() == SnomedMetadataRfx.getREL_CH_INFERRED_RELATIONSHIP_NID() 
                    && r.getTypeNid() == Snomed.IS_A.getLenient().getNid()){
                    ConceptVersionBI concept = Ts.get().getConceptVersion(statedCoordinate, r.getTargetNid());
                    Collection<? extends RelationshipVersionBI> nextRels = concept.getRelationshipsOutgoingActiveIsa();
                    HashSet<RelationshipVersionBI> found = findProximalPrimitveOLD(nextRels);
                    parentRels.addAll(found);
                }
            }
        }

        return parentRels;
    }
    
    //STEP 1
    private HashSet<RelationshipVersionBI> getPrimitives(ConceptVersionBI concept) throws IOException, ContradictionException{//return concepts?
        HashSet<RelationshipVersionBI> primitives = new HashSet<>();
        for(RelationshipVersionBI rel : concept.getRelationshipsOutgoingActiveIsa()){
            if(rel.getCharacteristicNid() == SnomedMetadataRfx.getREL_CH_INFERRED_RELATIONSHIP_NID()){
                ConceptVersionBI targetConcept = Ts.get().getConceptVersion(inferredCoordinate, rel.getTargetNid());
                if (!targetConcept.getConceptAttributes().getVersion(inferredCoordinate).isDefined()) {
                    primitives.add(rel);
                }else{
                    HashSet<RelationshipVersionBI> found = getPrimitives(targetConcept);
                    primitives.addAll(found);
                }
            }
        }
        return primitives;
    }
    
    //STEP 2
    private HashSet<RelationshipVersionBI> removeDuplicatePrimitives(HashSet<RelationshipVersionBI> primitives){
        //duplicate if same target, already know that rels are inferred, Is-as
        HashSet<RelationshipVersionBI> uniquePrimitives = new HashSet<>();
        HashSet<Integer> targetNids = new HashSet<>();
        for(RelationshipVersionBI rel : primitives){
            if(!targetNids.contains(rel.getTargetNid())){
                targetNids.add(rel.getTargetNid());
                uniquePrimitives.add(rel);
            }
        }
        return uniquePrimitives;
    }
    
    //STEP 3
    private HashSet<RelationshipVersionBI> getProximalPrimitives(HashSet<RelationshipVersionBI> primitives) throws IOException, ContradictionException{ 
        //all are inferred, Is-a relationships
        HashSet<RelationshipVersionBI> proximalPrimitives = new HashSet<>(primitives);
        for(RelationshipVersionBI p1 : primitives){
            for(RelationshipVersionBI p2 : primitives){
                //if p1 is a child of p2, only keep p1
                if(p1 != p2 && Ts.get().isKindOf(p1.getTargetNid(), p2.getTargetNid(), inferredCoordinate)){ 
                    proximalPrimitives.remove(p2);
                }
            }
        }
        return proximalPrimitives;
    }
    
    //STEP 4
    private HashSet<RelationshipVersionBI> getRoleRelationships(int conceptNid) throws IOException, ContradictionException {
        ConceptVersionBI conceptVersion = Ts.get().getConceptVersion(inferredCoordinate, conceptNid);
        Collection<? extends RelationshipVersionBI> rels = conceptVersion.getRelationshipsOutgoingActive();
        HashSet<RelationshipVersionBI> roleRels = new HashSet<>();
        for (RelationshipVersionBI r : rels) {
            if (r.getTypeNid() != Snomed.IS_A.getLenient().getNid()
                    && r.getCharacteristicNid() == SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid()) {
                roleRels.add(r);
            }
        }
        return roleRels;
    }
    
    private HashSet<RelationshipVersionBI> findShortNormRoleRelsToDisplay(HashSet<RelationshipVersionBI> roleRels, HashSet<RelationshipVersionBI> proximalParents) throws IOException, ContradictionException, NoSuchAlgorithmException{
        //keep proximal primitive Is-a relationships + roles/role groups on concept that differentiate from proximal primitives
        HashSet<RelationshipVersionBI> relsToKeep = new HashSet<>();
        HashSet<UUID> parentRelHashes = new HashSet<>();
        //find parent rel hashes
        for (RelationshipVersionBI p : proximalParents) {
            ConceptVersionBI parent = Ts.get().getConceptVersion(inferredCoordinate, p.getTargetNid());
            Collection<? extends RelationshipVersionBI> rels = parent.getRelationshipsOutgoingActive();
            HashSet<RelationshipVersionBI> roleRelsParent = new HashSet<>();
            for (RelationshipVersionBI r : rels) { //add all role relationships from parent
                if (r.getTypeNid() != Snomed.IS_A.getLenient().getNid()) {
                    roleRelsParent.add(r);
                }
            }
            
            if (!roleRelsParent.isEmpty()) {
                for (RelationshipVersionBI rel : roleRelsParent) {
                    if (rel.getGroup() == 0) { //if not in a group, make hash of single role rel
                        UUID hash = Type5UuidFactory.get(Ts.get().getUuidPrimordialForNid(rel.getTypeNid()).toString()
                                + Ts.get().getUuidPrimordialForNid(rel.getTargetNid()).toString());
                        parentRelHashes.add(hash);
                    } else { //if in a group, make hash of entire rel group
                        int group = rel.getGroup();
                        ArrayList<String> hashes = new ArrayList<>();
                        String hash = null;
                        for (RelationshipVersionBI r : roleRelsParent) {
                            if (r.getGroup() == group && r.getCharacteristicNid() == rel.getCharacteristicNid()) {
                                hash = Ts.get().getUuidPrimordialForNid(r.getTypeNid()).toString()
                                        + Ts.get().getUuidPrimordialForNid(r.getTargetNid()).toString();
                                hashes.add(hash);
                            }
                        }
                        Collections.sort(hashes); //make sure role hashes are always in same order
                        String groupString = null;
                        for (String s : hashes) {
                            groupString = groupString + s;
                        }
                        UUID groupHash = Type5UuidFactory.get(groupString);
                        parentRelHashes.add(groupHash);
                    }
                }
            }
        }
        
        //make hashes for role rels from concept
        for (RelationshipVersionBI rel : roleRels) {
            if (rel.getGroup() == 0) { //if not in a group, make hash of single role rel
                UUID hash = Type5UuidFactory.get(Ts.get().getUuidPrimordialForNid(rel.getTypeNid()).toString()
                        + Ts.get().getUuidPrimordialForNid(rel.getTargetNid()).toString());
                if(!parentRelHashes.contains(hash)){ //keep rel, if rel is not found on parent
                    relsToKeep.add(rel);
                }
            } else { //if in a group, make hash of entire rel group
                int group = rel.getGroup();
                ArrayList<String> hashes = new ArrayList<>();
                HashSet<RelationshipVersionBI> relGroup = new HashSet<>();
                String hash = null;
                for (RelationshipVersionBI r : roleRels) {
                    if (r.getGroup() == group && r.getCharacteristicNid() == rel.getCharacteristicNid()) {
                        hash = Ts.get().getUuidPrimordialForNid(r.getTypeNid()).toString()
                                + Ts.get().getUuidPrimordialForNid(r.getTargetNid()).toString();
                        hashes.add(hash);
                        relGroup.add(r);
                    }
                }
                Collections.sort(hashes); //make sure role hashes are always in same order
                String groupString = null;
                for (String s : hashes) {
                    groupString = groupString + s;
                }
                UUID groupHash = Type5UuidFactory.get(groupString);
                if (!parentRelHashes.contains(groupHash)) { //keep rel group, if rel group is not found on parent
                    relsToKeep.addAll(relGroup);
                }
            }
        }
        return relsToKeep;
    }
    
    private HashSet<RelationshipVersionBI> findStatedInferredLongNormRoleRels(HashSet<RelationshipVersionBI> statedRoleRels, HashSet<RelationshipVersionBI> inferredRoleRels) 
            throws IOException, ContradictionException, NoSuchAlgorithmException {
        //find all rels that are inherited and also stated
        duplicateRels = new HashSet<>();
        HashSet<UUID> statedRelHashes = new HashSet<>();
        HashMap<UUID, RelationshipVersionBI> relToHashMap = new HashMap<>();
        HashMap<UUID, HashSet<RelationshipVersionBI>> relGroupToHashMap = new HashMap<>();

        for (RelationshipVersionBI rel : statedRoleRels) { //these are the stated rels, don't care about original rel only hash
            if (rel.getGroup() == 0) {
                UUID hash = Type5UuidFactory.get(Ts.get().getUuidPrimordialForNid(rel.getTypeNid()).toString()
                        + Ts.get().getUuidPrimordialForNid(rel.getTargetNid()).toString());
                statedRelHashes.add(hash);
            } else {
                int group = rel.getGroup();
                ArrayList<String> hashes = new ArrayList<>();

                String hash = null;
                for (RelationshipVersionBI r : statedRoleRels) {
                    if (r.getGroup() == group && r.getCharacteristicNid() == rel.getCharacteristicNid()) {
                        hash = Ts.get().getUuidPrimordialForNid(r.getTypeNid()).toString()
                                + Ts.get().getUuidPrimordialForNid(r.getTargetNid()).toString();
                        hashes.add(hash);
                    }
                }
                Collections.sort(hashes);
                String groupString = null;
                for (String s : hashes) {
                    groupString = groupString + s;
                }
                UUID groupHash = Type5UuidFactory.get(groupString);
                statedRelHashes.add(groupHash);
            }
        }
        
        for (RelationshipVersionBI rel : inferredRoleRels) { //these are the inferred rels, map rel to hash
            if (rel.getGroup() == 0) {
                UUID hash = Type5UuidFactory.get(Ts.get().getUuidPrimordialForNid(rel.getTypeNid()).toString()
                        + Ts.get().getUuidPrimordialForNid(rel.getTargetNid()).toString());
                relToHashMap.put(hash, rel);
            } else {
                int group = rel.getGroup();
                ArrayList<String> hashes = new ArrayList<>();

                String hash = null;
                HashSet<RelationshipVersionBI> relGroup = new HashSet<>();
                for (RelationshipVersionBI r : inferredRoleRels) {
                    if (r.getGroup() == group && r.getCharacteristicNid() == rel.getCharacteristicNid()) {
                        hash = Ts.get().getUuidPrimordialForNid(r.getTypeNid()).toString()
                                + Ts.get().getUuidPrimordialForNid(r.getTargetNid()).toString();
                        hashes.add(hash);
                        relGroup.add(r);
                    }
                }
                Collections.sort(hashes);
                String groupString = null;
                for (String s : hashes) {
                    groupString = groupString + s;
                }
                UUID groupHash = Type5UuidFactory.get(groupString);
                relGroupToHashMap.put(groupHash, relGroup);
            }
        }

        ConceptVersionBI conceptVersion = Ts.get().getConceptVersion(inferredCoordinate, cv.getConceptNid());
        Collection<? extends ConceptVersionBI> parentConcepts = conceptVersion.getRelationshipsOutgoingTargetConceptsActiveIsa();
        for (ConceptVersionBI parent : parentConcepts) {
            HashSet<RelationshipVersionBI> roleRelsParent = new HashSet<>();
            for (RelationshipVersionBI p : parent.getRelationshipsOutgoingActive()) { //these are the rels of the immediate parents (inherited)
                if (p.getTypeNid() != Snomed.IS_A.getLenient().getNid()) {
                    roleRelsParent.add(p);
                }
            }
            for (RelationshipVersionBI rel : roleRelsParent) { //these are the rels of the immediate parents (inherited)
                if (rel.getGroup() == 0) {
                    UUID hash = Type5UuidFactory.get(Ts.get().getUuidPrimordialForNid(rel.getTypeNid()).toString()
                            + Ts.get().getUuidPrimordialForNid(rel.getTargetNid()).toString());
                    if (statedRelHashes.contains(hash)) {
                        duplicateRels.add(relToHashMap.get(hash));
                    }
                } else {
                    int group = rel.getGroup();
                    ArrayList<String> hashes = new ArrayList<>();
                    String hash = null;
                    for (RelationshipVersionBI r : roleRelsParent) {
                        if (r.getGroup() == group && r.getCharacteristicNid() == rel.getCharacteristicNid()) {
                            hash = Ts.get().getUuidPrimordialForNid(r.getTypeNid()).toString()
                                    + Ts.get().getUuidPrimordialForNid(r.getTargetNid()).toString();
                            hashes.add(hash);
                        }
                    }
                    Collections.sort(hashes);
                    String groupString = null;
                    for (String s : hashes) {
                        groupString = groupString + s;
                    }
                    UUID groupHash = Type5UuidFactory.get(groupString);
                    if (statedRelHashes.contains(groupHash)) {
                        duplicateRels.addAll(relGroupToHashMap.get(groupHash));
                    }
                }
            }
        }

        return duplicateRels;
    }
    
    private void removeContradictions(Collection<? extends ComponentVersionBI> componentVersions) {
        List<ComponentVersionBI> extraVersions = new ArrayList<>();
        
        for (ComponentVersionBI componentVersion : componentVersions) {
            if (componentCountForConflict.containsKey(componentVersion.getNid())) {
                componentCountForConflict.get(componentVersion.getNid()).add(componentVersion.getStampNid());
                extraVersions.add(componentVersion);
            } else {
                List<Integer> stamp = new ArrayList<>();
                
                stamp.add(componentVersion.getStampNid());
                componentCountForConflict.put(componentVersion.getNid(), stamp);
            }
        }
        
        componentVersions.removeAll(extraVersions);
    }
    
    private void setupDescriptions(CountDownLatch latch, Collection<? extends I_DescriptionTuple> descs,
            Collection<DragPanelDescription> panels, CollapsePanel cp)
            throws TerminologyException, IOException {
        if (stop) {
            return;
        }
        
        SetupDescriptions setterUpper = new SetupDescriptions(latch, descs, panels, cp);
        
        ACE.threadPool.execute(setterUpper);
    }
    
    private void updateUncommitted() {
        // show subpanels which are uncomitted
        if (activeDescriptionPanels != null) {
            for (DragPanelDescription dc : activeDescriptionPanels) {
                if (stop) {
                    return;
                }
                
                for (JComponent ref : dc.getRefexSubpanels()) {
                    DragPanelExtension rp = (DragPanelExtension) ref;
                    
                    if (rp.getThingToDrag().isUncommitted()) {
                        rp.setVisible(true);
                    }
                }
            }
        }
        
        if (activeDescriptionPanels != null) {
            for (DragPanelDescription dc : activeDescriptionPanels) {
                if (stop) {
                    return;
                }
                DescriptionAnalogBI thingToDrag = dc.getThingToDrag();
                if (thingToDrag.isUncommitted()) {
                    dc.setVisible(true);
                }
            }
        }
        
        if (inactiveDescriptionPanels != null) {
            for (DragPanelDescription dc : inactiveDescriptionPanels) {
                if (stop) {
                    return;
                }
                DescriptionAnalogBI thingToDrag = dc.getThingToDrag();
                if (thingToDrag.isUncommitted()) {
                    dc.setVisible(true);
                }
            }
        }
        
        if (inactiveInferredRelPanels != null) {
            for (DragPanelRel rel : inactiveInferredRelPanels) {
                if (stop) {
                    return;
                }
                
                if (rel.getThingToDrag().isUncommitted()) {
                    rel.setVisible(true);
                }
            }
        }
        
        if (inactiveStatedRelPanels != null) {
            for (DragPanelRel rel : inactiveStatedRelPanels) {
                if (stop) {
                    return;
                }
                
                if (rel.getThingToDrag().isUncommitted()) {
                    rel.setVisible(true);
                }
            }
        }
        
        if (activeInferredRelPanels != null) {
            for (DragPanelRel rel : activeInferredRelPanels) {
                if (stop) {
                    return;
                }
                
                if (rel.getThingToDrag().isUncommitted()) {
                    rel.setVisible(true);
                }
            }
        }
        
        if (activeStatedRelPanels != null) {
            for (DragPanelRel rel : activeStatedRelPanels) {
                if (stop) {
                    return;
                }
                
                if (rel.getThingToDrag().isUncommitted()) {
                    rel.setVisible(true);
                }
            }
        }
        
        if (inactiveStatedRels != null) {
            for (I_RelTuple rel : inactiveStatedRels) {
                if ((rel.getGroup() > 0) && rel.isUncommitted()) {
                    List<JComponent> historyPanels = cpr.getHistoryPanels();
                    
                    for (JComponent panel : historyPanels) {
                        panel.setVisible(true);
                    }
                }
            }
        }
        
        if (inactiveInferredRels != null) {
            for (I_RelTuple rel : inactiveInferredRels) {
                if ((rel.getGroup() > 0) && rel.isUncommitted()) {
                    List<JComponent> historyPanels = cpr.getHistoryPanels();
                    
                    for (JComponent panel : historyPanels) {
                        panel.setVisible(true);
                    }
                }
            }
        }
    }
    
    protected void setupPathCheckboxPane() throws IOException, ContradictionException {
        if (paths == null) {
            return;
        }

        // Construct check box panel that lists the different paths, just above the main concept view
        if (cView.getSettings() == null || cView.getSettings().getNavigator() == null
                || cView.getSettings().getNavigator().getHistoryPanel() == null
                || cView.getSettings().getNavigator().getHistoryPanel().getPathCheckMap() == null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        setupHistoryElements();
                    } catch (ContradictionException | IOException ex) {
                        AceLog.getAppLog().alertAndLogException(ex);
                    }
                }
            });
        } else {
            setupHistoryElements();
        }
    }
    java.util.Timer t = new java.util.Timer();
    
    private void setupRels(CountDownLatch latch, Collection<? extends I_RelTuple> rels,
            Collection<DragPanelRel> panels, CollapsePanel cp, boolean inferred)
            throws TerminologyException, IOException {
        SetupRels setterUpper = new SetupRels(latch, rels, panels, cp, inferred);
        
        if (stop) {
            return;
        }
        
        ACE.threadPool.execute(setterUpper);
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

    //~--- get methods ---------------------------------------------------------
    public DragPanelConceptAttributes getConAttrComponent(ConceptAttributeAnalogBI conAttr,
            CollapsePanel parentCollapsePanel)
            throws TerminologyException, IOException {
        DragPanelConceptAttributes dragConAttrPanel = new DragPanelConceptAttributes(new GridBagLayout(), this,
                parentCollapsePanel, conAttr);
        
        addToPositionPanelMap(dragConAttrPanel);
        
        return dragConAttrPanel;
    }
    
    public DragPanelDescription getDescComponent(DescriptionAnalogBI desc, CollapsePanel parentCollapsePanel)
            throws TerminologyException, IOException {
        DragPanelDescription dragDescPanel = new DragPanelDescription(new GridBagLayout(), this,
                parentCollapsePanel, desc);
        
        addToPositionPanelMap(dragDescPanel);
        
        return dragDescPanel;
    }
    
    public DragPanelDescTemplate getDescTemplate(final DescriptionSpec desc)
            throws TerminologyException, IOException {
        DragPanelDescTemplate descPanel = new DragPanelDescTemplate(new GridBagLayout(), this, desc);
        
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
                getLabel(desc.getDescriptionTypeSpec().getStrict(config.getViewCoordinate()).getNid(), true);
        
        descPanel.add(typeLabel, gbc);
        typeLabel.addPropertyChangeListener("termComponent", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                try {
                    desc.setDescriptionTypeSpec(SpecFactory.get((I_GetConceptData) evt.getNewValue(),
                            config.getViewCoordinate()));
                } catch (IOException ex) {
                    Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
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
        textPane.setText(desc.getDescriptionText());
        descPanel.add(textPane, gbc);
        textPane.getDocument().addDocumentListener(new UpdateTextTemplateDocumentListener(textPane, desc,
                config));
        
        return descPanel;
    }
    
    public Collection<DragPanel.DropPanelActionManager> getDropPanelActionManagers() {
        if (dpamsRef == null) {
            dramsLock.lock();
            
            try {
                if (dpamsRef == null) {
                    AtomicReference<Collection<DragPanel.DropPanelActionManager>> tempRef =
                            new AtomicReference<>();
                    
                    tempRef.compareAndSet(null, new ConcurrentSkipListSet<DragPanel.DropPanelActionManager>());
                    dpamsRef = tempRef;
                }
            } finally {
                dramsLock.unlock();
            }
        }
        
        return dpamsRef.get();
    }
    
    JCheckBox getJCheckBox() {
        JCheckBox check = new JCheckBox();
        
        check.setFont(check.getFont().deriveFont(settings.getFontSize()));
        check.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
        
        return check;
    }
    
    public JLabel getJLabel(String text) {
        JLabel l = new JLabel(text);
        
        l.setFont(l.getFont().deriveFont(settings.getFontSize()));
        l.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
        
        return l;
    }
    
    private TermComponentLabel getLabel(int nid, boolean canDrop) throws TerminologyException, IOException {
        TermComponentLabel termLabel = new TermComponentLabel(LabelText.PREFERRED);
        
        termLabel.setLineWrapEnabled(true);
        termLabel.getDropTarget().setActive(canDrop);
        termLabel.setFixedWidth(150);
        termLabel.setFont(termLabel.getFont().deriveFont(settings.getFontSize()));
        termLabel.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
        termLabel.setTermComponent(Terms.get().getConcept(nid));
        
        return termLabel;
    }
    
    public ActionListener getPanelsChangedActionListener() {
        return pcal;
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
    
    public Map<PositionBI, Collection<DragPanelComponentVersion<?>>> getPositionPanelMap() {
        return positionPanelMap;
    }
    
    public DragPanelRel getRelComponent(RelationshipVersionBI r, CollapsePanel parentCollapsePanel,
            boolean inferred)
            throws TerminologyException, IOException, ContradictionException {
        DragPanelRel relPanel = new DragPanelRel(new GridBagLayout(), this, parentCollapsePanel, r, inferred);
        
        addToPositionPanelMap(relPanel);
        
        return relPanel;
    }
    
    public DragPanelRelGroup getRelGroupComponent(RelationshipGroupVersionBI group, CollapsePanel parentCollapsePanel)
            throws TerminologyException, IOException, ContradictionException {
        DragPanelRelGroup relGroupPanel = new DragPanelRelGroup(new GridBagLayout(), this, parentCollapsePanel,
                group);
        
        relGroupPanel.setupDrag(group);
        relGroupPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = group.getRelationships().size() + 2;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        Color color = new Color (86,201,27);
        JButton actionMenuButton = relGroupPanel.getActionMenuButton();
        actionMenuButton.setMinimumSize(new Dimension(16, 16));
        actionMenuButton.setPreferredSize(new Dimension(16, 16));
        actionMenuButton.setBackground(color);
        actionMenuButton.setOpaque(true);
        relGroupPanel.add(actionMenuButton, gbc);
        gbc.gridy++;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.weighty = 1;
        JLabel relGroupLabel = getJLabel(" ");
        relGroupLabel.setOpaque(true);
        relGroupLabel.setMinimumSize(new Dimension(16, 12));
        relGroupLabel.setPreferredSize(new Dimension(16, 12));
        relGroupPanel.setDropPopupInset(relGroupLabel.getPreferredSize().width);
        relGroupLabel.setBackground(color);
        relGroupPanel.add(relGroupLabel, gbc);
        gbc.gridy = 0;
        gbc.gridheight = 3;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.gridheight = 1;
        
        
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
        
        for (RelationshipVersionBI rv : group.getRelationshipsActiveAllVersions()) {
            activeRelIds.add(rv.getNid());
            
            DragPanelRel dpr = getRelComponent(rv, parentCollapsePanel, rv.isInferred());
            if (settings.getRelAssertionType().equals(RelAssertionType.LONG_NORMAL_FORM)) {
                if (duplicateRels.contains(rv)) {
                    dpr.setInheritedDuplicate();
                }                
            }
            setShowConflicts(rv, dpr);
            
            cprg.addToggleComponent(dpr);
            dpr.setInGroup(true);
            relGroupPanel.add(dpr, gbc);
            gbc.gridy++;
            cprg.setAlertCount(cprg.alertCount += dpr.getAlertSubpanelCount());
            cprg.setRefexCount(cprg.refexCount += dpr.getRefexSubpanelCount());
            cprg.setHistoryCount(cprg.historyCount += dpr.getHistorySubpanelCount());
            cprg.setTemplateCount(cprg.templateCount += dpr.getTemplateSubpanelCount());
        }
        
        boolean relHistoryIsShown = cpr.isShown(DragPanelComponentVersion.SubPanelTypes.HISTORY);
        
        HashSet<Integer> addedRels = new HashSet<>();
        for (RelationshipVersionBI rv : group.getRelationshipsAll()) {
            if (!activeRelIds.contains(rv.getNid())
                    && !addedRels.contains(rv.getNid())) {
                addedRels.add(rv.getNid());
                DragPanelRel dpr = getRelComponent(rv, parentCollapsePanel, rv.isInferred());
                setShowConflicts(rv, dpr);
                
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
    
    public DragPanelRelTemplate getRelTemplate(final RelationshipSpec spec) throws TerminologyException, IOException {
        DragPanelRelTemplate relPanel = new DragPanelRelTemplate(new GridBagLayout(), this, spec);
        
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
        
        TermComponentLabel typeLabel = getLabel(spec.getRelationshipTypeSpec().getStrict(statedCoordinate).getNid(), true);
        
        relPanel.add(typeLabel, gbc);
        typeLabel.addPropertyChangeListener("termComponent", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                try {
                    spec.setRelationshipTypeSpec(SpecFactory.get((I_GetConceptData) evt.getNewValue(),
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
        
        TermComponentLabel destLabel = getLabel(spec.getTargetSpec().getStrict(statedCoordinate).getNid(), true);
        
        relPanel.add(destLabel, gbc);
        destLabel.addPropertyChangeListener("termComponent", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                try {
                    spec.setTargetSpec(SpecFactory.get((I_GetConceptData) evt.getNewValue(),
                            config.getViewCoordinate()));
                } catch (IOException ex) {
                    Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        return relPanel;
    }
    
    public Map<Integer, JCheckBox> getRowToPathCheckMap() {
        return rowToPathCheckMap;
    }
    
    public Collection<DragPanel> getSeperatorComponents() {
        return seperatorComponents;
    }
    
    public ConceptViewSettings getSettings() {
        return settings;
    }

    //~--- set methods ---------------------------------------------------------
    private void setShowConflicts(ComponentVersionBI cv, DragPanelComponentVersion cvp) {
        if (sapsForConflict.isEmpty()) {
            return;
        }
        List<DragPanelExtension> refexSubpanels = cvp.getRefexSubpanels();
        for (DragPanelExtension rp : refexSubpanels) {
            RefexVersionBI<?> annot = rp.getThingToDrag();
            if (sapsForConflict.contains(annot.getStampNid())) {
                rp.showConflicts(sapsForConflict);
            }
        }
        List<DragPanelComponentVersion> hxSubpanels = cvp.getHistorySubpanels();
        for (DragPanelComponentVersion panel : hxSubpanels) {
            ComponentVersionBI component = (ComponentVersionBI) panel.getThingToDrag();
            if (sapsForConflict.contains(component.getStampNid())) {
                panel.showConflicts(sapsForConflict);
            }
        }
        if (sapsForConflict.contains(cv.getStampNid())) {
            cvp.showConflicts(sapsForConflict);
        }
    }
    
    private void setupHistoryElements() throws ContradictionException, IOException {
        JPanel pathCheckboxPanel = cView.getPathCheckboxPanel();
        pathCheckboxPanel.removeAll();
        pathCheckboxPanel.setLayout(new GridBagLayout());
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
        Map<PathBI, Boolean> pathShowCheckMap = cView.getSettings().getNavigator().getHistoryPanel().getPathCheckMap();
        for (PathBI path : paths) {
            if (stop) {
                return;
            }
            pathRowMap.put(path, row);
            ConceptVersionBI pathVersion = Ts.get().getConceptVersion(statedCoordinate, path.getConceptNid());
            JCheckBox pathCheck = getJCheckBox();
            pathCheck.addActionListener(new RefreshHistoryViewListener(path, pathShowCheckMap));
            if (settings.getNavigator().getDropSide() == ConceptViewSettings.SIDE.LEFT) {
                gbc.anchor = GridBagConstraints.NORTHWEST;
                pathCheck.setHorizontalTextPosition(SwingConstants.RIGHT);
                pathCheck.setHorizontalAlignment(SwingConstants.LEFT);
            } else {
                gbc.anchor = GridBagConstraints.NORTHEAST;
                pathCheck.setHorizontalTextPosition(SwingConstants.LEFT);
                pathCheck.setHorizontalAlignment(SwingConstants.RIGHT);
            }
            if (pathVersion.getDescriptionPreferred() != null) {
                pathCheck.setText(pathVersion.getDescriptionPreferred().getText());
            } else {
                pathCheck.setText(pathVersion.toString());
            }
            Boolean select = pathShowCheckMap.get(path);
            if (select == null) {
                select = true;
                pathShowCheckMap.put(path, select);
            }
            if (select) {
                pathCheck.setSelected(true);
            } else {
                pathCheck.setSelected(false);
            }
            rowToPathCheckMap.put(row, pathCheck);
            pathCheck.setVisible(cView.isHistoryShown());
            pathCheckboxPanel.add(pathCheck, gbc);
            gbc.gridy++;
            row++;
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    GuiUtil.tickle(cView.getSettings().getNavigator().getHistoryPanel().getParentHistoryPanel());
                } catch (IOException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            }
        });
    }

    //~--- inner classes -------------------------------------------------------
    private class GetActionsSwingWorker extends SwingWorker<Collection<Action>, Collection<Action>> {
        
        JButton actionMenuButton;
        RelationshipGroupVersionBI group;

        //~--- constructors -----------------------------------------------------
        public GetActionsSwingWorker(JButton actionMenuButton, RelationshipGroupVersionBI group) {
            this.actionMenuButton = actionMenuButton;
            this.group = group;
        }

        //~--- methods ----------------------------------------------------------
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
                
                if ((actions == null) || actions.isEmpty()) {
                    actionMenuButton.setEnabled(false);
                }
            } catch (InterruptedException | ExecutionException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }
    
    public class RefreshHistoryViewListener implements ActionListener {
        
        PathBI path;
        Map<PathBI, Boolean> pathShowCheckMap;
        
        private RefreshHistoryViewListener(PathBI path) {
            this.path = path;
        }
        
        private RefreshHistoryViewListener(PathBI path, Map<PathBI, Boolean> pathShowCheckMap) {
            this.path = path;
            this.pathShowCheckMap = pathShowCheckMap;
        }
        
        @Override
        public void actionPerformed(ActionEvent ae) {
            JCheckBox checkBox = (JCheckBox) ae.getSource();
            if (checkBox.isSelected()) {
                pathShowCheckMap.put(path, true);
            } else {
                pathShowCheckMap.put(path, false);
            }
            if (stop) {
                return;
            }
            
            if (settings.isNavigatorSetup()) {
                settings.getNavigator().updateHistoryPanel();
            }
        }
    }
    
    private class SetupDescriptions implements Runnable {
        
        CollapsePanel cp;
        Collection<? extends I_DescriptionTuple> descs;
        CountDownLatch latch;
        Collection<DragPanelDescription> panels;

        //~--- constructors -----------------------------------------------------
        public SetupDescriptions(CountDownLatch latch, Collection<? extends I_DescriptionTuple> descs,
                Collection<DragPanelDescription> panels, CollapsePanel cp) {
            this.latch = latch;
            this.descs = descs;
            this.panels = panels;
            this.cp = cp;
        }

        //~--- methods ----------------------------------------------------------
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
                } catch (TerminologyException | IOException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            }
            
            latch.countDown();
        }
    }
    
    private class SetupRels implements Runnable {
        
        CollapsePanel cp;
        boolean inferred;
        CountDownLatch latch;
        Collection<DragPanelRel> panels;
        Collection<? extends I_RelTuple> rels;

        //~--- constructors -----------------------------------------------------
        public SetupRels(CountDownLatch latch, Collection<? extends I_RelTuple> rels,
                Collection<DragPanelRel> panels, CollapsePanel cp, boolean inferred) {
            this.latch = latch;
            this.rels = rels;
            this.panels = panels;
            this.cp = cp;
            this.inferred = inferred;
        }

        //~--- methods ----------------------------------------------------------
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
                } catch (TerminologyException | IOException | ContradictionException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            }
            
            latch.countDown();
        }
    }
    
    private static class UpdateTextTemplateDocumentListener implements DocumentListener, ActionListener {
        
        boolean update = false;
        I_GetConceptData c;
        I_ConfigAceFrame config;
        DescriptionSpec desc;
        FixedWidthJEditorPane editorPane;
        Timer t;

        //~--- constructors -----------------------------------------------------
        public UpdateTextTemplateDocumentListener(FixedWidthJEditorPane editorPane, DescriptionSpec desc,
                I_ConfigAceFrame config)
                throws TerminologyException, IOException {
            super();
            this.editorPane = editorPane;
            this.desc = desc;
            this.config = config;
            t = new Timer(1000, this);
            t.start();
            c = Terms.get().getConcept(desc.getConceptSpec().getStrict(config.getViewCoordinate()).getNid());
        }

        //~--- methods ----------------------------------------------------------
        @Override
        public void actionPerformed(ActionEvent e) {
            if (update) {
                update = false;
                desc.setDescText(editorPane.extractText());
            }
        }
        
        @Override
        public void changedUpdate(DocumentEvent e) {
            update = true;
        }
        
        @Override
        public void insertUpdate(DocumentEvent e) {
            update = true;
        }
        
        @Override
        public void removeUpdate(DocumentEvent e) {
            update = true;
        }
    }
    
    private void sortDescriptions() throws TerminologyException, ContradictionException, IOException {
        ArrayList<DragPanelDescription> temp = new ArrayList<>();
        DragPanelDescription fsnPanel = null;
        DragPanelDescription prefUS = null;
        DragPanelDescription prefGB = null;
        DragPanelDescription prefNL = null;
        DragPanelDescription prefSE = null;
        DragPanelDescription prefDK = null;
        List<DragPanelDescription> prefXX = new ArrayList<>();
        List<DragPanelDescription> tempUS = new ArrayList<>();
        List<DragPanelDescription> tempGB = new ArrayList<>();
        List<DragPanelDescription> tempNL = new ArrayList<>();
        List<DragPanelDescription> tempSE = new ArrayList<>();
        List<DragPanelDescription> tempDK = new ArrayList<>();
        List<DragPanelDescription> tempXX = new ArrayList<>();
        ConceptChronicleBI cc = (ConceptChronicleBI) layoutConcept;
        ConceptVersionBI cv = cc.getVersion(statedCoordinate);
        DescriptionVersionBI fsn = cv.getDescriptionFullySpecified();
        Collection<? extends DescriptionVersionBI> prefs = cv.getDescriptionsPreferredActive();
        HashSet<Integer> prefNids = new HashSet<>();
        for (DescriptionVersionBI d : prefs) {
            Collection<? extends RefexVersionBI<?>> annots = d.getAnnotationMembersActive(statedCoordinate, SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getLenient().getNid());
            for (RefexVersionBI a : annots) {
                RefexNidVersionBI ri = (RefexNidVersionBI) a;
                if (ri.getNid1() == SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid()) {
                    prefNids.add(d.getNid());
                }
            }
            annots = d.getAnnotationMembersActive(statedCoordinate, SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getLenient().getNid());
            for (RefexVersionBI a : annots) {
                RefexNidVersionBI ri = (RefexNidVersionBI) a;
                if (ri.getNid1() == SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid()) {
                    prefNids.add(d.getNid());
                }
            }
            if (Ts.get().hasUuid(RefsetAux.DA_REFEX.getUuids()[0])) {
                annots = d.getAnnotationMembersActive(statedCoordinate, RefsetAux.DA_REFEX.getLenient().getNid());
                for (RefexVersionBI a : annots) {
                    RefexNidVersionBI ri = (RefexNidVersionBI) a;
                    if (ri.getNid1() == SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid()) {
                        prefNids.add(d.getNid());
                    }
                }
            }
            if (Ts.get().hasUuid(RefsetAux.NL_REFEX.getUuids()[0])) {
                annots = d.getAnnotationMembersActive(statedCoordinate, RefsetAux.NL_REFEX.getLenient().getNid());
                for (RefexVersionBI a : annots) {
                    RefexNidVersionBI ri = (RefexNidVersionBI) a;
                    if (ri.getNid1() == SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid()) {
                        prefNids.add(d.getNid());
                    }
                }

            }
            if (Ts.get().hasUuid(RefsetAux.SV_REFEX.getUuids()[0])) {
                annots = d.getAnnotationMembersActive(statedCoordinate, RefsetAux.SV_REFEX.getLenient().getNid());
                for (RefexVersionBI a : annots) {
                    RefexNidVersionBI ri = (RefexNidVersionBI) a;
                    if (ri.getNid1() == SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid()) {
                        prefNids.add(d.getNid());
                    }
                }

            }
        }
        for (DragPanelDescription dc : activeDescriptionPanels) {
            if (dc.getDraggedThing().getNid() != fsn.getNid()) {
                if (dc.getDraggedThing().hasAnnotationMemberActive(statedCoordinate, SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getLenient().getNid())) {
                    if (prefNids.contains(dc.getDraggedThing().getNid())) {
                        prefUS = dc;
                    } else {
                        tempUS.add(dc);
                    }
                } else if (dc.getDraggedThing().hasAnnotationMemberActive(statedCoordinate, SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getLenient().getNid())) {
                    if (prefNids.contains(dc.getDraggedThing().getNid())) {
                        prefGB = dc;
                    } else {
                        tempGB.add(dc);
                    }
                } else if (dc.getDraggedThing().getLang().equals("nl")) {
                    if (prefNids.contains(dc.getDraggedThing().getNid())) {
                        prefNL = dc;
                    } else {
                        tempNL.add(dc);
                    }
                } else if (dc.getDraggedThing().getLang().equals("sv")) {
                    if (prefNids.contains(dc.getDraggedThing().getNid())) {
                        prefSE = dc;
                    } else {
                        tempSE.add(dc);
                    }
                } else if (dc.getDraggedThing().getLang().equals("da")) {
                    if (prefNids.contains(dc.getDraggedThing().getNid())) {
                        prefDK = dc;
                    } else {
                        tempDK.add(dc);
                    }
                } else if (dc.getDraggedThing().getLang().equals("da")) {
                    if (prefNids.contains(dc.getDraggedThing().getNid())) {
                        prefXX.add(dc);
                    } else {
                        tempXX.add(dc);
                    }
                }
            } else {
                fsnPanel = dc;
            }
        }
        int index = 0;
        if(fsnPanel != null){
            temp.add(index++, fsnPanel);
        }
        if(prefUS != null){
            temp.add(index++, prefUS);
        }
        if (!tempUS.isEmpty()) {
            Collections.sort(tempUS, new DescPanelComparator());
            for (DragPanelDescription dc : tempUS) {
                temp.add(index++, dc);
            }
        }
        if (prefGB != null) {
            temp.add(index++, prefGB);
        }
        if (!tempGB.isEmpty()) {
            Collections.sort(tempGB, new DescPanelComparator());
            for (DragPanelDescription dc : tempGB) {
                temp.add(index++, dc);
            }
        }
        if (prefDK != null) {
            temp.add(index++, prefDK);
        }
        if (!tempDK.isEmpty()) {
            Collections.sort(tempDK, new DescPanelComparator());
            for (DragPanelDescription dc : tempDK) {
                temp.add(index++, dc);
            }
        }
        if (prefNL != null) {
            temp.add(index++, prefNL);
        }
        if (!tempNL.isEmpty()) {
            Collections.sort(tempNL, new DescPanelComparator());
            for (DragPanelDescription dc : tempNL) {
                temp.add(index++, dc);
            }
        }
        if (prefSE != null) {
            temp.add(index++, prefSE);
        }
        if (!tempSE.isEmpty()) {
            Collections.sort(tempSE, new DescPanelComparator());
            for (DragPanelDescription dc : tempSE) {
                temp.add(index++, dc);
            }
        }
        if (!prefXX.isEmpty()) {
            Collections.sort(prefXX, new DescPanelComparator());
            for (DragPanelDescription dc : prefXX) {
                temp.add(index++, dc);
            }
        }
        if (!tempXX.isEmpty()) {
            Collections.sort(tempXX, new DescPanelComparator());
            for (DragPanelDescription dc : tempXX) {
                temp.add(index++, dc);
            }
        }
        
        if (activeDescriptionPanels.size() == temp.size()) {
            activeDescriptionPanels = temp;
        } else {
            System.out.println("** Sorted description panels do not match original size. **");
        }
    }
    
    public class DescPanelComparator implements Comparator<DragPanelDescription> {

        @Override
        public int compare(DragPanelDescription o1, DragPanelDescription o2) {
            return o1.getDraggedThing().getNid() - o2.getDraggedThing().getNid();
        }
    }
}
