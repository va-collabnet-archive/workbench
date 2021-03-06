/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.dwfa.ace.tree;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.config.FrameConfigSnapshot;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.arena.conceptview.ConceptViewRenderer;
import org.ihtsdo.thread.NamedThreadFactory;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.TermChangeListener;

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
import java.util.logging.Level;

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

public class TermTreeHelper extends TermChangeListener implements PropertyChangeListener {
   private static ThreadGroup treeExpansionGroup = new ThreadGroup("Tree expansion ");
   private static ImageIcon   statedView         =
      new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/graph_edge.png"));
   private static ImageIcon inferredView =
      new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/chrystal_ball.png"));
   private static ImageIcon inferredThenStatedView =
      new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/inferred-then-stated.png"));

   //~--- fields --------------------------------------------------------------

   private final Map<TreeIdPath, ExpandNodeSwingWorker> expansionWorkers = new ConcurrentHashMap<TreeIdPath,
                                                                              ExpandNodeSwingWorker>();
   private ExecutorService treeExpandThread = Executors.newFixedThreadPool(1,
                                                 new NamedThreadFactory(treeExpansionGroup,
                                                    "tree expansion "));
   private I_ConfigAceFrame     aceFrameConfig;
   private ActivityPanel        activity;
   private RelAssertionType     assertionType;
   private TermTreeCellRenderer renderer;
   private JButton              statedInferredButton;
   private JTreeWithDragImage   tree;

   //~--- constructors --------------------------------------------------------

   public TermTreeHelper(I_ConfigAceFrame config) {
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
   public void changeNotify(long sequence, Set<Integer> originsOfChangedRels, Set<Integer> destinationsOfChangedRels, Set<Integer> referencedComponentsOfChangedRefexs, Set<Integer> changedComponents, Set<Integer> changedComponentAlerts, Set<Integer> changedComponentTemplates, boolean fromClassification) {
      if (AceLog.getAppLog().isLoggable(Level.FINE)) {
         AceLog.getAppLog().info("Term change. Sequence: " + sequence + 
                                 " changed rel origins: " + originsOfChangedRels + 
                                 " changed rel targets: " + destinationsOfChangedRels + 
                                 " changed refexesReferencing: " + referencedComponentsOfChangedRefexs + 
                                 " changedComponents: " + changedComponents);
      }
   }

   private I_GetConceptDataForTree handleCollapse(TreeExpansionEvent evt, I_ConfigAceFrame aceFrameConfig) {
      TreeIdPath idPath = new TreeIdPath(evt.getPath());

      stopWorkersOnPath(idPath, "stopping for collapse");

      DefaultMutableTreeNode node = (DefaultMutableTreeNode) evt.getPath().getLastPathComponent();

      node.removeAllChildren();

      I_GetConceptDataForTree userObject = (I_GetConceptDataForTree) node.getUserObject();
      DefaultTreeModel        model      = (DefaultTreeModel) tree.getModel();

      /*
       * To avoid having JTree re-expand the root node, we disable
       * ask-allows-children when we notify JTree about the new node
       * structure.
       */
      model.setAsksAllowsChildren(false);
      model.nodeStructureChanged(node);
      model.setAsksAllowsChildren(true);

      return userObject;
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
      ExpandNodeSwingWorker foundWorker = expansionWorkers.get(key);

      if (foundWorker != null) {
         foundWorker.stopWork(message);
         expansionWorkers.remove(key);
      }
   }

   protected void removeExpansionWorker(TreeIdPath key, ExpandNodeSwingWorker worker, String message) {
      ExpandNodeSwingWorker foundWorker = expansionWorkers.get(key);

      if ((worker != null) && (foundWorker == worker)) {
         worker.stopWork(message);
         expansionWorkers.remove(key);
      }
   }

   protected void removeStaleExpansionWorker(TreeIdPath key) {
      ExpandNodeSwingWorker foundWorker = expansionWorkers.get(key);

      if (foundWorker.getContinueWork() == false) {
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
      I_GetConceptDataForTree userObject = handleCollapse(evt, aceFrameConfig);

      aceFrameConfig.getChildrenExpandedNodes().remove(userObject.getConceptNid());
   }

   protected void treeTreeExpanded(TreeExpansionEvent evt) {
      DefaultMutableTreeNode node   = (DefaultMutableTreeNode) evt.getPath().getLastPathComponent();
      TreeIdPath             idPath = new TreeIdPath(evt.getPath());

      stopWorkersOnPath(idPath, "stopping before expansion");

      I_GetConceptDataForTree userObject = (I_GetConceptDataForTree) node.getUserObject();

      if (userObject != null) {
         aceFrameConfig.getChildrenExpandedNodes().add(userObject.getConceptNid());

         FrameConfigSnapshot   configSnap = new FrameConfigSnapshot(aceFrameConfig);
         ExpandNodeSwingWorker worker     = new ExpandNodeSwingWorker((DefaultTreeModel) tree.getModel(),
                                               tree, node, new CompareConceptBeansForTree(configSnap), this,
                                               configSnap, assertionType);

         treeExpandThread.execute(worker);
         expansionWorkers.put(idPath, worker);
      }
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

   public Map<TreeIdPath, ExpandNodeSwingWorker> getExpansionWorkers() {
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

      tree = new JTreeWithDragImage(aceFrameConfig, this);
      tree.putClientProperty("JTree.lineStyle", "None");
      tree.setLargeModel(true);

      // tree.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
      tree.setTransferHandler(new TerminologyTransferHandler(tree));

      // tree.setDragEnabled(true);
      ToolTipManager.sharedInstance().registerComponent(tree);
      renderer = new TermTreeCellRenderer(aceFrameConfig, this);
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

   public TermTreeCellRenderer getRenderer() {
      return renderer;
   }

   public JTreeWithDragImage getTree() {
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
