package org.ihtsdo.arena.conceptview;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.TermComponentLabel.LabelText;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.arena.ArenaComponentSettings;
import org.ihtsdo.arena.conceptview.ConceptViewSettings.DescType;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

public abstract class DragPanelComponentVersion<T extends ComponentVersionBI> extends DragPanel<T>
        implements I_ToggleSubPanels {
   private static int MIN_HEIGHT = 22;

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private static ImageIcon  ghostIcon        =
      new ImageIcon(DragPanelRel.class.getResource("/16x16/plain/ghost.png"));
   private static ImageIcon dynamicPopupImage =
      new ImageIcon(DragPanelRel.class.getResource("/16x16/plain/dynamic_popup.png"));
   private static ImageIcon conflictIcon =
      new ImageIcon(DragPanelRel.class.getResource("/16x16/plain/warning.png"));

   //~--- fields --------------------------------------------------------------

   private int                                historicalRefexSubPanelCount = 0;
   private List<DragPanelComponentVersion<?>> refexSubPanels               =
      new ArrayList<DragPanelComponentVersion<?>>();
   private List<DragPanelComponentVersion<?>> historicalRefexSubPanels     =
      new ArrayList<DragPanelComponentVersion<?>>();
   private List<JComponent>                   alertSubPanels    = new ArrayList<JComponent>();
   private List<JComponent>                   templateSubPanels = new ArrayList<JComponent>();
   private List<DragPanelComponentVersion<?>> historySubPanels  =
      new ArrayList<DragPanelComponentVersion<?>>();
   protected JLabel      conflictLabel = new JLabel(getConflictIcon());
   private boolean       collapsed     = true;
   private JButton       collapseExpandButton;
   private CollapsePanel parentCollapsePanel;

   //~--- constant enums ------------------------------------------------------

   public enum SubPanelTypes { REFEX, ALERT, TEMPLATE, HISTORY }

   //~--- constructors --------------------------------------------------------

   public DragPanelComponentVersion(ConceptViewLayout viewLayout, CollapsePanel parentCollapsePanel,
                                    T component) {
      super(viewLayout, component);
      this.parentCollapsePanel = parentCollapsePanel;
      setupCollapseExpandButton();
   }

   public DragPanelComponentVersion(LayoutManager layout, ConceptViewLayout viewLayout,
                                    CollapsePanel parentCollapsePanel, T component) {
      super(layout, viewLayout, component);
      this.parentCollapsePanel = parentCollapsePanel;
      setupCollapseExpandButton();
   }

   //~--- methods -------------------------------------------------------------

   public void addHistoryPanels(GridBagConstraints gbc) throws IOException, TerminologyException {
      gbc.gridy++;
      gbc.gridwidth = gbc.gridx;
      gbc.gridx     = 0;
      gbc.weightx   = 1;
      gbc.fill      = GridBagConstraints.HORIZONTAL;

      boolean first = true;
      int     top   = 1;

      for (DragPanelComponentVersion<T> ovp : getOtherVersionPanels()) {
         if (first) {
            first = false;
         } else {
            top = 0;
         }

         ovp.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(top, 0, 1, 1,
                 ovp.getBackground().darker()), BorderFactory.createMatteBorder(top, 0, 1, 1,
                    ovp.getBackground().brighter())));
         ovp.setVisible(parentCollapsePanel.isShown(SubPanelTypes.HISTORY)
                        && parentCollapsePanel.areExtrasShown());
         add(ovp, gbc);
         historySubPanels.add(ovp);
         gbc.gridy++;
      }
   }

   public void addPanelsChangedActionListener(ActionListener l) {
      if (collapseExpandButton != null) {
         collapseExpandButton.addActionListener(l);
      }
   }

   public void addRefexPanels(GridBagConstraints gbc) throws IOException, TerminologyException {
      gbc.gridy++;
      gbc.gridwidth = gbc.gridx;
      gbc.gridx     = 1;
      gbc.weightx   = 1;
      gbc.fill      = GridBagConstraints.HORIZONTAL;

      Collection<? extends RefexVersionBI<?>> refexes =
         getThingToDrag().getCurrentRefexes(getSettings().getConfig().getViewCoordinate());
      Set<Integer> refexIds         = new HashSet<Integer>();
      Set<Integer> refexIdsWithDups = new HashSet<Integer>();

      for (RefexVersionBI<?> rx : refexes) {
         if (refexIds.contains(rx.getCollectionNid())) {
            refexIdsWithDups.add(rx.getCollectionNid());
         } else {
            refexIds.add(rx.getCollectionNid());
         }
      }

      Collection<? extends RefexVersionBI<?>> tempRefexList =
         getThingToDrag().getInactiveRefexes(getSettings().getConfig().getViewCoordinate());

      for (RefexVersionBI<?> rx : tempRefexList) {
         if (refexIds.contains(rx.getCollectionNid())) {
            refexIdsWithDups.add(rx.getCollectionNid());
         } else {
            refexIds.add(rx.getCollectionNid());
         }
      }

      for (RefexVersionBI<?> rx : refexes) {
         if (!refexIdsWithDups.contains(rx.getCollectionNid())) {
            DragPanelExtension dpe = new DragPanelExtension(viewLayout, getParentCollapsePanel(), rx);

            getSettings().getView().getSeperatorComponents().add(dpe);
            dpe.setBorder(BorderFactory.createEtchedBorder());
            dpe.setVisible(parentCollapsePanel.isShown(SubPanelTypes.REFEX)
                           && parentCollapsePanel.areExtrasShown());
            add(dpe, gbc);
            refexSubPanels.add(dpe);
            gbc.gridy++;
         }
      }

      for (RefexVersionBI<?> rx : tempRefexList) {
         if (!refexIdsWithDups.contains(rx.getCollectionNid())) {
            DragPanelExtension dpe = new DragPanelExtension(viewLayout, getParentCollapsePanel(), rx);

            getSettings().getView().getSeperatorComponents().add(dpe);
            dpe.setInactiveBackground();
            dpe.setBorder(BorderFactory.createEtchedBorder());
            dpe.setVisible(parentCollapsePanel.isShown(SubPanelTypes.REFEX)
                           && parentCollapsePanel.areExtrasShown());
            add(dpe, gbc);
            historicalRefexSubPanels.add(dpe);
            gbc.gridy++;
         }
      }

      historicalRefexSubPanelCount = 0;
      historicalRefexSubPanelCount = historicalRefexSubPanels.size();

      // add reviesed refexes to count
      for (RefexVersionBI<?> rx : refexes) {
         if (!refexIdsWithDups.contains(rx.getCollectionNid())) {
            if (rx.getVersions().size() > 1) {
               historicalRefexSubPanelCount++;
            }
         }
      }
   }

   protected void addSubPanels(GridBagConstraints gbc) throws IOException, TerminologyException {
      if (parentCollapsePanel != null) {
         addHistoryPanels(gbc);
         addRefexPanels(gbc);
         addWarningPanels(gbc);
         addTemplateSubpanels(gbc);
         updateCollapseExpandButton();
      }
   }

   public void addTemplateSubpanels(GridBagConstraints gbc) throws IOException {}

   public void addWarningPanels(GridBagConstraints gbc) throws IOException {}

   @Override
   public void hideSubPanels(EnumSet<SubPanelTypes> panels) {
      collapsed = true;

      if (collapseExpandButton != null) {
         collapseExpandButton.setIcon(
             new ImageIcon(CollapsePanel.class.getResource(ArenaComponentSettings.IMAGE_PATH + (collapsed
                 ? "maximize.gif"
                 : "minimize.gif"))));
      }

      setPanelVisibility(historySubPanels, panels, (SubPanelTypes) null);
      setPanelVisibility(refexSubPanels, panels, (SubPanelTypes) null);
      setPanelVisibility(historicalRefexSubPanels, panels, (SubPanelTypes) null);
      setPanelVisibility(alertSubPanels, panels, (SubPanelTypes) null);
      setPanelVisibility(templateSubPanels, panels, (SubPanelTypes) null);
   }

   private void setupCollapseExpandButton() {
      collapseExpandButton = new JButton(new AbstractAction("",
              new ImageIcon(ConceptViewRenderer.class.getResource(ArenaComponentSettings.IMAGE_PATH
                 + "maximize.gif"))) {

         /**
          *
          */
         private static final long serialVersionUID = 1L;
         @Override
         public void actionPerformed(ActionEvent e) {
            collapsed = !collapsed;

            if (collapsed) {
               hideSubPanels(DragPanelComponentVersion.this.parentCollapsePanel.getSubpanelsToShow());
            } else {
               showSubPanels(DragPanelComponentVersion.this.parentCollapsePanel.getSubpanelsToShow());
            }

            ((JButton) e.getSource()).setIcon(
                new ImageIcon(CollapsePanel.class.getResource(ArenaComponentSettings.IMAGE_PATH + (collapsed
                    ? "maximize.gif"
                    : "minimize.gif"))));
            updateCollapseExpandButton();
         }
      }) {
         @Override
         public void setBounds(int x, int y, int width, int height) {
            super.setBounds(x, y, width, Math.max(MIN_HEIGHT, height));
         }
         @Override
         public void setBounds(Rectangle r) {
            r.height = Math.max(MIN_HEIGHT, r.height);
            super.setBounds(r);
         }
         @Override
         public void resize(int width, int height) {
            super.resize(width, Math.max(MIN_HEIGHT, height));
         }
         @Override
         public void resize(Dimension d) {
            d.height = Math.max(MIN_HEIGHT, d.height);
            super.resize(d);
         }
         @Override
         public void setSize(int width, int height) {
            super.setSize(width, Math.max(MIN_HEIGHT, height));
         }
         @Override
         public void setSize(Dimension d) {
            d.height = Math.max(MIN_HEIGHT, d.height);
            super.setSize(d);
         }
         @Override
         public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();

            d.height = Math.max(MIN_HEIGHT, d.height);

            return d;
         }
         @Override
         public void setMinimumSize(Dimension d) {
            d.height = Math.max(MIN_HEIGHT, d.height);
            super.setMinimumSize(d);
         }
      };
   }

   @Override
   public void showConflicts(Collection<Integer> saptCol) {
      if (saptCol.contains(getComponentVersion().getSapNid())) {
         conflictLabel.setVisible(true);
      }

      for (DragPanelComponentVersion<?> panel : refexSubPanels) {
         panel.showConflicts(saptCol);
      }

      for (DragPanelComponentVersion<?> panel : historicalRefexSubPanels) {
         panel.showConflicts(saptCol);
      }

      for (DragPanelComponentVersion<?> panel : historySubPanels) {
         panel.showConflicts(saptCol);
      }
   }

   @Override
   public void showSubPanels(EnumSet<SubPanelTypes> panels) {
      collapsed = false;

      if (collapseExpandButton != null) {
         collapseExpandButton.setIcon(
             new ImageIcon(CollapsePanel.class.getResource(ArenaComponentSettings.IMAGE_PATH + (collapsed
                 ? "maximize.gif"
                 : "minimize.gif"))));
      }

      setPanelVisibility(historySubPanels, panels, SubPanelTypes.HISTORY);
      setPanelVisibility(refexSubPanels, panels, SubPanelTypes.REFEX);
      setPanelVisibility(historicalRefexSubPanels, panels,
                         EnumSet.of(SubPanelTypes.REFEX, SubPanelTypes.HISTORY));
      setPanelVisibility(alertSubPanels, panels, SubPanelTypes.ALERT);
      setPanelVisibility(templateSubPanels, panels, SubPanelTypes.TEMPLATE);
   }

   private void updateCollapseExpandButton() {
      if (getSubpanelCount() == 0) {
         if (collapseExpandButton.isVisible()) {
            collapseExpandButton.setVisible(false);
            collapseExpandButton.setEnabled(false);
         }
      } else {
         if (!collapseExpandButton.isVisible()) {
            collapseExpandButton.setVisible(true);
            collapseExpandButton.setEnabled(true);
         }

         StringBuilder toolTipBuffer = new StringBuilder();
         boolean       first         = true;

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

   //~--- get methods ---------------------------------------------------------

   protected JButton getActionMenuButton() {
      JButton popupMenuButton = new JButton(dynamicPopupImage);

      popupMenuButton.setPreferredSize(new Dimension(21, 16));
      popupMenuButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      popupMenuButton.setToolTipText("contextual editing actions");
      popupMenuButton.setOpaque(false);
      popupMenuButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));

      return popupMenuButton;
   }

   public int getAlertSubpanelCount() {
      return alertSubPanels.size();
   }

   public List<JComponent> getAlertSubpanels() {
      return alertSubPanels;
   }

   protected JButton getCollapseExpandButton() {
      collapseExpandButton.setPreferredSize(new Dimension(21, 16));
      collapseExpandButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      collapseExpandButton.setToolTipText("Collapse/Expand");
      collapseExpandButton.setOpaque(false);
      collapseExpandButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));
      updateCollapseExpandButton();
      addPanelsChangedActionListener(getSettings().getView().getPanelsChangedActionListener());

      return collapseExpandButton;
   }

   protected JButton getComponentActionMenuButton() {
      JButton actionButton = getActionMenuButton();

      (new GetActionListWorker(actionButton)).execute();

      return actionButton;
   }

   protected T getComponentVersion() {
      return getThingToDrag();
   }

   public static ImageIcon getConflictIcon() {
      return conflictIcon;
   }

   public static ImageIcon getGhostIcon() {
      return ghostIcon;
   }

   public List<? extends JComponent> getHistoricalRefexSubPanels() {
      return historicalRefexSubPanels;
   }

   public int getHistorySubpanelCount() {
      return historySubPanels.size() + historicalRefexSubPanelCount;
   }

   public List<? extends JComponent> getHistorySubpanels() {
      return historySubPanels;
   }

   protected JLabel getJLabel(String text) {
      JLabel l = new JLabel(text);

      l.setFont(l.getFont().deriveFont(getSettings().getFontSize()));
      l.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
      l.setMinimumSize(new Dimension(15, 30));

      return l;
   }

   protected TermComponentLabel getLabel(int nid, boolean canDrop, DescType textType) throws IOException {
      return getLabel(nid, canDrop, textType.getLabelText());
   }

   protected TermComponentLabel getLabel(int nid, boolean canDrop, LabelText textType) throws IOException {
      try {
         TermComponentLabel termLabel = new TermComponentLabel(textType);

         termLabel.setLineWrapEnabled(true);
         termLabel.getDropTarget().setActive(canDrop);
         termLabel.setFixedWidth(150);
         termLabel.setFont(termLabel.getFont().deriveFont(getSettings().getFontSize()));
         termLabel.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
         termLabel.setTermComponent(Terms.get().getConcept(nid));

         return termLabel;
      } catch (TerminologyException terminologyException) {
         throw new IOException(terminologyException);
      }
   }

   public abstract Collection<DragPanelComponentVersion<T>> getOtherVersionPanels()
           throws IOException, TerminologyException;

   public CollapsePanel getParentCollapsePanel() {
      return parentCollapsePanel;
   }

   public int getRefexSubpanelCount() {
      return refexSubPanels.size();
   }

   public List<? extends JComponent> getRefexSubpanels() {
      return refexSubPanels;
   }

   public int getSubpanelCount() {
      return getAlertSubpanelCount() + getRefexSubpanelCount() + getTemplateSubpanelCount()
             + getHistorySubpanelCount();
   }

   public int getTemplateSubpanelCount() {
      return templateSubPanels.size();
   }

   public List<JComponent> getTemplateSubpanels() {
      return templateSubPanels;
   }

   @Override
   public String getUserString(T obj) {
      return obj.toUserString();
   }

   @Override
   public boolean isExpanded() {
      return !collapsed;
   }

   //~--- set methods ---------------------------------------------------------

   private void setPanelVisibility(List<? extends JComponent> componentList,
                                   EnumSet<SubPanelTypes> subpanelsToShow,
                                   EnumSet<SubPanelTypes> panelTypes) {
      for (JComponent panel : componentList) {
         if (panelTypes == null) {
            panel.setVisible(false);
         } else {
            panel.setVisible(subpanelsToShow.containsAll(panelTypes));
         }
      }
   }

   private void setPanelVisibility(List<? extends JComponent> componentList,
                                   EnumSet<SubPanelTypes> subpanelsToShow, SubPanelTypes panelType) {
      for (JComponent panel : componentList) {
         if (panelType == null) {
            panel.setVisible(false);
         } else {
            panel.setVisible(subpanelsToShow.contains(panelType));
         }
      }
   }

   //~--- inner classes -------------------------------------------------------

   private class GetActionListWorker extends SwingWorker<Collection<Action>, Collection<Action>> {
      private JButton actionButton;

      //~--- constructors -----------------------------------------------------

      public GetActionListWorker(JButton actionButton) {
         this.actionButton = actionButton;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      protected Collection<Action> doInBackground() throws Exception {
         return getMenuActions();
      }

      @Override
      protected void done() {
         try {
            Collection<Action> actions = get();

            actionButton.addActionListener(new DoDynamicPopup(actions));

            if ((actions == null) || actions.isEmpty()) {
               actionButton.setVisible(false);
            }
         } catch (InterruptedException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         } catch (ExecutionException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         }
      }
   }
}
