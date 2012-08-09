
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.taxonomy.model;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.log.AceLog;

import org.ihtsdo.taxonomy.NodeStore;
import org.ihtsdo.taxonomy.TaxonomyNodeRenderer;
import org.ihtsdo.taxonomy.model.NodeFactory;
import org.ihtsdo.taxonomy.nodes.LeafNode;
import org.ihtsdo.taxonomy.nodes.RootNode;
import org.ihtsdo.taxonomy.nodes.TaxonomyNode;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidListBI;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author kec
 */
public class TaxonomyModel implements TreeModel {
   private int                                      nextChildIndex = 0;
   protected CopyOnWriteArraySet<TreeModelListener> listeners      =
      new CopyOnWriteArraySet<>();
   protected NodeStore                              nodeStore      = new NodeStore();
   private Iterator<Long>                           childItr;
   private TaxonomyNode                             lastParentNode;
   protected NodeFactory                            nodeFactory;
   private RootNode                                 rootNode;
   protected TerminologySnapshotDI                  ts;

   //~--- constructors --------------------------------------------------------

   public TaxonomyModel(ViewCoordinate vc, NidListBI roots, TaxonomyNodeRenderer renderer, JTree tree,
                        ChildNodeFilterBI childNodeFilter)
           throws IOException, Exception {
      ts          = Ts.get().getSnapshot(vc);
      nodeFactory = new NodeFactory(this, renderer, tree, childNodeFilter);
      rootNode    = new RootNode(nodeFactory.getNodeComparator());
      nodeStore.add(rootNode);

      for (int cnid : roots.getListArray()) {
         nodeFactory.makeNode(cnid, rootNode);
      }

      rootNode.setText("root");
      tree.setModel(this);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void addTreeModelListener(TreeModelListener l) {
      listeners.add(l);
   }

   public void addTreeWillExpandListener(JTree tree) {
      nodeFactory.addNodeExpansionListener(tree);
   }

   public void removeTreeWillExpandListener(JTree tree) {
      nodeFactory.removeNodeExpansionListener(tree);
   }

   protected void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children) {
      TreeModelEvent e = new TreeModelEvent(source, path, childIndices, children);

      for (TreeModelListener l : listeners) {
         l.treeNodesInserted(e);
      }
   }

   public void nodesWereInserted(TaxonomyNode parentNode, int[] newNodeIndices) {
      int      cCount      = newNodeIndices.length;
      Object[] newChildren = new Object[cCount];

      for (int counter = 0; counter < cCount; counter++) {
         newChildren[counter] = getChild(parentNode, newNodeIndices[counter]);
      }

      fireTreeNodesInserted(this, getPathToRoot(parentNode), newNodeIndices, newChildren);
   }

   void nodesWereRemoved(TaxonomyNode parentNode, int[] removedNodeIndices) {
      int      cCount          = removedNodeIndices.length;
      Object[] removedChildren = new Object[cCount];

      for (int counter = 0; counter < cCount; counter++) {
         removedChildren[counter] = getChild(parentNode, removedNodeIndices[counter]);
      }

      fireTreeNodesInserted(this, getPathToRoot(parentNode), removedNodeIndices, removedChildren);
   }

   @Override
   public void removeTreeModelListener(TreeModelListener l) {
      listeners.remove(l);
   }

   public void treeStructureChanged(TreePath path) {
      TreeModelEvent e = new TreeModelEvent(this, path);

      for (TreeModelListener l : listeners) {
         l.treeStructureChanged(e);
      }
   }

   public void unLink() {
      nodeFactory.unLink();
      listeners.clear();
   }

   @Override
   public void valueForPathChanged(TreePath path, Object newValue) {
      TreeModelEvent evt = new TreeModelEvent(newValue, path);

      for (TreeModelListener l : listeners) {
         l.treeNodesChanged(evt);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public TaxonomyNode getChild(Object parent, int index) {
      if (index < 0) {
         System.out.println("Bad Index: " + index);
         index = 0;
      }

      TaxonomyNode parentNode = (TaxonomyNode) parent;

      if (lastParentNode == parentNode) {
         if (nextChildIndex == index) {
            if (childItr != null) {
               return getNextNode();
            }
         }
      }

      lastParentNode = parentNode;
      nextChildIndex = index;
      childItr       = parentNode.getChildren().iterator();

      for (int i = 0; i < index; i++) {
         if (childItr.hasNext()) {
            childItr.next();
         } else {
            return null;
         }
      }

      return getNextNode();
   }

   @Override
   public int getChildCount(Object parent) {
      if (((TaxonomyNode) parent).getChildren() == null) {
         return 0;
      }

      return ((TaxonomyNode) parent).getChildren().size();
   }

   public static int getCnid(long nodeId) {
      return (int) (nodeId >>> 32);
   }

   @Override
   public int getIndexOfChild(Object parent, Object child) {
      Iterator<Long> itr = ((TaxonomyNode) parent).getChildren().iterator();

      for (int i = 0; itr.hasNext(); i++) {
         Long ni = itr.next();

         if ((ni == ((TaxonomyNode) child).nodeId)) {
            return i;
         }
      }

      return -1;
   }

   private TaxonomyNode getNextNode() {
      nextChildIndex++;

      if (childItr.hasNext()) {
         Long         nsi   = childItr.next();
         TaxonomyNode child = nodeStore.get(nsi);

         if (child != null) {
            return child;
         }

         try {
            return nodeFactory.makeNode(getCnid(nsi), lastParentNode);
         } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         }
      }

      return null;
   }

   public NodeFactory getNodeFactory() {
      return nodeFactory;
   }

   public static long getNodeId(int cnid, int parentNid) {
      long nodeId = cnid;

      nodeId = nodeId & 0x00000000FFFFFFFFL;

      long nid1Long = parentNid;

      nid1Long = nid1Long & 0x00000000FFFFFFFFL;
      nodeId   = nodeId << 32;
      nodeId   = nodeId | nid1Long;

      return nodeId;
   }

   public NodeStore getNodeStore() {
      return nodeStore;
   }

   public TaxonomyNode getParent(TaxonomyNode node) {
      return nodeStore.get(node.parentNodeId);
   }

   public static int getParentNid(long nodeId) {
      return (int) nodeId;
   }

   public TaxonomyNode[] getPathToRoot(TaxonomyNode aNode) {
      return getPathToRoot(aNode, 0);
   }

   protected TaxonomyNode[] getPathToRoot(TaxonomyNode aNode, int depth) {
      TaxonomyNode[] retNodes;

      if (aNode == null) {
         if (depth == 0) {
            return null;
         } else {
            retNodes = new TaxonomyNode[depth];
         }
      } else {
         depth++;

         if (aNode == rootNode) {
            retNodes = new TaxonomyNode[depth];
         } else {
            retNodes = getPathToRoot(nodeStore.get(aNode.parentNodeId), depth);
         }

         retNodes[retNodes.length - depth] = aNode;
      }

      return retNodes;
   }

   @Override
   public RootNode getRoot() {
      return rootNode;
   }

   public TerminologySnapshotDI getTs() {
      return ts;
   }

   @Override
   public boolean isLeaf(Object node) {
      if (node instanceof LeafNode) {
         return true;
      }

      TaxonomyNode taxonomyNode = (TaxonomyNode) node;

      if ((taxonomyNode == null) || taxonomyNode.isLeaf()) {
         return true;
      }

      return false;
   }

   //~--- set methods ---------------------------------------------------------

   public void setTs(TerminologySnapshotDI snapshot) {
      this.ts = snapshot;
   }
}
