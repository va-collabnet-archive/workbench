/*
 * Created by JFormDesigner on Thu Sep 02 18:40:03 GMT-03:00 2010
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
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.RowSorter.SortKey;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import net.jini.config.ConfigurationException;
import net.jini.core.entry.Entry;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.lookup.ServiceItemFilter;
import net.jini.lookup.entry.Name;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.task.ProcessAttachmentKeys;
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
import org.dwfa.bpa.util.SwingWorker;
import org.dwfa.bpa.worker.Worker;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.queue.ObjectServerCore;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.I_ContextualizeDescription;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.panel.TranslationHelperPanel;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.ui.ConfigTranslationModule.InboxColumn;

/**
 * @author Guillermo Reynoso
 */
public class SpecialInboxPanel extends JPanel {

	private static final long serialVersionUID = -8941067319987416891L;

	private static final String CUSTOM_NODE_KEY = "CUSTOM_NODE_KEY";

	private String queueName;
	private I_SelectProcesses selector;
	private SelectRevisionProcess2 revisionProcSelector;
	private Worker worker;
	private Worker cloneWorker;
	private I_QueueProcesses queue;
	private I_GetConceptData fsn;
	private I_GetConceptData preferred;
	private I_GetConceptData notAcceptable;
	private I_GetConceptData inactive;
	private I_GetConceptData retired;
	private boolean closing;
	private I_GetConceptData enRefset;
	public final String EntryIDBeanType = DataFlavor.javaJVMLocalObjectMimeType + ";class=" + EntryID.class.getName();
	private DefaultMutableTreeNode wNode;
	private DefaultMutableTreeNode iNode;
	private DefaultMutableTreeNode sNode;
	private DefaultMutableTreeNode cNode;
	private HashMap<String, Set<EntryID>> hashFolders;
	private HashMap<EntryID, QueueTableObj> hashAllItems;
	private DefaultMutableTreeNode selectedFolder;
	private String[] cNames = { "Source Name", "Status" };
	private LinkedHashSet<InboxColumn> colSet;
	private ArrayList<InboxColumn> colPos;
	private List<SortKey> lSortKeys;
	private DefaultMutableTreeNode oNode;
	private I_Work outboxReadWorker;
	private I_QueueProcesses outboxQueue;
	TranslationConceptEditorRO translationConceptViewer1;

	private SimpleDateFormat formatter;

	public SpecialInboxPanel(I_Work worker, String userName, I_SelectProcesses selector) throws TerminologyException, IOException {

		initComponents();
		this.selector = selector;
		this.worker = (Worker) worker;
		queueName = userName;
		formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		revisionProcSelector = new SelectRevisionProcess2();
		hashFolders = new HashMap<String, Set<EntryID>>();
		enRefset = Terms.get().getConcept(RefsetAuxiliary.Concept.LANGUAGE_REFSET_EN.getUids());
		notAcceptable = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.NOT_ACCEPTABLE.getUids());
		inactive = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.INACTIVE.getUids());
		retired = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
		fsn = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
		preferred = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
		hashAllItems = new HashMap<EntryID, QueueTableObj>();

		initCustomComponents();
	}

	private void initCustomComponents() {
		itemsTable.setAutoCreateRowSorter(true);
		itemsTable.setShowHorizontalLines(true);
		itemsTable.setShowVerticalLines(false);
		itemsTable.setGridColor(Color.LIGHT_GRAY);

		itemsTable.addMouseListener(new InboxTableMouselistener(itemsTable));

		try {
			loadQueueItems();
			refreshItemsTable();
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

	private void refreshItemsTable() {
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
				case STATUS_DATE:
					row[column]=formatter.format(qTO.getStatusTime());
					break;
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

	private void loadQueueItems() throws RemoteException, IOException, TaskFailedException, InterruptedException, PrivilegedActionException, ConfigurationException, LeaseDeniedException,
			TerminologyException {

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
		HashMap<String, String> worklistHash = new HashMap<String, String>();
		HashMap<String, String> statusHash = new HashMap<String, String>();
		I_EncodeBusinessProcess process = null;
		Collection<I_DescribeBusinessProcess> processes = this.queue.getProcessMetaData(revisionProcSelector);

		if (processes.size() <= 0) {
			processes = this.queue.getProcessMetaData(selector);
			HashMap<Integer, Integer> countAssignments = new HashMap<Integer, Integer>();
			int countAssignmentsInt = 0;
			String status = "";
			String worklistName = "";
			for (I_DescribeBusinessProcess descProcess : processes) {
				try {
					I_DescribeQueueEntry qEntry = (I_DescribeQueueEntry) descProcess;
					process = this.queue.read(qEntry.getEntryID(), null);
					WorkListMember member = (WorkListMember) process.readAttachement(ProcessAttachmentKeys.WORKLIST_MEMBER.getAttachmentKey());
					if (member != null) {
						countAssignmentsInt++;
						I_TerminologyProject project = getProjectForMember(member, config);
						if (project != null) {
							if (!countAssignments.keySet().contains(project.getId())) {
								countAssignments.put(project.getId(), 1);
							} else {
								countAssignments.put(project.getId(), countAssignments.get(project.getId()) + 1);
							}
							if (worklistHash.containsKey(member.getWorkListUUID().toString())) {
								worklistName = worklistHash.get(member.getWorkListUUID().toString());
							} else {
								I_GetConceptData workListConcept = Terms.get().getConcept(member.getWorkListUUID());
								WorkList worklist = (WorkList) TerminologyProjectDAO.getWorkList(workListConcept, config);
								worklistName = worklist.getName();
								worklistHash.put(member.getWorkListUUID().toString(), worklistName);
								hashFolders.put(worklistName, new HashSet<EntryID>());
							}
							addEntryToWorklistFolder(worklistName, qEntry.getEntryID());

							Set<String> tagsArray = (Set<String>) process.readAttachement(CUSTOM_NODE_KEY);

							String[] targetTerms = getTerms(member, project, true);
							String[] sourceTerms = getTerms(member, project, false);
							if (statusHash.containsKey(member.getActivityStatus().toString())) {
								status = statusHash.get(member.getActivityStatus().toString());
							} else {
								I_GetConceptData statusConcept = Terms.get().getConcept(member.getActivityStatus());
								status = statusConcept.toString();
								statusHash.put(member.getActivityStatus().toString(), status);
								hashFolders.put(status, new HashSet<EntryID>());
							}
							Long statusTime = member.getStatusDate();
							QueueTableObj tObj = new QueueTableObj("leaf", sourceTerms[0], sourceTerms[1], status, targetTerms[0], targetTerms[1], qEntry.getEntryID(), tagsArray,
									worklistName,statusTime);
							hashAllItems.put(qEntry.getEntryID(), tObj);
							// nodeRoot.add(new DefaultMutableTreeNode(new
							// QueueTreeTableObj("leaf",sourceTerms[0],sourceTerms[1],status,
							// targetTerms[0],targetTerms[1],
							// qEntry.getEntryID())));
						}
					}
				} catch (ClassNotFoundException e) {

					e.printStackTrace();
				} catch (NoMatchingEntryException e) {

					e.printStackTrace();
					break;
				} catch (RemoteException e) {

					e.printStackTrace();
				} catch (IOException e) {

					e.printStackTrace();
				}
			}
		}
	}

	class SelectRevisionProcess2 implements I_SelectProcesses {

		@Override
		public boolean select(I_DescribeBusinessProcess process) {
			String sub = process.getSubject();
			if (sub != null)
				return sub.equals(TranslationHelperPanel.AUTO_PROCESS_WORKLIST_MEMBERS_REVIEW);
			return false;
		}

		@Override
		public boolean select(I_DescribeObject object) {
			I_DescribeBusinessProcess objectBP = (I_DescribeBusinessProcess) object;
			String sub = objectBP.getSubject();
			if (sub != null)
				return sub.equals(TranslationHelperPanel.AUTO_PROCESS_WORKLIST_MEMBERS_REVIEW);
			return false;
		}

	}

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

	private void getOutboxItems() throws TerminologyException, IOException, TaskFailedException, LoginException, ConfigurationException, PrivilegedActionException, InterruptedException {

		I_ConfigAceFrame config;
		config = Terms.get().getActiveAceFrameConfig();
		String outboxQueueName = config.getUsername() + ".outbox";
		ServiceID serviceID = null;
		Class<?>[] serviceTypes = new Class[] { I_QueueProcesses.class };
		Entry[] attrSetTemplates = new Entry[] { new Name(outboxQueueName) };
		ServiceTemplate template = new ServiceTemplate(serviceID, serviceTypes, attrSetTemplates);
		ServiceItemFilter filter = null;
		if (outboxReadWorker == null)
			outboxReadWorker = worker.getTransactionIndependentClone();

		ServiceItem service = outboxReadWorker.lookup(template, filter);
		if (service == null) {
			throw new TaskFailedException("No queue with the specified name could be found: " + outboxQueueName);
		}
		outboxQueue = (I_QueueProcesses) service.service;
		Collection<I_DescribeBusinessProcess> processes = outboxQueue.getProcessMetaData(selector);
		HashMap<Integer, Integer> countAssignments = new HashMap<Integer, Integer>();
		HashMap<String, String> statusHash = new HashMap<String, String>();
		I_EncodeBusinessProcess process = null;
		int countAssignmentsInt = 0;
		String status = "";
		for (I_DescribeBusinessProcess descProcess : processes) {
			try {
				I_DescribeQueueEntry qEntry = (I_DescribeQueueEntry) descProcess;
				process = outboxQueue.read(qEntry.getEntryID(), null);
				WorkListMember member = (WorkListMember) process.readAttachement(ProcessAttachmentKeys.WORKLIST_MEMBER.getAttachmentKey());
				if (member != null) {
					countAssignmentsInt++;
					I_TerminologyProject project = getProjectForMember(member, config);
					if (project != null) {
						if (!countAssignments.keySet().contains(project.getId())) {
							countAssignments.put(project.getId(), 1);
						} else {
							countAssignments.put(project.getId(), countAssignments.get(project.getId()) + 1);
						}
						HashSet<String> oFolder = new HashSet<String>();

						String[] targetTerms = getTerms(member, project, true);
						String[] sourceTerms = getTerms(member, project, false);
						if (statusHash.containsKey(member.getActivityStatus().toString())) {
							status = statusHash.get(member.getActivityStatus().toString());
						} else {
							I_GetConceptData statusConcept = Terms.get().getConcept(member.getActivityStatus());
							status = statusConcept.toString();
							statusHash.put(member.getActivityStatus().toString(), status);

						}
						Long statusTime = member.getStatusDate();
						
						QueueTableObj tObj = new QueueTableObj("leaf", sourceTerms[0], sourceTerms[1], status, targetTerms[0], targetTerms[1], qEntry.getEntryID(), oFolder, null,statusTime);
						hashAllItems.put(qEntry.getEntryID(), tObj);

					}
				}
			} catch (ClassNotFoundException e) {

				e.printStackTrace();
			} catch (NoMatchingEntryException e) {

				e.printStackTrace();
				break;
			} catch (RemoteException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}

	}

	private void addEntryToWorklistFolder(String worklistName, EntryID entryID) {
		Set<EntryID> foldEntries = null;
		if (hashFolders.containsKey(worklistName))
			foldEntries = hashFolders.get(worklistName);
		else
			foldEntries = new HashSet<EntryID>();

		foldEntries.add(entryID);
		hashFolders.put(worklistName, foldEntries);

	}

	private I_TerminologyProject getProjectForMember(WorkListMember member, I_ConfigAceFrame config) throws TerminologyException, IOException {
		I_GetConceptData workListConcept = Terms.get().getConcept(member.getWorkListUUID());
		WorkList workList = TerminologyProjectDAO.getWorkList(workListConcept, config);
		I_TerminologyProject project = TerminologyProjectDAO.getProjectForWorklist(workList, config);
		return project;
	}

	private String[] getTerms(WorkListMember member, I_TerminologyProject translationProject, boolean targetLanguage) {
		String[] retString = { "", "" };
		if (translationProject instanceof TranslationProject) {
			I_TermFactory tf = Terms.get();
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

	class MenuItemListener implements ActionListener {

		private QueueTableObj node;
		private String folderType;

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

		public void setItem(QueueTableObj node, String folderType) {
			this.node = node;
			this.folderType = folderType;
		}

	}

	public class InboxTableMouselistener extends MouseAdapter {
		private JTable table;
		private JPopupMenu menu;
		private JMenuItem mItem;
		private MenuItemListener mItemListener;
		private int xPoint;
		private int yPoint;

		InboxTableMouselistener(JTable table) {
			this.table = table;
			menu = new JPopupMenu();
			mItem = new JMenuItem();
			mItem.setText("Open read only view");
			mItemListener = new MenuItemListener();
			mItem.addActionListener(mItemListener);
			menu.add(mItem);
		}

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

	List<I_Work> cloneList = new ArrayList<I_Work>();
	private boolean setByCode;

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

	public class ReloadOS extends SwingWorker<Boolean> {

		@Override
		protected Boolean construct() throws Exception {
			ObjectServerCore.refreshServers();
			return true;
		}

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
				refreshItemsTable();
				setByCode = false;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private void delEntryFromStatusFolder(String statusFolder, EntryID entryId) {

		Set<EntryID> foldEntries = null;
		if (hashFolders.containsKey(statusFolder)) {
			foldEntries = hashFolders.get(statusFolder);
			foldEntries.remove(entryId);
			hashFolders.put(statusFolder, foldEntries);
		}
	}

	private void delEntryFromWorklistFolder(String worklistName, EntryID entId) {
		Set<EntryID> foldEntries = null;
		if (hashFolders.containsKey(worklistName)) {
			foldEntries = hashFolders.get(worklistName);
			foldEntries.remove(entryId);
			hashFolders.put(worklistName, foldEntries);
		}

	}

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

	private EntryID entryId;

	public EntryID getEntryID() {
		return entryId;
	}

	public void setEntryID(EntryID entryId) {
		this.entryId = entryId;
	}

	private EntryID nextEntryId;

	public EntryID getNextEntryID() {
		return nextEntryId;
	}

	public void setNextEntryID(EntryID nextEntryId) {

		this.nextEntryId = nextEntryId;
	}

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

	private int processInExecution;

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		containerPanel = new JPanel();
		panel1 = new JPanel();
		label1 = new JLabel();
		closeButton = new JButton();
		scrollPane1 = new JScrollPane();
		itemsTable = new JTable();

		// ======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout());

		// ======== containerPanel ========
		{
			containerPanel.setLayout(new GridBagLayout());
			((GridBagLayout) containerPanel.getLayout()).columnWidths = new int[] { 247, 0 };
			((GridBagLayout) containerPanel.getLayout()).rowHeights = new int[] { 0, 93, 0 };
			((GridBagLayout) containerPanel.getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
			((GridBagLayout) containerPanel.getLayout()).rowWeights = new double[] { 0.0, 1.0, 1.0E-4 };

			// ======== panel1 ========
			{
				panel1.setLayout(new GridBagLayout());
				((GridBagLayout) panel1.getLayout()).columnWidths = new int[] { 0, 0, 0 };
				((GridBagLayout) panel1.getLayout()).rowHeights = new int[] { 0, 0 };
				((GridBagLayout) panel1.getLayout()).columnWeights = new double[] { 1.0, 0.0, 1.0E-4 };
				((GridBagLayout) panel1.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

				// ---- label1 ----
				label1.setText("Archival items");
				panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

				// ---- closeButton ----
				closeButton.setText("Close");
				closeButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						closeButtonActionPerformed(e);
					}
				});
				panel1.add(closeButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			}
			containerPanel.add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

			// ======== scrollPane1 ========
			{
				scrollPane1.setViewportView(itemsTable);
			}
			containerPanel.add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(containerPanel, BorderLayout.CENTER);
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel containerPanel;
	private JPanel panel1;
	private JLabel label1;
	private JButton closeButton;
	private JScrollPane scrollPane1;
	private JTable itemsTable;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
