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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.util.swing.GuiUtil;

/**
 *
 * @author kec
 */
public class HistoryPanel {

    private JLabel setupLabel(String hxString, int locX) {
        JLabel historyLabel = new JLabel("");
        historyLabel.setVisible(false);
        historyLabel.setBorder(
        BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        historyLabel.setLocation(locX, 0);
        historyLabel.setSize(10, 1000);
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
    Map<JCheckBox, List<JComponent>> checkComponentMap 
            = new HashMap<JCheckBox, List<JComponent>>();
    List<JCheckBox> positionCheckList = new ArrayList<JCheckBox>();
    Map<JCheckBox, JLabel> positionHeaderCheckLabelMap = new HashMap<JCheckBox, JLabel>();
    Map<JCheckBox, JLabel> positionVersionPanelCheckLabelMap = new HashMap<JCheckBox, JLabel>();
    int hxWidth = 0;
        Map<PathBI, Integer> pathRowMap;
        Map<Integer, JCheckBox> rowToPathCheckMap;

    private class TopChangeListener implements ComponentListener {

        @Override
        public void componentResized(ComponentEvent ce) {
            resizeIfNeeded();
        }

        @Override
        public void componentMoved(ComponentEvent ce) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void componentShown(ComponentEvent ce) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void componentHidden(ComponentEvent ce) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private class VerticalScrollActionListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent ce) {
            BoundedRangeModel eventModel = (BoundedRangeModel) ce.getSource();
            for (JLabel hxLabel: positionVersionPanelCheckLabelMap.values()) {
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

    private HistoryPanel(ConceptView view, JScrollPane historyScroller) throws IOException {
        this.view = view;
        pathRowMap = view.getPathRowMap();
        rowToPathCheckMap = view.getRowToPathCheckMap();
        Map<PositionBI, Collection<ComponentVersionDragPanel<?>>> positionPanelMap = view.getPositionPanelMap();
        TreeSet<PositionBI> positionOrderedSet = view.getPositionOrderedSet();
        if (pathRowMap != null && positionOrderedSet != null) {
            setupHeader(view);
            combineHistoryPanels();
            setupVersionPanel(pathRowMap, positionOrderedSet,
                    positionPanelMap);
            topHistoryPanel.addComponentListener(new TopChangeListener());
            historyScroller.setViewportView(topHistoryPanel);
        }
        historyHeaderPanel.setSize(hxWidth, view.getHistoryPanel().getHeight());
        historyHeaderPanel.setPreferredSize(historyHeaderPanel.getSize());
        historyHeaderPanel.setMinimumSize(historyHeaderPanel.getSize());
        versionPanel.setSize(hxWidth, view.getHeight());
        versionPanel.setPreferredSize(versionPanel.getSize());
        versionScroller.getHorizontalScrollBar().getModel().
                addChangeListener(new HorizonatalScrollActionListener());
        ((JScrollPane) view.getParent().getParent()).getVerticalScrollBar().
                getModel().addChangeListener(new VerticalScrollActionListener());
    }

    private void resizeIfNeeded() {
        combineHistoryPanels();
        GuiUtil.tickle(historyHeaderPanel);
    }

    private void setupVersionPanel(Map<PathBI, Integer> pathRowMap,
            TreeSet<PositionBI> positionOrderedSet,
            Map<PositionBI, Collection<ComponentVersionDragPanel<?>>> positionPanelMap) throws IOException {
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
            addAllPositions(version, dragPanel);
        } else {
            ButtonGroup group = new ButtonGroup();
            addPosition(version.getPosition(), group, 
                version, dragPanel);
            for (ComponentVersionDragPanel panel : versionPanels) {
                addPosition(panel.getComponentVersion().getPosition(), group, 
                    version, panel);
            }
        }
        for (ComponentVersionDragPanel refexPanel : refexPanels) {
            processPanel(refexPanel);
        }
    }

    private void addAllPositions(ComponentVersionBI version, ComponentVersionDragPanel<?> dragPanel) throws IOException {
        Set<PositionBI> positions = version.getChronicle().getPositions();
        ButtonGroup group = new ButtonGroup();
        for (PositionBI p : positions) {
            addPosition(p, group, version, dragPanel);
        }
    }

    
    private void addPosition(PositionBI p, ButtonGroup group, 
            ComponentVersionBI version, 
            ComponentVersionDragPanel<?> dragPanel) {
        try {
            JRadioButton button = new JRadioButton();
            button.setToolTipText(p.toString());
            if (group != null) {
                group.add(button);
            }
            if (version.getPosition().equals(p)) {
                button.setSelected(true);
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
            versionPanel.add(button);
            button.setSize(button.getPreferredSize());
        } catch (Throwable ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private List<ComponentVersionDragPanel<?>> getVersionPanels(
            ComponentVersionDragPanel<?> dragPanel) {
        List<ComponentVersionDragPanel<?>> versionPanels =
                new ArrayList<ComponentVersionDragPanel<?>>();
        for (Component comp : dragPanel.getComponents()) {
            if (comp.isVisible()) {
                if (comp instanceof ComponentVersionDragPanel) {
                    ComponentVersionDragPanel cvdp =
                            (ComponentVersionDragPanel) comp;
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
    }
    
    private static final int xStartLoc = 5;
    
    private void  redoLayout() {
        int currentX = xStartLoc;
        for (JCheckBox positionCheck: positionCheckList) {
            PositionBI position = checkPositionMap.get(positionCheck);
            int row = pathRowMap.get(position.getPath());
            JCheckBox rowCheck = rowToPathCheckMap.get(row);
            positionCheck.setVisible(rowCheck.isVisible());
            positionCheck.setLocation(currentX, positionCheck.getY());
            for (JComponent componentInColumn: checkComponentMap.get(positionCheck)) {
                componentInColumn.setVisible(positionCheck.isVisible());
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

    public static JPanel setupHistoryPanel(ConceptView view, JScrollPane historyScroller) throws IOException {
        HistoryPanel historyPanel = new HistoryPanel(view, historyScroller);
        return historyPanel.topHistoryPanel;
    }
}
