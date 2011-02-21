/*
 * Created by JFormDesigner on Tue Mar 23 15:14:02 GMT-04:00 2010
 */

package org.ihtsdo.project.panel.details;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.model.Partition;
import org.ihtsdo.project.model.PartitionScheme;
import org.ihtsdo.project.panel.RefsetPartitionerPanel;
import org.ihtsdo.project.panel.TranslationHelperPanel;
import org.ihtsdo.project.util.IconUtilities;

/**
 * @author Guillermo Reynoso
 */
public class PartitionSchemeDetailsPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PartitionScheme partitionScheme;
	private I_ConfigAceFrame config;
	private DefaultListModel list2Model;

	public PartitionSchemeDetailsPanel(PartitionScheme partitionScheme, I_ConfigAceFrame config) {
		initComponents();
		this.partitionScheme = partitionScheme;
		this.config = config;

		I_TermFactory termFactory = Terms.get();

		try {
			label11.setIcon(IconUtilities.helpIcon);
			label11.setText("");		
			textField1.setText(partitionScheme.getName());
			I_GetConceptData refset = partitionScheme.getSourceRefset(config);
			label9.setText(refset.toString());

			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			Set<? extends I_GetConceptData> parents = refset.getSourceRelTargets(
					config.getAllowedStatus(), allowedDestRelTypes, 
					config.getViewPositionSetReadOnly(), config.getPrecedence(),
					config.getConflictResolutionStrategy());

			I_GetConceptData parent = parents.iterator().next();

			if (parent.getConceptNid() == ArchitectonicAuxiliary.Concept.WORKSETS_ROOT.localize().getNid()) {
				label10.setText("WorkSet");
			} else if (parent.getConceptNid() == ArchitectonicAuxiliary.Concept.PARTITIONS_ROOT.localize().getNid()) {
				label10.setText("Partition");
			} else {
				label10.setText("Refset");
			}

			button4.setEnabled(false);

			updateList2Content();

			button1.setEnabled(false);
			
			boolean isPartitioningManager = TerminologyProjectDAO.checkPermissionForProject(
					config.getDbConfig().getUserConcept(), 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.localize().getNid()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.PARTITIONING_MANAGER_ROLE.localize().getNid()), 
					config);
			
			if (!isPartitioningManager) {
				button1.setVisible(false);
				button2.setVisible(false);
				button3.setVisible(false);
				button4.setVisible(false);
				button5.setVisible(false);
				textField1.setEditable(false);
				label6.setVisible(false);
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					e.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void updateList2Content() {
		list2Model = new DefaultListModel();
		List<Partition> partitions = partitionScheme.getPartitions();
		Collections.sort(partitions,
				new Comparator<Partition>()
				{
					public int compare(Partition f1, Partition f2)
					{
						return f1.toString().compareTo(f2.toString());
					}
				});
		for (Partition partition : partitions) {
			list2Model.addElement(partition);
		}
		list2.setModel(list2Model);
		list2.validate();
	}

	private void textField1KeyTyped(KeyEvent e) {
		if (textField1.getText().equals(partitionScheme.getName())) {
			button4.setEnabled(false);
		} else {
			button4.setEnabled(true);
		}
	}

	private void button4ActionPerformed(ActionEvent e) {
		partitionScheme.setName(textField1.getText());
		TerminologyProjectDAO.updatePartitionSchemeMetadata(partitionScheme, config);
		try {
			Terms.get().commit();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		button4.setEnabled(false);
		JOptionPane.showMessageDialog(this,
				"Partition scheme saved!", 
				"Message", JOptionPane.INFORMATION_MESSAGE);
		TranslationHelperPanel.refreshProjectPanelNode(config);
	}

	private void button3ActionPerformed(ActionEvent e) {
		// retire partition scheme
		int n = JOptionPane.showConfirmDialog(
				this,
				"Would you like to retire the partition scheme?",
				"Confirmation",
				JOptionPane.YES_NO_OPTION);

		if (n==0) {
			try {
				TerminologyProjectDAO.retirePartitionScheme(partitionScheme, config);
				Terms.get().commit();
				JOptionPane.showMessageDialog(this,
						"Partition scheme retired!", 
						"Message", JOptionPane.INFORMATION_MESSAGE);
				TranslationHelperPanel.refreshProjectPanelParentNode(config);
				TranslationHelperPanel.closeProjectDetailsTab(config);
			} catch (Exception e3) {
				JOptionPane.showMessageDialog(this,
						e3.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE);
				e3.printStackTrace();
			}
		}
	}

	private void button1ActionPerformed(ActionEvent e) {
		// combine partitions
		try {
			List<Partition> partitionsToCombine = new ArrayList<Partition>();
			if(list2.getSelectedIndices().length > 0) {
				int[] selectedIndices = list2.getSelectedIndices();

				for (int i : selectedIndices) {
					partitionsToCombine.add((Partition)list2Model.getElementAt(i));
				}
				String newPartitionName = JOptionPane.showInputDialog(null, "This process will combine\n" +
						"the selected (" + partitionsToCombine.size() + 
						") partitions into a new one.\n" +
						"Enter new Partition Name : ", 
						"", 1);
				if (newPartitionName != null) {
					TerminologyProjectDAO.combinePartitions(partitionsToCombine, newPartitionName, config);
					Terms.get().commit();
					updateList2Content();
					JOptionPane.showMessageDialog(this,
							"Partitions merged!", 
							"Message", JOptionPane.INFORMATION_MESSAGE);
					TranslationHelperPanel.refreshProjectPanelNode(config);
				}

			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(this,
					e1.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		} 
	}

	private void list2ValueChanged(ListSelectionEvent e) {
		if (list2.getSelectedIndices().length > 1) {
			button1.setEnabled(true);
		} else {
			button1.setEnabled(false);
		}
	}

	private void button2ActionPerformed(ActionEvent e) {

		AceFrameConfig afconfig = (AceFrameConfig)config;
		AceFrame ace=afconfig.getAceFrame();
		JTabbedPane tp=ace.getCdePanel().getConceptTabs();
		if (tp!=null){
			int tabCount=tp.getTabCount();
			for (int i=0;i<tabCount;i++){
				if (tp.getTitleAt(i).equals(TranslationHelperPanel.REFSETPARTITIONER_TAB_NAME)){
					tp.remove(i);
					tp.repaint();
					tp.revalidate();
				}
			}
			RefsetPartitionerPanel pmp=new RefsetPartitionerPanel(this.partitionScheme,config);
			tp.addTab(TranslationHelperPanel.REFSETPARTITIONER_TAB_NAME, pmp);
			tp.setSelectedIndex(tp.getTabCount()-1);
			tp.revalidate();
			tp.repaint();
		}

	}

	private void button5ActionPerformed(ActionEvent e) {
		updateList2Content();
	}

	private void label11MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("PARTITION_SCHEME_DETAILS");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		tabbedPane1 = new JTabbedPane();
		panel0 = new JPanel();
		panel1 = new JPanel();
		panel2 = new JPanel();
		label1 = new JLabel();
		panel9 = new JPanel();
		label2 = new JLabel();
		textField1 = new JTextField();
		label3 = new JLabel();
		label9 = new JLabel();
		label4 = new JLabel();
		label10 = new JLabel();
		panel6 = new JPanel();
		label8 = new JLabel();
		panel8 = new JPanel();
		button2 = new JButton();
		button3 = new JButton();
		button4 = new JButton();
		label11 = new JLabel();
		panel10 = new JPanel();
		label5 = new JLabel();
		scrollPane2 = new JScrollPane();
		list2 = new JList();
		panel11 = new JPanel();
		label7 = new JLabel();
		label6 = new JLabel();
		panel7 = new JPanel();
		button5 = new JButton();
		button1 = new JButton();
		panel12 = new JPanel();

		//======== this ========
		setBackground(new Color(238, 238, 238));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

		//======== tabbedPane1 ========
		{

			//======== panel0 ========
			{
				panel0.setLayout(new GridBagLayout());
				((GridBagLayout)panel0.getLayout()).columnWidths = new int[] {0, 0};
				((GridBagLayout)panel0.getLayout()).rowHeights = new int[] {0, 0, 0};
				((GridBagLayout)panel0.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
				((GridBagLayout)panel0.getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0E-4};

				//======== panel1 ========
				{
					panel1.setLayout(new GridBagLayout());
					((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0};
					((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
					((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

					//======== panel2 ========
					{
						panel2.setLayout(new GridBagLayout());
						((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {355, 0};
						((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0};
						((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
						((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

						//---- label1 ----
						label1.setText("Partition scheme details");
						label1.setFont(new Font("Lucida Grande", Font.BOLD, 14));
						panel2.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//======== panel9 ========
						{
							panel9.setLayout(new GridBagLayout());
							((GridBagLayout)panel9.getLayout()).columnWidths = new int[] {0, 0, 0};
							((GridBagLayout)panel9.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
							((GridBagLayout)panel9.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
							((GridBagLayout)panel9.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

							//---- label2 ----
							label2.setText("Name:");
							panel9.add(label2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 5), 0, 0));

							//---- textField1 ----
							textField1.addKeyListener(new KeyAdapter() {
								@Override
								public void keyTyped(KeyEvent e) {
									textField1KeyTyped(e);
								}
							});
							panel9.add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 0), 0, 0));

							//---- label3 ----
							label3.setText("Source refset:");
							panel9.add(label3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 5), 0, 0));

							//---- label9 ----
							label9.setText("text");
							panel9.add(label9, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 0), 0, 0));

							//---- label4 ----
							label4.setText("Refset type:");
							panel9.add(label4, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 0, 5), 0, 0));

							//---- label10 ----
							label10.setText("text");
							panel9.add(label10, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 0, 0), 0, 0));
						}
						panel2.add(panel9, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					panel1.add(panel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//======== panel6 ========
					{
						panel6.setLayout(new GridBagLayout());
						((GridBagLayout)panel6.getLayout()).columnWidths = new int[] {230, 0};
						((GridBagLayout)panel6.getLayout()).rowHeights = new int[] {0, 0};
						((GridBagLayout)panel6.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
						((GridBagLayout)panel6.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

						//---- label8 ----
						label8.setText("<html><body>\nClick \u2018Edit partition\u2019  to display the workset and the new partition panel<br><br>\n\nClick \u2018Retire partition scheme\u2019  to retire a partition scheme. Partitions must be deleted first\n</html>");
						panel6.add(label8, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					panel1.add(panel6, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel0.add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel8 ========
				{
					panel8.setLayout(new GridBagLayout());
					((GridBagLayout)panel8.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
					((GridBagLayout)panel8.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel8.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel8.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- button2 ----
					button2.setText("Edit partitions");
					button2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button2.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button2ActionPerformed(e);
						}
					});
					panel8.add(button2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- button3 ----
					button3.setText("Retire scheme");
					button3.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button3.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button3ActionPerformed(e);
						}
					});
					panel8.add(button3, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- button4 ----
					button4.setText("Save");
					button4.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button4.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button4ActionPerformed(e);
						}
					});
					panel8.add(button4, new GridBagConstraints(12, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- label11 ----
					label11.setText("text");
					label11.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							label11MouseClicked(e);
						}
					});
					panel8.add(label11, new GridBagConstraints(15, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel0.add(panel8, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			tabbedPane1.addTab("Partition scheme", panel0);


			//======== panel10 ========
			{
				panel10.setLayout(new GridBagLayout());
				((GridBagLayout)panel10.getLayout()).columnWidths = new int[] {349, 188, 0};
				((GridBagLayout)panel10.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
				((GridBagLayout)panel10.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
				((GridBagLayout)panel10.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 0.0, 1.0E-4};

				//---- label5 ----
				label5.setText("Partitions");
				panel10.add(label5, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== scrollPane2 ========
				{

					//---- list2 ----
					list2.addListSelectionListener(new ListSelectionListener() {
						@Override
						public void valueChanged(ListSelectionEvent e) {
							list2ValueChanged(e);
						}
					});
					scrollPane2.setViewportView(list2);
				}
				panel10.add(scrollPane2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel11 ========
				{
					panel11.setLayout(new GridBagLayout());
					((GridBagLayout)panel11.getLayout()).columnWidths = new int[] {0, 0};
					((GridBagLayout)panel11.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel11.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
					((GridBagLayout)panel11.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

					//---- label7 ----
					label7.setText("<html><body>\nThe list of partitions is displayed as new partitions are created<br><br>\n\nClick \u2018Refresh\u2019 to update the partitions list\n</html>");
					panel11.add(label7, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel10.add(panel11, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//---- label6 ----
				label6.setText("Control + click for selecting multiple partitions");
				label6.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
				panel10.add(label6, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel7 ========
				{
					panel7.setLayout(new GridBagLayout());
					((GridBagLayout)panel7.getLayout()).columnWidths = new int[] {0, 0, 0};
					((GridBagLayout)panel7.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel7.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel7.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- button5 ----
					button5.setText("Refresh");
					button5.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button5.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button5ActionPerformed(e);
						}
					});
					panel7.add(button5, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- button1 ----
					button1.setText("Merge selected partitions");
					button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button1ActionPerformed(e);
						}
					});
					panel7.add(button1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel10.add(panel7, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 0, 5), 0, 0));

				//======== panel12 ========
				{
					panel12.setLayout(new GridBagLayout());
					((GridBagLayout)panel12.getLayout()).columnWidths = new int[] {0, 0};
					((GridBagLayout)panel12.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel12.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
					((GridBagLayout)panel12.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
				}
				panel10.add(panel12, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			tabbedPane1.addTab("Partitions", panel10);

		}
		add(tabbedPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JTabbedPane tabbedPane1;
	private JPanel panel0;
	private JPanel panel1;
	private JPanel panel2;
	private JLabel label1;
	private JPanel panel9;
	private JLabel label2;
	private JTextField textField1;
	private JLabel label3;
	private JLabel label9;
	private JLabel label4;
	private JLabel label10;
	private JPanel panel6;
	private JLabel label8;
	private JPanel panel8;
	private JButton button2;
	private JButton button3;
	private JButton button4;
	private JLabel label11;
	private JPanel panel10;
	private JLabel label5;
	private JScrollPane scrollPane2;
	private JList list2;
	private JPanel panel11;
	private JLabel label7;
	private JLabel label6;
	private JPanel panel7;
	private JButton button5;
	private JButton button1;
	private JPanel panel12;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
