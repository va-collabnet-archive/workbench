/*
 * Created by JFormDesigner on Sun Apr 25 17:56:17 GMT-04:00 2010
 */

package org.ihtsdo.rules;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.UUID;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.drools.KnowledgeBase;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;

/**
 * @author Guillermo Reynoso
 */
public class RulesBrowserPanel extends JPanel {
	KnowledgeBase kbase;
	BooleanTableModel tableModel;
	RulesAgenda agenda;
	I_ConfigAceFrame config;

	public RulesBrowserPanel(KnowledgeBase kbase, I_ConfigAceFrame config) throws IOException {
		this.kbase = kbase;
		this.config = config;
		initComponents();
		String[] columnNames = {"Package",
				"Rule", "Exclude?"};
		String[][] data = null;
		tableModel = new BooleanTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int x, int y) {
				if (y == 2 ) {
					return true;
				} else {
					return false;
				}
			}
		};

		I_ConfigAceDb configDb = config.getDbConfig();

		agenda = (RulesAgenda) configDb.getProperty("RulesAgenda");

		if (agenda == null) {
			agenda = new RulesAgenda();
		}

		for (KnowledgePackage kpackg : kbase.getKnowledgePackages()) {
			//System.out.println("** " + kpackg.getName());
			for (Rule rule : kpackg.getRules()) {
				//System.out.println("**** " + rule.getName());
				boolean excluded = false;
				//String ruleUid = (String) rule.getMetaData().get("UID");
				String ruleUid = (String) rule.getMetaAttribute("UID");
				if (ruleUid != null) {
					if (agenda.getExcludedRules().containsKey(UUID.fromString(ruleUid))) {
						excluded = true;
					}
				}
				if (agenda.getExcludedRules().containsValue(rule.getName())) {
					excluded = true;
				}
				tableModel.addRow(new Object[] {kpackg.getName(), rule.getName(), new Boolean(false)});
			}
		}
		table1.setModel(tableModel);
		table1.validate();
	}

	private void button1ActionPerformed(ActionEvent e) {
		for (int i=0; i < tableModel.getRowCount(); i++) {
			if (tableModel.getValueAt(i, 2).equals(true)) {
				agenda.getExcludedRules().put(UUID.randomUUID(), (String) tableModel.getValueAt(i, 1));
			} else {
				for (UUID id : agenda.getExcludedRules().keySet()) {
					if (agenda.getExcludedRules().get(id).equals(tableModel.getValueAt(i, 1))) {
						agenda.getExcludedRules().remove(id);
					}
				}
			}
		}
		try {
			config.getDbConfig().setProperty("RulesAgenda", agenda);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		config.setStatusMessage("Exclusions configuration saved...");
	}

	public class BooleanTableModel extends DefaultTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public BooleanTableModel() {
			super();
		}

		public BooleanTableModel(int rowCount, int columnCount) {
			super(rowCount, columnCount);
		}

		public BooleanTableModel(Object[] columnNames, int rowCount) {
			super(columnNames, rowCount);
		}

		public BooleanTableModel(Object[][] data, Object[] columnNames) {
			super(data, columnNames);
		}

		public BooleanTableModel(Vector columnNames, int rowCount) {
			super(columnNames, rowCount);
		}

		public BooleanTableModel(Vector data, Vector columnNames) {
			super(data, columnNames);
		}

		public Class getColumnClass(int col) {
			return col == 2 ? Boolean.class : super.getColumnClass(col);
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		scrollPane1 = new JScrollPane();
		table1 = new JTable();
		panel1 = new JPanel();
		button1 = new JButton();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0E-4};

		//======== scrollPane1 ========
		{
			scrollPane1.setViewportView(table1);
		}
		add(scrollPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- button1 ----
			button1.setText("Save");
			button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed(e);
				}
			});
			panel1.add(button1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JScrollPane scrollPane1;
	private JTable table1;
	private JPanel panel1;
	private JButton button1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
