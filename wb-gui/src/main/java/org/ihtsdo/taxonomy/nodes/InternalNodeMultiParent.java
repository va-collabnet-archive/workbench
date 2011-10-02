
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.taxonomy.nodes;

//~--- JDK imports ------------------------------------------------------------

import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * @author kec
 */
public class InternalNodeMultiParent extends InternalNode {
   public ConcurrentSkipListSet<Long> extraParents          = new ConcurrentSkipListSet<Long>();
   boolean                            hasExtraParents       = false;
   boolean                            secondaryParentOpened = false;
   boolean                            isLeaf                = false;

   //~--- constructors --------------------------------------------------------

   public InternalNodeMultiParent(int cnid, int parentNid, long parentNodeId, Comparator nodeComparator) {
      super(cnid, parentNid, parentNodeId, nodeComparator);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void addExtraParent(TaxonomyNode extraParent) {
      this.extraParents.add(extraParent.nodeId);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public ConcurrentSkipListSet<Long> getExtraParents() {
      return extraParents;
   }

   @Override
   public TaxonomyNode getFinalNode() {
      if (!this.hasExtraParents) {
         if (this.isLeaf) {
            LeafNode node = new LeafNode(getCnid(), getParentNid(), parentNodeId);

            node.text = this.text;
            node.setSortComparable(this.getSortComparable());

            return node;
         } else {
            if (isSecondaryParentNode()) {
               LeafNode node = new LeafNode(getCnid(), getParentNid(), parentNodeId);

               node.text = this.text;
               node.setSortComparable(this.getSortComparable());

               return node;
            } else {
               InternalNode node = new InternalNode(getCnid(), getParentNid(), parentNodeId,
                                      getChildren().comparator());

               node.text = this.text;
               node.setSortComparable(this.getSortComparable());
               node.getChildren().addAll(this.getChildren());
               node.nidNodeMap.putAll(this.nidNodeMap);

               return node;
            }
         }
      }

      if (this.isLeaf) {
         LeafNodeMultiParent node = new LeafNodeMultiParent(getCnid(), getParentNid(), parentNodeId);

         node.text = this.text;
         node.setSortComparable(this.getSortComparable());
         node.extraParents = this.extraParents;

         return node;
      }

      return this;
   }

   @Override
   public boolean hasExtraParents() {
      return hasExtraParents;
   }

   @Override
   public boolean isLeaf() {
      return isLeaf;
   }

   @Override
   public boolean isSecondaryParentOpened() {
      return secondaryParentOpened;
   }

   //~--- set methods ---------------------------------------------------------

   public void setHasExtraParents(boolean hasExtraParents) {
      this.hasExtraParents = hasExtraParents;
   }

   public void setIsLeaf(boolean isLeaf) {
      this.isLeaf = isLeaf;
   }

   @Override
   public void setSecondaryParentOpened(boolean secondaryParentOpened) {
      this.secondaryParentOpened = secondaryParentOpened;
   }
}
