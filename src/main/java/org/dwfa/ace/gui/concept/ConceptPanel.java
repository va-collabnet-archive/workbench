package org.dwfa.ace.gui.concept;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.AbstractSpinnerModel;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dwfa.ace.ACE;
import org.dwfa.ace.TermComponentDataCheckSelectionListener;
import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.TermComponentListSelectionListener;
import org.dwfa.ace.TermComponentTreeSelectionListener;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.ExecutionRecord;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.Position;

import com.sleepycat.je.DatabaseException;

public class ConceptPanel extends JPanel implements I_HostConceptPlugins,
		PropertyChangeListener, Scrollable {

	private class LabelListener implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent evt) {
			updateTab(label.getTermComponent());
			if (label.getTermComponent() != null) {
				ace.getAceFrameConfig().setLastViewed(
						(I_GetConceptData) label.getTermComponent());
			}
			firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt
					.getNewValue());
		}

	}

	private class ShowPluginComponentActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						contentScroller.setViewportView(getContentPane());
					} catch (DatabaseException e) {
						AceLog.getAppLog().alertAndLog(
								ConceptPanel.this,
								Level.SEVERE,
								"Database Exception: "
										+ e.getLocalizedMessage(), e);
					}
				}
			});
		}

	}

	private class UncommittedChangeListener implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent arg0) {
			setTermComponent(getTermComponent());
		}

	}

	private class FixedToggleChangeActionListener implements ActionListener,
			PropertyChangeListener {

		public void actionPerformed(ActionEvent e) {
			perform();
		}

		private void perform() {
			firePropertyChange(I_HostConceptPlugins.SHOW_HISTORY,
					!historyButton.isSelected(), historyButton.isSelected());
			try {
				contentScroller.setViewportView(getContentPane());
			} catch (DatabaseException e1) {
				AceLog.getAppLog().alertAndLog(ConceptPanel.this, Level.SEVERE,
						"Database Exception: " + e1.getLocalizedMessage(), e1);
			}
		}

		public void propertyChange(PropertyChangeEvent arg0) {
			perform();
		}
	}

	private class UpdateTogglesPropertyChangeListener implements
			PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent arg0) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					updateToggles();
				}

			});
		}
	}

	private TermComponentLabel label;

	private JToggleButton historyButton;

	private JScrollPane contentScroller;

	private JToggleButton usePrefButton;

	private ACE ace;

	public static ImageIcon UNLINKED_ICON = new ImageIcon(ACE.class
			.getResource("/24x24/plain/carabiner.png"));

	public static ImageIcon SEARCH_LINK_ICON = new ImageIcon(ACE.class
			.getResource("/24x24/plain/carabiner_find.png"));

	public static ImageIcon TREE_LINK_ICON = new ImageIcon(ACE.class
			.getResource("/24x24/plain/carabiner_tree.png"));

	public static ImageIcon LIST_LINK_ICON = new ImageIcon(ACE.class
			.getResource("/24x24/plain/carabiner_up_arrow.png"));

	public static ImageIcon DATA_CHECK_LINK_ICON = new ImageIcon(ACE.class
			.getResource("/24x24/plain/carabiner_alert.png"));

	public static ImageIcon SMALL_SEARCH_LINK_ICON = new ImageIcon(ACE.class
			.getResource("/16x16/plain/find.png"));

	public static ImageIcon SMALL_TREE_LINK_ICON = new ImageIcon(ACE.class
			.getResource("/16x16/plain/text_tree.png"));

	public static ImageIcon SMALL_LIST_LINK_ICON = new ImageIcon(ACE.class
			.getResource("/16x16/plain/arrow_up_green.png"));

	public static ImageIcon SMALL_ALERT_LINK_ICON = new ImageIcon(ACE.class
			.getResource("/16x16/plain/warning.png"));

	private ConflictPlugin conflictPlugin = new ConflictPlugin();

	private AbstractPlugin srcRelPlugin = new SrcRelPlugin();

	private RelPlugin destRelPlugin = new DestRelPlugin();

	private IdPlugin idPlugin = new IdPlugin();

	private ImagePlugin imagePlugin = new ImagePlugin();

	private DescriptionPlugin descPlugin = new DescriptionPlugin();

	private LineagePlugin lineagePlugin = new LineagePlugin();

	ConceptAttributePlugin conceptAttributePlugin = new ConceptAttributePlugin();

	private List<I_PluginToConceptPanel> plugins;

	private Map<TOGGLES, I_PluginToConceptPanel> pluginMap = new HashMap<TOGGLES, I_PluginToConceptPanel>();

	public ImageIcon tabIcon;

	private JTabbedPane conceptTabs;

	private JToggleButton inferredButton;

	private FixedToggleChangeActionListener fixedToggleChangeActionListener;

	private PropertyChangeListener labelListener = new LabelListener();

	private JToggleButton refsetToggleButton;

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
					setToolTipText("This panel is linked to the hierarchy selection");
					tabIcon = SMALL_TREE_LINK_ICON;
				} else if (value == UNLINKED_ICON) {
					setToolTipText("This panel is not linked to other selections");
					tabIcon = null;
				} else if (value == LIST_LINK_ICON) {
					setToolTipText("This panel is linked to the list selection above");
					tabIcon = SMALL_LIST_LINK_ICON;
				} else if (value == DATA_CHECK_LINK_ICON) {
					setToolTipText("This panel is linked to the data check selection");
					tabIcon = SMALL_ALERT_LINK_ICON;
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

	public ConceptPanel(ACE ace, LINK_TYPE link) throws DatabaseException,
			IOException, ClassNotFoundException {
		this(ace, link, null);
	}

	public ConceptPanel(ACE ace, LINK_TYPE link, boolean enableListLink)
			throws DatabaseException, IOException, ClassNotFoundException {
		this(ace, link, null, enableListLink);
	}

	public ConceptPanel(ACE ace, LINK_TYPE link, JTabbedPane conceptTabs)
			throws DatabaseException, IOException, ClassNotFoundException {
		this(ace, link, conceptTabs, false);
	}

	public ConceptPanel(ACE ace, LINK_TYPE link, JTabbedPane conceptTabs,
			boolean enableListLink) throws DatabaseException, IOException,
			ClassNotFoundException {
		super(new GridBagLayout());
		this.ace = ace;
		UpdateTogglesPropertyChangeListener updateListener = new UpdateTogglesPropertyChangeListener();
		this.ace.getAceFrameConfig().addPropertyChangeListener(
				"visibleComponentToggles", updateListener);
		if (ACE.editMode) {
			plugins = new ArrayList<I_PluginToConceptPanel>(Arrays
					.asList(new I_PluginToConceptPanel[] { idPlugin,
							conceptAttributePlugin, descPlugin, srcRelPlugin,
							destRelPlugin, lineagePlugin, imagePlugin,
							conflictPlugin }));
		} else {
			plugins = new ArrayList<I_PluginToConceptPanel>(Arrays
					.asList(new I_PluginToConceptPanel[] { idPlugin,
							conceptAttributePlugin, descPlugin, srcRelPlugin,
							destRelPlugin, lineagePlugin, }));
		}
		ace.getAceFrameConfig().addPropertyChangeListener("uncommitted",
				new UncommittedChangeListener());
		label = new TermComponentLabel(this.ace.getAceFrameConfig());
		fixedToggleChangeActionListener = new FixedToggleChangeActionListener();
		this.ace.getAceFrameConfig().addPropertyChangeListener(
				"visibleRefsets", fixedToggleChangeActionListener);
		this.ace.getAceFrameConfig().addPropertyChangeListener(this);
		this.conceptTabs = conceptTabs;
		GridBagConstraints c = new GridBagConstraints();

		LinkListModel linkSpinnerModel = new LinkListModel(new ImageIcon[] {
				UNLINKED_ICON, SEARCH_LINK_ICON, TREE_LINK_ICON,
				DATA_CHECK_LINK_ICON }, link.ordinal());
		if (enableListLink) {
			linkSpinnerModel = new LinkListModel(new ImageIcon[] {
					UNLINKED_ICON, SEARCH_LINK_ICON, TREE_LINK_ICON,
					DATA_CHECK_LINK_ICON, LIST_LINK_ICON }, link.ordinal());
		}
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

		for (I_PluginToConceptPanel plugin : plugins) {
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

	public JComponent getToggleBar() throws IOException, ClassNotFoundException {
		JPanel toggleBar = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;

		JPanel leftTogglePane = new JPanel(new FlowLayout());
		toggleBar.add(leftTogglePane, c);

		JPanel rightTogglePane = new JPanel(new FlowLayout());

		ShowPluginComponentActionListener l = new ShowPluginComponentActionListener();
		for (I_PluginToConceptPanel plugin : plugins) {
			for (JComponent component : plugin.getToggleBarComponents()) {

				leftTogglePane.add(component);
			}
			plugin.addShowComponentListener(l);
		}
		fixedToggleChangeActionListener = new FixedToggleChangeActionListener();

		inferredButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/24x24/plain/yinyang.png")));
		inferredButton.setSelected(false);
		inferredButton.setVisible(false);
		inferredButton
				.setToolTipText("<html>Yin and yang can also be seen as a process of <br>"
						+ "transformation which describes the changes between<br>"
						+ " the phases of a cycle. For example, cold water (yin) <br>"
						+ "can be boiled and eventually turn into steam (yang).<p> <p>"
						+ "Stated forms (yin) can be classified and turned into<br>"
						+ "inferred forms (yang).");
		leftTogglePane.add(inferredButton);

		refsetToggleButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/24x24/plain/paperclip.png")));
		refsetToggleButton.setSelected(false);
		refsetToggleButton.setVisible(ACE.editMode);
		refsetToggleButton
				.setToolTipText("show hide refset entries of types selected in the preferences");
		refsetToggleButton.addActionListener(fixedToggleChangeActionListener);
		leftTogglePane.add(refsetToggleButton);

		usePrefButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/24x24/plain/component_preferences.png")));
		usePrefButton.setSelected(false);
		usePrefButton.setVisible(ACE.editMode);
		usePrefButton.setToolTipText("use preferences to filter views");
		usePrefButton.addActionListener(fixedToggleChangeActionListener);
		leftTogglePane.add(usePrefButton);

		historyButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/24x24/plain/history.png")));
		historyButton.setSelected(false);
		historyButton.addActionListener(fixedToggleChangeActionListener);
		historyButton.setToolTipText("show/hide the history records");
		leftTogglePane.add(historyButton);
		
		idPlugin.getToggleButton().addActionListener(fixedToggleChangeActionListener);

		c.gridx++;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		toggleBar.add(new JPanel(), c);

		File componentPluginDir = new File("plugins" + File.separator
				+ "component");
		File[] plugins = componentPluginDir.listFiles(new FilenameFilter() {
			public boolean accept(File arg0, String fileName) {
				return fileName.toLowerCase().endsWith(".bp");
			}

		});
		if (plugins != null) {
			c.weightx = 0.0;
			c.weighty = 0.0;
			c.fill = GridBagConstraints.NONE;
			c.gridx++;
			toggleBar.add(rightTogglePane, c);
			boolean exceptions = false;
			StringBuffer exceptionMessage = new StringBuffer();
			exceptionMessage
					.append("<html>Exception(s) reading the following plugin(s): <p><p>");
			for (File f : plugins) {
				AceLog.getAppLog().info(
						"Reading plugin: " + f.getAbsolutePath());
				try {
					FileInputStream fis = new FileInputStream(f);
					BufferedInputStream bis = new BufferedInputStream(fis);
					ObjectInputStream ois = new ObjectInputStream(bis);
					BusinessProcess bp = (BusinessProcess) ois.readObject();
					ois.close();
					byte[] iconBytes = (byte[]) bp
							.readAttachement("button_icon");
					if (iconBytes != null) {
						ImageIcon icon = new ImageIcon(iconBytes);
						JButton pluginButton = new JButton(icon);
						pluginButton.setToolTipText(bp.getSubject());
						pluginButton.addActionListener(new PluginListener(f));
						rightTogglePane.add(pluginButton, c);
						AceLog.getAppLog().info(
								"adding component plugin: " + f.getName());
					} else {
						JButton pluginButton = new JButton(bp.getName());
						pluginButton.setToolTipText(bp.getSubject());
						pluginButton.addActionListener(new PluginListener(f));
						rightTogglePane.add(pluginButton, c);
						AceLog.getAppLog().info(
								"adding component plugin: " + f.getName());
					}
				} catch (Throwable e) {
					exceptions = true;
					exceptionMessage.append("Exception reading: "
							+ f.getAbsolutePath() + "<p>");
					AceLog.getAppLog().log(Level.SEVERE,
							"Exception reading: " + f.getAbsolutePath(), e);
				}
			}

			if (exceptions) {
				exceptionMessage
						.append("<p>Please see the log file for more details.");
				JOptionPane
						.showMessageDialog(this, exceptionMessage.toString());
			}
		}
		pluginMap.put(TOGGLES.ID, idPlugin);
		pluginMap.put(TOGGLES.ATTRIBUTES, conceptAttributePlugin);
		pluginMap.put(TOGGLES.DESCRIPTIONS, descPlugin);
		pluginMap.put(TOGGLES.SOURCE_RELS, srcRelPlugin);
		pluginMap.put(TOGGLES.DEST_RELS, destRelPlugin);
		pluginMap.put(TOGGLES.LINEAGE, lineagePlugin);
		pluginMap.put(TOGGLES.IMAGE, imagePlugin);
		pluginMap.put(TOGGLES.CONFLICT, conflictPlugin);

		updateToggles();

		return toggleBar;
	}

	private void updateToggles() {
		for (TOGGLES t : TOGGLES.values()) {
			boolean visible = ((AceFrameConfig) ace.getAceFrameConfig())
					.isToggleVisible(t);
			if (pluginMap.get(t) != null) {
				I_PluginToConceptPanel plugin = pluginMap.get(t);
				for (JComponent toggleComponent : plugin
						.getToggleBarComponents()) {
					toggleComponent.setVisible(visible);
					toggleComponent.setEnabled(visible);
				}
			} else {
				switch (t) {
				case HISTORY:
					historyButton.setVisible(visible);
					historyButton.setEnabled(visible);
					break;
				case PREFERENCES:
					if (ACE.editMode) {
						usePrefButton.setVisible(visible);
						usePrefButton.setEnabled(visible);
					}
					break;
				case REFSETS:
					refsetToggleButton.setVisible(visible);
					refsetToggleButton.setEnabled(visible);
					break;
				case STATED_INFERRED:
					if (ACE.editMode) {
						inferredButton.setVisible(visible);
						inferredButton.setEnabled(visible);
					}
					break;

				default:
					break;
				}
			}
		}
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
				getConfig().setStatusMessage("Executing: " + bp.getName());
				final MasterWorker worker = getConfig().getWorker();
				// Set concept bean
				// Set config

				worker.writeAttachment(WorkerAttachmentKeys.ACE_FRAME_CONFIG
						.name(), getConfig());
				bp.writeAttachment(ProcessAttachmentKeys.I_GET_CONCEPT_DATA
						.name(), label.getTermComponent());
				worker.writeAttachment(
						WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name(),
						ConceptPanel.this);
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
								getConfig().setStatusMessage(
										"<html><font color='#006400'>execute");
								if (exceptionMessage.equals("")) {
									getConfig().setStatusMessage(
											"<html>Execution of <font color='blue'>"
													+ bp.getName()
													+ "</font> complete.");
								} else {
									getConfig().setStatusMessage(
											"<html><font color='blue'>Process complete: <font color='red'>"
													+ exceptionMessage);
								}
							}
						});
					}

				};
				new Thread(r).start();
			} catch (Exception e1) {
				getConfig().setStatusMessage("Exception during execution.");
				AceLog.getAppLog().alertAndLogException(e1);
			}
		}

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
			if (index >= 0) {
				if (termComponent != null) {
					ConceptBean cb = (ConceptBean) termComponent;
					String desc;
					try {
						I_DescriptionTuple tdt = cb.getDescTuple(getConfig()
								.getShortLabelDescPreferenceList(), getConfig());
						if (tdt != null) {
							desc = tdt.getText();
						} else {
							desc = cb.getInitialText();
						}
					} catch (IOException e) {
						AceLog.getAppLog().alertAndLogException(e);
						setTermComponent(null);
						return;
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
				} else {
					conceptTabs.setTitleAt(index, "empty");
					conceptTabs.setToolTipTextAt(index, "empty");
				}
				conceptTabs.setIconAt(index, tabIcon);
			}
		}
	}

	public void addTermChangeListener(PropertyChangeListener l) {
		addPropertyChangeListener(I_ContainTermComponent.TERM_COMPONENT, l);
	}

	public void removeTermChangeListener(PropertyChangeListener l) {
		removePropertyChangeListener(I_ContainTermComponent.TERM_COMPONENT, l);
	}

	public boolean getUsePrefs() {
		return usePrefButton.isSelected();
	}

	public boolean showHistory() {
		return historyButton.isSelected();
	}

	public I_ConfigAceFrame getConfig() {
		return ace.getAceFrameConfig();
	}

	private TermComponentTreeSelectionListener treeListener;

	private TermComponentListSelectionListener listListener;

	private TermComponentDataCheckSelectionListener dataCheckListener;

	private JList linkedList;

	public void changeLinkListener(LINK_TYPE type) {
		if (treeListener != null) {
			ace.removeTreeSelectionListener(treeListener);
			treeListener = null;
		}
		if (listListener != null) {
			linkedList.removeListSelectionListener(listListener);
			listListener = null;
		}
		if (dataCheckListener != null) {
			ace.removeDataCheckListener(dataCheckListener);
			dataCheckListener = null;
		}
		ace.removeSearchLinkedComponent(this);
		switch (type) {
		case TREE_LINK:
			treeListener = new TermComponentTreeSelectionListener(this);
			ace.addTreeSelectionListener(treeListener);
			break;
		case SEARCH_LINK:
			ace.addSearchLinkedComponent(this);
			break;
		case UNLINKED:
			break;
		case LIST_LINK:
			if (linkedList != null) {
				listListener = new TermComponentListSelectionListener(this);
				linkedList.addListSelectionListener(listListener);
			}
			break;
		case DATA_CHECK_LINK:
			dataCheckListener = new TermComponentDataCheckSelectionListener(
					this);
			ace.addDataCheckListener(dataCheckListener);
			break;

		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("viewPositions")) {
			fixedToggleChangeActionListener.actionPerformed(null);
		} else if (evt.getPropertyName().equals("commit")) {
			if (label.getTermComponent() != null) {
				ConceptBean cb = (ConceptBean) label.getTermComponent();
				try {
					if (cb.getConceptAttributes() == null) {
						label.setTermComponent(null);
					}
				} catch (IOException e) {
					label.setTermComponent(null);
					AceLog.getAppLog().alertAndLogException(e);
				}
			}
			this.firePropertyChange("commit", null, null);
		}
	}

	public boolean getShowHistory() {
		return historyButton.isSelected();
	}

	public VIEW_TYPE getViewType() {
		if (inferredButton.isSelected()) {
			return VIEW_TYPE.INFERRED;
		}
		return VIEW_TYPE.STATED;
	}

	public I_GetConceptData getHierarchySelection() {
		return ace.getAceFrameConfig().getHierarchySelection();
	}

	public LogWithAlerts getEditLog() {
		return AceLog.getEditLog();
	}

	public JList getLinkedList() {
		return linkedList;
	}

	public void setLinkedList(JList linkedList) {
		this.linkedList = linkedList;
	}

	public void unlink() {
		changeLinkListener(LINK_TYPE.UNLINKED);
	}

	public I_GetConceptData getConcept(Collection<UUID> ids)
			throws TerminologyException, IOException {
		return ConceptBean.get(ids);
	}

	public I_GetConceptData getConcept(UUID[] ids) throws TerminologyException,
			IOException {
		return ConceptBean.get(Arrays.asList(ids));
	}

	public I_Position newPosition(I_Path path, int version) {
		return new Position(version, path);
	}

	public I_IntSet newIntSet() {
		return new IntSet();
	}

	public void addUncommitted(I_GetConceptData concept) {
		ACE.addUncommitted((I_Transact) concept);
	}

	public void setAllTogglesToState(final boolean state) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				for (I_PluginToConceptPanel plugin : plugins) {
					for (JComponent component : plugin.getToggleBarComponents()) {
						if (JToggleButton.class.isAssignableFrom(component
								.getClass())) {
							JToggleButton toggle = (JToggleButton) component;
							if (toggle.isSelected() == state) {
								// nothing to do...
							} else {
								toggle.doClick();
							}
						}
					}
				}
			}
		});
	}

	public void setLinkType(LINK_TYPE link) {
		changeLinkListener(link);
	}

	public void setToggleState(final TOGGLES toggle, final boolean state) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				I_PluginToConceptPanel plugin = pluginMap.get(toggle);
				if (plugin != null) {
					for (JComponent component : plugin.getToggleBarComponents()) {
						if (JToggleButton.class.isAssignableFrom(component
								.getClass())) {
							JToggleButton toggleButton = (JToggleButton) component;
							if (toggleButton.isSelected() == state) {
								// nothing to do...
							} else {
								toggleButton.doClick();
							}
						}
					}
				} else {
					switch (toggle) {
					case HISTORY:
						if (historyButton.isSelected() == state) {
							// nothing to do...
						} else {
							historyButton.doClick();
						}
						break;
					case PREFERENCES:
						if (usePrefButton.isSelected() == state) {
							// nothing to do...
						} else {
							usePrefButton.doClick();
						}
						break;
					case REFSETS:
						if (refsetToggleButton.isSelected() == state) {
							// nothing to do...
						} else {
							refsetToggleButton.doClick();
						}
						break;
					case STATED_INFERRED:
						if (inferredButton.isSelected() == state) {
							// nothing to do...
						} else {
							inferredButton.doClick();
						}
						break;

					default:
						throw new UnsupportedOperationException(
								" Can't handle toggle: " + toggle);
					}
				}
			}

		});

	}

	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) {
		return 75;
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) {
		return 10;
	}

	public boolean getShowRefsets() {
		return refsetToggleButton.isSelected();
	}

	public boolean getToggleState(TOGGLES toggle) {
		I_PluginToConceptPanel plugin = pluginMap.get(toggle);
		if (plugin != null) {
			for (JComponent component : plugin.getToggleBarComponents()) {
				if (JToggleButton.class.isAssignableFrom(component.getClass())) {
					JToggleButton toggleButton = (JToggleButton) component;
					return toggleButton.isSelected();
				}
			}
		} else {
			switch (toggle) {
			case HISTORY:
				return historyButton.isSelected();
			case PREFERENCES:
				return usePrefButton.isSelected();
			case REFSETS:
				return refsetToggleButton.isSelected();
			case STATED_INFERRED:
				return inferredButton.isSelected();
			}
		}
		throw new UnsupportedOperationException(" Can't handle toggle: "
				+ toggle);
	}
}
