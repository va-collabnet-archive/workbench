/*
 * Created by JFormDesigner on Fri Aug 05 16:22:27 GMT-03:00 2011
 */

package org.ihtsdo.qa.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.qa.inheritance.InheritedRelationships;
import org.ihtsdo.qa.inheritance.RelationshipsDAO;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

/**
 * @author Guillermo Reynoso
 */
public class RelationshipTreeViewerPanel extends JPanel {

	private static final long serialVersionUID = 2902078592415391231L;
	private I_GetConceptData concept;
	private I_TermFactory tf;
	private DefaultTreeModel model;
	private String factContextName;
	private DefaultListModel conceptModel;
	private DefaultMutableTreeNode top;

	public RelationshipTreeViewerPanel(ConceptVersionBI conceptBi, String factContextName) {
		initComponents();

		tf = Terms.get();
		try {
			this.concept = tf.getConcept(conceptBi.getNid());
			this.factContextName = factContextName;
			initCustomComponents();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public RelationshipTreeViewerPanel(String factContextName) {
		initComponents();

		tf = Terms.get();
		this.concept = null;
		this.factContextName = factContextName;

		try {
			initCustomComponents();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
	}

	public RelationshipTreeViewerPanel(I_GetConceptData oldStyleConcept, String factContextName) {
		initComponents();

		tf = Terms.get();
		this.concept = oldStyleConcept;
		this.factContextName = factContextName;

		try {
			initCustomComponents();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
	}

	private void initCustomComponents() throws IOException, TerminologyException {
		DefaultTreeCellRenderer rend = new DefaultTreeCellRenderer();
		rend.setIconTextGap(3);

		relTree.setCellRenderer(rend);
		relTree.setRowHeight(20);
		top = new DefaultMutableTreeNode("root");
		final DefaultMutableTreeNode conceptNode;
		if (concept != null) {
			conceptNode = new DefaultMutableTreeNode(concept.getInitialText());
		} else {
			conceptNode = new DefaultMutableTreeNode("");
		}
		model = new DefaultTreeModel(top);
		addNodeInSortedOrder(top, conceptNode);
		relTree.setModel(model);

		conceptModel = new DefaultListModel();
		conceptModel.addListDataListener(new ListDataListener() {

			@Override
			public void intervalRemoved(ListDataEvent arg0) {
			}

			@Override
			public void intervalAdded(ListDataEvent arg0) {
				try {
					concept = (I_GetConceptData) conceptModel.get(0);
					createTree(conceptNode);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (TerminologyException e) {
					e.printStackTrace();
				}
				conceptList.setToolTipText(conceptModel.get(0).toString());
			}

			@Override
			public void contentsChanged(ListDataEvent arg0) {
			}
		});
		conceptList.setModel(conceptModel);
		if (concept != null) {
			conceptModel.addElement(concept);
		}
		conceptList.setName(ObjectTransferHandler.TARGET_LIST_NAME);
		conceptList.setMinimumSize(new Dimension(600, 25));
		conceptList.setPreferredSize(new Dimension(600, 25));
		conceptList.setMaximumSize(new Dimension(600, 25));
		conceptList.setBorder(new BevelBorder(BevelBorder.LOWERED));

		// Configure drag and drop
		conceptList.setTransferHandler(new ObjectTransferHandler(tf.getActiveAceFrameConfig(), null));
		if (concept != null) {
			createTree(conceptNode);
		}
	}

	private void createTree(DefaultMutableTreeNode top) throws IOException, TerminologyException {
		relTree.removeAll();
		DefaultMutableTreeNode conceptNode = new DefaultMutableTreeNode(concept.getInitialText());
		addNodeInSortedOrder(top, conceptNode);
		
		RelationshipsDAO rDao = new RelationshipsDAO();
		InheritedRelationships inhRel = rDao.getInheritedRelationships(concept);

		DefaultMutableTreeNode constNormalFormNode = new DefaultMutableTreeNode("Constraint Normal Form");

		addNodeInSortedOrder(conceptNode, constNormalFormNode);
		// Inherited single roles
		for (I_RelTuple relTuple : inhRel.getSingleRoles()) {
			createGenericRelationProperties(constNormalFormNode, relTuple);
		}

		// Inherited grouped roles
		DefaultMutableTreeNode groupRoot = new DefaultMutableTreeNode("Role groups");
		addNodeInSortedOrder(constNormalFormNode, groupRoot);
		int groupNr = 0;
		for (I_RelTuple[] relTuples : inhRel.getRoleGroups()) {
			groupNr++;
			DefaultMutableTreeNode group = new DefaultMutableTreeNode("Group " + groupNr);
			addNodeInSortedOrder(groupRoot, group);
			for (I_RelTuple relTuple : relTuples) {
				createGenericRelationProperties(group, relTuple);
			}
		}
		// Is A's Stated
		List<I_RelTuple> relTuples = (List<I_RelTuple>) rDao.getStatedIsARels(concept);

		for (I_RelTuple relTuple : relTuples) {
			createGenericRelationProperties(constNormalFormNode, relTuple);
		}
		I_IntSet cptModelRels = null;
		// Not defining rels
		if (cptModelRels == null) {
			cptModelRels = RulesLibrary.getConceptModelRels();
		}
		I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
		for (RelationshipVersionBI relTuple : concept.getSourceRelTuples(config.getAllowedStatus(), cptModelRels, config.getViewPositionSetReadOnly(), config.getPrecedence(),
				config.getConflictResolutionStrategy())) {
			if (!rDao.isDefiningChar(relTuple.getCharacteristicNid())) {
				createGenericRelationProperties(constNormalFormNode, relTuple);

			}
		}

		model.reload();

	}

	private void createGenericRelationProperties(DefaultMutableTreeNode parentNode, RelationshipVersionBI relTuple) throws IOException, TerminologyException {
		DefaultMutableTreeNode relNode = createRelationNode(relTuple);
		addNodeInSortedOrder(parentNode, relNode);

		UUID primordalUuid = relTuple.getPrimUuid();
		DefaultMutableTreeNode primordialUuidNode = new DefaultMutableTreeNode("Primordial UUID - (" + primordalUuid.toString() + ")");

		DefaultMutableTreeNode typeNode = createRelAttributeNode(relTuple.getTypeNid());
		addNodeInSortedOrder(parentNode, typeNode);

		DefaultMutableTreeNode targetNode = createRelAttributeNode(relTuple.getDestinationNid());
		addNodeInSortedOrder(parentNode, targetNode);

		DefaultMutableTreeNode autorNode = createRelAttributeNode(relTuple.getAuthorNid());
		addNodeInSortedOrder(parentNode, autorNode);

		DefaultMutableTreeNode originalNode = createRelAttributeNode(relTuple.getOriginNid());
		addNodeInSortedOrder(parentNode, originalNode);

		DefaultMutableTreeNode characteristicNode = createRelAttributeNode(relTuple.getCharacteristicNid());
		addNodeInSortedOrder(parentNode, characteristicNode);

		DefaultMutableTreeNode pathNode = createRelAttributeNode(relTuple.getPathNid());
		addNodeInSortedOrder(parentNode, pathNode);

		DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode("Group - " + relTuple.getGroup());
		addNodeInSortedOrder(parentNode, groupNode);

		DefaultMutableTreeNode statusNode = createRelAttributeNode(relTuple.getStatusNid());
		addNodeInSortedOrder(parentNode, statusNode);

		DefaultMutableTreeNode timeNode = new DefaultMutableTreeNode("Time: " + relTuple.getTime());
		addNodeInSortedOrder(parentNode, timeNode);

		DefaultMutableTreeNode factContextNode = new DefaultMutableTreeNode("Fact context name: " + factContextName);
		addNodeInSortedOrder(parentNode, factContextNode);

	}

	private void createGenericRelationProperties(DefaultMutableTreeNode parentNode, I_RelTuple relTuple) throws IOException, TerminologyException {
		DefaultMutableTreeNode relNode = createRelationNode(relTuple);
		addNodeInSortedOrder(parentNode, relNode);

		UUID primordalUuid = relTuple.getPrimUuid();
		DefaultMutableTreeNode primordialUuidNode = new DefaultMutableTreeNode("Primordial UUID - (" + primordalUuid.toString() + ")");

		DefaultMutableTreeNode typeNode = createRelAttributeNode(relTuple.getTypeNid());
		addNodeInSortedOrder(primordialUuidNode, typeNode);

		DefaultMutableTreeNode targetNode = createRelAttributeNode(relTuple.getDestinationNid());
		addNodeInSortedOrder(primordialUuidNode, targetNode);

		DefaultMutableTreeNode autorNode = createRelAttributeNode(relTuple.getAuthorNid());
		addNodeInSortedOrder(primordialUuidNode, autorNode);

		DefaultMutableTreeNode originalNode = createRelAttributeNode(relTuple.getOriginNid());
		addNodeInSortedOrder(primordialUuidNode, originalNode);

		DefaultMutableTreeNode characteristicNode = createRelAttributeNode(relTuple.getCharacteristicNid());
		addNodeInSortedOrder(primordialUuidNode, characteristicNode);

		DefaultMutableTreeNode pathNode = createRelAttributeNode(relTuple.getPathNid());
		addNodeInSortedOrder(primordialUuidNode, pathNode);

		DefaultMutableTreeNode statusNode = createRelAttributeNode(relTuple.getStatusNid());
		addNodeInSortedOrder(primordialUuidNode, statusNode);

		DefaultMutableTreeNode timeNode = new DefaultMutableTreeNode("Time: " + relTuple.getTime());
		addNodeInSortedOrder(primordialUuidNode, timeNode);

		DefaultMutableTreeNode factContextNode = new DefaultMutableTreeNode("Fact context name: " + factContextName);
		addNodeInSortedOrder(primordialUuidNode, factContextNode);
	}

	private DefaultMutableTreeNode createRelationNode(RelationshipVersionBI relTuple) throws IOException, TerminologyException {
		String typeUuid = tf.nidToUuid(relTuple.getTypeNid()).toString();
		I_GetConceptData type = tf.getConcept(UUID.fromString(typeUuid));

		String targetUuid = tf.nidToUuid(relTuple.getDestinationNid()).toString();
		I_GetConceptData target = tf.getConcept(UUID.fromString(targetUuid));

		DefaultMutableTreeNode relNode = new DefaultMutableTreeNode(type.getInitialText() + " - " + target.getInitialText());
		return relNode;
	}

	private DefaultMutableTreeNode createRelationNode(I_RelTuple relTuple) throws IOException, TerminologyException {
		String typeUuid = tf.nidToUuid(relTuple.getTypeNid()).toString();
		I_GetConceptData type = tf.getConcept(UUID.fromString(typeUuid));

		String targetUuid = tf.nidToUuid(relTuple.getDestinationNid()).toString();
		I_GetConceptData target = tf.getConcept(UUID.fromString(targetUuid));

		DefaultMutableTreeNode relNode = new DefaultMutableTreeNode(type.getInitialText() + " - " + target.getInitialText());
		return relNode;
	}

	private DefaultMutableTreeNode createRelAttributeNode(int conceptNid) throws IOException, TerminologyException {
		String uuid = tf.nidToUuid(conceptNid).toString();
		I_GetConceptData concept = tf.getConcept(UUID.fromString(uuid));

		DefaultMutableTreeNode node = new DefaultMutableTreeNode(concept.getInitialText() + " - (" + uuid + ")");
		return node;
	}

	private void addNodeInSortedOrder(DefaultMutableTreeNode parent, DefaultMutableTreeNode child) {
		int n = parent.getChildCount();
		if (n == 0) {
			model.insertNodeInto(child, parent, 0);
			parent.add(child);
			return;
		}
		// DefaultMutableTreeNode node = null;
		// for (int i = 0; i < n; i++) {
		// node = (DefaultMutableTreeNode) parent.getChildAt(i);
		// if (node.toString().compareTo(child.toString()) > 0) {
		// model.insertNodeInto(child, parent, i);
		// return;
		// }
		// }
		model.insertNodeInto(child, parent, parent.getChildCount());
		return;
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		conceptTextBox = new JPanel();
		label1 = new JLabel();
		conceptList = new JList();
		label2 = new JLabel();
		scrollPane1 = new JScrollPane();
		relTree = new JTree();

		// ======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new GridBagLayout());
		((GridBagLayout) getLayout()).columnWidths = new int[] { 0, 0 };
		((GridBagLayout) getLayout()).rowHeights = new int[] { 0, 0, 0 };
		((GridBagLayout) getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
		((GridBagLayout) getLayout()).rowWeights = new double[] { 0.0, 1.0, 1.0E-4 };

		// ======== conceptTextBox ========
		{
			conceptTextBox.setLayout(new GridBagLayout());
			((GridBagLayout) conceptTextBox.getLayout()).columnWidths = new int[] { 0, 344, 0, 0 };
			((GridBagLayout) conceptTextBox.getLayout()).rowHeights = new int[] { 0, 0 };
			((GridBagLayout) conceptTextBox.getLayout()).columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0E-4 };
			((GridBagLayout) conceptTextBox.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

			// ---- label1 ----
			label1.setText("Concept");
			conceptTextBox.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- conceptList ----
			conceptList.setVisibleRowCount(1);
			conceptTextBox.add(conceptList, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- label2 ----
			label2.setText("<html><font size=\"-1\">(Drag and drop concept to see the relationships)");
			conceptTextBox.add(label2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(conceptTextBox, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

		// ======== scrollPane1 ========
		{

			// ---- relTree ----
			relTree.setRootVisible(false);
			scrollPane1.setViewportView(relTree);
		}
		add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel conceptTextBox;
	private JLabel label1;
	private JList conceptList;
	private JLabel label2;
	private JScrollPane scrollPane1;
	private JTree relTree;
	// JFormDesigner - End of variables declaration //GEN-END:variables

}
