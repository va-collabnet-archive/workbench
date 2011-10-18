/**
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

package org.ihtsdo.project;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.model.WorkSetMember;

/**
 * The Class WorksetReport.
 */
public class WorksetReport extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The config. */
	private I_ConfigAceFrame config;

	/**
	 * Instantiates a new workset report.
	 * 
	 * @param config the config
	 */
	public WorksetReport(I_ConfigAceFrame config) {
		this.config = config;
		initComponents();
		for (I_TerminologyProject project : TerminologyProjectDAO.getAllTranslationProjects(config)) {
			comboBox1.addItem(project);
		}
		comboBox1.revalidate();

	}

	/**
	 * Combo box1 action performed.
	 * 
	 * @param e the e
	 */
	private void comboBox1ActionPerformed(ActionEvent e) {
		comboBox2.removeAllItems();
		for (WorkSet workset : TerminologyProjectDAO.getAllWorkSetsForProject((TranslationProject)comboBox1.getSelectedItem(), config)) {
			comboBox2.addItem(workset);
		}
	}

	/**
	 * Button1 action performed.
	 * 
	 * @param e the e
	 */
	private void button1ActionPerformed(ActionEvent e) {
		WorkSet selectedWorkset = (WorkSet) comboBox2.getSelectedItem();
		if (selectedWorkset != null) {
			List<String> tempNamesCollector = new ArrayList<String>();
			tempNamesCollector.add("Concept");
//			List<WorkList> worklists = TerminologyProjectDAO.getAllWorkListsForWorkSet(selectedWorkset, config);
//			for (WorkList worklist : worklists) {
//				tempNamesCollector.add(worklist.getBatch().getName() + " / " + worklist.getDestination());
//			}
			String[][] data = null;
			DefaultTableModel tableModel = new DefaultTableModel(data, tempNamesCollector.toArray()) {
				private static final long serialVersionUID = 1L;

				public boolean isCellEditable(int x, int y) {
					return false;
				}
			};
			
			for (WorkSetMember member : TerminologyProjectDAO.getAllWorkSetMembers(selectedWorkset, config)) {
				List<String> tempRowDataCollector = new ArrayList<String>();
				tempRowDataCollector.add(member.getName());
//				for (WorkList worklist : worklists) {
//					WorkListMember listMember = TerminologyProjectDAO.getWorkListMember(member.getConcept(), worklist.getId(), config);
//					try {
//						tempRowDataCollector.add(LocalVersionedTerminology.get().getConcept(listMember.getActivityStatus()).toString());
//					} catch (TerminologyException e1) {
//						e1.printStackTrace();
//					} catch (IOException e1) {
//						e1.printStackTrace();
//					}
//				}
				tableModel.addRow(tempRowDataCollector.toArray());
			}
			
			table1.setModel(tableModel);
			table1.revalidate();

		}
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		comboBox1 = new JComboBox();
		comboBox2 = new JComboBox();
		button1 = new JButton();
		scrollPane1 = new JScrollPane();
		table1 = new JTable();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- comboBox1 ----
			comboBox1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					comboBox1ActionPerformed(e);
				}
			});
			panel1.add(comboBox1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));
			panel1.add(comboBox2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

			//---- button1 ----
			button1.setText("Search");
			button1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed(e);
				}
			});
			panel1.add(button1, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

		//======== scrollPane1 ========
		{
			scrollPane1.setViewportView(table1);
		}
		add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	/** The panel1. */
	private JPanel panel1;
	
	/** The combo box1. */
	private JComboBox comboBox1;
	
	/** The combo box2. */
	private JComboBox comboBox2;
	
	/** The button1. */
	private JButton button1;
	
	/** The scroll pane1. */
	private JScrollPane scrollPane1;
	
	/** The table1. */
	private JTable table1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
