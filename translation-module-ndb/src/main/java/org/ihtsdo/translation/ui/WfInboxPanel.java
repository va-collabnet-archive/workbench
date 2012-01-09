/*
 * Created by JFormDesigner on Wed Dec 07 16:03:54 GMT-03:00 2011
 */

package org.ihtsdo.translation.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.panel.TranslationHelperPanel;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.api.WorkflowInterpreter;
import org.ihtsdo.project.workflow.api.WorkflowSearcher;
import org.ihtsdo.project.workflow.event.EventMediator;
import org.ihtsdo.project.workflow.event.GenericEvent.EventType;
import org.ihtsdo.project.workflow.event.NewTagEvent;
import org.ihtsdo.project.workflow.event.NewTagEventHandler;
import org.ihtsdo.project.workflow.event.TagRemovedEvent;
import org.ihtsdo.project.workflow.event.TagRemovedEventHandler;
import org.ihtsdo.project.workflow.filters.FilterFactory;
import org.ihtsdo.project.workflow.filters.WfComponentFilter;
import org.ihtsdo.project.workflow.filters.WfDestinationFilter;
import org.ihtsdo.project.workflow.filters.WfSearchFilterBI;
import org.ihtsdo.project.workflow.filters.WfTagFilter;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfInstance.ActionReport;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.tag.InboxTag;
import org.ihtsdo.project.workflow.tag.TagManager;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.model.InboxTableModel;
import org.ihtsdo.translation.ui.ConfigTranslationModule.InboxColumn;
import org.ihtsdo.translation.ui.event.InboxItemSelectedEvent;
import org.ihtsdo.translation.ui.event.InboxItemSelectedEventHandler;
import org.ihtsdo.translation.ui.event.ItemDestinationChangedEvent;
import org.ihtsdo.translation.ui.event.ItemSentToSpecialFolderEvent;
import org.ihtsdo.translation.ui.event.ItemStateChangedEvent;

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
	private boolean expanded = false;
	private JDialog d;
	private ConfigTranslationModule cfg;
	private int currentModelRowNum;
	private boolean specialTag;

	public WfInboxPanel() {
		initComponents();
		try {
			tagManager = TagManager.getInstance();
			provider = new WfComponentProvider();
			model = new InboxTableModel(progressBar1);
			sorter = new TableRowSorter<InboxTableModel>(model);
			inboxTable.setModel(model);
			inboxTable.setRowSorter(sorter);
			inboxTable.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);

			suscribeToEvents();

			try {
				cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			checkBox1.setSelected(cfg.isAutoOpenNextInboxItem());

			filterPanel.setVisible(false);

			filterList = new HashMap<String, WfSearchFilterBI>();
			if (tf != null) {
				config = tf.getActiveAceFrameConfig();
			}
			if (config != null) {
				I_GetConceptData userConcept = config.getDbConfig().getUserConcept();
				user = new WfUser(userConcept.getInitialText(), userConcept.getPrimUuid());
			}
			initTagMenu();
			updateDestinationCombo();
			updateFilters();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void suscribeToEvents() {

		EventMediator mediator = EventMediator.getInstance();

		mediator.suscribe(EventType.NEW_TAG_ADDED, new NewTagEventHandler<NewTagEvent>(this) {
			@Override
			public void handleEvent(NewTagEvent event) {
				InboxTag tag = event.getTag();
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
		});
		mediator.suscribe(EventType.TAG_REMOVED, new TagRemovedEventHandler<TagRemovedEvent>(this) {
			@Override
			public void handleEvent(TagRemovedEvent event) {
				InboxTag tag = event.getTag();
				String tagMenuString = tag.toString();
				int compCount = menu2.getComponentCount();
				for (int i = 0; i < compCount; i++) {
					if (menu2.getComponent(i) instanceof JMenuItem) {
						JMenuItem tagMenu = (JMenuItem) menu2.getComponent(i);
						if (tagMenu.getText().equals(tagMenuString)) {
							menu2.remove(i);
							break;
						}
					}

				}
			}
		});

		mediator.suscribe(EventType.INBOX_ITEM_SELECTED, new InboxItemSelectedEventHandler<InboxItemSelectedEvent>(this) {
			@Override
			public void handleEvent(InboxItemSelectedEvent event) {
				currentRow = null;
				WfSearchFilterBI filter = FilterFactory.getInstance().createFilterFromObject(event.getInboxItem());
				Object oldValue = event.getOldInboxItem();
				if (oldValue != null) {
					WfSearchFilterBI oldFilter = FilterFactory.getInstance().createFilterFromObject(oldValue);
					filterList.remove(oldFilter.getType());
				}
				filterList.put(filter.getType(), filter);
				model.updatePage(filterList);

			}
		});

	}

	private void initTagMenu() {
		menu2.removeAll();
		menu2.add(createNewTag);
		menu2.addSeparator();
		try {
			List<InboxTag> tags = tagManager.getTagNames();
			if (tags != null) {
				for (InboxTag tag : tags) {
					if (!tag.getTagName().equals("outbox") && !tag.getTagName().equals("todo")) {
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
			WfInstance wfi = (WfInstance) selectedRow[InboxColumn.values().length + 1];
			List<String> uuidList = new ArrayList<String>();
			String uuid = wfi.getComponentId().toString();
			uuidList.add(uuid);
			JMenuItem jMenuItem = (JMenuItem) e.getSource();
			InboxTag tag = menuItemCache.get(jMenuItem.getText());
			tag.setUuidList(uuidList);
			TagManager.getInstance().tag(tag);
			model.addTagToCache(uuid, tag);
			String taggedComponent = TagManager.getInstance().getHeader(tag) + selectedRow[InboxColumn.SOURCE_PREFERRED.getColumnNumber()];
			model.setValueAt(taggedComponent, modelRowIndex, InboxColumn.SOURCE_PREFERRED.getColumnNumber());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void removeTagMenuItemActionPerformed(ActionEvent e) {
		try {
			int tableRowIndex = inboxTable.getSelectedRow();
			int modelRowIndex = inboxTable.convertRowIndexToModel(tableRowIndex);
			Object[] selectedRow = model.getRow(modelRowIndex);
			WfInstance wfi = (WfInstance) selectedRow[InboxColumn.values().length + 1];
			InboxTag tag = model.getTagByUuid(wfi.getComponentId().toString());
			model.removeTagFromCache(wfi.getComponentId().toString());
			TagManager.getInstance().removeTag(tag, wfi.getComponentId().toString());
			model.setValueAt(tf.getConcept(wfi.getComponentId()).toUserString(), modelRowIndex, InboxColumn.SOURCE_PREFERRED.getColumnNumber());
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
		Set<String> keys = filterList.keySet();
		for (String key : keys) {
			WfSearchFilterBI filter = filterList.get(key);
			if (filter instanceof WfTagFilter) {
				WfTagFilter tf = (WfTagFilter) filter;
				if (tf.getTag().getTagName().equals(TagManager.OUTBOX) || tf.getTag().getTagName().equals(TagManager.TODO)) {
					specialTag = true;
				} else {
					specialTag = false;
				}
			}
		}
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
			openItem();
		}
	}

	private void openItem() {
		int selectedIndex = inboxTable.getSelectedRow();
		if (selectedIndex >= 0) {
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

			currentModelRowNum = inboxTable.convertRowIndexToModel(selectedIndex);
			WfInstance wfInstance = (WfInstance) model.getValueAt(currentModelRowNum, InboxColumn.values().length + 1);
			JTabbedPane tpc = ((AceFrameConfig) config).getAceFrame().getCdePanel().getConceptTabs();

			if (uiPanel == null) {
				uiPanel = new TranslationPanel();
				tpc.addTab(TranslationHelperPanel.TRANSLATION_TAB_NAME, uiPanel);
				tpc.setSelectedIndex(tpc.getTabCount() - 1);
				uiPanel.addPropertyChangeListener(new ChangeListener());
			} else if (tpc != null) {
				int tabCount = tpc.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tpc.getTitleAt(i).equals(TranslationHelperPanel.TRANSLATION_TAB_NAME)) {
						if (tpc.getComponentAt(i) instanceof TranslationPanel) {
							uiPanel = (TranslationPanel) tpc.getComponentAt(i);
							tpc.setSelectedIndex(i);
							if (!uiPanel.verifySavePending(null, false)) {
								return;
							}
						}
						break;
					}
				}
			}
			uiPanel.updateUI(wfInstance, false);
			currentRow = model.getRow(currentModelRowNum);
			currentTranslationItem.setText(currentRow[InboxColumn.SOURCE_PREFERRED.getColumnNumber()].toString());
			if (model.getRowCount() > 0) {
				inboxTable.setRowSelectionInterval(nextIndex, nextIndex);
			}
		}
	}

	class ChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent arg0) {
			if (arg0.getPropertyName().equals(TranslationPanel.ACTION_LAUNCHED)) {
				Object newValue = arg0.getNewValue();
				Object oldValue = arg0.getOldValue();
				if (newValue instanceof WfInstance) {
					WfInstance newWfInstance = (WfInstance) newValue;
					WfInstance oldWfInstance = (WfInstance) oldValue;

					ActionReport actionReport = newWfInstance.getActionReport();
					if (actionReport != null) {
						switch (actionReport) {
						case CANCEL:
							break;
						case COMPLETE:
							if (newWfInstance.getDestination().equals(user)) {
								model.updateRow(currentRow, currentModelRowNum, specialTag);
								EventMediator.getInstance().fireEvent(new ItemStateChangedEvent(newWfInstance));
								inboxTreePanel1.itemUserAndStateChanged(oldWfInstance, newWfInstance);
							} else {
								try {
									tagManager.sendToOutbox(newWfInstance.getComponentId().toString());
									EventMediator.getInstance().fireEvent(new ItemDestinationChangedEvent(newWfInstance));
									inboxTreePanel1.itemStateChanged(oldWfInstance, newWfInstance);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							break;
						case SAVE_AS_TODO:
							try {
								tagManager.saveAsToDo(((WfInstance) currentRow[InboxColumn.values().length + 1]).getComponentId().toString());
								EventMediator.getInstance().fireEvent(new ItemSentToSpecialFolderEvent(newWfInstance));
								model.removeRow(currentModelRowNum);
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						case OUTBOX:
							try {
								tagManager.sendToOutbox(((WfInstance) currentRow[InboxColumn.values().length + 1]).getComponentId().toString());
								EventMediator.getInstance().fireEvent(new ItemSentToSpecialFolderEvent(newWfInstance));
								model.removeRow(currentModelRowNum);
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						default:
							break;
						}
						openItem();
					} else {

					}
				}
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
				WfInstance wfi = (WfInstance) selectedRow[InboxColumn.values().length + 1];
				List<String> uuidList = new ArrayList<String>();
				String uuid = wfi.getComponentId().toString();
				uuidList.add(uuid);
				tag.setUuidList(uuidList);
				TagManager.getInstance().createTag(tag);
				model.addTagToCache(uuid, tag);
				model.setValueAt(TagManager.getInstance().getHeader(tag) + selectedRow[InboxColumn.SOURCE_PREFERRED.getColumnNumber()], modelRowIndex, InboxColumn.SOURCE_PREFERRED.getColumnNumber());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void expandActionPerformed(ActionEvent e) {
		if (!expanded) {
			model.setColumnCount(model.getRealColumnSize());
			model.fireTableStructureChanged();
			filterPanel.setVisible(true);
			splitPanel.setResizeWeight(1.0d);
			d = new JDialog();
			d.setAlwaysOnTop(true);
			Dimension dim = new Dimension(850, 500);
			d.setPreferredSize(dim);
			d.setContentPane(inboxItems);
			d.setVisible(true);
			d.pack();
			d.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			expanded = true;
			expand.setText("Zoom out");
		} else {
			filterPanel.setVisible(false);
			model.setColumnCount(3);
			model.fireTableStructureChanged();
			splitPanel.setResizeWeight(0.5d);
			expand.setText("Zoom in");
			splitPanel.setBottomComponent(d.getContentPane());
			d.dispose();
			expanded = false;
		}
	}

	private void closeInboxActionPerformed(ActionEvent e) {
		try {
			AceFrameConfig config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();
			AceFrame ace = config.getAceFrame();

			JTabbedPane tpc = ace.getCdePanel().getLeftTabs();
			if (tpc != null) {
				int tabCount = tpc.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tpc.getTitleAt(i).equals(TranslationHelperPanel.TRANSLATION_LEFT_MENU)) {
						if (tpc.getComponentAt(i) instanceof TranslationPanel) {
							TranslationPanel uiPanel = (TranslationPanel) tpc.getComponentAt(i);
							if (!uiPanel.verifySavePending(null, false)) {
								return;
							}
						}
						tpc.remove(i);
						tpc.repaint();
						tpc.revalidate();
						break;
					}

				}
			}
			if (tpc != null) {
				tpc = ace.getCdePanel().getConceptTabs();
				int tabCount = tpc.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tpc.getTitleAt(i).equals(TranslationHelperPanel.TRANSLATION_TAB_NAME)) {
						if (tpc.getComponentAt(i) instanceof TranslationConceptEditor6) {
							TranslationConceptEditor6 uiPanel = (TranslationConceptEditor6) tpc.getComponentAt(i);
							if (!uiPanel.verifySavePending(null, false)) {
								return;
							}
							uiPanel.AutokeepInInbox();
						}
						tpc.remove(i);
						tpc.repaint();
						tpc.revalidate();
						break;
					}

				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void checkBox1ActionPerformed(ActionEvent e) {
		ConfigTranslationModule cfg = null;
		try {
			cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
			cfg.setAutoOpenNextInboxItem(checkBox1.isSelected());
			LanguageUtil.setTranslationConfig(Terms.get().getActiveAceFrameConfig(), cfg);
		} catch (IOException ex) {

			ex.printStackTrace();
		} catch (TerminologyException ex) {

			ex.printStackTrace();
		}
	}

	private void sendToOutboxActionPerformed(ActionEvent e) {
		try {
			WorkflowSearcher searcher = new WorkflowSearcher();
			Collection<WfSearchFilterBI> collection = new ArrayList<WfSearchFilterBI>();
			InboxTag tag = TagManager.getInstance().getTagContent(TagManager.OUTBOX);
			collection.add(new WfTagFilter(tag));
			List<WfInstance> wfInstances = searcher.searchWfInstances(collection);
			for (WfInstance instance : wfInstances) {
				WorkflowInterpreter interpreter = WorkflowInterpreter.createWorkflowInterpreter(instance.getWfDefinition());
				WfUser nextDestination = interpreter.getNextDestination(instance, instance.getWorkList());
				WfInstance.updateDestination(instance, nextDestination);
			}
			Terms.get().commit();
			TagManager.getInstance().emptyOutboxTag();
			inboxTreePanel1.updateTree();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		panel2 = new JPanel();
		progressBar1 = new JProgressBar();
		splitPanel = new JSplitPane();
		inboxTreePanel1 = new InboxTreePanel();
		inboxItems = new JPanel();
		scrollPane1 = new JScrollPane();
		inboxTable = new JTable();
		panel1 = new JPanel();
		expand = new JButton();
		filterPanel = new JPanel();
		label2 = new JLabel();
		panel3 = new JPanel();
		label4 = new JLabel();
		label5 = new JLabel();
		label6 = new JLabel();
		componentFilter = new JTextField();
		destinationCombo = new JComboBox();
		stateFilter = new JTextField();
		filterButton = new JButton();
		label1 = new JLabel();
		currentTranslationItem = new JLabel();
		checkBox1 = new JCheckBox();
		panel4 = new JPanel();
		closeInbox = new JButton();
		sendToOutbox = new JButton();
		popupMenu1 = new JPopupMenu();
		menu2 = new JMenu();
		createNewTag = new JMenuItem();
		removeTagMenuItem = new JMenuItem();

		// ======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout(5, 5));

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

		// ======== splitPanel ========
		{
			splitPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitPanel.setResizeWeight(0.5);
			splitPanel.setTopComponent(inboxTreePanel1);

			// ======== inboxItems ========
			{
				inboxItems.setLayout(new BorderLayout(5, 5));

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
				inboxItems.add(scrollPane1, BorderLayout.CENTER);

				// ======== panel1 ========
				{
					panel1.setBorder(new EmptyBorder(5, 5, 5, 5));
					panel1.setLayout(new GridBagLayout());
					((GridBagLayout) panel1.getLayout()).columnWidths = new int[] { 89, 0, 0, 0 };
					((GridBagLayout) panel1.getLayout()).rowHeights = new int[] { 0, 6, 0, 0, 0 };
					((GridBagLayout) panel1.getLayout()).columnWeights = new double[] { 0.0, 0.0, 1.0, 1.0E-4 };
					((GridBagLayout) panel1.getLayout()).rowWeights = new double[] { 1.0, 1.0, 1.0, 0.0, 1.0E-4 };

					// ---- expand ----
					expand.setText("Zoom in");
					expand.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							expandActionPerformed(e);
						}
					});
					panel1.add(expand, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 5, 5), 0, 0));

					// ======== filterPanel ========
					{
						filterPanel.setVisible(false);
						filterPanel.setLayout(new BorderLayout(5, 5));

						// ---- label2 ----
						label2.setText("Filters:");
						filterPanel.add(label2, BorderLayout.WEST);

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
						filterPanel.add(panel3, BorderLayout.CENTER);

						// ---- filterButton ----
						filterButton.setText(">>>");
						filterButton.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								filterButtonActionPerformed(e);
							}
						});
						filterPanel.add(filterButton, BorderLayout.EAST);
					}
					panel1.add(filterPanel, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

					// ---- label1 ----
					label1.setText("Current item:");
					panel1.add(label1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));
					panel1.add(currentTranslationItem, new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

					// ---- checkBox1 ----
					checkBox1.setText("Automatically open next item in inbox after finishing with current item");
					checkBox1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							checkBox1ActionPerformed(e);
						}
					});
					panel1.add(checkBox1, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				}
				inboxItems.add(panel1, BorderLayout.NORTH);
			}
			splitPanel.setBottomComponent(inboxItems);
		}
		add(splitPanel, BorderLayout.CENTER);

		// ======== panel4 ========
		{
			panel4.setLayout(new GridBagLayout());
			((GridBagLayout) panel4.getLayout()).columnWidths = new int[] { 0, 0, 0 };
			((GridBagLayout) panel4.getLayout()).rowHeights = new int[] { 0, 0 };
			((GridBagLayout) panel4.getLayout()).columnWeights = new double[] { 1.0, 0.0, 1.0E-4 };
			((GridBagLayout) panel4.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

			// ---- closeInbox ----
			closeInbox.setText("Close");
			closeInbox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					closeInboxActionPerformed(e);
				}
			});
			panel4.add(closeInbox, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 5), 0, 0));

			// ---- sendToOutbox ----
			sendToOutbox.setText("Send");
			sendToOutbox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sendToOutboxActionPerformed(e);
				}
			});
			panel4.add(sendToOutbox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel4, BorderLayout.NORTH);

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
	private JPanel panel2;
	private JProgressBar progressBar1;
	private JSplitPane splitPanel;
	private InboxTreePanel inboxTreePanel1;
	private JPanel inboxItems;
	private JScrollPane scrollPane1;
	private JTable inboxTable;
	private JPanel panel1;
	private JButton expand;
	private JPanel filterPanel;
	private JLabel label2;
	private JPanel panel3;
	private JLabel label4;
	private JLabel label5;
	private JLabel label6;
	private JTextField componentFilter;
	private JComboBox destinationCombo;
	private JTextField stateFilter;
	private JButton filterButton;
	private JLabel label1;
	private JLabel currentTranslationItem;
	private JCheckBox checkBox1;
	private JPanel panel4;
	private JButton closeInbox;
	private JButton sendToOutbox;
	private JPopupMenu popupMenu1;
	private JMenu menu2;
	private JMenuItem createNewTag;
	private JMenuItem removeTagMenuItem;

	// JFormDesigner - End of variables declaration //GEN-END:variables

}

class ArrayComparator implements Comparator<Object[]> {
	private final int columnToSort;

	public ArrayComparator(int columnToSort) {
		this.columnToSort = columnToSort;
	}

	public int compare(Object[] c1, Object[] c2) {
		return c1[columnToSort].toString().compareTo(c2[columnToSort].toString());
	}
}
