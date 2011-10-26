/*
 * Created by JFormDesigner on Mon Apr 05 12:50:50 GMT-04:00 2010
 */

package org.ihtsdo.translation.ui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.document.DocumentManager;

/**
 * @author Guillermo Reynoso
 */
public class AddLinguisticGuideline extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public AddLinguisticGuideline() {
		initComponents();
	}

	private void button1ActionPerformed(ActionEvent e) {
		try {
			I_GetConceptData linguisticGuidelinesRoot = Terms.get().getConcept(
					ArchitectonicAuxiliary.Concept.LINGUISTIC_GUIDELINES_ROOT.getUids());
			if (textField1.getText().length() > 0 && textField2.getText().length() > 0 &&
					textArea1.getText().length() > 0) {
				DocumentManager.addNewLinguisticGuideline(textField1.getText(), textField2.getText(), 
						textArea1.getText(), linguisticGuidelinesRoot, Terms.get().getActiveAceFrameConfig());
				Terms.get().commit();
				JOptionPane.showMessageDialog(this,
						"Guideline added!",
						"Info",
						JOptionPane.OK_OPTION);
				textField1.setText("");
				textField2.setText("");
				textArea1.setText("");
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(this,
					e1.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label1 = new JLabel();
		panel2 = new JPanel();
		label2 = new JLabel();
		textField1 = new JTextField();
		label3 = new JLabel();
		textField2 = new JTextField();
		label4 = new JLabel();
		scrollPane1 = new JScrollPane();
		textArea1 = new JTextArea();
		panel1 = new JPanel();
		button1 = new JButton();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

		//---- label1 ----
		label1.setText("Create new Linguistic Guideline");
		add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {169, 451, 0, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};

			//---- label2 ----
			label2.setText("Guideline Title (FSN):");
			panel2.add(label2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));
			panel2.add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label3 ----
			label3.setText("Pattern (Pref. term):");
			panel2.add(label3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));
			panel2.add(textField2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//---- label4 ----
			label4.setText("Recommendation:");
			panel2.add(label4, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

			//======== scrollPane1 ========
			{
				scrollPane1.setViewportView(textArea1);
			}
			panel2.add(scrollPane1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));
		}
		add(panel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- button1 ----
			button1.setText("Save as new concept");
			button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed(e);
				}
			});
			panel1.add(button1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JLabel label1;
	private JPanel panel2;
	private JLabel label2;
	private JTextField textField1;
	private JLabel label3;
	private JTextField textField2;
	private JLabel label4;
	private JScrollPane scrollPane1;
	private JTextArea textArea1;
	private JPanel panel1;
	private JButton button1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
