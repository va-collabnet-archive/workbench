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
package org.ihtsdo.rules;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.qa.gui.TextAreaRenderer;

/**
 * The Class RulesResultsPanel.
 */
public class RulesResultsPanel extends JPanel implements ActionListener {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The config. */
	I_ConfigAceFrame config;
	
	/** The table model. */
	DefaultTableModel tableModel;

	/**
	 * Instantiates a new rules results panel.
	 * 
	 * @param results the results
	 */
	public RulesResultsPanel(HashMap<I_GetConceptData, List<AlertToDataConstraintFailure>> results) {
		this.setLayout(new BorderLayout());
		try {
			config =  Terms.get().getActiveAceFrameConfig();
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Box topContainer = new Box(BoxLayout.X_AXIS);
		topContainer.add(new JLabel("Execution result:"));

		String[] columnNames = {"Concept",
		"Alerts"};
		String[][] data = null;
		tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};
		for (I_GetConceptData concept : results.keySet()) {
			String alerts = "<html>";
			for (AlertToDataConstraintFailure alert : results.get(concept)) {
				alerts = alerts + alert.getAlertMessage() + " ";
			}
			String noHTMLString = alerts.replaceAll("\\<.*?\\>", "");
			tableModel.addRow(new String[] {concept.toString(), noHTMLString});
		}

		JTable table = new JTable(tableModel);
		table.getColumnModel().getColumn(1).setCellRenderer(new TextAreaRenderer());
		JScrollPane scrollPane = new JScrollPane(table);
		table.setGridColor(Color.GRAY);
		Container centerContainer = new Container();
		centerContainer.setLayout(new BorderLayout());
		centerContainer.add(table.getTableHeader(), BorderLayout.PAGE_START);
		centerContainer.add(scrollPane, BorderLayout.CENTER);

		this.add(topContainer, BorderLayout.PAGE_START);
		this.add(centerContainer, BorderLayout.CENTER);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
	}

}
