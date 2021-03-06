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

package org.ihtsdo.qa.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

/**
 * The Class QAManager.
 *
 * @author Guillermo Reynoso
 */
public class QAManager extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The Constant QA_MANAGER. */
	public static final String QA_MANAGER = "QA Manager";

	/**
	 * Instantiates a new qA manager.
	 */
	public QAManager() {
		initComponents();
		try {
			button1.setVisible(false); //hide close button
			panel3.add(new RulesDeploymentPkgBrowserPanel(Terms.get().getActiveAceFrameConfig()), BorderLayout.CENTER);
			//panel4.add(new RulesContextMatrixPanel(Terms.get().getActiveAceFrameConfig()), BorderLayout.CENTER);
			panel5.add(new RulesContextEditorPanel(Terms.get().getActiveAceFrameConfig()), BorderLayout.CENTER);
			panel6.add(new TestListPanel(Terms.get().getActiveAceFrameConfig()), BorderLayout.CENTER);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Button1 action performed.
	 */
	private void button1ActionPerformed() {
//		Close not possible without dependency from wb-gui
//		
//		AceFrameConfig config;
//		try {
//			config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();
//			AceFrame ace=config.getAceFrame();
//			JTabbedPane tp=ace.getCdePanel().getConceptTabs();
//			if (tp!=null){
//				int tabCount=tp.getTabCount();
//				for (int i=0;i<tabCount;i++){
//					if (tp.getTitleAt(i).equals(QA_MANAGER)){
//						tp.remove(i);
//						tp.repaint();
//						tp.revalidate();
//					}
//
//				}
//			}
//		} catch (TerminologyException e) {
//			AceLog.getAppLog().alertAndLogException(e);
//		} catch (IOException e) {
//			AceLog.getAppLog().alertAndLogException(e);
//		}
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		tabbedPane1 = new JTabbedPane();
		panel3 = new JPanel();
		panel5 = new JPanel();
		panel6 = new JPanel();
		panel2 = new JPanel();
		button1 = new JButton();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0E-4};

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

			//======== tabbedPane1 ========
			{

				//======== panel3 ========
				{
					panel3.setLayout(new BorderLayout());
				}
				tabbedPane1.addTab("Rules Deployment Packages", panel3);


				//======== panel5 ========
				{
					panel5.setLayout(new BorderLayout());
				}
				tabbedPane1.addTab("Rule-Context editor", panel5);


				//======== panel6 ========
				{
					panel6.setLayout(new BorderLayout());
				}
				tabbedPane1.addTab("Test List", panel6);

			}
			panel1.add(tabbedPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- button1 ----
			button1.setText("Close");
			button1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed();
				}
			});
			panel2.add(button1, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	/** The panel1. */
	private JPanel panel1;
	
	/** The tabbed pane1. */
	private JTabbedPane tabbedPane1;
	
	/** The panel3. */
	private JPanel panel3;
	
	/** The panel5. */
	private JPanel panel5;
	
	/** The panel6. */
	private JPanel panel6;
	
	/** The panel2. */
	private JPanel panel2;
	
	/** The button1. */
	private JButton button1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
