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

package org.ihtsdo.project.view.details;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.ihtsdo.issue.IssueRepoRegistration;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkListMember;

/**
 * The Class WorklistMemberHistoryFrame.
 *
 * @author Guillermo Reynoso
 */
public class WorklistMemberHistoryFrame extends JFrame {
	
	/** The wmlpanel. */
	private WorklistMemberLogPanel wmlpanel;
	
	/**
	 * Instantiates a new worklist member history frame.
	 *
	 * @param title the title
	 */
	public WorklistMemberHistoryFrame(String title) {
		initComponents();
		this.setTitle(title);
		this.setSize(new Dimension(900,600));
		wmlpanel = new WorklistMemberLogPanel();
		this.add(wmlpanel, BorderLayout.CENTER);
		this.validate();
		
	}
	
	/**
	 * Refresh panel.
	 *
	 * @param member the member
	 * @param translationProject the translation project
	 * @param repo the repo
	 * @param regis the regis
	 */
	public void refreshPanel(WorkListMember member,I_TerminologyProject translationProject, IssueRepository repo, IssueRepoRegistration regis){

		wmlpanel.showMemberChanges(member,translationProject,repo, regis) ;
	}

	/**
	 * Ok button action performed.
	 */
	private void okButtonActionPerformed() {
	
	}
	
	/**
	 * Inits the components.
	 */
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
	/** The dialog pane. */
	private JPanel dialogPane;
	
	/** The content panel. */
	private JPanel contentPanel;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
