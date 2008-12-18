package org.dwfa.ace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import javax.security.auth.login.LoginException;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.lookup.ServiceItemFilter;

import org.dwfa.ace.actions.Abort;
import org.dwfa.ace.actions.ChangeFramePassword;
import org.dwfa.ace.actions.Commit;
import org.dwfa.ace.actions.ImportBaselineJar;
import org.dwfa.ace.actions.ImportChangesetJar;
import org.dwfa.ace.actions.ImportJavaChangeset;
import org.dwfa.ace.actions.SaveProfile;
import org.dwfa.ace.actions.SaveProfileAs;
import org.dwfa.ace.actions.WriteJar;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.api.AceEditor;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.api.I_HostConceptPlugins.LINK_TYPE;
import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.checks.UncommittedListModel;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.config.CreatePathPanel;
import org.dwfa.ace.config.FrameConfigSnapshot;
import org.dwfa.ace.config.SelectPathAndPositionPanel;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.gui.concept.ConceptPanel;
import org.dwfa.ace.list.TerminologyIntList;
import org.dwfa.ace.list.TerminologyIntListModel;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.search.SearchPanel;
import org.dwfa.ace.table.refset.RefsetDefaults;
import org.dwfa.ace.table.refset.RefsetDefaultsConcept;
import org.dwfa.ace.table.refset.RefsetDefaultsLanguage;
import org.dwfa.ace.table.refset.RefsetDefaultsLanguageScoped;
import org.dwfa.ace.table.refset.RefsetDefaultsMeasurement;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.ace.task.commit.I_Fixup;
import org.dwfa.ace.task.commit.I_TestDataConstraints;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure.ALERT_TYPE;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.ace.tree.CompareConceptBeansForTree;
import org.dwfa.ace.tree.ConceptBeanForTree;
import org.dwfa.ace.tree.ExpandNodeSwingWorker;
import org.dwfa.ace.tree.I_GetConceptDataForTree;
import org.dwfa.ace.tree.JTreeWithDragImage;
import org.dwfa.ace.tree.TermTreeCellRenderer;
import org.dwfa.ace.tree.TreeIdPath;
import org.dwfa.ace.tree.TreeMouseListener;
import org.dwfa.ace.utypes.UniversalIdList;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.ExecutionRecord;
import org.dwfa.bpa.gui.glue.PropertyListenerGlue;
import org.dwfa.bpa.gui.glue.PropertySetListenerGlue;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.tasks.editor.CheckboxEditor;
import org.dwfa.bpa.util.I_DoQuitActions;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.queue.gui.QueueViewerPanel;
import org.dwfa.svn.SvnPanel;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ExtensionByReferenceBean;
import org.dwfa.vodb.types.IntList;

import com.sleepycat.je.DatabaseException;

public class ACE extends JPanel implements PropertyChangeListener,
		I_DoQuitActions {

	private static ImageIcon errorIcon = new ImageIcon(ACE.class
			.getResource("/24x24/plain/forbidden.png"));
	private static ImageIcon warningIcon = new ImageIcon(ACE.class
			.getResource("/24x24/plain/warning.png"));
	private static ImageIcon informationalIcon = new ImageIcon(ACE.class
			.getResource("/24x24/plain/information.png"));
	private static ImageIcon resolvedIcon = new ImageIcon(ACE.class
			.getResource("/24x24/plain/transform.png"));
	
	private List<TermComponentDataCheckSelectionListener> dataCheckListeners = new ArrayList<TermComponentDataCheckSelectionListener>();
	
	public void addDataCheckListener(TermComponentDataCheckSelectionListener l) {
		dataCheckListeners.add(l);
	}

	public class ProcessMenuActionListener implements ActionListener {
	    private class MenuProcessThread implements Runnable {
	        
	        private String action;
	        
	        
	        /**
	         * @param action
	         */
	        public MenuProcessThread(String action) {
	            super();
	            this.action = action;
	        }


	        public void run() {
	            try {
	                ObjectInputStream ois = new ObjectInputStream(
	                        new BufferedInputStream(new FileInputStream(
	                                processFile)));
	                I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois
	                        .readObject();
	                ois.close();
	                if (worker.isExecuting()) {
	                	worker = worker.getTransactionIndependentClone();
	                }
	                process.execute(worker);
	                worker.commitTransactionIfActive();
	            } catch (Exception ex) {

	                worker.getLogger().log(Level.SEVERE, ex.getMessage(),
	                        ex);
	                JOptionPane.showMessageDialog(null, "<html>Exception processing action: " + 
	                        action + "<p><p>" + 
	                        ex.getMessage() + "<p><p>See log for details.");
	            }
	        }
	    };
	    private File processFile;
	    private I_Work worker;

	    public ProcessMenuActionListener(File processFile, I_Work worker) {
	        super();
	        this.processFile = processFile;
	        this.worker = worker;
	    }

	    public void actionPerformed(ActionEvent e) {
	        new Thread(new MenuProcessThread(e.getActionCommand()), "Menu Process Execution").start();
	    }
	}
	
	public void removeDataCheckListener(TermComponentDataCheckSelectionListener l) {
		dataCheckListeners.remove(l);
	}

	public class ListenForDataChecks implements ListDataListener,
			ActionListener {

		public void contentsChanged(ListDataEvent listEvt) {
			layoutAlerts();
		}

		public void intervalAdded(ListDataEvent arg0) {
			layoutAlerts();
		}

		public void intervalRemoved(ListDataEvent arg0) {
			layoutAlerts();
		}

		private void layoutAlerts() {
			JPanel uncommittedListPanel = new JPanel(new GridBagLayout());
			JScrollPane uncommittedComponentScroller = new JScrollPane(
					uncommittedListPanel);
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.gridheight = 1;
			c.gridwidth = 1;
			c.anchor = GridBagConstraints.NORTHWEST;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1;
			c.weighty = 0;

			for (AlertToDataConstraintFailure alert : dataCheckListModel) {
				setupAlert(alert);
				uncommittedListPanel.add(alert.getRendererComponent(), c);
				c.gridy++;
			}

			c.weighty = 1;
			c.gridy++;
			uncommittedListPanel.add(new JPanel(), c);
			
			int dataCheckIndex = leftTabs.indexOfTab(dataCheckTabLabel);
			leftTabs.setComponentAt(dataCheckIndex, uncommittedComponentScroller);
			int taxonomyIndex = leftTabs.indexOfTab(taxonomyTabLabel);
			if (dataCheckListModel.size() > 0) {
				leftTabs.setSelectedIndex(dataCheckIndex);
				if (dataCheckPanel == null) {
					try {
						dataCheckPanel = new ConceptPanel(ACE.this, LINK_TYPE.DATA_CHECK_LINK,
								conceptTabs, Integer.MAX_VALUE);
						conceptPanels.add(dataCheckPanel);
					} catch (DatabaseException e) {
						AceLog.getAppLog().alertAndLogException(e);
					} catch (IOException e) {
						AceLog.getAppLog().alertAndLogException(e);
					} catch (ClassNotFoundException e) {
						AceLog.getAppLog().alertAndLogException(e);
					}
				}
				conceptTabs.addTab("Checks", ConceptPanel.SMALL_ALERT_LINK_ICON, dataCheckPanel, "Data Checks Linked");
				
			} else {
				leftTabs.setSelectedIndex(taxonomyIndex);
				if (dataCheckPanel != null) {
					if (conceptTabs.indexOfComponent(dataCheckPanel) >= 0) {
						conceptTabs.remove(dataCheckPanel);
						dataCheckPanel.setTermComponent(null);
					}
				}
			}
		}

		
		private void setupAlert(AlertToDataConstraintFailure alert) {
			if (alert.getRendererComponent() == null) {

				JLabel label = new JLabel();
				label.setText(alert.getAlertMessage());
				switch (alert.getAlertType()) {
				case ERROR:
					label.setIcon(errorIcon);
				case INFORMATIONAL: 
					label.setIcon(informationalIcon);
					break;
				case RESOLVED: 
					label.setIcon(resolvedIcon);
					break;
				case WARNING:
					label.setIcon(warningIcon);
					break;
				}

				JPanel componentPanel = new JPanel(new GridBagLayout());
				
				
				GridBagConstraints c = new GridBagConstraints();
				c.gridx = 0;
				c.gridy = 0;
				c.gridheight = 1;
				c.gridwidth = 2;
				c.anchor = GridBagConstraints.NORTHWEST;
				c.fill = GridBagConstraints.HORIZONTAL;
				c.weightx = 1;
				c.weighty = 0;
				componentPanel.add(label, c);
				c.weightx = 0;
				c.gridwidth = 1;
				c.gridy++;
				c.anchor = GridBagConstraints.EAST;
				if (alert.getFixOptions() != null
						&& alert.getFixOptions().size() > 0) {
					componentPanel.add(new JLabel("fixes: "), c);
					c.anchor = GridBagConstraints.WEST;
					c.weightx = 1;
					c.gridx++;
					List<Object> fixList = new ArrayList<Object>();
					fixList.add(" ");
					fixList.addAll(alert.getFixOptions());
					JComboBox testCombo = new JComboBox(fixList.toArray());
					testCombo.addActionListener(this);
					componentPanel.add(testCombo, c);
				}
				boolean isSelected = false;
				if (isSelected) {
					componentPanel.setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createMatteBorder(0, 1, 1, 1, Color.BLUE), 
							BorderFactory.createEmptyBorder(1,0,0,0)));
				} else {
					componentPanel.setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK), 
							BorderFactory.createEmptyBorder(1,1,0,1)));
				}
				
				addFocus(componentPanel, alert);
				addFocus(label, alert);

				alert.setRendererComponent(componentPanel);
			}
		}

		private void addFocus(JComponent component, final AlertToDataConstraintFailure alert) {
			component.setFocusable(true);
			component.setEnabled(true);
			component.setRequestFocusEnabled(true);
			component.addMouseListener(new MouseListener() {

				public void mouseClicked(MouseEvent e) {
					// nothing to do
				}

				public void mouseEntered(MouseEvent e) {
					// nothing to do
				}

				public void mouseExited(MouseEvent e) {
					// nothing to do
				}

				public void mousePressed(MouseEvent e) {
					// nothing to do
				}

				public void mouseReleased(MouseEvent e) {
					AceLog.getAppLog().info("Mouse released in alert...");
					for (TermComponentDataCheckSelectionListener l: dataCheckListeners) {
						l.setSelection(alert.getConceptWithAlert());
					}
				}
				
			});
			
			/*
			Could not get the focus system working. TODO get focus system working with alerts. 
			component.addFocusListener(new FocusListener() {

				public void focusGained(FocusEvent e) {
					AceLog.getAppLog().info("Alert is now focused");
				}

				public void focusLost(FocusEvent e) {
					// nothing to do...
					
				}
				
			});
			
			*/
		}

		public void actionPerformed(ActionEvent evt) {
			JComboBox comboBox = (JComboBox) evt.getSource();
			if (I_Fixup.class.isAssignableFrom(comboBox.getSelectedItem().getClass())) {
				I_Fixup fixup = (I_Fixup) comboBox.getSelectedItem();
				try {
					fixup.fix();
					//ACE.fireCommit();
				} catch (Exception ex) {
					AceLog.getAppLog().alertAndLogException(ex);
				}
			}

		}
	}

	public class SetRefsetInToggleVisible implements ActionListener {

		EXT_TYPE type;

		TOGGLES t;

		public SetRefsetInToggleVisible(EXT_TYPE type, TOGGLES t) {
			super();
			this.type = type;
			this.t = t;
		}

		public void actionPerformed(ActionEvent evt) {
			JToggleButton button = (JToggleButton) evt.getSource();
			aceFrameConfig.setRefsetInToggleVisible(type, t, button
					.isSelected());
		}
	}

	public class SetToggleVisibleListener implements ActionListener {
		TOGGLES t;

		public SetToggleVisibleListener(TOGGLES t) {
			super();
			this.t = t;
		}

		public void actionPerformed(ActionEvent evt) {
			JToggleButton button = (JToggleButton) evt.getSource();
			aceFrameConfig.setTogglesInComponentPanelVisible(t, button
					.isSelected());
		}
	}

	private static Set<I_Transact> uncommitted = Collections
			.synchronizedSet(new HashSet<I_Transact>());
	
	private static Map<I_GetConceptData, Collection<AlertToDataConstraintFailure>> dataCheckMap = new HashMap<I_GetConceptData, Collection<AlertToDataConstraintFailure>>();
	
	private static int maxHistoryListSize = 100;

	private static LinkedList<I_Transact> imported = new LinkedList<I_Transact>();

	public static void addImported(I_Transact to) {
		imported.addLast(to);
		while (imported.size() > maxHistoryListSize) {
			imported.removeFirst();
		}

		if (aceConfig != null) {
			for (I_ConfigAceFrame frameConfig : getAceConfig().aceFrames) {
				frameConfig.setCommitEnabled(true);
				if (ConceptBean.class.isAssignableFrom(to.getClass())) {
					frameConfig.addImported((I_GetConceptData) to);
				}
			}
		}
	}

	private static List<I_TestDataConstraints> commitTests = new ArrayList<I_TestDataConstraints>();

	private static List<I_TestDataConstraints> creationTests = new ArrayList<I_TestDataConstraints>();

	public static void addUncommitted(I_Transact to) {
		I_Transact extraToAdd = null;
		ConceptBean uncommittedBean = null;
		if (ExtensionByReferenceBean.class.isAssignableFrom(to.getClass())) {
			ExtensionByReferenceBean eb = (ExtensionByReferenceBean) to;
			try {
				if (eb.isUncommitted() == false) {
					removeUncommitted(to);
					uncommittedBean = ConceptBean.get(eb.getExtension()
							.getComponentId());
					if (uncommittedBean.isUncommitted() && uncommittedBean.isExtensionUncommitted()) {
						removeUncommitted(uncommittedBean);
					}
					if (eb.getExtension().getVersions().size() == 0) {
						eb.abort();
					}
					List<AlertToDataConstraintFailure> warningsAndErrors = new ArrayList<AlertToDataConstraintFailure>();
					dataCheckMap.put(uncommittedBean, warningsAndErrors);
					addUncommitted(uncommittedBean);
					 for (I_ThinExtByRefVersioned ext: LocalVersionedTerminology.get().getAllExtensionsForComponent(uncommittedBean.getConceptId(), true)) {
						 for (I_ThinExtByRefPart part: ext.getVersions()) {
							 if (part.getVersion() == Integer.MAX_VALUE) {
								 addUncommitted(ExtensionByReferenceBean.get(ext.getMemberId()));
								 break;
							 }
						 }
					 }
					return;
				} else {
					extraToAdd = ConceptBean.get(eb.getExtension()
							.getComponentId());
				}
			} catch (IOException e) {
				AceLog.getEditLog().alertAndLogException(e);
			}
		}
		if (ConceptBean.class.isAssignableFrom(to.getClass())) {
			uncommittedBean = (ConceptBean) to;
			try {
				if (uncommittedBean.isUncommitted() == false 
						&& uncommittedBean.isExtensionUncommitted() == false) {
					dataCheckMap.remove(uncommittedBean);
					removeUncommitted(to);
					return;
				}
			} catch (IOException e) {
				AceLog.getEditLog().alertAndLogException(e);
			}
		}
		List<AlertToDataConstraintFailure> warningsAndErrors = new ArrayList<AlertToDataConstraintFailure>();
		dataCheckMap.put(uncommittedBean, warningsAndErrors);
		for (I_TestDataConstraints test : creationTests) {
			try {
				warningsAndErrors.addAll(test.test(to, false));
			} catch (Exception e) {
				AceLog.getEditLog().alertAndLogException(e);
			}
		}
		uncommitted.add(to);
		if (extraToAdd != null) {
			addUncommitted(extraToAdd);
		}
		if (aceConfig != null) {
			for (I_ConfigAceFrame frameConfig : getAceConfig().aceFrames) {
				frameConfig.setCommitEnabled(true);
				updateAlerts(frameConfig);
				if (ConceptBean.class.isAssignableFrom(to.getClass())) {
					ConceptBean cb = (ConceptBean) to;
					try {
						if (cb.isUncommitted() || cb.isExtensionUncommitted()) {
							frameConfig.addUncommitted(cb);
						} else {
							frameConfig.removeUncommitted(cb);
						}
					} catch (IOException e) {
						AceLog.getEditLog().alertAndLogException(e);
					}
				}
			}
		}
	}
	
	public static void updateAlerts(I_ConfigAceFrame frameConfig) {
		ACE aceInstance = ((AceFrameConfig) frameConfig).getAceFrame()
				.getCdePanel();
		aceInstance.getUncommittedListModel().clear();
		
		
		
		for (Collection<AlertToDataConstraintFailure> alerts: dataCheckMap.values()) {
			aceInstance.getUncommittedListModel().addAll(alerts);
		}
		if (aceInstance.getUncommittedListModel().size() > 0) {
			for (int i = 0; i < aceInstance.leftTabs.getTabCount(); i++) {
				if (aceInstance.leftTabs.getTitleAt(i).equals(
						dataCheckTabLabel)) {
					aceInstance.leftTabs.setSelectedIndex(i);
					break;
				}
			}
		} else {
			for (TermComponentDataCheckSelectionListener l: aceInstance.dataCheckListeners) {
				l.setSelection(null);
			}
		}
	}

	public static void removeUncommitted(I_Transact to) {
		uncommitted.remove(to);
		if (aceConfig != null) {
			for (I_ConfigAceFrame frameConfig : getAceConfig().aceFrames) {
				if (ConceptBean.class.isAssignableFrom(to.getClass())) {
					frameConfig.removeUncommitted((I_GetConceptData) to);
					updateAlerts(frameConfig);
				}
				if (uncommitted.size() == 0) {
					frameConfig.setCommitEnabled(false);
				}
			}
		}
	}

	private static Set<I_WriteChangeSet> csWriters = new HashSet<I_WriteChangeSet>();

	private static Set<I_ReadChangeSet> csReaders = new HashSet<I_ReadChangeSet>();

	protected final static int MENU_MASK = getMenuMask();

	private static int getMenuMask() {
		try {
			return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		} catch (HeadlessException e) {
			//
		}
		return 0;
	}

	private static boolean writeChangeSets = true;

	public static void resumeChangeSetWriters() {
		writeChangeSets = true;
	}

	public static void suspendChangeSetWriters() {
		writeChangeSets = false;
	}
	
	public static int commitSequence = 0;

	/*
	 * 
	 */
	public static void commit() throws IOException {
		commitSequence++;
		synchronized (uncommitted) {
			boolean testFailures = false;
			Set<I_Transact> testFailureSet = new HashSet<I_Transact>();
			List<AlertToDataConstraintFailure> warningsAndErrors = new ArrayList<AlertToDataConstraintFailure>();
			AceLog.getEditLog().info("Uncommitted count: " + uncommitted.size());
			AceLog.getEditLog().finer("Uncommitted set: " + uncommitted);
			for (I_Transact to : uncommitted) {
				for (I_TestDataConstraints test : commitTests) {
					try {
						for (AlertToDataConstraintFailure failure : test.test(
								to, true)) {
							warningsAndErrors.add(failure);
							if (failure.getAlertType() == ALERT_TYPE.ERROR) {
								testFailureSet.add(to);
								testFailures = true;
							}
						}

					} catch (Exception e) {
						AceLog.getEditLog().alertAndLogException(e);
					}
				}
			}

			if (testFailures) {
				int n = JOptionPane
						.showConfirmDialog(
								null,
								"Would you like to cancel the commit?\n"
										+ "If you continue, components with test failures will be rolled back prior to commit.\n  ",
								"Failures Detected", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
				if (n == JOptionPane.YES_OPTION) {
					return;
				}
				for (I_Transact to : testFailureSet) {
					to.abort();
				}
			}

			Date now = new Date();
			Set<TimePathId> values = new HashSet<TimePathId>();
			if (writeChangeSets) {
				for (I_WriteChangeSet writer : csWriters) {
					AceLog.getEditLog().info(
							"Opening writer: " + writer.toString());
					writer.open();
				}
			}
			int version = ThinVersionHelper.convert(now.getTime());
			AceLog.getEditLog().info(
					"Starting commit: " + version + " (" + now.getTime() + ")");

			UniversalIdList uncommittedIds = new UniversalIdList();

			for (I_Transact cb : uncommitted) {
				if (I_GetConceptData.class.isAssignableFrom(cb.getClass())) {
					I_GetConceptData igcd = (I_GetConceptData) cb;
					for (int nid : igcd.getUncommittedIds().getSetValues()) {
						I_IdVersioned idv = AceConfig.getVodb().getId(nid);
						try {
							uncommittedIds.getUncommittedIds().add(
									idv.getUniversal());
						} catch (TerminologyException e) {
							throw new ToIoException(e);
						}
					}
				} else if (ExtensionByReferenceBean.class.isAssignableFrom(cb
						.getClass())) {
					ExtensionByReferenceBean ebrBean = (ExtensionByReferenceBean) cb;
					if (ebrBean.isFirstCommit()) {
						I_IdVersioned idv = AceConfig.getVodb().getId(
								ebrBean.getMemberId());
						try {
							uncommittedIds.getUncommittedIds().add(
									idv.getUniversal());
						} catch (TerminologyException e) {
							throw new ToIoException(e);
						}
					}
				}
			}
			if (writeChangeSets) {
				if (uncommitted.size() > 0) {
					for (I_WriteChangeSet writer : csWriters) {
						writer.writeChanges(uncommittedIds, ThinVersionHelper
								.convert(version));
					}
					for (I_Transact cb : uncommitted) {
						for (I_WriteChangeSet writer : csWriters) {
							writer.writeChanges(cb, ThinVersionHelper
									.convert(version));
						}
					}
				}
			}

			try {
		        if (VodbEnv.isTransactional()) {
		        	AceConfig.getVodb().startTransaction();
		        }

				for (I_Transact cb : uncommitted) {
					cb.commit(version, values);
				}
				AceConfig.getVodb().addPositions(values);
		        if (VodbEnv.isTransactional()) {
		        	AceConfig.getVodb().commitTransaction();
		        }
				AceConfig.getVodb().sync();
			} catch (DatabaseException e) {
		        if (VodbEnv.isTransactional()) {
		        	AceConfig.getVodb().cancelTransaction();
		        }
				throw new ToIoException(e);
			}
			if (writeChangeSets) {
				for (I_WriteChangeSet writer : csWriters) {
					AceLog.getEditLog().info(
							"Committing writer: " + writer.toString());
					writer.commit();
				}
			}
			uncommitted.clear();
			fireCommit();
			AceLog.getEditLog().info(
					"Finished commit: " + version + " (" + now.getTime() + ")");
			if (aceConfig != null) {
				for (I_ConfigAceFrame frameConfig : getAceConfig().aceFrames) {
					frameConfig.setCommitEnabled(true);
					ACE aceInstance = ((AceFrameConfig) frameConfig)
							.getAceFrame().getCdePanel();
					aceInstance.getUncommittedListModel().clear();
				}
			}
		}
	}

	public static void abort() throws IOException {
		for (I_Transact cb : uncommitted) {
			cb.abort();
		}
		uncommitted.clear();
		dataCheckMap.clear();
		fireCommit();
	}

	private static void fireCommit() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (getAceConfig() != null) {
					for (I_ConfigAceFrame frameConfig : getAceConfig().aceFrames) {
						frameConfig.fireCommit();
						frameConfig.setCommitEnabled(false);
						updateAlerts(frameConfig);
					}
				}
			}
		});
	}

	/*
	 * A class that tracks the focused component. This is necessary to delegate
	 * the menu cut/copy/paste commands to the right component. An instance of
	 * this class is listening and when the user fires one of these commands, it
	 * calls the appropriate action on the currently focused component.
	 */
	private class TransferActionListener implements ActionListener,
			PropertyChangeListener {
		private JComponent focusOwner = null;

		public TransferActionListener() {
			KeyboardFocusManager manager = KeyboardFocusManager
					.getCurrentKeyboardFocusManager();
			manager.addPropertyChangeListener("permanentFocusOwner", this);
		}

		public void propertyChange(PropertyChangeEvent e) {
			Object o = e.getNewValue();
			if (o instanceof JComponent) {
				focusOwner = (JComponent) o;
			} else {
				focusOwner = null;
			}
		}

		public void actionPerformed(ActionEvent e) {
			if (focusOwner == null) {
				return;
			}
			String action = (String) e.getActionCommand();
			Action a = focusOwner.getActionMap().get(action);
			if (a != null) {
				a.actionPerformed(new ActionEvent(focusOwner,
						ActionEvent.ACTION_PERFORMED, null));
			}
		}
	}

	public class MoveListener implements ActionListener {

		public void actionPerformed(ActionEvent evt) {
			queueViewer.getMoveListener().actionPerformed(evt);

		}

	}

	public class ShowAllQueuesListener implements ActionListener {

		public void actionPerformed(ActionEvent evt) {
			JToggleButton showButton = (JToggleButton) evt.getSource();
			aceFrameConfig.setShowAllQueues(showButton.isSelected());
			try {
				queueViewer.refreshQueues();
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}
	}

	private class StatusChangeListener implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent evt) {
			statusLabel.setText((String) evt.getNewValue());
		}

	}

	private class ManageBottomPaneActionListener implements ActionListener {
		int lastLocation = 0;

		boolean hidden = true;

		public void actionPerformed(ActionEvent e) {
			// AceLog.getAppLog().info("bottom panel action: " + e);
			boolean show = showSearchToggle.isSelected()
					|| showSignpostPanelToggle.isSelected();
			if (show) {
				Container shownContainer = null;
				if (showSearchToggle == e.getSource()) {
					if (showSignpostPanelToggle.isSelected()) {
						showSignpostPanelToggle.setSelected(false);
					}
					int splitLoc = upperLowerSplit.getDividerLocation();
					upperLowerSplit.setBottomComponent(searchPanel);
					upperLowerSplit.setDividerLocation(splitLoc);
					shownContainer = searchPanel;
				} else if (showSignpostPanelToggle == e.getSource()) {
					if (showSearchToggle.isSelected()) {
						showSearchToggle.setSelected(false);
					}
					int splitLoc = upperLowerSplit.getDividerLocation();
					upperLowerSplit.setBottomComponent(signpostPanel);
					upperLowerSplit.setDividerLocation(splitLoc);
					shownContainer = signpostPanel;
				}
				if (hidden) {
					// AceLog.getAppLog().info("showing bottom panel");
					if (lastLocation == 0) {
						lastLocation = upperLowerSplit.getHeight() - 200;
					}
					if (upperLowerSplit.getHeight() - lastLocation < 50) {
						lastLocation = upperLowerSplit.getHeight() - 200;
					}
					upperLowerSplit.setDividerLocation(lastLocation);
					hidden = false;
				} else {
					// AceLog.getAppLog().info("bottom panel is already shown");
				}
				while (shownContainer != null) {
					shownContainer.validate();
					shownContainer = shownContainer.getParent();
				}
			} else {
				// AceLog.getAppLog().info("hiding bottom panel");
				lastLocation = upperLowerSplit.getDividerLocation();
				upperLowerSplit.setDividerLocation(upperLowerSplit.getHeight());
				hidden = true;
			}
			resizePalttes();
		}

	}

	private class PreferencesPaletteActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (preferencesPalette == null) {
				try {
					makeConfigPalette();
				} catch (Exception ex) {
					AceLog.getAppLog().alertAndLogException(ex);
				}
			}
			if (showPreferencesButton.isSelected()) {
				getRootPane().getLayeredPane().moveToFront(preferencesPalette);
				deselectOthers(showPreferencesButton);
			}
			preferencesPalette.togglePalette(((JToggleButton) e.getSource())
					.isSelected());
		}

	}

	private class SubversionPaletteActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				updateSvnPalette();
				setInitialSvnPosition();
				if (showSubversionButton.isSelected()) {
					subversionPalette.setVisible(true);
					getRootPane().getLayeredPane().moveToFront(subversionPalette);
					deselectOthers(showSubversionButton);
				}
				subversionPalette.togglePalette(((JToggleButton) e.getSource())
						.isSelected());
			} catch (Exception e1) {
				AceLog.getAppLog().alertAndLogException(e1);
			}
		}

	}

	private class QueuesPaletteActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (queuePalette == null) {
				try {
					makeQueuePalette();
				} catch (Exception ex) {
					AceLog.getAppLog().alertAndLogException(ex);
				}
			}
			if (showQueuesButton.isSelected()) {
				getRootPane().getLayeredPane().moveToFront(queuePalette);
				deselectOthers(showQueuesButton);
			}
			queuePalette.setSize(ACE.this.getWidth()
					- termTreeConceptSplit.getDividerLocation(), conceptTabs
					.getHeight() + 4);
			queuePalette.togglePalette(((JToggleButton) e.getSource())
					.isSelected());
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					queueViewer.requestFocusOnEntry();
				}

			});
		}

	}

	private class ResizeComponentAdaptor extends ComponentAdapter {
		@Override
		public void componentResized(ComponentEvent e) {
			resizePalttes();
		}

	}

	private class ProcessPaletteActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (processPalette == null) {
				try {
					makeProcessPalette();
				} catch (Exception ex) {
					AceLog.getAppLog().alertAndLogException(ex);
				}
			}
			if (showProcessBuilder.isSelected()) {
				getRootPane().getLayeredPane().moveToFront(processPalette);
				deselectOthers(showProcessBuilder);
			}
			processPalette.setSize(ACE.this.getWidth()
					- termTreeConceptSplit.getDividerLocation(), conceptTabs
					.getHeight() + 4);
			processPalette.togglePalette(((JToggleButton) e.getSource())
					.isSelected());
		}

	}

	private class HistoryPaletteActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (historyPalette == null) {
				makeHistoryPalette();
			}
			if (((JToggleButton) e.getSource()).isSelected()) {
				getRootPane().getLayeredPane().moveToFront(historyPalette);
			}
			historyPalette.togglePalette(((JToggleButton) e.getSource())
					.isSelected());
		}
	}

	private class AddressPaletteActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (addressPalette == null) {
				makeAddressPalette();
			}
			if (((JToggleButton) e.getSource()).isSelected()) {
				getRootPane().getLayeredPane().moveToFront(addressPalette);
			}
			addressPalette.togglePalette(((JToggleButton) e.getSource())
					.isSelected());
		}
	}

	private class TogglePanelsActionListener implements ActionListener,
			ComponentListener {
		private Integer origWidth;

		private Integer dividerLocation;

		private Rectangle bounds;

		public void actionPerformed(ActionEvent e) {
			bounds = getTopLevelAncestor().getBounds();
			if (origWidth == null) {
				getRootPane().addComponentListener(this);
				origWidth = bounds.width;
			}
			if (showComponentButton.isSelected()
					&& (showTreeButton.isSelected() == false)) {
				dividerLocation = termTreeConceptSplit.getDividerLocation();
				// AceLog.getLog().info(dividerLocation);
			}
			if (showTreeButton.isSelected()
					&& (showComponentButton.isSelected() == false)) {
				dividerLocation = termTreeConceptSplit.getDividerLocation();
				// AceLog.getLog().info(dividerLocation);
			}
			if (e.getSource() == showComponentButton) {
				if (showComponentButton.isSelected()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							if (showTreeButton.isSelected()) {
								if (dividerLocation < 250) {
									dividerLocation = 250;
								}
								termTreeConceptSplit
										.setDividerLocation(dividerLocation);
							} else {
								termTreeConceptSplit.setDividerLocation(0);
							}
						}
					});
				} else {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							termTreeConceptSplit.setDividerLocation(3000);
							if (showTreeButton.isSelected() == false) {
								showTreeButton.setSelected(true);
							}
						}
					});
				}
			} else if (e.getSource() == showTreeButton) {
				if (showTreeButton.isSelected()) {
					if (showComponentButton.isSelected()) {
						if (dividerLocation < 250) {
							dividerLocation = 250;
						}
						termTreeConceptSplit
								.setDividerLocation(dividerLocation);
					} else {
						termTreeConceptSplit.setDividerLocation(3000);
					}
				} else {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							termTreeConceptSplit.setDividerLocation(0);
							showComponentButton.setSelected(true);
						}
					});
				}
			}
		}

		public void componentHidden(ComponentEvent e) {
		}

		public void componentMoved(ComponentEvent e) {
		}

		public void componentResized(ComponentEvent e) {
			bounds = getTopLevelAncestor().getBounds();
			origWidth = bounds.width;
			dividerLocation = termTreeConceptSplit.getDividerLocation();
		}

		public void componentShown(ComponentEvent e) {
		}

	}

	protected JMenuItem addQueueMI, moveToDiskMI;

	private QueueViewerPanel queueViewer;

	private JLabel statusLabel = new JLabel();

	private JTreeWithDragImage tree;

	private JTabbedPane leftTabs = new JTabbedPane();

	private JPanel topPanel;

	private JTabbedPane conceptTabs = new JTabbedPane();

	private ConceptPanel c1Panel;

	private ConceptPanel c2Panel;

	private JComponent termTree;

	private SearchPanel searchPanel;

	private JSplitPane upperLowerSplit = new JSplitPane(
			JSplitPane.VERTICAL_SPLIT);

	private JSplitPane termTreeConceptSplit = new JSplitPane(
			JSplitPane.HORIZONTAL_SPLIT);

	private JToggleButton showComponentButton;

	private JToggleButton showTreeButton;

	private JToggleButton showSubversionButton;

	private JToggleButton showQueuesButton;

	private JToggleButton showProcessBuilder;

	private TogglePanelsActionListener resizeListener = new TogglePanelsActionListener();

	private ManageBottomPaneActionListener bottomPanelActionListener = new ManageBottomPaneActionListener();

	private CdePalette preferencesPalette;

	private CdePalette subversionPalette;

	private CdePalette queuePalette;

	private CdePalette processPalette;

	private JToggleButton showHistoryButton;

	private CdePalette historyPalette;

	private JToggleButton showSearchToggle;

	public static ExecutorService threadPool = Executors.newFixedThreadPool(9);

	public static ExecutorService treeExpandThread = Executors
			.newFixedThreadPool(1);

	public static Timer timer = new Timer();

	private AceFrameConfig aceFrameConfig;

	private JPanel treeProgress;

	private static AceConfig aceConfig;

	public static boolean editMode = true;

	private JMenu fileMenu = new JMenu("File");

	private JButton commitButton;

	private JButton cancelButton;

	private TerminologyListModel viewerHistoryTableModel = new TerminologyListModel();

	private TerminologyListModel uncommittedTableModel = new TerminologyListModel();

	private TerminologyListModel commitHistoryTableModel = new TerminologyListModel();

	private TerminologyListModel importHistoryTableModel = new TerminologyListModel();

	private TerminologyListModel favoritesTableModel = new TerminologyListModel();

	private JToggleButton showPreferencesButton;

	private Configuration config;

	private TerminologyList batchConceptList;

	private ArrayList<ConceptPanel> conceptPanels;

	private MasterWorker menuWorker;

	private UncommittedListModel dataCheckListModel;
	private ConceptPanel dataCheckPanel;

	private static String dataCheckTabLabel = "data checks";
	private static String taxonomyTabLabel = "taxonomy";
	
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private class RightPalettePoint implements I_GetPalettePoint {

		public Point getPalettePoint() {
			return new Point(topPanel.getLocation().x + topPanel.getWidth(),
					topPanel.getLocation().y + topPanel.getHeight() + 1
							+ getMenuSpacer());
		}

	}

	private int getMenuSpacer() {
		if (System.getProperty("apple.laf.useScreenMenuBar") != null
				&& System.getProperty("apple.laf.useScreenMenuBar").equals(
						"true")) {
			return 0;
		}
		return 24;
	}

	private class LeftPalettePoint implements I_GetPalettePoint {

		public Point getPalettePoint() {
			return new Point(topPanel.getLocation().x, topPanel.getLocation().y
					+ topPanel.getHeight() + getMenuSpacer() + 1);
		}
	}
	
	private String pluginRoot;
	public String getPluginRoot() {
		return pluginRoot;
	}

	public ACE(Configuration config) {
		this(config, "plugins");
	}
	/**
	 * http://java.sun.com/developer/JDCTechTips/2003/tt1210.html#2
	 * 
	 * @param aceFrameConfig
	 * @throws PrivilegedActionException
	 * @throws IOException
	 * @throws ConfigurationException
	 * @throws LoginException
	 * @throws DatabaseException
	 * 
	 * @throws DatabaseException
	 */
	public ACE(Configuration config, String pluginRoot) {
		super(new GridBagLayout());
		this.pluginRoot = pluginRoot;
		try {
			menuWorker = new MasterWorker(config);

			// only initialize these once as they are static lists...
			if (commitTests.size() == 0) {
				loadTests("commit", commitTests);
			}
			if (creationTests.size() == 0) {
				loadTests("precommit", creationTests);
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.config = config;

		this.addComponentListener(new ResizeComponentAdaptor());
	}

	private void loadTests(String directory, List<I_TestDataConstraints> list) {
		File componentPluginDir = new File(getPluginRoot() + File.separator
				+ directory);
		File[] plugins = componentPluginDir.listFiles(new FilenameFilter() {
			public boolean accept(File arg0, String fileName) {
				return fileName.toLowerCase().endsWith(".task");
			}

		});

		if (plugins != null) {
			for (File f : plugins) {
				try {
					FileInputStream fis = new FileInputStream(f);
					BufferedInputStream bis = new BufferedInputStream(fis);
					ObjectInputStream ois = new ObjectInputStream(bis);
					I_TestDataConstraints test = (I_TestDataConstraints) ois
							.readObject();
					ois.close();
					list.add(test);
				} catch (Exception e) {
					AceLog.getAppLog().alertAndLog(Level.WARNING,
							"Processing: " + f.getAbsolutePath(), e);
				}
			}
		}
	}

	public void deselectOthers(JToggleButton selectedOne) {
		AceLog.getAppLog().info("Deselecting others");
		if (showPreferencesButton != selectedOne) {
			if (showPreferencesButton.isSelected()) {
				showPreferencesButton.doClick();
			}
		}
		if (showSubversionButton != selectedOne) {
			if (showSubversionButton.isSelected()) {
				showSubversionButton.doClick();
			}
		}
		if (showQueuesButton != selectedOne) {
			if (showQueuesButton.isSelected()) {
				showQueuesButton.doClick();
			}
		}
		if (showProcessBuilder != selectedOne) {
			if (showProcessBuilder.isSelected()) {
				showProcessBuilder.doClick();
			}
		}
	}

	public void setup(I_ConfigAceFrame aceFrameConfig)
			throws DatabaseException, IOException, ClassNotFoundException,
			TerminologyException {
		menuWorker.writeAttachment(
				WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), aceFrameConfig);
		this.aceFrameConfig = (AceFrameConfig) aceFrameConfig;
		this.aceFrameConfig.addPropertyChangeListener(this);
		try {
			masterProcessBuilderPanel = new ProcessBuilderContainer(config,
					aceFrameConfig);
			descListProcessBuilderPanel = new ProcessBuilderContainer(config,
					aceFrameConfig);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
		searchPanel = new SearchPanel(aceFrameConfig);
		searchPanel.addComponentListener(new ResizePalettesListener());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;
		topPanel = getTopPanel();
		add(topPanel, c);
		c.gridy++;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		add(getContentPanel(), c);
		c.gridx = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.gridy++;
		c.gridwidth = 2;
		add(getBottomPanel(), c);
		aceFrameConfig.addPropertyChangeListener("statusMessage",
				new StatusChangeListener());
		if (aceFrameConfig.getTabHistoryMap().get("viewerHistoryList") == null) {
			aceFrameConfig.getTabHistoryMap().put("viewerHistoryList", new ArrayList<I_GetConceptData>());
		}
		viewerHistoryTableModel = new TerminologyListModel(aceFrameConfig.getTabHistoryMap().get("viewerHistoryList"));
	}

	public JMenuBar createMenuBar() throws LoginException, SecurityException,
			ConfigurationException, IOException, PrivilegedActionException,
			IntrospectionException, InvocationTargetException,
			IllegalAccessException, PropertyVetoException,
			ClassNotFoundException, NoSuchMethodException {
		JMenuBar menuBar = new JMenuBar();
		JMenu editMenu = new JMenu("Edit");
		menuBar.add(editMenu);
		addToMenuBar(menuBar, editMenu);
		return menuBar;
	}

	public JMenuBar addToMenuBar(JMenuBar menuBar, JMenu editMenu)
			throws LoginException, SecurityException, ConfigurationException,
			IOException, PrivilegedActionException, IntrospectionException,
			InvocationTargetException, IllegalAccessException,
			PropertyVetoException, ClassNotFoundException,
			NoSuchMethodException {
		addFileMenu(menuBar);
		addEditMenu(menuBar, editMenu);
		addProcessMenus(menuBar);

		return menuBar;
	}

	public void addProcessMenus(JMenuBar menuBar) throws FileNotFoundException,
			IOException, ClassNotFoundException {

		File menuDir = new File(pluginRoot + File.separator + "menu");
		if (menuDir.listFiles() != null) {
			addProcessMenuItems(menuBar, menuDir);
		}
	}

	private void addProcessMenuItems(JMenuBar menuBar, File menuDir)
			throws IOException, FileNotFoundException, ClassNotFoundException {
		for (File f : menuDir.listFiles()) {
			JMenu newMenu;
			if (f.isDirectory()) {
				if (f.getName().equals("File")) {
					newMenu = this.fileMenu;
					fileMenu.addSeparator();
					if (menuBar.getComponentIndex(fileMenu) == -1) {
						menuBar.add(fileMenu);
					}

				} else {
					newMenu = new JMenu(f.getName());
					menuBar.add(newMenu);
				}
				if (f.listFiles() != null) {
					for (File processFile : f.listFiles()) {
						if (processFile.isDirectory()) {
							JMenu submenu = new JMenu(processFile.getName());
							newMenu.add(submenu);
							addSubmenMenuItems(submenu, processFile);
						} else {
							try {
								ActionListener processMenuListener = new ProcessMenuActionListener(
										processFile, menuWorker);
								ObjectInputStream ois = new ObjectInputStream(
										new BufferedInputStream(
												new FileInputStream(processFile)));
								I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois
										.readObject();
								ois.close();
								JMenuItem processMenuItem = new JMenuItem(
										process.getName());
								processMenuItem
										.addActionListener(processMenuListener);
								newMenu.add(processMenuItem);
							} catch (IOException e) {
								AceLog.getAppLog().alertAndLog(null,
										Level.SEVERE,
										"processing: " + processFile, e);
							} catch (ClassNotFoundException e) {
								AceLog.getAppLog().alertAndLog(null,
										Level.SEVERE,
										"processing: " + processFile, e);
							}
						}
					}
				}
			}
		}
	}

	private void addSubmenMenuItems(JMenu subMenu, File menuDir)
			throws IOException, FileNotFoundException, ClassNotFoundException {
		for (File f : menuDir.listFiles()) {
			if (f.isDirectory()) {
				JMenu newSubMenu = new JMenu(f.getName());
				subMenu.add(newSubMenu);
				addSubmenMenuItems(newSubMenu, f);
			} else {
				ActionListener processMenuListener = new ProcessMenuActionListener(
						f, menuWorker);
				ObjectInputStream ois = new ObjectInputStream(
						new BufferedInputStream(new FileInputStream(f)));
				I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois
						.readObject();
				ois.close();
				JMenuItem processMenuItem = new JMenuItem(process.getName());
				processMenuItem.addActionListener(processMenuListener);
				subMenu.add(processMenuItem);
			}
		}
	}

	private void addEditMenu(JMenuBar menuBar, JMenu editMenu) {
		editMenu.removeAll();
		JMenuItem menuItem;
		editMenu.setMnemonic(KeyEvent.VK_E);
		TransferActionListener actionListener = new TransferActionListener();

		menuItem = new JMenuItem("Cut");
		menuItem.setActionCommand((String) TransferHandler.getCutAction()
				.getValue(Action.NAME));
		menuItem.addActionListener(actionListener);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItem.setMnemonic(KeyEvent.VK_T);
		editMenu.add(menuItem);
		menuItem = new JMenuItem("Copy");
		menuItem.setActionCommand((String) TransferHandler.getCopyAction()
				.getValue(Action.NAME));
		menuItem.addActionListener(actionListener);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItem.setMnemonic(KeyEvent.VK_C);
		editMenu.add(menuItem);
		menuItem = new JMenuItem("Paste");
		menuItem.setActionCommand((String) TransferHandler.getPasteAction()
				.getValue(Action.NAME));
		menuItem.addActionListener(actionListener);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuItem.setMnemonic(KeyEvent.VK_P);
		editMenu.add(menuItem);

		menuBar.add(editMenu);
	}

	public void addFileMenu(JMenuBar menuBar) throws LoginException,
			ConfigurationException, IOException, PrivilegedActionException,
			SecurityException, IntrospectionException,
			InvocationTargetException, IllegalAccessException,
			PropertyVetoException, ClassNotFoundException,
			NoSuchMethodException {
		if (editMode) {
			JMenuItem menuItem = null;
			menuItem = new JMenuItem("Export Baseline Jar...");
			menuItem.addActionListener(new WriteJar(aceConfig));
			fileMenu.add(menuItem);
			fileMenu.addSeparator();
			menuItem = new JMenuItem("Import Java Changeset...");
			menuItem.addActionListener(new ImportJavaChangeset(config));
			fileMenu.add(menuItem);
			fileMenu.addSeparator();
			menuItem = new JMenuItem("Import Changeset Jar...");
			menuItem.addActionListener(new ImportChangesetJar(config));
			fileMenu.add(menuItem);
			menuItem = new JMenuItem("Import Baseline Jar...");
			menuItem.addActionListener(new ImportBaselineJar(config));
			fileMenu.add(menuItem);
			fileMenu.addSeparator();
			menuItem = new JMenuItem("Change Password...");
			menuItem.addActionListener(new ChangeFramePassword(this));
			fileMenu.add(menuItem);
			menuItem = new JMenuItem("Save Profile");
			menuItem.addActionListener(new SaveProfile());
			fileMenu.add(menuItem);
			menuItem = new JMenuItem("Save Profile As...");
			menuItem.addActionListener(new SaveProfileAs());
			fileMenu.add(menuItem);
			menuBar.add(fileMenu);
		}

	}

	private static void addActionButton(ActionListener actionListener,
			String resource, String tooltipText, JPanel topPanel,
			GridBagConstraints c) {
		JButton newProcess = new JButton(new ImageIcon(ACE.class
				.getResource(resource)));
		newProcess.setToolTipText(tooltipText);
		newProcess.addActionListener(actionListener);
		topPanel.add(newProcess, c);
		c.gridx++;
	}

	private static void addActionToggleButton(ActionListener actionListener,
			String resource, String tooltipText, JPanel topPanel,
			GridBagConstraints c, int size) {
		JToggleButton newProcess;
		switch (size) {
		case 24:
			newProcess = new JToggleButton(new ImageIcon(ACE.class
					.getResource(resource)));
			break;
		case 32:
			newProcess = new JToggleButton(new ImageIcon(ACE.class
					.getResource(resource)));
			break;
		case 48:
			newProcess = new JToggleButton(new ImageIcon(ACE.class
					.getResource(resource)));
			break;
		default:
			newProcess = new JToggleButton(new ImageIcon(ACE.class
					.getResource(resource)));
			break;
		}
		newProcess.setToolTipText(tooltipText);
		newProcess.addActionListener(actionListener);
		topPanel.add(newProcess, c);
		c.gridx++;
	}

	private JComponent getContentPanel() throws DatabaseException, IOException,
			ClassNotFoundException, TerminologyException {
		termTree = getHierarchyPanel();
		conceptPanels = new ArrayList<ConceptPanel>();
		c1Panel = new ConceptPanel(this, LINK_TYPE.TREE_LINK, conceptTabs, 1);
		conceptPanels.add(c1Panel);
		c2Panel = new ConceptPanel(this, LINK_TYPE.SEARCH_LINK, conceptTabs, 2);
		conceptPanels.add(c2Panel);
		conceptTabs.addComponentListener(new ResizePalettesListener());
		conceptTabs.addTab("Tree", ConceptPanel.SMALL_TREE_LINK_ICON, c1Panel,
				"Tree Linked");
		conceptTabs.addTab("Search", ConceptPanel.SMALL_SEARCH_LINK_ICON,
				c2Panel, "Search Linked");

		ConceptPanel c3panel = new ConceptPanel(this, LINK_TYPE.UNLINKED,
				conceptTabs, 3);
		conceptPanels.add(c3panel);
		conceptTabs.addTab("Empty", null, c3panel, "Unlinked");
		ConceptPanel c4panel = new ConceptPanel(this, LINK_TYPE.UNLINKED,
				conceptTabs, 4);
		conceptPanels.add(c4panel);
		conceptTabs.addTab("Empty", null, c4panel, "Unlinked");     
		conceptTabs.addTab("List", 
					new ImageIcon(ACE.class.getResource("/16x16/plain/notebook.png")), getConceptListEditor());

		conceptTabs.setMinimumSize(new Dimension(0, 0));
		c2Panel.setMinimumSize(new Dimension(0, 0));

		
		termTreeConceptSplit.setRightComponent(conceptTabs);
		leftTabs.addTab(taxonomyTabLabel, termTree);
		if (editMode) {
			leftTabs.addTab(dataCheckTabLabel, getUncommittedList());
		}

		ConceptPanel c5panel = new ConceptPanel(this, LINK_TYPE.UNLINKED,
				leftTabs, 3);
		conceptPanels.add(c5panel);
		leftTabs.addTab("Empty", null, c5panel, "Unlinked");     
		leftTabs.setMinimumSize(new Dimension(0, 0));

		
		termTreeConceptSplit.setLeftComponent(leftTabs);
		termTree.setMinimumSize(new Dimension(0, 0));
		termTreeConceptSplit.setOneTouchExpandable(true);
		termTreeConceptSplit.setContinuousLayout(true);
		termTreeConceptSplit.setDividerLocation(aceFrameConfig
				.getTreeTermDividerLoc());
		termTreeConceptSplit.setResizeWeight(0.5);
		termTreeConceptSplit.setLastDividerLocation(aceFrameConfig
				.getTreeTermDividerLoc());

		upperLowerSplit.setTopComponent(termTreeConceptSplit);
		upperLowerSplit.setBottomComponent(searchPanel);
		upperLowerSplit.setOneTouchExpandable(true);
		upperLowerSplit.setContinuousLayout(true);
		upperLowerSplit.setResizeWeight(1);
		upperLowerSplit.setLastDividerLocation(500);
		upperLowerSplit.setDividerLocation(2000);
		searchPanel.setMinimumSize(new Dimension(0, 0));

		JPanel content = new JPanel();
		content.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.gridheight = 2;
		c.fill = GridBagConstraints.BOTH;
		content.add(upperLowerSplit, c);

		return content;
	}

	private JComponent getUncommittedList() {
		dataCheckListModel = new UncommittedListModel();
		JPanel uncommittedListPanel = new JPanel(new GridBagLayout());
		dataCheckListModel.addListDataListener(new ListenForDataChecks());
		JScrollPane uncommittedComponentScroller = new JScrollPane(
				uncommittedListPanel);
		return uncommittedComponentScroller;
	}

	CollectionEditorContainer conceptListEditor;

	private Component getConceptListEditor() throws DatabaseException,
			IOException, ClassNotFoundException {
		if (conceptListEditor == null) {
			if (aceFrameConfig.getTabHistoryMap().get("batchList") == null) {
				aceFrameConfig.getTabHistoryMap().put("batchList", new ArrayList<I_GetConceptData>());
			}
			TerminologyListModel batchListModel = new TerminologyListModel(aceFrameConfig.getTabHistoryMap().get("batchList"));
			batchConceptList = new TerminologyList(batchListModel, true, true, aceFrameConfig);
			conceptListEditor = new CollectionEditorContainer(batchConceptList,
					this, descListProcessBuilderPanel);
		}
		return conceptListEditor;
	}

	protected JMenuItem newProcessMI, readProcessMI, takeProcessNoTranMI,
			takeProcessTranMI, saveProcessMI, saveForLauncherQueueMI,
			saveAsXmlMI;

	private JPanel masterProcessBuilderPanel;

	private JPanel descListProcessBuilderPanel;

	private JPanel workflowPanel;

	private JPanel signpostPanel = new JPanel();

	private JToggleButton showAddressesButton;

	private CdePalette addressPalette;

	private JList addressList;

	private PreferencesPaletteActionListener preferencesActionListener;

	private HistoryPaletteActionListener hpal;

	private AddressPaletteActionListener apal;

	private ProcessPaletteActionListener showProcessBuilderActionListener;

	private QueuesPaletteActionListener showQueuesActionListener;

	private JToggleButton showSignpostPanelToggle;
	private JTabbedPane svnTabs;

	private static boolean runShutdownProcesses = true;

	private void makeProcessPalette() throws Exception {
		JLayeredPane layers = getRootPane().getLayeredPane();
		processPalette = new CdePalette(new BorderLayout(),
				new RightPalettePoint());
		layers.add(processPalette, JLayeredPane.PALETTE_LAYER);
		processPalette.add(masterProcessBuilderPanel, BorderLayout.CENTER);
		processPalette.setBorder(BorderFactory.createRaisedBevelBorder());
		int width = getWidth() - termTreeConceptSplit.getDividerLocation();
		int height = getHeight() - topPanel.getHeight();
		Rectangle topBounds = topPanel.getBounds();
		processPalette.setSize(width, height);

		processPalette.setLocation(new Point(topBounds.x + topBounds.width,
				topBounds.y + topBounds.height + 1));
		processPalette.setOpaque(true);
		processPalette.doLayout();
		addComponentListener(processPalette);
		processPalette.setVisible(true);

	}

	private void makeQueuePalette() throws Exception {
		JLayeredPane layers = getRootPane().getLayeredPane();
		queuePalette = new CdePalette(new BorderLayout(),
				new RightPalettePoint());
		layers.add(queuePalette, JLayeredPane.PALETTE_LAYER);

		MasterWorker worker = new MasterWorker(config);
		worker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(),
				aceFrameConfig);
		queuePalette.add(makeQueueViewerPanel(config, worker, aceFrameConfig
				.getInboxQueueFilter()), BorderLayout.CENTER);
		queuePalette.setBorder(BorderFactory.createRaisedBevelBorder());
		int width = getWidth() - termTreeConceptSplit.getDividerLocation();
		int height = getHeight() - topPanel.getHeight();
		Rectangle topBounds = topPanel.getBounds();
		queuePalette.setSize(width, height);

		queuePalette.setLocation(new Point(topBounds.x + topBounds.width,
				topBounds.y + topBounds.height + 1));
		queuePalette.setOpaque(true);
		queuePalette.doLayout();
		addComponentListener(queuePalette);
		queuePalette.setVisible(true);

	}

	public JPanel makeQueueViewerPanel(Configuration config,
			MasterWorker worker, ServiceItemFilter queueFilter)
			throws Exception {
		queueViewer = new QueueViewerPanel(config, worker, queueFilter);
		JPanel combinedPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		combinedPanel.add(getQueueViewerTopPanel(), c);
		c.gridy++;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		combinedPanel.add(queueViewer, c);
		return combinedPanel;

	}

	private JPanel getQueueViewerTopPanel() {
		JPanel listEditorTopPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		c.weighty = 0;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		listEditorTopPanel.add(new JLabel(" "), c); // placeholder for left
		// sided button
		c.weightx = 1.0;
		listEditorTopPanel.add(new JLabel(" "), c); // filler
		c.gridx++;
		c.weightx = 0.0;
		addActionButton(new MoveListener(), "/24x24/plain/outbox_out.png",
				"Take Selected Processes and Save To Disk (no transaction)",
				listEditorTopPanel, c);
		addActionToggleButton(new ShowAllQueuesListener(),
				"/24x24/plain/funnel_delete.png", "Show all queues",
				listEditorTopPanel, c, 24);
		return listEditorTopPanel;

	}

	private void updateSvnPalette() throws Exception {
		if (subversionPalette == null) {
			JLayeredPane layers = getRootPane().getLayeredPane();
			subversionPalette = new CdePalette(new BorderLayout(),
					new RightPalettePoint());
			if (svnTabs == null) {
				svnTabs = new JTabbedPane();
			}
			AceLog.getAppLog().info(
					"Subversion entries: "
							+ aceFrameConfig.getSubversionMap().keySet());
			for (String key : aceFrameConfig.getSubversionMap().keySet()) {
				SvnPanel svnTable = new SvnPanel(aceFrameConfig, key);
				svnTabs.addTab(key, svnTable);
			}

			layers.add(subversionPalette, JLayeredPane.PALETTE_LAYER);
			subversionPalette.add(svnTabs, BorderLayout.CENTER);
			subversionPalette
					.setBorder(BorderFactory.createRaisedBevelBorder());

			subversionPalette.setVisible(false);
		} else {
			HashSet<String> tabTitles = new HashSet<String>();
			if (svnTabs.getTabCount() != aceFrameConfig.getSubversionMap().keySet().size()) {
				for (int i = 0; i < svnTabs.getTabCount(); i++) {
					tabTitles.add(svnTabs.getTitleAt(i));
				}
				for (String key : aceFrameConfig.getSubversionMap().keySet()) {
					if (tabTitles.contains(key) == false) {
						SvnPanel svnTable = new SvnPanel(aceFrameConfig, key);
						svnTabs.addTab(key, svnTable);
					}
				}
			}
		}
	}

	boolean svnPositionSet = false;

	private void setInitialSvnPosition() {
		if (svnPositionSet == false) {
			svnPositionSet = true;
			int width = 650;
			int height = 550;
			Rectangle topBounds = topPanel.getBounds();
			subversionPalette.setSize(width, height);

			subversionPalette.setLocation(new Point(topBounds.x
					+ topBounds.width, topBounds.y + topBounds.height + 1));
			subversionPalette.setOpaque(true);
			subversionPalette.doLayout();
			addComponentListener(subversionPalette);
		}
	}

	private void makeConfigPalette() throws Exception {
		JLayeredPane layers = getRootPane().getLayeredPane();
		preferencesPalette = new CdePalette(new BorderLayout(),
				new RightPalettePoint());
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("View", makeViewConfig());
		tabs.addTab("Edit", makeEditConfig());
		tabs.addTab("Path", new SelectPathAndPositionPanel(false, "for view",
				aceFrameConfig, new PropertySetListenerGlue(
						"removeViewPosition", "addViewPosition",
						"replaceViewPosition", "getViewPositionSet",
						I_Position.class, aceFrameConfig)));
		tabs.addTab("New Path", new CreatePathPanel(aceFrameConfig));
		tabs.addTab("Ref Set", makeRefsetConfig());
		tabs.addTab("Component Panel", makeComponentConfig());

		layers.add(preferencesPalette, JLayeredPane.PALETTE_LAYER);
		preferencesPalette.add(tabs, BorderLayout.CENTER);
		preferencesPalette.setBorder(BorderFactory.createRaisedBevelBorder());

		int width = 600;
		int height = 550;
		preferencesPalette.setSize(width, height);

		Rectangle topBounds = topPanel.getBounds();
		preferencesPalette.setLocation(new Point(topBounds.x + topBounds.width,
				topBounds.y + topBounds.height + 1));
		preferencesPalette.setOpaque(true);
		preferencesPalette.doLayout();
		addComponentListener(preferencesPalette);
		preferencesPalette.setVisible(true);

	}

	private void removeConfigPalette() {
		if (preferencesPalette != null) {
			CdePalette oldPallette = preferencesPalette;
			preferencesPalette = null;
			oldPallette.setVisible(false);
			JLayeredPane layers = getRootPane().getLayeredPane();
			oldPallette.removeGhost();
			layers.remove(oldPallette);
		}
		if (showPreferencesButton.isSelected()) {
			try {
				makeConfigPalette();
				getRootPane().getLayeredPane().moveToFront(preferencesPalette);
				preferencesPalette.togglePalette(showPreferencesButton
						.isSelected());
			} catch (Exception ex) {
				AceLog.getAppLog().alertAndLogException(ex);
			}
		}

	}

	private JComponent makeRefsetViewPanel() {

		TerminologyListModel refsetViewTableModel = new TerminologyListModel();
		for (int id : aceFrameConfig.getRefsetsToShowInTaxonomy()
				.getListValues()) {
			refsetViewTableModel.addElement(ConceptBean.get(id));
		}
		refsetViewTableModel.addListDataListener(aceFrameConfig
				.getRefsetsToShowInTaxonomy());
		TerminologyList refsetViewList = new TerminologyList(
				refsetViewTableModel, aceFrameConfig);

		refsetViewList.setBorder(BorderFactory
				.createTitledBorder("Refsets to show in taxonomy view: "));
		JPanel refsetViewPrefPanel = new JPanel(new GridLayout(0, 1));
		refsetViewPrefPanel.add(new JScrollPane(refsetViewList));

		return new JScrollPane(refsetViewPrefPanel);
	}

	private JComponent makeRefsetTaxonomySortPanel() {

		TerminologyListModel refsetTaxonomySortTableModel = new TerminologyListModel();
		for (int id : aceFrameConfig.getRefsetsToSortTaxonomy().getListValues()) {
			refsetTaxonomySortTableModel.addElement(ConceptBean.get(id));
		}
		refsetTaxonomySortTableModel.addListDataListener(aceFrameConfig
				.getRefsetsToSortTaxonomy());
		TerminologyList refsetTaxonomySortList = new TerminologyList(
				refsetTaxonomySortTableModel, aceFrameConfig);

		refsetTaxonomySortList.setBorder(BorderFactory
				.createTitledBorder("Refsets to sort taxonomy view: "));
		JPanel refsetTaxonomySortPrefPanel = new JPanel(new GridLayout(0, 1));
		refsetTaxonomySortPrefPanel
				.add(new JScrollPane(refsetTaxonomySortList));

		return new JScrollPane(refsetTaxonomySortPrefPanel);
	}

	private JComponent makeDescPrefPanel() {

		TerminologyListModel descTypeTableModel = new TerminologyListModel();
		for (int id : aceFrameConfig.getDescTypes().getSetValues()) {
			descTypeTableModel.addElement(ConceptBean.get(id));
		}
		descTypeTableModel.addListDataListener(aceFrameConfig.getDescTypes());
		TerminologyList descList = new TerminologyList(descTypeTableModel,
				aceFrameConfig);
		descList.setBorder(BorderFactory
				.createTitledBorder("Description types: "));

		JPanel descPrefPanel = new JPanel(new GridLayout(0, 1));
		descPrefPanel.add(new JScrollPane(descList));

		TerminologyListModel shortLabelPrefOrderTableModel = new TerminologyListModel();
		for (int id : aceFrameConfig.getShortLabelDescPreferenceList()
				.getListValues()) {
			shortLabelPrefOrderTableModel.addElement(ConceptBean.get(id));
		}
		shortLabelPrefOrderTableModel.addListDataListener(aceFrameConfig
				.getShortLabelDescPreferenceList());
		TerminologyList shortLabelOrderList = new TerminologyList(
				shortLabelPrefOrderTableModel, aceFrameConfig);

		shortLabelOrderList.setBorder(BorderFactory
				.createTitledBorder("Short Label preference order: "));
		descPrefPanel.add(new JScrollPane(shortLabelOrderList));

		TerminologyListModel longLabelPrefOrderTableModel = new TerminologyListModel();
		for (int id : aceFrameConfig.getLongLabelDescPreferenceList()
				.getListValues()) {
			longLabelPrefOrderTableModel.addElement(ConceptBean.get(id));
		}
		longLabelPrefOrderTableModel.addListDataListener(aceFrameConfig
				.getLongLabelDescPreferenceList());
		TerminologyList longLabelOrderList = new TerminologyList(
				longLabelPrefOrderTableModel, aceFrameConfig);

		longLabelOrderList.setBorder(BorderFactory
				.createTitledBorder("Long label preference order: "));
		descPrefPanel.add(new JScrollPane(longLabelOrderList));

		TerminologyListModel treeDescPrefOrderTableModel = new TerminologyListModel();
		for (int id : aceFrameConfig.getTreeDescPreferenceList()
				.getListValues()) {
			treeDescPrefOrderTableModel.addElement(ConceptBean.get(id));
		}
		treeDescPrefOrderTableModel.addListDataListener(aceFrameConfig
				.getTreeDescPreferenceList());
		TerminologyList treePrefOrderList = new TerminologyList(
				treeDescPrefOrderTableModel, aceFrameConfig);

		treePrefOrderList.setBorder(BorderFactory
				.createTitledBorder("Tree preference order: "));
		descPrefPanel.add(new JScrollPane(treePrefOrderList));

		TerminologyListModel descPrefOrderTableModel = new TerminologyListModel();
		for (int id : aceFrameConfig.getTableDescPreferenceList()
				.getListValues()) {
			descPrefOrderTableModel.addElement(ConceptBean.get(id));
		}
		descPrefOrderTableModel.addListDataListener(aceFrameConfig
				.getTableDescPreferenceList());
		TerminologyList prefOrderList = new TerminologyList(
				descPrefOrderTableModel, aceFrameConfig);

		prefOrderList.setBorder(BorderFactory
				.createTitledBorder("Table preference order: "));
		descPrefPanel.add(new JScrollPane(prefOrderList));

		return descPrefPanel;
	}

	private JComponent makeTaxonomyPrefPanel() {
		JPanel relPrefPanel = new JPanel(new GridLayout(0, 1));

		JPanel checkPanel = new JPanel(new GridLayout(0, 1));

		checkPanel.add(getCheckboxEditor("use inferred rels in taxonomy view",
				"showInferredInTaxonomy", aceFrameConfig
						.getShowInferredInTaxonomy(), false));
		checkPanel.add(getCheckboxEditor("allow variable height taxonomy view",
				"variableHeightTaxonomyView", aceFrameConfig
						.getVariableHeightTaxonomyView(), false));
		checkPanel.add(getCheckboxEditor("show viewer images in taxonomy view",
				"showViewerImagesInTaxonomy", aceFrameConfig
						.getShowViewerImagesInTaxonomy(), true));
		checkPanel.add(getCheckboxEditor("show refset info in taxonomy view",
				"showRefsetInfoInTaxonomy", aceFrameConfig
						.getShowRefsetInfoInTaxonomy(), true));
		checkPanel.add(getCheckboxEditor("sort taxonomy using refset",
				"sortTaxonomyUsingRefset", aceFrameConfig
						.getSortTaxonomyUsingRefset(), true));

		relPrefPanel.add(checkPanel);
		relPrefPanel.add(new JScrollPane(makeTermList("parent relationships:",
				aceFrameConfig.getDestRelTypes())));
		relPrefPanel.add(new JScrollPane(makeTermList("child relationships:",
				aceFrameConfig.getSourceRelTypes())));
		relPrefPanel.add(new JScrollPane(makeTermList(
				"stated view characteristic types:", aceFrameConfig
						.getStatedViewTypes())));
		relPrefPanel.add(new JScrollPane(makeTermList(
				"inferred view characteristic types:", aceFrameConfig
						.getInferredViewTypes())));
		return relPrefPanel;
	}

	private Component getCheckboxEditor(String label, String propertyName,
			boolean initialValue, boolean enabled) {
		CheckboxEditor checkBoxEditor = new CheckboxEditor();
		checkBoxEditor.getCustomEditor().setEnabled(enabled);
		checkBoxEditor.setValue(initialValue);

		checkBoxEditor.setPropertyDisplayName(label);
		aceFrameConfig.addPropertyChangeListener("show"
				+ propertyName.toUpperCase().substring(0, 1)
				+ propertyName.substring(1), new PropertyListenerGlue(
				"setValue", Object.class, checkBoxEditor));
		checkBoxEditor.addPropertyChangeListener(new PropertyListenerGlue("set"
				+ propertyName.toUpperCase().substring(0, 1)
				+ propertyName.substring(1), Boolean.class, aceFrameConfig));

		return checkBoxEditor.getCustomEditor();
	}

	private TerminologyList makeTermList(String title, I_IntSet set) {
		TerminologyListModel termListModel = new TerminologyListModel();
		for (int id : set.getSetValues()) {
			termListModel.addElement(ConceptBean.get(id));
		}
		termListModel.addListDataListener(set);
		TerminologyList terminologyList = new TerminologyList(termListModel,
				aceFrameConfig);
		terminologyList.setBorder(BorderFactory.createTitledBorder(title));
		return terminologyList;
	}

	private TerminologyIntList makeTermList(String title, I_IntList list) {
		TerminologyIntListModel termListModel = new TerminologyIntListModel(
				(IntList) list);
		TerminologyIntList terminologyList = new TerminologyIntList(
				termListModel, aceFrameConfig);
		terminologyList.setBorder(BorderFactory.createTitledBorder(title));
		return terminologyList;
	}

	private JComponent makeStatusPrefPanel() {
		TerminologyListModel statusValuesModel = new TerminologyListModel();
		for (int id : aceFrameConfig.getAllowedStatus().getSetValues()) {
			statusValuesModel.addElement(ConceptBean.get(id));
		}
		statusValuesModel
				.addListDataListener(aceFrameConfig.getAllowedStatus());
		TerminologyList statusList = new TerminologyList(statusValuesModel,
				aceFrameConfig);
		statusList.setBorder(BorderFactory
				.createTitledBorder("Status values for display:"));
		return new JScrollPane(statusList);
	}

	private JComponent makeRootPrefPanel() {
		TerminologyListModel rootModel = new TerminologyListModel();
		for (int id : aceFrameConfig.getRoots().getSetValues()) {
			rootModel.addElement(ConceptBean.get(id));
		}
		rootModel.addListDataListener(aceFrameConfig.getRoots());
		TerminologyList rootList = new TerminologyList(rootModel,
				aceFrameConfig);
		rootList
				.setBorder(BorderFactory.createTitledBorder("Hierarchy roots:"));
		return new JScrollPane(rootList);
	}

	private JTabbedPane makeViewConfig() throws Exception {
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("descriptions", makeDescPrefPanel());
		tabs.addTab("roots", makeRootPrefPanel());
		tabs.addTab("taxonomy", makeTaxonomyPrefPanel());
		tabs.addTab("taxonomy sort", makeRefsetTaxonomySortPanel());
		tabs.addTab("status", makeStatusPrefPanel());
		tabs.addTab("refset", makeRefsetViewPanel());
		return tabs;
	}

	private JTabbedPane makeRefsetConfig() throws Exception {
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Concept", makeRefsetDefaults(TOGGLES.ATTRIBUTES));

		tabs.addTab("Descriptions", makeRefsetDefaults(TOGGLES.DESCRIPTIONS));

		tabs.addTab("Source Rels", makeRefsetDefaults(TOGGLES.SOURCE_RELS));

		tabs.addTab("Dest Rels", makeRefsetDefaults(TOGGLES.DEST_RELS));

		tabs.addTab("Images", makeRefsetDefaults(TOGGLES.IMAGE));

		return tabs;
	}

	private JTabbedPane makeRefsetDefaults(TOGGLES toggle)
			throws TerminologyException, IOException {
		JTabbedPane tabs = new JTabbedPane();
		// tabs.addTab("enabled ref set types" , new
		// JScrollPane(makeRefsetCheckboxPane(toggle)));
		for (EXT_TYPE type : EXT_TYPE.values()) {
			tabs.addTab(type.getInterfaceName(), new JScrollPane(
					makeRefsetDefaultsPanel(toggle, type)));
		}
		return tabs;
	}

	private JScrollPane makeComponentConfig() throws Exception {
		return new JScrollPane(makeComponentToggleCheckboxPane());
	}

	private JPanel makeRefsetDefaultsPanel(TOGGLES toggle, EXT_TYPE type)
			throws TerminologyException, IOException {
		JPanel defaultsPane = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		JCheckBox box = new JCheckBox(type.getInterfaceName() + " enabled");
		box.setSelected(aceFrameConfig.isRefsetInToggleVisible(type, toggle));
		box.addActionListener(new SetRefsetInToggleVisible(type, toggle));
		defaultsPane.add(box, c);
		c.gridy++;
		JTabbedPane editDefaultsTabs = new JTabbedPane();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		defaultsPane.add(editDefaultsTabs, c);

		switch (type) {
		case BOOLEAN:
			addDefaults(editDefaultsTabs, (RefsetDefaults) aceFrameConfig
					.getRefsetPreferencesForToggle(toggle)
					.getBooleanPreferences(), type);
			break;
		case CONCEPT:
			addDefaults(editDefaultsTabs, (RefsetDefaults) aceFrameConfig
					.getRefsetPreferencesForToggle(toggle)
					.getConceptPreferences(), type);
			editDefaultsTabs.addTab("concept types", new JScrollPane(
					makePopupConfigPanel(aceFrameConfig
							.getRefsetPreferencesForToggle(toggle)
							.getConceptPreferences().getConceptPopupIds(),
							"Concept types for popup:")));

			break;
		case CON_INT:
			addDefaults(editDefaultsTabs, (RefsetDefaults) aceFrameConfig
					.getRefsetPreferencesForToggle(toggle)
					.getConIntPreferences(), type);
			editDefaultsTabs.addTab("concept types", new JScrollPane(
					makePopupConfigPanel(aceFrameConfig
							.getRefsetPreferencesForToggle(toggle)
							.getConIntPreferences().getConceptPopupIds(),
							"Concept types for popup:")));

			break;
		case INTEGER:
			addDefaults(editDefaultsTabs, (RefsetDefaults) aceFrameConfig
					.getRefsetPreferencesForToggle(toggle)
					.getIntegerPreferences(), type);

			break;
		case LANGUAGE:
			addDefaults(editDefaultsTabs, (RefsetDefaults) aceFrameConfig
					.getRefsetPreferencesForToggle(toggle)
					.getLanguagePreferences(), type);
			editDefaultsTabs.addTab("acceptability types", new JScrollPane(
					makePopupConfigPanel(aceFrameConfig
							.getRefsetPreferencesForToggle(toggle)
							.getLanguagePreferences()
							.getAcceptabilityPopupIds(),
							"Acceptability types for popup:")));
			editDefaultsTabs.addTab("correctness types", new JScrollPane(
					makePopupConfigPanel(aceFrameConfig
							.getRefsetPreferencesForToggle(toggle)
							.getLanguagePreferences().getCorrectnessPopupIds(),
							"Correctness types for popup:")));
			editDefaultsTabs.addTab("degree of synonymy types",
					new JScrollPane(makePopupConfigPanel(aceFrameConfig
							.getRefsetPreferencesForToggle(toggle)
							.getLanguagePreferences()
							.getDegreeOfSynonymyPopupIds(),
							"Degree of synonymy for popup:")));

			break;
		case MEASUREMENT:
			addDefaults(editDefaultsTabs, (RefsetDefaults) aceFrameConfig
					.getRefsetPreferencesForToggle(toggle)
					.getMeasurementPreferences(), type);
			editDefaultsTabs.addTab("units of measure types", new JScrollPane(
					makePopupConfigPanel(aceFrameConfig
							.getRefsetPreferencesForToggle(toggle)
							.getMeasurementPreferences()
							.getUnitsOfMeasurePopupIds(),
							"Units of measure for popup:")));

			break;
		case SCOPED_LANGUAGE:
			addDefaults(editDefaultsTabs, (RefsetDefaults) aceFrameConfig
					.getRefsetPreferencesForToggle(toggle)
					.getLanguageScopedPreferences(), type);

			editDefaultsTabs.addTab("acceptability types", new JScrollPane(
					makePopupConfigPanel(aceFrameConfig
							.getRefsetPreferencesForToggle(toggle)
							.getLanguageScopedPreferences()
							.getAcceptabilityPopupIds(),
							"Acceptability types for popup:")));
			editDefaultsTabs.addTab("correctness types", new JScrollPane(
					makePopupConfigPanel(aceFrameConfig
							.getRefsetPreferencesForToggle(toggle)
							.getLanguageScopedPreferences()
							.getCorrectnessPopupIds(),
							"Correctness types for popup:")));
			editDefaultsTabs.addTab("degree of synonymy types",
					new JScrollPane(makePopupConfigPanel(aceFrameConfig
							.getRefsetPreferencesForToggle(toggle)
							.getLanguageScopedPreferences()
							.getDegreeOfSynonymyPopupIds(),
							"Degree of synonymy for popup:")));
			editDefaultsTabs.addTab("scope types", new JScrollPane(
					makePopupConfigPanel(aceFrameConfig
							.getRefsetPreferencesForToggle(toggle)
							.getLanguageScopedPreferences().getScopePopupIds(),
							"Scope types for popup:")));
			editDefaultsTabs.addTab("tag types", new JScrollPane(
					makePopupConfigPanel(aceFrameConfig
							.getRefsetPreferencesForToggle(toggle)
							.getLanguageScopedPreferences().getTagPopupIds(),
							"Tags for popup:")));
			break;
		case STRING:
			addDefaults(editDefaultsTabs, (RefsetDefaults) aceFrameConfig
					.getRefsetPreferencesForToggle(toggle)
					.getStringPreferences(), type);

			break;
		case CROSS_MAP:
			addDefaults(editDefaultsTabs, (RefsetDefaults) aceFrameConfig
					.getRefsetPreferencesForToggle(toggle)
					.getCrossMapPreferences(), type);
			break;
		case CROSS_MAP_FOR_REL:
			addDefaults(editDefaultsTabs, (RefsetDefaults) aceFrameConfig
					.getRefsetPreferencesForToggle(toggle)
					.getCrossMapForRelPreferences(), type);
			break;
		case TEMPLATE:
			addDefaults(editDefaultsTabs, (RefsetDefaults) aceFrameConfig
					.getRefsetPreferencesForToggle(toggle)
					.getTemplatePreferences(), type);
			break;
		case TEMPLATE_FOR_REL:
			addDefaults(editDefaultsTabs, (RefsetDefaults) aceFrameConfig
					.getRefsetPreferencesForToggle(toggle)
					.getTemplateForRelPreferences(), type);
			break;
		default:
			break;
		}

		return defaultsPane;
	}

	private void addDefaults(JTabbedPane editDefaultsTabs,
			RefsetDefaults defaults, EXT_TYPE type) {
		// Start with defaults for all refsets...

		// Default refset
		JPanel refsetsDefault = new JPanel(new GridLayout(0, 1));

		TermComponentLabel defaultRefset = new TermComponentLabel(
				aceFrameConfig);
		defaultRefset.setTermComponent(defaults.getDefaultRefset());
		gluePreferenceLabel(defaults, "defaultRefset", defaultRefset);
		wrapAndAdd(refsetsDefault, defaultRefset, "Default refset: ");

		// Status
		TermComponentLabel defaultStatus = new TermComponentLabel(
				aceFrameConfig);
		defaultStatus.setTermComponent(defaults.getDefaultStatusForRefset());
		gluePreferenceLabel(defaults, "defaultStatus", defaultStatus);
		wrapAndAdd(refsetsDefault, defaultStatus, "Default status: ");

		switch (type) {
		case BOOLEAN:
			// @todo
			break;
		case CONCEPT:
			TermComponentLabel defaultForConceptRefset = new TermComponentLabel(
					aceFrameConfig);
			defaultForConceptRefset
					.setTermComponent(((RefsetDefaultsConcept) defaults)
							.getDefaultForConceptRefset());
			gluePreferenceLabel(defaults, "defaultForConceptRefset",
					defaultForConceptRefset);
			wrapAndAdd(refsetsDefault, defaultForConceptRefset,
					"Default concept: ");
			break;
		case CON_INT:
			TermComponentLabel defaultForConIntRefset = new TermComponentLabel(
					aceFrameConfig);
			defaultForConIntRefset
					.setTermComponent(((RefsetDefaultsConcept) defaults)
							.getDefaultForConceptRefset());
			gluePreferenceLabel(defaults, "defaultForConIntRefset",
					defaultForConIntRefset);
			wrapAndAdd(refsetsDefault, defaultForConIntRefset,
					"Default concept: ");
			break;
		case INTEGER:
			// @todo
			break;
		case SCOPED_LANGUAGE:
			TermComponentLabel defaultScopeForScopedLanguageRefset = new TermComponentLabel(
					aceFrameConfig);
			defaultScopeForScopedLanguageRefset
					.setTermComponent(((RefsetDefaultsLanguageScoped) defaults)
							.getDefaultScopeForScopedLanguageRefset());
			gluePreferenceLabel(defaults,
					"defaultScopeForScopedLanguageRefset",
					defaultScopeForScopedLanguageRefset);
			wrapAndAdd(refsetsDefault, defaultScopeForScopedLanguageRefset,
					"Default scope: ");

			TermComponentLabel defaultTagForScopedLanguageRefset = new TermComponentLabel(
					aceFrameConfig);
			defaultTagForScopedLanguageRefset
					.setTermComponent(((RefsetDefaultsLanguageScoped) defaults)
							.getDefaultTagForScopedLanguageRefset());
			gluePreferenceLabel(defaults, "defaultTagForScopedLanguageRefset",
					defaultTagForScopedLanguageRefset);
			wrapAndAdd(refsetsDefault, defaultTagForScopedLanguageRefset,
					"Default tag: ");
			// @todo priority

		case LANGUAGE:
			TermComponentLabel defaultAcceptabilityForLanguageRefset = new TermComponentLabel(
					aceFrameConfig);
			defaultAcceptabilityForLanguageRefset
					.setTermComponent(((RefsetDefaultsLanguage) defaults)
							.getDefaultAcceptabilityForLanguageRefset());
			gluePreferenceLabel(defaults,
					"defaultAcceptabilityForLanguageRefset",
					defaultAcceptabilityForLanguageRefset);
			wrapAndAdd(refsetsDefault, defaultAcceptabilityForLanguageRefset,
					"Default acceptability: ");

			TermComponentLabel defaultCorrectnessForLanguageRefset = new TermComponentLabel(
					aceFrameConfig);
			defaultCorrectnessForLanguageRefset
					.setTermComponent(((RefsetDefaultsLanguage) defaults)
							.getDefaultCorrectnessForLanguageRefset());
			gluePreferenceLabel(defaults,
					"defaultCorrectnessForLanguageRefset",
					defaultCorrectnessForLanguageRefset);
			wrapAndAdd(refsetsDefault, defaultCorrectnessForLanguageRefset,
					"Default correctness: ");

			TermComponentLabel defaultDegreeOfSynonymyForLanguageRefset = new TermComponentLabel(
					aceFrameConfig);
			defaultDegreeOfSynonymyForLanguageRefset
					.setTermComponent(((RefsetDefaultsLanguage) defaults)
							.getDefaultDegreeOfSynonymyForLanguageRefset());
			gluePreferenceLabel(defaults,
					"defaultDegreeOfSynonymyForLanguageRefset",
					defaultDegreeOfSynonymyForLanguageRefset);
			wrapAndAdd(refsetsDefault,
					defaultDegreeOfSynonymyForLanguageRefset,
					"Default degree of synonomy: ");

			break;
		case MEASUREMENT:

			// @todo measurement

			TermComponentLabel defaultUnitsOfMeasureForMeasurementRefset = new TermComponentLabel(
					aceFrameConfig);
			defaultUnitsOfMeasureForMeasurementRefset
					.setTermComponent(((RefsetDefaultsMeasurement) defaults)
							.getDefaultUnitsOfMeasureForMeasurementRefset());
			gluePreferenceLabel(defaults, "defaultTagForScopedLanguageRefset",
					defaultUnitsOfMeasureForMeasurementRefset);
			wrapAndAdd(refsetsDefault,
					defaultUnitsOfMeasureForMeasurementRefset,
					"Default units of measure: ");

			break;

		case STRING:
			// @todo string
			break;
		case CROSS_MAP:
			// @todo
			break;
		case CROSS_MAP_FOR_REL:
			// @todo
			break;
		case TEMPLATE:
			// @todo
			break;
		case TEMPLATE_FOR_REL:
			// @todo
			break;
		default:
			break;
		}
		editDefaultsTabs.addTab("defaults", refsetsDefault);

		// add standard popups...
		editDefaultsTabs.addTab("refset types", new JScrollPane(
				makePopupConfigPanel(defaults.getRefsetPopupIds(),
						"Refset types for popup:")));

		editDefaultsTabs.addTab("status types", new JScrollPane(
				makePopupConfigPanel(defaults.getStatusPopupIds(),
						"Status for popup:")));

	}

	private void gluePreferenceLabel(RefsetDefaults defaults,
			String propertyName, TermComponentLabel labelToGlue) {
		defaults.addPropertyChangeListener(propertyName,
				new PropertyListenerGlue("setTermComponent",
						I_AmTermComponent.class, labelToGlue));

		labelToGlue.addPropertyChangeListener("termComponent",
				new PropertyListenerGlue("set"
						+ propertyName.toUpperCase().charAt(0)
						+ propertyName.substring(1), I_GetConceptData.class,
						defaults));
	}

	private JPanel makeComponentToggleCheckboxPane() {
		JPanel checkBoxPane = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		for (TOGGLES t : TOGGLES.values()) {
			JCheckBox box = new JCheckBox(t.name());
			box.setSelected(aceFrameConfig.isToggleVisible(t));
			box.addActionListener(new SetToggleVisibleListener(t));
			checkBoxPane.add(box, c);
			c.gridy++;
		}
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		checkBoxPane.add(new JPanel(), c);
		return checkBoxPane;
	}

	private JTabbedPane makeEditConfig() throws Exception {
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("defaults", new JScrollPane(madeDefaultsPanel()));
		tabs.addTab("rel type", new JScrollPane(makePopupConfigPanel(
				aceFrameConfig.getEditRelTypePopup(),
				"Relationship types for popup:")));
		tabs.addTab("rel refinabilty", new JScrollPane(makePopupConfigPanel(
				aceFrameConfig.getEditRelRefinabiltyPopup(),
				"Relationship refinability for popup:")));
		tabs.addTab("rel characteristic", new JScrollPane(makePopupConfigPanel(
				aceFrameConfig.getEditRelCharacteristicPopup(),
				"Relationship characteristics for popup:")));
		tabs.addTab("desc type", new JScrollPane(makePopupConfigPanel(
				aceFrameConfig.getEditDescTypePopup(),
				"Description types for popup:")));
		tabs.addTab("image type", new JScrollPane(makePopupConfigPanel(
				aceFrameConfig.getEditImageTypePopup(),
				"Image types for popup:")));
		tabs.addTab("status", new JScrollPane(makePopupConfigPanel(
				aceFrameConfig.getEditStatusTypePopup(),
				"Status values for popup:")));
		return tabs;
	}

	private JComponent madeDefaultsPanel() {
		JPanel defaultsPanel = new JPanel(new GridLayout(0, 1));

		TermComponentLabel defaultStatus = new TermComponentLabel(
				aceFrameConfig);
		defaultStatus.setTermComponent(aceFrameConfig.getDefaultStatus());
		aceFrameConfig.addPropertyChangeListener("defaultStatus",
				new PropertyListenerGlue("setTermComponent",
						I_AmTermComponent.class, defaultStatus));
		defaultStatus.addTermChangeListener(new PropertyListenerGlue(
				"setDefaultStatus", I_GetConceptData.class, aceFrameConfig));

		wrapAndAdd(defaultsPanel, defaultStatus, "Default status: ");

		TermComponentLabel defaultImageType = new TermComponentLabel(
				aceFrameConfig);
		defaultImageType.setTermComponent(aceFrameConfig.getDefaultImageType());
		aceFrameConfig.addPropertyChangeListener("defaultImageType",
				new PropertyListenerGlue("setTermComponent",
						I_AmTermComponent.class, defaultImageType));
		defaultImageType.addTermChangeListener(new PropertyListenerGlue(
				"setDefaultImageType", I_GetConceptData.class, aceFrameConfig));
		wrapAndAdd(defaultsPanel, defaultImageType, "Default image type: ");

		TermComponentLabel defaultDescType = new TermComponentLabel(
				aceFrameConfig);
		defaultDescType.setTermComponent(aceFrameConfig
				.getDefaultDescriptionType());
		aceFrameConfig.addPropertyChangeListener("defaultDescriptionType",
				new PropertyListenerGlue("setTermComponent",
						I_AmTermComponent.class, defaultDescType));
		defaultDescType.addTermChangeListener(new PropertyListenerGlue(
				"setDefaultDescriptionType", I_GetConceptData.class,
				aceFrameConfig));
		wrapAndAdd(defaultsPanel, defaultDescType, "Default description type: ");

		TermComponentLabel defaultRelType = new TermComponentLabel(
				aceFrameConfig);
		defaultRelType.setTermComponent(aceFrameConfig
				.getDefaultRelationshipType());
		aceFrameConfig.addPropertyChangeListener("defaultRelationshipType",
				new PropertyListenerGlue("setTermComponent",
						I_AmTermComponent.class, defaultRelType));
		defaultRelType.addTermChangeListener(new PropertyListenerGlue(
				"setDefaultRelationshipType", I_GetConceptData.class,
				aceFrameConfig));
		wrapAndAdd(defaultsPanel, defaultRelType, "Default relationship type: ");

		TermComponentLabel defaultRelCharacteristicType = new TermComponentLabel(
				aceFrameConfig);
		defaultRelCharacteristicType.setTermComponent(aceFrameConfig
				.getDefaultRelationshipCharacteristic());
		aceFrameConfig.addPropertyChangeListener(
				"defaultRelationshipCharacteristic", new PropertyListenerGlue(
						"setTermComponent", I_AmTermComponent.class,
						defaultRelCharacteristicType));
		defaultRelCharacteristicType
				.addTermChangeListener(new PropertyListenerGlue(
						"setDefaultRelationshipCharacteristic",
						I_GetConceptData.class, aceFrameConfig));
		wrapAndAdd(defaultsPanel, defaultRelCharacteristicType,
				"Default relationship characteristic: ");

		TermComponentLabel defaultRelRefinability = new TermComponentLabel(
				aceFrameConfig);
		defaultRelRefinability.setTermComponent(aceFrameConfig
				.getDefaultRelationshipRefinability());
		aceFrameConfig.addPropertyChangeListener(
				"defaultRelationshipRefinability", new PropertyListenerGlue(
						"setTermComponent", I_AmTermComponent.class,
						defaultRelRefinability));
		defaultRelRefinability.addTermChangeListener(new PropertyListenerGlue(
				"setDefaultRelationshipRefinability", I_GetConceptData.class,
				aceFrameConfig));
		wrapAndAdd(defaultsPanel, defaultRelRefinability,
				"Default relationship refinability: ");

		return defaultsPanel;
	}

	private void wrapAndAdd(JPanel defaultsPanel,
			TermComponentLabel defaultLabel, String borderTitle) {
		JPanel defaultItemPanel = new JPanel(new GridLayout(1, 1));
		defaultItemPanel.setBorder(BorderFactory
				.createTitledBorder(borderTitle));
		defaultItemPanel.add(defaultLabel);
		defaultsPanel.add(defaultItemPanel);
	}

	private JComponent makePopupConfigPanel(I_IntList list, String borderLabel) {

		TerminologyIntList popupList = makeTermList(borderLabel, list);

		JPanel popupPanel = new JPanel(new GridLayout(0, 1));
		popupPanel.add(new JScrollPane(popupList));
		return popupPanel;
	}

	private void makeHistoryPalette() {
		JLayeredPane layers = getRootPane().getLayeredPane();
		historyPalette = new CdePalette(new BorderLayout(),
				new LeftPalettePoint());
		JTabbedPane tabs = new JTabbedPane();

		TerminologyList viewerList = new TerminologyList(
				viewerHistoryTableModel, false, false, aceFrameConfig);
		tabs.addTab("viewer", new JScrollPane(viewerList));
		if (aceFrameConfig.getTabHistoryMap().get("favoritesList") == null) {
			aceFrameConfig.getTabHistoryMap().put("favoritesList", new ArrayList<I_GetConceptData>());
		}
		favoritesTableModel = new TerminologyListModel(aceFrameConfig.getTabHistoryMap().get("favoritesList"));
		TerminologyList favorites = new TerminologyList(
				favoritesTableModel, true, false, aceFrameConfig);
		
		
		tabs.addTab("favorites", new JScrollPane(favorites));
		if (editMode) {
			TerminologyList uncommittedList = new TerminologyList(
					uncommittedTableModel, false, false, aceFrameConfig);
			tabs.addTab("uncommitted", new JScrollPane(uncommittedList));
			TerminologyList commitList = new TerminologyList(
					commitHistoryTableModel, false, false, aceFrameConfig);
			tabs.addTab("changed", new JScrollPane(commitList));
			TerminologyList importList = new TerminologyList(
					importHistoryTableModel, false, false, aceFrameConfig);
			tabs.addTab("imported", new JScrollPane(importList));
		}
		historyPalette.add(tabs, BorderLayout.CENTER);
		historyPalette.setBorder(BorderFactory.createRaisedBevelBorder());
		layers.add(historyPalette, JLayeredPane.PALETTE_LAYER);
		int width = 400;
		int height = 500;
		Rectangle topBounds = topPanel.getBounds();
		historyPalette.setSize(width, height);

		historyPalette.setLocation(new Point(topBounds.x - width, topBounds.y
				+ topBounds.height + 1));
		historyPalette.setOpaque(true);
		historyPalette.doLayout();
		addComponentListener(historyPalette);
		historyPalette.setVisible(true);
	}

	private void makeAddressPalette() {
		JLayeredPane layers = getRootPane().getLayeredPane();
		addressPalette = new CdePalette(new BorderLayout(),
				new LeftPalettePoint());
		addressList = new JList(aceFrameConfig.getAddressesList());
		addressPalette.add(new JScrollPane(addressList), BorderLayout.CENTER);
		addressPalette.setBorder(BorderFactory.createRaisedBevelBorder());
		layers.add(addressPalette, JLayeredPane.PALETTE_LAYER);
		int width = 400;
		int height = 500;
		Rectangle topBounds = topPanel.getBounds();
		addressPalette.setSize(width, height);

		addressPalette.setLocation(new Point(topBounds.x - width, topBounds.y
				+ topBounds.height + 1));
		addressPalette.setOpaque(true);
		addressPalette.doLayout();
		addComponentListener(addressPalette);
		addressPalette.setVisible(true);
	}

	JComponent getHierarchyPanel() throws TerminologyException, IOException {
		if (tree != null) {
			for (TreeExpansionListener tel : tree.getTreeExpansionListeners()) {
				tree.removeTreeExpansionListener(tel);
			}
			for (TreeSelectionListener tsl : tree.getTreeSelectionListeners()) {
				tree.removeTreeSelectionListener(tsl);
			}
			for (TreeWillExpandListener twel : tree
					.getTreeWillExpandListeners()) {
				tree.removeTreeWillExpandListener(twel);
			}
		}
		tree = new JTreeWithDragImage(aceFrameConfig);
		tree.putClientProperty("JTree.lineStyle", "None");
		tree.addMouseListener(new TreeMouseListener(aceFrameConfig));
		tree.setLargeModel(true);
		// tree.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		tree.setTransferHandler(new TerminologyTransferHandler(tree));
		tree.setDragEnabled(true);

		tree.setCellRenderer(new TermTreeCellRenderer(aceFrameConfig));
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		DefaultTreeModel model = setRoots();
		/*
		 * Since nodes are added dynamically in this application, the only true
		 * leaf nodes are nodes that don't allow children to be added. (By
		 * default, askAllowsChildren is false and all nodes without children
		 * are considered to be leaves.)
		 * 
		 * But there's a complication: when the tree structure changes, JTree
		 * pre-expands the root node unless it's a leaf. To avoid having the
		 * root pre-expanded, we set askAllowsChildren *after* assigning the new
		 * root.
		 */

		model.setAsksAllowsChildren(true);

		tree.addTreeExpansionListener(new TreeExpansionListener() {
			public void treeExpanded(TreeExpansionEvent evt) {
				treeTreeExpanded(evt);
			}

			public void treeCollapsed(TreeExpansionEvent evt) {
				treeTreeCollapsed(evt);
			}
		});

		tree.addTreeSelectionListener(new TreeSelectionListener() {

			public void valueChanged(TreeSelectionEvent evt) {
				treeValueChanged(evt);
			}

		});
		JScrollPane treeView = new JScrollPane(tree);
		for (int id : aceFrameConfig.getChildrenExpandedNodes().getSetValues()) {
			AceLog.getAppLog().info("Child expand: " + id);
		}
		for (int id : aceFrameConfig.getParentExpandedNodes().getSetValues()) {
			AceLog.getAppLog().info("Parent expand: " + id);
		}
		for (int i = 0; i < tree.getRowCount(); i++) {
			TreePath path = tree.getPathForRow(i);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
					.getLastPathComponent();
			ConceptBeanForTree treeBean = (ConceptBeanForTree) node
					.getUserObject();
			if (aceFrameConfig.getChildrenExpandedNodes().contains(
					treeBean.getConceptId())) {
				tree.expandPath(new TreePath(node.getPath()));
			}
		}
		return treeView;
	}

	private DefaultTreeModel setRoots() {
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

		DefaultMutableTreeNode root = new DefaultMutableTreeNode(null, true);

		for (int rootId : aceFrameConfig.getRoots().getSetValues()) {
			root.add(new DefaultMutableTreeNode(ConceptBeanForTree.get(rootId,
					Integer.MIN_VALUE, 0, false), true));
		}
		model.setRoot(root);
		return model;
	}

	protected void treeValueChanged(TreeSelectionEvent evt) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) evt.getPath()
				.getLastPathComponent();
		String nodeStr = getNodeString(node);
		String s = evt.isAddedPath() ? "Selected " + nodeStr : "";
		aceFrameConfig.setStatusMessage(s);
		if (node != null) {
			ConceptBeanForTree treeBean = (ConceptBeanForTree) node
					.getUserObject();
			aceFrameConfig.setHierarchySelection(treeBean.getCoreBean());
		} else {
			aceFrameConfig.setHierarchySelection(null);
		}
	}

	private String getNodeString(DefaultMutableTreeNode node) {
		String nodeStr = node.toString();
		if ((node.getUserObject() != null)
				&& (I_GetConceptData.class.isAssignableFrom(node
						.getUserObject().getClass()))) {
			I_GetConceptData concept = (I_GetConceptData) node.getUserObject();
			try {
				I_DescriptionTuple desc = concept.getDescTuple(aceFrameConfig
						.getShortLabelDescPreferenceList(), aceFrameConfig);
				if (desc != null) {
					nodeStr = desc.getText();
				} else {
					AceLog.getAppLog().info(
							" descTuple is null: " + concept.toString());
					nodeStr = concept.getInitialText();
				}
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}

		}
		return nodeStr;
	}

	protected void treeTreeCollapsed(TreeExpansionEvent evt) {
		I_GetConceptDataForTree userObject = handleCollapse(evt);
		aceFrameConfig.getChildrenExpandedNodes().remove(
				userObject.getConceptId());

	}

	private I_GetConceptDataForTree handleCollapse(TreeExpansionEvent evt) {
		System.out
				.println("Collapsing " + evt.getPath().getLastPathComponent());
		TreeIdPath idPath = new TreeIdPath(evt.getPath());
		stopWorkersOnPath(idPath, "stopping for collapse");
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) evt.getPath()
				.getLastPathComponent();
		String nodeStr = getNodeString(node);
		node.removeAllChildren();
		I_GetConceptDataForTree userObject = (I_GetConceptDataForTree) node
				.getUserObject();

		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

		/*
		 * To avoid having JTree re-expand the root node, we disable
		 * ask-allows-children when we notify JTree about the new node
		 * structure.
		 */

		model.setAsksAllowsChildren(false);
		model.nodeStructureChanged(node);
		model.setAsksAllowsChildren(true);

		aceFrameConfig.setStatusMessage("Collapsed " + nodeStr);
		return userObject;
	}

	private void stopWorkersOnPath(TreeIdPath idPath, String message) {
		synchronized (expansionWorkers) {
			if (idPath == null) {
				List<TreeIdPath> allKeys = new ArrayList<TreeIdPath>(
						expansionWorkers.keySet());
				for (TreeIdPath key : allKeys) {
					AceLog.getAppLog().info("  Stopping all: " + key);
					removeAnyMatchingExpansionWorker(key, message);
				}
			} else {
				if (expansionWorkers.containsKey(idPath)) {
					AceLog.getAppLog().info("  Stopping: " + idPath);
					removeAnyMatchingExpansionWorker(idPath, message);
				}

				List<TreeIdPath> otherKeys = new ArrayList<TreeIdPath>(
						expansionWorkers.keySet());
				for (TreeIdPath key : otherKeys) {
					if (key.initiallyEqual(idPath)) {
						AceLog.getAppLog().info("  Stopping child: " + key);
						removeAnyMatchingExpansionWorker(key, message);
					}
				}
			}
		}
	}

	private void removeAnyMatchingExpansionWorker(TreeIdPath key, String message) {
		synchronized (expansionWorkers) {
			ExpandNodeSwingWorker foundWorker = expansionWorkers.get(key);
			if (foundWorker != null) {
				foundWorker.stopWork(message);
				expansionWorkers.remove(key);
			}
		}
	}

	public void removeExpansionWorker(TreeIdPath key,
			ExpandNodeSwingWorker worker, String message) {
		synchronized (expansionWorkers) {
			ExpandNodeSwingWorker foundWorker = expansionWorkers.get(key);
			if ((worker != null) && (foundWorker == worker)) {
				worker.stopWork(message);
				expansionWorkers.remove(key);
			}
		}
	}

	public static Map<TreeIdPath, ExpandNodeSwingWorker> expansionWorkers = new HashMap<TreeIdPath, ExpandNodeSwingWorker>();

	protected void treeTreeExpanded(TreeExpansionEvent evt) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) evt.getPath()
				.getLastPathComponent();
		String nodeStr = getNodeString(node);
		TreeIdPath idPath = new TreeIdPath(evt.getPath());
		synchronized (expansionWorkers) {
			stopWorkersOnPath(idPath, "stopping before expansion");
			I_GetConceptDataForTree userObject = (I_GetConceptDataForTree) node
					.getUserObject();
			if (userObject != null) {
				aceFrameConfig.getChildrenExpandedNodes().add(
						userObject.getConceptId());
				aceFrameConfig.setStatusMessage("Expanding " + nodeStr + "...");
				FrameConfigSnapshot configSnap = new FrameConfigSnapshot(aceFrameConfig);
				ExpandNodeSwingWorker worker = new ExpandNodeSwingWorker(
						(DefaultTreeModel) tree.getModel(), tree, node,
						new CompareConceptBeansForTree(configSnap), this, configSnap);
				treeExpandThread.execute(worker);
				expansionWorkers.put(idPath, worker);
			}
		}
	}

	private JPanel getTopPanel() throws IOException, ClassNotFoundException {
		JPanel topPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		showHistoryButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/history2.png")));
		if (editMode) {
			showHistoryButton
					.setToolTipText("history of user commits and concepts viewed");
		} else {
			showHistoryButton.setToolTipText("history of concepts viewed");
		}
		hpal = new HistoryPaletteActionListener();
		showHistoryButton.addActionListener(hpal);
		topPanel.add(showHistoryButton, c);
		c.gridx++;
		showAddressesButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/address_book3.png")));
		showAddressesButton
				.setToolTipText("address book of project participants");
		apal = new AddressPaletteActionListener();
		showAddressesButton.addActionListener(apal);
		showAddressesButton.setVisible(editMode);
		topPanel.add(showAddressesButton, c);
		c.gridx++;

		// address_book3.png
		topPanel.add(new JPanel(), c);
		c.gridx++;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		showTreeButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/text_tree.png")));
		showTreeButton
				.setToolTipText("Show the hierarchy view of the terminology content.");
		showTreeButton.setSelected(true);
		showTreeButton.addActionListener(resizeListener);
		topPanel.add(showTreeButton, c);
		c.gridx++;
		showComponentButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/components.png")));
		showComponentButton
				.setToolTipText("Show the component view of the terminology content.");
		showComponentButton.setSelected(true);
		showComponentButton.addActionListener(resizeListener);
		topPanel.add(showComponentButton, c);
		c.gridx++;
		treeProgress = new JPanel(new GridLayout(1, 1));
		topPanel.add((JPanel) treeProgress, c);
		c.gridx++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		workflowPanel = new JPanel();
		topPanel.add(workflowPanel, c);
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.gridx++;
		// topPanel.add(getComponentToggles2(), c);
		// c.gridx++;

		File componentPluginDir = new File(getPluginRoot() + File.separator
				+ "viewer");
		File[] plugins = componentPluginDir.listFiles(new FilenameFilter() {
			public boolean accept(File arg0, String fileName) {
				return fileName.toLowerCase().endsWith(".bp");
			}

		});

		if (plugins != null) {
			c.weightx = 0.0;
			c.weightx = 0.0;
			c.fill = GridBagConstraints.NONE;
			for (File f : plugins) {
				FileInputStream fis = new FileInputStream(f);
				BufferedInputStream bis = new BufferedInputStream(fis);
				ObjectInputStream ois = new ObjectInputStream(bis);
				BusinessProcess bp = (BusinessProcess) ois.readObject();
				ois.close();
				byte[] iconBytes = (byte[]) bp.readAttachement("button_icon");
				if (iconBytes != null) {
					ImageIcon icon = new ImageIcon(iconBytes);
					JButton pluginButton = new JButton(icon);
					pluginButton.setToolTipText(bp.getSubject());
					pluginButton.addActionListener(new PluginListener(f));
					c.gridx++;
					topPanel.add(pluginButton, c);
					AceLog.getAppLog().info(
							"adding viewer plugin: " + f.getName());
				} else {
					JButton pluginButton = new JButton(bp.getName());
					pluginButton.setToolTipText(bp.getSubject());
					pluginButton.addActionListener(new PluginListener(f));
					c.gridx++;
					topPanel.add(pluginButton, c);
					AceLog.getAppLog().info(
							"adding viewer plugin: " + f.getName());
				}
			}
		}

		c.gridx++;
		topPanel.add(new JLabel("   "), c);
		c.gridx++;
		
		TransporterLabel flashButton = new TransporterLabel(new ImageIcon(ACE.class
				.getResource("/32x32/plain/flash.png")), this);
		topPanel.add(flashButton, c);
		c.gridx++;

		showQueuesButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/inbox.png")));
		topPanel.add(showQueuesButton, c);
		showQueuesActionListener = new QueuesPaletteActionListener();
		showQueuesButton.addActionListener(showQueuesActionListener);
		showQueuesButton.setToolTipText("Show the queue viewer...");
		showQueuesButton.setVisible(editMode);
		c.gridx++;

		showProcessBuilder = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/cube_molecule.png")));
		topPanel.add(showProcessBuilder, c);
		showProcessBuilderActionListener = new ProcessPaletteActionListener();
		showProcessBuilder.addActionListener(showProcessBuilderActionListener);
		showProcessBuilder.setToolTipText("Show the process builder...");
		showProcessBuilder.setVisible(editMode);
		c.gridx++;

		showSubversionButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/svn.png")));
		topPanel.add(showSubversionButton, c);
		showSubversionButton
				.addActionListener(new SubversionPaletteActionListener());
		showSubversionButton.setToolTipText("Show Subversion panel...");
		showSubversionButton.setVisible(false);
		c.gridx++;
		showPreferencesButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/preferences.png")));
		preferencesActionListener = new PreferencesPaletteActionListener();
		showPreferencesButton.addActionListener(preferencesActionListener);
		topPanel.add(showPreferencesButton, c);
		showPreferencesButton.setToolTipText("Show preferences panel...");
		showPreferencesButton.setVisible(editMode);
		c.gridx++;

		return topPanel;
	}

	private class PluginListener implements ActionListener {
		File pluginProcessFile;

		private PluginListener(File pluginProcessFile) {
			super();
			this.pluginProcessFile = pluginProcessFile;
		}

		public void actionPerformed(ActionEvent e) {
			try {
				FileInputStream fis = new FileInputStream(pluginProcessFile);
				BufferedInputStream bis = new BufferedInputStream(fis);
				ObjectInputStream ois = new ObjectInputStream(bis);
				final BusinessProcess bp = (BusinessProcess) ois.readObject();
				ois.close();
				aceFrameConfig.setStatusMessage("Executing: " + bp.getName());
				final MasterWorker worker = aceFrameConfig.getWorker();

				worker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG
						.name(), aceFrameConfig);
				worker.writeAttachment(
						WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name(),
						this);
				Runnable r = new Runnable() {
					private String exceptionMessage;

					public void run() {
						I_EncodeBusinessProcess process = bp;
						try {
							worker.getLogger().info(
									"Worker: " + worker.getWorkerDesc() + " ("
											+ worker.getId()
											+ ") executing process: "
											+ process.getName());
							worker.execute(process);
							SortedSet<ExecutionRecord> sortedRecords = new TreeSet<ExecutionRecord>(
									process.getExecutionRecords());
							Iterator<ExecutionRecord> recordItr = sortedRecords
									.iterator();
							StringBuffer buff = new StringBuffer();
							while (recordItr.hasNext()) {
								ExecutionRecord rec = recordItr.next();
								buff.append("\n");
								buff.append(rec.toString());
							}
							worker.getLogger().info(buff.toString());
							exceptionMessage = "";
						} catch (Throwable e1) {
							worker.getLogger().log(Level.WARNING,
									e1.toString(), e1);
							exceptionMessage = e1.toString();
						}
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								aceFrameConfig
										.setStatusMessage("<html><font color='#006400'>execute");
								if (exceptionMessage.equals("")) {
									aceFrameConfig
											.setStatusMessage("<html>Execution of <font color='blue'>"
													+ bp.getName()
													+ "</font> complete.");
								} else {
									aceFrameConfig
											.setStatusMessage("<html><font color='blue'>Process complete: <font color='red'>"
													+ exceptionMessage);
								}
							}
						});
					}

				};
				new Thread(r).start();
			} catch (Exception e1) {
				aceFrameConfig.setStatusMessage("Exception during execution.");
				AceLog.getAppLog().alertAndLogException(e1);
			}
		}

	}

	JPanel getBottomPanel() {
		JPanel bottomPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		showSearchToggle = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/find.png")));
		showSearchToggle.addActionListener(bottomPanelActionListener);
		bottomPanel.add(showSearchToggle, c);
		c.gridx++;
		showSignpostPanelToggle = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/signpost.png")));
		showSignpostPanelToggle.addActionListener(bottomPanelActionListener);
		showSignpostPanelToggle.setVisible(true);
		bottomPanel.add(showSignpostPanelToggle, c);
		c.gridx++;
		
		TransporterLabel flashButton = new TransporterLabel(new ImageIcon(ACE.class
				.getResource("/32x32/plain/flash.png")), this);
		bottomPanel.add(flashButton, c);
		c.gridx++;

		bottomPanel.add(new JLabel("  "), c);
		c.gridx++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		bottomPanel.add(statusLabel, c);
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.gridx++;
		cancelButton = new JButton("cancel");
		cancelButton.setEnabled(false);
		cancelButton.addActionListener(new Abort());
		bottomPanel.add(cancelButton, c);
		cancelButton.setVisible(editMode);
		c.gridx++;
		commitButton = new JButton("commit");
		commitButton.setEnabled(false);
		commitButton.addActionListener(new Commit());
		commitButton.setVisible(editMode);
		bottomPanel.add(commitButton, c);
		c.gridx++;
		bottomPanel.add(new JLabel("   "), c);
		c.gridx++;

		signpostPanel = new JPanel();
		signpostPanel.add(new JLabel("Signpost Panel " + new Date()));
		signpostPanel.addComponentListener(new ResizePalettesListener());

		return bottomPanel;
	}

	public class ResizePalettesListener implements ComponentListener {
		public void componentHidden(ComponentEvent e) {
			resizePalttes();
		}

		public void componentMoved(ComponentEvent e) {
			resizePalttes();
		}

		public void componentResized(ComponentEvent e) {
			resizePalttes();
		}

		public void componentShown(ComponentEvent e) {
			resizePalttes();
		}
	}

	public void addTreeSelectionListener(TreeSelectionListener tsl) {
		tree.addTreeSelectionListener(tsl);
	}

	public void removeTreeSelectionListener(TreeSelectionListener tsl) {
		tree.removeTreeSelectionListener(tsl);
	}

	public I_ConfigAceFrame getAceFrameConfig() {
		return aceFrameConfig;
	}

	public void addSearchLinkedComponent(I_ContainTermComponent component) {
		searchPanel.addLinkedComponent(component);
	}

	public void removeSearchLinkedComponent(I_ContainTermComponent component) {
		searchPanel.removeLinkedComponent(component);
	}

	public void setTreeActivityPanel(ActivityPanel ap) {
		treeProgress.removeAll();
		treeProgress.add(ap);

	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("viewPositions")) {
			updateHierarchyView(evt.getPropertyName());
		} else if (evt.getPropertyName().equals("commit")) {
			updateHierarchyView(evt.getPropertyName());
			for (int i = 0; i < uncommittedTableModel.getSize(); i++) {
				commitHistoryTableModel.addElement(uncommittedTableModel.getElementAt(i));
			}
			uncommittedTableModel.clear();
			removeConfigPalette();
			while (commitHistoryTableModel.getSize() > maxHistoryListSize) {
				commitHistoryTableModel.removeElement(commitHistoryTableModel
						.getSize() - 1);
			}
			} else if (evt.getPropertyName().equals("commitEnabled")) {
			commitButton.setEnabled(aceFrameConfig.isCommitEnabled());
			if (aceFrameConfig.isCommitEnabled()) {
				commitButton
						.setText("<html><b><font color='green'>commit</font></b>");
			} else {
				commitButton.setText("commit");
			}
			cancelButton.setEnabled(aceFrameConfig.isCommitEnabled());
		} else if (evt.getPropertyName().equals("lastViewed")) {
			viewerHistoryTableModel.addElement(0, (ConceptBean) evt
					.getNewValue());
			while (viewerHistoryTableModel.getSize() > maxHistoryListSize) {
				viewerHistoryTableModel.removeElement(viewerHistoryTableModel
						.getSize() - 1);
			}
		} else if (evt.getPropertyName().equals("uncommitted")) {
			uncommittedTableModel.clear();
			for (I_Transact t : uncommitted) {
				if (ConceptBean.class.isAssignableFrom(t.getClass())) {
					uncommittedTableModel.addElement((ConceptBean) t);
				}
			}
		} else if (evt.getPropertyName().equals("imported")) {
			importHistoryTableModel.clear();
			for (I_Transact t : imported) {
				if (ConceptBean.class.isAssignableFrom(t.getClass())) {
					importHistoryTableModel.addElement((ConceptBean) t);
				}
			}
		} else if (evt.getPropertyName().equals("roots")) {
			try {
				setRoots();
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}
	}

	private void updateHierarchyView(String propChangeName) {
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		stopWorkersOnPath(null, "stopping for change in " + propChangeName);
		for (int i = 0; i < root.getChildCount(); i++) {
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) root
					.getChildAt(i);
			I_GetConceptData cb = (I_GetConceptData) childNode.getUserObject();
			if (aceFrameConfig.getChildrenExpandedNodes().contains(
					cb.getConceptId())) {
				TreePath tp = new TreePath(childNode);
				TreeExpansionEvent treeEvent = new TreeExpansionEvent(model, tp);
				handleCollapse(treeEvent);
				treeTreeExpanded(treeEvent);
			}
		}
	}

	public JMenu getFileMenu() {
		return fileMenu;
	}

	public static AceConfig getAceConfig() {
		return aceConfig;
	}

	public static void setAceConfig(AceConfig aceConfig) {
		if (ACE.aceConfig == null) {
			ACE.aceConfig = aceConfig;
			AceEditor.setAceFrames(aceConfig.aceFrames);
		} else {
			throw new UnsupportedOperationException(
					"Ace.aceConfig is already set");
		}
	}

	public static Set<I_ReadChangeSet> getCsReaders() {
		return csReaders;
	}

	public static Set<I_WriteChangeSet> getCsWriters() {
		return csWriters;
	}

	public JList getBatchConceptList() {
		return batchConceptList;
	}

	public ArrayList<ConceptPanel> getConceptPanels() {
		return conceptPanels;
	}

	public JTabbedPane getConceptTabs() {
		return conceptTabs;
	}

	public JPanel getWorkflowPanel() {
		return workflowPanel;
	}

	public JList getAddressList() {
		return addressList;
	}

	public void performLuceneSearch(String query,
			List<I_TestSearchResults> extraCriterion) {
		searchPanel.performLuceneSearch(query, extraCriterion);
	}

	public void setShowAddresses(boolean show) {
		if (show != showAddressesButton.isSelected()) {
			showAddressesButton.setSelected(show);
			apal.actionPerformed(new ActionEvent(showAddressesButton, 0,
					"toggle"));
		}
	}

	public void setShowComponentView(boolean show) {
		if (show != showComponentButton.isSelected()) {
			showComponentButton.setSelected(show);
			resizeListener.actionPerformed(new ActionEvent(showComponentButton,
					0, "toggle"));
		}
	}

	public void setShowHierarchyView(boolean show) {
		if (show != showTreeButton.isSelected()) {
			showTreeButton.setSelected(show);
			resizeListener.actionPerformed(new ActionEvent(showTreeButton, 0,
					"toggle"));
		}
	}

	public void setShowHistory(boolean show) {
		if (show != showHistoryButton.isSelected()) {
			showHistoryButton.setSelected(show);
			hpal
					.actionPerformed(new ActionEvent(showHistoryButton, 0,
							"toggle"));
		}
	}

	public void setShowPreferences(boolean show) {
		if (show != showPreferencesButton.isSelected()) {
			showPreferencesButton.setSelected(show);
			preferencesActionListener.actionPerformed(new ActionEvent(
					showPreferencesButton, 0, "toggle"));
		}
	}

	public void setShowSearch(boolean show) {
		if (show != showSearchToggle.isSelected()) {
			showSearchToggle.setSelected(show);
			bottomPanelActionListener.actionPerformed(new ActionEvent(
					showSearchToggle, 0, "toggle"));
		}
	}

	public void showListView() {
		setShowComponentView(true);
		conceptTabs.setSelectedComponent(conceptListEditor);
	}

	public void setupSvn() {
		try {
			updateSvnPalette();
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	public void setShowProcessBuilder(boolean show) {
		AceLog.getAppLog().info("set show process builder: " + show);
		if (show != showProcessBuilder.isSelected()) {
			showProcessBuilder.setSelected(show);
			showProcessBuilderActionListener.actionPerformed(new ActionEvent(
					showProcessBuilder, 0, "toggle"));
		}
	}

	public void setShowQueueViewer(boolean show) {
		AceLog.getAppLog().info("set show process builder: " + show);
		if (show != showQueuesButton.isSelected()) {
			showQueuesButton.setSelected(show);
			showQueuesActionListener.actionPerformed(new ActionEvent(
					showQueuesButton, 0, "toggle"));
		}
	}

	public boolean quit() {

		if (editMode) {
			if (uncommitted.size() > 0) {
				AceLog.getAppLog().info("Uncommitted: " + uncommitted);
				if (aceConfig != null) {
					for (I_ConfigAceFrame frameConfig : getAceConfig().aceFrames) {
						frameConfig.setCommitEnabled(true);
					}
				}

				JOptionPane
						.showMessageDialog(
								this,
								"<html>There are uncommitted changes.<p>Please commit or cancel before quitting.");
				return false;
			}

			int option = JOptionPane.showConfirmDialog(this,
					"Save profile before quitting?", "Save profile?",
					JOptionPane.YES_NO_OPTION);
			if (option == JOptionPane.YES_OPTION) {
				try {
					AceConfig.config.save();
				} catch (IOException e) {
					AceLog.getAppLog().alertAndLogException(e);
					return false;
				}
			}
		}

		if (runShutdownProcesses) {
			runShutdownProcesses = false;
			File configFile = aceFrameConfig.getMasterConfig().getConfigFile();
			File shutdownFolder = new File(configFile.getParentFile()
					.getParentFile(), "shutdown");
			executeShutdownProcesses(shutdownFolder);
			shutdownFolder = new File(configFile.getParentFile(), "shutdown");
			executeShutdownProcesses(shutdownFolder);
		}

		return true;
	}

	private void executeShutdownProcesses(File shutdownFolder) {
		if (shutdownFolder.exists()) {
			AceLog.getAppLog().info("Shutdown folder exists");
			File[] startupFiles = shutdownFolder
					.listFiles(new FilenameFilter() {

						public boolean accept(File dir, String name) {
							return name.endsWith(".bp");
						}
					});
			if (startupFiles != null) {
				for (int i = 0; i < startupFiles.length; i++) {
					try {
						AceLog.getAppLog().info(
								"Executing shutdown business process: "
										+ startupFiles[i]);
						FileInputStream fis = new FileInputStream(
								startupFiles[i]);
						BufferedInputStream bis = new BufferedInputStream(fis);
						ObjectInputStream ois = new ObjectInputStream(bis);
						I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois
								.readObject();
						aceFrameConfig.getWorker().execute(process);
						AceLog.getAppLog().info(
								"Finished shutdown business process: "
										+ startupFiles[i]);
					} catch (Throwable e1) {
						AceLog.getAppLog().alertAndLog(
								Level.SEVERE,
								e1.getMessage() + " thrown by "
										+ startupFiles[i], e1);
					}
				}
			} else {
				AceLog.getAppLog().info(
						"No shutdown processes found. Folder exists: "
								+ shutdownFolder.exists());
			}
		} else {
			AceLog.getAppLog().info("NO shutdown folder exists");
		}
	}

	public JPanel getSignpostPanel() {
		return signpostPanel;
	}

	public void setShowSignpostPanel(boolean show) {
		if (show != showSignpostPanelToggle.isSelected()) {
			showSignpostPanelToggle.setSelected(show);
			bottomPanelActionListener.actionPerformed(new ActionEvent(
					showSignpostPanelToggle, 0, "toggle"));
		}
	}

	public void setShowSignpostToggleVisible(boolean visible) {
		showSignpostPanelToggle.setVisible(visible);
	}

	public void setShowSignpostToggleEnabled(boolean enabled) {
		showSignpostPanelToggle.setEnabled(enabled);
	}

	public void setSignpostToggleIcon(ImageIcon icon) {
		showSignpostPanelToggle.setIcon(icon);
	}

	private void resizePalttes() {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				if (queuePalette != null) {
					queuePalette.setSize(ACE.this.getWidth()
							- termTreeConceptSplit.getDividerLocation() + 6,
							conceptTabs.getHeight() + 4);
					revalidateAllParents(queuePalette);
					// revalidateAllDescendants(queuePalette);
				}
				if (processPalette != null) {
					processPalette.setSize(ACE.this.getWidth()
							- termTreeConceptSplit.getDividerLocation() + 6,
							conceptTabs.getHeight() + 4);
					revalidateAllParents(processPalette);
					// revalidateAllDescendants(processPalette);
				}
			}

			private void revalidateAllParents(Container cont) {
				while (cont != null) {
					cont.validate();
					cont = cont.getParent();
				}
			}

			@SuppressWarnings("unused")
			private void revalidateAllDescendants(Container cont) {
				while (cont != null) {
					cont.validate();
					for (Component desc : cont.getComponents()) {
						if (Container.class.isAssignableFrom(desc.getClass())) {
							revalidateAllDescendants((Container) desc);
						}
					}
				}
			}
		});
	}

	public I_HostConceptPlugins getListConceptViewer() {
		return conceptListEditor.getConceptPanel();
	}

	public boolean isAddressToggleVisible() {
		return showAddressesButton.isVisible();
	}

	public boolean isBuilderToggleVisible() {
		return showProcessBuilder.isVisible();
	}

	public boolean isComponentToggleVisible() {
		return showComponentButton.isVisible();
	}

	public boolean isHierarchyToggleVisible() {
		return showTreeButton.isVisible();
	}

	public boolean isHistoryToggleVisible() {
		return showHistoryButton.isVisible();
	}

	public boolean isInboxToggleVisible() {
		return showQueuesButton.isVisible();
	}

	public boolean isPreferencesToggleVisible() {
		return showPreferencesButton.isVisible();
	}

	public boolean isProgressToggleVisible() {
		return treeProgress.isVisible();
	}

	public boolean isSubversionToggleVisible() {
		return showSubversionButton.isVisible();
	}

	public void setAddressToggleVisible(final boolean visible) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				showAddressesButton.setVisible(visible);
			}
		});
	}

	public void setBuilderToggleVisible(final boolean visible) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				showProcessBuilder.setVisible(visible);
			}
		});
	}

	public void setComponentToggleVisible(final boolean visible) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				showComponentButton.setVisible(visible);
			}
		});
	}

	public void setHierarchyToggleVisible(final boolean visible) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				showTreeButton.setVisible(visible);
			}
		});
	}

	public void setHistoryToggleVisible(final boolean visible) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				showHistoryButton.setVisible(visible);
			}
		});
	}

	public void setInboxToggleVisible(final boolean visible) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				showQueuesButton.setVisible(visible);
			}
		});
	}

	public void setPreferencesToggleVisible(final boolean visible) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				showPreferencesButton.setVisible(visible);
			}
		});
	}

	public void setProgressToggleVisible(final boolean visible) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				treeProgress.setVisible(visible);
			}
		});
	}

	public void setSubversionToggleVisible(final boolean visible) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				showSubversionButton.setVisible(visible);
			}
		});
	}

	public void setCommitAbortButtonsVisible(final boolean visible) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				commitButton.setVisible(visible);
				cancelButton.setVisible(visible);
			}
		});
	}

	public static Set<I_Transact> getUncommitted() {
		return Collections.unmodifiableSet(uncommitted);
	}

	public UncommittedListModel getUncommittedListModel() {
		return dataCheckListModel;
	}

	public JTreeWithDragImage getTree() {
		return tree;
	}

}
