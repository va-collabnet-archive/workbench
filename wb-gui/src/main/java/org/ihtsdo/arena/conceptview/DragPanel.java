package org.ihtsdo.arena.conceptview;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.TermLabelMaker;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.dnd.DragMonitor;
import org.dwfa.ace.log.AceLog;

import org.ihtsdo.arena.ScrollablePanel;
import org.ihtsdo.arena.ScrollablePanel.ScrollDirection;
import org.ihtsdo.arena.context.action.DropActionPanel;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.api.relationship.group.RelGroupVersionBI;
import org.ihtsdo.tk.drools.facts.Context;
import org.ihtsdo.tk.drools.facts.FactFactory;
import org.ihtsdo.tk.spec.SpecBI;

import org.intsdo.tk.drools.manager.DroolsExecutionManager;

import sun.awt.dnd.SunDragSourceContextPeer;

//~--- JDK imports ------------------------------------------------------------

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.drools.facts.View;

public abstract class DragPanel<T extends Object> extends JPanel implements Transferable {
   private static final AtomicInteger atomicInt = new AtomicInteger();
   private static final int           minSize   = 24;

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private static boolean    anotherDragging  = false;

   //~--- fields --------------------------------------------------------------

   private Collection<Action>     actionList             =
      Collections.synchronizedCollection(new ArrayList<Action>());
   private Collection<JComponent> dropComponents         =
      Collections.synchronizedList(new ArrayList<JComponent>());
   private int                    dropPopupInset         = 20;
   @SuppressWarnings("unused")
   private Set<DataFlavor>        supportedImportFlavors = null;
   private Set<File>              kbFiles                = new HashSet<File>();
   private boolean                dragEnabled;
   private DropPanelActionManager dropPanelMgr;
   private int                    id;
   protected boolean              inGroup;
   private Object                 lastThingBeingDropped;
   protected T                    thingToDrag;
   DragPanelTransferHandler       transferHandler;
   protected ConceptViewLayout    viewLayout;

   //~--- constructors --------------------------------------------------------

   public DragPanel(ConceptViewLayout viewLayout, T component) {
      super();
      this.thingToDrag = component;
      this.id          = atomicInt.incrementAndGet();
      setup(viewLayout);
   }

   public DragPanel(LayoutManager layout, ConceptViewLayout viewLayout, T component) {
      super(layout);
      this.thingToDrag = component;
      this.id          = atomicInt.incrementAndGet();
      setup(viewLayout);
   }

   //~--- methods -------------------------------------------------------------

   public void removeReferences() {
      dropPanelMgr.removeReferences();
   }

   private void setup(ConceptViewLayout viewLayout) {
      this.viewLayout = viewLayout;
      this.setMinimumSize(new Dimension(minSize, minSize));
      this.kbFiles.add(new File("drools-rules/ContextualDropActions.drl"));
      dropPanelMgr = new DropPanelActionManager();

      try {
         DroolsExecutionManager.setup(DragPanel.class.getCanonicalName(), kbFiles);
      } catch (IOException e1) {
         AceLog.getAppLog().alertAndLogException(e1);
      }
   }

   public void setupDrag(T thingToDrag) {
      this.thingToDrag = thingToDrag;

      if (this.transferHandler == null) {
         this.transferHandler = new DragPanelTransferHandler("draggedThing");
         this.setTransferHandler(this.transferHandler);
         DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY,
                 new DragGestureListenerWithImage(new DragPanelDragSourceListener()));
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the value of the <code>dragEnabled</code> property.
    *
    * @return the value of the <code>dragEnabled</code> property
    * @see #setDragEnabled
    * @since 1.4
    */
   public boolean getDragEnabled() {
      return dragEnabled;
   }

   public Image getDragImage() {
      Image    image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
      Graphics g     = image.getGraphics();

      paint(g);

      FilteredImageSource fis = new FilteredImageSource(image.getSource(),
                                   TermLabelMaker.getTransparentFilter());

      image = Toolkit.getDefaultToolkit().createImage(fis);

      return image;
   }

   public int getDropPopupInset() {
      return this.dropPopupInset;
   }

   public int getId() {
      return id;
   }

   protected void getKbActions(Object thingToDrop) {
      getSettings().getView().setupDrop(thingToDrop);
      actionList.clear();
      actionList.addAll(getKbActionsNoSetup(thingToDrop));
   }

   private Collection<Action> getKbActionsNoSetup(Object thingToDrop) {
      ArrayList<Action> list = new ArrayList<Action>();

      try {
         if (getSettings().getConcept() != null) {
            if (I_GetConceptData.class.isAssignableFrom(thingToDrop.getClass())) {
               I_GetConceptData conceptToDrop = (I_GetConceptData) thingToDrop;

               thingToDrop = Ts.get().getConceptVersion(getSettings().getConfig().getViewCoordinate(),
                       conceptToDrop.getConceptNid());
            } else if (ComponentVersionBI.class.isAssignableFrom(thingToDrop.getClass())
                       || SpecBI.class.isAssignableFrom(thingToDrop.getClass())) {
               ViewCoordinate      coordinate = getSettings().getConfig().getViewCoordinate();
               Map<String, Object> globals    = new HashMap<String, Object>();

               globals.put("vc", coordinate);
               globals.put("actions", list);
               globals.put("cvSettings", getSettings());
               globals.put("config", getSettings().getConfig());

               if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                  AceLog.getAppLog().fine("dropTarget: " + thingToDrag);
                  AceLog.getAppLog().fine("thingToDrop: " + thingToDrop);
               }
               
               View viewType;
               if(getSettings().getRelAssertionType() == RelAssertionType.STATED){
                   viewType = View.STATED;
               }else if (getSettings().getRelAssertionType() == RelAssertionType.INFERRED){
                   viewType = View.INFERRED;
               }else if (getSettings().getRelAssertionType() == RelAssertionType.INFERRED_THEN_STATED){
                   viewType = View.STATED_AND_INFERRED;
               }else{
                   RelAssertionType relAssertionType = getSettings().getRelAssertionType();
                   viewType = View.STATED;
               }

               Collection<Object> facts = new ArrayList<Object>();

               facts.add(FactFactory.get(Context.DROP_OBJECT, thingToDrop, coordinate));
               facts.add(FactFactory.get(Context.DROP_TARGET, thingToDrag, coordinate));
               facts.add(FactFactory.get(viewType));
               DroolsExecutionManager.fireAllRules(DragPanel.class.getCanonicalName(), kbFiles, globals,
                       facts, false);
            }
         }
      } catch (Throwable e) {
         AceLog.getAppLog().alertAndLogException(e);
      }

      return list;
   }

   protected Collection<Action> getMenuActions() {
      return getKbActionsNoSetup(thingToDrag);
   }

   @Override
   public Dimension getMinimumSize() {
      Dimension dmnsn = super.getMinimumSize();

      if (dmnsn.width < minSize) {
         dmnsn.width = minSize;
      }

      if (dmnsn.height < minSize) {
         dmnsn.height = minSize;
      }

      return dmnsn;
   }

   public abstract DataFlavor getNativeDataFlavor();

   @Override
   public Dimension getPreferredSize() {
      Dimension dmnsn = super.getPreferredSize();

      if (dmnsn.width < minSize) {
         dmnsn.width = minSize;
      }

      if (dmnsn.height < minSize) {
         dmnsn.height = minSize;
      }

      return dmnsn;
   }

   public ConceptViewSettings getSettings() {
      return viewLayout.getSettings();
   }

   public Set<DataFlavor> getSupportedImportFlavors() {
      return DragPanelDataFlavors.dragPanelFlavorSet;
   }

   public T getThingToDrag() {
      return thingToDrag;
   }

   @Override
   public T getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
      return thingToDrag;
   }

   protected abstract int getTransferMode();

   public abstract String getUserString(T obj);

   public boolean isInGroup() {
      return inGroup;
   }

   public boolean isReallyVisible() {
      if (!isVisible()) {
         return false;
      }

      Point     mouseLocation = MouseInfo.getPointerInfo().getLocation();
      Container parent        = getParent();

      while (parent != null) {
         Point parentLoc = new Point(0, 0);

         SwingUtilities.convertPointToScreen(parentLoc, parent);

         Rectangle parentBounds = parent.getBounds();

         parentBounds.x = parentLoc.x;
         parentBounds.y = parentLoc.y;

         if (!parentBounds.contains(mouseLocation)) {
            return false;
         }

         parent = parent.getParent();
      }

      return true;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setBounds(Rectangle rctngl) {
      if (rctngl.width < minSize) {
         rctngl.width = minSize;
      }

      if (rctngl.height < minSize) {
         rctngl.height = minSize;
      }

      super.setBounds(rctngl);
   }

   @Override
   public void setBounds(int x, int y, int width, int height) {
      if (width < minSize) {
         width = minSize;
      }

      if (height < minSize) {
         height = minSize;
      }

      super.setBounds(x, y, width, height);
   }

   public void setDragEnabled(boolean b) {
      if (b && GraphicsEnvironment.isHeadless()) {
         throw new HeadlessException();
      }

      dragEnabled = b;
   }

   public void setDropPopupInset(int inset) {
      this.dropPopupInset = inset;
   }

   public void setInGroup(boolean inGroup) {
      this.inGroup = inGroup;
   }

   @Override
   public void setMinimumSize(Dimension dmnsn) {
      if (dmnsn.width < minSize) {
         dmnsn.width = minSize;
      }

      if (dmnsn.height < minSize) {
         dmnsn.height = minSize;
      }

      super.setMinimumSize(dmnsn);
   }

   @Override
   public void setSize(Dimension dmnsn) {
      if (dmnsn.width < minSize) {
         dmnsn.width = minSize;
      }

      if (dmnsn.height < minSize) {
         dmnsn.height = minSize;
      }

      super.setSize(dmnsn);
   }

   @Override
   public void setSize(int w, int h) {
      if (w < minSize) {
         w = minSize;
      }

      if (h < minSize) {
         h = minSize;
      }

      super.setSize(w, h);
   }

   //~--- inner classes -------------------------------------------------------

   private class DragGestureListenerWithImage implements DragGestureListener {
      DragSourceListener dsl;

      //~--- constructors -----------------------------------------------------

      public DragGestureListenerWithImage(DragSourceListener dsl) {
         super();
         this.dsl = dsl;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public void dragGestureRecognized(DragGestureEvent dge) {
         Image dragImage   = getDragImage();
         Point imageOffset = new Point(0, 0);

         try {
            dge.startDrag(DragSource.DefaultCopyDrop, dragImage, imageOffset,
                          transferHandler.createTransferable(DragPanel.this), dsl);
         } catch (InvalidDnDOperationException e) {
            AceLog.getAppLog().log(Level.WARNING, e.getMessage(), e);
            AceLog.getAppLog().log(Level.INFO, "Resetting SunDragSourceContextPeer [1]");
            SunDragSourceContextPeer.setDragDropInProgress(false);
         }
      }
   }


   private class DragPanelDragSourceListener implements DragSourceListener {
      @Override
      public void dragDropEnd(DragSourceDropEvent dsde) {}

      @Override
      public void dragEnter(DragSourceDragEvent dsde) {}

      @Override
      public void dragExit(DragSourceEvent dse) {}

      @Override
      public void dragOver(DragSourceDragEvent dsde) {}

      @Override
      public void dropActionChanged(DragSourceDragEvent dsde) {}
   }


   protected class DragPanelTransferHandler extends TransferHandler {
      private static final long serialVersionUID = 1L;

      //~--- constructors -----------------------------------------------------

      public DragPanelTransferHandler(String propName) {
         super(propName);
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public boolean canImport(TransferSupport support) {
         try {
            Object thingBeingDropped = null;

            if (support.isDataFlavorSupported(DragPanelDataFlavors.conceptFlavor)) {
               thingBeingDropped = (I_GetConceptData) support.getTransferable().getTransferData(
                  DragPanelDataFlavors.conceptFlavor);
            } else if (support.isDataFlavorSupported(DragPanelDataFlavors.descVersionFlavor)) {
               thingBeingDropped = (DescriptionVersionBI) support.getTransferable().getTransferData(
                  DragPanelDataFlavors.descVersionFlavor);
            } else if (support.isDataFlavorSupported(DragPanelDataFlavors.relGroupFlavor)) {
               thingBeingDropped = (RelGroupVersionBI) support.getTransferable().getTransferData(
                  DragPanelDataFlavors.relGroupFlavor);
            } else if (support.isDataFlavorSupported(DragPanelDataFlavors.relVersionFlavor)) {
               thingBeingDropped = (RelationshipVersionBI) support.getTransferable().getTransferData(
                  DragPanelDataFlavors.relVersionFlavor);
            } else if (support.getComponent() == DragPanel.this) {
               thingBeingDropped = DragPanel.this.thingToDrag;
            }

            if (thingBeingDropped != null) {
               if ((lastThingBeingDropped == null)
                       || (thingBeingDropped.equals(lastThingBeingDropped) == false)) {
                  lastThingBeingDropped = thingBeingDropped;
                  actionList.clear();
                  getKbActions(thingBeingDropped);
                  dropComponents.clear();

                  if (actionList.size() > -1) {
                     for (Action a : actionList) {
                        try {
                           dropComponents.add(new DropActionPanel(a));
                        } catch (TooManyListenersException e) {
                           AceLog.getAppLog().alertAndLogException(e);
                        }
                     }

                     return true;
                  }
               }
            } else {
               System.out.println("Changing to null");
               actionList.clear();
               dropComponents.clear();
            }
         } catch (UnsupportedFlavorException e) {
            AceLog.getAppLog().alertAndLogException(e);
         } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
         }

         return false;
      }

      @Override
      public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
         throw new UnsupportedOperationException();
      }

      @Override
      protected Transferable createTransferable(JComponent c) {
         return super.createTransferable(c);
      }

      @Override
      public void exportAsDrag(JComponent comp, InputEvent e, int action) {
         super.exportAsDrag(comp, e, action);
      }

      @Override
      protected void exportDone(JComponent source, Transferable data, int action) {
         super.exportDone(source, data, action);
      }

      @Override
      public boolean importData(TransferSupport support) {
         return super.importData(support);
      }

      @Override
      public boolean importData(JComponent comp, Transferable t) {
         return super.importData(comp, t);
      }

      //~--- get methods ------------------------------------------------------

      @Override
      public int getSourceActions(JComponent c) {
         return DnDConstants.ACTION_COPY;
      }

      @Override
      public Icon getVisualRepresentation(Transferable t) {
         return super.getVisualRepresentation(t);
      }
   }


   public class DropPanelActionManager
           implements ActionListener, I_DispatchDragStatus, Comparable<DropPanelActionManager> {
      private boolean                dragging            = false;
      private JComponent             dropPanel           = new JLabel("dropPanel");
      private boolean                panelAdded          = false;
      private boolean                gridLayout          = true;
      private int                    dpamId              = atomicInt.incrementAndGet();
      private Collection<JComponent> addedDropComponents = new ArrayList<JComponent>();
      private DropPanelProxy         dpp;
      private JLayeredPane           rootLayers;
      private JPanel                 sfp;
      private JScrollPane            sfpScroller;
      private Timer                  timer;

      //~--- constructors -----------------------------------------------------

      public DropPanelActionManager() {
         super();
         this.dpp = new DropPanelProxy(this);
         DragPanel.this.addHierarchyListener(this.dpp);
         timer = new Timer(250, this);
         viewLayout.getDropPanelActionManagers().add(this);

      }

      //~--- methods ----------------------------------------------------------

      @Override
      public void actionPerformed(ActionEvent e) {
         if (DragPanel.this.isShowing() == false) {
            return;
         }

         if (dragging) {
            if (addedDropComponents.equals(dropComponents) == false) {
               sfp.removeAll();
               addedDropComponents = new ArrayList<JComponent>();

               for (JComponent c : dropComponents) {
                  addedDropComponents.add(c);
                  sfp.add(c);
               }
            }

            if (addedDropComponents.isEmpty()) {
               return;
            }

            Point mouseLocation             = MouseInfo.getPointerInfo().getLocation();
            Point mouseLocationForDragPanel = mouseLocation.getLocation();

            SwingUtilities.convertPointFromScreen(mouseLocationForDragPanel, DragPanel.this);

            if (DragPanel.this.contains(mouseLocationForDragPanel)) {
               if (DragPanel.this.isInGroup()) {
                  setDragPanelVisible(true);
               } else if (!anotherDragging && DragPanel.this.isShowing()) {
                  anotherDragging = true;
                  setDragPanelVisible(true);
               } else if (DragPanelRelGroup.class.isAssignableFrom(DragPanel.this.getClass())) {
                  setDragPanelVisible(true);
               }
            } else {
               Point mouseLocationForDropPanel = mouseLocation.getLocation();

               SwingUtilities.convertPointFromScreen(mouseLocationForDropPanel, dropPanel);

               if (dropPanel.contains(mouseLocationForDropPanel) && panelAdded) {
                  if (mouseLocationForDropPanel.x < 10) {
                     BoundedRangeModel scrollerModel = sfpScroller.getHorizontalScrollBar().getModel();

                     scrollerModel.setExtent(1);

                     if (scrollerModel.getValue() > scrollerModel.getMinimum()) {
                        int newValue = Math.max(scrollerModel.getValue() - 20, 0);

                        scrollerModel.setValue(newValue);
                     }
                  } else if (dropPanel.getWidth() - mouseLocationForDropPanel.x < 10) {
                     BoundedRangeModel scrollerModel = sfpScroller.getHorizontalScrollBar().getModel();

                     scrollerModel.setExtent(1);

                     if (scrollerModel.getValue() < scrollerModel.getMaximum()) {
                        int newValue = Math.min(scrollerModel.getValue() + 20, scrollerModel.getMaximum());

                        scrollerModel.setValue(newValue);
                     }
                  }
               } else {
                  setDragPanelVisible(false);
               }
            }
         } else {
            setDragPanelVisible(false);
         }
      }

      @Override
      public int compareTo(DropPanelActionManager o) {
         return this.dpamId - o.dpamId;
      }

      @Override
      public void dragFinished() {

//       AceLog.getAppLog().info("stopping drag for: " + id);
         timer.stop();
         dragging = false;
         setDragPanelVisible(false);

         if (dropPanel != null) {
            if (rootLayers != null) {
               rootLayers.remove(dropPanel);
            }

            dropPanel = null;
         }

         anotherDragging = false;
         addedDropComponents.clear();
         dropComponents.clear();
         actionList.clear();
         lastThingBeingDropped = null;
      }

      @Override
      public void dragStarted() {

//       AceLog.getAppLog().info("starting drag for: " + id);
         if ((thingToDrag == null) || (DragPanel.this.isVisible() == false)) {
            return;
         }

         (new DragStarter()).execute();
      }

      public void removeReferences() {
         if (viewLayout.stop) {
            timer.stop();
            timer.removeActionListener(this);
            DragMonitor.removeDragListener(dpp);

         }
      }

      //~--- set methods ------------------------------------------------------

      private void setDragPanelVisible(boolean visible) {

//       AceLog.getAppLog().info("set drag visible: " + visible + " for: "+ id);
         if (visible) {
            if (!panelAdded) {
               if (DragPanel.this.isReallyVisible()) {
                  panelAdded = true;
                  sfpScroller.getHorizontalScrollBar().setValue(0);
                  sfpScroller.getVerticalScrollBar().setValue(0);

                  Point loc = DragPanel.this.getLocationOnScreen();

                  rootLayers = DragPanel.this.getRootPane().getLayeredPane();
                  rootLayers.add(dropPanel, JLayeredPane.PALETTE_LAYER);

                  if (DragPanelRelGroup.class.isAssignableFrom(DragPanel.this.getClass())) {
                     rootLayers.setLayer(dropPanel, JLayeredPane.PALETTE_LAYER, -1);
                  } else {
                     rootLayers.setLayer(dropPanel, JLayeredPane.PALETTE_LAYER, 1);
                  }

                  SwingUtilities.convertPointFromScreen(loc, rootLayers);
                  dropPanel.setSize(DragPanel.this.getWidth() - getDropPopupInset(), 48);
                  dropPanel.setLocation(loc.x + getDropPopupInset(), loc.y - dropPanel.getHeight());
                  dropPanel.setVisible(true);
                  dropPanel.setBorder(BorderFactory.createLineBorder(Color.gray));
               }
            }
         } else {
            if (panelAdded) {
               if (!DragPanel.this.isInGroup()) {
                  anotherDragging = false;
               }

               panelAdded = false;
               dropPanel.setVisible(false);
            }
         }
      }

      //~--- inner classes ----------------------------------------------------

      private class DragStarter extends SwingWorker<String, String> {
         @Override
         protected String doInBackground() throws Exception {
            return getUserString(thingToDrag);
         }

         @Override
         protected void done() {
            try {
               String        userString = get();
               LayoutManager layout     = new FlowLayout(FlowLayout.LEADING, 5, 5);
               JPanel        sfp        = new ScrollablePanel(layout, ScrollDirection.LEFT_TO_RIGHT);

               if (gridLayout) {
                  layout = new GridLayout(1, 0, 5, 5);
                  sfp    = new JPanel(layout);
               }

               sfp.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

               JPanel dropPanel        = new JPanel(new BorderLayout());
               JPanel dropTargetPanels = new JPanel(new GridLayout(1, 1));
               JLabel dropPanelLabel   = new JLabel("    " + userString);

//             AceLog.getAppLog().info("Setting up drag panel for: " + id + " " + userString);
               dropPanelLabel.setFont(dropPanelLabel.getFont().deriveFont(getSettings().getFontSize()));
               dropPanelLabel.setOpaque(true);
               dropPanelLabel.setBackground(ConceptViewTitle.TITLE_COLOR);

               if (dropPanel == null) {
                  return;
               }

               dropPanel.add(dropPanelLabel, BorderLayout.PAGE_START);

               if (dropPanel == null) {
                  return;
               }

               dropPanel.add(dropTargetPanels, BorderLayout.CENTER);

               JScrollPane sfpScroller = new JScrollPane(sfp);

               sfpScroller.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
               sfpScroller.setAutoscrolls(true);
               sfpScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
               sfpScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
               dropTargetPanels.add(sfpScroller);
               DropPanelActionManager.this.dropPanel   = dropPanel;
               DropPanelActionManager.this.sfpScroller = sfpScroller;
               DropPanelActionManager.this.sfp         = sfp;
               dragging                                = true;
               timer.start();
            } catch (InterruptedException ex) {
               AceLog.getAppLog().alertAndLogException(ex);
            } catch (ExecutionException ex) {
               AceLog.getAppLog().alertAndLogException(ex);
            }

            if (!DragMonitor.isDragging()) {
               timer.stop();
               dragging        = false;
               anotherDragging = false;
               panelAdded      = false;

               if (dropPanel != null) {
                  dropPanel.setVisible(false);

                  if (rootLayers != null) {
                     rootLayers.remove(dropPanel);
                  }

                  dropPanel = null;
               }
            }
         }
      }
   }
}
