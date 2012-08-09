
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.taxonomy.nodes;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.taxonomy.model.TaxonomyModel;
import org.ihtsdo.taxonomy.TaxonomyNodeRenderer.NodeIcon;
import org.ihtsdo.tk.api.ConceptContainerBI;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** 
 *
 * @author kec
 */
public abstract class TaxonomyNode implements ConceptContainerBI, Comparable<TaxonomyNode> {
   static Collection<Long> empty = Collections.unmodifiableCollection(new ArrayList<Long>());

   //~--- fields --------------------------------------------------------------

   String               text = "-";
   private final int    hash;
   private NodeIcon     icon;
   public final long    nodeId;
   protected long[]     nodesToCompare;
   public final long    parentNodeId;
   private List<Color>  pathColors;
   protected Comparable sortComparable;

   //~--- constructors --------------------------------------------------------

   public TaxonomyNode(int cnid, int parentNid, long parentNodeId) {
      nodeId         = TaxonomyModel.getNodeId(cnid, parentNid);
      nodesToCompare = new long[] { nodeId, Long.MAX_VALUE };
      assert nodeId != parentNodeId;
      this.parentNodeId = parentNodeId;
      hash              = Hashcode.compute(cnid, parentNid);
   }

   public TaxonomyNode(TaxonomyNode another) {
      this.nodeId         = TaxonomyModel.getNodeId(another.getConceptNid(), another.getParentNid());
      this.nodesToCompare = another.nodesToCompare;
      assert nodeId != another.parentNodeId;
      this.parentNodeId = another.parentNodeId;
      this.hash              = another.hash;
      this.pathColors = another.pathColors;
      this.sortComparable = another.sortComparable;
      this.text = another.text;
      this.icon = another.icon;
   }

   //~--- methods -------------------------------------------------------------

   public abstract boolean addChild(TaxonomyNode child);

   public abstract void addExtraParent(TaxonomyNode extraParent);

   public abstract boolean childrenAreSet();

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof TaxonomyNode) {
         return this.nodeId == ((TaxonomyNode) obj).nodeId;
      }

      return false;
   }

    @Override
    public int compareTo(TaxonomyNode o) {
        if (this.nodeId == o.nodeId) {
            return 0;
        }
        if (this.nodeId > o.nodeId) {
            return 1;
        }
        return -1;
    }

   @Override
   public int hashCode() {
      return hash;
   }

   @Override
   public String toString() {
      return text;
   }

   //~--- get methods ---------------------------------------------------------

   public abstract Collection<Long> getChildren();

   @Override
   public int getConceptNid() {
      return TaxonomyModel.getCnid(nodeId);
   }

   public abstract Collection<Long> getExtraParents();

   public abstract TaxonomyNode getFinalNode();

   public NodeIcon getIcon() {
      return icon;
   }

   public int getIndex(long childNodeId) {
      int index = 0;

      for (Long childNodeId2 : getChildren()) {
         if (childNodeId2 == childNodeId) {
            return index;
         }

         index++;
      }

      return -1;
   }

   public long getNodeId() {
      return nodeId;
   }

   public long[] getNodesToCompare() {
      return nodesToCompare;
   }

   public int getParentDepth() {
      return 0;
   }

   public int getParentNid() {
      return TaxonomyModel.getParentNid(nodeId);
   }

   public List<Color> getPathColors() throws IOException {
      if (pathColors == null) {
         return new ArrayList(0);
      }

      return pathColors;
   }

   public Comparable getSortComparable() {
      return sortComparable;
   }

   public String getText() {
      return text;
   }

   public abstract boolean hasExtraParents();

   public abstract boolean isLeaf();

   public boolean isSecondaryParentNode() {
      return false;
   }

   public abstract boolean isSecondaryParentOpened();

   //~--- set methods ---------------------------------------------------------

   public void setHasExtraParents(boolean b) {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   public void setIcon(NodeIcon icon) {
      this.icon = icon;
   }

   public void setNodesToCompare(long[] nodesToCompare) {
      this.nodesToCompare = nodesToCompare;
   }

   public void setParentDepth(int depth) {
      throw new UnsupportedOperationException();
   }

   public void setPathColors(List<Color> pathColors) throws IOException {
      this.pathColors = pathColors;
   }

   public abstract void setSecondaryParentOpened(boolean secondaryParentOpened);

   public void setSortComparable(Comparable sortComparable) {
      this.sortComparable = sortComparable;
   }

   public void setText(String text) {
      this.text = text;
   }
}
