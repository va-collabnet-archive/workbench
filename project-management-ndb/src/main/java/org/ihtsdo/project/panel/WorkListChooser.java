/*
 * Created by JFormDesigner on Tue Aug 31 19:05:45 GMT-03:00 2010
 */

package org.ihtsdo.project.panel;

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
import javax.swing.border.*;
import javax.swing.event.*;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkSet;

/**
 * @author Guillermo Reynoso
 */
public class WorkListChooser extends JDialog {
	
	private static final String NO_WORKLIST_SELECTED = "You have to select a worklist.";
	
	private DefaultListModel workListModel;
	private DefaultComboBoxModel projectComboModel;
	private I_ConfigAceFrame config;
	private WorkList workList =  null;
	
	public WorkListChooser(I_ConfigAceFrame config) {
		this.config = config;
		initComponents();
		initCustomComponents();
	}

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
	
	public WorkList showModalDialog() {
		setModal(true);
		this.setPreferredSize(new Dimension(500,500));
		pack();
		setVisible(true);
		return workList;
	}

	private void close(WorkList canceled) {
		this.workList = canceled;
		dispose();
	}
	
	private void openButtonActionPerformed(ActionEvent e) {
		if(workListList.getSelectedIndex() != -1){
			close(workList);
		}else{
			errorLabel.setText(NO_WORKLIST_SELECTED);
		}
	}

	private void cancelButtonActionPerformed(ActionEvent e) {
		close(null);
	}

	private void projectComboBoxItemStateChanged(ItemEvent e) {
		if(projectComboModel.getSelectedItem() instanceof I_TerminologyProject){
			I_TerminologyProject selectedProject = (I_TerminologyProject)projectComboModel.getSelectedItem();
			updateWorklistList(selectedProject);
		}else{
			updateWorklistList(null);
		}
	}

	private void updateWorklistList(I_TerminologyProject project) {
		workListModel.removeAllElements();
		if(project != null){
			List<WorkSet> worksets = TerminologyProjectDAO.getAllWorkSetsForProject(project, config);
			for (WorkSet workSet : worksets) {
				List<WorkList> worklists = TerminologyProjectDAO.getAllWorklistForWorkset(workSet, config);
				for (WorkList workList : worklists) {
					workListModel.addElement(workList);
				}
			}
		}else{
			workListModel.removeAllElements();
		}
	}

	private void workListListValueChanged(ListSelectionEvent e) {
		workList = (WorkList)workListList.getSelectedValue();
		errorLabel.setText("");
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
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

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout(5, 5));

		//======== buttonPanel ========
		{
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

			//---- openButton ----
			openButton.setText("Open");
			openButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					openButtonActionPerformed(e);
				}
			});
			buttonPanel.add(openButton);

			//---- cancelButton ----
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

		//======== projectPanel ========
		{
			projectPanel.setBorder(new EmptyBorder(3, 5, 0, 5));
			projectPanel.setLayout(new GridBagLayout());
			((GridBagLayout)projectPanel.getLayout()).columnWidths = new int[] {0, 177, 0};
			((GridBagLayout)projectPanel.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)projectPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
			((GridBagLayout)projectPanel.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- label1 ----
			label1.setText("Project");
			projectPanel.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- projectComboBox ----
			projectComboBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					projectComboBoxItemStateChanged(e);
				}
			});
			projectPanel.add(projectComboBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		contentPane.add(projectPanel, BorderLayout.NORTH);

		//======== componentPanel ========
		{
			componentPanel.setBorder(new EmptyBorder(0, 5, 0, 5));
			componentPanel.setLayout(new BorderLayout(3, 3));

			//======== worklistListScroll ========
			{

				//---- workListList ----
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
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel buttonPanel;
	private JButton openButton;
	private JButton cancelButton;
	private JPanel projectPanel;
	private JLabel label1;
	private JComboBox projectComboBox;
	private JPanel componentPanel;
	private JScrollPane worklistListScroll;
	private JList workListList;
	private JLabel errorLabel;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
