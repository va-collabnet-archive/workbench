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

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.ihtsdo.issue.Issue;
import org.ihtsdo.issue.IssueComment;
import org.ihtsdo.issue.IssueRepoRegistration;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.implementation.CollabnetIssueManager;

/**
 * The Class IssueCommentsPanel.
 */
public class IssueCommentsPanel extends JPanel {
	
	/** The issue. */
	private Issue issue;
	
	/** The issue repo. */
	private IssueRepository issueRepo;
	
	/** The user name. */
	private String userName;

	private IssueRepoRegistration regis;
	
	/**
	 * Instantiates a new issue comments panel.
	 */
	public IssueCommentsPanel() {
		initComponents();
		pBar.setVisible(false);
	}
	
	/**
	 * Sets the init.
	 * 
	 * @param issue the issue
	 * @param issueRepo the issue repo
	 * @param regis 
	 * @param userName the user name
	 */
	public void setInit(Issue issue,IssueRepository issueRepo,IssueRepoRegistration regis, String userName){
		this.issue=issue;
		this.issueRepo=issueRepo;
		this.regis=regis;
		this.userName=userName;
		
		loadComments();
	}
	public void clear(){
		txtComm.setText("");
		loadCommentTable(new ArrayList<IssueComment>());
	}
	/**
	 * Load comments.
	 */
	private void loadComments() {
		if (issueRepo.getType()==IssueRepository.REPOSITORY_TYPE.WEB_SITE.ordinal()){
			CollabnetIssueManager cIM=new CollabnetIssueManager();
			List<IssueComment> issueCommList=new ArrayList<IssueComment>();
			
			try{
				cIM.openRepository(issueRepo, getSiteUserName(), getSiteUserPassword());
			}catch(Exception e){
				e.printStackTrace();
				message("Sorry, cannot connect to repository.\n" + e.getMessage());
				return;
			}
			try {
				issueCommList=cIM.getCommentsList(issue);
			} catch (Exception e) {
				e.printStackTrace();
				message("Sorry, cannot retrieve comments for this issue.\n" + e.getMessage());
				return;
			}
			loadCommentTable(issueCommList);
		}
		
	}
	private String getSiteUserPassword() {
		return this.regis.getPassword();
	}

	private String getSiteUserName() {
		return this.regis.getUserId();
	}

	/**
	 * Load comment table.
	 * 
	 * @param issueCommList the issue comm list
	 */
	private void loadCommentTable(List<IssueComment> issueCommList) {
		String[] columnNames = {"Date",
        "User","Comment"};
		String[][] data = null;
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};
		for (int i=0;i<issueCommList.size();i++) {
			if (!issueCommList.get(i).getUser().equals(getSiteUserName()))
				tableModel.addRow(new String[] {issueCommList.get(i).getCommentDate() ,issueCommList.get(i).getUser() ,
					issueCommList.get(i).getComment()});
		}

		table1.setModel(tableModel);
		TableColumnModel cmodel = table1.getColumnModel(); 
		TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
		cmodel.getColumn(0).setCellRenderer(textAreaRenderer); 
		cmodel.getColumn(1).setCellRenderer(textAreaRenderer); 
		cmodel.getColumn(2).setCellRenderer(textAreaRenderer); 
		table1.revalidate();
		
	}

	/**
	 * Message.
	 * 
	 * @param string the string
	 */
	private void message(String string) {

         JOptionPane.showOptionDialog(   
        		this,   
                string,   
                "Information", JOptionPane.DEFAULT_OPTION,   
                JOptionPane.INFORMATION_MESSAGE, null, null,   
                null );   
	}
	
	/**
	 * B send action performed.
	 */
	private void bSendActionPerformed() {
		if (txtComm.getText().trim().equals("")){
			message("Sorry, cannot send an empty comment.");
			return;
		}
		if (issue==null){
			message("Sorry, Could you select an issue on the list and press \"Send\" again?\nThank you.");
			return;
		}
		if (issueRepo.getType()==IssueRepository.REPOSITORY_TYPE.WEB_SITE.ordinal()){
			pBar.setIndeterminate(true);
			pBar.setVisible(true);
			this.revalidate();
			this.repaint();
			CollabnetIssueManager cIM=new CollabnetIssueManager();
			try {
				cIM.openRepository(issueRepo, getSiteUserName(), getSiteUserPassword());
			} catch (Exception e1) {

				pBar.setVisible(false);
				e1.printStackTrace();
				message("Sorry, cannot connect to repository.\n" + e1.getMessage());
				return;
			}
			issue.setCommentsForUpdate(txtComm.getText().trim());

			SimpleDateFormat sdf=new SimpleDateFormat("MM/dd/yyyy HH:mm");
			try {
				cIM.setIssueData(issue);
				pBar.setVisible(false);
				message("Comment sent.");
				DefaultTableModel tableModel = (DefaultTableModel)table1.getModel();
				tableModel.insertRow(0,new String[] {sdf.format(new Date()),userName,
						txtComm.getText().trim()});
				table1.revalidate();
			} catch (Exception e) {
				pBar.setVisible(false);
				e.printStackTrace();
				message("Sorry, cannot send the comment.\n" + e.getMessage() );
			}
		}
		
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label1 = new JLabel();
		pBar = new JProgressBar();
		scrollPane1 = new JScrollPane();
		txtComm = new JTextArea();
		bSend = new JButton();
		scrollPane2 = new JScrollPane();
		table1 = new JTable();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 35, 35, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0, 1.0E-4};

		//---- label1 ----
		label1.setText("Issue Comments");
		label1.setFont(new Font("Tahoma", Font.BOLD, 14));
		add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- pBar ----
		pBar.setIndeterminate(true);
		add(pBar, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== scrollPane1 ========
		{
			scrollPane1.setViewportView(txtComm);
		}
		add(scrollPane1, new GridBagConstraints(0, 1, 2, 2, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- bSend ----
		bSend.setText("Send");
		bSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				bSendActionPerformed();
			}
		});
		add(bSend, new GridBagConstraints(2, 1, 1, 2, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== scrollPane2 ========
		{

			//---- table1 ----
			table1.setModel(new DefaultTableModel(
				new Object[][] {
				},
				new String[] {
					null, null, null
				}
			));
			scrollPane2.setViewportView(table1);
		}
		add(scrollPane2, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JLabel label1;
	private JProgressBar pBar;
	private JScrollPane scrollPane1;
	private JTextArea txtComm;
	private JButton bSend;
	private JScrollPane scrollPane2;
	private JTable table1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
