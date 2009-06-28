package org.dwfa.ace.refset;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.dwfa.ace.ACE;
import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.gui.concept.ConceptPanel;
import org.dwfa.ace.gui.concept.I_PluginToConceptPanel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData;
import org.dwfa.ace.table.refset.ReflexiveRefsetMemberTableModel;
import org.dwfa.ace.table.refset.ReflexiveRefsetUtil;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.REFSET_FIELD_TYPE;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.tree.TermTreeHelper;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.ExecutionRecord;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.util.SwingWorker;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinExtBinder;
import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntSet;

public class RefsetSpecEditor implements I_HostConceptPlugins,
		PropertyChangeListener {
	
	PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}


	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(propertyName, listener);
	}


	public boolean equals(Object obj) {
		return pcs.equals(obj);
	}


	public void fireIndexedPropertyChange(String propertyName, int index,
			boolean oldValue, boolean newValue) {
		pcs.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
	}


	public void fireIndexedPropertyChange(String propertyName, int index,
			int oldValue, int newValue) {
		pcs.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
	}


	public void fireIndexedPropertyChange(String propertyName, int index,
			Object oldValue, Object newValue) {
		pcs.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
	}


	public void firePropertyChange(PropertyChangeEvent evt) {
		pcs.firePropertyChange(evt);
	}


	public void firePropertyChange(String propertyName, boolean oldValue,
			boolean newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}


	public void firePropertyChange(String propertyName, int oldValue,
			int newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}


	public void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}


	public PropertyChangeListener[] getPropertyChangeListeners() {
		return pcs.getPropertyChangeListeners();
	}


	public PropertyChangeListener[] getPropertyChangeListeners(
			String propertyName) {
		return pcs.getPropertyChangeListeners(propertyName);
	}


	public int hashCode() {
		return pcs.hashCode();
	}


	public boolean hasListeners(String propertyName) {
		return pcs.hasListeners(propertyName);
	}


	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}


	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(propertyName, listener);
	}


	public String toString() {
		return pcs.toString();
	}

	public class RefsetSpecSelectionListener implements TreeSelectionListener {

		public void valueChanged(TreeSelectionEvent tse) {
			if (tse.getPath() != null) {
				TreePath selectionPath = tse.getPath();
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
				I_ThinExtByRefVersioned ext = (I_ThinExtByRefVersioned) selectedNode.getUserObject();
				
		        try {
		        		
					EXT_TYPE extType = ThinExtBinder.getExtensionType(ext);
					
					EnumSet<EXT_TYPE> allowedTypes = EnumSet.of(EXT_TYPE.CONCEPT_CONCEPT, 
							EXT_TYPE.CONCEPT_CONCEPT_CONCEPT, EXT_TYPE.CONCEPT_CONCEPT_STRING);
					
					if (allowedTypes.contains(extType) == false) {
						throw new Exception("Can't handle " + extType);
					}
					
					
					
		        	List<ReflexiveRefsetFieldData> columns = new ArrayList<ReflexiveRefsetFieldData>();
		        			        	
		        	getDefaultSpecColumns(extType, columns);
		        	
		        	if (extType == EXT_TYPE.CONCEPT_CONCEPT_CONCEPT) {
			        	ReflexiveRefsetFieldData column3 = new ReflexiveRefsetFieldData();
			        	column3.setColumnName("constraint");
			        	column3.setCreationEditable(true);
			        	column3.setUpdateEditable(false);
			        	column3.setFieldClass(Number.class);
			        	column3.setMin(5);
			        	column3.setPref(175);
			        	column3.setMax(2000);
			        	column3.setInvokedOnPart(true);
			        	column3.setReadMethod(extType.getPartClass().getMethod("getC3id"));
			        	column3.setWriteMethod(extType.getPartClass().getMethod("setC3id", int.class));
			        	column3.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
			        	columns.add(column3);
		        		
		        	} else if (extType == EXT_TYPE.CONCEPT_CONCEPT_STRING) {
			        	ReflexiveRefsetFieldData column3 = new ReflexiveRefsetFieldData();
			        	column3.setColumnName("query string");
			        	column3.setCreationEditable(true);
			        	column3.setUpdateEditable(false);
			        	column3.setFieldClass(String.class);
			        	column3.setMin(5);
			        	column3.setPref(175);
			        	column3.setMax(2000);
			        	column3.setInvokedOnPart(true);
			        	column3.setReadMethod(extType.getPartClass().getMethod("getStr"));
			        	column3.setWriteMethod(extType.getPartClass().getMethod("setStr", String.class));
			        	column3.setType(REFSET_FIELD_TYPE.STRING);
			        	columns.add(column3);
		        	}
		        	
		        	if (historyButton.isSelected()) {
			        	ReflexiveRefsetFieldData column4 = new ReflexiveRefsetFieldData();
			        	column4.setColumnName("version");
			        	column4.setCreationEditable(false);
			        	column4.setUpdateEditable(false);
			        	column4.setFieldClass(Number.class);
			        	column4.setMin(5);
			        	column4.setPref(150);
			        	column4.setMax(150);
			        	column4.setInvokedOnPart(true);
			        	column4.setReadMethod(extType.getPartClass().getMethod("getVersion"));
			        	column4.setWriteMethod(extType.getPartClass().getMethod("setVersion", int.class));
			        	column4.setType(REFSET_FIELD_TYPE.VERSION);
			        	columns.add(column4);

			        	ReflexiveRefsetFieldData column5 = new ReflexiveRefsetFieldData();
			        	column5.setColumnName("path");
			        	column5.setCreationEditable(false);
			        	column5.setUpdateEditable(false);
			        	column5.setFieldClass(String.class);
			        	column5.setMin(5);
			        	column5.setPref(150);
			        	column5.setMax(150);
			        	column5.setInvokedOnPart(true);
			        	column5.setReadMethod(extType.getPartClass().getMethod("getPathId"));
			        	column5.setWriteMethod(extType.getPartClass().getMethod("setPathId", int.class));
			        	column5.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
			        	columns.add(column5);
		        	}
		        	
		        	
		        	ReflexiveRefsetMemberTableModel reflexiveModel = new ReflexiveRefsetMemberTableModel(RefsetSpecEditor.this,
		        			columns.toArray(new ReflexiveRefsetFieldData[columns.size()]));
		        	
		        	reflexiveModel.setComponentId(ext.getMemberId());
		        	reflexiveModel.getRowCount();
		        	clauseTable.setModel(reflexiveModel);
				} catch (Exception e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			} else {
		    	try {
			    	List<ReflexiveRefsetFieldData> columns = new ArrayList<ReflexiveRefsetFieldData>();
					getDefaultSpecColumns(EXT_TYPE.CONCEPT_CONCEPT, columns);
			    	ReflexiveRefsetMemberTableModel reflexiveModel = new ReflexiveRefsetMemberTableModel(RefsetSpecEditor.this,
			    			columns.toArray(new ReflexiveRefsetFieldData[columns.size()]));
		        	reflexiveModel.setComponentId(Integer.MIN_VALUE);
			       	reflexiveModel.getRowCount();
		        	clauseTable.setModel(reflexiveModel);
				} catch (Exception e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			}
		}

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
				updateSpecTree();
			} catch (Exception e1) {
				AceLog.getAppLog().alertAndLog(contentPanel,
						Level.SEVERE,
						"Database Exception: " + e1.getLocalizedMessage(), e1);
			}
		}

		public void propertyChange(PropertyChangeEvent arg0) {
			perform();
		}
	}

	private class LabelListener implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent evt) {
			if (label.getTermComponent() != null) {
				ace.getAceFrameConfig().setLastViewed(
						(I_GetConceptData) label.getTermComponent());
				if (tabHistoryList.size() == 0) {
					tabHistoryList.addFirst((I_GetConceptData) label
							.getTermComponent());
				} else if ((tabHistoryList.size() > 0)
						&& (label.getTermComponent().equals(
								tabHistoryList.getFirst()) == false)) {
					tabHistoryList.addFirst((I_GetConceptData) label
							.getTermComponent());
				}
				while (tabHistoryList.size() > 20) {
					tabHistoryList.removeLast();
				}
			}
			updateSpecTree();
			if (treeHelper.getRenderer() != null) {
				treeHelper.getRenderer().propertyChange(new PropertyChangeEvent(this, "showRefsetInfoInTaxonomy", null, null));
				treeHelper.getRenderer().propertyChange(new PropertyChangeEvent(this, "variableHeightTaxonomyView", null, null));
				treeHelper.getRenderer().propertyChange(new PropertyChangeEvent(this, "highlightConflictsInTaxonomyView", null, null));
				treeHelper.getRenderer().propertyChange(new PropertyChangeEvent(this, "showViewerImagesInTaxonomy", null, null));
				treeHelper.getRenderer().propertyChange(new PropertyChangeEvent(this, "refsetsToShow", null, null));
			} else {
				AceLog.getAppLog().info("treeHelper.getRenderer() == null");
			}
			firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt
					.getNewValue());
		}


	}

	private class ShowHistoryListener implements ActionListener {

		private class ShowHistoryAction extends AbstractAction {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			I_GetConceptData concept;

			public ShowHistoryAction(I_GetConceptData concept) {
				super(concept.toString());
				this.concept = concept;
			}

			public void actionPerformed(ActionEvent e) {
				RefsetSpecEditor.this.setTermComponent(concept);
			}

		}

		public void actionPerformed(ActionEvent e) {
			if (tabHistoryList.size() > 1) {
				JPopupMenu popup = new JPopupMenu();
				List<I_GetConceptData> historyToRemove = new ArrayList<I_GetConceptData>();
				for (I_GetConceptData historyItem : tabHistoryList) {
					try {
						if (LocalVersionedTerminology.get().getUids(
								historyItem.getConceptId()) != null) {
							JMenuItem menuItem = new JMenuItem(
									new ShowHistoryAction(historyItem));
							popup.add(menuItem);
						} else {
							historyToRemove.add(historyItem);
						}
					} catch (IOException e1) {
						historyToRemove.add(historyItem);
					} catch (TerminologyException e2) {
						historyToRemove.add(historyItem);
					}
				}
				tabHistoryList.removeAll(historyToRemove);
				Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
				SwingUtilities.convertPointFromScreen(mouseLocation,
						contentPanel);
				popup.show(contentPanel, mouseLocation.x,
						mouseLocation.y);
			}
		}
	}

	private ACE ace;

	private LinkedList<I_GetConceptData> tabHistoryList;

	private ArrayList<I_PluginToConceptPanel> plugins;

	private TermComponentLabel label;

	private PropertyChangeListener labelListener = new LabelListener();

	private FixedToggleChangeActionListener fixedToggleChangeActionListener;

	private JToggleButton historyButton;

	private static final String TAB_HISTORY_KEY = "refset 0";

	private Map<TOGGLES, I_PluginToConceptPanel> pluginMap = new HashMap<TOGGLES, I_PluginToConceptPanel>();

	private JButton componentHistoryButton;

	private JTree specTree;

	public I_GetConceptData refsetSpecConcept;

	private JTableWithDragImage clauseTable;
	
	private JPanel topPanel;


	private JComponent contentPanel;

	private TermTreeHelper treeHelper;

	public RefsetSpecEditor(ACE ace, TermTreeHelper treeHelper) throws Exception {
		super();
		this.ace = ace;
		this.treeHelper = treeHelper;
		topPanel = new JPanel(new GridBagLayout());

		this.tabHistoryList = (LinkedList<I_GetConceptData>) ace
				.getAceFrameConfig().getTabHistoryMap().get(TAB_HISTORY_KEY);

		if (this.tabHistoryList == null) {
			this.tabHistoryList = new LinkedList<I_GetConceptData>();
			ace.getAceFrameConfig().getTabHistoryMap().put(TAB_HISTORY_KEY,
					this.tabHistoryList);
		}

		plugins = new ArrayList<I_PluginToConceptPanel>(Arrays
					.asList(new I_PluginToConceptPanel[] {}));
		ace.getAceFrameConfig().addPropertyChangeListener("uncommitted",
				new UncommittedChangeListener());
		label = new TermComponentLabel(this.ace.getAceFrameConfig());
		fixedToggleChangeActionListener = new FixedToggleChangeActionListener();
		this.ace.getAceFrameConfig().addPropertyChangeListener(
				"visibleRefsets", fixedToggleChangeActionListener);
		this.ace.getAceFrameConfig().addPropertyChangeListener(this);
		GridBagConstraints c = new GridBagConstraints();

		JLabel linkSpinner = new JLabel(new ImageIcon(ACE.class
				.getResource("/24x24/plain/paperclip.png")));
		linkSpinner.setBorder(BorderFactory.createEmptyBorder(3, 3, 2, 5));

		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		topPanel.add(linkSpinner, c);
		c.gridx++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		topPanel.add(label, c);
		c.weightx = 0.0;
		c.gridx++;
		componentHistoryButton = new JButton(ConceptPanel.HISTORY_ICON);
		componentHistoryButton.addActionListener(new ShowHistoryListener());
		componentHistoryButton
				.setToolTipText("click to show history of the RefSet Specification displayed in this viewer");
		topPanel.add(componentHistoryButton, c);

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		topPanel.add(getToggleBar(), c);
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0;
		c.gridy++;
		topPanel.setBorder(BorderFactory.createRaisedBevelBorder());

		this.contentPanel = getContentPane();
		label.addPropertyChangeListener("termComponent", labelListener);
		if (this.tabHistoryList.size() > 0
				&& this.tabHistoryList.getFirst() != null) {
			this.setTermComponent(this.tabHistoryList.getFirst());
		}
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

		fixedToggleChangeActionListener = new FixedToggleChangeActionListener();

		historyButton = new JToggleButton(new ImageIcon(ACE.class
				.getResource("/24x24/plain/history.png")));
		historyButton.setSelected(false);
		historyButton.addActionListener(fixedToggleChangeActionListener);
		historyButton.setToolTipText("show/hide the history records");

		leftTogglePane.add(historyButton);

		c.gridx++;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		toggleBar.add(new JPanel(), c);

		File componentPluginDir = new File(ace.getPluginRoot() + File.separator
				+ "refsetspec");
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
						.showMessageDialog(this.contentPanel, exceptionMessage.toString());
			}
		}

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
						RefsetSpecEditor.this);
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

	private JComponent getContentPane() throws Exception {
		JPanel content = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.BOTH;

    	List<ReflexiveRefsetFieldData> columns = new ArrayList<ReflexiveRefsetFieldData>();
    	getDefaultSpecColumns(EXT_TYPE.CONCEPT_CONCEPT, columns);
    	ReflexiveRefsetMemberTableModel reflexiveModel = new ReflexiveRefsetMemberTableModel(RefsetSpecEditor.this,
    			columns.toArray(new ReflexiveRefsetFieldData[columns.size()]));
    	reflexiveModel.setComponentId(Integer.MIN_VALUE);
       	reflexiveModel.getRowCount();
    	
		JPanel clauseTablePanel = ReflexiveRefsetUtil.getExtensionPanel("Refset Specification:", reflexiveModel,
				RefsetSpecEditor.this, false, false);
		clauseTable = (JTableWithDragImage) clauseTablePanel.getClientProperty("extTable");
		content.add(clauseTablePanel, c);
		c.gridy++;
		c.weighty = 1.0;


		specTree = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode(null)));
		specTree.addMouseListener(new RefsetSpecTreeMouseListener(ace.getAceFrameConfig()));
		specTree.setCellRenderer(new RefsetSpecTreeCellRenderer(ace.getAceFrameConfig()));
		specTree.setRootVisible(false);
		specTree.setShowsRootHandles(true);
		
		content.add(new JScrollPane(specTree), c);
		
		specTree.addTreeSelectionListener(new RefsetSpecSelectionListener());
		
		c.gridy++;
		
		for (I_PluginToConceptPanel plugin : plugins) {
			if (plugin.showComponent()) {
				content.add(plugin.getComponent(this), c);
				c.gridy++;
			}
		}
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.BOTH;
		content.add(new JPanel(), c);
		return content;
	}

	public I_AmTermComponent getTermComponent() {
		return label.getTermComponent();
	}

	public void setTermComponent(final I_AmTermComponent termComponent) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				label.setTermComponent(termComponent);
			}
		});
	}


	public I_GetConceptData getHierarchySelection() {
		throw new UnsupportedOperationException();
	}


	public boolean getShowHistory() {
		return historyButton.isSelected();
	}


	public boolean getShowRefsets() {
		throw new UnsupportedOperationException();
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
			}
		}
		throw new UnsupportedOperationException(" Can't handle toggle: "
				+ toggle);
	}


	public boolean getUsePrefs() {
		return false;
	}


	public VIEW_TYPE getViewType() {
		throw new UnsupportedOperationException();
	}


	public void setAllTogglesToState(boolean state) {
		throw new UnsupportedOperationException();
	}


	public void setLinkType(LINK_TYPE link) {
		throw new UnsupportedOperationException();
	}


	public void setToggleState(TOGGLES toggle, boolean state) {
		throw new UnsupportedOperationException();
	}


	public void unlink() {
		throw new UnsupportedOperationException();
	}


	public I_ConfigAceFrame getConfig() {
		return ace.getAceFrameConfig();
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


	public Dimension getPreferredScrollableViewportSize() {
		return new Dimension(30,30);
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
	
	private void updateSpecTree() {
		UpdateTreeSpec updater = new UpdateTreeSpec();
		updater.start();
	}
	
	private class UpdateTreeSpec extends SwingWorker<DefaultMutableTreeNode> {

		private DefaultMutableTreeNode root;
		private TreePath selectionPath;

		@Override
		protected DefaultMutableTreeNode construct() throws Exception {
			
			boolean newRefset = true;
			selectionPath = specTree.getSelectionPath();
			
			DefaultMutableTreeNode oldRoot = (DefaultMutableTreeNode) specTree.getModel().getRoot();
			
			
			I_GetConceptData refsetConcept = (I_GetConceptData) label.getTermComponent();
			IntSet relTypes = new IntSet();			
			refsetSpecConcept = null;
			if (refsetConcept != null) {
				relTypes.add(RefsetAuxiliary.Concept.SPECIFIES_REFSET.localize().getNid());
				List<I_RelTuple> refsetSpecTuples = refsetConcept.getDestRelTuples(ace.getAceFrameConfig().getAllowedStatus(), 
						relTypes, ace.getAceFrameConfig().getViewPositionSet(), true);
				if (refsetSpecTuples != null && refsetSpecTuples.size() >0) {
					refsetSpecConcept = ConceptBean.get(refsetSpecTuples.get(0).getC1Id());
				}
			}
			root = new DefaultMutableTreeNode(refsetSpecConcept);
			
			if (oldRoot.getUserObject() != null && refsetSpecConcept != null) {
				I_GetConceptData oldRefsetConcept = (I_GetConceptData) oldRoot.getUserObject();
				newRefset = oldRefsetConcept.getConceptId() != refsetSpecConcept.getConceptId();
			}
			
			if (refsetSpecConcept != null) {
				List<I_ThinExtByRefVersioned> extensions = LocalVersionedTerminology.get().getAllExtensionsForComponent(refsetSpecConcept.getConceptId(), true);
				HashMap<Integer, DefaultMutableTreeNode> extensionMap = new HashMap<Integer, DefaultMutableTreeNode>();
				HashSet<Integer> fetchedComponents = new HashSet<Integer>();
				fetchedComponents.add(refsetSpecConcept.getConceptId());
				addExtensionsToMap(extensions, extensionMap, fetchedComponents);		
				AceLog.getAppLog().info("Extension map: " + extensionMap);
				for (DefaultMutableTreeNode extNode: extensionMap.values()) {
					I_ThinExtByRefVersioned ext = (I_ThinExtByRefVersioned) extNode.getUserObject();
					if (ext.getComponentId() == refsetSpecConcept.getConceptId()) {
						root.add(extNode);
					} else {
						extensionMap.get(ext.getComponentId()).add(extNode);
					}
				}
			}
			return root;
		}

		private void addExtensionsToMap(
				List<I_ThinExtByRefVersioned> extensions,
				HashMap<Integer, 
				DefaultMutableTreeNode> extensionMap, 
				HashSet<Integer> fetchedComponents) throws IOException {
			for (I_ThinExtByRefVersioned ext: extensions) {
				extensionMap.put(ext.getMemberId(), new DefaultMutableTreeNode(ext));
				if (fetchedComponents.contains(ext.getMemberId())== false) {
					fetchedComponents.add(ext.getMemberId());
					addExtensionsToMap(LocalVersionedTerminology.get().getAllExtensionsForComponent(ext.getMemberId(), true), 
							extensionMap, fetchedComponents);		
				}
			}
		}

		@Override
		protected void finished() {
			try {
				get();
				DefaultTreeModel tm = (DefaultTreeModel) specTree.getModel();
				tm.setRoot(root);
				specTree.setSelectionPath(selectionPath);
				
			} catch (InterruptedException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (ExecutionException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
			super.finished();
		}
		
	}

	public JTree getTreeInSpecEditor() {
		return specTree;
	}

	public I_GetConceptData getRefsetSpecInSpecEditor() {
		return refsetSpecConcept;
	}

	public TermComponentLabel getLabel() {
		return label;
	}


	private void getDefaultSpecColumns(EXT_TYPE extType,
			List<ReflexiveRefsetFieldData> columns)
			throws NoSuchMethodException {
		ReflexiveRefsetFieldData column1 = new ReflexiveRefsetFieldData();
		column1.setColumnName("truth");
		column1.setCreationEditable(true);
		column1.setUpdateEditable(false);
		column1.setFieldClass(Number.class);
		column1.setMin(5);
		column1.setPref(50);
		column1.setMax(50);
		column1.setInvokedOnPart(true);
		column1.setReadMethod(extType.getPartClass().getMethod("getC1id"));
		column1.setWriteMethod(extType.getPartClass().getMethod("setC1id", int.class));
		column1.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
		columns.add(column1);
		
		ReflexiveRefsetFieldData column2 = new ReflexiveRefsetFieldData();
		column2.setColumnName("clause");
		column2.setCreationEditable(true);
		column2.setUpdateEditable(false);
		column2.setFieldClass(Number.class);
		column2.setMin(5);
		column2.setPref(75);
		column2.setMax(1000);
		column2.setInvokedOnPart(true);
		column2.setReadMethod(extType.getPartClass().getMethod("getC2id"));
		column2.setWriteMethod(extType.getPartClass().getMethod("setC2id", int.class));
		column2.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
		columns.add(column2);
	}
	
	public JPanel getTopPanel() {
		return topPanel;
	}

	public JComponent getContentPanel() {
		return contentPanel;
	}

}
