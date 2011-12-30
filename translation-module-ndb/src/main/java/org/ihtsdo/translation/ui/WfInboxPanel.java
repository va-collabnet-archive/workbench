/*
 * Created by JFormDesigner on Wed Dec 07 16:03:54 GMT-03:00 2011
 */

package org.ihtsdo.translation.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.panel.TranslationHelperPanel;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.filters.FilterFactory;
import org.ihtsdo.project.workflow.filters.WfComponentFilter;
import org.ihtsdo.project.workflow.filters.WfDestinationFilter;
import org.ihtsdo.project.workflow.filters.WfSearchFilterBI;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.tag.InboxTag;
import org.ihtsdo.project.workflow.tag.TagManager;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.model.InboxTableModel;

public class WfInboxPanel extends JPanel {
	private static final I_TermFactory tf = Terms.get();
	private static I_ConfigAceFrame config;
	private static final long serialVersionUID = -4013056429939416545L;
	private InboxTableModel model;
	private WfComponentProvider provider;
	private WfUser user;
	protected HashMap<String, WfSearchFilterBI> filterList;
	private TableRowSorter<InboxTableModel> sorter;
	private TranslationPanel uiPanel;
	private int nextIndex = 0;
	private Object[] currentRow;
	private TagManager tagManager;
	private HashMap<String, InboxTag> menuItemCache = new HashMap<String, InboxTag>();

	public WfInboxPanel() {

		initComponents();
		try {
			tagManager = TagManager.getInstance();
			tagManager.addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent arg0) {
					if (arg0.getPropertyName().equals(TagManager.NEW_TAG_ADDED)) {
						InboxTag createdTag = (InboxTag) arg0.getNewValue();
						menuItemCache.put(createdTag.toString(), createdTag);
						initTagMenu();
					}
				}
			});
			provider = new WfComponentProvider();
			model = new InboxTableModel(progressBar1);
			sorter = new TableRowSorter<InboxTableModel>(model);
			inboxTable.setModel(model);
			inboxTable.setRowSorter(sorter);
			inboxTable.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);

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
					currentRow = null;
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

			initTagMenu();
			updateDestinationCombo();
			updateFilters();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initTagMenu() {
		try {
			List<InboxTag> tags = tagManager.getTagNames();
			if (tags != null) {
				for (InboxTag tag : tags) {
					JMenuItem tagMenuItem = new JMenuItem();
					String tagMenuString = tag.toString();
					tagMenuItem.setText(tagMenuString);
					menuItemCache.put(tagMenuString, tag);
					tagMenuItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							tagItemActionPreformed(e);
						}
					});
					menu2.add(tagMenuItem);
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void tagItemActionPreformed(ActionEvent e) {
		try {
			int tableRowIndex = inboxTable.getSelectedRow();
			int modelRowIndex = inboxTable.convertRowIndexToModel(tableRowIndex);
			Object[] selectedRow = model.getRow(modelRowIndex);
			WfInstance wfi = (WfInstance) selectedRow[InboxTableModel.WORKFLOW_ITEM];
			List<String> uuidList = new ArrayList<String>();
			uuidList.add(wfi.getComponentId().toString());
			JMenuItem jMenuItem = (JMenuItem) e.getSource();
			InboxTag tag = menuItemCache.get(jMenuItem.getText());
			tag.setUuidList(uuidList);
			TagManager.getInstance().tag(tag);
			model.setValueAt(TagManager.getInstance().getHeader(tag) + selectedRow[InboxTableModel.COMPONENT], modelRowIndex, InboxTableModel.COMPONENT);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void removeTagMenuItemActionPerformed(ActionEvent e) {
		try {
			int tableRowIndex = inboxTable.getSelectedRow();
			int modelRowIndex = inboxTable.convertRowIndexToModel(tableRowIndex);
			Object[] selectedRow = model.getRow(modelRowIndex);
			WfInstance wfi = (WfInstance) selectedRow[InboxTableModel.WORKFLOW_ITEM];
			InboxTag tag = model.getTagByUuid(wfi.getComponentId().toString());
			TagManager.getInstance().removeTag(tag, wfi.getComponentId().toString());
			model.setValueAt(tf.getConcept(wfi.getComponentId()).toUserString(), modelRowIndex, InboxTableModel.COMPONENT);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (TerminologyException ex) {
			ex.printStackTrace();
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
		frame.addWindowStateListener(new WindowStateListener() {

			@Override
			public void windowStateChanged(WindowEvent e) {
				System.out.println("resilze");
			}
		});
		// Display the window.
		frame.pack();
		frame.setVisible(true);

		Object[][] data = new Object[43][];
		for (int i = 0; i < 43; i++) {
			String stringx = "<html><body><table style=\"table-layout:fixed;\"><tr><td style=\"background-color:${COLOR};white-space:nowrap;\">text<td><td style=\"white-space:nowrap;\">RESTO es un texto super larto y la verdad que se va haciendo mas largo";
			Object[] row = { stringx.replace("${COLOR}", "RED") + i, "b" + i, "c" + i, "d" + i, "e" + i };
			data[i] = row;
		}
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
		if (e.getButton() == MouseEvent.BUTTON3) {
			int xPoint = e.getX();
			int yPoint = e.getY();
			int row = inboxTable.rowAtPoint(new Point(xPoint, yPoint));
			inboxTable.setRowSelectionInterval(row, row);
			if (row > -1) {
				popupMenu1.show(inboxTable, xPoint, yPoint);
			}
		} else if (e.getClickCount() == 2) {
			int selectedIndex = inboxTable.getSelectedRow();
			if (selectedIndex >= 0) {

				ConfigTranslationModule cfg = null;
				try {
					cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				if ((selectedIndex + 1) < model.getRowCount()) {
					if (cfg != null && cfg.isAutoOpenNextInboxItem()) {
						nextIndex = selectedIndex + 1;
					}
				}

				int modelRowNum = inboxTable.convertRowIndexToModel(selectedIndex);
				WfInstance wfInstance = (WfInstance) model.getValueAt(modelRowNum, InboxTableModel.WORKFLOW_ITEM);
				JTabbedPane tpc = ((AceFrameConfig) config).getAceFrame().getCdePanel().getConceptTabs();
				if (model.getRowCount() > 0) {
					inboxTable.setRowSelectionInterval(nextIndex, nextIndex);
				}

				if (uiPanel == null) {
					uiPanel = new TranslationPanel();
					tpc.addTab(TranslationHelperPanel.TRANSLATION_TAB_NAME, uiPanel);
					tpc.setSelectedIndex(tpc.getTabCount() - 1);
					uiPanel.addPropertyChangeListener(new PropertyChangeListener() {
						@Override
						public void propertyChange(PropertyChangeEvent arg0) {
							if (arg0.getPropertyName().equals(TranslationPanel.ACTION_LAUNCHED)) {
								arg0.getNewValue();
								arg0.getOldValue();
							}
						}
					});
				} else if (tpc != null) {
					int tabCount = tpc.getTabCount();
					for (int i = 0; i < tabCount; i++) {
						if (tpc.getTitleAt(i).equals(TranslationHelperPanel.TRANSLATION_TAB_NAME)) {
							if (tpc.getComponentAt(i) instanceof TranslationPanel) {
								uiPanel = (TranslationPanel) tpc.getComponentAt(i);
								tpc.setSelectedIndex(i);
								ContextualizedDescription descriptionInEditor = uiPanel.getDescriptionInEditor();
								if (descriptionInEditor != null && !descriptionInEditor.getText().trim().equals("")) {
									if (!uiPanel.verifySavePending(null, false)) {
										return;
									}
								}
							}
						}
						break;
					}
				}
				uiPanel.updateUI(wfInstance, false);
				if (currentRow != null) {
					model.addRow(currentRow);
				}
				currentRow = model.getRow(modelRowNum);
				currentTranslationItem.setText("Current item: " + currentRow[InboxTableModel.COMPONENT]);
				model.removeRow(modelRowNum);
			}
		}
	}

	private void createNewTagActionPerformed(ActionEvent e) {
		NewTagPanel tagPanel = new NewTagPanel();
		InboxTag tag = tagPanel.showModalDialog();
		if (tag != null) {
			try {
				int tableRowIndex = inboxTable.getSelectedRow();
				int modelRowIndex = inboxTable.convertRowIndexToModel(tableRowIndex);
				Object[] selectedRow = model.getRow(tableRowIndex);
				WfInstance wfi = (WfInstance) selectedRow[InboxTableModel.WORKFLOW_ITEM];
				List<String> uuidList = new ArrayList<String>();
				uuidList.add(wfi.getComponentId().toString());
				tag.setUuidList(uuidList);
				TagManager.getInstance().createTag(tag);
				model.setValueAt(TagManager.getInstance().getHeader(tag) + selectedRow[InboxTableModel.COMPONENT], modelRowIndex, InboxTableModel.COMPONENT);
			} catch (IOException e1) {
				e1.printStackTrace();
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
		panel4 = new JPanel();
		scrollPane1 = new JScrollPane();
		inboxTable = new JTable();
		currentTranslationItem = new JLabel();
		popupMenu1 = new JPopupMenu();
		menu2 = new JMenu();
		createNewTag = new JMenuItem();
		removeTagMenuItem = new JMenuItem();

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

			// ======== panel4 ========
			{
				panel4.setLayout(new BorderLayout(5, 5));

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
				panel4.add(scrollPane1, BorderLayout.CENTER);
				panel4.add(currentTranslationItem, BorderLayout.NORTH);
			}
			splitPane1.setRightComponent(panel4);
		}
		add(splitPane1, BorderLayout.CENTER);

		// ======== popupMenu1 ========
		{

			// ======== menu2 ========
			{
				menu2.setText("Tag");

				// ---- createNewTag ----
				createNewTag.setText("new tag");
				createNewTag.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						createNewTagActionPerformed(e);
					}
				});
				menu2.add(createNewTag);
				menu2.addSeparator();
			}
			popupMenu1.add(menu2);

			// ---- removeTagMenuItem ----
			removeTagMenuItem.setText("Remove Tag");
			removeTagMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					removeTagMenuItemActionPerformed(e);
				}
			});
			popupMenu1.add(removeTagMenuItem);
		}
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
	private JPanel panel4;
	private JScrollPane scrollPane1;
	private JTable inboxTable;
	private JLabel currentTranslationItem;
	private JPopupMenu popupMenu1;
	private JMenu menu2;
	private JMenuItem createNewTag;
	private JMenuItem removeTagMenuItem;
	// JFormDesigner - End of variables declaration //GEN-END:variables

}

class ArrayComparator implements Comparator<Object[]> {
	private final int columnToSort;
	private final boolean ascending;

	public ArrayComparator(int columnToSort, boolean ascending) {
		this.columnToSort = columnToSort;
		this.ascending = ascending;
	}

	public int compare(Object[] c1, Object[] c2) {
		return c1[columnToSort].toString().compareTo(c2[columnToSort].toString());
	}
}
