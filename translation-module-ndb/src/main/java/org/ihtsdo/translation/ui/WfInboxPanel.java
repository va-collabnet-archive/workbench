/*
 * Created by JFormDesigner on Wed Dec 07 16:03:54 GMT-03:00 2011
 */

package org.ihtsdo.translation.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import javax.swing.*;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrameConfig;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.panel.TranslationHelperPanel;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.filters.FilterFactory;
import org.ihtsdo.project.workflow.filters.WfComponentFilter;
import org.ihtsdo.project.workflow.filters.WfDestinationFilter;
import org.ihtsdo.project.workflow.filters.WfSearchFilterBI;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.translation.model.InboxTableModel;
import org.ihtsdo.translation.ui.config.InboxItemConfigurationPanel;

public class WfInboxPanel extends JPanel {
	private static final I_TermFactory tf = Terms.get();
	private static I_ConfigAceFrame config;
	private static final long serialVersionUID = -4013056429939416545L;
	private InboxTableModel model;
	private WfComponentProvider provider;
	private WfUser user;
	protected HashMap<String, WfSearchFilterBI> filterList;

	public WfInboxPanel() {
		initComponents();
		try {
			provider = new WfComponentProvider();
			model = new InboxTableModel(progressBar1);
			inboxTable.setModel(model);
			inboxTable.setAutoCreateRowSorter(true);
			filterList = new HashMap<String, WfSearchFilterBI>();
			if (tf != null) {
				config = tf.getActiveAceFrameConfig();
			}
			if (config != null) {
				I_GetConceptData userConcept = config.getDbConfig().getUserConcept();
				user = new WfUser(userConcept.getInitialText(), userConcept.getPrimUuid());
			}
			inboxTreePanel1.addPropertyChangeListener(InboxTreePanel.INBOX_ITEM_SELECTED, new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent arg0) {
					WfSearchFilterBI filter = FilterFactory.getInstance().createFilterFromObject(arg0.getNewValue());
					Object oldValue = arg0.getOldValue();
					if (oldValue != null) {
						WfSearchFilterBI oldFilter = FilterFactory.getInstance().createFilterFromObject(oldValue);
						filterList.remove(oldFilter.getType());
					}
					filterList.put(filter.getType(), filter);
					model.updatePage(filterList);
				}
			});
			updateDestinationCombo();
			updateFilters();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateDestinationCombo() {
		List<WfUser> users = provider.getUsers();
		destinationCombo.addItem("");
		for (WfUser wfUser : users) {
			destinationCombo.addItem(wfUser);
		}
	}

	public static void main(String[] args) {
		createAndShowGUI();
	}

	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("TableFilterDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Create and set up the content pane.
		WfInboxPanel newContentPane = new WfInboxPanel();
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);

		Object[][] data = { { "Comp1", "T1", "worklist3", "User1", "escalated" }, { "another component", "T2", "worklist3", "User1", "rechazado" },
				{ "distinct", "T3", "worklist3", "User2", "Traducido" }, { "pack", "T4", "worklist4", "Alo", "rechazado" }, { "frame", "T5", "worklist3", "ALE", "Traducido" },
				{ "content", "T6", "worklist4", "Patrix", "Traducido" }, { "Status", "T7", "worklist4", "manu", "escalated" }, { "pane", "Tt", "worklist4", "fulano", "escalated" },
				{ "update", "cc", "zorklist 6", "montoto", "rechazado" }, { "table", "dd", "aworklist", "montoya", "Traducido" }, { "init", "T1", "aworklist", "alo", "Traducido" },
				{ "components", "aworklist", "ale", "User1", "escalated" }, { "GEN", "T1", "bworklist", "Manu", "rechazado" }, { "void", "T1", "bworklist", "montoto", "Traducido" },
				{ "private", "T1", "bworklist", "Patrix", "rechazado" } };
		newContentPane.model.updateTable(data);
	}

	private void filterButtonActionPerformed(ActionEvent e) {
		updateFilters();
		updateTable();
	}

	private void updateTable() {
		model.updatePage(filterList);
	}

	private void updateFilters() {
		String componentFilter = this.componentFilter.getText();
		WfUser destinationFilter = null;
		try {
			destinationFilter = (WfUser) destinationCombo.getSelectedItem();
		} catch (ClassCastException cce) {
			destinationFilter = user;
		}
		WfComponentFilter wfCompFilter = new WfComponentFilter("");
		if (!componentFilter.equals("")) {
			wfCompFilter = new WfComponentFilter(componentFilter);
			filterList.put(wfCompFilter.getType(), wfCompFilter);
		} else {
			filterList.remove(wfCompFilter.getType());
		}

		if (destinationFilter != null) {
			WfDestinationFilter df = new WfDestinationFilter(destinationFilter);
			filterList.put(df.getType(), df);
		}
	}

	private void inboxTableMouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			int selectedIndex = inboxTable.getSelectedRow();
			if (selectedIndex >= 0) {
				int modelRowNum = inboxTable.convertRowIndexToModel(selectedIndex);
				WfInstance wfInstance = (WfInstance) model.getValueAt(modelRowNum, InboxTableModel.WORKFLOW_ITEM);
				JTabbedPane tpc = ((AceFrameConfig) config).getAceFrame().getCdePanel().getConceptTabs();
				if (tpc != null) {
					int tabCount = tpc.getTabCount();
					TranslationPanel uiPanel = null;
					for (int i = 0; i < tabCount; i++) {
						if (tpc.getTitleAt(i).equals(TranslationHelperPanel.TRANSLATION_TAB_NAME)) {
							if (tpc.getComponentAt(i) instanceof TranslationPanel) {
								uiPanel = (TranslationPanel) tpc.getComponentAt(i);

								tpc.setSelectedIndex(i);
								uiPanel.updateUI(wfInstance, false);
								ContextualizedDescription descriptionInEditor = uiPanel.getDescriptionInEditor();
								if (descriptionInEditor != null && !descriptionInEditor.getText().trim().equals("")) {
									if (!uiPanel.verifySavePending(null, false)) {
										return;
									}
								}
								break;
							}
						}

					}
					if (uiPanel == null) {
						uiPanel = new TranslationPanel();
						tpc.addTab(TranslationHelperPanel.TRANSLATION_TAB_NAME, uiPanel);

						tpc.setSelectedIndex(tpc.getTabCount()-1);
						uiPanel.updateUI(wfInstance, false);
					}
				}
			}
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		label2 = new JLabel();
		panel3 = new JPanel();
		label4 = new JLabel();
		label5 = new JLabel();
		label6 = new JLabel();
		componentFilter = new JTextField();
		destinationCombo = new JComboBox();
		stateFilter = new JTextField();
		filterButton = new JButton();
		panel2 = new JPanel();
		progressBar1 = new JProgressBar();
		splitPane1 = new JSplitPane();
		inboxTreePanel1 = new InboxTreePanel();
		scrollPane1 = new JScrollPane();
		inboxTable = new JTable();

		// ======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout(5, 5));

		// ======== panel1 ========
		{
			panel1.setLayout(new BorderLayout(5, 5));

			// ---- label2 ----
			label2.setText("Filters:");
			panel1.add(label2, BorderLayout.WEST);

			// ======== panel3 ========
			{
				panel3.setLayout(new GridBagLayout());
				((GridBagLayout) panel3.getLayout()).columnWidths = new int[] { 0, 0, 0, 0 };
				((GridBagLayout) panel3.getLayout()).rowHeights = new int[] { 0, 0, 0 };
				((GridBagLayout) panel3.getLayout()).columnWeights = new double[] { 1.0, 1.0, 1.0, 1.0E-4 };
				((GridBagLayout) panel3.getLayout()).rowWeights = new double[] { 0.0, 0.0, 1.0E-4 };

				// ---- label4 ----
				label4.setText("Component");
				panel3.add(label4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

				// ---- label5 ----
				label5.setText("Destination");
				panel3.add(label5, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

				// ---- label6 ----
				label6.setText("State");
				panel3.add(label6, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
				panel3.add(componentFilter, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
				panel3.add(destinationCombo, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
				panel3.add(stateFilter, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			}
			panel1.add(panel3, BorderLayout.CENTER);

			// ---- filterButton ----
			filterButton.setText(">>>");
			filterButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					filterButtonActionPerformed(e);
				}
			});
			panel1.add(filterButton, BorderLayout.EAST);
		}
		add(panel1, BorderLayout.NORTH);

		// ======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout) panel2.getLayout()).columnWidths = new int[] { 0, 0 };
			((GridBagLayout) panel2.getLayout()).rowHeights = new int[] { 0, 0 };
			((GridBagLayout) panel2.getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
			((GridBagLayout) panel2.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

			// ---- progressBar1 ----
			progressBar1.setVisible(false);
			panel2.add(progressBar1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, BorderLayout.SOUTH);

		// ======== splitPane1 ========
		{
			splitPane1.setLeftComponent(inboxTreePanel1);

			// ======== scrollPane1 ========
			{

				// ---- inboxTable ----
				inboxTable.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						inboxTableMouseClicked(e);
					}
				});
				scrollPane1.setViewportView(inboxTable);
			}
			splitPane1.setRightComponent(scrollPane1);
		}
		add(splitPane1, BorderLayout.CENTER);
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel panel1;
	private JLabel label2;
	private JPanel panel3;
	private JLabel label4;
	private JLabel label5;
	private JLabel label6;
	private JTextField componentFilter;
	private JComboBox destinationCombo;
	private JTextField stateFilter;
	private JButton filterButton;
	private JPanel panel2;
	private JProgressBar progressBar1;
	private JSplitPane splitPane1;
	private InboxTreePanel inboxTreePanel1;
	private JScrollPane scrollPane1;
	private JTable inboxTable;
	// JFormDesigner - End of variables declaration //GEN-END:variables

}
