package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.tree.JTreeWithDragImage;
import org.ihtsdo.arena.conceptview.ConceptViewSettings.SIDE;

public class ConceptNavigator extends JPanel {

    private HistoryPanel historyPanel;
    private JButton implementButton;

    public JButton getImplementButton() {
        return implementButton;
    }

    public class ConceptChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    historyPanel = null;
                }
            });
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
            view.setHistoryShown(true);

            treeScroller.setVisible(false);
            focusDrop.setVisible(false);
            historyScroller.setVisible(true);
            statedInferredScroller.setVisible(false);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    updateHistoryPanel();
                }
            });
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
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    updateHistoryPanel();
                }
            });
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
        ConceptChangeListener ccl = new ConceptChangeListener();
        this.view.addHostListener(ccl);
        this.view.getConfig().addPropertyChangeListener("commit", ccl);

        navigatorTree = (JTreeWithDragImage) treeScroller.getViewport().getView();
        focusDrop = new FocusDrop(new ImageIcon(ACE.class.getResource("/16x16/plain/flash.png")),
                navigatorTree, config);
        layoutNavigator();
    }

    protected void resetHistoryPanel() {
        historyPanel = null;
    }

    protected void updateHistoryPanel() {
        try {
            if (historyPanel == null) {
                historyPanel = new HistoryPanel(view, historyScroller, this);
                if (view.isHistoryShown()) {
                    treeScroller.setVisible(false);
                    focusDrop.setVisible(false);
                    historyScroller.setVisible(true);
                    statedInferredScroller.setVisible(false);
                    historyPanel.resizeIfNeeded();
                }
            } else {
                historyPanel.resizeIfNeeded();
            }
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
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
        implementButton = new JButton(new ImageIcon(
                ConceptViewRenderer.class.getResource("/16x16/plain/magic-wand.png")));
        implementButton.setBorder(BorderFactory.createEmptyBorder(2, 20, 2, 20));
        implementButton.setToolTipText("apply selected version changes");
        implementButton.setVisible(true);
        implementButton.setEnabled(false);
        topPanel.add(implementButton, gbc);
        gbc.gridx++;
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
