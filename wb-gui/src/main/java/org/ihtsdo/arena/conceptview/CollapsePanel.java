package org.ihtsdo.arena.conceptview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.dwfa.ace.log.AceLog;

import org.ihtsdo.arena.ArenaComponentSettings;
import org.ihtsdo.arena.conceptview.ComponentVersionDragPanel.SubPanelTypes;
import org.ihtsdo.arena.conceptview.ConceptView.PanelSection;
import org.ihtsdo.arena.context.action.BpActionFactoryNoPanel;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.drools.facts.ConceptFact;
import org.ihtsdo.tk.drools.facts.Context;
import org.intsdo.tk.drools.manager.DroolsExecutionManager;

public class CollapsePanel extends JPanel {

    static final ImageIcon showAlertsIcon = new ImageIcon(
            ConceptViewRenderer.class.getResource(
            "/16x16/plain/warning.png"));
    static final ImageIcon hideAlertsIcon =
            new ImageIcon(getBlackAndWhite(showAlertsIcon.getImage()));
    static final ImageIcon showExtrasIcon = new ImageIcon(
            ConceptViewRenderer.class.getResource(
            ArenaComponentSettings.IMAGE_PATH
            + "minimize.gif"));
    static final ImageIcon hideExtrasIcon = new ImageIcon(
            ConceptViewRenderer.class.getResource(
            ArenaComponentSettings.IMAGE_PATH
            + "maximize.gif"));
    static final ImageIcon showRefexesIcon = new ImageIcon(
            ConceptViewRenderer.class.getResource(
            "/16x16/plain/paperclip.png"));
    static final ImageIcon hideRefexesIcon =
            new ImageIcon(getBlackAndWhite(showRefexesIcon.getImage()));
    static final ImageIcon showTemplatesIcon = new ImageIcon(
            ConceptViewRenderer.class.getResource(
            "/16x16/plain/lightbulb_on.png"));
    static final ImageIcon hideTemplatesIcon =
            new ImageIcon(getBlackAndWhite(showTemplatesIcon.getImage()));
    static final ImageIcon showHistoryIcon = new ImageIcon(
            ConceptViewRenderer.class.getResource(
            "/16x16/plain/history2.png"));
    static final ImageIcon hideHistoryIcon =
            new ImageIcon(getBlackAndWhite(showHistoryIcon.getImage()));
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    int refexCount = 0;
    int templateCount = 0;
    int alertCount = 0;
    int historyCount = 1;
    Set<I_ToggleSubPanels> components = new HashSet<I_ToggleSubPanels>();
    private JButton alertsButton;
    private JButton extrasButton;
    private JButton refexButton;
    private JButton historyButton;
    private JButton templatessButton;
    private List<JComponent> alertPanels = new ArrayList<JComponent>();
    private List<JComponent> refexPanels = new ArrayList<JComponent>();
    private List<JComponent> templatePanels = new ArrayList<JComponent>();
    private List<JComponent> historyPanels = new ArrayList<JComponent>();
    private List<JComponent> retiredPanels = new ArrayList<JComponent>();

    public List<JComponent> getRetiredPanels() {
        return retiredPanels;
    }
    private JButton collapseExpandButton;
    private JButton dynamicPopupMenuButton;
    private CollapsePanelPrefs prefs;
    private PanelSection sectionType;
    private final ConceptViewSettings settings;
    private Set<File> kbFiles = new HashSet<File>();
    private String kbKey;

    public void setShown(boolean shown, SubPanelTypes type) {
        prefs.setShown(shown, type);
    }

    public boolean isShown(SubPanelTypes type) {
        return prefs.isShown(type);
    }

    /**
     * @return the extrasShown
     */
    public boolean areExtrasShown() {
        return prefs.getExtrasShown();
    }

    /**
     * @param extrasShown the extrasShown to set
     */
    public void setExtrasShown(boolean extrasShown) {
        prefs.setExtrasShown(extrasShown);
    }

    /**
     * @return the subpanelsToShow
     */
    public EnumSet<ComponentVersionDragPanel.SubPanelTypes> getSubpanelsToShow() {
        return prefs.getSubpanelsToShow();
    }
    Set<PanelSection> noMenuSections =
            EnumSet.of(PanelSection.EXTRAS, PanelSection.REL_GRP);
    public CollapsePanel(String labelStr, ConceptViewSettings settings,
            CollapsePanelPrefs prefs, PanelSection sectionType) {
        this(labelStr, settings,
            prefs, sectionType, null);
    }

    public CollapsePanel(String labelStr, ConceptViewSettings settings,
            CollapsePanelPrefs prefs, PanelSection sectionType, JButton menuButton) {
        super();
        this.prefs = prefs;
        this.sectionType = sectionType;
        this.settings = settings;
        this.kbFiles.add(new File("drools-rules/ContextualSectionDropdown.drl"));
        this.kbKey = labelStr + CollapsePanel.class.getCanonicalName();

        try {
            DroolsExecutionManager.setup(kbKey, kbFiles);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
        settings.getFontSize();
        setBackground(Color.LIGHT_GRAY);
        setOpaque(true);
        setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.GRAY));
        setLayout(new BorderLayout());

        JPanel toolBar1 = new JPanel();
        toolBar1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 2));
        toolBar1.setOpaque(false);
        if (!noMenuSections.contains(sectionType)) {
            toolBar1.add(getRefexButton());
            toolBar1.add(getTemplateButton());
            toolBar1.add(getAlertsButton());
            toolBar1.add(getHistoryButton());
            toolBar1.add(getShowExtrasButton());
        } else {
            getRefexButton();
            getTemplateButton();
            getAlertsButton();
            getHistoryButton();
            getShowExtrasButton();
        }
        add(toolBar1, BorderLayout.WEST);

        JLabel label = new JLabel(labelStr, JLabel.LEFT);
        label.setFont(getFont().deriveFont(settings.getFontSize()));
        label.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 2));
        add(label, BorderLayout.CENTER);
        JPanel toolBar2 = new JPanel();
        toolBar2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 2));
        toolBar2.setOpaque(false);
        if (menuButton != null) {
            toolBar2.add(menuButton);
        } else if (!noMenuSections.contains(sectionType)) {
            toolBar2.add(getDynamicPopupMenuButton());
        } else {
            getDynamicPopupMenuButton();
        }
        toolBar2.add(getCollapseExpandButton());
        add(toolBar2, BorderLayout.EAST);
    }

    public void addPanelsChangedActionListener(ActionListener l) {
        alertsButton.addActionListener(l);
        extrasButton.addActionListener(l);
        refexButton.addActionListener(l);
        historyButton.addActionListener(l);
        templatessButton.addActionListener(l);
        collapseExpandButton.addActionListener(l);

    }

    private class DoDynamicPopup implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            JPopupMenu popup = new JPopupMenu();
            popup.add(setupMenuItem(new JMenuItem(" ")));
            getKbActions();
            switch (sectionType) {
                case CONCEPT:
                    for (Action a : conceptActions) {
                        popup.add(setupMenuItem(new JMenuItem(a)));
                    }
                    break;
                case DESC:
                    for (Action a : descriptionActions) {
                        popup.add(setupMenuItem(new JMenuItem(a)));
                    }
                    break;
                case REL:
                    for (Action a : relActions) {
                        popup.add(setupMenuItem(new JMenuItem(a)));
                    }
                    break;
            }

            popup.show(dynamicPopupMenuButton,
                    dynamicPopupMenuButton.getX()
                    + dynamicPopupMenuButton.getWidth()
                    - popup.getPreferredSize().width,
                    0);
        }

        private JMenuItem setupMenuItem(JMenuItem item) {
            //item.setFont(item.getFont().deriveFont(settings.getFontSize()));
            return item;
        }
    }
    Collection<Action> conceptActions = new ArrayList<Action>();
    Collection<Action> descriptionActions = new ArrayList<Action>();
    Collection<Action> relActions = new ArrayList<Action>();

    private void getKbActions() {
        conceptActions.clear();
        descriptionActions.clear();
        relActions.clear();

        try {
            if (settings.getConcept() != null) {
                ViewCoordinate coordinate = settings.getConfig().getViewCoordinate();
                Map<String, Object> globals = new HashMap<String, Object>();
                globals.put("vc", coordinate);
                globals.put("conceptActions", conceptActions);
                globals.put("descriptionActions", descriptionActions);
                globals.put("relActions", relActions);
                globals.put("actionFactory", new BpActionFactoryNoPanel(
                        settings.getConfig(),
                        settings.getHost()));

                Collection<Object> facts = new ArrayList<Object>();
                ConceptFact cFact = new ConceptFact(Context.FOCUS_CONCEPT,
                        Ts.get().getConceptVersion(coordinate,
                        settings.getConcept().getNid()),
                        coordinate);
                facts.add(cFact);

                DroolsExecutionManager.fireAllRules(
                		CollapsePanel.class.getCanonicalName(),
                        kbFiles,
                        globals,
                        facts,
                        false);
            }

        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }
    private static ImageIcon dynamicPopupImage = new ImageIcon(
                ConceptViewRenderer.class.getResource(
                "/16x16/plain/dynamic_popup.png"));
    private JButton getDynamicPopupMenuButton() {
        dynamicPopupMenuButton = new JButton(dynamicPopupImage);
        dynamicPopupMenuButton.setPreferredSize(new Dimension(21, 16));
        dynamicPopupMenuButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        dynamicPopupMenuButton.setToolTipText("contextual editing actions");
        dynamicPopupMenuButton.setOpaque(false);
        dynamicPopupMenuButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));
        dynamicPopupMenuButton.addActionListener(new DoDynamicPopup());
        return dynamicPopupMenuButton;
    }

    private static BufferedImage getBlackAndWhite(Image disImage) {
        JPanel imageObserver = new JPanel();
        BufferedImage image = new BufferedImage(disImage.getWidth(imageObserver),
                disImage.getHeight(imageObserver),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D graph = image.createGraphics();
        graph.drawImage(disImage, 0, 0, imageObserver);

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(cs, null);
        return op.filter(image, null);
    }

    private JButton getShowExtrasButton() {

        extrasButton = new JButton(new AbstractAction("", showExtrasIcon) {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                setExtrasShown(!areExtrasShown());
                for (JComponent jc : refexPanels) {
                    jc.setVisible(areExtrasShown());
                }
                for (JComponent jc : alertPanels) {
                    jc.setVisible(areExtrasShown());
                }
                for (JComponent jc : templatePanels) {
                    jc.setVisible(areExtrasShown());
                }
                for (JComponent jc : historyPanels) {
                    jc.setVisible(areExtrasShown());
                }
                for (I_ToggleSubPanels cvdp : components) {
                    if (areExtrasShown()) {
                        cvdp.showSubPanels(getSubpanelsToShow());
                    } else {
                        cvdp.hideSubPanels(getSubpanelsToShow());
                    }
                }
                setExtrasIcon();
                updateExtras();
            }
        });
        extrasButton.setPreferredSize(new Dimension(21, 16));
        extrasButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        extrasButton.setToolTipText("hide/show extra info for all group members");
        extrasButton.setOpaque(false);
        extrasButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));
        extrasButton.setSelected(areExtrasShown());
        setExtrasIcon();
        updateExtras();
        return extrasButton;
    }

    private void setExtrasIcon() {
        extrasButton.setIcon((areExtrasShown() ? showExtrasIcon
                : hideExtrasIcon));
    }

    private JButton getRefexButton() {
        refexButton = new JButton(new AbstractAction("", showRefexesIcon) {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                SubPanelTypes subpanelType = SubPanelTypes.REFEX;
                Icon icon = (!isShown(subpanelType) ? showRefexesIcon : hideRefexesIcon);
                handleToggleAction(subpanelType, e, icon);
            }
        });
        return setupSubpanelToggleButton(refexButton, SubPanelTypes.REFEX,
                "hide/show refexes", refexCount);
    }

    private JButton getHistoryButton() {
        historyButton = new JButton(new AbstractAction("", showHistoryIcon) {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                SubPanelTypes subpanelType = SubPanelTypes.HISTORY;
                boolean shown = isShown(subpanelType);
                Icon icon = (!shown ? showHistoryIcon : hideHistoryIcon);
                for (JComponent retiredPanel : retiredPanels) {
                    retiredPanel.setVisible(!shown);
                }
                handleToggleAction(subpanelType, e, icon);
            }
        });
        return setupSubpanelToggleButton(historyButton, SubPanelTypes.HISTORY,
                "hide/show history", historyCount);
    }

    private void handleToggleAction(SubPanelTypes subpanelType, ActionEvent e, Icon icon) {
        setShown(!isShown(subpanelType), subpanelType);
        updateShowSubpanelSet(isShown(subpanelType), subpanelType);
        ((JButton) e.getSource()).setIcon(icon);
        updateSubpanels();
    }

    private JButton setupSubpanelToggleButton(JButton toggleButton,
            SubPanelTypes subpanelType, String toolTipText, int count) {
        toggleButton.setSelected(isShown(subpanelType));
        toggleButton.setPreferredSize(new Dimension(21, 16));
        toggleButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        toggleButton.setToolTipText(toolTipText);
        toggleButton.setOpaque(false);
        toggleButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));
        if (count == 0) {
            toggleButton.setIcon(null);
            toggleButton.setEnabled(false);
            toggleButton.setMaximumSize(emptyDimension);
            toggleButton.setMinimumSize(emptyDimension);
            toggleButton.setPreferredSize(emptyDimension);
        }
        return toggleButton;
    }

    private void updateShowSubpanelSet(boolean show,
            ComponentVersionDragPanel.SubPanelTypes subpanel) {
        if (show) {
            getSubpanelsToShow().add(subpanel);
        } else {
            getSubpanelsToShow().remove(subpanel);
        }
        updateExtras();
    }

    private JButton getTemplateButton() {

        templatessButton = new JButton(new AbstractAction("", showTemplatesIcon) {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                SubPanelTypes subpanelType = SubPanelTypes.TEMPLATE;
                Icon icon = (!isShown(subpanelType) ? showTemplatesIcon : hideTemplatesIcon);
                handleToggleAction(subpanelType, e, icon);
            }
        });
        return setupSubpanelToggleButton(templatessButton, SubPanelTypes.TEMPLATE,
                "hide/show suggestions", templateCount);
    }

    private JButton getAlertsButton() {

        alertsButton = new JButton(new AbstractAction("", showAlertsIcon) {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                SubPanelTypes subpanelType = SubPanelTypes.ALERT;
                Icon icon = (!isShown(subpanelType) ? showAlertsIcon : hideAlertsIcon);
                handleToggleAction(subpanelType, e, icon);

            }
        });

        return setupSubpanelToggleButton(alertsButton, SubPanelTypes.ALERT,
                "hide/show warnings & errors", alertCount);
    }

    private void updateSubpanels() {
        if (areExtrasShown()) {
            for (I_ToggleSubPanels jc : components) {
                jc.showSubPanels(getSubpanelsToShow());
            }
        } else {
            for (I_ToggleSubPanels jc : components) {
                if (jc.isExpanded()) {
                    jc.showSubPanels(getSubpanelsToShow());
                }
            }

        }
    }

    private JButton getCollapseExpandButton() {
        collapseExpandButton = new JButton(new AbstractAction("", new ImageIcon(
                ConceptViewRenderer.class.getResource(ArenaComponentSettings.IMAGE_PATH + "minimize.gif"))) {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                prefs.setCollapsed(!prefs.isCollapsed());
                for (I_ToggleSubPanels jc : components) {
                    jc.setVisible(!prefs.isCollapsed());
                }
                ((JButton) e.getSource()).setIcon(new ImageIcon(
                        CollapsePanel.class.getResource(ArenaComponentSettings.IMAGE_PATH
                        + (prefs.isCollapsed() ? "maximize.gif"
                        : "minimize.gif"))));
            }
        });
        collapseExpandButton.setPreferredSize(new Dimension(21, 16));
        collapseExpandButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        collapseExpandButton.setToolTipText("Collapse/Expand");
        collapseExpandButton.setOpaque(false);
        collapseExpandButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));
        collapseExpandButton.setSelected(prefs.isCollapsed());
        collapseExpandButton.setIcon(new ImageIcon(
                CollapsePanel.class.getResource(ArenaComponentSettings.IMAGE_PATH
                + (prefs.isCollapsed() ? "maximize.gif"
                : "minimize.gif"))));
        return collapseExpandButton;
    }

    public void addToggleComponent(I_ToggleSubPanels component) {
        components.add(component);
    }
    private static int emptyWidth = 21;
    private static int emptyHeight = 16;
    private Dimension emptyDimension = new Dimension(emptyWidth, emptyHeight);

    private void updateExtras() {
        setExtrasIcon();
        if ((alertCount + refexCount + templateCount + historyCount) == 0) {
            extrasButton.setVisible(false);
        } else {
            extrasButton.setVisible(true);
        }
    }

    public int getHistoryCount() {
        return historyCount;
    }

    public void setHistoryCount(int historyCount) {
        this.historyCount = historyCount;
        if (historyCount + retiredPanels.size() == 0) {
            historyButton.setIcon(null);
            historyButton.setEnabled(false);
            historyButton.setMaximumSize(emptyDimension);
            historyButton.setMinimumSize(emptyDimension);
            historyButton.setPreferredSize(emptyDimension);
        } else {
            historyButton.setEnabled(true);
            historyButton.setIcon(isShown(SubPanelTypes.HISTORY) ? showHistoryIcon
                    : hideHistoryIcon);
        }
        updateExtras();
    }

    public void setAlertCount(int alertCount) {
        this.alertCount = alertCount;
        if (alertCount == 0) {
            alertsButton.setIcon(null);
            alertsButton.setEnabled(false);
            alertsButton.setMaximumSize(emptyDimension);
            alertsButton.setMinimumSize(emptyDimension);
            alertsButton.setPreferredSize(emptyDimension);
        } else {
            alertsButton.setEnabled(true);
            alertsButton.setIcon(isShown(SubPanelTypes.ALERT) ? showAlertsIcon
                    : hideAlertsIcon);
        }
        updateExtras();
    }

    public void setRefexCount(int refexCount) {
        this.refexCount = refexCount;
        if (refexCount == 0) {
            refexButton.setIcon(null);
            refexButton.setEnabled(false);
            refexButton.setMaximumSize(emptyDimension);
            refexButton.setMinimumSize(emptyDimension);
            refexButton.setPreferredSize(emptyDimension);
        } else {
            refexButton.setEnabled(true);
            refexButton.setIcon((isShown(SubPanelTypes.REFEX) ? showRefexesIcon
                    : hideRefexesIcon));
        }
        updateExtras();
    }

    public void setTemplateCount(int templateCount) {
        this.templateCount = templateCount;
        if (templateCount == 0) {
            templatessButton.setIcon(null);
            templatessButton.setEnabled(false);
            templatessButton.setMaximumSize(emptyDimension);
            templatessButton.setMinimumSize(emptyDimension);
            templatessButton.setPreferredSize(emptyDimension);
        } else {
            templatessButton.setIcon(isShown(SubPanelTypes.TEMPLATE) ? showTemplatesIcon
                    : hideTemplatesIcon);
            templatessButton.setEnabled(true);
        }
        updateExtras();
    }

    public List<JComponent> getAlertPanels() {
        return alertPanels;
    }

    public List<JComponent> getRefexPanels() {
        return refexPanels;
    }

    public List<JComponent> getTemplatePanels() {
        return templatePanels;
    }

    public List<JComponent> getHistoryPanels() {
        return historyPanels;
    }
}
