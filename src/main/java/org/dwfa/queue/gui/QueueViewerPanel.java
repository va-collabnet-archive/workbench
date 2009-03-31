/*
 * Created on Apr 25, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.TreeSelectionModel;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.id.Uuid;
import net.jini.id.UuidFactory;
import net.jini.lookup.ServiceItemFilter;
import net.jini.lookup.entry.Name;

import org.dwfa.bpa.ExecutionRecord;
import org.dwfa.bpa.gui.ProcessPanel;
import org.dwfa.bpa.process.EntryID;
import org.dwfa.bpa.process.I_DescribeQueueEntry;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.NoMatchingEntryException;
import org.dwfa.bpa.util.SwingWorker;
import org.dwfa.bpa.util.TableSorter;
import org.dwfa.bpa.util.TableSorter.SortOrder;
import org.dwfa.jini.TransactionParticipantAggregator;
import org.dwfa.queue.ObjectServerCore;

/**
 * @author kec
 * 
 */
public class QueueViewerPanel extends JPanel {
	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	private I_Work worker;

	I_EncodeBusinessProcess process;

	MoveToDiskActionListener moveListener = new MoveToDiskActionListener();

	EntryID processEntryID;

	private static Logger logger = QueueViewerFrame.logger;

	private ServiceItemFilter queuefilter;

	public class ExecuteAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent evt) {
			executeActionListener.actionPerformed(evt);

		}

	}

	private class ExecuteProcessActionListener implements ActionListener {
		Configuration config;

		Uuid id;

		I_Work worker;

		String exceptionMessage = "";

		boolean indexOutOfBoundsExceptionThrown;

		/**
		 * @param config
		 * @param id
		 * @param worker
		 * @param frames
		 */
		public ExecuteProcessActionListener(Configuration config, Uuid id,
				I_Work worker) {
			super();
			this.config = config;
			this.id = id;
			this.worker = worker;
		}

		List<I_Work> cloneList = new ArrayList<I_Work>();

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {

			execute.setText("execute");
			execute.setEnabled(false);
			statusMessage.setText("<html><font color='red'>Executing process");
			Runnable r = new Runnable() {
				public void run() {
					ListSelectionModel lsm = tableOfQueueEntries
							.getSelectionModel();
					int firstSelectedRow = lsm.getMinSelectionIndex();
					int lastSelectedRow = lsm.getMaxSelectionIndex();
					if (logger.isLoggable(Level.FINE)) {
						logger.log(Level.FINE, "firstSelectedRow: "
								+ firstSelectedRow + " lastSelectedRow: "
								+ lastSelectedRow);

					}
					List<I_DescribeQueueEntry> selectedProcesses = new ArrayList<I_DescribeQueueEntry>();
					indexOutOfBoundsExceptionThrown = false;
					for (int i = firstSelectedRow; i <= lastSelectedRow; i++) {
						if (lsm.isSelectedIndex(i)) {
							try {
								int modelIndex = tableOfQueueEntriesSortingTable
										.modelIndex(i);
								I_DescribeQueueEntry processMeta = tableOfQueueEntriesModel
										.getRowMetaData(modelIndex);
								selectedProcesses.add(processMeta);
								if (logger.isLoggable(Level.FINE)) {
									logger
											.log(Level.FINE,
													"selectedProcesses: "
															+ processMeta);
								}
							} catch (IndexOutOfBoundsException e) {
								logger.log(Level.WARNING, e.toString(), e);
								indexOutOfBoundsExceptionThrown = true;
							}
						}
					}
					Iterator<I_DescribeQueueEntry> selectionItr = selectedProcesses
							.iterator();
					while (selectionItr.hasNext()) {
						I_DescribeQueueEntry processMeta = selectionItr.next();
						final String message = "<html><font color='red'>Executing process: "
								+ "<font color='blue'>" + processMeta.getName();
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								statusMessage.setText(message);
							}
						});
						try {
							I_EncodeBusinessProcess processToExecute = tableOfQueueEntriesModel
									.getQueue().take(processMeta.getEntryID(),
											worker.getActiveTransaction());
							if (worker.isExecuting()) {
								I_Work altWorker = null;
								for (I_Work alt : cloneList) {
									if (alt.isExecuting() == false) {
										altWorker = alt;
										break;
									}
								}
								if (altWorker == null) {
									altWorker = worker
											.getTransactionIndependentClone();
									cloneList.add(altWorker);
								}
								altWorker.execute(processToExecute);
							} else {
								worker.execute(processToExecute);
							}
							if (logger.isLoggable(Level.FINE)) {
								SortedSet<ExecutionRecord> sortedRecords = new TreeSet<ExecutionRecord>(
										processToExecute.getExecutionRecords());
								Iterator<ExecutionRecord> recordItr = sortedRecords
										.iterator();
								StringBuffer buff = new StringBuffer();
								while (recordItr.hasNext()) {
									ExecutionRecord rec = recordItr.next();
									buff.append("\n");
									buff.append(rec.toString());
								}
								logger.fine(buff.toString());
							}
							exceptionMessage = "";
						} catch (Throwable e1) {
							logger.log(Level.WARNING, e1.toString(), e1);
							exceptionMessage = e1.toString();
						}
					}

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							execute
									.setText("<html><font color='#006400'>execute");
							execute.setEnabled(true);
							if (exceptionMessage.equals("")) {
								statusMessage
										.setText("<html><font color='blue'>Process complete");
							} else {
								statusMessage
										.setText("<html><font color='blue'>Process complete: <font color='red'>"
												+ exceptionMessage);
							}
							refresh.doClick();
							if (indexOutOfBoundsExceptionThrown) {
								try {
									updateQueueModelAndDisplay(tableOfQueueEntriesModel
											.getQueue());
								} catch (Exception e) {
									logger.log(Level.WARNING, e.toString(), e);
								}
							}
							ListSelectionModel lsm = tableOfQueueEntries
									.getSelectionModel();
							lsm.setSelectionInterval(0, 0);
							execute.requestFocusInWindow();
						}
					});
				}

			};
			new Thread(r).start();

		}

	}

	public class MoveToDiskActionListener implements ActionListener {

		/**
		 * @param config
		 * @param id
		 * @param worker
		 * @param frames
		 */
		public MoveToDiskActionListener() {
			super();
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			// Create a file dialog box to prompt for a new file to display

			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser
					.setDialogTitle("Select directory to put selected processes");
			int returnVal = chooser.showDialog(QueueViewerPanel.this
					.getTopLevelAncestor(), "Select Directory");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				final File directory = chooser.getSelectedFile();
				execute.setEnabled(false);
				Runnable r = new Runnable() {
					public void run() {
						ListSelectionModel lsm = tableOfQueueEntries
								.getSelectionModel();
						int firstSelectedRow = lsm.getMinSelectionIndex();
						int lastSelectedRow = lsm.getMaxSelectionIndex();
						List<I_DescribeQueueEntry> selectedProcesses = new ArrayList<I_DescribeQueueEntry>();
						for (int i = firstSelectedRow; i <= lastSelectedRow; i++) {
							if (lsm.isSelectedIndex(i)) {
								I_DescribeQueueEntry processMeta = tableOfQueueEntriesModel
										.getRowMetaData(i);
								selectedProcesses.add(processMeta);
							}
						}
						Iterator<I_DescribeQueueEntry> selectionItr = selectedProcesses
								.iterator();
						while (selectionItr.hasNext()) {
							I_DescribeQueueEntry processMeta = selectionItr
									.next();
							final String message = "<html><font color='red'>Moving process to disk: "
									+ "<font color='blue'>"
									+ processMeta.getName();
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									statusMessage.setText(message);
								}
							});
							try {
								I_EncodeBusinessProcess processToExecute = tableOfQueueEntriesModel
										.getQueue().take(
												processMeta.getEntryID(),
												worker.getActiveTransaction());
								// write to disk here
								File processFile = new File(directory,
										processMeta.getName() + " - "
												+ processMeta.getProcessID()
												+ "."
												+ processMeta.getEntryID()
												+ ".bp");
								FileOutputStream fos = new FileOutputStream(
										processFile);
								BufferedOutputStream bos = new BufferedOutputStream(
										fos);
								ObjectOutputStream oos = new ObjectOutputStream(
										bos);
								oos.writeObject(processToExecute);
								oos.close();

								worker.commitActiveTransaction();
							} catch (Throwable e1) {
								logger.log(Level.WARNING, e1.toString(), e1);
							}
						}

						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								execute
										.setText("<html><font color='#006400'>execute");
								execute.setEnabled(true);
								statusMessage
										.setText("<html><font color='blue'>Take complete");
								refresh.doClick();
							}
						});
					}

				};
				new Thread(r).start();
			}

		}

	}

	private class CommitListener implements ActionListener {

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent arg0) {
			QueueAdaptor qAdaptor = null;
			if (tableOfQueues.getSelectedRow() >= 0) {
				qAdaptor = (QueueAdaptor) tableOfQueues.getModel().getValueAt(
						tableOfQueues.getSelectedRow(), 0);
			}
			// System.out.println(" Transaction committed. Performing queue refresh. ");

			if (qAdaptor == null) {
				// nothing to do...
			} else {
				try {
					updateQueueModelAndDisplay(qAdaptor.queue);

				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}

	}

	public class RefreshObjectServers extends SwingWorker<Boolean> {
		@Override
		protected Boolean construct() throws Exception {
			ObjectServerCore.refreshServers();
			return true;
		}

		@Override
		protected void finished() {
			try {
				get();
				QueueAdaptor qAdaptor = null;
				if (tableOfQueues.getSelectedRow() >= 0) {
					qAdaptor = (QueueAdaptor) tableOfQueues.getModel()
							.getValueAt(tableOfQueues.getSelectedRow(), 0);
				}

				if (qAdaptor == null) {
					// nothing to do...
					if (logger.isLoggable(Level.FINE)) {
						logger.fine("Refresh, but qAdaptor is null");
					}
				} else {
					try {
						if (logger.isLoggable(Level.FINE)) {
							logger.fine("Starting refresh for qAdaptor");
						}
						updateQueueModelAndDisplay(qAdaptor.queue);
					} catch (Exception e1) {
						logger.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
					}
				}
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			} catch (ExecutionException e) {
				logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}

	private class RefreshListener implements ActionListener {

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent arg0) {
			RefreshObjectServers ros = new RefreshObjectServers();
			ros.start();

			/*
			QueueAdaptor qAdaptor = null;
			if (tableOfQueues.getSelectedRow() >= 0) {
				qAdaptor = (QueueAdaptor) tableOfQueues.getModel().getValueAt(
						tableOfQueues.getSelectedRow(), 0);
			}

			if (qAdaptor == null) { // nothing to do...
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Refresh, but qAdaptor is null");
				}
			} else {
				try {
					if (logger.isLoggable(Level.FINE)) {
						logger.fine("Starting refresh for qAdaptor");
					}
					updateQueueModelAndDisplay(qAdaptor.queue);
				} catch (Exception e1) {
					logger.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
				}
			}
			*/
		}

	}

	private class ProcessSelectionActionListener implements
			ListSelectionListener {
		TableSorter sorter;

		public ProcessSelectionActionListener(TableSorter sorter) {
			this.sorter = sorter;
		}

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		public void valueChanged(ListSelectionEvent ev) {
			// Ignore extra messages.
			if (ev.getValueIsAdjusting()) {
				return;
			}
			ListSelectionModel lsm = (ListSelectionModel) ev.getSource();
			if (lsm.isSelectionEmpty()) {
				// no rows are selected
				int dividerLoc = queueContentsSplitPane.getDividerLocation();
				queueContentsSplitPane.setBottomComponent(new JLabel(
						"No process is selected"));
				queueContentsSplitPane.setDividerLocation(dividerLoc);
				execute.setText("execute");
				execute.setEnabled(false);
				process = null;
			} else {
				try {
					if (lsm.getMinSelectionIndex() < sorter.getRowCount()) {
						int firstSelectedRow = sorter.modelIndex(lsm
								.getMinSelectionIndex());
						tableOfQueueEntriesModel.updateQueueData();
						I_DescribeQueueEntry processMeta = tableOfQueueEntriesModel
								.getRowMetaData(firstSelectedRow);
						processEntryID = processMeta.getEntryID();
						try {
							process = tableOfQueueEntriesModel.getQueue().read(
									processMeta.getEntryID(), null);
							ProcessPanel processPanel = new ProcessPanel(
									process, worker, null);
							int dividerLoc = queueContentsSplitPane
									.getDividerLocation();
							queueContentsSplitPane
									.setBottomComponent(new JScrollPane(
											processPanel));
							queueContentsSplitPane
									.setDividerLocation(dividerLoc);
							execute
									.setText("<html><font color='#006400'>execute");
							execute.setEnabled(true);
						} catch (NoMatchingEntryException ex) {
							logger.info(" NoMatchingEntry: " + ex);
							lsm.clearSelection();
							int dividerLoc = queueContentsSplitPane
									.getDividerLocation();
							queueContentsSplitPane
									.setBottomComponent(new JLabel(
											"No matching entry"));
							queueContentsSplitPane
									.setDividerLocation(dividerLoc);
							execute.setText("execute");
							execute.setEnabled(false);
						}
					} else {
						lsm.clearSelection();
						int dividerLoc = queueContentsSplitPane
								.getDividerLocation();
						queueContentsSplitPane.setBottomComponent(new JLabel(
								"No selected entry"));
						queueContentsSplitPane.setDividerLocation(dividerLoc);
						execute.setText("execute");
						execute.setEnabled(false);
					}
				} catch (Exception ex) {
					logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
					int dividerLoc = queueContentsSplitPane
							.getDividerLocation();
					queueContentsSplitPane.setBottomComponent(new JLabel(ex
							.getMessage()));
					queueContentsSplitPane.setDividerLocation(dividerLoc);
				}
			}

		}

	}

	private class QueueSelectionListener implements ListSelectionListener {

		public void valueChanged(ListSelectionEvent e) {
			if (tableOfQueues.getSelectedRow() >= 0) {
				QueueAdaptor qAdaptor = (QueueAdaptor) tableOfQueues.getModel()
						.getValueAt(tableOfQueues.getSelectedRow(), 0);

				if (qAdaptor == null) {
					clearQueuePanel();
				} else {
					try {
						updateQueueModelAndDisplay(qAdaptor.queue);
					} catch (Exception e1) {
						int dividerLoc = queueContentsSplitPane
								.getDividerLocation();
						queueContentsSplitPane.setTopComponent(new JPanel());
						queueContentsSplitPane.setBottomComponent(new JLabel(
								"<html>No process is selected secondary to exception: <br>"
										+ e1.toString()));
						queueContentsSplitPane.setDividerLocation(dividerLoc);
						logger.log(Level.WARNING, e1.toString(), e1);
					}
				}
			} else {
				clearQueuePanel();
			}
		}

		private void clearQueuePanel() {
			int dividerLoc = queueContentsSplitPane.getDividerLocation();
			queueContentsSplitPane.setTopComponent(new JPanel());
			queueContentsSplitPane.setBottomComponent(new JLabel(
					"No process is selected"));
			queueContentsSplitPane.setDividerLocation(dividerLoc);
		}

	}

	JSplitPane splitPane = new JSplitPane();

	JSplitPane queueContentsSplitPane = new JSplitPane(
			JSplitPane.VERTICAL_SPLIT);

	private JTable tableOfQueues;

	private ListOfQueuesTableModel tableOfQueuesModel;

	private JButton execute = new JButton("execute");

	private JButton refresh = new JButton("<html><font color='#006400'>refresh");

	private JLabel statusMessage = new JLabel();

	QueueTableModel tableOfQueueEntriesModel;

	JTable tableOfQueueEntries;

	private ExecuteProcessActionListener executeActionListener;

	public QueueViewerPanel(Configuration jiniConfig, I_Work worker)
			throws Exception {
		this(jiniConfig, worker, null);
	}

	public QueueViewerPanel(Configuration jiniConfig, I_Work worker,
			ServiceItemFilter queuefilter) throws Exception {
		super(new GridBagLayout());
		this.queuefilter = queuefilter;
		this.worker = worker;
		this.splitPane.setLeftComponent(createTableOfQueues());
		this.splitPane.setRightComponent(this.queueContentsSplitPane);
		this.splitPane.setDividerLocation(250);
		this.splitPane.setOneTouchExpandable(true);
		this.queueContentsSplitPane.setTopComponent(new JPanel());
		this.queueContentsSplitPane.setBottomComponent(new JLabel(
				"No process selected."));
		this.queueContentsSplitPane.setDividerLocation(225);
		this.queueContentsSplitPane.setOneTouchExpandable(true);
		this.refresh.addActionListener(new RefreshListener());
		// splitPane.setDividerLocation(0);
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 0;
		this.add(this.splitPane, c);
		this.execute.setEnabled(false);
		this.executeActionListener = new ExecuteProcessActionListener(
				jiniConfig, UuidFactory.generate(), worker);
		this.execute.addActionListener(executeActionListener);
		JPanel statusPanel = makeStatusPanel(this.statusMessage, refresh,
				execute);
		c.weighty = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		this.add(statusPanel, c);
		getQueues(QueueViewerPanel.this.worker);
		TransactionParticipantAggregator
				.addCommitListener(new CommitListener());

	}

	/**
	 * @param worker
	 * @throws InterruptedException
	 * @throws RemoteException
	 * @throws PrivilegedActionException
	 * @throws ConfigurationException
	 */
	@SuppressWarnings("unchecked")
	private void getQueues(I_Work worker) throws InterruptedException,
			RemoteException, PrivilegedActionException, ConfigurationException {
		worker.doAsPrivileged(getQueuesAction, null);
	}

	GetQueuesAsWorker getQueuesAction = new GetQueuesAsWorker();

	private TableSorter tableOfQueuesSortingTable;

	private TableSorter tableOfQueueEntriesSortingTable;

	/**
	 * @param worker
	 * @throws InterruptedException
	 * @throws RemoteException
	 * @throws PrivilegedActionException
	 * @throws ConfigurationException
	 */
	@SuppressWarnings("unchecked")
	private class GetQueuesAsWorker implements PrivilegedExceptionAction {

		public Object run() throws Exception {
			ServiceTemplate template = new ServiceTemplate(null,
					new Class[] { I_QueueProcesses.class }, null);

			ServiceItem[] services = worker.lookup(template, 1, 50,
					queuefilter, 3000);

			if (logger.isLoggable(Level.FINE)) {
				logger.info("Found " + services.length
						+ " matching I_QueueProcesses services...");
			}
			for (int i = 0; i < services.length; i++) {
				if (logger.isLoggable(Level.FINE)) {
					logger.fine("Service[" + i + "]: " + services[i].service
							+ " Attributes: "
							+ Arrays.asList(services[i].attributeSets));
				}
				I_QueueProcesses queue = (I_QueueProcesses) services[i].service;
				/* Prepare the server proxy */

				queue = (I_QueueProcesses) worker.prepareProxy(queue,
						I_QueueProcesses.class);

				String queueName = "unnamed queue";
				for (int j = 0; j < services[i].attributeSets.length; j++) {
					if (services[i].attributeSets[j] != null) {
						if (Name.class
								.isAssignableFrom(services[i].attributeSets[j]
										.getClass())) {
							Name nameEntry = (Name) services[i].attributeSets[j];
							queueName = nameEntry.name;
						}
					}
				}
				QueueViewerPanel.this.addQueue(queue, queueName,
						services[i].serviceID);

			}
			return null;
		}
	}

	private JPanel makeStatusPanel(JLabel statusMessage, JButton refresh,
			JButton execute) {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		c.gridy = 0;
		c.gridx = 6;
		panel.add(new JLabel("    "), c); // filler for grow box.

		c.gridx = 5;
		panel.add(refresh, c);
		c.gridx = 4;
		panel.add(execute, c);
		c.gridx = 3;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(statusMessage, c);
		c.weightx = 0;
		c.gridx = 2;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		panel.add(new JLabel("   "), c);
		c.gridx = 1;
		// ButtonGroup group = new ButtonGroup();
		// JRadioButton taskButton = new JRadioButton("task");
		// taskButton.setSelected(true);
		// taskButton.addActionListener(new ChangeToTaskActionListener());
		// group.add(taskButton);
		panel.add(new JPanel(), c);
		c.gridx = 0;
		// JRadioButton dataButton = new JRadioButton("data");
		// dataButton.setSelected(false);
		// group.add(dataButton);
		// dataButton.addActionListener(new ChangeToDataActionListener());
		panel.add(new JPanel(), c);

		return panel;
	}

	private JScrollPane createTableOfQueues() {
		tableOfQueuesModel = new ListOfQueuesTableModel();
		tableOfQueuesSortingTable = new TableSorter(tableOfQueuesModel);
		tableOfQueuesSortingTable.setSortingStatus(0, SortOrder.ASCENDING);
		tableOfQueues = new JTable(tableOfQueuesSortingTable);
		tableOfQueuesSortingTable
				.setTableHeader(tableOfQueues.getTableHeader());
		// Set up tool tips for column headers.
		tableOfQueuesSortingTable
				.getTableHeader()
				.setToolTipText(
						"Click to specify sorting; Control-Click to specify secondary sorting");
		// Create a tableOfQueues that allows one selection at a time.
		tableOfQueues.getSelectionModel().addListSelectionListener(
				new QueueSelectionListener());
		tableOfQueues.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		ToolTipManager.sharedInstance().registerComponent(tableOfQueues);
		return new JScrollPane(tableOfQueues);
	}

	/**
	 * @param queue
	 * @param queueName
	 * @throws PrivilegedActionException
	 * @throws ConfigurationException
	 */
	public void addQueue(I_QueueProcesses queue, String queueName, ServiceID id)
			throws ConfigurationException, PrivilegedActionException {
		QueueAdaptor qAdaptor = new QueueAdaptor(queue, queueName, id);
		this.tableOfQueuesModel.addQueue(qAdaptor);
	}

	public void refreshQueues() throws RemoteException, InterruptedException,
			PrivilegedActionException, ConfigurationException {
		this.tableOfQueues.clearSelection();
		this.tableOfQueuesModel.clear();
		getQueues(QueueViewerPanel.this.worker);
	}

	/**
	 * @return Returns the moveListener.
	 */
	public MoveToDiskActionListener getMoveListener() {
		return moveListener;
	}

	public I_Work getWorker() {
		return worker;
	}

	private I_QueueProcesses selectedQueue;

	/**
	 * @param queue
	 * @throws RemoteException
	 * @throws IOException
	 */
	private void updateQueueModelAndDisplay(final I_QueueProcesses queue)
			throws RemoteException, IOException {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					if (selectedQueue != queue) {
						selectedQueue = queue;
						tableOfQueueEntriesModel = new QueueTableModel(queue);
						tableOfQueueEntriesSortingTable = new TableSorter(
								tableOfQueueEntriesModel);
						tableOfQueueEntries = new JTable(
								tableOfQueueEntriesSortingTable);

						tableOfQueueEntries.getInputMap().put(
								KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
								"executeTask");
						tableOfQueueEntries.getInputMap().put(
								KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
								"executeTask");
						tableOfQueueEntries.getActionMap().put("executeTask",
								new ExecuteAction());

						tableOfQueueEntriesSortingTable
								.setTableHeader(tableOfQueueEntries
										.getTableHeader());

						// Set up tool tips for column headers.
						tableOfQueueEntriesSortingTable
								.getTableHeader()
								.setToolTipText(
										"Click to specify sorting; Control-Click to specify secondary sorting");

						tableOfQueueEntries
								.getSelectionModel()
								.addListSelectionListener(
										new ProcessSelectionActionListener(
												tableOfQueueEntriesSortingTable));
						queueContentsSplitPane.setTopComponent(new JScrollPane(
								tableOfQueueEntries));
						int dividerLoc = queueContentsSplitPane
								.getDividerLocation();
						queueContentsSplitPane.setBottomComponent(new JLabel(
								"No process is selected"));
						queueContentsSplitPane.setDividerLocation(dividerLoc);
					} else {
						int selectedRow = tableOfQueueEntries.getSelectedRow();
						EntryID selectedEntry = null;
						if (selectedRow >= 0
								&& selectedRow < tableOfQueueEntries
										.getRowCount()) {
							selectedEntry = (EntryID) tableOfQueueEntries
									.getModel().getValueAt(selectedRow, 6);
						}
						tableOfQueueEntriesModel.updateQueueData();
						if (selectedEntry != null) {
							boolean entryFound = false;
							for (int row = 0; row < tableOfQueueEntries
									.getRowCount(); row++) {
								EntryID entry = (EntryID) tableOfQueueEntries
										.getModel().getValueAt(row, 6);
								if (entry.equals(selectedEntry)) {
									tableOfQueueEntries.getSelectionModel()
											.addSelectionInterval(row, row);
									entryFound = true;
									break;
								}
							}
							if (entryFound == false) {
								tableOfQueueEntries.getSelectionModel()
										.setSelectionInterval(0, 0);
							}
						} else {
							tableOfQueueEntries.getSelectionModel()
									.setSelectionInterval(0, 0);
						}
						tableOfQueueEntries.requestFocusInWindow();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.log(Level.SEVERE, e.toString(), e);
				}
			}

		});

	}

	public void requestFocusOnEntry() {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				if (tableOfQueueEntries != null) {
					tableOfQueueEntries.requestFocusInWindow();
				}
			}

		});
	}

}
