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

import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.SwingWorker;
import org.dwfa.tapi.TerminologyException;

public class RefsetViewerPanel extends JPanel {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private class LabelListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            I_GetConceptData refset = (I_GetConceptData) editor.getTermComponent();
            if (refset != null) {
                viewerTreeModel.setRoot(new DefaultMutableTreeNode(refset));
            } else {
                viewerTreeModel.setRoot(new DefaultMutableTreeNode(null));
            }
            updateTree();
            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }

    private JTree viewerTree;
    private RefsetSpecEditor editor;
    private DefaultTreeModel viewerTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode(null));

    public RefsetViewerPanel(I_ConfigAceFrame configAceFrame, RefsetSpecEditor editor) throws TerminologyException,
            IOException {
        super(new GridLayout(1, 1));
        this.editor = editor;
        editor.getLabel().addPropertyChangeListener("termComponent", new LabelListener());

        viewerTree = new JTree(viewerTreeModel);
        viewerTree.setCellRenderer(new RefsetMemberTreeRenderer(configAceFrame));
        viewerTree.setRootVisible(false);
        viewerTree.setShowsRootHandles(true);
        add(new JScrollPane(viewerTree));
    }

    public void updateTree() {
        UpdateTree updater = new UpdateTree();
        updater.start();
    }

    private class UpdateTree extends SwingWorker<DefaultMutableTreeNode> {

        @Override
        protected DefaultMutableTreeNode construct() throws Exception {

            DefaultMutableTreeNode oldRoot = (DefaultMutableTreeNode) viewerTreeModel.getRoot();
            I_GetConceptData refsetConcept = (I_GetConceptData) oldRoot.getUserObject();

            DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode();
            if (refsetConcept != null) {
                DefaultMutableTreeNode committedNodes = new DefaultMutableTreeNode("Committed Members");
                DefaultMutableTreeNode uncommittedNodes = new DefaultMutableTreeNode("Uncommitted Members");
                newRoot.add(committedNodes);
                newRoot.add(uncommittedNodes);
                Collection<? extends I_ExtendByRef> extensions = Terms.get().getRefsetExtensionMembers(
                    refsetConcept.getConceptNid());

                for (I_ExtendByRef ext : extensions) {
                    committedNodes.add(new DefaultMutableTreeNode(ext));
                }
            }
            return newRoot;
        }

        @Override
        protected void finished() {
            try {
                viewerTreeModel.setRoot(get());
            } catch (InterruptedException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (ExecutionException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
            super.finished();
        }

    }

}
