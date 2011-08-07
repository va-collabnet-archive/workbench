package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.dwfa.ace.TermComponentLabel.LabelText;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.tree.ExpandPathToNodeStateListener;
import org.dwfa.ace.tree.JTreeWithDragImage;
import org.dwfa.ace.tree.TermTreeHelper;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.ArenaComponentSettings;
import org.ihtsdo.arena.PreferencesNode;
import org.ihtsdo.tk.api.RelAssertionType;

public class ConceptViewSettings extends ArenaComponentSettings {

    public static final int NAVIGATOR_WIDTH = 350;
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 3;

    private class DescTypeActionListener implements ActionListener {

        DescPreference descPreference;

        public DescTypeActionListener(DescPreference descPreference) {
            this.descPreference = descPreference;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                JComboBox descTypeCombo = (JComboBox) e.getSource();
                switch (descPreference) {
                    case C1_REFEX:
                        c1Refex = (DescType) descTypeCombo.getSelectedItem();
                        break;
                    case C2_REFEX:
                        c2Refex = (DescType) descTypeCombo.getSelectedItem();
                        break;
                    case C3_REFEX:
                        c3Refex = (DescType) descTypeCombo.getSelectedItem();
                        break;
                    case DESC_TYPE:
                        descType = (DescType) descTypeCombo.getSelectedItem();
                        break;
                    case REL_TARGET:
                        relTarget = (DescType) descTypeCombo.getSelectedItem();
                        break;
                    case REL_TYPE:
                        relType = (DescType) descTypeCombo.getSelectedItem();
                        break;
                    case REFEX_NAME:
                        refexName = (DescType) descTypeCombo.getSelectedItem();
                        break;
                    default:
                        throw new RuntimeException("Can't handle type: " + descPreference);
               }
               getView().resetLastLayoutSequence();
               getView().layoutConcept(getConcept());
            } catch (IOException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    public enum SIDE {

        RIGHT, LEFT
    };

    public enum DescType {

        PREFERRED("preferred"),
        FULLY_SPECIFIED("fully specified");
        private String displayName;

        private DescType(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public LabelText getLabelText() {
            switch (this) {
                case FULLY_SPECIFIED:
                    return LabelText.FULLYSPECIFIED;
                case PREFERRED:
                    return LabelText.PREFERRED;
                default:
                    throw new RuntimeException("Can't handle type: " + this);
            }
        }
    }

    public enum DescPreference {

        DESC_TYPE("desc type"),
        REL_TYPE("rel type"),
        REL_TARGET("rel target"),
        C1_REFEX("c1 refex"),
        C2_REFEX("c2 refex"),
        C3_REFEX("c3 refex"),
        REFEX_NAME("refex name");

        String displayName;
        private DescPreference(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
    // dataVersion = 1;
    private Integer linkedTab = null;
    // dataVersion = 2;
    private boolean forAjudciation = false;
    // dataVersion = 3
    private DescType descType = DescType.PREFERRED;
    private DescType relType = DescType.PREFERRED;
    private DescType relTarget = DescType.FULLY_SPECIFIED;
    private DescType c1Refex = DescType.PREFERRED;
    private DescType c2Refex = DescType.PREFERRED;
    private DescType c3Refex = DescType.PREFERRED;
    private DescType refexName = DescType.PREFERRED;
    // transient
    private transient ConceptView view;
    private transient ConceptNavigator navigator;
    private transient JTreeWithDragImage navigatorTree;
    private transient JToggleButton navButton;
    private transient JButton statedInferredButton;
    private transient ConceptChangedListener conceptChangedListener;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(linkedTab);
        out.writeBoolean(forAjudciation);
        out.writeObject(descType);
        out.writeObject(relType);
        out.writeObject(relTarget);
        out.writeObject(c1Refex);
        out.writeObject(c2Refex);
        out.writeObject(c3Refex);
        out.writeObject(refexName);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            linkedTab = (Integer) in.readObject();
            if (dataVersion >= 2) {
                forAjudciation = in.readBoolean();
            } else {
                forAjudciation = false;
            }
            if (dataVersion >= 3) {
                descType = (DescType) in.readObject();
                relType = (DescType) in.readObject();
                relTarget = (DescType) in.readObject();
                c1Refex = (DescType) in.readObject();
                c2Refex = (DescType) in.readObject();
                c3Refex = (DescType) in.readObject();
                refexName = (DescType) in.readObject();
            } else {
                descType = DescType.PREFERRED;
                relType = DescType.PREFERRED;
                relTarget = DescType.FULLY_SPECIFIED;
                c1Refex = DescType.PREFERRED;
                c2Refex = DescType.PREFERRED;
                c3Refex = DescType.PREFERRED;
                refexName = DescType.PREFERRED;
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public ConceptViewSettings() {
        super();
        this.linkedTab = 0;
        this.forAjudciation = false;
    }

    public ConceptViewSettings(boolean forAjudciation) {
        super();
        this.linkedTab = 0;
        this.forAjudciation = forAjudciation;
    }

    public ConceptViewSettings(boolean forAjudciation, Integer linkedTab) {
        super();
        this.linkedTab = linkedTab;
        this.forAjudciation = forAjudciation;
    }

    public boolean isForAjudciation() {
        return forAjudciation;
    }

    public void setForAjudciation(boolean forAjudciation) {
        this.forAjudciation = forAjudciation;
    }

    private class ConceptChangedListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (view != null) {
                try {
                    view.layoutConcept((I_GetConceptData) getHost().getTermComponent());
                } catch (IOException iOException) {
                    AceLog.getAppLog().alertAndLogException(iOException);
                }
            }

        }
    }

    public ConceptView getView() {
        return view;
    }

    @Override
    public ConceptView makeComponent(I_ConfigAceFrame config) {
        if (view == null) {
            this.conceptChangedListener = new ConceptChangedListener();
            view = new ConceptView(config, this, (ConceptViewRenderer) this.renderer);
            addHostListener(conceptChangedListener);
            try {
                view.layoutConcept((I_GetConceptData) getHost().getTermComponent());
            } catch (IOException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
        return view;
    }

    @Override
    public String getTitle() {
        if (getHost() != null) {
            if (getHost().getTermComponent() != null) {
                return getHost().getTermComponent().toString();
            }
        }
        return "empty";
    }

    public boolean isNavigatorSetup() {
        if (navigator == null) {
            return false;
        }
        if (getConfig() == null) {
            return false;
        }

        if (getConfig().getConceptViewer(linkedTab) == null) {
            return false;
        }
        return true;
    }

    @Override
    public I_HostConceptPlugins getHost() {
        if (linkedTab != null && linkedTab != -1) {
            if (getConfig() != null) {
                if (linkedTab == -2) {
                    return getConfig().getListConceptViewer();
                }
                return getConfig().getConceptViewer(linkedTab);
            }
        }
        return null;
    }

    @Override
    public JComponent getLinkComponent() {
        if (linkedTab != null && linkedTab >= 0) {
            JButton goToLinkButton = new JButton(new AbstractAction(" "
                    + linkedTab.toString() + " ") {

                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (linkedTab != null && linkedTab != -1) {
                        getConfig().selectConceptViewer(linkedTab);
                    }
                }
            });
            goToLinkButton.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 1));
            goToLinkButton.setForeground(Color.GRAY);
            return goToLinkButton;
        }
        return new JLabel();
    }

    @Override
    public List<AbstractButton> getSpecializedButtons() {
        List<AbstractButton> buttons = new ArrayList<AbstractButton>();
        navButton = getNavigatorButton();
        buttons.add(navButton);
        statedInferredButton = getStatedInferredButton();
        buttons.add(statedInferredButton);
        return buttons;
    }

    public JToggleButton getNavButton() {
        return navButton;
    }

    public boolean hideNavigator() {
        if (preferences != null) {
            JLayeredPane layers = renderer.getRootPane().getLayeredPane();

            preferences.setVisible(false);
            preferences.invalidate();
            layers.remove(preferences);
        }

        if (navButton.isSelected()) {
            navButton.doClick();
            return true;
        }
        return false;
    }

    public boolean showNavigator() {
        if (!navButton.isSelected()) {
            navButton.doClick();
            return false;
        }
        return true;
    }
    private static ImageIcon statedView = new ImageIcon(
            ConceptViewRenderer.class.getResource("/16x16/plain/graph_edge.png"));
    private static ImageIcon inferredView = new ImageIcon(
            ConceptViewRenderer.class.getResource("/16x16/plain/chrystal_ball.png"));
    private static ImageIcon inferredAndStatedView = new ImageIcon(
            ConceptViewRenderer.class.getResource("/16x16/plain/inferred-then-stated.png"));
    RelAssertionType relAssertionType = RelAssertionType.STATED;

    public RelAssertionType getRelAssertionType() {
        return relAssertionType;
    }

    protected JButton getStatedInferredButton() {
        JButton button = new JButton(new AbstractAction("", statedView) {

            @Override
            public void actionPerformed(ActionEvent e) {
                view.resetLastLayoutSequence();
                JButton button = (JButton) e.getSource();
                if (relAssertionType == null) {
                    relAssertionType = RelAssertionType.STATED;
                }
                switch (relAssertionType) {

                    case INFERRED:
                        relAssertionType = RelAssertionType.INFERRED_THEN_STATED;
                        button.setIcon(inferredAndStatedView);
                        button.setToolTipText("showing inferred and stated, toggle to show stated...");
                        conceptChangedListener.propertyChange(null);
                        break;
                    case STATED:
                        relAssertionType = RelAssertionType.INFERRED;
                        button.setIcon(inferredView);
                        button.setToolTipText("showing inferred, toggle to show toggle to show inferred and stated...");
                        conceptChangedListener.propertyChange(null);
                        break;
                    case INFERRED_THEN_STATED:
                        relAssertionType = RelAssertionType.STATED;
                        button.setIcon(statedView);
                        button.setToolTipText("showing stated, toggle to show inferred...");
                        conceptChangedListener.propertyChange(null);
                        break;

                }
            }
        });
        button.setSelected(true);
        button.setPreferredSize(new Dimension(21, 16));
        button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        return button;
    }

    public boolean showStated() {
        if (relAssertionType == null) {
            relAssertionType = RelAssertionType.STATED;
        }
        return relAssertionType != RelAssertionType.INFERRED;
    }

    public boolean showInferred() {
        if (relAssertionType == null) {
            relAssertionType = RelAssertionType.STATED;
        }
        return relAssertionType != RelAssertionType.STATED;
    }

    private JToggleButton getNavigatorButton() {
        JToggleButton button = new JToggleButton(new AbstractAction("", new ImageIcon(
                ConceptViewRenderer.class.getResource("/16x16/plain/compass.png"))) {

            /**
             *
             */
            private static final long serialVersionUID = 1L;
            boolean showNavigator = false;
            boolean historyWasShown = false;

            @Override
            public void actionPerformed(ActionEvent e) {
                showNavigator = !showNavigator;
                JLayeredPane layers = renderer.getRootPane().getLayeredPane();
                if (showNavigator) {
                    setNavigatorLocation();
                    getNavigator().setVisible(true);
                    if (((JToggleButton) e.getSource()).isSelected() == false) {
                        ((JToggleButton) e.getSource()).setSelected(true);
                    }
                    view.setHistoryShown(historyWasShown);
                } else {
                    getNavigator().setVisible(false);
                    historyWasShown = view.isHistoryShown();
                    view.setHistoryShown(false);
                    getNavigator().invalidate();
                    layers.remove(getNavigator());
                    if (((JToggleButton) e.getSource()).isSelected()) {
                        ((JToggleButton) e.getSource()).setSelected(false);
                    }
                }
            }
        });
        button.setPreferredSize(new Dimension(21, 16));
        button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        button.setToolTipText("show navigator");
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        return button;
    }

    public void setNavigatorLocation() {
        int rightSpace = -1;
        int leftSpace = -1;
        JLayeredPane layers = renderer.getRootPane().getLayeredPane();
        Point loc = SwingUtilities.convertPoint(renderer, new Point(0, 0), layers);
        if (layers.getWidth() > loc.x + renderer.getWidth() + getNavigator().getWidth() + rightSpace) {
            loc.x = loc.x + renderer.getWidth() + rightSpace;
            getNavigator().setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.GRAY));
            getNavigator().setDropSide(SIDE.RIGHT);
        } else {
            loc.x = loc.x - getNavigator().getWidth() - leftSpace;
            getNavigator().setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, Color.GRAY));
            getNavigator().setDropSide(SIDE.LEFT);
        }
        getNavigator().setBounds(loc.x, loc.y, getNavigator().getWidth(), renderer.getHeight() + 1);
        layers.add(getNavigator(), JLayeredPane.PALETTE_LAYER);
    }

    protected ConceptNavigator getNavigator() {
        if (navigator == null) {
            try {
                TermTreeHelper hierarchicalTreeHelper = new TermTreeHelper(config);
                JScrollPane treeScroller = hierarchicalTreeHelper.getHierarchyPanel();
                navigatorTree = (JTreeWithDragImage) treeScroller.getViewport().getView();
                navigatorTree.setFont(navigatorTree.getFont().deriveFont(getFontSize()));
                navigator = new ConceptNavigator(treeScroller, config, view);
                //navigator.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.GRAY));
                navigator.setOpaque(true);
                navigator.setBounds(0, 0, NAVIGATOR_WIDTH, 20);
            } catch (Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }

        if (getHost() != null) {
            if (getHost().getTermComponent() != null) {

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            ExpandPathToNodeStateListener expandPathToNodeStateListener =
                                    new ExpandPathToNodeStateListener(navigatorTree, config,
                                    (I_GetConceptData) getHost().getTermComponent());
                        } catch (IOException e) {
                            AceLog.getAppLog().alertAndLogException(e);
                        } catch (TerminologyException e) {
                            AceLog.getAppLog().alertAndLogException(e);
                        }
                    }
                });
            }
        }
        return navigator;
    }

    @Override
    protected void setupSubtypes() {
        this.getPrefRoot().add(newDescTypeNode(DescPreference.DESC_TYPE));
        this.getPrefRoot().add(newDescTypeNode(DescPreference.REL_TYPE));
        this.getPrefRoot().add(newDescTypeNode(DescPreference.REL_TARGET));
        this.getPrefRoot().add(newDescTypeNode(DescPreference.REFEX_NAME));
        this.getPrefRoot().add(newDescTypeNode(DescPreference.C1_REFEX));
        this.getPrefRoot().add(newDescTypeNode(DescPreference.C2_REFEX));
        this.getPrefRoot().add(newDescTypeNode(DescPreference.C3_REFEX));
    }

    private PreferencesNode newDescTypeNode(DescPreference descPreference) {
        JComboBox descTypeCombo = new JComboBox(DescType.values());
        switch (descPreference) {
            case C1_REFEX:
                descTypeCombo.setSelectedItem(c1Refex);
                break;
            case C2_REFEX:
                descTypeCombo.setSelectedItem(c2Refex);
                break;
            case C3_REFEX:
                descTypeCombo.setSelectedItem(c3Refex);
                break;
            case DESC_TYPE:
                descTypeCombo.setSelectedItem(descType);
                break;
            case REL_TARGET:
                descTypeCombo.setSelectedItem(relTarget);
                break;
            case REL_TYPE:
                descTypeCombo.setSelectedItem(relType);
                break;
            case REFEX_NAME:
               descTypeCombo.setSelectedItem(refexName);
                break;
            default:
                throw new RuntimeException("Can't handle type: " + descPreference);

        }
        descTypeCombo.addActionListener(new DescTypeActionListener(descPreference));

        JPanel descTypePanel = new JPanel(new GridLayout(1, 1));
        descTypePanel.add(descTypeCombo);
        return new PreferencesNode(descPreference.toString(), descTypePanel);
    }

    @Override
    public I_GetConceptData getConcept() {
        if (getHost() != null) {
            return (I_GetConceptData) getHost().getTermComponent();
        }
        return null;
    }

    public Integer getLinkedTab() {
        return linkedTab;
    }

    public void setLinkedTab(Integer linkedTab) {
        this.linkedTab = linkedTab;
    }

    public void regenerateWfPanel(I_GetConceptData con) {
    }

    public DescType getC1Refex() {
        return c1Refex;
    }

    public DescType getC2Refex() {
        return c2Refex;
    }

    public DescType getC3Refex() {
        return c3Refex;
    }

    public DescType getDescType() {
        return descType;
    }

    public DescType getRelTarget() {
        return relTarget;
    }

    public DescType getRelType() {
        return relType;
    }

    public DescType getRefexName() {
        return refexName;
    }
}
