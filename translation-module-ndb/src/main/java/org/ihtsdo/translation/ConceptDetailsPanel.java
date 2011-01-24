/*
 * Created by JFormDesigner on Mon Mar 01 12:02:51 GMT-03:00 2010
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

/**
 * @author Guillermo Reynoso
 */
public class ConceptDetailsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private I_GetConceptData concept;
	private int definingChar=-1;
	public ConceptDetailsPanel(I_GetConceptData concept) {
		initComponents();	
		this.concept=concept;
		tree1.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
		tree1.setCellRenderer(new IconRenderer());
		tree1.setRootVisible(true);
		tree1.setShowsRootHandles(false);
		DefaultTreeModel dtm;
		try {
			definingChar=ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.localize().getNid();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TerminologyException e1) {
			// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tree1.revalidate();
			tree1.repaint();
		}
	}
	class IconRenderer extends DefaultTreeCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		Icon redIcon;
		Icon attributeIcon;
		Icon orangeIcon;
		Icon descriptionIcon;
		Icon fsnIcon;
		Icon blackIcon;
		Icon preferredIcon;
		Icon roleGroupIcon;
		Icon associationIcon;

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

	@SuppressWarnings("unchecked")
	private DefaultTreeModel getConceptTreeModel(I_GetConceptData concept) throws Exception {
		I_TermFactory tf = Terms.get();
		//TODO add config as parameter
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

				String statusName = tf.getConcept(attributes.getStatusId()).toString();
				DefaultMutableTreeNode statusNode = new DefaultMutableTreeNode(
						new TreeEditorObjectWrapper(statusName, TreeEditorObjectWrapper.ATTRIBUTE, attributes.getMutablePart()));
				top.add(statusNode);

				List<I_RelTuple> relationships = (List<I_RelTuple>) concept.getDestRelTuples(config.getAllowedStatus(), null,
						config.getViewPositionSetReadOnly(),
						config.getPrecedence(), config.getConflictResolutionStrategy());
//				List<I_RelVersioned> relationships2 = (List<I_RelVersioned>) concept.getDestRels();

				List<DefaultMutableTreeNode> nodesToAdd = new ArrayList<DefaultMutableTreeNode>();

				HashMap<Integer,List<DefaultMutableTreeNode>> mapGroup= new HashMap<Integer,List<DefaultMutableTreeNode>>() ;
				List<DefaultMutableTreeNode> roleList=new ArrayList<DefaultMutableTreeNode>();
				int group=0;
				for (I_RelTuple relationship : relationships) {
					I_GetConceptData targetConcept = tf.getConcept(relationship.getC2Id());
					I_GetConceptData typeConcept = tf.getConcept(relationship.getTypeId());
					String label = typeConcept + ": " + targetConcept;

					if (relationship.getTypeId() == ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid()) {
						DefaultMutableTreeNode supertypeNode = new DefaultMutableTreeNode(
								new TreeEditorObjectWrapper(label, TreeEditorObjectWrapper.SUPERTYPE, relationship.getMutablePart()));
						nodesToAdd.add(supertypeNode);
					} else {
						if (relationship.getGroup()==0){
							if (relationship.getCharacteristicId()==definingChar){
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
     * @param query the query
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (dtm!=null)
				tree1.setVisibleRowCount(dtm.getChildCount(dtm.getRoot())+1);
			tree1.revalidate();
			tree1.repaint();
		}
    }

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
	private JScrollPane scrollPane1;
	private JTree tree1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
