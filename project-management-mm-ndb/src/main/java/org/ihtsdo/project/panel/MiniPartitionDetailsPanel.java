/*
 * Created by JFormDesigner on Wed Mar 24 12:49:39 GMT-04:00 2010
 */

package org.ihtsdo.project.panel;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.ihtsdo.project.model.Partition;
import org.ihtsdo.project.model.PartitionMember;
import org.ihtsdo.project.panel.dnd.GetPartitionMember;
import org.ihtsdo.project.panel.dnd.ListConceptDragSourceListener;
import org.ihtsdo.project.panel.dnd.ListDragGestureListenerWithImage;
import org.ihtsdo.project.panel.dnd.ObjectTransferHandler;
import org.ihtsdo.project.panel.dnd.RemovePartitionMember;

/**
 * @author Guillermo Reynoso
 */
public class MiniPartitionDetailsPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Partition partition;
	private I_ConfigAceFrame config;
	private DefaultListModel list1Model;
	private ObjectTransferHandler ConceptDnDHandler;
	
	public MiniPartitionDetailsPanel(Partition partition, I_ConfigAceFrame config) {
		initComponents();
		this.partition = partition;
		this.config = config;
		
		label2.setText(partition.getName());
		label3.setText("(" + partition.getPartitionMembers().size() + ")");
		panel3.setVisible(false);
		updateList1Content();
		checkBox1.setSelected(false);

		ConceptDnDHandler=new ObjectTransferHandler(config,new GetPartitionMember(this.partition,config));
		list1.setTransferHandler(ConceptDnDHandler);
		
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(list1, DnDConstants.ACTION_MOVE,
            new ListDragGestureListenerWithImage(new ListConceptDragSourceListener(list1,new RemovePartitionMember(config)),list1,config));
	
	}
	
	class listDataListener implements ListDataListener{

		@Override
		public void contentsChanged(ListDataEvent e) {
			label3.setText("(" + ((DefaultListModel)e.getSource()).size() + ")");
			
			
		}

		@Override
		public void intervalAdded(ListDataEvent e) {
			label3.setText("(" + ((DefaultListModel)e.getSource()).size() + ")");
			
			
		}

		@Override
		public void intervalRemoved(ListDataEvent e) {
			label3.setText("(" + ((DefaultListModel)e.getSource()).size() + ")");
			
			
		}}
	
	private void updateList1Content() {
		list1Model = new DefaultListModel();
		List<PartitionMember> members = partition.getPartitionMembers();
		Collections.sort(members,
				new Comparator<PartitionMember>()
				{
					public int compare(PartitionMember f1, PartitionMember f2)
					{
						return f1.toString().compareTo(f2.toString());
					}
				});
		list1Model.addListDataListener(new listDataListener() );
		for (PartitionMember member : members) {
			list1Model.addElement(member);
		}
		list1.setModel(list1Model);
		list1.validate();
	}

	private void checkBox1ActionPerformed(ActionEvent e) {
		panel3.setVisible(!panel3.isVisible());
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		label1 = new JLabel();
		label2 = new JLabel();
		panel2 = new JPanel();
		checkBox1 = new JCheckBox();
		label3 = new JLabel();
		panel3 = new JPanel();
		scrollPane1 = new JScrollPane();
		list1 = new JList();

		//======== this ========
		setBorder(new LineBorder(Color.lightGray));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0, 1.0E-4};

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- label1 ----
			label1.setText("Partition name:");
			label1.setFont(new Font("Lucida Grande", Font.BOLD, 13));
			panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label2 ----
			label2.setText("text");
			panel1.add(label2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 36, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- checkBox1 ----
			checkBox1.setText("Show members");
			checkBox1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					checkBox1ActionPerformed(e);
				}
			});
			panel2.add(checkBox1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label3 ----
			label3.setText("(-)");
			panel2.add(label3, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel3 ========
		{
			panel3.setLayout(new GridBagLayout());
			((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
			((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

			//======== scrollPane1 ========
			{

				//---- list1 ----
				list1.setVisibleRowCount(5);
				scrollPane1.setViewportView(list1);
			}
			panel3.add(scrollPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel1;
	private JLabel label1;
	private JLabel label2;
	private JPanel panel2;
	private JCheckBox checkBox1;
	private JLabel label3;
	private JPanel panel3;
	private JScrollPane scrollPane1;
	private JList list1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
