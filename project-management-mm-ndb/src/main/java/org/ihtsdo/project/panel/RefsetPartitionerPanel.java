/*
 * Created by JFormDesigner on Thu Mar 18 10:50:52 GMT-04:00 2010
 */

package org.ihtsdo.project.panel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.model.Partition;
import org.ihtsdo.project.model.PartitionScheme;
import org.ihtsdo.project.panel.dnd.ListConceptDragSourceListener;
import org.ihtsdo.project.panel.dnd.ListDragGestureListenerWithImage;
import org.ihtsdo.project.panel.dnd.ObjectTransferHandler;
import org.ihtsdo.project.util.IconUtilities;

/**
 * @author Guillermo Reynoso
 */
public class RefsetPartitionerPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private PartitionScheme partitionScheme;
	private I_ConfigAceFrame config;
	private DefaultListModel list1Model;
	private boolean list1DisplayAll;
	ObjectTransferHandler ConceptDnDHandler ;

	public RefsetPartitionerPanel(PartitionScheme partitionScheme, I_ConfigAceFrame config) {
		initComponents();
		I_TermFactory termFactory = Terms.get();
		this.partitionScheme = partitionScheme;
		this.config = config;

		try {
			label11.setIcon(IconUtilities.helpIcon);
			label11.setText("");
			label8.setText(partitionScheme.getName());
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

			updateList1Content();

			list1DisplayAll = false;
			radioButton1.setSelected(false);
			radioButton2.setSelected(true);

			updatePanel7Content();
			ConceptDnDHandler=new ObjectTransferHandler(config,null);
			list1.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	        list1.setDropMode(DropMode.INSERT);
			list1.setDragEnabled(true);
			list1.setTransferHandler(ConceptDnDHandler);

	        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(list1, DnDConstants.ACTION_MOVE,
	            new ListDragGestureListenerWithImage(new ListConceptDragSourceListener(list1,null),list1,config));

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					e.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	void updatePanel7Content() {
		for (Component component : panel7.getComponents()) {
			panel7.remove(component);
		}
		TreeMap<String,Partition> sm=new TreeMap<String, Partition>();
		for (Partition partition : partitionScheme.getPartitions()) {
			if (partition!=null)
				sm.put(partition.getName(), partition);
		}
		for (String key:sm.keySet()){
			JPanel partitionPanel = new MiniPartitionDetailsPanel(sm.get(key), config);
			panel7.add(partitionPanel);
			
		}
		panel7.add(new JLabel(""));
//		for (Partition partition : partitionScheme.getPartitions()) {
//			JPanel partitionPanel = new MiniPartitionDetailsPanel(partition, config);
//			panel7.add(partitionPanel);
//		}
		panel7.revalidate();
		scrollPane2.revalidate();
		panel10.revalidate();
		panel7.setSize(panel7.getWidth() + 1, panel7.getHeight() + 1);
		panel7.setSize(panel7.getWidth() - 1, panel7.getHeight() - 1);
//		button1.setEnabled(true);
//		button3.setEnabled(true);
	}

	
	class listDataListener implements ListDataListener{

		@Override
		public void contentsChanged(ListDataEvent e) {
			label7.setText("(" + ((DefaultListModel)e.getSource()).size() + ")");
			
			
		}

		@Override
		public void intervalAdded(ListDataEvent e) {
			label7.setText("(" + ((DefaultListModel)e.getSource()).size() + ")");
			
		}

		@Override
		public void intervalRemoved(ListDataEvent e) {
			label7.setText("(" + ((DefaultListModel)e.getSource()).size() + ")");
			
		}
	}
	
	void updateList1Content() {
		list1Model = new DefaultListModel();

		list1Model.addListDataListener(new listDataListener() );
		I_TermFactory tf = Terms.get();
		boolean areThereRemainingMembers = false;
		if (list1DisplayAll) {
			try {
				List<I_GetConceptData> members = new ArrayList<I_GetConceptData>();

				for ( I_ExtendByRef extension : 
					tf.getRefsetExtensionMembers(partitionScheme.getSourceRefset(config).getConceptNid())) {
					I_ExtendByRefPart lastPart = TerminologyProjectDAO.getLastExtensionPart(extension);
					if (TerminologyProjectDAO.isActive(lastPart.getStatusNid())) {
						I_GetConceptData member = tf.getConcept(extension.getComponentNid());
						members.add(member);
					}
				}
				
				Collections.sort(members,
						new Comparator<I_GetConceptData>()
						{
							public int compare(I_GetConceptData f1, I_GetConceptData f2)
							{
								return f1.toString().compareTo(f2.toString());
							}
						});
				
				for (I_GetConceptData member : members) {
					list1Model.addElement(member);
				}
				
				areThereRemainingMembers = 
					(TerminologyProjectDAO.getMembersNotPartitioned(partitionScheme, config).size() 
						> 0);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this,
						e.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		} else {
			List<I_GetConceptData> notPartitionedMembers = 
				TerminologyProjectDAO.getMembersNotPartitioned(partitionScheme, config);
			areThereRemainingMembers = (notPartitionedMembers.size() > 0);
			Collections.sort(notPartitionedMembers,
					new Comparator<I_GetConceptData>()
					{
						public int compare(I_GetConceptData f1, I_GetConceptData f2)
						{
							return f1.toString().compareTo(f2.toString());
						}
					});
			for (I_GetConceptData notPartitionedMember : notPartitionedMembers) {
				list1Model.addElement(notPartitionedMember);
			}
		}
		list1.setModel(list1Model);
		list1.validate();
		label7.setText("("+ list1Model.getSize() + ")");
		button1.setEnabled(areThereRemainingMembers);
		button3.setEnabled(areThereRemainingMembers);
	}

	private void radioButton1ActionPerformed(ActionEvent e) {
		list1DisplayAll = true;
		updateList1Content();
	}

	private void radioButton2ActionPerformed(ActionEvent e) {
		list1DisplayAll = false;
		updateList1Content();
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
		}
	}

	private void panel7ComponentResized(ComponentEvent e) {
		panel7.validate();
	}

	private void scrollPane2ComponentResized(ComponentEvent e) {
		scrollPane2.validate();
	}

	private void button1ActionPerformed(ActionEvent e) {
		JPanel newPartitionPanel =new NewPartitionPanel(partitionScheme, this, config);
		panel7.add(newPartitionPanel);
		panel7.revalidate();
		button1.setEnabled(false);
		button3.setEnabled(false);
	}

	private void button3ActionPerformed(ActionEvent e) {
		JPanel partitionSpliterPanel =new PartitionSplitterPanel(partitionScheme, this, config);
		panel7.add(partitionSpliterPanel);
		panel7.revalidate();
		button1.setEnabled(false);
		button3.setEnabled(false);
	}

	private void label11MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("REFSET_PARTITIONER");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		panel2 = new JPanel();
		label1 = new JLabel();
		panel9 = new JPanel();
		label4 = new JLabel();
		label8 = new JLabel();
		panel3 = new JPanel();
		label2 = new JLabel();
		label9 = new JLabel();
		label6 = new JLabel();
		label10 = new JLabel();
		panel4 = new JPanel();
		panel5 = new JPanel();
		panel11 = new JPanel();
		label3 = new JLabel();
		label7 = new JLabel();
		scrollPane1 = new JScrollPane();
		list1 = new JList();
		panel6 = new JPanel();
		radioButton1 = new JRadioButton();
		radioButton2 = new JRadioButton();
		panel10 = new JPanel();
		label5 = new JLabel();
		scrollPane2 = new JScrollPane();
		panel7 = new JPanel();
		panel8 = new JPanel();
		button2 = new JButton();
		button1 = new JButton();
		button3 = new JButton();
		label11 = new JLabel();

		//======== this ========
		setBackground(new Color(238, 238, 238));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

			//======== panel2 ========
			{
				panel2.setBackground(new Color(238, 238, 238));
				panel2.setLayout(new GridBagLayout());
				((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0};
				((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
				((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

				//---- label1 ----
				label1.setText("Refset Partitioner");
				label1.setFont(new Font("Lucida Grande", Font.BOLD, 14));
				label1.setBackground(new Color(238, 238, 238));
				panel2.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			panel1.add(panel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//======== panel9 ========
			{
				panel9.setLayout(new GridBagLayout());
				((GridBagLayout)panel9.getLayout()).columnWidths = new int[] {0, 157, 0};
				((GridBagLayout)panel9.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel9.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
				((GridBagLayout)panel9.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

				//---- label4 ----
				label4.setText("Partition scheme:");
				panel9.add(label4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- label8 ----
				label8.setText("text");
				panel9.add(label8, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			panel1.add(panel9, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//======== panel3 ========
			{
				panel3.setLayout(new GridBagLayout());
				((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 156, 42, 87, 0};
				((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
				((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

				//---- label2 ----
				label2.setText("Refset name:");
				panel3.add(label2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- label9 ----
				label9.setText("text");
				panel3.add(label9, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- label6 ----
				label6.setText("Refset type:");
				panel3.add(label6, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- label10 ----
				label10.setText("text");
				panel3.add(label10, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			panel1.add(panel3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel4 ========
		{
			panel4.setLayout(new GridBagLayout());
			((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {100, 0, 0};
			((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
			((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

			//======== panel5 ========
			{
				panel5.setLayout(new GridBagLayout());
				((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 0};
				((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
				((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

				//======== panel11 ========
				{
					panel11.setLayout(new GridBagLayout());
					((GridBagLayout)panel11.getLayout()).columnWidths = new int[] {0, 0, 0};
					((GridBagLayout)panel11.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel11.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel11.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- label3 ----
					label3.setText("Members");
					panel11.add(label3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- label7 ----
					label7.setText("(-)");
					panel11.add(label7, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel5.add(panel11, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== scrollPane1 ========
				{
					scrollPane1.setViewportView(list1);
				}
				panel5.add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel6 ========
				{
					panel6.setLayout(new GridBagLayout());
					((GridBagLayout)panel6.getLayout()).columnWidths = new int[] {0, 0, 0};
					((GridBagLayout)panel6.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel6.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel6.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- radioButton1 ----
					radioButton1.setText("All refset members");
					radioButton1.setFont(new Font("Lucida Grande", Font.PLAIN, 8));
					radioButton1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							radioButton1ActionPerformed(e);
						}
					});
					panel6.add(radioButton1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- radioButton2 ----
					radioButton2.setText("Remaining members");
					radioButton2.setFont(new Font("Lucida Grande", Font.PLAIN, 8));
					radioButton2.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							radioButton2ActionPerformed(e);
						}
					});
					panel6.add(radioButton2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel5.add(panel6, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			panel4.add(panel5, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//======== panel10 ========
			{
				panel10.setLayout(new GridBagLayout());
				((GridBagLayout)panel10.getLayout()).columnWidths = new int[] {0, 0};
				((GridBagLayout)panel10.getLayout()).rowHeights = new int[] {0, 0, 0};
				((GridBagLayout)panel10.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
				((GridBagLayout)panel10.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

				//---- label5 ----
				label5.setText("Partitions");
				panel10.add(label5, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== scrollPane2 ========
				{
					scrollPane2.addComponentListener(new ComponentAdapter() {
						@Override
						public void componentResized(ComponentEvent e) {
							scrollPane2ComponentResized(e);
						}
					});

					//======== panel7 ========
					{
						panel7.addComponentListener(new ComponentAdapter() {
							@Override
							public void componentResized(ComponentEvent e) {
								panel7ComponentResized(e);
							}
						});
						panel7.setLayout(new BoxLayout(panel7, BoxLayout.Y_AXIS));
					}
					scrollPane2.setViewportView(panel7);
				}
				panel10.add(scrollPane2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			panel4.add(panel10, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel4, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel8 ========
		{
			panel8.setBackground(new Color(238, 238, 238));
			panel8.setLayout(new GridBagLayout());
			((GridBagLayout)panel8.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0};
			((GridBagLayout)panel8.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel8.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0, 0.0, 1.0E-4};
			((GridBagLayout)panel8.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- button2 ----
			button2.setText("Close");
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

			//---- button1 ----
			button1.setText("Add partition");
			button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed(e);
				}
			});
			panel8.add(button1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- button3 ----
			button3.setText("Split remaining members");
			button3.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			button3.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button3ActionPerformed(e);
				}
			});
			panel8.add(button3, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label11 ----
			label11.setText("text");
			label11.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					label11MouseClicked(e);
				}
			});
			panel8.add(label11, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel8, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(radioButton1);
		buttonGroup1.add(radioButton2);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel1;
	private JPanel panel2;
	private JLabel label1;
	private JPanel panel9;
	private JLabel label4;
	private JLabel label8;
	private JPanel panel3;
	private JLabel label2;
	private JLabel label9;
	private JLabel label6;
	private JLabel label10;
	private JPanel panel4;
	private JPanel panel5;
	private JPanel panel11;
	private JLabel label3;
	private JLabel label7;
	private JScrollPane scrollPane1;
	private JList list1;
	private JPanel panel6;
	private JRadioButton radioButton1;
	private JRadioButton radioButton2;
	private JPanel panel10;
	private JLabel label5;
	private JScrollPane scrollPane2;
	private JPanel panel7;
	private JPanel panel8;
	private JButton button2;
	private JButton button1;
	private JButton button3;
	private JLabel label11;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
