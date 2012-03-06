/*
 * Created by JFormDesigner on Tue Mar 06 16:15:08 GMT-03:00 2012
 */

package org.ihtsdo.translation.ui.translation;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * @author Guillermo Reynoso
 */
public class DescriptionPanel extends JPanel {
	public DescriptionPanel() {
		initComponents();
	}

	private void targetTextFieldMouseClicked(MouseEvent e) {
		// TODO add your code here
	}

	private void label13MouseClicked(MouseEvent e) {
		// TODO add your code here
	}

	private void bLaunchActionPerformed() {
		// TODO add your code here
	}

	private void button1ActionPerformed() {
		// TODO add your code here
	}

	private void label12MouseClicked(MouseEvent e) {
		// TODO add your code here
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel2 = new JPanel();
		panel15 = new JPanel();
		separator1 = new JSeparator();
		label2 = new JLabel();
		scrollPane5 = new JScrollPane();
		targetTextField = new JTextArea();
		panel18 = new JPanel();
		label1 = new JLabel();
		comboBox1 = new JComboBox();
		label7 = new JLabel();
		rbAct = new JRadioButton();
		rbInact = new JRadioButton();
		label5 = new JLabel();
		cmbAccep = new JComboBox();
		label3 = new JLabel();
		rbYes = new JRadioButton();
		rbNo = new JRadioButton();
		label13 = new JLabel();
		panel19 = new JPanel();
		label4 = new JLabel();
		cmbActions = new JComboBox();
		bLaunch = new JButton();
		button1 = new JButton();
		label12 = new JLabel();

		//======== this ========
		setLayout(new BorderLayout());

		//======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {1.0, 1.0, 1.0E-4};

			//======== panel15 ========
			{
				panel15.setMinimumSize(new Dimension(10, 20));
				panel15.setPreferredSize(new Dimension(10, 20));
				panel15.setLayout(new GridBagLayout());
				((GridBagLayout)panel15.getLayout()).columnWidths = new int[] {80, 0, 0};
				((GridBagLayout)panel15.getLayout()).rowHeights = new int[] {18, 0, 0};
				((GridBagLayout)panel15.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
				((GridBagLayout)panel15.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};
				panel15.add(separator1, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//---- label2 ----
				label2.setText("Term:");
				label2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel15.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
					new Insets(0, 0, 0, 5), 0, 0));

				//======== scrollPane5 ========
				{

					//---- targetTextField ----
					targetTextField.setRows(5);
					targetTextField.setLineWrap(true);
					targetTextField.setPreferredSize(new Dimension(0, 32));
					targetTextField.setMinimumSize(new Dimension(0, 32));
					targetTextField.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					targetTextField.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							targetTextFieldMouseClicked(e);
						}
					});
					scrollPane5.setViewportView(targetTextField);
				}
				panel15.add(scrollPane5, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			panel2.add(panel15, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//======== panel18 ========
			{
				panel18.setPreferredSize(new Dimension(10, 12));
				panel18.setMinimumSize(new Dimension(10, 12));
				panel18.setLayout(new GridBagLayout());
				((GridBagLayout)panel18.getLayout()).columnWidths = new int[] {0, 248, 0, 0, 0, 0, 0};
				((GridBagLayout)panel18.getLayout()).rowHeights = new int[] {0, 0, 18, 0};
				((GridBagLayout)panel18.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0, 0.0, 1.0, 1.0, 1.0E-4};
				((GridBagLayout)panel18.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

				//---- label1 ----
				label1.setText("Term type");
				label1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel18.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- comboBox1 ----
				comboBox1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel18.add(comboBox1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label7 ----
				label7.setText("Status:");
				label7.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel18.add(label7, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- rbAct ----
				rbAct.setText("Active");
				rbAct.setSelected(true);
				rbAct.setBackground(new Color(238, 238, 238));
				rbAct.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel18.add(rbAct, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- rbInact ----
				rbInact.setText("Inactive");
				rbInact.setBackground(new Color(238, 238, 238));
				rbInact.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel18.add(rbInact, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//---- label5 ----
				label5.setText("Acceptability:");
				label5.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel18.add(label5, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- cmbAccep ----
				cmbAccep.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel18.add(cmbAccep, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label3 ----
				label3.setText("Is case significant?");
				label3.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel18.add(label3, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- rbYes ----
				rbYes.setText("Yes");
				rbYes.setBackground(new Color(238, 238, 238));
				rbYes.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel18.add(rbYes, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- rbNo ----
				rbNo.setSelected(true);
				rbNo.setText("No");
				rbNo.setBackground(new Color(238, 238, 238));
				rbNo.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel18.add(rbNo, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//---- label13 ----
				label13.setText("text");
				label13.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				label13.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						label13MouseClicked(e);
					}
				});
				panel18.add(label13, new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			panel2.add(panel18, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, BorderLayout.CENTER);

		//======== panel19 ========
		{
			panel19.setLayout(new GridBagLayout());
			((GridBagLayout)panel19.getLayout()).columnWidths = new int[] {77, 172, 0, 0, 0, 0};
			((GridBagLayout)panel19.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel19.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel19.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- label4 ----
			label4.setText("Action");
			label4.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel19.add(label4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- cmbActions ----
			cmbActions.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel19.add(cmbActions, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- bLaunch ----
			bLaunch.setText("Save");
			bLaunch.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			bLaunch.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					bLaunchActionPerformed();
				}
			});
			panel19.add(bLaunch, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button1 ----
			button1.setText("Cancel");
			button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed();
				}
			});
			panel19.add(button1, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label12 ----
			label12.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					label12MouseClicked(e);
				}
			});
			panel19.add(label12, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel19, BorderLayout.SOUTH);

		//---- buttonGroup3 ----
		ButtonGroup buttonGroup3 = new ButtonGroup();
		buttonGroup3.add(rbAct);
		buttonGroup3.add(rbInact);

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(rbYes);
		buttonGroup1.add(rbNo);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel2;
	private JPanel panel15;
	private JSeparator separator1;
	private JLabel label2;
	private JScrollPane scrollPane5;
	private JTextArea targetTextField;
	private JPanel panel18;
	private JLabel label1;
	private JComboBox comboBox1;
	private JLabel label7;
	private JRadioButton rbAct;
	private JRadioButton rbInact;
	private JLabel label5;
	private JComboBox cmbAccep;
	private JLabel label3;
	private JRadioButton rbYes;
	private JRadioButton rbNo;
	private JLabel label13;
	private JPanel panel19;
	private JLabel label4;
	private JComboBox cmbActions;
	private JButton bLaunch;
	private JButton button1;
	private JLabel label12;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
