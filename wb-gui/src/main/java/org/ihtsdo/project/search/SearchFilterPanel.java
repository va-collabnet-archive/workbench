/*
 * Created by JFormDesigner on Tue Jan 08 18:34:34 GMT-03:00 2013
 */

package org.ihtsdo.project.search;

import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.filter.WfDestinationFilter;
import org.ihtsdo.project.filter.WfProjectFilter;
import org.ihtsdo.project.filter.WfWorklistFilter;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.tk.workflow.api.WfFilterBI;
import org.ihtsdo.tk.workflow.api.WfUserBI;
import org.ihtsdo.tk.workflow.api.WorkListBI;

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
		try {
			addButton.setIcon(new ImageIcon(ACE.class.getResource("/16x16/plain/add2.png")));
			removeButton.setIcon(new ImageIcon(ACE.class.getResource("/16x16/plain/delete2.png")));
		} catch (Exception e) {
			e.printStackTrace();
		}

		filterTypeCombo.addItem("");
		filterTypeCombo.addItem(new WfDestinationFilter());
		filterTypeCombo.addItem(new WfWorklistFilter());
		filterTypeCombo.addItem(new WfProjectFilter());
	}

	private void addButtonActionPerformed(ActionEvent e) {
		container.addNewFilterPanel();
	}

	private void removeButtonActionPerformed(ActionEvent e) {
		container.removeFilterPanel(this);
	}

	public WfFilterBI getWfFilter() {
		Object filterObject = filterCombo.getSelectedItem();
		if (filterObject instanceof WfUser) {
			return new WfDestinationFilter((WfUser) filterCombo.getSelectedItem());
		} else if (filterObject instanceof WorkList) {
			return new WfWorklistFilter(((WorkList) filterCombo.getSelectedItem()).getUuid());
		} else {
			return null;
		}
	}

	private void filterTypeComboItemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			if (e.getItem() instanceof WfDestinationFilter) {
				filterCombo.removeAllItems();
				WfDestinationFilter df = (WfDestinationFilter) e.getItem();
				List<WfUserBI> fitlerOptions = df.getFilterOptions();
				for (WfUserBI wfUserBI : fitlerOptions) {
					filterCombo.addItem(wfUserBI);
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
					for (I_TerminologyProject i_TerminologyProject : projects) {
						filterCombo.addItem(i_TerminologyProject);
					}
				} catch (TerminologyException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
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
		filterTypeCombo = new JComboBox();
		label1 = new JLabel();
		filterCombo = new JComboBox();

		// ======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout) getLayout()).columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		((GridBagLayout) getLayout()).rowHeights = new int[] { 0, 0 };
		((GridBagLayout) getLayout()).columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 1.0E-4 };
		((GridBagLayout) getLayout()).rowWeights = new double[] { 1.0, 1.0E-4 };

		// ---- addButton ----
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addButtonActionPerformed(e);
			}
		});
		add(addButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0,
				0));

		// ---- removeButton ----
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeButtonActionPerformed(e);
			}
		});
		add(removeButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0),
				0, 0));

		// ---- filterTypeCombo ----
		filterTypeCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				filterTypeComboItemStateChanged(e);
			}
		});
		add(filterTypeCombo, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0),
				0, 0));
		add(label1, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		add(filterCombo, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,
				0));
		// JFormDesigner - End of component initialization
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JButton addButton;
	private JButton removeButton;
	private JComboBox filterTypeCombo;
	private JLabel label1;
	private JComboBox filterCombo;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
