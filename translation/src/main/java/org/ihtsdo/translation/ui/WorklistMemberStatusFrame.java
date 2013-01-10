/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ihtsdo.translation.ui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.view.details.WorklistMemberStatusPanel;

/**
 * The Class WorklistMemberStatusFrame.
 *
 * @author Guillermo Reynoso
 */
public class WorklistMemberStatusFrame extends JFrame {
	
	/**
	 * Instantiates a new worklist member status frame.
	 */
	public WorklistMemberStatusFrame() {
		initComponents();
	}
	
	/**
	 * Instantiates a new worklist member status frame.
	 *
	 * @param workList the work list
	 * @param config the config
	 */
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

	/**
	 * Inits the components.
	 */
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
