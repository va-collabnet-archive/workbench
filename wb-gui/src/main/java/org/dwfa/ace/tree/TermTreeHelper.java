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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.dwfa.ace.ACE;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.config.FrameConfigSnapshot;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.thread.NamedThreadFactory;

public class TermTreeHelper implements PropertyChangeListener {
	private static ThreadGroup treeExpansionGroup = new ThreadGroup("Tree expansion ");
    private final Map<TreeIdPath, ExpandNodeSwingWorker> expansionWorkers = new HashMap<TreeIdPath, ExpandNodeSwingWorker>();
    private final ExecutorService treeExpandThread = Executors.newFixedThreadPool(1, new NamedThreadFactory(treeExpansionGroup, 
	"tree expansion "));

    private final ACE ace;
    private JTreeWithDragImage tree;
    private ActivityPanel activity;
    private TermTreeCellRenderer renderer;
    private final I_ConfigAceFrame aceFrameConfig;

    public TermTreeHelper(final I_ConfigAceFrame aceFrameConfig, final ACE ace) {
        super();
        this.ace = ace;
        this.aceFrameConfig = aceFrameConfig;
    }

    public void addTreeSelectionListener(final TreeSelectionListener tsl) {
        this.tree.addTreeSelectionListener(tsl);
    }

    public Map<TreeIdPath, ExpandNodeSwingWorker> getExpansionWorkers() {
        return this.expansionWorkers;
    }

    public JScrollPane getHierarchyPanel() throws TerminologyException, IOException {
        if (this.tree != null) {
            for (final TreeExpansionListener tel : this.tree.getTreeExpansionListeners()) {
                this.tree.removeTreeExpansionListener(tel);
            }
            for (final TreeSelectionListener tsl : this.tree.getTreeSelectionListeners()) {
                this.tree.removeTreeSelectionListener(tsl);
            }
            for (final TreeWillExpandListener twel : this.tree.getTreeWillExpandListeners()) {
                this.tree.removeTreeWillExpandListener(twel);
            }
        }
        this.tree = new JTreeWithDragImage(this.aceFrameConfig, this);
        this.tree.putClientProperty("JTree.lineStyle", "None");
        this.tree.addMouseListener(new TreeMouseListener(this.ace));
        this.tree.setLargeModel(true);
        // tree.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        this.tree.setTransferHandler(new TerminologyTransferHandler(this.tree));
        this.tree.setDragEnabled(false);
        ToolTipManager.sharedInstance().registerComponent(this.tree);
        this.renderer = new TermTreeCellRenderer(this.aceFrameConfig);
        this.tree.setCellRenderer(this.renderer);
        this.tree.setRootVisible(false);
        this.tree.setShowsRootHandles(true);
        final DefaultTreeModel model = this.setRoots();
        /*
         * Since nodes are added dynamically in this application, the only true
         * leaf nodes are nodes that don't allow children to be added. (By
         * default, askAllowsChildren is false and all nodes without children
         * are considered to be leaves.)
         * 
         * But there's a complication: when the tree structure changes, JTree
         * pre-expands the root node unless it's a leaf. To avoid having the
         * root pre-expanded, we set askAllowsChildrenafter assigning the new
         * root.
         */

        model.setAsksAllowsChildren(true);

        this.tree.addTreeExpansionListener(new TreeExpansionListener() {
            public void treeCollapsed(final TreeExpansionEvent evt) {
                TermTreeHelper.this.treeTreeCollapsed(evt, TermTreeHelper.this.aceFrameConfig);
            }

            public void treeExpanded(final TreeExpansionEvent evt) {
                TermTreeHelper.this.treeTreeExpanded(evt);
            }
        });

        this.tree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(final TreeSelectionEvent evt) {
                TermTreeHelper.this.treeValueChanged(evt);
            }

        });
        final JScrollPane treeView = new JScrollPane(this.tree);
        this.tree.setScroller(treeView);
        for (int i = 0; i < this.tree.getRowCount(); i++) {
            final TreePath path = this.tree.getPathForRow(i);
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            final ConceptBeanForTree treeBean = (ConceptBeanForTree) node.getUserObject();
            if (this.aceFrameConfig.getChildrenExpandedNodes().contains(treeBean.getConceptNid())) {
                this.tree.expandPath(new TreePath(node.getPath()));
            }
        }
        return treeView;
    }

    public TermTreeCellRenderer getRenderer() {
        return this.renderer;
    }

    public JTreeWithDragImage getTree() {
        return this.tree;
    }

    public ActivityPanel getTreeActivityPanel() {
        return this.activity;
    }

    public ExecutorService getTreeExpandThread() {
        return this.treeExpandThread;
    }

    public void propertyChange(final PropertyChangeEvent evt) {
        try {
			this.setRoots();
		} catch (final Throwable e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
        this.updateHierarchyView(evt.getPropertyName());
    }

    public void removeTreeSelectionListener(final TreeSelectionListener tsl) {
        this.tree.removeTreeSelectionListener(tsl);
    }

    public DefaultTreeModel setRoots() throws TerminologyException, IOException {
        final DefaultTreeModel model = (DefaultTreeModel) this.tree.getModel();

        final DefaultMutableTreeNode root = new DefaultMutableTreeNode(null, true);

        for (final int rootId : this.aceFrameConfig.getRoots().getSetValues()) {
            root.add(new DefaultMutableTreeNode(ConceptBeanForTree.get(rootId, Integer.MIN_VALUE, 0, false,
                this.aceFrameConfig), true));
        }
        model.setRoot(root);
        return model;
    }

    public void setTreeActivityPanel(final ActivityPanel activity) {
        this.activity = activity;
    }

    public void updateHierarchyView(final String propChangeName) {
        final DefaultTreeModel model = (DefaultTreeModel) this.tree.getModel();
        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        this.stopWorkersOnPath(null, "stopping for change in " + propChangeName);
        for (int i = 0; i < root.getChildCount(); i++) {
            final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) root.getChildAt(i);
            final I_GetConceptData cb = (I_GetConceptData) childNode.getUserObject();
            if (this.aceFrameConfig.getChildrenExpandedNodes().contains(cb.getConceptNid())) {
                final TreePath tp = new TreePath(childNode);
                final TreeExpansionEvent treeEvent = new TreeExpansionEvent(model, tp);
                this.handleCollapse(treeEvent, this.aceFrameConfig);
                this.treeTreeExpanded(treeEvent);
            }
        }
    }

    protected void removeExpansionWorker(final TreeIdPath key, final ExpandNodeSwingWorker worker, final String message) {
        synchronized (this.expansionWorkers) {
            final ExpandNodeSwingWorker foundWorker = this.expansionWorkers.get(key);
            if (worker != null && foundWorker == worker) {
                worker.stopWork(message);
                this.expansionWorkers.remove(key);
            }
        }
    }

    protected void removeStaleExpansionWorker(final TreeIdPath key) {
        synchronized (this.expansionWorkers) {
            final ExpandNodeSwingWorker foundWorker = this.expansionWorkers.get(key);
            if (foundWorker.getContinueWork() == false) {
                this.expansionWorkers.remove(key);
            }
        }
    }

    protected void treeTreeCollapsed(final TreeExpansionEvent evt, final I_ConfigAceFrame aceFrameConfig) {
        final I_GetConceptDataForTree userObject = this.handleCollapse(evt, aceFrameConfig);
        aceFrameConfig.getChildrenExpandedNodes().remove(userObject.getConceptNid());

    }

    protected void treeTreeExpanded(final TreeExpansionEvent evt) {
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) evt.getPath().getLastPathComponent();
        final TreeIdPath idPath = new TreeIdPath(evt.getPath());
        synchronized (this.expansionWorkers) {
            this.stopWorkersOnPath(idPath, "stopping before expansion");
            final I_GetConceptDataForTree userObject = (I_GetConceptDataForTree) node.getUserObject();
            if (userObject != null) {
                this.aceFrameConfig.getChildrenExpandedNodes().add(userObject.getConceptNid());
                final FrameConfigSnapshot configSnap = new FrameConfigSnapshot(this.aceFrameConfig);
                final ExpandNodeSwingWorker worker = new ExpandNodeSwingWorker((DefaultTreeModel) this.tree.getModel(), this.tree,
                    node, new CompareConceptBeansForTree(configSnap), this, configSnap);
                this.treeExpandThread.execute(worker);
                this.expansionWorkers.put(idPath, worker);
            }
        }
    }

    protected void treeValueChanged(final TreeSelectionEvent evt) {
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) evt.getPath().getLastPathComponent();
        if (node != null) {
            final ConceptBeanForTree treeBean = (ConceptBeanForTree) node.getUserObject();
            if (treeBean != null) {
                this.aceFrameConfig.setHierarchySelection(treeBean.getCoreBean());
            }
        } else {
            this.aceFrameConfig.setHierarchySelection(null);
        }
    }

    private I_GetConceptDataForTree handleCollapse(final TreeExpansionEvent evt, final I_ConfigAceFrame aceFrameConfig) {
        final TreeIdPath idPath = new TreeIdPath(evt.getPath());
        this.stopWorkersOnPath(idPath, "stopping for collapse");
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) evt.getPath().getLastPathComponent();
        node.removeAllChildren();
        final I_GetConceptDataForTree userObject = (I_GetConceptDataForTree) node.getUserObject();

        final DefaultTreeModel model = (DefaultTreeModel) this.tree.getModel();

        /*
         * To avoid having JTree re-expand the root node, we disable
         * ask-allows-children when we notify JTree about the new node
         * structure.
         */

        model.setAsksAllowsChildren(false);
        model.nodeStructureChanged(node);
        model.setAsksAllowsChildren(true);

        return userObject;
    }

    private void removeAnyMatchingExpansionWorker(final TreeIdPath key, final String message) {
        synchronized (this.expansionWorkers) {
            final ExpandNodeSwingWorker foundWorker = this.expansionWorkers.get(key);
            if (foundWorker != null) {
                foundWorker.stopWork(message);
                this.expansionWorkers.remove(key);
            }
        }
    }

    private void stopWorkersOnPath(final TreeIdPath idPath, final String message) {
        synchronized (this.expansionWorkers) {
            if (idPath == null) {
                final List<TreeIdPath> allKeys = new ArrayList<TreeIdPath>(this.expansionWorkers.keySet());
                for (final TreeIdPath key : allKeys) {
                    this.removeAnyMatchingExpansionWorker(key, message);
                }
            } else {
                if (this.expansionWorkers.containsKey(idPath)) {
                    this.removeAnyMatchingExpansionWorker(idPath, message);
                }

                final List<TreeIdPath> otherKeys = new ArrayList<TreeIdPath>(this.expansionWorkers.keySet());
                for (final TreeIdPath key : otherKeys) {
                    if (key.initiallyEqual(idPath)) {
                        this.removeAnyMatchingExpansionWorker(key, message);
                    }
                }
            }
        }
    }

}
