/*
 * Created by JFormDesigner on Mon Oct 25 14:43:45 GMT-03:00 2010
 */

package org.ihtsdo.translation.ui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.*;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.panel.details.*;

/**
 * @author Guillermo Reynoso
 */
public class WorklistMemberStatusFrame extends JFrame {
	public WorklistMemberStatusFrame() {
		initComponents();
	}
	public WorklistMemberStatusFrame(WorkList workList, I_ConfigAceFrame config) {
		initComponents();

		WorklistMemberStatusPanel worklistMemberStatusPanel1=new WorklistMemberStatusPanel(workList, config);

		Container contentPane = getContentPane();
		contentPane.add(worklistMemberStatusPanel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		
		WindowListener wndCloser = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				setVisible(false);
				dispose();

			}
		};
		addWindowListener(wndCloser);
		setSize(800, 500);

		this.validate();
		this.repaint();
		SwingUtilities.updateComponentTreeUI(worklistMemberStatusPanel1);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new GridBagLayout());
		((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0};
		((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
