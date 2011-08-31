
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.tree;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.config.FrameConfigSnapshot;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.tree.ConceptBeanForTree;
import org.dwfa.ace.tree.TreeIdPath;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.arena.conceptview.ConceptViewRenderer;
import org.ihtsdo.cern.colt.map.OpenIntIntHashMap;
import org.ihtsdo.helper.thread.NamedThreadFactory;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.TermChangeListener;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;

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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author kec
 */
public class TaxonomyHelper extends TermChangeListener implements PropertyChangeListener {
   private static ThreadGroup treeExpansionGroup = new ThreadGroup("Tree expansion ");
   private static ImageIcon   statedView         =
      new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/graph_edge.png"));
   private static ImageIcon inferredView =
      new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/chrystal_ball.png"));
   private static ImageIcon inferredThenStatedView =
      new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/inferred-then-stated.png"));

   //~--- fields --------------------------------------------------------------

   private final Map<TreeIdPath, ExpandTaxonomyNodeWorker> expansionWorkers =
      new ConcurrentHashMap<TreeIdPath, ExpandTaxonomyNodeWorker>();
   private ExecutorService treeExpandThread = Executors.newFixedThreadPool(1,
                                                 new NamedThreadFactory(treeExpansionGroup,
                                                    "tree expansion "));
   private OpenIntIntHashMap    expandedNodes = new OpenIntIntHashMap();
   private I_ConfigAceFrame     aceFrameConfig;
   private ActivityPanel        activity;
   private RelAssertionType     assertionType;
   private TaxonomyNodeRenderer renderer;
   private JButton              statedInferredButton;
   private TaxonomyTree         tree;

   //~--- constructors --------------------------------------------------------

   public TaxonomyHelper(I_ConfigAceFrame config) {
      super();
      this.aceFrameConfig = config;
      this.assertionType  = config.getRelAssertionType();
      Ts.get().addTermChangeListener(this);
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
      AceLog.getAppLog().info("Term change. Sequence: " + sequence + " changedXrefs: " + changedXrefs
                              + " changedComponents: " + changedComponents);
   }

   private TaxonomyNode handleCollapse(TreeExpansionEvent evt, I_ConfigAceFrame aceFrameConfig) {
      TreeIdPath idPath = new TreeIdPath(evt.getPath());

      stopWorkersOnPath(idPath, "stopping for collapse");

      TaxonomyNode node = (TaxonomyNode) evt.getPath().getLastPathComponent();

      node.removeAllChildren();

      DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

      /*
       * To avoid having JTree re-expand the root node, we disable
       * ask-allows-children when we notify JTree about the new node
       * structure.
       */
      model.setAsksAllowsChildren(false);
      model.nodeStructureChanged(node);
      model.setAsksAllowsChildren(true);

      return node;
   }

   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      try {
         setRoots();
      } catch (Throwable e) {
         AceLog.getAppLog().alertAndLogException(e);
      }

      updateHierarchyView(evt.getPropertyName());
   }

   private void removeAnyMatchingExpansionWorker(TreeIdPath key, String message) {
      ExpandTaxonomyNodeWorker foundWorker = expansionWorkers.get(key);

      if (foundWorker != null) {
         foundWorker.cancel(false);
         expansionWorkers.remove(key);
      }
   }

   protected void removeExpansionWorker(TreeIdPath key, ExpandTaxonomyNodeWorker worker, String message) {
      ExpandTaxonomyNodeWorker foundWorker = expansionWorkers.get(key);

      if ((worker != null) && (foundWorker == worker)) {
         worker.cancel(false);
         expansionWorkers.remove(key);
      }
   }

   protected void removeStaleExpansionWorker(TreeIdPath key) {
      ExpandTaxonomyNodeWorker foundWorker = expansionWorkers.get(key);

      if (foundWorker.isCancelled()) {
         expansionWorkers.remove(key);
      }
   }

   public void removeTreeSelectionListener(TreeSelectionListener tsl) {
      tree.removeTreeSelectionListener(tsl);
   }

   private void stopWorkersOnPath(TreeIdPath idPath, String message) {
      if (idPath == null) {
         List<TreeIdPath> allKeys = new ArrayList<TreeIdPath>(expansionWorkers.keySet());

         for (TreeIdPath key : allKeys) {
            removeAnyMatchingExpansionWorker(key, message);
         }
      } else {
         if (expansionWorkers.containsKey(idPath)) {
            removeAnyMatchingExpansionWorker(idPath, message);
         }

         List<TreeIdPath> otherKeys = new ArrayList<TreeIdPath>(expansionWorkers.keySet());

         for (TreeIdPath key : otherKeys) {
            if (key.initiallyEqual(idPath)) {
               removeAnyMatchingExpansionWorker(key, message);
            }
         }
      }
   }

   protected void treeTreeCollapsed(TreeExpansionEvent evt, I_ConfigAceFrame aceFrameConfig) {
      TaxonomyNode node = handleCollapse(evt, aceFrameConfig);

      expandedNodes.removeKey(node.getCnid());
   }

   protected void treeTreeExpanded(TreeExpansionEvent evt) {
      TaxonomyNode node   = (TaxonomyNode) evt.getPath().getLastPathComponent();
      TreeIdPath   idPath = new TreeIdPath(evt.getPath());

      stopWorkersOnPath(idPath, "stopping before expansion");
      expandedNodes.removeKey(node.getCnid());

      FrameConfigSnapshot      configSnap   = new FrameConfigSnapshot(aceFrameConfig);
      ConceptVersionBI         cv           = null;
      ExpandTaxonomyNodeWorker parentWorker = null;
      ExpandTaxonomyNodeWorker worker       = new ExpandTaxonomyNodeWorker(cv,
                                                 (DefaultTreeModel) tree.getModel(), node, parentWorker,
                                                 renderer);

      treeExpandThread.execute(worker);
      expansionWorkers.put(idPath, worker);
   }

   protected void treeValueChanged(TreeSelectionEvent evt) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) evt.getPath().getLastPathComponent();

      if (node != null) {
         ConceptBeanForTree treeBean = (ConceptBeanForTree) node.getUserObject();

         if (treeBean != null) {
            aceFrameConfig.setHierarchySelection(treeBean.getCoreBean());
         }
      } else {
         aceFrameConfig.setHierarchySelection(null);
      }
   }

   public void updateHierarchyView(String propChangeName) {
      DefaultTreeModel       model = (DefaultTreeModel) tree.getModel();
      DefaultMutableTreeNode root  = (DefaultMutableTreeNode) model.getRoot();

      stopWorkersOnPath(null, "stopping for change in " + propChangeName);

      for (int i = 0; i < root.getChildCount(); i++) {
         DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) root.getChildAt(i);
         I_GetConceptData       cb        = (I_GetConceptData) childNode.getUserObject();

         if (aceFrameConfig.getChildrenExpandedNodes().contains(cb.getConceptNid())) {
            TreePath           tp        = new TreePath(childNode);
            TreeExpansionEvent treeEvent = new TreeExpansionEvent(model, tp);

            handleCollapse(treeEvent, aceFrameConfig);
            treeTreeExpanded(treeEvent);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   public RelAssertionType getAssertionType() {
      return assertionType;
   }

   public Map<TreeIdPath, ExpandTaxonomyNodeWorker> getExpansionWorkers() {
      return expansionWorkers;
   }

   public JScrollPane getHierarchyPanel() throws TerminologyException, IOException {
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

      tree = new TaxonomyTree(aceFrameConfig, this);
      tree.putClientProperty("JTree.lineStyle", "None");
      tree.setLargeModel(true);

      // tree.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
      tree.setTransferHandler(new TerminologyTransferHandler(tree));

      // tree.setDragEnabled(true);
      ToolTipManager.sharedInstance().registerComponent(tree);
      renderer = new TaxonomyNodeRenderer(aceFrameConfig, this);
      tree.setCellRenderer(renderer);
      tree.setRootVisible(false);
      tree.setShowsRootHandles(true);

      DefaultTreeModel model = setRoots();

      /*
       * Since nodes are added dynamically in this application, the only true
       * leaf nodes are nodes that don't allow children to be added. (By
       * default, askAllowsChildren is false and all nodes without children
       * are considered to be leaves.)
       *
       * But there's a complication: when the tree structure changes, JTree
       * pre-expands the root node unless it's a leaf. To avoid having the
       * root pre-expanded, we set askAllowsChildrenafter assigning the new
       * root.
       */
      model.setAsksAllowsChildren(true);
      tree.addTreeExpansionListener(new TreeExpansionListener() {
         @Override
         public void treeExpanded(TreeExpansionEvent evt) {
            treeTreeExpanded(evt);
         }
         @Override
         public void treeCollapsed(TreeExpansionEvent evt) {
            treeTreeCollapsed(evt, aceFrameConfig);
         }
      });
      tree.addTreeSelectionListener(new TreeSelectionListener() {
         @Override
         public void valueChanged(TreeSelectionEvent evt) {
            treeValueChanged(evt);
         }
      });

      JScrollPane treeView = new JScrollPane(tree);

      tree.setScroller(treeView);

      for (int i = 0; i < tree.getRowCount(); i++) {
         TreePath               path     = tree.getPathForRow(i);
         DefaultMutableTreeNode node     = (DefaultMutableTreeNode) path.getLastPathComponent();
         ConceptBeanForTree     treeBean = (ConceptBeanForTree) node.getUserObject();

         if (aceFrameConfig.getChildrenExpandedNodes().contains(treeBean.getConceptNid())) {
            tree.expandPath(new TreePath(node.getPath()));
         }
      }

      statedInferredButton = new JButton(new AbstractAction("", statedView) {
         @Override
         public void actionPerformed(ActionEvent e) {
            switch (assertionType) {
            case INFERRED :
               assertionType = RelAssertionType.INFERRED_THEN_STATED;
               statedInferredButton.setIcon(inferredThenStatedView);
               statedInferredButton.setToolTipText("showing inferred then stated, toggle to show stated...");
               updateHierarchyView("changed from stated to inferred then stated");

               break;

            case INFERRED_THEN_STATED :
               assertionType = RelAssertionType.STATED;
               statedInferredButton.setIcon(statedView);
               statedInferredButton.setToolTipText("showing stated, toggle to show inferred...");
               updateHierarchyView("changed from inferred to stated");

               break;

            case STATED :
               assertionType = RelAssertionType.INFERRED;
               statedInferredButton.setIcon(inferredView);
               statedInferredButton.setToolTipText(
                   "showing inferred, toggle to show inferred then stated...");
               updateHierarchyView("changed from stated to inferred");

               break;
            }
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
      treeView.setColumnHeaderView(buttonPanel);
      c.gridx++;
      c.weightx = 1;

      JLabel view = new JLabel(aceFrameConfig.getViewPositionSetReadOnly().toString());

      buttonPanel.add(view, c);

      return treeView;
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

   public ExecutorService getTreeExpandThread() {
      return treeExpandThread;
   }

   //~--- set methods ---------------------------------------------------------

   public DefaultTreeModel setRoots() throws TerminologyException, IOException {
      DefaultTreeModel       model = (DefaultTreeModel) tree.getModel();
      DefaultMutableTreeNode root  = new DefaultMutableTreeNode(null, true);

      for (int rootId : aceFrameConfig.getRoots().getSetValues()) {
         root.add(new DefaultMutableTreeNode(ConceptBeanForTree.get(rootId, Integer.MIN_VALUE, 0, false,
                 aceFrameConfig), true));
      }

      model.setRoot(root);

      return model;
   }

   public void setTreeActivityPanel(ActivityPanel activity) {
      this.activity = activity;
   }
}
