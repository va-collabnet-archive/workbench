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

package org.ihtsdo.project.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkList;

/**
 * The Class WorkListChooser.
 * 
 * @author Guillermo Reynoso
 */
public class NacWorkListChooser extends JDialog {

	/** The Constant NO_WORKLIST_SELECTED. */
	private static final String NO_WORKLIST_SELECTED = "You have to select a worklist.";

	/** The work list model. */
	private DefaultListModel workListModel;

	/** The project combo model. */
	private DefaultComboBoxModel projectComboModel;

	/** The config. */
	private I_ConfigAceFrame config;

	/** The work list. */
	private WorkList workList = null;

	/**
	 * Instantiates a new work list chooser.
	 * 
	 * @param config
	 *            the config
	 */
	public NacWorkListChooser(I_ConfigAceFrame config) {
		this.config = config;
		initComponents();
		initCustomComponents();
	}

	/**
	 * Inits the custom components.
	 */
	private void initCustomComponents() {

		errorLabel.setForeground(Color.RED);

		projectComboModel = new DefaultComboBoxModel();
		projectComboBox.setModel(projectComboModel);

		workListModel = new DefaultListModel();
		workListList.setModel(workListModel);

		List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);
		projectComboModel.addElement("");
		for (I_TerminologyProject iTerminologyProject : projects) {
			projectComboModel.addElement(iTerminologyProject);
		}

		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				close(null);
			}
		});

	}

	/**
	 * Show modal dialog.
	 * 
	 * @return the work list
	 */
	public WorkList showModalDialog() {
		setModal(true);
		this.setPreferredSize(new Dimension(500, 500));
		pack();
		setVisible(true);
		return workList;
	}

	/**
	 * Close.
	 * 
	 * @param canceled
	 *            the canceled
	 */
	private void close(WorkList canceled) {
		this.workList = canceled;
		dispose();
	}

	/**
	 * Open button action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void openButtonActionPerformed(ActionEvent e) {
		if (workListList.getSelectedIndex() != -1) {
			close(workList);
		} else {
			errorLabel.setText(NO_WORKLIST_SELECTED);
		}
	}

	/**
	 * Cancel button action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void cancelButtonActionPerformed(ActionEvent e) {
		close(null);
	}

	/**
	 * Project combo box item state changed.
	 * 
	 * @param e
	 *            the e
	 */
	private void projectComboBoxItemStateChanged(ItemEvent e) {
		if (projectComboModel.getSelectedItem() instanceof I_TerminologyProject) {
			I_TerminologyProject selectedProject = (I_TerminologyProject) projectComboModel.getSelectedItem();
			updateWorklistList(selectedProject);
		} else {
			updateWorklistList(null);
		}
	}

	/**
	 * Update worklist list.
	 * 
	 * @param project
	 *            the project
	 */
	private void updateWorklistList(I_TerminologyProject project) {
		workListModel.removeAllElements();
		if (project != null) {
			List<WorkList> worklists = TerminologyProjectDAO.getAllNacWorkLists(project, config);
			for (WorkList workList : worklists) {
				workListModel.addElement(workList);
			}
		} else {
			workListModel.removeAllElements();
		}
	}

	/**
	 * Work list list value changed.
	 * 
	 * @param e
	 *            the e
	 */
	private void workListListValueChanged(ListSelectionEvent e) {
		workList = (WorkList) workListList.getSelectedValue();
		errorLabel.setText("");
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		buttonPanel = new JPanel();
		openButton = new JButton();
		cancelButton = new JButton();
		projectPanel = new JPanel();
		label1 = new JLabel();
		projectComboBox = new JComboBox();
		componentPanel = new JPanel();
		worklistListScroll = new JScrollPane();
		workListList = new JList();
		errorLabel = new JLabel();

		// ======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout(5, 5));

		// ======== buttonPanel ========
		{
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

			// ---- openButton ----
			openButton.setText("Open");
			openButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					openButtonActionPerformed(e);
				}
			});
			buttonPanel.add(openButton);

			// ---- cancelButton ----
			cancelButton.setText("cancel");
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cancelButtonActionPerformed(e);
				}
			});
			buttonPanel.add(cancelButton);
		}
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		// ======== projectPanel ========
		{
			projectPanel.setBorder(new EmptyBorder(3, 5, 0, 5));
			projectPanel.setLayout(new GridBagLayout());
			((GridBagLayout) projectPanel.getLayout()).columnWidths = new int[] { 0, 177, 0 };
			((GridBagLayout) projectPanel.getLayout()).rowHeights = new int[] { 0, 0 };
			((GridBagLayout) projectPanel.getLayout()).columnWeights = new double[] { 0.0, 0.0, 1.0E-4 };
			((GridBagLayout) projectPanel.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

			// ---- label1 ----
			label1.setText("Project");
			projectPanel.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- projectComboBox ----
			projectComboBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					projectComboBoxItemStateChanged(e);
				}
			});
			projectPanel.add(projectComboBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		contentPane.add(projectPanel, BorderLayout.NORTH);

		// ======== componentPanel ========
		{
			componentPanel.setBorder(new EmptyBorder(0, 5, 0, 5));
			componentPanel.setLayout(new BorderLayout(3, 3));

			// ======== worklistListScroll ========
			{

				// ---- workListList ----
				workListList.addListSelectionListener(new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						workListListValueChanged(e);
					}
				});
				worklistListScroll.setViewportView(workListList);
			}
			componentPanel.add(worklistListScroll, BorderLayout.CENTER);
			componentPanel.add(errorLabel, BorderLayout.SOUTH);
		}
		contentPane.add(componentPanel, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	/** The button panel. */
	private JPanel buttonPanel;

	/** The open button. */
	private JButton openButton;

	/** The cancel button. */
	private JButton cancelButton;

	/** The project panel. */
	private JPanel projectPanel;

	/** The label1. */
	private JLabel label1;

	/** The project combo box. */
	private JComboBox projectComboBox;

	/** The component panel. */
	private JPanel componentPanel;

	/** The worklist list scroll. */
	private JScrollPane worklistListScroll;

	/** The work list list. */
	private JList workListList;

	/** The error label. */
	private JLabel errorLabel;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
