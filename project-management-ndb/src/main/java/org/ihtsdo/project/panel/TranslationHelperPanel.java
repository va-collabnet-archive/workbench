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
package org.ihtsdo.project.panel;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.tapi.TerminologyException;

/**
 * The Class TranslationHelperPanel.
 */
public class TranslationHelperPanel {
	
	/** The tabs. */
	private static JTabbedPane tabs;

	public static final String  REFSETPARTITIONER_TAB_NAME="Refset partitioner";

	public static final String PROJECTS_DETAILS_TAB_NAME="Project Properties";

	/** The Constant ISSUE_REPO_PROPERTY_NAME. */
	public static final String ISSUE_REPO_PROPERTY_NAME="IssueRepositories";
	
	/** The Constant ISSUE_TAB_NAME. */
	public static final String ISSUE_TAB_NAME="Issue";
	
	/** The Constant ISSUE_LIST_TAB_NAME. */
	public static final String ISSUE_LIST_TAB_NAME="Issue List";
	
	/** The Constant ISSUE_REPO_TAB_NAME. */
	public static final String ISSUE_REPO_TAB_NAME="Issue Repository";
	
	/** The Constant PROJECT_MGR_TAB_NAME. */
	public static final String PROJECT_MGR_TAB_NAME="Project Manager";
	
	/** The Constant WORKSET_MGR_TAB_NAME. */
	public static final String WORKSET_MGR_TAB_NAME="Workset Manager";
	
	/** The Constant WORKLIST_MGR_TAB_NAME. */
	public static final String WORKLIST_MGR_TAB_NAME="Worklist Manager";
	
	/** The Constant BATCHPLAN_MGR_TAB_NAME. */
	public static final String BATCHPLAN_MGR_TAB_NAME="Batching plan Manager";
	
	/** The Constant IX_DICTIO_TAB_NAME. */
	public static final String IX_DICTIO_TAB_NAME="Index Dictionary";
	
	/** The Constant IX_DOCS_TAB_NAME. */
	public static final String IX_DOCS_TAB_NAME="Index Documents";
	
	/** The Constant IX_TRANSL_MEMORY_TAB_NAME. */
	public static final String IX_TRANSL_MEMORY_TAB_NAME="Index Translation Memory";
	
	/** The Constant SIMILARITY_TAB_NAME. */
	public static final String SIMILARITY_TAB_NAME="Translation help";
	
	/** The Constant SEARCH_DICT_TAB_NAME. */
	public static final String SEARCH_DICT_TAB_NAME="Search Dictionary";
	
	/** The Constant SEARCH_DOCS_TAB_NAME. */
	public static final String SEARCH_DOCS_TAB_NAME="Search Documents";
	
	/** The Constant SEARCH_TRANSL_MEM_TAB_NAME. */
	public static final String SEARCH_TRANSL_MEM_TAB_NAME="Search Translation Memory";
	
	/** The Constant TRANSLATION_TAB_NAME. */
	public static final String TRANSLATION_TAB_NAME="Translation";
	
	/** The Constant ARCHIVAL_TAB_NAME. */
	public static final String ARCHIVAL_TRANSLATION_TAB_NAME = "Archival translation";
	
	/** The Constant ISSUE_VIEW_TAB_NAME. */
	public static final String ISSUE_VIEW_TAB_NAME="Translation Issues";
	
	/** The Constant GLOSSARY_ENFORCE. */
	public static final String GLOSSARY_ENFORCE="Glossary enforcement";
	
	/** The Constant HIERARCHY_NAVIGATOR. */
	public static final String HIERARCHY_NAVIGATOR="Hierarchy Navigator";
	
	/** The Constant CONCEPT_VERSIONS_TAB_NAME. */
	public static final String CONCEPT_VERSIONS_TAB_NAME="Concept Versions";
	
	/** The Constant TREE_EDITOR. */
	public static final String TREE_EDITOR="Tree Editor";
	
	/** The Constant MULTI_VIEWER. */
	public static final String MULTI_VIEWER="Multi Viewer";
	
	/** The Constant TRANS_REPORT. */
	public static final String TRANS_REPORT="Report";
	
	public static final String SUBSET_IMPORT_EXPORT = "Import-Export";

	public static final String TRANSLATION_LEFT_MENU = "Inbox";
	
	public static final String ARCHIVAL_ITEMS_LEFT_MENU = "Sp Translation Menu";
	
	public static final String QA_MANAGER = "QA Manager";
	
	public static final String AUTO_PROCESS_WORKLIST_MEMBERS_REVIEW = "Auto-Process for Worklist Members Review";

	public static final String AUTO_PROCESS_WORKLIST_MEMBERS_REVIEW_CANCEL = "Cancel Auto-Process for Worklist Members Review"; 
	
	public static final String EXTERNAL_ISSUES_ACTIVE_PROPERTY_FLAG="A: EXTERNAL_ISSUES_ACTIVE";
	
	public static final String REFSET_VIEWER_NAME = "Refset viewer";
	
	public static final String REPORT_PANEL_NAME = "Reports";
	
	public static final String STRESS_TAB_NAME = "Stress test";

	public static final String MEMBER_LOG_TAB_NAME = "Member log";

	public static final String LIST_COMPONENT_VIEWER_NAME = "List component viewer";


	/**
	 * Instantiates a new translation helper panel.
	 * 
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public TranslationHelperPanel() throws TerminologyException, IOException {
		initialize();
	}

	/**
	 * Initialize.
	 * 
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void initialize() throws TerminologyException, IOException{
		I_TermFactory termFactory= Terms.get();
		I_ConfigAceFrame config= termFactory.getActiveAceFrameConfig();
		final JPanel signpostPanel = config.getSignpostPanel();
		
		if (tabs==null){
			tabs = new JTabbedPane();
			signpostPanel.setSize(signpostPanel.getWidth(), 370);
		}
		else{
			Component[] components = signpostPanel.getComponents();
			for (int i = 0; i < components.length; i++) {
				if (signpostPanel.getComponent(i).equals(tabs)){
		            config.setShowSignpostPanel(true);
		            signpostPanel.revalidate();
					signpostPanel.repaint();
					Container cont = signpostPanel;
					while (cont != null) {
						cont.validate();
						cont = cont.getParent();
					}
					return;
				}
			}	
			if (signpostPanel.getHeight()<370)
				signpostPanel.setSize(signpostPanel.getWidth(),  370);
		}
		Component[] components = signpostPanel.getComponents();
		for (int i = 0; i < components.length; i++) {
			signpostPanel.remove(components[i]);
		}

		signpostPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.anchor = GridBagConstraints.NORTHWEST;
		signpostPanel.add(tabs, c);

		config.setShowSignpostPanel(true);
		signpostPanel.setSize(signpostPanel.getWidth(), 370);
		signpostPanel.repaint();
		Container cont = signpostPanel;
		while (cont != null) {
			cont.validate();
			cont = cont.getParent();
		}
		
	}
	
	/**
	 * Gets the tabbed panel.
	 * 
	 * @return the tabbed panel
	 */
	public JTabbedPane getTabbedPanel(){
		return tabs;
	}
	
	public static void closeProjectDetailsTab(I_ConfigAceFrame config){

		AceFrameConfig afconfig=(AceFrameConfig)config;
		AceFrame ace=afconfig.getAceFrame();
		JTabbedPane tp=ace.getCdePanel().getConceptTabs();
		if (tp!=null){
			int tabCount=tp.getTabCount();
			for (int i=tabCount-1;i>-1;i--){
				if (tp.getTitleAt(i).equals(TranslationHelperPanel.PROJECTS_DETAILS_TAB_NAME)){
					tp.remove(i);
					tp.revalidate();
					tp.repaint();
				}else{
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.REFSETPARTITIONER_TAB_NAME)){
						tp.remove(i);
						tp.revalidate();
						tp.repaint();
					}
				}
			}
		}
	}
	synchronized 
	public static void setFocusToProjectPanel(){
		
		Timer timer = new Timer(1100, new setProjectPanelFocus());
		timer.setRepeats(false);
		timer.start();
	}
	static class setProjectPanelFocus implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			AceFrameConfig config;
			try {
				config = (AceFrameConfig)Terms.get().getActiveAceFrameConfig();
				AceFrame acef=config.getAceFrame();
				
				JTabbedPane tp=acef.getCdePanel().getLeftTabs();
				
				if (tp!=null){
					int tabCount=tp.getTabCount();
					for (int i=0;i<tabCount;i++){
						if (tp.getTitleAt(i).equals(TranslationHelperPanel.PROJECT_MGR_TAB_NAME)){
							tp.setSelectedIndex(i);
							tp.revalidate();
							tp.repaint();
							return;
						}
					}
				}
			} catch (TerminologyException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
			
		}
	}
	public static void refreshProjectPanelNode(I_ConfigAceFrame config){

		AceFrameConfig afconfig=(AceFrameConfig)config;
		AceFrame ace=afconfig.getAceFrame();
		JTabbedPane tp=ace.getCdePanel().getLeftTabs();
		if (tp!=null){
			int tabCount=tp.getTabCount();
			for (int i=0;i<tabCount;i++){
				if (tp.getTitleAt(i).equals(TranslationHelperPanel.PROJECT_MGR_TAB_NAME)){
					tp.setSelectedIndex(i);

					((ProjectsPanel)tp.getComponentAt(i)).refreshNode();
					tp.revalidate();
					tp.repaint();
					break;
				}
			}
		}
		setFocusToProjectPanel();
	}

	public static void refreshProjectPanelParentNode(I_ConfigAceFrame config){

		AceFrameConfig afconfig=(AceFrameConfig)config;
		AceFrame ace=afconfig.getAceFrame();
		JTabbedPane tp=ace.getCdePanel().getLeftTabs();
		if (tp!=null){
			int tabCount=tp.getTabCount();
			for (int i=0;i<tabCount;i++){
				if (tp.getTitleAt(i).equals(TranslationHelperPanel.PROJECT_MGR_TAB_NAME)){
					tp.setSelectedIndex(i);

					((ProjectsPanel)tp.getComponentAt(i)).refreshParentNode();
					tp.revalidate();
					tp.repaint();
					break;
				}
			}
		}
		setFocusToProjectPanel();
	}
	/**
	 * Show tabbed panel.
	 * 
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void showTabbedPanel() throws TerminologyException, IOException{
		initialize();
	}
}
