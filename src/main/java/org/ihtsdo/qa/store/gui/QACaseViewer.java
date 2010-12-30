/*
 * Created by JFormDesigner on Wed Dec 29 13:29:38 GMT-03:00 2010
 */

package org.ihtsdo.qa.store.gui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.*;
import javax.swing.*;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * @author Guillermo Reynoso
 */
public class QACaseViewer extends JDialog {
	public QACaseViewer(Object conceptUuid, Object conceptSctId, Object conceptName,Object status,
			Object dispo, Object assignedTo, Object time) {
		initComponents();
		conceptUuidLabel.setText(""+conceptUuid);
		sctIdLabel.setText(""+conceptSctId);
		conceptNameLabel.setText(""+conceptName);
		statusLabel.setText(""+status);
		dispositionLabel.setText(""+dispo);
		assignedLabel.setText(""+assignedTo);
		timeLabel.setText(""+time);
		this.setSize(640, 250);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private void closeButtonActionPerformed(ActionEvent e) {
		dispose();
	}


	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		label1 = new JLabel();
		conceptUuidLabel = new JLabel();
		label2 = new JLabel();
		sctIdLabel = new JLabel();
		label7 = new JLabel();
		conceptNameLabel = new JLabel();
		label3 = new JLabel();
		statusLabel = new JLabel();
		label4 = new JLabel();
		dispositionLabel = new JLabel();
		label5 = new JLabel();
		assignedLabel = new JLabel();
		label6 = new JLabel();
		timeLabel = new JLabel();
		closeButton = new JButton();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new GridBagLayout());
		((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0};
		((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

		//======== panel1 ========
		{
			panel1.setBorder(new EmptyBorder(5, 5, 5, 5));
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {27, 27, 27, 27, 27, 27, 27, 0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

			//---- label1 ----
			label1.setText("Concept UUID");
			panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 10), 0, 0));
			panel1.add(conceptUuidLabel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 10), 0, 0));

			//---- label2 ----
			label2.setText("Concept Sctid");
			panel1.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 10), 0, 0));
			panel1.add(sctIdLabel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 10), 0, 0));

			//---- label7 ----
			label7.setText("Concept Name");
			panel1.add(label7, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 10), 0, 0));
			panel1.add(conceptNameLabel, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 10), 0, 0));

			//---- label3 ----
			label3.setText("Status");
			panel1.add(label3, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 10), 0, 0));
			panel1.add(statusLabel, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 10), 0, 0));

			//---- label4 ----
			label4.setText("Disposition");
			panel1.add(label4, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 10), 0, 0));
			panel1.add(dispositionLabel, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 10), 0, 0));

			//---- label5 ----
			label5.setText("Assigned to");
			panel1.add(label5, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 10), 0, 0));
			panel1.add(assignedLabel, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 10), 0, 0));

			//---- label6 ----
			label6.setText("Time");
			panel1.add(label6, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 10), 0, 0));
			panel1.add(timeLabel, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 10), 0, 0));

			//---- closeButton ----
			closeButton.setText("Close");
			closeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					closeButtonActionPerformed(e);
				}
			});
			panel1.add(closeButton, new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		contentPane.add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel1;
	private JLabel label1;
	private JLabel conceptUuidLabel;
	private JLabel label2;
	private JLabel sctIdLabel;
	private JLabel label7;
	private JLabel conceptNameLabel;
	private JLabel label3;
	private JLabel statusLabel;
	private JLabel label4;
	private JLabel dispositionLabel;
	private JLabel label5;
	private JLabel assignedLabel;
	private JLabel label6;
	private JLabel timeLabel;
	private JButton closeButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
