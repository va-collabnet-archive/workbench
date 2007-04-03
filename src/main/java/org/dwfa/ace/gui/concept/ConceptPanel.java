package org.dwfa.ace.gui.concept;

import java.awt.Dimension;
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
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.AbstractSpinnerModel;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dwfa.ace.ACE;
import org.dwfa.ace.AceLog;
import org.dwfa.ace.I_ContainTermComponent;
import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.TermComponentSelectionListener;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.task.AttachmentKeys;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.worker.MasterWorker;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.I_Transact;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.ThinConPart;
import org.dwfa.vodb.types.ThinConVersioned;
import org.dwfa.vodb.types.ThinDescPart;
import org.dwfa.vodb.types.ThinDescVersioned;
import org.dwfa.vodb.types.ThinRelPart;
import org.dwfa.vodb.types.ThinRelVersioned;

import com.sleepycat.je.DatabaseException;

public class ConceptPanel extends JPanel implements I_HostConceptPlugins,
		I_TermFactory, PropertyChangeListener {

	public enum LINK_TYPE {
		UNLINKED, SEARCH_LINK, TREE_LINK
	};

	private class LabelListener implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent evt) {
			updateTab(label.getTermComponent());
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
						AceLog.alertAndLog(ConceptPanel.this, Level.SEVERE,
								"Database Exception: "
										+ e.getLocalizedMessage(), e);
					}
				}
			});
		}

	}

	private class ToggleHistoryChangeActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			firePropertyChange(I_HostConceptPlugins.SHOW_HISTORY,
					!historyButton.isSelected(), historyButton.isSelected());
			try {
				contentScroller.setViewportView(getContentPane());
			} catch (DatabaseException e1) {
				AceLog.alertAndLog(ConceptPanel.this, Level.SEVERE,
						"Database Exception: " + e1.getLocalizedMessage(), e1);
			}
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

	public static ImageIcon SMALL_SEARCH_LINK_ICON = new ImageIcon(ACE.class
			.getResource("/16x16/plain/find.png"));

	public static ImageIcon SMALL_TREE_LINK_ICON = new ImageIcon(ACE.class
			.getResource("/16x16/plain/text_tree.png"));

	private ConflictPlugin conflictPlugin = new ConflictPlugin();

	private AbstractPlugin srcRelPlugin = new SrcRelPlugin();

	private DestRelPlugin destRelPlugin = new DestRelPlugin();

	private IdPlugin idPlugin = new IdPlugin();

	private ImagePlugin imagePlugin = new ImagePlugin();

	private DescriptionPlugin descPlugin = new DescriptionPlugin();

	private LineagePlugin lineagePlugin = new LineagePlugin();

	ConceptAttributePlugin conceptAttributePlugin = new ConceptAttributePlugin();

	private List<I_PluginToConceptPanel> plugins = new ArrayList<I_PluginToConceptPanel>(
			Arrays
					.asList(new I_PluginToConceptPanel[] { idPlugin,
							conceptAttributePlugin, descPlugin, srcRelPlugin,
							destRelPlugin, lineagePlugin, imagePlugin,
							conflictPlugin }));

	public ImageIcon tabIcon;

	private JTabbedPane conceptTabs;

	private JToggleButton inferredButton;

	private ToggleHistoryChangeActionListener historyChangeActionListener;

	private PropertyChangeListener labelListener = new LabelListener();

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

	public ConceptPanel(ACE ace, LINK_TYPE link) throws DatabaseException,
			IOException, ClassNotFoundException {
		this(ace, link, null);
	}

	public ConceptPanel(ACE ace, LINK_TYPE link, JTabbedPane conceptTabs)
			throws DatabaseException, IOException, ClassNotFoundException {
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

		ShowPluginComponentActionListener l = new ShowPluginComponentActionListener();
		for (I_PluginToConceptPanel plugin : plugins) {
			for (JComponent component : plugin.getToggleBarComponents()) {
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
					toggleBar.add(pluginButton, c);
				}
			}
		}

		return toggleBar;
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
				BusinessProcess bp = (BusinessProcess) ois.readObject();
				ois.close();
				getConfig().setStatusMessage("Executing: " + bp.getName());
				MasterWorker worker = getConfig().getWorker();
				// Set concept bean
				// Set config

				worker.writeAttachment(AttachmentKeys.ACE_FRAME_CONFIG.name(),
						getConfig());
				worker.writeAttachment(
						AttachmentKeys.I_GET_CONCEPT_DATA.name(), label
								.getTermComponent());
				worker.writeAttachment(AttachmentKeys.I_TERM_FACTORY.name(),
						ConceptPanel.this);
				worker.writeAttachment(AttachmentKeys.I_HOST_CONCEPT_PLUGINS
						.name(), ConceptPanel.this);
				worker.execute(bp);
				getConfig().setStatusMessage(
						"Execution of " + bp.getName() + " complete.");
			} catch (Exception e1) {
				getConfig().setStatusMessage("Exception during execution.");
				AceLog.alertAndLogException(e1);
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
			if (termComponent != null) {
				ConceptBean cb = (ConceptBean) termComponent;
				String desc;
				try {
					I_DescriptionTuple tdt = cb.getDescTuple(getConfig()
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
		} else if (evt.getPropertyName().equals("commit")) {
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

	public ConceptBean getHierarchySelection() {
		return ace.getAceFrameConfig().getHierarchySelection();
	}

	public I_GetConceptData newConcept(UUID newConceptId, boolean defined)
			throws TerminologyException, IOException {
		canEdit();
		ConceptBean newBean = ConceptBean.get(newConceptId);
		int idSource = AceConfig.vodb
				.uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID
						.getUids());
		int status = AceConfig.vodb
				.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
		int nid = AceConfig.vodb.uuidToNativeWithGeneration(newConceptId,
				idSource, getConfig().getEditingPathSet(), Integer.MAX_VALUE);
		ThinConVersioned conceptAttributes = new ThinConVersioned(nid,
				getConfig().getEditingPathSet().size());
		for (Path p : getConfig().getEditingPathSet()) {
			ThinConPart attributePart = new ThinConPart();
			attributePart.setVersion(Integer.MAX_VALUE);
			attributePart.setDefined(defined);
			attributePart.setPathId(p.getConceptId());
			attributePart.setConceptStatus(status);
			conceptAttributes.addVersion(attributePart);
		}
		newBean.setUncommittedConceptAttributes(conceptAttributes);
		newBean.getUncommittedIds().add(nid);
		ACE.addUncommitted(newBean);
		return newBean;
	}

	public I_DescriptionVersioned newDescription(UUID newDescriptionId,
			I_GetConceptData concept, String lang, String text,
			I_ConceptualizeLocally descType) throws TerminologyException,
			IOException {
		canEdit();
		ACE.addUncommitted((I_Transact) concept);
		int idSource = AceConfig.vodb
				.uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID
						.getUids());
		int descId = AceConfig.vodb.uuidToNativeWithGeneration(
				newDescriptionId, idSource, getConfig().getEditingPathSet(),
				Integer.MAX_VALUE);
		ThinDescVersioned desc = new ThinDescVersioned(descId, concept
				.getConceptId(), getConfig().getEditingPathSet().size());
		ThinDescPart descPart = new ThinDescPart();
		desc.addVersion(descPart);
		boolean capStatus = false;
		int status = AceConfig.vodb
				.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
		int typeId = AceConfig.vodb
				.uuidToNative(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE
						.getUids());
		for (Path p : getConfig().getEditingPathSet()) {
			descPart.setVersion(Integer.MAX_VALUE);
			descPart.setPathId(p.getConceptId());
			descPart.setInitialCaseSignificant(capStatus);
			descPart.setLang(lang);
			descPart.setStatusId(status);
			descPart.setText(text);
			descPart.setTypeId(typeId);
		}
		concept.getUncommittedDescriptions().add(desc);
		concept.getUncommittedIds().add(descId);
		return desc;
	}

	public I_RelVersioned newRelationship(UUID newRelUid,
			I_GetConceptData concept) throws TerminologyException, IOException {
		canEdit();
		int idSource = AceConfig.vodb
				.uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID
						.getUids());
		int relId = AceConfig.vodb.uuidToNativeWithGeneration(newRelUid,
				idSource, getConfig().getEditingPathSet(), Integer.MAX_VALUE);
		ThinRelVersioned rel = new ThinRelVersioned(relId, concept
				.getConceptId(), getConfig().getHierarchySelection()
				.getConceptId(), 1);
		ThinRelPart relPart = new ThinRelPart();
		rel.addVersion(relPart);
		int status = AceConfig.vodb
				.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
		for (Path p : getConfig().getEditingPathSet()) {
			relPart.setVersion(Integer.MAX_VALUE);
			relPart.setPathId(p.getConceptId());
			relPart.setStatusId(status);
			relPart.setRelTypeId(getConfig().getDefaultRelationshipType()
					.getConceptId());
			relPart.setCharacteristicId(getConfig()
					.getDefaultRelationshipCharacteristic().getConceptId());
			relPart.setRefinabilityId(getConfig()
					.getDefaultRelationshipRefinability().getConceptId());
			relPart.setGroup(0);
		}
		concept.getUncommittedSourceRels().add(rel);
		concept.getUncommittedIds().add(relId);
		ACE.addUncommitted((I_Transact) concept);
		return rel;

	}

	public I_RelVersioned newRelationship(UUID newRelUid,
			I_GetConceptData concept, 
			I_ConceptualizeLocally relType,
			I_ConceptualizeLocally relDestination,
			I_ConceptualizeLocally relCharacteristic,
			I_ConceptualizeLocally relRefinability, int relGroup)
			throws TerminologyException, IOException {
		canEdit();
		int idSource = AceConfig.vodb
				.uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID
						.getUids());
		
		int relId = AceConfig.vodb.uuidToNativeWithGeneration(newRelUid,
				idSource, getConfig().getEditingPathSet(), Integer.MAX_VALUE);
		
		ThinRelVersioned rel = new ThinRelVersioned(relId, concept
				.getConceptId(), relDestination.getNid(), getConfig().getEditingPathSet().size());
		
		ThinRelPart relPart = new ThinRelPart();
		
		rel.addVersion(relPart);
		
		int status = AceConfig.vodb
				.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
		
		for (Path p : getConfig().getEditingPathSet()) {
			relPart.setVersion(Integer.MAX_VALUE);
			relPart.setPathId(p.getConceptId());
			relPart.setStatusId(status);
			relPart.setRelTypeId(relType.getNid());
			relPart.setCharacteristicId(relCharacteristic.getNid());
			relPart.setRefinabilityId(relRefinability.getNid());
			relPart.setGroup(relGroup);
		}
		concept.getUncommittedSourceRels().add(rel);
		concept.getUncommittedIds().add(relId);
		ACE.addUncommitted((I_Transact) concept);
		return rel;

	}

	private void canEdit() throws TerminologyException {
		if (getConfig().getEditingPathSet().size() == 0) {
			JOptionPane.showMessageDialog(this,
					"You must select an editing path before editing...");
			throw new TerminologyException("No editing path selected.");
		}
	}
}
