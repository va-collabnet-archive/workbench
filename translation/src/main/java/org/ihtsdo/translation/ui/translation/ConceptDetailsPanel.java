/*
 * Created by JFormDesigner on Tue Mar 06 16:20:05 GMT-03:00 2012
 */

package org.ihtsdo.translation.ui.translation;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.spec.ValidationException;
import org.ihtsdo.translation.TreeEditorObjectWrapper;
import org.ihtsdo.translation.ui.DetailsIconRenderer;

/**
 * @author Guillermo Reynoso
 */
public class ConceptDetailsPanel extends JPanel {
	/** The inactive. */
	/** The Snomed_ isa. */
	private int definingChar;
	/** The inferred. */
	private int inferred;

	public ConceptDetailsPanel() {
		initComponents();
		try {
			definingChar = SnomedMetadataRf2.DEFINING_RELATIONSHIP_RF2.getLenient().getNid();
			inferred = SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid();
			tree3.setCellRenderer(new DetailsIconRenderer());
			tree3.setRootVisible(true);
			tree3.setShowsRootHandles(false);

		} catch (ValidationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void updateDetailsTree(ConceptVersionBI concept) {
		I_TermFactory tf = Terms.get();
		DefaultMutableTreeNode top = null;
		if (concept != null) {
			try {
				I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
				ConceptAttributeVersionBI attributes = concept.getConceptAttributesActive();

				if (attributes.isActive(config.getViewCoordinate())) {
					top = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toUserString(), IconUtilities.INACTIVE, concept.getDescriptionPreferred()));
				} else if (attributes.isDefined()) {
					top = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toUserString(), IconUtilities.DEFINED, concept.getDescriptionPreferred()));
				} else {
					top = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toUserString(), IconUtilities.PRIMITIVE, concept.getDescriptionPreferred()));
				}

				Long concpetID = null;
				Collection<? extends IdBI> identifier = concept.getAdditionalIds();
				for (IdBI i_IdPart : identifier) {
					if (i_IdPart.getAuthorityNid() == tf.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids())) {
						concpetID = (Long) i_IdPart.getDenotation();
					}
				}
				DefaultMutableTreeNode conceptIdNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper("Concept ID: " + concpetID,
						IconUtilities.ID, concpetID));
				top.add(conceptIdNode);
				
				Collection<? extends RelationshipVersionBI> relationships = concept.getRelationshipsOutgoingActive();

				List<DefaultMutableTreeNode> nodesToAdd = new ArrayList<DefaultMutableTreeNode>();

				HashMap<Integer, List<DefaultMutableTreeNode>> mapGroup = new HashMap<Integer, List<DefaultMutableTreeNode>>();
				List<DefaultMutableTreeNode> roleList = new ArrayList<DefaultMutableTreeNode>();
				int group = 0;
				for (RelationshipVersionBI relationship : relationships) {
					ConceptVersionBI targetConcept = Ts.get().getConceptVersion(config.getViewCoordinate(), relationship.getTargetNid());
					ConceptVersionBI typeConcept = Ts.get().getConceptVersion(config.getViewCoordinate(), relationship.getTypeNid());
					String label = typeConcept.toUserString() + ": " + targetConcept.getDescriptionPreferred().toUserString();

					if (relationship.getPrimUuid().equals(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"))) {
						DefaultMutableTreeNode supertypeNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, TreeEditorObjectWrapper.SUPERTYPE, relationship));
						nodesToAdd.add(supertypeNode);
					} else {
						if (relationship.getGroup() == 0) {
							if (relationship.getCharacteristicNid() == SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid()) {
								DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, TreeEditorObjectWrapper.ROLE, relationship));
								nodesToAdd.add(roleNode);
							} else {
								DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, TreeEditorObjectWrapper.ASSOCIATION, relationship));
								nodesToAdd.add(roleNode);
							}
						} else {
							group = relationship.getGroup();
							if (mapGroup.containsKey(group)) {
								roleList = mapGroup.get(group);
							} else {
								roleList = new ArrayList<DefaultMutableTreeNode>();
							}

							roleList.add(new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, TreeEditorObjectWrapper.ROLE, relationship)));
							mapGroup.put(group, roleList);
						}
					}
				}
				
				for (DefaultMutableTreeNode loopNode : nodesToAdd) {
					TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
					if (nodeObject.getType() == IconUtilities.DEFINED_PARENT || nodeObject.getType() == IconUtilities.PRIMITIVE_PARENT
							|| nodeObject.getType() == IconUtilities.INACTIVE_PARENT) {
						top.add(loopNode);
					}
				}

				for (DefaultMutableTreeNode loopNode : nodesToAdd) {
					TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
					if (nodeObject.getType() == IconUtilities.ROLE) {
						top.add(loopNode);
					}
				}
				for (int key : mapGroup.keySet()) {
					List<DefaultMutableTreeNode> lRoles = (List<DefaultMutableTreeNode>) mapGroup.get(key);
					DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper("Group:" + key,
							IconUtilities.ROLEGROUP, lRoles));
					for (DefaultMutableTreeNode rNode : lRoles) {
						groupNode.add(rNode);
					}
					top.add(groupNode);
				}
				for (DefaultMutableTreeNode loopNode : nodesToAdd) {
					TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
					if (nodeObject.getType() == IconUtilities.ASSOCIATION) {
						top.add(loopNode);
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (ContradictionException e) {
				e.printStackTrace();
			}
			DefaultTreeModel treeModel = new DefaultTreeModel(top);

			tree3.setModel(treeModel);

			for (int i = 0; i < tree3.getRowCount(); i++) {
				tree3.expandRow(i);
			}
			tree3.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree3.revalidate();
		}
	}
	
	public void updateDetailsTree(WorkListMember worklistMember) {
		ConceptVersionBI concept = null;
		try {
			concept = Ts.get().getConceptVersion(Terms.get().getActiveAceFrameConfig().getViewCoordinate(), worklistMember.getConcept().getUids());
			updateDetailsTree(concept);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		tree3 = new JTree();

		// ======== this ========
		setLayout(new BorderLayout());

		// ---- tree3 ----
		tree3.setVisibleRowCount(4);
		tree3.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		add(tree3, BorderLayout.CENTER);
		// JFormDesigner - End of component initialization
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JTree tree3;
	// JFormDesigner - End of variables declaration //GEN-END:variables

}
