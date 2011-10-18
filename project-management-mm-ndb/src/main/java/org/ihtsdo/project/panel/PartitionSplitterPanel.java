/*
 * Created by JFormDesigner on Thu Mar 18 14:40:06 GMT-04:00 2010
 */

package org.ihtsdo.project.panel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.*;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.model.PartitionScheme;
import org.ihtsdo.project.refset.partition.RefsetSplitter;

/**
 * @author Guillermo Reynoso
 */
public class PartitionSplitterPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PartitionScheme partitionScheme;
	private I_ConfigAceFrame config;
	private RefsetPartitionerPanel partitionerPanel;
	private RefsetSplitter refsetSplitter;

	public PartitionSplitterPanel(PartitionScheme partitionScheme, RefsetPartitionerPanel partitionerPanel,
			I_ConfigAceFrame config) {
		this.partitionScheme = partitionScheme;
		this.partitionerPanel = partitionerPanel;
		this.config = config;
		initComponents();
		pBarW.setVisible(false);
		refsetSplitter = new RefsetSplitter();
		editorPane1.setContentType("text/html");
	}

	private void button3ActionPerformed(ActionEvent e) {
		partitionerPanel.updateList1Content();
		partitionerPanel.updatePanel7Content();
	}

	private List<Integer> getPortionsFromTextField() {
		List<Integer> portions = new ArrayList<Integer>();
		split:
			for (String portion : textField1.getText().replace(" ", "").split(",")) {
				try {
				Integer number = Integer.parseInt(portion);
				portions.add(number);
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(this,
							"Error: portion '" + portion + "' is not a valid number",
							"Error",
							JOptionPane.ERROR_MESSAGE);
					break split;
				}
			}
		return portions;
	}

	private void button1ActionPerformed(ActionEvent e) {
		try {
			editorPane1.setText("");
			editorPane1.validate();
			List<List<I_GetConceptData>> conceptsSets = refsetSplitter.calculatePartitions(
					partitionScheme, getPortionsFromTextField(), 
					textField3.getText(), config);
			String textAreaMessage = "<html><body>Calculated portions: <br><br>";
			int count = 0;
			for (List<I_GetConceptData> conceptsSet : conceptsSets) {
				count++;
				textAreaMessage = textAreaMessage + "Partition " + count + " (" + 
				textField3.getText() + " " + count + "): " + 
				conceptsSet.size() + " concepts.<br>";
			}
			editorPane1.setText(textAreaMessage);
			editorPane1.validate();
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(this,
					e1.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}
	}

	private void button2ActionPerformed(ActionEvent e) {
		try {
			if (textField3.getText() == null) throw new Exception("Name prefix must not be empty.");
			if (textField3.getText().length() < 6) throw new Exception("Name prefix must be at least 6 characters.");

			config.getChildrenExpandedNodes().clear();
			pBarW.setMinimum(0);
			pBarW.setMaximum(100);
			pBarW.setIndeterminate(true);
			pBarW.setVisible(true);
			pBarW.repaint();
			pBarW.revalidate();
			panel2.repaint();
			panel2.revalidate();
			repaint();
			revalidate();

			SwingUtilities.invokeLater(new Runnable(){
				public void run(){

					Thread appThr=new Thread(){
						public void run(){
							try {
								refsetSplitter.splitRefset(
										partitionScheme, getPortionsFromTextField(), 
										textField3.getText(), config);
								Terms.get().commit();
							} catch (Exception e) {
								e.printStackTrace();
								JOptionPane.showMessageDialog(PartitionSplitterPanel.this,
										e.getMessage(),
										"Error",
										JOptionPane.ERROR_MESSAGE);
							}
							partitionerPanel.updateList1Content();
							partitionerPanel.updatePanel7Content();
							pBarW.setVisible(false);

							SwingUtilities.invokeLater(new Runnable(){
								public void run(){
									TranslationHelperPanel.refreshProjectPanelNode(config);
								}
							});
						}
					};
					appThr.start();
				}
			});

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
		panel1 = new JPanel();
		label4 = new JLabel();
		textField3 = new JTextField();
		label2 = new JLabel();
		textField1 = new JTextField();
		panel3 = new JPanel();
		label3 = new JLabel();
		scrollPane1 = new JScrollPane();
		editorPane1 = new JEditorPane();
		panel2 = new JPanel();
		button1 = new JButton();
		button2 = new JButton();
		button3 = new JButton();
		pBarW = new JProgressBar();

		//======== this ========
		setBorder(new LineBorder(Color.lightGray));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0, 0.0, 1.0E-4};

		//---- label1 ----
		label1.setText("Refset Splitter");
		label1.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {165, 262, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

			//---- label4 ----
			label4.setText("New partitions name prefix");
			panel1.add(label4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));
			panel1.add(textField3, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//---- label2 ----
			label2.setText("Split pattern (%)");
			panel1.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
			panel1.add(textField1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel3 ========
		{
			panel3.setLayout(new GridBagLayout());
			((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
			((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- label3 ----
			label3.setText("Example: 25,25,50 for three partitions, must sum 100%");
			label3.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
			panel3.add(label3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== scrollPane1 ========
		{

			//---- editorPane1 ----
			editorPane1.setEditable(false);
			scrollPane1.setViewportView(editorPane1);
		}
		add(scrollPane1, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- button1 ----
			button1.setText("Calculate partitions");
			button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed(e);
				}
			});
			panel2.add(button1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button2 ----
			button2.setText("Save");
			button2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					button2ActionPerformed(e);
				}
			});
			panel2.add(button2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button3 ----
			button3.setText("Cancel");
			button3.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button3.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					button3ActionPerformed(e);
				}
			});
			panel2.add(button3, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));
			panel2.add(pBarW, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JLabel label1;
	private JPanel panel1;
	private JLabel label4;
	private JTextField textField3;
	private JLabel label2;
	private JTextField textField1;
	private JPanel panel3;
	private JLabel label3;
	private JScrollPane scrollPane1;
	private JEditorPane editorPane1;
	private JPanel panel2;
	private JButton button1;
	private JButton button2;
	private JButton button3;
	private JProgressBar pBarW;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
