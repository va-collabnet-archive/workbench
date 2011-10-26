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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

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
 * The Class IssuesListPanel.
 */
public class IssuesListPanel extends JPanel {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The debug. */
	private boolean debug=true;
	
	/** The term factory. */
	private I_TermFactory termFactory;
	
	/** The config. */
	private I_ConfigAceFrame config;
	
	/** The db config. */
	private I_ConfigAceDb dbConfig;
	
	/** The im. */
	private I_IssueManager im;
	
	/** The issue repo. */
	private IssueRepository issueRepo;
	
	/**
	 * Instantiates a new issues list panel.
	 * 
	 * @throws Exception the exception
	 */
	public IssuesListPanel() throws Exception {
		termFactory=LocalVersionedTerminology.get();
		initComponents();
		config=termFactory.getActiveAceFrameConfig();
		dbConfig= config.getDbConfig();
		loadRepos(null);
		addListeners();
	}
	
	/**
	 * Instantiates a new issues list panel.
	 * 
	 * @param termFactory the term factory
	 * 
	 * @throws Exception the exception
	 */
	public IssuesListPanel(I_TermFactory termFactory) throws Exception {
		this.termFactory=termFactory;
		initComponents();
		this.config=termFactory.getActiveAceFrameConfig();
		this.dbConfig= config.getDbConfig();
		loadRepos(null);
		addListeners();
	}
	
	/**
	 * Adds the listeners.
	 */
	private void addListeners(){
		   SelectionListener listener = new SelectionListener(table1);
		    table1.getSelectionModel().addListSelectionListener(listener);
	}
    
	/**
	 * The listener interface for receiving selection events.
	 * The class that is interested in processing a selection
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addSelectionListener<code> method. When
	 * the selection event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see SelectionEvent
	 */
	class SelectionListener implements ListSelectionListener {
        
        /** The table. */
        JTable table;
    
        // It is necessary to keep the table since it is not possible
        // to determine the table from the event's source
        /**
         * Instantiates a new selection listener.
         * 
         * @param table the table
         */
        SelectionListener(JTable table) {
            this.table = table;
        }
        
        /* (non-Javadoc)
         * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
         */
        public void valueChanged(ListSelectionEvent e) {
            // If cell selection is enabled, both row and column change events are fired
        	if (!(e.getSource() == table.getSelectionModel()
                  && table.getRowSelectionAllowed())) {
        		return;
            }
    
            if (e.getValueIsAdjusting()) {
                // The mouse button has not yet been released
            	return;
            }
            else{
        		int  first=table.getSelectedRow();
        		if (first>-1){
        			Object row=table.getModel().getValueAt(first, 0);
            		if (row!=null){
            			Issue issue=(Issue)((ListObj)row).getAtrValue();
            			if (issue!=null && issueRepo!=null){        
            				try {

            					pBar.setIndeterminate(true);
            					pBar.setVisible(true);
            					pBar.repaint();
            					pBar.revalidate();
            					//TODO: fix
								//issuesPanel1.setInitEdition(issue, issueRepo);
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
            					pBar.setIndeterminate(false);
            					pBar.setVisible(false);
								message("Sorry, cannot set the issue on panel");
							}
            				issueCommentsPanel1.setInit(issue, issueRepo, null, config.getUsername());
        					pBar.setIndeterminate(false);
        					pBar.setVisible(false);
            			}
            		}
            	}
            	
            }
        }
    }

	/**
	 * C repo item state changed.
	 */
	private void cRepoItemStateChanged() {		
		issueRepo=(IssueRepository)((ListObj)cRepo.getSelectedItem()).getAtrValue();
//		if (ir.getType()==IssueRepository.REPOSITORY_TYPE.WEB_SITE.ordinal()){
//			loadCNIssueManager();
//		}else{
//			loadAceIssueManager();
//		}
		if (issueRepo!=null){
			pBar.setIndeterminate(true);
			pBar.setVisible(true);
			loadIssues();
			pBar.setIndeterminate(false);
			pBar.setVisible(false);
		}
	}

	/**
	 * Load issues.
	 */
	private void loadIssues() {
		List<Issue>issueL=new ArrayList<Issue>();
		
		if (issueRepo.getType()==IssueRepository.REPOSITORY_TYPE.WEB_SITE.ordinal()){
			CollabnetIssueManager cIM=new CollabnetIssueManager();
			
			try{
				//TODO: fix
				cIM.openRepository(issueRepo, "", "");
			}catch(Exception e){
				e.printStackTrace();
				message("Sorry, cannot connect to repository.\n" + e.getMessage());
				return;
			}
			try {
				issueL=cIM.getAllIssues();
			} catch (Exception e) {
				e.printStackTrace();
				message("Sorry, cannot retrieve comments for this issue.\n" + e.getMessage());
				return;
			}
		}
		loadIssuesTable(issueL);
		
		
	}

	/**
	 * Load issues table.
	 * 
	 * @param issueL the issue l
	 */
	private void loadIssuesTable(List<Issue> issueL) {
		String[] columnNames = {"Title",
		        "Status","Component","User"};
		String[][] data = null;
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};
		for (int i=0;i<issueL.size();i++) {
			//TODO: fix
//			tableModel.addRow(new Object[] {new ListObj("I",issueL.get(i).getTitle() ,issueL.get(i)),issueL.get(i).getStatus() ,
//					issueL.get(i).getComponent(),issueL.get(i).getExternalUser()});
		}

		table1.setModel(tableModel);
		TableColumnModel cmodel = table1.getColumnModel(); 
		TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
		cmodel.getColumn(0).setCellRenderer(textAreaRenderer); 
		cmodel.getColumn(1).setCellRenderer(textAreaRenderer); 
		cmodel.getColumn(2).setCellRenderer(textAreaRenderer); 
		cmodel.getColumn(3).setCellRenderer(textAreaRenderer); 
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
	 * Load repos.
	 * 
	 * @param issueRepository the issue repository
	 */
	@SuppressWarnings("unchecked")
	private void loadRepos(IssueRepository issueRepository) {
		// TODO Auto-generated method stub
		MutableComboBoxModel mutComboModel=(MutableComboBoxModel)new DefaultComboBoxModel();
		cRepo.setModel(mutComboModel);
		
		try{
			boolean selected;
			if (debug){
				addObjectToCombo(cRepo, null, "None",true);
				List<IssueRepository> irs=IssueRepositoryDAO.getAllIssueRepository(config);
				
				for(IssueRepository ir:irs){
					selected=false;
					if (issueRepository!=null)
						if(issueRepository.getId()==ir.getId())	selected=true;
						
					addObjectToCombo(cRepo, ir, ir.getName(),selected);
					
				}
			}
			else{
				addObjectToCombo(cRepo, null, "None",true);
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
	 * Inits the components.
	 * 
	 * @throws Exception the exception
	 */
	private void initComponents() throws Exception {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label1 = new JLabel();
		cRepo = new JComboBox();
		pBar = new JProgressBar();
		splitPane1 = new JSplitPane();
		scrollPane1 = new JScrollPane();
		table1 = new JTable();
		tabbedPane1 = new JTabbedPane();
		scrollPane2 = new JScrollPane();
		issuesPanel1 = new IssuesPanel();
		issueCommentsPanel1 = new IssueCommentsPanel();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 196, 75, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {35, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

		//---- label1 ----
		label1.setText("Issue  Repository");
		label1.setFont(new Font("Tahoma", Font.BOLD, 12));
		add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- cRepo ----
		cRepo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				cRepoItemStateChanged();
			}
		});
		add(cRepo, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- pBar ----
		pBar.setVisible(false);
		pBar.setIndeterminate(true);
		add(pBar, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== splitPane1 ========
		{
			splitPane1.setDividerLocation(250);

			//======== scrollPane1 ========
			{

				//---- table1 ----
				table1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				scrollPane1.setViewportView(table1);
			}
			splitPane1.setLeftComponent(scrollPane1);

			//======== tabbedPane1 ========
			{

				//======== scrollPane2 ========
				{
					scrollPane2.setViewportView(issuesPanel1);
				}
				tabbedPane1.addTab("Issue", scrollPane2);

				tabbedPane1.addTab("Comments", issueCommentsPanel1);

			}
			splitPane1.setRightComponent(tabbedPane1);
		}
		add(splitPane1, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	/** The label1. */
	private JLabel label1;
	
	/** The c repo. */
	private JComboBox cRepo;
	
	/** The p bar. */
	private JProgressBar pBar;
	
	/** The split pane1. */
	private JSplitPane splitPane1;
	
	/** The scroll pane1. */
	private JScrollPane scrollPane1;
	
	/** The table1. */
	private JTable table1;
	
	/** The tabbed pane1. */
	private JTabbedPane tabbedPane1;
	
	/** The scroll pane2. */
	private JScrollPane scrollPane2;
	
	/** The issues panel1. */
	private IssuesPanel issuesPanel1;
	
	/** The issue comments panel1. */
	private IssueCommentsPanel issueCommentsPanel1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
