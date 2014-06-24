
/**
 *
 */
package org.dwfa.ace.refset;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntSet;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.util.swing.GuiUtil;

class UpdateTreeSpec extends SwingWorker<RefsetSpecTreeNode, Object> {
   private static int count = 0;

   //~--- fields --------------------------------------------------------------

   private IntSet              cantFindWarning       = new IntSet();
   public boolean              cancel                = false;
   private boolean             newRefset             = true;
   private IntSet              childrenExpandedNodes = new IntSet();
   private int                 selectedNodeId        = Integer.MAX_VALUE;
   private int                 commentScrollHorizValue;
   private int                 commentScrollVertValue;
   private JTableWithDragImage commentTable;
   private I_ConfigAceFrame    frameConfig;
   private int                 id;
   private I_GetConceptData    localRefsetSpecConcept;
   private RefsetSpecTreeNode  newSelectedNode;
   private RefsetSpecTreeNode  oldRoot;
   private ConceptChronicleBI    refsetConcept;

   /**
    *
    */
   private final RefsetSpecEditor refsetSpecEditor;
   private RefsetSpecTreeNode     root;
   private TreePath               selectionPath;
   private int                    specScrollHorizValue;
   private int                    specScrollVertValue;

   //~--- constructors --------------------------------------------------------

   /**
    * @param refsetSpecEditor
    */
   UpdateTreeSpec(RefsetSpecEditor refsetSpecEditor) {
      this.id               = count++;
      this.refsetSpecEditor = refsetSpecEditor;
      frameConfig           = this.refsetSpecEditor.ace.getAceFrameConfig();
      oldRoot               = (RefsetSpecTreeNode) this.refsetSpecEditor.specTree.getModel().getRoot();
      refsetConcept         = (I_GetConceptData) this.refsetSpecEditor.label.getTermComponent();
   }

   //~--- methods -------------------------------------------------------------

   private void addChildrenExpandedNodes(RefsetSpecTreeNode node) {
      if (this.refsetSpecEditor.specTree.hasBeenExpanded(new TreePath(node.getPath()))) {
         childrenExpandedNodes.add(getId(node));

         for (RefsetSpecTreeNode childNode : node.getChildren()) {
            addChildrenExpandedNodes(childNode);
         }
      }
   }

   private void addExtensionsToMap(I_GetConceptData localRefsetSpecConcept,
                                   HashMap<Integer, RefsetSpecTreeNode> extensionMap)
           throws IOException, TerminologyException {
      Collection<? extends I_ExtendByRef> members =
         Terms.get().getRefsetExtensionMembers(localRefsetSpecConcept.getNid());

      for (I_ExtendByRef ext : members) {
         int currentTupleCount = ext.getTuples(frameConfig.getAllowedStatus(),
                                    frameConfig.getViewPositionSetReadOnly(), frameConfig.getPrecedence(),
                                    frameConfig.getConflictResolutionStrategy()).size();

         if ((currentTupleCount > 0) || this.refsetSpecEditor.historyButton.isSelected()) {
            extensionMap.put(ext.getMemberId(),
                             new RefsetSpecTreeNode(ext, this.refsetSpecEditor.ace.getAceFrameConfig()));
         }
      }
   }

   @Override
   protected RefsetSpecTreeNode doInBackground() throws Exception {
      if (cancel) {
         return null;
      }

      commentTable = refsetSpecEditor.getRefsetSpecPanel().createCommentTable(
         this.refsetSpecEditor.ace.getAceFrameConfig(), this.refsetSpecEditor);
      specScrollHorizValue    = this.refsetSpecEditor.specTreeScroller.getHorizontalScrollBar().getValue();
      specScrollVertValue     = this.refsetSpecEditor.specTreeScroller.getVerticalScrollBar().getValue();
      commentScrollHorizValue = this.refsetSpecEditor.commentScroller.getHorizontalScrollBar().getValue();
      commentScrollVertValue  = this.refsetSpecEditor.commentScroller.getVerticalScrollBar().getValue();
      selectionPath           = this.refsetSpecEditor.specTree.getLeadSelectionPath();

      if (selectionPath != null) {
         RefsetSpecTreeNode selectedNode = (RefsetSpecTreeNode) selectionPath.getLastPathComponent();

         if (selectedNode != null) {
            selectedNodeId = getId(selectedNode);
         }
      }

      this.refsetSpecEditor.refsetSpecConcept = null;

      if (cancel) {
         return null;
      }

      if (refsetConcept != null) {
           ConceptVersionBI refsetConceptVersion = refsetConcept.getVersion(Terms.get().getActiveAceFrameConfig().getViewCoordinate());
           Collection<? extends ConceptVersionBI> relationshipsTargetSourceConcepts = 
                    refsetConceptVersion.getRelationshipsIncomingSourceConcepts(RefsetAuxiliary.Concept.SPECIFIES_REFSET.localize().getNid());
           if ((relationshipsTargetSourceConcepts != null) && (relationshipsTargetSourceConcepts.size() > 0)) {
            this.refsetSpecEditor.refsetSpecConcept =
               (I_GetConceptData) relationshipsTargetSourceConcepts.iterator().next().getChronicle();
            localRefsetSpecConcept = this.refsetSpecEditor.refsetSpecConcept;
         }
      }

      if (cancel) {
         return null;
      }

      root = new RefsetSpecTreeNode(localRefsetSpecConcept, this.refsetSpecEditor.ace.getAceFrameConfig());

      if ((oldRoot.getUserObject() != null) && (localRefsetSpecConcept != null)) {
         I_GetConceptData oldRefsetSpecConcept = (I_GetConceptData) oldRoot.getUserObject();

         newRefset = oldRefsetSpecConcept.getConceptNid() != localRefsetSpecConcept.getConceptNid();
      }

      if (localRefsetSpecConcept != null) {
         if (cancel) {
            return null;
         }

         if (newRefset == false) {
            addChildrenExpandedNodes(oldRoot);
         }

         HashMap<Integer, RefsetSpecTreeNode> extensionMap = new HashMap<Integer, RefsetSpecTreeNode>();

         if (cancel) {
            return null;
         }

         addExtensionsToMap(localRefsetSpecConcept, extensionMap);

         List<RefsetSpecTreeNode> nodesToRemove = new ArrayList<RefsetSpecTreeNode>();

         for (RefsetSpecTreeNode extNode : extensionMap.values()) {
            if (cancel) {
               return null;
            }

            I_ExtendByRef ext = (I_ExtendByRef) extNode.getUserObject();

            if ((localRefsetSpecConcept != null) && (ext != null)) {
               if (ext.getComponentNid() == localRefsetSpecConcept.getConceptNid()) {
                  root.add(extNode);
               } else {
                  if (extensionMap.containsKey(ext.getComponentNid())) {
                     extensionMap.get(ext.getComponentNid()).add(extNode);
                  } else {
                     if (!cantFindWarning.contains(ext.getComponentNid())) {
                        cantFindWarning.add(ext.getComponentNid());

                        I_GetConceptData conceptWithComponent =
                           Terms.get().getConceptForNid(ext.getComponentNid());
                        StringBuffer msg = new StringBuffer();

                        msg.append("Warning: Missing parent clause for: [");
                        msg.append(ext.getComponentNid());
                        msg.append("] ");
                        msg.append(extNode.toString());
                        msg.append("\n\nExtension:\n");
                        msg.append(ext);
                        msg.append(" \n\n map:\n");
                        msg.append(extensionMap);
                        msg.append(" \n\nlocalRefsetSpecConcept:\n");
                        msg.append(localRefsetSpecConcept.toLongString());
                        msg.append("\n\nConcept with component:\n");

                        if (conceptWithComponent != null) {
                           msg.append(conceptWithComponent.toLongString());
                        } else {
                           msg.append("null");
                        }

                        msg.append("\n\nlocal refset spec concept:\n");
                        msg.append(localRefsetSpecConcept.toLongString());
                        AceLog.getAppLog().warning(msg.toString());
                        msg = new StringBuffer();
                        msg.append("<html>Warning: Missing parent clause for: [");
                        msg.append(ext.getComponentNid());
                        msg.append("] ");
                        msg.append(extNode.toString());
                        msg.append(
                            "<br>Probably the parent was retired, but the children clauses where not.");
                        msg.append(
                            "<br>Please retire all the child clauses. "
                            + "<br><br>If the child clauses are not shown, toggle the history button. "
                            + "<br><br>UpdateTreeSpec: ");
                        msg.append(id);
                        AceLog.getAppLog().alertAndLogException(new Exception(msg.toString()));
                        nodesToRemove.add(extNode);
                     }
                  }
               }
            } else {
               break;
            }
         }

         for (RefsetSpecTreeNode toRemove : nodesToRemove) {
            extensionMap.remove(((I_ExtendByRef) toRemove.getUserObject()).getMemberId());
         }
      }

      if (cancel) {
         return null;
      }

      sortTree(root);

      return root;
   }

   @Override
   protected void done() {
      try {
         get();

         if (cancel) {
            return;
         }

         DefaultTreeModel tm = (DefaultTreeModel) this.refsetSpecEditor.specTree.getModel();

         tm.setRoot(root);

         if (cancel) {
            return;
         }

         this.refsetSpecEditor.commentScroller.setViewportView(commentTable);

         if (newRefset == false) {
            expandNodes(root);

            if (newSelectedNode != null) {
               this.refsetSpecEditor.specTree.setSelectionPath(new TreePath(newSelectedNode.getPath()));
               this.refsetSpecEditor.specTree.setLeadSelectionPath(new TreePath(newSelectedNode.getPath()));
            }

            this.refsetSpecEditor.specTreeScroller.getHorizontalScrollBar().setValue(specScrollHorizValue);
            this.refsetSpecEditor.specTreeScroller.getVerticalScrollBar().setValue(specScrollVertValue);
            this.refsetSpecEditor.commentScroller.getHorizontalScrollBar().setValue(commentScrollHorizValue);
            this.refsetSpecEditor.commentScroller.getVerticalScrollBar().setValue(commentScrollVertValue);
            GuiUtil.tickle(this.refsetSpecEditor.commentScroller);
            GuiUtil.tickle(this.refsetSpecEditor.specTree);
            GuiUtil.tickle(this.refsetSpecEditor.specTreeScroller);
         } else {
            this.refsetSpecEditor.commentScroller.getHorizontalScrollBar().setValue(0);
            this.refsetSpecEditor.commentScroller.getVerticalScrollBar().setValue(0);
         }

         if (selectionPath != null) {
            this.refsetSpecEditor.specTree.expandPath(selectionPath);
         }
      } catch (InterruptedException e) {
         AceLog.getAppLog().alertAndLogException(e);
      } catch (ExecutionException e) {
         AceLog.getAppLog().alertAndLogException(e);
      }

      super.done();
   }

   private void expandNodes(RefsetSpecTreeNode node) {
      if (getId(node) == selectedNodeId) {
         newSelectedNode = node;
      }

      if (childrenExpandedNodes.contains(getId(node))) {
         this.refsetSpecEditor.specTree.expandPath(new TreePath(node.getPath()));

         if (node.getChildren() != null) {
            for (RefsetSpecTreeNode childNode : node.getChildren()) {
               expandNodes(childNode);
            }
         }
      }
   }

   private void sortTree(RefsetSpecTreeNode node) {
      if (node.sortChildren()) {
         for (RefsetSpecTreeNode child : node.getChildren()) {
            sortTree(child);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   private int getId(RefsetSpecTreeNode node) {
      if (I_GetConceptData.class.isAssignableFrom(node.getUserObject().getClass())) {
         return ((I_GetConceptData) node.getUserObject()).getConceptNid();
      } else if (I_ExtendByRef.class.isAssignableFrom(node.getUserObject().getClass())) {
         I_ExtendByRef ext = (I_ExtendByRef) node.getUserObject();

         return ext.getMemberId();
      }

      return Integer.MAX_VALUE;
   }
}
