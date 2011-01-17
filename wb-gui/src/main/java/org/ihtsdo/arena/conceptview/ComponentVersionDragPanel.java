package org.ihtsdo.arena.conceptview;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
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

public abstract class ComponentVersionDragPanel<T extends ComponentVersionBI> 
         extends DragPanel<T> implements I_ToggleSubPanels {

    /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public enum SubPanelTypes {
      REFEX, ALERT, TEMPLATE
   };
   
   private List<JComponent> refexSubPanels = new ArrayList<JComponent>();
   private List<JComponent> alertSubPanels = new ArrayList<JComponent>();
   private List<JComponent> templateSubPanels = new ArrayList<JComponent>();
   
   private CollapsePanel parentCollapsePanel;
   
   private boolean collapsed = false;

   public CollapsePanel getParentCollapsePanel() {
      return parentCollapsePanel;
   }

   public ComponentVersionDragPanel(ConceptViewSettings settings,
           CollapsePanel parentCollapsePanel) {
      super(settings);
      this.parentCollapsePanel = parentCollapsePanel;
   }

   public ComponentVersionDragPanel(LayoutManager layout,
           ConceptViewSettings settings,
           CollapsePanel parentCollapsePanel) {
      super(layout, settings);
      this.parentCollapsePanel = parentCollapsePanel;
   }
   
   protected JLabel getJLabel(String text) {
      JLabel l = new JLabel(text);
      l.setFont(l.getFont().deriveFont(getSettings().getFontSize()));
      l.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
      return l;
   }
   
   protected TermComponentLabel getLabel(int nid, boolean canDrop)
           throws TerminologyException, IOException {
      TermComponentLabel termLabel = new TermComponentLabel();
      termLabel.setLineWrapEnabled(true);
      termLabel.getDropTarget().setActive(canDrop);
      termLabel.setFixedWidth(100);
      termLabel.setFont(termLabel.getFont().deriveFont(getSettings().getFontSize()));
      termLabel.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
      termLabel.setTermComponent(Terms.get().getConcept(nid));
      return termLabel;
   }


   @Override
   public String getUserString(T obj) {
      return obj.toUserString();
   }

   @Override
   public void showSubPanels(EnumSet<SubPanelTypes> panels) {
      setPanelVisibility(refexSubPanels, panels, SubPanelTypes.REFEX);
      setPanelVisibility(alertSubPanels, panels, SubPanelTypes.ALERT);
      setPanelVisibility(templateSubPanels, panels, SubPanelTypes.TEMPLATE);
   }

   private void setPanelVisibility(List<JComponent> componentList,
           EnumSet<SubPanelTypes> subpanelsToShow,
           SubPanelTypes panelType) {
      for (JComponent refexPanel : componentList) {
         refexPanel.setVisible(subpanelsToShow.contains(panelType));
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

     public int getAlertSubpanelCount() {
      return alertSubPanels.size();
   }

   public int getRefexSubpanelCount() {
      return refexSubPanels.size();
   }

   public int getTemplateSubpanelCount() {
      return templateSubPanels.size();
   }
   
   public int getSubpanelCount() {
      return getAlertSubpanelCount() + getRefexSubpanelCount() 
              + getTemplateSubpanelCount();
   }
   
   protected JButton getCollapseExpandButton() {
      JButton button = new JButton(new AbstractAction("", new ImageIcon(
              ConceptViewRenderer.class.getResource(ArenaComponentSettings.IMAGE_PATH + "minimize.gif"))) {

         /**
          * 
          */
         private static final long serialVersionUID = 1L;

         @Override
         public void actionPerformed(ActionEvent e) {
            collapsed = !collapsed;
            showSubPanels(parentCollapsePanel.subpanelsToShow);
            ((JButton) e.getSource()).setIcon(new ImageIcon(
                    CollapsePanel.class.getResource(ArenaComponentSettings.IMAGE_PATH
                    + (collapsed ? "maximize.gif"
                    : "minimize.gif"))));
         }
      });
      button.setPreferredSize(new Dimension(21, 16));
      button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      button.setToolTipText("Collapse/Expand");
      button.setOpaque(false);
      button.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));
      if (getSubpanelCount() == -1/*TODO*/) {
         button.setIcon(null);
         button.setEnabled(false);
      }
      return button;
   }

}
