package org.ihtsdo.arena.conceptview;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.ArenaComponentSettings;

import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;

public abstract class ComponentVersionDragPanel<T extends ComponentVersionBI>
        extends DragPanel<T> implements I_ToggleSubPanels {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private void updateCollapseExpandButton() {
      if (getSubpanelCount() == 0 && collapseExpandButton.isVisible()) {
         collapseExpandButton.setVisible(false);
         collapseExpandButton.setEnabled(false);
      } else {
         if (!collapseExpandButton.isVisible()) {
            collapseExpandButton.setVisible(true);
            collapseExpandButton.setEnabled(true);
         }
         StringBuilder toolTipBuffer = new StringBuilder();
         boolean first = true;
         if (getRefexSubpanelCount() > 0) {
            toolTipBuffer.append(getRefexSubpanelCount());
            toolTipBuffer.append(" refexes");

            first = false;
         }
         if (getAlertSubpanelCount() > 0) {
            if (!first) {
               toolTipBuffer.append(", ");
            }

            toolTipBuffer.append(getAlertSubpanelCount());
            toolTipBuffer.append(" alerts");
            first = false;
         }
         if (getTemplateSubpanelCount() > 0) {
            if (!first) {
               toolTipBuffer.append(", ");
            }
            toolTipBuffer.append(getTemplateSubpanelCount());
            toolTipBuffer.append(" templates");
            first = false;
         }
         collapseExpandButton.setToolTipText(toolTipBuffer.toString());
      }
   }

   public enum SubPanelTypes {

      REFEX, ALERT, TEMPLATE, HISTORY
   };
   private List<JComponent> refexSubPanels = new ArrayList<JComponent>();
   private List<JComponent> alertSubPanels = new ArrayList<JComponent>();
   private List<JComponent> templateSubPanels = new ArrayList<JComponent>();
   private List<JComponent> historySubPanels = new ArrayList<JComponent>();
   private JButton collapseExpandButton;
   private CollapsePanel parentCollapsePanel;
   private boolean collapsed = true;

   public CollapsePanel getParentCollapsePanel() {
      return parentCollapsePanel;
   }

   public ComponentVersionDragPanel(ConceptViewSettings settings,
           CollapsePanel parentCollapsePanel, T component) {
      super(settings, component);
      this.parentCollapsePanel = parentCollapsePanel;
   }

   public ComponentVersionDragPanel(LayoutManager layout,
           ConceptViewSettings settings,
           CollapsePanel parentCollapsePanel, T component) {
      super(layout, settings, component);
      this.parentCollapsePanel = parentCollapsePanel;
   }

   protected JLabel getJLabel(String text) {
      JLabel l = new JLabel(text);
      l.setFont(l.getFont().deriveFont(getSettings().getFontSize()));
      l.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
      return l;
   }

   protected TermComponentLabel getLabel(int nid, boolean canDrop)
           throws IOException {
      try {
         TermComponentLabel termLabel = new TermComponentLabel();
         termLabel.setLineWrapEnabled(true);
         termLabel.getDropTarget().setActive(canDrop);
         termLabel.setFixedWidth(100);
         termLabel.setFont(termLabel.getFont().deriveFont(getSettings().getFontSize()));
         termLabel.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
         termLabel.setTermComponent(Terms.get().getConcept(nid));
         return termLabel;
      } catch (TerminologyException terminologyException) {
         throw new IOException(terminologyException);
      }
   }

   @Override
   public String getUserString(T obj) {
      return obj.toUserString();
   }

   @Override
   public void showSubPanels(EnumSet<SubPanelTypes> panels) {
      collapsed = false;
      if (collapseExpandButton != null) {
         collapseExpandButton.setIcon(new ImageIcon(
                 CollapsePanel.class.getResource(ArenaComponentSettings.IMAGE_PATH
                 + (collapsed ? "maximize.gif"
                 : "minimize.gif"))));
      }
      setPanelVisibility(refexSubPanels, panels, SubPanelTypes.REFEX);
      setPanelVisibility(alertSubPanels, panels, SubPanelTypes.ALERT);
      setPanelVisibility(templateSubPanels, panels, SubPanelTypes.TEMPLATE);
   }

   @Override
   public void hideSubPanels(EnumSet<SubPanelTypes> panels) {
      collapsed = true;
      if (collapseExpandButton != null) {
         collapseExpandButton.setIcon(new ImageIcon(
                 CollapsePanel.class.getResource(ArenaComponentSettings.IMAGE_PATH
                 + (collapsed ? "maximize.gif"
                 : "minimize.gif"))));
      }
      setPanelVisibility(refexSubPanels, panels, null);
      setPanelVisibility(alertSubPanels, panels, null);
      setPanelVisibility(templateSubPanels, panels, null);
   }

   private void setPanelVisibility(List<JComponent> componentList,
           EnumSet<SubPanelTypes> subpanelsToShow,
           SubPanelTypes panelType) {
      for (JComponent panel : componentList) {
         if (panelType == null) {
            panel.setVisible(false);
         } else {
            panel.setVisible(subpanelsToShow.contains(panelType));
         }
      }
   }

   public List<JComponent> getAlertSubpanels() {
      return alertSubPanels;
   }

   public List<JComponent> getRefexSubpanels() {
      return refexSubPanels;
   }

   public List<JComponent> getTemplateSubpanels() {
      return templateSubPanels;
   }

   public List<JComponent> getHistorySubpanels() {
      return historySubPanels;
   }

   public int getAlertSubpanelCount() {
      return alertSubPanels.size();
   }

   public int getRefexSubpanelCount() {
      return refexSubPanels.size();
   }

   public int getHistorySubpanelCount() {
      return historySubPanels.size();
   }

   public int getTemplateSubpanelCount() {
      return templateSubPanels.size();
   }

   public int getSubpanelCount() {
      return getAlertSubpanelCount() + getRefexSubpanelCount()
              + getTemplateSubpanelCount();
   }

   @Override
   public boolean isExpanded() {
      return !collapsed;
   }

   protected JButton getCollapseExpandButton() {
      collapseExpandButton = new JButton(new AbstractAction("", new ImageIcon(
              ConceptViewRenderer.class.getResource(
              ArenaComponentSettings.IMAGE_PATH + "maximize.gif"))) {

         /**
          *
          */
         private static final long serialVersionUID = 1L;

         @Override
         public void actionPerformed(ActionEvent e) {
            collapsed = !collapsed;
            if (collapsed) {
               hideSubPanels(parentCollapsePanel.getSubpanelsToShow());
            } else {
               showSubPanels(parentCollapsePanel.getSubpanelsToShow());
            }
            ((JButton) e.getSource()).setIcon(new ImageIcon(
                    CollapsePanel.class.getResource(ArenaComponentSettings.IMAGE_PATH
                    + (collapsed ? "maximize.gif"
                    : "minimize.gif"))));
            updateCollapseExpandButton();
         }
      });
      collapseExpandButton.setPreferredSize(new Dimension(21, 16));
      collapseExpandButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      collapseExpandButton.setToolTipText("Collapse/Expand");
      collapseExpandButton.setOpaque(false);
      collapseExpandButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));
      updateCollapseExpandButton();
      return collapseExpandButton;
   }

   protected T getComponentVersion() {
      return getThingToDrag();
   }

   protected void addSubPanels(GridBagConstraints gbc) throws IOException {
      addRefexPanels(gbc);
      addWarningPanels(gbc);
      addTemplateSubpanels(gbc);
      updateCollapseExpandButton();
   }

   public void addRefexPanels(GridBagConstraints gbc) throws IOException {
      gbc.gridy++;
      gbc.gridwidth = gbc.gridx;
      gbc.gridx = 1;
      gbc.weightx = 1;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      Collection<? extends RefexVersionBI<?>> refexes =
              getThingToDrag().getCurrentRefexes(
              getSettings().getConfig().getViewCoordinate());
      for (RefexVersionBI<?> rx : refexes) {
         DragPanelExtension dpe =
                 new DragPanelExtension(getSettings(), null, rx);
         dpe.setBorder(BorderFactory.createEtchedBorder());
         dpe.setVisible(parentCollapsePanel.isShown(SubPanelTypes.REFEX)
                 && parentCollapsePanel.areExtrasShown());
         add(dpe, gbc);
         refexSubPanels.add(dpe);
         gbc.gridy++;
      }
   }

   public void addWarningPanels(GridBagConstraints gbc) throws IOException {
   }

   public void addTemplateSubpanels(GridBagConstraints gbc) throws IOException {
   }
}
