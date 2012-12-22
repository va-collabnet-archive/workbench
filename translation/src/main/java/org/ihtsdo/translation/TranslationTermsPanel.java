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

package org.ihtsdo.translation;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.I_ContextualizeDescription;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * The Class TranslationTermsPanel.
 */
public class TranslationTermsPanel extends JPanel {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The synonym. */
	private I_GetConceptData synonym;
	
	/** The fsn. */
	private I_GetConceptData fsn;
	
	/** The l model. */
	private DefaultListModel lModel;
	
	/** The concept. */
	private I_GetConceptData concept;
	
	/**
	 * Instantiates a new translation terms panel.
	 *
	 * @param languageRefsetConcepts the language refset concepts
	 * @param concept the concept
	 */
	public TranslationTermsPanel(List<I_GetConceptData> languageRefsetConcepts,I_GetConceptData concept) {
		initComponents();
		this.concept=concept;
		lModel=new DefaultListModel();
		if (languageRefsetConcepts!=null){
			for (I_GetConceptData langConcept:languageRefsetConcepts){
				lModel.add(lModel.getSize(), langConcept);
			}
		}
//		loadLanguages();
		I_TermFactory tf = Terms.get();
		try {
			synonym = tf.getConcept(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
			fsn = tf.getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		tree1.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
		tree1.setCellRenderer(new IconRenderer());
		tree1.setRootVisible(true);
		tree1.setShowsRootHandles(false);
		DefaultTreeModel dtm;
		if (concept!=null && lModel!=null){
			try {
				dtm=getConceptTreeModel(concept);
				tree1.setModel(dtm);

				for (int i = 0; i < tree1.getRowCount(); i++) {
					tree1.expandRow(i);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			tree1.revalidate();
			tree1.repaint();
		}
			
		
	}
//
//	private void onceInit() throws Exception {
//		I_TermFactory tf = Terms.get();
//		I_GetConceptData concept = tf.getConcept(UUID.fromString("c265cf22-2a11-3488-b71e-296ec0317f96"));
//		I_GetConceptData parentConcept = tf.getConcept(UUID.fromString("900042ba-b637-38ae-95d7-1974896da7fb"));
//
//
//		System.out.println(concept + " - " + parentConcept);
//		originLangMemberRefset = ConceptMembershipRefset.createNewLanguageMembershipRefset(
//				"Origin Language Members" , parentConcept.getConceptId());
//
//		langMemberRefset = ConceptMembershipRefset.createNewLanguageMembershipRefset(
//				"Language Members for spec" , parentConcept.getConceptId());
//
//		langSpecRefset = LanguageSpecRefset.createNewLanguageSpecRefset(
//				"Language Spec", parentConcept.getConceptId(), 
//				langMemberRefset.getRefsetId(), originLangMemberRefset.getRefsetId());
//
//		List<ContextualizedDescription> descriptions = LanguageUtil.getContextualizedDescriptions(
//				concept.getConceptId(), originLangMemberRefset.getRefsetId(), true);
//		for (I_ContextualizeDescription description : descriptions) {
//			if (description.getTypeId() == fsn.getConceptId()) {
//				description.contextualizeThisDescription(originLangMemberRefset.getRefsetId(), 
//						preferred.getConceptId());
//			} else if (description.getTypeId() == synonym.getConceptId()) {
//				description.contextualizeThisDescription(originLangMemberRefset.getRefsetId(), 
//						acceptable.getConceptId());
//			} else {
//				description.contextualizeThisDescription(originLangMemberRefset.getRefsetId(), 
//						preferred.getConceptId());
//			}
//		}
//
//		for (I_ContextualizeDescription description : descriptions) {
//			if (description.getText().trim().equals("Asthmatic")) {
//				description.contextualizeThisDescription(langSpecRefset.getRefsetId(), 
//						preferred.getConceptId());
//			} else if (description.getText().trim().equals("Asthma")) {
//				description.contextualizeThisDescription(langSpecRefset.getRefsetId(), 
//						acceptable.getConceptId());
//			} else if (description.getText().trim().equals("BHR - Bronchial hyperreactivity")) {
//				description.contextualizeThisDescription(langSpecRefset.getRefsetId(), 
//						notAcceptable.getConceptId());
//			}
//		}
//
//		langSpecRefset.computeLanguageRefsetSpec();
//
//		tf.commit();
//		
//	}

	/**
 * Gets the concept tree model.
 *
 * @param concept the concept
 * @return the concept tree model
 * @throws Exception the exception
 */
private DefaultTreeModel getConceptTreeModel(I_GetConceptData concept) throws Exception {
		I_TermFactory tf = Terms.get();
		DefaultMutableTreeNode top = null;
		if (concept != null) {
			try {
				top = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toString(), TreeEditorObjectWrapper.CONCEPT, concept));

				for (int i=0;i<lModel.getSize();i++){

					I_GetConceptData langRefset=(I_GetConceptData)lModel.get(i);
					List<ContextualizedDescription> descriptions = LanguageUtil.getContextualizedDescriptions(
						concept.getConceptNid(), langRefset.getConceptNid(), true);

					DefaultMutableTreeNode groupLang = new DefaultMutableTreeNode(
							new TreeEditorObjectWrapper(langRefset.getInitialText(), TreeEditorObjectWrapper.FOLDER,langRefset ));

					List<DefaultMutableTreeNode> nodesToAdd = new ArrayList<DefaultMutableTreeNode>();
					
					for (I_ContextualizeDescription description : descriptions) {
						if (description.getLanguageExtension()!=null && description.getLanguageRefsetId()==langRefset.getConceptNid()){
							DefaultMutableTreeNode descriptionNode = null;
							if (description.getTypeId() == fsn.getConceptNid()) {
								descriptionNode = new DefaultMutableTreeNode(
										new TreeEditorObjectWrapper("FSN:" + description.getText(), TreeEditorObjectWrapper.FSNDESCRIPTION, description));

							} else if (description.getTypeId() == synonym.getConceptNid()) {
								descriptionNode = new DefaultMutableTreeNode(
										new TreeEditorObjectWrapper("Acceptable:" + description.getText(), TreeEditorObjectWrapper.SYNONYMN, description));
							} else {
								descriptionNode = new DefaultMutableTreeNode(
										new TreeEditorObjectWrapper("Preferred:" + description.getText(), TreeEditorObjectWrapper.PREFERRED , description));
							}

							I_GetConceptData descriptionStatusConcept = tf.getConcept(description.getDescriptionStatusId());
							descriptionNode.add(new DefaultMutableTreeNode(
									new TreeEditorObjectWrapper(descriptionStatusConcept.toString(), TreeEditorObjectWrapper.DESCRIPTIONINFO, description)));
							descriptionNode.add(new DefaultMutableTreeNode(
									new TreeEditorObjectWrapper("Is case significant: " + description.isInitialCaseSignificant(), TreeEditorObjectWrapper.DESCRIPTIONINFO, description)));
							nodesToAdd.add(descriptionNode);
						}
					}
					for (DefaultMutableTreeNode loopNode : nodesToAdd) {
						TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
						if (nodeObject.getType() == TreeEditorObjectWrapper.FSNDESCRIPTION) {
							groupLang.add(loopNode);
						}
					}

					for (DefaultMutableTreeNode loopNode : nodesToAdd) {
						TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
						if (nodeObject.getType() == TreeEditorObjectWrapper.PREFERRED) {
							groupLang.add(loopNode);
						}
					}

					for (DefaultMutableTreeNode loopNode : nodesToAdd) {
						TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
						if (nodeObject.getType() == TreeEditorObjectWrapper.SYNONYMN) {
							groupLang.add(loopNode);
						}
					}
					top.add(groupLang);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (TerminologyException e) {
				e.printStackTrace();
			}
		}
		
		DefaultTreeModel treeModel = new DefaultTreeModel(top);
		
		return treeModel;
	}

	/**
	 * Load languages.
	 */
	private void loadLanguages(){
		I_IntSet allowedDestRelTypes =  Terms.get().newIntSet();
		try {
			//TODO add config as parameter
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			DefaultListModel lModelExc = new DefaultListModel() ;
			allowedDestRelTypes.add(Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
			Collection<UUID> uuids = RefsetAuxiliary.Concept.LANGUAGE_REFSET.getUids();
			I_GetConceptData conc = Terms.get().getConcept(uuids);
			Set<? extends I_GetConceptData>concepts=conc.getDestRelOrigins(config.getAllowedStatus(),
					allowedDestRelTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), 
					config.getConflictResolutionStrategy());
			for (I_GetConceptData con:concepts){
				
				lModelExc.add(lModelExc.getSize(),con);
				
			}
			list1.setModel(lModelExc);
			list2.setModel(lModel);
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Tree1 value changed.
	 *
	 * @param e the e
	 */
	private void tree1ValueChanged(TreeSelectionEvent e) {
		// TODO add your code here
	}

	/**
	 * The Class IconRenderer.
	 */
	class IconRenderer extends DefaultTreeCellRenderer {
		
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;
		
		/** The red icon. */
		Icon redIcon;
		
		/** The attribute icon. */
		Icon attributeIcon;
		
		/** The orange icon. */
		Icon orangeIcon;
		
		/** The description icon. */
		Icon descriptionIcon;
		
		/** The fsn icon. */
		Icon fsnIcon;
		
		/** The black icon. */
		Icon blackIcon;
		
		/** The preferred icon. */
		Icon preferredIcon;
		
		/** The role group icon. */
		Icon roleGroupIcon;
		
		/** The folder. */
		Icon folder;

		/**
		 * Instantiates a new icon renderer.
		 */
		public IconRenderer() {
			redIcon = new ImageIcon("icons/91.png");
			attributeIcon = new ImageIcon("icons/ConceptStatus.gif");
			orangeIcon = new ImageIcon("icons/90.png");
			descriptionIcon = new ImageIcon("icons/Description.gif");
			fsnIcon = new ImageIcon("icons/Name.gif");
			blackIcon = new ImageIcon("icons/85.png");
			preferredIcon= new ImageIcon("icons/Preferred.gif");
			roleGroupIcon=new ImageIcon("icons/rolegroup.gif");
			folder=new ImageIcon("icons/folder.png");
		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
		 */
		@Override
		public Component getTreeCellRendererComponent(
				JTree tree,
				Object value,
				boolean sel,
				boolean expanded,
				boolean leaf,
				int row,
				boolean hasFocus) {

			super.getTreeCellRendererComponent(
					tree, value, sel,
					expanded, leaf, row,
					hasFocus);
			DefaultMutableTreeNode node =  (DefaultMutableTreeNode)value;
			if (node!=null && (node.getUserObject() instanceof TreeEditorObjectWrapper)){
				TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) node.getUserObject();
				switch (nodeObject.getType()) {
				case 3: setIcon(attributeIcon); break;
				case 4: setIcon(fsnIcon); break;
				case 5: setIcon(preferredIcon); break;
				case 6: setIcon(orangeIcon); break;
				case 7: setIcon(redIcon); break;
				case 8: setIcon(descriptionIcon); break;
				case 10: setIcon(descriptionIcon); break;
				case 11: setIcon(roleGroupIcon); break;
				case 12: setIcon(folder); break;
				default: setIcon(blackIcon); break;
				}
			}else{
				setIcon(blackIcon); 
			}
			return this;
		}

	}
    
    /**
     * Update.
     *
     * @param concept the concept
     */
	synchronized
    public void update(I_GetConceptData concept){
		this.concept=concept;
		DefaultTreeModel dtm;
		if (concept!=null && lModel!=null){
			try {
				dtm=getConceptTreeModel(concept);
				tree1.setModel(dtm);
				for (int i =0 ;i<tree1.getRowCount();i++){
					TreePath tp=tree1.getPathForRow(i);
					if (tp.getPath().length==2)
						tree1.expandRow(i);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			tree1.revalidate();
			tree1.repaint();
		}
    }

	/**
	 * List1 value changed.
	 */
	private void list1ValueChanged() {
		// TODO add your code here
	}

	/**
	 * B incl action performed.
	 */
	private void bInclActionPerformed() {
		DefaultListModel lModelExc=(DefaultListModel) list1.getModel();
		
		while (list1.getSelectedIndices().length>0){
			I_GetConceptData listObj=(I_GetConceptData)lModelExc.remove(list1.getSelectedIndices()[0]);
			lModel.add(lModel.getSize(),listObj);
		}
		
	}

	/**
	 * B excl action performed.
	 */
	private void bExclActionPerformed() {

		DefaultListModel lModelExc=(DefaultListModel) list1.getModel();
		
		while (list2.getSelectedIndices().length>0){
			I_GetConceptData listObj=(I_GetConceptData)lModel.remove(list2.getSelectedIndices()[0]);
			lModelExc.add(lModelExc.getSize(),listObj);
		}
	}

	/**
	 * List2 value changed.
	 */
	private void list2ValueChanged() {
		// TODO add your code here
	}

	/**
	 * Bsync action performed.
	 */
	private void bsyncActionPerformed() {
		loadLanguages();
	}

	/**
	 * Tabbed pane1 state changed.
	 */
	private void tabbedPane1StateChanged() {
		if (tabbedPane1.getSelectedIndex()==0)
			update(concept);
	}
	
	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		tabbedPane1 = new JTabbedPane();
		scrollPane1 = new JScrollPane();
		tree1 = new JTree();
		this2 = new JPanel();
		label1 = new JLabel();
		panel1 = new JPanel();
		label3 = new JLabel();
		bsync = new JButton();
		label4 = new JLabel();
		scrollPane2 = new JScrollPane();
		list1 = new JList();
		panel2 = new JPanel();
		bIncl = new JButton();
		bExcl = new JButton();
		scrollPane3 = new JScrollPane();
		list2 = new JList();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

		//======== tabbedPane1 ========
		{
			tabbedPane1.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					tabbedPane1StateChanged();
				}
			});

			//======== scrollPane1 ========
			{

				//---- tree1 ----
				tree1.setVisibleRowCount(1);
				tree1.addTreeSelectionListener(new TreeSelectionListener() {
					public void valueChanged(TreeSelectionEvent e) {
						tree1ValueChanged(e);
					}
				});
				scrollPane1.setViewportView(tree1);
			}
			tabbedPane1.addTab("View", scrollPane1);


			//======== this2 ========
			{
				this2.setLayout(new GridBagLayout());
				((GridBagLayout)this2.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
				((GridBagLayout)this2.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
				((GridBagLayout)this2.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0, 1.0E-4};
				((GridBagLayout)this2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

				//---- label1 ----
				label1.setText("Languages");
				this2.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== panel1 ========
				{
					panel1.setLayout(new GridBagLayout());
					((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
					((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- label3 ----
					label3.setText("Excluded");
					panel1.add(label3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- bsync ----
					bsync.setIcon(new ImageIcon("icons/isync_icon.jpg"));
					bsync.setSelectedIcon(null);
					bsync.setToolTipText("Refresh list");
					bsync.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							bsyncActionPerformed();
						}
					});
					panel1.add(bsync, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				this2.add(panel1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label4 ----
				label4.setText("Included");
				this2.add(label4, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== scrollPane2 ========
				{

					//---- list1 ----
					list1.addListSelectionListener(new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent e) {
							list1ValueChanged();
						}
					});
					scrollPane2.setViewportView(list1);
				}
				this2.add(scrollPane2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//======== panel2 ========
				{
					panel2.setLayout(new GridBagLayout());
					((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0};
					((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
					((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

					//---- bIncl ----
					bIncl.setText(">");
					bIncl.setFont(new Font("Lucida Grande", Font.BOLD, 13));
					bIncl.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							bInclActionPerformed();
							bInclActionPerformed();
						}
					});
					panel2.add(bIncl, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- bExcl ----
					bExcl.setText("<");
					bExcl.setFont(new Font("Lucida Grande", Font.BOLD, 13));
					bExcl.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							bExclActionPerformed();
							bExclActionPerformed();
						}
					});
					panel2.add(bExcl, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));
				}
				this2.add(panel2, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 0, 5), 0, 0));

				//======== scrollPane3 ========
				{

					//---- list2 ----
					list2.addListSelectionListener(new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent e) {
							list2ValueChanged();
						}
					});
					scrollPane3.setViewportView(list2);
				}
				this2.add(scrollPane3, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			tabbedPane1.addTab("Config", this2);

		}
		add(tabbedPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	/** The tabbed pane1. */
	private JTabbedPane tabbedPane1;
	
	/** The scroll pane1. */
	private JScrollPane scrollPane1;
	
	/** The tree1. */
	private JTree tree1;
	
	/** The this2. */
	private JPanel this2;
	
	/** The label1. */
	private JLabel label1;
	
	/** The panel1. */
	private JPanel panel1;
	
	/** The label3. */
	private JLabel label3;
	
	/** The bsync. */
	private JButton bsync;
	
	/** The label4. */
	private JLabel label4;
	
	/** The scroll pane2. */
	private JScrollPane scrollPane2;
	
	/** The list1. */
	private JList list1;
	
	/** The panel2. */
	private JPanel panel2;
	
	/** The b incl. */
	private JButton bIncl;
	
	/** The b excl. */
	private JButton bExcl;
	
	/** The scroll pane3. */
	private JScrollPane scrollPane3;
	
	/** The list2. */
	private JList list2;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

}
