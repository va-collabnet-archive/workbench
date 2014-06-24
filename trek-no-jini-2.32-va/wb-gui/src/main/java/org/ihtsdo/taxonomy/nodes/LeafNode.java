
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.taxonomy.nodes;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

/**
 *
 * @author kec
 */
public class LeafNode extends TaxonomyNode {
   public LeafNode(TaxonomyNode node) {
      super(node);
   }
   public LeafNode(int cnid, int parentNid, long parentNodeId) {
      super(cnid, parentNid, parentNodeId);
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean addChild(TaxonomyNode child) {
      throw new UnsupportedOperationException("Leaf nodes can't have children. ");
   }

   @Override
   public void addExtraParent(TaxonomyNode extraParent) {
      throw new UnsupportedOperationException("Node can't have extra parent. ");
   }

   @Override
   public boolean childrenAreSet() {
      return true;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Collection<Long> getChildren() {
      return empty;
   }

   @Override
   public Collection<Long> getExtraParents() {
      return empty;
   }

   @Override
   public TaxonomyNode getFinalNode() {
      return this;
   }

   @Override
   public boolean hasExtraParents() {
      return false;
   }

   @Override
   public boolean isLeaf() {
      return true;
   }

   @Override
   public boolean isSecondaryParentOpened() {
      return false;
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setSecondaryParentOpened(boolean secondaryParentOpened) {

      //
   }
}
