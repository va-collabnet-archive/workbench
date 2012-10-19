
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.taxonomy;

//~--- non-JDK imports --------------------------------------------------------

import java.awt.Component;
import java.awt.Insets;
import org.ihtsdo.taxonomy.model.NodePath;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.gui.popup.ProcessPopupUtil;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.search.QueryBean;
import org.dwfa.ace.search.SimilarConceptQuery;
import org.dwfa.ace.tree.I_RenderAndFocusOnBean;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.taxonomy.model.TaxonomyModel;
import org.ihtsdo.taxonomy.nodes.RootNode;
import org.ihtsdo.taxonomy.nodes.SecondaryParentNode;
import org.ihtsdo.taxonomy.nodes.SecondaryParentNodeRoot;
import org.ihtsdo.taxonomy.nodes.TaxonomyNode;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 *
 * @author kec
 */
public class TaxonomyMouseListenerForAce extends MouseAdapter {
   private ACE            ace;
   private TaxonomyHelper helper;

   //~--- constructors --------------------------------------------------------

   public TaxonomyMouseListenerForAce(ACE ace, TaxonomyHelper helper) {
      super();
      this.ace    = ace;
      this.helper = helper;
   }

   //~--- methods -------------------------------------------------------------

   private void addAllParentsAsExtra(ConceptVersionBI nodeConcept, TaxonomyNode node)
           throws ContradictionException, IOException {
      node.getExtraParents().clear();
      if (node.getParentNid() != Integer.MAX_VALUE) {    // test if root
         for (ConceptVersionBI parent : nodeConcept.getRelationshipsOutgoingTargetConceptsActiveIsa()) {
            if (parent.getNid() != node.getParentNid()) {
               TaxonomyNode extraParentNode = null;
               long[]       nodesToCompare  = new long[node.getNodesToCompare().length + 1];

               System.arraycopy(node.getNodesToCompare(), 0, nodesToCompare, 0,
                                node.getNodesToCompare().length);
               nodesToCompare[node.getNodesToCompare().length] = Long.MAX_VALUE;

               if (parent.getRelationshipsOutgoingActiveIsa().isEmpty()) {
                  extraParentNode = new SecondaryParentNodeRoot(parent.getNid(), nodeConcept.getNid(),
                          node.parentNodeId, nodesToCompare);
               } else {
                  extraParentNode = new SecondaryParentNode(parent.getNid(), nodeConcept.getNid(),
                          node.parentNodeId, nodesToCompare);
               }

               helper.getNodeStore().add(extraParentNode);
               extraParentNode.setParentDepth(node.getParentDepth() + 1);
               helper.getRenderer().setupTaxonomyNode(extraParentNode, parent);
               node.addExtraParent(extraParentNode);
            }
         }
      }
   }

   private void makeAndShowPopup(MouseEvent e, ConceptChronicleBI selectedConcept) {
      JPopupMenu popup;

      try {
         popup = makePopup(e, selectedConcept);
         popup.show(e.getComponent(), e.getX(), e.getY());
      } catch (Exception e1) {
         AceLog.getAppLog().alertAndLogException(e1);
      }
   }

   private JPopupMenu makePopup(MouseEvent e, ConceptChronicleBI selectedConcept)
           throws FileNotFoundException, IOException, ClassNotFoundException, TerminologyException {
      JPopupMenu popup        = new JPopupMenu();
      JMenuItem  noActionItem = new JMenuItem("");

      popup.add(noActionItem);

      JMenuItem searchForSimilarConcepts = new JMenuItem("Search for similar concepts...");

      popup.add(searchForSimilarConcepts);
      searchForSimilarConcepts.addActionListener(new SetSearchToSimilar());
      popup.addSeparator();
      ProcessPopupUtil.addSubmenMenuItems(popup, new File(AceFrame.pluginRoot, "taxonomy"),
              ace.getAceFrameConfig().getWorker());

      return popup;
   }

   @Override
   public void mousePressed(MouseEvent e) {
      try {
         JTree         tree   = (JTree) e.getSource();
         TaxonomyModel model  = (TaxonomyModel) tree.getModel();
         int           selRow = tree.getRowForLocation(e.getX(), e.getY());

         // AceLog.getLog().info("Selected row: " + selRow);
         TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());

         if (selPath != null) {
            if (selRow != -1) {
               TaxonomyNode node = (TaxonomyNode) selPath.getLastPathComponent();

               if (node instanceof RootNode) {
                  return;
               }

               ConceptChronicleBI selectedConcept = Ts.get().getConcept(node.getConceptNid());

               if (e.isPopupTrigger()) {
                  makeAndShowPopup(e, selectedConcept);
               } else {
                  I_RenderAndFocusOnBean renderer = (I_RenderAndFocusOnBean) tree.getCellRenderer();
                  Insets insets = new Insets(0, 0, 0, 0);
                  if (((TaxonomyNodeRenderer) renderer).getBorder() != null) {
                      insets = ((TaxonomyNodeRenderer) renderer).getBorder().getBorderInsets((Component) renderer);
                  }
                  renderer = (TaxonomyNodeRenderer) renderer.getTreeCellRendererComponent(tree, node, true,
                          tree.isExpanded(selRow), node.isLeaf(), selRow, true);

                  Rectangle bounds = tree.getRowBounds(selRow);

                  if (e.getClickCount() == 1) {
                     if (node.hasExtraParents()) {
                        Rectangle iconBounds = renderer.getIconRect(node.getParentDepth());

                        if ((e.getPoint().x > bounds.x + iconBounds.x)
                                && (e.getPoint().x + 1 < bounds.x + iconBounds.x + iconBounds.width + insets.left)) {
                           openOrCloseParent(tree, model, node, bounds);
                        }
                     }
                  }

                  // tree.setSelectionPath(new TreePath(selPath.getPath()));
                  int newRow = tree.getRowForPath(selPath);

                  // AceLog.getLog().info("New row: " + newRow);
                  tree.setSelectionInterval(newRow, newRow);
               }
            }
         }
      } catch (Exception ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }
   }

   @Override
   public void mouseReleased(MouseEvent e) {
      JTree tree   = (JTree) e.getSource();
      int   selRow = tree.getRowForLocation(e.getX(), e.getY());

      // AceLog.getLog().info("Selected row: " + selRow);
      TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());

      if (selPath != null) {
         if (selRow != -1) {
            try {
               TaxonomyNode node = (TaxonomyNode) selPath.getLastPathComponent();

               if (node instanceof RootNode) {
                  return;
               }

               ConceptChronicleBI selectedConcept = Ts.get().getConcept(node.getConceptNid());

               if (e.isPopupTrigger()) {
                  makeAndShowPopup(e, selectedConcept);
               }
            } catch (IOException ex) {
               AceLog.getAppLog().alertAndLogException(ex);
            }
         }
      }
   }

   private void openOrCloseParent(JTree tree, TaxonomyModel model, TaxonomyNode node, Rectangle bounds)
           throws IOException {
      boolean addNodes = !node.isSecondaryParentOpened();

      node.setSecondaryParentOpened(addNodes);

      ConceptVersionBI nodeConcept = Ts.get().getConceptVersion(helper.getViewCoordinate(), node.getConceptNid());

      helper.getRenderer().setupTaxonomyNode(node, nodeConcept);
      tree.paintImmediately(bounds);

      TaxonomyNode parentNode = model.getParent(node);

      if ((parentNode != null)) {
         if (addNodes) {
            try {
               addAllParentsAsExtra(nodeConcept, node);
               parentNode.getChildren().addAll(node.getExtraParents());
            } catch (Exception e) {
               AceLog.getAppLog().alertAndLogException(e);
            }
         } else {    // remove nodes
            removeAllExtraParents(model, node);
            ace.getAceFrameConfig().getParentExpandedNodes().remove(node.getConceptNid());
         }

         model.treeStructureChanged(NodePath.getTreePath(model, parentNode));
      }
   }

   private void removeAllExtraParents(TaxonomyModel model, TaxonomyNode node) {
      if (node != null) {
         TaxonomyNode parentNode = model.getParent(node);

         for (Long extraParentNodeId : node.getExtraParents()) {
            removeAllExtraParents(model, model.getNodeStore().get(extraParentNodeId));
            parentNode.getChildren().remove(extraParentNodeId);
         }

         model.treeStructureChanged(NodePath.getTreePath(model, node));
      }
   }

   //~--- inner classes -------------------------------------------------------

   public class SetSearchToSimilar implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent arg0) {
         ConceptChronicleBI selectedConcept = ace.getAceFrameConfig().getHierarchySelection();

         try {
            QueryBean qb = SimilarConceptQuery.make((I_GetConceptData) selectedConcept,
                              ace.getAceFrameConfig());

            ace.getSearchPanel().setQuery(qb);
         } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
         }
      }
   }
}
