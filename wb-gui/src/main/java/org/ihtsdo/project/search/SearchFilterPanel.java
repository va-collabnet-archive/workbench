/*
 * Created by JFormDesigner on Tue Jan 08 18:34:34 GMT-03:00 2013
 */

package org.ihtsdo.project.search;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.filter.WfCompletionFilter;
import org.ihtsdo.project.filter.WfCompletionFilter.CompletionOption;
import org.ihtsdo.project.filter.WfDestinationFilter;
import org.ihtsdo.project.filter.WfIsKindOfFilter;
import org.ihtsdo.project.filter.WfProjectFilter;
import org.ihtsdo.project.filter.WfStateFilter;
import org.ihtsdo.project.filter.WfStringFilter;
import org.ihtsdo.project.filter.WfWorklistFilter;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.qa.gui.ObjectTransferHandler;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.workflow.api.WfFilterBI;
import org.ihtsdo.tk.workflow.api.WfStateBI;
import org.ihtsdo.tk.workflow.api.WfUserBI;
import javax.swing.JTextField;
import java.awt.Dimension;

/**
 * @author Guillermo Reynoso
 */
public class SearchFilterPanel extends JPanel {
	private static final long serialVersionUID = 3331067536194480444L;
	private WFSearchFilterContainerBI container;

	public SearchFilterPanel() {
		initComponents();
	}

	public SearchFilterPanel(WFSearchFilterContainerBI wfInstanceSearchPanel) {
		initComponents();
		this.container = wfInstanceSearchPanel;
		addButton.setIcon(new ImageIcon(ACE.class.getResource("/16x16/plain/add2.png")));
		removeButton.setIcon(new ImageIcon(ACE.class.getResource("/16x16/plain/delete2.png")));

		filterTypeCombo.addItem("");
		filterTypeCombo.addItem(new WfDestinationFilter());
		filterTypeCombo.addItem(new WfWorklistFilter());
		filterTypeCombo.addItem(new WfProjectFilter());
		filterTypeCombo.addItem(new WfStateFilter());
		filterTypeCombo.addItem(new WfCompletionFilter());
		filterTypeCombo.addItem(new WfIsKindOfFilter());
		filterTypeCombo.addItem(new WfStringFilter());
	}

	public SearchFilterPanel(WFSearchFilterContainerBI wfInstanceSearchPanel, List<WfFilterBI> filters) {
		initComponents();
		this.container = wfInstanceSearchPanel;
		addButton.setIcon(new ImageIcon(ACE.class.getResource("/16x16/plain/add2.png")));
		removeButton.setIcon(new ImageIcon(ACE.class.getResource("/16x16/plain/delete2.png")));

		filterTypeCombo.addItem("");
		for (WfFilterBI wfFilterBI : filters) {
			filterTypeCombo.addItem(wfFilterBI);
		}
	}

	private void addButtonActionPerformed(ActionEvent e) {
		container.addNewFilterPanel();
	}

	private void removeButtonActionPerformed(ActionEvent e) {
		container.removeFilterPanel(this);
	}

	public WfFilterBI getWfFilter() {
		Object filterObject = null;
		if (filterTypeCombo.getSelectedItem() instanceof WfStringFilter) {
			filterObject = textField.getText();
		} else {
			filterObject = filterCombo.getSelectedItem();
		}
		if (filterObject instanceof WfUser) {
			return new WfDestinationFilter((WfUser) filterCombo.getSelectedItem());
		} else if (filterObject instanceof WorkList) {
			return new WfWorklistFilter(((WorkList) filterCombo.getSelectedItem()).getUuid());
		} else if (filterObject instanceof WfState) {
			return new WfStateFilter((WfState) filterCombo.getSelectedItem());
		} else if (filterObject instanceof I_TerminologyProject) {
			UUID uid = ((I_TerminologyProject) filterCombo.getSelectedItem()).getUids().iterator().next();
			return new WfProjectFilter(uid);
		} else if (filterObject instanceof CompletionOption) {
			CompletionOption co = (CompletionOption) filterCombo.getSelectedItem();
			return new WfCompletionFilter(co);
		} else if (filterObject instanceof String) {
			String string = textField.getText();
			if (!string.trim().equals("")) {
				return new WfStringFilter(string);
			}
		} else if (filterObject instanceof ConceptVersionBI) {
			ConceptVersionBI parentConcept = (ConceptVersionBI) filterCombo.getSelectedItem();
			return new WfIsKindOfFilter(parentConcept.getConceptNid());
		} else {
			return null;
		}
		return null;
	}

	class ComboBoxRenderer extends JLabel implements ListCellRenderer<Object> {
		private static final long serialVersionUID = -1305398231576147755L;

		public ComboBoxRenderer() {
			setOpaque(true);
		}

		/*
		 * This method finds the image and text corresponding to the selected
		 * value and returns the label, set up to display the text and image.
		 */
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			// Get the selected index. (The index param isn't
			// always valid, so just use the value.)
			ConceptVersionBI selectedIndex = (ConceptVersionBI) value;
			if (selectedIndex != null) {
				setText(selectedIndex.toUserString());
			}
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			// Set the icon and text. If icon was null, say so.
			return this;
		}
	}

	private void filterTypeComboItemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			filterCombo.setVisible(true);
			textField.setVisible(false);
			filterCombo.setTransferHandler(null);
			filterCombo.setRenderer(new JComboBox<>().getRenderer());
			if (e.getItem() instanceof WfDestinationFilter) {
				filterCombo.removeAllItems();
				WfDestinationFilter df = (WfDestinationFilter) e.getItem();
				List<WfUser> fitlerOptions = df.getFilterOptions();
				Collections.sort(fitlerOptions);
				for (WfUserBI wfUserBI : fitlerOptions) {
					filterCombo.addItem(wfUserBI);
				}
			} else if (e.getItem() instanceof WfStateFilter) {
				filterCombo.removeAllItems();
				WfStateFilter sf = (WfStateFilter) e.getItem();
				List<WfState> states = sf.getFilterOptions();
				Collections.sort(states);
				for (WfStateBI wfStateBI : states) {
					filterCombo.addItem(wfStateBI);
				}
			} else if (e.getItem() instanceof WfWorklistFilter) {
				filterCombo.removeAllItems();
				try {
					I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
					List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);
					for (I_TerminologyProject i_TerminologyProject : projects) {
						List<WorkSet> worksets = TerminologyProjectDAO.getAllWorkSetsForProject(i_TerminologyProject, config);
						for (WorkSet workSet : worksets) {
							List<WorkList> worklists = TerminologyProjectDAO.getAllWorklistForWorkset(workSet, config);
							Collections.sort(worklists);
							for (WorkList workList : worklists) {
								filterCombo.addItem(workList);
							}
						}
					}
				} catch (TerminologyException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else if (e.getItem() instanceof WfProjectFilter) {
				filterCombo.removeAllItems();
				try {
					I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
					List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);
					Collections.sort(projects);
					for (I_TerminologyProject project : projects) {
						filterCombo.addItem(project);
					}
				} catch (TerminologyException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else if (e.getItem() instanceof WfIsKindOfFilter) {
				filterCombo.removeAllItems();
				try {
					filterCombo.setTransferHandler(new ObjectTransferHandler(Terms.get().getActiveAceFrameConfig(), null));
					filterCombo.setRenderer(new ComboBoxRenderer());
				} catch (TerminologyException | IOException e1) {
					e1.printStackTrace();
				}
			} else if (e.getItem() instanceof WfStringFilter) {
				filterCombo.setVisible(false);
				textField.setVisible(true);
			} else if (e.getItem() instanceof WfCompletionFilter) {
				filterCombo.removeAllItems();
				CompletionOption[] completions = WfCompletionFilter.CompletionOption.values();
				for (CompletionOption comp : completions) {
					filterCombo.addItem(comp);
				}
			} else {
				filterCombo.removeAllItems();
				filterCombo.addItem("");
			}
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		addButton = new JButton();
		removeButton = new JButton();
		filterTypeCombo = new JComboBox<Object>();
		label1 = new JLabel();
		filterCombo = new JComboBox<Object>();

		// ======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout) getLayout()).columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		((GridBagLayout) getLayout()).rowHeights = new int[] { 0, 0 };
		((GridBagLayout) getLayout()).columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4 };
		((GridBagLayout) getLayout()).rowWeights = new double[] { 1.0, 1.0E-4 };

		// ---- addButton ----
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addButtonActionPerformed(e);
			}
		});
		add(addButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 5), 0, 0));

		// ---- removeButton ----
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeButtonActionPerformed(e);
			}
		});
		add(removeButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 5), 0, 0));

		// ---- filterTypeCombo ----
		filterTypeCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				filterTypeComboItemStateChanged(e);
			}
		});
		add(filterTypeCombo, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
		add(label1, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
		add(filterCombo, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

		textField = new JTextField();
		textField.setPreferredSize(new Dimension(400, 28));
		textField.setMinimumSize(new Dimension(400, 28));
		textField.setVisible(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 5;
		gbc_textField.gridy = 0;
		add(textField, gbc_textField);
		textField.setColumns(10);
		// JFormDesigner - End of component initialization
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JButton addButton;
	private JButton removeButton;
	private JComboBox<Object> filterTypeCombo;
	private JLabel label1;
	private JComboBox<Object> filterCombo;
	private JTextField textField;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
