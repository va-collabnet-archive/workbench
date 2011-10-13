
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.taxonomy;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.taxonomy.nodes.RootNode;
import org.ihtsdo.taxonomy.nodes.TaxonomyNode;

//~--- JDK imports ------------------------------------------------------------

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.tree.TreePath;

/**
 *
 * @author kec
 */
public class NodePath {
   public static TreePath getTreePath(TaxonomyModel model, TaxonomyNode node) {
      if (node instanceof RootNode) {
         return new TreePath(node);
      }

      LinkedList<Long> idList = new LinkedList<Long>();

      idList.addLast(node.nodeId);

      TaxonomyNode parentNode = model.nodeStore.get(node.parentNodeId);

      while (parentNode != null) {
         idList.addLast(parentNode.nodeId);

         if ((model.nodeStore.get(parentNode.parentNodeId) != null)
                 && (parentNode.nodeId != model.nodeStore.get(parentNode.parentNodeId).nodeId)) {
            parentNode = model.nodeStore.get(parentNode.parentNodeId);
         } else {
            parentNode = null;
         }
      }

      TaxonomyNode[] nodes   = new TaxonomyNode[idList.size()];
      int            index   = 0;
      Iterator<Long> descItr = idList.descendingIterator();

      while (descItr.hasNext()) {
         nodes[index++] = model.nodeStore.get(descItr.next());
      }

      return new TreePath(nodes);
   }
}
