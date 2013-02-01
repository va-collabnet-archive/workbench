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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.security.auth.login.LoginException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.EntryID;
import org.dwfa.bpa.process.I_DescribeBusinessProcess;
import org.dwfa.bpa.process.I_DescribeObject;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_SelectProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.NoMatchingEntryException;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.worker.Worker;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.queue.ObjectServerCore;
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
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.project.view.TranslationHelperPanel;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.ui.ConfigTranslationModule.InboxColumn;

/**
 * The Class SpecialInboxPanel.
 *
 * @author Guillermo Reynoso
 */
public class SpecialInboxPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8941067319987416891L;

	/** The Constant CUSTOM_NODE_KEY. */
	private static final String CUSTOM_NODE_KEY = "CUSTOM_NODE_KEY";

	/** The queue name. */
	private String queueName;
	
	/** The selector. */
	private I_SelectProcesses selector;
	
	/** The revision proc selector. */
	private SelectRevisionProcess2 revisionProcSelector;
	
	/** The worker. */
	private Worker worker;
	
	/** The queue. */
	private I_QueueProcesses queue;
	
	/** The fsn. */
	private I_GetConceptData fsn;
	
	/** The preferred. */
	private I_GetConceptData preferred;
	
	/** The not acceptable. */
	private I_GetConceptData notAcceptable;
	
	/** The inactive. */
	private I_GetConceptData inactive;
	
	/** The retired. */
	private I_GetConceptData retired;
	
	/** The closing. */
	private boolean closing;
	
	/** The en refset. */
	private I_GetConceptData enRefset;
	
	/** The Entry id bean type. */
	public final String EntryIDBeanType = DataFlavor.javaJVMLocalObjectMimeType + ";class=" + EntryID.class.getName();
	
	/** The hash folders. */
	private HashMap<String, Set<EntryID>> hashFolders;
	
	/** The hash all items. */
	private HashMap<EntryID, QueueTableObj> hashAllItems;
	
	/** The c names. */
	private String[] cNames = { "Source Name", "Status" };
	
	/** The col set. */
	private LinkedHashSet<InboxColumn> colSet;
	
	/** The col pos. */
	private ArrayList<InboxColumn> colPos;
	
	/** The l sort keys. */
	private List<SortKey> lSortKeys;
	
	/** The outbox read worker. */
	private I_Work outboxReadWorker;
	
	/** The outbox queue. */
	private I_QueueProcesses outboxQueue;
	
	/** The translation concept viewer1. */
	TranslationConceptEditorRO translationConceptViewer1;

	/** The formatter. */
	private SimpleDateFormat formatter;

	/**
	 * Instantiates a new special inbox panel.
	 *
	 * @param worker the worker
	 * @param userName the user name
	 * @param selector the selector
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public SpecialInboxPanel(I_Work worker, String userName, I_SelectProcesses selector) throws TerminologyException, IOException {

		initComponents();
		
		label2.setIcon(IconUtilities.helpIcon);
		label2.setText("");
		
		this.selector = selector;
		this.worker = (Worker) worker;
		queueName = userName;
		formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		revisionProcSelector = new SelectRevisionProcess2();
		hashFolders = new HashMap<String, Set<EntryID>>();
		enRefset = Terms.get().getConcept(RefsetAuxiliary.Concept.LANGUAGE_REFSET_EN.getUids());
		inactive = Terms.get().getConcept(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
		fsn = Terms.get().getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
		preferred = Terms.get().getConcept(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());
		hashAllItems = new HashMap<EntryID, QueueTableObj>();

		initCustomComponents();
	}

	/**
	 * Inits the custom components.
	 */
	private void initCustomComponents() {
		itemsTable.setAutoCreateRowSorter(true);
		itemsTable.setShowHorizontalLines(true);
		itemsTable.setShowVerticalLines(false);
		itemsTable.setGridColor(Color.LIGHT_GRAY);

		itemsTable.addMouseListener(new InboxTableMouselistener(itemsTable));

		try {
			loadQueueItems();
			refreshItemsTable();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	/**
	 * Refresh items table.
	 */
	private void refreshItemsTable() {
		try {
			getOutboxItems();
		} catch (Exception e1) {
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
		List<QueueTableObj> queueTO = new ArrayList<QueueTableObj>();

		Set<EntryID> keys = hashAllItems.keySet();
		Iterator<EntryID> it = keys.iterator();
		while (it.hasNext()) {
			EntryID entry = it.next();
			queueTO.add(hashAllItems.get(entry));
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
	 * Sets the table model data.
	 *
	 * @param tableModel the table model
	 * @param queueTO the queue to
	 */
	private void setTableModelData(DefaultTableModel tableModel, List<QueueTableObj> queueTO) {
		int rowLen = cNames.length;
		for (QueueTableObj qTO : queueTO) {
			Object[] row = new Object[rowLen];
			row[0] = qTO;
			for (int column = 1; column < rowLen; column++) {

				InboxColumn iCol = colPos.get(column - 1);

				switch (iCol) {
				case SOURCE_PREFERRED:
					row[column]=  qTO.getSourcePref();
					break;
				case STATUS:
					row[column]= qTO.getStatus();
					break;
//				case STATUS_DATE:
//					row[column]=formatter.format(qTO.getStatusTime());
//					break;
				case TARGET_FSN:
					row[column]=  qTO.getTargetFSN();
					break;
				case TARGET_PREFERRED:
					row[column]=  qTO.getTargetPref();
					break;
				}
			}
			tableModel.addRow(row);
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
	private void loadQueueItems() throws RemoteException, IOException, TaskFailedException, InterruptedException, PrivilegedActionException, TerminologyException {
		throw new UnsupportedOperationException("TODO: Jini removal");
	}

	/**
	 * The Class SelectRevisionProcess2.
	 */
	class SelectRevisionProcess2 implements I_SelectProcesses {

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
		// inboxItemCheckbox.setSelected( cfg.isAutoOpenNextInboxItem());
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
	private void getOutboxItems() throws TerminologyException, IOException, TaskFailedException, LoginException, PrivilegedActionException, InterruptedException {
		throw new UnsupportedOperationException("TODO: Jini removal");
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
	 * Gets the terms.
	 *
	 * @param member the member
	 * @param translationProject the translation project
	 * @param targetLanguage the target language
	 * @return the terms
	 */
	private String[] getTerms(WorkListMember member, I_TerminologyProject translationProject, boolean targetLanguage) {
		String[] retString = { "", "" };
		if (translationProject instanceof TranslationProject) {
			String sFsn = "";
			String sPref = "";
			I_GetConceptData langRefset = null;
			List<I_GetConceptData> langSets;
			if (member.getConcept() != null) {
				try {
					if (targetLanguage) {
						langRefset = ((TranslationProject) translationProject).getTargetLanguageRefset();
					} else {
						langSets = ((TranslationProject) translationProject).getSourceLanguageRefsets();
						if (langSets != null) {
							if (langSets.size() > 0) {
								for (I_GetConceptData lCon : langSets) {
									if (lCon.getConceptNid() == enRefset.getConceptNid()) {
										langRefset = lCon;
										break;
									}
								}
								if (langRefset == null) {
									langRefset = langSets.get(0);
								}
							}
						}
					}
					if (langRefset != null) {
						List<ContextualizedDescription> descriptions = LanguageUtil.getContextualizedDescriptions(member.getConcept().getConceptNid(), langRefset.getConceptNid(), true);

						for (I_ContextualizeDescription description : descriptions) {
							if (description.getLanguageExtension() != null && description.getLanguageRefsetId() == langRefset.getConceptNid()) {

								if (!(description.getAcceptabilityId() == notAcceptable.getConceptNid() || description.getExtensionStatusId() == inactive.getConceptNid() || description
										.getDescriptionStatusId() == retired.getConceptNid())) {

									if (description.getTypeId() == fsn.getConceptNid()) {
										sFsn = description.getText();
									} else {
										if (description.getAcceptabilityId() == preferred.getConceptNid()) {
											sPref = description.getText();
										}
									}
								}
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (TerminologyException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				retString[0] = sFsn;
				retString[1] = sPref;
			}

		}
		return retString;
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

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (node != null) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						EntryID entryId = (EntryID) node.getEntryId();
						I_ConfigAceFrame config;
						try {
							config = Terms.get().getActiveAceFrameConfig();
							I_EncodeBusinessProcess process = null;
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
			menu = new JPopupMenu();
			mItem = new JMenuItem();
			mItem.setText("Open read only view");
			mItemListener = new MenuItemListener();
			mItem.addActionListener(mItemListener);
			menu.add(mItem);
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {

			if (e.getClickCount() == 2) {

				xPoint = e.getX();
				yPoint = e.getY();
				int row = table.rowAtPoint(new Point(xPoint, yPoint));
				if (row > -1) {
					int rowModel = table.convertRowIndexToModel(row);
					DefaultTableModel model = (DefaultTableModel) table.getModel();

					QueueTableObj node = (QueueTableObj) model.getValueAt(rowModel, 0);
					EntryID entryId = (EntryID) node.getEntryId();
					I_ConfigAceFrame config;
					try {
						config = Terms.get().getActiveAceFrameConfig();
						I_EncodeBusinessProcess process = null;
						process = queue.read(entryId, null);
						WorkListMember member = (WorkListMember) process.readAttachement(ProcessAttachmentKeys.WORKLIST_MEMBER.getAttachmentKey());
						if (member != null) {
							I_TerminologyProject project = getProjectForMember(member, config);
							if (project instanceof TranslationProject) {
								AceFrameConfig aceConfig = (AceFrameConfig) config;
								AceFrame ace = aceConfig.getAceFrame();
								JTabbedPane tp = ace.getCdePanel().getConceptTabs();
								if (tp != null) {
									int tabCount = tp.getTabCount();
									boolean tabExists = false;
									
									for (int i = 0; i < tabCount; i++) {
										if (tp.getTitleAt(i).equals(TranslationHelperPanel.ARCHIVAL_TRANSLATION_TAB_NAME)) {
											translationConceptViewer1.updateUI( (TranslationProject) project,member);
											tp.setSelectedIndex(i);
											tabExists = true;
											break;
										}
									}
									if(!tabExists){
										translationConceptViewer1 = new TranslationConceptEditorRO();
										translationConceptViewer1.updateUI( (TranslationProject) project,member);
										tp.add(translationConceptViewer1);
										tp.setSelectedIndex(tp.getTabCount()-1);
									}
									tp.revalidate();
									tp.repaint();
								}
							}

						}
					} catch (TerminologyException ex) {
						ex.printStackTrace();
					} catch (IOException ex) {
						ex.printStackTrace();
					} catch (ClassNotFoundException ex) {
						ex.printStackTrace();
					} catch (NoMatchingEntryException ex) {
						ex.printStackTrace();
					}
				}

			}
		}

	}

	/** The clone list. */
	List<I_Work> cloneList = new ArrayList<I_Work>();

	/**
	 * Execute process.
	 */
	protected void executeProcess() {
		Runnable r = new Runnable() {

			public void run() {
				EntryID entId = getEntryID();
				try {
					I_EncodeBusinessProcess processToExecute = queue.take(entId, worker.getActiveTransaction());
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
						setViewItemAndRemove(entId);
						processInExecution++;
						altWorker.execute(processToExecute);
					} else {

						setViewItemAndRemove(entId);

						processInExecution++;
						worker.execute(processToExecute);
					}
					processInExecution--;
					if (processInExecution < 1) {
						setEntryID(null);
					}
				} catch (Throwable e1) {
					e1.printStackTrace();
				}
				synchronized (this) {
					if (!closing) {
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
			}

			synchronized private void setViewItemAndRemove(EntryID entId) {

				QueueTableObj itemObj = hashAllItems.get(entId);

				// lViewing.setText(itemObj.getSourceFSN());

				Set<String> tags = itemObj.getTagsArray();
				if (tags != null) {
					delEntryFromFolders(tags, entId);
				}

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
	 * The Class ReloadOS.
	 */
	public class ReloadOS extends SwingWorker<Boolean> {

		/* (non-Javadoc)
		 * @see org.dwfa.swing.SwingWorker#construct()
		 */
		@Override
		protected Boolean construct() throws Exception {
			ObjectServerCore.refreshServers();
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
				refreshItemsTable();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

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
	 * Close button action performed.
	 *
	 * @param e the e
	 */
	private void closeButtonActionPerformed(ActionEvent e) {
		AceFrameConfig config;
		try {
			config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();
			AceFrame ace = config.getAceFrame();
			JTabbedPane tp = ace.getCdePanel().getLeftTabs();
			if (tp != null) {
				int tabCount = tp.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.ARCHIVAL_ITEMS_LEFT_MENU)) {
						tp.remove(i);
						tp.repaint();
						tp.revalidate();
					}
				}
			}

			JTabbedPane tpc = ace.getCdePanel().getConceptTabs();
			if (tpc != null) {
				int tabCount = tpc.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tpc.getTitleAt(i).equals(TranslationHelperPanel.ARCHIVAL_TRANSLATION_TAB_NAME)) {
						tpc.remove(i);
						tpc.repaint();
						tpc.revalidate();
					}
				}
			}
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Label2 mouse clicked.
	 *
	 * @param e the e
	 */
	private void label2MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("ARCHIVAL_QUEUE");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	/** The process in execution. */
	private int processInExecution;

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		containerPanel = new JPanel();
		panel1 = new JPanel();
		label1 = new JLabel();
		closeButton = new JButton();
		label2 = new JLabel();
		scrollPane1 = new JScrollPane();
		itemsTable = new JTable();

		//======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout());

		//======== containerPanel ========
		{
			containerPanel.setLayout(new GridBagLayout());
			((GridBagLayout)containerPanel.getLayout()).columnWidths = new int[] {247, 0};
			((GridBagLayout)containerPanel.getLayout()).rowHeights = new int[] {0, 93, 0};
			((GridBagLayout)containerPanel.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
			((GridBagLayout)containerPanel.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

			//======== panel1 ========
			{
				panel1.setLayout(new GridBagLayout());
				((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 1.0E-4};
				((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

				//---- label1 ----
				label1.setText("Archival items");
				panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- closeButton ----
				closeButton.setText("Close");
				closeButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						closeButtonActionPerformed(e);
					}
				});
				panel1.add(closeButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- label2 ----
				label2.setText("text");
				label2.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						label2MouseClicked(e);
					}
				});
				panel1.add(label2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			containerPanel.add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//======== scrollPane1 ========
			{
				scrollPane1.setViewportView(itemsTable);
			}
			containerPanel.add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(containerPanel, BorderLayout.CENTER);
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	/** The container panel. */
	private JPanel containerPanel;
	
	/** The panel1. */
	private JPanel panel1;
	
	/** The label1. */
	private JLabel label1;
	
	/** The close button. */
	private JButton closeButton;
	
	/** The label2. */
	private JLabel label2;
	
	/** The scroll pane1. */
	private JScrollPane scrollPane1;
	
	/** The items table. */
	private JTable itemsTable;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
