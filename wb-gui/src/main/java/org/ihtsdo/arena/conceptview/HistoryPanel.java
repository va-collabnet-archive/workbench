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
import java.util.List;
import java.util.Map;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
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
    Map<PositionPanelKey, JRadioButton> panelRadioMap = new HashMap<PositionPanelKey, JRadioButton>();
    Map<JRadioButton, PositionPanelKey> radioPanelMap = new HashMap<JRadioButton, PositionPanelKey>();
    int hxWidth = 0;
    Map<PathBI, Integer> pathRowMap;
    Map<Integer, JCheckBox> rowToPathCheckMap;
    private final Map<PositionBI, Collection<ComponentVersionDragPanel<?>>> positionPanelMap;

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

    public HistoryPanel(ConceptView view, JScrollPane historyScroller) throws IOException {
        this.view = view;
        pathRowMap = view.getPathRowMap();
        rowToPathCheckMap = view.getRowToPathCheckMap();
        positionPanelMap = view.getPositionPanelMap();
        TreeSet<PositionBI> positionOrderedSet = view.getPositionOrderedSet();
        if (pathRowMap != null && positionOrderedSet != null) {
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

    private void processPanel(ComponentVersionDragPanel<?> dragPanel) throws IOException {
        ComponentVersionBI version = dragPanel.getComponentVersion();
        List<ComponentVersionDragPanel<?>> versionPanels =
                getVersionPanels(dragPanel);
        List<ComponentVersionDragPanel<?>> refexPanels =
                getRefexPanels(dragPanel);
        if (versionPanels.isEmpty()) {
            processAllPositions(version, dragPanel);
        } else {
            ButtonGroup group = new ButtonGroup();
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
        ButtonGroup group = new ButtonGroup();
        for (ComponentVersionBI positionVersion : positionVersions) {
            processPosition(group, version, positionVersion, dragPanel);
        }
    }

    private class PositionPanelKey {

        PositionBI p;
        ComponentVersionDragPanel<?> dragPanel;

        public PositionPanelKey(PositionBI p, ComponentVersionDragPanel<?> dragPanel) {
            this.p = p;
            this.dragPanel = dragPanel;
        }

        public ComponentVersionDragPanel<?> getDragPanel() {
            return dragPanel;
        }

        public PositionBI getP() {
            return p;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof PositionPanelKey) {
                PositionPanelKey ppk = (PositionPanelKey) o;
                return this.p.equals(ppk.p)
                        && this.dragPanel.equals(ppk.dragPanel);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Hashcode.compute(new int[]{p.hashCode(), dragPanel.hashCode()});
        }
    }

    private void processPosition(ButtonGroup group,
            ComponentVersionBI viewVersion, ComponentVersionBI positionVersion,
            ComponentVersionDragPanel<?> dragPanel) {
        try {
            boolean add = false;
            PositionBI p = positionVersion.getPosition();
            PositionPanelKey ppk = new PositionPanelKey(p, dragPanel);
            JRadioButton button = panelRadioMap.get(ppk);
            if (button == null) {
                button = new JRadioButton();
                panelRadioMap.put(ppk, button);
                radioPanelMap.put(button, ppk);
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
            if (group != null) {
                if (add) {
                    group.add(button);
                }
            }
            if (viewVersion == positionVersion) {
                button.setSelected(true);
                button.setBackground(Color.LIGHT_GRAY);
                button.setOpaque(true);
            }
            int yLoc = dragPanel.getY();
            Component parentPanel = dragPanel.getParent();
            while (parentPanel != null && parentPanel != view) {
                yLoc += parentPanel.getY();
                parentPanel = parentPanel.getParent();
            }
            JCheckBox positionCheck = positionCheckMap.get(p);
            button.setVisible(positionCheck.isVisible());
            checkComponentMap.get(positionCheck).add(button);
            button.setLocation(positionCheck.getX(), yLoc);
            if (add) {
                versionPanel.add(button);
            }
            button.setSize(button.getPreferredSize());
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
                } else {
                    PositionPanelKey ppk = new PositionPanelKey(cvdp.getComponentVersion().getPosition(), dragPanel);
                    JRadioButton button = panelRadioMap.get(ppk);
                    if (button != null) {
                        button.setVisible(false);
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
            } else {
                JRadioButton button = panelRadioMap.get(comp);
                if (button != null) {
                    button.setVisible(false);
                }
            }
        }
        return versionPanels;
    }
    private static final int insetAdjustment = 3;

    private void combineHistoryPanels() {
        topHistoryPanel.removeAll();
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
            int row = pathRowMap.get(position.getPath());
            JCheckBox rowCheck = rowToPathCheckMap.get(row);
            positionCheck.setVisible(rowCheck.isSelected());
            positionCheck.setLocation(currentX, positionCheck.getY());
            for (JComponent componentInColumn : checkComponentMap.get(positionCheck)) {
                boolean visible = positionCheck.isVisible();
                if (visible && componentInColumn instanceof JRadioButton) {
                    JRadioButton radioButton = (JRadioButton) componentInColumn;
                    PositionPanelKey ppk = radioPanelMap.get(radioButton);
                    if (ppk != null && ppk.dragPanel != null) {
                        if (!ppk.dragPanel.isVisible()) {
                            visible = false;
                        }
                    }
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
    }

    private void setupHeader(ConceptView view) {
        TreeSet<PositionBI> positionOrderedSet = view.getPositionOrderedSet();
        if (pathRowMap == null
                || rowToPathCheckMap == null
                || positionOrderedSet == null) {
            return;
        }
        int locX = xStartLoc;
        for (PositionBI p : positionOrderedSet) {
            assert p != null;
            assert pathRowMap != null;
            assert rowToPathCheckMap != null;
            assert p.getPath() != null;
            Integer row = pathRowMap.get(p.getPath());
            if (row == null) {
                continue;
            }
            JCheckBox rowCheck = rowToPathCheckMap.get(row);
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
