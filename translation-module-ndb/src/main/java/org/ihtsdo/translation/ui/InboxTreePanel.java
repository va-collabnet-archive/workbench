/*
 * Created by JFormDesigner on Mon Mar 15 18:06:53 GMT-03:00 2010
 */

package org.ihtsdo.translation.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.project.workflow.api.WorkflowSearcher;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.tag.InboxTag;
import org.ihtsdo.project.workflow.tag.TagChange;
import org.ihtsdo.project.workflow.tag.TagManager;

/**
 * @author Vahram Manukyan
 */
public class InboxTreePanel extends JPanel implements Observer {
	public static final String INBOX_ITEM_SELECTED = "inboxItem";
	private static final long serialVersionUID = 2726469814051803842L;
	private DefaultTreeModel model;
	private WorkflowSearcher searcher;
	private I_ConfigAceFrame config;
	private Object inboxItem;
	private WorklistItemsWorker worklistItemsWorker;
	private I_GetConceptData user;
	TagManager tagManager;
	private DefaultMutableTreeNode cNode;

	public InboxTreePanel() {
		initComponents();

		// Tag Manager initialization
		tagManager = TagManager.getInstance();
		tagManager.addObserver(this);

		inboxFolderTree.setRootVisible(false);
		inboxFolderTree.setShowsRootHandles(false);
		searcher = new WorkflowSearcher();
		try {
			I_TermFactory tf = Terms.get();
			if (tf != null) {
				config = tf.getActiveAceFrameConfig();
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (config != null) {
			user = config.getDbConfig().getUserConcept();
		}
		updateTree();
	}

	public void setInboxItem(Object newInboxItem) {
		firePropertyChange(InboxTreePanel.INBOX_ITEM_SELECTED, this.inboxItem, newInboxItem);
		this.inboxItem = newInboxItem;
	}

	public Object getInboxItem() {
		return inboxItem;
	}

	private void updateTree() {
		cNode = new DefaultMutableTreeNode(new FolderTreeObj(IconUtilities.CUSTOM_NODE_ROOT, IconUtilities.CUSTOM_NODE, new FolderMetadata(IconUtilities.CUSTOM_NODE, true)));
		DefaultMutableTreeNode iNode = new DefaultMutableTreeNode(new FolderTreeObj(IconUtilities.INBOX_NODE, IconUtilities.INBOX_NODE, new FolderMetadata(IconUtilities.INBOX_NODE, true)));
		DefaultMutableTreeNode sNode = new DefaultMutableTreeNode(new FolderTreeObj(IconUtilities.STATUS_NODE_ROOT, IconUtilities.STATUS_NODE, new FolderMetadata(IconUtilities.STATUS_NODE, true)));
		// updateStatusNode(sNode);
		DefaultMutableTreeNode wNode = new DefaultMutableTreeNode(new FolderTreeObj(IconUtilities.WORKLIST_NODE_ROOT, IconUtilities.WORKLIST_NODE,
				new FolderMetadata(IconUtilities.WORKLIST_NODE, true)));
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();

		model = new DefaultTreeModel(root);
		inboxFolderTree.setModel(model);
		inboxFolderTree.revalidate();
		inboxFolderTree.repaint();
		root.add(iNode);
		root.add(wNode);
		root.add(sNode);
		root.add(cNode);
		inboxFolderTree.expandRow(3);
		inboxFolderTree.expandRow(2);
		inboxFolderTree.expandRow(1);

		model.reload();
		updateWorkflowNodes(wNode, sNode, cNode);
	}

	private void updateWorkflowNodes(DefaultMutableTreeNode wNode, DefaultMutableTreeNode sNode, DefaultMutableTreeNode cNode) {
		if (worklistItemsWorker != null && !worklistItemsWorker.isDone()) {
			worklistItemsWorker.cancel(true);
			worklistItemsWorker = null;
		}
		worklistItemsWorker = new WorklistItemsWorker(inboxFolderTree, wNode, sNode, cNode, config, searcher, model, user);
		worklistItemsWorker.addPropertyChangeListener(new ProgressListener(progressBar));
		worklistItemsWorker.execute();
	}

	public void setTreeModel(DefaultTreeModel treeModel) {
		this.inboxFolderTree.setModel(treeModel);
		this.inboxFolderTree.revalidate();
	}

	private void inboxFolderTreeValueChanged(TreeSelectionEvent e) {
		Object node = inboxFolderTree.getLastSelectedPathComponent();
		if (node instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
			Object userObject = treeNode.getUserObject();
			if (userObject instanceof InboxTreeItem) {
				InboxTreeItem inboxItem = (InboxTreeItem) userObject;
				setInboxItem(inboxItem.getUserObject());
			}
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		scrollPane1 = new JScrollPane();
		inboxFolderTree = new JTree();
		progressBar = new JProgressBar();

		// ======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout) getLayout()).columnWidths = new int[] { 0, 0 };
		((GridBagLayout) getLayout()).rowHeights = new int[] { 0, 0, 0 };
		((GridBagLayout) getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
		((GridBagLayout) getLayout()).rowWeights = new double[] { 1.0, 0.0, 1.0E-4 };

		// ======== scrollPane1 ========
		{

			// ---- inboxFolderTree ----
			inboxFolderTree.addTreeSelectionListener(new TreeSelectionListener() {
				@Override
				public void valueChanged(TreeSelectionEvent e) {
					inboxFolderTreeValueChanged(e);
				}
			});
			scrollPane1.setViewportView(inboxFolderTree);
		}
		add(scrollPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

		// ---- progressBar ----
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);
		add(progressBar, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JScrollPane scrollPane1;
	private JTree inboxFolderTree;
	private JProgressBar progressBar;

	// JFormDesigner - End of variables declaration //GEN-END:variables
	public JTree getTree() {
		return inboxFolderTree;
	}

	public void setTreeMouseListener() {
		// InboxTreeMouselistener inboxTreeMouselistener) {
		// this.tree1.addMouseListener(inboxTreeMouselistener);
	}

	@Override
	public void update(Observable o, Object arg) {
		TagChange change = (TagChange) arg;
		InboxTag newTag = change.getTag();
		if (change.getType().equals(TagChange.NEW_TAG_ADDED)) {
			InboxTreeItem inboxTreeItem = new InboxTreeItem(newTag, newTag.getUuidList().size());
			DefaultMutableTreeNode chldNode = new DefaultMutableTreeNode(inboxTreeItem);
			cNode.add(chldNode);
			model.reload(cNode);
			model.reload(chldNode);
		} else if (change.getType().equals(TagChange.ITEM_TAGGED)) {
			int childCount = cNode.getChildCount();
			for (int i = 0; i < childCount; i++) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode) cNode.getChildAt(i);
				InboxTreeItem treeItem = (InboxTreeItem) child.getUserObject();
				InboxTag tag = (InboxTag) treeItem.getUserObject();
				if (tag.equals(newTag)) {
					if (!newTag.getUuidList().isEmpty()) {
						treeItem.setItemSize(newTag.getUuidList().size());
						treeItem.setUserObject(newTag);
						child.setUserObject(treeItem);
					} else {
						model.removeNodeFromParent(child);
					}
					model.reload(cNode);
					model.reload(child);
					break;
				}
			}
		} else if (change.getType().equals(TagChange.SPECIAL_TAG_ADDED)) {
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) cNode.getParent();
			int childCount = root.getChildCount();
			for (int i = 0; i < childCount; i++) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
				if (child.getUserObject() instanceof InboxTreeItem) {
					InboxTreeItem treeItem = (InboxTreeItem) child.getUserObject();
					InboxTag tag = (InboxTag) treeItem.getUserObject();
					if (tag.equals(newTag)) {
						treeItem.setItemSize(newTag.getUuidList().size());
						treeItem.setUserObject(newTag);
						child.setUserObject(treeItem);
						model.reload(root);
						model.reload(child);
						break;
					}
				}
			}
		} else if (change.getType().equals(TagChange.TAG_REMOVED)) {
			int childCount = cNode.getChildCount();
			for (int i = 0; i < childCount; i++) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode) cNode.getChildAt(i);
				InboxTreeItem treeItem = (InboxTreeItem) child.getUserObject();
				InboxTag tag = (InboxTag) treeItem.getUserObject();
				if (tag.equals(newTag)) {
					if (newTag.getUuidList().isEmpty()) {
						cNode.remove(child);
						model.reload(cNode);
					} else {
						treeItem.setItemSize(newTag.getUuidList().size());
						treeItem.setUserObject(newTag);
						child.setUserObject(treeItem);
						model.reload(child);
					}
					break;
				}
			}
		}
	}

}

class FolderMetadata implements Serializable {
	private static final long serialVersionUID = 1L;
	private boolean pendingRefresh;
	private String folderName;

	public FolderMetadata(String folderName, boolean pendingRefresh) {
		this.folderName = folderName;
		this.pendingRefresh = pendingRefresh;
	}

	public boolean isPendingRefresh() {
		return pendingRefresh;
	}

	public void setPendingRefresh(boolean pendingRefresh) {
		this.pendingRefresh = pendingRefresh;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

}

class MyRenderer extends DefaultTreeCellRenderer {
	private static final long serialVersionUID = 3133934284155240487L;

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		if (node.getUserObject() instanceof InboxTreeItem) {
			setIcon(new ImageIcon(IconUtilities.WORKLIST_NODE));
		}
		return this;
	}

}

class InboxTreeItem {
	private Object userObject;
	private Integer itemSize;

	InboxTreeItem(Object userObject, Integer itemSize) {
		super();
		this.userObject = userObject;
		this.itemSize = itemSize;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setItemSize(Integer itemSize) {
		this.itemSize = itemSize;
	}

	public Integer getItemSize() {
		return itemSize;
	}

	@Override
	public String toString() {
		return userObject.toString() + " (" + itemSize + ")";
	}
}

class WorklistItemsWorker extends SwingWorker<List<InboxTreeItem>, InboxTreeItem> {
	private DefaultMutableTreeNode wNode;
	private DefaultMutableTreeNode sNode;
	private DefaultMutableTreeNode cNode;
	private I_ConfigAceFrame config;
	private WorkflowSearcher searcher;
	private DefaultTreeModel model;
	private I_GetConceptData user;
	private JTree inboxFolderTreee;

	public WorklistItemsWorker(JTree inboxFolderTreee, DefaultMutableTreeNode wNode, DefaultMutableTreeNode sNode, DefaultMutableTreeNode cNode, I_ConfigAceFrame config, WorkflowSearcher searcher,
			DefaultTreeModel model, I_GetConceptData user) {
		super();
		this.wNode = wNode;
		this.sNode = sNode;
		this.cNode = cNode;
		this.config = config;
		this.searcher = searcher;
		this.model = model;
		this.user = user;
		this.inboxFolderTreee = inboxFolderTreee;
	}

	@Override
	public List<InboxTreeItem> doInBackground() {
		try {
			if (config != null) {
				getWorklistsStatusesAndSize();
				getTags();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void getTags() {
		TagManager tm = TagManager.getInstance();
		List<InboxTag> tagsContent = new ArrayList<InboxTag>();
		try {
			tagsContent = tm.getAllTagsContent();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (tagsContent != null && !tagsContent.isEmpty()) {
			for (InboxTag tag : tagsContent) {
				InboxTreeItem inboxItem = new InboxTreeItem(tag, tag.getUuidList().size());
				publish(inboxItem);
			}
		}
	}

	private void getWorklistsStatusesAndSize() {
		try {
			WfUser wfUser;
			wfUser = new WfUser(user.getInitialText(), user.getPrimUuid());
			HashMap<Object, Integer> worklists = searcher.getCountByWorklistAndState(wfUser);
			Set<Object> worklistsAndStates = worklists.keySet();
			for (Object loopObject : worklistsAndStates) {
				InboxTreeItem inboxItem = new InboxTreeItem(loopObject, worklists.get(loopObject));
				publish(inboxItem);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void done() {
		try {
			get();

			TreeNode[] path = wNode.getPath();
			inboxFolderTreee.expandPath(new TreePath(path));

			path = sNode.getPath();
			inboxFolderTreee.expandPath(new TreePath(path));

			path = cNode.getPath();
			inboxFolderTreee.expandPath(new TreePath(path));
		} catch (Exception ignore) {
			ignore.printStackTrace();
		}
	}

	@Override
	protected void process(List<InboxTreeItem> chunks) {
		for (InboxTreeItem inboxTreeItem : chunks) {
			if (inboxTreeItem.getItemSize() > 0) {
				DefaultMutableTreeNode chldNode = new DefaultMutableTreeNode(inboxTreeItem);
				if (inboxTreeItem.getUserObject() instanceof WorkList) {
					wNode.add(chldNode);
				} else if (inboxTreeItem.getUserObject() instanceof WfState) {
					sNode.add(chldNode);
				} else if (inboxTreeItem.getUserObject() instanceof InboxTag) {
					InboxTag tag = (InboxTag) inboxTreeItem.getUserObject();
					if (tag.getTagName().equals("outbox") || tag.getTagName().equals("todo")) {
						DefaultMutableTreeNode root = (DefaultMutableTreeNode) cNode.getParent();
						root.add(chldNode);
						model.reload(root);
						model.reload(chldNode);
					} else {
						cNode.add(chldNode);
					}
				}
				model.reload(wNode);
				model.reload(sNode);
				model.reload(cNode);
			}
		}
	}

};

class ProgressListener implements PropertyChangeListener {
	// Prevent creation without providing a progress bar.
	@SuppressWarnings("unused")
	private ProgressListener() {
	}

	public ProgressListener(JProgressBar progressBar) {
		this.progressBar = progressBar;
		this.progressBar.setVisible(true);
		this.progressBar.setIndeterminate(true);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
			progressBar.setIndeterminate(false);
			progressBar.setVisible(false);
		}
	}

	private JProgressBar progressBar;
}
