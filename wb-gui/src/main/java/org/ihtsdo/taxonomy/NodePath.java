
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.taxonomy;

//~--- JDK imports ------------------------------------------------------------

import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.tree.TreePath;
import org.ihtsdo.taxonomy.nodes.RootNode;
import org.ihtsdo.taxonomy.nodes.TaxonomyNode;

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

      TaxonomyNode parentNode = model.nodeMap.get(node.parentNodeId);

      while (parentNode != null) {
         idList.addLast(parentNode.nodeId);
         if (model.nodeMap.get(parentNode.parentNodeId) != null && parentNode.nodeId != model.nodeMap.get(parentNode.parentNodeId).nodeId) {
            parentNode = model.nodeMap.get(parentNode.parentNodeId);
         } else {
             parentNode = null;
         }
      }

      TaxonomyNode[]         nodes     = new TaxonomyNode[idList.size()];
      int            index   = 0;
      Iterator<Long> descItr = idList.descendingIterator();

      while (descItr.hasNext()) {
         nodes[index++] = model.nodeMap.get(descItr.next());
      }
      return new TreePath(nodes);
   }
}
