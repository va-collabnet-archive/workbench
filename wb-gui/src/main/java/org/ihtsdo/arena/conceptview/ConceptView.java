package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.dnd.DragMonitor;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.arena.ScrollablePanel;
import org.ihtsdo.arena.context.action.DropActionPanel;
import org.ihtsdo.arena.drools.EditPanelKb;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.drools.facts.Context;
import org.ihtsdo.tk.drools.facts.FactFactory;
import org.ihtsdo.tk.spec.SpecBI;
import org.intsdo.tk.drools.manager.DroolsExecutionManager;

public class ConceptView extends JPanel {
    
    private final ConceptViewRenderer cvRenderer;
    
    private void setupPrefMap() {
        prefMap.put(PanelSection.CONCEPT, new CollapsePanelPrefs());
        prefMap.put(PanelSection.DESC, new CollapsePanelPrefs());
        prefMap.put(PanelSection.REL, new CollapsePanelPrefs());
        prefMap.put(PanelSection.REL_GRP, new CollapsePanelPrefs());
        prefMap.put(PanelSection.EXTRAS, new CollapsePanelPrefs());
        
        
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
        
        private class DragStarter extends SwingWorker<Object, Object> {
            
            @Override
            protected Object doInBackground() throws Exception {
                LayoutManager layout = new FlowLayout(FlowLayout.LEADING, 5, 5);
                JPanel sfp = new ScrollablePanel(layout);
                if (gridLayout) {
                    layout = new GridLayout(0, 1, 5, 5);
                    sfp = new JPanel(layout);
                }
                
                sfp.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                JPanel dropPanel = new JPanel(new GridLayout(1, 1));
                JScrollPane sfpScroller = new JScrollPane(sfp);
                sfpScroller.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                sfpScroller.setAutoscrolls(true);
                sfpScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                sfpScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                if (dropPanel == null) {
                    return null;
                }
                dropPanel.add(sfpScroller);
                DropPanelActionManager.this.dropPanel = dropPanel;
                DropPanelActionManager.this.sfpScroller = sfpScroller;
                DropPanelActionManager.this.sfp = sfp;
                dragging = true;
                timer.start();
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    get();
                    if (!DragMonitor.isDragging()) {
                        timer.stop();
                        dragging = false;
                        panelAdded = false;
                        if (dropPanel != null) {
                            dropPanel.setVisible(false);
                            JLayeredPane rootLayers = ConceptView.this.getRootPane().getLayeredPane();
                            if (rootLayers != null) {
                                rootLayers.remove(dropPanel);
                            }
                        }
                        dropPanel = null;
                    }
                } catch (InterruptedException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                } catch (ExecutionException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            }
        }
        private Timer timer;
        private boolean dragging = false;
        private JComponent dropPanel = null;
        private boolean panelAdded = false;
        private JScrollPane sfpScroller;
        private boolean gridLayout = true;
        private JPanel sfp;
        private Collection<JComponent> addedDropComponents = new ArrayList<JComponent>();
        private DropPanelProxy dpp;
        
        public DropPanelActionManager() {
            super();
            dpp = new DropPanelProxy(this);
            ConceptView.this.addHierarchyListener(this.dpp);
            timer = new Timer(250, this);
        }
        /* (non-Javadoc)
         * @see org.ihtsdo.arena.conceptview.I_DispatchDragStatus#dragStarted()
         */
        
        @Override
        public void dragStarted() {
            (new DragStarter()).execute();
        }
        /* (non-Javadoc)
         * @see org.ihtsdo.arena.conceptview.I_DispatchDragStatus#dragFinished()
         */
        
        @Override
        public void dragFinished() {
            timer.stop();
            dragging = false;
            setDragPanelVisible(false);
            if (dropPanel != null) {
                JLayeredPane rootLayers = ConceptView.this.getRootPane().getLayeredPane();
                rootLayers.remove(dropPanel);
                dropPanel = null;
            }
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
                                scrollerModel.setValue(scrollerModel.getValue() - 20);
                            }
                        } else if (dropPanel.getHeight() - mouseLocationForDropPanel.y < 10) {
                            BoundedRangeModel scrollerModel = sfpScroller.getVerticalScrollBar().getModel();
                            scrollerModel.setExtent(1);
                            if (scrollerModel.getValue() < scrollerModel.getMaximum()) {
                                scrollerModel.setValue(scrollerModel.getValue() + 20);
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
                    dropPanel.setVisible(false);
                }
            }
        }
    }
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private I_ConfigAceFrame config;
    private ConceptViewSettings settings;
    
    public ConceptViewSettings getSettings() {
        return settings;
    }
    private EditPanelKb kb;
    
    public EditPanelKb getKb() {
        return kb;
    }
    private I_DispatchDragStatus dropPanelMgr;
    private Collection<Action> actionList =
            Collections.synchronizedCollection(new ArrayList<Action>());
    private I_GetConceptData concept;
    
    public enum PanelSection {
        
        CONCEPT,
        DESC, REL, REL_GRP, EXTRAS
    };
    private Map<PanelSection, CollapsePanelPrefs> prefMap =
            new EnumMap<PanelSection, CollapsePanelPrefs>(PanelSection.class);
    
    public Map<PanelSection, CollapsePanelPrefs> getPrefMap() {
        return prefMap;
    }
    
    public I_GetConceptData getConcept() {
        return concept;
    }
    private Collection<JComponent> dropComponents =
            Collections.synchronizedList(new ArrayList<JComponent>());
    private boolean historyShown = false;
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
        setupPrefMap();
        dropPanelMgr = new DropPanelActionManager();
    }    
    
    public Map<Integer, JCheckBox> getRowToPathCheckMap() {
        return cvLayout.getRowToPathCheckMap();
    }
    
    public Map<PositionBI, Collection<DragPanelComponentVersion<?>>> getPositionPanelMap() {
        return cvLayout.getPositionPanelMap();
    }
    
    public void addHostListener(PropertyChangeListener l) {
        if (settings != null && settings.getHost() != null) {
            settings.getHost().addPropertyChangeListener("termComponent", l);
        }
    }
    
    public JPanel getHistoryPanel() {
        return cvLayout.getHistoryPanel();
    }
    
    public boolean isHistoryShown() {
        return historyShown;
    }
    
    public void setHistoryShown(boolean historyShown) {
        this.historyShown = historyShown;
        for (JComponent hc : getRowToPathCheckMap().values()) {
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
    
    private void addCommitListener(ConceptViewSettings settings) {
        settings.getConfig().addPropertyChangeListener("commit",
                new PropertyChangeListener() {
                    
                    Long lastPropId = Long.MIN_VALUE;
                    
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (evt.getPropagationId() == null
                                || (Long) evt.getPropagationId() > lastPropId) {
                            try {
                                if (ConceptView.this.concept == null) {
                                    layoutConcept(ConceptView.this.concept);
                                } else {
                                    if (ConceptView.this.concept.isCanceled()) {
                                        getSettings().getHost().setTermComponent(null);
                                    } else {
                                        layoutConcept(ConceptView.this.concept);
                                    }
                                }
                                
                            } catch (IOException ex) {
                                AceLog.getAppLog().alertAndLogException(ex);
                            }
                            if (evt.getPropagationId() != null) {
                                lastPropId = (Long) evt.getPropagationId();
                            }
                        }
                        
                    }
                });
    }
    ConceptViewLayout cvLayout;
    
    public ConceptViewLayout getCvLayout() {
        return cvLayout;
    }
    
    public ActionListener getPanelsChangedActionListener() {
        return cvLayout.getPanelsChangedActionListener();
    }
    private final Set<ComponentVersionBI> changedVersionSelections = new HashSet<ComponentVersionBI>();
    
    public Set<ComponentVersionBI> getChangedVersionSelections() {
        return changedVersionSelections;
    }
    
    long lastLayoutSequence = Long.MIN_VALUE;
    public void resetLastLayoutSequence() {
        lastLayoutSequence = Long.MIN_VALUE;
    }
    
    public void layoutConcept(I_GetConceptData concept) throws IOException {
        if (concept != null) {
            if (lastLayoutSequence == concept.getLastModificationSequence() &&
                concept == this.concept) {
                return;
            }
            lastLayoutSequence = concept.getLastModificationSequence();
        }
        
        removeAll();
        if (concept == null
                || this.concept == null
                || this.concept.equals(concept) == false
                || this.concept.isCanceled()) {
            changedVersionSelections.clear();
        }
        this.concept = concept;
        if (cvLayout != null) {
            cvLayout.stop();
        }
        this.settings.getNavigator().resetHistoryPanel();
        cvLayout = new ConceptViewLayout(this, concept);
        cvLayout.execute();
        getCvRenderer().updateCancelAndCommit();
        
        
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
    
    protected Collection<Action> getKbActions(Object thingToDrop) {
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
        return cvLayout.getPathRowMap();
    }
    
    public TreeSet<PositionBI> getPositionOrderedSet() {
        return cvLayout.getPositionOrderedSet();
    }
    
    public ConceptViewRenderer getCvRenderer() {
        return cvRenderer;
    }
    
    public I_ConfigAceFrame getConfig() {
        return config;
    }
    
    public Collection<DragPanel> getSeperatorComponents() {
        return cvLayout.getSeperatorComponents();
    }
}
