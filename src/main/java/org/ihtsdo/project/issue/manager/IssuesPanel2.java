/**
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

package org.ihtsdo.project.issue.manager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicBorders.SplitPaneBorder;

import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.ihtsdo.issue.Issue;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.implementation.I_IssueManager;

/**
 * The Class IssuesPanel.
 */
public class IssuesPanel2 extends JPanel {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The config. */
	private I_ConfigAceFrame config;
	
	/** The db config. */
	private I_ConfigAceDb dbConfig;
	
	/** The im. */
	private I_IssueManager im;
	
	/** The issue. */
	private Issue issue;
	
	/** The debug. */
	private boolean debug=true;

	private String txtCompId;

	private String txtCompName;

	private int projectId;

	private IssueRepository ir;

	private String siteUserName;

	private String siteUserPassword;
	
	/**
	 * Instantiates a new issues panel.
	 * 
	 * @throws Exception the exception
	 */
	public IssuesPanel2() {
		initComponents();
	}
	
	public String getIssueTitle(){
		return txtTitle.getText();
	}
	
	public String getIssueDescription(){
		return txtDesc.getText();
	}
	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		scrollPane1 = new JScrollPane();
		panel2 = new JPanel();
		label1 = new JLabel();
		label10 = new JLabel();
		label2 = new JLabel();
		txtTitle = new JTextField();
		label9 = new JLabel();
		scrollPane2 = new JScrollPane();
		txtDesc = new JTextArea();

		//======== scrollPane1 ========
		{

			//======== panel2 ========
			{
				panel2.setLayout(new BorderLayout());

				//======== this ========
				{
					this.setLayout(new GridBagLayout());
					((GridBagLayout)getLayout()).columnWidths = new int[] {26, 0, 405, 0, 0};
					((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 26, 0};
					((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};
					((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

					//---- label1 ----
					label1.setText("New Issue");
					label1.setFont(label1.getFont().deriveFont(label1.getFont().getStyle() | Font.BOLD, label1.getFont().getSize() + 2f));
					this.add(label1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					this.add(label10, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label2 ----
					label2.setText("Title");
					this.add(label2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					this.add(txtTitle, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label9 ----
					label9.setText("Description");
					this.add(label9, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//======== scrollPane2 ========
					{
						scrollPane2.setViewportView(txtDesc);
					}
					this.add(scrollPane2, new GridBagConstraints(2, 2, 1, 2, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
				}
				panel2.add(this, BorderLayout.CENTER);
			}
			scrollPane1.setViewportView(panel2);
		}
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JScrollPane scrollPane1;
	private JPanel panel2;
	private JLabel label1;
	private JLabel label10;
	private JLabel label2;
	private JTextField txtTitle;
	private JLabel label9;
	private JScrollPane scrollPane2;
	private JTextArea txtDesc;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
