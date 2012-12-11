/**
 * Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.dwfa.ace.refset;

//~--- non-JDK imports --------------------------------------------------------
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
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
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.dwfa.ace.ACE;
import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_PluginToConceptPanel;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.config.IWorkflowQuery;
import org.dwfa.ace.gui.concept.ConceptPanel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.refset.ReflexiveRefsetCommentTableModel;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.INVOKE_ON_OBJECT_TYPE;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.REFSET_FIELD_TYPE;
import org.dwfa.ace.table.refset.ReflexiveRefsetMemberTableModel;
import org.dwfa.ace.table.refset.ReflexiveRefsetUtil;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.ExecutionRecord;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.taxonomy.TaxonomyHelper;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

//~--- JDK imports ------------------------------------------------------------

public class RefsetSpecEditor implements I_HostConceptPlugins,
		PropertyChangeListener {

	private static final String TAB_HISTORY_KEY = "refset 0";
	private static EnumSet<EConcept.REFSET_TYPES> allowedTypes = EnumSet.of(
			EConcept.REFSET_TYPES.CID_CID, EConcept.REFSET_TYPES.CID_CID_CID,
			EConcept.REFSET_TYPES.CID_CID_STR);

	// ~--- fields
	// --------------------------------------------------------------
	PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private PropertyChangeListener labelListener = new LabelListener();
	private Map<TOGGLES, I_PluginToConceptPanel> pluginMap = new HashMap<TOGGLES, I_PluginToConceptPanel>();
	ACE ace;

	/* package */I_GetConceptData refsetSpecConcept;

	private TaxonomyHelper treeHelper;
	private TaxonomyHelper refsetTreeHelper;

	private UpdateTreeSpec updater;

	private LinkedList<I_GetConceptData> tabHistoryList;
	private I_AmTermComponent tempComponent;

	private JPanel topPanel;

	private JPanel toggleBar;

	private JPanel leftTogglePane;
	private JPanel rightTogglePane;

	TermComponentLabel refexConceptLabel;

	private JLabel memberCountValueLabel;
	private JLabel lastComputeTimeValueLabel;
	private JLabel computeStatusValueLabel;
	private JLabel computeTypeValueLabel;

	JToggleButton historyButton;
	private JButton componentHistoryButton;

	private FixedToggleChangeActionListener fixedToggleChangeActionListener;

	/** This includes the "compute" button */
	private ArrayList<org.dwfa.ace.api.I_PluginToConceptPanel> plugins;

	private JComponent contentPanel;

	private JTableWithDragImage clauseTable;
	JScrollPane specTreeScroller;
	JTree specTree;

	private JTableWithDragImage commentTable;
	private ReflexiveRefsetCommentTableModel commentTableModel;
	JScrollPane commentScroller;

	/** The bottom panel with "taxonomy", "tree", "members" tabs */
	private RefsetSpecPanel refsetSpecPanel;

	/** a filter to only show certain clauses */
	private Predicate<I_ExtendByRef> clauseFilter = Predicates.alwaysTrue();

	public static enum EditState {
		READONLY, EDIT, REVIEW
	}

	private EditState localEditState = EditState.READONLY;

	// mini object to allow the editor to discover
	// (a) if there is a workflow and
	// (b) if this refset is active in it.

	IWorkflowQuery workflowQuery = null;

	JButton addSpecMetaData;

	private static final String SPEC_METADATA_BTN_ICON = "/24x24/plain/paperclip_new.png";

	public static final String DIFF_TITLE = "Diff";
	File[] pluginFiles;

	List<JButton> pluginButtons;
	private JButton btnPromo;
	private JButton btnDiff;

	// ~--- constructors
	// --------------------------------------------------------

	public RefsetSpecEditor(ACE ace, TaxonomyHelper treeHelper,
			TaxonomyHelper refsetTree, RefsetSpecPanel refsetSpecPanel,
			EditState locES) throws Exception {
		super();
		setLocalEditState(locES);
		init(ace, treeHelper, refsetTree, refsetSpecPanel);
	}

	public RefsetSpecEditor(ACE ace, TaxonomyHelper treeHelper,
			TaxonomyHelper refsetTree, RefsetSpecPanel refsetSpecPanel)
			throws Exception {
		super();
		init(ace, treeHelper, refsetTree, refsetSpecPanel);
	}

	private void init(ACE ace, TaxonomyHelper treeHelper,
			TaxonomyHelper refsetTree, RefsetSpecPanel refsetSpecPanel)
			throws Exception {
		this.ace = ace;
		this.treeHelper = treeHelper;
		this.refsetTreeHelper = refsetTree;
		this.refsetSpecPanel = refsetSpecPanel;

		this.tabHistoryList = (LinkedList<I_GetConceptData>) ace
				.getAceFrameConfig().getTabHistoryMap().get(TAB_HISTORY_KEY);

		if (this.tabHistoryList == null) {
			this.tabHistoryList = new LinkedList<I_GetConceptData>();
			ace.getAceFrameConfig().getTabHistoryMap()
					.put(TAB_HISTORY_KEY, this.tabHistoryList);
		}

		plugins = new ArrayList<org.dwfa.ace.api.I_PluginToConceptPanel>(
				Arrays.asList(new org.dwfa.ace.api.I_PluginToConceptPanel[] {}));
		ace.getAceFrameConfig().addPropertyChangeListener("uncommitted",
				new UncommittedChangeListener());

		refexConceptLabel = new TermComponentLabel(this.ace.getAceFrameConfig());
		refexConceptLabel.addMouseListener(new RefsetCommentPopupListener(ace
				.getAceFrameConfig(), this));
		refexConceptLabel.addTermChangeListener(treeHelper);
		refexConceptLabel.addTermChangeListener(refsetTree);

		fixedToggleChangeActionListener = new FixedToggleChangeActionListener();
		this.ace.getAceFrameConfig().addPropertyChangeListener(
				"visibleRefsets", fixedToggleChangeActionListener);
		this.ace.getAceFrameConfig().addPropertyChangeListener(this);

		getTopPanel();
		this.contentPanel = createContentPanel();
		refexConceptLabel.addPropertyChangeListener("termComponent",
				labelListener);

		if ((this.tabHistoryList.size() > 0)
				&& (this.tabHistoryList.getFirst() != null)) {
			this.tempComponent = this.tabHistoryList.getFirst();
			this.setTermComponent(this.tabHistoryList.getFirst());
		}
	}

	// ~--- methods
	// -------------------------------------------------------------
	/* package */void addHistoryActionListener(ActionListener al) {
		this.historyButton.addActionListener(al);
	}

	@Override
	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(propertyName, listener);
	}

	private void firePropertyChange(PropertyChangeEvent evt) {
		pcs.firePropertyChange(evt);
	}

	private void firePropertyChange(String propertyName, boolean oldValue,
			boolean newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}

	private void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		pcs.firePropertyChange(propertyName, oldValue, newValue);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("viewPositions")) {
			fixedToggleChangeActionListener.actionPerformed(null);
		} else if (evt.getPropertyName().equals("commit")) {
			if (refexConceptLabel.getTermComponent() != null) {
				I_GetConceptData cb = (I_GetConceptData) refexConceptLabel
						.getTermComponent();

				try {
					if (cb.getConceptAttributes() == null) {
						refexConceptLabel.setTermComponent(null);
					}
				} catch (IOException e) {
					refexConceptLabel.setTermComponent(null);
					AceLog.getAppLog().alertAndLogException(e);
				}
			}

			this.updateSpecTree(false);

			PropertyChangeEvent pce = new PropertyChangeEvent(this, "commit",
					null, null);

			pce.setPropagationId(AceFrameConfig.propigationId.incrementAndGet());
			this.firePropertyChange(pce);
		} else if (evt.getPropertyName().equals("uncommitted")) {
			this.updateSpecTree(false);
		} else if (evt.getPropertyName().equals("refsetSpecChanged")) {
			this.updateSpecTree(false);
		}
	}

	/* package */void refresh() {
		updatePanel();

		if (commentTableModel != null) {
			commentTableModel.fireTableDataChanged();
		}
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(propertyName, listener);
	}

	public void unlink() {
		throw new UnsupportedOperationException();
	}

	private void updatePanel() {
		I_GetConceptData refset = (I_GetConceptData) getLabel()
				.getTermComponent();
		if (refset != null) {
			RefsetSpec spec = new RefsetSpec(refset, true,
					ace.getAceFrameConfig());

			computeTypeValueLabel.setText(spec.getComputeTypeString());

			try {
				boolean needsCompute = spec.needsCompute();

				if (needsCompute) {
					Long lastComputeTime = spec.getLastComputeTime();

					if (lastComputeTime == null) {
						computeStatusValueLabel.setText("never computed");
						computeStatusValueLabel.setForeground(Color.red);
					} else {
						computeStatusValueLabel
								.setText("modified since last compute");
						computeStatusValueLabel.setForeground(Color.red);
					}
				} else {
					Long lastComputeTime = spec.getLastComputeTime();

					if (lastComputeTime == null) {
						computeStatusValueLabel.setText("never  computed");
						computeStatusValueLabel.setForeground(Color.black);
					} else {
						computeStatusValueLabel
								.setText("unmodified since last compute");
						computeStatusValueLabel.setForeground(Color.black);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				computeStatusValueLabel.setText("Unknown");
				computeStatusValueLabel.setForeground(Color.red);
				computeStatusValueLabel.setBorder(null);
			}

			try {
				Long lastComputeTime = spec.getLastComputeTime();

				if (lastComputeTime == null) {
					lastComputeTimeValueLabel.setText("never");
				} else {
					lastComputeTimeValueLabel.setText(""
							+ new Date(lastComputeTime));
				}
			} catch (Exception e) {
				lastComputeTimeValueLabel
						.setText("Error fetching last compute time");
				e.printStackTrace();
			}

			try {
				ConceptVersionBI refsetVersion = Ts.get()
						.getConceptVersion(getConfig().getViewCoordinate(),
								refset.getConceptNid());
				memberCountValueLabel.setText(""
						+ refsetVersion.getRefsetMembersActive().size());

			} catch (Exception e) {
				memberCountValueLabel.setText("Error computing member count");
				e.printStackTrace();
			}

			if (leftTogglePane != null) {
				leftTogglePane.validate();

				if (leftTogglePane.getParent() != null) {
					leftTogglePane.getParent().validate();
				}
			}
			refreshRightTogglePane();
		}

		refsetSpecConcept = null;
		tempComponent = refexConceptLabel.getTermComponent();

		if (refexConceptLabel.getTermComponent() != null) {
			ace.getAceFrameConfig().setLastViewed(
					(I_GetConceptData) refexConceptLabel.getTermComponent());

			if (tabHistoryList.size() == 0) {
				tabHistoryList.addFirst((I_GetConceptData) refexConceptLabel
						.getTermComponent());
			} else if ((tabHistoryList.size() > 0)
					&& (refexConceptLabel.getTermComponent().equals(
							tabHistoryList.getFirst()) == false)) {
				tabHistoryList.addFirst((I_GetConceptData) refexConceptLabel
						.getTermComponent());
			}

			while (tabHistoryList.size() > 20) {
				tabHistoryList.removeLast();
			}
		}

		updateSpecTree(false);
		commentTableModel.fireTableDataChanged();

		if (treeHelper.getRenderer() != null) {
			treeHelper.getRenderer().propertyChange(
					new PropertyChangeEvent(this, "showRefsetInfoInTaxonomy",
							null, null));
			treeHelper.getRenderer().propertyChange(
					new PropertyChangeEvent(this, "variableHeightTaxonomyView",
							null, null));
			treeHelper.getRenderer().propertyChange(
					new PropertyChangeEvent(this,
							"highlightConflictsInTaxonomyView", null, null));
			treeHelper.getRenderer().propertyChange(
					new PropertyChangeEvent(this, "showViewerImagesInTaxonomy",
							null, null));
			treeHelper.getRenderer().propertyChange(
					new PropertyChangeEvent(this, "refsetsToShow", null, null));
		} else {
			AceLog.getAppLog().info("treeHelper.getRenderer() == null");
		}

		if (refsetTreeHelper.getRenderer() != null) {
			refsetTreeHelper.getRenderer().propertyChange(
					new PropertyChangeEvent(this, "showRefsetInfoInTaxonomy",
							null, null));
			refsetTreeHelper.getRenderer().propertyChange(
					new PropertyChangeEvent(this, "variableHeightTaxonomyView",
							null, null));
			refsetTreeHelper.getRenderer().propertyChange(
					new PropertyChangeEvent(this,
							"highlightConflictsInTaxonomyView", null, null));
			refsetTreeHelper.getRenderer().propertyChange(
					new PropertyChangeEvent(this, "showViewerImagesInTaxonomy",
							null, null));
			refsetTreeHelper.getRenderer().propertyChange(
					new PropertyChangeEvent(this, "refsetsToShow", null, null));
		} else {
			AceLog.getAppLog().info("treeHelper.getRenderer() == null");
		}

	}

	/* package */synchronized void updateSpecTree(boolean clearSelection) {
		refsetSpecConcept = null;

		if (clearSelection) {
			specTree.clearSelection();
		}

		if (updater != null) {
			updater.cancel = true;
		}

		updater = new UpdateTreeSpec(this, clauseFilter);
		updater.execute();
	}

	private void updateToggles() {
		for (TOGGLES t : TOGGLES.values()) {
			boolean visible = ((AceFrameConfig) ace.getAceFrameConfig())
					.isToggleVisible(t);

			if (pluginMap.get(t) != null) {
				org.dwfa.ace.api.I_PluginToConceptPanel plugin = pluginMap
						.get(t);

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

	// ~--- get methods
	// ---------------------------------------------------------
	/* package */ReflexiveRefsetCommentTableModel getCommentTableModel() {
		return commentTableModel;
	}

	public I_ConfigAceFrame getConfig() {
		return ace.getAceFrameConfig();
	}

	private JComponent createContentPanel() throws Exception {
		JTabbedPane refsetTabs = new JTabbedPane();

		refsetTabs.addTab("specification", createSpecPanel());
		commentTable = getRefsetSpecPanel().createCommentTable(
				ace.getAceFrameConfig(), this);
		commentTableModel = (ReflexiveRefsetCommentTableModel) commentTable
				.getModel();
		commentScroller = new JScrollPane(commentTable);
		refsetTabs.addTab("comments", commentScroller);

		return refsetTabs;
	}

	/* package */JComponent getContentPanel() {
		return contentPanel;
	}

	private static void getDefaultSpecColumns(EConcept.REFSET_TYPES extType,
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
		column1.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
		column1.setReadMethod(extType.getPartClass().getMethod("getC1id"));
		column1.setWriteMethod(extType.getPartClass().getMethod("setC1id",
				int.class));
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
		column2.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
		column2.setReadMethod(extType.getPartClass().getMethod("getC2id"));
		column2.setWriteMethod(extType.getPartClass().getMethod("setC2id",
				int.class));
		column2.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
		columns.add(column2);
	}

	public I_GetConceptData getHierarchySelection() {
		throw new UnsupportedOperationException();
	}

	/* package */TermComponentLabel getLabel() {
		return refexConceptLabel;
	}

	/* package */I_GetConceptData getRefsetSpecInSpecEditor()
			throws IOException, TerminologyException {
		I_GetConceptData refsetConcept = (I_GetConceptData) getLabel()
				.getTermComponent();

		if (refsetConcept != null) {
			Set<? extends I_GetConceptData> specs = Terms
					.get()
					.getRefsetHelper(ace.getAceFrameConfig())
					.getSpecificationRefsetForRefset(refsetConcept,
							ace.getAceFrameConfig());

			if (specs.size() > 0) {
				refsetSpecConcept = specs.iterator().next();
			}
		}

		return refsetSpecConcept;
	}

	public RefsetSpecPanel getRefsetSpecPanel() {
		return refsetSpecPanel;
	}

	@Override
	public boolean getShowHistory() {
		return historyButton.isSelected();
	}

	@Override
	public boolean getShowRefsets() {
		throw new UnsupportedOperationException();
	}

	private JComponent createSpecPanel() throws Exception {
		JPanel specPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.BOTH;

		List<ReflexiveRefsetFieldData> columns = new ArrayList<ReflexiveRefsetFieldData>();

		getDefaultSpecColumns(EConcept.REFSET_TYPES.CID_CID, columns);

		ReflexiveRefsetMemberTableModel reflexiveModel = new ReflexiveRefsetMemberTableModel(
				RefsetSpecEditor.this,
				columns.toArray(new ReflexiveRefsetFieldData[columns.size()]));

		reflexiveModel.setComponentId(Integer.MIN_VALUE);
		reflexiveModel.getRowCount();

		JPanel clauseTablePanel = ReflexiveRefsetUtil.getExtensionPanel(null,
				reflexiveModel, RefsetSpecEditor.this, false, false);

		clauseTable = (JTableWithDragImage) clauseTablePanel
				.getClientProperty("extTable");

		int columnIndex = 0;

		for (ReflexiveRefsetFieldData columnId : reflexiveModel.getColumns()) {
			clauseTable.getColumnModel().getColumn(columnIndex)
					.setIdentifier(columnId);
			columnIndex++;
		}

		specPanel.add(clauseTablePanel, c);
		c.gridy++;
		c.weighty = 1.0;
		specTree = new JTree(new DefaultTreeModel(new RefsetSpecTreeNode(null)));
		specTree.addMouseListener(new RefsetSpecTreeMouseListener(ace
				.getAceFrameConfig(), this));
		specTree.setCellRenderer(new RefsetSpecTreeCellRenderer(ace
				.getAceFrameConfig()));
		specTree.setRootVisible(false);
		specTree.setShowsRootHandles(true);
		specTreeScroller = new JScrollPane(specTree);
		specPanel.add(specTreeScroller, c);
		specTree.addTreeSelectionListener(new RefsetSpecSelectionListener());
		c.gridy++;

		for (org.dwfa.ace.api.I_PluginToConceptPanel plugin : plugins) {

			if (plugin.showComponent()) {
				AceLog.getAppLog().info(
						"Adding plugins " + plugin.getName() + " component = "
								+ plugin.getComponent(this));

				specPanel.add(plugin.getComponent(this), c);
				c.gridy++;
			}
		}

		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.BOTH;
		specPanel.add(new JPanel(), c);

		return specPanel;
	}

	@Override
	public I_AmTermComponent getTermComponent() {
		I_AmTermComponent returnValue = tempComponent;

		if (returnValue != null) {
			return returnValue;
		}

		return refexConceptLabel.getTermComponent();
	}

	private JComponent createToggleBar() throws IOException,
			ClassNotFoundException {
		toggleBar = new JPanel(new GridBagLayout());
		GridBagConstraints outer = new GridBagConstraints();
		outer.anchor = GridBagConstraints.WEST;
		outer.gridx = 0;
		outer.gridy = 0;
		outer.fill = GridBagConstraints.NONE;
		outer.weightx = 0;
		outer.weighty = 0;
		toggleBar.add(getLeftTogglePane(), outer);
		outer.weightx = 1.0;
		outer.fill = GridBagConstraints.HORIZONTAL;
		toggleBar.add(new JPanel(), outer);

		outer.weightx = 0.0;
		outer.weighty = 0.0;
		outer.fill = GridBagConstraints.NONE;
		outer.anchor = GridBagConstraints.SOUTHEAST;
		outer.gridx++;

		toggleBar.add(getRightTogglePane(), outer);

		updateToggles();

		return toggleBar;
	}

	private int getNumRefsetSpecTuples(I_GetConceptData refset) {

		if (refset != null) {
			try {
				IntSet relTypes = new IntSet();
				relTypes.add(RefsetAuxiliary.Concept.SPECIFIES_REFSET
						.localize().getNid());
				List<? extends I_RelTuple> refsetSpecTuples = refset
						.getDestRelTuples(ace.getAceFrameConfig()
								.getAllowedStatus(), relTypes, ace
								.getAceFrameConfig()
								.getViewPositionSetReadOnly(),
								ace.aceFrameConfig.getPrecedence(),
								ace.aceFrameConfig
										.getConflictResolutionStrategy());
				if (refsetSpecTuples != null) {
					return refsetSpecTuples.size();

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return 0;
	}

	private List<JButton> getRtpStdButtons() {
		AceLog.getAppLog().info("getRtpStdButtons");
		I_GetConceptData refset = (I_GetConceptData) getLabel()
				.getTermComponent();

		// fire property

		List<JButton> rtpStdButtons = new ArrayList<JButton>();

		boolean isRefsetWithSpec = false;
		if (refset != null && getNumRefsetSpecTuples(refset) > 0) {
			isRefsetWithSpec = true;
		}

		// if EDIT
		if (localEditState == EditState.EDIT) {
			// not yet a refset i.e. no metadata -
			if (isRefsetWithSpec) {
				rtpStdButtons.add(getAddSpecMetaData());
			}

			// If current path not promote path
			if (refset != null) {
				// if(refset.getPositions().)

				rtpStdButtons.add(getBtnPromo());
			}

		}

		// If refset is not null then add diff button
		if (isRefsetWithSpec) {
			rtpStdButtons.add(getBtnDiff());
		}

		return rtpStdButtons;

	}

	@Override
	public boolean getToggleState(TOGGLES toggle) {
		org.dwfa.ace.api.I_PluginToConceptPanel plugin = pluginMap.get(toggle);

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
			default:
				// Do nothing
			}
		}

		throw new UnsupportedOperationException(" Can't handle toggle: "
				+ toggle);

	}

	/* package */
	JPanel getTopPanel() throws Exception {

		if (topPanel == null) {
			topPanel = new JPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			JLabel linkSpinner = new JLabel(new ImageIcon(
					ACE.class.getResource("/24x24/plain/paperclip.png")));
			linkSpinner.setBorder(BorderFactory.createEmptyBorder(3, 3, 2, 5));
			c.anchor = GridBagConstraints.WEST;
			c.gridx = 0;
			c.gridy = 0;
			c.fill = GridBagConstraints.BOTH;
			topPanel.add(linkSpinner, c);

			c.gridx++;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			topPanel.add(refexConceptLabel, c);

			c.weightx = 0.0;
			c.gridx++;

			// FIXME : we are replacing a previously plumbed-in listener - the
			// first
			// one will be disconnected
			fixedToggleChangeActionListener = new FixedToggleChangeActionListener();

			historyButton = new JToggleButton(new ImageIcon(
					ACE.class.getResource("/24x24/plain/history.png")));
			historyButton.setSelected(false);
			historyButton.addActionListener(fixedToggleChangeActionListener);
			historyButton.setToolTipText("show/hide the history records");
			topPanel.add(historyButton, c);

			c.gridx++;
			componentHistoryButton = new JButton(ConceptPanel.HISTORY_ICON);
			componentHistoryButton.addActionListener(new ShowHistoryListener());
			componentHistoryButton
					.setToolTipText("click to show history of the RefSet Specification displayed in this viewer");
			topPanel.add(componentHistoryButton, c);

			c.gridx = 0;
			c.gridy++;
			c.gridwidth = 5;
			c.fill = GridBagConstraints.HORIZONTAL;

			JComponent toggleBar = createToggleBar();
			topPanel.add(toggleBar, c);
			c.fill = GridBagConstraints.BOTH;
			c.weighty = 1.0;
			c.gridy++;
			topPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		}

		return topPanel;
	}

	/* package */JTree getTreeInSpecEditor() {
		return specTree;
	}

	public boolean getUsePrefs() {
		return false;
	}

	// ~--- set methods
	// ---------------------------------------------------------
	public void setAllTogglesToState(boolean state) {
		throw new UnsupportedOperationException();
	}

	public void setLinkType(LINK_TYPE link) {
		throw new UnsupportedOperationException();
	}

	public void setTermComponent(final I_AmTermComponent termComponent) {
		tempComponent = termComponent;

		if (SwingUtilities.isEventDispatchThread()) {
			refexConceptLabel.setTermComponent(termComponent);
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					public void run() {
						refexConceptLabel.setTermComponent(termComponent);
					}
				});
			} catch (InterruptedException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (InvocationTargetException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}

	}

	public void setToggleState(TOGGLES toggle, boolean state) {
		throw new UnsupportedOperationException();
	}

	public static boolean addRefsetMetadata(boolean markedParentOnly,
			I_GetConceptData memberRefset, I_GetConceptData refsetComputeTypeIn) {
		try {
			if (memberRefset == null) {
				return false;
			}
			I_ConfigAceFrame aceConfig = Terms.get().getActiveAceFrameConfig();
			String name = Ts
					.get()
					.getConceptVersion(aceConfig.getViewCoordinate(),
							memberRefset.getConceptNid())
					.getPreferredDescription().getText();
			if (name.endsWith(" refset")) {
				name = name.replace(" refset", "");
			}
			I_GetConceptData status = Terms.get().getConcept(
					ArchitectonicAuxiliary.Concept.CURRENT.getUids());
			if (Ts.get().hasUuid(
					SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()[0])) {
				status = (I_GetConceptData) SnomedMetadataRf2.ACTIVE_VALUE_RF2
						.getLenient();
			}
			I_GetConceptData refsetComputeType = refsetComputeTypeIn;
			if (refsetComputeTypeIn == null) {
				refsetComputeType = Terms.get().getConcept(
						RefsetAuxiliary.Concept.CONCEPT_COMPUTE_TYPE.getUids());
			}

			I_GetConceptData fsnConcept = Terms
					.get()
					.getConcept(
							ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
									.getUids());
			I_GetConceptData ptConcept = Terms.get().getConcept(
					ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
							.getUids());
			I_GetConceptData isA = Terms.get().getConcept(
					ArchitectonicAuxiliary.Concept.IS_A_REL.getUids());
			I_GetConceptData supportingRefset = Terms.get().getConcept(
					RefsetAuxiliary.Concept.SUPPORTING_REFSETS.getUids());
			I_GetConceptData markedParentRel = Terms.get().getConcept(
					RefsetAuxiliary.Concept.MARKED_PARENT_REFSET.getUids());
			I_GetConceptData markedParentIsATypeRel = Terms.get().getConcept(
					RefsetAuxiliary.Concept.MARKED_PARENT_IS_A_TYPE.getUids());
			I_GetConceptData specifiesRefsetRel = Terms.get().getConcept(
					RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
			I_GetConceptData commentsRel = Terms.get().getConcept(
					RefsetAuxiliary.Concept.COMMENTS_REL.getUids());
			I_GetConceptData editTimeRel = Terms.get().getConcept(
					RefsetAuxiliary.Concept.EDIT_TIME_REL.getUids());
			I_GetConceptData computeTimeRel = Terms.get().getConcept(
					RefsetAuxiliary.Concept.COMPUTE_TIME_REL.getUids());
			I_GetConceptData purposeRel = Terms.get().getConcept(
					RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids());
			I_GetConceptData refsetComputeTypeRel = Terms.get().getConcept(
					RefsetAuxiliary.Concept.REFSET_COMPUTE_TYPE_REL.getUids());
			I_GetConceptData stringAnnotation = Terms.get()
					.getConcept(
							RefsetAuxiliary.Concept.STRING_ANNOTATION_PURPOSE
									.getUids());
			I_GetConceptData markedParentAnnotation = Terms.get().getConcept(
					RefsetAuxiliary.Concept.REFSET_PARENT_MEMBER_PURPOSE
							.getUids());
			I_GetConceptData specAnnotation = Terms.get().getConcept(
					RefsetAuxiliary.Concept.REFSET_SPECIFICATION.getUids());
			I_GetConceptData ancillaryDataAnnotation = Terms.get().getConcept(
					RefsetAuxiliary.Concept.ANCILLARY_DATA.getUids());
			// check that the name isn't null or empty etc
			if ((name == null) || name.trim().equals("")) {
				throw new TaskFailedException("Refset name cannot be empty.");
			}
			String refsetSpecName = name + " refset spec";
			String markedParentName = name + " marked parent";
			String commentsName = name + " comments refset";
			String editTimeName = name + " edit time refset";
			String computeTimeName = name + " compute time refset";
			// create new concepts
			try {
				I_GetConceptData markedParent = newConcept(aceConfig, status);
				I_GetConceptData editTimeRefset = newConcept(aceConfig, status);
				I_GetConceptData computeTimeRefset = newConcept(aceConfig,
						status);
				I_GetConceptData commentsRefset = newConcept(aceConfig, status);
				newDescription(markedParent, fsnConcept, markedParentName,
						aceConfig, status);
				newDescription(markedParent, ptConcept, markedParentName,
						aceConfig, status);
				newRelationship(memberRefset, markedParentRel, markedParent,
						aceConfig);
				newRelationship(markedParent, purposeRel,
						markedParentAnnotation, aceConfig);
				newDescription(editTimeRefset, fsnConcept, editTimeName,
						aceConfig, status);
				newDescription(editTimeRefset, ptConcept, editTimeName,
						aceConfig, status);
				newDescription(computeTimeRefset, fsnConcept, computeTimeName,
						aceConfig, status);
				newDescription(computeTimeRefset, ptConcept, computeTimeName,
						aceConfig, status);
				newDescription(commentsRefset, fsnConcept, commentsName,
						aceConfig, status);
				newDescription(commentsRefset, ptConcept, commentsName,
						aceConfig, status);
				I_GetConceptData refsetSpec = null;
				if (!markedParentOnly) {
					refsetSpec = newConcept(aceConfig, status);
					newDescription(refsetSpec, fsnConcept, refsetSpecName,
							aceConfig, status);
					newDescription(refsetSpec, ptConcept, refsetSpecName,
							aceConfig, status);
					newRelationship(refsetSpec, specifiesRefsetRel,
							memberRefset, aceConfig);
					newRelationship(refsetSpec, isA, supportingRefset,
							aceConfig);
					newRelationship(refsetSpec, refsetComputeTypeRel,
							refsetComputeType, aceConfig);
					// supporting refsets purpose relationships
					newRelationship(refsetSpec, purposeRel, specAnnotation,
							aceConfig);
					RefsetSpec spec = new RefsetSpec(refsetSpec, aceConfig);
					spec.modifyOverallSpecStatus(status);
				}
				newRelationship(markedParent, isA, supportingRefset, aceConfig);
				newRelationship(commentsRefset, purposeRel, stringAnnotation,
						aceConfig);
				newRelationship(editTimeRefset, purposeRel,
						ancillaryDataAnnotation, aceConfig);
				newRelationship(computeTimeRefset, purposeRel,
						ancillaryDataAnnotation, aceConfig);
				newRelationship(commentsRefset, isA, supportingRefset,
						aceConfig);
				newRelationship(editTimeRefset, isA, supportingRefset,
						aceConfig);
				newRelationship(computeTimeRefset, isA, supportingRefset,
						aceConfig);
				newRelationship(memberRefset, commentsRel, commentsRefset,
						aceConfig);
				newRelationship(memberRefset, editTimeRel, editTimeRefset,
						aceConfig);
				newRelationship(memberRefset, computeTimeRel,
						computeTimeRefset, aceConfig);
				Terms.get().addUncommittedNoChecks(markedParent);
				Terms.get().addUncommittedNoChecks(commentsRefset);
				Terms.get().addUncommittedNoChecks(editTimeRefset);
				Terms.get().addUncommittedNoChecks(computeTimeRefset);
				Terms.get().addUncommittedNoChecks(memberRefset);
				Ts.get().commit(markedParent);
				Ts.get().commit(commentsRefset);
				Ts.get().commit(editTimeRefset);
				Ts.get().commit(computeTimeRefset);
				Ts.get().commit(memberRefset);
				if (!markedParentOnly) {
					Terms.get().addUncommittedNoChecks(refsetSpec);
					Ts.get().commit(refsetSpec);
					Terms.get().addUncommittedNoChecks(memberRefset);
					Ts.get().commit(memberRefset);
				}
				// create FSN and PT for each

			} catch (TerminologyException ex) {
				Terms.get().cancel();
				JOptionPane
						.showMessageDialog(
								LogWithAlerts.getActiveFrame(null),
								"Refset wizard cannot be completed. "
										+ ex.getMessage(), "",
								JOptionPane.ERROR_MESSAGE);
				return false;
			}
			I_IntSet availableIsATypes = aceConfig.getDestRelTypes();
			for (int isAType : availableIsATypes.getSetValues()) {
				newRelationship(memberRefset, markedParentIsATypeRel, Terms
						.get().getConcept(isAType), aceConfig);
			}
			Terms.get().addUncommittedNoChecks(memberRefset);
			Ts.get().commit(memberRefset);
		} catch (Exception exception) {
			AceLog.getAppLog().alertAndLogException(exception);
		}
		return true;
	}

	private static I_GetConceptData newConcept(I_ConfigAceFrame aceConfig,
			I_GetConceptData status) throws Exception {
		boolean isDefined = true;
		UUID conceptUuid = UUID.randomUUID();
		I_GetConceptData newConcept = Terms.get().newConcept(conceptUuid,
				isDefined, aceConfig, status.getNid());

		return newConcept;
	}

	private static void newDescription(I_GetConceptData concept,
			I_GetConceptData descriptionType, String description,
			I_ConfigAceFrame aceConfig, I_GetConceptData status)
			throws TerminologyException, Exception {
		I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(
				Terms.get().getActiveAceFrameConfig());
		I_IntSet actives = helper.getCurrentStatusIntSet();

		if (descriptionType.getNid() == ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
				.localize().getNid()) {
			String filteredDescription = description;

			filteredDescription = filteredDescription.trim();

			// new removal using native lucene escaping
			filteredDescription = QueryParser.escape(filteredDescription);

			SearchResult result = Terms.get().doLuceneSearch(
					filteredDescription);

			for (int i = 0; i < result.topDocs.totalHits; i++) {
				Document doc = result.searcher
						.doc(result.topDocs.scoreDocs[i].doc);
				int cnid = Integer.parseInt(doc.get("cnid"));
				int dnid = Integer.parseInt(doc.get("dnid"));

				if (cnid == concept.getConceptNid()) {
					continue;
				}

				try {
					I_DescriptionVersioned<?> potential_fsn = Terms.get()
							.getDescription(dnid, cnid);
					if (potential_fsn != null
							&& potential_fsn.getMutableParts() != null) {
						for (I_DescriptionPart<?> part_search : potential_fsn
								.getMutableParts()) {
							if (actives.contains(part_search.getStatusNid())
									&& (part_search.getTypeNid() == ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
											.localize().getNid())
									&& part_search.getText()
											.equals(description)) {
								throw new TerminologyException(
										"Concept already exists in database with FSN: "
												+ description);
							}
						}
					}
				} catch (IOException ioe) {
					AceLog.getAppLog().warning(
							"unique fsn check. Doc: \n" + doc + "\nex:\n"
									+ ioe.toString());
				}
			}
		}

		UUID descUuid = UUID.randomUUID();

		Terms.get().newDescription(descUuid, concept, "en", description,
				descriptionType, Terms.get().getActiveAceFrameConfig(),
				status.getNid());
		Terms.get().addUncommittedNoChecks(concept);
	}

	private static void newRelationship(I_GetConceptData concept,
			I_GetConceptData relationshipType, I_GetConceptData destination,
			I_ConfigAceFrame aceConfig) throws Exception {
		int statusId = Terms.get()
				.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids())
				.getConceptNid();
		UUID relUuid = UUID.randomUUID();
		I_GetConceptData charConcept = Terms.get().getConcept(
				ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
		I_GetConceptData refConcept = Terms.get().getConcept(
				ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
		int group = 0;

		Terms.get().newRelationship(relUuid, concept, relationshipType,
				destination, charConcept, refConcept,
				Terms.get().getConcept(statusId), group,
				Terms.get().getActiveAceFrameConfig());
		Terms.get().addUncommittedNoChecks(concept);
	}

	// ~--- inner classes
	// -------------------------------------------------------

	private class InternalActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			AceLog.getAppLog().info("InternalActionListener AP");
			if (e.getSource() instanceof JButton) {
				AceLog.getAppLog().info("InternalActionListener IS button");
				if (e.getSource() == getAddSpecMetaData()) {
					if (addRefsetMetadata(false,
							(I_GetConceptData) getTermComponent(), null)) {
						return;
					}
				}
				if (e.getSource() == getBtnDiff()) {
					AceLog.getAppLog().info("InternalActionListener ISDIFF");
					I_GetConceptData igcd = (I_GetConceptData) getTermComponent();
					String conName = "";
					try {
						conName = igcd.getInitialText();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					String conUid = igcd.getUUIDs().get(0).toString();
					firePropertyChange(DIFF_TITLE, conName, conUid);
				}
				if (e.getSource() == getBtnPromo()) {
					promoteRefset();
				}
			}

		}
	}

	private class FixedToggleChangeActionListener implements ActionListener,
			PropertyChangeListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			perform();
		}

		private void perform() {
			firePropertyChange(I_HostConceptPlugins.SHOW_HISTORY,
					!historyButton.isSelected(), historyButton.isSelected());

			try {
				updateSpecTree(false);

				if ((refsetSpecPanel != null)
						&& (refsetSpecPanel.getRefsetTable() != null)) {
					refsetSpecPanel.getRefsetTable().repaint();
				}
			} catch (Exception e1) {
				AceLog.getAppLog().alertAndLog(contentPanel, Level.SEVERE,
						"Database Exception: " + e1.getLocalizedMessage(), e1);
			}
		}

		@Override
		public void propertyChange(PropertyChangeEvent arg0) {
			perform();
		}
	}

	private class LabelListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			AceLog.getAppLog().info("propertyChange");
			updatePanel();
			localEditState = EditState.READONLY;
			String conId = "";
			I_GetConceptData refset = (I_GetConceptData) getLabel()
					.getTermComponent();
			try {
				if (refset != null) {
					conId = refset.getUids().iterator().next().toString();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			boolean conIdSet = conId != null && conId.length() > 0;

			if (getWorkflowQuery() == null) {
				localEditState = EditState.EDIT;
			} else {
				if (conIdSet
						&& !getWorkflowQuery().isConceptActiveInWorkflow(conId,
								IWorkflowQuery.REFSET)) {
					localEditState = EditState.EDIT;
				}
			}
			firePropertyChange(evt.getPropertyName(), evt.getOldValue(),
					evt.getNewValue());
		}
	}

	private class PluginListener implements ActionListener {

		File pluginProcessFile;

		// ~--- constructors
		// -----------------------------------------------------
		private PluginListener(File pluginProcessFile) {
			super();
			this.pluginProcessFile = pluginProcessFile;
		}

		// ~--- methods
		// ----------------------------------------------------------
		public void actionPerformed(ActionEvent e) {
			try {
				final JButton button = (JButton) e.getSource();

				button.setEnabled(false);

				FileInputStream fis = new FileInputStream(pluginProcessFile);
				BufferedInputStream bis = new BufferedInputStream(fis);
				ObjectInputStream ois = new ObjectInputStream(bis);
				final BusinessProcess bp = (BusinessProcess) ois.readObject();

				ois.close();
				getConfig().setStatusMessage("Executing: " + bp.getName());

				final I_Work worker;

				if (getConfig().getWorker().isExecuting()) {
					worker = getConfig().getWorker()
							.getTransactionIndependentClone();
				} else {
					worker = getConfig().getWorker();
				}

				// Set concept bean
				// Set config
				worker.writeAttachment(
						WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(),
						getConfig());
				bp.writeAttachment(
						ProcessAttachmentKeys.I_GET_CONCEPT_DATA.name(),
						refexConceptLabel.getTermComponent());
				worker.writeAttachment(
						WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name(),
						RefsetSpecEditor.this);

				Runnable r = new Runnable() {

					private String exceptionMessage;

					public void run() {
						button.setEnabled(false);

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
							button.setEnabled(true);
						} catch (Throwable e1) {
							worker.getLogger().log(Level.WARNING,
									e1.toString(), e1);
							exceptionMessage = e1.toString();
							button.setEnabled(true);
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

								ace.getAceFrameConfig().refreshRefsetTab();
							}
						});
					}
				};

				new Thread(r, this.getClass().getCanonicalName()).start();
			} catch (Exception e1) {
				getConfig().setStatusMessage("Exception during execution.");
				AceLog.getAppLog().alertAndLogException(e1);
			}
		}
	}

	private class RefsetSpecSelectionListener implements TreeSelectionListener {

		public void valueChanged(TreeSelectionEvent tse) {
			if (tse.getNewLeadSelectionPath() != null) {
				TreePath selectionPath = tse.getNewLeadSelectionPath();
				RefsetSpecTreeNode selectedNode = (RefsetSpecTreeNode) selectionPath
						.getLastPathComponent();
				I_ExtendByRef ext = (I_ExtendByRef) selectedNode
						.getUserObject();

				try {
					EConcept.REFSET_TYPES extType = EConcept.REFSET_TYPES
							.nidToType(ext.getTypeNid());

					if (allowedTypes.contains(extType) == false) {
						throw new Exception("Can't handle " + extType);
					}

					List<ReflexiveRefsetFieldData> columns = new ArrayList<ReflexiveRefsetFieldData>();

					getDefaultSpecColumns(extType, columns);

					if (extType == EConcept.REFSET_TYPES.CID_CID_CID) {
						ReflexiveRefsetFieldData column3 = new ReflexiveRefsetFieldData();

						column3.setColumnName("constraint");
						column3.setCreationEditable(true);
						column3.setUpdateEditable(false);
						column3.setFieldClass(Number.class);
						column3.setMin(5);
						column3.setPref(175);
						column3.setMax(2000);
						column3.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
						column3.setReadMethod(extType.getPartClass().getMethod(
								"getC3id"));
						column3.setWriteMethod(extType.getPartClass()
								.getMethod("setC3id", int.class));
						column3.setType(REFSET_FIELD_TYPE.COMPONENT_IDENTIFIER);
						columns.add(column3);
					} else if (extType == EConcept.REFSET_TYPES.CID_CID_STR) {
						ReflexiveRefsetFieldData column3 = new ReflexiveRefsetFieldData();

						column3.setColumnName("query string");
						column3.setCreationEditable(true);
						column3.setUpdateEditable(false);
						column3.setFieldClass(String.class);
						column3.setMin(5);
						column3.setPref(175);
						column3.setMax(2000);
						column3.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
						column3.setReadMethod(extType.getPartClass().getMethod(
								"getStringValue"));
						column3.setWriteMethod(extType.getPartClass()
								.getMethod("setStringValue", String.class));
						column3.setType(REFSET_FIELD_TYPE.STRING);
						columns.add(column3);
					}

					ReflexiveRefsetFieldData column4 = new ReflexiveRefsetFieldData();

					column4.setColumnName("status");
					column4.setCreationEditable(true);
					column4.setUpdateEditable(true);
					column4.setFieldClass(Number.class);
					column4.setMin(5);
					column4.setPref(150);
					column4.setMax(150);
					column4.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
					column4.setReadMethod(extType.getPartClass().getMethod(
							"getStatusId"));
					column4.setWriteMethod(extType.getPartClass().getMethod(
							"setStatusId", int.class));
					column4.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
					columns.add(column4);

					if (historyButton.isSelected()) {
						ReflexiveRefsetFieldData column5 = new ReflexiveRefsetFieldData();

						column5.setColumnName("time");
						column5.setCreationEditable(false);
						column5.setUpdateEditable(false);
						column5.setFieldClass(Number.class);
						column5.setMin(5);
						column5.setPref(150);
						column5.setMax(150);
						column5.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
						column5.setReadMethod(extType.getPartClass().getMethod(
								"getTime"));
						column5.setWriteMethod(extType.getPartClass()
								.getMethod("setTime", long.class));
						column5.setType(REFSET_FIELD_TYPE.TIME);
						columns.add(column5);

						ReflexiveRefsetFieldData column6 = new ReflexiveRefsetFieldData();

						column6.setColumnName("path");
						column6.setCreationEditable(false);
						column6.setUpdateEditable(false);
						column6.setFieldClass(String.class);
						column6.setMin(5);
						column6.setPref(150);
						column6.setMax(150);
						column6.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
						column6.setReadMethod(extType.getPartClass().getMethod(
								"getPathId"));
						column6.setWriteMethod(extType.getPartClass()
								.getMethod("setPathId", int.class));
						column6.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
						columns.add(column6);
					}

					ReflexiveRefsetMemberTableModel reflexiveModel = new ReflexiveRefsetMemberTableModel(
							RefsetSpecEditor.this,
							columns.toArray(new ReflexiveRefsetFieldData[columns
									.size()]));

					reflexiveModel.setComponentId(ext.getMemberId());
					reflexiveModel.getRowCount();
					clauseTable.setModel(reflexiveModel);

					int columnIndex = 0;

					for (ReflexiveRefsetFieldData columnId : reflexiveModel
							.getColumns()) {
						clauseTable.getColumnModel().getColumn(columnIndex)
								.setIdentifier(columnId);
						columnIndex++;
					}
				} catch (Exception e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			} else {
				try {
					List<ReflexiveRefsetFieldData> columns = new ArrayList<ReflexiveRefsetFieldData>();

					getDefaultSpecColumns(EConcept.REFSET_TYPES.CID_CID,
							columns);

					ReflexiveRefsetMemberTableModel reflexiveModel = new ReflexiveRefsetMemberTableModel(
							RefsetSpecEditor.this,
							columns.toArray(new ReflexiveRefsetFieldData[columns
									.size()]));

					reflexiveModel.setComponentId(Integer.MIN_VALUE);
					reflexiveModel.getRowCount();
					clauseTable.setModel(reflexiveModel);

					int columnIndex = 0;

					for (ReflexiveRefsetFieldData columnId : reflexiveModel
							.getColumns()) {
						clauseTable.getColumnModel().getColumn(columnIndex)
								.setIdentifier(columnId);
						columnIndex++;
					}
				} catch (Exception e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			}
		}
	}

	private class ShowHistoryListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (tabHistoryList.size() > 1) {
				JPopupMenu popup = new JPopupMenu();
				List<I_GetConceptData> historyToRemove = new ArrayList<I_GetConceptData>();

				for (I_GetConceptData historyItem : tabHistoryList) {
					try {
						if (Terms.get().getUids(historyItem.getConceptNid()) != null) {
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
				popup.show(contentPanel, mouseLocation.x, mouseLocation.y);
			}
		}

		// ~--- inner classes
		// ----------------------------------------------------
		private class ShowHistoryAction extends AbstractAction {

			/**
             *
             */
			private static final long serialVersionUID = 1L;
			// ~--- fields
			// --------------------------------------------------------
			I_GetConceptData concept;

			// ~--- constructors
			// --------------------------------------------------
			public ShowHistoryAction(I_GetConceptData concept) {
				super(concept.toString());
				this.concept = concept;
			}

			// ~--- methods
			// -------------------------------------------------------
			public void actionPerformed(ActionEvent e) {
				RefsetSpecEditor.this.setTermComponent(concept);
			}
		}
	}

	private class UncommittedChangeListener implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent arg0) {
			UpdateTreeSpec specUpdater = new UpdateTreeSpec(
					RefsetSpecEditor.this, clauseFilter);

			specUpdater.execute();
		}
	}

	/**
	 * Set a filter used to show only nodes that we want to find interesting
	 * 
	 * @param clauseFilter
	 */
	public void setFilter(Predicate<I_ExtendByRef> clauseFilter) {
		this.clauseFilter = clauseFilter;
		updateSpecTree(true);
	}

	/* package */EditState getLocalEditState() {
		return localEditState;
	}

	public void setLocalEditState(EditState localEditStateIn) {
		this.localEditState = localEditStateIn;
	}

	public final IWorkflowQuery getWorkflowQuery() {
		return workflowQuery;
	}

	public final void setWorkflowQuery(IWorkflowQuery workflowQueryIn) {
		workflowQuery = workflowQueryIn;
	}

	public final JButton getAddSpecMetaData() {
		if (addSpecMetaData == null) {
			addSpecMetaData = new JButton(new ImageIcon(
					ACE.class.getResource(SPEC_METADATA_BTN_ICON)));
			addSpecMetaData.addActionListener(new InternalActionListener());
			addSpecMetaData
					.setToolTipText("Add metadata to enable refset specification for this concept.");
		}

		return addSpecMetaData;
	}

	public final void setAddSpecMetaData(final JButton addSpecMetaDataIn) {
		addSpecMetaData = addSpecMetaDataIn;
	}

	public final JPanel getLeftTogglePane() {

		if (leftTogglePane == null) {

			// Inspect refset

			// if (getLabel() != null && getLabel().getTermComponent() != null)
			// {
			// I_GetConceptData refsetLabel = (I_GetConceptData) getLabel()
			// .getTermComponent();
			//
			// AceLog.getAppLog().info(
			// "refsetLabel " + refsetLabel.toLongString());
			// }
			//
			// // .getRefsetSpecEditor()
			// if (getTermComponent() != null) {
			// I_GetConceptData refsetTC = (I_GetConceptData)
			// getTermComponent();
			// AceLog.getAppLog().info("refsetTC " + refsetTC.toLongString());
			// }
			// AceLog.getAppLog().info("localEditState = " + localEditState);

			leftTogglePane = new JPanel(new GridBagLayout());
			GridBagConstraints inner = new GridBagConstraints();

			inner.anchor = GridBagConstraints.WEST;
			inner.gridx = 0;
			inner.gridy = 3;
			inner.fill = GridBagConstraints.NONE;
			inner.weightx = 0;
			inner.weighty = 0;
			inner.gridheight = 2; // make button use 2 rows
			inner.insets = new Insets(0, 0, 0, 0);

			inner.gridx = 0;
			inner.gridy = 0;
			inner.gridheight = 1;
			inner.insets = new Insets(0, 0, 0, 0);
			inner.anchor = GridBagConstraints.EAST;

			JLabel memberCountLabel = new JLabel(
					"member count at last compute: ");

			leftTogglePane.add(memberCountLabel, inner);
			inner.gridy++;

			JLabel lastComputeTimeLabel = new JLabel("time of last compute: ");

			leftTogglePane.add(lastComputeTimeLabel, inner);
			inner.gridy++;

			JLabel computeStatusLabel = new JLabel("compute status: ");

			leftTogglePane.add(computeStatusLabel, inner);
			inner.gridy++;

			JLabel computeTypeLabel = new JLabel("compute type: ");

			leftTogglePane.add(computeTypeLabel, inner);
			inner.gridy++;

			inner.anchor = GridBagConstraints.WEST;
			inner.gridx++;
			inner.gridy--;
			inner.gridy--;
			inner.gridy--;
			inner.gridy--;
			memberCountValueLabel = new JLabel("");
			leftTogglePane.add(memberCountValueLabel, inner);
			inner.gridy++;
			lastComputeTimeValueLabel = new JLabel("");
			leftTogglePane.add(lastComputeTimeValueLabel, inner);
			inner.gridy++;
			computeStatusValueLabel = new JLabel("");
			leftTogglePane.add(computeStatusValueLabel, inner);
			inner.gridy++;
			computeTypeValueLabel = new JLabel("");
			leftTogglePane.add(computeTypeValueLabel, inner);
			inner.gridy++;

		}

		return leftTogglePane;
	}

	public final void setLeftTogglePane(JPanel leftTogglePaneIn) {
		leftTogglePane = leftTogglePaneIn;
	}

	public final JPanel getRightTogglePane() {

		if (rightTogglePane == null) {
			rightTogglePane = new JPanel(new FlowLayout());
			addRtpButtons();
		}

		return rightTogglePane;
	}

	public void addRtpButtons() {
		if (getPluginFiles() != null) {
			boolean exceptions = false;
			StringBuilder exceptionMessage = new StringBuilder();
			exceptionMessage
					.append("<html>Exception(s) reading the following plugin(s): <p><p>");
			for (JButton btn : getPluginButtons()) {
				// rightTogglePane.add(btn, contraints);
				getRightTogglePane().add(btn);
			}
			if (exceptions) {
				exceptionMessage
						.append("<p>Please see the log file for more details.");
				JOptionPane.showMessageDialog(this.contentPanel,
						exceptionMessage.toString());
			}
		}

		for (JButton btn : getRtpStdButtons()) {
			getRightTogglePane().add(btn);
			// rightTogglePane.add(btn, contraints);
		}
	}

	public final void setRightTogglePane(JPanel rightTogglePaneIn) {
		rightTogglePane = rightTogglePaneIn;
	}

	public final void refreshRightTogglePane() {
		getRightTogglePane().removeAll();
		addRtpButtons();
		getRightTogglePane().validate();
	}

	public final File[] getPluginFiles() {

		if (pluginFiles == null) {
			File componentPluginDir = new File(ace.getPluginRoot()
					+ File.separator + "refsetspec");
			pluginFiles = componentPluginDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File arg0, String fileName) {
					return fileName.toLowerCase().endsWith(".bp")
							&& fileName
									.contains("compute refset from refset spec");
				}
			});
		}
		return pluginFiles;
	}

	public final void setPluginFiles(File[] pluginFilesIn) {
		pluginFiles = pluginFilesIn;
	}

	private List<JButton> getPluginButtons() {
		if (pluginButtons == null) {
			pluginButtons = new ArrayList<JButton>();
		}
		if (pluginButtons.size() == 0) {
			for (File f : getPluginFiles()) {
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
						pluginButtons.add(pluginButton);
					} else {
						JButton pluginButton = new JButton(bp.getName());

						pluginButton.setToolTipText(bp.getSubject());
						pluginButton.addActionListener(new PluginListener(f));
						pluginButtons.add(pluginButton);
					}
				} catch (Throwable e) {
					// exceptions = true;
					// exceptionMessage.append("Exception reading plugin: ")
					// .append(f.getAbsolutePath()).append("<p>");
					AceLog.getAppLog().log(Level.SEVERE,
							"Exception reading: " + f.getAbsolutePath(), e);
				}
			}
		}

		return pluginButtons;
	}

	public final void setPluginButtons(List<JButton> pluginButtonsIn) {
		pluginButtons = pluginButtonsIn;
	}

	public void promoteRefset() {

		I_GetConceptData refset = (I_GetConceptData) getLabel()
				.getTermComponent();

		if (refset != null && getNumRefsetSpecTuples(refset) == 0) {
			promoteRefset(refset);
			updatePanel();
		}

	}

	public void promoteRefset(I_GetConceptData refsetToPromote) {

		// Position is Latest

		// get the termfactory
		I_TermFactory tf = Terms.get();
		// Get the frameConfig;
		AceFrameConfig frameConfig = null;
		try {
			frameConfig = (AceFrameConfig) tf.getActiveAceFrameConfig();

			if (frameConfig != null) {

				PathSetReadOnly promotionPaths = new PathSetReadOnly(
						frameConfig.getPromotionPathSet());
				Set<? extends PositionBI> viewPositionSet = frameConfig
						.getViewPositionSet();
				// there should be only 1 promotionPath
				if (promotionPaths.size() > 0) {

					PositionBI viewPosition = viewPositionSet.iterator().next();
					promoteRefset(frameConfig, viewPosition, promotionPaths,
							tf, refsetToPromote);

					for (I_GetConceptData specificationRefsetIdentity : Terms
							.get()
							.getRefsetHelper(frameConfig)
							.getSpecificationRefsetForRefset(refsetToPromote,
									frameConfig)) {
						promoteRefset(frameConfig, viewPosition,
								promotionPaths, tf, specificationRefsetIdentity);
					}
					for (I_GetConceptData promotionRefsetIdentity : Terms
							.get()
							.getRefsetHelper(frameConfig)
							.getPromotionRefsetForRefset(refsetToPromote,
									frameConfig)) {
						promoteRefset(frameConfig, viewPosition,
								promotionPaths, tf, promotionRefsetIdentity);
					}
					for (I_GetConceptData markedParentRefsetIdentity : Terms
							.get()
							.getRefsetHelper(frameConfig)
							.getMarkedParentRefsetForRefset(refsetToPromote,
									frameConfig)) {
						promoteRefset(frameConfig, viewPosition,
								promotionPaths, tf, markedParentRefsetIdentity);
					}
					for (I_GetConceptData commentsRefsetIdentity : Terms
							.get()
							.getRefsetHelper(frameConfig)
							.getCommentsRefsetForRefset(refsetToPromote,
									frameConfig)) {
						promoteRefset(frameConfig, viewPosition,
								promotionPaths, tf, commentsRefsetIdentity);
					}

					for (I_GetConceptData editTimeRefsetIdentity : Terms
							.get()
							.getRefsetHelper(frameConfig)
							.getEditTimeRefsetForRefset(refsetToPromote,
									frameConfig)) {
						promoteRefset(frameConfig, viewPosition,
								promotionPaths, tf, editTimeRefsetIdentity);
					}

					for (I_GetConceptData computeTimeRefsetIdentity : Terms
							.get()
							.getRefsetHelper(frameConfig)
							.getComputeTimeRefsetForRefset(refsetToPromote,
									frameConfig)) {
						promoteRefset(frameConfig, viewPosition,
								promotionPaths, tf, computeTimeRefsetIdentity);
					}
				}

			}
			tf.commit();
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.SEVERE, "promoteRefset Exception", e);
		}
	}

	/**
	 * @see org.dwfa.ace.task.promote.PromoteRefset.promoteRefset Get rid of all
	 *      the activity promotionPath default edit path, viewposition = latest
	 */

	private void promoteRefset(I_ConfigAceFrame config,
			PositionBI viewPosition, PathSetReadOnly promotionPaths,
			I_TermFactory tf, I_GetConceptData refsetIdentity)
			throws TerminologyException, IOException {
		AceLog.getAppLog().info(
				"promoteRefset called refsetID = "
						+ refsetIdentity.toLongString());
		Collection<? extends I_ExtendByRef> extensions = tf
				.getRefsetExtensionMembers(refsetIdentity.getConceptNid());
		AceLog.getAppLog().info(
				"promoteRefset extensions size = " + extensions.size());
		refsetIdentity.promote(viewPosition, promotionPaths, null,
				config.getPrecedence());
		for (I_ExtendByRef ext : extensions) {
			AceLog.getAppLog().info("promoteRefset I_ExtendByRef ext = " + ext);
			ext.promote(viewPosition, new PathSetReadOnly(promotionPaths),
					null, config.getPrecedence());
		}
		AceLog.getAppLog().info("promoteRefset addUncommittedNoChecks");
		tf.addUncommittedNoChecks(refsetIdentity);
	}

	public final JButton getBtnPromo() {
		if (btnPromo == null) {
			btnPromo = new JButton("Promote");
			btnPromo.addActionListener(new InternalActionListener());
		}
		return btnPromo;
	}

	public final void setBtnPromo(JButton btnPromoIn) {
		btnPromo = btnPromoIn;
	}

	public final JButton getBtnDiff() {
		if (btnDiff == null) {
			btnDiff = new JButton(DIFF_TITLE);
			btnDiff.addActionListener(new InternalActionListener());
		}
		return btnDiff;
	}

	public final void setBtnDiff(JButton btnDiffIn) {
		btnDiff = btnDiffIn;
	}

}
