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

package org.ihtsdo.qa.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.drools.definition.rule.Rule;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
import org.ihtsdo.rules.context.RulesContextHelper;
import org.ihtsdo.rules.context.RulesDeploymentPackageReference;
import org.ihtsdo.rules.context.RulesDeploymentPackageReferenceHelper;

/**
 * The Class RulesContextMatrixPanel.
 *
 * @author Guillermo Reynoso
 */
public class RulesContextMatrixPanel extends JPanel {
	
	/** The table model. */
	private DefaultTableModel tableModel;
	
	/** The tf. */
	private I_TermFactory tf;
	
	/** The config. */
	private I_ConfigAceFrame config;
	
	/** The rules package helper. */
	private RulesDeploymentPackageReferenceHelper rulesPackageHelper = null;
	
	/** The context helper. */
	private RulesContextHelper contextHelper = null;

	/**
	 * Instantiates a new rules context matrix panel.
	 *
	 * @param config the config
	 */
	public RulesContextMatrixPanel(I_ConfigAceFrame config) {
		initComponents();
		this.tf = Terms.get();
		this.config = config;
		rulesPackageHelper = new RulesDeploymentPackageReferenceHelper(config);
		contextHelper = new RulesContextHelper(config);
		updateTable1();
	}

	/**
	 * Update table1.
	 */
	private void updateTable1() {
		try {
			table1.setAutoCreateRowSorter(true);
			I_GetConceptData agendaMetadataRefset = tf.getConcept(RefsetAuxiliary.Concept.RULES_CONTEXT_METADATA_REFSET.getUids());
			I_GetConceptData includeClause = tf.getConcept(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids());
			I_GetConceptData excludeClause = tf.getConcept(RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL.getUids());
			List<String> columnNames = new ArrayList<String>();
			columnNames.add("Name");
			columnNames.add("Description");
			columnNames.add("DITA UUID");

			List<? extends I_GetConceptData> contexts = contextHelper.getAllContexts();

			for (I_GetConceptData context : contexts) {
				columnNames.add(context.toString());
			}

			String[][] data = null;
			tableModel = new DefaultTableModel(data, columnNames.toArray()) {
				private static final long serialVersionUID = 1L;

				public boolean isCellEditable(int x, int y) {
					return false;
				}
			};
			for (RulesDeploymentPackageReference deloymentPackage : rulesPackageHelper.getAllRulesDeploymentPackages()) {
				List<String> rowData = new ArrayList<String>();
				rowData.add("Deployment Package: " + deloymentPackage.getName());
				boolean isOnline = deloymentPackage.validate();
				if (isOnline) {
					rowData.add("");
				} else {
					rowData.add("OFFLINE");
				}
				rowData.add("");
				for (I_GetConceptData context : contexts) {
					rowData.add("");
				}
				tableModel.addRow(rowData.toArray());
				int rulesCount = 0;
				if (isOnline) {
					for (Rule rule : deloymentPackage.getRules()) {
						rulesCount++;
						//AceLog.getAppLog().info("** rule: " + rule.getName());
						String ruleUid = null;
						String description =  null;
						String ditaUid = null;
						
						try {
							ruleUid = (String) rule.getMetaData().get("UUID");
							//ruleUid = rule.getMetaAttribute("UID");
							description = (String) rule.getMetaData().get("DESCRIPTION");
							//description = rule.getMetaAttribute("DESCRIPTION");
							ditaUid = (String) rule.getMetaData().get("DITA_UID");
							//ditaUid = rule.getMetaAttribute("DITA_UID");
						} catch (Exception e) {
							// problem retrieving metadata, do nothing
							AceLog.getAppLog().info("Malformed metadata..");
						}
						
						if (description == null) description = "";
						if (ditaUid == null) ditaUid = "";

						rowData = new ArrayList<String>();
						rowData.add(rule.getName());
						rowData.add(description);
						rowData.add(ditaUid);

						if (ruleUid != null) {
							for (I_GetConceptData context : contexts) {
								//AceLog.getAppLog().info("** context: " + context.toString());
								//Look for agenda
								String columnValue = "Included by default";
								I_GetConceptData role = contextHelper.getRoleInContext(ruleUid, context);
								if (role != null) {
									columnValue = role.toString();
								} 
								//AceLog.getAppLog().info("Adding: " + columnCheckBox);
								rowData.add(columnValue);
							}
						} else {
							for (I_GetConceptData context : contexts) {
								rowData.add("No UID Metadata");
							}
						}
						tableModel.addRow(rowData.toArray());
					}
				}
				if (rulesCount == 0) {
					rowData = new ArrayList<String>();
					rowData.add("No rules");
					rowData.add("");
					rowData.add("");
					for (I_GetConceptData context : contexts) {
						rowData.add("");
					}
					tableModel.addRow(rowData.toArray());
				}
			}
			table1.setModel(tableModel);
			table1.setGridColor(Color.BLACK);
			table1.setShowGrid(true);
			TableColumnModel cmodel = table1.getColumnModel(); 
			TextAreaRendererForDeploymentPackages textAreaRenderer = new TextAreaRendererForDeploymentPackages();
			for (int i=0 ; i< cmodel.getColumnCount() ; i++) {
				cmodel.getColumn(i).setCellRenderer(textAreaRenderer);
			}
			// refresh table
			table1.revalidate();
			table1.repaint();
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		} 
	}

	/**
	 * Button2 action performed.
	 *
	 * @param e the e
	 */
	private void button2ActionPerformed(ActionEvent e) {
		updateTable1();
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel2 = new JPanel();
		label1 = new JLabel();
		scrollPane1 = new JScrollPane();
		table1 = new JTable();
		panel1 = new JPanel();
		button2 = new JButton();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

		//======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- label1 ----
			label1.setText("Rule-Context Matrix");
			panel2.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));
		}
		add(panel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

		//======== scrollPane1 ========
		{
			scrollPane1.setViewportView(table1);
		}
		add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- button2 ----
			button2.setText("Refresh");
			button2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					button2ActionPerformed(e);
				}
			});
			panel1.add(button2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	/** The panel2. */
	private JPanel panel2;
	
	/** The label1. */
	private JLabel label1;
	
	/** The scroll pane1. */
	private JScrollPane scrollPane1;
	
	/** The table1. */
	private JTable table1;
	
	/** The panel1. */
	private JPanel panel1;
	
	/** The button2. */
	private JButton button2;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
