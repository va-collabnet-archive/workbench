/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.util.swing.GuiUtil;

/**
 *
 * @author kec
 */
public class HistoryPanel {

    private static class UpdateHistoryBorder implements ActionListener {

        String hxString;
        JLabel hxLabel;

        public UpdateHistoryBorder(String hxString, JLabel historyLabel) {
            this.hxString = hxString;
            this.hxLabel = historyLabel;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            JCheckBox check = (JCheckBox) ae.getSource();
            if (check.isSelected()) {
                hxLabel.setBorder(
                        BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY),
                        new HistoryBorder(
                        BorderFactory.createEmptyBorder(),
                        hxString,
                        new Font("monospaced", Font.PLAIN, 12),
                        Color.BLACK)));

            } else {
                hxLabel.setBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
            }
        }
    }
    ConceptView view;
    JPanel topHistoryPanel = new JPanel(null);
    JPanel historyHeaderPanel = new JPanel(null);
    JPanel versionPanel = new JPanel(null);
    JScrollPane versionScroller = new JScrollPane(versionPanel);
    Map<PositionBI, JCheckBox> positionCheckMap = new HashMap<PositionBI, JCheckBox>();
    int hxWidth = 0;
    
    private class TopChangeListener implements ComponentListener {

        @Override
        public void componentResized(ComponentEvent ce) {
            combineHistoryPanels();
            GuiUtil.tickle(historyHeaderPanel);
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

    private HistoryPanel(ConceptView view, JScrollPane historyScroller) throws IOException {
        this.view = view;
        Map<PathBI, Integer> pathRowMap = view.getPathRowMap();
        Map<PositionBI, Collection<ComponentVersionDragPanel<?>>> positionPanelMap = view.getPositionPanelMap();
        //versionPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, Color.red));
        versionPanel.setMinimumSize(versionPanel.getSize());
        versionPanel.setPreferredSize(versionPanel.getSize());
        TreeSet<PositionBI> positionOrderedSet = view.getPositionOrderedSet();
        if (pathRowMap != null && positionOrderedSet != null) {
            setupHeader(view);
            combineHistoryPanels();
            topHistoryPanel.addComponentListener(new TopChangeListener());
            historyScroller.setViewportView(topHistoryPanel);
            setupVersionPanel(pathRowMap, positionOrderedSet,
                    positionPanelMap);
        }
        historyHeaderPanel.setLocation(0, 0);
        //historyHeaderPanel.setSize(hxWidth, view.getHistoryPanel().getHeight());
        historyHeaderPanel.setPreferredSize(historyHeaderPanel.getSize());
        historyHeaderPanel.setMinimumSize(historyHeaderPanel.getSize());
        versionPanel.setSize(hxWidth, view.getHeight());
        //versionScroller.setSize(hxWidth, versionScroller.getHeight());
      }

    private void setupVersionPanel(Map<PathBI, Integer> pathRowMap,
            TreeSet<PositionBI> positionOrderedSet,
            Map<PositionBI, Collection<ComponentVersionDragPanel<?>>> positionPanelMap) throws IOException {
        for (Collection<ComponentVersionDragPanel<?>> panelSet : positionPanelMap.values()) {
            for (ComponentVersionDragPanel<?> dragPanel : panelSet) {
                ComponentVersionBI version = dragPanel.getComponentVersion();
                Set<PositionBI> positions = version.getChronicle().getPositions();
                ButtonGroup group = new ButtonGroup();
                for (PositionBI p : positions) {
                    JRadioButton button = new JRadioButton();
                    button.setToolTipText(p.toString());
                    group.add(button);
                    if (version.getPosition().equals(p)) {
                        button.setSelected(true);
                    }
                    Point dragPanelLoc = dragPanel.getLocationOnScreen();
                    SwingUtilities.convertPointFromScreen(dragPanelLoc, versionPanel);
                    int midPoint = dragPanelLoc.y + (dragPanel.getHeight() / 2) - 12;
                    JCheckBox positionCheck = positionCheckMap.get(p);
                    button.setLocation(positionCheck.getX(), midPoint);
                    versionPanel.add(button);
                    button.setSize(button.getPreferredSize());
                }
            }
        }
    }

    private static final int insetAdjustment = 3;
    
    private void combineHistoryPanels() {
        topHistoryPanel.removeAll();
        historyHeaderPanel.setSize(topHistoryPanel.getWidth(), 
                view.getHistoryPanel().getHeight() - insetAdjustment);
        topHistoryPanel.add(historyHeaderPanel);
        historyHeaderPanel.setLocation(0, 0);
        topHistoryPanel.add(versionScroller);
        versionScroller.setSize(topHistoryPanel.getWidth(), view.getParent().getHeight() + insetAdjustment);
        versionScroller.setLocation(0, historyHeaderPanel.getHeight() + 1);
        versionScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        versionPanel.setSize(hxWidth, view.getHeight());
        /*
        versionScroller.getVerticalScrollBar().setModel(
                ((JScrollPane) view.getParent().getParent()).getVerticalScrollBar().getModel());
         
         */
    }

    private void setupHeader(ConceptView view) {
        Map<PathBI, Integer> pathRowMap = view.getPathRowMap();
        Map<Integer, JCheckBox> rowToPathCheckMap = view.getRowToPathCheckMap();
        TreeSet<PositionBI> positionOrderedSet = view.getPositionOrderedSet();
        if (pathRowMap == null
                || rowToPathCheckMap == null
                || positionOrderedSet == null) {
            return;
        }
        int locX = 5;
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
            positionCheckMap.put(p, positionCheck);
            positionCheck.setLocation(locX, rowCheck.getLocation().y);
            positionCheck.setSize(positionCheck.getPreferredSize());
            positionCheck.setToolTipText(p.toString());
            historyHeaderPanel.add(positionCheck);
            locX += positionCheck.getWidth();

            JLabel historyLabel = new JLabel("");
            historyLabel.setBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
            historyHeaderPanel.add(historyLabel);
            historyLabel.setLocation(locX, 0);
            historyLabel.setSize(4, 40);
            locX += historyLabel.getWidth();
            positionCheck.addActionListener(
                    new UpdateHistoryBorder(p.toString(), historyLabel));
        }
        hxWidth = locX + 20;
    }

    public static JPanel setupHistoryPanel(ConceptView view, JScrollPane historyScroller) throws IOException {
        HistoryPanel historyPanel = new HistoryPanel(view, historyScroller);
        return historyPanel.topHistoryPanel;
    }
}
