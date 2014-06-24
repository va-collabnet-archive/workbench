/*
 * Created by JFormDesigner on Tue Sep 21 16:47:47 GMT-03:00 2010
 */

package org.dwfa.ace.modeler.tool;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.KeyEvent;

import javax.swing.*;

import org.dwfa.ace.api.I_ConfigAceFrame;

/**
 * @author Guillermo Reynoso
 */
public class AskForConceptParent extends JPanel {
	
	private DefaultListModel listModel;
	public AskForConceptParent(I_ConfigAceFrame config) {
		initComponents();
		list1.setTransferHandler(new ObjectTransferHandler(config, null));
		listModel=new DefaultListModel();
		list1.setModel(listModel);
		list1.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	private void list1KeyTyped(KeyEvent e) {
		//		System.out.println(e.getKeyCode());
		//		System.out.println(e.getKeyChar());
		//		System.out.println();
		//		System.out.println(e.getKeyText(e.getKeyCode()));
//		String keyChar = String.valueOf(e.getKeyChar());
		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
//		if ("d".equals(keyChar)) {
			removeSelectedList1Items();
		}
	}

	private void removeSelectedList1Items() {
		if(list1.getSelectedIndices().length > 0) {
			int[] tmp = list1.getSelectedIndices();
			int[] selectedIndices = list1.getSelectedIndices();

			for (int i = tmp.length-1; i >=0; i--) {
				selectedIndices = list1.getSelectedIndices();
				listModel.removeElementAt(selectedIndices[i]);
			} // end-for
		} //
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label1 = new JLabel();
		label2 = new JLabel();
		scrollPane1 = new JScrollPane();
		list1 = new JList();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};

		//---- label1 ----
		label1.setText("Parent selection");
		add(label1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- label2 ----
		label2.setText("Drop concept parents on the list");
		add(label2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//======== scrollPane1 ========
		{

			//---- list1 ----
			list1.addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) {
					list1KeyTyped(e);
				}
			});
			scrollPane1.setViewportView(list1);
		}
		add(scrollPane1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JLabel label1;
	private JLabel label2;
	private JScrollPane scrollPane1;
	private JList list1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	public DefaultListModel getListModel() {
		return listModel;
	}

	public void setListModel(DefaultListModel listModel) {
		this.listModel = listModel;
	}
}
