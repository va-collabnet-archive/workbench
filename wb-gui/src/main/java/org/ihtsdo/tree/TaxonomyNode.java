
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.tree;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tree.TaxonomyNodeRenderer.NodeIcon;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import org.ihtsdo.tk.api.NidSet;

/**
 *
 * @author kec
 */
public class TaxonomyNode extends Object implements Cloneable, MutableTreeNode, Comparable<TaxonomyNode> {
   private int    cNid        = Integer.MAX_VALUE;
   private String conceptText = "unset";

   /** true if the node is able to have children */
   protected boolean               allowsChildren;
   private ArrayList<TaxonomyNode> children;
   private NodeIcon                icon;
   List<MediaChronicleBI>          mediaList;
   private MutableTreeNode         parent;
   int                             parentDepth;
   NidSetBI                        parentNidSet;
   List<Color>                     pathColors;
   int                             relId;
   private boolean                 secondaryParentNode;

   //~--- constructors --------------------------------------------------------

   public TaxonomyNode() {}

   public TaxonomyNode(int cNid, int relId, int parentDepth, boolean secondaryParentNode) {
      this.cNid                = cNid;
      this.relId               = relId;
      this.parentDepth         = parentDepth;
      this.secondaryParentNode = secondaryParentNode;
   }

   //~--- methods -------------------------------------------------------------

   /**
    * Removes <code>newChild</code> from its parent and makes it a child of
    * this node by adding it to the end of this node's child array.
    *
    * @see    #insert
    * @param  newChild node to add as a child of this node
    * @exception IllegalArgumentException    if <code>newChild</code>
    *                  is null
    * @exception IllegalStateException   if this node does not allow
    *                  children
    */
   public void add(MutableTreeNode newChild) {
      if ((newChild != null) && (newChild.getParent() == this)) {
         insert(newChild, getChildCount() - 1);
      } else {
         insert(newChild, getChildCount());
      }
   }

   /**
    * Creates and returns a forward-order enumeration of this node's
    * children.  Modifying this node's child array invalidates any child
    * enumerations created before the modification.
    *
    * @return an Enumeration of this node's children
    */
   @Override
   public Enumeration children() {
      if (children == null) {
         return DefaultMutableTreeNode.EMPTY_ENUMERATION;
      } else {
         return Collections.enumeration(children);
      }
   }

   @Override
   public Object clone() {
      throw new UnsupportedOperationException();
   }

   @Override
   public int compareTo(TaxonomyNode o) {

      // TODO add support for refset sorting here...
      return this.conceptText.compareTo(o.conceptText);
   }

   @Override
   public void insert(MutableTreeNode newChild, int childIndex) {
      children.add(childIndex, (TaxonomyNode) newChild);
   }

   @Override
   public void remove(int childIndex) {
      children.remove(childIndex);
   }

   @Override
   public void remove(MutableTreeNode aChild) {
      if (aChild != null) {
         children.remove((TaxonomyNode) aChild);
      }
   }

   public void removeAllChildren() {
      children.clear();
   }

   @Override
   public void removeFromParent() {
      if (parent != null) {
         parent.remove(this);
      }
   }

   @Override
   public String toString() {
      return conceptText;
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Returns true if this node is allowed to have children.
    *
    * @return true if this node allows children, else false
    */
   @Override
   public boolean getAllowsChildren() {
      return allowsChildren;
   }

   @Override
   public TreeNode getChildAt(int index) {
      if (children == null) {
         throw new ArrayIndexOutOfBoundsException("node has no children");
      }

      return (TreeNode) children.get(index);
   }

   @Override
   public int getChildCount() {
      if (children == null) {
         return 0;
      } else {
         return children.size();
      }
   }

   public int getCnid() {
      return cNid;
   }

   public String getConceptText() {
      if (conceptText == null) {}

      return conceptText;
   }

   public NodeIcon getIcon() {
      return icon;
   }

   @Override
   public int getIndex(TreeNode aChild) {
      return children.indexOf(aChild);    // linear search
   }

   public List<MediaChronicleBI> getMedia() {
      if (mediaList == null) {}

      return mediaList;
   }

   @Override
   public TreeNode getParent() {
      return parent;
   }

   public int getParentDepth() {
      return parentDepth;
   }

   public NidSetBI getParentNidSet(ConceptVersionBI cv) throws IOException {
      if (parentNidSet == null) {
          parentNidSet = new NidSet(cv.getRelsOutgoingDestinationsNidsActiveIsa());
      }
      return parentNidSet;
   }

   public List<Color> getPathColors(ViewCoordinate vc, Map<Integer, Color> pathColorMap) throws IOException {
      if (pathColors == null) {
         if (cNid != Integer.MAX_VALUE) {
            ConceptChronicleBI c          = Ts.get().getConcept(cNid);
            List<Color>        tempColors = new ArrayList<Color>();

            for (ConAttrVersionBI t : c.getConAttrs().getVersions()) {
               Color pathColor = pathColorMap.get(t.getPathNid());

               if (pathColor != null) {
                  tempColors.add(pathColor);
               }
            }

            pathColors = tempColors;
         } else {
            pathColors = new ArrayList();
         }
      }

      return pathColors;
   }

   @Override
   public boolean isLeaf() {
      return (getChildCount() == 0);
   }

   boolean isParentOpened() {
      throw new UnsupportedOperationException("Not yet implemented");
   }

   boolean isSecondaryParentNode() {
      return secondaryParentNode;
   }

   //~--- set methods ---------------------------------------------------------

   public void setConceptText(String conceptText) {
      this.conceptText = conceptText;
   }

   public void setIcon(NodeIcon icon) {
      this.icon = icon;
   }

   @Override
   public void setParent(MutableTreeNode newParent) {
      parent = newParent;
   }

   @Override
   public void setUserObject(Object object) {
      throw new UnsupportedOperationException("Not supported.");
   }

    boolean isExpanded() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
