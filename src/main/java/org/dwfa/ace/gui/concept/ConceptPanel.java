package org.dwfa.ace.gui.concept;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.AbstractSpinnerModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.dwfa.ace.ACE;
import org.dwfa.ace.AceLog;
import org.dwfa.ace.I_ContainTermComponent;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.TermComponentSelectionListener;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.edit.AddConceptPart;
import org.dwfa.ace.edit.AddDescription;
import org.dwfa.ace.table.ConceptTableModel;
import org.dwfa.ace.table.ConceptTableRenderer;
import org.dwfa.ace.table.DescriptionTableModel;
import org.dwfa.ace.table.DescriptionTableRenderer;
import org.dwfa.ace.table.DescriptionsForConceptTableModel;
import org.dwfa.ace.table.I_CellTextWithTuple;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.ConceptTableModel.CONCEPT_FIELD;
import org.dwfa.ace.table.ConceptTableModel.StringWithConceptTuple;
import org.dwfa.ace.table.DescriptionTableModel.DESC_FIELD;
import org.dwfa.ace.table.DescriptionTableModel.StringWithDescTuple;
import org.dwfa.ace.tree.JTreeWithDragImage;
import org.dwfa.ace.tree.LineageTreeCellRenderer;
import org.dwfa.bpa.util.TableSorter;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.I_AmTermComponent;
import org.dwfa.vodb.types.ThinDescTuple;
import org.dwfa.vodb.types.ThinRelTuple;

import com.sleepycat.je.DatabaseException;

public class ConceptPanel extends JPanel implements I_HostConceptPlugins,
		PropertyChangeListener {

	public enum LINK_TYPE {
		UNLINKED, SEARCH_LINK, TREE_LINK
	};

	private class ShowPluginComponentActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						contentScroller.setViewportView(getContentPane());
					} catch (DatabaseException e) {
						AceLog.alertAndLog(ConceptPanel.this, Level.SEVERE, "Database Exception: " + e.getLocalizedMessage(), e);
					}
				}
			});
		}

	}

	private class ToggleLineageChangeActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						contentScroller.setViewportView(getContentPane());
					} catch (DatabaseException e) {
						AceLog.alertAndLog(ConceptPanel.this, Level.SEVERE, "Database Exception: " + e.getLocalizedMessage(), e);
					}
				}
			});
		}

	}

	private class ToggleHistoryChangeActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			firePropertyChange(I_HostConceptPlugins.SHOW_HISTORY, !historyButton.isSelected(), historyButton.isSelected());
			for (TableModelListener l : conceptTableModel
					.getTableModelListeners()) {
				conceptTableModel.removeTableModelListener(l);
			}
			removeTermChangeListener(conceptTableModel);
			conceptTableModel = new ConceptTableModel(getConceptColumns(),
					ConceptPanel.this);

			for (TableModelListener l : descTableModel.getTableModelListeners()) {
				descTableModel.removeTableModelListener(l);
			}
			removeTermChangeListener(descTableModel);
			descTableModel = new DescriptionsForConceptTableModel(
					getDescColumns(), ConceptPanel.this);


			try {
				contentScroller.setViewportView(getContentPane());
				PropertyChangeEvent pce = new PropertyChangeEvent(
						ConceptPanel.this, "termComponent", null, label
								.getTermComponent());
				conceptTableModel.fireTableDataChanged();
				conceptTableModel.propertyChange(pce);
				descTableModel.propertyChange(pce);
				descTableModel.fireTableDataChanged();
			} catch (DatabaseException e1) {
				AceLog.alertAndLog(ConceptPanel.this, Level.SEVERE, "Database Exception: " + e1.getLocalizedMessage(), e1);
			}
		}
	}


	private class ToggleConceptChangeActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			JToggleButton toggle = (JToggleButton) e.getSource();
			if (toggle.isSelected()) {
				conceptTableModel = new ConceptTableModel(getConceptColumns(),
						ConceptPanel.this);
			} else {
				for (TableModelListener l : conceptTableModel
						.getTableModelListeners()) {
					conceptTableModel.removeTableModelListener(l);
				}
				removeTermChangeListener(conceptTableModel);
			}
			try {
				contentScroller.setViewportView(getContentPane());
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						PropertyChangeEvent pce = new PropertyChangeEvent(
								ConceptPanel.this, "termComponent", null, label
										.getTermComponent());
						conceptTableModel.propertyChange(pce);
						conceptTableModel.fireTableDataChanged();
					}
				});
			} catch (DatabaseException e1) {
				AceLog.alertAndLog(ConceptPanel.this, Level.SEVERE,
						"Database Exception: " + e1.getLocalizedMessage(), e1);
			}
		}

	}

	private class ToggleDescChangeActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			JToggleButton toggle = (JToggleButton) e.getSource();
			if (toggle.isSelected()) {
				descTableModel = new DescriptionsForConceptTableModel(
						getDescColumns(), ConceptPanel.this);
			} else {
				for (TableModelListener l : descTableModel
						.getTableModelListeners()) {
					descTableModel.removeTableModelListener(l);
				}
				removeTermChangeListener(descTableModel);
			}
			try {
				contentScroller.setViewportView(getContentPane());
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						PropertyChangeEvent pce = new PropertyChangeEvent(
								ConceptPanel.this, "termComponent", null, label
										.getTermComponent());
						descTableModel.propertyChange(pce);
						descTableModel.fireTableDataChanged();
					}
				});
			} catch (DatabaseException e1) {
				AceLog.alertAndLog(ConceptPanel.this, Level.SEVERE, "Database Exception: " + e1.getLocalizedMessage(), e1);
			}
		}

	}



	TermComponentLabel label;

	PropertyChangeListener labelListener = new LabelListener();

	private JToggleButton descButton;

	private JToggleButton lineageButton;

	private JToggleButton historyButton;

	private JScrollPane contentScroller;

	private DescriptionsForConceptTableModel descTableModel;

	private ConceptTableModel conceptTableModel;

	private JToggleButton usePrefButton;

	private ACE ace;

	public static ImageIcon UNLINKED_ICON = new ImageIcon(ACE.class
			.getResource("/24x24/plain/carabiner.png"));

	public static ImageIcon SEARCH_LINK_ICON = new ImageIcon(ACE.class
			.getResource("/24x24/plain/carabiner_find.png"));

	public static ImageIcon TREE_LINK_ICON = new ImageIcon(ACE.class
			.getResource("/24x24/plain/carabiner_tree.png"));

	public static ImageIcon SMALL_SEARCH_LINK_ICON = new ImageIcon(ACE.class
			.getResource("/16x16/plain/find.png"));

	public static ImageIcon SMALL_TREE_LINK_ICON = new ImageIcon(ACE.class
			.getResource("/16x16/plain/text_tree.png"));
	
	private ConflictPlugin conflictPlugin = new ConflictPlugin();
	private AbstractPlugin srcRelPlugin = new SrcRelPlugin();
	private DestRelPlugin destRelPlugin = new DestRelPlugin();
	private IdPlugin idPlugin = new IdPlugin();
	private ImagePlugin imagePlugin = new ImagePlugin();
	
	private List<I_PluginToConceptPanel> plugins = new ArrayList<I_PluginToConceptPanel>(
			Arrays.asList(new I_PluginToConceptPanel[] { idPlugin, srcRelPlugin, destRelPlugin, imagePlugin, conflictPlugin }));

	public ImageIcon tabIcon;

	private JTabbedPane conceptTabs;

	private JToggleButton inferredButton;

	private ToggleHistoryChangeActionListener historyChangeActionListener;

	private JToggleButton conceptButton;

	private JTreeWithDragImage lineageTree;

	private LineageTreeCellRenderer lineageRenderer;

	private class LabelListener implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent evt) {
			updateTab(label.getTermComponent());
			try {
				updateLineageModel();
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt
					.getNewValue());
		}

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private class LinkListModel extends AbstractSpinnerModel {
		ImageIcon[] items;

		int currentSelection = 0;

		public LinkListModel(ImageIcon[] items) {
			this(items, 0);
		}

		public LinkListModel(ImageIcon[] items, int currentSelection) {
			super();
			this.items = items;
			this.currentSelection = currentSelection;
		}

		public Object getNextValue() {
			currentSelection++;
			if (currentSelection >= items.length) {
				currentSelection = 0;
			}
			return getValue();
		}

		public Object getPreviousValue() {
			currentSelection--;
			if (currentSelection < 0) {
				currentSelection = items.length - 1;
			}
			return getValue();
		}

		public Object getValue() {
			return items[currentSelection];
		}

		public void setValue(Object value) {
			for (int i = 0; i < items.length; i++) {
				if (items[i] == value) {
					currentSelection = i;
					changeLinkListener(LINK_TYPE.values()[i]);
					break;
				}
			}
			fireStateChanged();
		}

	}

	private class LinkEditor extends JLabel implements ChangeListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public LinkEditor(JSpinner spinner) {
			setOpaque(true);

			// Get info from the model.
			LinkListModel myModel = (LinkListModel) (spinner.getModel());
			Icon value = (Icon) myModel.getValue();
			setIcon(value);
			spinner.addChangeListener(this);

			// Set tool tip text.
			updateToolTipText(spinner);

			// Set size info.
			Dimension size = new Dimension(value.getIconWidth() + 4, value
					.getIconHeight() + 4);
			setMinimumSize(size);
			setPreferredSize(size);
		}

		protected void updateToolTipText(JSpinner spinner) {
			String toolTipText = spinner.getToolTipText();
			if (toolTipText != null) {
				// JSpinner has tool tip text. Use it.
				if (!toolTipText.equals(getToolTipText())) {
					setToolTipText(toolTipText);
				}
			} else {
				// Define our own tool tip text.
				LinkListModel myModel = (LinkListModel) (spinner.getModel());
				Icon value = (Icon) myModel.getValue();
				if (value == SEARCH_LINK_ICON) {
					setToolTipText("This panel is linked to the search selection");
					tabIcon = SMALL_SEARCH_LINK_ICON;
				} else if (value == TREE_LINK_ICON) {
					setToolTipText("This panel is linked to the tree selection");
					tabIcon = SMALL_TREE_LINK_ICON;
				} else if (value == UNLINKED_ICON) {
					setToolTipText("This panel is not linked to other selections");
					tabIcon = null;
				}
			}
		}

		public void stateChanged(ChangeEvent e) {
			JSpinner mySpinner = (JSpinner) (e.getSource());
			LinkListModel myModel = (LinkListModel) (mySpinner.getModel());
			setIcon((Icon) myModel.getValue());
			updateToolTipText(mySpinner);
			updateTab(label.getTermComponent());
		}
	}

	public ConceptPanel(ACE ace, LINK_TYPE link) throws DatabaseException {
		this(ace, link, null);
	}

	public ConceptPanel(ACE ace, LINK_TYPE link, JTabbedPane conceptTabs)
			throws DatabaseException {
		super(new GridBagLayout());
		this.ace = ace;
		label = new TermComponentLabel(this.ace.getAceFrameConfig());
		historyChangeActionListener = new ToggleHistoryChangeActionListener();
		this.ace.getAceFrameConfig().addPropertyChangeListener(this);
		this.conceptTabs = conceptTabs;
		GridBagConstraints c = new GridBagConstraints();
		LinkListModel linkSpinnerModel = new LinkListModel(new ImageIcon[] {
				UNLINKED_ICON, SEARCH_LINK_ICON, TREE_LINK_ICON }, link
				.ordinal());
		JSpinner linkSpinner = new JSpinner(linkSpinnerModel);
		linkSpinner.setBorder(BorderFactory.createEmptyBorder(3, 3, 2, 5));

		linkSpinner.setEditor(new LinkEditor(linkSpinner));
		changeLinkListener(link);

		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		add(linkSpinner, c);
		c.gridx++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		add(label, c);

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(getToggleBar(), c);
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0;
		c.gridy++;

		conceptTableModel = new ConceptTableModel(getConceptColumns(), this);
		descTableModel = new DescriptionsForConceptTableModel(getDescColumns(),
				this);

		contentScroller = new JScrollPane(getContentPane());
		contentScroller.getVerticalScrollBar().setUnitIncrement(20);
		add(contentScroller, c);
		setBorder(BorderFactory.createRaisedBevelBorder());
		label.addPropertyChangeListener("termComponent", labelListener);
	}

	public JComponent getContentPane() throws DatabaseException {
		JPanel content = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		if (conceptButton.isSelected()) {
			content.add(getConceptPanel(), c);
			c.gridy++;
		}
		if (descButton.isSelected()) {
			content.add(getDescPanel(), c);
			c.gridy++;
		}
		if (lineageButton.isSelected()) {
			content.add(getLineagePanel(), c);
			c.gridy++;
		}
		for (I_PluginToConceptPanel plugin: plugins) {
			if (plugin.showComponent()) {
				content.add(plugin.getComponent(this), c);
				c.gridy++;
			}
		}
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		content.add(new JPanel(), c);
		return content;
	}

	private Component getLineagePanel() throws DatabaseException {
		JPanel lineagePanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 2;
		JLabel lineageLabel = new JLabel("Lineage:");
		lineageLabel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
		lineagePanel.add(lineageLabel, c);

		SmallProgressPanel lineageProgress = new SmallProgressPanel();
		lineageProgress.setVisible(false);
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.SOUTHEAST;
		c.gridx++;
		lineagePanel.add(lineageProgress, c);

		lineageTree = new JTreeWithDragImage(this.ace.getAceFrameConfig());
		lineageTree.putClientProperty("JTree.lineStyle", "Angled");
		//lineageTree.putClientProperty("JTree.lineStyle", "None");
		lineageTree.setLargeModel(true);
		lineageTree.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		lineageTree.setTransferHandler(new TerminologyTransferHandler());
		lineageTree.setDragEnabled(true);
		lineageRenderer = new LineageTreeCellRenderer(this.ace
				.getAceFrameConfig());
		lineageTree.setCellRenderer(lineageRenderer);
		lineageTree.setRootVisible(false);
		lineageTree.setShowsRootHandles(false);
		updateLineageModel();

		c.anchor = GridBagConstraints.EAST;
		c.gridx = 0;
		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridwidth = 1;
		JPanel filler = new JPanel();
		filler.setMaximumSize(new Dimension(40, 20));
		filler.setMinimumSize(new Dimension(40, 20));
		filler.setPreferredSize(new Dimension(40, 20));
		lineagePanel.add(filler, c);
		c.gridx++;
		c.weightx = 1.0;
		c.weighty = 0.0;
		lineagePanel.add(lineageTree, c);
		lineageTree.setBorder(BorderFactory
				.createMatteBorder(1, 1, 0, 0, Color.GRAY));
		 
		JPanel filler2 = new JPanel();
		filler2.setMaximumSize(new Dimension(40, 20));
		filler2.setMinimumSize(new Dimension(40, 20));
		filler2.setPreferredSize(new Dimension(40, 20));
		filler2.setBackground(Color.white);
		filler2.setOpaque(true);
		c.weightx = 0.0;
		c.gridx++;
		filler2.setBorder(BorderFactory
				.createMatteBorder(1, 0, 0, 0, Color.GRAY));
		lineagePanel.add(filler2, c);

		lineagePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(1, 1, 1, 3), BorderFactory
				.createLineBorder(Color.GRAY)));
		return lineagePanel;
	}

	private void updateLineageModel() throws DatabaseException {
		DefaultTreeModel model = (DefaultTreeModel) lineageTree.getModel();
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("ROOT");
		model.setRoot(root);

		ConceptBean bean = (ConceptBean) getTermComponent();
		if (bean != null) {
			lineageRenderer.setFocusBean(bean);
			List<List<ConceptBean>> lineage = getLineage(bean, 0);
			if (AceLog.isLoggable(Level.FINE)) {
				StringBuffer buf = new StringBuffer();
				buf.append("Lineage for: " + bean);
				for (List<ConceptBean> parentLine : lineage) {
					buf.append("\n");
					buf.append(parentLine);
				}
				AceLog.fine(buf.toString());
			}
			addLineageToNode(lineage, root);
			model.nodeStructureChanged(root);
			for (int i = 0; i < 100; i++) {
				lineageTree.expandRow(i);
			}
		} else {
			root.add(new DefaultMutableTreeNode(" "));
			model.nodeStructureChanged(root);
		}
	}

	private void addLineageToNode(List<List<ConceptBean>> lineage,
			DefaultMutableTreeNode root) {
		Map<ConceptBean, DefaultMutableTreeNode> childrenNodes = new HashMap<ConceptBean, DefaultMutableTreeNode>();
		Map<ConceptBean, List<List<ConceptBean>>> childrenLineage = new HashMap<ConceptBean, List<List<ConceptBean>>>();
		for (List<ConceptBean> parentLine : lineage) {
			childrenNodes.put(parentLine.get(0), new DefaultMutableTreeNode(parentLine.get(0)));
			if (childrenLineage.get(parentLine.get(0)) == null) {
				childrenLineage.put(parentLine.get(0), new ArrayList<List<ConceptBean>>());
			}
			if (parentLine.size() > 1) {
				List<ConceptBean> shortenedLineage = new ArrayList<ConceptBean>(parentLine);
				shortenedLineage.remove(0);
				childrenLineage.get(parentLine.get(0)).add(shortenedLineage);
			}
		}
		for (ConceptBean childBean: childrenNodes.keySet()) {
			DefaultMutableTreeNode childNode = childrenNodes.get(childBean);
			root.add(childNode);
			if (childrenLineage.get(childBean).size() > 0) {
				addLineageToNode(childrenLineage.get(childBean),
						childNode);
			}
		}
		
		
	}

	private List<List<ConceptBean>> getLineage(ConceptBean bean, int depth)
			throws DatabaseException {
		List<List<ConceptBean>> lineage = new ArrayList<List<ConceptBean>>();

		List<ThinRelTuple> sourceRelTuples = bean.getSourceRelTuples(
				ace.getAceFrameConfig().getAllowedStatus(),
				ace.getAceFrameConfig().getDestRelTypes(),
				ace.getAceFrameConfig().getViewPositionSet(), false);
		if ((sourceRelTuples.size() > 0) && (depth < 40)) {
			for (ThinRelTuple rel : sourceRelTuples) {
				ConceptBean parent = ConceptBean.get(rel.getC2Id());
				List<List<ConceptBean>> parentLineage = getLineage(parent,
						depth + 1);
				for (List<ConceptBean> parentLine : parentLineage) {
					parentLine.add(bean);
					lineage.add(parentLine);
				}
			}
		} else {
			lineage.add(new ArrayList<ConceptBean>(Arrays
					.asList(new ConceptBean[] { bean })));
		}
		return lineage;
	}

	public JComponent getToggleBar() {
		JPanel toggleBar = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
	
		conceptButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/24x24/plain/bullet_triangle_blue.png")));
		conceptButton.setSelected(true);
		conceptButton
				.addActionListener(new ToggleConceptChangeActionListener());
		toggleBar.add(conceptButton, c);
		c.gridx++;
		descButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/24x24/plain/paragraph.png")));
		descButton.setSelected(true);
		descButton.addActionListener(new ToggleDescChangeActionListener());
		toggleBar.add(descButton, c);
		c.gridx++;
	
		lineageButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/24x24/plain/nav_up_right_green.png")));
		lineageButton.setSelected(true);
		lineageButton
				.addActionListener(new ToggleLineageChangeActionListener());
		toggleBar.add(lineageButton, c);
		c.gridx++;
	
		ShowPluginComponentActionListener l = new ShowPluginComponentActionListener();
		for (I_PluginToConceptPanel plugin: plugins) {
			for (JComponent component: plugin.getToggleBarComponents()) {
				toggleBar.add(component, c);
				c.gridx++;
			}
			plugin.addShowComponentListener(l);
		}
		inferredButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/24x24/plain/yinyang.png")));
		inferredButton.setSelected(false);
		inferredButton
				.setToolTipText("<html>Yin and yang can also be seen as a process of <br>"
						+ "transformation which describes the changes between<br>"
						+ " the phases of a cycle. For example, cold water (yin) <br>"
						+ "can be boiled and eventually turn into steam (yang).<p> <p>"
						+ "Stated forms (yin) can be classified and turned into<br>"
						+ "inferred forms (yang).");
		toggleBar.add(inferredButton, c);
		c.gridx++;
		usePrefButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/24x24/plain/component_preferences.png")));
		usePrefButton.setSelected(false);
		historyChangeActionListener = new ToggleHistoryChangeActionListener();
		usePrefButton.addActionListener(historyChangeActionListener);
		toggleBar.add(usePrefButton, c);
		c.gridx++;
		historyButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/24x24/plain/history.png")));
		historyButton.setSelected(false);
		historyButton.addActionListener(historyChangeActionListener);
		toggleBar.add(historyButton, c);
		c.gridy++;
		c.gridx++;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		toggleBar.add(new JPanel(), c);
		return toggleBar;
	}

	public I_AmTermComponent getTermComponent() {
		return label.getTermComponent();
	}

	public void setTermComponent(final I_AmTermComponent termComponent) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				label.setTermComponent(termComponent);
				contentScroller.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
			}
		});

	}

	private void updateTab(final I_AmTermComponent termComponent) {
		int titleLength = 15;
		if (conceptTabs != null) {
			int index = conceptTabs.indexOfComponent(this);
			if (termComponent != null) {
				ConceptBean cb = (ConceptBean) termComponent;
				String desc;
				try {
					ThinDescTuple tdt = cb.getDescTuple(getConfig()
							.getShortLabelDescPreferenceList(), getConfig());
					if (tdt != null) {
						desc = tdt.getText();
					} else {
						desc = "null tdt: " + cb.getInitialText();
					}
				} catch (DatabaseException e) {
					e.printStackTrace();
					desc = termComponent.toString();
				}
				String shortDesc;
				if (desc.length() > titleLength) {
					shortDesc = desc.substring(0, titleLength);
					shortDesc = shortDesc + "...";
				} else {
					shortDesc = desc;
				}
				conceptTabs.setTitleAt(index, shortDesc);
				conceptTabs.setToolTipTextAt(index, desc);
			}
			conceptTabs.setIconAt(index, tabIcon);
		}
	}

	private JPanel getConceptPanel() {
		JPanel conceptPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 2;
		JLabel conceptLabel = new JLabel("Concept:");
		conceptLabel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
		conceptPanel.add(conceptLabel, c);

		SmallProgressPanel concProgress = new SmallProgressPanel();
		concProgress.setVisible(false);
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.SOUTHEAST;
		c.gridx++;
		conceptPanel.add(concProgress, c);
		conceptTableModel.setProgress(concProgress);

		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy++;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridheight = 2;
		JButton rowAddAfter = new JButton(new ImageIcon(ACE.class
				.getResource("/24x24/plain/row_add_after.png")));
		conceptPanel.add(rowAddAfter, c);
		rowAddAfter.addActionListener(new AddConceptPart(this, getConfig()));
		c.gridheight = 1;
		c.gridx++;
		TableSorter sortingTable = new TableSorter(conceptTableModel);
		JTable conceptTable = new JTableWithDragImage(sortingTable);
		conceptTable.addMouseListener(conceptTableModel.makePopupListener(
				conceptTable, getConfig()));

		conceptTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sortingTable.setTableHeader(conceptTable.getTableHeader());

		CONCEPT_FIELD[] columnEnums = conceptTableModel.getColumnEnums();
		for (int i = 0; i < conceptTable.getColumnCount(); i++) {
			TableColumn column = conceptTable.getColumnModel().getColumn(i);
			CONCEPT_FIELD columnDesc = columnEnums[i];
			column.setIdentifier(columnDesc);
			column.setPreferredWidth(columnDesc.getPref());
			column.setMaxWidth(columnDesc.getMax());
			column.setMinWidth(columnDesc.getMin());
		}

		// Set up tool tips for column headers.
		sortingTable
				.getTableHeader()
				.setToolTipText(
						"Click to specify sorting; Control-Click to specify secondary sorting");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		conceptPanel.add(conceptTable.getTableHeader(), c);
		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridheight = 6;

		ConceptTableRenderer renderer = new ConceptTableRenderer();
		conceptTable.setDefaultRenderer(StringWithConceptTuple.class, renderer);
		JComboBox comboBox = new JComboBox() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void setSelectedItem(Object anObject) {
				Boolean value = null;
				if (Boolean.class.isAssignableFrom(anObject.getClass())) {
					value = (Boolean) anObject;
				} else if (StringWithConceptTuple.class
						.isAssignableFrom(anObject.getClass())) {
					I_CellTextWithTuple swt = (I_CellTextWithTuple) anObject;
					value = Boolean.parseBoolean(swt.getCellText());
				}
				super.setSelectedItem(value);
			}
		};
		comboBox.addItem(new Boolean(true));
		comboBox.addItem(new Boolean(false));
		conceptTable.setDefaultEditor(Boolean.class, new DefaultCellEditor(
				comboBox));

		conceptTable.getColumn(CONCEPT_FIELD.STATUS).setCellEditor(
				new ConceptTableModel.ConceptStatusFieldEditor(ace
						.getAceFrameConfig()));

		conceptTable.setDefaultRenderer(String.class, renderer);
		conceptPanel.add(conceptTable, c);
		conceptPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(1, 1, 1, 3), BorderFactory
				.createLineBorder(Color.GRAY)));
		c.gridheight = 1;
		c.gridx = 0;
		return conceptPanel;
	}

	private JPanel getDescPanel() {
		JPanel descPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 2;
		JLabel descLabel = new JLabel("Descriptions:");
		descLabel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
		descPanel.add(descLabel, c);

		SmallProgressPanel descProgress = new SmallProgressPanel();
		descProgress.setVisible(false);
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.SOUTHEAST;
		c.gridx++;
		descPanel.add(descProgress, c);
		descTableModel.setProgress(descProgress);

		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy++;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridheight = 2;
		JButton rowAddAfter = new JButton(new ImageIcon(ACE.class
				.getResource("/24x24/plain/row_add_after.png")));
		descPanel.add(rowAddAfter, c);
		rowAddAfter.addActionListener(new AddDescription(this, getConfig()));

		c.gridheight = 1;
		c.gridx++;
		TableSorter sortingTable = new TableSorter(descTableModel);
		JTable descTable = new JTableWithDragImage(sortingTable);
		descTable.setDragEnabled(true);
		descTable.setTransferHandler(new TerminologyTransferHandler());
		descTable.addMouseListener(descTableModel.makePopupListener(descTable,
				getConfig()));
		descTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sortingTable.setTableHeader(descTable.getTableHeader());

		DESC_FIELD[] columnEnums = descTableModel.getColumnEnums();

		for (int i = 0; i < descTable.getColumnCount(); i++) {
			TableColumn column = descTable.getColumnModel().getColumn(i);
			DESC_FIELD columnDesc = columnEnums[i];
			column.setIdentifier(columnDesc);
			column.setPreferredWidth(columnDesc.getPref());
			column.setMaxWidth(columnDesc.getMax());
			column.setMinWidth(columnDesc.getMin());
		}

		// Set up tool tips for column headers.
		sortingTable
				.getTableHeader()
				.setToolTipText(
						"Click to specify sorting; Control-Click to specify secondary sorting");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		descPanel.add(descTable.getTableHeader(), c);
		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridheight = 6;

		DescriptionTableRenderer renderer = new DescriptionTableRenderer();
		descTable.setDefaultRenderer(Boolean.class, renderer);
		JComboBox comboBox = new JComboBox() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void setSelectedItem(Object anObject) {
				Boolean value = null;
				if (Boolean.class.isAssignableFrom(anObject.getClass())) {
					value = (Boolean) anObject;
				} else if (StringWithDescTuple.class.isAssignableFrom(anObject
						.getClass())) {
					StringWithDescTuple swt = (StringWithDescTuple) anObject;
					value = Boolean.parseBoolean(swt.getCellText());
				}
				super.setSelectedItem(value);
			}
		};
		comboBox.addItem(new Boolean(true));
		comboBox.addItem(new Boolean(false));
		descTable.setDefaultEditor(Boolean.class, new DefaultCellEditor(
				comboBox));

		descTable.setDefaultEditor(StringWithDescTuple.class,
				new DescriptionTableModel.DescTextFieldEditor());
		descTable.setDefaultRenderer(StringWithDescTuple.class, renderer);
		descTable.getColumn(DESC_FIELD.TYPE).setCellEditor(
				new DescriptionTableModel.DescTypeFieldEditor(ace
						.getAceFrameConfig()));
		descTable.getColumn(DESC_FIELD.STATUS).setCellEditor(
				new DescriptionTableModel.DescStatusFieldEditor(ace
						.getAceFrameConfig()));

		descTable.setDefaultRenderer(Number.class, renderer);
		descTable.setDefaultRenderer(String.class, renderer);
		descPanel.add(descTable, c);
		descPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(1, 1, 1, 3), BorderFactory
				.createLineBorder(Color.GRAY)));

		c.gridheight = 1;
		c.gridx = 0;
		return descPanel;
	}


	public void addTermChangeListener(PropertyChangeListener l) {
		addPropertyChangeListener(I_ContainTermComponent.TERM_COMPONENT, l);
	}

	public void removeTermChangeListener(PropertyChangeListener l) {
		removePropertyChangeListener(I_ContainTermComponent.TERM_COMPONENT, l);
	}

	private CONCEPT_FIELD[] getConceptColumns() {
		List<CONCEPT_FIELD> fields = new ArrayList<CONCEPT_FIELD>();
		fields.add(CONCEPT_FIELD.DEFINED);
		fields.add(CONCEPT_FIELD.STATUS);
		if (historyButton.isSelected()) {
			fields.add(CONCEPT_FIELD.VERSION);
			fields.add(CONCEPT_FIELD.BRANCH);
		}
		return fields.toArray(new CONCEPT_FIELD[fields.size()]);
	}

	private DESC_FIELD[] getDescColumns() {
		List<DESC_FIELD> fields = new ArrayList<DESC_FIELD>();
		fields.add(DESC_FIELD.TEXT);
		fields.add(DESC_FIELD.TYPE);
		fields.add(DESC_FIELD.CASE_FIXED);
		fields.add(DESC_FIELD.LANG);
		fields.add(DESC_FIELD.STATUS);
		if (historyButton.isSelected()) {
			fields.add(DESC_FIELD.VERSION);
			fields.add(DESC_FIELD.BRANCH);
		}
		return fields.toArray(new DESC_FIELD[fields.size()]);
	}

	public boolean getUsePrefs() {
		return usePrefButton.isSelected();
	}

	public boolean showHistory() {
		return historyButton.isSelected();
	}

	public AceFrameConfig getConfig() {
		return ace.getAceFrameConfig();
	}

	TermComponentSelectionListener treeListener;

	private void changeLinkListener(LINK_TYPE type) {
		if (treeListener != null) {
			ace.removeTreeSelectionListener(treeListener);
			treeListener = null;
			ace.removeSearchLinkedComponent(this);
		}
		switch (type) {
		case TREE_LINK:
			treeListener = new TermComponentSelectionListener(this);
			ace.addTreeSelectionListener(treeListener);
			break;
		case SEARCH_LINK:
			ace.addSearchLinkedComponent(this);
			break;
		case UNLINKED:
			break;
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("viewPosition")) {
			historyChangeActionListener.actionPerformed(null);
		}
	}

	public boolean getShowHistory() {
		return historyButton.isSelected();
	}
}
