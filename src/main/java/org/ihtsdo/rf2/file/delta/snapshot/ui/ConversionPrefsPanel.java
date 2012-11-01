/*
 * Created by JFormDesigner on Thu Feb 17 13:42:09 GMT-03:00 2011
 */

package org.ihtsdo.rf2.file.delta.snapshot.ui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.*;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.apache.commons.configuration.CompositeConfiguration;

/**
 * @author Varsha Parekh
 */
public class ConversionPrefsPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8567987860140447569L;
	CompositeConfiguration config;
	
	public ConversionPrefsPanel(CompositeConfiguration config) {
		initComponents();
		this.config = config;
		
		textField1.setText(config.getString("rf2inputfiles.concepts"));
		textField2.setText(config.getString("rf2inputfiles.descriptions"));
		textField3.setText(config.getString("rf2inputfiles.relationships"));
		textField4.setText(config.getString("rf2inputfiles.languages"));
		textField5.setText(config.getString("rf2inputfiles.attributes"));
		textField6.setText(config.getString("rf2inputfiles.simplemaps"));
		textField7.setText(config.getString("rf2inputfiles.associations"));
		textField8.setText(config.getString("rf2inputfiles.qualifiers"));
	}

	private void initComponents() {
		
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		label1 = new JLabel();
		panel2 = new JPanel();
		label2 = new JLabel();
		tabbedPane1 = new JTabbedPane();
		panel4 = new JPanel();
		panel5 = new JPanel();
		button10 = new JButton();
		label3 = new JLabel();
		textField1 = new JTextField();
		button2 = new JButton();
		label4 = new JLabel();
		textField2 = new JTextField();
		button3 = new JButton();
		label5 = new JLabel();
		textField3 = new JTextField();
		button4 = new JButton();
		label6 = new JLabel();
		textField4 = new JTextField();
		button5 = new JButton();
		label7 = new JLabel();
		textField5 = new JTextField();
		button6 = new JButton();
		label8 = new JLabel();
		textField6 = new JTextField();
		button7 = new JButton();
		label9 = new JLabel();
		textField7 = new JTextField();
		button8 = new JButton();
		label10 = new JLabel();
		textField8 = new JTextField();
		button9 = new JButton();
		panel20 = new JPanel();
		panel21 = new JPanel();
		panel6 = new JPanel();
		panel7 = new JPanel();
		label11 = new JLabel();
		panel8 = new JPanel();
		hSpacer1 = new JPanel(null);
		radioButton1 = new JRadioButton();
		radioButton2 = new JRadioButton();
		panel9 = new JPanel();
		hSpacer2 = new JPanel(null);
		label12 = new JLabel();
		textField9 = new JTextField();
		label13 = new JLabel();
		panel17 = new JPanel();
		label22 = new JLabel();
		panel10 = new JPanel();
		label14 = new JLabel();
		panel11 = new JPanel();
		hSpacer3 = new JPanel(null);
		radioButton3 = new JRadioButton();
		radioButton4 = new JRadioButton();
		panel12 = new JPanel();
		hSpacer4 = new JPanel(null);
		label15 = new JLabel();
		textField10 = new JTextField();
		label16 = new JLabel();
		panel19 = new JPanel();
		label24 = new JLabel();
		panel13 = new JPanel();
		label17 = new JLabel();
		panel14 = new JPanel();
		hSpacer5 = new JPanel(null);
		radioButton5 = new JRadioButton();
		radioButton6 = new JRadioButton();
		panel15 = new JPanel();
		hSpacer6 = new JPanel(null);
		label18 = new JLabel();
		textField11 = new JTextField();
		label19 = new JLabel();
		panel16 = new JPanel();
		hSpacer7 = new JPanel(null);
		label20 = new JLabel();
		textField12 = new JTextField();
		label21 = new JLabel();
		panel18 = new JPanel();
		label23 = new JLabel();
		panel3 = new JPanel();
		comboBox1 = new JComboBox();
		button11 = new JButton();
		button1 = new JButton();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0E-4};

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- label1 ----
			label1.setText("IHTSDO RF2 to RF1 conversion tool");
			label1.setFont(new Font("Lucida Grande", Font.BOLD, 14));
			panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
		}
		add(panel1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- label2 ----
			label2.setText("Preferences:");
			label2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel2.add(label2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
		}
		add(panel2, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//======== tabbedPane1 ========
		{

			//======== panel4 ========
			{
				panel4.setLayout(new GridBagLayout());
				((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {0, 0, 63, 0, 0};
				((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
				((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 0.0, 1.0E-4};
				((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

				//======== panel5 ========
				{
					panel5.setLayout(new GridBagLayout());
					((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- button10 ----
					button10.setText("Set from folder");
					button10.setFont(button10.getFont().deriveFont(button10.getFont().getSize() - 2f));
					panel5.add(button10, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
				}
				panel4.add(panel5, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label3 ----
				label3.setText("Concepts");
				panel4.add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));
				panel4.add(textField1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- button2 ----
				button2.setText(">");
				button2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel4.add(button2, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label4 ----
				label4.setText("Descriptions");
				panel4.add(label4, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));
				panel4.add(textField2, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- button3 ----
				button3.setText(">");
				button3.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel4.add(button3, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label5 ----
				label5.setText("Relationships");
				panel4.add(label5, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));
				panel4.add(textField3, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- button4 ----
				button4.setText(">");
				button4.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel4.add(button4, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label6 ----
				label6.setText("Language refsets");
				panel4.add(label6, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));
				panel4.add(textField4, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- button5 ----
				button5.setText(">");
				button5.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel4.add(button5, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label7 ----
				label7.setText("Attribute-value refsets");
				panel4.add(label7, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));
				panel4.add(textField5, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- button6 ----
				button6.setText(">");
				button6.setFont(button6.getFont().deriveFont(button6.getFont().getSize() - 2f));
				panel4.add(button6, new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label8 ----
				label8.setText("Simple map refsets");
				panel4.add(label8, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));
				panel4.add(textField6, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- button7 ----
				button7.setText(">");
				button7.setFont(button7.getFont().deriveFont(button7.getFont().getSize() - 2f));
				panel4.add(button7, new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label9 ----
				label9.setText("Associations refsets");
				panel4.add(label9, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));
				panel4.add(textField7, new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- button8 ----
				button8.setText(">");
				button8.setFont(button8.getFont().deriveFont(button8.getFont().getSize() - 2f));
				panel4.add(button8, new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label10 ----
				label10.setText("Qualifiers refsets");
				panel4.add(label10, new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));
				panel4.add(textField8, new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- button9 ----
				button9.setText(">");
				button9.setFont(button9.getFont().deriveFont(button9.getFont().getSize() - 2f));
				panel4.add(button9, new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));
			}
			tabbedPane1.addTab("RF2 - Source files", panel4);


			//======== panel20 ========
			{
				panel20.setLayout(new GridBagLayout());
				((GridBagLayout)panel20.getLayout()).columnWidths = new int[] {0, 0, 0};
				((GridBagLayout)panel20.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel20.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
				((GridBagLayout)panel20.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
			}
			tabbedPane1.addTab("RF1 - Output", panel20);


			//======== panel21 ========
			{
				panel21.setLayout(new GridBagLayout());
				((GridBagLayout)panel21.getLayout()).columnWidths = new int[] {0, 0, 0};
				((GridBagLayout)panel21.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel21.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
				((GridBagLayout)panel21.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
			}
			tabbedPane1.addTab("Auxiliary data", panel21);


			//======== panel6 ========
			{
				panel6.setLayout(new GridBagLayout());
				((GridBagLayout)panel6.getLayout()).columnWidths = new int[] {0, 0};
				((GridBagLayout)panel6.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
				((GridBagLayout)panel6.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
				((GridBagLayout)panel6.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

				//======== panel7 ========
				{
					panel7.setLayout(new GridBagLayout());
					((GridBagLayout)panel7.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
					((GridBagLayout)panel7.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel7.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel7.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- label11 ----
					label11.setText("RF1 files generation:");
					panel7.add(label11, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
				}
				panel6.add(panel7, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel8 ========
				{
					panel8.setLayout(new GridBagLayout());
					((GridBagLayout)panel8.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0};
					((GridBagLayout)panel8.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel8.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel8.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
					panel8.add(hSpacer1, new GridBagConstraints(0, 0, 4, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- radioButton1 ----
					radioButton1.setText("Enabled");
					panel8.add(radioButton1, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- radioButton2 ----
					radioButton2.setText("Disabled");
					panel8.add(radioButton2, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel6.add(panel8, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel9 ========
				{
					panel9.setLayout(new GridBagLayout());
					((GridBagLayout)panel9.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 148, 209, 0};
					((GridBagLayout)panel9.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel9.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel9.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
					panel9.add(hSpacer2, new GridBagConstraints(0, 0, 4, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- label12 ----
					label12.setText("Date: ");
					panel9.add(label12, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
					panel9.add(textField9, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- label13 ----
					label13.setText("(format like \"20110131\")");
					label13.setFont(label13.getFont().deriveFont(label13.getFont().getSize() - 2f));
					panel9.add(label13, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel6.add(panel9, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel17 ========
				{
					panel17.setLayout(new GridBagLayout());
					((GridBagLayout)panel17.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel17.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel17.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel17.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- label22 ----
					label22.setText("Tip: RF1 output = app/output/RF1");
					label22.setFont(label22.getFont().deriveFont(label22.getFont().getSize() - 3f));
					panel17.add(label22, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel6.add(panel17, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel10 ========
				{
					panel10.setLayout(new GridBagLayout());
					((GridBagLayout)panel10.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
					((GridBagLayout)panel10.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel10.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel10.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- label14 ----
					label14.setText("Snapshot generation:");
					panel10.add(label14, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
				}
				panel6.add(panel10, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel11 ========
				{
					panel11.setLayout(new GridBagLayout());
					((GridBagLayout)panel11.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0};
					((GridBagLayout)panel11.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel11.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel11.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
					panel11.add(hSpacer3, new GridBagConstraints(0, 0, 4, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- radioButton3 ----
					radioButton3.setText("Enabled");
					panel11.add(radioButton3, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- radioButton4 ----
					radioButton4.setText("Disabled");
					panel11.add(radioButton4, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel6.add(panel11, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel12 ========
				{
					panel12.setLayout(new GridBagLayout());
					((GridBagLayout)panel12.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 148, 209, 0};
					((GridBagLayout)panel12.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel12.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel12.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
					panel12.add(hSpacer4, new GridBagConstraints(0, 0, 4, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- label15 ----
					label15.setText("Date: ");
					panel12.add(label15, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
					panel12.add(textField10, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- label16 ----
					label16.setText("(format like \"20110131\")");
					label16.setFont(label16.getFont().deriveFont(label16.getFont().getSize() - 2f));
					panel12.add(label16, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel6.add(panel12, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel19 ========
				{
					panel19.setLayout(new GridBagLayout());
					((GridBagLayout)panel19.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel19.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel19.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel19.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- label24 ----
					label24.setText("Tip: Snapshot output = app/output/snapshot");
					label24.setFont(label24.getFont().deriveFont(label24.getFont().getSize() - 3f));
					panel19.add(label24, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel6.add(panel19, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel13 ========
				{
					panel13.setLayout(new GridBagLayout());
					((GridBagLayout)panel13.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
					((GridBagLayout)panel13.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel13.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel13.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- label17 ----
					label17.setText("Delta generation:");
					panel13.add(label17, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
				}
				panel6.add(panel13, new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel14 ========
				{
					panel14.setLayout(new GridBagLayout());
					((GridBagLayout)panel14.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0};
					((GridBagLayout)panel14.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel14.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel14.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
					panel14.add(hSpacer5, new GridBagConstraints(0, 0, 4, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- radioButton5 ----
					radioButton5.setText("Enabled");
					panel14.add(radioButton5, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- radioButton6 ----
					radioButton6.setText("Disabled");
					panel14.add(radioButton6, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel6.add(panel14, new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel15 ========
				{
					panel15.setLayout(new GridBagLayout());
					((GridBagLayout)panel15.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 148, 209, 0};
					((GridBagLayout)panel15.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel15.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel15.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
					panel15.add(hSpacer6, new GridBagConstraints(0, 0, 4, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- label18 ----
					label18.setText("Initial date: ");
					panel15.add(label18, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
					panel15.add(textField11, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- label19 ----
					label19.setText("(format like \"20110131\")");
					label19.setFont(label19.getFont().deriveFont(label19.getFont().getSize() - 2f));
					panel15.add(label19, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel6.add(panel15, new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel16 ========
				{
					panel16.setLayout(new GridBagLayout());
					((GridBagLayout)panel16.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 148, 209, 0};
					((GridBagLayout)panel16.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel16.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel16.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
					panel16.add(hSpacer7, new GridBagConstraints(0, 0, 4, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- label20 ----
					label20.setText("Final date: ");
					panel16.add(label20, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
					panel16.add(textField12, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- label21 ----
					label21.setText("(format like \"20110131\")");
					label21.setFont(label21.getFont().deriveFont(label21.getFont().getSize() - 2f));
					panel16.add(label21, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel6.add(panel16, new GridBagConstraints(0, 12, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel18 ========
				{
					panel18.setLayout(new GridBagLayout());
					((GridBagLayout)panel18.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel18.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel18.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel18.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- label23 ----
					label23.setText("Tip: Delta output = app/output/delta");
					label23.setFont(label23.getFont().deriveFont(label23.getFont().getSize() - 3f));
					panel18.add(label23, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel6.add(panel18, new GridBagConstraints(0, 13, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			tabbedPane1.addTab("Process preferences", panel6);

		}
		add(tabbedPane1, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//======== panel3 ========
		{
			panel3.setLayout(new GridBagLayout());
			((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0};
			((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
			panel3.add(comboBox1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button11 ----
			button11.setText("Add profile");
			panel3.add(button11, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button1 ----
			button1.setText("Start conversion");
			button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel3.add(button1, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel3, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(radioButton1);
		buttonGroup1.add(radioButton2);

		//---- buttonGroup2 ----
		ButtonGroup buttonGroup2 = new ButtonGroup();
		buttonGroup2.add(radioButton3);
		buttonGroup2.add(radioButton4);

		//---- buttonGroup3 ----
		ButtonGroup buttonGroup3 = new ButtonGroup();
		buttonGroup3.add(radioButton5);
		buttonGroup3.add(radioButton6);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel1;
	private JLabel label1;
	private JPanel panel2;
	private JLabel label2;
	private JTabbedPane tabbedPane1;
	private JPanel panel4;
	private JPanel panel5;
	private JButton button10;
	private JLabel label3;
	private JTextField textField1;
	private JButton button2;
	private JLabel label4;
	private JTextField textField2;
	private JButton button3;
	private JLabel label5;
	private JTextField textField3;
	private JButton button4;
	private JLabel label6;
	private JTextField textField4;
	private JButton button5;
	private JLabel label7;
	private JTextField textField5;
	private JButton button6;
	private JLabel label8;
	private JTextField textField6;
	private JButton button7;
	private JLabel label9;
	private JTextField textField7;
	private JButton button8;
	private JLabel label10;
	private JTextField textField8;
	private JButton button9;
	private JPanel panel20;
	private JPanel panel21;
	private JPanel panel6;
	private JPanel panel7;
	private JLabel label11;
	private JPanel panel8;
	private JPanel hSpacer1;
	private JRadioButton radioButton1;
	private JRadioButton radioButton2;
	private JPanel panel9;
	private JPanel hSpacer2;
	private JLabel label12;
	private JTextField textField9;
	private JLabel label13;
	private JPanel panel17;
	private JLabel label22;
	private JPanel panel10;
	private JLabel label14;
	private JPanel panel11;
	private JPanel hSpacer3;
	private JRadioButton radioButton3;
	private JRadioButton radioButton4;
	private JPanel panel12;
	private JPanel hSpacer4;
	private JLabel label15;
	private JTextField textField10;
	private JLabel label16;
	private JPanel panel19;
	private JLabel label24;
	private JPanel panel13;
	private JLabel label17;
	private JPanel panel14;
	private JPanel hSpacer5;
	private JRadioButton radioButton5;
	private JRadioButton radioButton6;
	private JPanel panel15;
	private JPanel hSpacer6;
	private JLabel label18;
	private JTextField textField11;
	private JLabel label19;
	private JPanel panel16;
	private JPanel hSpacer7;
	private JLabel label20;
	private JTextField textField12;
	private JLabel label21;
	private JPanel panel18;
	private JLabel label23;
	private JPanel panel3;
	private JComboBox comboBox1;
	private JButton button11;
	private JButton button1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
