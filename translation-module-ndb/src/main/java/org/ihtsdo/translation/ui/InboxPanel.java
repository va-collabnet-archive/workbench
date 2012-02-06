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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.IntrospectionException;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.security.auth.login.LoginException;
import javax.swing.DefaultRowSorter;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.jini.config.ConfigurationException;
import net.jini.core.entry.Entry;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.TransactionException;
import net.jini.lookup.ServiceItemFilter;
import net.jini.lookup.entry.Name;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.EntryID;
import org.dwfa.bpa.process.I_DescribeBusinessProcess;
import org.dwfa.bpa.process.I_DescribeObject;
import org.dwfa.bpa.process.I_DescribeQueueEntry;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_SelectProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.NoMatchingEntryException;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.worker.Worker;
import org.dwfa.queue.ObjectServerCore;
import org.dwfa.queue.SelectAll;
import org.dwfa.queue.bpa.worker.OnDemandOutboxQueueWorker;
import org.dwfa.swing.SwingWorker;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.I_ContextualizeDescription;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.panel.PanelHelperFactory;
import org.ihtsdo.project.panel.TranslationHelperPanel;
import org.ihtsdo.project.refset.PromotionRefset;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.project.workflow.api.WorkflowSearcher;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.ui.ConfigTranslationModule.InboxColumn;

/**
 * The Class InboxPanel.
 *
 * @author Alejandro Rodriguez
 */
public class InboxPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant CUSTOM_NODE_KEY. */
	public static final String CUSTOM_NODE_KEY = "CUSTOM_NODE_KEY";

	/** The Constant FOLDER_TAGS_SEPARATOR. */
	private static final String FOLDER_TAGS_SEPARATOR = "/";
	
	/** The queue name. */
	private String queueName;
	
	/** The selector. */
	private I_SelectProcesses selector;
	
	/** The revision proc selector. */
	private SelectRevisionProcess revisionProcSelector;
	
	/** The worker. */
	private Worker worker;
	
	/** The clone worker. */
	private Worker cloneWorker;
	
	/** The queue. */
	private I_QueueProcesses queue;
	
	/** The fsn. */
	private I_GetConceptData fsn;
	
	/** The preferred. */
	private I_GetConceptData preferred;
	
	/** The inactive. */
	private I_GetConceptData inactive;
	
	/** The closing. */
	private boolean closing;
	
	/** The Entry id bean type. */
	public final String EntryIDBeanType = DataFlavor.javaJVMLocalObjectMimeType + ";class=" + EntryID.class.getName();
	
	/** The w node. */
	private DefaultMutableTreeNode wNode;
	
	/** The i node. */
	private DefaultMutableTreeNode iNode;
	
	/** The s node. */
	private DefaultMutableTreeNode sNode;
	
	/** The c node. */
	private DefaultMutableTreeNode cNode;
	
	/** The hash folders. */
	private HashMap<String, Set<EntryID>> hashFolders;
	
	/** The hash all items. */
	private HashMap<EntryID, QueueTableObj> hashAllItems;
	
	/** The selected folder. */
	private DefaultMutableTreeNode selectedFolder;
	
	/** The c names. */
	private String[] cNames = { "Source Name", "Status" };
	
	/** The col set. */
	private LinkedHashSet<InboxColumn> colSet;
	
	/** The col pos. */
	private ArrayList<InboxColumn> colPos;
	
	/** The l sort keys. */
	private List<SortKey> lSortKeys;
	
	/** The o node. */
	private DefaultMutableTreeNode oNode;
	
	/** The outbox read worker. */
	private I_Work outboxReadWorker;
	
	/** The outbox queue. */
	private I_QueueProcesses outboxQueue;

	/** The worklist hash. */
	private HashMap<Integer, String> worklistHash;

	/** The formatter. */
	private SimpleDateFormat formatter;
	
	/** The revision proc cancel selector. */
	private SelectRevisionProcessCancel revisionProcCancelSelector;

	/**
	 * Instantiates a new inbox panel.
	 *
	 * @param worker the worker
	 * @param queueName the queue name
	 * @param selector the selector
	 * @throws RemoteException the remote exception
	 * @throws TaskFailedException the task failed exception
	 * @throws LeaseDeniedException the lease denied exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 * @throws PrivilegedActionException the privileged action exception
	 * @throws ConfigurationException the configuration exception
	 * @throws TerminologyException the terminology exception
	 */
	public InboxPanel(I_Work worker, String queueName, I_SelectProcesses selector) throws RemoteException, TaskFailedException, LeaseDeniedException, IOException, InterruptedException,
			PrivilegedActionException, ConfigurationException, TerminologyException {

		initComponents();

		label4.setIcon(IconUtilities.helpIcon);
		label4.setText("");

		this.queueName = queueName;
		if (selector == null)
			this.selector = new SelectAll();
		else
			this.selector = selector;

		revisionProcSelector = new SelectRevisionProcess();
		revisionProcCancelSelector = new SelectRevisionProcessCancel();
		this.worker = (Worker) worker;
		try {
			this.cloneWorker = (Worker) worker.getTransactionIndependentClone();
			this.cloneWorker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), Terms.get().getActiveAceFrameConfig());
				
		} catch (LoginException e) {
			e.printStackTrace();
		}
		formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		worklistHash = new HashMap<Integer, String>();

		this.userName = Terms.get().getActiveAceFrameConfig().getUsername();
		fsn = Terms.get().getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
		preferred = Terms.get().getConcept(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());

		inactive = Terms.get().getConcept(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
		closing = false;
		foldTree.setRootVisible(false);
		foldTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		foldTree.setCellRenderer(new IconRenderer());
		foldTree.addMouseListener(new InboxTreeMouselistener(foldTree));
		hashFolders = new HashMap<String, Set<EntryID>>();
		hashAllItems = new HashMap<EntryID, QueueTableObj>();
		setByCode = true;
		itemsTable.setAutoCreateRowSorter(true);
		itemsTable.setShowHorizontalLines(true);
		itemsTable.setShowVerticalLines(false);
		itemsTable.setGridColor(Color.LIGHT_GRAY);
		getFolderTreeModel();
		selectedFolder = iNode;
		refreshItemsTable(selectedFolder);
		setByCode = false;
		itemsTable.addMouseListener(new InboxTableMouselistener(itemsTable));

		JTableHeader header = itemsTable.getTableHeader();

		header.addMouseListener(new ColumnHeaderListener());

		FolderTreeDropTarget ftDropTarget = new FolderTreeDropTarget(foldTree);

		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(itemsTable, DnDConstants.ACTION_COPY_OR_MOVE,
				new TableDragGestureListener(new TableConceptDragSourceListener(), itemsTable));

	}

	// class ItemsRowSorterListener implements RowSorterListener{
	//
	// @Override
	// public void sorterChanged(RowSorterEvent e) {
	// if(e.getType() == RowSorterEvent.Type.SORTED ) {
	// System.out.println("Sorter");
	// } else if(e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED ) {
	// System.out.println("order changed");
	// }
	// List<SortKey> lsor = e.getSource().getSortKeys();
	//
	// for (SortKey l:lsor){
	// System.out.println("after key column:" + l.getSortOrder() + "sortOrder:"
	// + l.getSortOrder().ordinal() + "-" + l.getSortOrder().name());
	//
	// }
	// }
	//
	// }
	/**
	 * The listener interface for receiving columnHeader events.
	 * The class that is interested in processing a columnHeader
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addColumnHeaderListener<code> method. When
	 * the columnHeader event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see ColumnHeaderEvent
	 */
	class ColumnHeaderListener extends MouseAdapter {

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent evt) {
			JTable table = ((JTableHeader) evt.getSource()).getTable();
			DefaultRowSorter rowSorter = (DefaultRowSorter) table.getRowSorter();
			lSortKeys = rowSorter.getSortKeys();
		}
	}

	/**
	 * The listener interface for receiving tableConceptDragSource events.
	 * The class that is interested in processing a tableConceptDragSource
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addTableConceptDragSourceListener<code> method. When
	 * the tableConceptDragSource event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see TableConceptDragSourceEvent
	 */
	class TableConceptDragSourceListener implements DragSourceListener {

		/**
		 * Instantiates a new table concept drag source listener.
		 */
		public TableConceptDragSourceListener() {

		}

		/* (non-Javadoc)
		 * @see java.awt.dnd.DragSourceListener#dragDropEnd(java.awt.dnd.DragSourceDropEvent)
		 */
		public void dragDropEnd(DragSourceDropEvent dsde) {
		}

		/* (non-Javadoc)
		 * @see java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.DragSourceDragEvent)
		 */
		public void dragEnter(DragSourceDragEvent dsde) {
		}

		/* (non-Javadoc)
		 * @see java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent)
		 */
		public void dragExit(DragSourceEvent dse) {
		}

		/* (non-Javadoc)
		 * @see java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent)
		 */
		public void dragOver(DragSourceDragEvent dsde) {
		}

		/* (non-Javadoc)
		 * @see java.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.DragSourceDragEvent)
		 */
		public void dropActionChanged(DragSourceDragEvent dsde) {
		}

	}

	/**
	 * The listener interface for receiving tableDragGesture events.
	 * The class that is interested in processing a tableDragGesture
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addTableDragGestureListener<code> method. When
	 * the tableDragGesture event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see TableDragGestureEvent
	 */
	class TableDragGestureListener implements DragGestureListener {
		
		/** The dsl. */
		DragSourceListener dsl;
		
		/** The j table. */
		JTable jTable;

		/**
		 * Instantiates a new table drag gesture listener.
		 *
		 * @param dsl the dsl
		 * @param jTable the j table
		 */
		public TableDragGestureListener(DragSourceListener dsl, JTable jTable) {

			super();
			this.jTable = jTable;
			this.dsl = dsl;
		}

		/* (non-Javadoc)
		 * @see java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.DragGestureEvent)
		 */
		public void dragGestureRecognized(DragGestureEvent dge) {

			int[] indices = jTable.getSelectedRows();
			DefaultTableModel model = (DefaultTableModel) jTable.getModel();
			try {
				EntryID[] entries = new EntryID[indices.length];
				for (int i = 0; i < indices.length; i++) {

					int index = indices[i];
					int rowModel = jTable.convertRowIndexToModel(index);
					QueueTableObj qto = (QueueTableObj) model.getValueAt(rowModel, 0);

					entries[i] = qto.getEntryId();
				}
				dge.startDrag(DragSource.DefaultMoveDrop, getTransferable(entries), dsl);

			} catch (InvalidDnDOperationException e) {
				AceLog.getAppLog().info(e.toString());
			} catch (Exception ex) {
				AceLog.getAppLog().alertAndLogException(ex);
			}

		}

		/**
		 * Gets the transferable.
		 *
		 * @param entries the entries
		 * @return the transferable
		 * @throws TerminologyException the terminology exception
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		private Transferable getTransferable(EntryID[] entries) throws TerminologyException, IOException {
			return new EntryIDTransferable(entries);
		}

	}

	/**
	 * The Class SelectRevisionProcess.
	 */
	class SelectRevisionProcess implements I_SelectProcesses {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/* (non-Javadoc)
		 * @see org.dwfa.bpa.process.I_SelectProcesses#select(org.dwfa.bpa.process.I_DescribeBusinessProcess)
		 */
		@Override
		public boolean select(I_DescribeBusinessProcess process) {
			String sub = process.getSubject();
			if (sub != null)
				return sub.equals(TranslationHelperPanel.AUTO_PROCESS_WORKLIST_MEMBERS_REVIEW);

			return false;
		}

		/* (non-Javadoc)
		 * @see org.dwfa.bpa.process.I_SelectObjects#select(org.dwfa.bpa.process.I_DescribeObject)
		 */
		@Override
		public boolean select(I_DescribeObject object) {
			I_DescribeBusinessProcess objectBP = (I_DescribeBusinessProcess) object;
			String sub = objectBP.getSubject();
			if (sub != null)
				return sub.equals(TranslationHelperPanel.AUTO_PROCESS_WORKLIST_MEMBERS_REVIEW);

			return false;
		}

	}

	/**
	 * The Class SelectRevisionProcessCancel.
	 */
	class SelectRevisionProcessCancel implements I_SelectProcesses {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/* (non-Javadoc)
		 * @see org.dwfa.bpa.process.I_SelectProcesses#select(org.dwfa.bpa.process.I_DescribeBusinessProcess)
		 */
		@Override
		public boolean select(I_DescribeBusinessProcess process) {
			String sub = process.getSubject();
			if (sub != null)
				return sub.equals(TranslationHelperPanel.AUTO_PROCESS_WORKLIST_MEMBERS_REVIEW_CANCEL);

			return false;
		}

		/* (non-Javadoc)
		 * @see org.dwfa.bpa.process.I_SelectObjects#select(org.dwfa.bpa.process.I_DescribeObject)
		 */
		@Override
		public boolean select(I_DescribeObject object) {
			I_DescribeBusinessProcess objectBP = (I_DescribeBusinessProcess) object;
			String sub = objectBP.getSubject();
			if (sub != null)
				return sub.equals(TranslationHelperPanel.AUTO_PROCESS_WORKLIST_MEMBERS_REVIEW_CANCEL);

			return false;
		}

	}

	/**
	 * The Class EntryIDTransferable.
	 */
	class EntryIDTransferable implements Transferable {

		/** The entry id transferable. */
		EntryID[] entryIDTransferable;

		/** The entry id bean flavor. */
		private DataFlavor entryIDBeanFlavor;

		/** The supported flavors. */
		DataFlavor[] supportedFlavors;

		/**
		 * Instantiates a new entry id transferable.
		 *
		 * @param entries the entries
		 */
		public EntryIDTransferable(EntryID[] entries) {
			super();
			this.entryIDTransferable = entries;

			try {
				entryIDBeanFlavor = new DataFlavor(EntryIDBeanType);
			} catch (ClassNotFoundException e) {
				// should never happen.
				throw new RuntimeException(e);
			}
			supportedFlavors = new DataFlavor[] { entryIDBeanFlavor };
		}

		/* (non-Javadoc)
		 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
		 */
		@SuppressWarnings("deprecation")
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (entryIDTransferable == null) {
				return null;
			}
			if (flavor.equals(EntryIDBeanType)) {
				return entryIDTransferable;
			}
			throw new UnsupportedFlavorException(flavor);
		}

		/* (non-Javadoc)
		 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
		 */
		public DataFlavor[] getTransferDataFlavors() {
			return supportedFlavors;
		}

		/* (non-Javadoc)
		 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
		 */
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			for (DataFlavor f : supportedFlavors) {
				if (f.equals(flavor)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * The Class FolderTreeDropTarget.
	 */
	class FolderTreeDropTarget implements DropTargetListener {
		
		/** The tree. */
		private JTree tree;
		
		/** The entry id bean flavor. */
		private DataFlavor entryIDBeanFlavor;
		
		/** The acceptable type. */
		private boolean acceptableType;

		/**
		 * Instantiates a new folder tree drop target.
		 *
		 * @param tree the tree
		 */
		public FolderTreeDropTarget(JTree tree) {
			this.tree = tree;
			new DropTarget(tree, DnDConstants.ACTION_COPY_OR_MOVE, this);

			try {
				entryIDBeanFlavor = new DataFlavor(EntryIDBeanType);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		// Implementation of the DropTargetListener interface
		/* (non-Javadoc)
		 * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
		 */
		public void dragEnter(DropTargetDragEvent dtde) {
			System.out.println("dragEnter, drop action = " + dtde.getDropAction());
			//
			// // Get the type of object being transferred and determine
			// // whether it is appropriate.
			checkTransferType(dtde);
			//
			// // Accept or reject the drag.
			// acceptOrRejectDrag(dtde);
		}

		/* (non-Javadoc)
		 * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
		 */
		public void dragOver(DropTargetDragEvent dtde) {
			System.out.println("DropTarget dragOver, drop action = " + dtde.getDropAction());

			// Accept or reject the drag
			acceptOrRejectDrag(dtde);
		}

		/* (non-Javadoc)
		 * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
		 */
		public void dropActionChanged(DropTargetDragEvent dtde) {
			System.out.println("DropTarget dropActionChanged, drop action = " + dtde.getDropAction());

			// Accept or reject the drag
			acceptOrRejectDrag(dtde);
		}

		/* (non-Javadoc)
		 * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
		 */
		public void dragExit(DropTargetEvent dte) {
			System.out.println("DropTarget dragExit");
		}

		/* (non-Javadoc)
		 * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
		 */
		public void drop(DropTargetDropEvent dtde) {
			System.out.println("DropTarget drop, drop action = " + dtde.getDropAction());

			// Check the drop action
			if (acceptableType && (dtde.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) != 0) {

				Point p = dtde.getLocation();
				TreePath path = tree.getClosestPathForLocation(p.x, p.y);
				if (path != null) {

					DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) path.getLastPathComponent();

					FolderTreeObj tObj = (FolderTreeObj) targetNode.getUserObject();
					if (tObj != null) {

						if (tObj.getObjType().equals(IconUtilities.CUSTOM_NODE) || tObj.getObjType().equals(IconUtilities.CUSTOM_NODE_ROOT) || tObj.getObjType().equals(IconUtilities.INBOX_NODE)) {

							Transferable transf = dtde.getTransferable();
							Object o = null;
							try {
								o = transf.getTransferData(entryIDBeanFlavor);
								String folderName = ((FolderMetadata) tObj.getAtrValue()).getFolderName();
								boolean result = dropObject(o, folderName);

								dtde.acceptDrop(DnDConstants.ACTION_COPY);
								dtde.dropComplete(result);

								synchronized (this) {
									if (!closing) {
										// if
										// (entId.toString().equals(getEntryID().toString())){

										SwingUtilities.invokeLater(new Runnable() {
											public void run() {
												setupExecuteEnd();
											}

											private void setupExecuteEnd() {
												RefreshServer ros = new RefreshServer();
												ros.start();

											}

										});
									}
								}
								System.out.println("Drop completed, success: " + result);
							} catch (UnsupportedFlavorException e) {
								e.printStackTrace();
								System.out.println("Exception while handling drop " + e);
								dtde.dropComplete(false);
							} catch (IOException e) {
								e.printStackTrace();
								System.out.println("Exception while handling drop " + e);
								dtde.dropComplete(false);
							}

						} else {
							System.out.println("Drop target rejected drop");
							dtde.rejectDrop();
						}
					} else {
						System.out.println("Drop target rejected drop");
						dtde.rejectDrop();
					}
				} else {
					System.out.println("Drop target rejected drop");
					dtde.rejectDrop();
				}
			} else {
				System.out.println("Drop target rejected drop");
				dtde.rejectDrop();
			}
		}

		// Internal methods start here

		/**
		 * Drop object.
		 *
		 * @param o the o
		 * @param folderTarget the folder target
		 * @return true, if successful
		 */
		private boolean dropObject(Object o, String folderTarget) {

			if (o instanceof EntryID[]) {

				EntryID[] entries = (EntryID[]) o;
				FolderTreeObj tObj = (FolderTreeObj) selectedFolder.getUserObject();
				String sourceFolder = ((FolderMetadata) tObj.getAtrValue()).getFolderName();
				if (tObj.getObjType().equals(IconUtilities.STATUS_NODE) || tObj.getObjType().equals(IconUtilities.WORKLIST_NODE)) {
					copyItemsToFolder(entries, sourceFolder, folderTarget);
				} else if (tObj.getObjType().equals(IconUtilities.OUTBOX_NODE)) {
					moveItemsFromOutbox(entries, folderTarget);
				} else {
					moveItemsToFolder(entries, sourceFolder, folderTarget);
				}
			}

			return true;
		}

		/**
		 * Accept or reject drag.
		 *
		 * @param dtde the dtde
		 * @return true, if successful
		 */
		protected boolean acceptOrRejectDrag(DropTargetDragEvent dtde) {
			int dropAction = dtde.getDropAction();
			int sourceActions = dtde.getSourceActions();
			boolean acceptedDrag = false;
			System.out.println("\tSource actions are " + sourceActions + ", drop action is " + dropAction);

			if (!acceptableType || (sourceActions & DnDConstants.ACTION_COPY_OR_MOVE) == 0) {
				System.out.println("Drop target rejecting drag");
				dtde.rejectDrag();
			} else {

				Point p = dtde.getLocation();
				TreePath path = tree.getClosestPathForLocation(p.x, p.y);
				DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) path.getLastPathComponent();
				FolderTreeObj tObj = (FolderTreeObj) targetNode.getUserObject();
				if (tObj == null) {
					dtde.rejectDrag();
				} else {
					if (tObj.getObjType().equals(IconUtilities.CUSTOM_NODE) || tObj.getObjType().equals(IconUtilities.CUSTOM_NODE_ROOT) || tObj.getObjType().equals(IconUtilities.INBOX_NODE)) {
						System.out.println("Drop target offering COPY");
						dtde.acceptDrag(DnDConstants.ACTION_COPY);
						acceptedDrag = true;

					}
				}
			}
			return acceptedDrag;
		}

		/**
		 * Check transfer type.
		 *
		 * @param dtde the dtde
		 */
		protected void checkTransferType(DropTargetDragEvent dtde) {
			acceptableType = dtde.isDataFlavorSupported(entryIDBeanFlavor);
			System.out.println("File type acceptable - " + acceptableType);
		}
	}

	/**
	 * Refresh items table.
	 *
	 * @param selectedFolder the selected folder
	 */
	synchronized private void refreshItemsTable(DefaultMutableTreeNode selectedFolder) {

		FolderTreeObj tObj = (FolderTreeObj) selectedFolder.getUserObject();
		if (tObj.getObjType().equals(IconUtilities.OUTBOX_NODE))
			try {
				getOutboxItems();
			} catch (TaskFailedException e1) {
				e1.printStackTrace();
			} catch (LoginException e1) {
				e1.printStackTrace();
			} catch (TerminologyException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (ConfigurationException e1) {
				e1.printStackTrace();
			} catch (PrivilegedActionException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

		String[] columnNames = getColumnHeaders();

		String[][] data = null;
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};
		Set<QueueTableObj> queueTO = new HashSet<QueueTableObj>();
		if (tObj != null) {
			String folderName = ((FolderMetadata) tObj.getAtrValue()).getFolderName();
			Set<EntryID> foldEntries = hashFolders.get(folderName);
			for (EntryID entryId : foldEntries) {
				queueTO.add(hashAllItems.get(entryId));
			}

		}
		setTableModelData(tableModel, queueTO);

		itemsTable.setModel(tableModel);
		if (lSortKeys == null) {
			lSortKeys = new ArrayList<SortKey>();
			lSortKeys.add(new SortKey(0, SortOrder.ASCENDING));
		}

		itemsTable.getRowSorter().setSortKeys(lSortKeys);
		itemsTable.revalidate();

	}

	/**
	 * Copy items to folder.
	 *
	 * @param entries the entries
	 * @param sourceFolder the source folder
	 * @param folderTarget the folder target
	 */
	synchronized public void copyItemsToFolder(EntryID[] entries, String sourceFolder, String folderTarget) {
		Set<EntryID> foldEntries = hashFolders.get(folderTarget);

		for (EntryID entryId : entries) {
			foldEntries.add(entryId);
			AddTagToProcess(entryId, folderTarget);
		}

	}

	/**
	 * Adds the tag to process.
	 *
	 * @param entryID the entry id
	 * @param folderTarget the folder target
	 */
	private void AddTagToProcess(EntryID entryID, String folderTarget) {
		I_EncodeBusinessProcess process = null;
		try {
			QueueTableObj qto = (QueueTableObj) hashAllItems.get(entryID);
			Set<String> tags = qto.getTagsArray();
			if (tags == null) {
				tags = new HashSet<String>();
			}
			tags.add(folderTarget);
			process = queue.take(entryID, cloneWorker.getActiveTransaction());

			String[] parsedSubj = TerminologyProjectDAO.getParsedItemSubject(process.getSubject());
			parsedSubj[TerminologyProjectDAO.subjectIndexes.TAGS_ARRAY.ordinal()] = getTagString(tags);
			process.setSubject(TerminologyProjectDAO.getSubjectFromArray(parsedSubj));

			// process.writeAttachment(CUSTOM_NODE_KEY, tags);
			process.setOriginator(this.userName);
			queue.write(process, cloneWorker.getActiveTransaction());
			cloneWorker.commitTransactionIfActive();
			// qto.setTagsArray(tags);
			// hashAllItems.put(entryID, qto);

		} catch (RemoteException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		} catch (NoMatchingEntryException e) {

			e.printStackTrace();
		} catch (LeaseDeniedException e) {

			e.printStackTrace();
		} catch (TransactionException e) {

			e.printStackTrace();
		} catch (InterruptedException e) {

			e.printStackTrace();
		} catch (PrivilegedActionException e) {

			e.printStackTrace();
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}

	}

	// public void copyAllItemsToFolder(String sourceFolder, String
	// folderTarget) {
	// Set<EntryID> foldEntries = hashFolders.get(folderTarget);
	// Set<EntryID> custFoldEntries=hashFolders.get( sourceFolder);
	//
	// for (EntryID entryId:custFoldEntries){
	// foldEntries.add(entryId);
	// AddTagToProcess(entryId, folderTarget);
	// }
	//
	// }

	/**
	 * Sets the table model data.
	 *
	 * @param tableModel the table model
	 * @param queueTO the queue to
	 */
	private void setTableModelData(DefaultTableModel tableModel, Set<QueueTableObj> queueTO) {
		int rowLen = cNames.length;
		for (QueueTableObj qTO : queueTO) {
			Object[] row = new Object[rowLen];
			row[0] = qTO;
			for (int column = 1; column < rowLen; column++) {

				InboxColumn iCol = colPos.get(column - 1);

				switch (iCol) {
				case SOURCE_PREFERRED:
					row[column] = qTO.getSourcePref();
					break;
				case STATUS:
					row[column] = qTO.getStatus();
					break;
//				case STATUS_DATE:
//					row[column] = formatter.format(qTO.getStatusTime());
//					break;
				case TARGET_FSN:
					row[column] = qTO.getTargetFSN();
					break;
				case TARGET_PREFERRED:
					row[column] = qTO.getTargetPref();
					break;
				}
			}
			tableModel.addRow(row);
		}
	}

	/**
	 * Gets the column headers.
	 *
	 * @return the column headers
	 */
	private String[] getColumnHeaders() {
		ConfigTranslationModule cfg = null;
		try {
			cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		inboxItemCheckbox.setSelected(cfg.isAutoOpenNextInboxItem());
		colSet = new LinkedHashSet<InboxColumn>();
		colSet.add(InboxColumn.STATUS);
		colPos = new ArrayList<InboxColumn>();
		if (cfg != null) {
			colSet = cfg.getColumnsDisplayedInInbox();
			List<String> colName = new ArrayList<String>();

			if (colSet != null && colSet.size() > 0) {
				cNames = new String[colSet.size() + 1];
				colName.add("Source FSN");
				for (InboxColumn iCol : colSet) {
					colName.add(iCol.getColumnName());
					colPos.add(iCol);

				}
				colName.toArray(cNames);
			} else {
				colPos.add(InboxColumn.STATUS);
			}
		} else {
			colPos.add(InboxColumn.STATUS);
		}
		return cNames;
	}

	/**
	 * The Class IconRenderer.
	 */
	class IconRenderer extends DefaultTreeCellRenderer {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/* (non-Javadoc)
		 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
		 */
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			if (node != null && (node.getUserObject() instanceof FolderTreeObj)) {
				FolderTreeObj nodeObject = (FolderTreeObj) node.getUserObject();

				setIcon(IconUtilities.getIconForInboxTree(nodeObject.getObjType()));
				FolderMetadata fMData = (FolderMetadata) nodeObject.getAtrValue();
				Set<EntryID> setEntries = hashFolders.get(fMData.getFolderName());
				int esize = setEntries.size();
				if (esize > 0) {
					setText(fMData.getFolderName() + " (" + esize + ")");
					// setBounds(this.getX(), this.getY(), tree.getWidth()-10,
					// this.getHeight());
					// repaint();
					// revalidate();
				}
			}
			return this;
		}

	}

	/**
	 * Gets the folder tree model.
	 *
	 * @return the folder tree model
	 * @throws RemoteException the remote exception
	 * @throws TaskFailedException the task failed exception
	 * @throws LeaseDeniedException the lease denied exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 * @throws PrivilegedActionException the privileged action exception
	 * @throws ConfigurationException the configuration exception
	 * @throws TerminologyException the terminology exception
	 */
	synchronized private void getFolderTreeModel() throws RemoteException, TaskFailedException, LeaseDeniedException, IOException, InterruptedException, PrivilegedActionException,
			ConfigurationException, TerminologyException {

		cNode = new DefaultMutableTreeNode(new FolderTreeObj(IconUtilities.CUSTOM_NODE_ROOT, IconUtilities.CUSTOM_NODE, new FolderMetadata(IconUtilities.CUSTOM_NODE, true)));

		Set<EntryID> foldEntries = new HashSet<EntryID>();
		hashFolders.put(IconUtilities.CUSTOM_NODE, foldEntries);

		getCustomNodeConfig();

		iNode = new DefaultMutableTreeNode(new FolderTreeObj(IconUtilities.INBOX_NODE, IconUtilities.INBOX_NODE, new FolderMetadata(IconUtilities.INBOX_NODE, true)));

		foldEntries = new HashSet<EntryID>();
		hashFolders.put(IconUtilities.INBOX_NODE, foldEntries);

		sNode = new DefaultMutableTreeNode(new FolderTreeObj(IconUtilities.STATUS_NODE_ROOT, IconUtilities.STATUS_NODE, new FolderMetadata(IconUtilities.STATUS_NODE, true)));

		foldEntries = new HashSet<EntryID>();
		hashFolders.put(IconUtilities.STATUS_NODE, foldEntries);

		wNode = new DefaultMutableTreeNode(new FolderTreeObj(IconUtilities.WORKLIST_NODE_ROOT, IconUtilities.WORKLIST_NODE, new FolderMetadata(IconUtilities.WORKLIST_NODE, true)));

		foldEntries = new HashSet<EntryID>();
		hashFolders.put(IconUtilities.WORKLIST_NODE, foldEntries);

		loadQueueItems();

		oNode = new DefaultMutableTreeNode(new FolderTreeObj(IconUtilities.OUTBOX_NODE, IconUtilities.OUTBOX_NODE, new FolderMetadata(IconUtilities.OUTBOX_NODE, true)));

		foldEntries = new HashSet<EntryID>();
		hashFolders.put(IconUtilities.OUTBOX_NODE, foldEntries);

		try {
			getOutboxItems();
		} catch (LoginException e) {
			e.printStackTrace();
		}
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();

		root.add(iNode);
		root.add(wNode);
		root.add(sNode);
		root.add(cNode);
		root.add(oNode);
		DefaultTreeModel tModel = new DefaultTreeModel(root);

		foldTree.setModel(tModel);
		foldTree.expandRow(3);
		foldTree.expandRow(2);
		foldTree.expandRow(1);
		if (selectedFolder == null) {
			foldTree.setSelectionPath(new TreePath(iNode.getPath()));
			// String
			// text=foldTree.convertValueToText(foldTree.getLastSelectedPathComponent(),
			// true, true, true, 0, true);
			// if (text!=null)
			// System.out.println(text);
			// DefaultTreeModel model=foldTree.getModel();
			// model.g
		} else {
			// DefaultMutableTreeNode nodetp = (DefaultMutableTreeNode)
			// tp.getLastPathComponent();
			FolderTreeObj fto = (FolderTreeObj) selectedFolder.getUserObject();
			setSelectedFolder(fto.getAtrName());

			// foldTree.setSelectionPath(tp);
		}
		foldTree.validate();
		foldTree.revalidate();
		foldTree.repaint();
	}

	/**
	 * Sets the selected folder.
	 *
	 * @param atrName the new selected folder
	 */
	private void setSelectedFolder(String atrName) {
		boolean testPath = false;
		TreePath tp = foldTree.getNextMatch(atrName, 0, Position.Bias.Forward);
		if (tp != null)
			testPath = testNode(atrName, (DefaultMutableTreeNode) tp.getLastPathComponent());

		while (!testPath && tp != null) {
			tp = foldTree.getNextMatch(atrName, foldTree.getRowForPath(tp) + 1, Position.Bias.Forward);
			if (tp != null)
				testPath = testNode(atrName, (DefaultMutableTreeNode) tp.getLastPathComponent());

		}
		if (testPath) {
			foldTree.setExpandsSelectedPaths(true);
			foldTree.setSelectionPath(tp);
		}
	}

	/**
	 * Test node.
	 *
	 * @param atrName the atr name
	 * @param node the node
	 * @return true, if successful
	 */
	private boolean testNode(String atrName, DefaultMutableTreeNode node) {

		FolderTreeObj foldObj1 = (FolderTreeObj) node.getUserObject();
		if (atrName.equals(foldObj1.getAtrName()))
			return true;

		return false;
	}

//	private boolean testPaths(DefaultMutableTreeNode[] nodes1, DefaultMutableTreeNode[] nodes2) {
//		if (nodes1.length != nodes2.length)
//			return false;
//		int nodesCount = nodes1.length;
//		for (int i = 0; i < nodesCount; i++) {
//			if (nodes1[i] != null && nodes2[i] != null) {
//
//				FolderTreeObj foldObj1 = (FolderTreeObj) nodes1[i].getUserObject();
//				FolderMetadata fMData1 = (FolderMetadata) foldObj1.getAtrValue();
//				FolderTreeObj foldObj2 = (FolderTreeObj) nodes2[i].getUserObject();
//				FolderMetadata fMData2 = (FolderMetadata) foldObj2.getAtrValue();
//
//				if (!fMData1.getFolderName().equals(fMData2.getFolderName()))
//					return false;
//
//			} else if (nodes1[i] != nodes2[i]) {
//				return false;
//			}
//		}
//		return true;
//	}

	/**
 * Gets the outbox items.
 *
 * @return the outbox items
 * @throws TerminologyException the terminology exception
 * @throws IOException Signals that an I/O exception has occurred.
 * @throws TaskFailedException the task failed exception
 * @throws LoginException the login exception
 * @throws ConfigurationException the configuration exception
 * @throws PrivilegedActionException the privileged action exception
 * @throws InterruptedException the interrupted exception
 */
private void getOutboxItems() throws TerminologyException, IOException, TaskFailedException, LoginException, ConfigurationException, PrivilegedActionException, InterruptedException {

		ConfigTranslationModule cfg = null;
		try {
			cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		colSet = cfg.getColumnsDisplayedInInbox();
		boolean targetFSNCol = false;
		boolean targetPrefCol = false;
		for (InboxColumn iCol : colSet) {

			switch (iCol) {
			case TARGET_FSN:
				targetFSNCol = true;
				break;
			case TARGET_PREFERRED:
				targetPrefCol = true;
				break;
			}
		}
		I_ConfigAceFrame config;
		config = Terms.get().getActiveAceFrameConfig();
		String outboxQueueName = config.getUsername() + ".outbox";
		ServiceID serviceID = null;
		Class<?>[] serviceTypes = new Class[] { I_QueueProcesses.class };
		Entry[] attrSetTemplates = new Entry[] { new Name(outboxQueueName) };
		ServiceTemplate template = new ServiceTemplate(serviceID, serviceTypes, attrSetTemplates);
		ServiceItemFilter filter = null;
		if (outboxReadWorker == null){
			outboxReadWorker = worker.getTransactionIndependentClone();
			outboxReadWorker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), config);
		}

		ServiceItem service = outboxReadWorker.lookup(template, filter);
		if (service == null) {
			throw new TaskFailedException("No queue with the specified name could be found: " + outboxQueueName);
		}
		outboxQueue = (I_QueueProcesses) service.service;
		Collection<I_DescribeBusinessProcess> processes = outboxQueue.getProcessMetaData(selector);
		HashMap<Integer, Integer> projectHash = new HashMap<Integer, Integer>();
		HashMap<Integer, String> statusHash = new HashMap<Integer, String>();
//		I_EncodeBusinessProcess process = null;
		int countAssignmentsInt = 0;
//		Object[] arr;
//		List<Integer> sourceLang = null;
		Integer targetLang = null;
//		Integer langRefset;
		Integer statusId;
		Long statusTime;
		String[] targetTerms = new String[] { "", "" };
//		String[] sourceTerms;
		HashSet<String> oFolder;
		String status = "";
		for (I_DescribeBusinessProcess descProcess : processes) {
			try {
				I_DescribeQueueEntry qEntry = (I_DescribeQueueEntry) descProcess;
				String[] parsedSubj = TerminologyProjectDAO.getParsedItemSubject(qEntry.getSubject());

				if (parsedSubj.length == TerminologyProjectDAO.subjectIndexes.values().length) {

					String worklistmemberPref = parsedSubj[TerminologyProjectDAO.subjectIndexes.WORKLIST_MEMBER_SOURCE_PREF.ordinal()];
					String worklistmemberName = parsedSubj[TerminologyProjectDAO.subjectIndexes.WORKLIST_MEMBER_SOURCE_NAME.ordinal()];
					statusTime = Long.parseLong(parsedSubj[TerminologyProjectDAO.subjectIndexes.STATUS_TIME.ordinal()]);

					String statusIdStr = parsedSubj[TerminologyProjectDAO.subjectIndexes.STATUS_ID.ordinal()];
					try {
						statusId = Terms.get().uuidToNative(UUID.fromString(statusIdStr));
					} catch (IllegalArgumentException e) {
						statusId = Integer.valueOf(statusIdStr);
					}
					
					String projectIdStr = parsedSubj[TerminologyProjectDAO.subjectIndexes.PROJECT_ID.ordinal()];
					Integer projectId = null;
					try {
						projectId = Terms.get().uuidToNative(UUID.fromString(projectIdStr));
					} catch (IllegalArgumentException e) {
						projectId = Integer.valueOf(projectIdStr);
					}

					String worklistMemberIdStr = parsedSubj[TerminologyProjectDAO.subjectIndexes.WORKLIST_MEMBER_ID.ordinal()];
					Integer worklistmemberId = null;
					try {
						worklistmemberId = Terms.get().uuidToNative(UUID.fromString(worklistMemberIdStr));
					} catch (IllegalArgumentException e) {
						worklistmemberId = Integer.valueOf(worklistMemberIdStr);
					}

//					String promoRefsetgIdStr = parsedSubj[TerminologyProjectDAO.subjectIndexes.PROMO_REFSET_ID.ordinal()];
//					Integer promoRefsetId = null;
//					try {
//						promoRefsetId = Terms.get().uuidToNative(UUID.fromString(promoRefsetgIdStr));
//					} catch (IllegalArgumentException e) {
//						promoRefsetId = Integer.valueOf(promoRefsetgIdStr);
//					}

					countAssignmentsInt++;
					oFolder = new HashSet<String>();
					oFolder.add(IconUtilities.OUTBOX_NODE);
					addEntryToFolders(oFolder, qEntry.getEntryID());

					//
					if (projectHash.containsKey(projectId)) {
						targetLang = projectHash.get(projectId);

					} else {
						targetLang = null;
						if (targetFSNCol || targetPrefCol) {
							try {
								targetLang = TerminologyProjectDAO.getTargetLanguageRefsetIdForProjectId(projectId, config);
							} catch (Exception e) {
								e.printStackTrace();
							}

						}
						projectHash.put(projectId, targetLang);
					}
					if (targetFSNCol || targetPrefCol) {
						targetTerms = getTargetTerms(worklistmemberId, targetLang, targetFSNCol, targetPrefCol);
					} else {
						targetTerms[0] = "";
						targetTerms[1] = "";

					}

					// statusId =
					// TerminologyProjectDAO.getPromotionStatusIdForRefsetId(promoRefsetId,
					// worklistmemberId, config);
					if (statusHash.containsKey(statusId)) {
						status = statusHash.get(statusId);
					} else {
						I_GetConceptData statusConcept = Terms.get().getConcept(statusId);
						status = statusConcept.toString();
						statusHash.put(statusId, status);

						// DefaultMutableTreeNode statNode=new
						// DefaultMutableTreeNode(new
						// FolderTreeObj(IconUtilities.STATUS_NODE,status,new
						// FolderMetadata(status,true)));
						// sNode.add(statNode);
						//
						// hashFolders.put(status, new HashSet<EntryID>());
					}

					QueueTableObj tObj = new QueueTableObj("leaf", worklistmemberName, worklistmemberPref, status, targetTerms[0], targetTerms[1], qEntry.getEntryID(), oFolder, null, statusTime);
					hashAllItems.put(qEntry.getEntryID(), tObj);
				}
			} catch (RemoteException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	/**
	 * The Class RunRevisionProcess.
	 */
	class RunRevisionProcess implements Runnable {
		
		/** The processes. */
		Collection<I_DescribeBusinessProcess> processes;

		/**
		 * Instantiates a new run revision process.
		 *
		 * @param processes the processes
		 */
		public RunRevisionProcess(Collection<I_DescribeBusinessProcess> processes) {
			super();
			this.processes = processes;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			for (I_DescribeBusinessProcess descProcess : processes) {
				executeReviewProcess(((I_DescribeQueueEntry) descProcess).getEntryID());

				ObjectServerCore.refreshServers();
			}

			// synchronized (this){
			// if (!closing){
			// // if (entId.toString().equals(getEntryID().toString())){
			//
			// SwingUtilities.invokeLater(new Runnable() {
			// public void run() {
			// setupExecuteEnd();
			// }
			//
			// private void setupExecuteEnd() {
			// RefreshServer res = new RefreshServer();
			// res.start();
			//
			// }
			// });
			// }
			// }
		}

		/**
		 * Execute review process.
		 *
		 * @param entId the ent id
		 */
		protected void executeReviewProcess(EntryID entId) {
			try {
				I_EncodeBusinessProcess processToExecute = null;
				if (worker.isExecuting()) {
					I_Work altWorker = null;
					for (I_Work alt : cloneList) {
						if (alt.isExecuting() == false) {
							altWorker = alt;
							break;
						}
					}
					if (altWorker == null) {
						altWorker = worker.getTransactionIndependentClone();
						altWorker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), Terms.get().getActiveAceFrameConfig());
						
						cloneList.add(altWorker);
					}

					processToExecute = queue.take(entId, altWorker.getActiveTransaction());
					altWorker.execute(processToExecute);
				} else {

					processToExecute = queue.take(entId, worker.getActiveTransaction());
					worker.execute(processToExecute);
				}
			} catch (Throwable e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Load queue items.
	 *
	 * @throws RemoteException the remote exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TaskFailedException the task failed exception
	 * @throws InterruptedException the interrupted exception
	 * @throws PrivilegedActionException the privileged action exception
	 * @throws ConfigurationException the configuration exception
	 * @throws LeaseDeniedException the lease denied exception
	 * @throws TerminologyException the terminology exception
	 */
	private void loadQueueItems() throws RemoteException, IOException, TaskFailedException, InterruptedException, PrivilegedActionException, ConfigurationException, LeaseDeniedException,
			TerminologyException {

		long tim = new java.util.Date().getTime();
		ConfigTranslationModule cfg = null;
		try {
			cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		colSet = cfg.getColumnsDisplayedInInbox();
		boolean targetFSNCol = false;
		boolean targetPrefCol = false;
		if (colSet != null) {
			for (InboxColumn iCol : colSet) {
				switch (iCol) {
				case TARGET_FSN:
					targetFSNCol = true;
					break;
				case TARGET_PREFERRED:
					targetPrefCol = true;
					break;
				}
			}
		}
		ServiceID serviceID = null;
		Class<?>[] serviceTypes = new Class[] { I_QueueProcesses.class };
		Entry[] attrSetTemplates = new Entry[] { new Name(queueName) };
		ServiceTemplate template = new ServiceTemplate(serviceID, serviceTypes, attrSetTemplates);
		ServiceItemFilter filter = null;
		ServiceItem service = worker.lookup(template, filter);
		if (service == null) {
			throw new TaskFailedException("No queue with the specified name could be found: " + queueName);
		}
		this.queue = (I_QueueProcesses) service.service;

		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		HashMap<Integer, Integer> projectHash = new HashMap<Integer, Integer>();
		HashMap<Integer, String> statusHash = new HashMap<Integer, String>();
		// I_EncodeBusinessProcess process = null;

		Collection<I_DescribeBusinessProcess> processes = this.queue.getProcessMetaData(revisionProcCancelSelector);

		if (processes.size() > 0) {
			RunRevisionProcess revisionProc = new RunRevisionProcess(processes);
			revisionProc.run();
			// ObjectServerCore.refreshServers();
			loadQueueItems();
		} else {
			processes = this.queue.getProcessMetaData(revisionProcSelector);

			if (processes.size() > 0) {
				// SwingUtilities.invokeLater(new
				// RunRevisionProcess(processes));
				RunRevisionProcess revisionProc = new RunRevisionProcess(processes);
				revisionProc.run();
				// ObjectServerCore.refreshServers();
				loadQueueItems();

			} else {
				worklistHash = new HashMap<Integer, String>();
				processes = this.queue.getProcessMetaData(selector);
				// HashMap<Integer,Integer> countAssignments = new
				// HashMap<Integer,Integer>();
				int countAssignmentsInt = 0;
				String status = "";
				// String worklistName="";
				// List<Integer> sourceLang=null ;
				Integer targetLang = null;
				// Integer langRefset;
				Integer statusId;
				Long statusTime;
				String[] targetTerms = new String[] { "", "" };
				Set<String> tagsArray;
				String[] tagsplit;
				for (I_DescribeBusinessProcess descProcess : processes) {
					try {
						I_DescribeQueueEntry qEntry = (I_DescribeQueueEntry) descProcess;
						String[] parsedSubj = TerminologyProjectDAO.getParsedItemSubject(qEntry.getSubject());

						if (parsedSubj.length == TerminologyProjectDAO.subjectIndexes.values().length) {

							String projectIdStr = parsedSubj[TerminologyProjectDAO.subjectIndexes.PROJECT_ID.ordinal()];
							Integer projectId = null;
							try {
								projectId = Terms.get().uuidToNative(UUID.fromString(projectIdStr));
							} catch (IllegalArgumentException e) {
								projectId = Integer.valueOf(projectIdStr);
							}

							String worklistmemberPref = parsedSubj[TerminologyProjectDAO.subjectIndexes.WORKLIST_MEMBER_SOURCE_PREF.ordinal()];

							String worklistIdStr = parsedSubj[TerminologyProjectDAO.subjectIndexes.WORKLIST_ID.ordinal()];

							Integer worklistId = null;
							try {
								worklistId = Terms.get().uuidToNative(UUID.fromString(worklistIdStr));
							} catch (IllegalArgumentException e) {
								worklistId = Integer.valueOf(worklistIdStr);
							}

							String worklistMemberIdStr = parsedSubj[TerminologyProjectDAO.subjectIndexes.WORKLIST_MEMBER_ID.ordinal()];
							Integer worklistmemberId = null;
							try {
								worklistmemberId = Terms.get().uuidToNative(UUID.fromString(worklistMemberIdStr));
							} catch (IllegalArgumentException e) {
								worklistmemberId = Integer.valueOf(worklistMemberIdStr);
							}

							String worklistmemberName = parsedSubj[TerminologyProjectDAO.subjectIndexes.WORKLIST_MEMBER_SOURCE_NAME.ordinal()];
							String worklistName = parsedSubj[TerminologyProjectDAO.subjectIndexes.WORKLIST_NAME.ordinal()];

//							String promoRefsetgIdStr = parsedSubj[TerminologyProjectDAO.subjectIndexes.PROMO_REFSET_ID.ordinal()];
//							Integer promoRefsetId = null;
//							try {
//								promoRefsetId = Terms.get().uuidToNative(UUID.fromString(promoRefsetgIdStr));
//							} catch (IllegalArgumentException e) {
//								promoRefsetId = Integer.valueOf(promoRefsetgIdStr);
//							}

							String tags = parsedSubj[TerminologyProjectDAO.subjectIndexes.TAGS_ARRAY.ordinal()].trim();
							String statusIdStr = parsedSubj[TerminologyProjectDAO.subjectIndexes.STATUS_ID.ordinal()];
							try {
								statusId = Terms.get().uuidToNative(UUID.fromString(statusIdStr));
							} catch (IllegalArgumentException e) {
								statusId = Integer.valueOf(statusIdStr);
							}
							statusTime = Long.parseLong(parsedSubj[TerminologyProjectDAO.subjectIndexes.STATUS_TIME.ordinal()]);

							// }
							// process = this.queue.read(qEntry.getEntryID(),
							// null);
							//
							// WorkListMember
							// member=(WorkListMember)process.readAttachement(ProcessAttachmentKeys.WORKLIST_MEMBER.getAttachmentKey());
							// if (member!=null){
							countAssignmentsInt++;
							// I_TerminologyProject
							// project=getProjectForMember(member,config);
							// if (project!=null){
							// if
							// (!countAssignments.keySet().contains(project.getId()))
							// {
							// countAssignments.put(project.getId(), 1);
							// } else {
							// countAssignments.put(project.getId(),
							// countAssignments.get(project.getId()) + 1);
							// }
							if (!worklistHash.containsKey(worklistId)) {

								worklistHash.put(worklistId, worklistName);

								DefaultMutableTreeNode worklistNode = new DefaultMutableTreeNode(new FolderTreeObj(IconUtilities.WORKLIST_NODE, worklistName, new FolderMetadata(worklistName, true)));
								wNode.add(worklistNode);

								hashFolders.put(worklistName, new HashSet<EntryID>());
							}
							addEntryToWorklistFolder(worklistName, qEntry.getEntryID());
							if (tags.equals("")) {
								tagsArray = null;
								addEntryToFolders(null, qEntry.getEntryID());
							} else {
								tagsArray = new HashSet<String>();
								tagsplit = tags.split(FOLDER_TAGS_SEPARATOR);
								for (String tag : tagsplit) {
									tagsArray.add(tag);
								}

								addEntryToFolders(tagsArray, qEntry.getEntryID());
							}
							//
							if (projectHash.containsKey(projectId)) {
								targetLang = projectHash.get(projectId);

							} else {
								targetLang = null;
								if (targetFSNCol || targetPrefCol) {
									try {
										targetLang = TerminologyProjectDAO.getTargetLanguageRefsetIdForProjectId(projectId, config);
									} catch (Exception e) {
										e.printStackTrace();
									}

								}
								projectHash.put(projectId, targetLang);
							}
							if (targetFSNCol || targetPrefCol) {
								targetTerms = getTargetTerms(worklistmemberId, targetLang, targetFSNCol, targetPrefCol);
							} else {
								targetTerms[0] = "";
								targetTerms[1] = "";

							}
							// sourceTerms=getSourceTerms(worklistmemberId,langRefset,true,sourcePrefCol);

							// statusId =
							// TerminologyProjectDAO.getPromotionStatusIdForRefsetId(promoRefsetId,
							// worklistmemberId, config);
							if (statusHash.containsKey(statusId)) {
								status = statusHash.get(statusId);
							} else {
								I_GetConceptData statusConcept = Terms.get().getConcept(statusId);
								status = statusConcept.toString();
								statusHash.put(statusId, status);

								DefaultMutableTreeNode statNode = new DefaultMutableTreeNode(new FolderTreeObj(IconUtilities.STATUS_NODE, status, new FolderMetadata(status, true)));
								sNode.add(statNode);

								hashFolders.put(status, new HashSet<EntryID>());
							}
							addEntryToStatusFolder(status, qEntry.getEntryID());
							QueueTableObj tObj = new QueueTableObj("leaf", worklistmemberName, worklistmemberPref, status, targetTerms[0], targetTerms[1], qEntry.getEntryID(), tagsArray,
									worklistName, statusTime);
							hashAllItems.put(qEntry.getEntryID(), tObj);
							// nodeRoot.add(new DefaultMutableTreeNode(new
							// QueueTreeTableObj("leaf",sourceTerms[0],sourceTerms[1],status,
							// targetTerms[0],targetTerms[1],
							// qEntry.getEntryID())));

						}
					} catch (RemoteException e) {

						e.printStackTrace();
					} catch (IOException e) {

						e.printStackTrace();
					}
				}
				label2.setText("(" + (countAssignmentsInt + processInExecution) + ")");
			}
		}
		System.out.println("Inbox took:" + (new java.util.Date().getTime() - tim) + " miliseconds");
	}

//	private String[] getSourceTerms(Integer worklistmemberId, Integer sourceLang,  boolean sourcePrefCol) {
//		String[] retString = { "", "" };
//		String sFsn = "";
//		String sPref = "";
//		String sRetFsn = "";
//		String sRetPref = "";
//		if (sourceLang != null) {
//
//			List<ContextualizedDescription> descriptions;
//			try {
//				descriptions = LanguageUtil.getContextualizedDescriptions(worklistmemberId, sourceLang, true);
//
//				for (I_ContextualizeDescription description : descriptions) {
//					if (description.getLanguageExtension() != null && description.getLanguageRefsetId() == sourceLang) {
//
//						if (!( description.getExtensionStatusId() == inactive.getConceptNid() || description
//								.getDescriptionStatusId() == inactive.getConceptNid())) {
//
//							if (description.getTypeId() == fsn.getConceptNid()) {
//								sFsn = description.getText();
//								if (!sourcePrefCol || !sPref.equals("")) {
//									break;
//								}
//							} else {
//								if (sourcePrefCol && description.getAcceptabilityId() == preferred.getConceptNid()) {
//									sPref = description.getText();
//									if (!sFsn.equals("")) {
//										break;
//									}
//								}
//							}
//
//						}else{
//							if (description.getTypeId() == fsn.getConceptNid() ) {
//								sRetFsn=description.getText();
//							}else if (sourcePrefCol ){
//								sRetPref=description.getText();
//							}
//						}
//					}
//				}
//			} catch (TerminologyException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			if (sFsn.equals(""))
//				retString[0]=sRetFsn;
//			else
//				retString[0]=sFsn;
//			
//			if (sPref.equals(""))
//				retString[1]=sRetPref;
//			else
//				retString[1]=sPref;
//		}
//
//		return retString;
//
//	}

	/**
 * Gets the target terms.
 *
 * @param worklistmemberId the worklistmember id
 * @param targetLang the target lang
 * @param targetFSNCol the target fsn col
 * @param targetPrefCol the target pref col
 * @return the target terms
 */
private String[] getTargetTerms(Integer worklistmemberId, Integer targetLang, boolean targetFSNCol, boolean targetPrefCol) {
		String[] retString = { "", "" };
		String sFsn = "";
		String sPref = "";
		String sRetFsn = "";
		String sRetPref = "";
		if (targetLang != null) {
			List<ContextualizedDescription> descriptions;
			try {
				descriptions = LanguageUtil.getContextualizedDescriptions(worklistmemberId, targetLang, true);

				for (I_ContextualizeDescription description : descriptions) {
					if (description.getLanguageExtension() != null && description.getLanguageRefsetId() == targetLang) {

						if (!( description.getExtensionStatusId() == inactive.getConceptNid() || description
								.getDescriptionStatusId() == inactive.getConceptNid())) {

							if (targetFSNCol && description.getTypeId() == fsn.getConceptNid()) {
								sFsn = description.getText();
								if (!targetPrefCol || !sPref.equals("")) {
									break;
								}
							} else {
								if (targetPrefCol && description.getAcceptabilityId() == preferred.getConceptNid()) {
									sPref = description.getText();
									if (!targetFSNCol || !sFsn.equals("")) {
										break;
									}
								}
							}

						}else{
							if (description.getTypeId() == fsn.getConceptNid() ) {
								sRetFsn=description.getText();
							}else if (targetPrefCol ){
								sRetPref=description.getText();
							}
						}
					}
				}
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (sFsn.equals(""))
				retString[0]=sRetFsn;
			else
				retString[0]=sFsn;
			
			if (sPref.equals(""))
				retString[1]=sRetPref;
			else
				retString[1]=sPref;
		}

		return retString;

	}

	/**
	 * Adds the entry to worklist folder.
	 *
	 * @param worklistName the worklist name
	 * @param entryID the entry id
	 */
	private void addEntryToWorklistFolder(String worklistName, EntryID entryID) {
		Set<EntryID> foldEntries = null;
		if (hashFolders.containsKey(worklistName))
			foldEntries = hashFolders.get(worklistName);
		else
			foldEntries = new HashSet<EntryID>();

		foldEntries.add(entryID);
		hashFolders.put(worklistName, foldEntries);

	}

	/**
	 * Adds the entry to status folder.
	 *
	 * @param statusFolder the status folder
	 * @param entryId the entry id
	 */
	private void addEntryToStatusFolder(String statusFolder, EntryID entryId) {
		Set<EntryID> foldEntries = null;
		if (hashFolders.containsKey(statusFolder))
			foldEntries = hashFolders.get(statusFolder);
		else
			foldEntries = new HashSet<EntryID>();

		foldEntries.add(entryId);
		hashFolders.put(statusFolder, foldEntries);

	}

	/**
	 * Del entry from status folder.
	 *
	 * @param statusFolder the status folder
	 * @param entryId the entry id
	 */
	private void delEntryFromStatusFolder(String statusFolder, EntryID entryId) {

		Set<EntryID> foldEntries = null;
		if (hashFolders.containsKey(statusFolder)) {
			foldEntries = hashFolders.get(statusFolder);
			foldEntries.remove(entryId);
			hashFolders.put(statusFolder, foldEntries);
		}
	}

	/**
	 * Del entry from worklist folder.
	 *
	 * @param worklistName the worklist name
	 * @param entId the ent id
	 */
	private void delEntryFromWorklistFolder(String worklistName, EntryID entId) {
		Set<EntryID> foldEntries = null;
		if (hashFolders.containsKey(worklistName)) {
			foldEntries = hashFolders.get(worklistName);
			foldEntries.remove(entryId);
			hashFolders.put(worklistName, foldEntries);
		}

	}

	/**
	 * Adds the entry to folders.
	 *
	 * @param tagsArray the tags array
	 * @param entryId the entry id
	 */
	private void addEntryToFolders(Set<String> tagsArray, EntryID entryId) {
		Set<EntryID> foldEntries = null;
		if (tagsArray == null || tagsArray.size() < 1) {
			if (hashFolders.containsKey(IconUtilities.INBOX_NODE))
				foldEntries = hashFolders.get(IconUtilities.INBOX_NODE);
			else
				foldEntries = new HashSet<EntryID>();

			foldEntries.add(entryId);
			hashFolders.put(IconUtilities.INBOX_NODE, foldEntries);
		} else {
			for (String folderKey : tagsArray) {
				if (hashFolders.containsKey(folderKey))
					foldEntries = hashFolders.get(folderKey);
				else {
					foldEntries = new HashSet<EntryID>();
				}

				foldEntries.add(entryId);
				hashFolders.put(folderKey, foldEntries);
			}
		}
	}

	/**
	 * Del entry from folders.
	 *
	 * @param tagsArray the tags array
	 * @param entryId the entry id
	 */
	private void delEntryFromFolders(Set<String> tagsArray, EntryID entryId) {

		for (String folderKey : tagsArray) {
			Set<EntryID> foldEntries = null;
			if (hashFolders.containsKey(folderKey)) {
				foldEntries = hashFolders.get(folderKey);
				foldEntries.remove(entryId);
				hashFolders.put(folderKey, foldEntries);
			}
		}

	}

	/**
	 * Gets the custom node config.
	 *
	 * @return the custom node config
	 */
	private void getCustomNodeConfig() {
		try {
			ThinFolderTreeStructure foldStruc = (ThinFolderTreeStructure) Terms.get().getActiveAceFrameConfig().getDbConfig().getProperty(CUSTOM_NODE_KEY);
			if (foldStruc != null) {
				createNodesForChildFolders(foldStruc, cNode);
			}
		} catch (IOException e) {

			e.printStackTrace();
		} catch (TerminologyException e) {

			e.printStackTrace();
		}
	}

	/**
	 * Creates the nodes for child folders.
	 *
	 * @param folderStruc the folder struc
	 * @param node the node
	 */
	private void createNodesForChildFolders(ThinFolderTreeStructure folderStruc, DefaultMutableTreeNode node) {
		for (ThinFolderTreeStructure child : folderStruc.getChildren()) {
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new FolderTreeObj(IconUtilities.CUSTOM_NODE, child.getFolderName(), new FolderMetadata(child.getFolderName(), true)));
			node.add(childNode);
			createNodesForChildFolders(child, childNode);
			hashFolders.put(child.getFolderName(), new HashSet<EntryID>());
		}
	}

	/**
	 * The Class FolderMetadata.
	 */
	class FolderMetadata implements Serializable {
		
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;
		
		/** The pending refresh. */
		private boolean pendingRefresh;
		
		/** The folder name. */
		private String folderName;

		/**
		 * Instantiates a new folder metadata.
		 *
		 * @param folderName the folder name
		 * @param pendingRefresh the pending refresh
		 */
		public FolderMetadata(String folderName, boolean pendingRefresh) {
			this.folderName = folderName;
			this.pendingRefresh = pendingRefresh;
		}

		/**
		 * Checks if is pending refresh.
		 *
		 * @return true, if is pending refresh
		 */
		public boolean isPendingRefresh() {
			return pendingRefresh;
		}

		/**
		 * Sets the pending refresh.
		 *
		 * @param pendingRefresh the new pending refresh
		 */
		public void setPendingRefresh(boolean pendingRefresh) {
			this.pendingRefresh = pendingRefresh;
		}

		/**
		 * Gets the folder name.
		 *
		 * @return the folder name
		 */
		public String getFolderName() {
			return folderName;
		}

		/**
		 * Sets the folder name.
		 *
		 * @param folderName the new folder name
		 */
		public void setFolderName(String folderName) {
			this.folderName = folderName;
		}

	}

	/**
	 * Sets the next entry.
	 *
	 * @param entryID the new next entry
	 */
	synchronized private void setNextEntry(EntryID entryID) {
		EntryID nEntryID = null;
		for (int i = 0; i < itemsTable.getRowCount(); i++) {
			QueueTableObj qto = (QueueTableObj) itemsTable.getValueAt(i, 0);
			if (qto.getEntryId().toString().equals(entryID.toString())) {
				if (i < itemsTable.getRowCount() - 1) {
					QueueTableObj nqto = (QueueTableObj) itemsTable.getValueAt(i + 1, 0);
					nEntryID = nqto.getEntryId();
					break;
				}
			}
		}
		setNextEntryID(nEntryID);
	}

	/**
	 * Gets the project for member.
	 *
	 * @param member the member
	 * @param config the config
	 * @return the project for member
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private I_TerminologyProject getProjectForMember(WorkListMember member, I_ConfigAceFrame config) throws TerminologyException, IOException {
		I_GetConceptData workListConcept = Terms.get().getConcept(member.getWorkListUUID());
		WorkList workList = TerminologyProjectDAO.getWorkList(workListConcept, config);
		I_TerminologyProject project = TerminologyProjectDAO.getProjectForWorklist(workList, config);
		return project;
	}

	/**
	 * Button1 action performed.
	 *
	 * @param e the e
	 */
	private void button1ActionPerformed(ActionEvent e) {
		closing = true;
		AceFrameConfig config;
		try {
			config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();
			AceFrame ace = config.getAceFrame();

			JTabbedPane tpc = ace.getCdePanel().getConceptTabs();
			if (tpc != null) {
				int tabCount = tpc.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tpc.getTitleAt(i).equals(TranslationHelperPanel.TRANSLATION_TAB_NAME)) {
						if (tpc.getComponentAt(i) instanceof TranslationConceptEditor6) {
							TranslationConceptEditor6 uiPanel = (TranslationConceptEditor6) tpc.getComponentAt(i);
							if (!uiPanel.verifySavePending(null, false)) {
								closing = false;
								return;
							}
							uiPanel.AutokeepInInbox();
						}
						tpc.remove(i);
						tpc.repaint();
						tpc.revalidate();
						break;
					}

				}
			}
			if (closing) {
				JTabbedPane tp = ace.getCdePanel().getLeftTabs();
				if (tp != null) {
					int tabCount = tp.getTabCount();
					for (int i = 0; i < tabCount; i++) {
						if (tp.getTitleAt(i).equals(TranslationHelperPanel.TRANSLATION_LEFT_MENU)) {
							tp.remove(i);
							tp.repaint();
							tp.revalidate();
							break;
						}

					}
					tabCount = tp.getTabCount();
					for (int i = 0; i < tabCount; i++) {
						if (tp.getTitleAt(i).equals(TranslationHelperPanel.SIMILARITY_TAB_NAME)) {
							tp.remove(i);
							tp.repaint();
							tp.revalidate();
							break;
						}

					}
					tabCount = tp.getTabCount();
					for (int i = 0; i < tabCount; i++) {
						if (tp.getTitleAt(i).equals(TranslationHelperPanel.MEMBER_LOG_TAB_NAME)) {
							tp.remove(i);
							tp.repaint();
							tp.revalidate();
							break;
						}

					}
				}
			}
		} catch (TerminologyException ex) {

			ex.printStackTrace();
		} catch (IOException ex) {

			ex.printStackTrace();
		}
	}

	/** The entry id. */
	private EntryID entryId;

	/**
	 * Gets the entry id.
	 *
	 * @return the entry id
	 */
	public EntryID getEntryID() {
		return entryId;
	}

	/**
	 * Sets the entry id.
	 *
	 * @param entryId the new entry id
	 */
	public void setEntryID(EntryID entryId) {

		this.entryId = entryId;
	}

	/** The next entry id. */
	private EntryID nextEntryId;
	
	/** The user name. */
	private String userName;

	/**
	 * Gets the next entry id.
	 *
	 * @return the next entry id
	 */
	public EntryID getNextEntryID() {
		return nextEntryId;
	}

	/**
	 * Sets the next entry id.
	 *
	 * @param nextEntryId the new next entry id
	 */
	public void setNextEntryID(EntryID nextEntryId) {

		this.nextEntryId = nextEntryId;
	}

	/**
	 * The listener interface for receiving treeMenuItem events.
	 * The class that is interested in processing a treeMenuItem
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addTreeMenuItemListener<code> method. When
	 * the treeMenuItem event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see TreeMenuItemEvent
	 */
	class TreeMenuItemListener implements ActionListener {

		/** The node. */
		private DefaultMutableTreeNode node;
		
		/** The acc event. */
		private ActionEvent accEvent;

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (node != null) {
				this.accEvent = e;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (accEvent.getActionCommand().equals("Add")) {

							String folderName = JOptionPane.showInputDialog(null, "Enter new Folder Name : ", "", 1);
							if (folderName != null && !folderName.trim().equals("")) {
								if (hashFolders.containsKey(folderName)) {

									JOptionPane.showMessageDialog(foldTree, "Already exists a folder with this name.", "Error", JOptionPane.ERROR_MESSAGE);
									return;
								}
								hashFolders.put(folderName, new HashSet<EntryID>());
								DefaultMutableTreeNode tmpNode = new DefaultMutableTreeNode(new FolderTreeObj(IconUtilities.CUSTOM_NODE, folderName, new FolderMetadata(folderName, true)));
								((DefaultTreeModel) foldTree.getModel()).insertNodeInto(tmpNode, node, node.getChildCount());
								foldTree.scrollPathToVisible(new TreePath(tmpNode.getPath()));
								foldTree.revalidate();
								saveCustomFolders();
							}
							return;
						}
						if (accEvent.getActionCommand().equals("Delete")) {

							if (JOptionPane.showConfirmDialog(foldTree, "Delete this folder?\nItems from this folder will move to Inbox.", "Delete confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

								FolderTreeObj tObj = (FolderTreeObj) node.getUserObject();
								String folderName = ((FolderMetadata) tObj.getAtrValue()).getFolderName();
								moveItemsToInbox(folderName);
								hashFolders.remove(folderName);
								((DefaultTreeModel) foldTree.getModel()).removeNodeFromParent(node);
								foldTree.revalidate();
								saveCustomFolders();
							}
							return;
						}
						if (accEvent.getActionCommand().equals("Worklist info")) {
							FolderTreeObj fto = (FolderTreeObj) node.getUserObject();
							FolderMetadata fMData = (FolderMetadata) fto.getAtrValue();
							String worklistName = fMData.getFolderName();
							int worklistId = 0;
							if (node != null)
								for (Integer workslistKey : InboxPanel.this.worklistHash.keySet()) {
									if (InboxPanel.this.worklistHash.get(workslistKey).equals(worklistName)) {
										worklistId = workslistKey;
										break;
									}
								}
							if (worklistId != 0) {
								I_ConfigAceFrame config;
								try {
									config = Terms.get().getActiveAceFrameConfig();
									WorkList worklist = TerminologyProjectDAO.getWorkList(Terms.get().getConcept(worklistId), config);
									WorklistMemberStatusFrame wFrame = new WorklistMemberStatusFrame(worklist, config);
									wFrame.setVisible(true);

								} catch (TerminologyException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}

				});
			}

		}

		/**
		 * Sets the node.
		 *
		 * @param node the new node
		 */
		public void setNode(DefaultMutableTreeNode node) {
			this.node = node;
		}

	}

	/**
	 * Save custom folders.
	 */
	private void saveCustomFolders() {
		try {
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			ThinFolderTreeStructure thinkStr = getThinkStructure(cNode);

			config.getDbConfig().setProperty("CUSTOM_NODE_KEY", thinkStr);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Gets the think structure.
	 *
	 * @param node the node
	 * @return the think structure
	 */
	private ThinFolderTreeStructure getThinkStructure(DefaultMutableTreeNode node) {
		ThinFolderTreeStructure foldStruc = new ThinFolderTreeStructure();

		FolderTreeObj tObj = (FolderTreeObj) node.getUserObject();
		FolderMetadata fMData = (FolderMetadata) tObj.getAtrValue();
		foldStruc.setFolderName(fMData.getFolderName());

		ThinFolderTreeStructure[] childStruc = new ThinFolderTreeStructure[node.getChildCount()];
		for (int i = 0; i < node.getChildCount(); i++) {

			childStruc[i] = getThinkStructure((DefaultMutableTreeNode) node.getChildAt(i));
		}
		foldStruc.setChildren(childStruc);
		return foldStruc;
	}

	/**
	 * Move items from outbox.
	 *
	 * @param entries the entries
	 * @param folderName the folder name
	 */
	synchronized private void moveItemsFromOutbox(EntryID[] entries, String folderName) {
		Set<EntryID> custFoldEntries = hashFolders.get(folderName);

		for (EntryID entryId : entries) {
			custFoldEntries.add(entryId);
			setTagToOutboxProcess(entryId, folderName);
		}
	}

	/**
	 * Gets the tag string.
	 *
	 * @param tagSet the tag set
	 * @return the tag string
	 */
	private String getTagString(Set<String> tagSet) {
		StringBuffer ret = new StringBuffer("");
		int i = 0;
		for (String tag : tagSet) {
			i++;
			ret.append(tag);
			if (i < tagSet.size())
				ret.append(FOLDER_TAGS_SEPARATOR);
		}
		return ret.toString();
	}

	/**
	 * Sets the tag to outbox process.
	 *
	 * @param entryID the entry id
	 * @param folderName the folder name
	 */
	private void setTagToOutboxProcess(EntryID entryID, String folderName) {

		I_EncodeBusinessProcess process = null;
		try {
			QueueTableObj qto = (QueueTableObj) hashAllItems.get(entryID);
			Set<String> tags = qto.getTagsArray();
			if (tags != null) {
				tags.remove(IconUtilities.OUTBOX_NODE);
			} else {
				tags = new HashSet<String>();
			}
			tags.add(folderName);
			process = outboxQueue.take(entryID, outboxReadWorker.getActiveTransaction());

			String[] parsedSubj = TerminologyProjectDAO.getParsedItemSubject(process.getSubject());

			if (parsedSubj.length == TerminologyProjectDAO.subjectIndexes.values().length) {

				String worklistIdStr = parsedSubj[TerminologyProjectDAO.subjectIndexes.WORKLIST_ID.ordinal()];

				Integer worklistId = null;
				try {
					worklistId = Terms.get().uuidToNative(UUID.fromString(worklistIdStr));
				} catch (IllegalArgumentException e) {
					worklistId = Integer.valueOf(worklistIdStr);
				} catch (TerminologyException e) {
					e.printStackTrace();
				}

				String worklistMemberIdStr = parsedSubj[TerminologyProjectDAO.subjectIndexes.WORKLIST_MEMBER_ID.ordinal()];
				Integer worklistmemberId = null;
				try {
					worklistmemberId = Terms.get().uuidToNative(UUID.fromString(worklistMemberIdStr));
				} catch (IllegalArgumentException e) {
					worklistmemberId = Integer.valueOf(worklistMemberIdStr);
				} catch (TerminologyException e) {
					e.printStackTrace();
				}

				try {

					I_GetConceptData memberCpt = Terms.get().getConcept(worklistmemberId);
					I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
					WorkList worklist = TerminologyProjectDAO.getWorkList(Terms.get().getConcept(worklistId), config);
					PromotionRefset pRefset = worklist.getPromotionRefset(config);
					WorkListMember member = TerminologyProjectDAO.getWorkListMember(memberCpt, worklist, config);
					I_GetConceptData statusCon = pRefset.getPreviousPromotionStatus(member.getId(), config);
					Long statusTime = pRefset.getPreviousStatusTime(member.getId(), config);
					if (statusCon != null) {
						member.setActivityStatus(statusCon);
						member = TerminologyProjectDAO.updateWorkListMemberMetadata(member, config);
						try {
							Terms.get().commit();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					Integer tid = (Integer) process.getProperty(ProcessAttachmentKeys.LAST_USER_TASKID.getAttachmentKey());
					process.setCurrentTaskId(tid);

					parsedSubj[TerminologyProjectDAO.subjectIndexes.TAGS_ARRAY.ordinal()] = getTagString(tags);
					parsedSubj[TerminologyProjectDAO.subjectIndexes.STATUS_ID.ordinal()] = String.valueOf(statusCon.getConceptNid());
					parsedSubj[TerminologyProjectDAO.subjectIndexes.STATUS_TIME.ordinal()] = String.valueOf(statusTime);
					process.setSubject(TerminologyProjectDAO.getSubjectFromArray(parsedSubj));
				} catch (TerminologyException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IntrospectionException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (PropertyVetoException e) {
					e.printStackTrace();
				}
				process.setDestination(queue.getNodeInboxAddress());
				process.setOriginator(this.userName);
				queue.write(process, cloneWorker.getActiveTransaction());
				cloneWorker.commitTransactionIfActive();
			} else {
				outboxQueue.write(process, outboxReadWorker.getActiveTransaction());
			}
			outboxReadWorker.commitTransactionIfActive();
			// qto.setTagsArray(tags);
			// hashAllItems.put(entryID, qto);
			// SwingUtilities.invokeLater(new Runnable() {
			// public void run() {
			// Timer timer = new Timer(1100, new setInboxPanelFocus());
			// timer.setRepeats(false);
			// timer.start();
			// }
			//
			// });

		} catch (RemoteException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		} catch (NoMatchingEntryException e) {

			e.printStackTrace();
		} catch (LeaseDeniedException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (PrivilegedActionException e) {
			e.printStackTrace();
		} catch (TransactionException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Move items to inbox.
	 *
	 * @param folderName the folder name
	 */
	synchronized private void moveItemsToInbox(String folderName) {
		Set<EntryID> foldEntries = hashFolders.get(IconUtilities.INBOX_NODE);
		Set<EntryID> custFoldEntries = hashFolders.get(folderName);

		for (EntryID entryId : custFoldEntries) {
			foldEntries.add(entryId);
			removeTagFromProcess(entryId, folderName);
		}
	}

	/**
	 * Move items to folder.
	 *
	 * @param entries the entries
	 * @param folderSource the folder source
	 * @param folderTarget the folder target
	 */
	synchronized private void moveItemsToFolder(EntryID[] entries, String folderSource, String folderTarget) {
		Set<EntryID> targetEntries = hashFolders.get(folderTarget);
		Set<EntryID> sourceEntries = hashFolders.get(folderSource);

		for (EntryID entryID : entries) {
			sourceEntries.remove(entryID);
			targetEntries.add(entryID);
			changeTagFromProcess(entryID, folderSource, folderTarget);
		}

	}

	/**
	 * Change tag from process.
	 *
	 * @param entryID the entry id
	 * @param folderSource the folder source
	 * @param folderTarget the folder target
	 */
	private void changeTagFromProcess(EntryID entryID, String folderSource, String folderTarget) {
		I_EncodeBusinessProcess process = null;
		try {
			QueueTableObj qto = (QueueTableObj) hashAllItems.get(entryID);
			Set<String> tags = qto.getTagsArray();
			if (tags != null) {
				tags.remove(folderSource);
			} else {
				tags = new HashSet<String>();
			}
			tags.add(folderTarget);
			process = queue.take(entryID, cloneWorker.getActiveTransaction());

			String[] parsedSubj = TerminologyProjectDAO.getParsedItemSubject(process.getSubject());
			parsedSubj[TerminologyProjectDAO.subjectIndexes.TAGS_ARRAY.ordinal()] = getTagString(tags);
			process.setSubject(TerminologyProjectDAO.getSubjectFromArray(parsedSubj));

			// process.writeAttachment(CUSTOM_NODE_KEY, tags);
			process.setOriginator(this.userName);
			queue.write(process, cloneWorker.getActiveTransaction());
			cloneWorker.commitTransactionIfActive();
			// qto.setTagsArray(tags);
			// hashAllItems.put(entryID, qto);

		} catch (RemoteException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		} catch (NoMatchingEntryException e) {

			e.printStackTrace();
		} catch (LeaseDeniedException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (PrivilegedActionException e) {
			e.printStackTrace();
		} catch (TransactionException e) {
			e.printStackTrace();
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Removes the tag from process.
	 *
	 * @param entryId the entry id
	 * @param folderName the folder name
	 */
	private void removeTagFromProcess(EntryID entryId, String folderName) {
		I_EncodeBusinessProcess process = null;
		try {
			process = queue.take(entryId, cloneWorker.getActiveTransaction());
			QueueTableObj qto = (QueueTableObj) hashAllItems.get(entryId);
			Set<String> tags = qto.getTagsArray();
			if (tags != null) {
				tags.remove(folderName);
				qto.setTagsArray(tags);
				hashAllItems.put(entryId, qto);

				String[] parsedSubj = TerminologyProjectDAO.getParsedItemSubject(process.getSubject());
				parsedSubj[TerminologyProjectDAO.subjectIndexes.TAGS_ARRAY.ordinal()] = getTagString(tags);
				process.setSubject(TerminologyProjectDAO.getSubjectFromArray(parsedSubj));

				// process.writeAttachment(CUSTOM_NODE_KEY, tags);
				process.setOriginator(this.userName);
				queue.write(process, cloneWorker.getActiveTransaction());
				cloneWorker.commitTransactionIfActive();
			}
		} catch (RemoteException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		} catch (NoMatchingEntryException e) {

			e.printStackTrace();
		} catch (LeaseDeniedException e) {

			e.printStackTrace();
		} catch (TransactionException e) {

			e.printStackTrace();
		} catch (InterruptedException e) {

			e.printStackTrace();
		} catch (PrivilegedActionException e) {

			e.printStackTrace();
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}

	}

	/**
	 * The Class InboxTreeMouselistener.
	 */
	public class InboxTreeMouselistener extends MouseAdapter {
		
		/** The tree. */
		private JTree tree;
		
		/** The menu. */
		private JPopupMenu menu;
		
		/** The m item. */
		private JMenuItem mItem;
		
		/** The m item listener. */
		private TreeMenuItemListener mItemListener;
		
		/** The x point. */
		private int xPoint;
		
		/** The y point. */
		private int yPoint;

		/**
		 * Instantiates a new inbox tree mouselistener.
		 *
		 * @param tree the tree
		 */
		InboxTreeMouselistener(JTree tree) {
			this.tree = tree;
			mItemListener = new TreeMenuItemListener();
		}

		/**
		 * Gets the menu.
		 *
		 * @param nodeType the node type
		 * @param hasChildren the has children
		 * @return the menu
		 */
		private void getMenu(String nodeType, boolean hasChildren) {

			menu = new JPopupMenu();
			mItem = new JMenuItem();
			mItem.setText("Add Folder");
			mItem.setActionCommand("Add");
			mItem.addActionListener(mItemListener);
			menu.add(mItem);
			if (nodeType.equals(IconUtilities.CUSTOM_NODE) && !hasChildren) {
				mItem = new JMenuItem();
				mItem.setText("Delete Folder");
				mItem.setActionCommand("Delete");
				mItem.addActionListener(mItemListener);
				menu.add(mItem);
				return;
			}

			if (nodeType.equals(IconUtilities.WORKLIST_NODE)) {
				mItem = new JMenuItem();
				mItem.setText("View worklist info");
				mItem.setActionCommand("Worklist info");
				mItem.addActionListener(mItemListener);
				menu.add(mItem);
				return;

			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@SuppressWarnings("static-access")
		@Override
		public void mouseClicked(MouseEvent e) {

			if (e.getButton() == e.BUTTON3) {

				xPoint = e.getX();
				yPoint = e.getY();
				int row = tree.getRowForLocation(xPoint, yPoint);
				if (row > -1) {
					TreePath treePath = tree.getPathForRow(row);
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
					FolderTreeObj tObj = (FolderTreeObj) node.getUserObject();
					if (tObj.getObjType().equals(IconUtilities.CUSTOM_NODE) || tObj.getObjType().equals(IconUtilities.CUSTOM_NODE_ROOT) || tObj.getObjType().equals(IconUtilities.WORKLIST_NODE)) {
						getMenu(tObj.getObjType(), (node.getChildCount() > 0));
						mItemListener.setNode(node);
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								menu.show(tree, xPoint, yPoint);
							}
						});
					}
				}
			}
		}

	}

	/**
	 * The listener interface for receiving menuItem events.
	 * The class that is interested in processing a menuItem
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addMenuItemListener<code> method. When
	 * the menuItem event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see MenuItemEvent
	 */
	class MenuItemListener implements ActionListener {

		/** The node. */
		private QueueTableObj node;
		
		/** The folder type. */
		private String folderType;
		
		/** The acc event. */
		private ActionEvent accEvent;

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (node != null) {
				this.accEvent = e;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						EntryID entryId = (EntryID) node.getEntryId();
						I_ConfigAceFrame config;
						try {
							config = Terms.get().getActiveAceFrameConfig();
							I_EncodeBusinessProcess process = null;
							if (accEvent.getActionCommand().equals("Open read only view")) {
								if (folderType.equals(IconUtilities.OUTBOX_NODE)) {
									process = outboxQueue.read(entryId, null);

								} else {
									process = queue.read(entryId, null);
								}
								WorkListMember member = (WorkListMember) process.readAttachement(ProcessAttachmentKeys.WORKLIST_MEMBER.getAttachmentKey());
								if (member != null) {
									I_TerminologyProject project = getProjectForMember(member, config);
									if (project instanceof TranslationProject) {
										TranslationConceptFrame popFrame = new TranslationConceptFrame(member, (TranslationProject) project);
										popFrame.setVisible(true);
									}
								}
								return;
							}
							if (accEvent.getActionCommand().equals("Send to Inbox")) {
								if (folderType.equals(IconUtilities.OUTBOX_NODE)) {
									setTagToOutboxProcess(entryId, IconUtilities.INBOX_NODE);

									synchronized (this) {
										if (!closing) {
											// if
											// (entId.toString().equals(getEntryID().toString())){

											SwingUtilities.invokeLater(new Runnable() {
												public void run() {
													setupExecuteEnd();
												}

												private void setupExecuteEnd() {
													RefreshServer ros = new RefreshServer();
													ros.start();

												}

											});
										}
									}
								}
							}

						} catch (TerminologyException e) {

							e.printStackTrace();
						} catch (IOException e) {

							e.printStackTrace();
						} catch (ClassNotFoundException e) {

							e.printStackTrace();
						} catch (NoMatchingEntryException e) {

							e.printStackTrace();
						}

					}
				});
			}

		}

		/**
		 * Sets the item.
		 *
		 * @param node the node
		 * @param folderType the folder type
		 */
		public void setItem(QueueTableObj node, String folderType) {
			this.node = node;
			this.folderType = folderType;
		}

	}

	/**
	 * The Class InboxTableMouselistener.
	 */
	public class InboxTableMouselistener extends MouseAdapter {
		
		/** The table. */
		private JTable table;
		
		/** The menu. */
		private JPopupMenu menu;
		
		/** The m item. */
		private JMenuItem mItem;
		
		/** The m item listener. */
		private MenuItemListener mItemListener;
		
		/** The x point. */
		private int xPoint;
		
		/** The y point. */
		private int yPoint;

		/**
		 * Instantiates a new inbox table mouselistener.
		 *
		 * @param table the table
		 */
		InboxTableMouselistener(JTable table) {
			this.table = table;
			mItemListener = new MenuItemListener();
		}

		/**
		 * Gets the menu.
		 *
		 * @param nodeType the node type
		 * @return the menu
		 */
		private void getMenu(String nodeType) {

			menu = new JPopupMenu();
			mItem = new JMenuItem();
			mItem.setText("Open read only [v]iew");
			mItem.setActionCommand("Open read only view");
			mItem.setMnemonic(KeyEvent.VK_V);
			mItem.addActionListener(mItemListener);
			menu.add(mItem);
			if (nodeType.equals(IconUtilities.OUTBOX_NODE)) {
				mItem = new JMenuItem();
				mItem.setText("Send to [I]nbox");
				mItem.setMnemonic(KeyEvent.VK_I);
				mItem.setActionCommand("Send to Inbox");
				mItem.addActionListener(mItemListener);
				menu.add(mItem);
				return;
			}

		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {

			if (e.getButton() == MouseEvent.BUTTON3) {

				xPoint = e.getX();
				yPoint = e.getY();
				int row = table.rowAtPoint(new Point(xPoint, yPoint));
				if (row > -1) {
					int rowModel = table.convertRowIndexToModel(row);
					DefaultTableModel model = (DefaultTableModel) table.getModel();

					QueueTableObj node = (QueueTableObj) model.getValueAt(rowModel, 0);

					FolderTreeObj tObj = (FolderTreeObj) selectedFolder.getUserObject();

					String folderType = "";
					if (tObj != null) {

						folderType = tObj.getObjType();

					}
					mItemListener.setItem(node, folderType);
					getMenu(folderType);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							menu.show(table, xPoint, yPoint);
						}
					});
				}
			} else {
				if (e.getClickCount() == 2 && this.table.isEnabled() && processInExecution < 2) {

					AceFrameConfig config;
					try {
						config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();
						AceFrame ace = config.getAceFrame();

						JTabbedPane tpc = ace.getCdePanel().getConceptTabs();
						if (tpc != null) {
							int tabCount = tpc.getTabCount();
							for (int i = 0; i < tabCount; i++) {
								if (tpc.getTitleAt(i).equals(TranslationHelperPanel.TRANSLATION_TAB_NAME)) {
									if (tpc.getComponentAt(i) instanceof TranslationConceptEditor6) {
										TranslationConceptEditor6 uiPanel = (TranslationConceptEditor6) tpc.getComponentAt(i);
										ContextualizedDescription descriptionInEditor = uiPanel.getDescriptionInEditor();
										if (descriptionInEditor != null && !descriptionInEditor.getText().trim().equals("")) {
											if (!uiPanel.verifySavePending(null, false)) {
												return;
											}
										}
										break;
									}
								}

							}
						}

					} catch (TerminologyException ex) {

						ex.printStackTrace();
						return;
					} catch (IOException ex) {

						ex.printStackTrace();
						return;
					}
					FolderTreeObj tObj = (FolderTreeObj) selectedFolder.getUserObject();
					if (tObj != null) {

						if (tObj.getObjType().equals(IconUtilities.OUTBOX_NODE))
							return;

					}
					int row = table.getSelectedRow();

					if (row > -1) {
						int rowModel = table.convertRowIndexToModel(row);
						DefaultTableModel model = (DefaultTableModel) table.getModel();
						QueueTableObj node = (QueueTableObj) model.getValueAt(rowModel, 0);

						final EntryID entryId = (EntryID) node.getEntryId();
						if (entryId != null) {
							setEntryID(entryId);
							ConfigTranslationModule cfg = null;
							try {
								cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
							} catch (IOException ex) {

								ex.printStackTrace();
							} catch (TerminologyException ex) {

								ex.printStackTrace();
							}
							EntryID nextEntryId = null;
							if (cfg != null && cfg.isAutoOpenNextInboxItem()) {
								if (row < itemsTable.getRowCount() - 1) {
									QueueTableObj nextNode = (QueueTableObj) itemsTable.getValueAt(row + 1, 0);
									if (nextNode != null) {
										nextEntryId = nextNode.getEntryId();
									}
								}
							}
							setNextEntryID(nextEntryId);
							// AlltranslationProjectsEnabled(false);
							// button1.setEnabled(false);
							executeProcess();
						}
					}
				}
			}
		}
	}

	/** The clone list. */
	List<I_Work> cloneList = new ArrayList<I_Work>();
	
	/** The process in execution. */
	private int processInExecution;
	
	/** The set by code. */
	private boolean setByCode;

	/**
	 * Execute process.
	 */
	protected void executeProcess() {
		Runnable r = new Runnable() {

			public void run() {
				EntryID entId = getEntryID();
				try {
					// Thread t = new Thread(new Runnable() {
					// @Override
					// public void run() {
					// try {
					// AceFrameConfig config =
					// (AceFrameConfig)Terms.get().getActiveAceFrameConfig();
					// StringBuffer sb = new
					// StringBuffer("Opening inbox item.");
					// try{
					// while(true){
					// config.setStatusMessage(sb.toString());
					// for (int j = 0; j < 5; j++) {
					// sb.append('.');
					// config.setStatusMessage(sb.toString());
					// Thread.sleep(250);
					// }
					// sb = new StringBuffer("Opening inbox item.");
					// }
					// } catch (InterruptedException e) {
					// config.setStatusMessage("");
					// }
					// } catch (TerminologyException e) {
					// e.printStackTrace();
					// } catch (IOException e) {
					// e.printStackTrace();
					// }
					// }
					// });
					// t.start();
					I_EncodeBusinessProcess processToExecute = null;
					// t.interrupt();

					if (worker.isExecuting()) {

						I_Work altWorker = null;
						for (I_Work alt : cloneList) {
							if (alt.isExecuting() == false) {
								altWorker = alt;
								break;
							}
						}
						if (altWorker == null) {
							altWorker = worker.getTransactionIndependentClone();
							altWorker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), Terms.get().getActiveAceFrameConfig());
							
							cloneList.add(altWorker);
						}
						processToExecute = queue.take(entId, altWorker.getActiveTransaction());
						setViewItemAndRemove(entId);
						processInExecution++;
						altWorker.execute(processToExecute);
					} else {

						processToExecute = queue.take(entId, worker.getActiveTransaction());

						setViewItemAndRemove(entId);

						processInExecution++;
						worker.execute(processToExecute);
					}
					processInExecution--;
					if (processInExecution < 1) {
						setEntryID(null);
						// TestCfgForNextAction(entId);
					}
				} catch (Throwable e1) {
					e1.printStackTrace();
				}
				synchronized (this) {
					if (!closing) {
						// if
						// (entId.toString().equals(getEntryID().toString())){

						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								setupExecuteEnd();
							}

							private void setupExecuteEnd() {
								ReloadOS ros = new ReloadOS();
								ros.start();

							}
						});
					}
				}
				// }
			}

			synchronized private void setViewItemAndRemove(EntryID entId) {

				QueueTableObj itemObj = hashAllItems.get(entId);

				lViewing.setText(itemObj.getSourceFSN());

				Set<String> tags = itemObj.getTagsArray();
				if (tags != null)
					delEntryFromFolders(tags, entId);

				delEntryFromInboxFolder(entId);
				delEntryFromStatusFolder(itemObj.getStatus(), entId);
				delEntryFromWorklistFolder(itemObj.getWorklistName(), entId);

				DefaultTableModel model = (DefaultTableModel) itemsTable.getModel();
				for (int i = 0; i < model.getRowCount(); i++) {
					QueueTableObj qto = (QueueTableObj) model.getValueAt(i, 0);
					if (qto.getEntryId().toString().equals(entId.toString())) {
						model.removeRow(i);
						break;
					}
				}
				itemsTable.revalidate();

			}
		};
		new Thread(r).start();

	}

	/**
	 * Del entry from inbox folder.
	 *
	 * @param entId the ent id
	 */
	protected void delEntryFromInboxFolder(EntryID entId) {

		Set<EntryID> foldEntries = hashFolders.get(IconUtilities.INBOX_NODE);
		foldEntries.remove(entryId);
		hashFolders.put(IconUtilities.INBOX_NODE, foldEntries);

	}

	/**
	 * Test cfg for next action.
	 *
	 * @param entId the ent id
	 */
	synchronized protected void TestCfgForNextAction(EntryID entId) {

		ConfigTranslationModule cfg = null;
		try {
			cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
		} catch (IOException ex) {

			ex.printStackTrace();
		} catch (TerminologyException ex) {

			ex.printStackTrace();
		}
		if (cfg != null && cfg.isAutoOpenNextInboxItem()) {
			setNextEntry(entId);
		}
	}

	/**
	 * Fold tree value changed.
	 *
	 * @param e the e
	 */
	private void foldTreeValueChanged(TreeSelectionEvent e) {
		if (!setByCode) {

			DefaultMutableTreeNode node = (DefaultMutableTreeNode) foldTree.getLastSelectedPathComponent();
			selectedFolder = node;
			refreshItemsTable(node);
		}
	}

	/**
	 * The Class ReloadOS.
	 */
	public class ReloadOS extends SwingWorker<Boolean> {

		/* (non-Javadoc)
		 * @see org.dwfa.swing.SwingWorker#construct()
		 */
		@Override
		protected Boolean construct() throws Exception {
			// ObjectServerCore.refreshServers();
			return true;
		}

		/* (non-Javadoc)
		 * @see org.dwfa.swing.SwingWorker#finished()
		 */
		@Override
		protected void finished() {
			try {
				get();
			} catch (InterruptedException e1) {

				e1.printStackTrace();
			} catch (ExecutionException e1) {

				e1.printStackTrace();
			}
			try {
				setByCode = true;

				getFolderTreeModel();
				refreshItemsTable(selectedFolder);
				selectAndExecNextEntry();
				setByCode = false;

			} catch (RemoteException e) {

				e.printStackTrace();
			} catch (TaskFailedException e) {

				e.printStackTrace();
			} catch (LeaseDeniedException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			} catch (InterruptedException e) {

				e.printStackTrace();
			} catch (PrivilegedActionException e) {

				e.printStackTrace();
			} catch (ConfigurationException e) {

				e.printStackTrace();
			} catch (TerminologyException e) {

				e.printStackTrace();
			}
		}

	}

	/**
	 * The Class RefreshServer.
	 */
	public class RefreshServer extends SwingWorker<Boolean> {

		/* (non-Javadoc)
		 * @see org.dwfa.swing.SwingWorker#construct()
		 */
		@Override
		protected Boolean construct() throws Exception {
			// ObjectServerCore.refreshServers();
			return true;
		}

		/* (non-Javadoc)
		 * @see org.dwfa.swing.SwingWorker#finished()
		 */
		@Override
		protected void finished() {
			try {
				get();
			} catch (InterruptedException e1) {

				e1.printStackTrace();
			} catch (ExecutionException e1) {

				e1.printStackTrace();
			}
			try {
				setByCode = true;
				getFolderTreeModel();
				refreshItemsTable(selectedFolder);
				setByCode = false;
			} catch (RemoteException e) {

				e.printStackTrace();
			} catch (TaskFailedException e) {

				e.printStackTrace();
			} catch (LeaseDeniedException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			} catch (InterruptedException e) {

				e.printStackTrace();
			} catch (PrivilegedActionException e) {

				e.printStackTrace();
			} catch (ConfigurationException e) {

				e.printStackTrace();
			} catch (TerminologyException e) {

				e.printStackTrace();
			}
		}

	}

	/**
	 * Select and exec next entry.
	 */
	public void selectAndExecNextEntry() {

		ConfigTranslationModule cfg = null;
		try {
			cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
		} catch (IOException ex) {

			ex.printStackTrace();
		} catch (TerminologyException ex) {

			ex.printStackTrace();
		}

		if (cfg != null && cfg.isAutoOpenNextInboxItem()) {
			entryId = getNextEntryID();
			this.revalidate();
			this.validate();

			if (entryId != null && processInExecution < 1) {
				setEntryID(entryId);
				setNextEntry(entryId);

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						executeProcess();
					}
				});
			} else {
				if (processInExecution < 1)
					lViewing.setText("");
			}
		} else {
			setNextEntryID(null);
			if (processInExecution < 1)
				lViewing.setText("");
		}
	}

	/**
	 * Refresh inbox.
	 */
	public void refreshInbox() {

		synchronized (this) {
			if (!closing) {

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						setupExecuteEnd();
					}

					private void setupExecuteEnd() {
						RefreshServer ros = new RefreshServer();
						ros.start();

					}
				});
			}
		}
	}

	/**
	 * Sets the item check box.
	 *
	 * @param checked the new item check box
	 */
	public void setItemCheckBox(boolean checked) {
		inboxItemCheckbox.setSelected(checked);
		inboxItemCheckboxActionPerformed();
	}

	/**
	 * Inbox item checkbox action performed.
	 */
	private void inboxItemCheckboxActionPerformed() {
		if (!setByCode) {
			ConfigTranslationModule cfg = null;
			try {
				cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
				cfg.setAutoOpenNextInboxItem(inboxItemCheckbox.isSelected());
				LanguageUtil.setTranslationConfig(Terms.get().getActiveAceFrameConfig(), cfg);
			} catch (IOException ex) {

				ex.printStackTrace();
			} catch (TerminologyException ex) {

				ex.printStackTrace();
			}
		}
	}

	/**
	 * B send items action performed.
	 */
	private void bSendItemsActionPerformed() {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				runOutboxWorker();
				RefreshServer res = new RefreshServer();
				res.start();
			}
		});
	}

	/**
	 * Run outbox worker.
	 */
	private void runOutboxWorker() {
		List<Worker> lWorker = worker.getWorkerList();
		OnDemandOutboxQueueWorker oWorker = null;
		for (Worker worktmp : lWorker) {
			if (OnDemandOutboxQueueWorker.class.isAssignableFrom(worktmp.getClass())) {
				oWorker = (OnDemandOutboxQueueWorker) worktmp;
				oWorker.send();
			}
		}

	}

	/**
	 * Label4 mouse clicked.
	 *
	 * @param e the e
	 */
	private void label4MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("TRANSLATION_INBOX");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Refresh button action performed.
	 *
	 * @param e the e
	 */
	private void refreshButtonActionPerformed(ActionEvent e) {
		try {
			setByCode = true;
			getFolderTreeModel();
			refreshItemsTable(selectedFolder);
			selectAndExecNextEntry();
			setByCode = false;
		} catch (RemoteException e1) {
			e1.printStackTrace();
		} catch (TaskFailedException e1) {
			e1.printStackTrace();
		} catch (LeaseDeniedException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (PrivilegedActionException e1) {
			e1.printStackTrace();
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Open new inbox action performed.
	 *
	 * @param e the e
	 */
	private void openNewInboxActionPerformed(ActionEvent e) {
		WfInboxPanel inboxPanel = new WfInboxPanel();
		TranslationHelperPanel thp;
		try {
			thp = PanelHelperFactory.getTranslationHelperPanel();
			JTabbedPane tp = thp.getTabbedPanel();
			if (tp != null) {
				int tabCount = tp.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tp.getTitleAt(i).equals("NEW INBOX")) {
						tp.setSelectedIndex(i);
						thp.showTabbedPanel();
					}
				}
				JPanel panel = new JPanel();
				panel.setLayout(new BorderLayout());
				panel.add(inboxPanel, BorderLayout.CENTER);

				tp.addTab("NEW INBOX", panel);
				tp.setSelectedIndex(tp.getTabCount() - 1);
				thp.showTabbedPanel();
			}
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Rnd modify wl members action performed.
	 *
	 * @param e the e
	 */
	private void rndModifyWlMembersActionPerformed(ActionEvent e) {
		WorkflowSearcher.randomModifyWlMembers();
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		panel2 = new JPanel();
		panel1 = new JPanel();
		label1 = new JLabel();
		label2 = new JLabel();
		refreshButton = new JButton();
		button1 = new JButton();
		label4 = new JLabel();
		inboxItemCheckbox = new JCheckBox();
		bSendItems = new JButton();
		scrollPane1 = new JScrollPane();
		foldTree = new JTree();
		panel3 = new JPanel();
		label3 = new JLabel();
		lViewing = new JLabel();
		panel11 = new JPanel();
		scrollPane2 = new JScrollPane();
		itemsTable = new JTable();
		openNewInbox = new JButton();
		rndModifyWlMembers = new JButton();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

		//======== panel2 ========
		{
			panel2.setBackground(new Color(238, 238, 238));
			panel2.setBorder(new EmptyBorder(5, 15, 5, 5));
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 205, 0, 0, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0, 1.0E-4};

			//======== panel1 ========
			{
				panel1.setBackground(new Color(238, 238, 238));
				panel1.setLayout(new GridBagLayout());
				((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0};
				((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0};
				((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
				((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

				//---- label1 ----
				label1.setText("Assignments");
				panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label2 ----
				label2.setText("(-)");
				panel1.add(label2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- refreshButton ----
				refreshButton.setText("Refresh");
				refreshButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						refreshButtonActionPerformed(e);
					}
				});
				panel1.add(refreshButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- button1 ----
				button1.setText("[C]lose");
				button1.setIcon(null);
				button1.setMnemonic('C');
				button1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						button1ActionPerformed(e);
					}
				});
				panel1.add(button1, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label4 ----
				label4.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						label4MouseClicked(e);
					}
				});
				panel1.add(label4, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//---- inboxItemCheckbox ----
				inboxItemCheckbox.setText("Automatically [o]pen next item in inbox after finishing with current item");
				inboxItemCheckbox.setMnemonic('O');
				inboxItemCheckbox.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						inboxItemCheckboxActionPerformed();
					}
				});
				panel1.add(inboxItemCheckbox, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- bSendItems ----
				bSendItems.setText("Se[n]d");
				bSendItems.setMnemonic('N');
				bSendItems.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						bSendItemsActionPerformed();
					}
				});
				panel1.add(bSendItems, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));
			}
			panel2.add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//======== scrollPane1 ========
			{

				//---- foldTree ----
				foldTree.setVisibleRowCount(4);
				foldTree.addTreeSelectionListener(new TreeSelectionListener() {
					@Override
					public void valueChanged(TreeSelectionEvent e) {
						foldTreeValueChanged(e);
					}
				});
				scrollPane1.setViewportView(foldTree);
			}
			panel2.add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//======== panel3 ========
			{
				panel3.setLayout(new GridBagLayout());
				((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0, 0};
				((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
				((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

				//---- label3 ----
				label3.setText("Now viewing:");
				label3.setBackground(new Color(220, 233, 249));
				panel3.add(label3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- lViewing ----
				lViewing.setBackground(new Color(220, 233, 249));
				panel3.add(lViewing, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			panel2.add(panel3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//======== panel11 ========
			{
				panel11.setBackground(new Color(220, 233, 249));
				panel11.setLayout(new GridBagLayout());
				((GridBagLayout)panel11.getLayout()).columnWidths = new int[] {0, 0};
				((GridBagLayout)panel11.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel11.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
				((GridBagLayout)panel11.getLayout()).rowWeights = new double[] {1.0, 0.0, 0.0, 1.0E-4};

				//======== scrollPane2 ========
				{
					scrollPane2.setViewportView(itemsTable);
				}
				panel11.add(scrollPane2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//---- openNewInbox ----
				openNewInbox.setText("OPEN NEW INBOX PANEL");
				openNewInbox.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						openNewInboxActionPerformed(e);
					}
				});
				panel11.add(openNewInbox, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//---- rndModifyWlMembers ----
				rndModifyWlMembers.setText("RANDOM MODIFY WORKLIST MEMBERS");
				rndModifyWlMembers.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						rndModifyWlMembersActionPerformed(e);
					}
				});
				panel11.add(rndModifyWlMembers, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			panel2.add(panel11, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	/** The panel2. */
	private JPanel panel2;
	
	/** The panel1. */
	private JPanel panel1;
	
	/** The label1. */
	private JLabel label1;
	
	/** The label2. */
	private JLabel label2;
	
	/** The refresh button. */
	private JButton refreshButton;
	
	/** The button1. */
	private JButton button1;
	
	/** The label4. */
	private JLabel label4;
	
	/** The inbox item checkbox. */
	private JCheckBox inboxItemCheckbox;
	
	/** The b send items. */
	private JButton bSendItems;
	
	/** The scroll pane1. */
	private JScrollPane scrollPane1;
	
	/** The fold tree. */
	private JTree foldTree;
	
	/** The panel3. */
	private JPanel panel3;
	
	/** The label3. */
	private JLabel label3;
	
	/** The l viewing. */
	private JLabel lViewing;
	
	/** The panel11. */
	private JPanel panel11;
	
	/** The scroll pane2. */
	private JScrollPane scrollPane2;
	
	/** The items table. */
	private JTable itemsTable;
	
	/** The open new inbox. */
	private JButton openNewInbox;
	
	/** The rnd modify wl members. */
	private JButton rndModifyWlMembers;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
