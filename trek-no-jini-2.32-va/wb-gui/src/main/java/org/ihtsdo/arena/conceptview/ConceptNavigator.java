package org.ihtsdo.arena.conceptview;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.arena.conceptview.history.HistoryPanel;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;

import org.ihtsdo.arena.conceptview.ConceptViewSettings.SIDE;
import org.ihtsdo.taxonomy.TaxonomyTree;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

public class ConceptNavigator extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    //~--- fields --------------------------------------------------------------
    private SIDE side = SIDE.RIGHT;
    private FocusDrop focusDrop;
    private HistoryPanel historyPanel;

    public HistoryPanel getHistoryPanel() throws IOException {
        if (historyPanel == null) {
            historyPanel = new HistoryPanel(view, historyScroller, this);
        }
        return historyPanel;
    }
    private final JScrollPane historyScroller;
    private JButton implementButton;
    private TaxonomyTree navigatorTree;
    private final JPanel topPanel;
    private JScrollPane treeScroller;
    private final ConceptView view;

    //~--- constructors --------------------------------------------------------
    public ConceptNavigator(JScrollPane treeScroller, I_ConfigAceFrame config, ConceptView view) throws IOException {
        super(new GridBagLayout());
        this.treeScroller = treeScroller;
        this.historyScroller = new JScrollPane(new JLabel("History panel"));
        this.historyScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.historyScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        this.historyScroller.setVisible(false);
        this.view = view;

        ConceptChangeListener ccl = new ConceptChangeListener();

        this.view.addHostListener(ccl);
        navigatorTree = (TaxonomyTree) treeScroller.getViewport().getView();
        focusDrop = new FocusDrop(new ImageIcon(ACE.class.getResource("/16x16/plain/flash.png")),
                navigatorTree, config);
        topPanel = setupTopPanel();
        layoutNavigator();
    }

    //~--- methods -------------------------------------------------------------
    private void layoutNavigator() {
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 0;
        add(topPanel, gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        add(treeScroller, gbc);
        add(historyScroller, gbc);
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

    private void privateHxPanelUpdate() throws IOException {
        getHistoryPanel();
        if (view.isHistoryShown()) {
            treeScroller.setVisible(false);
            focusDrop.setVisible(false);
            historyScroller.setVisible(true);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        view.resetLastLayoutSequence();
                        view.redoConceptViewLayout();
                    } catch (IOException ex) {
                        AceLog.getAppLog().alertAndLogException(ex);
                    }
                }
            });
        } else {
            view.resetLastLayoutSequence();
            view.redoConceptViewLayout();
        }
    }

    protected void resetHistoryPanel() throws IOException {
        getHistoryPanel().updateHistoryLayout();
       
    }

    private JPanel setupTopPanel() {
        JPanel thePanel = new JPanel(new GridBagLayout());

        thePanel.setCursor(Cursor.getDefaultCursor());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;

        JLabel navIcon =
                new JLabel(new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/compass.png")));

        thePanel.add(navIcon, gbc);
        navIcon.setVisible(false);
        gbc.weightx = 1;
        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        thePanel.add(new JLabel(" "), gbc);
        gbc.gridx++;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        implementButton =
                new JButton(new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/magic-wand.png")));
        implementButton.setBorder(BorderFactory.createEmptyBorder(2, 20, 2, 40));
        implementButton.setToolTipText("apply selected version changes");
        implementButton.setVisible(true);
        implementButton.setEnabled(false);
        thePanel.add(implementButton, gbc);
        gbc.gridx++;

        JButton taxonomyButton =
                new JButton(new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/text_tree.png")));

        taxonomyButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 5));
        taxonomyButton.addActionListener(new TaxonomyAction());
        taxonomyButton.setToolTipText("show taxonomy");
        thePanel.add(taxonomyButton, gbc);
        gbc.gridx++;

        JButton historyButton =
                new JButton(new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/radar-chart.png")));

        historyButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 20));
        historyButton.addActionListener(new HistoryAction());
        historyButton.setToolTipText("show history for concept");
        thePanel.add(historyButton, gbc);
        thePanel.setBackground(ConceptViewTitle.TITLE_COLOR);
        thePanel.setOpaque(true);
        thePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        return thePanel;
    }

    protected void updateHistoryPanel() {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    privateHxPanelUpdate();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            historyScroller.revalidate();
                        }
                    });
                } catch (IOException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            }
        });
    }

    //~--- get methods ---------------------------------------------------------
    public SIDE getDropSide() {
        return this.side;
    }

    public JButton getImplementButton() {
        return implementButton;
    }

    //~--- set methods ---------------------------------------------------------
    public void setDropSide(SIDE side) {
        if (this.side != side) {
            this.side = side;
            layoutNavigator();
        }
    }

    //~--- inner classes -------------------------------------------------------
    public class ConceptChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    refreshHistory();
                }
            });
        }
    }

    public class HistoryAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            view.setHistoryShown(true);
            treeScroller.setVisible(false);
            focusDrop.setVisible(false);
            historyScroller.setVisible(true);
            historyScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            refreshHistory();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    refreshHistory();
                }
            });
        }
    }

    private void refreshHistory() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateHistoryPanel();
                view.getSettings().fireConceptChanged();
                historyScroller.revalidate();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        historyScroller.revalidate();
                        int width = historyPanel.getHxWidth();
                        historyScroller.getViewport().scrollRectToVisible(
                                new Rectangle(width - 200, 0, 2, 4));
                    }
                });
            }
        });
    }

    public class StatedInferredAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            treeScroller.setVisible(false);
            focusDrop.setVisible(false);
            historyScroller.setVisible(false);
            view.setHistoryShown(false);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateHistoryPanel();
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
        }
    }
}
