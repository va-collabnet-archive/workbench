/*
 * Created by JFormDesigner on Thu Aug 19 18:09:22 GMT-03:00 2010
 */

package org.ihtsdo.project.tasks;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;

/**
 * @author Guillermo Reynoso
 */
public class GetInforForUnassignedWork extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private I_ConfigAceFrame config;
	
	private I_TerminologyProject selectedProject = null;
	private WorkList selectedWorkList = null;
	
	public GetInforForUnassignedWork(I_ConfigAceFrame config) {
		this.config = config;
		initComponents();
		
		for (I_TerminologyProject loopProject : TerminologyProjectDAO.getAllProjects(config)) {
			projectCombo.addItem(loopProject);
		}
		
	}
	
	private void updateWorkListsCombo() {
		nacWorkListCombo.removeAllItems();
		if (selectedProject!=null){
			if (TranslationProject.class.isAssignableFrom(selectedProject.getClass())){
				TranslationProject project=(TranslationProject) selectedProject;
				try {
					String str = "";
					if (!(project.getSourceLanguageRefsets()!=null && (project.getSourceLanguageRefsets().size()>0) )){
						str="The translation project has not source language.\n";
					}
					if (project.getTargetLanguageRefset()==null){
						str+="The translation project has not target language";
					}
					if (!str.equals("")){
						JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
								str, "",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				} catch (HeadlessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TerminologyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (projectCombo.getSelectedItem() != null) {
			I_TerminologyProject selectedProject = (I_TerminologyProject) projectCombo.getSelectedItem();
			for (WorkList nacWorkList : TerminologyProjectDAO.getAllNacWorkLists(selectedProject, config)) {
				nacWorkListCombo.addItem(nacWorkList);
			}
		}
	}

	private void comboBox1ActionPerformed(ActionEvent e) {
		//updateWorkListsCombo();
	}

	private void projectComboItemStateChanged(ItemEvent e) {
		selectedProject = (I_TerminologyProject) projectCombo.getSelectedItem();
		updateWorkListsCombo();
	}

	private void nacWorkListComboItemStateChanged(ItemEvent e) {
		selectedWorkList = (WorkList) nacWorkListCombo.getSelectedItem();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label1 = new JLabel();
		label2 = new JLabel();
		projectCombo = new JComboBox();
		label3 = new JLabel();
		nacWorkListCombo = new JComboBox();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

		//---- label1 ----
		label1.setText("Assign to:");
		add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- label2 ----
		label2.setText("Project");
		add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- projectCombo ----
		projectCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				comboBox1ActionPerformed(e);
			}
		});
		projectCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				projectComboItemStateChanged(e);
			}
		});
		add(projectCombo, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//---- label3 ----
		label3.setText("WorkList");
		add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 5), 0, 0));

		//---- nacWorkListCombo ----
		nacWorkListCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				nacWorkListComboItemStateChanged(e);
			}
		});
		add(nacWorkListCombo, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JLabel label1;
	private JLabel label2;
	private JComboBox projectCombo;
	private JLabel label3;
	private JComboBox nacWorkListCombo;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	public I_TerminologyProject getSelectedProject() {
		return selectedProject;
	}

	public void setSelectedProject(I_TerminologyProject selectedProject) {
		this.selectedProject = selectedProject;
	}

	public WorkList getSelectedWorkList() {
		return selectedWorkList;
	}

	public void setSelectedWorkList(WorkList selectedWorkList) {
		this.selectedWorkList = selectedWorkList;
	}
}
