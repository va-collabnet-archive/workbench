package org.ihtsdo.arena.conceptview;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

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
    private static final int dataVersion = 2;
        
    
    private transient ConceptChangedListener conceptChangedListener;

    public enum SIDE {

        RIGHT, LEFT
    };
    // dataVersion = 1;
    private Integer linkedTab = null;
    // dataVersion = 2;
    private boolean forAjudciation = false;

    // transient
    private transient ConceptView view;
    private transient ConceptNavigator navigator;
    private transient JTreeWithDragImage navigatorTree;
    private transient JToggleButton navButton;
    private transient JButton statedInferredButton;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(linkedTab);
        out.writeBoolean(forAjudciation);
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
        this.getPrefRoot().add(addComponentPrefs("attributes"));
        this.getPrefRoot().add(addComponentPrefs("descriptions"));
        this.getPrefRoot().add(addComponentPrefs("relationships"));
        this.getPrefRoot().add(addComponentPrefs("images"));
        this.getPrefRoot().add(addComponentPrefs("refset members"));
    }

    private PreferencesNode addComponentPrefs(String componentStr) {
        PreferencesNode componentNode = new PreferencesNode(componentStr, new JCheckBox("show " + componentStr + ": "));
        componentNode.add(new PreferencesNode("identifiers", new JCheckBox("show identifiers: ")));
        componentNode.add(new PreferencesNode("extensions", new JCheckBox("show extensions: ")));
        PreferencesNode templateNode = new PreferencesNode("templates", new JCheckBox("show templates: "));
        componentNode.add(templateNode);
        templateNode.add(new PreferencesNode("drools template", new JCheckBox("drools templates: ")));

        PreferencesNode filterNode = new PreferencesNode("filters", new JCheckBox("show filters: "));
        filterNode.add(new PreferencesNode("drools filter", new JCheckBox("drools filters: ")));
        componentNode.add(filterNode);
        if (componentStr.equals("descriptions")) {
            componentNode.add(new PreferencesNode("language", new JCheckBox("show language: ")));
            componentNode.add(new PreferencesNode("case sensitivity", new JCheckBox("case sensitive: ")));
            templateNode.add(new PreferencesNode("fully specified", new JCheckBox("fully specified: ")));
        } else if (componentStr.equals("relationships")) {
            componentNode.add(new PreferencesNode("refinability", new JCheckBox("show refinability: ")));
            componentNode.add(new PreferencesNode("characteristic", new JCheckBox("show characteristic: ")));
            templateNode.add(new PreferencesNode("procedures", new JCheckBox("procedures: ")));
            templateNode.add(new PreferencesNode("medicine", new JCheckBox("medicine: ")));
            templateNode.add(new PreferencesNode("finding", new JCheckBox("finding: ")));
            templateNode.add(new PreferencesNode("lab test", new JCheckBox("lab test: ")));
        }
        return componentNode;
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
}
