/*
 * Created by JFormDesigner on Fri Aug 05 16:22:27 GMT-03:00 2011
 */

package org.ihtsdo.qa.gui.viewers.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import javax.swing.tree.DefaultTreeModel;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.qa.gui.ObjectTransferHandler;
import org.ihtsdo.qa.gui.viewers.TreeEditorObjectWrapper;
import org.ihtsdo.qa.gui.viewers.utils.IconUtilities;
import org.ihtsdo.qa.inheritance.RelationshipsDAO;
import org.ihtsdo.testmodel.DrRelationship;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * @author Guillermo Reynoso
 */
public class ConceptDetailsPanel extends JPanel {

	private static final long serialVersionUID = 2902078592415391231L;
	private I_GetConceptData concept;
	private I_TermFactory tf;
	private DefaultTreeModel model;
	private DefaultListModel conceptModel;
	private DefaultMutableTreeNode top;
	private DefaultMutableTreeNode conceptNode;
	private int definingChar = -1;
	private String factContextName = "";
	private I_GetConceptData inactive;
	private I_GetConceptData retired;

	public ConceptDetailsPanel(ConceptVersionBI conceptBi) {
		initComponents();

		tf = Terms.get();
		try {
			this.concept = tf.getConcept(conceptBi.getNid());
			initCustomComponents();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ConceptDetailsPanel() {
		initComponents();

		tf = Terms.get();
		this.concept = null;

		try {
			initCustomComponents();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
	}

	public ConceptDetailsPanel(I_GetConceptData oldStyleConcept) {
		initComponents();

		tf = Terms.get();
		this.concept = oldStyleConcept;

		try {
			initCustomComponents();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
	}

	private void initCustomComponents() throws IOException, TerminologyException {
		definingChar = ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.localize().getNid();

		inactive = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.INACTIVE.getUids());
		retired = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.RETIRED.getUids());

		conceptDetailsTree.setCellRenderer(new DetailsIconRenderer());
		conceptDetailsTree.setRootVisible(false);
		conceptDetailsTree.setShowsRootHandles(false);

		conceptDetailsTree.setRowHeight(20);
		top = new DefaultMutableTreeNode("root");

		model = new DefaultTreeModel(top);

		conceptDetailsTree.setModel(model);

		conceptModel = new DefaultListModel();
		conceptList.setModel(conceptModel);
		if (concept != null) {
			conceptModel.addElement(concept);
		}
		conceptModel.addListDataListener(new ListDataListener() {

			@Override
			public void intervalRemoved(ListDataEvent arg0) {
			}

			@Override
			public void intervalAdded(ListDataEvent arg0) {
				concept = (I_GetConceptData) conceptModel.get(0);
				conceptList.setToolTipText(conceptModel.get(0).toString());
				try {
					populateDetailsTree();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void contentsChanged(ListDataEvent arg0) {
			}
		});
		conceptList.setName(ObjectTransferHandler.TARGET_LIST_NAME);
		conceptList.setMinimumSize(new Dimension(300, 25));
		conceptList.setMaximumSize(new Dimension(300, 25));
		conceptList.setPreferredSize(new Dimension(300, 25));
		conceptList.setBorder(new BevelBorder(BevelBorder.LOWERED));

		// Configure drag and drop
		conceptList.setTransferHandler(new ObjectTransferHandler(tf.getActiveAceFrameConfig(), null));
		try {
			populateDetailsTree();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void populateDetailsTree() throws Exception {
		top.removeAllChildren();
		if (concept != null) {
			try {
				I_TermFactory tf = Terms.get();
				I_ConfigAceFrame config = tf.getActiveAceFrameConfig();

				I_ConceptAttributeTuple attributes = null;
				attributes = concept.getConceptAttributeTuples(config.getPrecedence(), config.getConflictResolutionStrategy()).iterator().next();

				String statusName = tf.getConcept(attributes.getStatusId()).toString();
				if (statusName.equalsIgnoreCase("retired") || statusName.equalsIgnoreCase("inactive")) {
					conceptNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toString(), IconUtilities.INACTIVE, concept));
				} else if (attributes.isDefined()) {
					conceptNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toString(), IconUtilities.DEFINED, concept));
				} else {
					conceptNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toString(), IconUtilities.PRIMITIVE, concept));
				}

				model.insertNodeInto(conceptNode, top, top.getChildCount());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (TerminologyException e) {
				e.printStackTrace();
			}

			createTree(conceptNode);

			model.reload();
			conceptDetailsTree.revalidate();
			conceptDetailsTree.repaint();
		}
	}

	private void createTree(DefaultMutableTreeNode top) throws IOException, TerminologyException {
		RelationshipsDAO rDao = new RelationshipsDAO();
		List<DrRelationship> inhRel = rDao.getConstraintNormalForm(concept, "");

		DefaultMutableTreeNode constNormalFormNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper("Constraint Normal Form", IconUtilities.FSNDESCRIPTION, null));

		addNodeInSortedOrder(top, constNormalFormNode);
		// Inherited single roles
		Map<Integer, ArrayList<DrRelationship>> cnfRels = new HashMap<Integer, ArrayList<DrRelationship>>();
		for (DrRelationship relTuple : inhRel) {
			Integer key = relTuple.getRelGroup();
			if (key == 0) {
				createGenericRelationProperties(constNormalFormNode, relTuple);
			} else {
				if (cnfRels.containsKey(key)) {
					cnfRels.get(key).add(relTuple);
				} else {
					ArrayList<DrRelationship> group = new ArrayList<DrRelationship>();
					group.add(relTuple);
					cnfRels.put(key, group);
				}
			}
		}

		Set<Integer> cnfKeySet = cnfRels.keySet();
		for (Integer integer : cnfKeySet) {
			DefaultMutableTreeNode group = new DefaultMutableTreeNode(new TreeEditorObjectWrapper("Group " + integer, IconUtilities.BLUE_ICON, null));
			addNodeInSortedOrder(constNormalFormNode, group);
			for (DrRelationship relTuple : cnfRels.get(integer)) {
				createGenericRelationProperties(group, relTuple);
			}
		}
		
		// All Stated
		List<I_RelTuple> relTuples = (List<I_RelTuple>) rDao.getStatedAllRels(concept);
		DefaultMutableTreeNode statedRels = new DefaultMutableTreeNode(new TreeEditorObjectWrapper("Stated Relationships", IconUtilities.FSNDESCRIPTION, null));
		addNodeInSortedOrder(top, statedRels);

		Map<Integer, ArrayList<I_RelTuple>> statedRelGropus = new HashMap<Integer, ArrayList<I_RelTuple>>();
		for (I_RelTuple relTuple : relTuples) {
			Integer key = Integer.valueOf(relTuple.getGroup());
			if (key == 0) {
				createGenericRelationProperties(statedRels, relTuple);
			} else {
				if (statedRelGropus.containsKey(key)) {
					statedRelGropus.get(key).add(relTuple);
				} else {
					ArrayList<I_RelTuple> group = new ArrayList<I_RelTuple>();
					group.add(relTuple);
					statedRelGropus.put(key, group);
				}
			}
		}

		Set<Integer> statedKeySet = statedRelGropus.keySet();
		for (Integer integer : statedKeySet) {
			DefaultMutableTreeNode group = new DefaultMutableTreeNode(new TreeEditorObjectWrapper("Group " + integer, IconUtilities.BLUE_ICON, null));
			addNodeInSortedOrder(statedRels, group);
			for (I_RelTuple relTuple : statedRelGropus.get(integer)) {
				createGenericRelationProperties(group, relTuple);
			}
		}
		
		// Inferred
		List<I_RelTuple> inferredRelTuples = (List<I_RelTuple>) rDao.getInferredRels(concept);
		DefaultMutableTreeNode inferredRels = new DefaultMutableTreeNode(new TreeEditorObjectWrapper("Inferred Relationships", IconUtilities.FSNDESCRIPTION, null));
		addNodeInSortedOrder(top, inferredRels);

		Map<Integer, ArrayList<I_RelTuple>> inferrdRelGropus = new HashMap<Integer, ArrayList<I_RelTuple>>();
		for (I_RelTuple relTuple : inferredRelTuples) {
			Integer key = Integer.valueOf(relTuple.getGroup());
			if (key == 0) {
				createGenericRelationProperties(inferredRels, relTuple);
			} else {
				if (inferrdRelGropus.containsKey(key)) {
					inferrdRelGropus.get(key).add(relTuple);
				} else {
					ArrayList<I_RelTuple> group = new ArrayList<I_RelTuple>();
					group.add(relTuple);
					inferrdRelGropus.put(key, group);
				}
			}
		}

		Set<Integer> inferredKeySet = inferrdRelGropus.keySet();
		for (Integer integer : inferredKeySet) {
			DefaultMutableTreeNode group = new DefaultMutableTreeNode(new TreeEditorObjectWrapper("Group " + integer, IconUtilities.BLUE_ICON, null));
			addNodeInSortedOrder(inferredRels, group);
			for (I_RelTuple relTuple : inferrdRelGropus.get(integer)) {
				createGenericRelationProperties(group, relTuple);
			}
		}

		model.reload();
	}

	private void createGenericRelationProperties(DefaultMutableTreeNode parentNode, RelationshipVersionBI relTuple) throws IOException, TerminologyException {
		DefaultMutableTreeNode relNode = createRelationNode(relTuple);
		addNodeInSortedOrder(parentNode, relNode);

		UUID primordalUuid = relTuple.getPrimUuid();
		String primUUIDLabel = "Primordial UUID - (" + primordalUuid.toString() + ")";
		DefaultMutableTreeNode primordialUuidNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(primUUIDLabel, IconUtilities.GREEN_ICON, null));
		addNodeInSortedOrder(relNode, primordialUuidNode);

		DefaultMutableTreeNode typeNode = createRelAttributeNode(relTuple.getTypeNid());
		addNodeInSortedOrder(relNode, typeNode);

		DefaultMutableTreeNode targetNode = createRelAttributeNode(relTuple.getDestinationNid());
		addNodeInSortedOrder(relNode, targetNode);

		DefaultMutableTreeNode autorNode = createRelAttributeNode(relTuple.getAuthorNid());
		addNodeInSortedOrder(relNode, autorNode);

		DefaultMutableTreeNode originalNode = createRelAttributeNode(relTuple.getOriginNid());
		addNodeInSortedOrder(relNode, originalNode);

		DefaultMutableTreeNode characteristicNode = createRelAttributeNode(relTuple.getCharacteristicNid());
		addNodeInSortedOrder(relNode, characteristicNode);

		DefaultMutableTreeNode pathNode = createRelAttributeNode(relTuple.getPathNid());
		addNodeInSortedOrder(relNode, pathNode);

		DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode("Group - " + relTuple.getGroup());
		addNodeInSortedOrder(relNode, groupNode);

		DefaultMutableTreeNode statusNode = createRelAttributeNode(relTuple.getStatusNid());
		addNodeInSortedOrder(relNode, statusNode);

		DefaultMutableTreeNode timeNode = new DefaultMutableTreeNode("Time: " + relTuple.getTime());
		addNodeInSortedOrder(relNode, timeNode);

		DefaultMutableTreeNode factContextNode = new DefaultMutableTreeNode("Fact context name: " + factContextName);
		addNodeInSortedOrder(relNode, factContextNode);

	}

	private void createGenericRelationProperties(DefaultMutableTreeNode parentNode, I_RelTuple relTuple) throws IOException, TerminologyException {
		DefaultMutableTreeNode relNode = createRelationNode(relTuple);
		addNodeInSortedOrder(parentNode, relNode);

		UUID primordalUuid = relTuple.getPrimUuid();
		String primUUIDLabel = "Primordial UUID - (" + primordalUuid.toString() + ")";
		DefaultMutableTreeNode primordialUuidNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(primUUIDLabel, IconUtilities.GREEN_ICON, null));
		addNodeInSortedOrder(relNode, primordialUuidNode);

		DefaultMutableTreeNode typeNode = createRelAttributeNode(relTuple.getTypeNid());
		addNodeInSortedOrder(relNode, typeNode);

		DefaultMutableTreeNode targetNode = createRelAttributeNode(relTuple.getDestinationNid());
		addNodeInSortedOrder(relNode, targetNode);

		DefaultMutableTreeNode autorNode = createRelAttributeNode(relTuple.getAuthorNid());
		addNodeInSortedOrder(relNode, autorNode);

		DefaultMutableTreeNode originalNode = createRelAttributeNode(relTuple.getOriginNid());
		addNodeInSortedOrder(relNode, originalNode);

		DefaultMutableTreeNode characteristicNode = createRelAttributeNode(relTuple.getCharacteristicNid());
		addNodeInSortedOrder(relNode, characteristicNode);

		DefaultMutableTreeNode pathNode = createRelAttributeNode(relTuple.getPathNid());
		addNodeInSortedOrder(relNode, pathNode);

		DefaultMutableTreeNode statusNode = createRelAttributeNode(relTuple.getStatusNid());
		addNodeInSortedOrder(relNode, statusNode);

		DefaultMutableTreeNode timeNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper("Time: " + relTuple.getTime(), IconUtilities.GREEN_ICON, null));
		addNodeInSortedOrder(relNode, timeNode);

		DefaultMutableTreeNode factContextNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper("Fact context name: " + factContextName, IconUtilities.GREEN_ICON, null));
		addNodeInSortedOrder(relNode, factContextNode);
	}
	
	private void createGenericRelationProperties(DefaultMutableTreeNode parentNode, DrRelationship relTuple) throws IOException, TerminologyException {
		DefaultMutableTreeNode relNode = createRelationNode(relTuple);
		addNodeInSortedOrder(parentNode, relNode);
		
		UUID primordalUuid = UUID.fromString(relTuple.getPrimordialUuid());
		String primUUIDLabel = "Primordial UUID - (" + primordalUuid.toString() + ")";
		DefaultMutableTreeNode primordialUuidNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(primUUIDLabel, IconUtilities.GREEN_ICON, null));
		addNodeInSortedOrder(relNode, primordialUuidNode);
		
		DefaultMutableTreeNode typeNode = createRelAttributeNode(relTuple.getTypeUuid());
		addNodeInSortedOrder(relNode, typeNode);
		
		DefaultMutableTreeNode targetNode = createRelAttributeNode(relTuple.getTargetUuid());
		addNodeInSortedOrder(relNode, targetNode);
		
		DefaultMutableTreeNode autorNode = createRelAttributeNode(relTuple.getAuthorUuid());
		addNodeInSortedOrder(relNode, autorNode);
		
		DefaultMutableTreeNode originalNode = createRelAttributeNode(relTuple.getSourceUuid());
		addNodeInSortedOrder(relNode, originalNode);
		
		DefaultMutableTreeNode characteristicNode = createRelAttributeNode(relTuple.getCharacteristicUuid());
		addNodeInSortedOrder(relNode, characteristicNode);
		
		DefaultMutableTreeNode pathNode = createRelAttributeNode(relTuple.getPathUuid());
		addNodeInSortedOrder(relNode, pathNode);
		
		DefaultMutableTreeNode statusNode = createRelAttributeNode(relTuple.getStatusUuid());
		addNodeInSortedOrder(relNode, statusNode);
		
		DefaultMutableTreeNode timeNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper("Time: " + relTuple.getTime(), IconUtilities.GREEN_ICON, null));
		addNodeInSortedOrder(relNode, timeNode);
		
		DefaultMutableTreeNode factContextNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper("Fact context name: " + factContextName, IconUtilities.GREEN_ICON, null));
		addNodeInSortedOrder(relNode, factContextNode);
	}

	private DefaultMutableTreeNode createRelationNode(RelationshipVersionBI relTuple) throws IOException, TerminologyException {
		String typeUuid = tf.nidToUuid(relTuple.getTypeNid()).toString();
		I_GetConceptData type = tf.getConcept(UUID.fromString(typeUuid));

		String targetUuid = tf.nidToUuid(relTuple.getDestinationNid()).toString();
		I_GetConceptData target = tf.getConcept(UUID.fromString(targetUuid));

		String uuid = tf.nidToUuid(relTuple.getCharacteristicNid()).toString();
		I_GetConceptData charType = tf.getConcept(UUID.fromString(uuid));
		String charTypeStr = charType.getInitialText().substring(0, charType.getInitialText().indexOf("(") - 1);
		
		I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
		NidSetBI descTypes = tf.newIntSet();
		descTypes.add(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getConceptNid());
		
		NidSetBI allowedStatuses = tf.newIntSet();
		allowedStatuses.add(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getConceptNid());
		List<? extends I_DescriptionTuple> descriptions = target.getDescriptionTuples(allowedStatuses , descTypes , config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
		String fsn = target.getInitialText();
		if(descriptions!= null && !descriptions.isEmpty()){
			fsn = descriptions.get(0).getText();
		}
		
		String label = fsn + " - " + target.getInitialText() + " (" + charTypeStr + ")";

		DefaultMutableTreeNode relNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.TRANSLATION_PROJECT, null));
		return relNode;
	}

	private DefaultMutableTreeNode createRelationNode(I_RelTuple relTuple) throws IOException, TerminologyException {
		String typeUuid = tf.nidToUuid(relTuple.getTypeNid()).toString();
		I_GetConceptData type = tf.getConcept(UUID.fromString(typeUuid));

		String targetUuid = tf.nidToUuid(relTuple.getDestinationNid()).toString();
		I_GetConceptData target = tf.getConcept(UUID.fromString(targetUuid));

		String uuid = tf.nidToUuid(relTuple.getCharacteristicNid()).toString();
		I_GetConceptData charType = tf.getConcept(UUID.fromString(uuid));
		// String charTypeStr = charType.getInitialText().substring(0,
		// charType.getInitialText().indexOf("(") - 1);

		I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
		List<? extends I_ConceptAttributeTuple> conceptAttributeTuples = target.getConceptAttributeTuples(config.getPrecedence(), config.getConflictResolutionStrategy());
		DefaultMutableTreeNode relNode = new DefaultMutableTreeNode();
		String charTypeStr = charType.getInitialText();
		if(charType.getInitialText().contains("(")){
			charTypeStr = charType.getInitialText().substring(0, charType.getInitialText().indexOf("(") - 1);
		}
		
		NidSetBI descTypes = tf.newIntSet();
		descTypes.add(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getConceptNid());
		
		NidSetBI allowedStatuses = tf.newIntSet();
		allowedStatuses.add(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getConceptNid());
		List<? extends I_DescriptionTuple> descriptions = target.getDescriptionTuples(allowedStatuses , descTypes , config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
		String fsn = target.getInitialText();
		if(descriptions!= null && !descriptions.isEmpty()){
			fsn = descriptions.get(0).getText();
		}
		
		String label = fsn + " (" + charTypeStr + ")";
		if (!conceptAttributeTuples.isEmpty() && config.getDestRelTypes().contains(relTuple.getTypeNid())) {
			I_ConceptAttributeTuple attributes = conceptAttributeTuples.get(0);
			
			if (attributes.getStatusNid() == retired.getNid() || attributes.getStatusNid() == inactive.getNid()) {
				relNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.INACTIVE_PARENT, target));
			} else if (attributes.isDefined()) {
				relNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.DEFINED_PARENT, target));
			} else {
				relNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.PRIMITIVE_PARENT, target));
			}
		} else {
			label = type.getInitialText() + " - " + fsn + " (" + charTypeStr + ")";
			relNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.ROLE, target));
		}

		// String label = type.getInitialText() + " - " +
		// target.getInitialText() + " ("+ charTypeStr +")";

		return relNode;
	}
	
	private DefaultMutableTreeNode createRelationNode(DrRelationship relTuple) throws IOException, TerminologyException {
		String typeUuid = relTuple.getTypeUuid();
		I_GetConceptData type = tf.getConcept(UUID.fromString(typeUuid));
		
		String targetUuid = relTuple.getTargetUuid();
		I_GetConceptData target = tf.getConcept(UUID.fromString(targetUuid));
		
		String uuid = relTuple.getCharacteristicUuid();
		I_GetConceptData charType = tf.getConcept(UUID.fromString(uuid));
		// String charTypeStr = charType.getInitialText().substring(0,
		// charType.getInitialText().indexOf("(") - 1);
		
		I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
		List<? extends I_ConceptAttributeTuple> conceptAttributeTuples = target.getConceptAttributeTuples(config.getPrecedence(), config.getConflictResolutionStrategy());
		DefaultMutableTreeNode relNode = new DefaultMutableTreeNode();
		String charTypeStr = charType.getInitialText();
		if(charType.getInitialText().contains("(")){
			charTypeStr = charType.getInitialText().substring(0, charType.getInitialText().indexOf("(") - 1);
		}
		
		NidSetBI descTypes = tf.newIntSet();
		descTypes.add(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getConceptNid());
		
		NidSetBI allowedStatuses = tf.newIntSet();
		allowedStatuses.add(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getConceptNid());
		List<? extends I_DescriptionTuple> descriptions = target.getDescriptionTuples(allowedStatuses , descTypes , config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
		String fsn = target.getInitialText();
		if(descriptions!= null && !descriptions.isEmpty()){
			fsn = descriptions.get(0).getText();
		}
		
		String label = fsn + " (" + charTypeStr + ")";
		if (!conceptAttributeTuples.isEmpty() && config.getDestRelTypes().contains(tf.uuidToNative(UUID.fromString(typeUuid)))) {
			I_ConceptAttributeTuple attributes = conceptAttributeTuples.get(0);
			
			if (attributes.getStatusNid() == retired.getNid() || attributes.getStatusNid() == inactive.getNid()) {
				relNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.INACTIVE_PARENT, target));
			} else if (attributes.isDefined()) {
				relNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.DEFINED_PARENT, target));
			} else {
				relNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.PRIMITIVE_PARENT, target));
			}
		} else {
			label = type.getInitialText() + " - " + fsn + " (" + charTypeStr + ")";
			relNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.ROLE, target));
		}
		
		// String label = type.getInitialText() + " - " +
		// target.getInitialText() + " ("+ charTypeStr +")";
		
		return relNode;
	}

	private DefaultMutableTreeNode createRelAttributeNode(int conceptNid) throws IOException, TerminologyException {
		String uuid = tf.nidToUuid(conceptNid).toString();
		I_GetConceptData concept = tf.getConcept(UUID.fromString(uuid));

		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.getInitialText() + " - (" + uuid + ")", IconUtilities.GREEN_ICON, null));
		return node;
	}
	private DefaultMutableTreeNode createRelAttributeNode(String conceptUUID) throws IOException, TerminologyException {
		I_GetConceptData concept = tf.getConcept(UUID.fromString(conceptUUID));
		
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.getInitialText() + " - (" + conceptUUID + ")", IconUtilities.GREEN_ICON, null));
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
		label2 = new JLabel();
		label1 = new JLabel();
		conceptList = new JList();
		scrollPane1 = new JScrollPane();
		conceptDetailsTree = new JTree();

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
			((GridBagLayout) conceptTextBox.getLayout()).columnWidths = new int[] { 0, 0, 0 };
			((GridBagLayout) conceptTextBox.getLayout()).rowHeights = new int[] { 0, 0, 0 };
			((GridBagLayout) conceptTextBox.getLayout()).columnWeights = new double[] { 0.0, 0.0, 1.0E-4 };
			((GridBagLayout) conceptTextBox.getLayout()).rowWeights = new double[] { 0.0, 0.0, 1.0E-4 };

			// ---- label2 ----
			label2.setText("<html><font size=\"-1\">(Drag and drop concept to see the relationships)");
			conceptTextBox.add(label2, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0));

			// ---- label1 ----
			label1.setText("Concept");
			conceptTextBox.add(label1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));

			// ---- conceptList ----
			conceptList.setVisibleRowCount(1);
			conceptTextBox.add(conceptList, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(conceptTextBox, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

		// ======== scrollPane1 ========
		{

			// ---- conceptDetailsTree ----
			conceptDetailsTree.setRootVisible(false);
			scrollPane1.setViewportView(conceptDetailsTree);
		}
		add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel conceptTextBox;
	private JLabel label2;
	private JLabel label1;
	private JList conceptList;
	private JScrollPane scrollPane1;
	private JTree conceptDetailsTree;
	// JFormDesigner - End of variables declaration //GEN-END:variables

}
