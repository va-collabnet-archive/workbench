/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

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

    public static JPanel setupHistoryPanel(ConceptView view) {
        JPanel historyPanel = new JPanel(new GridBagLayout());
        Map<PathBI, Integer> pathRowMap = view.getPathRowMap();
        TreeSet<PositionBI> positionOrderedSet = view.getPositionOrderedSet();
        if (pathRowMap != null && positionOrderedSet != null) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridy = 0;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            gbc.weightx = 0;
            gbc.weighty = 0;

            JCheckBox startCheck = view.getJCheckBox();
            historyPanel.add(startCheck, gbc);
            gbc.gridx++;

            for (PositionBI p : positionOrderedSet) {
                gbc.gridheight = 1;
                gbc.gridy = pathRowMap.get(p.getPath());
                JCheckBox positionCheck = view.getJCheckBox();
                positionCheck.setToolTipText(p.toString());
                historyPanel.add(positionCheck, gbc);
                gbc.gridx++;
                gbc.gridheight = 10;
                gbc.gridy = 0;
                JLabel historyLabel = new JLabel("");
                historyLabel.setBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
                historyPanel.add(historyLabel, gbc);
                gbc.gridx++;
                positionCheck.addActionListener(
                        new UpdateHistoryBorder(p.toString(), historyLabel));
            }
            gbc.gridheight = 1;
            gbc.weightx = 1;
            historyPanel.add(new JLabel(""), gbc);
            gbc.weightx = 0;
            gbc.gridx = 0;
            gbc.gridy = pathRowMap.size();

            // add some random button groups...
            Random randomGenerator = new Random();
            gbc.gridy++;
            gbc.gridx = 1;
            ButtonGroup group0 = new ButtonGroup();

            for (PositionBI p : positionOrderedSet) {
                if (randomGenerator.nextInt(100) > 60) {
                    JRadioButton button = new JRadioButton();
                    button.setToolTipText(p.toString());
                    group0.add(button);
                    button.setSelected(true);
                    historyPanel.add(button, gbc);
                }
                gbc.gridx += 2;
            }

            gbc.gridy++;
            gbc.gridx = 1;
            ButtonGroup group1 = new ButtonGroup();
            for (PositionBI p : positionOrderedSet) {
                if (randomGenerator.nextInt(100) > 60) {
                    JRadioButton button = new JRadioButton();
                    button.setToolTipText(p.toString());
                    group1.add(button);
                    button.setSelected(true);
                    historyPanel.add(button, gbc);
                }
                gbc.gridx += 2;
            }

            gbc.gridy++;
            gbc.gridx = 1;
            ButtonGroup group = new ButtonGroup();
            for (PositionBI p : positionOrderedSet) {
                if (randomGenerator.nextInt(100) > 60) {
                    JRadioButton button = new JRadioButton();
                    button.setToolTipText(p.toString());
                    group.add(button);
                    button.setSelected(true);
                    historyPanel.add(button, gbc);
                }
                gbc.gridx += 2;
            }

            gbc.gridx++;
            gbc.weighty = 1;
            gbc.gridy++;
            historyPanel.add(new JLabel(""), gbc);

        }
        return historyPanel;
    }

}
