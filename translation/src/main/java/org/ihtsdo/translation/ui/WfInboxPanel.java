/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ihtsdo.translation.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.filter.WfDestinationFilter;
import org.ihtsdo.project.filter.WfStateFilter;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.I_TerminologyProject.Type;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.refset.PromotionAndAssignmentRefset;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.project.util.WorkflowSearcher;
import org.ihtsdo.project.view.TranslationHelperPanel;
import org.ihtsdo.project.view.event.EventMediator;
import org.ihtsdo.project.view.event.GenericEvent.EventType;
import org.ihtsdo.project.view.event.NewTagEvent;
import org.ihtsdo.project.view.event.NewTagEventHandler;
import org.ihtsdo.project.view.event.SendBackToInboxEvent;
import org.ihtsdo.project.view.event.TagRemovedEvent;
import org.ihtsdo.project.view.event.TagRemovedEventHandler;
import org.ihtsdo.project.view.tag.FilterFactory;
import org.ihtsdo.project.view.tag.InboxTag;
import org.ihtsdo.project.view.tag.TagManager;
import org.ihtsdo.project.view.tag.WfTagFilter;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.api.WorkflowInterpreter;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfInstance.ActionReport;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.workflow.api.WfFilterBI;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.model.InboxTableModel;
import org.ihtsdo.translation.ui.ConfigTranslationModule.InboxColumn;
import org.ihtsdo.translation.ui.event.ClearAllPanelEvent;
import org.ihtsdo.translation.ui.event.ClearAllPanelEventHandler;
import org.ihtsdo.translation.ui.event.ClearDescriptionPanelEvent;
import org.ihtsdo.translation.ui.event.SaveDescriptionEvent;
import org.ihtsdo.translation.ui.event.SaveDescriptionEvent.ActionType;
import org.ihtsdo.translation.ui.event.SaveDescriptionEventHandler;
import org.ihtsdo.translation.ui.inbox.event.EmptyInboxItemSelectedEvent;
import org.ihtsdo.translation.ui.inbox.event.EmptyInboxItemSelectedEventHandler;
import org.ihtsdo.translation.ui.inbox.event.InboxItemSelectedEvent;
import org.ihtsdo.translation.ui.inbox.event.InboxItemSelectedEventHandler;
import org.ihtsdo.translation.ui.inbox.event.ItemDestinationChangedEvent;
import org.ihtsdo.translation.ui.inbox.event.ItemRemovedFromTodoEvent;
import org.ihtsdo.translation.ui.inbox.event.ItemSentToSpecialFolderEvent;
import org.ihtsdo.translation.ui.inbox.event.ItemStateChangedEvent;
import org.ihtsdo.translation.ui.inbox.event.RestFromToDoEvent;
import org.ihtsdo.translation.ui.translation.TranslationView;
import org.ihtsdo.translation.workflow.filters.WfComponentFilter;
import org.ihtsdo.translation.workflow.filters.WfDefaultDescFilter;
import org.ihtsdo.translation.workflow.filters.WfTargetFsnFilter;
import org.ihtsdo.translation.workflow.filters.WfTargetPreferredFilter;

/**
 * The Class WfInboxPanel.
 */
public class WfInboxPanel extends JPanel {

	/** The Constant tf. */
	private static final I_TermFactory tf = Terms.get();

	/** The config. */
	private static I_ConfigAceFrame config;

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4013056429939416545L;

	/** The model. */
	private InboxTableModel model;

	/** The user. */
	private WfUser user;

	/** The filter list. */
	protected HashMap<String, WfFilterBI> filterList;

	/** The sorter. */
	private TableRowSorter<InboxTableModel> sorter;

	/** The ui panel. */
	private TranslationView uiPanel;

	/** The current row. */
	private Object[] currentRow;

	/** The tag manager. */
	private TagManager tagManager;

	/** The menu item cache. */
	private HashMap<String, InboxTag> menuItemCache = new HashMap<String, InboxTag>();

	/** The expanded. */
	private boolean expanded = false;

	/** The d. */
	private JDialog d;

	/** The cfg. */
	private ConfigTranslationModule cfg;

	/** The current model row num. */
	private int currentModelRowNum;

	/** The special tag. */
	private boolean specialTag;

	/** The new inbox item. */
	protected Object newInboxItem;

	protected boolean todoSelected;

	private int selectedIndex;

	private BatchCommitWorker batchCommitWorker;

	private boolean filterRemoved;

	private boolean presentFilters;

	private WfInstance previousInstance;

	/**
	 * Instantiates a new wf inbox panel.
	 */
	public WfInboxPanel() {
		initComponents();
		try {
			tagManager = TagManager.getInstance();
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
			filterButton.setIcon(IconUtilities.FUNNEL_ADD);
			removeFilters.setIcon(IconUtilities.FUNNEL_DELETE);

			hookLabel.setIcon(IconUtilities.helpIcon);
			hookLabel.setText("");
			filterList = new HashMap<String, WfFilterBI>();
			if (tf != null) {
				config = tf.getActiveAceFrameConfig();
			}
			if (config != null) {
				I_GetConceptData userConcept = config.getDbConfig().getUserConcept();
				user = new WfUser(userConcept.getInitialText(), userConcept.getPrimUuid());
			}
			if (user != null) {
				WfDestinationFilter df = new WfDestinationFilter(user);
				filterList.put(df.getType(), df);
			}
			initTagMenu();
			updateFilters();
			refreshStatusesFilter();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Refresh statuses filter.
	 */
	private void refreshStatusesFilter() {
		List<WfState> states = new WfComponentProvider().getAllStates();
		statusFilterCombo.addItem("");
		for (WfState wfState : states) {
			statusFilterCombo.addItem(wfState);
		}
	}

	/**
	 * Suscribe to events.
	 */
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
				int compCount = menu2.getItemCount();
				if (tag.getUuidList().isEmpty()) {
					for (int i = 0; i < compCount; i++) {
						JMenuItem tagMenu = menu2.getItem(i);
						if (tagMenuString != null && tagMenu != null && tagMenu.getText() != null) {
							if (tagMenu.getText().equals(tagMenuString)) {
								menu2.remove(i);
								break;
							}
						}
					}
				}
			}
		});

		mediator.suscribe(EventType.INBOX_ITEM_SELECTED, new InboxItemSelectedEventHandler<InboxItemSelectedEvent>(this) {
			@Override
			public void handleEvent(InboxItemSelectedEvent event) {
				// currentRow = null;
				newInboxItem = event.getInboxItem();
				if (newInboxItem instanceof InboxTag) {
					InboxTag tag = (InboxTag) newInboxItem;
					if (tag.getTagName().equals(TagManager.TODO)) {
						todoSelected = true;
					} else {
						todoSelected = false;
					}
				}
				WfFilterBI filter = FilterFactory.getInstance().createFilterFromObject(newInboxItem);
				Object oldValue = event.getOldInboxItem();
				if (oldValue != null) {
					WfFilterBI oldFilter = FilterFactory.getInstance().createFilterFromObject(oldValue);
					if (oldFilter != null) {
						filterList.remove(oldFilter.getType());
					}
				}
				if (filter != null) {
					filterList.put(filter.getType(), filter);
				}
				model.updatePage(filterList);
			}
		});

		mediator.suscribe(EventType.EMPTY_INBOX_ITEM_SELECTED, new EmptyInboxItemSelectedEventHandler<EmptyInboxItemSelectedEvent>(this) {
			@Override
			public void handleEvent(EmptyInboxItemSelectedEvent event) {
				Set<String> keys = filterList.keySet();
				List<String> filtersToRemove = new ArrayList<String>();
				for (String string : keys) {
					if (string != new WfDestinationFilter().getType()) {
						filtersToRemove.add(string);
					}
				}
				for (String string2 : filtersToRemove) {
					filterList.remove(string2);
				}
				model.clearTable();
			}
		});

		mediator.suscribe(EventType.CLEAR_ALL, new ClearAllPanelEventHandler<ClearAllPanelEvent>(this) {
			@Override
			public void handleEvent(ClearAllPanelEvent event) {
				currentModelRowNum = 0;
				newInboxItem = null;
				selectedIndex = 0;
				while (model.getRowCount() > 0) {
					model.removeRow(0);
				}
			}
		});

		mediator.suscribe(EventType.SAVE_DESCRIPTION, new SaveDescriptionEventHandler<SaveDescriptionEvent>(this) {
			@Override
			public void handleEvent(SaveDescriptionEvent event) {
				WfInstance previousItem = event.getPreviousItem();
				WfInstance newItem = event.getNewItem();
				ActionType type = event.getType();
				propertyChange(newItem, previousItem, type);
			}
		});
	}

	/**
	 * Inits the tag menu.
	 */
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

	/**
	 * Tag item action preformed.
	 * 
	 * @param e
	 *            the e
	 */
	private void tagItemActionPreformed(ActionEvent e) {
		try {
			int tableRowIndex = inboxTable.getSelectedRow();
			int modelRowIndex = inboxTable.convertRowIndexToModel(tableRowIndex);
			Object[] selectedRow = model.getRow(modelRowIndex);
			WfInstance wfi = (WfInstance) selectedRow[InboxColumn.values().length];
			List<String[]> uuidList = new ArrayList<String[]>();
			String[] uuid = TagManager.getTagWorklistConceptUuids(wfi);
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

	/**
	 * Removes the tag menu item action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void removeTagMenuItemActionPerformed(ActionEvent e) {
		try {
			int tableRowIndex = inboxTable.getSelectedRow();
			int modelRowIndex = inboxTable.convertRowIndexToModel(tableRowIndex);
			Object[] selectedRow = model.getRow(modelRowIndex);
			WfInstance wfi = (WfInstance) selectedRow[InboxColumn.values().length];
			InboxTag tag = model.getTagByUuid(TagManager.getTagWorklistConceptUuids(wfi));
			model.removeTagFromCache(TagManager.getTagWorklistConceptUuids(wfi));
			TagManager.getInstance().removeTag(tag, TagManager.getTagWorklistConceptUuids(wfi));
			if (newInboxItem instanceof InboxTag) {
				InboxTag currentTagSelected = (InboxTag) newInboxItem;
				if (currentTagSelected.getTagName().equals(tag.getTagName())) {
					model.removeRow(modelRowIndex);
				}
			} else {
				model.setValueAt(tf.getConcept(wfi.getComponentId()).toUserString(), modelRowIndex, InboxColumn.SOURCE_PREFERRED.getColumnNumber());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (TerminologyException ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		createAndShowGUI();
	}

	/**
	 * Creates the and show gui.
	 */
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

	/**
	 * Filter button action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void filterButtonActionPerformed(ActionEvent e) {
		updateFilters();
		updateTable();
	}

	/**
	 * Removes the filters action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void removeFiltersActionPerformed(ActionEvent e) {
		presentFilters = false;
		componentFilter.setText("");
		defaultDescText.setText("");
		targetPreferredFilter.setText("");
		targetFsnFilter.setText("");
		statusFilterCombo.setSelectedIndex(0);
		updateFilters();
		updateTable();
	}

	/**
	 * Update table.
	 */
	private void updateTable() {
		Set<String> keys = filterList.keySet();
		for (String key : keys) {
			WfFilterBI filter = filterList.get(key);
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
		model.fireTableDataChanged();
	}

	/**
	 * Update filters.
	 */
	private void updateFilters() {
		presentFilters = false;

		String defaultText = this.defaultDescText.getText();

		WfDefaultDescFilter wfDefDescFilter = new WfDefaultDescFilter("");

		if (!defaultText.equals("")) {
			wfDefDescFilter = new WfDefaultDescFilter(defaultText);
			filterList.put(wfDefDescFilter.getType(), wfDefDescFilter);
			presentFilters = true;
		} else {
			filterList.remove(wfDefDescFilter.getType());
		}

		String componentFilter = this.componentFilter.getText();

		WfComponentFilter wfCompFilter = new WfComponentFilter("");

		if (!componentFilter.equals("")) {
			wfCompFilter = new WfComponentFilter(componentFilter);
			filterList.put(wfCompFilter.getType(), wfCompFilter);
			presentFilters = true;
		} else {
			filterList.remove(wfCompFilter.getType());
		}

		WfTargetPreferredFilter targetFilter = new WfTargetPreferredFilter("");
		if (!targetPreferredFilter.getText().trim().equals("")) {
			targetFilter = new WfTargetPreferredFilter(targetPreferredFilter.getText());
			filterList.put(targetFilter.getType(), targetFilter);
			presentFilters = true;
		} else {
			filterList.remove(targetFilter.getType());
		}

		WfTargetFsnFilter fsnFilter = new WfTargetFsnFilter("");
		if (!targetFsnFilter.getText().trim().equals("")) {
			fsnFilter = new WfTargetFsnFilter(targetFsnFilter.getText());
			filterList.put(fsnFilter.getType(), fsnFilter);
			presentFilters = true;
		} else {
			filterList.remove(fsnFilter.getType());
		}

		WfState state = null;
		if (statusFilterCombo.getSelectedItem() instanceof WfState) {
			state = (WfState) statusFilterCombo.getSelectedItem();
		}

		WfStateFilter wfStateFilter = new WfStateFilter(state);

		if (state != null) {
			filterList.put(wfStateFilter.getType(), wfStateFilter);
			presentFilters = true;
		} else {
			filterList.remove(wfStateFilter.getType());
		}
	}

	/**
	 * Inbox table mouse clicked.
	 * 
	 * @param e
	 *            the e
	 */
	private void inboxTableMouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			int xPoint = e.getX();
			int yPoint = e.getY();
			int row = inboxTable.rowAtPoint(new Point(xPoint, yPoint));
			inboxTable.setRowSelectionInterval(row, row);

			if (row > -1) {
				// Check if the item is already tagged
				int modelRowIndex = inboxTable.convertRowIndexToModel(row);
				Object[] selectedRow = model.getRow(modelRowIndex);
				WfInstance wfi = (WfInstance) selectedRow[InboxColumn.values().length];
				InboxTag tag = model.getTagByUuid(TagManager.getTagWorklistConceptUuids(wfi));
				if (tag == null) {
					menu2.setEnabled(true);
					removeTagMenuItem.setEnabled(false);
					backToInbox.setEnabled(false);
				} else if (tag.getTagName().equals(TagManager.OUTBOX) || tag.getTagName().equals(TagManager.TODO)) {
					menu2.setEnabled(false);
					removeTagMenuItem.setEnabled(false);
					backToInbox.setEnabled(true);
				} else {
					menu2.setEnabled(false);
					removeTagMenuItem.setEnabled(true);
					backToInbox.setEnabled(false);
				}
				popupMenu1.show(inboxTable, xPoint, yPoint);
			}
		} else if (e.getClickCount() == 2) {
			openItem();
		}
	}

	private void openItem() {
		openItem(true);
	}

	/**
	 * Open item.
	 */
	private void openItem(boolean checkUncommited) {
		int previousIndex = selectedIndex;
		selectedIndex = inboxTable.getSelectedRow();
		if (selectedIndex >= 0) {
			try {
				cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());

				if (previousInstance != null) {
					I_GetConceptData previousConcept = Terms.get().getConcept(previousInstance.getComponentId());
					Collection<? extends ConceptChronicleBI> uncommited = Ts.get().getUncommittedConcepts();
					boolean isUncommited = false;
					for (ConceptChronicleBI conceptChronicleBI : uncommited) {
						if (conceptChronicleBI.getConceptNid() == previousConcept.getConceptNid()) {
							isUncommited = true;
							break;
						}
					}
					if (isUncommited && checkUncommited) {
						Object[] options = { "Commit concept", "Cancel concept", "Stay in concept" };
						int n = JOptionPane.showOptionDialog(this, "Uncommited concept in editor", "Uncommited", JOptionPane.YES_NO_CANCEL_OPTION,
								JOptionPane.WARNING_MESSAGE, null, options, options[2]);
						if (n == JOptionPane.YES_OPTION) {
							boolean commited = previousConcept.commit(ChangeSetGenerationPolicy.INCREMENTAL,
									ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
							if (!commited) {
								returnToPreviousItem(previousIndex);
							}
						} else if (n == JOptionPane.NO_OPTION) {
							previousConcept.cancel();
							Ts.get().cancel();
						} else {
							returnToPreviousItem(previousIndex);
						}
					}
				}
				currentModelRowNum = inboxTable.convertRowIndexToModel(selectedIndex);
				previousInstance = (WfInstance) model.getValueAt(currentModelRowNum, InboxColumn.values().length);

				UUID memberUUid = previousInstance.getComponentId();
				String memberStringUUid = previousInstance.getComponentId().toString();

				I_TerminologyProject project = TerminologyProjectDAO.getProjectForWorklist(previousInstance.getWorkList(), config);

				JTabbedPane tpc = ((AceFrameConfig) config).getAceFrame().getCdePanel().getConceptTabs();
				if (project.getProjectType().compareTo(Type.TRANSLATION) != 0) {
					I_HostConceptPlugins viewer = config.getConceptViewer(1);
					I_GetConceptData concept = Terms.get().getConcept(memberUUid);
					if (concept != null) {
						viewer.setTermComponent(concept);

						int tabCount = tpc.getTabCount();
						for (int i = 0; i < tabCount; i++) {
							if (tpc.getTitleAt(i).equals("arena")) {
								tpc.setSelectedIndex(i);
							}
						}
						// JOptionPane pane = new
						// JOptionPane("The concept was sent to arena",
						// JOptionPane.INFORMATION_MESSAGE);
						// JDialog dialog = pane.createDialog(this,
						// "Sent concept");
						//
						// dialog.setVisible(true);
						// pane.setVisible(true);
						// Thread.sleep(2000);
						// dialog.setVisible(false);
						// pane.setVisible(false);
						// dialog.dispose();
						// dialog=null;
						// pane=null;

					}
					return;
				}

				if (uiPanel == null) {
					uiPanel = new TranslationView();
					tpc.addTab(TranslationHelperPanel.TRANSLATION_TAB_NAME, uiPanel);
					tpc.setSelectedIndex(tpc.getTabCount() - 1);
				} else if (tpc != null) {
					int tabCount = tpc.getTabCount();
					for (int i = 0; i < tabCount; i++) {
						if (tpc.getTitleAt(i).equals(TranslationHelperPanel.TRANSLATION_TAB_NAME)) {
							if (tpc.getComponentAt(i) instanceof TranslationView) {
								uiPanel = (TranslationView) tpc.getComponentAt(i);
								tpc.setSelectedIndex(i);
							}
							break;
						}
					}
				}
				boolean readOnly = false;

				InboxTag outboxContent = tagManager.getTagContent(TagManager.OUTBOX);
				List<String[]> uuidList = outboxContent.getUuidList();

				UUID worklistUuid = previousInstance.getWorkList().getUids().get(0);
				boolean outbox = false;
				for (String[] strings : uuidList) {
					if (strings[0].equals(worklistUuid.toString()) && strings[1].equals(memberStringUUid)) {
						readOnly = true;
						outbox = true;
						break;
					}
				}
				uiPanel.updateUi(previousInstance, readOnly, outbox);
				currentRow = model.getRow(currentModelRowNum);
				currentTranslationItem.setText(currentRow[InboxColumn.SOURCE_PREFERRED.getColumnNumber()].toString());
			} catch (IndexOutOfBoundsException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (TerminologyException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		} else {
			AceFrameConfig config;
			try {
				config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();
				AceFrame ace = config.getAceFrame();

				JTabbedPane tpc = ace.getCdePanel().getLeftTabs();
				int tabCount = tpc.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tpc.getTitleAt(i).equals(TranslationHelperPanel.TRANSLATION_TAB_NAME)) {
						tpc.remove(i);
						tpc.repaint();
						tpc.revalidate();
						break;
					}

				}
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void returnToPreviousItem(int previousIndex) {
		if (previousIndex > 0 && previousIndex < inboxTable.getRowCount()) {
			inboxTable.setRowSelectionInterval(previousIndex, previousIndex);
			selectedIndex = previousIndex;
			return;
		} else if (inboxTable.getRowCount() >= 0) {
			inboxTable.setRowSelectionInterval(0, 0);
			selectedIndex = 0;
			return;
		}
	}

	private void propertyChange(WfInstance newWfInstance, WfInstance oldWfInstance, ActionType type) {
		if (type.equals(ActionType.ACTION_LAUNCHED)) {
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
							if (!newWfInstance.isCompleted() && newWfInstance.isActive()) {
								tagManager.sendToOutbox(TagManager.getTagWorklistConceptUuids(newWfInstance));
							}
							EventMediator.getInstance().fireEvent(new ItemDestinationChangedEvent(newWfInstance));
							inboxTreePanel1.itemStateChanged(oldWfInstance, newWfInstance);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					break;
				case SAVE_AS_TODO:
					try {
						tagManager.saveAsToDo(TagManager.getTagWorklistConceptUuids((WfInstance) currentRow[InboxColumn.values().length]));
						EventMediator.getInstance().fireEvent(new ItemSentToSpecialFolderEvent(newWfInstance, oldWfInstance));
						int currentItemViewIndex = 0;
						try {
							currentItemViewIndex = inboxTable.convertRowIndexToView(currentModelRowNum);
						} catch (Exception e) {
						}
						if (!specialTag) {
							WfInstance efi = (WfInstance) model.getRow(currentModelRowNum)[InboxColumn.values().length];
							if (model.getRowCount() > currentModelRowNum && efi.getComponentId().equals(oldWfInstance.getComponentId())) {
								model.removeRow(currentModelRowNum);
							}
							if (inboxTable.getRowCount() > 0 && inboxTable.getRowCount() > currentItemViewIndex) {
								inboxTable.setRowSelectionInterval(currentItemViewIndex, currentItemViewIndex);
							} else if (inboxTable.getRowCount() <= currentItemViewIndex && inboxTable.getRowCount() > 0) {
								inboxTable.setRowSelectionInterval(inboxTable.getRowCount() - 1, inboxTable.getRowCount() - 1);
							} else if (inboxTable.getRowCount() == 0) {
								EventMediator.getInstance().fireEvent(new ClearAllPanelEvent());
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case OUTBOX:
					try {
						if (!newWfInstance.isCompleted() && newWfInstance.isActive()) {
							tagManager.sendToOutbox(TagManager.getTagWorklistConceptUuids((WfInstance) currentRow[InboxColumn.values().length]));
						}
						EventMediator.getInstance().fireEvent(new ItemSentToSpecialFolderEvent(newWfInstance, oldWfInstance));
						int currentItemViewIndex = 0;
						try {
							currentItemViewIndex = inboxTable.convertRowIndexToView(currentModelRowNum);
						} catch (Exception e) {
						}

						if (todoSelected
								&& TagManager.getInstance().getTagContent(TagManager.TODO).getUuidList()
										.contains(oldWfInstance.getComponentId().toString())) {
							EventMediator.getInstance().fireEvent(new RestFromToDoEvent());
						}
						WfInstance efi = (WfInstance) model.getRow(currentModelRowNum)[InboxColumn.values().length];
						if (model.getRowCount() > currentModelRowNum && efi.getComponentId().equals(oldWfInstance.getComponentId())) {
							model.removeRow(currentModelRowNum);
						}
						if (inboxTable.getRowCount() > 0 && inboxTable.getRowCount() > currentItemViewIndex) {
							inboxTable.setRowSelectionInterval(currentItemViewIndex, currentItemViewIndex);
						} else if (inboxTable.getRowCount() <= currentItemViewIndex && inboxTable.getRowCount() > 0) {
							inboxTable.setRowSelectionInterval(inboxTable.getRowCount() - 1, inboxTable.getRowCount() - 1);
						} else if (inboxTable.getRowCount() == 0) {
							EventMediator.getInstance().fireEvent(new ClearAllPanelEvent());
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				default:
					break;
				}
			}
			if (cfg.isAutoOpenNextInboxItem()) {
				if (selectedIndex + 1 < inboxTable.getRowCount()) {
					inboxTable.setRowSelectionInterval(selectedIndex, selectedIndex);
				}
				openItem();
			} else {
				EventMediator.getInstance().fireEvent(new ClearDescriptionPanelEvent(true));
			}
		} else if (type.equals(ActionType.SEND_TO_OUTBOX_LAUNCHED)) {
			try {
				if (!newWfInstance.isCompleted() && newWfInstance.isActive()) {
					tagManager.sendToOutbox(TagManager.getTagWorklistConceptUuids((WfInstance) currentRow[InboxColumn.values().length]));
				}
				EventMediator.getInstance().fireEvent(new ItemSentToSpecialFolderEvent(oldWfInstance, oldWfInstance));
				int currentItemViewIndex = 0;
				try {
					currentItemViewIndex = inboxTable.convertRowIndexToView(currentModelRowNum);
				} catch (Exception e) {
				}
				WfInstance efi = (WfInstance) model.getRow(currentModelRowNum)[InboxColumn.values().length];
				if (model.getRowCount() > currentModelRowNum && efi.getComponentId().equals(oldWfInstance.getComponentId())) {
					model.removeRow(currentModelRowNum);
				}
				if (inboxTable.getRowCount() > 0 && inboxTable.getRowCount() > currentItemViewIndex) {
					inboxTable.setRowSelectionInterval(currentItemViewIndex, currentItemViewIndex);
				} else if (inboxTable.getRowCount() <= currentItemViewIndex && inboxTable.getRowCount() > 0) {
					inboxTable.setRowSelectionInterval(inboxTable.getRowCount() - 1, inboxTable.getRowCount() - 1);
				} else if (inboxTable.getRowCount() == 0) {
					EventMediator.getInstance().fireEvent(new ClearAllPanelEvent());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (type.equals(ActionType.NEXT_ITEM) || type.equals(ActionType.NO_ACTION)) {
			int currentItemViewIndex = 0;
			try {
				currentItemViewIndex = inboxTable.convertRowIndexToView(currentModelRowNum);
			} catch (Exception e) {
			}
			if (inboxTable.getRowCount() > 0 && inboxTable.getRowCount() > currentItemViewIndex + 1) {
				inboxTable.setRowSelectionInterval(currentItemViewIndex + 1, currentItemViewIndex + 1);
			} else if (inboxTable.getRowCount() <= currentItemViewIndex && inboxTable.getRowCount() > 0) {
				inboxTable.setRowSelectionInterval(inboxTable.getRowCount() - 1, inboxTable.getRowCount() - 1);
			} else if (inboxTable.getRowCount() == 0) {
				EventMediator.getInstance().fireEvent(new ClearAllPanelEvent());
			}
			if (cfg.isAutoOpenNextInboxItem()) {
				openItem(false);
			} else {
				EventMediator.getInstance().fireEvent(new ClearDescriptionPanelEvent(true));
			}
		}
	}

	/**
	 * Creates the new tag action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void createNewTagActionPerformed(ActionEvent e) {
		NewTagPanel tagPanel = new NewTagPanel();
		InboxTag tag = tagPanel.showModalDialog();
		if (tag != null) {
			try {
				int tableRowIndex = inboxTable.getSelectedRow();
				int modelRowIndex = inboxTable.convertRowIndexToModel(tableRowIndex);
				Object[] selectedRow = model.getRow(modelRowIndex);
				WfInstance wfi = (WfInstance) selectedRow[InboxColumn.values().length];
				List<String[]> uuidList = new ArrayList<String[]>();
				String[] uuid = TagManager.getTagWorklistConceptUuids(wfi);
				uuidList.add(uuid);
				tag.setUuidList(uuidList);
				TagManager.getInstance().createTag(tag);
				model.addTagToCache(uuid, tag);
				model.setValueAt(TagManager.getInstance().getHeader(tag) + selectedRow[InboxColumn.SOURCE_PREFERRED.getColumnNumber()],
						modelRowIndex, InboxColumn.SOURCE_PREFERRED.getColumnNumber());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Close inbox action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void closeInboxActionPerformed(ActionEvent e) {
		try {
			AceFrameConfig config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();
			AceFrame ace = config.getAceFrame();

			JTabbedPane tpc = ace.getCdePanel().getLeftTabs();
			if (tpc != null) {
				int tabCount = tpc.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tpc.getTitleAt(i).equals(TranslationHelperPanel.TRANSLATION_LEFT_MENU)) {
						int previousIndex = selectedIndex;
						selectedIndex = inboxTable.getSelectedRow();
						if (selectedIndex >= 0) {
							try {
								cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());

								if (previousIndex > 0 && previousIndex < inboxTable.getRowCount()) {
									int previousModelRowNum = inboxTable.convertRowIndexToModel(previousIndex);
									WfInstance previousInstance = (WfInstance) model.getValueAt(previousModelRowNum, InboxColumn.values().length);
									I_GetConceptData previousConcept = Terms.get().getConcept(previousInstance.getComponentId());
									Collection<? extends ConceptChronicleBI> uncommited = Ts.get().getUncommittedConcepts();
									boolean isUncommited = false;
									for (ConceptChronicleBI conceptChronicleBI : uncommited) {
										if (conceptChronicleBI.getConceptNid() == previousConcept.getConceptNid()) {
											isUncommited = true;
											break;
										}
									}
									if (isUncommited) {
										Object[] options = { "Commit concept", "Cancel concept", "Stay in concept" };
										int n = JOptionPane.showOptionDialog(this, "Uncommited concept in editor", "Uncommited",
												JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[2]);
										if (n == JOptionPane.YES_OPTION) {
											previousConcept.commit(ChangeSetGenerationPolicy.INCREMENTAL,
													ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
										} else if (n == JOptionPane.NO_OPTION) {
											previousConcept.cancel();
											Ts.get().cancel();
										} else if (n == JOptionPane.CANCEL_OPTION) {
											inboxTable.setRowSelectionInterval(previousIndex, previousIndex);
											selectedIndex = previousIndex;
											return;
										} else {
											inboxTable.setRowSelectionInterval(previousIndex, previousIndex);
											selectedIndex = previousIndex;
											return;
										}
									}
								}
							} catch (Exception ex) {
								ex.printStackTrace();
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
				int tabCount = tpc.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tpc.getTitleAt(i).equals(TranslationHelperPanel.SIMILARITY_TAB_NAME)) {
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
						if (tpc.getComponentAt(i) instanceof TranslationPanel) {
							TranslationPanel uiPanel = (TranslationPanel) tpc.getComponentAt(i);
							if (!uiPanel.verifySavePending(null, false, false)) {
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
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Check box1 action performed.
	 * 
	 * @param e
	 *            the e
	 */
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

	/**
	 * Empty action performed.
	 * 
	 * @param e
	 *            the event
	 */
	private void emptyOutboxActionPerformed(ActionEvent e) {
		try {
			WorkflowSearcher searcher = new WorkflowSearcher();
			Collection<WfFilterBI> collection = new ArrayList<WfFilterBI>();
			InboxTag tag = TagManager.getInstance().getTagContent(TagManager.OUTBOX);
			collection.add(new WfTagFilter(tag));
			List<WfInstance> wfInstances = searcher.searchWfInstances(collection);
			for (WfInstance instance : wfInstances) {
				WorkflowInterpreter interpreter = WorkflowInterpreter.createWorkflowInterpreter(instance.getWfDefinition());
				WfUser nextDestination = interpreter.getNextDestination(instance, instance.getWorkList());
				WfInstance.updateDestination(instance, nextDestination);
			}
			if (batchCommitWorker != null && !batchCommitWorker.isDone()) {
				batchCommitWorker.cancel(true);
				batchCommitWorker = null;
			}
			batchCommitWorker = new BatchCommitWorker(wfInstances);
			batchCommitWorker.addPropertyChangeListener(new ProgressListener(progressBar1));
			batchCommitWorker.execute();

			TagManager.getInstance().emptyOutboxTag();
			inboxTreePanel1.updateTree();
			model.clearTable();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	class BatchCommitWorker extends SwingWorker<String, String> {
		private List<WfInstance> wfInstances;

		public BatchCommitWorker(List<WfInstance> wfInstances) {
			this.wfInstances = wfInstances;
		}

		@Override
		protected String doInBackground() throws Exception {
			I_TermFactory tf = Terms.get();
			for (WfInstance instance : wfInstances) {
				tf.addUncommittedNoChecks(tf.getConcept(instance.getComponentId()));
			}
			tf.commit();
			return null;
		}

	}

	/**
	 * Back to inbox action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void backToInboxActionPerformed(ActionEvent e) {
		try {

			int tableRowIndex = inboxTable.getSelectedRow();
			int modelRowIndex = inboxTable.convertRowIndexToModel(tableRowIndex);
			Object[] selectedRow = model.getRow(modelRowIndex);
			WfInstance wfi = (WfInstance) selectedRow[InboxColumn.values().length];
			InboxTag tag = model.getTagByUuid(TagManager.getTagWorklistConceptUuids(wfi));
			if (tag.getTagName().equals(TagManager.OUTBOX)) {
				WorkList workList = wfi.getWorkList();
				PromotionAndAssignmentRefset promotionRefset = workList.getPromotionRefset(config);
				I_Identify nid = Terms.get().getId(wfi.getComponentId());
				I_GetConceptData prevStatus = promotionRefset.getPreviousPromotionStatus(nid.getConceptNid(), config);
				WfState prevState = new WfComponentProvider().statusConceptToWfState(prevStatus);
				WfInstance.updateInstanceState(wfi, prevState);
				Terms.get().getConcept(wfi.getComponentId())
						.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
				EventMediator.getInstance().fireEvent(new SendBackToInboxEvent(wfi));
			} else if (tag.getTagName().equals(TagManager.TODO)) {
				EventMediator.getInstance().fireEvent(new ItemRemovedFromTodoEvent(wfi));
			}
			model.removeRow(modelRowIndex);
			model.removeTagFromCache(TagManager.getTagWorklistConceptUuids(wfi));
			TagManager.getInstance().removeTag(tag, TagManager.getTagWorklistConceptUuids(wfi));
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (TerminologyException ex) {
			ex.printStackTrace();
		} catch (Exception e6) {
			e6.printStackTrace();
		}
	}

	/**
	 * Label5 mouse clicked.
	 * 
	 * @param e
	 *            the e
	 */
	private void label5MouseClicked(MouseEvent e) {
		if (!expanded) {
			InboxColumn[] columns = InboxColumn.values();
			LinkedHashSet<InboxColumn> allColumns = new LinkedHashSet<ConfigTranslationModule.InboxColumn>();
			for (InboxColumn inboxColumn : columns) {
				allColumns.add(inboxColumn);
			}
			model.setColumns(allColumns);
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
			d.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					atachToInboxPanel();
				}
			});
			expanded = true;
			label5.setText("- Attach");
			label5.setToolTipText("Click to attach back to inbox.");
			model.fireTableStructureChanged();
		} else {
			atachToInboxPanel();
		}
	}

	/**
	 * Atach to inbox panel.
	 */
	private void atachToInboxPanel() {
		filterPanel.setVisible(false);
		model.refreshColumnsStruct();
		splitPanel.setResizeWeight(0.5d);
		if (!presentFilters) {
			label5.setText("+ Detach");
		} else {
			label5.setText("+ Detach (filter applied)");
		}
		label5.setToolTipText("Click to detach and view filters.");
		splitPanel.setBottomComponent(d.getContentPane());
		d.dispose();
		expanded = false;
		model.fireTableStructureChanged();
	}

	private void refreshButtonActionPerformed(ActionEvent e) {
		refresh();
	}

	public void refresh() {
		inboxTreePanel1.updateTree();
		model.clearTable();
	}

	private void hookLabelMouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("TRANSLATION_INBOX");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Inits the components.
	 */
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
		panel6 = new JPanel();
		separator2 = new JSeparator();
		label5 = new JLabel();
		separator3 = new JSeparator();
		filterPanel = new JPanel();
		panel3 = new JPanel();
		label4 = new JLabel();
		label2 = new JLabel();
		label3 = new JLabel();
		label6 = new JLabel();
		componentFilter = new JTextField();
		targetPreferredFilter = new JTextField();
		targetFsnFilter = new JTextField();
		statusFilterCombo = new JComboBox();
		panel5 = new JPanel();
		filterButton = new JButton();
		removeFilters = new JButton();
		label1 = new JLabel();
		currentTranslationItem = new JLabel();
		checkBox1 = new JCheckBox();
		panel4 = new JPanel();
		closeInbox = new JButton();
		refreshButton = new JButton();
		sendToOutbox = new JButton();
		hookLabel = new JLabel();
		popupMenu1 = new JPopupMenu();
		menu2 = new JMenu();
		createNewTag = new JMenuItem();
		removeTagMenuItem = new JMenuItem();
		backToInbox = new JMenuItem();

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
			panel2.add(progressBar1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,
					0, 0, 0), 0, 0));
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
					((GridBagLayout) panel1.getLayout()).columnWidths = new int[] { 35, 0, 0, 0 };
					((GridBagLayout) panel1.getLayout()).rowHeights = new int[] { 15, 6, 0, 0, 0 };
					((GridBagLayout) panel1.getLayout()).columnWeights = new double[] { 0.0, 1.0, 1.0, 1.0E-4 };
					((GridBagLayout) panel1.getLayout()).rowWeights = new double[] { 0.0, 1.0, 1.0, 0.0, 1.0E-4 };

					// ======== panel6 ========
					{
						panel6.setLayout(new GridBagLayout());
						((GridBagLayout) panel6.getLayout()).columnWidths = new int[] { 0, 0, 0, 0 };
						((GridBagLayout) panel6.getLayout()).rowHeights = new int[] { 0, 0 };
						((GridBagLayout) panel6.getLayout()).columnWeights = new double[] { 0.0, 1.0, 1.0, 1.0E-4 };
						((GridBagLayout) panel6.getLayout()).rowWeights = new double[] { 1.0, 1.0E-4 };

						// ---- separator2 ----
						separator2.setMaximumSize(new Dimension(40, 10));
						separator2.setPreferredSize(new Dimension(35, 12));
						panel6.add(separator2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
								new Insets(0, 0, 0, 0), 0, 0));

						// ---- label5 ----
						label5.setText("+ Detach");
						label5.setForeground(new Color(0, 0, 204));
						label5.setVerticalAlignment(SwingConstants.TOP);
						label5.setFont(label5.getFont().deriveFont(label5.getFont().getStyle() | Font.ITALIC));
						label5.setToolTipText("Click to detach and view filters.");
						label5.setMaximumSize(new Dimension(70, 16));
						label5.setMinimumSize(new Dimension(400, 16));
						label5.setPreferredSize(new Dimension(70, 16));
						label5.setHorizontalTextPosition(SwingConstants.CENTER);
						label5.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								label5MouseClicked(e);
							}
						});
						panel6.add(label5, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
								new Insets(0, 0, 0, 0), 0, 0));

						// ---- separator3 ----
						separator3.setMaximumSize(new Dimension(32767, 10));
						panel6.add(separator3, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
								new Insets(0, 0, 0, 0), 0, 0));
					}
					panel1.add(panel6, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,
							0, 5, 0), 0, 0));

					// ======== filterPanel ========
					{
						filterPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
						filterPanel.setLayout(new BorderLayout(5, 5));

						// ======== panel3 ========
						{
							panel3.setLayout(new GridBagLayout());
							((GridBagLayout) panel3.getLayout()).columnWidths = new int[] { 0, 0, 0, 0, 0, 0 };
							((GridBagLayout) panel3.getLayout()).rowHeights = new int[] { 0, 0, 0 };
							((GridBagLayout) panel3.getLayout()).columnWeights = new double[] { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0E-4 };
							((GridBagLayout) panel3.getLayout()).rowWeights = new double[] { 0.0, 0.0, 1.0E-4 };

							lblDefaultDescription = new JLabel("Default description");
							GridBagConstraints gbc_lblDefaultDescription = new GridBagConstraints();
							gbc_lblDefaultDescription.insets = new Insets(0, 0, 5, 5);
							gbc_lblDefaultDescription.gridx = 0;
							gbc_lblDefaultDescription.gridy = 0;
							panel3.add(lblDefaultDescription, gbc_lblDefaultDescription);

							// ---- label4 ----
							label4.setText("Source preferred");
							panel3.add(label4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 5, 5), 0, 0));

							// ---- label2 ----
							label2.setText("Target preferred");
							panel3.add(label2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 5, 5), 0, 0));

							// ---- label3 ----
							label3.setText("Target FSN");
							panel3.add(label3, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 5, 5), 0, 0));

							// ---- label6 ----
							label6.setText("Status");
							panel3.add(label6, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 5, 0), 0, 0));

							defaultDescText = new JTextField();
							GridBagConstraints gbc_defaultDescText = new GridBagConstraints();
							gbc_defaultDescText.insets = new Insets(0, 0, 0, 5);
							gbc_defaultDescText.fill = GridBagConstraints.HORIZONTAL;
							gbc_defaultDescText.gridx = 0;
							gbc_defaultDescText.gridy = 1;
							panel3.add(defaultDescText, gbc_defaultDescText);
							defaultDescText.setColumns(10);
							panel3.add(componentFilter, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
									GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
							panel3.add(targetPreferredFilter, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
									GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
							panel3.add(targetFsnFilter, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
									GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
							panel3.add(statusFilterCombo, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
									GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
						}
						filterPanel.add(panel3, BorderLayout.CENTER);

						// ======== panel5 ========
						{
							panel5.setLayout(new GridBagLayout());
							((GridBagLayout) panel5.getLayout()).columnWidths = new int[] { 0, 0, 0 };
							((GridBagLayout) panel5.getLayout()).rowHeights = new int[] { 0, 0 };
							((GridBagLayout) panel5.getLayout()).columnWeights = new double[] { 1.0, 0.0, 1.0E-4 };
							((GridBagLayout) panel5.getLayout()).rowWeights = new double[] { 1.0, 1.0E-4 };

							// ---- filterButton ----
							filterButton.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									filterButtonActionPerformed(e);
								}
							});
							panel5.add(filterButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
									new Insets(0, 0, 0, 5), 0, 0));

							// ---- removeFilters ----
							removeFilters.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									removeFiltersActionPerformed(e);
								}
							});
							panel5.add(removeFilters, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
									GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						}
						filterPanel.add(panel5, BorderLayout.SOUTH);
					}
					panel1.add(filterPanel, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

					// ---- label1 ----
					label1.setText("Current item:");
					panel1.add(label1, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,
							0, 5, 5), 0, 0));
					panel1.add(currentTranslationItem, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

					// ---- checkBox1 ----
					checkBox1.setText("Automatically open next item in inbox after finishing with current item");
					checkBox1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							checkBox1ActionPerformed(e);
						}
					});
					panel1.add(checkBox1, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
				}
				inboxItems.add(panel1, BorderLayout.NORTH);
			}
			splitPanel.setBottomComponent(inboxItems);
		}
		add(splitPanel, BorderLayout.CENTER);

		// ======== panel4 ========
		{
			panel4.setLayout(new GridBagLayout());
			((GridBagLayout) panel4.getLayout()).columnWidths = new int[] { 0, 0, 0, 0, 0 };
			((GridBagLayout) panel4.getLayout()).rowHeights = new int[] { 0, 0 };
			((GridBagLayout) panel4.getLayout()).columnWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 1.0E-4 };
			((GridBagLayout) panel4.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

			// ---- closeInbox ----
			closeInbox.setText("Close");
			closeInbox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					closeInboxActionPerformed(e);
				}
			});
			panel4.add(closeInbox, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0,
					0, 0, 5), 0, 0));

			// ---- refreshButton ----
			refreshButton.setText("Refresh");
			refreshButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					refreshButtonActionPerformed(e);
				}
			});
			panel4.add(refreshButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,
					0, 0, 5), 0, 0));

			// ---- sendToOutbox ----
			sendToOutbox.setText("Send");
			sendToOutbox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					emptyOutboxActionPerformed(e);
				}
			});
			panel4.add(sendToOutbox, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,
					0, 0, 5), 0, 0));

			// ---- hookLabel ----
			hookLabel.setText("text");
			hookLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					hookLabelMouseClicked(e);
				}
			});
			panel4.add(hookLabel, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0,
					0, 0), 0, 0));
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

			// ---- backToInbox ----
			backToInbox.setText("Send back to inbox");
			backToInbox.setEnabled(false);
			backToInbox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					backToInboxActionPerformed(e);
				}
			});
			popupMenu1.add(backToInbox);
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
	private JPanel panel6;
	private JSeparator separator2;
	private JLabel label5;
	private JSeparator separator3;
	private JPanel filterPanel;
	private JPanel panel3;
	private JLabel label4;
	private JLabel label2;
	private JLabel label3;
	private JLabel label6;
	private JTextField componentFilter;
	private JTextField targetPreferredFilter;
	private JTextField targetFsnFilter;
	private JComboBox statusFilterCombo;
	private JPanel panel5;
	private JButton filterButton;
	private JButton removeFilters;
	private JLabel label1;
	private JLabel currentTranslationItem;
	private JCheckBox checkBox1;
	private JPanel panel4;
	private JButton closeInbox;
	private JButton refreshButton;
	private JButton sendToOutbox;
	private JLabel hookLabel;
	private JPopupMenu popupMenu1;
	private JMenu menu2;
	private JMenuItem createNewTag;
	private JMenuItem removeTagMenuItem;
	private JMenuItem backToInbox;
	private JLabel lblDefaultDescription;
	private JTextField defaultDescText;
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
