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
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ihtsdo.arena.ArenaComponentSettings;

public class CollapsePanel extends JPanel {

     static final ImageIcon showAlerts = new ImageIcon(
              ConceptViewRenderer.class.getResource(
              "/16x16/plain/warning.png"));
     static final ImageIcon hideAlerts =
              new ImageIcon(getBlackAndWhite(showAlerts.getImage()));

     static final ImageIcon showExtrasIcon = new ImageIcon(
              ConceptViewRenderer.class.getResource(ArenaComponentSettings.IMAGE_PATH
              + "minimize.gif"));
     static final ImageIcon hideExtrasIcon = new ImageIcon(
              ConceptViewRenderer.class.getResource(ArenaComponentSettings.IMAGE_PATH
              + "maximize.gif"));

     static final ImageIcon showRefexes = new ImageIcon(
              ConceptViewRenderer.class.getResource(
              "/16x16/plain/paperclip.png"));
     static final ImageIcon hideRefexes =
              new ImageIcon(getBlackAndWhite(showRefexes.getImage()));

    static final ImageIcon showTemplates = new ImageIcon(
              ConceptViewRenderer.class.getResource(
              "/16x16/plain/lightbulb_on.png"));
     static final ImageIcon hideTemplates =
              new ImageIcon(getBlackAndWhite(showTemplates.getImage()));

   /**
    *
    */
   private static final long serialVersionUID = 1L;



   int refexCount = 0;
   int templateCount = 0;
   int alertCount = 0;
   Set<I_ToggleSubPanels> components = new HashSet<I_ToggleSubPanels>();
   private JButton alertsButton;
   private JButton extrasButton;
   private JButton refexButton;
   private JButton templatessButton;

   private List<JComponent> alertPanels = new ArrayList<JComponent>();
   private List<JComponent> refexPanels = new ArrayList<JComponent>();
   private List<JComponent> templatePanels = new ArrayList<JComponent>();
   private JButton collapseExpandButton;

   private CollapsePanelPrefs prefs;

   /**
    * @return the refexesShown
    */
   public boolean areRefexesShown() {
      return prefs.areRefexesShown();
   }

   /**
    * @param refexesShown the refexesShown to set
    */
   public void setRefexesShown(boolean refexesShown) {
      prefs.setRefexesShown(refexesShown);
   }

   /**
    * @return the templatesShown
    */
   public boolean areTemplatesShown() {
      return prefs.areTemplatesShown();
   }

   /**
    * @param templatesShown the templatesShown to set
    */
   public void setTemplatesShown(boolean templatesShown) {
      prefs.setAlertsShown(templatesShown);
   }

   /**
    * @return the alertsShown
    */
   public boolean areAlertsShown() {
      return prefs.areAlertsShown();
   }

   /**
    * @param alertsShown the alertsShown to set
    */
   public void setAlertsShown(boolean alertsShown) {
      prefs.setAlertsShown(alertsShown);
   }

   /**
    * @return the extrasShown
    */
   public boolean areExtrasShown() {
      return prefs.areExtrasShown();
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

   public CollapsePanel(String labelStr, ArenaComponentSettings settings,
           CollapsePanelPrefs prefs) {
      super();
      this.prefs = prefs;
      setBackground(Color.LIGHT_GRAY);
      setOpaque(true);
      setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, Color.GRAY));
      setLayout(new BorderLayout());

      JPanel toolBar1 = new JPanel();
      toolBar1.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 2));
      toolBar1.setOpaque(false);
      toolBar1.add(getRefexButton());
      toolBar1.add(getTemplateButton());
      toolBar1.add(getAlertsButton());
      toolBar1.add(getShowExtrasButton());
      add(toolBar1, BorderLayout.WEST);

      JLabel label = new JLabel(labelStr, JLabel.CENTER);
      label.setFont(getFont().deriveFont(settings.getFontSize()));
      label.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 2));
      add(label, BorderLayout.CENTER);
      JPanel toolBar2 = new JPanel();
      toolBar2.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 2));
      toolBar2.setOpaque(false);
      toolBar2.add(getCollapseExpandButton());
      add(toolBar2, BorderLayout.EAST);
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
            for (JComponent jc: refexPanels) {
               jc.setVisible(areExtrasShown());
            }
            for (JComponent jc: alertPanels) {
               jc.setVisible(areExtrasShown());
            }
            for (JComponent jc: templatePanels) {
               jc.setVisible(areExtrasShown());
            }
            for (I_ToggleSubPanels cvdp : components) {
               if (areExtrasShown()) {
                  cvdp.showSubPanels(getSubpanelsToShow());
               } else {
                  cvdp.hideSubPanels(getSubpanelsToShow());
               }
             }
            ((JButton) e.getSource()).setIcon((areExtrasShown() ? showExtrasIcon
                    : hideExtrasIcon));
         }
      });
      extrasButton.setPreferredSize(new Dimension(21, 16));
      extrasButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      extrasButton.setToolTipText("Hide/Show extra info for all group members");
      extrasButton.setOpaque(false);
      extrasButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));

      return extrasButton;
   }

   private JButton getRefexButton() {
      refexButton = new JButton(new AbstractAction("", showRefexes) {

         /**
          *
          */
         private static final long serialVersionUID = 1L;

         @Override
         public void actionPerformed(ActionEvent e) {
            setRefexesShown(!areRefexesShown());
            updateShowSubpanelSet(areRefexesShown(), ComponentVersionDragPanel.SubPanelTypes.REFEX);
            ((JButton) e.getSource()).setIcon((areRefexesShown() ? showRefexes
                    : hideRefexes));
            updateSubpanels();
         }
      });
      refexButton.setSelected(areRefexesShown());
      refexButton.setPreferredSize(new Dimension(21, 16));
      refexButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      refexButton.setToolTipText("Hide/Show refexes");
      refexButton.setOpaque(false);
      refexButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));
      if (refexCount == 0) {
         refexButton.setIcon(null);
         refexButton.setEnabled(false);
         refexButton.setMaximumSize(emptyDimension);
         refexButton.setMinimumSize(emptyDimension);
         refexButton.setPreferredSize(emptyDimension);
     }
      return refexButton;
   }

   private void updateShowSubpanelSet(boolean show,
           ComponentVersionDragPanel.SubPanelTypes subpanel) {
      if (show) {
         getSubpanelsToShow().add(subpanel);
      } else {
         getSubpanelsToShow().remove(subpanel);
      }
   }

   private JButton getTemplateButton() {

      templatessButton = new JButton(new AbstractAction("", showTemplates) {

         /**
          *
          */
         private static final long serialVersionUID = 1L;

         @Override
         public void actionPerformed(ActionEvent e) {
            setTemplatesShown(!areTemplatesShown());
            updateShowSubpanelSet(areTemplatesShown(), ComponentVersionDragPanel.SubPanelTypes.TEMPLATE);
            ((JButton) e.getSource()).setIcon(areTemplatesShown() ? showTemplates
                    : hideTemplates);
            updateSubpanels();
         }
      });
      templatessButton.setSelected(areTemplatesShown());
      templatessButton.setPreferredSize(new Dimension(21, 16));
      templatessButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      templatessButton.setToolTipText("Hide/Show suggestions");
      templatessButton.setOpaque(false);
      templatessButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));
      if (templateCount == 0) {
         templatessButton.setIcon(null);
         templatessButton.setEnabled(false);
         templatessButton.setMaximumSize(emptyDimension);
         templatessButton.setMinimumSize(emptyDimension);
         templatessButton.setPreferredSize(emptyDimension);
      }
      return templatessButton;
   }

   private JButton getAlertsButton() {

      alertsButton = new JButton(new AbstractAction("", showAlerts) {

         /**
          *
          */
         private static final long serialVersionUID = 1L;

         @Override
         public void actionPerformed(ActionEvent e) {
            setAlertsShown(!areAlertsShown());
            updateShowSubpanelSet(areAlertsShown(), ComponentVersionDragPanel.SubPanelTypes.ALERT);
            ((JButton) e.getSource()).setIcon(areAlertsShown() ? showAlerts
                    : hideAlerts);
            updateSubpanels();
         }
      });
      alertsButton.setSelected(areAlertsShown());
      alertsButton.setPreferredSize(new Dimension(21, 16));
      alertsButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      alertsButton.setToolTipText("Hide/Show warnings & errors");
      alertsButton.setOpaque(false);
      alertsButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));
      if (alertCount == 0) {
         alertsButton.setIcon(null);
         alertsButton.setEnabled(false);
         alertsButton.setMaximumSize(emptyDimension);
         alertsButton.setMinimumSize(emptyDimension);
         alertsButton.setPreferredSize(emptyDimension);
      }
       return alertsButton;
   }

   private void updateSubpanels() {
      for (I_ToggleSubPanels jc : components) {
         if (areExtrasShown()) {
            jc.showSubPanels(getSubpanelsToShow());
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

            for (I_ToggleSubPanels jc : components) {
               jc.setVisible(areExtrasShown());
            }
            ((JButton) e.getSource()).setIcon(new ImageIcon(
                    CollapsePanel.class.getResource(ArenaComponentSettings.IMAGE_PATH
                    + (areExtrasShown() ? "maximize.gif"
                    : "minimize.gif"))));
         }
      });
      collapseExpandButton.setPreferredSize(new Dimension(21, 16));
      collapseExpandButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      collapseExpandButton.setToolTipText("Collapse/Expand");
      collapseExpandButton.setOpaque(false);
      collapseExpandButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));
      updateCollapse();
      return collapseExpandButton;
   }

   public void addToggleComponent(I_ToggleSubPanels component) {
      components.add(component);
   }

   private static int emptyWidth = 21;
   private static int emptyHeight = 16;
   private Dimension emptyDimension = new Dimension(emptyWidth, emptyHeight);

   private void updateCollapse() {
      if ((alertCount + refexCount + templateCount) == 0) {
         collapseExpandButton.setVisible(false);
      } else {
         collapseExpandButton.setVisible(true);
      }
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
         if (areAlertsShown()) {
            alertsButton.setIcon(areAlertsShown() ? showAlerts
                    : hideAlerts);
            alertsButton.setEnabled(true);
         }

      }
      updateCollapse();
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
            refexButton.setIcon((areRefexesShown() ? showRefexes
                    : hideRefexes));

            refexButton.setEnabled(true);
      }
     updateCollapse();
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
         templatessButton.setIcon(areTemplatesShown() ? showTemplates
                    : hideTemplates);
         templatessButton.setEnabled(true);
      }
     updateCollapse();
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

}
