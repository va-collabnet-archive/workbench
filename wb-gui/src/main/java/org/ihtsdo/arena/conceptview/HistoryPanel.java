/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.hash.Hashcode;
import org.ihtsdo.util.swing.GuiUtil;

/**
 *
 * @author kec
 */
public class HistoryPanel {

    private static final int HISTORY_LABEL_WIDTH = 11;

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
            if (button.isSelected()) {
                if (originalButtonSelections.contains(button)) {
                    // back to original state
                } else {
                    // unimplemented change
                    button.setBackground(Color.YELLOW);
                    button.setOpaque(true);
                    changedSelections.add(button);
                }
            } else {
                button.setBackground(versionPanel.getBackground());
                button.setOpaque(false);
                changedSelections.remove(button);
            }
            if (changedSelections.size() > 0) {
                applyButton.setVisible(true);
                applyButton.setEnabled(true);
            } else {
                applyButton.setVisible(false);
                applyButton.setEnabled(false);
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
    JPanel topHistoryPanel = new JPanel(null);
    JPanel historyHeaderPanel = new JPanel(null);
    JScrollPane historyHeaderScroller = new JScrollPane(historyHeaderPanel);
    JPanel versionPanel = new JPanel(null);
    JScrollPane versionScroller = new JScrollPane(versionPanel);
    Map<PositionBI, JCheckBox> positionCheckMap = new HashMap<PositionBI, JCheckBox>();
    Map<JCheckBox, PositionBI> checkPositionMap = new HashMap<JCheckBox, PositionBI>();
    Map<JCheckBox, List<JComponent>> checkComponentMap = new HashMap<JCheckBox, List<JComponent>>();
    List<JCheckBox> positionCheckList = new ArrayList<JCheckBox>();
    Map<JCheckBox, JLabel> positionHeaderCheckLabelMap = new HashMap<JCheckBox, JLabel>();
    Map<JCheckBox, JLabel> positionVersionPanelCheckLabelMap = new HashMap<JCheckBox, JLabel>();
    int hxWidth = 0;
    private final Map<PositionBI, Collection<ComponentVersionDragPanel<?>>> positionPanelMap;
    private final Map<Integer, ButtonGroup> nidGroupMap = new HashMap<Integer, ButtonGroup>();
    private final Map<NidSapNid, JRadioButton> nidSapNidButtonMap = new HashMap<NidSapNid, JRadioButton>();
    private final Map<JRadioButton, Integer> buttonSapMap = new HashMap<JRadioButton, Integer>();
    private final Map<JRadioButton, Set<JComponent>> buttonPanelSetMap =
            new HashMap<JRadioButton, Set<JComponent>>();
    private final Map<JRadioButton, ComponentVersionBI> buttonVersionMap =
            new HashMap<JRadioButton, ComponentVersionBI>();
    private final Set<JRadioButton> originalButtonSelections = new HashSet<JRadioButton>();
    private final Set<JRadioButton> changedSelections = new HashSet<JRadioButton>();
    private final SelectedVersionChangedListener svcl = new SelectedVersionChangedListener();
    private final JButton applyButton = new JButton();

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
            versionPanel.setSize(hxWidth, view.getHeight());
            versionPanel.setPreferredSize(versionPanel.getSize());
            BoundedRangeModel historyScrollModel =
                    versionScroller.getVerticalScrollBar().getModel();
            historyScrollModel.setMaximum(eventModel.getMaximum());
            historyScrollModel.setMinimum(eventModel.getMinimum());
            historyScrollModel.setValue(eventModel.getValue());
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
            } catch (IOException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    public HistoryPanel(ConceptView view, JScrollPane historyScroller) throws IOException {
        this.view = view;
        positionPanelMap = view.getPositionPanelMap();
        applyButton.setToolTipText("apply selected version changes");
        applyButton.setIcon(new ImageIcon(
                HistoryPanel.class.getResource("/16x16/plain/magic-wand.png")));
        applyButton.addActionListener(new ApplyVersionChangesListener());

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
        versionPanel.setSize(hxWidth, view.getHeight());
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

    private ButtonGroup getButtonGroup(int componentNid) {
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
            ButtonGroup group = getButtonGroup(version.getNid());
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
        Collection<ComponentVersionBI> positionVersions = version.getChronicle().getVersions();
        ButtonGroup group = getButtonGroup(version.getNid());
        for (ComponentVersionBI positionVersion : positionVersions) {
            processPosition(group, version, positionVersion, dragPanel);
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
                if (viewVersion == positionVersion) {
                    originalButtonSelections.add(button);
                    button.setSelected(true);
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
        historyHeaderPanel.setPreferredSize(historyHeaderPanel.getSize());
        topHistoryPanel.add(historyHeaderScroller);
        historyHeaderScroller.setLocation(0, 0);
        historyHeaderScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        historyHeaderScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        historyHeaderScroller.setSize(topHistoryPanel.getWidth(), view.getHistoryPanel().getHeight());
        historyHeaderScroller.setLocation(0, 0);
        topHistoryPanel.add(versionScroller);
        versionScroller.setSize(topHistoryPanel.getWidth(), view.getParent().getHeight() + insetAdjustment);
        versionScroller.setLocation(0, historyHeaderPanel.getHeight() + 1);
        versionScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        versionPanel.setLocation(0, 0);
        versionPanel.setSize(hxWidth, view.getHeight());
        versionPanel.setPreferredSize(versionPanel.getPreferredSize());

        topHistoryPanel.add(applyButton);
        applyButton.setBorder(BorderFactory.createEmptyBorder());
        applyButton.setSize(30, 20);
        applyButton.setLocation((topHistoryPanel.getWidth() / 2)
                - (applyButton.getWidth() / 2),
                topHistoryPanel.getHeight() - applyButton.getHeight() - 1);
        syncVerticalLayout();
    }
    private static final int xStartLoc = 5;

    private void redoLayout() {

        int currentX = xStartLoc;
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
            positionCheck.setVisible(rowCheck.isSelected());
            positionCheck.setLocation(currentX, positionCheck.getY());
            for (JComponent componentInColumn : checkComponentMap.get(positionCheck)) {
                boolean visible = positionCheck.isVisible();
                if (visible && componentInColumn instanceof JRadioButton) {
                    JRadioButton radioButton = (JRadioButton) componentInColumn;
                    visible = isPanelVisibleForButton(radioButton);
                }
                componentInColumn.setVisible(visible);
                componentInColumn.setLocation(currentX, componentInColumn.getY());
            }
            if (positionCheck.isVisible()) {
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
            if (comp.isVisible()) {

                JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
                sep.setSize(versionPanel.getWidth(), 6);

                int yLoc = comp.getY();
                Component parentPanel = comp.getParent();
                while (parentPanel != null && parentPanel != view) {
                    yLoc += parentPanel.getY();
                    parentPanel = parentPanel.getParent();
                }

                sep.setLocation(0, yLoc - 6);
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
        hxWidth = locX + 400;
    }
}
