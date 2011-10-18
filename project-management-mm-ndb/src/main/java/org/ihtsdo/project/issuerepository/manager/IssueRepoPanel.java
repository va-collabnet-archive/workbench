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

package org.ihtsdo.project.issuerepository.manager;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.MutableComboBoxModel;

import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.panel.TranslationHelperPanel;

/**
 * The Class IssueRepoPanel.
 */
public class IssueRepoPanel extends JPanel {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The term factory. */
	private I_TermFactory termFactory;
	
	/** The config. */
	private I_ConfigAceFrame config;
	
	/** The db config. */
	private I_ConfigAceDb dbConfig; 
	
	/**
	 * Instantiates a new issue repo panel.
	 * 
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public IssueRepoPanel() throws TerminologyException, IOException {
		termFactory=LocalVersionedTerminology.get();
		config=termFactory.getActiveAceFrameConfig();
		initComponents();
		loadValues();
	}

	/**
	 * Instantiates a new issue repo panel.
	 * 
	 * @param termFactory the term factory
	 * 
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public IssueRepoPanel(I_TermFactory termFactory) throws TerminologyException, IOException {
		this.termFactory=termFactory;
		config=termFactory.getActiveAceFrameConfig();
		initComponents();
		loadValues();
	}

	/**
	 * Load values.
	 */
	private void loadValues() {
		// TODO Auto-generated method stub
		MutableComboBoxModel mutComboModel=(MutableComboBoxModel)new DefaultComboBoxModel();
		cProj.setModel(mutComboModel);
		loadProjects();
		loadTypes();
	}

	/**
	 * Load types.
	 */
	private void loadTypes() {
		// TODO Auto-generated method stub
		cType.setModel(new DefaultComboBoxModel(IssueRepository.REPOSITORY_TYPE.values()));
		
	}

	/**
	 * Load projects.
	 */
	private void loadProjects() {
		// TODO Auto-generated method stub
		   List<TranslationProject > projects=TerminologyProjectDAO.getAllTranslationProjects(config) ;
	        
	        for (int i=0;i<projects.size();i++ ){
	        	addObjectToCombo(cProj, projects.get(i).getId(),projects.get(i).getName());
	        }
	}

	/**
	 * Adds the object to combo.
	 * 
	 * @param cmb the cmb
	 * @param object the object
	 * @param name the name
	 */
	private void addObjectToCombo(JComboBox cmb,
			Object object,String name) {
		// TODO Auto-generated method stub
		cmb.addItem(new ListObj("P",name,object));
	}

	/**
	 * B save action performed.
	 */
	@SuppressWarnings("unchecked")
	private void bSaveActionPerformed() {
		// TODO add your code here
		IssueRepository ir=new IssueRepository();
		ir.setExternalProjectId(txtExtProjId.getText());
		ir.setRepositoryId(txtExtRepoId.getText());
		ir.setName(txtName.getText());
		ir.setProjectId((Integer)((ListObj)cProj.getSelectedItem()).getAtrValue() );
		ir.setType(((IssueRepository.REPOSITORY_TYPE) cType.getSelectedItem()).ordinal());
		ir.setUrl(txtUrl.getText());
		//TODO:fix
//		ir.setConnUser(txtUser.getText());
//		ir.setConnPass(txtPass.getText());
		
		I_GetConceptData issConcept=IssueRepositoryDAO.addIssueRepoToMetahier(ir, config);

		message("Issue repository created");
		

//        I_ImplementTermFactory impTermFactory = (I_ImplementTermFactory) LocalVersionedTerminology.get();
//		dbConfig= impTermFactory.newAceDbConfig();
//         
//		dbConfig.getAceFrames().add(config);
//       
		
		dbConfig=config.getDbConfig();
		List<UUID> repoUUIDs;
		try {
			Object obj=dbConfig.getProperty(TranslationHelperPanel.ISSUE_REPO_PROPERTY_NAME);
			if (obj!=null){
				repoUUIDs = (List<UUID>)obj;
			}else{
				repoUUIDs=new ArrayList<UUID>();
			}
			for (UUID uid:issConcept.getUids()){
				repoUUIDs.add(uid);
			}
			dbConfig.setProperty(TranslationHelperPanel.ISSUE_REPO_PROPERTY_NAME,repoUUIDs);
			message("Issue repository added to user");

		} catch (IOException e) {
			// TODO Auto-generated catch block
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
		loadProjects();
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label2 = new JLabel();
		label1 = new JLabel();
		label3 = new JLabel();
		label8 = new JLabel();
		label9 = new JLabel();
		label4 = new JLabel();
		label5 = new JLabel();
		label6 = new JLabel();
		label7 = new JLabel();
		txtPass = new JTextField();
		txtUser = new JTextField();
		txtUrl = new JTextField();
		txtExtRepoId = new JTextField();
		txtExtProjId = new JTextField();
		cProj = new JComboBox();
		cType = new JComboBox();
		txtName = new JTextField();
		bRefresh = new JButton();
		panel1 = new JPanel();
		bSave = new JButton();
		bCancel = new JButton();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

		//---- label2 ----
		label2.setText("Name");
		add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- label1 ----
		label1.setText("Issue Repository");
		label1.setFont(label1.getFont().deriveFont(label1.getFont().getStyle() | Font.BOLD, label1.getFont().getSize() + 2f));
		add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- label3 ----
		label3.setText("Type");
		add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- label8 ----
		label8.setText("Project");
		add(label8, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- label9 ----
		label9.setText("External Project Id");
		add(label9, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- label4 ----
		label4.setText("External Id");
		add(label4, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- label5 ----
		label5.setText("Url");
		add(label5, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- label6 ----
		label6.setText("Connection User");
		add(label6, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- label7 ----
		label7.setText("Connection Pass");
		add(label7, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		add(txtPass, new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		add(txtUser, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		add(txtUrl, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		add(txtExtRepoId, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		add(txtExtProjId, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		add(cProj, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		add(cType, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		add(txtName, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- bRefresh ----
		bRefresh.setText("Refresh");
		bRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bRefreshActionPerformed();
			}
		});
		add(bRefresh, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

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
		add(panel1, new GridBagConstraints(1, 10, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 5), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JLabel label2;
	private JLabel label1;
	private JLabel label3;
	private JLabel label8;
	private JLabel label9;
	private JLabel label4;
	private JLabel label5;
	private JLabel label6;
	private JLabel label7;
	private JTextField txtPass;
	private JTextField txtUser;
	private JTextField txtUrl;
	private JTextField txtExtRepoId;
	private JTextField txtExtProjId;
	private JComboBox cProj;
	private JComboBox cType;
	private JTextField txtName;
	private JButton bRefresh;
	private JPanel panel1;
	private JButton bSave;
	private JButton bCancel;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
