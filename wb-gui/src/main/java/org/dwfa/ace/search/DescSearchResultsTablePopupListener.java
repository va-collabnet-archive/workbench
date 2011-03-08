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
package org.dwfa.ace.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.gui.popup.ProcessPopupUtil;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.DescriptionTableModel.StringWithDescTuple;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.ace.tree.ExpandPathToNodeStateListener;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.ace.table.WorkflowHistoryTableModel.WorkflowFSNWithConceptTuple;
import org.ihtsdo.etypes.EConcept;

public class DescSearchResultsTablePopupListener extends MouseAdapter implements ActionListener {

    private I_DescriptionTuple descTuple;
    private I_GetConceptData rowConcept;
    private int selectedRow;
    private I_ConfigAceFrame config;
    private JTable descTable;
    private ACE ace;
	private int panelId;

    DescSearchResultsTablePopupListener(I_ConfigAceFrame config, ACE ace, int panelId) {
        super();
        this.config = config;
        this.ace = ace;
        this.panelId = panelId;
    }

    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            try {
                handlePopup(e);
            } catch (FileNotFoundException e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            } catch (IOException e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            } catch (ClassNotFoundException e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            } catch (TerminologyException e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            }
            e.consume();
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            try {
                handlePopup(e);
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
    }

    private void handlePopup(MouseEvent e) throws FileNotFoundException, IOException, ClassNotFoundException,
            TerminologyException {
        descTable = (JTable) e.getSource();
        if (descTable.getCellRect(descTable.getSelectedRow(), descTable.getSelectedColumn(), true).contains(e.getPoint())) {
            selectedRow = descTable.getSelectedRow();

            Object obj = descTable.getValueAt(selectedRow, 0);
            UUID rowUuid = null;

            if (panelId == I_MakeCriterionPanel.searchPanelId)
            {
            	StringWithDescTuple swdt = (StringWithDescTuple)obj;
            	descTuple = swdt.getTuple();
                rowUuid = Terms.get().getId(descTuple.getDescId()).getUUIDs().iterator().next();
                rowConcept = Terms.get().getConcept(descTuple.getConceptNid());
            } else if (panelId == I_MakeCriterionPanel.workflowHistorySearchPanelId)
            {
		        TableModel wftm = descTable.getModel();
		        if (descTable.getSelectedRow() >= 0)
		        {
	            	WorkflowFSNWithConceptTuple conField = null;

		        	Object value = wftm.getValueAt(descTable.getSelectedRow(), 0);
	            	conField = (WorkflowFSNWithConceptTuple)value;

	            	rowConcept = Terms.get().getConcept(conField.getTuple().getConceptNid());
		        }
            }

            JPopupMenu popup = new JPopupMenu();
            JMenuItem menuItem = new JMenuItem(" ");
            popup.add(menuItem);

            if (ace.getRefsetSpecInSpecEditor() != null && ace.refsetTabIsSelected()) {
                JTree specTree = ace.getTreeInSpecEditor();
                if (specTree.isVisible() && specTree.getSelectionCount() > 0) {
                    TreePath selPath = specTree.getSelectionPath();
                    if (selPath != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                        I_ExtendByRef specPart = (I_ExtendByRef) node.getUserObject();
                        switch (EConcept.REFSET_TYPES.nidToType(specPart.getTypeNid())) {
                        case CID_CID:
                            popup.addSeparator();
                            Collection<? extends I_ExtendByRef> extensions =
                                    Terms.get()
                                        .getRefsetExtensionMembers(config.getRefsetSpecInSpecEditor().getConceptNid());
                            HashMap<Integer, I_ExtendByRef> memberIdBasedExtensionMap =
                                    new HashMap<Integer, I_ExtendByRef>();
                            memberIdBasedExtensionMap = populateMemberIdBasedExtensionMap(extensions);
                            RefsetSpec refsetSpecHelper = new RefsetSpec(config.getRefsetSpecInSpecEditor(), config);
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

                            addRefsetItems(popup, excludeConcept, excludeDesc, excludeRel, excludeContains, rowUuid);
                            break;
                        default:
                        }
                    }
                }
            }

            popup.addSeparator();

            addProcessItems(popup, new File(AceFrame.pluginRoot, "search-results"));
            popup.addSeparator();
            menuItem = new JMenuItem("Show in taxonomy");
            popup.add(menuItem);
            menuItem.addActionListener(this);
            menuItem = new JMenuItem("Put in Concept Tab L-1");
            popup.add(menuItem);
            menuItem.addActionListener(this);
            popup.addSeparator();
            menuItem = new JMenuItem("Put in Concept Tab R-1");
            popup.add(menuItem);
            menuItem.addActionListener(this);
            menuItem = new JMenuItem("Put in Concept Tab R-2");
            popup.add(menuItem);
            menuItem.addActionListener(this);
            menuItem = new JMenuItem("Put in Concept Tab R-3");
            popup.add(menuItem);
            menuItem.addActionListener(this);
            menuItem = new JMenuItem("Put in Concept Tab R-4");
            popup.add(menuItem);
            popup.addSeparator();
            menuItem.addActionListener(this);
            menuItem = new JMenuItem("Add to list");
            menuItem.addActionListener(this);
            popup.add(menuItem);
            popup.show(descTable, e.getX(), e.getY());
            e.consume();
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

    private void addRefsetItems(JPopupMenu popup, boolean excludesConcept, boolean excludesDesc, boolean excludesRel,
            boolean excludesContains, UUID descUuid) throws FileNotFoundException, IOException, ClassNotFoundException {
        // adding grouping clauses (OR, AND, !OR, !AND) - these are always displayed
        File groupingFile = new File(AceFrame.pluginRoot, "refsetspec/branch-popup/grouping");
        JMenu newSubMenuGrouping = new JMenu(groupingFile.getName());
        popup.add(newSubMenuGrouping);
        ProcessPopupUtil.addSubmenMenuItems(newSubMenuGrouping, groupingFile, config.getWorker(), descUuid);

        // sub-menu for "concept-contains-desc" and "concept-contains-rel"
        if (!excludesContains) {
            File containsFile = new File(AceFrame.pluginRoot, "refsetspec/branch-popup/contains");
            JMenu newSubMenuContains = new JMenu(containsFile.getName());
            popup.add(newSubMenuContains);
            ProcessPopupUtil.addSubmenMenuItems(newSubMenuContains, containsFile, config.getWorker(), descUuid);
        }

        // sub-menu for concept based clauses e.g. concept is, concept is child of
        if (!excludesConcept) {
            File conceptFile = new File(AceFrame.pluginRoot, "refsetspec/branch-popup/concept");
            JMenu newSubMenuConcept = new JMenu(conceptFile.getName());
            popup.add(newSubMenuConcept);
            ProcessPopupUtil.addSubmenMenuItems(newSubMenuConcept, conceptFile, config.getWorker(), descUuid);
            // sub-menu for diff
            conceptFile = new File(AceFrame.pluginRoot, "refsetspec/branch-popup/diff");
            newSubMenuConcept = new JMenu(conceptFile.getName());
            popup.add(newSubMenuConcept);
            ProcessPopupUtil.addSubmenMenuItems(newSubMenuConcept, conceptFile, config.getWorker(), descUuid);
        }

        // sub-menu for desc based clauses e.g. desc is, desc is child of
        if (!excludesDesc) {
            File descFile = new File(AceFrame.pluginRoot, "refsetspec/branch-popup/desc");
            JMenu newSubMenuDesc = new JMenu(descFile.getName());
            popup.add(newSubMenuDesc);
            ProcessPopupUtil.addSubmenMenuItems(newSubMenuDesc, descFile, config.getWorker(), descUuid);
        }

        // sub-menu for rel based clauses e.g. rel is
        if (!excludesRel) {
            File relFile = new File(AceFrame.pluginRoot, "refsetspec/branch-popup/rel");
            JMenu newSubMenuRel = new JMenu(relFile.getName());
            popup.add(newSubMenuRel);
            ProcessPopupUtil.addSubmenMenuItems(newSubMenuRel, relFile, config.getWorker(), descUuid);
        }

    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Show in taxonomy")) {
            try {
                AceFrameConfig frameConfig = (AceFrameConfig) config;
                new ExpandPathToNodeStateListener(frameConfig.getAceFrame().getCdePanel().getTree(), config, rowConcept);
                config.setHierarchySelection(rowConcept);
            } catch (IOException e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            } catch (TerminologyException e1) {
                AceLog.getAppLog().alertAndLogException(e1);
			}
        } else if (e.getActionCommand().equals("Put in Concept Tab L-1")) {
            I_HostConceptPlugins viewer = config.getConceptViewer(5);
            viewer.setTermComponent(rowConcept);
        } else if (e.getActionCommand().equals("Put in Concept Tab R-1")) {
            I_HostConceptPlugins viewer = config.getConceptViewer(1);
            viewer.setTermComponent(rowConcept);
        } else if (e.getActionCommand().equals("Put in Concept Tab R-2")) {
            I_HostConceptPlugins viewer = config.getConceptViewer(2);
            viewer.setTermComponent(rowConcept);
        } else if (e.getActionCommand().equals("Put in Concept Tab R-3")) {
            I_HostConceptPlugins viewer = config.getConceptViewer(3);
            viewer.setTermComponent(rowConcept);
        } else if (e.getActionCommand().equals("Put in Concept Tab R-4")) {
            I_HostConceptPlugins viewer = config.getConceptViewer(4);
            viewer.setTermComponent(rowConcept);
        } else if (e.getActionCommand().equals("Add to list")) {
            JList conceptList = config.getBatchConceptList();
            I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
            model.addElement(rowConcept);
        }
    }

    private void addProcessItems(JPopupMenu popup, File directory) {
        try {
            ProcessPopupUtil.addSubmenMenuItems(popup, directory, config.getWorker());
        } catch (FileNotFoundException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (ClassNotFoundException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

}
