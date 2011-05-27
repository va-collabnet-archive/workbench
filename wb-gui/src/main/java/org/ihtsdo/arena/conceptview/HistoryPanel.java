/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.hash.Hashcode;
import org.ihtsdo.util.swing.GuiUtil;

/**
 *
 * @author kec
 */
public class HistoryPanel {

    private static final int HISTORY_LABEL_WIDTH = 11;
    private final ConceptNavigator navigator;

    private JLabel setupLabel(String hxString, int locX) {
        JLabel historyLabel = new JLabel("");
        historyLabel.setVisible(false);
        historyLabel.setBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        historyLabel.setLocation(locX, 0);
        historyLabel.setSize(HISTORY_LABEL_WIDTH, 1000);
        historyLabel.setBorder(
                BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY),
                new HistoryBorder(
                BorderFactory.createEmptyBorder(),
                hxString,
                new Font("monospaced", Font.PLAIN, 12),
                Color.BLACK)));
        return historyLabel;
    }

    private void syncVerticalLayout() {
        BoundedRangeModel eventModel =
                view.getCvRenderer().getConceptScrollPane().
                getVerticalScrollBar().getModel();
        BoundedRangeModel historyScrollModel =
                versionScroller.getVerticalScrollBar().getModel();
        historyScrollModel.setMaximum(eventModel.getMaximum());
        historyScrollModel.setMinimum(eventModel.getMinimum());
        historyScrollModel.setValue(eventModel.getValue());
        versionPanel.setLocation(0, -eventModel.getValue());
    }

    private class SelectedVersionChangedListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent ce) {
            JRadioButton button = (JRadioButton) ce.getSource();
            ComponentVersionBI version = buttonVersionMap.get(button);
            int changedCount = changedSelections.size();
            if (button.isSelected()) {
                if (originalButtonSelections.contains(button)) {
                    // back to original state
                    if (version.getTime() == Long.MAX_VALUE) {
                        button.setBackground(Color.YELLOW);
                        button.setOpaque(true);
                    }

                } else {
                    // unimplemented change
                    button.setBackground(Color.ORANGE);
                    button.setOpaque(true);
                    changedSelections.add(button);
                    view.getChangedVersionSelections().add(version);
                }
            } else {
                button.setBackground(versionPanel.getBackground());
                button.setOpaque(false);
                changedSelections.remove(button);
                view.getChangedVersionSelections().remove(version);
            }
            if (changedCount != changedSelections.size()) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (changedSelections.size() > 0) {
                            navigator.getImplementButton().setEnabled(true);
                        } else {
                            navigator.getImplementButton().setEnabled(false);
                        }
                    }
                });
            }
        }
    }

    private class UpdateHistoryBorder implements ActionListener {

        JLabel hxLabel;
        JLabel versionHxLabel;

        public UpdateHistoryBorder(JLabel historyLabel, JLabel versionHxLabel) {
            this.hxLabel = historyLabel;
            this.versionHxLabel = versionHxLabel;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            JCheckBox check = (JCheckBox) ae.getSource();
            if (check.isSelected()) {
                BoundedRangeModel historyScrollModel =
                        versionScroller.getVerticalScrollBar().getModel();
                hxLabel.setVisible(true);
                hxLabel.setLocation(hxLabel.getX(),
                        historyScrollModel.getValue());
                versionHxLabel.setVisible(true);
                versionHxLabel.setLocation(hxLabel.getX(),
                        historyScrollModel.getValue());
            } else {
                hxLabel.setVisible(false);
                versionHxLabel.setVisible(false);
            }
            redoLayout();
        }
    }
    ConceptView view;
    JPanel topHistoryPanel = new JPanel(new GridBagLayout());
    JPanel historyHeaderPanel = new JPanel(null);
    JScrollPane historyHeaderScroller = new JScrollPane(historyHeaderPanel);
    JPanel versionPanel;
    JScrollPane versionScroller;
    Map<PositionBI, JCheckBox> positionCheckMap = new HashMap<PositionBI, JCheckBox>();
    Map<JCheckBox, PositionBI> checkPositionMap = new HashMap<JCheckBox, PositionBI>();
    Map<JCheckBox, List<JComponent>> checkComponentMap = new HashMap<JCheckBox, List<JComponent>>();
    List<JCheckBox> positionCheckList = new ArrayList<JCheckBox>();
    Map<JCheckBox, JLabel> positionHeaderCheckLabelMap = new HashMap<JCheckBox, JLabel>();
    Map<JCheckBox, JLabel> positionVersionPanelCheckLabelMap = new HashMap<JCheckBox, JLabel>();
    int hxWidth = 0;
    private final Map<PositionBI, Collection<ComponentVersionDragPanel<?>>> positionPanelMap;
    private final Map<Integer, ButtonGroup> nidGroupMap = new HashMap<Integer, ButtonGroup>();
    private final Map<Integer, ButtonGroup> inferredNidGroupMap = new HashMap<Integer, ButtonGroup>();
    private final Map<NidSapNid, JRadioButton> nidSapNidButtonMap = new HashMap<NidSapNid, JRadioButton>();
    private final Map<JRadioButton, Integer> buttonSapMap = new HashMap<JRadioButton, Integer>();
    private final Map<JRadioButton, Set<JComponent>> buttonPanelSetMap =
            new HashMap<JRadioButton, Set<JComponent>>();
    private final Map<JRadioButton, ComponentVersionBI> buttonVersionMap =
            new HashMap<JRadioButton, ComponentVersionBI>();
    private final Set<JRadioButton> originalButtonSelections = new HashSet<JRadioButton>();
    private final Set<JRadioButton> changedSelections = new HashSet<JRadioButton>();
    private final SelectedVersionChangedListener svcl = new SelectedVersionChangedListener();

    private static class NidSapNid {

        int nid;
        int sapNid;

        public NidSapNid(int nid, int sapNid) {
            this.nid = nid;
            this.sapNid = sapNid;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NidSapNid) {
                NidSapNid another = (NidSapNid) o;
                return nid == another.nid && sapNid
                        == another.sapNid;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Hashcode.compute(new int[]{nid, sapNid});
        }
    }

    private boolean isPanelVisibleForButton(JRadioButton button) {
        Set<JComponent> panels = buttonPanelSetMap.get(button);
        if (panels == null) {
            return false;
        }
        boolean visible = false;
        for (JComponent panel : panels) {
            if (panel.isVisible()) {
                return true;
            }
        }
        return visible;
    }

    private void putPanelInButtonMap(JRadioButton button, JComponent panel) {
        Set<JComponent> panels = buttonPanelSetMap.get(button);
        if (panels == null) {
            panels = new HashSet<JComponent>();
            buttonPanelSetMap.put(button, panels);
        }
        panels.add(panel);
    }

    private class HistoryHierarchyListener implements HierarchyListener {

        @Override
        public void hierarchyChanged(HierarchyEvent he) {
            syncVerticalLayout();
        }
    }

    private class TopChangeListener implements ComponentListener {

        @Override
        public void componentResized(ComponentEvent ce) {
            resizeIfNeeded();
        }

        @Override
        public void componentMoved(ComponentEvent ce) {
            //
        }

        @Override
        public void componentShown(ComponentEvent ce) {
            syncVerticalLayout();
        }

        @Override
        public void componentHidden(ComponentEvent ce) {
            //
        }
    }

    private class VerticalScrollActionListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent ce) {
            BoundedRangeModel eventModel = (BoundedRangeModel) ce.getSource();
            for (JLabel hxLabel : positionVersionPanelCheckLabelMap.values()) {
                hxLabel.setLocation(hxLabel.getX(), eventModel.getValue());
            }
            versionPanel.setSize(Math.max(hxWidth, 
                        ConceptViewSettings.NAVIGATOR_WIDTH - 6), eventModel.getMaximum());
            versionPanel.setPreferredSize(versionPanel.getSize());
            BoundedRangeModel historyScrollModel =
                    versionScroller.getVerticalScrollBar().getModel();
            historyScrollModel.setMaximum(eventModel.getMaximum());
            historyScrollModel.setMinimum(eventModel.getMinimum());
            historyScrollModel.setValue(eventModel.getValue());
            versionPanel.setLocation(0, -eventModel.getValue());
        }
    }

    private class HorizonatalScrollActionListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent ce) {
            BoundedRangeModel eventModel = (BoundedRangeModel) ce.getSource();
            BoundedRangeModel historyScrollModel =
                    historyHeaderScroller.getHorizontalScrollBar().getModel();
            historyScrollModel.setMaximum(eventModel.getMaximum());
            historyScrollModel.setMinimum(eventModel.getMinimum());
            historyScrollModel.setValue(eventModel.getValue());
        }
    }

    private class ApplyVersionChangesListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            try {
                for (JRadioButton button : changedSelections) {
                    EditCoordinate ec = view.getConfig().getEditCoordinate();
                    ComponentVersionBI cv = buttonVersionMap.get(button);
                    for (int pathNid : ec.getEditPaths()) {
                        ((AnalogGeneratorBI) cv).makeAnalog(
                                cv.getStatusNid(),
                                ec.getAuthorNid(),
                                pathNid, Long.MAX_VALUE);
                    }
                }
                Ts.get().addUncommitted(view.getConcept());
                reset();
                navigator.getImplementButton().setEnabled(false);
            } catch (IOException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }
    private ApplyVersionChangesListener avcl = new ApplyVersionChangesListener();

    private class ScrollableHxPanel extends JPanel implements Scrollable {

        public ScrollableHxPanel() {
            super(null);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(Math.max(hxWidth, 
                        ConceptViewSettings.NAVIGATOR_WIDTH - 6), view.getHeight());
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 1;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 1;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return false;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
        
    }
    public HistoryPanel(ConceptView view, JScrollPane historyScroller,
            ConceptNavigator navigator) throws IOException {
        this.view = view;
        versionPanel = new ScrollableHxPanel();
        versionScroller = new JScrollPane(versionPanel);

        positionPanelMap = view.getPositionPanelMap();
        this.navigator = navigator;
        navigator.getImplementButton().addActionListener(avcl);

        TreeSet<PositionBI> positionOrderedSet = view.getPositionOrderedSet();
        if (view.getPathRowMap() != null && positionOrderedSet != null) {
            setupHeader(view);
            combineHistoryPanels();
            setupVersionPanel(positionPanelMap);
            topHistoryPanel.addComponentListener(new TopChangeListener());
            historyScroller.setViewportView(topHistoryPanel);
        }
        historyHeaderPanel.setSize(hxWidth, view.getHistoryPanel().getHeight());
        historyHeaderPanel.setPreferredSize(historyHeaderPanel.getSize());
        historyHeaderPanel.setMinimumSize(historyHeaderPanel.getSize());
        versionPanel.setSize(Math.max(hxWidth, 
                        ConceptViewSettings.NAVIGATOR_WIDTH - 6), view.getHeight());
        versionPanel.setPreferredSize(versionPanel.getSize());
        versionPanel.addHierarchyListener(new HistoryHierarchyListener());
        versionScroller.getHorizontalScrollBar().getModel().
                addChangeListener(new HorizonatalScrollActionListener());
        ((JScrollPane) view.getParent().getParent()).getVerticalScrollBar().
                getModel().addChangeListener(new VerticalScrollActionListener());
        redoLayout();
    }

    private void reset() {
        nidSapNidButtonMap.clear();
        buttonSapMap.clear();
        buttonPanelSetMap.clear();
        originalButtonSelections.clear();
        changedSelections.clear();
        view.getChangedVersionSelections().clear();
    }

    public void resizeIfNeeded() {
        try {
            combineHistoryPanels();
            setupVersionPanel(positionPanelMap);
            redoLayout();
            GuiUtil.tickle(historyHeaderPanel);
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void setupVersionPanel(Map<PositionBI, Collection<ComponentVersionDragPanel<?>>> positionPanelMap) throws IOException {
        for (Collection<ComponentVersionDragPanel<?>> panelSet : positionPanelMap.values()) {
            for (ComponentVersionDragPanel<?> dragPanel : panelSet) {
                processPanel(dragPanel);
            }
        }

        for (JLabel versionPanelLabel : positionVersionPanelCheckLabelMap.values()) {
            versionPanelLabel.setVisible(false);
            versionPanel.add(versionPanelLabel);
        }
    }

    private ButtonGroup getButtonGroup(int componentNid, boolean inferred) {
        if (inferred) {
            ButtonGroup group = inferredNidGroupMap.get(componentNid);
            if (group == null) {
                group = new ButtonGroup();
                inferredNidGroupMap.put(componentNid, group);
            }
            return group;
        }
        ButtonGroup group = nidGroupMap.get(componentNid);
        if (group == null) {
            group = new ButtonGroup();
            nidGroupMap.put(componentNid, group);
        }
        return group;
    }

    private void processPanel(ComponentVersionDragPanel<?> dragPanel) throws IOException {
        ComponentVersionBI version = dragPanel.getComponentVersion();
        List<ComponentVersionDragPanel<?>> versionPanels =
                getVersionPanels(dragPanel);
        List<ComponentVersionDragPanel<?>> refexPanels =
                getRefexPanels(dragPanel);
        if (versionPanels.isEmpty()) {
            processAllPositions(version, dragPanel);
        } else {
            boolean inferred = false;
            if (version instanceof RelationshipVersionBI) {
                inferred = ((RelationshipVersionBI) version).isInferred();
            }
            ButtonGroup group = getButtonGroup(version.getNid(), inferred);
            processPosition(group,
                    version, version, dragPanel);
            for (ComponentVersionDragPanel panel : versionPanels) {
                processPosition(group,
                        version, panel.getComponentVersion(), panel);
            }
        }
        for (ComponentVersionDragPanel refexPanel : refexPanels) {
            processPanel(refexPanel);
        }
    }

    private void processAllPositions(ComponentVersionBI version, ComponentVersionDragPanel<?> dragPanel) throws IOException {
        if (version instanceof RelationshipVersionBI) {
            RelationshipVersionBI rv = (RelationshipVersionBI) version;
            Collection<RelationshipVersionBI> positionVersions = rv.getChronicle().getVersions();
            ButtonGroup group = getButtonGroup(version.getNid(), rv.isInferred());
            for (RelationshipVersionBI positionVersion : positionVersions) {
                if (positionVersion.isInferred() == rv.isInferred()) {
                    processPosition(group, version, positionVersion, dragPanel);
                }
            }
        } else {
            Collection<ComponentVersionBI> positionVersions = version.getChronicle().getVersions();
            ButtonGroup group = getButtonGroup(version.getNid(), false);
            for (ComponentVersionBI positionVersion : positionVersions) {
                processPosition(group, version, positionVersion, dragPanel);
            }
        }
    }

    private void processPosition(ButtonGroup group,
            ComponentVersionBI viewVersion, ComponentVersionBI positionVersion,
            ComponentVersionDragPanel<?> dragPanel) {
        try {
            boolean add = false;
            PositionBI p = positionVersion.getPosition();
            int sapNid = positionVersion.getSapNid();
            NidSapNid nidSapNidKey = new NidSapNid(viewVersion.getNid(), sapNid);
            JRadioButton button = nidSapNidButtonMap.get(nidSapNidKey);
            if (button == null) {
                button = new JRadioButton();
                button.addChangeListener(svcl);
                buttonVersionMap.put(button, positionVersion);
                if (positionVersion.getTime() == Long.MAX_VALUE) {
                    // unimplemented change
                    button.setBackground(Color.YELLOW);
                    button.setOpaque(true);
                }
                if (view.getChangedVersionSelections().contains(positionVersion)) {
                    button.setBackground(Color.ORANGE);
                    button.setOpaque(true);
                    button.setSelected(true);
                    changedSelections.add(button);
                }


                if (viewVersion == positionVersion) {
                    originalButtonSelections.add(button);
                    if (group.getSelection() == null) {
                        button.setSelected(true);
                    }

                }
                nidSapNidButtonMap.put(nidSapNidKey, button);
                buttonSapMap.put(button, sapNid);
                add = true;
                ConceptVersionBI author =
                        Ts.get().getConceptVersion(
                        view.getConfig().getViewCoordinate(),
                        positionVersion.getAuthorNid());
                ConceptVersionBI status =
                        Ts.get().getConceptVersion(
                        view.getConfig().getViewCoordinate(),
                        positionVersion.getStatusNid());
                StringBuilder sb = new StringBuilder();
                sb.append("<html>");
                sb.append(positionVersion.toUserString(Ts.get().getSnapshot(view.getConfig().getViewCoordinate())));
                sb.append("<br>");
                if (status.getPreferredDescription() != null) {
                    sb.append(status.getPreferredDescription().getText());
                } else {
                    sb.append(status.toString());
                }
                sb.append("<br>");
                if (author.getPreferredDescription() != null) {
                    sb.append(author.getPreferredDescription().getText());
                } else {
                    sb.append(author.toString());
                }
                sb.append("<br>");
                sb.append(p.toString());
                if (viewVersion == positionVersion) {
                    sb.append("<br>");
                    sb.append("<font color='#ff0000'>");
                    sb.append("latest on view</font>");
                }
                sb.append("</html>");
                button.setToolTipText(sb.toString());
            }

            boolean enableButton = true;
            if (positionVersion instanceof RelationshipVersionBI) {
                RelationshipVersionBI rv = (RelationshipVersionBI) positionVersion;
                if (rv.isInferred()) {
                    enableButton = false;
                }
            }
            button.setEnabled(enableButton);

            putPanelInButtonMap(button, dragPanel);
            if (group != null) {
                if (add) {
                    group.add(button);
                }
            }
            int yLoc = dragPanel.getY();
            Component parentPanel = dragPanel.getParent();
            while (parentPanel != null && parentPanel != view) {
                yLoc += parentPanel.getY();
                parentPanel = parentPanel.getParent();
            }
            JCheckBox positionCheck = positionCheckMap.get(p);
            if (positionCheck != null) {
                button.setVisible(positionCheck.isVisible());
                checkComponentMap.get(positionCheck).add(button);
                button.setLocation(positionCheck.getX(), yLoc);
                if (add) {
                    versionPanel.add(button);
                }
                button.setSize(button.getPreferredSize());
            }
        } catch (Throwable ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private List<ComponentVersionDragPanel<?>> getVersionPanels(
            ComponentVersionDragPanel<?> dragPanel) throws IOException {
        List<ComponentVersionDragPanel<?>> versionPanels =
                new ArrayList<ComponentVersionDragPanel<?>>();
        for (Component comp : dragPanel.getComponents()) {
            if (comp instanceof ComponentVersionDragPanel) {
                ComponentVersionDragPanel cvdp =
                        (ComponentVersionDragPanel) comp;
                if (cvdp.isVisible()) {
                    if (cvdp.getComponentVersion().getChronicle().equals(
                            dragPanel.getComponentVersion().getChronicle())) {
                        versionPanels.add(cvdp);
                    }
                }
            }
        }
        return versionPanels;
    }

    private List<ComponentVersionDragPanel<?>> getRefexPanels(
            ComponentVersionDragPanel<?> dragPanel) {
        List<ComponentVersionDragPanel<?>> versionPanels =
                new ArrayList<ComponentVersionDragPanel<?>>();
        for (Component comp : dragPanel.getComponents()) {
            if (comp.isVisible() && comp instanceof ComponentVersionDragPanel) {
                ComponentVersionDragPanel cvdp =
                        (ComponentVersionDragPanel) comp;
                if (!cvdp.getComponentVersion().getChronicle().equals(
                        dragPanel.getComponentVersion().getChronicle())) {
                    versionPanels.add(cvdp);
                }
            }
        }
        return versionPanels;
    }
    private static final int insetAdjustment = 3;

    private void combineHistoryPanels() {
        topHistoryPanel.removeAll();
        for (JRadioButton button : buttonSapMap.keySet()) {
            button.setVisible(false);
        }

        historyHeaderPanel.setSize(hxWidth,
                view.getHistoryPanel().getHeight() - insetAdjustment);
        historyHeaderPanel.setBorder(BorderFactory.createEmptyBorder());
        historyHeaderPanel.setPreferredSize(historyHeaderPanel.getSize());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        topHistoryPanel.add(historyHeaderScroller, gbc);
        historyHeaderScroller.setLocation(0, 0);
        historyHeaderScroller.setBorder(BorderFactory.createEmptyBorder());
        historyHeaderScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        historyHeaderScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        historyHeaderScroller.setSize(topHistoryPanel.getWidth(),
                view.getHistoryPanel().getHeight());
        historyHeaderScroller.setLocation(0, 0);
        gbc.gridy++;
        topHistoryPanel.add(versionScroller, gbc);
        versionScroller.setSize(topHistoryPanel.getWidth(), view.getParent().getHeight() + insetAdjustment);
        versionScroller.setLocation(0, historyHeaderPanel.getHeight() + 1);
        versionScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        versionScroller.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
        versionPanel.setLocation(0, 0);
        versionPanel.setSize(Math.max(hxWidth, 
                        ConceptViewSettings.NAVIGATOR_WIDTH - 6), view.getParent().getParent().getHeight()
                + (view.getHistoryPanel().getHeight() + view.getHistoryPanel().getY()));
        versionPanel.setPreferredSize(versionPanel.getPreferredSize());
//        topHistoryPanel.setSize(hxWidth, 
//                historyHeaderPanel.getHeight() + versionPanel.getHeight());
        syncVerticalLayout();
    }
    private static final int xStartLoc = 5;

    private void redoLayout() {

        int currentX = xStartLoc;
        //int yAdjust = -historyHeaderPanel.getHeight();
        next:
        for (JCheckBox positionCheck : positionCheckList) {
            PositionBI position = checkPositionMap.get(positionCheck);
            if (position == null) {
                continue next;
            }
            Integer row = view.getPathRowMap().get(position.getPath());
            if (row == null) {
                continue next;
            }

            JCheckBox rowCheck = view.getRowToPathCheckMap().get(row);
            boolean showPosition = rowCheck.isSelected();
            positionCheck.setVisible(showPosition);
            positionCheck.setLocation(currentX, positionCheck.getY());
            for (JComponent componentInColumn : checkComponentMap.get(positionCheck)) {
                if (componentInColumn instanceof JRadioButton) {
                    JRadioButton radioButton = (JRadioButton) componentInColumn;
                    if (showPosition) {
                        if (isPanelVisibleForButton(radioButton)) {
                            componentInColumn.setVisible(true);
                            int maxY = 0;
                            Point location = new Point();
                            for (JComponent panel : buttonPanelSetMap.get(radioButton)) {
                                if (panel.getParent() != null && panel.isVisible()) {
                                    location = panel.getLocation();
                                    SwingUtilities.convertPointToScreen(location, panel.getParent());
                                    maxY = Math.max(maxY, location.y);
                                }
                            }
                            location.y = maxY;
                            SwingUtilities.convertPointFromScreen(location, componentInColumn.getParent());
                            componentInColumn.setLocation(currentX, location.y);
                        } else {
                            componentInColumn.setVisible(false);
                        }
                    }
                } else {
                    componentInColumn.setVisible(showPosition);
                }
                componentInColumn.setLocation(currentX, componentInColumn.getY());
            }
            if (showPosition) {
                currentX += positionCheck.getWidth();
            }

            JLabel positionLabel = positionHeaderCheckLabelMap.get(positionCheck);
            if (positionLabel.isVisible()) {
                positionLabel.setLocation(currentX, positionLabel.getY());
                JLabel versionPanelLabel =
                        positionVersionPanelCheckLabelMap.get(positionCheck);
                if (versionPanelLabel != null) {
                    versionPanelLabel.setVisible(true);
                    versionPanelLabel.setLocation(currentX, versionPanelLabel.getY());
                }
                if (positionCheck.isVisible()) {
                    currentX += positionLabel.getWidth();
                } else {
                    positionLabel.setVisible(false);
                }
            }
        }
        redoGrid();
    }
    private List<JSeparator> seperators = new ArrayList<JSeparator>();

    private void redoGrid() {
        for (JSeparator sep : seperators) {
            versionPanel.remove(sep);
        }
        seperators.clear();
        for (JComponent comp : view.getSeperatorComponents()) {
            if (comp.getParent() != null) {
                JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
                sep.setSize(Math.max(versionPanel.getWidth(), 
                        ConceptViewSettings.NAVIGATOR_WIDTH - 3), 6);

                Point location = comp.getLocation();
                SwingUtilities.convertPointToScreen(location, comp.getParent());
                SwingUtilities.convertPointFromScreen(location, versionPanel.getParent());

                sep.setLocation(0, location.y - sep.getHeight());
                versionPanel.add(sep);
                seperators.add(sep);
            }
        }
    }

    private void setupHeader(ConceptView view) {
        TreeSet<PositionBI> positionOrderedSet = view.getPositionOrderedSet();
        if (view.getPathRowMap() == null
                || view.getRowToPathCheckMap() == null
                || positionOrderedSet == null) {
            return;
        }
        int locX = xStartLoc;
        for (PositionBI p : positionOrderedSet) {
            assert p != null;
            assert view.getPathRowMap() != null;
            assert view.getRowToPathCheckMap() != null;
            assert p.getPath() != null;
            Integer row = view.getPathRowMap().get(p.getPath());
            if (row == null) {
                continue;
            }
            JCheckBox rowCheck = view.getRowToPathCheckMap().get(row);
            JCheckBox positionCheck = view.getJCheckBox();
            positionCheck.setVisible(rowCheck.isSelected());
            positionCheckList.add(positionCheck);
            checkComponentMap.put(positionCheck, new ArrayList<JComponent>());
            positionCheckMap.put(p, positionCheck);
            checkPositionMap.put(positionCheck, p);
            positionCheck.setLocation(locX, rowCheck.getLocation().y);
            positionCheck.setSize(positionCheck.getPreferredSize());
            positionCheck.setToolTipText(p.toString());
            historyHeaderPanel.add(positionCheck);
            if (positionCheck.isVisible()) {
                locX += positionCheck.getWidth();
            }

            JLabel historyLabel = setupLabel("", locX);
            positionHeaderCheckLabelMap.put(positionCheck, historyLabel);

            historyHeaderPanel.add(historyLabel);

            JLabel versionHistoryLabel = setupLabel(p.toString(), 0);
            positionVersionPanelCheckLabelMap.put(positionCheck, versionHistoryLabel);

            positionCheck.addActionListener(
                    new UpdateHistoryBorder(historyLabel, versionHistoryLabel));
        }
        hxWidth = locX;
    }
}
