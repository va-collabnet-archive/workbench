/*
 * Created by JFormDesigner on Thu Aug 26 17:04:29 GMT-03:00 2010
 */

package org.ihtsdo.project.panel.details;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.ihtsdo.issue.IssueRepoRegistration;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkListMember;

/**
 * @author Guillermo Reynoso
 */
public class WorklistMemberHistoryFrame extends JFrame {
	private org.ihtsdo.project.panel.details.WorklistMemberLogPanel wmlpanel;
	public WorklistMemberHistoryFrame(String title) {
		initComponents();
		this.setTitle(title);
		this.setSize(new Dimension(900,600));
		wmlpanel = new WorklistMemberLogPanel();
		this.add(wmlpanel, BorderLayout.CENTER);
		this.validate();
		
	}
	
	public void refreshPanel(WorkListMember member,TranslationProject translationProject, IssueRepository repo, IssueRepoRegistration regis){

		wmlpanel.showMemberChanges(member,translationProject,repo, regis) ;
	}

	private void okButtonActionPerformed() {
	
	}
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		dialogPane = new JPanel();
		contentPanel = new JPanel();

		//======== this ========
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new BorderLayout());
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(null);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel dialogPane;
	private JPanel contentPanel;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
