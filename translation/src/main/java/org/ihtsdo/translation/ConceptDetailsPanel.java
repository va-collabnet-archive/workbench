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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * The Class ConceptDetailsPanel.
 *
 * @author Guillermo Reynoso
 */
public class ConceptDetailsPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The concept. */
	private I_GetConceptData concept;
	
	/** The inferred char. */
	private int inferredChar=-1;
	
	/** The Snomed_ isa. */
	private I_GetConceptData Snomed_Isa;
	
	/**
	 * Instantiates a new concept details panel.
	 *
	 * @param concept the concept
	 */
	public ConceptDetailsPanel(I_GetConceptData concept) {
		initComponents();	
		this.concept=concept;
		tree1.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
		tree1.setCellRenderer(new IconRenderer());
		tree1.setRootVisible(true);
		tree1.setShowsRootHandles(false);
		try {
			Snomed_Isa= Terms.get().getConcept(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"));
		} catch (TerminologyException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		DefaultTreeModel dtm;
		try {
			inferredChar=SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (this.concept!=null ){
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
		
		/** The association icon. */
		Icon associationIcon;

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
			associationIcon=new ImageIcon("icons/Association.gif");
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
				case 12: setIcon(associationIcon); break;
				default: setIcon(blackIcon); break;
				}
			}else{
				setIcon(blackIcon); 
			}
			return this;
		}

	}

	/**
	 * Gets the concept tree model.
	 *
	 * @param concept the concept
	 * @return the concept tree model
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unchecked")
	private DefaultTreeModel getConceptTreeModel(I_GetConceptData concept) throws Exception {
		I_TermFactory tf = Terms.get();
		I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
		DefaultMutableTreeNode top = null;
		if (concept != null) {
			try {
				top = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toString(), TreeEditorObjectWrapper.CONCEPT, concept));
				I_ConceptAttributeTuple attributes = null;
				attributes = concept.getConceptAttributeTuples(config.getPrecedence(), 
						config.getConflictResolutionStrategy()).iterator().next();
				String definedOrPrimitive = "";
				if (attributes.isDefined()) {
					definedOrPrimitive = "fully defined";
				} else {
					definedOrPrimitive = "primitive";
				}
				DefaultMutableTreeNode idNode = new DefaultMutableTreeNode(
						new TreeEditorObjectWrapper(definedOrPrimitive, TreeEditorObjectWrapper.ATTRIBUTE, attributes.getMutablePart()));
				top.add(idNode);

				String statusName = tf.getConcept(attributes.getStatusNid()).toString();
				DefaultMutableTreeNode statusNode = new DefaultMutableTreeNode(
						new TreeEditorObjectWrapper(statusName, TreeEditorObjectWrapper.ATTRIBUTE, attributes.getMutablePart()));
				top.add(statusNode);

				List<I_RelTuple> relationships = (List<I_RelTuple>) concept.getSourceRelTuples(config.getAllowedStatus(),null,
						config.getViewPositionSetReadOnly(),
						config.getPrecedence(), config.getConflictResolutionStrategy(),config.getClassifierConcept().getNid(),RelAssertionType.INFERRED);

				List<DefaultMutableTreeNode> nodesToAdd = new ArrayList<DefaultMutableTreeNode>();

				HashMap<Integer,List<DefaultMutableTreeNode>> mapGroup= new HashMap<Integer,List<DefaultMutableTreeNode>>() ;
				List<DefaultMutableTreeNode> roleList=new ArrayList<DefaultMutableTreeNode>();
				int group=0;
				for (I_RelTuple relationship : relationships) {
					I_GetConceptData targetConcept = tf.getConcept(relationship.getC2Id());
					I_GetConceptData typeConcept = tf.getConcept(relationship.getTypeNid());
					String label = typeConcept + ": " + targetConcept;

					if (relationship.getTypeNid()==Snomed_Isa.getConceptNid()
							|| relationship.getTypeNid() == ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid()) {
						DefaultMutableTreeNode supertypeNode = new DefaultMutableTreeNode(
								new TreeEditorObjectWrapper(label, TreeEditorObjectWrapper.SUPERTYPE, relationship.getMutablePart()));
						nodesToAdd.add(supertypeNode);
					} else {
						if (relationship.getGroup()==0){
							if (relationship.getCharacteristicId()==inferredChar){
								DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(
										new TreeEditorObjectWrapper(label, TreeEditorObjectWrapper.ROLE, relationship.getMutablePart()));
								nodesToAdd.add(roleNode);
							}
							else{
								DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(
										new TreeEditorObjectWrapper(label, TreeEditorObjectWrapper.ASSOCIATION, relationship.getMutablePart()));
								nodesToAdd.add(roleNode);
							}
						}
						else
						{
							group = relationship.getGroup();
							if (mapGroup.containsKey(group)){
								roleList=mapGroup.get(group);
							}
							else{
								roleList=new ArrayList<DefaultMutableTreeNode>();
							}

							roleList.add(new DefaultMutableTreeNode(
									new TreeEditorObjectWrapper(label, TreeEditorObjectWrapper.ROLE, relationship.getMutablePart())));
							mapGroup.put(group, roleList);
						}
					}
				}

			

				for (DefaultMutableTreeNode loopNode : nodesToAdd) {
					TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
					if (nodeObject.getType() == TreeEditorObjectWrapper.SUPERTYPE) {
						top.add(loopNode);
					}
				}

				for (DefaultMutableTreeNode loopNode : nodesToAdd) {
					TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
					if (nodeObject.getType() == TreeEditorObjectWrapper.ROLE) {
						top.add(loopNode);
					}
				}
				for (int key:mapGroup.keySet()){
					List<DefaultMutableTreeNode> lRoles=(List<DefaultMutableTreeNode>)mapGroup.get(key);
					DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(
							new TreeEditorObjectWrapper("Group:" + key, TreeEditorObjectWrapper.ROLEGROUP,lRoles ));
					for (DefaultMutableTreeNode rNode: lRoles){
						groupNode.add(rNode);
					}
					top.add(groupNode);
				}
				for (DefaultMutableTreeNode loopNode : nodesToAdd) {
					TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
					if (nodeObject.getType() == TreeEditorObjectWrapper.ASSOCIATION) {
						top.add(loopNode);
					}
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
     * Update.
     *
     * @param concept the concept
     */
	synchronized
    public void update(I_GetConceptData concept){
		this.concept=concept;
		DefaultTreeModel dtm=null;
		if (concept!=null ){
			try {
				dtm=getConceptTreeModel(concept);
				tree1.setModel(dtm);

				for (int i = 0; i < tree1.getRowCount(); i++) {
					tree1.expandRow(i);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (dtm!=null)
				tree1.setVisibleRowCount(dtm.getChildCount(dtm.getRoot())+1);
			tree1.revalidate();
			tree1.repaint();
		}
    }

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		scrollPane1 = new JScrollPane();
		tree1 = new JTree();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {110, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

		//======== scrollPane1 ========
		{

			//---- tree1 ----
			tree1.setVisibleRowCount(5);
			scrollPane1.setViewportView(tree1);
		}
		add(scrollPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	/** The scroll pane1. */
	private JScrollPane scrollPane1;
	
	/** The tree1. */
	private JTree tree1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
