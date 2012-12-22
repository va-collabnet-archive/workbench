/*
 * Created by JFormDesigner on Tue Mar 06 16:20:05 GMT-03:00 2012
 */

package org.ihtsdo.translation.ui.translation;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.spec.ValidationException;
import org.ihtsdo.translation.TreeEditorObjectWrapper;
import org.ihtsdo.translation.ui.DetailsIconRenderer;

/**
 * @author Guillermo Reynoso
 */
public class ConceptDetailsPanel extends JPanel {
	/** The inactive. */
	private I_GetConceptData inactive;
	/** The Snomed_ isa. */
	private I_GetConceptData snomedIsa;
	private int definingChar;
	/** The inferred. */
	private int inferred;

	public ConceptDetailsPanel() {
		initComponents();
		try {
			inactive = Terms.get().getConcept(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
			snomedIsa = Terms.get().getConcept(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"));
			definingChar = SnomedMetadataRf2.DEFINING_RELATIONSHIP_RF2.getLenient().getNid();
			inferred = SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid();
			
			tree3.setCellRenderer(new DetailsIconRenderer());
			tree3.setRootVisible(true);
			tree3.setShowsRootHandles(false);
			
		} catch (ValidationException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void updateDetailsTree(I_GetConceptData concept) {
		I_TermFactory tf = Terms.get();
		DefaultMutableTreeNode top = null;
		if (concept != null) {
			try {
				I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
				I_ConceptAttributeTuple attributes = null;
				attributes = concept.getConceptAttributeTuples(config.getPrecedence(), config.getConflictResolutionStrategy()).iterator().next();

				if (attributes.getStatusNid() == inactive.getConceptNid()) {
					top = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toString(), IconUtilities.INACTIVE, concept));
				} else if (attributes.isDefined()) {
					top = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toString(), IconUtilities.DEFINED, concept));
				} else {
					top = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toString(), IconUtilities.PRIMITIVE, concept));
				}

				Long concpetID = null;
				I_Identify identifier = concept.getIdentifier();
				List<? extends I_IdPart> parts = identifier.getMutableIdParts();
				Long lastTime = Long.MIN_VALUE;
				for (I_IdPart i_IdPart : parts) {
					if (i_IdPart.getAuthorityNid() == tf.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids()) && i_IdPart.getTime() > lastTime) {
						lastTime = i_IdPart.getTime();
						concpetID = (Long) i_IdPart.getDenotation();
					}
				}
				DefaultMutableTreeNode conceptIdNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper("Concept ID: " + concpetID, IconUtilities.ID, concpetID));
				top.add(conceptIdNode);

				List<I_RelTuple> relationships = (List<I_RelTuple>) concept.getSourceRelTuples(config.getAllowedStatus(), config.getDestRelTypes(), config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy(),
						config.getClassifierConcept().getNid(), RelAssertionType.INFERRED);

				List<DefaultMutableTreeNode> nodesToAdd = new ArrayList<DefaultMutableTreeNode>();

				HashMap<Integer, List<DefaultMutableTreeNode>> mapGroup = new HashMap<Integer, List<DefaultMutableTreeNode>>();
				List<DefaultMutableTreeNode> roleList = new ArrayList<DefaultMutableTreeNode>();
				int group = 0;
				for (I_RelTuple relationship : relationships) {
					I_GetConceptData targetConcept = tf.getConcept(relationship.getC2Id());
					I_GetConceptData typeConcept = tf.getConcept(relationship.getTypeNid());
					String label = typeConcept + ": " + targetConcept;

					if ((relationship.getTypeNid() == snomedIsa.getConceptNid()) || (relationship.getTypeNid() == ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid())) {
						attributes = targetConcept.getConceptAttributeTuples(config.getPrecedence(), config.getConflictResolutionStrategy()).iterator().next();
						DefaultMutableTreeNode supertypeNode = null;
						if (attributes.getStatusNid() == inactive.getConceptNid()) {
							supertypeNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.INACTIVE_PARENT, relationship.getMutablePart()));

						} else if (attributes.isDefined()) {
							supertypeNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.DEFINED_PARENT, relationship.getMutablePart()));
						} else {
							supertypeNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.PRIMITIVE_PARENT, relationship.getMutablePart()));

						}
						nodesToAdd.add(supertypeNode);
					} else {
						if (relationship.getGroup() == 0) {
							if (relationship.getCharacteristicId() == definingChar || relationship.getCharacteristicId() == inferred) {
								DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.ROLE, relationship.getMutablePart()));
								nodesToAdd.add(roleNode);
							} else {
								DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.ASSOCIATION, relationship.getMutablePart()));
								nodesToAdd.add(roleNode);
							}
						} else {
							group = relationship.getGroup();
							if (mapGroup.containsKey(group)) {
								roleList = mapGroup.get(group);
							} else {
								roleList = new ArrayList<DefaultMutableTreeNode>();
							}

							roleList.add(new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.ROLE, relationship.getMutablePart())));
							mapGroup.put(group, roleList);
						}
					}
				}

				for (DefaultMutableTreeNode loopNode : nodesToAdd) {
					TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
					if (nodeObject.getType() == IconUtilities.DEFINED_PARENT || nodeObject.getType() == IconUtilities.PRIMITIVE_PARENT || nodeObject.getType() == IconUtilities.INACTIVE_PARENT) {
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
					DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper("Group:" + key, IconUtilities.ROLEGROUP, lRoles));
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
