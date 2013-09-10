/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.dwfa.ace.tree;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.gui.popup.ProcessPopupUtil;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.RefsetCommentPopupListener;
import org.dwfa.ace.search.QueryBean;
import org.dwfa.ace.search.SimilarConceptQuery;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptContainerBI;

//~--- JDK imports ------------------------------------------------------------

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Collection;
import java.util.HashMap;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.ihtsdo.tk.query.RefsetSpec;

public class TreeMouseListener extends MouseAdapter {
   private ACE ace;

   //~--- constructors --------------------------------------------------------

   public TreeMouseListener(ACE ace) {
      super();
      this.ace = ace;
   }

   //~--- methods -------------------------------------------------------------

   private void addRefsetItems(JPopupMenu popup, boolean excludesConcept, boolean excludesDesc,
                               boolean excludesRel, boolean excludesContains)
           throws FileNotFoundException, IOException, ClassNotFoundException {

      // adding grouping clauses (OR, AND, !OR, !AND) - these are always displayed
      File  groupingFile       = new File(AceFrame.pluginRoot, "refsetspec/branch-popup/grouping");
      JMenu newSubMenuGrouping = new JMenu(groupingFile.getName());

      popup.add(newSubMenuGrouping);
      ProcessPopupUtil.addSubMenuItems(newSubMenuGrouping, groupingFile,
              ace.getAceFrameConfig().getWorker());

      // sub-menu for "concept-contains-desc" and "concept-contains-rel"
      if (!excludesContains) {
         File  containsFile       = new File(AceFrame.pluginRoot, "refsetspec/branch-popup/contains");
         JMenu newSubMenuContains = new JMenu(containsFile.getName());

         popup.add(newSubMenuContains);
         ProcessPopupUtil.addSubMenuItems(newSubMenuContains, containsFile,
                 ace.getAceFrameConfig().getWorker());
      }

      // sub-menu for concept based clauses e.g. concept is, concept is child of
      if (!excludesConcept) {
         File  conceptFile       = new File(AceFrame.pluginRoot, "refsetspec/branch-popup/concept");
         JMenu newSubMenuConcept = new JMenu(conceptFile.getName());

         popup.add(newSubMenuConcept);
         ProcessPopupUtil.addSubMenuItems(newSubMenuConcept, conceptFile,
                 ace.getAceFrameConfig().getWorker());

         // sub-menu for diff
         conceptFile       = new File(AceFrame.pluginRoot, "refsetspec/branch-popup/diff");
         newSubMenuConcept = new JMenu(conceptFile.getName());
         popup.add(newSubMenuConcept);
         ProcessPopupUtil.addSubMenuItems(newSubMenuConcept, conceptFile,
                 ace.getAceFrameConfig().getWorker());
      }

      // sub-menu for desc based clauses e.g. desc is, desc is child of
      if (!excludesDesc) {
         File  descFile       = new File(AceFrame.pluginRoot, "refsetspec/branch-popup/desc");
         JMenu newSubMenuDesc = new JMenu(descFile.getName());

         popup.add(newSubMenuDesc);
         ProcessPopupUtil.addSubMenuItems(newSubMenuDesc, descFile, ace.getAceFrameConfig().getWorker());
      }

      // sub-menu for rel based clauses e.g. rel is
      if (!excludesRel) {
         File  relFile       = new File(AceFrame.pluginRoot, "refsetspec/branch-popup/rel");
         JMenu newSubMenuRel = new JMenu(relFile.getName());

         popup.add(newSubMenuRel);
         ProcessPopupUtil.addSubMenuItems(newSubMenuRel, relFile, ace.getAceFrameConfig().getWorker());
      }
   }

   private boolean clauseIsChildOfConceptContainsDesc(I_ExtendByRef specPart,
           HashMap<Integer, I_ExtendByRef> componentIdBasedExtensionMap)
           throws IOException, TerminologyException {
      int                     conceptContainsDescNid =
         RefsetAuxiliary.Concept.CONCEPT_CONTAINS_DESC_GROUPING.localize().getNid();
      I_ExtendByRefPartCidCid cidCidPart             = (I_ExtendByRefPartCidCid) specPart;

      if (cidCidPart.getC2id() == conceptContainsDescNid) {
         return true;
      } else {
         I_ExtendByRef parentSpecPart = componentIdBasedExtensionMap.get(specPart.getComponentNid());

         if (parentSpecPart == null) {
            return false;
         } else {
            return clauseIsChildOfConceptContainsDesc(parentSpecPart, componentIdBasedExtensionMap);
         }
      }
   }

   private boolean clauseIsChildOfConceptContainsRel(I_ExtendByRef specPart,
           HashMap<Integer, I_ExtendByRef> componentIdBasedExtensionMap)
           throws IOException, TerminologyException {
      int                     conceptContainsRelNid =
         RefsetAuxiliary.Concept.CONCEPT_CONTAINS_REL_GROUPING.localize().getNid();
      I_ExtendByRefPartCidCid cidCidPart            = (I_ExtendByRefPartCidCid) specPart;

      if (cidCidPart.getC2id() == conceptContainsRelNid) {
         return true;
      } else {
         I_ExtendByRef parentSpecPart = componentIdBasedExtensionMap.get(specPart.getComponentNid());

         if (parentSpecPart == null) {
            return false;
         } else {
            return clauseIsChildOfConceptContainsRel(parentSpecPart, componentIdBasedExtensionMap);
         }
      }
   }

   private void makeAndShowPopup(MouseEvent e, I_GetConceptData selectedConcept) {
      JPopupMenu popup;

      try {
         popup = makePopup(e, selectedConcept);
         popup.show(e.getComponent(), e.getX(), e.getY());
      } catch (FileNotFoundException e1) {
         AceLog.getAppLog().alertAndLogException(e1);
      } catch (IOException e1) {
         AceLog.getAppLog().alertAndLogException(e1);
      } catch (ClassNotFoundException e1) {
         AceLog.getAppLog().alertAndLogException(e1);
      } catch (TerminologyException e1) {
         AceLog.getAppLog().alertAndLogException(e1);
      }
   }

   private JPopupMenu makePopup(MouseEvent e, I_GetConceptData selectedConcept)
           throws FileNotFoundException, IOException, ClassNotFoundException, TerminologyException {
      JPopupMenu popup        = new JPopupMenu();
      JMenuItem  noActionItem = new JMenuItem("");

      popup.add(noActionItem);

      if (ace.getRefsetSpecInSpecEditor() != null) {
         if (ace.refsetTabIsSelected()) {
            JTree specTree = ace.getTreeInSpecEditor();

            if (specTree.isVisible() && (specTree.getSelectionCount() > 0)) {
               TreePath selPath = specTree.getSelectionPath();

               if (selPath != null) {
                  DefaultMutableTreeNode node     = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                  I_ExtendByRef          specPart = (I_ExtendByRef) node.getUserObject();

                  switch (EConcept.REFSET_TYPES.nidToType(specPart.getTypeNid())) {
                  case CID_CID :
                     popup.addSeparator();

                     Collection<? extends I_ExtendByRef> extensions =
                        Terms.get().getRefsetExtensionMembers(
                            ace.getAceFrameConfig().getRefsetSpecInSpecEditor().getConceptNid());
                     HashMap<Integer, I_ExtendByRef> memberIdBasedExtensionMap = new HashMap<Integer,
                                                                                    I_ExtendByRef>();

                     memberIdBasedExtensionMap = populateMemberIdBasedExtensionMap(extensions);

                     RefsetSpec refsetSpecHelper =
                        new RefsetSpec(ace.getAceFrameConfig().getRefsetSpecInSpecEditor(),
                                       ace.getAceFrameConfig().getViewCoordinate());
                     boolean excludeDesc     = true;
                     boolean excludeConcept  = true;
                     boolean excludeRel      = true;
                     boolean excludeContains = true;

                     if (refsetSpecHelper.isDescriptionComputeType()) {

                        // show AND, OR, !AND, !OR
                        // show desc clauses
                        excludeDesc = false;
                     } else if (refsetSpecHelper.isRelationshipComputeType()) {

                        // show AND, OR, !AND, !OR
                        // show rel clauses
                        excludeRel = false;
                     } else {
                        if (clauseIsChildOfConceptContainsDesc(specPart, memberIdBasedExtensionMap)) {

                           // show AND, OR, !AND, !OR
                           // show desc clauses
                           excludeDesc = false;
                        } else if (clauseIsChildOfConceptContainsRel(specPart, memberIdBasedExtensionMap)) {

                           // show AND, OR, !AND, !OR
                           // show rel clauses
                           excludeRel = false;
                        } else {

                           // show AND, OR, !AND, !OR,
                           // show contains desc/rel, NOT contains desc/rel
                           // show concept clauses
                           excludeConcept  = false;
                           excludeContains = false;
                        }
                     }

                     addRefsetItems(popup, excludeConcept, excludeDesc, excludeRel, excludeContains);

                     break;

                  default :
                  }
               }
            }
         }

         popup.addSeparator();

         RefsetCommentPopupListener refsetCommentActionListener =
            new RefsetCommentPopupListener(ace.getAceFrameConfig(), ace.getRefsetSpecEditor());

         refsetCommentActionListener.setConceptForComment(selectedConcept);

         JMenuItem refsetCommmentItem = new JMenuItem(refsetCommentActionListener.getPrompt());

         refsetCommmentItem.addActionListener(refsetCommentActionListener.getActionListener());
         popup.add(refsetCommmentItem);
      }

      popup.addSeparator();

      JMenuItem searchForSimilarConcepts = new JMenuItem("Search for similar concepts...");

      popup.add(searchForSimilarConcepts);
      searchForSimilarConcepts.addActionListener(new SetSearchToSimilar());
      popup.addSeparator();
      ProcessPopupUtil.addSubMenuItems(popup, new File(AceFrame.pluginRoot, "taxonomy"),
              ace.getAceFrameConfig().getWorker());

      return popup;
   }

   @Override
   public void mousePressed(MouseEvent e) {
      try {
         JTree     tree   = (JTree) e.getSource();
         TreeModel model  = tree.getModel();
         int       selRow = tree.getRowForLocation(e.getX(), e.getY());

         // AceLog.getLog().info("Selected row: " + selRow);
         TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());

         if (selPath != null) {
            if (selRow != -1) {
               ConceptContainerBI node = (ConceptContainerBI) selPath.getLastPathComponent();

               if (e.isPopupTrigger()) {
                  makeAndShowPopup(e, (I_GetConceptData) Ts.get().getConcept(node));
               }
            }
         }
      } catch (Exception e1) {
         AceLog.getAppLog().alertAndLogException(e1);
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
            ConceptContainerBI node = (ConceptContainerBI) selPath.getLastPathComponent();

            if (e.isPopupTrigger()) {
               try {
                  makeAndShowPopup(e, (I_GetConceptData) Ts.get().getConcept(node));
               } catch (IOException ex) {
                  AceLog.getAppLog().alertAndLogException(ex);
               }
            }
         }
      }
   }

   private HashMap<Integer, I_ExtendByRef> populateMemberIdBasedExtensionMap(
           Collection<? extends I_ExtendByRef> extensions) {
      HashMap<Integer, I_ExtendByRef> extensionMap = new HashMap<Integer, I_ExtendByRef>();

      for (I_ExtendByRef extension : extensions) {
         extensionMap.put(extension.getMemberId(), extension);
      }

      return extensionMap;
   }

   //~--- inner classes -------------------------------------------------------

   public class SetSearchToSimilar implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent arg0) {
         I_GetConceptData selectedConcept = ace.getAceFrameConfig().getHierarchySelection();

         try {
            QueryBean qb = SimilarConceptQuery.make(selectedConcept, ace.getAceFrameConfig());

            ace.getSearchPanel().setQuery(qb);
         } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
         } catch (ClassNotFoundException e) {
            AceLog.getAppLog().alertAndLogException(e);
         } catch (InstantiationException e) {
            AceLog.getAppLog().alertAndLogException(e);
         } catch (IllegalAccessException e) {
            AceLog.getAppLog().alertAndLogException(e);
         } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
         }
      }
   }
}
