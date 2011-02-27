package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.tree.JTreeWithDragImage;
import org.ihtsdo.arena.conceptview.ConceptViewSettings.SIDE;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

public class ConceptNavigator extends JPanel {

    public class ConceptChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            updateHistoryPanel();
        }
    }

    public class TaxonomyAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            treeScroller.setVisible(true);
            focusDrop.setVisible(true);
            historyScroller.setVisible(false);
            view.setHistoryShown(false);
            statedInferredScroller.setVisible(false);
        }
    }

    public class HistoryAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            updateHistoryPanel();
            treeScroller.setVisible(false);
            focusDrop.setVisible(false);
            historyScroller.setVisible(true);
            view.setHistoryShown(true);
            statedInferredScroller.setVisible(false);
        }
    }

    public class StatedInferredAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            treeScroller.setVisible(false);
            focusDrop.setVisible(false);
            historyScroller.setVisible(false);
            view.setHistoryShown(false);
            statedInferredScroller.setVisible(true);
        }
    }
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private JTreeWithDragImage navigatorTree;
    private JScrollPane treeScroller;
    private JScrollPane historyScroller;
    private JScrollPane statedInferredScroller;
    private FocusDrop focusDrop;
    private SIDE side = SIDE.RIGHT;
    private final ConceptView view;

    public ConceptNavigator(JScrollPane treeScroller,
            I_ConfigAceFrame config,
            ConceptView view) {
        super(new GridBagLayout());
        this.treeScroller = treeScroller;
        this.historyScroller = new JScrollPane(new JLabel("History panel"));
        this.historyScroller.setVisible(false);
        this.statedInferredScroller = new JScrollPane(new JLabel("Stated inferred panel"));
        this.statedInferredScroller.setVisible(false);
        this.view = view;
        this.view.addHostListener(new ConceptChangeListener());

        navigatorTree = (JTreeWithDragImage) treeScroller.getViewport().getView();
        focusDrop = new FocusDrop(new ImageIcon(ACE.class.getResource("/16x16/plain/flash.png")),
                navigatorTree, config);
        layoutNavigator();
    }

    private static class UpdateHistoryBorder implements ActionListener {

        String hxString; 
        JLabel historyLabel;

        public UpdateHistoryBorder(String hxString, JLabel historyLabel) {
            this.hxString = hxString;
            this.historyLabel = historyLabel;
        }
        
        
        @Override
        public void actionPerformed(ActionEvent ae) {
            JCheckBox check = (JCheckBox) ae.getSource();
            if (check.isSelected()) {
               historyLabel.setBorder(
                        BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY),
                        new HistoryBorder(
                        BorderFactory.createEmptyBorder(),
                        hxString,
                        new Font("monospaced", Font.PLAIN, 12),
                        Color.BLACK)));
                
            } else {
               historyLabel.setBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
            }
        }
        
    }
    protected void updateHistoryPanel() {
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
                gbc.gridx+= 2;
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
                gbc.gridx+= 2;
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
                gbc.gridx+= 2;
            }

            gbc.gridx++;
            gbc.weighty = 1;
            gbc.gridy++;
            historyPanel.add(new JLabel(""), gbc);

        }
        historyScroller.setViewportView(historyPanel);
    }

    public void setDropSide(SIDE side) {
        if (this.side != side) {
            this.side = side;
            layoutNavigator();
        }
    }

    public SIDE getDropSide() {
        return this.side;
    }

    private void layoutNavigator() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 0;

        add(setupTopPanel(), gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(treeScroller, gbc);
        add(historyScroller, gbc);
        add(statedInferredScroller, gbc);
        gbc.weightx = 0;
        switch (side) {
            case LEFT:
                gbc.gridx = 2;
                break;
            case RIGHT:
                gbc.gridx = 0;
                break;
        }
        add(focusDrop, gbc);
    }

    private JPanel setupTopPanel() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;

        JLabel navIcon = new JLabel(new ImageIcon(
                ConceptViewRenderer.class.getResource("/16x16/plain/compass.png")));
        topPanel.add(navIcon, gbc);
        navIcon.setVisible(false);
        gbc.weightx = 1;
        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(new JLabel(" "), gbc);

        gbc.gridx++;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        JButton taxonomyButton = new JButton(new ImageIcon(
                ConceptViewRenderer.class.getResource("/16x16/plain/text_tree.png")));
        taxonomyButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        taxonomyButton.addActionListener(new TaxonomyAction());
        topPanel.add(taxonomyButton, gbc);
        gbc.gridx++;
        JButton historyButton = new JButton(new ImageIcon(
                ConceptViewRenderer.class.getResource("/16x16/plain/radar-chart.png")));
        historyButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        historyButton.addActionListener(new HistoryAction());
        topPanel.add(historyButton, gbc);
        gbc.gridx++;
        JButton statedInferredButton = new JButton(new ImageIcon(
                ConceptViewRenderer.class.getResource("/16x16/plain/chrystal_ball.png")));
        statedInferredButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        statedInferredButton.addActionListener(new StatedInferredAction());
        topPanel.add(statedInferredButton, gbc);


        topPanel.setBackground(ConceptViewTitle.TITLE_COLOR);
        topPanel.setOpaque(true);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        return topPanel;
    }
}
