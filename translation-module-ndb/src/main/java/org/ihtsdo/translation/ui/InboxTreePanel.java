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
import java.util.HashMap;
import java.util.List;
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
import org.ihtsdo.project.workflow.event.EventMediator;
import org.ihtsdo.project.workflow.event.GenericEvent.EventType;
import org.ihtsdo.project.workflow.event.ItemTaggedEvent;
import org.ihtsdo.project.workflow.event.ItemTaggedEventHandler;
import org.ihtsdo.project.workflow.event.NewTagEvent;
import org.ihtsdo.project.workflow.event.NewTagEventHandler;
import org.ihtsdo.project.workflow.event.OutboxContentChangeEvent;
import org.ihtsdo.project.workflow.event.OutboxContentChangedEventHandler;
import org.ihtsdo.project.workflow.event.SendBackToInboxEvent;
import org.ihtsdo.project.workflow.event.SendBackToInboxEventHandler;
import org.ihtsdo.project.workflow.event.TagRemovedEvent;
import org.ihtsdo.project.workflow.event.TagRemovedEventHandler;
import org.ihtsdo.project.workflow.event.TodoContentChangeEvent;
import org.ihtsdo.project.workflow.event.TodoContentsChangedEventHandler;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.tag.InboxTag;
import org.ihtsdo.project.workflow.tag.TagManager;
import org.ihtsdo.translation.ui.event.InboxItemSelectedEvent;
import org.ihtsdo.translation.ui.event.ItemDestinationChangedEvent;
import org.ihtsdo.translation.ui.event.ItemDestinationChangedEventHandler;
import org.ihtsdo.translation.ui.event.ItemSentToSpecialFolderEvent;
import org.ihtsdo.translation.ui.event.ItemSentToSpecialFolderEventHandler;
import org.ihtsdo.translation.ui.event.ItemStateChangedEvent;
import org.ihtsdo.translation.ui.event.ItemStateChangedEventHandler;

/**
 * @author Vahram Manukyan
 */
public class InboxTreePanel extends JPanel {
	public static final String INBOX_ITEM_SELECTED = "inboxItem";
	private static final long serialVersionUID = 2726469814051803842L;
	private DefaultTreeModel model;
	private WorkflowSearcher searcher;
	private I_ConfigAceFrame config;
	private InboxTreeItem inboxItem;
	private WorklistItemsWorker worklistItemsWorker;
	private I_GetConceptData user;
	TagManager tagManager;
	private DefaultMutableTreeNode cNode;
	private DefaultMutableTreeNode wNode;
	private DefaultMutableTreeNode sNode;
	private DefaultMutableTreeNode iNode;

	public InboxTreePanel() {
		initComponents();

		// Tag Manager initialization
		tagManager = TagManager.getInstance();

		suscribeHandlers();
		inboxFolderTree.setRootVisible(false);
		inboxFolderTree.setShowsRootHandles(false);
		inboxFolderTree.setCellRenderer(new IconRenderer());
		inboxFolderTree.setRowHeight(20);
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

	private void suscribeHandlers() {
		EventMediator eventMediator = EventMediator.getInstance();
		eventMediator.suscribe(EventType.OUTBOX_CONTENT_CHANGED, new OutboxContentChangedEventHandler<OutboxContentChangeEvent>(this) {
			@Override
			public void handleEvent(OutboxContentChangeEvent event) {
				Integer outboxSize = event.getOutboxSize();
				Object userObj = outbox.getUserObject();
				InboxTreeItem inboxItem = (InboxTreeItem) userObj;
				inboxItem.setItemSize(outboxSize);
				model.reload(outbox);
			}
		});

		eventMediator.suscribe(EventType.TODO_CONTENTS_CHANGED, new TodoContentsChangedEventHandler<TodoContentChangeEvent>(this) {
			@Override
			public void handleEvent(TodoContentChangeEvent event) {
				Integer outboxSize = event.getTodoSize();
				Object userObj = todo.getUserObject();
				InboxTreeItem inboxItem = (InboxTreeItem) userObj;
				inboxItem.setItemSize(outboxSize);
				model.reload(todo);
			}
		});

		eventMediator.suscribe(EventType.NEW_TAG_ADDED, new NewTagEventHandler<NewTagEvent>(this) {
			@Override
			public void handleEvent(NewTagEvent event) {
				InboxTag newTag = event.getTag();
				InboxTreeItem inboxTreeItem = new InboxTreeItem(newTag, newTag.getUuidList().size(), "icons/85.png");
				DefaultMutableTreeNode chldNode = new DefaultMutableTreeNode(inboxTreeItem);
				cNode.add(chldNode);
				model.reload(cNode);
			}
		});

		eventMediator.suscribe(EventType.ITEM_TAGGED, new ItemTaggedEventHandler<ItemTaggedEvent>(this) {
			@Override
			public void handleEvent(ItemTaggedEvent event) {
				InboxTag newTag = event.getTag();
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
						break;
					}
				}
			}
		});
		
		eventMediator.suscribe(EventType.ITEM_STATE_CHANGED, new ItemStateChangedEventHandler<ItemStateChangedEvent>(this) {
			@Override
			public void handleEvent(ItemStateChangedEvent event) {
				WfInstance wfInstance = event.getWfInstance();
				WfState state = wfInstance.getState();
				restFromStateNode(state);
			}
		});
		eventMediator.suscribe(EventType.ITEM_DESTINATION_CHANGED, new ItemDestinationChangedEventHandler<ItemDestinationChangedEvent>(this) {
			@Override
			public void handleEvent(ItemDestinationChangedEvent event) {
				WfInstance wfInstance = event.getWfInstance();
				WfState state = wfInstance.getState();
				restFromStateNode(state);
				restFromWorklistNode(wfInstance);
			}
		});
		
		eventMediator.suscribe(EventType.ITEM_SENT_TO_SPECIAL_FOLDER, new ItemSentToSpecialFolderEventHandler<ItemSentToSpecialFolderEvent>(this) {
			@Override
			public void handleEvent(ItemSentToSpecialFolderEvent event) {
				WfInstance wfInstance = event.getWfInstance();
				WfState state = wfInstance.getState();
				restFromStateNode(state);
				restFromWorklistNode(wfInstance);
			}
		});
		
		eventMediator.suscribe(EventType.TAG_REMOVED, new TagRemovedEventHandler<TagRemovedEvent>(this){
			@Override
			public void handleEvent(TagRemovedEvent event) {
				InboxTag newTag = event.getTag();
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
		});
		eventMediator.suscribe(EventType.SEND_BACK_TO_INBOX, new SendBackToInboxEventHandler<SendBackToInboxEvent>(this) {
			@Override
			public void handleEvent(SendBackToInboxEvent event) {
				restFromStateNode(event.getOldState());
				addToStateNode(event.getNewState());
			}
		});
	}
	
	public void updateTree() {
		cNode = new DefaultMutableTreeNode(new FolderTreeObj(IconUtilities.CUSTOM_NODE_ROOT, IconUtilities.CUSTOM_NODE, new FolderMetadata(IconUtilities.CUSTOM_NODE, true)));
		iNode = new DefaultMutableTreeNode(new FolderTreeObj(IconUtilities.INBOX_NODE, IconUtilities.INBOX_NODE, new FolderMetadata(IconUtilities.INBOX_NODE, true)));
		sNode = new DefaultMutableTreeNode(new FolderTreeObj(IconUtilities.STATUS_NODE_ROOT, IconUtilities.STATUS_NODE, new FolderMetadata(IconUtilities.STATUS_NODE, true)));
		// updateStatusNode(sNode);
		wNode = new DefaultMutableTreeNode(new FolderTreeObj(IconUtilities.WORKLIST_NODE_ROOT, IconUtilities.WORKLIST_NODE, new FolderMetadata(IconUtilities.WORKLIST_NODE, true)));
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
		worklistItemsWorker = new WorklistItemsWorker();
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
				if(this.inboxItem != null){
					EventMediator.getInstance().fireEvent(new InboxItemSelectedEvent(this.inboxItem.getUserObject(), inboxItem.getUserObject()));
				}else{
					EventMediator.getInstance().fireEvent(new InboxItemSelectedEvent(this.inboxItem, inboxItem.getUserObject()));
				}
				this.inboxItem = inboxItem;;
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
	public DefaultMutableTreeNode outbox;
	public DefaultMutableTreeNode todo;

	// JFormDesigner - End of variables declaration //GEN-END:variables
	public JTree getTree() {
		return inboxFolderTree;
	}

	public void setTreeMouseListener() {
		// InboxTreeMouselistener inboxTreeMouselistener) {
		// this.tree1.addMouseListener(inboxTreeMouselistener);
	}

	private void restFromWorklistNode(WfInstance wfInst) {
		try {
			WorkList worklist = wfInst.getWorkList();
			int statusChildCount = wNode.getChildCount();
			for (int i = 0; i < statusChildCount; i++) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode) wNode.getChildAt(i);
				InboxTreeItem childTreeItem = (InboxTreeItem) child.getUserObject();
				WorkList childWorklist = (WorkList) childTreeItem.getUserObject();
				if (childWorklist.getName().equals(worklist.getName())) {
					childTreeItem.setItemSize(childTreeItem.getItemSize() - 1);
					if (childTreeItem.getItemSize() > 0) {
						child.setUserObject(childTreeItem);
						model.reload(child);
					} else {
						wNode.remove(i);
						model.reload(sNode);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void restFromStateNode(WfState state) {
		int statusChildCount = sNode.getChildCount();
		for (int i = 0; i < statusChildCount; i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) sNode.getChildAt(i);
			InboxTreeItem childTreeItem = (InboxTreeItem) child.getUserObject();
			WfState childState = (WfState) childTreeItem.getUserObject();
			if (childState.getName().equals(state.getName())) {
				childTreeItem.setItemSize(childTreeItem.getItemSize() - 1);
				if (childTreeItem.getItemSize() > 0) {
					child.setUserObject(childTreeItem);
					model.reload(child);
				} else {
					sNode.remove(i);
					model.reload(sNode);
				}
			}
		}
	}

	private void addToStateNode(WfState state) {
		int statusChildCount = sNode.getChildCount();
		for (int i = 0; i < statusChildCount; i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) sNode.getChildAt(i);
			InboxTreeItem childTreeItem = (InboxTreeItem) child.getUserObject();
			WfState childState = (WfState) childTreeItem.getUserObject();
			if (childState.getName().equals(state.getName())) {
				childTreeItem.setItemSize(childTreeItem.getItemSize() + 1);
				if (childTreeItem.getItemSize() > 0) {
					child.setUserObject(childTreeItem);
					model.reload(child);
				} else {
					sNode.remove(i);
					model.reload(sNode);
				}
			}
		}
	}

	public void itemUserAndStateChanged(WfInstance oldWfInstance, WfInstance newWfInstance) {
		restFromStateNode(oldWfInstance.getState());
		addToStateNode(newWfInstance.getState());
		restFromWorklistNode(newWfInstance);

	}

	public void itemStateChanged(WfInstance oldWfInstance, WfInstance newWfInstance) {
		restFromStateNode(oldWfInstance.getState());
		addToStateNode(newWfInstance.getState());
	}

	class WorklistItemsWorker extends SwingWorker<List<InboxTreeItem>, InboxTreeItem> {
		private List<InboxTag> tagsContent;

		public WorklistItemsWorker() {
			super();
		}

		@Override
		public List<InboxTreeItem> doInBackground() {
			try {
				if (config != null) {
					getTags();
					getWorklistsStatusesAndSize();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		private void getTags() {
			TagManager tm = TagManager.getInstance();
			try {
				tagsContent = tm.getAllTagsContent();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (tagsContent != null && !tagsContent.isEmpty()) {
				for (InboxTag tag : tagsContent) {
					InboxTreeItem inboxItem = new InboxTreeItem(tag, tag.getUuidList().size(), "icons/85.png");
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
					String icon = "";
					if (loopObject instanceof WfState) {
						icon = IconUtilities.STATUS_NODE;
					} else if (loopObject instanceof WorkList) {
						icon = IconUtilities.WORKLIST_NODE;
					} 

					InboxTreeItem inboxItem = new InboxTreeItem(loopObject, worklists.get(loopObject), icon);
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
				inboxFolderTree.expandPath(new TreePath(path));

				path = sNode.getPath();
				inboxFolderTree.expandPath(new TreePath(path));

				path = cNode.getPath();
				inboxFolderTree.expandPath(new TreePath(path));
			} catch (Exception ignore) {
				ignore.printStackTrace();
			}
		}

		@Override
		protected void process(List<InboxTreeItem> chunks) {
			for (InboxTreeItem inboxTreeItem : chunks) {
				DefaultMutableTreeNode chldNode = new DefaultMutableTreeNode(inboxTreeItem);
				if (inboxTreeItem.getUserObject() instanceof WorkList) {
					if (inboxTreeItem.getItemSize() > 0) {
						wNode.add(chldNode);
					}
				} else if (inboxTreeItem.getUserObject() instanceof WfState) {
					if (inboxTreeItem.getItemSize() > 0) {
						sNode.add(chldNode);
					}
				} else if (inboxTreeItem.getUserObject() instanceof InboxTag) {
					InboxTag tag = (InboxTag) inboxTreeItem.getUserObject();
					if (tag.getTagName().equals(TagManager.OUTBOX) || tag.getTagName().equals(TagManager.TODO)) {
						DefaultMutableTreeNode root = (DefaultMutableTreeNode) cNode.getParent();
						if (tag.getTagName().equals(TagManager.OUTBOX)) {
							outbox = chldNode;
						} else if (tag.getTagName().equals(TagManager.TODO)) {
							todo = chldNode;
						}
						root.add(chldNode);
						model.reload(root);
						model.reload(chldNode);
					} else {
						if (inboxTreeItem.getItemSize() > 0) {
							cNode.add(chldNode);
						}
					}
				}
			}
			model.reload(wNode);
			model.reload(sNode);
			model.reload(cNode);
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
	private String icon;

	InboxTreeItem(Object userObject, Integer itemSize, String icon) {
		super();
		this.userObject = userObject;
		this.itemSize = itemSize;
		this.setIcon(icon);
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

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getIcon() {
		return icon;
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
	private List<InboxTag> tagsContent;

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
				getTags();
				getWorklistsStatusesAndSize();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void getTags() {
		TagManager tm = TagManager.getInstance();
		try {
			tagsContent = tm.getAllTagsContent();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (tagsContent != null && !tagsContent.isEmpty()) {
			for (InboxTag tag : tagsContent) {
				InboxTreeItem inboxItem = new InboxTreeItem(tag, tag.getUuidList().size(), "");
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
				String icon = "";
				if (loopObject instanceof WfState) {
					icon = IconUtilities.STATUS_NODE;
				} else if (loopObject instanceof WorkList) {
					icon = IconUtilities.WORKLIST_NODE;
				} else if (loopObject instanceof InboxTag) {
					icon = "icons/85.png";
				}

				InboxTreeItem inboxItem = new InboxTreeItem(loopObject, worklists.get(loopObject), icon);
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
			DefaultMutableTreeNode chldNode = new DefaultMutableTreeNode(inboxTreeItem);
			if (inboxTreeItem.getUserObject() instanceof WorkList) {
				if (inboxTreeItem.getItemSize() > 0) {
					wNode.add(chldNode);
				}
			} else if (inboxTreeItem.getUserObject() instanceof WfState) {
				if (inboxTreeItem.getItemSize() > 0) {
					sNode.add(chldNode);
				}
			} else if (inboxTreeItem.getUserObject() instanceof InboxTag) {
				InboxTag tag = (InboxTag) inboxTreeItem.getUserObject();
				if (tag.getTagName().equals("outbox") || tag.getTagName().equals("todo")) {
					DefaultMutableTreeNode root = (DefaultMutableTreeNode) cNode.getParent();
					root.add(chldNode);
					model.reload(root);
					model.reload(chldNode);
				} else {
					if (inboxTreeItem.getItemSize() > 0) {
						cNode.add(chldNode);
					}
				}
			}
		}
		model.reload(wNode);
		model.reload(sNode);
		model.reload(cNode);
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

class IconRenderer extends DefaultTreeCellRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		if (node != null && (node.getUserObject() instanceof FolderTreeObj)) {
			FolderTreeObj nodeObject = (FolderTreeObj) node.getUserObject();
			setIcon(IconUtilities.getIconForInboxTree(nodeObject.getObjType()));
			FolderMetadata fMData = (FolderMetadata) nodeObject.getAtrValue();
			setText(fMData.getFolderName());
		} else if (node.getUserObject() instanceof InboxTreeItem) {
			InboxTreeItem nodeObject = (InboxTreeItem) node.getUserObject();
			setIcon(IconUtilities.getIconForInboxTree(nodeObject.getIcon()));
		}
		return this;
	}

}
