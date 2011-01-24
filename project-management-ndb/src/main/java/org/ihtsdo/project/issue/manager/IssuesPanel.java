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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.UUID;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.MutableComboBoxModel;

import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.ihtsdo.issue.Issue;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;
import org.ihtsdo.issue.manager.implementation.CollabnetIssueManager;
import org.ihtsdo.issue.manager.implementation.I_IssueManager;
import org.ihtsdo.project.issuerepository.manager.ListObj;
import org.ihtsdo.project.panel.TranslationHelperPanel;

/**
 * The Class IssuesPanel.
 */
public class IssuesPanel extends JPanel {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The term factory. */
	private I_TermFactory termFactory;
	
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
	
	/**
	 * Instantiates a new issues panel.
	 * 
	 * @throws Exception the exception
	 */
	public IssuesPanel() throws Exception {
		initComponents();
	}
	
	/**
	 * Sets the init new.
	 * 
	 * @throws Exception the exception
	 */
	public void setInitNew() throws Exception{
		termFactory=LocalVersionedTerminology.get();
		config=termFactory.getActiveAceFrameConfig();
		dbConfig= config.getDbConfig();
		this.issue=null;
		loadValues(null,null);
	}
	
	/**
	 * Sets the init edition.
	 * 
	 * @param issue the issue
	 * @param issueRepository the issue repository
	 * 
	 * @throws Exception the exception
	 */
	public void setInitEdition(Issue issue,IssueRepository issueRepository) throws Exception{
		termFactory=LocalVersionedTerminology.get();
		config=termFactory.getActiveAceFrameConfig();
		dbConfig= config.getDbConfig();
		this.issue=issue;
		loadValues(issue,issueRepository);
		bRefresh.setEnabled(false);
	}
	
	/**
	 * Instantiates a new issues panel.
	 * 
	 * @param termFactory the term factory
	 * 
	 * @throws Exception the exception
	 */
	public IssuesPanel(I_TermFactory termFactory) throws Exception {
		this.termFactory=termFactory;
		initComponents();
		config=termFactory.getActiveAceFrameConfig();
		dbConfig= config.getDbConfig();
		this.issue=null;
		loadValues(null,null);
	}
	
	/**
	 * Load values.
	 * 
	 * @param issue the issue
	 * @param issueRepository the issue repository
	 * 
	 * @throws Exception the exception
	 */
	private void loadValues(Issue issue,IssueRepository issueRepository) throws Exception {
		// TODO Auto-generated method stub
		MutableComboBoxModel mutComboModel=(MutableComboBoxModel)new DefaultComboBoxModel();
		cRepo.setModel(mutComboModel);
		loadRepos(issueRepository);
		if (cRepo.getSelectedItem()==null){
			enableCtrl(false);
			return;
		}
		else
		{
			if (issue!=null){
				if (!issue.getExternalUser().equalsIgnoreCase(config.getUsername())){
					enableCtrl(false);
				}else{
					enableCtrl(true);
				}
			}
			else{
				enableCtrl(true);
			}
		}
		IssueRepository ir=(IssueRepository)((ListObj)cRepo.getSelectedItem()).getAtrValue();
		loadIRValues(ir);
		loadPriority(issue);
		loadStatus(issue);
		loadCategory(issue);	
		if (issue==null){	
			txtUser.setText(config.getUsername());
		}
		else{
			txtUser.setText(issue.getExternalUser());
			txtCompId.setText(issue.getComponentId());
			txtCompName.setText(issue.getComponent());
			txtTitle.setText(issue.getTitle());
			txtDesc.setText(issue.getDescription());
			
		}
	}
	
	/**
	 * Enable ctrl.
	 * 
	 * @param enable the enable
	 */
	private void enableCtrl(boolean enable){
		bSave.setEnabled(enable);
		bCancel.setEnabled(enable);
		
	}
	
	/**
	 * Load category.
	 * 
	 * @param issue the issue
	 */
	private void loadCategory(Issue issue) {
		// TODO Auto-generated method stub
		cCate.setModel(new DefaultComboBoxModel(Issue.CATEGORY.values()));
		if (issue!=null)
			cCate.setSelectedItem(Issue.CATEGORY.valueOf(issue.getCategory()));
		
	}
	
	/**
	 * Load status.
	 * 
	 * @param issue the issue
	 */
	private void loadStatus(Issue issue) {
		// TODO Auto-generated method stub
		cStat.setModel(new DefaultComboBoxModel(Issue.STATUS.values()));
		//TODO:fix
//		if (issue!=null)
//			cStat.setSelectedItem(Issue.STATUS.valueOf(issue.getStatus()));
		
	}
	
	/**
	 * Load priority.
	 * 
	 * @param issue the issue
	 */
	private void loadPriority(Issue issue) {
		// TODO Auto-generated method stub
		cPrio.setModel(new DefaultComboBoxModel(Issue.PRIORITY.values()));
		if (issue!=null)
			cPrio.setSelectedItem(Issue.PRIORITY.values()[Integer.parseInt(issue.getPriority())]);
	}
	
	/**
	 * Sets the component id.
	 * 
	 * @param componentId the component id
	 * @param componentName the component name
	 */
	public void setComponentId(String componentId,String componentName){
		txtCompId.setText(componentId);
		txtCompName.setText(componentName);
		this.repaint();
		this.validate();
	}
	
	/**
	 * Load ir values.
	 * 
	 * @param ir the ir
	 */
	private void loadIRValues(IssueRepository ir) {
		// TODO Auto-generated method stub
		if (ir.getType()==IssueRepository.REPOSITORY_TYPE.WEB_SITE.ordinal()){
			lblExtId.setText(ir.getRepositoryId());
			lblExtProjId.setText(ir.getExternalProjectId());
		}
		else{
			lblExtId.setText("");
			lblExtProjId.setText("");
		}	
	}
	
	/**
	 * Load repos.
	 * 
	 * @param issueRepository the issue repository
	 */
	@SuppressWarnings("unchecked")
	private void loadRepos(IssueRepository issueRepository) {
		// TODO Auto-generated method stub
		try{
			boolean selected;
			if (debug){
				List<IssueRepository> irs=IssueRepositoryDAO.getAllIssueRepository(config);
				
				for(IssueRepository ir:irs){
					selected=false;
					if (issueRepository!=null)
						if(issueRepository.getId()==ir.getId())	selected=true;
						
					addObjectToCombo(cRepo, ir, ir.getName(),selected);
					
				}
			}
			else{
				List<UUID>repoUUIDs=(List<UUID>) dbConfig.getProperty(TranslationHelperPanel.ISSUE_REPO_PROPERTY_NAME);
				if (repoUUIDs!=null){
					for (UUID uid:repoUUIDs){
						I_GetConceptData irepoConcept=termFactory.getConcept(new UUID[]{uid});
						IssueRepository ir=IssueRepositoryDAO.getIssueRepository(irepoConcept);

						selected=false;
						if (issueRepository!=null)
							if(issueRepository.getId()==ir.getId())	selected=true;
							
						addObjectToCombo(cRepo, ir, ir.getName(),selected);
					}
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
			System.out.println( dbConfig ==null);
			
		}
		
	} 

	/**
	 * Adds the object to combo.
	 * 
	 * @param cmb the cmb
	 * @param object the object
	 * @param name the name
	 * @param selected the selected
	 */
	private void addObjectToCombo(JComboBox cmb,
			Object object,String name,boolean selected) {
		// TODO Auto-generated method stub
		ListObj lo=new ListObj("P",name,object);
		cmb.addItem(lo);
		if (selected)
			cmb.setSelectedItem(lo);
	}

	/**
	 * C repo item state changed.
	 */
	private void cRepoItemStateChanged() {
		// TODO add your code here
		IssueRepository ir=(IssueRepository)((ListObj)cRepo.getSelectedItem()).getAtrValue();
		if (ir.getType()==IssueRepository.REPOSITORY_TYPE.WEB_SITE.ordinal()){
			loadCNIssueManager();
		}else{
			loadAceIssueManager();
		}
		loadIRValues(ir);
	}


	/**
	 * Load ace issue manager.
	 */
	private void loadAceIssueManager() {
		// TODO:fix
		//im=new AceIssueManager();
	}
	
	/**
	 * Load cn issue manager.
	 */
	private void loadCNIssueManager() {
		// TODO Auto-generated method stub
		im=new CollabnetIssueManager();
	}

	/**
	 * B save action performed.
	 */
	private void bSaveActionPerformed() {
		// TODO add your code here
		IssueRepository ir=(IssueRepository)((ListObj)cRepo.getSelectedItem()).getAtrValue();
		Issue is;
		if (issue==null)
			issue =new Issue();
	
		issue.setCategory(((Issue.CATEGORY)cCate.getSelectedItem()).toString());
		issue.setComponent(txtCompName.getText());
		issue.setComponentId(txtCompId.getText());
		issue.setDescription(txtDesc.getText());
		//issue.setExternalId(lblExtId.getText());
		issue.setPriority(String.valueOf(((Issue.PRIORITY)cPrio.getSelectedItem()).ordinal()));
		issue.setProjectId(lblExtProjId.getText());
		//TODO:fix
		//issue.setStatus(((Issue.STATUS)cStat.getSelectedItem()).toString());
		issue.setTitle(txtTitle.getText());
		issue.setExternalUser(txtUser.getText());
		//TODO:fix
		//issue.setUser(ir.getConnUser());
		try {
			
			//TODO:fix
			im.openRepository(ir,"","");
			if (issue.getExternalId()!=null && !issue.getExternalId().equals("")){
				im.setIssueData(issue);

				message("Issue upadated");
			}
			else{
				im.postNewIssue(issue);
				message("Issue posted");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			message("Error:" + e.getMessage());
			e.printStackTrace();
		}
		
	}

	/**
	 * Message.
	 * 
	 * @param string the string
	 */
	private void message(String string) {
		// TODO Auto-generated method stub

         JOptionPane.showOptionDialog(   
        		this,   
                string,   
                "Information", JOptionPane.DEFAULT_OPTION,   
                JOptionPane.INFORMATION_MESSAGE, null, null,   
                null );   
	}
	
	/**
	 * B cancel action performed.
	 */
	private void bCancelActionPerformed() {
		// TODO add your code here
	}

	/**
	 * B refresh action performed.
	 */
	private void bRefreshActionPerformed() {
		// TODO add your code here
		try {
			loadValues(null,null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		label13 = new JLabel();
		cRepo = new JComboBox();
		bRefresh = new JButton();
		label12 = new JLabel();
		lblExtId = new JLabel();
		label4 = new JLabel();
		lblExtProjId = new JLabel();
		label2 = new JLabel();
		txtTitle = new JTextField();
		label9 = new JLabel();
		scrollPane2 = new JScrollPane();
		txtDesc = new JTextArea();
		label3 = new JLabel();
		txtCompId = new JTextField();
		label7 = new JLabel();
		txtCompName = new JTextField();
		label5 = new JLabel();
		cPrio = new JComboBox();
		label6 = new JLabel();
		cStat = new JComboBox();
		label8 = new JLabel();
		cCate = new JComboBox();
		label11 = new JLabel();
		txtUser = new JTextField();
		panel1 = new JPanel();
		bSave = new JButton();
		bCancel = new JButton();

		//======== scrollPane1 ========
		{

			//======== panel2 ========
			{
				panel2.setLayout(new BorderLayout());

				//======== this ========
				{
					this.setLayout(new GridBagLayout());
					((GridBagLayout)getLayout()).columnWidths = new int[] {26, 0, 0, 0, 0};
					((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 31, 0, 0, 0, 0, 0, 0, 0, 0};
					((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};
					((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

					//---- label1 ----
					label1.setText("Issue");
					label1.setFont(label1.getFont().deriveFont(label1.getFont().getStyle() | Font.BOLD, label1.getFont().getSize() + 2f));
					this.add(label1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					this.add(label10, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label13 ----
					label13.setText("Repository");
					this.add(label13, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- cRepo ----
					cRepo.addItemListener(new ItemListener() {
						public void itemStateChanged(ItemEvent e) {
							cRepoItemStateChanged();
						}
					});
					this.add(cRepo, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- bRefresh ----
					bRefresh.setText("Refresh");
					bRefresh.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							bRefreshActionPerformed();
						}
					});
					this.add(bRefresh, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- label12 ----
					label12.setText("External Id");
					this.add(label12, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					this.add(lblExtId, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label4 ----
					label4.setText("External Project Id");
					this.add(label4, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					this.add(lblExtProjId, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label2 ----
					label2.setText("Title");
					this.add(label2, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					this.add(txtTitle, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label9 ----
					label9.setText("Description");
					this.add(label9, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//======== scrollPane2 ========
					{
						scrollPane2.setViewportView(txtDesc);
					}
					this.add(scrollPane2, new GridBagConstraints(2, 5, 1, 2, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label3 ----
					label3.setText("Component Id");
					this.add(label3, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					this.add(txtCompId, new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label7 ----
					label7.setText("Component name");
					this.add(label7, new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					this.add(txtCompName, new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label5 ----
					label5.setText("Priority");
					this.add(label5, new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					this.add(cPrio, new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label6 ----
					label6.setText("Status");
					this.add(label6, new GridBagConstraints(1, 10, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					this.add(cStat, new GridBagConstraints(2, 10, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label8 ----
					label8.setText("Category");
					this.add(label8, new GridBagConstraints(1, 11, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					this.add(cCate, new GridBagConstraints(2, 11, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label11 ----
					label11.setText("User");
					this.add(label11, new GridBagConstraints(1, 12, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
					this.add(txtUser, new GridBagConstraints(2, 12, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//======== panel1 ========
					{
						panel1.setLayout(new GridBagLayout());
						((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0};
						((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
						((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
						((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

						//---- bSave ----
						bSave.setText("Save");
						bSave.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								bSaveActionPerformed();
							}
						});
						panel1.add(bSave, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- bCancel ----
						bCancel.setText("Cancel");
						bCancel.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								bCancelActionPerformed();
							}
						});
						panel1.add(bCancel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					this.add(panel1, new GridBagConstraints(2, 13, 1, 1, 0.0, 0.0,
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
	/** The scroll pane1. */
	private JScrollPane scrollPane1;
	
	/** The panel2. */
	private JPanel panel2;
	
	/** The label1. */
	private JLabel label1;
	
	/** The label10. */
	private JLabel label10;
	
	/** The label13. */
	private JLabel label13;
	
	/** The c repo. */
	private JComboBox cRepo;
	
	/** The b refresh. */
	private JButton bRefresh;
	
	/** The label12. */
	private JLabel label12;
	
	/** The lbl ext id. */
	private JLabel lblExtId;
	
	/** The label4. */
	private JLabel label4;
	
	/** The lbl ext proj id. */
	private JLabel lblExtProjId;
	
	/** The label2. */
	private JLabel label2;
	
	/** The txt title. */
	private JTextField txtTitle;
	
	/** The label9. */
	private JLabel label9;
	
	/** The scroll pane2. */
	private JScrollPane scrollPane2;
	
	/** The txt desc. */
	private JTextArea txtDesc;
	
	/** The label3. */
	private JLabel label3;
	
	/** The txt comp id. */
	private JTextField txtCompId;
	
	/** The label7. */
	private JLabel label7;
	
	/** The txt comp name. */
	private JTextField txtCompName;
	
	/** The label5. */
	private JLabel label5;
	
	/** The c prio. */
	private JComboBox cPrio;
	
	/** The label6. */
	private JLabel label6;
	
	/** The c stat. */
	private JComboBox cStat;
	
	/** The label8. */
	private JLabel label8;
	
	/** The c cate. */
	private JComboBox cCate;
	
	/** The label11. */
	private JLabel label11;
	
	/** The txt user. */
	private JTextField txtUser;
	
	/** The panel1. */
	private JPanel panel1;
	
	/** The b save. */
	private JButton bSave;
	
	/** The b cancel. */
	private JButton bCancel;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
