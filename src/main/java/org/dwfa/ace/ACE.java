package org.dwfa.ace;

import java.awt.BorderLayout;
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
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

import org.dwfa.ace.ConceptPanel.LINK_TYPE;
import org.dwfa.ace.actions.Abort;
import org.dwfa.ace.actions.Commit;
import org.dwfa.ace.actions.ImportBaselineJar;
import org.dwfa.ace.actions.ImportChangesetJar;
import org.dwfa.ace.actions.SaveEnvironment;
import org.dwfa.ace.actions.WriteJar;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.config.CreatePathPanel;
import org.dwfa.ace.config.SelectPathAndPositionPanel;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.search.SearchPanel;
import org.dwfa.ace.tree.ConceptBeanForTree;
import org.dwfa.ace.tree.ExpandNodeSwingWorker;
import org.dwfa.ace.tree.I_GetConceptDataForTree;
import org.dwfa.ace.tree.JTreeWithDragImage;
import org.dwfa.ace.tree.TermTreeCellRenderer;
import org.dwfa.ace.tree.TreeIdPath;
import org.dwfa.ace.tree.TreeMouseListener;
import org.dwfa.bpa.gui.glue.PropertySetListenerGlue;
import org.dwfa.svn.CheckoutPanel;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.I_GetConceptData;
import org.dwfa.vodb.types.I_Transact;
import org.dwfa.vodb.types.Position;
import org.dwfa.vodb.types.TimePathId;

import com.sleepycat.je.DatabaseException;

public class ACE extends JPanel implements PropertyChangeListener {

	private static List<I_Transact> uncommitted = new ArrayList<I_Transact>();

	public static void addUncommitted(I_Transact to) {
		uncommitted.add(to);
	}

	public static void removeUncommitted(I_Transact to) {
		uncommitted.remove(to);
	}

	/*
	 * 
	 */
	public static void commit() throws DatabaseException {
		Date now = new Date();
		Set<TimePathId> values = new HashSet<TimePathId>();
		int version = ThinVersionHelper.convert(now.getTime());
		for (I_Transact cb : uncommitted) {
			cb.commit(version, values);
		}
		AceConfig.vodb.addTimeBranchValues(values);
		AceConfig.vodb.sync();
		uncommitted.clear();
	}

	public static void abort() throws DatabaseException {
		for (I_Transact cb : uncommitted) {
			cb.abort();
		}
		uncommitted.clear();
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

	private class ConfigPaletteActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (configPalette == null) {
				try {
					makeConfigPalette();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			getRootPane().getLayeredPane().moveToFront(configPalette);
			configPalette.togglePalette();
		}

	}

	private class SubversionPaletteActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (subversionPalette == null) {
				try {
					makeSubversionPalette();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			getRootPane().getLayeredPane().moveToFront(subversionPalette);
			subversionPalette.togglePalette();
		}

	}

	
	private class HistoryPaletteActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (historyPalette == null) {
				makeHistoryPalette();
			}
			getRootPane().getLayeredPane().moveToFront(historyPalette);
			historyPalette.togglePalette();
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
				// System.out.println(dividerLocation);
			}
			if (showTreeButton.isSelected()
					&& (showComponentButton.isSelected() == false)) {
				dividerLocation = termTreeConceptSplit.getDividerLocation();
				// System.out.println(dividerLocation);
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

	private JLabel statusLabel = new JLabel();

	private JTreeWithDragImage tree;

	private JPanel topPanel;

	private JTabbedPane conceptTabs = new JTabbedPane();

	private ConceptPanel c1Panel;

	private JComponent c2Panel;

	private JComponent termTree;

	private SearchPanel searchPanel;

	private JSplitPane upperLowerSplit = new JSplitPane(
			JSplitPane.VERTICAL_SPLIT);

	private JSplitPane termTreeConceptSplit = new JSplitPane(
			JSplitPane.HORIZONTAL_SPLIT);

	private JToggleButton showComponentButton;

	private JToggleButton showTreeButton;
	private JToggleButton showSubversionButton;

	private TogglePanelsActionListener resizeListener = new TogglePanelsActionListener();

	private ManageBottomPaneActionListener bottomPanelActionListener = new ManageBottomPaneActionListener();

	private CdePalette configPalette;
	
	private CdePalette subversionPalette;

	private JToggleButton showHistoryButton;

	private CdePalette historyPalette;

	private JToggleButton showSearchButton;

	public static ExecutorService threadPool = Executors.newFixedThreadPool(5);

	public static ExecutorService treeExpandThread = Executors
			.newFixedThreadPool(1);

	public static Timer timer = new Timer();

	private AceFrameConfig aceFrameConfig;

	private JPanel treeProgress;

	private AceConfig aceConfig;

	private JMenu fileMenu = new JMenu("File");

	private JMenu editMenu = new JMenu("Edit");

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private class RightPalettePoint implements I_GetPalettePoint {

		public Point getPalettePoint() {
			return new Point(topPanel.getLocation().x + topPanel.getWidth(),
					topPanel.getLocation().y + topPanel.getHeight() + 1);
		}

	}

	private class LeftPalettePoint implements I_GetPalettePoint {

		public Point getPalettePoint() {
			return new Point(topPanel.getLocation().x, topPanel.getLocation().y
					+ topPanel.getHeight() + 1);
		}

	}

	/**
	 * http://java.sun.com/developer/JDCTechTips/2003/tt1210.html#2
	 * 
	 * @param aceFrameConfig
	 * @throws DatabaseException 
	 * 
	 * @throws DatabaseException
	 */
	public ACE() {
		super(new GridBagLayout());
	}

	public void setup(AceFrameConfig aceFrameConfig, AceConfig aceConfig) throws DatabaseException {
		this.aceFrameConfig = aceFrameConfig;
		this.aceFrameConfig.addPropertyChangeListener(this);
		this.aceConfig = aceConfig;
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
	}
	
	

	public JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		addToMenuBar(menuBar);
		return menuBar;
	}
	public JMenuBar addToMenuBar(JMenuBar menuBar) {
		addFileMenu(menuBar);
		addEditMenu(menuBar);
		return menuBar;
	}

	private void addEditMenu(JMenuBar menuBar) {
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

	public void addFileMenu(JMenuBar menuBar) {
		JMenuItem menuItem = null;
		menuItem = new JMenuItem("Export baseline jar...");
		menuItem.addActionListener(new WriteJar(aceConfig));
		fileMenu.add(menuItem);
		fileMenu.addSeparator();
		menuItem = new JMenuItem("Import changeset jar...");
		menuItem.addActionListener(new ImportChangesetJar());
		fileMenu.add(menuItem);
		menuItem = new JMenuItem("Import baseline jar...");
		menuItem.addActionListener(new ImportBaselineJar());
		fileMenu.add(menuItem);
		fileMenu.addSeparator();
		menuItem = new JMenuItem("Save Environment...");
		menuItem.addActionListener(new SaveEnvironment());
		fileMenu.add(menuItem);
		menuBar.add(fileMenu);
	}

	private JComponent getContentPanel() throws DatabaseException {
		termTree = getHierarchyPanel();
		/*
		 * String htmlLabel = "<html><img src='" +
		 * ACE.class.getResource("/circle_red_x.gif") +"' border='0' ><img
		 * src='" + ACE.class.getResource("/triangle_yellow_exclamation.gif")
		 * +"' border='0' ></html>"; c1Panel = new JLabel(htmlLabel);
		 */
		c1Panel = new ConceptPanel(this, LINK_TYPE.TREE_LINK, conceptTabs);
		c2Panel = new ConceptPanel(this, LINK_TYPE.SEARCH_LINK, conceptTabs);
		conceptTabs.addTab("Tree", ConceptPanel.SMALL_TREE_LINK_ICON, c1Panel,
				"Tree Linked");
		conceptTabs.addTab("Search", ConceptPanel.SMALL_SEARCH_LINK_ICON,
				c2Panel, "Search Linked");
		conceptTabs.addTab("Empty", null, new ConceptPanel(this,
				LINK_TYPE.UNLINKED, conceptTabs), "Unlinked");
		conceptTabs.addTab("Empty 2", null, new ConceptPanel(this,
				LINK_TYPE.UNLINKED, conceptTabs), "Unlinked 2");

		conceptTabs.setMinimumSize(new Dimension(0, 0));
		c2Panel.setMinimumSize(new Dimension(0, 0));

		termTreeConceptSplit.setRightComponent(conceptTabs);
		termTreeConceptSplit.setLeftComponent(termTree);
		termTree.setMinimumSize(new Dimension(0, 0));
		termTreeConceptSplit.setOneTouchExpandable(true);
		termTreeConceptSplit.setContinuousLayout(true);
		termTreeConceptSplit.setDividerLocation(aceFrameConfig.getTreeTermDividerLoc());
		termTreeConceptSplit.setResizeWeight(0.5);
		termTreeConceptSplit.setLastDividerLocation(aceFrameConfig.getTreeTermDividerLoc());

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
	
	private void makeSubversionPalette() throws Exception {
		
		JLayeredPane layers = getRootPane().getLayeredPane();
		subversionPalette = new CdePalette(new BorderLayout(),
				new RightPalettePoint());
		JTabbedPane tabs = new JTabbedPane();
		CheckoutPanel svnTable = new CheckoutPanel();
		tabs.addTab("checkout", new JScrollPane(svnTable));

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
		configPalette = new CdePalette(new BorderLayout(),
				new RightPalettePoint());
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Path", new SelectPathAndPositionPanel(false, "for view",
				aceFrameConfig, new PropertySetListenerGlue("removeViewPosition",
						"addViewPosition", "replaceViewPosition",
						"getViewPositionSet", Position.class, aceFrameConfig)));
		tabs.addTab("View", makeViewConfig());
		tabs.addTab("Edit", makeEditConfig());
		tabs.addTab("New Path", new CreatePathPanel(aceFrameConfig));

		layers.add(configPalette, JLayeredPane.PALETTE_LAYER);
		configPalette.add(tabs, BorderLayout.CENTER);
		configPalette.setBorder(BorderFactory.createRaisedBevelBorder());

		int width = 500;
		int height = 550;
		Rectangle topBounds = topPanel.getBounds();
		configPalette.setSize(width, height);

		configPalette.setLocation(new Point(topBounds.x + topBounds.width,
				topBounds.y + topBounds.height + 1));
		configPalette.setOpaque(true);
		configPalette.doLayout();
		addComponentListener(configPalette);
		configPalette.setVisible(true);

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
		for (int id: aceFrameConfig.getShortLabelDescPreferenceList().getListValues()) {
			shortLabelPrefOrderTableModel.addElement(ConceptBean.get(id));
		}
		shortLabelPrefOrderTableModel.addListDataListener(aceFrameConfig.getShortLabelDescPreferenceList());
		TerminologyList shortLabelOrderList = new TerminologyList(shortLabelPrefOrderTableModel); 

		shortLabelOrderList.setBorder(BorderFactory
				.createTitledBorder("Short Label preference order: "));
		descPrefPanel.add(new JScrollPane(shortLabelOrderList));
		
		TerminologyListModel longLabelPrefOrderTableModel = new TerminologyListModel();
		for (int id: aceFrameConfig.getLongLabelDescPreferenceList().getListValues()) {
			longLabelPrefOrderTableModel.addElement(ConceptBean.get(id));
		}
		longLabelPrefOrderTableModel.addListDataListener(aceFrameConfig.getLongLabelDescPreferenceList());
		TerminologyList longLabelOrderList = new TerminologyList(longLabelPrefOrderTableModel); 

		longLabelOrderList.setBorder(BorderFactory
				.createTitledBorder("Long label preference order: "));
		descPrefPanel.add(new JScrollPane(longLabelOrderList));
		
		TerminologyListModel treeDescPrefOrderTableModel = new TerminologyListModel();
		for (int id: aceFrameConfig.getTreeDescPreferenceList().getListValues()) {
			treeDescPrefOrderTableModel.addElement(ConceptBean.get(id));
		}
		treeDescPrefOrderTableModel.addListDataListener(aceFrameConfig.getTreeDescPreferenceList());
		TerminologyList treePrefOrderList = new TerminologyList(treeDescPrefOrderTableModel); 

		treePrefOrderList.setBorder(BorderFactory
				.createTitledBorder("Tree preference order: "));
		descPrefPanel.add(new JScrollPane(treePrefOrderList));

		TerminologyListModel descPrefOrderTableModel = new TerminologyListModel();
		for (int id: aceFrameConfig.getTableDescPreferenceList().getListValues()) {
			descPrefOrderTableModel.addElement(ConceptBean.get(id));
		}
		descPrefOrderTableModel.addListDataListener(aceFrameConfig.getTableDescPreferenceList());
		TerminologyList prefOrderList = new TerminologyList(descPrefOrderTableModel); 

		prefOrderList.setBorder(BorderFactory
				.createTitledBorder("Table preference order: "));
		descPrefPanel.add(new JScrollPane(prefOrderList));
		
		return descPrefPanel;
	}

	private JComponent makeRelPrefPanel() {

				
		JPanel relPrefPanel = new JPanel(new GridLayout(0, 1));
		relPrefPanel.add(new JScrollPane(makeTermList("parent relationships:", aceFrameConfig.getDestRelTypes())));
		relPrefPanel.add(new JScrollPane(makeTermList("child relationships:", aceFrameConfig.getSourceRelTypes())));
		relPrefPanel.add(new JScrollPane(makeTermList("stated view characteristic types:", aceFrameConfig.getStatedViewTypes())));
		relPrefPanel.add(new JScrollPane(makeTermList("inferred view characteristic types:", aceFrameConfig.getInferredViewTypes())));
		return relPrefPanel;
	}

	private TerminologyList makeTermList(String title, IntSet conceptSet) {
		TerminologyListModel browseDownRelModel = new TerminologyListModel();
		for (int id : conceptSet.getSetValues()) {
			browseDownRelModel.addElement(ConceptBean.get(id));
		}
		browseDownRelModel.addListDataListener(conceptSet);
		TerminologyList browseDownRelList = new TerminologyList(
				browseDownRelModel);
		browseDownRelList.setBorder(BorderFactory
				.createTitledBorder(title));
		return browseDownRelList;
	}

	private JComponent makeStatusPrefPanel() {
		TerminologyListModel statusValuesModel = new TerminologyListModel();
		for (int id : aceFrameConfig.getAllowedStatus().getSetValues()) {
			statusValuesModel.addElement(ConceptBean.get(id));
		}
		statusValuesModel.addListDataListener(aceFrameConfig.getAllowedStatus());
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
		tabs.addTab("rel type", new JScrollPane(makePopupConfigPanel(aceFrameConfig.getEditRelTypePopup(), "Relationship types for popup:")));
		tabs.addTab("rel refinabilty", new JScrollPane(makePopupConfigPanel(aceFrameConfig.getEditRelRefinabiltyPopup(), "Relationship types for popup:")));
		tabs.addTab("rel characteristic", new JScrollPane(makePopupConfigPanel(aceFrameConfig.getEditRelCharacteristicPopup(), "Relationship types for popup:")));
		tabs.addTab("desc type", new JScrollPane(makePopupConfigPanel(aceFrameConfig.getEditDescTypePopup(), "Description types for popup:")));
		tabs.addTab("status", new JScrollPane(makePopupConfigPanel(aceFrameConfig.getEditStatusTypePopup(), "Status values for popup:")));
		return tabs;
	}

	private JComponent madeDefaultsPanel() {
		JPanel defaultsPanel = new JPanel(new GridLayout(0, 1));
		
		TermComponentLabel defaultStatus = new TermComponentLabel(aceFrameConfig);
		defaultStatus.setBorder(BorderFactory.createTitledBorder("Default status: "));
		defaultsPanel.add(defaultStatus);
		
		TermComponentLabel defaultDescType = new TermComponentLabel(aceFrameConfig);
		defaultDescType.setBorder(BorderFactory.createTitledBorder("Default description type: "));
		defaultsPanel.add(defaultDescType);
		
		TermComponentLabel defaultRelType = new TermComponentLabel(aceFrameConfig);
		defaultRelType.setBorder(BorderFactory.createTitledBorder("Default relationship type: "));
		defaultsPanel.add(defaultRelType);
		
		TermComponentLabel defaultRelCharacteristicType = new TermComponentLabel(aceFrameConfig);
		defaultRelCharacteristicType.setBorder(BorderFactory.createTitledBorder("Default relationship characteristic: "));
		defaultsPanel.add(defaultRelCharacteristicType);
		
		TermComponentLabel defaultRelRefinabilityType = new TermComponentLabel(aceFrameConfig);
		defaultRelRefinabilityType.setBorder(BorderFactory.createTitledBorder("Default relationship refinability: "));
		defaultsPanel.add(defaultRelRefinabilityType);
		
		
		return defaultsPanel;
	}
	private JComponent makePopupConfigPanel(IntSet popupSet, String borderLabel) {

		TerminologyList popupList = makeTermList(borderLabel, popupSet);

		JPanel popupPanel = new JPanel(new GridLayout(0, 1));
		popupPanel.add(new JScrollPane(popupList));
		return popupPanel;
	}

	private void makeHistoryPalette() {
		JLayeredPane layers = getRootPane().getLayeredPane();
		historyPalette = new CdePalette(new BorderLayout(),
				new LeftPalettePoint());
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("viewer history", new JLabel("viewer"));
		tabs.addTab("commit history", new JLabel("commit"));
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
		
		for (int rootId: aceFrameConfig.getRoots().getSetValues()) {
			root.add(new DefaultMutableTreeNode(ConceptBeanForTree.get(
					rootId, 0, false), true));
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
			System.out.println("Child expand: " + id);
		}
		for (int id : aceFrameConfig.getParentExpandedNodes().getSetValues()) {
			System.out.println("Parent expand: " + id);
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
		Object node = evt.getPath().getLastPathComponent();
		String s = evt.isAddedPath() ? "Selected " + node : "";
		statusLabel.setText(s);
	}

	protected void treeTreeCollapsed(TreeExpansionEvent evt) {
		I_GetConceptDataForTree userObject = handleCollapse(evt);
		aceFrameConfig.getChildrenExpandedNodes().remove(userObject.getConceptId());

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

		statusLabel.setText("Collapsed " + node);
		return userObject;
	}

	private void stopWorkersOnPath(TreeIdPath idPath, String message) {
		synchronized (expansionWorkers) {
			if (idPath == null) {
				List<TreeIdPath> allKeys = new ArrayList<TreeIdPath>(
						expansionWorkers.keySet());
				for (TreeIdPath key : allKeys) {
					System.out.println("  Stopping all: " + key);
					removeAnyMatchingExpansionWorker(key, message);
				}
			} else {
				if (expansionWorkers.containsKey(idPath)) {
					System.out.println("  Stopping: " + idPath);
					removeAnyMatchingExpansionWorker(idPath, message);
				}

				List<TreeIdPath> otherKeys = new ArrayList<TreeIdPath>(
						expansionWorkers.keySet());
				for (TreeIdPath key : otherKeys) {
					if (key.initiallyEqual(idPath)) {
						System.out.println("  Stopping child: " + key);
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


	public void removeExpansionWorker(TreeIdPath key, ExpandNodeSwingWorker worker, String message) {
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
				aceFrameConfig.getChildrenExpandedNodes().add(userObject.getConceptId());
				statusLabel.setText("Expanding " + node + "...");
				ExpandNodeSwingWorker worker = new ExpandNodeSwingWorker(
						(DefaultTreeModel) tree.getModel(), tree,
						node, new CompareConceptBeanInitialText(), this);
				treeExpandThread.execute(worker);
				expansionWorkers.put(idPath, worker);
			}
		}
	}


	JPanel getTopPanel() {
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
		treeProgress = new JPanel(new GridLayout(1,1));
		topPanel.add((JPanel) treeProgress, c);
		c.gridx++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		topPanel.add(new JPanel(), c);
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.gridx++;
		//topPanel.add(getComponentToggles2(), c);
		//c.gridx++;
		
		showSubversionButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/svn.png")));
		topPanel.add(showSubversionButton, c);
		showSubversionButton.addActionListener(new SubversionPaletteActionListener());
		c.gridx++;
		JToggleButton vpb = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/preferences.png")));
		vpb.addActionListener(new ConfigPaletteActionListener());
		topPanel.add(vpb, c);
		c.gridx++;

		return topPanel;
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
		JButton cancel = new JButton("cancel");
		cancel.addActionListener(new Abort());
		bottomPanel.add(cancel, c);
		c.gridx++;
		JButton commit = new JButton("commit");
		commit.addActionListener(new Commit());
		bottomPanel.add(commit, c);
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

	public AceFrameConfig getAceFrameConfig() {
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
		if (evt.getPropertyName().equals("viewPosition")) {
			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) model
					.getRoot();
			stopWorkersOnPath(null, "stopping for property change");
			for (int i = 0; i < root.getChildCount(); i++) {
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) root
						.getChildAt(i);
				I_GetConceptData cb = (I_GetConceptData) childNode
						.getUserObject();
				if (aceFrameConfig.getChildrenExpandedNodes().contains(
						cb.getConceptId())) {
					TreePath tp = new TreePath(childNode);
					TreeExpansionEvent treeEvent = new TreeExpansionEvent(
							model, tp);
					handleCollapse(treeEvent);
					treeTreeExpanded(treeEvent);
				}
			}

		}
	}

	public JMenu getEditMenu() {
		return editMenu;
	}

	public JMenu getFileMenu() {
		return fileMenu;
	}

}
