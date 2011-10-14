
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.taxonomy;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.ACE;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.IdentifierSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.log.AceLog;

import org.ihtsdo.arena.conceptview.ConceptViewRenderer;
import org.ihtsdo.concurrent.future.FutureHelper;
import org.ihtsdo.taxonomy.TaxonomyNodeRenderer.DescTypeToRender;
import org.ihtsdo.taxonomy.nodes.InternalNode;
import org.ihtsdo.taxonomy.nodes.RootNode;
import org.ihtsdo.taxonomy.nodes.TaxonomyNode;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidList;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.TermChangeListener;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.TreePath;

/**
 *
 * @author kec
 */
public class TaxonomyHelper extends TermChangeListener implements PropertyChangeListener {
   private static ImageIcon statedView =
      new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/graph_edge.png"));
   private static ImageIcon preferredDisplay =
      new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/component.png"));
   private static ImageIcon inferredView =
      new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/chrystal_ball.png"));
   private static ImageIcon inferredThenStatedView =
      new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/inferred-then-stated.png"));
   private static ImageIcon fsnDisplay =
      new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/component_yellow.png"));

   //~--- fields --------------------------------------------------------------

   private boolean              displayingFsn = true;
   private I_ConfigAceFrame     aceFrameConfig;
   private ActivityPanel        activity;
   private RelAssertionType     assertionType;
   private JButton              fsnPreferredButton;
   private String               helperName;
   private TaxonomyModel        model;
   private TaxonomyNodeRenderer renderer;
   private JButton              statedInferredButton;
   private TaxonomyTree         tree;

   //~--- constant enums ------------------------------------------------------

   static enum NodeAction { CHILDREN_CHANGED, ADDED_AS_PARENT, DISPLAY_CHANGED }

   //~--- constructors --------------------------------------------------------

   public TaxonomyHelper(I_ConfigAceFrame config, String helperName) {
      super();
      this.aceFrameConfig = config;
      this.assertionType  = config.getRelAssertionType();
      Ts.get().addTermChangeListener(this);
      this.helperName = helperName;
      this.aceFrameConfig.addPropertyChangeListener(this);
   }

   //~--- methods -------------------------------------------------------------

   public synchronized void addMouseListener(MouseListener ml) {
      tree.addMouseListener(ml);
   }

   public void addTreeSelectionListener(TreeSelectionListener tsl) {
      tree.addTreeSelectionListener(tsl);
   }

   @Override
   public void changeNotify(long sequence, Set<Integer> changedXrefs, Set<Integer> changedComponents) {
      ChangeWorker changeWorker = new ChangeWorker(sequence, changedXrefs, changedComponents);

      FutureHelper.addFuture(ACE.threadPool.submit(changeWorker));
   }

   protected void collapseTree(TreeExpansionEvent evt, I_ConfigAceFrame aceFrameConfig) {
      TaxonomyNode node = handleCollapse(evt, aceFrameConfig);
   }

   protected void expandTree(TreeExpansionEvent evt) {
      TaxonomyNode node = (TaxonomyNode) evt.getPath().getLastPathComponent();
   }

   private TaxonomyNode handleCollapse(TreeExpansionEvent evt, I_ConfigAceFrame aceFrameConfig) {
      TaxonomyNode node = (TaxonomyNode) evt.getPath().getLastPathComponent();

      return node;
   }

   public void handleDisplayChange() {
      ChangeDisplayWorker changeDisplayWorker = new ChangeDisplayWorker();

      FutureHelper.addFuture(ACE.threadPool.submit(changeDisplayWorker));
   }

   private void handleRelTypeChange() {
      ViewCoordinate vc = model.ts.getViewCoordinate();

      switch (assertionType) {
      case INFERRED :
         assertionType = RelAssertionType.INFERRED_THEN_STATED;
         statedInferredButton.setIcon(inferredThenStatedView);
         statedInferredButton.setToolTipText("showing inferred then stated, toggle to show stated...");
         vc.setRelAssertionType(assertionType);
         model.ts = Ts.get().getSnapshot(vc);
         updateHierarchyView("changed from stated to inferred then stated");

         break;

      case INFERRED_THEN_STATED :
         assertionType = RelAssertionType.STATED;
         statedInferredButton.setIcon(statedView);
         statedInferredButton.setToolTipText("showing stated, toggle to show inferred...");
         vc.setRelAssertionType(assertionType);
         model.ts = Ts.get().getSnapshot(vc);
         updateHierarchyView("changed from inferred to stated");

         break;

      case STATED :
         assertionType = RelAssertionType.INFERRED;
         statedInferredButton.setIcon(inferredView);
         statedInferredButton.setToolTipText("showing inferred, toggle to show inferred then stated...");
         vc.setRelAssertionType(assertionType);
         model.ts = Ts.get().getSnapshot(vc);
         updateHierarchyView("changed from stated to inferred");

         break;
      }
   }

   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals("roots")) {
         updateNewModel(evt.getPropertyName());
      } else if ("viewPositions".equals(evt.getPropertyName())
                 || "showPathInfoInTaxonomy".equals(evt.getPropertyName())
                 || "showRefsetInfoInTaxonomy".equals(evt.getPropertyName())
                 || "showViewerImagesInTaxonomy".equals(evt.getPropertyName())
                 || "updateHierarchyView".equals(evt.getPropertyName())) {
         updateHierarchyView(evt.getPropertyName());
      }
   }

   public void removeTreeSelectionListener(TreeSelectionListener tsl) {
      tree.removeTreeSelectionListener(tsl);
   }

   @Override
   public String toString() {
      return helperName;
   }

   protected void treeSelectionChanged(TreeSelectionEvent evt) {
      TaxonomyNode node = (TaxonomyNode) evt.getPath().getLastPathComponent();

      if ((node != null) &&!(node instanceof RootNode)) {
         try {
            aceFrameConfig.setHierarchySelection((I_GetConceptData) Ts.get().getConcept(node.getCnid()));
         } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         }
      } else {
         aceFrameConfig.setHierarchySelection(null);
      }
   }

   public void updateHierarchyView(String propChangeName) {
      RootNode root       = model.getRoot();
      int      childCount = root.getChildren().size();

      for (int i = 0; i < childCount; i++) {
         InternalNode childNode = (InternalNode) model.getChild(root, i);

         model.nodeFactory.removeDescendents(childNode);

         TreePath tp = new TreePath(NodePath.getTreePath(model, childNode));

         tree.collapseRow(i);
         tree.collapsePath(tp);
      }
   }

   public void updateNewModel(String propChangeName) {
      try {
         model = new TaxonomyModel(aceFrameConfig.getViewCoordinate(),
                                   new NidList(aceFrameConfig.getRoots().getSetValues()), renderer, tree);
      } catch (IOException ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      } catch (Exception ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }
   }

   //~--- get methods ---------------------------------------------------------

   public RelAssertionType getAssertionType() {
      return assertionType;
   }

   public JScrollPane getHierarchyPanel() throws IOException, Exception {
      if (tree != null) {
         for (TreeExpansionListener tel : tree.getTreeExpansionListeners()) {
            tree.removeTreeExpansionListener(tel);
         }

         for (TreeSelectionListener tsl : tree.getTreeSelectionListeners()) {
            tree.removeTreeSelectionListener(tsl);
         }

         for (TreeWillExpandListener twel : tree.getTreeWillExpandListeners()) {
            tree.removeTreeWillExpandListener(twel);
         }
      }

      tree     = new TaxonomyTree(aceFrameConfig, this);
      renderer = new TaxonomyNodeRenderer(aceFrameConfig, this);
      model    = new TaxonomyModel(aceFrameConfig.getViewCoordinate(),
                                   new NidList(aceFrameConfig.getRoots().getSetValues()), renderer, tree);
      tree.putClientProperty("JTree.lineStyle", "None");
      tree.setLargeModel(true);

      // tree.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
      tree.setTransferHandler(new TerminologyTransferHandler(tree));

      // tree.setDragEnabled(true);
      ToolTipManager.sharedInstance().registerComponent(tree);
      tree.setCellRenderer(renderer);
      tree.setRootVisible(false);
      tree.setShowsRootHandles(true);
      model.addTreeWillExpandListener(tree);
      tree.addTreeSelectionListener(new TreeSelectionListener() {
         @Override
         public void valueChanged(TreeSelectionEvent evt) {
            treeSelectionChanged(evt);
         }
      });

      JScrollPane treeView = new JScrollPane(tree);

      treeView.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

      // treeView.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
      tree.setScroller(treeView);

      for (int i = 0; i < tree.getRowCount(); i++) {
         TreePath     path = tree.getPathForRow(i);
         TaxonomyNode node = (TaxonomyNode) path.getLastPathComponent();
      }

      statedInferredButton = new JButton(new AbstractAction("", statedView) {
         @Override
         public void actionPerformed(ActionEvent e) {
            handleRelTypeChange();
         }
      });

      switch (assertionType) {
      case INFERRED :
         statedInferredButton.setIcon(inferredView);
         statedInferredButton.setToolTipText("showing inferred, toggle to show inferred then stated...");

         break;

      case INFERRED_THEN_STATED :
         statedInferredButton.setIcon(inferredThenStatedView);
         statedInferredButton.setToolTipText("showing inferred then stated, toggle to show stated...");

         break;

      case STATED :
         statedInferredButton.setIcon(inferredView);
         statedInferredButton.setToolTipText("showing stated, toggle to show inferred...");

         break;
      }

      statedInferredButton.setSelected(true);
      statedInferredButton.setPreferredSize(new Dimension(20, 16));
      statedInferredButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      statedInferredButton.setOpaque(false);
      statedInferredButton.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
      fsnPreferredButton = new JButton(new AbstractAction("", fsnDisplay) {
         @Override
         public void actionPerformed(ActionEvent e) {
            if (displayingFsn) {
               renderer.setTypeToRender(DescTypeToRender.PREFERRED);
               displayingFsn = false;
               fsnPreferredButton.setIcon(preferredDisplay);
               fsnPreferredButton.setToolTipText("displaying preferred term, toggle to show fsn");
               updateHierarchyView("changedToPreferred");
            } else {
               renderer.setTypeToRender(DescTypeToRender.FSN);
               displayingFsn = true;
               fsnPreferredButton.setIcon(fsnDisplay);
               fsnPreferredButton.setToolTipText("displaying fsn, toggle to show preferred term");
               updateHierarchyView("changedToFsn");
            }

            TaxonomyNode parent = (TaxonomyNode) tree.getModel().getRoot();

            handleDisplayChange();
         }
      });

      JPanel             buttonPanel = new JPanel(new GridBagLayout());
      GridBagConstraints c           = new GridBagConstraints();

      c.anchor  = GridBagConstraints.WEST;
      c.gridx   = 0;
      c.gridy   = 0;
      c.fill    = GridBagConstraints.NONE;
      c.weightx = 0;
      c.weighty = 0;
      buttonPanel.setOpaque(false);
      buttonPanel.add(statedInferredButton, c);
      c.gridx++;
      buttonPanel.add(fsnPreferredButton, c);
      treeView.setColumnHeaderView(buttonPanel);
      c.gridx++;
      c.weightx = 1;

      JLabel view = new JLabel(aceFrameConfig.getViewPositionSetReadOnly().toString());

      buttonPanel.add(view, c);

      return treeView;
   }

   NodeFactory getNodeFactory() {
      return model.nodeFactory;
   }

   public NodeStore getNodeStore() {
      return model.getNodeStore();
   }

   public TaxonomyNodeRenderer getRenderer() {
      return renderer;
   }

   public TaxonomyTree getTree() {
      return tree;
   }

   public ActivityPanel getTreeActivityPanel() {
      return activity;
   }

   public ViewCoordinate getViewCoordinate() {
      return this.aceFrameConfig.getViewCoordinate();
   }

   //~--- set methods ---------------------------------------------------------

   public void setTreeActivityPanel(ActivityPanel activity) {
      this.activity = activity;
   }

   ;

   //~--- inner classes -------------------------------------------------------

   protected class ChangeDisplayWorker extends SwingWorker<List<TaxonomyNode>, NodeChangeRecord> {
      List<Long> nodesToChange = new ArrayList<Long>();

      //~--- constructors -----------------------------------------------------

      protected ChangeDisplayWorker() {}

      //~--- methods ----------------------------------------------------------

      @Override
      protected List<TaxonomyNode> doInBackground() throws Exception {
         List<TaxonomyNode> contentChangedList = new ArrayList<TaxonomyNode>();
         IdentifierSet      changedConcepts    = (IdentifierSet) Terms.get().getEmptyIdSet();
         TerminologyStoreDI ts                 = Ts.get();

         for (Entry<Long, TaxonomyNode> entry : model.getNodeStore().nodeMap.entrySet()) {
            TaxonomyNode oldNode = entry.getValue();

            if (oldNode.getCnid() != Integer.MAX_VALUE) {
               TaxonomyNode newNode =
                  model.nodeFactory.makeNode(model.ts.getConceptVersion(oldNode.getCnid()),
                                             oldNode.getParentNid(),
                                             model.getNodeStore().get(oldNode.parentNodeId));

               if (oldNode.childrenAreSet()) {
                  CountDownLatch latch = model.nodeFactory.makeChildNodes(newNode);

                  latch.await();
               }

               NodeChangeRecord changeRec = new NodeChangeRecord(NodeAction.DISPLAY_CHANGED, oldNode,
                                               newNode);

               publish(changeRec);
               contentChangedList.add(newNode);
            }
         }

         return contentChangedList;
      }

      @Override
      protected void done() {
         try {
            List<TaxonomyNode> contentChangedList = get();

            for (TaxonomyNode node : contentChangedList) {
               model.valueForPathChanged(NodePath.getTreePath(model, node), node);
            }
         } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         }
      }

      @Override
      protected void process(List<NodeChangeRecord> chunks) {
         for (NodeChangeRecord nodeChangeRec : chunks) {
            switch (nodeChangeRec.action) {
            case DISPLAY_CHANGED :
               int[] removedNodeIndices = new int[nodeChangeRec.oldNode.getChildren().size()];

               for (int i = 0; i < removedNodeIndices.length; i++) {
                  removedNodeIndices[i] = i;
               }

               model.treeStructureChanged(NodePath.getTreePath(model, nodeChangeRec.newNode));
               model.nodesWereInserted(nodeChangeRec.newNode, removedNodeIndices);

               break;

            default :
               throw new UnsupportedOperationException("Can't handle: " + nodeChangeRec.action);
            }
         }
      }

      private void processComponentNid(TerminologyStoreDI ts, int changedComponentNid,
                                       IdentifierSet changedConcepts)
              throws IOException {
         int              cnid    = ts.getConceptNidForNid(changedComponentNid);
         Collection<Long> nodeIds = model.getNodeStore().getNodeIdsForConcept(cnid);

         if (!nodeIds.isEmpty()) {
            changedConcepts.setMember(cnid);
            nodesToChange.addAll(nodeIds);
         }
      }

      @Override
      public String toString() {
         return helperName + " change display worker";
      }
   }


   protected class ChangeWorker extends SwingWorker<List<TaxonomyNode>, NodeChangeRecord> {
      List<Long>   nodesToChange = new ArrayList<Long>();
      Set<Integer> changedComponents;
      Set<Integer> changedXrefs;
      long         sequence;

      //~--- constructors -----------------------------------------------------

      public ChangeWorker(long sequence, Set<Integer> changedXrefs, Set<Integer> changedComponents) {
         this.sequence          = sequence;
         this.changedXrefs      = changedXrefs;
         this.changedComponents = changedComponents;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      protected List<TaxonomyNode> doInBackground() throws Exception {
         List<TaxonomyNode> contentChangedList = new ArrayList<TaxonomyNode>();
         IdentifierSet      changedConcepts    = (IdentifierSet) Terms.get().getEmptyIdSet();
         TerminologyStoreDI ts                 = Ts.get();

         for (int changedComponentNid : changedComponents) {
            processComponentNid(ts, changedComponentNid, changedConcepts);
         }

         for (int changedComponentNid : changedXrefs) {
            processComponentNid(ts, changedComponentNid, changedConcepts);
         }

         for (Long nodeId : nodesToChange) {
            TaxonomyNode oldNode = model.getNodeStore().get(nodeId);
            TaxonomyNode newNode = model.nodeFactory.makeNode(model.ts.getConceptVersion(oldNode.getCnid()),
                                      oldNode.getParentNid(), model.getNodeStore().get(oldNode.parentNodeId));
            boolean childrenChanged = false;

            if (oldNode.childrenAreSet()) {
               CountDownLatch latch = model.nodeFactory.makeChildNodes(newNode);

               latch.await();
            }

            boolean contentChanged = !newNode.getText().equals(oldNode.getText());
            boolean parentsChanged = (newNode.hasExtraParents() != oldNode.hasExtraParents())
                                     ||!newNode.getExtraParents().equals(oldNode.getExtraParents());

            if (parentsChanged) {

               //
            }

            if (childrenChanged) {
               NodeChangeRecord changeRec = new NodeChangeRecord(NodeAction.CHILDREN_CHANGED, oldNode,
                                               newNode);

               publish(changeRec);

               //
            }

            if (contentChanged) {
               contentChangedList.add(newNode);
            }
         }

         return contentChangedList;
      }

      @Override
      protected void done() {
         try {
            List<TaxonomyNode> contentChangedList = get();

            for (TaxonomyNode node : contentChangedList) {
               model.valueForPathChanged(NodePath.getTreePath(model, node), node);
            }
         } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         }
      }

      @Override
      protected void process(List<NodeChangeRecord> chunks) {
         for (NodeChangeRecord nodeChangeRec : chunks) {
            switch (nodeChangeRec.action) {
            case ADDED_AS_PARENT :
               break;

            case CHILDREN_CHANGED :
               int[] removedNodeIndices = new int[nodeChangeRec.oldNode.getChildren().size()];

               for (int i = 0; i < removedNodeIndices.length; i++) {
                  removedNodeIndices[i] = i;
               }

               model.treeStructureChanged(NodePath.getTreePath(model, nodeChangeRec.newNode));
               model.nodesWereInserted(nodeChangeRec.newNode, removedNodeIndices);

               break;

            default :
               throw new UnsupportedOperationException("Can't handle: " + nodeChangeRec.action);
            }
         }
      }

      private void processComponentNid(TerminologyStoreDI ts, int changedComponentNid,
                                       IdentifierSet changedConcepts)
              throws IOException {
         int cnid = ts.getConceptNidForNid(changedComponentNid);

         if (cnid > Integer.MIN_VALUE) {
            Collection<Long> nodeIds = model.getNodeStore().getNodeIdsForConcept(cnid);

            if (!nodeIds.isEmpty()) {
               changedConcepts.setMember(cnid);
               nodesToChange.addAll(nodeIds);
            }
         }
      }

      @Override
      public String toString() {
         return helperName + " change worker";
      }
   }


   private static class NodeChangeRecord {
      NodeAction   action;
      TaxonomyNode newNode;
      TaxonomyNode oldNode;

      //~--- constructors -----------------------------------------------------

      public NodeChangeRecord(NodeAction action, TaxonomyNode oldNode, TaxonomyNode newNode) {
         this.action  = action;
         this.oldNode = oldNode;
         this.newNode = newNode;
      }
   }
}
