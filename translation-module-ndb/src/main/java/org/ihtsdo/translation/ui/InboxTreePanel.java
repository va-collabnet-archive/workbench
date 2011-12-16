/*
 * Created by JFormDesigner on Mon Mar 15 18:06:53 GMT-03:00 2010
 */

package org.ihtsdo.translation.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
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

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.api.WorkflowSearcher;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfUser;

/**
 * @author Vahram Manukyan
 */
public class InboxTreePanel extends JPanel {
	public static final String INBOX_ITEM_SELECTED = "inboxItem";
	private static final long serialVersionUID = 2726469814051803842L;
	private DefaultTreeModel model;
	private WfComponentProvider provider;
	private WorkflowSearcher searcher;
	private I_ConfigAceFrame config;
	private Object inboxItem;
	private WorklistItemsWorker worklistItemsWorker;
	private I_GetConceptData user;

	public InboxTreePanel() {
		initComponents();
		inboxFolderTree.setRootVisible(false);
		inboxFolderTree.setShowsRootHandles(false);
		provider = new WfComponentProvider();
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
		updateDestinationCombo();
	}

	private void updateDestinationCombo() {
		List<WfUser> users = provider.getUsers();
		userCombo.addItem("");
		for (WfUser wfUser : users) {
			userCombo.addItem(wfUser);
		}
	}

	public void setInboxItem(Object newInboxItem) {
		firePropertyChange(InboxTreePanel.INBOX_ITEM_SELECTED, this.inboxItem, newInboxItem);
		this.inboxItem = newInboxItem;
	}

	public Object getInboxItem() {
		return inboxItem;
	}

	private void updateTree() {
		DefaultMutableTreeNode cNode = new DefaultMutableTreeNode(new FolderTreeObj(IconUtilities.CUSTOM_NODE_ROOT, IconUtilities.CUSTOM_NODE, new FolderMetadata(IconUtilities.CUSTOM_NODE, true)));
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
		updateWorkflowNodes(wNode, sNode);
	}

	private void updateWorkflowNodes(DefaultMutableTreeNode wNode, DefaultMutableTreeNode sNode) {
		if (worklistItemsWorker != null && !worklistItemsWorker.isDone()) {
			worklistItemsWorker.cancel(true);
			worklistItemsWorker = null;
		}
		worklistItemsWorker = new WorklistItemsWorker(wNode, sNode, config, searcher, model, user);
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

	private void userComboItemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			Object item = e.getItem();
			if (item instanceof WfUser) {
				WfUser wfUser = (WfUser) item;
				try {
					user = Terms.get().getConcept(wfUser.getId());
				} catch (TerminologyException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				updateTree();
			}
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		label1 = new JLabel();
		userCombo = new JComboBox();
		scrollPane1 = new JScrollPane();
		inboxFolderTree = new JTree();
		progressBar = new JProgressBar();

		// ======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout) getLayout()).columnWidths = new int[] { 0, 0, 0 };
		((GridBagLayout) getLayout()).rowHeights = new int[] { 0, 0, 0, 0 };
		((GridBagLayout) getLayout()).columnWeights = new double[] { 0.0, 1.0, 1.0E-4 };
		((GridBagLayout) getLayout()).rowWeights = new double[] { 0.0, 1.0, 0.0, 1.0E-4 };

		// ---- label1 ----
		label1.setText("Users");
		add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

		// ---- userCombo ----
		userCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				userComboItemStateChanged(e);
			}
		});
		add(userCombo, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

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
		add(scrollPane1, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

		// ---- progressBar ----
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);
		add(progressBar, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JLabel label1;
	private JComboBox userCombo;
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

class WorklistItemsWorker extends SwingWorker<List<InboxTreeItem>, HashMap<DefaultMutableTreeNode, List<InboxTreeItem>>> {
	private DefaultMutableTreeNode wNode;
	private DefaultMutableTreeNode sNode;
	private I_ConfigAceFrame config;
	private WorkflowSearcher searcher;
	private DefaultTreeModel model;
	private I_GetConceptData user;

	public WorklistItemsWorker(DefaultMutableTreeNode wNode, DefaultMutableTreeNode sNode, I_ConfigAceFrame config, WorkflowSearcher searcher, DefaultTreeModel model, I_GetConceptData user) {
		super();
		this.wNode = wNode;
		this.sNode = sNode;
		this.config = config;
		this.searcher = searcher;
		this.model = model;
		this.user = user;
	}

	@Override
	public List<InboxTreeItem> doInBackground() {
		List<InboxTreeItem> inboxTreeItems = new ArrayList<InboxTreeItem>();
		try {
			if (config != null) {
				inboxTreeItems = getWorklistsAndSize();
				List<HashMap<DefaultMutableTreeNode, List<InboxTreeItem>>> chunks = new ArrayList<HashMap<DefaultMutableTreeNode, List<InboxTreeItem>>>();
				HashMap<DefaultMutableTreeNode, List<InboxTreeItem>> partialRes = new HashMap<DefaultMutableTreeNode, List<InboxTreeItem>>();
				partialRes.put(wNode, inboxTreeItems);
				chunks.add(partialRes);
				process(chunks);

				List<InboxTreeItem> statusTreeItems = getStatusNodesAndSize();
				chunks = new ArrayList<HashMap<DefaultMutableTreeNode, List<InboxTreeItem>>>();
				partialRes = new HashMap<DefaultMutableTreeNode, List<InboxTreeItem>>();
				partialRes.put(sNode, statusTreeItems);
				chunks.add(partialRes);
				process(chunks);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return inboxTreeItems;
	}

	private List<InboxTreeItem> getStatusNodesAndSize() {
		List<InboxTreeItem> inboxTreeItems = new ArrayList<InboxTreeItem>();
		try {
			WfUser wfUser;
			wfUser = new WfUser(user.getInitialText(), user.getPrimUuid());
			HashMap<WfState, Integer> worklists = searcher.getUserStatusList(wfUser);
			Set<WfState> states = worklists.keySet();
			inboxTreeItems = new ArrayList<InboxTreeItem>();
			for (WfState state : states) {
				InboxTreeItem inboxItem = new InboxTreeItem(state, worklists.get(state));
				inboxTreeItems.add(inboxItem);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return inboxTreeItems;
	}

	private List<InboxTreeItem> getWorklistsAndSize() throws IOException {
		List<InboxTreeItem> inboxTreeItems;
		WfUser wfUser;
		wfUser = new WfUser(user.getInitialText(), user.getPrimUuid());
		HashMap<WorkList, Integer> worklists = searcher.getUserWorklists(wfUser);
		Set<WorkList> wls = worklists.keySet();
		inboxTreeItems = new ArrayList<InboxTreeItem>();
		for (WorkList workList : wls) {
			InboxTreeItem inboxItem = new InboxTreeItem(workList, worklists.get(workList));
			inboxTreeItems.add(inboxItem);
		}
		return inboxTreeItems;
	}

	@Override
	public void done() {
		List<InboxTreeItem> inboxItems = null;
		try {
			inboxItems = get();
		} catch (Exception ignore) {
			ignore.printStackTrace();
		}
	}

	@Override
	protected void process(List<HashMap<DefaultMutableTreeNode, List<InboxTreeItem>>> chunks) {
		for (HashMap<DefaultMutableTreeNode, List<InboxTreeItem>> hashMap : chunks) {
			Set<DefaultMutableTreeNode> keys = hashMap.keySet();
			for (DefaultMutableTreeNode parentTreeNode : keys) {
				List<InboxTreeItem> childInfo = hashMap.get(parentTreeNode);
				for (InboxTreeItem inboxTreeItem : childInfo) {
					DefaultMutableTreeNode chldNode = new DefaultMutableTreeNode(inboxTreeItem);
					parentTreeNode.add(chldNode);
					model.reload(parentTreeNode);
				}
			}
		}
	}

};

class ProgressListener implements PropertyChangeListener {
	// Prevent creation without providing a progress bar.
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
