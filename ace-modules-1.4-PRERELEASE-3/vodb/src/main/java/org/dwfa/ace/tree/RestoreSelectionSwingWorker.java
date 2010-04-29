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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;
import org.dwfa.swing.SwingWorker;

public class RestoreSelectionSwingWorker extends SwingWorker<Object> implements ActionListener {

    private JTreeWithDragImage tree;
    private Object lastPropagationId;
    private int horizValue;
    private int vertValue;
    private TreePath selelectionPath;
    private TermTreeHelper helper;

    public RestoreSelectionSwingWorker(JTreeWithDragImage tree, Object lastPropagationId, int horizValue,
            int vertValue, TreePath selelectionPath, TermTreeHelper helper) {
        super();
        this.tree = tree;
        this.helper = helper;
        this.lastPropagationId = lastPropagationId;
        this.horizValue = horizValue;
        this.vertValue = vertValue;
        this.selelectionPath = selelectionPath;
    }

    public RestoreSelectionSwingWorker(RestoreSelectionSwingWorker other) {
        super();
        this.tree = other.tree;
        this.lastPropagationId = other.lastPropagationId;
        this.horizValue = other.horizValue;
        this.vertValue = other.vertValue;
        this.selelectionPath = other.selelectionPath;
        this.helper = other.helper;
    }

    @Override
    protected Object construct() throws Exception {
        return null;
    }

    @Override
    protected void finished() {
        try {
            get();
            if (lastPropagationId.equals(tree.getLastPropagationId())) {
                if (helper.getExpansionWorkers().size() == 0) {
                    AceLog.getAppLog().info("RestoreSelectionSwingWorker resetting selection: " + lastPropagationId);
                    if (selelectionPath != null && selelectionPath.getPathCount() > 0) {
                        Object[] nodesToMatch = selelectionPath.getPath();
                        TreePath pathToSelect = new TreePath(tree.getModel().getRoot());
                        for (int pathNode = 1; pathNode < nodesToMatch.length; pathNode++) {
                            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) pathToSelect.getLastPathComponent();
                            I_GetConceptData nodeToMatchObject = (I_GetConceptData) ((DefaultMutableTreeNode) nodesToMatch[pathNode]).getUserObject();
                            for (int childNode = 0; childNode < parent.getChildCount(); childNode++) {
                                DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(childNode);
                                I_GetConceptData childObject = (I_GetConceptData) child.getUserObject();
                                if (nodeToMatchObject.getConceptId() == childObject.getConceptId()) {
                                    pathToSelect = pathToSelect.pathByAddingChild(child);
                                    break;
                                }
                            }
                        }
                        tree.getSelectionModel().setSelectionPath(pathToSelect);
                    }
                    JScrollPane scroller = tree.getScroller();
                    scroller.getHorizontalScrollBar().setValue(horizValue);
                    scroller.getVerticalScrollBar().setValue(vertValue);
                } else {
                    helper.getTreeExpandThread().execute(new RestoreSelectionSwingWorker(this));
                    AceLog.getAppLog().info("Expansion workers: " + helper.getExpansionWorkers().entrySet());
                    AceLog.getAppLog().info("Adding back RestoreSelectionSwingWorker: " + lastPropagationId);
                    helper.removeStaleExpansionWorker(helper.getExpansionWorkers().keySet().iterator().next());
                }
            } else {
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().fine(
                        "RestoreSelectionSwingWorker ending secondary to inequal propigationId: " + lastPropagationId
                            + " " + tree.getLastPropagationId());
                }
            }
        } catch (InterruptedException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (ExecutionException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().info("RestoreSelectionSwingWorker timer thread finished: " + lastPropagationId);
        }
        helper.getTreeExpandThread().execute(this);
    }
}
