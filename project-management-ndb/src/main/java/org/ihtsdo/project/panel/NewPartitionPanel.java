/*
 * Created by JFormDesigner on Thu Mar 18 11:06:42 GMT-04:00 2010
 */

package org.ihtsdo.project.panel;

import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.model.PartitionScheme;
import org.ihtsdo.project.panel.details.ProjectDetailsPanel;
import org.ihtsdo.project.panel.dnd.GetPartitionMember;
import org.ihtsdo.project.panel.dnd.ListConceptDragSourceListener;
import org.ihtsdo.project.panel.dnd.ListDragGestureListenerWithImage;
import org.ihtsdo.project.panel.dnd.ObjectTransferHandler;
import org.ihtsdo.project.panel.dnd.RemovePartitionMember;
import org.ihtsdo.project.refset.partition.IsDescendantOfPartitioner;
import org.ihtsdo.project.refset.partition.ListPartitioner;
import org.ihtsdo.project.refset.partition.QuantityPartitioner;
import org.ihtsdo.project.refset.partition.RefsetPartitioner;
import org.ihtsdo.project.refset.partition.StringMatchPartitioner;

/**
 * @author Guillermo Reynoso
 */

public class NewPartitionPanel extends JPanel {

	private PartitionScheme partitionScheme;
	private I_ConfigAceFrame config;
	private RefsetPartitionerPanel partitionerPanel;
	private JList conceptList;
	private DefaultListModel conceptListModel;
	private ObjectTransferHandler conceptDnDHandler;
	private JTextField integerField;
	private JTextField stringField;
	private RefsetPartitioner refsetPartitioner;
	private DefaultListModel list1Model;
	private ObjectTransferHandler ConceptDnDHandler;


	public NewPartitionPanel(PartitionScheme partitionScheme, RefsetPartitionerPanel partitionerPanel,
			I_ConfigAceFrame config) {
		initComponents();
		this.partitionScheme = partitionScheme;
		this.partitionerPanel = partitionerPanel;
		
		//Init is descendant of concept list
		this.conceptList = new JList();
		this.conceptListModel= new DefaultListModel();
		
		conceptListModel.addListDataListener(new ListDataListener() {

			@Override
			public void intervalRemoved(ListDataEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void intervalAdded(ListDataEvent arg0) {
				//REFRESH THE MEMBERS LIST HERE.
			}
			@Override
			public void contentsChanged(ListDataEvent arg0) {
				
			}
		});
		conceptList.setModel(conceptListModel);

		conceptList.setName(ProjectDetailsPanel.TARGET_LIST_NAME);
		conceptList.setMinimumSize(new Dimension(250,25));
		conceptList.setPreferredSize(new Dimension(250,25));
		conceptList.setMaximumSize(new Dimension(250,25));
		conceptList.setBorder(new BevelBorder(BevelBorder.LOWERED));
		conceptDnDHandler =	new ObjectTransferHandler(this.config,null);
		conceptList.setTransferHandler(conceptDnDHandler);
		//END
		
		this.config = config;
		pBarW.setVisible(false);
		try {
			integerField = new JTextField(10);
			stringField = new JTextField(10);

			fillComboBox1Options();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					e.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} 		
		ConceptDnDHandler=new ObjectTransferHandler(config,null);

		list1Model = new DefaultListModel();
		list1.setModel(list1Model);

		
	}

	private void updateRefsetPartitionerData() {
		refsetPartitioner = (RefsetPartitioner) comboBox1.getSelectedItem();
		if (comboBox1.getSelectedItem().toString().equals("Is descendant of")) {
			IsDescendantOfPartitioner isDescendantOfPartitioner = (IsDescendantOfPartitioner) refsetPartitioner;
			try {
				isDescendantOfPartitioner.setParent(
						Terms.get().getConcept(((I_GetConceptData)conceptListModel.getElementAt(0)).getNid()));
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this,
						"Error retrieving parent concept",
						"Error",
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			refsetPartitioner = isDescendantOfPartitioner;
		} else if (comboBox1.getSelectedItem().toString().equals("Quantity")) {
			QuantityPartitioner quantityPartitioner = (QuantityPartitioner) refsetPartitioner;
			quantityPartitioner.setQuantity(Integer.parseInt(integerField.getText()));
			refsetPartitioner = quantityPartitioner;
		} else if (comboBox1.getSelectedItem().toString().equals("String match")) {
			StringMatchPartitioner stringMatchPartitioner = (StringMatchPartitioner) refsetPartitioner;
			stringMatchPartitioner.setPattern(stringField.getText());
			refsetPartitioner = stringMatchPartitioner;
		}else if (comboBox1.getSelectedItem().toString().equals("Dropping concepts")) {
			ListPartitioner listPartitioner = (ListPartitioner) refsetPartitioner;
			refsetPartitioner = listPartitioner;
		}
	}

	private void fillComboBox1Options() {
		RefsetPartitioner isDescendantOfPartitioner = new IsDescendantOfPartitioner();
		comboBox1.addItem(isDescendantOfPartitioner);
		RefsetPartitioner quantityPartitioner = new QuantityPartitioner();
		comboBox1.addItem(quantityPartitioner);
		RefsetPartitioner stringMatchPartitioner = new StringMatchPartitioner();
		comboBox1.addItem(stringMatchPartitioner);
		ListPartitioner listPartitioner = new ListPartitioner(list1);
		comboBox1.addItem(listPartitioner);
	}

	private void button2ActionPerformed(ActionEvent e) {
		partitionerPanel.updateList1Content();
		partitionerPanel.updatePanel7Content();
	}

	private void comboBox1ActionPerformed(ActionEvent e) {
		if (comboBox1.getSelectedItem().toString().equals("Is descendant of")) {
			for (Component component : panel8.getComponents()) {
				panel8.remove(component);
			}
			list1.setTransferHandler(null);
			panel8.add(new JLabel("Parent concept:"));
			panel8.add(conceptList);
		} else if (comboBox1.getSelectedItem().toString().equals("Quantity")) {
			for (Component component : panel8.getComponents()) {
				panel8.remove(component);
			}
			list1.setTransferHandler(null);
			panel8.add(new JLabel("Number of concepts:"));
			panel8.add(integerField);
		} else if (comboBox1.getSelectedItem().toString().equals("String match")) {
			for (Component component : panel8.getComponents()) {
				panel8.remove(component);
			}
			list1.setTransferHandler(null);
			panel8.add(new JLabel("String pattern:"));
			panel8.add(stringField);
		} else if (comboBox1.getSelectedItem().toString().equals("Dropping concepts")) {
			for (Component component : panel8.getComponents()) {
				panel8.remove(component);
			}
			list1.setTransferHandler(ConceptDnDHandler);
		}
		panel8.validate();
		panel8.repaint();
	}

	private void updateList1Content(List<I_GetConceptData> concepts) {
		Collections.sort(concepts,
				new Comparator<I_GetConceptData>()
				{
					public int compare(I_GetConceptData f1, I_GetConceptData f2)
					{
						return f1.toString().compareTo(f2.toString());
					}
				});
		list1Model = new DefaultListModel();
		for (I_GetConceptData concept : concepts) {
			list1Model.addElement(concept);
		}
		list1.setModel(list1Model);
		list1.validate();
		label2.setText(String.valueOf(concepts.size()));
	}

	private void button3ActionPerformed(ActionEvent e) {
		updateRefsetPartitionerData();
		try {
			updateList1Content(refsetPartitioner.getMembersToInclude(partitionScheme, 
					textField1.getText(), config));
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(this,
					e1.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}
	}

	private void button1ActionPerformed(ActionEvent e) {
		updateRefsetPartitionerData();
		if (textField1.getText() == null) {
			JOptionPane.showMessageDialog(this,
					"Enter a name for the new partition",
					"Error",
					JOptionPane.ERROR_MESSAGE);
		} else 
			if (textField1.getText().trim().length() < 6) {
				JOptionPane.showMessageDialog(this,
						"Name needs to have at least 6 characters",
						"Error",
						JOptionPane.ERROR_MESSAGE);
			} else {
				try {
					config.getChildrenExpandedNodes().clear();
					pBarW.setMinimum(0);
					pBarW.setMaximum(100);
					pBarW.setIndeterminate(true);
					pBarW.setVisible(true);
					pBarW.repaint();
					pBarW.revalidate();
					panel4.repaint();
					panel4.revalidate();
					repaint();
					revalidate();

					SwingUtilities.invokeLater(new Runnable(){
						public void run(){

							Thread appThr=new Thread(){
								public void run(){
									try {
										refsetPartitioner.createPartition(partitionScheme, textField1.getText(), config);
										Terms.get().commit();
									} catch (Exception e) {
										e.printStackTrace();
										JOptionPane.showMessageDialog(NewPartitionPanel.this,
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
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel2 = new JPanel();
		panel5 = new JPanel();
		panel1 = new JPanel();
		label1 = new JLabel();
		textField1 = new JTextField();
		comboBox1 = new JComboBox();
		panel8 = new JPanel();
		panel6 = new JPanel();
		panel3 = new JPanel();
		panel7 = new JPanel();
		label3 = new JLabel();
		label2 = new JLabel();
		scrollPane1 = new JScrollPane();
		list1 = new JList();
		panel4 = new JPanel();
		pBarW = new JProgressBar();
		button3 = new JButton();
		button1 = new JButton();
		button2 = new JButton();

		//======== this ========
		setBorder(new LineBorder(Color.lightGray));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {396, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0E-4};

		//======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {327, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

			//======== panel5 ========
			{
				panel5.setLayout(new GridBagLayout());
				((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 0};
				((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
				((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
				((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0, 1.0E-4};

				//======== panel1 ========
				{
					panel1.setLayout(new GridBagLayout());
					((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 243, 0};
					((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- label1 ----
					label1.setText("Partition name:");
					panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
					panel1.add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel5.add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//---- comboBox1 ----
				comboBox1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						comboBox1ActionPerformed(e);
					}
				});
				panel5.add(comboBox1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel8 ========
				{
					panel8.setBorder(null);
					panel8.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
				}
				panel5.add(panel8, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel6 ========
				{
					panel6.setLayout(new GridBagLayout());
					((GridBagLayout)panel6.getLayout()).columnWidths = new int[] {0, 0};
					((GridBagLayout)panel6.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel6.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
					((GridBagLayout)panel6.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

					//======== panel3 ========
					{
						panel3.setLayout(new GridBagLayout());
						((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {157, 0};
						((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0, 0};
						((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
						((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

						//======== panel7 ========
						{
							panel7.setLayout(new GridBagLayout());
							((GridBagLayout)panel7.getLayout()).columnWidths = new int[] {0, 0, 0};
							((GridBagLayout)panel7.getLayout()).rowHeights = new int[] {0, 0};
							((GridBagLayout)panel7.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
							((GridBagLayout)panel7.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

							//---- label3 ----
							label3.setText("Members");
							panel7.add(label3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 0, 5), 0, 0));

							//---- label2 ----
							label2.setText("(-)");
							label2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
							panel7.add(label2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 0, 0), 0, 0));
						}
						panel3.add(panel7, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//======== scrollPane1 ========
						{
							scrollPane1.setViewportView(list1);
						}
						panel3.add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					panel6.add(panel3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel5.add(panel6, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			panel2.add(panel5, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel4 ========
		{
			panel4.setLayout(new GridBagLayout());
			((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {55, 0, 0, 0, 0};
			((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- pBarW ----
			pBarW.setIndeterminate(true);
			panel4.add(pBarW, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button3 ----
			button3.setText("Preview");
			button3.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button3.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button3ActionPerformed(e);
				}
			});
			panel4.add(button3, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button1 ----
			button1.setText("Save");
			button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed(e);
				}
			});
			panel4.add(button1, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button2 ----
			button2.setText("Cancel");
			button2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button2ActionPerformed(e);
				}
			});
			panel4.add(button2, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel4, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel2;
	private JPanel panel5;
	private JPanel panel1;
	private JLabel label1;
	private JTextField textField1;
	private JComboBox comboBox1;
	private JPanel panel8;
	private JPanel panel6;
	private JPanel panel3;
	private JPanel panel7;
	private JLabel label3;
	private JLabel label2;
	private JScrollPane scrollPane1;
	private JList list1;
	private JPanel panel4;
	private JProgressBar pBarW;
	private JButton button3;
	private JButton button1;
	private JButton button2;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
