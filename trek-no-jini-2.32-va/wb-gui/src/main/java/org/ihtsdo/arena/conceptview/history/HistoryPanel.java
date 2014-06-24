
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.conceptview.history;

//~--- non-JDK imports --------------------------------------------------------
import org.dwfa.ace.log.AceLog;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ihtsdo.arena.conceptview.ConceptNavigator;
import org.ihtsdo.arena.conceptview.ConceptView;
import org.ihtsdo.arena.conceptview.ConceptViewSettings;
import org.ihtsdo.arena.conceptview.DragPanelComponentVersion;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

/**
 *
 * @author kec
 */
public class HistoryPanel {

    protected static final int HISTORY_LABEL_WIDTH = 14;
    protected static final int insetAdjustment = 3;
    protected static final int xStartLoc = 5;
    //~--- fields --------------------------------------------------------------
    int otherWidth = 150;
    private final SelectedVersionChangedListener svcl = new SelectedVersionChangedListener();
    VersionCheckHeader header = new VersionCheckHeader(this);

    public JPanel getParentHistoryPanel() {
        return header.getParentHistoryPanel();
    }
    private final Set<JRadioButton> originalButtonSelections = new HashSet<>();
    private final Map<NidSapNid, JRadioButton> nidStampNidButtonMap = new HashMap<>();
    /**
     * Map of component native identifiers to the button group for its versions.
     */
    private final Map<Integer, ButtonGroup> nidGroupMap = new HashMap<>();
    HorizonatalScrollActionListener hsal = new HorizonatalScrollActionListener();
    //J-
    //J+
    private final Set<JRadioButton> changedSelections = new HashSet<>();
    private final Map<JRadioButton, ComponentVersionBI> buttonVersionMap = new HashMap<>();
    private final Map<JRadioButton, Integer> buttonStampMap = new HashMap<>();
    private final Map<JRadioButton, Set<JComponent>> buttonPanelSetMap = new HashMap<>();
    private ApplyVersionChangesListener avcl = new ApplyVersionChangesListener();
    VerticalScrollActionListener vsal = new VerticalScrollActionListener();
    TopChangeListener tcl = new TopChangeListener();
    private List<Rectangle> seperators = new ArrayList<>();
    private final ConceptNavigator navigator;
    private Map<PositionBI, Collection<DragPanelComponentVersion<?>>> positionPanelMap;
    ScrollableHxPanel versionPanel;
    ConceptView view;
    int conceptNid = Integer.MAX_VALUE;
    JScrollPane versionPanelScroller;
    private final JScrollPane historyScroller;
    private int scrollerXLocation = Integer.MIN_VALUE;

    public int getScrollerXLocation() {
        return scrollerXLocation;
    }

    public void setScrollerXLocation(int scrollerXLocation) {
        this.scrollerXLocation = scrollerXLocation;
    }

    //~--- constructors --------------------------------------------------------
    public HistoryPanel(ConceptView view, JScrollPane historyScroller, ConceptNavigator navigator)
            throws IOException {
        this.view = view;
        this.historyScroller = historyScroller;
        this.historyScroller.getHorizontalScrollBar().getModel().addChangeListener(new HorizontalScrollActionListener());
        positionPanelMap = view.getPositionPanelMap();
        this.navigator = navigator;
        navigator.getImplementButton().addActionListener(avcl);

        if (view.getSettings().isForAdjudication() || view.getSettings().isForPromotion()) {
            navigator.getImplementButton().setEnabled(true);
        }
        historyScroller.setViewportView(header.getParentHistoryPanel());
        TreeSet<PositionBI> positionOrderedSet = view.getPositionOrderedSet();
        initializeVersionPanel();

        if ((view.getPathRowMap() != null) && (positionOrderedSet != null)) {
            header.setup();
            combineHistoryPanels();
            setupVersionPanel(positionPanelMap);
            header.versionCheckHeaderPanel.addComponentListener(new TopChangeListener());
        }

        otherWidth = ConceptViewSettings.NAVIGATOR_WIDTH - 6;
        header.versionCheckHeaderPanel.setSize(Math.max(header.hxWidth, ConceptViewSettings.NAVIGATOR_WIDTH - 6),
                view.getPathCheckboxPanel().getHeight());
        header.versionCheckHeaderPanel.setMinimumSize(header.versionCheckHeaderPanel.getSize());
        header.versionCheckHeaderPanel.setPreferredSize(header.versionCheckHeaderPanel.getSize());
        header.versionCheckHeaderPanel.setMaximumSize(header.versionCheckHeaderPanel.getSize());


        ((JScrollPane) view.getParent().getParent()).getVerticalScrollBar().getModel().addChangeListener(vsal);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                redoLayout();
                scrollRightIfChanged();
            }
        });
    }

    public Map<PathBI, Boolean> getPathCheckMap() {
        return header.getPathCheckMap();
    }

    //~--- methods -------------------------------------------------------------
    private void combineHistoryPanels() {

        for (JRadioButton button : buttonStampMap.keySet()) {
            button.setVisible(false);
        }
        sizeVersionPanel();
    }

    private void processAllPositions(ComponentVersionBI version, DragPanelComponentVersion<?> dragPanel)
            throws IOException {
        Collection<ComponentVersionBI> positionVersions = version.getChronicle().getVersions();
        ButtonGroup group = getButtonGroup(version.getNid());

        for (ComponentVersionBI positionVersion : positionVersions) {
            processPosition(group, version, positionVersion, dragPanel);
        }

    }

    private void processPanel(DragPanelComponentVersion<?> dragPanel) throws IOException {
        ComponentVersionBI version = dragPanel.getComponentVersion();
        List<DragPanelComponentVersion<?>> versionPanels = getVersionPanels(dragPanel);
        List<DragPanelComponentVersion<?>> refexPanels = getRefexPanels(dragPanel);

        if (versionPanels.isEmpty()) {
            processAllPositions(version, dragPanel);
        } else {
            ButtonGroup group = getButtonGroup(version.getNid());

            processPosition(group, version, version, dragPanel);

            for (DragPanelComponentVersion panel : versionPanels) {
                processPosition(group, version, panel.getComponentVersion(), panel);
            }
        }

        for (DragPanelComponentVersion refexPanel : refexPanels) {
            processPanel(refexPanel);
        }
    }

    private void processPosition(ButtonGroup group, ComponentVersionBI viewVersion,
            ComponentVersionBI positionVersion, DragPanelComponentVersion<?> dragPanel) {
        try {
            boolean add = false;
            PositionBI p = positionVersion.getPosition();
            int stamp = positionVersion.getStampNid();
            NidSapNid nidSapNidKey = new NidSapNid(viewVersion.getNid(), stamp);
            JRadioButton button = nidStampNidButtonMap.get(nidSapNidKey);

            if (button == null) {
                button = new JRadioButton();
                button.addChangeListener(svcl);
                buttonVersionMap.put(button, positionVersion);

                if (positionVersion.getTime() == Long.MAX_VALUE) {

                    // unimplemented change
                    button.setBackground(Color.YELLOW);
                    button.setOpaque(true);
                }

                if (view.getChangedVersionSelections().contains(positionVersion)) {
                    button.setBackground(Color.ORANGE);
                    button.setOpaque(true);
                    button.setSelected(true);
                    changedSelections.add(button);
                }

                if (viewVersion == positionVersion) {
                    originalButtonSelections.add(button);

                    if (group.getSelection() == null) {
                        button.setSelected(true);
                    }
                }

                nidStampNidButtonMap.put(nidSapNidKey, button);
                buttonStampMap.put(button, stamp);
                add = true;

                ConceptVersionBI author = Ts.get().getConceptVersion(view.getConfig().getViewCoordinate(),
                        positionVersion.getAuthorNid());
                ConceptVersionBI status = Ts.get().getConceptVersion(view.getConfig().getViewCoordinate(),
                        positionVersion.getStatusNid());
                StringBuilder sb = makeButtonToolTip(positionVersion, status, author, p, viewVersion);
                button.setToolTipText(sb.toString());
            }

            boolean enableButton = true;
            button.setEnabled(enableButton);
            putPanelInButtonMap(button, dragPanel);

            if (group != null) {
                if (add) {
                    group.add(button);
                }
            }

            int yLoc = dragPanel.getY();
            Component parentPanel = dragPanel.getParent();

            while ((parentPanel != null) && (parentPanel != view)) {
                yLoc += parentPanel.getY();
                parentPanel = parentPanel.getParent();
            }

            JCheckBox positionCheck = header.positionCheckMap.get(p);

            if (positionCheck != null) {
                button.setVisible(positionCheck.isVisible());
                List<JRadioButton> components = header.checkComponentMap.get(positionCheck);
                if (components == null) {
                    components = new ArrayList<>();
                    header.checkComponentMap.put(positionCheck, components);
                }

                components.add(button);
                button.setLocation(positionCheck.getX(), yLoc);

                versionPanel.add(button);


                button.setSize(button.getPreferredSize());
            }
        } catch (Throwable ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void putPanelInButtonMap(JRadioButton button, JComponent panel) {
        Set<JComponent> panels = buttonPanelSetMap.get(button);

        if (panels == null) {
            panels = new HashSet<>();
            buttonPanelSetMap.put(button, panels);
        }

        panels.add(panel);
    }

    private void redoGrid() {
        //initializeVersionPanel();

        seperators.clear();
        for (JComponent comp : view.getSeperatorComponents()) {
            if (comp.isVisible() && comp.getParent() != null) {
                Point location = comp.getLocation();
                location.y = location.y - comp.getInsets().top - 1;

                Container parent = comp.getParent();
                while (view != parent && parent != null) {
                    location.y = location.y + parent.getY();
                    parent = parent.getParent();
                }

                Rectangle sep = new Rectangle(0, location.y,
                        Math.max(versionPanel.getWidth() + 240, ConceptViewSettings.NAVIGATOR_WIDTH - 3),
                        comp.getHeight());
                seperators.add(sep);
            }
        }
        scrollRightIfChanged();
    }

    private void redoLayout() {
        if ((changedSelections.size() > 0) || view.getSettings().isForAdjudication()) {
            navigator.getImplementButton().setEnabled(true);
        } else {
            navigator.getImplementButton().setEnabled(false);
        }

        int currentX = xStartLoc;

        next:
        for (PositionBI p : view.getPositionOrderedSet()) {
            JCheckBox positionCheck = header.positionCheckMap.get(p);
            PositionBI position = header.checkPositionMap.get(positionCheck);

            if (position == null) {
                continue next;
            }

            Integer row = view.getPathRowMap().get(position.getPath());

            if (row == null) {
                continue next;
            }

            JCheckBox rowCheck = view.getRowToPathCheckMap().get(row);
            boolean showPosition = rowCheck.isSelected();

            positionCheck.setVisible(showPosition);
            positionCheck.setLocation(currentX, positionCheck.getY());

            for (JComponent componentInColumn : header.checkComponentMap.get(positionCheck)) {
                if (componentInColumn instanceof JRadioButton) {
                    JRadioButton radioButton = (JRadioButton) componentInColumn;

                    if (showPosition) {
                        if (isPanelVisibleForButton(radioButton)) {
                            componentInColumn.setVisible(true);

                            int maxY = 0;
                            Point location = new Point();

                            for (JComponent panel : buttonPanelSetMap.get(radioButton)) {
                                if ((panel.getParent() != null) && panel.isVisible()) {
                                    location = panel.getLocation();
                                    location.y = location.y - 2;
                                    Container parent = panel.getParent();
                                    while (view != parent && parent != null) {
                                        location.y = location.y + parent.getY();
                                        parent = parent.getParent();
                                    }

                                    maxY = Math.max(maxY, location.y);
                                }
                            }

                            location.y = maxY;
                            componentInColumn.setLocation(currentX, location.y);
                        } else {
                            componentInColumn.setVisible(false);
                        }
                    }
                } else {
                    componentInColumn.setVisible(showPosition);
                }

                componentInColumn.setLocation(currentX, componentInColumn.getY());
            }

            if (showPosition) {
                currentX += positionCheck.getWidth();
            }
        }
        currentX = currentX + 240;
        header.hxWidth = currentX;
        header.versionCheckHeaderPanel.setSize(Math.max(header.hxWidth, ConceptViewSettings.NAVIGATOR_WIDTH - 6),
                view.getPathCheckboxPanel().getHeight());
        header.versionCheckHeaderPanel.setMinimumSize(header.versionCheckHeaderPanel.getSize());
        header.versionCheckHeaderPanel.setPreferredSize(header.versionCheckHeaderPanel.getSize());
        header.versionCheckHeaderPanel.setMaximumSize(header.versionCheckHeaderPanel.getSize());
        versionPanel.setSize(Math.max(header.hxWidth + 240, ConceptViewSettings.NAVIGATOR_WIDTH - 6) + 20,
                versionPanel.getHeight());
        if (currentX > 5) {
            redoGrid();
        }
    }

    boolean conceptChanged() {
        if (view.getConcept() != null && conceptNid == view.getConcept().getConceptNid()) {
            return false;
        }
        maxX = Integer.MIN_VALUE;
        return true;
    }

    private void scrollRightIfChanged() {
        if (conceptChanged()) {
            if (view.getConcept() != null) {
                conceptNid = view.getConcept().getConceptNid();
            } else {
                conceptNid = Integer.MAX_VALUE;
            }
        }
    }

    public int getHxWidth() {
        return header.getHxWidth();
    }

    private void reset() {
        nidGroupMap.clear();
        nidStampNidButtonMap.clear();
        buttonStampMap.clear();
        buttonPanelSetMap.clear();
        originalButtonSelections.clear();
        changedSelections.clear();

        view.getChangedVersionSelections().clear();
        for (List<JRadioButton> radioButtonList : header.checkComponentMap.values()) {
            for (JRadioButton versionRadioButton : radioButtonList) {
                versionPanel.remove(versionRadioButton);
            }
        }
        buttonVersionMap.clear();
        positionPanelMap.clear();

    }

    public void updateHistoryLayout() {
        try {
            reset();
            positionPanelMap = view.getPositionPanelMap();
            header.setupVersionCheckHeader();
            combineHistoryPanels();
            setupVersionPanel(positionPanelMap);
            sizeVersionPanel();
            redoLayout();
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    public static JLabel setupLabel(String hxString, int locX) {
        JLabel historyLabel = new JLabel("");

        historyLabel.setVisible(false);
        historyLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        historyLabel.setLocation(locX, 0);
        historyLabel.setSize(HISTORY_LABEL_WIDTH, 1000);
        historyLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
                Color.GRAY), new HistoryBorder(BorderFactory.createEmptyBorder(), hxString,
                new Font("monospaced", Font.PLAIN, 12), Color.BLACK)));

        return historyLabel;
    }

    private void setupVersionPanel(Map<PositionBI, Collection<DragPanelComponentVersion<?>>> positionPanelMap)
            throws IOException {
        for (Collection<DragPanelComponentVersion<?>> panelSet : positionPanelMap.values()) {
            for (DragPanelComponentVersion<?> dragPanel : panelSet) {
                processPanel(dragPanel);
            }
        }
    }

    private void syncVerticalLayout() {
        BoundedRangeModel eventModel =
                view.getCvRenderer().getConceptScrollPane().getVerticalScrollBar().getModel();

        versionPanel.setLocation(versionPanel.getX(), -eventModel.getValue());
    }

    //~--- get methods ---------------------------------------------------------
    private ButtonGroup getButtonGroup(int componentNid) {
        ButtonGroup group = nidGroupMap.get(componentNid);

        if (group == null) {
            group = new ButtonGroup();
            nidGroupMap.put(componentNid, group);
        }

        return group;
    }

    private List<DragPanelComponentVersion<?>> getRefexPanels(DragPanelComponentVersion<?> dragPanel) {
        List<DragPanelComponentVersion<?>> versionPanels = new ArrayList<>();

        for (Component comp : dragPanel.getComponents()) {
            if (comp.isVisible() && (comp instanceof DragPanelComponentVersion)) {
                DragPanelComponentVersion cvdp = (DragPanelComponentVersion) comp;

                if (!cvdp.getComponentVersion().getChronicle().equals(
                        dragPanel.getComponentVersion().getChronicle())) {
                    versionPanels.add(cvdp);
                }
            }
        }

        return versionPanels;
    }

    private int getVersionPanelHeight() {
        return view.getParent().getParent().getHeight()
                + (view.getPathCheckboxPanel().getHeight() + view.getPathCheckboxPanel().getY() + 24);
    }

    private List<DragPanelComponentVersion<?>> getVersionPanels(DragPanelComponentVersion<?> dragPanel)
            throws IOException {
        List<DragPanelComponentVersion<?>> versionPanels = new ArrayList<>();

        for (Component comp : dragPanel.getComponents()) {
            if (comp instanceof DragPanelComponentVersion) {
                DragPanelComponentVersion cvdp = (DragPanelComponentVersion) comp;

                if (cvdp.isVisible()) {
                    if (cvdp.getComponentVersion().getChronicle().equals(
                            dragPanel.getComponentVersion().getChronicle())) {
                        versionPanels.add(cvdp);
                    }
                }
            }
        }

        return versionPanels;
    }

    private boolean isPanelVisibleForButton(JRadioButton button) {
        Set<JComponent> panels = buttonPanelSetMap.get(button);

        if (panels == null) {
            return false;
        }

        boolean visible = false;

        for (JComponent panel : panels) {
            if (panel.isVisible() && panel.getWidth() > 0 && panel.getHeight() > 0) {
                return true;
            }
        }

        return visible;
    }

    private void initializeVersionPanel() {
        versionPanel = new ScrollableHxPanel();
        versionPanel.setSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        versionPanel.addHierarchyListener(new HistoryHierarchyListener());
    }
    
    private void sizeVersionPanel() {
        versionPanel.setSize(Math.max(header.hxWidth + 240, ConceptViewSettings.NAVIGATOR_WIDTH) + 20,
                view.getHeight() + 20);
        versionPanel.setPreferredSize(versionPanel.getSize());
        versionPanel.setMinimumSize(versionPanel.getSize());
        versionPanel.setMaximumSize(versionPanel.getSize());
        syncVerticalLayout();
    }

    private StringBuilder makeButtonToolTip(ComponentVersionBI positionVersion, ConceptVersionBI status, ConceptVersionBI author, PositionBI p, ComponentVersionBI viewVersion) throws IOException, ContradictionException {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append(
                positionVersion.toUserString(Ts.get().getSnapshot(view.getConfig().getViewCoordinate())));
        sb.append("<br>");
        if (status.getDescriptionPreferred() != null) {
            sb.append(status.getDescriptionPreferred().getText());
        } else {
            sb.append(status.toString());
        }
        sb.append("<br>");
        if (author.getDescriptionPreferred() != null) {
            sb.append(author.getDescriptionPreferred().getText());
        } else {
            sb.append(author.toString());
        }
        sb.append("<br>");
        sb.append(p.toString());
        if (viewVersion == positionVersion) {
            sb.append("<br>");
            sb.append("<font color='#ff0000'>");
            sb.append("latest on view</font>");
        }
        sb.append("</html>");
        return sb;
    }

    PropertyChangeListener getMaxXListener() {
        return new MaxXPropChangeListener();
    }

    //~--- inner classes -------------------------------------------------------
    private class ApplyVersionChangesListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            try {
                EditCoordinate ec = view.getConfig().getEditCoordinate();

                for (JRadioButton button : changedSelections) {
                    ComponentVersionBI cv = buttonVersionMap.get(button);

                    for (int pathNid : ec.getEditPaths()) {
                        ((AnalogGeneratorBI) cv).makeAnalog(cv.getStatusNid(),
                                Long.MAX_VALUE,
                                ec.getAuthorNid(),
                                ec.getModuleNid(),
                                pathNid);
                    }
                }
                if (view.getSettings().isForAdjudication()) {
                    ConceptChronicleBI cc = view.getConcept();
                    ViewCoordinate adjudicateView = new ViewCoordinate(view.getConfig().getViewCoordinate());

                    cc.makeAdjudicationAnalogs(view.getConfig().getEditCoordinate(),
                            adjudicateView);
                }

                Ts.get().addUncommitted(view.getConcept());
                updateHistoryLayout();
                navigator.getImplementButton().setEnabled(false);
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    private class HistoryHierarchyListener implements HierarchyListener {

        @Override
        public void hierarchyChanged(HierarchyEvent he) {
            syncVerticalLayout();
        }
    }

    private class HorizonatalScrollActionListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent ce) {
            BoundedRangeModel eventModel = (BoundedRangeModel) ce.getSource();
            BoundedRangeModel historyScrollModel =
                    header.historyHeaderPanelScroller.getHorizontalScrollBar().getModel();

            historyScrollModel.setMaximum(eventModel.getMaximum());
            historyScrollModel.setMinimum(eventModel.getMinimum());
            historyScrollModel.setValue(eventModel.getValue());
        }
    }

    private static class NidSapNid {

        int nid;
        int sapNid;

        //~--- constructors -----------------------------------------------------
        public NidSapNid(int nid, int sapNid) {
            this.nid = nid;
            this.sapNid = sapNid;
        }

        //~--- methods ----------------------------------------------------------
        @Override
        public boolean equals(Object o) {
            if (o instanceof NidSapNid) {
                NidSapNid another = (NidSapNid) o;

                return (nid == another.nid) && (sapNid == another.sapNid);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return Hashcode.compute(new int[]{nid, sapNid});
        }
    }

    public class ScrollableHxPanel extends JPanel implements Scrollable {

        Color standardBackground;

        public ScrollableHxPanel() {
            super(null);
            standardBackground = getBackground();
            setBackground(Color.LIGHT_GRAY);
        }

        //~--- get methods ------------------------------------------------------
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(Math.max(otherWidth, ConceptViewSettings.NAVIGATOR_WIDTH - 6),
                    view.getHeight());
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 1;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return false;
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 1;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            Color fillColor = g2d.getColor();
            for (Rectangle rect : seperators) {
                g2d.setColor(standardBackground);
                g2d.fill(rect);
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.draw(rect);
            }

            FontRenderContext frc = g2d.getFontRenderContext();

            Font f = new Font("Lucidia Sans", Font.PLAIN, 12);

            List<Rectangle> textRects = new ArrayList<>();
// Draw lines first
            g2d.setColor(Color.WHITE);
            SortedSet<JCheckBox> checkSet = new TreeSet<>(new Comparator<JCheckBox>() {
                @Override
                public int compare(JCheckBox o1, JCheckBox o2) {
                    return o1.getX() - o2.getX();
                }
            });
            for (JCheckBox positionCheck : header.checkPositionMap.keySet()) {
                if (positionCheck.isVisible()
                        && positionCheck.getParent() != null
                        && positionCheck.isSelected()) {
                    int x = positionCheck.getX();
                    g2d.drawLine(x, -100, x, 2000);
                    checkSet.add(positionCheck);
                }
            }



            for (JCheckBox positionCheck : checkSet) {
                if (positionCheck.isVisible()
                        && positionCheck.getParent() != null
                        && positionCheck.isSelected()) {
                    int x = positionCheck.getX() + 2;

                    PositionBI p = header.checkPositionMap.get(positionCheck);
                    TextLayout tl = new TextLayout(p.toString(), f, frc);
                    float incrementHeight = tl.getAscent();

                    float y = -versionPanel.getY();
                    boolean found = false;
                    while (found == false) {
                        boolean drawOK = true;
                        y = y + incrementHeight;
                        g2d.setColor(Color.BLACK);
                        Rectangle tlRect = tl.getPixelBounds(frc, x, y);
                        tlRect.grow(2, 2);
                        for (Rectangle r : textRects) {
                            if (r.intersects(tlRect)) {
                                drawOK = false;
                                break;
                            }
                        }
                        if (drawOK) {
                            for (Component component : getComponents()) {
                                if (component.isVisible() && component.getBounds().intersects(tlRect)) {
                                    drawOK = false;
                                    break;
                                }
                            }
                            if (drawOK) {
                                found = true;
                                textRects.add(tlRect);
                                g2d.setColor(Color.WHITE);
                                g2d.fill(tlRect);
                                g2d.setColor(Color.decode("#E0E0E0"));
                                g2d.draw(tlRect);
                                g2d.setColor(Color.BLACK);
                                tl.draw(g2d, x, y);
                            }
                        }
                    }
                }
            }
            g2d.setColor(fillColor);
        }

        @Override
        public void setLocation(int x, int y) {
            JScrollPane parentScrollPane = (JScrollPane) getParent().getParent();
            x = Math.max(parentScrollPane.getHorizontalScrollBar().getValue(), 0);
            y = vsal.y;
            super.setLocation(x, y);
        }

        @Override
        public void setLocation(Point p) {
            super.setLocation(p);
        }
    }

    private class SelectedVersionChangedListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent ce) {
            JRadioButton button = (JRadioButton) ce.getSource();
            ComponentVersionBI version = buttonVersionMap.get(button);
            int changedCount = changedSelections.size();

            if (button.isSelected()) {
                if (originalButtonSelections.contains(button)) {

                    // back to original state
                    if (version.getTime() == Long.MAX_VALUE) {
                        button.setBackground(Color.YELLOW);
                        button.setOpaque(true);
                    }
                } else {

                    // unimplemented change
                    button.setBackground(Color.ORANGE);
                    button.setOpaque(true);
                    changedSelections.add(button);
                    view.getChangedVersionSelections().add(version);
                }
            } else {
                button.setBackground(versionPanel.getBackground());
                button.setOpaque(false);
                changedSelections.remove(button);
                view.getChangedVersionSelections().remove(version);
            }

            if (changedCount != changedSelections.size()) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if ((changedSelections.size() > 0) || view.getSettings().isForAdjudication()) {
                            navigator.getImplementButton().setEnabled(true);
                        } else {
                            navigator.getImplementButton().setEnabled(false);
                        }
                    }
                });
            }
        }
    }

    private class TopChangeListener implements ComponentListener {

        int lastId = Integer.MIN_VALUE;

        @Override
        public void componentHidden(ComponentEvent ce) {
            //
        }

        @Override
        public void componentMoved(ComponentEvent ce) {
            //
        }

        @Override
        public void componentResized(ComponentEvent ce) {
            if (ce.getID() != lastId) {
                lastId = ce.getID();
                updateHistoryLayout();
            }
        }

        @Override
        public void componentShown(ComponentEvent ce) {
            syncVerticalLayout();
        }
    }

    ActionListener getUpdateHistoryBorderActionListener(JLabel historyLabel, JLabel versionHxLabel) {
        return new UpdateHistoryBorder(historyLabel, versionHxLabel);
    }

    public class UpdateHistoryBorder implements ActionListener {

        JLabel hxLabel;
        JLabel versionHxLabel;
        long last = Long.MIN_VALUE;

        //~--- constructors -----------------------------------------------------
        public UpdateHistoryBorder(JLabel historyLabel, JLabel versionHxLabel) {
            this.hxLabel = historyLabel;
            this.versionHxLabel = versionHxLabel;
        }

        //~--- methods ----------------------------------------------------------
        @Override
        public void actionPerformed(ActionEvent ae) {
            JCheckBox check = (JCheckBox) ae.getSource();
            if (ae.getWhen() != last) {
                last = ae.getWhen();
                if (check.isSelected()) {

                    hxLabel.setVisible(true);
                    versionHxLabel.setVisible(true);
                } else {
                    hxLabel.setVisible(false);
                    versionHxLabel.setVisible(false);
                }

                updateHistoryLayout();
            }
        }
    }

    private class VerticalScrollActionListener implements ChangeListener {

        int y = 0;

        @Override
        public void stateChanged(ChangeEvent ce) {
            BoundedRangeModel eventModel = (BoundedRangeModel) ce.getSource();
            y = -eventModel.getValue();
            versionPanel.setLocation(0, -eventModel.getValue());
        }
    }

    private class HorizontalScrollActionListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent ce) {
            BoundedRangeModel eventModel = (BoundedRangeModel) ce.getSource();
            scrollerXLocation = eventModel.getValue();
        }
    }
    int maxX = Integer.MIN_VALUE;

    private class MaxXPropChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            int newMaxX = ((Integer) evt.getNewValue());
            if (newMaxX > maxX) {
                maxX = newMaxX;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        historyScroller.revalidate();
                        historyScroller.getHorizontalScrollBar().setValue(maxX);
                        historyScroller.getViewport().scrollRectToVisible(
                                new Rectangle(maxX, 0, 2, 4));
                    }
                });
            }
        }
    }
}
