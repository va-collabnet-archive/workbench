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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

public class ExpandPathToNodeStateListener implements ChangeListener {

    private JTreeWithDragImage tree;
    private ArrayList<I_GetConceptData> ancestors;
    private TreePath lastPath;
    private DefaultMutableTreeNode lastChildNode;
    private I_GetConceptData focus;

    public ExpandPathToNodeStateListener(JTreeWithDragImage tree, I_ConfigAceFrame config, I_GetConceptData focus)
            throws IOException, TerminologyException {
        super();
        this.tree = tree;
        this.focus = focus;
        if (focus == null) {
            return;
        }
        config.getParentExpandedNodes().clear();
        config.getChildrenExpandedNodes().clear();
        tree.addWorkerFinishedListener(this);
        ancestors = new ArrayList<I_GetConceptData>();
        ancestors.add(focus);
        List<? extends I_RelTuple> rels = focus.getSourceRelTuples(config.getAllowedStatus(), config.getDestRelTypes(),
            config.getViewPositionSetReadOnly(), 
            config.getPrecedence(), config.getConflictResolutionStrategy());
        while (rels.size() > 0) {
            for (I_RelTuple r : rels) {
                I_GetConceptData parent = Terms.get().getConcept(r.getC2Id());
                ancestors.add(0, parent);
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().fine("Adding parent: " + parent);
                }
                rels = parent.getSourceRelTuples(config.getAllowedStatus(), config.getDestRelTypes(),
                    config.getViewPositionSetReadOnly(), 
                    config.getPrecedence(), config.getConflictResolutionStrategy());
                break;
            }
        }

        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            DefaultMutableTreeNode termRoot = (DefaultMutableTreeNode) rootNode.getChildAt(i);
            tree.collapsePath(new TreePath(termRoot.getPath()));
        }
        for (I_GetConceptData child : ancestors) {
            for (int i = 0; i < rootNode.getChildCount(); i++) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
                ConceptBeanForTree dbft = (ConceptBeanForTree) childNode.getUserObject();
                if (dbft.getCoreBean().equals(child)) {
                    lastPath = new TreePath(childNode.getPath());
                    if (child.equals(focus)) {
                        tree.collapsePath(lastPath);
                    } else {
                        tree.expandPath(lastPath);
                    }
                    rootNode = childNode;
                    break;
                }
            }
        }

        // TreePath pathToShow;
        // tree.expandPath(pathToShow);

        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().info("Dropped on JTreeWithDragImage: " + focus);
            AceLog.getAppLog().info("Expansion list: " + ancestors);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (focus == null) {
            return;
        }
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) tree.getModel().getRoot();

        boolean allFound = true;
        for (I_GetConceptData child : ancestors) {
            boolean found = false;
            for (int i = 0; i < rootNode.getChildCount(); i++) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
                ConceptBeanForTree dbft = (ConceptBeanForTree) childNode.getUserObject();
                if (dbft.getCoreBean().equals(child)) {
                    lastChildNode = childNode;
                    lastPath = new TreePath(childNode.getPath());
                    if (dbft.getCoreBean().equals(focus)) {
                        tree.collapsePath(lastPath);
                    } else {
                        tree.expandPath(lastPath);
                    }
                    tree.scrollPathToVisible(lastPath);
                    rootNode = childNode;
                    found = true;
                    break;
                }
            }
            if (found == false) {
                allFound = false;
            }
        }
        if (allFound) {
            tree.removeWorkerFinishedListener(this);
            tree.setSelectionPath(lastPath);
            tree.scrollPathToVisible(lastPath);
            tree.scrollPathToVisible(lastPath);

            int sibCount = 0;
            DefaultMutableTreeNode sibling = lastChildNode.getNextSibling();
            while (sibling != null && sibCount < 4) {
                TreePath siblingPath = new TreePath(sibling.getPath());
                tree.collapsePath(siblingPath);
                tree.scrollPathToVisible(siblingPath);
                sibling = sibling.getNextSibling();
                sibCount++;
            }
        }
    }
}
