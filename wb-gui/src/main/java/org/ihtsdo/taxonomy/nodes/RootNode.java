
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.taxonomy.nodes;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.taxonomy.model.TaxonomyModel;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * @author kec
 */
public class RootNode extends TaxonomyNode {
   public ConcurrentSkipListSet<Long>          children;
   public ConcurrentSkipListMap<Integer, Long> nidNodeIdMap = new ConcurrentSkipListMap<Integer, Long>();
   boolean                                     isLeaf       = false;

   //~--- constructors --------------------------------------------------------

   public RootNode(Comparator c) {
      super(Integer.MAX_VALUE, Integer.MAX_VALUE,
            TaxonomyModel.getNodeId(Integer.MIN_VALUE, Integer.MIN_VALUE));
      children     = new ConcurrentSkipListSet<Long>(c);
   }

   //~--- methods -------------------------------------------------------------
   @Override
   public boolean addChild(TaxonomyNode child) {
      int cnid = child.getConceptNid();

      if (!this.nidNodeIdMap.containsKey(cnid)) {
         this.children.add(child.nodeId);
         this.nidNodeIdMap.put(child.getConceptNid(), child.nodeId);

         return true;
      }

      return false;
   }

   @Override
   public void addExtraParent(TaxonomyNode extraParent) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean childrenAreSet() {
      return true;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public Collection<Long> getChildren() {
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

    @Override
    public void setSecondaryParentOpened(boolean secondaryParentOpened) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
