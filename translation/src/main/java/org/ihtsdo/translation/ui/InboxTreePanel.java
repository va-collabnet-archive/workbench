/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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

package org.ihtsdo.translation.ui;

import java.awt.Color;
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
import java.util.Set;
import java.util.concurrent.CancellationException;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;
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
import org.ihtsdo.project.filter.WfCompletionFilter;
import org.ihtsdo.project.filter.WfCompletionFilter.CompletionOption;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.project.util.WorkflowSearcher;
import org.ihtsdo.project.view.event.EventMediator;
import org.ihtsdo.project.view.event.GenericEvent.EventType;
import org.ihtsdo.project.view.event.ItemTaggedEvent;
import org.ihtsdo.project.view.event.ItemTaggedEventHandler;
import org.ihtsdo.project.view.event.NewTagEvent;
import org.ihtsdo.project.view.event.NewTagEventHandler;
import org.ihtsdo.project.view.event.OutboxContentChangeEvent;
import org.ihtsdo.project.view.event.OutboxContentChangedEventHandler;
import org.ihtsdo.project.view.event.SendBackToInboxEvent;
import org.ihtsdo.project.view.event.SendBackToInboxEventHandler;
import org.ihtsdo.project.view.event.TagRemovedEvent;
import org.ihtsdo.project.view.event.TagRemovedEventHandler;
import org.ihtsdo.project.view.event.TodoContentChangeEvent;
import org.ihtsdo.project.view.event.TodoContentsChangedEventHandler;
import org.ihtsdo.project.view.tag.InboxTag;
import org.ihtsdo.project.view.tag.TagManager;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.tk.workflow.api.WfFilterBI;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.ui.inbox.event.EmptyInboxItemSelectedEvent;
import org.ihtsdo.translation.ui.inbox.event.InboxItemSelectedEvent;
import org.ihtsdo.translation.ui.inbox.event.ItemDestinationChangedEvent;
import org.ihtsdo.translation.ui.inbox.event.ItemDestinationChangedEventHandler;
import org.ihtsdo.translation.ui.inbox.event.ItemRemovedFromTodoEvent;
import org.ihtsdo.translation.ui.inbox.event.ItemRemovedFromTodoEventHandler;
import org.ihtsdo.translation.ui.inbox.event.ItemSentToSpecialFolderEvent;
import org.ihtsdo.translation.ui.inbox.event.ItemSentToSpecialFolderEventHandler;
import org.ihtsdo.translation.ui.inbox.event.ItemStateChangedEvent;
import org.ihtsdo.translation.ui.inbox.event.ItemStateChangedEventHandler;
import org.ihtsdo.translation.ui.inbox.event.RestFromToDoEvent;
import org.ihtsdo.translation.ui.inbox.event.RestFromToDoEventHandler;

/**
 * The Class InboxTreePanel.
 * 
 * @author Vahram Manukyan
 */
public class InboxTreePanel extends JPanel {

	/** The Constant INBOX_ITEM_SELECTED. */
	public static final String INBOX_ITEM_SELECTED = "inboxItem";

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2726469814051803842L;

	/** The model. */
	private DefaultTreeModel model;

	/** The searcher. */
	private WorkflowSearcher searcher;

	/** The config. */
	private I_ConfigAceFrame config;

	/** The inbox item. */
	private InboxTreeItem inboxItem;

	/** The worklist items worker. */
	private WorklistItemsWorker worklistItemsWorker;

	/** The user. */
	private I_GetConceptData user;

	/** The tag manager. */
	TagManager tagManager;

	/** The c node. */
	private DefaultMutableTreeNode cNode;

	/** The w node. */
	private DefaultMutableTreeNode wNode;

	/** The s node. */
	private DefaultMutableTreeNode sNode;

	/** The i node. */
	private DefaultMutableTreeNode iNode;

	/**
	 * Instantiates a new inbox tree panel.
	 */
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

	/**
	 * Suscribe handlers.
	 */
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
				TreeNode[] path = cNode.getPath();
				inboxFolderTree.expandPath(new TreePath(path));
				model.reload(cNode);
			}
		});

		eventMediator.suscribe(EventType.REST_FROM_TODO_NODE, new RestFromToDoEventHandler<RestFromToDoEvent>(this) {
			@Override
			public void handleEvent(RestFromToDoEvent event) {
				Object userObj = todo.getUserObject();
				InboxTreeItem inboxItem = (InboxTreeItem) userObj;
				int newSize = inboxItem.getItemSize() - 1;
				inboxItem.setItemSize(newSize);
				model.reload(todo);
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
				restFromInboxNode();
			}
		});

		eventMediator.suscribe(EventType.ITEM_SENT_TO_SPECIAL_FOLDER, new ItemSentToSpecialFolderEventHandler<ItemSentToSpecialFolderEvent>(this) {
			@Override
			public void handleEvent(ItemSentToSpecialFolderEvent event) {
				WfInstance wfInstance = event.getWfInstance();
				WfInstance oldInstance = event.getOldInstance();
				WfState oldState = oldInstance.getState();

				restFromStateNode(oldState);
				restFromWorklistNode(wfInstance);
				restFromInboxNode();
			}
		});

		eventMediator.suscribe(EventType.TAG_REMOVED, new TagRemovedEventHandler<TagRemovedEvent>(this) {
			@Override
			public void handleEvent(TagRemovedEvent event) {
				InboxTag newTag = event.getTag();
				int childCount = cNode.getChildCount();
				List<DefaultMutableTreeNode> nodesToRemove = new ArrayList<DefaultMutableTreeNode>();
				if (newTag.getTagName().equals(TagManager.OUTBOX)) {
					reloadNode(newTag, outbox, true, nodesToRemove);
					model.reload(outbox);
				} else if (newTag.getTagName().equals(TagManager.TODO)) {
					reloadNode(newTag, todo, true, nodesToRemove);
					model.reload(todo);
				} else {
					for (int i = 0; i < childCount; i++) {
						DefaultMutableTreeNode child = (DefaultMutableTreeNode) cNode.getChildAt(i);
						reloadNode(newTag, child, false, nodesToRemove);
						model.reload(child);
						childCount = cNode.getChildCount();
					}
				}
				for (DefaultMutableTreeNode defaultMutableTreeNode : nodesToRemove) {
					DefaultMutableTreeNode parent = (DefaultMutableTreeNode) defaultMutableTreeNode.getParent();
					parent.remove(defaultMutableTreeNode);
					model.reload(parent);
				}
				expandAll();
			}

			private void reloadNode(InboxTag newTag, DefaultMutableTreeNode child, boolean specialTag, List<DefaultMutableTreeNode> nodesToRemove) {
				InboxTreeItem treeItem = (InboxTreeItem) child.getUserObject();
				InboxTag tag = (InboxTag) treeItem.getUserObject();
				if (tag.equals(newTag)) {
					if (newTag.getUuidList().isEmpty() && !specialTag) {
						nodesToRemove.add(child);
					} else {
						treeItem.setItemSize(newTag.getUuidList().size());
						treeItem.setUserObject(newTag);
						child.setUserObject(treeItem);
					}
				}
			}
		});
		eventMediator.suscribe(EventType.SEND_BACK_TO_INBOX, new SendBackToInboxEventHandler<SendBackToInboxEvent>(this) {
			@Override
			public void handleEvent(SendBackToInboxEvent event) {
				addToStateNode(event.getNewInstance().getState());
				addToWorklistNode(event.getNewInstance());
				addToInboxNode();
			}
		});

		eventMediator.suscribe(EventType.ITEM_REMOVED_FROM_TODO, new ItemRemovedFromTodoEventHandler<ItemRemovedFromTodoEvent>(this) {
			@Override
			public void handleEvent(ItemRemovedFromTodoEvent event) {
				addToStateNode(event.getWfInstance().getState());
				addToWorklistNode(event.getWfInstance());
				addToInboxNode();
			}
		});
	}

	private void expandAll() {
		TreeNode[] path = wNode.getPath();
		inboxFolderTree.expandPath(new TreePath(path));

		path = sNode.getPath();
		inboxFolderTree.expandPath(new TreePath(path));

		path = cNode.getPath();
		inboxFolderTree.expandPath(new TreePath(path));
		model.reload(iNode);
	}

	/**
	 * Update tree.
	 */
	public void updateTree() {
		totalSize = 0;
		cNode = new DefaultMutableTreeNode(new FolderTreeObj(IconUtilities.CUSTOM_NODE_ROOT, IconUtilities.CUSTOM_NODE, new FolderMetadata(IconUtilities.CUSTOM_NODE, true)));
		iNode = new DefaultMutableTreeNode(new FolderTreeObj(IconUtilities.INBOX_NODE, IconUtilities.INBOX_NODE, new FolderMetadata(IconUtilities.INBOX_NODE, true)));
		sNode = new DefaultMutableTreeNode(new FolderTreeObj(IconUtilities.STATUS_NODE_ROOT, IconUtilities.STATUS_NODE, new FolderMetadata(IconUtilities.STATUS_NODE, true)));
		if (outbox != null) {
			model.removeNodeFromParent(outbox);
		}
		if (todo != null) {
			model.removeNodeFromParent(todo);
		}
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
		model.nodeChanged(sNode);
		model.nodeChanged(wNode);
		model.nodeChanged(cNode);
		updateWorkflowNodes(wNode, sNode, cNode);
	}

	/**
	 * Update workflow nodes.
	 * 
	 * @param wNode
	 *            the w node
	 * @param sNode
	 *            the s node
	 * @param cNode
	 *            the c node
	 */
	private void updateWorkflowNodes(DefaultMutableTreeNode wNode, DefaultMutableTreeNode sNode, DefaultMutableTreeNode cNode) {
		if (worklistItemsWorker != null && !worklistItemsWorker.isDone()) {
			worklistItemsWorker.cancel(true);
			worklistItemsWorker = null;
		}
		worklistItemsWorker = new WorklistItemsWorker();
		worklistItemsWorker.addPropertyChangeListener(new ProgressListener(progressBar));
		worklistItemsWorker.execute();
	}

	/**
	 * Sets the tree model.
	 * 
	 * @param treeModel
	 *            the new tree model
	 */
	public void setTreeModel(DefaultTreeModel treeModel) {
		this.inboxFolderTree.setModel(treeModel);
		this.inboxFolderTree.revalidate();
	}

	/**
	 * Inbox folder tree value changed.
	 * 
	 * @param e
	 *            the e
	 */
	private void inboxFolderTreeValueChanged(TreeSelectionEvent e) {
		try {
			Object node = inboxFolderTree.getLastSelectedPathComponent();
			if (node instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
				Object userObject = treeNode.getUserObject();
				if (userObject instanceof InboxTreeItem) {
					InboxTreeItem inboxItem = (InboxTreeItem) userObject;
					if (inboxItem.getItemSize() > 0) {
						if (this.inboxItem != null) {
							EventMediator.getInstance().fireEvent(new InboxItemSelectedEvent(this.inboxItem.getUserObject(), inboxItem.getUserObject()));
						} else {
							EventMediator.getInstance().fireEvent(new InboxItemSelectedEvent(this.inboxItem, inboxItem.getUserObject()));
						}
					} else {
						EventMediator.getInstance().fireEvent(new EmptyInboxItemSelectedEvent());
					}
					this.inboxItem = inboxItem;
				}
			}
		} catch (Exception ex) {
			if (!(ex instanceof CancellationException)) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Inits the components.
	 */
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
	/** The scroll pane1. */
	private JScrollPane scrollPane1;

	/** The inbox folder tree. */
	private JTree inboxFolderTree;

	/** The progress bar. */
	private JProgressBar progressBar;

	/** The outbox. */
	public DefaultMutableTreeNode outbox;

	/** The todo. */
	public DefaultMutableTreeNode todo;

	/** The todo. */
	public DefaultMutableTreeNode uncommited;

	/** The total size. */
	public Integer totalSize = 0;

	// JFormDesigner - End of variables declaration //GEN-END:variables
	/**
	 * Gets the tree.
	 * 
	 * @return the tree
	 */
	public JTree getTree() {
		return inboxFolderTree;
	}

	/**
	 * Sets the tree mouse listener.
	 */
	public void setTreeMouseListener() {
		// InboxTreeMouselistener inboxTreeMouselistener) {
		// this.tree1.addMouseListener(inboxTreeMouselistener);
	}

	/**
	 * Rest from worklist node.
	 * 
	 * @param wfInst
	 *            the wf inst
	 */
	private void restFromWorklistNode(WfInstance wfInst) {
		try {
			WorkList worklist = wfInst.getWorkList();
			int statusChildCount = wNode.getChildCount();
			List<DefaultMutableTreeNode> childsToRemove = new ArrayList<DefaultMutableTreeNode>();
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
						childsToRemove.add(child);
					}
				}
			}
			for (DefaultMutableTreeNode defaultMutableTreeNode : childsToRemove) {
				int chidlIndex = wNode.getIndex(defaultMutableTreeNode);
				wNode.remove(chidlIndex);
				model.reload(wNode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds the to worklist node.
	 * 
	 * @param wfInstance
	 *            the wf instance
	 */
	protected void addToWorklistNode(WfInstance wfInstance) {
		try {
			WorkList worklist = wfInstance.getWorkList();
			int wlChildCount = wNode.getChildCount();
			boolean wlExists = false;
			for (int i = 0; i < wlChildCount; i++) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode) wNode.getChildAt(i);
				InboxTreeItem childTreeItem = (InboxTreeItem) child.getUserObject();
				WorkList childWorklist = (WorkList) childTreeItem.getUserObject();
				if (childWorklist.getName().equals(worklist.getName())) {
					wlExists = true;
					childTreeItem.setItemSize(childTreeItem.getItemSize() + 1);
					if (childTreeItem.getItemSize() > 0) {
						child.setUserObject(childTreeItem);
						model.reload(child);
					}
				}
			}
			if (!wlExists) {
				InboxTreeItem inboxTreeItem = new InboxTreeItem(worklist, 1, IconUtilities.WORKLIST_NODE);
				DefaultMutableTreeNode chldNode = new DefaultMutableTreeNode(inboxTreeItem);
				wNode.add(chldNode);
				model.reload(wNode);
				inboxFolderTree.expandPath(new TreePath(wNode.getPath()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds the to inbox node.
	 */
	protected void addToInboxNode() {
		InboxTreeItem childTreeItem = (InboxTreeItem) iNode.getUserObject();
		childTreeItem.setItemSize(childTreeItem.getItemSize() + 1);
		iNode.setUserObject(childTreeItem);
		model.reload(iNode);
	}

	/**
	 * Rest from inbox node.
	 */
	protected void restFromInboxNode() {
		InboxTreeItem childTreeItem = (InboxTreeItem) iNode.getUserObject();
		childTreeItem.setItemSize(childTreeItem.getItemSize() - 1);
		iNode.setUserObject(childTreeItem);
		model.reload(iNode);
	}

	/**
	 * Rest from state node.
	 * 
	 * @param state
	 *            the state
	 */
	private void restFromStateNode(WfState state) {
		int statusChildCount = sNode.getChildCount();
		List<DefaultMutableTreeNode> childsToRemove = new ArrayList<DefaultMutableTreeNode>();
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
					childsToRemove.add(child);
				}
			}
		}
		if (!childsToRemove.isEmpty()) {
			for (DefaultMutableTreeNode defaultMutableTreeNode : childsToRemove) {
				int i = model.getIndexOfChild(sNode, defaultMutableTreeNode);
				sNode.remove(i);
				model.reload(sNode);
			}
		}
	}

	/**
	 * Adds the to state node.
	 * 
	 * @param state
	 *            the state
	 */
	private void addToStateNode(WfState state) {
		int statusChildCount = sNode.getChildCount();
		boolean added = false;
		List<DefaultMutableTreeNode> childsToRemove = new ArrayList<DefaultMutableTreeNode>();
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
					childsToRemove.add(child);
				}
				added = true;
			}
		}
		for (DefaultMutableTreeNode defaultMutableTreeNode : childsToRemove) {
			int i = model.getIndexOfChild(sNode, defaultMutableTreeNode);
			sNode.remove(i);
			model.reload(sNode);
		}
		if (!added) {
			DefaultMutableTreeNode chldNode = new DefaultMutableTreeNode(new InboxTreeItem(state, 1, IconUtilities.STATUS_NODE));
			sNode.add(chldNode);
			model.reload(sNode);
			inboxFolderTree.expandPath(new TreePath(sNode.getPath()));
		}
	}

	/**
	 * Item user and state changed.
	 * 
	 * @param oldWfInstance
	 *            the old wf instance
	 * @param newWfInstance
	 *            the new wf instance
	 */
	public void itemUserAndStateChanged(WfInstance oldWfInstance, WfInstance newWfInstance) {
		restFromStateNode(oldWfInstance.getState());
		// addToStateNode(newWfInstance.getState());
		restFromWorklistNode(newWfInstance);
	}

	/**
	 * Item state changed.
	 * 
	 * @param oldWfInstance
	 *            the old wf instance
	 * @param newWfInstance
	 *            the new wf instance
	 */
	public void itemStateChanged(WfInstance oldWfInstance, WfInstance newWfInstance) {
		restFromStateNode(oldWfInstance.getState());
		addToStateNode(newWfInstance.getState());
	}

	/**
	 * The Class WorklistItemsWorker.
	 */
	class WorklistItemsWorker extends SwingWorker<List<InboxTreeItem>, InboxTreeItem> {

		/** The tags content. */
		private List<InboxTag> tagsContent;

		/**
		 * Instantiates a new worklist items worker.
		 */
		public WorklistItemsWorker() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
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

		/**
		 * Gets the tags.
		 * 
		 * @return the tags
		 */
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

		/**
		 * Gets the worklists statuses and size.
		 * 
		 * @return the worklists statuses and size
		 * @throws TerminologyException 
		 */
		private void getWorklistsStatusesAndSize() throws TerminologyException {
			try {
				WfUser wfUser;
				wfUser = new WfUser(user.getInitialText(), user.getPrimUuid());
				ConfigTranslationModule cfg = null;
				cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
				List<WfFilterBI> filters = new ArrayList<WfFilterBI>();
				if(cfg.getCompletionMode() != null && cfg.getCompletionMode().equals(ConfigTranslationModule.CompletionMode.COMPLETE_INSTANCES)){
					filters.add(new WfCompletionFilter(CompletionOption.COMPLETE_INSTANCES));
				}else if(cfg.getCompletionMode() == null || cfg.getCompletionMode().equals(ConfigTranslationModule.CompletionMode.INCOMPLETE_INSTACES)){
					filters.add(new WfCompletionFilter(CompletionOption.INCOMPLETE_INSTACES));
				}
				HashMap<Object, Integer> worklists = searcher.getCountByWorklistAndState(wfUser, filters);
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.SwingWorker#done()
		 */
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
				model.reload(iNode);
			} catch (Exception ignore) {
				ignore.printStackTrace();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.SwingWorker#process(java.util.List)
		 */
		@Override
		protected void process(List<InboxTreeItem> chunks) {
			for (InboxTreeItem inboxTreeItem : chunks) {
				DefaultMutableTreeNode chldNode = new DefaultMutableTreeNode(inboxTreeItem);
				if (inboxTreeItem.getUserObject() instanceof WorkList) {
					if (inboxTreeItem.getItemSize() > 0) {
						totalSize = totalSize + inboxTreeItem.getItemSize();
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
			iNode.setUserObject(new InboxTreeItem(IconUtilities.INBOX_NODE, totalSize, IconUtilities.INBOX_NODE));
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
			if (nodeObject.getUserObject() instanceof InboxTag) {
				setBorder(new LineBorder(Color.WHITE, 1));
			}
			setIcon(IconUtilities.getIconForInboxTree(nodeObject.getIcon()));
		}
		return this;
	}

}
