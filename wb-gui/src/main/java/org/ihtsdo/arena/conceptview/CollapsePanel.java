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

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   boolean collapsed = false;
   boolean refex = false;
   boolean templates = false;
   boolean alerts = false;
   boolean showExtras = false;
   int refexCount = 1;
   int templateCount = 1;
   int alertCount = 1;
   EnumSet<ComponentVersionDragPanel.SubPanelTypes> subpanelsToShow = 
           EnumSet.noneOf(ComponentVersionDragPanel.SubPanelTypes.class);
   Set<I_ToggleSubPanels> components = new HashSet<I_ToggleSubPanels>();
   private JButton alertsButton;
   private JButton extrasButton;
   private JButton refexButton;
   private JButton templatessButton;
   
   private List<JComponent> alertPanels = new ArrayList<JComponent>();
   private List<JComponent> refexPanels = new ArrayList<JComponent>();
   private List<JComponent> templatePanels = new ArrayList<JComponent>();

   public boolean isCollapsed() {
      return collapsed;
   }

   public void setCollapsed(boolean collapsed) {
      this.collapsed = collapsed;
   }

   public CollapsePanel(String labelStr, ArenaComponentSettings settings) {
      super();
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

   private BufferedImage getBlackAndWhite(Image disImage) {
      BufferedImage image = new BufferedImage(disImage.getWidth(this), disImage.getHeight(this), BufferedImage.TYPE_INT_ARGB);

      Graphics2D graph = image.createGraphics();
      graph.drawImage(disImage, 0, 0, this);

      ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
      ColorConvertOp op = new ColorConvertOp(cs, null);
      return op.filter(image, null);
   }

   private JButton getShowExtrasButton() {
      final ImageIcon showExtrasIcon = new ImageIcon(
              ConceptViewRenderer.class.getResource(ArenaComponentSettings.IMAGE_PATH
              + "minimize.gif"));
      final ImageIcon hideExtrasIcon = new ImageIcon(
              ConceptViewRenderer.class.getResource(ArenaComponentSettings.IMAGE_PATH
              + "maximize.gif"));

      extrasButton = new JButton(new AbstractAction("", showExtrasIcon) {

         /**
          * 
          */
         private static final long serialVersionUID = 1L;

         @Override
         public void actionPerformed(ActionEvent e) {
            showExtras = !showExtras;
            for (JComponent jc: refexPanels) {
               jc.setVisible(showExtras);
            }
            for (JComponent jc: alertPanels) {
               jc.setVisible(showExtras);
            }
            for (JComponent jc: templatePanels) {
               jc.setVisible(showExtras);
            }
            for (I_ToggleSubPanels cvdp : components) {
               cvdp.showSubPanels(subpanelsToShow);
            }
            ((JButton) e.getSource()).setIcon((showExtras ? hideExtrasIcon
                    : showExtrasIcon));
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
      final ImageIcon showRefexes = new ImageIcon(
              ConceptViewRenderer.class.getResource(
              "/16x16/plain/paperclip.png"));
      final ImageIcon hideRefexes =
              new ImageIcon(getBlackAndWhite(showRefexes.getImage()));

      refexButton = new JButton(new AbstractAction("", showRefexes) {

         /**
          * 
          */
         private static final long serialVersionUID = 1L;

         @Override
         public void actionPerformed(ActionEvent e) {
            refex = !refex;
            showSubpanels(refex, ComponentVersionDragPanel.SubPanelTypes.REFEX);
            for (JComponent jc: refexPanels) {
               jc.setVisible(refex);
            }
            for (I_ToggleSubPanels jc : components) {
               //jc.setVisible(!collapsed);
            }
            ((JButton) e.getSource()).setIcon((refex ? hideRefexes
                    : showRefexes));
         }
      });
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

   private void showSubpanels(boolean show,
           ComponentVersionDragPanel.SubPanelTypes subpanel) {
      if (show) {
         subpanelsToShow.add(subpanel);
      } else {
         subpanelsToShow.remove(subpanel);
      }
      for (I_ToggleSubPanels cvdp : components) {
         cvdp.showSubPanels(subpanelsToShow);
      }
   }

   private JButton getTemplateButton() {
      final ImageIcon showTemplates = new ImageIcon(
              ConceptViewRenderer.class.getResource(
              "/16x16/plain/lightbulb_on.png"));
      final ImageIcon hideTemplates =
              new ImageIcon(getBlackAndWhite(showTemplates.getImage()));

      templatessButton = new JButton(new AbstractAction("", showTemplates) {

         /**
          * 
          */
         private static final long serialVersionUID = 1L;

         @Override
         public void actionPerformed(ActionEvent e) {
            templates = !templates;
            for (JComponent jc: templatePanels) {
               jc.setVisible(templates);
            }
              showSubpanels(templates, ComponentVersionDragPanel.SubPanelTypes.TEMPLATE);
            ((JButton) e.getSource()).setIcon(templates ? hideTemplates
                    : showTemplates);
         }
      });
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
      final ImageIcon showAlerts = new ImageIcon(
              ConceptViewRenderer.class.getResource(
              "/16x16/plain/warning.png"));
      final ImageIcon hideAlerts =
              new ImageIcon(getBlackAndWhite(showAlerts.getImage()));

      alertsButton = new JButton(new AbstractAction("", showAlerts) {

         /**
          * 
          */
         private static final long serialVersionUID = 1L;

         @Override
         public void actionPerformed(ActionEvent e) {
            alerts = !alerts;
            for (JComponent jc: alertPanels) {
               jc.setVisible(alerts);
            }
             showSubpanels(alerts, ComponentVersionDragPanel.SubPanelTypes.ALERT);
            ((JButton) e.getSource()).setIcon(alerts ? hideAlerts
                    : showAlerts);
         }
      });
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

   private JButton getCollapseExpandButton() {
      JButton button = new JButton(new AbstractAction("", new ImageIcon(
              ConceptViewRenderer.class.getResource(ArenaComponentSettings.IMAGE_PATH + "minimize.gif"))) {

         /**
          * 
          */
         private static final long serialVersionUID = 1L;

         @Override
         public void actionPerformed(ActionEvent e) {
            collapsed = !collapsed;

            for (I_ToggleSubPanels jc : components) {
               jc.setVisible(!collapsed);
            }
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
      if (refexCount + templateCount + alertCount == 0) {
         button.setIcon(null);
         button.setEnabled(false);
      }
      return button;
   }

   public void addToggleComponent(I_ToggleSubPanels component) {
      components.add(component);
   }

   private static int emptyWidth = 21;
   private static int emptyHeight = 16;
   private Dimension emptyDimension = new Dimension(emptyWidth, emptyHeight);
   
   public void setAlertCount(int alertCount) {
      this.alertCount = alertCount;
      if (alertCount == 0) {
         alertsButton.setIcon(null);
         alertsButton.setEnabled(false);
         alertsButton.setMaximumSize(emptyDimension);
         alertsButton.setMinimumSize(emptyDimension);
         alertsButton.setPreferredSize(emptyDimension);
      }
  }

   public void setRefexCount(int refexCount) {
      this.refexCount = refexCount;
      if (refexCount == 0) {
         refexButton.setIcon(null);
         refexButton.setEnabled(false);
         refexButton.setMaximumSize(emptyDimension);
         refexButton.setMinimumSize(emptyDimension);
         refexButton.setPreferredSize(emptyDimension);
     }
  }

   public void setTemplateCount(int templateCount) {
      this.templateCount = templateCount;
      if (templateCount == 0) {
         templatessButton.setIcon(null);
         templatessButton.setEnabled(false);
         templatessButton.setMaximumSize(emptyDimension);
         templatessButton.setMinimumSize(emptyDimension);
         templatessButton.setPreferredSize(emptyDimension);
      }
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
