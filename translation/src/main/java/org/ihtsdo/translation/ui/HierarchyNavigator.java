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

package org.ihtsdo.translation.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.FilteredImageSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.dwfa.ace.TermLabelMaker;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.dnd.ConceptTransferable;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.qa.gui.ObjectTransferHandler;
import org.ihtsdo.qa.gui.viewers.gui.TreeObj;
import org.ihtsdo.qa.gui.viewers.utils.IconUtilities;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * The Class HierarchyNavigator.
 * 
 * @author Guillermo Reynoso
 */
public class HierarchyNavigator extends JPanel {

	/** The config. */
	public I_ConfigAceFrame config;

	/** The allowed dest rel types. */
	private I_IntSet allowedDestRelTypes;

	/** The allowed status. */
	private I_IntSet allowedStatus;

	/** The container panel. */
	private JTabbedPane containerPanel;

	/** The concept model. */
	private DefaultListModel conceptModel;

	private WorkListMember worklistMember;

	/**
	 * Instantiates a new hierarchy navigator.
	 */
	public HierarchyNavigator() {
		initComponents();
		comboBox1.setTransferHandler(new ObjectTransferHandler(config, null));
		comboBox1.setRenderer(new ComboBoxRenderer());
		tree1.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
		tree2.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
		tree1.setCellRenderer(new HierarchyIconRenderer());
		tree2.setCellRenderer(new HierarchyIconRenderer());
		if (Terms.get() != null) {
			I_TermFactory termFactory = Terms.get();
			if (termFactory != null) {
				allowedDestRelTypes = termFactory.newIntSet();
				allowedStatus = termFactory.newIntSet();
			}
			try {
				config = Terms.get().getActiveAceFrameConfig();
				// allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
				allowedDestRelTypes = config.getDestRelTypes();
				// allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
				allowedStatus = config.getAllowedStatus();

			} catch (TerminologyException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
			tree1.setTransferHandler(new ObjectTransferHandler(config, null));
			tree1.setRootVisible(false);
			tree1.setShowsRootHandles(true);
			tree1.setDragEnabled(true);
			tree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

			ToolTipManager.sharedInstance().registerComponent(tree1);

			// applyDNDHack(tree1);
			tree2.setTransferHandler(new ObjectTransferHandler(config, null));
			tree2.setRootVisible(false);
			tree2.setShowsRootHandles(true);
			tree2.setDragEnabled(true);
			tree2.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		}
		ToolTipManager.sharedInstance().registerComponent(tree2);

		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(tree1, DnDConstants.ACTION_COPY, new DragGestureListenerWithImage(new TermLabelDragSourceListener(), tree1));

		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(tree2, DnDConstants.ACTION_COPY, new DragGestureListenerWithImage(new TermLabelDragSourceListener(), tree2));

		// applyDNDHack(tree2);
	}

	class ComboBoxRenderer extends JLabel implements ListCellRenderer {
		public ComboBoxRenderer() {
			setOpaque(true);
		}

		/*
		 * This method finds the image and text corresponding to the selected
		 * value and returns the label, set up to display the text and image.
		 */
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			// Get the selected index. (The index param isn't
			// always valid, so just use the value.)
			ConceptVersionBI selectedIndex = (ConceptVersionBI) value;
			if(selectedIndex != null){
				setText(selectedIndex.toUserString());
			}
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			// Set the icon and text. If icon was null, say so.
			return this;
		}
	}

	/**
	 * Sets the focus concept.
	 * 
	 * @param focusConcept
	 *            the new focus concept
	 */
	public void setFocusConcept(ConceptVersionBI focusConcept) {
		comboBox1.addItem(focusConcept);
		comboBox1.setSelectedItem(focusConcept);
	}

	/**
	 * Combo box1 action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void comboBox1ActionPerformed(ActionEvent e) {
		refreshTrees();
	}

	/**
	 * Refresh trees.
	 */
	private void refreshTrees() {
		DefaultTreeModel tree1Model = getParentsTreeModel((ConceptVersionBI) comboBox1.getSelectedItem());
		tree1.setModel(tree1Model);
		DefaultTreeModel childrenTreeModel = getChildrenTreeModel((ConceptVersionBI) comboBox1.getSelectedItem());
		tree2.setModel(childrenTreeModel);
		childrenTreeModel.reload();
		tree1Model.reload();
		tree1.revalidate();
		tree2.revalidate();
	}

	/**
	 * Combo box1 item state changed.
	 * 
	 * @param e
	 *            the e
	 */
	private void comboBox1ItemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			refreshTrees();
		}
	}

	/**
	 * The listener interface for receiving termLabelDragSource events. The
	 * class that is interested in processing a termLabelDragSource event
	 * implements this interface, and the object created with that class is
	 * registered with a component using the component's
	 * <code>addTermLabelDragSourceListener<code> method. When
	 * the termLabelDragSource event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see TermLabelDragSourceEvent
	 */
	private class TermLabelDragSourceListener implements DragSourceListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.dnd.DragSourceListener#dragDropEnd(java.awt.dnd.
		 * DragSourceDropEvent)
		 */
		public void dragDropEnd(DragSourceDropEvent dsde) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.
		 * DragSourceDragEvent)
		 */
		public void dragEnter(DragSourceDragEvent dsde) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent
		 * )
		 */
		public void dragExit(DragSourceEvent dse) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent
		 * )
		 */
		public void dragOver(DragSourceDragEvent dsde) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.
		 * DragSourceDragEvent)
		 */
		public void dropActionChanged(DragSourceDragEvent dsde) {
		}
	}

	/**
	 * The Class DragGestureListenerWithImage.
	 */
	private class DragGestureListenerWithImage implements DragGestureListener {

		/** The dsl. */
		DragSourceListener dsl;

		/** The j tree. */
		JTree jTree;

		/**
		 * Instantiates a new drag gesture listener with image.
		 * 
		 * @param dsl
		 *            the dsl
		 * @param jTree
		 *            the j tree
		 */
		public DragGestureListenerWithImage(DragSourceListener dsl, JTree jTree) {

			super();
			this.jTree = jTree;
			this.dsl = dsl;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd
		 * .DragGestureEvent)
		 */
		public void dragGestureRecognized(DragGestureEvent dge) {
			int selRow = jTree.getRowForLocation(dge.getDragOrigin().x, dge.getDragOrigin().y);
			TreePath path = jTree.getPathForLocation(dge.getDragOrigin().x, dge.getDragOrigin().y);
			if (selRow != -1) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				try {
					TreeObj To = (TreeObj) node.getUserObject();
					Object objvalue = To.getAtrValue();
					if (objvalue instanceof ConceptVersionBI) {
						ConceptVersionBI obj = (ConceptVersionBI) objvalue;
						Image dragImage = getDragImage(obj);
						Point imageOffset = new Point(-10, -(dragImage.getHeight(jTree) + 1));
						dge.startDrag(DragSource.DefaultCopyDrop, dragImage, imageOffset, getTransferable(obj), dsl);
					}
				} catch (InvalidDnDOperationException e) {
					AceLog.getAppLog().info(e.toString());
				} catch (Exception ex) {
					AceLog.getAppLog().alertAndLogException(ex);
				}
			}
		}

		/**
		 * Gets the transferable.
		 * 
		 * @param obj
		 *            the obj
		 * @return the transferable
		 * @throws TerminologyException
		 *             the terminology exception
		 * @throws IOException
		 *             signals that an I/O exception has occurred.
		 */
		private Transferable getTransferable(ConceptVersionBI obj) throws TerminologyException, IOException {
			return new ConceptTransferable(Terms.get().getConcept(obj.getConceptNid()));
		}

		/**
		 * Gets the drag image.
		 * 
		 * @param obj
		 *            the obj
		 * @return the drag image
		 * @throws IOException
		 *             signals that an I/O exception has occurred.
		 */
		public Image getDragImage(ConceptVersionBI obj) throws IOException {

			DescriptionVersionBI desc = null;
			try {
				desc = obj.getDescriptionPreferred();
			} catch (ContradictionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (desc != null) {
				JLabel dragLabel = TermLabelMaker.newLabel(desc, false, false).getLabel();
				dragLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
				Image dragImage = createImage(dragLabel.getWidth(), dragLabel.getHeight());
				dragLabel.setVisible(true);
				Graphics og = dragImage.getGraphics();
				og.setClip(dragLabel.getBounds());
				dragLabel.paint(og);
				og.dispose();
				FilteredImageSource fis = new FilteredImageSource(dragImage.getSource(), TermLabelMaker.getTransparentFilter());
				dragImage = Toolkit.getDefaultToolkit().createImage(fis);
				return dragImage;
			} else {
				return null;
			}
		}
	}

	/**
	 * Gets the children.
	 * 
	 * @param childCpt
	 *            the concept
	 * @return the children
	 */
	private ArrayList<RelationshipVersionBI> getChildren(ConceptVersionBI childCpt) {
		Collection<? extends RelationshipVersionBI> children = new ArrayList<RelationshipVersionBI>();
		ArrayList<RelationshipVersionBI> result = new ArrayList<RelationshipVersionBI>();
		try {
			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			children = childCpt.getRelationshipsIncomingActiveIsa();
			for (RelationshipVersionBI relationshipVersionBI : children) {
				if (rInfer.isSelected() && relationshipVersionBI.getCharacteristicNid() == SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getConceptNid()) {
					result.add(relationshipVersionBI);
				} else if (!rInfer.isSelected() && relationshipVersionBI.getCharacteristicNid() == SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getConceptNid()) {
					result.add(relationshipVersionBI);
				}
			}
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (ContradictionException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return result;
	}

	/**
	 * Gets the parents.
	 * 
	 * @param concept
	 *            the concept
	 * @return the parents
	 */
	private List<RelationshipVersionBI> getParents(ConceptVersionBI concept) {
		Collection<? extends RelationshipVersionBI> children = new ArrayList<RelationshipVersionBI>();
		ArrayList<RelationshipVersionBI> result = new ArrayList<RelationshipVersionBI>();
		try {
			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			children = concept.getRelationshipsOutgoingActiveIsa();
			for (RelationshipVersionBI relationshipVersionBI : children) {
				if (rInfer.isSelected() && relationshipVersionBI.getCharacteristicNid() == SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getConceptNid()) {
					result.add(relationshipVersionBI);
				} else if (!rInfer.isSelected() && relationshipVersionBI.getCharacteristicNid() == SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getConceptNid()) {
					result.add(relationshipVersionBI);
				}
			}
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (ContradictionException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return result;
	}

	/**
	 * Gets the parents tree model.
	 * 
	 * @param concept
	 *            the concept
	 * @return the parents tree model
	 */
	private DefaultTreeModel getParentsTreeModel(ConceptVersionBI concept) {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Root");
		if (concept != null) {
			getNewParent(top, concept);
		}

		DefaultTreeModel treeModel = new DefaultTreeModel(top);
		return treeModel;
	}

	/**
	 * Tree1 tree will expand.
	 * 
	 * @param e
	 *            the e
	 * @throws ExpandVetoException
	 *             the expand veto exception
	 */
	private void tree1TreeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
		TreePath path = e.getPath();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		if (node.getFirstChild().toString().trim().equals("Loading...")) {
			node.removeAllChildren();
			TreeObj to = (TreeObj) node.getUserObject();
			DescriptionVersionBI nodeConcept = (DescriptionVersionBI) to.getAtrValue();
			try {
				getNewParent(node, nodeConcept.getEnclosingConcept().getVersion(config.getViewCoordinate()));
			} catch (ContradictionException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void getNewParent(DefaultMutableTreeNode node, ConceptVersionBI nodeConcept) {
		for (RelationshipVersionBI ancestor : getParents(nodeConcept)) {
			DefaultMutableTreeNode newParent = null;
			try {
				ConceptVersionBI parentCpt = Ts.get().getConceptVersion(config.getViewCoordinate(), ancestor.getTargetNid());
				ConceptAttributeVersionBI conceptAttrs = parentCpt.getConceptAttributesActive();
				if (!conceptAttrs.isActive(config.getViewCoordinate())) {
					newParent = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.getInactiveParent()), parentCpt.toUserString() + " (" + getParents(parentCpt).size() + ")", parentCpt.getDescriptionPreferred()));
				} else if (conceptAttrs.isDefined()) {
					newParent = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.getDefinedParent()), parentCpt.toUserString() + " (" + getParents(parentCpt).size() + ")", parentCpt.getDescriptionPreferred()));
				} else {
					newParent = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.getPrimitiveParent()), parentCpt.toUserString() + " (" + getParents(parentCpt).size() + ")", parentCpt.getDescriptionPreferred()));
				}
				newParent.add(new DefaultMutableTreeNode("Loading..."));
				node.add(newParent);
			} catch (IOException e1) {
				AceLog.getAppLog().alertAndLogException(e1);
			} catch (ContradictionException e1) {
				AceLog.getAppLog().alertAndLogException(e1);
			}
		}
	}

	/**
	 * Tree2 tree will expand.
	 * 
	 * @param e
	 *            the e
	 * @throws ExpandVetoException
	 *             the expand veto exception
	 */
	private void tree2TreeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
		TreePath path = e.getPath();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		if (node.getFirstChild().toString().trim().equals("Loading...")) {
			node.removeAllChildren();
			TreeObj to = (TreeObj) node.getUserObject();
			DescriptionVersionBI nodeConcept = (DescriptionVersionBI) to.getAtrValue();
			try {
				getNewChild(nodeConcept.getEnclosingConcept().getVersion(config.getViewCoordinate()), node);
			} catch (ContradictionException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Gets the children tree model.
	 * 
	 * @param concept
	 *            the concept
	 * @return the children tree model
	 */
	private DefaultTreeModel getChildrenTreeModel(ConceptVersionBI concept) {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Root");
		if (concept != null) {
			getNewChild(concept, top);
		}

		DefaultTreeModel treeModel = new DefaultTreeModel(top);
		return treeModel;
	}

	private void getNewChild(ConceptVersionBI concept, DefaultMutableTreeNode top) {
		for (RelationshipVersionBI child : getChildren(concept)) {
			DefaultMutableTreeNode newChild = null;
			try {
				ConceptVersionBI childCpt = Ts.get().getConceptVersion(config.getViewCoordinate(), child.getSourceNid());
				ConceptAttributeVersionBI conceptAttrs = childCpt.getConceptAttributesActive();
				if (conceptAttrs != null) {
					if (!conceptAttrs.isActive(config.getViewCoordinate())) {
						newChild = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.getInactive()), childCpt.toUserString() + " (" + getChildren(childCpt).size() + ")", childCpt.getDescriptionPreferred()));
					} else if (conceptAttrs.isDefined()) {
						newChild = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.getDefined()), childCpt.toUserString() + " (" + getChildren(childCpt).size() + ")", childCpt.getDescriptionPreferred()));
					} else {
						newChild = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.getPrimitive()), childCpt.toUserString() + " (" + getChildren(childCpt).size() + ")", childCpt.getDescriptionPreferred()));
					}
					newChild.add(new DefaultMutableTreeNode("Loading..."));
					top.add(newChild);
				}
			} catch (IOException e1) {
				AceLog.getAppLog().alertAndLogException(e1);
			} catch (ContradictionException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}
	}

	/**
	 * Sets the container panel.
	 * 
	 * @param containerPanel
	 *            the new container panel
	 */
	public void setContainerPanel(JTabbedPane containerPanel) {
		this.containerPanel = containerPanel;
	}

	/**
	 * Expand button action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void expandButtonActionPerformed(ActionEvent e) {

		JPanel similarityPanel = (JPanel) containerPanel.getSelectedComponent();
		final int indx = containerPanel.getSelectedIndex();
		expandButton.setVisible(false);
		final JDialog similarityDialog = new JDialog();
		similarityDialog.setContentPane(similarityPanel);
		similarityDialog.setModal(true);
		similarityDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		similarityDialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				expandButton.setVisible(true);
				containerPanel.insertTab("Hierarchy", null, similarityDialog.getContentPane(), "Hierarchy", indx);
				containerPanel.setSelectedIndex(indx);
				similarityDialog.dispose();
			}
		});
		similarityDialog.setSize(new Dimension(700, 550));
		Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension ventana = similarityDialog.getSize();
		similarityDialog.setLocation((pantalla.width - ventana.width) / 2, (pantalla.height - ventana.height) / 2);

		similarityDialog.setVisible(true);
	}

	/**
	 * Radio button1 action performed.
	 */
	private void radioButton1ActionPerformed() {
		if (comboBox1.getSelectedItem() != null) {
			tree1.setModel(getParentsTreeModel((ConceptVersionBI) comboBox1.getSelectedItem()));
			tree2.setModel(getChildrenTreeModel((ConceptVersionBI) comboBox1.getSelectedItem()));
		}
	}

	/**
	 * R stat action performed.
	 */
	private void rStatActionPerformed() {
		if (comboBox1.getSelectedItem() != null) {
			tree1.setModel(getParentsTreeModel((ConceptVersionBI) comboBox1.getSelectedItem()));
			tree2.setModel(getChildrenTreeModel((ConceptVersionBI) comboBox1.getSelectedItem()));
		}
	}

	/**
	 * The Class HierarchyIconRenderer.
	 */
	class HierarchyIconRenderer extends DefaultTreeCellRenderer {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1502464349648287786L;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent
		 * (javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int,
		 * boolean)
		 */
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

			JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			if (node != null && (node.getUserObject() instanceof TreeObj)) {
				TreeObj nodeObject = (TreeObj) node.getUserObject();
				if (nodeObject != null) {
					Integer type = Integer.parseInt(nodeObject.getObjType());
					label.setIcon(IconUtilities.getIconForConceptDetails(type));

					if (type == IconUtilities.getDefined()) {
						label.setToolTipText("Fully defined descendant");
					} else if (type == IconUtilities.getPrimitive()) {
						label.setToolTipText("Primitive descendant");
					} else if (type == IconUtilities.getInactive()) {
						label.setToolTipText("Inactive descendant");
					} else if (type == IconUtilities.getPrimitiveParent()) {
						label.setToolTipText("Primitive ancestor");
					} else if (type == IconUtilities.getDefinedParent()) {
						label.setToolTipText("Fully defined ancestor");
					} else if (type == IconUtilities.getInactiveParent()) {
						label.setToolTipText("Inactive ancestor");
					}
				}
			}
			return label;
		}

	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		scrollPane1 = new JScrollPane();
		tree1 = new JTree();
		panel1 = new JPanel();
		comboBox1 = new JComboBox();
		panel2 = new JPanel();
		rInfer = new JRadioButton();
		rStat = new JRadioButton();
		expandButton = new JButton();
		scrollPane2 = new JScrollPane();
		tree2 = new JTree();

		// ======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new GridBagLayout());
		((GridBagLayout) getLayout()).columnWidths = new int[] { 424, 0 };
		((GridBagLayout) getLayout()).rowHeights = new int[] { 0, 0, 0, 0, 0 };
		((GridBagLayout) getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
		((GridBagLayout) getLayout()).rowWeights = new double[] { 1.0, 0.0, 1.0, 0.0, 1.0E-4 };

		// ======== scrollPane1 ========
		{

			// ---- tree1 ----
			tree1.setVisibleRowCount(5);
			tree1.addTreeWillExpandListener(new TreeWillExpandListener() {
				@Override
				public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
					tree1TreeWillExpand(e);
				}

				@Override
				public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException {
				}
			});
			scrollPane1.setViewportView(tree1);
		}
		add(scrollPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

		// ======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout) panel1.getLayout()).columnWidths = new int[] { 197, 0, 0 };
			((GridBagLayout) panel1.getLayout()).rowHeights = new int[] { 0, 0, 0 };
			((GridBagLayout) panel1.getLayout()).columnWeights = new double[] { 0.0, 1.0, 1.0E-4 };
			((GridBagLayout) panel1.getLayout()).rowWeights = new double[] { 0.0, 0.0, 1.0E-4 };

			// ---- comboBox1 ----
			comboBox1.setMaximumSize(new Dimension(250, 27));
			comboBox1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					comboBox1ActionPerformed(e);
				}
			});
			comboBox1.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					comboBox1ItemStateChanged(e);
				}
			});
			panel1.add(comboBox1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			// ======== panel2 ========
			{
				panel2.setLayout(new GridBagLayout());
				((GridBagLayout) panel2.getLayout()).columnWidths = new int[] { 0, 0, 0, 0 };
				((GridBagLayout) panel2.getLayout()).rowHeights = new int[] { 0, 0 };
				((GridBagLayout) panel2.getLayout()).columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0E-4 };
				((GridBagLayout) panel2.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

				// ---- rInfer ----
				rInfer.setText("Inferred");
				rInfer.setSelected(true);
				rInfer.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						radioButton1ActionPerformed();
					}
				});
				panel2.add(rInfer, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

				// ---- rStat ----
				rStat.setText("Stated");
				rStat.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						rStatActionPerformed();
					}
				});
				panel2.add(rStat, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

				// ---- expandButton ----
				expandButton.setText("Expand");
				expandButton.setMnemonic('E');
				expandButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						expandButtonActionPerformed(e);
					}
				});
				panel2.add(expandButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			}
			panel1.add(panel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

		// ======== scrollPane2 ========
		{

			// ---- tree2 ----
			tree2.setVisibleRowCount(5);
			tree2.addTreeWillExpandListener(new TreeWillExpandListener() {
				@Override
				public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
					tree2TreeWillExpand(e);
				}

				@Override
				public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException {
				}
			});
			scrollPane2.setViewportView(tree2);
		}
		add(scrollPane2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

		// ---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(rInfer);
		buttonGroup1.add(rStat);
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	/** The scroll pane1. */
	private JScrollPane scrollPane1;

	/** The tree1. */
	private JTree tree1;

	/** The panel1. */
	private JPanel panel1;

	/** The combo box1. */
	private JComboBox comboBox1;

	/** The panel2. */
	private JPanel panel2;

	/** The r infer. */
	private JRadioButton rInfer;

	/** The r stat. */
	private JRadioButton rStat;

	/** The expand button. */
	private JButton expandButton;

	/** The scroll pane2. */
	private JScrollPane scrollPane2;

	/** The tree2. */
	private JTree tree2;
	// JFormDesigner - End of variables declaration //GEN-END:variables

}
