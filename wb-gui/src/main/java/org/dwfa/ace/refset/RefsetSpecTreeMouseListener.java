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
package org.dwfa.ace.refset;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.gui.popup.ProcessPopupUtil;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.bpa.util.OpenFrames;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public class RefsetSpecTreeMouseListener extends MouseAdapter {

    private I_ConfigAceFrame aceConfig;
    private RefsetSpecEditor specEditor;

    public RefsetSpecTreeMouseListener(I_ConfigAceFrame aceConfig, RefsetSpecEditor specEditor) {
        super();
        this.aceConfig = aceConfig;
        this.specEditor = specEditor;
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            try {
                makeAndShowPopup(e);
            } catch (TerminologyException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            try {
                makeAndShowPopup(e);
            } catch (TerminologyException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void makeAndShowPopup(MouseEvent e) throws TerminologyException {
        try {
            I_GetConceptData spec = specEditor.getRefsetSpecInSpecEditor();
            RefsetSpec refsetSpecHelper = new RefsetSpec(spec, aceConfig);

            JPopupMenu popup = null;
            if (e.isPopupTrigger()) {
                try {
                    JTree tree = (JTree) e.getSource();
                    int rowForLocation = tree.getRowForLocation(e.getX(), e.getY());
                    int[] selectedRow = tree.getSelectionRows();
                    if (rowForLocation < 0 || selectedRow == null || selectedRow[0] != rowForLocation) {
                        tree.clearSelection();
                        // no row selected so show grouping clauses and concept-contains clauses
                        popup = makePopup(e, null, true, true, true, false);
                    } else {
                        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                        if (selPath != null) {
                            if (rowForLocation != -1) {
                                DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                                I_ExtendByRef specPart = (I_ExtendByRef) node.getUserObject();
                                Collection<? extends I_ExtendByRef> extensions =
                                        Terms.get().getRefsetExtensionMembers(
                                            aceConfig.getRefsetSpecInSpecEditor().getConceptNid());

                                HashMap<Integer, I_ExtendByRef> memberIdBasedExtensionMap =
                                        new HashMap<Integer, I_ExtendByRef>();
                                memberIdBasedExtensionMap = populateMemberIdBasedExtensionMap(extensions);

                                switch (EConcept.REFSET_TYPES.nidToType(specPart.getTypeNid())) {
                                case CID_CID:
                                    boolean excludeDesc = true;
                                    boolean excludeConcept = true;
                                    boolean excludeRel = true;
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
                                            excludeConcept = false;
                                            excludeContains = false;
                                        }
                                    }

                                    popup = makePopup(e, specPart, excludeConcept, excludeDesc, excludeRel, excludeContains);
                                    break;
                                case CID_CID_CID:
                                    popup =
                                            makePopup(e, new File(AceFrame.pluginRoot, "refsetspec/structural-query-popup"),
                                                specPart);
                                    break;
                                case CID_CID_STR:
                                    popup =
                                            makePopup(e, new File(AceFrame.pluginRoot, "refsetspec/text-query-popup"),
                                                specPart);
                                    break;
                                default:
                                    popup = null;
                                }
                            }
                        } else {
                            // no row selected so show grouping clauses and concept-contains clauses
                            popup = makePopup(e, null, true, true, true, false);
                        }
                    }
                    if (popup != null) {
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                } catch (FileNotFoundException e1) {
                    AceLog.getAppLog().alertAndLogException(e1);
                } catch (IOException e1) {
                    AceLog.getAppLog().alertAndLogException(e1);
                } catch (ClassNotFoundException e1) {
                    AceLog.getAppLog().alertAndLogException(e1);
                }
            }
        } catch (IOException e2) {
            throw new TerminologyException(e2.getMessage());
        }
    }

    private HashMap<Integer, I_ExtendByRef> populateMemberIdBasedExtensionMap(Collection<? extends I_ExtendByRef> extensions) {
        HashMap<Integer, I_ExtendByRef> extensionMap = new HashMap<Integer, I_ExtendByRef>();

        for (I_ExtendByRef extension : extensions) {
            extensionMap.put(extension.getMemberId(), extension);
        }
        return extensionMap;
    }

    private boolean clauseIsChildOfConceptContainsRel(I_ExtendByRef specPart,
            HashMap<Integer, I_ExtendByRef> componentIdBasedExtensionMap) throws IOException, TerminologyException {
        int conceptContainsRelNid = RefsetAuxiliary.Concept.CONCEPT_CONTAINS_REL_GROUPING.localize().getNid();
        I_ExtendByRefPartCidCid cidCidPart = (I_ExtendByRefPartCidCid) specPart;
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

    private boolean clauseIsChildOfConceptContainsDesc(I_ExtendByRef specPart,
            HashMap<Integer, I_ExtendByRef> componentIdBasedExtensionMap) throws IOException, TerminologyException {

        int conceptContainsDescNid = RefsetAuxiliary.Concept.CONCEPT_CONTAINS_DESC_GROUPING.localize().getNid();
        I_ExtendByRefPartCidCid cidCidPart = (I_ExtendByRefPartCidCid) specPart;
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

    private JPopupMenu makePopup(MouseEvent e, I_ExtendByRef specPart, boolean excludesConcept, boolean excludesDesc,
            boolean excludesRel, boolean excludesContains) throws FileNotFoundException, IOException,
            ClassNotFoundException, TerminologyException {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem noActionItem = new JMenuItem("");
        popup.add(noActionItem);

        // adding grouping clauses (OR, AND, !OR, !AND) - these are always displayed
        File groupingFile = new File(AceFrame.pluginRoot, "refsetspec/branch-popup/grouping");
        JMenu newSubMenuGrouping = new JMenu(groupingFile.getName());
        popup.add(newSubMenuGrouping);
        ProcessPopupUtil.addSubmenMenuItems(newSubMenuGrouping, groupingFile, this.aceConfig.getWorker());

        // sub-menu for "concept-contains-desc" and "concept-contains-rel"
        if (!excludesContains) {
            File containsFile = new File(AceFrame.pluginRoot, "refsetspec/branch-popup/contains");
            JMenu newSubMenuContains = new JMenu(containsFile.getName());
            popup.add(newSubMenuContains);
            ProcessPopupUtil.addSubmenMenuItems(newSubMenuContains, containsFile, this.aceConfig.getWorker());
        }

        // sub-menu for concept based clauses e.g. concept is, concept is child of
        if (!excludesConcept) {
            File conceptFile = new File(AceFrame.pluginRoot, "refsetspec/branch-popup/concept");
            JMenu newSubMenuConcept = new JMenu(conceptFile.getName());
            popup.add(newSubMenuConcept);
            ProcessPopupUtil.addSubmenMenuItems(newSubMenuConcept, conceptFile, this.aceConfig.getWorker());
            // sub-menu for diff
            conceptFile = new File(AceFrame.pluginRoot, "refsetspec/branch-popup/diff");
            newSubMenuConcept = new JMenu(conceptFile.getName());
            popup.add(newSubMenuConcept);
            ProcessPopupUtil.addSubmenMenuItems(newSubMenuConcept, conceptFile, this.aceConfig.getWorker());
        }

        // sub-menu for desc based clauses e.g. desc is, desc is child of
        if (!excludesDesc) {
            File descFile = new File(AceFrame.pluginRoot, "refsetspec/branch-popup/desc");
            JMenu newSubMenuDesc = new JMenu(descFile.getName());
            popup.add(newSubMenuDesc);
            ProcessPopupUtil.addSubmenMenuItems(newSubMenuDesc, descFile, this.aceConfig.getWorker());
        }

        // sub-menu for rel based clauses e.g. rel is
        if (!excludesRel) {
            File relFile = new File(AceFrame.pluginRoot, "refsetspec/branch-popup/rel");
            JMenu newSubMenuRel = new JMenu(relFile.getName());
            popup.add(newSubMenuRel);
            ProcessPopupUtil.addSubmenMenuItems(newSubMenuRel, relFile, this.aceConfig.getWorker());
        }

        if (specPart != null) {
            popup.addSeparator();

            boolean uncommitted = false;
            for (I_ExtendByRefPart part : specPart.getMutableParts()) {
                if (part.getVersion() == Integer.MAX_VALUE) {
                    uncommitted = true;
                    break;
                }
            }
            if (uncommitted) {
                JMenuItem cancelActionItem = new JMenuItem("Cancel change");
                cancelActionItem.addActionListener(new CancelChangeAction(specPart));
                popup.add(cancelActionItem);
            } else {
                List<I_ExtendByRefVersion> tuples =
                        (List<I_ExtendByRefVersion>) specPart.getTuples(aceConfig.getAllowedStatus(), aceConfig
                            .getViewPositionSetReadOnly(), aceConfig.getPrecedence(), aceConfig
                            .getConflictResolutionStrategy());

                if (tuples.iterator().hasNext()) {
                    I_ExtendByRefVersion firstTuple = tuples.iterator().next();
                    I_GetConceptData refsetConcept = Terms.get().getConcept(firstTuple.getRefsetId());
                    I_DescriptionTuple refsetDesc =
                            refsetConcept.getDescTuple(aceConfig.getTableDescPreferenceList(), aceConfig);
                    String prompt = "Add comment for '" + refsetDesc.getText() + "'";
                    JMenuItem commentActionItem = new JMenuItem(prompt + "...");
                    commentActionItem.addActionListener(new CommentSpecAction(firstTuple, prompt));
                    popup.add(commentActionItem);
                    popup.addSeparator();
                    JMenuItem retireActionItem = new JMenuItem("Retire");
                    retireActionItem.addActionListener(new RetireSpecAction(firstTuple));
                    popup.add(retireActionItem);

                    JMenuItem changeActionItem = new JMenuItem("Change...");
                    changeActionItem.addActionListener(new ChangeSpecAction(firstTuple));
                    popup.add(changeActionItem);
                } else {
                    tuples =
                            (List<I_ExtendByRefVersion>) specPart.getTuples(null, aceConfig.getViewPositionSetReadOnly(),
                                aceConfig.getPrecedence(), aceConfig.getConflictResolutionStrategy());
                }
            }

        }

        return popup;
    }

    private JPopupMenu makePopup(MouseEvent e, File directory, I_ExtendByRef specPart) throws FileNotFoundException,
            IOException, ClassNotFoundException, TerminologyException {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem noActionItem = new JMenuItem("");
        popup.add(noActionItem);
        ProcessPopupUtil.addSubmenMenuItems(popup, directory, this.aceConfig.getWorker());
        if (specPart != null) {
            popup.addSeparator();

            boolean uncommitted = false;
            for (I_ExtendByRefPart part : specPart.getMutableParts()) {
                if (part.getVersion() == Integer.MAX_VALUE) {
                    uncommitted = true;
                    break;
                }
            }
            if (uncommitted) {
                JMenuItem cancelActionItem = new JMenuItem("Cancel change");
                cancelActionItem.addActionListener(new CancelChangeAction(specPart));
                popup.add(cancelActionItem);
            } else {
                List<I_ExtendByRefVersion> tuples =
                        (List<I_ExtendByRefVersion>) specPart.getTuples(aceConfig.getAllowedStatus(), aceConfig
                            .getViewPositionSetReadOnly(), aceConfig.getPrecedence(), aceConfig
                            .getConflictResolutionStrategy());

                if (tuples.iterator().hasNext()) {
                    I_ExtendByRefVersion firstTuple = tuples.iterator().next();
                    I_GetConceptData refsetConcept = Terms.get().getConcept(firstTuple.getRefsetId());
                    I_DescriptionTuple refsetDesc =
                            refsetConcept.getDescTuple(aceConfig.getTableDescPreferenceList(), aceConfig);
                    String prompt = "Add comment for '" + refsetDesc.getText() + "'";
                    JMenuItem commentActionItem = new JMenuItem(prompt + "...");
                    commentActionItem.addActionListener(new CommentSpecAction(firstTuple, prompt));
                    popup.add(commentActionItem);
                    popup.addSeparator();
                    JMenuItem retireActionItem = new JMenuItem("Retire");
                    retireActionItem.addActionListener(new RetireSpecAction(firstTuple));
                    popup.add(retireActionItem);

                    JMenuItem changeActionItem = new JMenuItem("Change...");
                    changeActionItem.addActionListener(new ChangeSpecAction(firstTuple));
                    popup.add(changeActionItem);
                } else {
                    tuples =
                            (List<I_ExtendByRefVersion>) specPart.getTuples(null, aceConfig.getViewPositionSetReadOnly(),
                                aceConfig.getPrecedence(), aceConfig.getConflictResolutionStrategy());
                }
            }

        }

        return popup;
    }

    private class CommentSpecAction implements ActionListener {
        private I_ExtendByRefVersion thinExtByRefTuple;
        private String prompt;

        private CommentSpecAction(I_ExtendByRefVersion thinExtByRefTuple, String prompt) {
            super();
            this.thinExtByRefTuple = thinExtByRefTuple;
            this.prompt = prompt;
        }

        public void actionPerformed(ActionEvent arg0) {
            String commentText =
                    (String) JOptionPane.showInputDialog(aceConfig.getTreeInSpecEditor().getRootPane(), "", prompt
                        + ":             ", JOptionPane.PLAIN_MESSAGE, null, null, "");
            if (commentText != null && commentText.length() > 2) {
                try {
                    I_GetConceptData refsetIdentityConcept = aceConfig.getRefsetInSpecEditor();
                    I_HelpRefsets refsetHelper = Terms.get().getRefsetHelper(aceConfig);
                    Set<? extends I_GetConceptData> commentRefsets =
                            refsetHelper.getCommentsRefsetForRefset(refsetIdentityConcept, aceConfig);
                    if (commentRefsets.size() > 0) {
                        for (I_GetConceptData commentRefsetIdentityConcept : commentRefsets) {
                            RefsetPropertyMap refsetMap = new RefsetPropertyMap(REFSET_TYPES.STR);
                            refsetMap.put(REFSET_PROPERTY.STRING_VALUE, commentText);
                            I_ExtendByRef newExtension =
                                    refsetHelper.getOrCreateRefsetExtension(commentRefsetIdentityConcept.getNid(),
                                        thinExtByRefTuple.getMemberId(), REFSET_TYPES.STR, refsetMap, UUID.randomUUID());
                            Terms.get().addUncommitted(newExtension);
                        }
                    }
                } catch (Exception e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
                aceConfig.refreshRefsetTab();
            }
        }
    }

    public static void addExtensionsToMap(Collection<? extends I_ExtendByRef> list,
            HashMap<Integer, DefaultMutableTreeNode> extensionMap, HashSet<Integer> fetchedComponents, int refsetNid)
            throws IOException {
        for (I_ExtendByRef ext : list) {
            if (ext.getRefsetId() == refsetNid) {
                extensionMap.put(ext.getMemberId(), new DefaultMutableTreeNode(ext));
                if (!fetchedComponents.contains(ext.getMemberId())) {
                    fetchedComponents.add(ext.getMemberId());
                    addExtensionsToMap(Terms.get().getAllExtensionsForComponent(ext.getMemberId(), true), extensionMap,
                        fetchedComponents, refsetNid);
                }
            }
        }
    }

    private class RetireSpecAction implements ActionListener {
        private I_ExtendByRefVersion thinExtByRefTuple;

        private RetireSpecAction(I_ExtendByRefVersion thinExtByRefTuple) {
            super();
            this.thinExtByRefTuple = thinExtByRefTuple;
        }

        public void actionPerformed(ActionEvent arg0) {
            try {

                I_ExtendByRef clauseBeingRetired = (I_ExtendByRef) thinExtByRefTuple.getCore();
                Collection<? extends I_ExtendByRef> extensions =
                        Terms.get()
                            .getAllExtensionsForComponent(aceConfig.getRefsetSpecInSpecEditor().getConceptNid(), true);

                HashMap<Integer, DefaultMutableTreeNode> extensionMap = new HashMap<Integer, DefaultMutableTreeNode>();
                HashSet<Integer> fetchedComponents = new HashSet<Integer>();
                fetchedComponents.add(aceConfig.getRefsetSpecInSpecEditor().getConceptNid());

                addExtensionsToMap(extensions, extensionMap, fetchedComponents, aceConfig.getRefsetSpecInSpecEditor()
                    .getConceptNid());

                DefaultMutableTreeNode selectedRoot = new DefaultMutableTreeNode(clauseBeingRetired);
                DefaultMutableTreeNode specRoot = new DefaultMutableTreeNode(clauseBeingRetired);
                extensionMap.put(aceConfig.getRefsetSpecInSpecEditor().getConceptNid(), specRoot);

                for (DefaultMutableTreeNode extNode : extensionMap.values()) {
                    I_ExtendByRef ext = (I_ExtendByRef) extNode.getUserObject();
                    if (ext.getComponentNid() == clauseBeingRetired.getMemberId()) {
                        selectedRoot.add(extNode);
                    } else if (ext.getComponentNid() == aceConfig.getRefsetSpecInSpecEditor().getConceptNid()) {
                        // do nothing
                    } else {
                        extensionMap.get(ext.getComponentNid()).add(extNode);
                    }
                }

                int childCount = selectedRoot.getChildCount();
                if (childCount > 0) {
                    // display dialog that alerts the user that there are children clauses which need to be retired

                    Object[] options = { "Retire all", "Cancel" };
                    int n =
                            JOptionPane.showOptionDialog(OpenFrames.getActiveFrame(),
                                "The selected clause has child-clauses. How would you like to proceed?",
                                "Multiple clause retirement", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                options, options[0]);
                    if (n == JOptionPane.YES_OPTION) {
                        try {
                            // retire all children as well as the selected/current clause
                            retireClause(thinExtByRefTuple);

                            retireAllDescendants(selectedRoot);

                        } catch (Exception e) {
                            AceLog.getAppLog().alertAndLogException(e);
                        }
                    } else if (n == JOptionPane.NO_OPTION) {
                        return; // user has opted to cancel the retiring
                    }

                } else {
                    // retire only the clause as it has no children to deal with
                    retireClause(thinExtByRefTuple);
                }

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException();
            } catch (TerminologyException e) {
                e.printStackTrace();
                throw new RuntimeException();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException();
            }

            specEditor.updateSpecTree(false);
            aceConfig.refreshRefsetTab();
        }

        private void retireAllDescendants(DefaultMutableTreeNode selectedRoot) throws Exception {
            int childCount = selectedRoot.getChildCount();
            for (int i = 0; i < childCount; i++) {

                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) selectedRoot.getChildAt(i);
                I_ExtendByRef childClause = (I_ExtendByRef) childNode.getUserObject();
                List<? extends I_ExtendByRefVersion> childExtensions =
                        childClause.getTuples(aceConfig.getAllowedStatus(), aceConfig.getViewPositionSetReadOnly(),
                            aceConfig.getPrecedence(), aceConfig.getConflictResolutionStrategy());
                if (childExtensions.size() > 0) {
                    I_ExtendByRefVersion thinPart = childExtensions.get(0);
                    retireClause(thinPart);
                }

                retireAllDescendants(childNode);
            }
        }

        private void retireClause(I_ExtendByRefVersion currentExtVersion) throws Exception {
            I_ExtendByRefPart currentPart = currentExtVersion.getMutablePart();
            I_ExtendByRefPart newPart =
                    (I_ExtendByRefPart) currentPart.makeAnalog(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
                        currentPart.getPathNid(), Long.MAX_VALUE);
            currentExtVersion.getCore().addVersion(newPart);

            I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
            boolean prevAutoCommit = helper.isAutocommitActive();
            helper.setAutocommitActive(false);
            I_GetConceptData refsetIdentityConcept = aceConfig.getRefsetInSpecEditor();
            RefsetSpec refsetSpec = new RefsetSpec(refsetIdentityConcept, true, aceConfig);
            I_GetConceptData specConcept = refsetSpec.getRefsetSpecConcept();
            I_GetConceptData editTimeConcept = refsetSpec.getEditConcept();

            if (specConcept != null && editTimeConcept != null) {
                helper.newLongRefsetExtension(editTimeConcept.getConceptNid(), specConcept.getConceptNid(), System
                    .currentTimeMillis());
            }
            helper.setAutocommitActive(prevAutoCommit);
            Terms.get().addUncommitted(currentExtVersion.getCore());
        }
    }

    private class CancelChangeAction implements ActionListener {
        private I_ExtendByRef specPart;

        private CancelChangeAction(I_ExtendByRef specPart) {
            super();
            this.specPart = specPart;
        }

        public void actionPerformed(ActionEvent arg0) {
            try {
                Collection<? extends I_ExtendByRef> extensions =
                        Terms.get()
                            .getAllExtensionsForComponent(aceConfig.getRefsetSpecInSpecEditor().getConceptNid(), true);

                HashMap<Integer, DefaultMutableTreeNode> extensionMap = new HashMap<Integer, DefaultMutableTreeNode>();
                HashSet<Integer> fetchedComponents = new HashSet<Integer>();
                fetchedComponents.add(aceConfig.getRefsetSpecInSpecEditor().getConceptNid());

                addExtensionsToMap(extensions, extensionMap, fetchedComponents, aceConfig.getRefsetSpecInSpecEditor()
                    .getConceptNid());

                DefaultMutableTreeNode selectedRoot = new DefaultMutableTreeNode(specPart);
                DefaultMutableTreeNode specRoot = new DefaultMutableTreeNode(specPart);
                extensionMap.put(aceConfig.getRefsetSpecInSpecEditor().getConceptNid(), specRoot);

                for (DefaultMutableTreeNode extNode : extensionMap.values()) {
                    I_ExtendByRef ext = (I_ExtendByRef) extNode.getUserObject();
                    if (ext.getComponentNid() == specPart.getMemberId()) {
                        selectedRoot.add(extNode);
                    } else if (ext.getComponentNid() == aceConfig.getRefsetSpecInSpecEditor().getConceptNid()) {
                        // do nothing
                    } else {
                        extensionMap.get(ext.getComponentNid()).add(extNode);
                    }
                }

                int childCount = selectedRoot.getChildCount();
                if (childCount > 0) {
                    // display dialog that alerts the user that there are children clauses which need to be retired

                    Object[] options = { "Cancel all", "Cancel" };
                    int n =
                            JOptionPane.showOptionDialog(OpenFrames.getActiveFrame(),
                                "The selected clause has child-clauses. How would you like to proceed?",
                                "Multiple clause cancel", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                options, options[0]);
                    if (n == JOptionPane.YES_OPTION) {
                        try {
                            // cancel all children as well as the selected/current clause
                            forgetClause(specPart);

                            forgetAllDescendants(selectedRoot);

                        } catch (Exception e) {
                            AceLog.getAppLog().alertAndLogException(e);
                        }
                    } else if (n == JOptionPane.NO_OPTION) {
                        return; // user has opted to cancel
                    }

                } else {
                    // cancel only the clause as it has no children to deal with
                    forgetClause(specPart);
                }

                specEditor.updateSpecTree(false);
                aceConfig.refreshRefsetTab();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException();
            } catch (TerminologyException e) {
                e.printStackTrace();
                throw new RuntimeException();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }

        private void forgetAllDescendants(DefaultMutableTreeNode selectedRoot) throws Exception {
            int childCount = selectedRoot.getChildCount();
            for (int i = 0; i < childCount; i++) {

                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) selectedRoot.getChildAt(i);
                I_ExtendByRef childClause = (I_ExtendByRef) childNode.getUserObject();
                List<? extends I_ExtendByRefVersion> childExtensions =
                        childClause.getTuples(aceConfig.getAllowedStatus(), aceConfig.getViewPositionSetReadOnly(),
                            aceConfig.getPrecedence(), aceConfig.getConflictResolutionStrategy());
                if (childExtensions.size() > 0) {
                    I_ExtendByRef thinPart = childExtensions.get(0).getCore();
                    forgetClause(thinPart);
                }

                forgetAllDescendants(childNode);
            }
        }

        private void forgetClause(I_ExtendByRef specPart) throws Exception {
            specPart.cancel();
            try {
                if (specPart.isUncommitted()) {
                    Terms.get().forget(specPart);

                    I_GetConceptData refsetIdentityConcept = aceConfig.getRefsetInSpecEditor();
                    RefsetSpec refsetSpec = new RefsetSpec(refsetIdentityConcept, true, aceConfig);
                    refsetSpec.setLastEditTime(System.currentTimeMillis());
                }
                Terms.get().addUncommitted(Terms.get().getConcept(specPart.getRefsetId()));
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (TerminologyException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }
    }

    private class ChangeSpecAction implements ActionListener {
        private I_ExtendByRefVersion thinExtByRefTuple;

        private ChangeSpecAction(I_ExtendByRefVersion thinExtByRefTuple) {
            super();
            this.thinExtByRefTuple = thinExtByRefTuple;
        }

        public void actionPerformed(ActionEvent arg0) {

            I_ExtendByRefPart current = thinExtByRefTuple.getMutablePart();
            I_ExtendByRefPart newPart =
                    (I_ExtendByRefPart) current.makeAnalog(current.getStatusNid(), current.getPathNid(), Long.MAX_VALUE);

            thinExtByRefTuple.getCore().addVersion(newPart);
            Terms.get().addUncommitted(thinExtByRefTuple.getCore());
            specEditor.updateSpecTree(false);
            aceConfig.refreshRefsetTab();
        }
    }

}
