
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.tree;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.TermLabelMaker;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.dnd.AceTransferAction;
import org.dwfa.ace.dnd.ConceptTransferable;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.tree.ExpandNodeSwingWorker;
import org.dwfa.ace.tree.TermTreeHelper;
import org.dwfa.tapi.TerminologyException;

import sun.awt.dnd.SunDragSourceContextPeer;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.KeyEvent;
import java.awt.image.FilteredImageSource;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author kec
 */
public class TaxonomyTree extends JTree {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   //~--- fields --------------------------------------------------------------

   private List<ChangeListener> workerFinishedListeners = new ArrayList<ChangeListener>();
   private I_ConfigAceFrame     config;
   private TaxonomyHelper       helper;
   public Object                lastPropagationId;
   private TreePath             lastSelection;
   private TreePath             nextToLastSelection;
   public JScrollPane           scroller;

   //~--- constructors --------------------------------------------------------

   public TaxonomyTree(I_ConfigAceFrame config) {
      this(config, null);
   }

   protected TaxonomyTree(I_ConfigAceFrame config, TaxonomyHelper helper) {
      super();
      this.config = config;
      this.helper = helper;
      DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY,
              new DragGestureListenerWithImage(new TermLabelDragSourceListener()));

      InputMap imap = this.getInputMap();

      imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
               TransferHandler.getCutAction().getValue(Action.NAME));
      imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
               TransferHandler.getCopyAction().getValue(Action.NAME));
      imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
               TransferHandler.getPasteAction().getValue(Action.NAME));
      imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK),
               TransferHandler.getCutAction().getValue(Action.NAME));
      imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK),
               TransferHandler.getCopyAction().getValue(Action.NAME));
      imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK),
               TransferHandler.getPasteAction().getValue(Action.NAME));

      ActionMap map = this.getActionMap();

      map.put("cut", new AceTransferAction("cut"));
      map.put("copy", new AceTransferAction("copy"));
      map.put("paste", new AceTransferAction("paste"));
      addTreeSelectionListener(new SelectionListener());
   }

   //~--- methods -------------------------------------------------------------

   public void addWorkerFinishedListener(ChangeListener l) {
      workerFinishedListeners.add(l);
   }

   public void removeWorkerFinishedListener(ChangeListener l) {
      workerFinishedListeners.remove(l);
   }

   public void workerFinished(ExpandNodeSwingWorker worker) {
      ChangeEvent          event = new ChangeEvent(worker);
      List<ChangeListener> listeners;

      synchronized (workerFinishedListeners) {
         listeners = new ArrayList<ChangeListener>(workerFinishedListeners);
      }

      for (ChangeListener l : listeners) {
         l.stateChanged(event);
      }
   }

   //~--- get methods ---------------------------------------------------------

   public I_ConfigAceFrame getConfig() {
      return config;
   }

   public Object getLastPropagationId() {
      return lastPropagationId;
   }

   public JScrollPane getScroller() {
      return scroller;
   }

   //~--- set methods ---------------------------------------------------------

   public void setScroller(JScrollPane scroller) {
      this.scroller = scroller;
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
         int      selRow = getRowForLocation(dge.getDragOrigin().x, dge.getDragOrigin().y);
         TreePath path   = getPathForLocation(dge.getDragOrigin().x, dge.getDragOrigin().y);

         if (selRow != -1) {
            try {
               DefaultMutableTreeNode node        = (DefaultMutableTreeNode) path.getLastPathComponent();
               I_GetConceptData       obj         = (I_GetConceptData) node.getUserObject();
               Image                  dragImage   = getDragImage(obj);
               Point                  imageOffset = new Point(-10,
                                                       -(dragImage.getHeight(TaxonomyTree.this) + 1));

               try {
                  dge.startDrag(DragSource.DefaultCopyDrop, dragImage, imageOffset, getTransferable(obj),
                                dsl);
               } catch (InvalidDnDOperationException e) {

                  // AceLog.getAppLog().log(Level.WARNING, e.getMessage(), e);
                  AceLog.getAppLog().log(Level.INFO, "Resetting SunDragSourceContextPeer [4.]");
                  SunDragSourceContextPeer.setDragDropInProgress(false);
               } catch (Exception ex) {
                  AceLog.getAppLog().alertAndLogException(ex);
               }
            } catch (IOException ex) {
               AceLog.getAppLog().alertAndLogException(ex);
            }
         }
      }

      //~--- get methods ------------------------------------------------------

      public Image getDragImage(I_GetConceptData obj) throws IOException {
         I_DescriptionTuple desc = obj.getDescTuple(config.getTreeDescPreferenceList(), config);

         if (desc == null) {
            desc = obj.getDescriptions().iterator().next().getFirstTuple();
         }

         JLabel dragLabel = TermLabelMaker.newLabel(desc, false, false).getLabel();

         dragLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

         Image dragImage = createImage(dragLabel.getWidth(), dragLabel.getHeight());

         dragLabel.setVisible(true);

         Graphics og = dragImage.getGraphics();

         og.setClip(dragLabel.getBounds());
         dragLabel.paint(og);
         og.dispose();

         FilteredImageSource fis = new FilteredImageSource(dragImage.getSource(),
                                      TermLabelMaker.getTransparentFilter());

         dragImage = Toolkit.getDefaultToolkit().createImage(fis);

         return dragImage;
      }

      private Transferable getTransferable(I_GetConceptData obj) throws TerminologyException, IOException {
         return new ConceptTransferable(Terms.get().getConcept(obj.getConceptNid()));
      }
   }


   private class SelectionListener implements TreeSelectionListener {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
         nextToLastSelection = lastSelection;
         lastSelection       = e.getNewLeadSelectionPath();
      }
   }


   private class TermLabelDragSourceListener implements DragSourceListener {
      @Override
      public void dragDropEnd(DragSourceDropEvent dsde) {

         // TODO Auto-generated method stub
      }

      @Override
      public void dragEnter(DragSourceDragEvent dsde) {

         // TODO Auto-generated method stub
      }

      @Override
      public void dragExit(DragSourceEvent dse) {

         // TODO Auto-generated method stub
      }

      @Override
      public void dragOver(DragSourceDragEvent dsde) {

         // TODO Auto-generated method stub
      }

      @Override
      public void dropActionChanged(DragSourceDragEvent dsde) {

         // TODO Auto-generated method stub
      }
   }
}
