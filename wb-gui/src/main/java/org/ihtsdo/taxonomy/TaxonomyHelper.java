
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
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.log.AceLog;

import org.ihtsdo.arena.conceptview.ConceptViewRenderer;
import org.ihtsdo.concurrent.future.FutureHelper;
import org.ihtsdo.taxonomy.TaxonomyNodeRenderer.DescTypeToRender;
import org.ihtsdo.taxonomy.model.ChildNodeFilterBI;
import org.ihtsdo.taxonomy.model.NodeFactory;
import org.ihtsdo.taxonomy.model.NodePath;
import org.ihtsdo.taxonomy.model.TaxonomyModel;
import org.ihtsdo.taxonomy.nodes.RootNode;
import org.ihtsdo.taxonomy.nodes.TaxonomyNode;
import org.ihtsdo.taxonomy.path.PathExpander;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidList;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.TermChangeListener;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.TreePath;
import org.ihtsdo.tk.api.ContradictionException;

/**
 *
 * @author kec
 */
public class TaxonomyHelper extends TermChangeListener implements PropertyChangeListener {
   private static ImageIcon statedView = 
      new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/graph_edge.png"));
   private static ImageIcon preferredDisplay =
      new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/car_compact_green.png"));
   private static ImageIcon inferredView =
      new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/chrystal_ball.png"));
   private static ImageIcon inferredThenStatedView =
      new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/inferred-then-stated.png"));
   private static ImageIcon fsnDisplay =
      new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/truck_red.png"));

   //~--- fields --------------------------------------------------------------

   private boolean              displayingFsn = true;
   private TaxonomyNode         selectedNode  = null;
   private I_ConfigAceFrame     aceFrameConfig;
   private ActivityPanel        activity;
   private RelAssertionType     assertionType;
   private ChildNodeFilterBI    childNodeFilter;
   private JButton              fsnPreferredButton;
   private String               helperName;
   private TaxonomyModel        model;
   private TaxonomyNodeRenderer renderer;
   private JButton              statedInferredButton;
   private TaxonomyTree         tree;

   //~--- constant enums ------------------------------------------------------

   static enum NodeAction { CHILDREN_CHANGED, ADDED_AS_PARENT, DISPLAY_CHANGED }

   //~--- constructors --------------------------------------------------------

   public TaxonomyHelper(I_ConfigAceFrame config, String helperName, ChildNodeFilterBI childNodeFilter) {
      super(); 
      this.aceFrameConfig = config;
      this.assertionType  = config.getRelAssertionType();
      Ts.get().addTermChangeListener(this);
      this.helperName      = helperName;
      this.childNodeFilter = childNodeFilter;
      this.aceFrameConfig.addPropertyChangeListener(this);
   }

   //~--- methods -------------------------------------------------------------

   public synchronized void addMouseListener(MouseListener ml) {
      if (tree != null) {
         tree.addMouseListener(ml);
      }
   }

   public void addTreeSelectionListener(TreeSelectionListener tsl) {
      tree.addTreeSelectionListener(tsl);
   }

   @Override
   public void changeNotify(long sequence, Set<Integer> originsOfChangedRels, Set<Integer> destinationsOfChangedRels, Set<Integer> referencedComponentsOfChangedRefexs, Set<Integer> changedComponents, Set<Integer> changedComponentAlerts, Set<Integer> changedComponentTemplates, boolean fromClassification) {
        try {
            if (!renderer.getAssertionType().equals(RelAssertionType.INFERRED)) {
                if (renderer != null && model != null) {
                    NodeUpdator changeWorker = new NodeUpdator(model,
                            sequence,
                            originsOfChangedRels,
                            destinationsOfChangedRels,
                            referencedComponentsOfChangedRefexs,
                            changedComponents,
                            renderer,
                            helperName);

                    FutureHelper.addFuture(ACE.threadPool.submit(changeWorker));
                }
            } else if (renderer.getAssertionType().equals(RelAssertionType.INFERRED) && fromClassification) {
                if (renderer != null && model != null) {
                    NodeUpdator changeWorker = new NodeUpdator(model,
                            sequence,
                            originsOfChangedRels,
                            destinationsOfChangedRels,
                            referencedComponentsOfChangedRefexs,
                            changedComponents,
                            renderer,
                            helperName);

                    FutureHelper.addFuture(ACE.threadPool.submit(changeWorker));
                }
            }
        } catch (ContradictionException | IOException ex) {
           AceLog.getAppLog().alertAndLogException(ex);
        }
      
   }

   protected void collapseTree(TreeExpansionEvent evt, I_ConfigAceFrame aceFrameConfig) {
      TaxonomyNode node = handleCollapse(evt, aceFrameConfig);
   }

   private void expandToLastSelection() {
      if (selectedNode != null) {
         SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
               try {
                  PathExpander expander = new PathExpander(tree, aceFrameConfig,
                                             Ts.get().getConcept(selectedNode.getConceptNid()));

                  NodeFactory.pathExpanderExecutors.execute(expander);
               } catch (IOException ex) {
                  AceLog.getAppLog().alertAndLogException(ex);
               }
            }
         });
      }
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
      ViewCoordinate vc = model.getTs().getViewCoordinate();

      switch (assertionType) {
      case INFERRED :
         assertionType = RelAssertionType.INFERRED_THEN_STATED;
         statedInferredButton.setIcon(inferredThenStatedView);
         statedInferredButton.setToolTipText("showing inferred then stated, toggle to show stated...");
         vc.setRelationshipAssertionType(assertionType);
         model.setTs(Ts.get().getSnapshot(vc));
         updateNewModel("changed from stated to inferred then stated");

         break;

      case INFERRED_THEN_STATED :
         assertionType = RelAssertionType.STATED;
         statedInferredButton.setIcon(statedView);
         statedInferredButton.setToolTipText("showing stated, toggle to show inferred...");
         vc.setRelationshipAssertionType(assertionType);
         model.setTs(Ts.get().getSnapshot(vc));
         updateNewModel("changed from inferred to stated");

         break;

      case STATED :
         assertionType = RelAssertionType.INFERRED;
         statedInferredButton.setIcon(inferredView);
         statedInferredButton.setToolTipText("showing inferred, toggle to show inferred then stated...");
         vc.setRelationshipAssertionType(assertionType);
         model.setTs(Ts.get().getSnapshot(vc));
         updateNewModel("changed from stated to inferred");

         break;
      }
   }
   Object lastPropigationId = Long.MIN_VALUE;
   @Override
   public void propertyChange(PropertyChangeEvent evt) {
       if (evt.getPropagationId() != null && 
               evt.getPropagationId().equals(lastPropigationId)) {
           return;
       }
       lastPropigationId = evt.getPropagationId();
      if ("roots".equals(evt.getPropertyName())
//                 || "termComponent".equals(evt.getPropertyName())
                 || "viewPositions".equals(evt.getPropertyName())
                 || "showPathInfoInTaxonomy".equals(evt.getPropertyName())
                 || "showRefsetInfoInTaxonomy".equals(evt.getPropertyName())
                 || "showViewerImagesInTaxonomy".equals(evt.getPropertyName())
                 || "languagePref".equals(evt.getPropertyName())
                 || "updateHierarchyView".equals(evt.getPropertyName())) {
         updateNewModel(evt.getPropertyName());
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
            selectedNode = node;
            aceFrameConfig.setHierarchySelection((I_GetConceptData) Ts.get().getConcept(node.getConceptNid()));
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
         TaxonomyNode childNode = (TaxonomyNode) model.getChild(root, i);

         model.getNodeFactory().removeDescendents(childNode);

         TreePath tp = new TreePath(NodePath.getTreePath(model, childNode));

         tree.collapseRow(i);
         tree.collapsePath(tp);
      }

      expandToLastSelection();
   }

   public void updateNewModel(String propChangeName) {
      try {
         model.unLink();
         model.removeTreeWillExpandListener(tree);
         ViewCoordinate newVc = new ViewCoordinate(aceFrameConfig.getViewCoordinate());
         newVc.setRelationshipAssertionType(assertionType);
         model = new TaxonomyModel(newVc,
                                   new NidList(aceFrameConfig.getRoots().getSetValues()), renderer, tree,
                                   childNodeFilter);
         model.addTreeWillExpandListener(tree);
         expandToLastSelection();
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
                                   new NidList(aceFrameConfig.getRoots().getSetValues()), renderer, tree,
                                   childNodeFilter);
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
               updateNewModel("changedToPreferred");
            } else {
               renderer.setTypeToRender(DescTypeToRender.FSN);
               displayingFsn = true;
               fsnPreferredButton.setIcon(fsnDisplay);
               fsnPreferredButton.setToolTipText("displaying fsn, toggle to show preferred term");
               updateNewModel("changedToFsn");
            }

            TaxonomyNode parent = (TaxonomyNode) tree.getModel().getRoot();

            handleDisplayChange();
         }
      });
      fsnPreferredButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));

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
      c.gridx++;
      c.weightx = 1;

      JLabel view = new JLabel(aceFrameConfig.getViewPositionSetReadOnly().toString().trim());

      view.setHorizontalAlignment(SwingConstants.LEFT);
      buttonPanel.add(view, c);
      buttonPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0),
              BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                 Color.LIGHT_GRAY), BorderFactory.createEmptyBorder(0, 0, 2, 0))));
      treeView.setColumnHeaderView(buttonPanel);

      JPanel corner = new JPanel();

      corner.setBackground(Color.WHITE);
      corner.setOpaque(true);
      corner.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0),
              BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                 Color.LIGHT_GRAY), BorderFactory.createEmptyBorder(0, 0, 2, 0))));
      treeView.setCorner(JScrollPane.UPPER_RIGHT_CORNER, corner);

      return treeView;
   }

   NodeFactory getNodeFactory() {
      return model.getNodeFactory();
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
       ViewCoordinate vc = new ViewCoordinate(this.aceFrameConfig.getViewCoordinate());
       vc.setRelationshipAssertionType(assertionType);
       return vc;
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

         for (Entry<Long, TaxonomyNode> entry : model.getNodeStore().nodeMap.entrySet()) {
            TaxonomyNode oldNode = entry.getValue();

            if (oldNode.getConceptNid() != Integer.MAX_VALUE) {
               TaxonomyNode newNode =
                  model.getNodeFactory().makeNode(model.getTs().getConceptVersion(oldNode.getConceptNid()),
                                                  oldNode.getParentNid(),
                                                  model.getNodeStore().get(oldNode.parentNodeId));

               if (oldNode.childrenAreSet()) {
                  CountDownLatch latch = model.getNodeFactory().makeChildNodes(newNode);

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
