/*
 * Created by JFormDesigner on Mon Jul 26 18:25:16 GMT-03:00 2010
 */

package org.ihtsdo.qa.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.rules.context.RulesContextHelper;
import org.ihtsdo.rules.context.RulesDeploymentPackageReference;
import org.ihtsdo.rules.context.RulesDeploymentPackageReferenceHelper;

/**
 * @author Guillermo Reynoso
 */
public class RulesDeploymentPkgBrowserPanel extends JPanel {

	private DefaultListModel list1Model;
	private DefaultTableModel table1Model;
	RulesDeploymentPackageReferenceHelper rulesPackageHelper = null;
	RulesDeploymentPackageReference selectedRulesPackage = null;
	RulesContextHelper contextHelper = null;

	public RulesDeploymentPkgBrowserPanel(I_ConfigAceFrame config) {
		initComponents();
		try {
			panel1.setEnabled(false);
			Component[] components = panel1.getComponents();
			for (int i = 0; i < components.length; i++) {
				components[i].setEnabled(false);
			}
			components = panel2.getComponents();
			for (int i = 0; i < components.length; i++) {
				components[i].setEnabled(false);
			}
			rulesPackageHelper = new RulesDeploymentPackageReferenceHelper(config);
			contextHelper = new RulesContextHelper(config);
			updateList1();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateTable1() {
		table1Model = new DefaultTableModel() {
			public Class getColumnClass(int c) {
				if (c == 1) {
					return Boolean.class;
				} else {
					return getValueAt(0, c).getClass();
				}
			}
			public boolean isCellEditable(int row, int col) { 
				if (col == 1) {
					return true; 
				} else {
					return false;
				}
			}
		};

		Object columnNames[] = { "Context", "Included"};
		table1Model.setColumnIdentifiers(columnNames);
		List<I_GetConceptData> currentContexts = new ArrayList<I_GetConceptData>();
		if (selectedRulesPackage != null) {
			currentContexts= contextHelper.getContextsForPackage(selectedRulesPackage);
		}
		try {
			for (I_GetConceptData context : contextHelper.getAllContexts()) {
				Boolean check = false;
				if (currentContexts.contains(context)) {
					check = true;
				}
				Object row[] = {context, new Boolean(check)};
				table1Model.addRow(row);
			}
			table1.setModel(table1Model);
			table1.validate();
			table1.repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void updateList1() {
		DefaultListCellRenderer renderer = new DefaultListCellRenderer();
		updateList1(renderer);
	}
	private void updateList1(DefaultListCellRenderer renderer) {
		try {
			list1.setCellRenderer(renderer);
			list1Model = new DefaultListModel();
			for (RulesDeploymentPackageReference repo : rulesPackageHelper.getAllRulesDeploymentPackages()) {
				list1Model.addElement(repo);
			}
			list1.setModel(list1Model);
			list1.validate();
			scrollPane2.setVisible(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void prepareForEditing() {
		// selected
		selectedRulesPackage = (RulesDeploymentPackageReference) list1.getSelectedValue();
		if (selectedRulesPackage != null) {
			updateTable1();
			scrollPane2.setVisible(true);
			panel1.setEnabled(true);
			Component[] components = panel1.getComponents();
			for (int i = 0; i < components.length; i++) {
				components[i].setEnabled(true);
			}
			components = panel2.getComponents();
			for (int i = 0; i < components.length; i++) {
				components[i].setEnabled(true);
			}
			textField1.setText(selectedRulesPackage.getName());
			textField2.setText(selectedRulesPackage.getUrl());
		}
	}

	private void list1ValueChanged(ListSelectionEvent e) {
		prepareForEditing();
	}

	private void list1MouseClicked(MouseEvent e) {
		prepareForEditing();
	}

	private void button1ActionPerformed(ActionEvent e) {
		// cancel
		textField1.setText("");
		textField2.setText("");
		selectedRulesPackage = null;
		panel1.setEnabled(false);
		scrollPane2.setVisible(false);
		Component[] components = panel1.getComponents();
		for (int i = 0; i < components.length; i++) {
			components[i].setEnabled(false);
		}
		components = panel2.getComponents();
		for (int i = 0; i < components.length; i++) {
			components[i].setEnabled(false);
		}
		button3.setEnabled(true);
	}

	private void button2ActionPerformed(ActionEvent e) {
		// save
		if (selectedRulesPackage == null) {
			// new repo
			selectedRulesPackage = rulesPackageHelper.createNewRulesDeploymentPackage(textField1.getText(), textField2.getText());
		} else {
			selectedRulesPackage.setName(textField1.getText());
			selectedRulesPackage.setUrl(textField2.getText());
			rulesPackageHelper.updateDeploymentPackageReference(selectedRulesPackage);
		}
		
		try {
			RulesContextHelper contextHelper = new RulesContextHelper(Terms.get().getActiveAceFrameConfig());
			
			for (int i = 0 ; i < table1Model.getRowCount() ; i++) {
				Boolean include = (Boolean) table1Model.getValueAt(i, 1);
				I_GetConceptData context = (I_GetConceptData) table1Model.getValueAt(i, 0);
				
				if (include) {
					contextHelper.addPkgReferenceToContext(selectedRulesPackage, context);
				} else {
					contextHelper.removePkgReferenceFromContext(selectedRulesPackage, context);
				}
				
			}
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		textField1.setText("");
		textField2.setText("");
		selectedRulesPackage = null;
		panel1.setEnabled(false);
		scrollPane2.setVisible(false);
		Component[] components = panel1.getComponents();
		for (int i = 0; i < components.length; i++) {
			components[i].setEnabled(false);
		}
		components = panel2.getComponents();
		for (int i = 0; i < components.length; i++) {
			components[i].setEnabled(false);
		}
		button3.setEnabled(true);
		updateList1();
	}

	private void button3ActionPerformed(ActionEvent e) {
		// new
		textField1.setText("");
		textField2.setText("");
		selectedRulesPackage = null;
		panel1.setEnabled(true);
		updateTable1();
		scrollPane2.setVisible(true);
		Component[] components = panel1.getComponents();
		for (int i = 0; i < components.length; i++) {
			components[i].setEnabled(true);
		}
		components = panel2.getComponents();
		for (int i = 0; i < components.length; i++) {
			components[i].setEnabled(true);
		}
		button5.setEnabled(false);
		button3.setEnabled(false);
	}

	private void button4ActionPerformed(ActionEvent e) {
		//validate
		RulesDeploymentPackageReference pkg = (RulesDeploymentPackageReference) list1.getSelectedValue();
		if (pkg != null) {
			HashMap<UUID, Color> map = new HashMap<UUID, Color>();
			if (pkg.validate()) {
				map.put(pkg.getUuids().iterator().next(), Color.GREEN);
			} else {
				map.put(pkg.getUuids().iterator().next(), Color.RED);
			}
			updateList1(new MapPkgListRenderer(map));
		}
	}

	private void button5ActionPerformed(ActionEvent e) {
		//retire
		rulesPackageHelper.retireRulesDeploymentPackageReference(selectedRulesPackage);
		textField1.setText("");
		textField2.setText("");
		selectedRulesPackage = null;
		panel1.setEnabled(false);
		scrollPane2.setVisible(false);
		Component[] components = panel1.getComponents();
		for (int i = 0; i < components.length; i++) {
			components[i].setEnabled(false);
		}
		components = panel2.getComponents();
		for (int i = 0; i < components.length; i++) {
			components[i].setEnabled(false);
		}
		button3.setEnabled(true);
		updateList1();
	}

	private void button6ActionPerformed(ActionEvent e) {
		//update
		RulesDeploymentPackageReference pkg = (RulesDeploymentPackageReference) list1.getSelectedValue();
		if (pkg != null) {
			HashMap<UUID, Color> map = new HashMap<UUID, Color>();
			try {
				pkg.updateKnowledgeBase();
				map.put(pkg.getUuids().iterator().next(), Color.GREEN);
			} catch (Exception e1) {
				map.put(pkg.getUuids().iterator().next(), Color.RED);
			}
			updateList1(new MapPkgListRenderer(map));
		}
	}


	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label1 = new JLabel();
		label2 = new JLabel();
		scrollPane1 = new JScrollPane();
		list1 = new JList();
		panel1 = new JPanel();
		label3 = new JLabel();
		textField1 = new JTextField();
		label4 = new JLabel();
		textField2 = new JTextField();
		scrollPane2 = new JScrollPane();
		table1 = new JTable();
		panel2 = new JPanel();
		button5 = new JButton();
		button2 = new JButton();
		button1 = new JButton();
		panel3 = new JPanel();
		button6 = new JButton();
		button4 = new JButton();
		button3 = new JButton();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

		//---- label1 ----
		label1.setText("Deployment Packages");
		add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

		//---- label2 ----
		label2.setText("Selected deployment packages details");
		add(label2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

		//======== scrollPane1 ========
		{

			//---- list1 ----
			list1.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					list1ValueChanged(e);
				}
			});
			list1.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					list1MouseClicked(e);
				}
			});
			scrollPane1.setViewportView(list1);
		}
		add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0E-4};

			//---- label3 ----
			label3.setText("Name");
			panel1.add(label3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));
			panel1.add(textField1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

			//---- label4 ----
			label4.setText("URL");
			panel1.add(label4, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));
			panel1.add(textField2, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

			//======== scrollPane2 ========
			{
				scrollPane2.setViewportView(table1);
			}
			panel1.add(scrollPane2, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

			//======== panel2 ========
			{
				panel2.setLayout(new GridBagLayout());
				((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
				((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

				//---- button5 ----
				button5.setText("Retire");
				button5.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				button5.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						button5ActionPerformed(e);
					}
				});
				panel2.add(button5, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

				//---- button2 ----
				button2.setText("Save");
				button2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				button2.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						button2ActionPerformed(e);
					}
				});
				panel2.add(button2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

				//---- button1 ----
				button1.setText("Cancel");
				button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				button1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						button1ActionPerformed(e);
					}
				});
				panel2.add(button1, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
			}
			panel1.add(panel2, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

		//======== panel3 ========
		{
			panel3.setLayout(new GridBagLayout());
			((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
			((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- button6 ----
			button6.setText("Update Pkg");
			button6.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button6.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button6ActionPerformed(e);
				}
			});
			panel3.add(button6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

			//---- button4 ----
			button4.setText("Validate Pkg");
			button4.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button4.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button4ActionPerformed(e);
				}
			});
			panel3.add(button4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

			//---- button3 ----
			button3.setText("New");
			button3.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button3.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button3ActionPerformed(e);
				}
			});
			panel3.add(button3, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 5), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JLabel label1;
	private JLabel label2;
	private JScrollPane scrollPane1;
	private JList list1;
	private JPanel panel1;
	private JLabel label3;
	private JTextField textField1;
	private JLabel label4;
	private JTextField textField2;
	private JScrollPane scrollPane2;
	private JTable table1;
	private JPanel panel2;
	private JButton button5;
	private JButton button2;
	private JButton button1;
	private JPanel panel3;
	private JButton button6;
	private JButton button4;
	private JButton button3;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
