
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.arena.conceptview;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.log.AceLog;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.hash.Hashcode;
import org.ihtsdo.util.swing.GuiUtil;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ihtsdo.arena.contradiction.InteractiveAdjudicator;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

/**
 *
 * @author kec
 */
public class HistoryPanel {
   private static final int HISTORY_LABEL_WIDTH = 14;
   private static final int insetAdjustment     = 3;
   private static final int xStartLoc           = 5;

   //~--- fields --------------------------------------------------------------

   int                                                 hxWidth                           = 0;
   int                                                 otherWidth                        = 150;
   JPanel                                              topHistoryPanel                   =
      new JPanel(new GridBagLayout());
   private final SelectedVersionChangedListener        svcl                              =
      new SelectedVersionChangedListener();
   Map<JCheckBox, JLabel>                              positionVersionPanelCheckLabelMap =
      new HashMap<JCheckBox, JLabel>();
   Map<JCheckBox, JLabel>                              positionHeaderCheckLabelMap       =
      new HashMap<JCheckBox, JLabel>();
   Map<PositionBI, JCheckBox>                          positionCheckMap                  =
      new HashMap<PositionBI, JCheckBox>();
   private final Set<JRadioButton>                     originalButtonSelections          =
      new HashSet<JRadioButton>();
   private final Map<NidSapNid, JRadioButton>          nidSapNidButtonMap                =
      new HashMap<NidSapNid, JRadioButton>();
   private final Map<Integer, ButtonGroup>             nidGroupMap                       =
      new HashMap<Integer, ButtonGroup>();
   private final Map<Integer, ButtonGroup>             inferredNidGroupMap               =
      new HashMap<Integer, ButtonGroup>();
   HorizonatalScrollActionListener                     hsal                              =
      new HorizonatalScrollActionListener();
   //J-
   JPanel                                              historyHeaderPanel                = new JPanel(null);
   JScrollPane                                         historyHeaderPanelScroller        =
      new JScrollPane(historyHeaderPanel);
   //J+
   Map<JCheckBox, PositionBI>                          checkPositionMap                  =
      new HashMap<JCheckBox, PositionBI>();
   Map<JCheckBox, List<JComponent>>                    checkComponentMap                 =
      new HashMap<JCheckBox, List<JComponent>>();
   private final Set<JRadioButton>                     changedSelections                 =
      new HashSet<JRadioButton>();
   private final Map<JRadioButton, ComponentVersionBI> buttonVersionMap                  =
      new HashMap<JRadioButton, ComponentVersionBI>();
   private final Map<JRadioButton, Integer>         buttonSapMap      = new HashMap<JRadioButton, Integer>();
   private final Map<JRadioButton, Set<JComponent>> buttonPanelSetMap = new HashMap<JRadioButton,
                                                                           Set<JComponent>>();
   private ApplyVersionChangesListener                                     avcl       =
      new ApplyVersionChangesListener();
   VerticalScrollActionListener                                            vsal       =
      new VerticalScrollActionListener();
   TopChangeListener                                                       tcl        =
      new TopChangeListener();
   private List<JSeparator>                                                seperators =
      new ArrayList<JSeparator>();
   private final ConceptNavigator                                          navigator;
   private final Map<PositionBI, Collection<DragPanelComponentVersion<?>>> positionPanelMap;
   JPanel                                                                  versionPanel;
   JScrollPane                                                             versionScroller;
   ConceptView                                                             view;

   //~--- constructors --------------------------------------------------------

   public HistoryPanel(ConceptView view, JScrollPane historyScroller, ConceptNavigator navigator)
           throws IOException {
      this.view        = view;
      versionPanel     = new ScrollableHxPanel();
      versionScroller  = new JScrollPane(versionPanel);
      positionPanelMap = view.getPositionPanelMap();
      this.navigator   = navigator;
      navigator.getImplementButton().addActionListener(avcl);

      if (view.getSettings().isForAdjudication()) {
         navigator.getImplementButton().setEnabled(true);
      }

      TreeSet<PositionBI> positionOrderedSet = view.getPositionOrderedSet();

      if ((view.getPathRowMap() != null) && (positionOrderedSet != null)) {
         setupHeader(view);
         combineHistoryPanels();
         setupVersionPanel(positionPanelMap);
         topHistoryPanel.addComponentListener(new TopChangeListener());
         historyScroller.setViewportView(topHistoryPanel);
      }

      otherWidth = ConceptViewSettings.NAVIGATOR_WIDTH - 6;
      historyHeaderPanel.setSize(Math.max(hxWidth, ConceptViewSettings.NAVIGATOR_WIDTH - 6),
                                 view.getHistoryPanel().getHeight());
      historyHeaderPanel.setMinimumSize(historyHeaderPanel.getSize());
      historyHeaderPanel.setPreferredSize(historyHeaderPanel.getSize());
      historyHeaderPanel.setMaximumSize(historyHeaderPanel.getSize());
      versionPanel.setSize(Math.max(hxWidth, ConceptViewSettings.NAVIGATOR_WIDTH - 6),
                           getVersionPanelHeight());
      versionPanel.setMinimumSize(versionPanel.getSize());
      versionPanel.setPreferredSize(versionPanel.getSize());
      versionPanel.setMaximumSize(versionPanel.getSize());
      versionPanel.addHierarchyListener(new HistoryHierarchyListener());
      versionScroller.getHorizontalScrollBar().getModel().addChangeListener(hsal);
      ((JScrollPane) view.getParent().getParent()).getVerticalScrollBar().getModel().addChangeListener(vsal);
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            redoLayout();
         }
      });
   }

   //~--- methods -------------------------------------------------------------

   private void combineHistoryPanels() {
      topHistoryPanel.removeAll();

      for (JRadioButton button : buttonSapMap.keySet()) {
         button.setVisible(false);
      }

      historyHeaderPanel.setSize(otherWidth, view.getHistoryPanel().getHeight() - insetAdjustment);
      historyHeaderPanel.setMaximumSize(historyHeaderPanel.getSize());
      historyHeaderPanel.setMinimumSize(historyHeaderPanel.getSize());
      historyHeaderPanel.setPreferredSize(historyHeaderPanel.getSize());
      historyHeaderPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

      GridBagConstraints gbc = new GridBagConstraints();

      gbc.weightx = 1.0;
      gbc.weighty = 0.0;
      gbc.gridx   = 0;
      gbc.gridy   = 0;
      gbc.fill    = GridBagConstraints.HORIZONTAL;
      gbc.anchor  = GridBagConstraints.NORTHWEST;
      topHistoryPanel.add(historyHeaderPanelScroller, gbc);
      historyHeaderPanelScroller.setLocation(0, 0);
      historyHeaderPanelScroller.setBorder(BorderFactory.createEmptyBorder());
      historyHeaderPanelScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
      historyHeaderPanelScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      historyHeaderPanelScroller.setSize(otherWidth, view.getHistoryPanel().getHeight());
      historyHeaderPanelScroller.setLocation(0, 0);
      gbc.gridy++;
      gbc.weighty = 1.0;
      topHistoryPanel.add(versionScroller, gbc);
      versionScroller.setSize(otherWidth, view.getParent().getHeight() + 24 + insetAdjustment);
      versionScroller.setLocation(0, historyHeaderPanel.getHeight() + 1);
      versionScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
      versionScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      versionScroller.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
      versionPanel.setLocation(0, 0);
      versionPanel.setSize(Math.max(otherWidth, ConceptViewSettings.NAVIGATOR_WIDTH - 6),
                           getVersionPanelHeight());
      versionPanel.setPreferredSize(versionPanel.getSize());
      versionPanel.setMinimumSize(versionPanel.getSize());
      versionPanel.setMaximumSize(versionPanel.getSize());
      syncVerticalLayout();
   }

   private void processAllPositions(ComponentVersionBI version, DragPanelComponentVersion<?> dragPanel)
           throws IOException {
      if (version instanceof RelationshipVersionBI) {
         RelationshipVersionBI             rv               = (RelationshipVersionBI) version;
         Collection<RelationshipVersionBI> positionVersions = rv.getChronicle().getVersions();
         ButtonGroup                       group            = getButtonGroup(version.getNid(),
                                                                 rv.isInferred());

         for (RelationshipVersionBI positionVersion : positionVersions) {
            if (positionVersion.isInferred() == rv.isInferred()) {
               processPosition(group, version, positionVersion, dragPanel);
            }
         }
      } else {
         Collection<ComponentVersionBI> positionVersions = version.getChronicle().getVersions();
         ButtonGroup                    group            = getButtonGroup(version.getNid(), false);

         for (ComponentVersionBI positionVersion : positionVersions) {
            processPosition(group, version, positionVersion, dragPanel);
         }
      }
   }

   private void processPanel(DragPanelComponentVersion<?> dragPanel) throws IOException {
      ComponentVersionBI                 version       = dragPanel.getComponentVersion();
      List<DragPanelComponentVersion<?>> versionPanels = getVersionPanels(dragPanel);
      List<DragPanelComponentVersion<?>> refexPanels   = getRefexPanels(dragPanel);

      if (versionPanels.isEmpty()) {
         processAllPositions(version, dragPanel);
      } else {
         boolean inferred = false;

         if (version instanceof RelationshipVersionBI) {
            inferred = ((RelationshipVersionBI) version).isInferred();
         }

         ButtonGroup group = getButtonGroup(version.getNid(), inferred);

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
         boolean      add          = false;
         PositionBI   p            = positionVersion.getPosition();
         int          sapNid       = positionVersion.getSapNid();
         NidSapNid    nidSapNidKey = new NidSapNid(viewVersion.getNid(), sapNid);
         JRadioButton button       = nidSapNidButtonMap.get(nidSapNidKey);

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

            nidSapNidButtonMap.put(nidSapNidKey, button);
            buttonSapMap.put(button, sapNid);
            add = true;

            ConceptVersionBI author = Ts.get().getConceptVersion(view.getConfig().getViewCoordinate(),
                                         positionVersion.getAuthorNid());
            ConceptVersionBI status = Ts.get().getConceptVersion(view.getConfig().getViewCoordinate(),
                                         positionVersion.getStatusNid());
            StringBuilder sb = new StringBuilder();

            sb.append("<html>");
            sb.append(
                positionVersion.toUserString(Ts.get().getSnapshot(view.getConfig().getViewCoordinate())));
            sb.append("<br>");

            if (status.getPreferredDescription() != null) {
               sb.append(status.getPreferredDescription().getText());
            } else {
               sb.append(status.toString());
            }

            sb.append("<br>");

            if (author.getPreferredDescription() != null) {
               sb.append(author.getPreferredDescription().getText());
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
            button.setToolTipText(sb.toString());
         }

         boolean enableButton = true;

         if (positionVersion instanceof RelationshipVersionBI) {
            RelationshipVersionBI rv = (RelationshipVersionBI) positionVersion;

            if (rv.isInferred()) {
               enableButton = false;
            }
         }

         button.setEnabled(enableButton);
         putPanelInButtonMap(button, dragPanel);

         if (group != null) {
            if (add) {
               group.add(button);
            }
         }

         int       yLoc        = dragPanel.getY();
         Component parentPanel = dragPanel.getParent();

         while ((parentPanel != null) && (parentPanel != view)) {
            yLoc        += parentPanel.getY();
            parentPanel = parentPanel.getParent();
         }

         JCheckBox positionCheck = positionCheckMap.get(p);

         if (positionCheck != null) {
            button.setVisible(positionCheck.isVisible());
            checkComponentMap.get(positionCheck).add(button);
            button.setLocation(positionCheck.getX(), yLoc);

            if (add) {
               versionPanel.add(button);
            }

            button.setSize(button.getPreferredSize());
         }
      } catch (Throwable ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }
   }

   private void putPanelInButtonMap(JRadioButton button, JComponent panel) {
      Set<JComponent> panels = buttonPanelSetMap.get(button);

      if (panels == null) {
         panels = new HashSet<JComponent>();
         buttonPanelSetMap.put(button, panels);
      }

      panels.add(panel);
   }

   private void redoGrid() {
      for (JSeparator sep : seperators) {
         versionPanel.remove(sep);
      }

      seperators.clear();

      for (JComponent comp : view.getSeperatorComponents()) {
         if (comp.getParent() != null) {
            JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);

            sep.setSize(Math.max(versionPanel.getWidth(), ConceptViewSettings.NAVIGATOR_WIDTH - 3), 6);

            Point location = comp.getLocation();

            SwingUtilities.convertPointToScreen(location, comp.getParent());
            SwingUtilities.convertPointFromScreen(location, versionPanel.getParent());
            sep.setLocation(0, location.y - sep.getHeight());
            versionPanel.add(sep);
            seperators.add(sep);
         }
      }
   }

   private void redoLayout() {
      if ((changedSelections.size() > 0) || view.getSettings().isForAdjudication()) {
         navigator.getImplementButton().setEnabled(true);
      } else {
         navigator.getImplementButton().setEnabled(false);
      }

      int currentX = xStartLoc;

      // int yAdjust = -historyHeaderPanel.getHeight();
      next:
      for (PositionBI p : view.getPositionOrderedSet()) {
         JCheckBox positionCheck = positionCheckMap.get(p);
         PositionBI position = checkPositionMap.get(positionCheck);

         if (position == null) {
            continue next;
         }

         Integer row = view.getPathRowMap().get(position.getPath());

         if (row == null) {
            continue next;
         }

         JCheckBox rowCheck     = view.getRowToPathCheckMap().get(row);
         boolean   showPosition = rowCheck.isSelected();

         positionCheck.setVisible(showPosition);
         positionCheck.setLocation(currentX, positionCheck.getY());

         for (JComponent componentInColumn : checkComponentMap.get(positionCheck)) {
            if (componentInColumn instanceof JRadioButton) {
               JRadioButton radioButton = (JRadioButton) componentInColumn;

               if (showPosition) {
                  if (isPanelVisibleForButton(radioButton)) {
                     componentInColumn.setVisible(true);

                     int   maxY     = 0;
                     Point location = new Point();

                     for (JComponent panel : buttonPanelSetMap.get(radioButton)) {
                        if ((panel.getParent() != null) && panel.isVisible()) {
                           location = panel.getLocation();
                           SwingUtilities.convertPointToScreen(location, panel.getParent());
                           maxY = Math.max(maxY, location.y);
                        }
                     }

                     location.y = maxY;
                     SwingUtilities.convertPointFromScreen(location, componentInColumn.getParent());
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

         JLabel positionLabel = positionHeaderCheckLabelMap.get(positionCheck);

         if (positionLabel.isVisible()) {
            positionLabel.setLocation(currentX, positionLabel.getY());

            JLabel versionPanelLabel = positionVersionPanelCheckLabelMap.get(positionCheck);

            if (versionPanelLabel != null) {
               versionPanelLabel.setVisible(true);
               versionPanelLabel.setLocation(currentX, versionPanelLabel.getY());
            }

            if (positionCheck.isVisible()) {
               currentX += positionLabel.getWidth();
            } else {
               positionLabel.setVisible(false);
            }
         }
      }

      hxWidth = currentX;
      historyHeaderPanel.setSize(Math.max(hxWidth, ConceptViewSettings.NAVIGATOR_WIDTH - 6),
                                 view.getHistoryPanel().getHeight());
      historyHeaderPanel.setMinimumSize(historyHeaderPanel.getSize());
      historyHeaderPanel.setPreferredSize(historyHeaderPanel.getSize());
      historyHeaderPanel.setMaximumSize(historyHeaderPanel.getSize());
      versionPanel.setSize(Math.max(hxWidth, ConceptViewSettings.NAVIGATOR_WIDTH - 6),
                           getVersionPanelHeight());
      versionPanel.setMinimumSize(versionPanel.getSize());
      versionPanel.setPreferredSize(versionPanel.getSize());
      versionPanel.setMaximumSize(versionPanel.getSize());
      redoGrid();
   }

   public void refreshHistory() {
      resizeIfNeeded();
   }

   public void removeListeners() {
      navigator.getImplementButton().removeActionListener(avcl);
      topHistoryPanel.removeComponentListener(tcl);
      versionScroller.getHorizontalScrollBar().getModel().removeChangeListener(hsal);
      ((JScrollPane) view.getParent().getParent()).getVerticalScrollBar().getModel().removeChangeListener(
          vsal);
   }

   private void reset() {
      nidSapNidButtonMap.clear();
      buttonSapMap.clear();
      buttonPanelSetMap.clear();
      originalButtonSelections.clear();
      changedSelections.clear();
      view.getChangedVersionSelections().clear();
   }

   private void resetHeader(ConceptView view) {
      TreeSet<PositionBI> positionOrderedSet = view.getPositionOrderedSet();

      if ((view.getPathRowMap() == null) || (view.getRowToPathCheckMap() == null)
              || (positionOrderedSet == null)) {
         return;
      }

      int locX = xStartLoc;

      for (PositionBI p : positionOrderedSet) {
         assert p != null;
         assert view.getPathRowMap() != null;
         assert view.getRowToPathCheckMap() != null;
         assert p.getPath() != null;

         Integer row = view.getPathRowMap().get(p.getPath());

         if (row == null) {
            continue;
         }

         JCheckBox rowCheck      = view.getRowToPathCheckMap().get(row);
         JCheckBox positionCheck = positionCheckMap.get(p);
         Point location = rowCheck.getLocation();
         if(location != null){
            positionCheck.setLocation(locX, rowCheck.getLocation().y);
            positionCheck.setSize(positionCheck.getPreferredSize());
            positionCheck.setToolTipText(p.toString());

            if (positionCheck.isVisible()) {
                locX += positionCheck.getWidth();
            }
         }
      }
      historyHeaderPanel.revalidate();
      hxWidth = locX;
   }

   public void resizeIfNeeded() {
      try {

         resetHeader(view);
         combineHistoryPanels();
         setupVersionPanel(positionPanelMap);
         redoLayout();
         GuiUtil.tickle(historyHeaderPanel);
      } catch (IOException ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }
   }

   private void setupHeader(ConceptView view) {
      TreeSet<PositionBI> positionOrderedSet = view.getPositionOrderedSet();

      if ((view.getPathRowMap() == null) || (view.getRowToPathCheckMap() == null)
              || (positionOrderedSet == null)) {
         return;
      }

      int locX = xStartLoc;

      for (PositionBI p : positionOrderedSet) {
         assert p != null;
         assert view.getPathRowMap() != null;
         assert view.getRowToPathCheckMap() != null;
         assert p.getPath() != null;

         Integer row = view.getPathRowMap().get(p.getPath());

         if (row == null) {
            continue;
         }

         JCheckBox rowCheck      = view.getRowToPathCheckMap().get(row);
         JCheckBox positionCheck = view.getJCheckBox();

         positionCheck.setVisible(rowCheck.isSelected());
         checkComponentMap.put(positionCheck, new ArrayList<JComponent>());
         positionCheckMap.put(p, positionCheck);
         checkPositionMap.put(positionCheck, p);
         positionCheck.setLocation(locX, rowCheck.getLocation().y);
         positionCheck.setSize(positionCheck.getPreferredSize());
         positionCheck.setToolTipText(p.toString());
         historyHeaderPanel.add(positionCheck);

         if (positionCheck.isVisible()) {
            locX += positionCheck.getWidth();
         }

         JLabel historyLabel = setupLabel("", locX);

         positionHeaderCheckLabelMap.put(positionCheck, historyLabel);
         historyHeaderPanel.add(historyLabel);

         JLabel versionHistoryLabel = setupLabel(p.toString(), 0);

         positionVersionPanelCheckLabelMap.put(positionCheck, versionHistoryLabel);
         positionCheck.addActionListener(new UpdateHistoryBorder(historyLabel, versionHistoryLabel));
      }

      hxWidth = locX;
   }

   private JLabel setupLabel(String hxString, int locX) {
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

      for (JLabel versionPanelLabel : positionVersionPanelCheckLabelMap.values()) {
         versionPanelLabel.setVisible(false);
         versionPanel.add(versionPanelLabel);
      }
   }

   private void syncVerticalLayout() {
      BoundedRangeModel eventModel =
         view.getCvRenderer().getConceptScrollPane().getVerticalScrollBar().getModel();
      BoundedRangeModel historyScrollModel = versionScroller.getVerticalScrollBar().getModel();

      historyScrollModel.setMaximum(eventModel.getMaximum());
      historyScrollModel.setMinimum(eventModel.getMinimum());
      historyScrollModel.setValue(eventModel.getValue());
      versionPanel.setLocation(0, -eventModel.getValue());
   }

   //~--- get methods ---------------------------------------------------------

   private ButtonGroup getButtonGroup(int componentNid, boolean inferred) {
      if (inferred) {
         ButtonGroup group = inferredNidGroupMap.get(componentNid);

         if (group == null) {
            group = new ButtonGroup();
            inferredNidGroupMap.put(componentNid, group);
         }

         return group;
      }

      ButtonGroup group = nidGroupMap.get(componentNid);

      if (group == null) {
         group = new ButtonGroup();
         nidGroupMap.put(componentNid, group);
      }

      return group;
   }

   private List<DragPanelComponentVersion<?>> getRefexPanels(DragPanelComponentVersion<?> dragPanel) {
      List<DragPanelComponentVersion<?>> versionPanels = new ArrayList<DragPanelComponentVersion<?>>();

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
             + (view.getHistoryPanel().getHeight() + view.getHistoryPanel().getY() + 24);
   }

   private List<DragPanelComponentVersion<?>> getVersionPanels(DragPanelComponentVersion<?> dragPanel)
           throws IOException {
      List<DragPanelComponentVersion<?>> versionPanels = new ArrayList<DragPanelComponentVersion<?>>();

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
         if (panel.isVisible()) {
            return true;
         }
      }

      return visible;
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

               adjudicateView.setContradictionManager(new InteractiveAdjudicator(nidGroupMap, buttonVersionMap));
               cc.makeAdjudicationAnalogs(view.getConfig().getEditCoordinate(),
                                          adjudicateView);
            }

            Ts.get().addUncommitted(view.getConcept());
            reset();
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
         BoundedRangeModel eventModel         = (BoundedRangeModel) ce.getSource();
         BoundedRangeModel historyScrollModel =
            historyHeaderPanelScroller.getHorizontalScrollBar().getModel();

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
         this.nid    = nid;
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
         return Hashcode.compute(new int[] { nid, sapNid });
      }
   }


   private class ScrollableHxPanel extends JPanel implements Scrollable {
      public ScrollableHxPanel() {
         super(null);
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
   }


   private class SelectedVersionChangedListener implements ChangeListener {
      @Override
      public void stateChanged(ChangeEvent ce) {
         JRadioButton       button       = (JRadioButton) ce.getSource();
         ComponentVersionBI version      = buttonVersionMap.get(button);
         int                changedCount = changedSelections.size();

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
         resizeIfNeeded();
      }

      @Override
      public void componentShown(ComponentEvent ce) {
         syncVerticalLayout();
      }
   }


   private class UpdateHistoryBorder implements ActionListener {
      JLabel hxLabel;
      JLabel versionHxLabel;

      //~--- constructors -----------------------------------------------------

      public UpdateHistoryBorder(JLabel historyLabel, JLabel versionHxLabel) {
         this.hxLabel        = historyLabel;
         this.versionHxLabel = versionHxLabel;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public void actionPerformed(ActionEvent ae) {
         JCheckBox check = (JCheckBox) ae.getSource();

         if (check.isSelected()) {
            BoundedRangeModel historyScrollModel = versionScroller.getVerticalScrollBar().getModel();

            hxLabel.setVisible(true);
            hxLabel.setLocation(hxLabel.getX(), historyScrollModel.getValue());
            versionHxLabel.setVisible(true);
            versionHxLabel.setLocation(hxLabel.getX(), historyScrollModel.getValue());
         } else {
            hxLabel.setVisible(false);
            versionHxLabel.setVisible(false);
         }

         resizeIfNeeded();
      }
   }


   private class VerticalScrollActionListener implements ChangeListener {
      @Override
      public void stateChanged(ChangeEvent ce) {
         BoundedRangeModel eventModel = (BoundedRangeModel) ce.getSource();

         for (JLabel hxLabel : positionVersionPanelCheckLabelMap.values()) {
            hxLabel.setLocation(hxLabel.getX(), eventModel.getValue());
         }

         versionPanel.setSize(Math.max(hxWidth, ConceptViewSettings.NAVIGATOR_WIDTH - 6),
                              Math.max(eventModel.getMaximum() + 48, getVersionPanelHeight()));
         versionPanel.setMinimumSize(versionPanel.getSize());
         versionPanel.setPreferredSize(versionPanel.getSize());
         versionPanel.setMaximumSize(versionPanel.getSize());

         BoundedRangeModel historyScrollModel = versionScroller.getVerticalScrollBar().getModel();

         historyScrollModel.setMaximum(eventModel.getMaximum());
         historyScrollModel.setMinimum(eventModel.getMinimum());
         historyScrollModel.setValue(eventModel.getValue());
         versionPanel.setLocation(0, -eventModel.getValue());
      }
   }
}
