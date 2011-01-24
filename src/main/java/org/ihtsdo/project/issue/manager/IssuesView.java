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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.issue.Issue;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;
import org.ihtsdo.issue.manager.implementation.CollabnetIssueManager;
import org.ihtsdo.issue.manager.implementation.I_IssueManager;
import org.ihtsdo.project.panel.TranslationHelperPanel;
import org.ihtsdo.project.panel.TreeObj;

/**
 * The Class IssuesView.
 */
public class IssuesView extends JPanel {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The concept. */
	private I_GetConceptData concept;
	
	/** The config. */
	private I_ConfigAceFrame config;
	
	/** The term factory. */
	private I_TermFactory termFactory;
	
	/** The tree model. */
	private DefaultTreeModel treeModel;
	
	/** The root node. */
	private DefaultMutableTreeNode rootNode;
	
	/** The debug. */
	private boolean debug=false;
	
	/** The issues repos. */
	private List<IssueRepository> issuesRepos;
	
	/** The db config. */
	private I_ConfigAceDb dbConfig;
	
	/** The source lang code. */
	private String sourceLangCode;
	
	/** The im. */
	private I_IssueManager im;
	
	/** The Constant CONCEPTNODE. */
	private static final String CONCEPTNODE = "C";
	
	/** The Constant ISSUENODE. */
	private static final String ISSUENODE = "IN";
	
	/** The Constant FSNROOTNODE. */
	private static final String FSNROOTNODE = "FR";
	
	/** The Constant PREFROOTNODE. */
	private static final String PREFROOTNODE = "PR";
	
	/** The Constant SYNROOTNODE. */
	private static final String SYNROOTNODE = "SR";
	
	/** The Constant DESCRIPTIONNODE. */
	private static final String DESCRIPTIONNODE = "DN";
	
	/**
	 * Instantiates a new issues view.
	 * 
	 * @param concept the concept
	 * @param sourceLangCode the source lang code
	 * 
	 * @throws Exception the exception
	 */
	public IssuesView(I_GetConceptData concept,String sourceLangCode) throws Exception {
		this.concept=concept;
		this.sourceLangCode=sourceLangCode;
		initComponents();
		loadValues();
	}

	/**
	 * Load values.
	 */
	@SuppressWarnings("unchecked")
	private void loadValues() {
		// TODO Auto-generated method stub
		try{
			DefaultMutableTreeNode rootNode=new DefaultMutableTreeNode("Root node");
			treeModel=new DefaultTreeModel(rootNode);
			tree1.setModel(treeModel);
			tree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	        tree1.setRootVisible(false);
	        termFactory=LocalVersionedTerminology.get();
			config= termFactory.getActiveAceFrameConfig();
			dbConfig= config.getDbConfig();
	
			DefaultMutableTreeNode conceptNode= addObject(rootNode,new TreeObj(CONCEPTNODE,"Concept: " + concept.getInitialText(),null),true);
			
			int FSNid=termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
			int PREFid=termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
			int SYNid=termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids());
			
			issuesRepos=new ArrayList<IssueRepository>();
	
			loadRepos();
			List<I_DescriptionTuple> descriptions;
			if (issuesRepos.size()>0){
				for (IssueRepository ir:issuesRepos)
					loadIssuesForConcept(ir,conceptNode);
				
				DefaultMutableTreeNode fsn=addObject(rootNode,new TreeObj(FSNROOTNODE,"Fully Specified Name",null),true); 
				rootNode.add(fsn);
				DefaultMutableTreeNode preferred=addObject(rootNode,new TreeObj(PREFROOTNODE,"Preferred Term",null),true); 
				rootNode.add(preferred);
				DefaultMutableTreeNode synonym=addObject(rootNode,new TreeObj(SYNROOTNODE,"Synonym",null),true); 
				rootNode.add(synonym);
				descriptions = (List<I_DescriptionTuple>) concept.getDescriptionTuples(config.getAllowedStatus(), config.getDescTypes(), 
						config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
				for (I_DescriptionTuple description : descriptions) {
					if (description.getLang().equals(sourceLangCode) ) {
						if (description.getTypeId() == FSNid) {
							DefaultMutableTreeNode nodeTmp=addObject(fsn,new TreeObj(DESCRIPTIONNODE,description.getText(),null),true);
							for (IssueRepository ir:issuesRepos)
								loadIssuesForDescription(ir,nodeTmp,description);
						} else if (description.getTypeId() == PREFid) {
							DefaultMutableTreeNode nodeTmp=addObject(preferred,new TreeObj(DESCRIPTIONNODE,description.getText(),null),true);
							for (IssueRepository ir:issuesRepos)
								loadIssuesForDescription(ir,nodeTmp,description);
						} else if (description.getTypeId() == SYNid) {
							DefaultMutableTreeNode nodeTmp=addObject(synonym,new TreeObj(DESCRIPTIONNODE,description.getText(),null),true);
							for (IssueRepository ir:issuesRepos)
								loadIssuesForDescription(ir,nodeTmp,description);
						}
					}
				}
	
			}
			else{
				message("No issues repositories registred for user.");
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			message(e.getMessage());
		} catch (TerminologyException e) {
			e.printStackTrace();
			message(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			message(e.getMessage());
		}
	}


	/**
	 * Load issues for description.
	 * 
	 * @param iRepo the i repo
	 * @param node the node
	 * @param description the description
	 */
	private void loadIssuesForDescription(IssueRepository iRepo,
			DefaultMutableTreeNode node, I_DescriptionTuple description) {
		if (iRepo.getType()==IssueRepository.REPOSITORY_TYPE.WEB_SITE.ordinal()){
			
			try {
				if (im==null){
					im=new CollabnetIssueManager();
					//TODO:fix
					im.openRepository(iRepo,"","");
				}
				List<Issue>issueL=im.getIssuesForComponentId(description.getDescVersioned().getUniversal().getDescId().iterator().next().toString());
				for (Issue issue:issueL){
					TreeObj to=new TreeObj(ISSUENODE,"<html><img src='file:icons/blue.gif' /><b>Issue title: </b>" + issue.getTitle() + "</html>",new Object[]{issue,iRepo});
					addObject(node,to,true);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				message(e.getMessage());
			}
		}
		else{
			// TODO Auto-generated method stub
	    	
		}	
		
	}

	/**
	 * Load repos.
	 */
	@SuppressWarnings("unchecked")
	private void loadRepos() {
		// TODO Auto-generated method stub
		try{
			if (debug){
				issuesRepos=IssueRepositoryDAO.getAllIssueRepository(config);
				
			}
			else{
				List<UUID>repoUUIDs=(List<UUID>) dbConfig.getProperty(TranslationHelperPanel.ISSUE_REPO_PROPERTY_NAME);
				if (repoUUIDs!=null){
					boolean existInList;
					for (UUID uid:repoUUIDs){
						I_GetConceptData irepoConcept=termFactory.getConcept(new UUID[]{uid});
						IssueRepository ir=IssueRepositoryDAO.getIssueRepository(irepoConcept);
						existInList=false;
						for (IssueRepository irepo:issuesRepos){
							if (irepo.getId()==ir.getId()){
								existInList=true;
								break;
							}
						}
						if (!existInList)
							issuesRepos.add(ir);
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
     * Load issues for concept.
     * 
     * @param iRepo the i repo
     * @param node the node
     */
    private void loadIssuesForConcept(IssueRepository iRepo,DefaultMutableTreeNode node) {
		if (iRepo.getType()==IssueRepository.REPOSITORY_TYPE.WEB_SITE.ordinal()){
			im=new CollabnetIssueManager();
			try {
				//TODO:fix
				im.openRepository(iRepo,"","");
			
				List<Issue>issueL=im.getIssuesForComponentId(concept.getUids().get(0).toString());
				for (Issue issue:issueL){
					TreeObj to=new TreeObj(ISSUENODE,"<html><img src='file:icons/green.gif' /><b>Issue title: </b>" + issue.getTitle() + "</html>",new Object[]{issue,iRepo});
					addObject(node,to,true);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				message(e.getMessage());
			}
		}
		else{
			// TODO Auto-generated method stub
	    	
		}	
    	
		
	}

	/**
	 * Adds the object.
	 * 
	 * @param child the child
	 * 
	 * @return the default mutable tree node
	 */
	public DefaultMutableTreeNode addObject(Object child) {
        DefaultMutableTreeNode parentNode = null;
        TreePath parentPath = tree1.getSelectionPath();

        if (parentPath == null) {
            //There's no selection. Default to the root node.
            parentNode = rootNode;
        } else {
            parentNode = (DefaultMutableTreeNode)
                         (parentPath.getLastPathComponent());
        }

        return addObject(parentNode, child, true);
    }
    
    /**
     * Adds the object.
     * 
     * @param parent the parent
     * @param child the child
     * @param shouldBeVisible the should be visible
     * 
     * @return the default mutable tree node
     */
    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                            Object child,
                                            boolean shouldBeVisible) {
        DefaultMutableTreeNode childNode =
                new DefaultMutableTreeNode(child);
        
        treeModel.insertNodeInto(childNode, parent,
                                 parent.getChildCount());

        //Make sure the user can see the lovely new node.
        if (shouldBeVisible) {
            tree1.scrollPathToVisible(new TreePath(childNode.getPath()));
        }
        return childNode;
    }


	/**
	 * Gets the issue panel.
	 * 
	 * @return the issue panel
	 */
	public IssuesPanel getIssuePanel(){
		return issuesPanel1;
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
	 * Tree1 value changed.
	 */
	private void tree1ValueChanged() { 
		DefaultMutableTreeNode node;
	    node = (DefaultMutableTreeNode)(tree1.getLastSelectedPathComponent());
	    
	    if (node == null) return;
	    TreeObj to=(TreeObj)node.getUserObject();
    	if (to.getObjType().equals(ISSUENODE) ){
    		Object[] aObj=(Object[])to.getAtrValue();
    		Issue issue=(Issue)aObj[0];
    		IssueRepository iRepo=(IssueRepository)aObj[1];
    		try {
				issuesPanel1.setInitEdition(issue, iRepo);
				issuesPanel1.revalidate();
				issuesPanel1.repaint();
				issueCommentsPanel1.setInit(issue, iRepo,null, config.getUsername());
				issueCommentsPanel1.revalidate();
				issueCommentsPanel1.repaint();
			} catch (Exception e) {
				message("Error: " + e.getLocalizedMessage());
				e.printStackTrace();
			}
    	}
	}
	
	/**
	 * Inits the components.
	 * 
	 * @throws Exception the exception
	 */
	private void initComponents() throws Exception {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		scrollPane1 = new JScrollPane();
		tree1 = new JTree();
		tabbedPane1 = new JTabbedPane();
		scrollPane2 = new JScrollPane();
		issuesPanel1 = new IssuesPanel();
		issueCommentsPanel1 = new IssueCommentsPanel();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {420, 0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0E-4};

		//======== scrollPane1 ========
		{

			//---- tree1 ----
			tree1.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					tree1ValueChanged();
				}
			});
			scrollPane1.setViewportView(tree1);
		}
		add(scrollPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//======== tabbedPane1 ========
		{

			//======== scrollPane2 ========
			{
				scrollPane2.setViewportView(issuesPanel1);
			}
			tabbedPane1.addTab("Issue", scrollPane2);

			tabbedPane1.addTab("Comments", issueCommentsPanel1);

		}
		add(tabbedPane1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	/** The scroll pane1. */
	private JScrollPane scrollPane1;
	
	/** The tree1. */
	private JTree tree1;
	
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
