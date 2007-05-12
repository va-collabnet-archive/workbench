package org.dwfa.ace;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.gui.concept.ConceptPanel;
import org.dwfa.ace.gui.concept.ConceptPanel.LINK_TYPE;
import org.dwfa.ace.task.AttachmentKeys;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.worker.MasterWorker;

import com.sleepycat.je.DatabaseException;

public class CollectionEditorContainer extends JPanel {
	
	private class ShowComponentActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (showComponentView.isSelected()) {
				showProcessBuilder.setSelected(false);
				listSplit.setBottomComponent(conceptPanelScroller);
				if (lastDividerLocation > 0) {
					listSplit.setDividerLocation(lastDividerLocation);
				} else {
					listSplit.setDividerLocation(0.30);
				}
			}
			if (showOnlyList()) {
				showListOnly();
			}
		}
		
	}

	private class ShowProcessBuilderActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (showProcessBuilder.isSelected()) {
				showComponentView.setSelected(false);
				listSplit.setBottomComponent(processBuilder);
				if (lastDividerLocation > 0) {
					listSplit.setDividerLocation(lastDividerLocation);
				} else {
					listSplit.setDividerLocation(0.30);
				}
			}
			if (showOnlyList()) {
				showListOnly();
			}
		}

		
	}
	private void showListOnly() {
		int dividerLocation = listSplit.getDividerLocation();
		if (dividerLocation != 3000) {
			lastDividerLocation = dividerLocation;
			listSplit.setBottomComponent(new JPanel());	
			listSplit.setDividerLocation(3000);
		}
	}
	private boolean showOnlyList() {
		return showComponentView.isSelected() == false && showProcessBuilder.isSelected() == false;
	}
	int lastDividerLocation = -1;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JComponent conceptPanelScroller;
	private JComponent processBuilder;
	private JToggleButton showComponentView;
	private JToggleButton showProcessBuilder;
	private JSplitPane listSplit;
	private ACE ace;
	private ConceptPanel cp;

	public I_ConfigAceFrame getConfig() {
		return ace.getAceFrameConfig();
	}

	public CollectionEditorContainer(JList list, ACE ace, JPanel descListProcessBuilderPanel) throws DatabaseException, IOException, ClassNotFoundException {
		super(new GridBagLayout());
		this.ace = ace;
		this.processBuilder = descListProcessBuilderPanel;
		cp = new ConceptPanel(ace,
				LINK_TYPE.LIST_LINK, true);
		cp.setLinkedList(list);
		cp.changeLinkListener(LINK_TYPE.LIST_LINK);
		conceptPanelScroller = new JScrollPane(cp);
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(getListEditorTopPanel(), c);
		c.gridy++;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		add(getListSplit(list, ace), c);
	}

	private JSplitPane getListSplit(JList list, ACE ace) throws DatabaseException,
			IOException, ClassNotFoundException {
		listSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		listSplit.setOneTouchExpandable(true);
		listSplit.setTopComponent(new JScrollPane(list));
		listSplit.setBottomComponent(conceptPanelScroller);
		listSplit.setDividerLocation(3000);
		return listSplit;
	}

	private JPanel getListEditorTopPanel() throws IOException, ClassNotFoundException {
		JPanel listEditorTopPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		c.weighty = 0;
		c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		showComponentView = new JToggleButton(new ImageIcon(
				ACE.class.getResource("/32x32/plain/component.png")));
		showComponentView.addActionListener(new ShowComponentActionListener());
		listEditorTopPanel.add(showComponentView, c);
		c.gridx++;
		showProcessBuilder = new JToggleButton(new ImageIcon(
				ACE.class.getResource("/32x32/plain/cube_molecule.png")));
		listEditorTopPanel.add(showProcessBuilder, c);
		showProcessBuilder.addActionListener(new ShowProcessBuilderActionListener());
		c.gridx++;
		c.weightx = 1.0;
		listEditorTopPanel.add(new JLabel(" "), c);
		c.gridx++;
		c.weightx = 0.0;
		
		
		File componentPluginDir = new File("plugins" + File.separator
				+ "list");
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
					listEditorTopPanel.add(pluginButton, c);
					AceLog.getAppLog().info("adding collection plugin: " + f.getName());
				} else {
					JButton pluginButton = new JButton(bp.getName());
					pluginButton.setToolTipText(bp.getSubject());
					pluginButton.addActionListener(new PluginListener(f));
					c.gridx++;
					listEditorTopPanel.add(pluginButton, c);
					AceLog.getAppLog().info("adding collection plugin: " + f.getName());
				}
			}
		}

		
		listEditorTopPanel.add(new JToggleButton(new ImageIcon(ACE.class
				.getResource("/32x32/plain/branch_delete.png"))), c);
		return listEditorTopPanel;

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
				JList conceptList = getConfig().getBatchConceptList();
				I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
				
				I_GetConceptData concept = null;
				if (conceptList.getSelectedIndex() != -1) {
					concept = (I_GetConceptData) model.getElementAt(conceptList.getSelectedIndex());
				}

				
				worker.writeAttachment(AttachmentKeys.ACE_FRAME_CONFIG.name(),
						getConfig());
				worker.writeAttachment(
						AttachmentKeys.I_GET_CONCEPT_DATA.name(), concept);
				worker.writeAttachment(AttachmentKeys.I_TERM_FACTORY.name(),
						cp);
				worker.writeAttachment(AttachmentKeys.I_HOST_CONCEPT_PLUGINS
						.name(), cp);
				worker.execute(bp);
				getConfig().setStatusMessage(
						"Execution of " + bp.getName() + " complete.");
			} catch (Exception e1) {
				getConfig().setStatusMessage("Exception during execution.");
				AceLog.getAppLog().alertAndLogException(e1);
			}
		}

	}


}
