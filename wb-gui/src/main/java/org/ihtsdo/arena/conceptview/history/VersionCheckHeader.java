/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.conceptview.history;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import org.ihtsdo.tk.api.PositionBI;

/**
 *
 * @author kec
 */
public class VersionCheckHeader implements ActionListener {

    /**
     * JPanel that contains the position check box versionPanelScroller, and the version versionPanelScroller with the radio buttons for all
     * the versions of the components in the concept.
     */
    private JPanel parentHistoryPanel = new JPanel(new GridBagLayout());

    /**
     * A map that links the collection of positions that are part of the history of a concept with the check
     * box that determines the vertical alignment of component version radio buttons that show the history of
     * that component. The reverse of the
     * <code>checkPositionMap</code>
     */
    protected Map<PositionBI, JCheckBox> positionCheckMap = new HashMap<>();
    /**
     * A map that links the check box that determines the vertical alignment of component version radio
     * buttons that show the history of that component with the collection of positions that are part of the
     * history of a concept. The reverse of the
     * <code>positionCheckMap</code>
     */
    protected Map<JCheckBox, PositionBI> checkPositionMap = new HashMap<>();
    /**
     * A map that links a position check box (whose location determine the vertical alignment) with the radio
     * buttons for each version of each component that is part of the concept.
     */
    protected Map<JCheckBox, List<JRadioButton>> checkComponentMap = new HashMap<>();
    /**
     * Panel at the top of the history versionPanelScroller that holds the check boxes that provide vertical alignment for
     * position radio buttons.
     */
    protected JPanel historyHeaderPanel = new JPanel(null);
    JScrollPane historyHeaderPanelScroller = new JScrollPane(historyHeaderPanel);
    int hxWidth = 0;
    private HistoryPanel hxPanel;

    public VersionCheckHeader(HistoryPanel hxPanel) {
        this.hxPanel = hxPanel;
    }

    public int getHxWidth() {
        return hxWidth;
    }

    public void setup() {
        parentHistoryPanel.removeAll();

        historyHeaderPanel.setSize(hxPanel.otherWidth, hxPanel.view.getHistoryPanel().getHeight() - HistoryPanel.insetAdjustment);
        historyHeaderPanel.setMaximumSize(historyHeaderPanel.getSize());
        historyHeaderPanel.setMinimumSize(historyHeaderPanel.getSize());
        historyHeaderPanel.setPreferredSize(historyHeaderPanel.getSize());
        historyHeaderPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        parentHistoryPanel.add(historyHeaderPanelScroller, gbc);
        historyHeaderPanelScroller.setLocation(0, 0);
        historyHeaderPanelScroller.setBorder(BorderFactory.createEmptyBorder());
        historyHeaderPanelScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        historyHeaderPanelScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        historyHeaderPanelScroller.setSize(hxPanel.otherWidth, hxPanel.view.getHistoryPanel().getHeight());
        historyHeaderPanelScroller.setLocation(0, 0);
        gbc.gridy++;
        gbc.weighty = 1.0;
        hxPanel.versionPanelScroller = new JScrollPane(hxPanel.versionPanel);
        hxPanel.versionPanelScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        hxPanel.versionPanelScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        parentHistoryPanel.add(hxPanel.versionPanelScroller, gbc);
    }

    public JPanel getTopHistoryPanel() {
        return parentHistoryPanel;
    }

    public Map<PositionBI, JCheckBox> getPositionCheckMap() {
        return positionCheckMap;
    }

    public Map<JCheckBox, PositionBI> getCheckPositionMap() {
        return checkPositionMap;
    }

    public Map<JCheckBox, List<JRadioButton>> getCheckComponentMap() {
        return checkComponentMap;
    }

    private void reset(boolean resetPositions) {
        for (List<JRadioButton> buttonList : checkComponentMap.values()) {
            for (JRadioButton rb : buttonList) {
                hxPanel.versionPanel.remove(rb);
            }
        }
        for (JCheckBox positionCheck : positionCheckMap.values()) {
            historyHeaderPanel.remove(positionCheck);
        }
        if (resetPositions) {
            positionCheckMap.clear();
        }

        checkComponentMap.clear();
    }

    public void setupHeader() {
        reset(hxPanel.conceptChanged());
        TreeSet<PositionBI> positionOrderedSet = hxPanel.view.getPositionOrderedSet();

        if ((hxPanel.view.getPathRowMap() == null) || (hxPanel.view.getRowToPathCheckMap() == null)
                || (positionOrderedSet == null)) {
            return;
        }

        int locX = HistoryPanel.xStartLoc;

        for (PositionBI p : positionOrderedSet) {
            assert p != null;
            assert hxPanel.view.getPathRowMap() != null;
            assert hxPanel.view.getRowToPathCheckMap() != null;
            assert p.getPath() != null;

            Integer row = hxPanel.view.getPathRowMap().get(p.getPath());

            if (row == null) {
                continue;
            }

            JCheckBox rowCheck = hxPanel.view.getRowToPathCheckMap().get(row);
            boolean positionCheckVisible = rowCheck.isSelected();
            JCheckBox positionCheck = positionCheckMap.get(p);
            if (positionCheck == null) {
                positionCheck = hxPanel.view.makeJCheckBox();
                positionCheck.addActionListener(this);
                positionCheckMap.put(p, positionCheck);
            }
            positionCheck.setVisible(positionCheckVisible);
            checkComponentMap.put(positionCheck, new ArrayList<JRadioButton>());
            checkPositionMap.put(positionCheck, p);
            positionCheck.setSize(positionCheck.getPreferredSize());
            positionCheck.setToolTipText(p.toString());
            if (positionCheckVisible) {
                historyHeaderPanel.add(positionCheck);
                positionCheck.setLocation(locX, rowCheck.getLocation().y);
                locX += positionCheck.getWidth();
             }
        }

        hxWidth = locX;
        historyHeaderPanel.revalidate();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        hxPanel.versionPanel.repaint(hxPanel.versionPanel.getBounds());
    }
}
