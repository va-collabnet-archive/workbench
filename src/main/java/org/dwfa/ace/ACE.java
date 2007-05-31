package org.dwfa.ace;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
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
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;

import org.dwfa.ace.actions.Abort;
import org.dwfa.ace.actions.ChangeEnvironmentPassword;
import org.dwfa.ace.actions.Commit;
import org.dwfa.ace.actions.ImportBaselineJar;
import org.dwfa.ace.actions.ImportChangesetJar;
import org.dwfa.ace.actions.ImportJavaChangeset;
import org.dwfa.ace.actions.SaveEnvironment;
import org.dwfa.ace.actions.WriteJar;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.CreatePathPanel;
import org.dwfa.ace.config.SelectPathAndPositionPanel;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.gui.concept.ConceptPanel;
import org.dwfa.ace.gui.concept.ConceptPanel.LINK_TYPE;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.search.SearchPanel;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.tree.ConceptBeanForTree;
import org.dwfa.ace.tree.ExpandNodeSwingWorker;
import org.dwfa.ace.tree.I_GetConceptDataForTree;
import org.dwfa.ace.tree.JTreeWithDragImage;
import org.dwfa.ace.tree.TermTreeCellRenderer;
import org.dwfa.ace.tree.TreeIdPath;
import org.dwfa.ace.tree.TreeMouseListener;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.ExecutionRecord;
import org.dwfa.bpa.gui.ProcessMenuActionListener;
import org.dwfa.bpa.gui.glue.PropertyListenerGlue;
import org.dwfa.bpa.gui.glue.PropertySetListenerGlue;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.queue.gui.QueueViewerPanel;
import org.dwfa.svn.SvnPanel;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.ConceptBean;

import com.sleepycat.je.DatabaseException;

public class ACE extends JPanel implements PropertyChangeListener {

	private static Set<I_Transact> uncommitted = new HashSet<I_Transact>();

	private static List<I_Transact> imported = new ArrayList<I_Transact>();

	public static void addImported(I_Transact to) {
		imported.add(to);
		for (I_ConfigAceFrame frameConfig : getAceConfig().aceFrames) {
			frameConfig.setCommitEnabled(true);
			if (ConceptBean.class.isAssignableFrom(to.getClass())) {
				frameConfig.addImported((I_GetConceptData) to);
			}
		}
	}

	public static void addUncommitted(I_Transact to) {
		uncommitted.add(to);
		for (I_ConfigAceFrame frameConfig : getAceConfig().aceFrames) {
			frameConfig.setCommitEnabled(true);
			if (ConceptBean.class.isAssignableFrom(to.getClass())) {
				frameConfig.addUncommitted((I_GetConceptData) to);
			}
		}
	}

	public static void removeUncommitted(I_Transact to) {
		uncommitted.remove(to);
		for (I_ConfigAceFrame frameConfig : getAceConfig().aceFrames) {
			frameConfig.addUncommitted(null);
			if (uncommitted.size() == 0) {
				frameConfig.setCommitEnabled(false);
			}
		}
	}

	private static Set<I_WriteChangeSet> csWriters = new HashSet<I_WriteChangeSet>();

	private static Set<I_ReadChangeSet> csReaders = new HashSet<I_ReadChangeSet>();

	protected final static int MENU_MASK = Toolkit.getDefaultToolkit()
			.getMenuShortcutKeyMask();

	/*
	 * 
	 */
	public static void commit() throws IOException {
		Date now = new Date();
		Set<TimePathId> values = new HashSet<TimePathId>();
		for (I_WriteChangeSet writer : csWriters) {
			writer.open();
		}
		int version = ThinVersionHelper.convert(now.getTime());
		AceLog.getEditLog().info(
				"Starting commit: " + version + " (" + now.getTime() + ")");

		for (I_Transact cb : uncommitted) {
			for (I_WriteChangeSet writer : csWriters) {
				writer.writeChanges(cb, ThinVersionHelper.convert(version));
			}
			cb.commit(version, values);
		}
		try {
			AceConfig.getVodb().addTimeBranchValues(values);
			AceConfig.getVodb().sync();
		} catch (DatabaseException e) {
			throw new ToIoException(e);
		}
		for (I_WriteChangeSet writer : csWriters) {
			writer.commit();
		}
		uncommitted.clear();
		fireCommit();
		AceLog.getEditLog().info(
				"Finished commit: " + version + " (" + now.getTime() + ")");
	}

	public static void abort() throws IOException {
		for (I_Transact cb : uncommitted) {
			cb.abort();
		}
		uncommitted.clear();
		fireCommit();
	}

	private static void fireCommit() {
		for (I_ConfigAceFrame frameConfig : getAceConfig().aceFrames) {
			frameConfig.fireCommit();
			frameConfig.setCommitEnabled(false);
		}
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
			viewer.getMoveListener().actionPerformed(evt);

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
			if (showSearchButton == e.getSource()) {
				if (showSearchButton.isSelected()) {
					if (hidden) {
						if (lastLocation == 0) {
							lastLocation = upperLowerSplit.getHeight() - 200;
						}
						upperLowerSplit.setDividerLocation(lastLocation);
						hidden = false;
					}

				} else {
					lastLocation = upperLowerSplit.getDividerLocation();
					upperLowerSplit.setDividerLocation(upperLowerSplit
							.getHeight());
					hidden = true;
				}
			}
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
			if (subversionPalette == null) {
				try {
					makeSubversionPalette();
				} catch (Exception ex) {
					AceLog.getAppLog().alertAndLogException(ex);
				}
			}
			if (showSubversionButton.isSelected()) {
				getRootPane().getLayeredPane().moveToFront(subversionPalette);
				deselectOthers(showSubversionButton);
			}
			subversionPalette.togglePalette(((JToggleButton) e.getSource())
					.isSelected());
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
			queuePalette.togglePalette(((JToggleButton) e.getSource())
					.isSelected());
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

	private QueueViewerPanel viewer;

	private JLabel statusLabel = new JLabel();

	private JTreeWithDragImage tree;

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

	private JToggleButton showSearchButton;

	public static ExecutorService threadPool = Executors.newFixedThreadPool(5);

	public static ExecutorService treeExpandThread = Executors
			.newFixedThreadPool(1);

	public static Timer timer = new Timer();

	private I_ConfigAceFrame aceFrameConfig;

	private JPanel treeProgress;

	private static AceConfig aceConfig;

	private JMenu fileMenu = new JMenu("File");

	private JButton commitButton;

	private JButton cancelButton;

	private TerminologyListModel viewerHistoryTableModel = new TerminologyListModel();

	private TerminologyListModel commitHistoryTableModel = new TerminologyListModel();

	private TerminologyListModel importHistoryTableModel = new TerminologyListModel();

	private JToggleButton showPreferencesButton;

	private Configuration config;

	private JList batchConceptList;

	private ArrayList<ConceptPanel> conceptPanels;

	private MasterWorker menuWorker;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private class RightPalettePoint implements I_GetPalettePoint {

		public Point getPalettePoint() {
			return new Point(topPanel.getLocation().x + topPanel.getWidth(),
					topPanel.getLocation().y + topPanel.getHeight() + 1 + getMenuSpacer());
		}

	}

	private int getMenuSpacer() {
		if (System.getProperty("apple.laf.useScreenMenuBar") != null &&
				System.getProperty("apple.laf.useScreenMenuBar").equals("true")) {
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
	public ACE(Configuration config) {
		super(new GridBagLayout());
		try {
			menuWorker = new MasterWorker(config);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
		this.config = config;
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
			throws DatabaseException, IOException, ClassNotFoundException {
		this.aceFrameConfig = aceFrameConfig;
		this.aceFrameConfig.addPropertyChangeListener(this);
		try {
			masterProcessBuilderPanel = new ProcessBuilderContainer(config,
					aceFrameConfig);
			descListProcessBuilderPanel = new ProcessBuilderContainer(config,
					aceFrameConfig);
		} catch (LoginException e) {
			throw new RuntimeException(e);
		} catch (ConfigurationException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (PrivilegedActionException e) {
			throw new RuntimeException(e);
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (PropertyVetoException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		searchPanel = new SearchPanel(aceFrameConfig);
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

	public JMenuBar addToMenuBar(JMenuBar menuBar, JMenu editMenu) throws LoginException,
			SecurityException, ConfigurationException, IOException,
			PrivilegedActionException, IntrospectionException,
			InvocationTargetException, IllegalAccessException,
			PropertyVetoException, ClassNotFoundException,
			NoSuchMethodException {
		addFileMenu(menuBar);
		addEditMenu(menuBar, editMenu);
		addProcessMenus(menuBar);
		
		return menuBar;
	}
	
	public void addProcessMenus(JMenuBar menuBar) throws FileNotFoundException, IOException, ClassNotFoundException {

		File menuDir = new File("plugins/menu");
        if (menuDir.listFiles() != null) {
            addProcessMenuItems(menuBar, menuDir);
        }
    }

	private void addProcessMenuItems(JMenuBar menuBar, File menuDir) throws IOException, FileNotFoundException, ClassNotFoundException {
		for (File f : menuDir.listFiles()) {
		    JMenu newMenu;
		    if (f.isDirectory()) {
		        if (f.getName().equals("File")) {
		            newMenu = this.fileMenu;
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
			                ActionListener processMenuListener = new ProcessMenuActionListener(
			                        processFile, menuWorker);
			                ObjectInputStream ois = new ObjectInputStream(
			                        new BufferedInputStream(new FileInputStream(
			                                processFile)));
			                I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois
			                        .readObject();
			                ois.close();
			                JMenuItem processMenuItem = new JMenuItem(process
			                        .getName());
			                processMenuItem.addActionListener(processMenuListener);
			                newMenu.add(processMenuItem);
		            	}
		            }
		        }
		        if (newMenu == fileMenu) {
		            fileMenu.addSeparator();
		        }
		    }
		}
	}
	private void addSubmenMenuItems(JMenu subMenu, File menuDir) throws IOException, FileNotFoundException, ClassNotFoundException {
		for (File f : menuDir.listFiles()) {
			if (f.isDirectory()) {
				JMenu newSubMenu = new JMenu(f.getName());
				subMenu.add(newSubMenu);
				addSubmenMenuItems(newSubMenu, f);
		    } else {
                ActionListener processMenuListener = new ProcessMenuActionListener(
                        f, menuWorker);
                ObjectInputStream ois = new ObjectInputStream(
                        new BufferedInputStream(new FileInputStream(
                                f)));
                I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois
                        .readObject();
                ois.close();
                JMenuItem processMenuItem = new JMenuItem(process
                        .getName());
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
		menuItem.addActionListener(new ChangeEnvironmentPassword());
		fileMenu.add(menuItem);
		menuItem = new JMenuItem("Save Environment...");
		menuItem.addActionListener(new SaveEnvironment());
		fileMenu.add(menuItem);

		menuBar.add(fileMenu);
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

	private JComponent getContentPanel() throws DatabaseException, IOException,
			ClassNotFoundException {
		termTree = getHierarchyPanel();
		/*
		 * String htmlLabel = "<html><img src='" +
		 * ACE.class.getResource("/circle_red_x.gif") +"' border='0' ><img
		 * src='" + ACE.class.getResource("/triangle_yellow_exclamation.gif")
		 * +"' border='0' ></html>"; c1Panel = new JLabel(htmlLabel);
		 */
		conceptPanels = new ArrayList<ConceptPanel>();
		c1Panel = new ConceptPanel(this, LINK_TYPE.TREE_LINK, conceptTabs);
		conceptPanels.add(c1Panel);
		c2Panel = new ConceptPanel(this, LINK_TYPE.SEARCH_LINK, conceptTabs);
		conceptPanels.add(c2Panel);
		conceptTabs.addTab("Tree", ConceptPanel.SMALL_TREE_LINK_ICON, c1Panel,
				"Tree Linked");
		conceptTabs.addTab("Search", ConceptPanel.SMALL_SEARCH_LINK_ICON,
				c2Panel, "Search Linked");

		ConceptPanel c3panel = new ConceptPanel(this, LINK_TYPE.UNLINKED,
				conceptTabs);
		conceptPanels.add(c3panel);
		conceptTabs.addTab("Empty", null, c3panel, "Unlinked");
		ConceptPanel c4panel = new ConceptPanel(this, LINK_TYPE.UNLINKED,
				conceptTabs);
		conceptPanels.add(c3panel);
		conceptTabs.addTab("Empty 2", null, c4panel, "Unlinked 2");
		// conceptTabs.addTab("Description List", getDescListEditor());
		conceptTabs.addTab("List", getConceptListEditor());

		conceptTabs.setMinimumSize(new Dimension(0, 0));
		c2Panel.setMinimumSize(new Dimension(0, 0));

		termTreeConceptSplit.setRightComponent(conceptTabs);
		termTreeConceptSplit.setLeftComponent(termTree);
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

	private Component getConceptListEditor() throws DatabaseException,
			IOException, ClassNotFoundException {
		batchConceptList = new TerminologyList();
		return new CollectionEditorContainer(batchConceptList, this,
				descListProcessBuilderPanel);
	}

	protected JMenuItem newProcessMI, readProcessMI, takeProcessNoTranMI,
			takeProcessTranMI, saveProcessMI, saveForLauncherQueueMI,
			saveAsXmlMI;

	private JPanel masterProcessBuilderPanel;

	private JPanel descListProcessBuilderPanel;

	private JPanel workflowPanel;

	private JToggleButton showAddressesButton;

	private CdePalette addressPalette;

	private JList addressList;

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
		queuePalette.add(makeQueueViewerPanel(config, worker),
				BorderLayout.CENTER);
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

	public JPanel makeQueueViewerPanel(Configuration config, MasterWorker worker)
			throws RemoteException, InterruptedException, IOException,
			ConfigurationException, PrivilegedActionException {
		viewer = new QueueViewerPanel(config, worker);
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
		combinedPanel.add(viewer, c);
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
		return listEditorTopPanel;

	}

	private void makeSubversionPalette() throws Exception {

		JLayeredPane layers = getRootPane().getLayeredPane();
		subversionPalette = new CdePalette(new BorderLayout(),
				new RightPalettePoint());
		JTabbedPane tabs = new JTabbedPane();
		for (String key: aceFrameConfig.getSubversionMap().keySet()) {
			SvnPanel svnTable = new SvnPanel(aceFrameConfig, key);
			tabs.addTab("change sets", svnTable);
		}

		layers.add(subversionPalette, JLayeredPane.PALETTE_LAYER);
		subversionPalette.add(tabs, BorderLayout.CENTER);
		subversionPalette.setBorder(BorderFactory.createRaisedBevelBorder());

		int width = 650;
		int height = 550;
		Rectangle topBounds = topPanel.getBounds();
		subversionPalette.setSize(width, height);

		subversionPalette.setLocation(new Point(topBounds.x + topBounds.width,
				topBounds.y + topBounds.height + 1));
		subversionPalette.setOpaque(true);
		subversionPalette.doLayout();
		addComponentListener(subversionPalette);
		subversionPalette.setVisible(true);
	}

	private void makeConfigPalette() throws Exception {
		JLayeredPane layers = getRootPane().getLayeredPane();
		preferencesPalette = new CdePalette(new BorderLayout(),
				new RightPalettePoint());
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Path", new SelectPathAndPositionPanel(false, "for view",
				aceFrameConfig, new PropertySetListenerGlue(
						"removeViewPosition", "addViewPosition",
						"replaceViewPosition", "getViewPositionSet",
						I_Position.class, aceFrameConfig)));
		tabs.addTab("View", makeViewConfig());
		tabs.addTab("Edit", makeEditConfig());
		tabs.addTab("New Path", new CreatePathPanel(aceFrameConfig));

		layers.add(preferencesPalette, JLayeredPane.PALETTE_LAYER);
		preferencesPalette.add(tabs, BorderLayout.CENTER);
		preferencesPalette.setBorder(BorderFactory.createRaisedBevelBorder());

		int width = 500;
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

	private JComponent makeDescPrefPanel() {

		TerminologyListModel descTypeTableModel = new TerminologyListModel();
		for (int id : aceFrameConfig.getDescTypes().getSetValues()) {
			descTypeTableModel.addElement(ConceptBean.get(id));
		}
		descTypeTableModel.addListDataListener(aceFrameConfig.getDescTypes());
		TerminologyList descList = new TerminologyList(descTypeTableModel);
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
				shortLabelPrefOrderTableModel);

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
				longLabelPrefOrderTableModel);

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
				treeDescPrefOrderTableModel);

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
				descPrefOrderTableModel);

		prefOrderList.setBorder(BorderFactory
				.createTitledBorder("Table preference order: "));
		descPrefPanel.add(new JScrollPane(prefOrderList));

		return descPrefPanel;
	}

	private JComponent makeRelPrefPanel() {

		JPanel relPrefPanel = new JPanel(new GridLayout(0, 1));
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

	private TerminologyList makeTermList(String title, I_IntSet set) {
		TerminologyListModel browseDownRelModel = new TerminologyListModel();
		for (int id : set.getSetValues()) {
			browseDownRelModel.addElement(ConceptBean.get(id));
		}
		browseDownRelModel.addListDataListener(set);
		TerminologyList browseDownRelList = new TerminologyList(
				browseDownRelModel);
		browseDownRelList.setBorder(BorderFactory.createTitledBorder(title));
		return browseDownRelList;
	}

	private JComponent makeStatusPrefPanel() {
		TerminologyListModel statusValuesModel = new TerminologyListModel();
		for (int id : aceFrameConfig.getAllowedStatus().getSetValues()) {
			statusValuesModel.addElement(ConceptBean.get(id));
		}
		statusValuesModel
				.addListDataListener(aceFrameConfig.getAllowedStatus());
		TerminologyList statusList = new TerminologyList(statusValuesModel);
		statusList.setBorder(BorderFactory
				.createTitledBorder("Status values for display:"));
		return statusList;
	}

	private JComponent makeRootPrefPanel() {
		TerminologyListModel rootModel = new TerminologyListModel();
		for (int id : aceFrameConfig.getRoots().getSetValues()) {
			rootModel.addElement(ConceptBean.get(id));
		}
		rootModel.addListDataListener(aceFrameConfig.getRoots());
		TerminologyList statusList = new TerminologyList(rootModel);
		statusList.setBorder(BorderFactory
				.createTitledBorder("Hierarchy roots:"));
		return statusList;
	}

	private JTabbedPane makeViewConfig() throws Exception {
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("descriptions", makeDescPrefPanel());
		tabs.addTab("relationships", makeRelPrefPanel());
		tabs.addTab("status", makeStatusPrefPanel());
		tabs.addTab("roots", makeRootPrefPanel());
		return tabs;
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
		defaultStatus.addPropertyChangeListener("defaultStatus",
				new PropertyListenerGlue("setDefaultStatus",
						I_GetConceptData.class, aceFrameConfig));
		wrapAndAdd(defaultsPanel, defaultStatus, "Default status: ");

		TermComponentLabel defaultDescType = new TermComponentLabel(
				aceFrameConfig);
		defaultDescType.setTermComponent(aceFrameConfig
				.getDefaultDescriptionType());
		defaultDescType.addPropertyChangeListener("defaultDescriptionType",
				new PropertyListenerGlue("setDefaultDescriptionType",
						I_GetConceptData.class, aceFrameConfig));
		wrapAndAdd(defaultsPanel, defaultDescType, "Default description type: ");

		TermComponentLabel defaultRelType = new TermComponentLabel(
				aceFrameConfig);
		defaultRelType.setTermComponent(aceFrameConfig
				.getDefaultRelationshipType());
		defaultRelType.addPropertyChangeListener("defaultRelationshipType",
				new PropertyListenerGlue("setDefaultRelationshipType",
						I_GetConceptData.class, aceFrameConfig));
		wrapAndAdd(defaultsPanel, defaultRelType, "Default relationship type: ");

		TermComponentLabel defaultRelCharacteristicType = new TermComponentLabel(
				aceFrameConfig);
		defaultRelCharacteristicType.setTermComponent(aceFrameConfig
				.getDefaultRelationshipCharacteristic());
		defaultRelCharacteristicType.addPropertyChangeListener(
				"defaultRelationshipCharacteristic", new PropertyListenerGlue(
						"setDefaultRelationshipCharacteristic",
						I_GetConceptData.class, aceFrameConfig));
		wrapAndAdd(defaultsPanel, defaultRelCharacteristicType,
				"Default relationship characteristic: ");

		TermComponentLabel defaultRelRefinability = new TermComponentLabel(
				aceFrameConfig);
		defaultRelRefinability.setTermComponent(aceFrameConfig
				.getDefaultRelationshipRefinability());
		defaultRelRefinability.addPropertyChangeListener(
				"defaultRelationshipRefinability", new PropertyListenerGlue(
						"setDefaultRelationshipRefinability",
						I_GetConceptData.class, aceFrameConfig));
		wrapAndAdd(defaultsPanel, defaultRelRefinability,
				"Default relationship refinability: ");

		return defaultsPanel;
	}

	private void wrapAndAdd(JPanel defaultsPanel,
			TermComponentLabel defaultLabel, String borderTitle) {
		JPanel defaultStatusPanel = new JPanel(new GridLayout(1, 1));
		defaultStatusPanel.setBorder(BorderFactory
				.createTitledBorder(borderTitle));
		defaultStatusPanel.add(defaultLabel);
		defaultsPanel.add(defaultStatusPanel);
	}

	private JComponent makePopupConfigPanel(I_IntSet set, String borderLabel) {

		TerminologyList popupList = makeTermList(borderLabel, set);

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
				viewerHistoryTableModel);
		tabs.addTab("viewer", new JScrollPane(viewerList));
		TerminologyList commitList = new TerminologyList(
				commitHistoryTableModel);
		tabs.addTab("uncommitted", new JScrollPane(commitList));
		TerminologyList importList = new TerminologyList(
				importHistoryTableModel);
		tabs.addTab("imported", new JScrollPane(importList));
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
		addressList = new JList(aceFrameConfig.getAddressesList().toArray());
		addressPalette
				.add(new JScrollPane(addressList), BorderLayout.CENTER);
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

	JComponent getHierarchyPanel() {
		tree = new JTreeWithDragImage(aceFrameConfig);
		tree.putClientProperty("JTree.lineStyle", "None");
		tree.addMouseListener(new TreeMouseListener(aceFrameConfig));
		tree.setLargeModel(true);
		// tree.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		tree.setTransferHandler(new TerminologyTransferHandler());
		tree.setDragEnabled(true);
		tree.setCellRenderer(new TermTreeCellRenderer(aceFrameConfig));
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

		DefaultMutableTreeNode root = new DefaultMutableTreeNode(null, true);

		for (int rootId : aceFrameConfig.getRoots().getSetValues()) {
			root.add(new DefaultMutableTreeNode(ConceptBeanForTree.get(rootId,
					0, false), true));
		}
		model.setRoot(root);
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

	protected void treeValueChanged(TreeSelectionEvent evt) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) evt.getPath()
				.getLastPathComponent();
		String s = evt.isAddedPath() ? "Selected " + node : "";
		aceFrameConfig.setStatusMessage(s);
		if (node != null) {
			ConceptBeanForTree treeBean = (ConceptBeanForTree) node
					.getUserObject();
			aceFrameConfig.setHierarchySelection(treeBean.getCoreBean());
		} else {
			aceFrameConfig.setHierarchySelection(null);
		}
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

		aceFrameConfig.setStatusMessage("Collapsed " + node);
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
		TreeIdPath idPath = new TreeIdPath(evt.getPath());
		synchronized (expansionWorkers) {
			stopWorkersOnPath(idPath, "stopping before expansion");
			I_GetConceptDataForTree userObject = (I_GetConceptDataForTree) node
					.getUserObject();
			if (userObject != null) {
				aceFrameConfig.getChildrenExpandedNodes().add(
						userObject.getConceptId());
				aceFrameConfig.setStatusMessage("Expanding " + node + "...");
				ExpandNodeSwingWorker worker = new ExpandNodeSwingWorker(
						(DefaultTreeModel) tree.getModel(), tree, node,
						new CompareConceptBeanInitialText(), this);
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
		showHistoryButton
				.setToolTipText("history of user commits and concepts viewed");
		showHistoryButton.addActionListener(new HistoryPaletteActionListener());
		topPanel.add(showHistoryButton, c);
		c.gridx++;
		showAddressesButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/address_book3.png")));
		showAddressesButton
				.setToolTipText("address book of project participants");
		showAddressesButton
				.addActionListener(new AddressPaletteActionListener());
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
				.setToolTipText("Show the tree view of the terminology content.");
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

		File componentPluginDir = new File("plugins" + File.separator
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

		showQueuesButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/inbox.png")));
		topPanel.add(showQueuesButton, c);
		showQueuesButton.addActionListener(new QueuesPaletteActionListener());
		showQueuesButton.setToolTipText("Show the queue viewer...");
		c.gridx++;

		showProcessBuilder = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/cube_molecule.png")));
		topPanel.add(showProcessBuilder, c);
		showProcessBuilder
				.addActionListener(new ProcessPaletteActionListener());
		showProcessBuilder.setToolTipText("Show the process builder...");
		c.gridx++;

		showSubversionButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/svn.png")));
		topPanel.add(showSubversionButton, c);
		showSubversionButton
				.addActionListener(new SubversionPaletteActionListener());
		showSubversionButton.setToolTipText("Show Subversion panel...");
		c.gridx++;
		showPreferencesButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/preferences.png")));
		showPreferencesButton
				.addActionListener(new PreferencesPaletteActionListener());
		topPanel.add(showPreferencesButton, c);
		showPreferencesButton.setToolTipText("Show preferences panel...");
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

				worker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(),
						aceFrameConfig);
				worker.writeAttachment(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS
						.name(), this);
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
		showSearchButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/find.png")));
		showSearchButton.addActionListener(bottomPanelActionListener);
		bottomPanel.add(showSearchButton, c);
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
		c.gridx++;
		commitButton = new JButton("commit");
		commitButton.setEnabled(false);
		commitButton.addActionListener(new Commit());
		bottomPanel.add(commitButton, c);
		c.gridx++;
		bottomPanel.add(new JLabel("   "), c);
		c.gridx++;

		return bottomPanel;
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
			commitHistoryTableModel.clear();
			removeConfigPalette();
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
			while (viewerHistoryTableModel.getSize() > 40) {
				viewerHistoryTableModel.removeElement(viewerHistoryTableModel
						.getSize() - 1);
			}
		} else if (evt.getPropertyName().equals("uncommitted")) {
			commitHistoryTableModel.clear();
			for (I_Transact t : uncommitted) {
				if (ConceptBean.class.isAssignableFrom(t.getClass())) {
					commitHistoryTableModel.addElement((ConceptBean) t);
				}
			}
		} else if (evt.getPropertyName().equals("imported")) {
			importHistoryTableModel.clear();
			for (I_Transact t : imported) {
				if (ConceptBean.class.isAssignableFrom(t.getClass())) {
					importHistoryTableModel.addElement((ConceptBean) t);
				}
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

}
