
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.taxonomy.nodes;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * @author kec
 */
public class InternalNode extends TaxonomyNode {
   public ConcurrentSkipListMap<Integer, Long> nidNodeMap     = new ConcurrentSkipListMap<Integer, Long>();
   boolean                                     childrenAreSet = false;
   private ConcurrentSkipListSet<Long>         children;

   //~--- constructors --------------------------------------------------------

   public InternalNode(int cnid, int parentNid, long parentNodeId, Comparator comparator) {
      super(cnid, parentNid, parentNodeId);
      children = new ConcurrentSkipListSet<Long>(comparator);
   }


   public InternalNode(InternalNodeMultiParent another) {
      super(another);
      children = another.getChildren();
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean addChild(TaxonomyNode child) {
      Integer cnid = child.getConceptNid();

      if (!this.nidNodeMap.containsKey(cnid)) {
         this.children.add(child.nodeId);
         this.nidNodeMap.put(cnid, child.nodeId);

         return true;
      }

      return false;
   }

   @Override
   public void addExtraParent(TaxonomyNode extraParent) {
      throw new UnsupportedOperationException("Node can't have extra parents");
   }

   @Override
   public boolean childrenAreSet() {
      return childrenAreSet;
   }

   public void clearChildren() {
      nidNodeMap.clear();
      children.clear();
      childrenAreSet = false;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public ConcurrentSkipListSet<Long> getChildren() {
      return children;
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
      return false;
   }

   @Override
   public boolean isSecondaryParentOpened() {
      return false;
   }

   //~--- set methods ---------------------------------------------------------

   public void setChildrenAreSet(boolean childrenAreSet) {
      this.childrenAreSet = childrenAreSet;
   }

   @Override
   public void setSecondaryParentOpened(boolean secondaryParentOpened) {

      //
   }
}
