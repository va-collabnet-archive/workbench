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

import java.awt.*;
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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.FilteredImageSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.dwfa.ace.TermLabelMaker;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.dnd.ConceptTransferable;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.panel.TreeObj;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.tk.api.RelAssertionType;
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
	
	/** The inactive. */
	private I_GetConceptData inactive;
	
	/** The retired. */
	private I_GetConceptData retired;
	
	/** The container panel. */
	private JTabbedPane containerPanel;

	/**
	 * Instantiates a new hierarchy navigator.
	 */
	public HierarchyNavigator() {
		initComponents();

		comboBox1.setTransferHandler(new ObjectTransferHandler());

		DefaultMutableTreeNode tmpRoot = new DefaultMutableTreeNode();

		tree1.setModel(new DefaultTreeModel(tmpRoot));
		tree2.setModel(new DefaultTreeModel(tmpRoot));
		tree1.setCellRenderer(new HierarchyIconRenderer());
		tree2.setCellRenderer(new HierarchyIconRenderer());
		I_TermFactory termFactory = Terms.get();
		if (termFactory != null) {
			allowedDestRelTypes = termFactory.newIntSet();
			allowedStatus = termFactory.newIntSet();
			try {
				config = termFactory.getActiveAceFrameConfig();
				// allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
				allowedDestRelTypes = config.getDestRelTypes();
				// allowedDestRelTypes=Terms.get().newIntSet();
				// allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
				allowedStatus = Terms.get().newIntSet();
				allowedStatus.add(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
				allowedStatus.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
				// allowedStatus=config.getAllowedStatus();

				inactive = Terms.get().getConcept(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
				retired = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
				// allowedStatus.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
				// allowedStatus.add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
				// config.setAllowedStatus(allowedStatus);
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		tree1.setTransferHandler(new ObjectTransferHandler());
		tree1.setRootVisible(false);
		tree1.setShowsRootHandles(true);
		tree1.setDragEnabled(true);
		tree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		ToolTipManager.sharedInstance().registerComponent(tree1);

		// applyDNDHack(tree1);
		tree2.setTransferHandler(new ObjectTransferHandler());
		tree2.setRootVisible(false);
		tree2.setShowsRootHandles(true);
		tree2.setDragEnabled(true);
		tree2.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		ToolTipManager.sharedInstance().registerComponent(tree2);

		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(tree1, DnDConstants.ACTION_COPY, new DragGestureListenerWithImage(new TermLabelDragSourceListener(), tree1));

		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(tree2, DnDConstants.ACTION_COPY, new DragGestureListenerWithImage(new TermLabelDragSourceListener(), tree2));

		// applyDNDHack(tree2);
	}

	/**
	 * Sets the focus concept.
	 *
	 * @param focusConcept the new focus concept
	 */
	public void setFocusConcept(I_GetConceptData focusConcept) {
		DefaultMutableTreeNode tmpRoot = new DefaultMutableTreeNode();

		tree1.setModel(new DefaultTreeModel(tmpRoot));
		tree2.setModel(new DefaultTreeModel(tmpRoot));

		comboBox1.addItem(focusConcept);
		comboBox1.setSelectedItem(focusConcept);
		// tree1.setModel(getParentsTreeModel(focusConcept));
		// tree2.setModel(getChildrenTreeModel(focusConcept));

	}

	/**
	 * Combo box1 action performed.
	 *
	 * @param e the e
	 */
	private void comboBox1ActionPerformed(ActionEvent e) {
		tree1.setModel(getParentsTreeModel((I_GetConceptData) comboBox1.getSelectedItem()));
		tree2.setModel(getChildrenTreeModel((I_GetConceptData) comboBox1.getSelectedItem()));
	}

	/**
	 * The listener interface for receiving termLabelDragSource events.
	 * The class that is interested in processing a termLabelDragSource
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addTermLabelDragSourceListener<code> method. When
	 * the termLabelDragSource event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see TermLabelDragSourceEvent
	 */
	private class TermLabelDragSourceListener implements DragSourceListener {

		/* (non-Javadoc)
		 * @see java.awt.dnd.DragSourceListener#dragDropEnd(java.awt.dnd.DragSourceDropEvent)
		 */
		public void dragDropEnd(DragSourceDropEvent dsde) {
			// TODO Auto-generated method stub
		}

		/* (non-Javadoc)
		 * @see java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.DragSourceDragEvent)
		 */
		public void dragEnter(DragSourceDragEvent dsde) {
			// TODO Auto-generated method stub
		}

		/* (non-Javadoc)
		 * @see java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent)
		 */
		public void dragExit(DragSourceEvent dse) {
			// TODO Auto-generated method stub
		}

		/* (non-Javadoc)
		 * @see java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent)
		 */
		public void dragOver(DragSourceDragEvent dsde) {
			// TODO Auto-generated method stub
		}

		/* (non-Javadoc)
		 * @see java.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.DragSourceDragEvent)
		 */
		public void dropActionChanged(DragSourceDragEvent dsde) {
			// TODO Auto-generated method stub
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
		 * @param dsl the dsl
		 * @param jTree the j tree
		 */
		public DragGestureListenerWithImage(DragSourceListener dsl, JTree jTree) {

			super();
			this.jTree = jTree;
			this.dsl = dsl;
		}

		/* (non-Javadoc)
		 * @see java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.DragGestureEvent)
		 */
		public void dragGestureRecognized(DragGestureEvent dge) {
			int selRow = jTree.getRowForLocation(dge.getDragOrigin().x, dge.getDragOrigin().y);
			TreePath path = jTree.getPathForLocation(dge.getDragOrigin().x, dge.getDragOrigin().y);
			if (selRow != -1) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				try {
					TreeObj To = (TreeObj) node.getUserObject();
					Object objvalue = To.getAtrValue();
					if (objvalue instanceof I_GetConceptData) {
						I_GetConceptData obj = (I_GetConceptData) objvalue;
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
		 * @param obj the obj
		 * @return the transferable
		 * @throws TerminologyException the terminology exception
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		private Transferable getTransferable(I_GetConceptData obj) throws TerminologyException, IOException {
			return new ConceptTransferable(Terms.get().getConcept(obj.getConceptNid()));
		}

		/**
		 * Gets the drag image.
		 *
		 * @param obj the obj
		 * @return the drag image
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public Image getDragImage(I_GetConceptData obj) throws IOException {

			I_DescriptionTuple desc = obj.getDescTuple(config.getTreeDescPreferenceList(), config);
			if (desc == null) {
				desc = obj.getDescriptions().iterator().next().getFirstTuple();
			}
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
		}
	}

	/**
	 * The Class ObjectTransferHandler.
	 */
	class ObjectTransferHandler extends TransferHandler {
		
		/* (non-Javadoc)
		 * @see javax.swing.TransferHandler#importData(javax.swing.JComponent, java.awt.datatransfer.Transferable)
		 */
		public boolean importData(JComponent c, Transferable t) {
			if (canImport(c, t.getTransferDataFlavors())) {
				try {
					DataFlavor conceptBeanFlavor;
					conceptBeanFlavor = TerminologyTransferHandler.conceptBeanFlavor;

					if (hasConceptBeanFlavor(t.getTransferDataFlavors(), conceptBeanFlavor)) {

						I_GetConceptData concept = (I_GetConceptData) t.getTransferData(conceptBeanFlavor);
						comboBox1.addItem(concept);
						comboBox1.setSelectedItem(concept);
					}
					return true;
				} catch (UnsupportedFlavorException ufe) {
					System.out.println("importData: unsupported data flavor");
					ufe.printStackTrace();
				} catch (IOException ioe) {
					System.out.println("importData: I/O exception");
					ioe.printStackTrace();
				}
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see javax.swing.TransferHandler#createTransferable(javax.swing.JComponent)
		 */
		protected Transferable createTransferable(JComponent c) {
			if (c instanceof JTree) {
				JTree tree = (JTree) c;
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

				Object objvalue = node.getUserObject();
				I_GetConceptData concept = null;
				if (objvalue instanceof TreeObj) {
					TreeObj To = (TreeObj) objvalue;
					if (To.getAtrValue() != null)
						concept = (I_GetConceptData) To.getAtrValue();
				} else if (objvalue instanceof I_GetConceptData) {
					concept = (I_GetConceptData) objvalue;
				}
				if (concept != null) {
					return new ConceptTransferable(concept);
				}
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent)
		 */
		public int getSourceActions(JComponent c) {
			return COPY_OR_MOVE;
		}

		/* (non-Javadoc)
		 * @see javax.swing.TransferHandler#exportDone(javax.swing.JComponent, java.awt.datatransfer.Transferable, int)
		 */
		protected void exportDone(JComponent c, Transferable data, int action) {
			if (action == MOVE) {
				// not used
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent, java.awt.datatransfer.DataFlavor[])
		 */
		public boolean canImport(JComponent c, DataFlavor[] flavors) {
			if (c.isEnabled() && c instanceof JComboBox) {
				DataFlavor conceptBeanFlavor;
				conceptBeanFlavor = TerminologyTransferHandler.conceptBeanFlavor;

				if (hasConceptBeanFlavor(flavors, conceptBeanFlavor)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Checks for concept bean flavor.
		 *
		 * @param flavors the flavors
		 * @param conceptBeanFlavor the concept bean flavor
		 * @return true, if successful
		 */
		private boolean hasConceptBeanFlavor(DataFlavor[] flavors, DataFlavor conceptBeanFlavor) {
			for (int i = 0; i < flavors.length; i++) {
				if (conceptBeanFlavor.equals(flavors[i])) {
					return true;
				}
			}

			return false;
		}
	}

	/**
	 * Gets the children.
	 *
	 * @param concept the concept
	 * @return the children
	 */
	private List<? extends I_RelTuple> getChildren(I_GetConceptData concept) {
		List<? extends I_RelTuple> children = new ArrayList<I_RelTuple>();
		try {
			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			if (rInfer.isSelected()) {
				children = concept.getDestRelTuples(allowedStatus, allowedDestRelTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy(), config
						.getClassifierConcept().getNid(), RelAssertionType.INFERRED);
			} else {
				children = concept.getDestRelTuples(allowedStatus, allowedDestRelTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy(), config
						.getClassifierConcept().getNid(), RelAssertionType.STATED);
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return children;
	}

	/**
	 * Gets the children tree model.
	 *
	 * @param concept the concept
	 * @return the children tree model
	 */
	private DefaultTreeModel getChildrenTreeModel(I_GetConceptData concept) {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Root");
		if (concept != null) {
			for (I_RelTuple child : getChildren(concept)) {
				DefaultMutableTreeNode newChild = null;
				try {
					I_GetConceptData childCpt = Terms.get().getConcept(child.getC1Id());
					I_ConceptAttributeTuple attributes = childCpt.getConceptAttributeTuples(config.getPrecedence(), config.getConflictResolutionStrategy()).get(0);
					String sizeStr = "";
					if (getChildren(childCpt).size() > 0) {
						sizeStr = " (" + getChildren(childCpt).size() + ")";
					}
					if (attributes.getStatusNid() == inactive.getNid() || attributes.getStatusNid() == retired.getNid()) {
						newChild = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.INACTIVE), childCpt.toString() + sizeStr, childCpt));
					} else if (attributes.isDefined()) {
						newChild = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.DEFINED), childCpt.toString() + sizeStr, childCpt));
					} else {
						newChild = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.PRIMITIVE), childCpt.toString() + sizeStr, childCpt));
					}

					if (getChildren(childCpt).size() > 0) {
						newChild.add(new DefaultMutableTreeNode("Loading..."));
					}
					top.add(newChild);
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (TerminologyException e1) {
					e1.printStackTrace();
				}
			}
		}

		DefaultTreeModel treeModel = new DefaultTreeModel(top);
		return treeModel;
	}

	/**
	 * Gets the parents.
	 *
	 * @param concept the concept
	 * @return the parents
	 */
	private List<? extends I_RelTuple> getParents(I_GetConceptData concept) {
		List<? extends I_RelTuple> parents = new ArrayList<I_RelTuple>();
		try {
			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			if (rInfer.isSelected()) {
				parents = concept.getSourceRelTuples(allowedStatus, allowedDestRelTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy(), config
						.getClassifierConcept().getNid(), RelAssertionType.INFERRED);
			} else {
				parents = concept.getSourceRelTuples(allowedStatus, allowedDestRelTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy(), config
						.getClassifierConcept().getNid(), RelAssertionType.STATED);
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return parents;
	}

	/**
	 * Gets the parents tree model.
	 *
	 * @param concept the concept
	 * @return the parents tree model
	 */
	private DefaultTreeModel getParentsTreeModel(I_GetConceptData concept) {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Root");
		if (concept != null) {
			for (I_RelTuple parent : getParents(concept)) {
				DefaultMutableTreeNode newParent = null;
				try {
					I_GetConceptData parentCpt = Terms.get().getConcept(parent.getC2Id());
					I_ConceptAttributeTuple attributes = parentCpt.getConceptAttributeTuples(config.getPrecedence(), config.getConflictResolutionStrategy()).get(0);
					String sizeStr = "";
					if (getParents(parentCpt).size() > 0) {
						sizeStr = " (" + getParents(parentCpt).size() + ")";
					}
					if (attributes.getStatusNid() == retired.getNid() || attributes.getStatusNid() == inactive.getNid()) {
						newParent = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.INACTIVE_PARENT), parentCpt.toString() + sizeStr, parentCpt));
					} else if (attributes.isDefined()) {
						newParent = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.DEFINED_PARENT), parentCpt.toString() + sizeStr, parentCpt));
					} else {
						newParent = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.PRIMITIVE_PARENT), parentCpt.toString() + sizeStr, parentCpt));
					}
					if (getParents(parentCpt).size() > 0) {
						newParent.add(new DefaultMutableTreeNode("Loading..."));
					}
					top.add(newParent);
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (TerminologyException e1) {
					e1.printStackTrace();
				}
			}
		}

		DefaultTreeModel treeModel = new DefaultTreeModel(top);
		return treeModel;
	}

	/**
	 * Tree1 tree will expand.
	 *
	 * @param e the e
	 * @throws ExpandVetoException the expand veto exception
	 */
	private void tree1TreeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
		TreePath path = e.getPath();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		if (node.getFirstChild().toString().trim().equals("Loading...")) {
			node.removeAllChildren();
			TreeObj to = (TreeObj) node.getUserObject();
			I_GetConceptData nodeConcept = (I_GetConceptData) to.getAtrValue();
			for (I_RelTuple ancestor : getParents(nodeConcept)) {

				DefaultMutableTreeNode newParent = null;
				try {
					I_GetConceptData parentCpt = Terms.get().getConcept(ancestor.getC2Id());
					I_ConceptAttributeTuple attributes = parentCpt.getConceptAttributeTuples(config.getPrecedence(), config.getConflictResolutionStrategy()).get(0);
					String sizeStr = "";
					if (getParents(parentCpt).size() > 0) {
						sizeStr = " (" + getParents(parentCpt).size() + ")";
					}
					if (attributes.getStatusNid() == retired.getNid() || attributes.getStatusNid() == inactive.getNid()) {
						newParent = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.INACTIVE_PARENT), parentCpt.toString() + sizeStr, parentCpt));
					} else if (attributes.isDefined()) {
						newParent = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.DEFINED_PARENT), parentCpt.toString() + sizeStr, parentCpt));
					} else {
						newParent = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.PRIMITIVE_PARENT), parentCpt.toString() + sizeStr, parentCpt));
					}
					if (getParents(parentCpt).size() > 0) {
						newParent.add(new DefaultMutableTreeNode("Loading..."));
					}
					node.add(newParent);
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (TerminologyException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * Tree2 tree will expand.
	 *
	 * @param e the e
	 * @throws ExpandVetoException the expand veto exception
	 */
	private void tree2TreeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
		TreePath path = e.getPath();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		if (node.getFirstChild().toString().trim().equals("Loading...")) {
			node.removeAllChildren();
			TreeObj to = (TreeObj) node.getUserObject();
			I_GetConceptData nodeConcept = (I_GetConceptData) to.getAtrValue();
			for (I_RelTuple descendant : getChildren(nodeConcept)) {
				DefaultMutableTreeNode newChild = null;
				try {

					I_GetConceptData childCpt = Terms.get().getConcept(descendant.getC1Id());
					I_ConceptAttributeTuple attributes = childCpt.getConceptAttributeTuples(config.getPrecedence(), config.getConflictResolutionStrategy()).get(0);
					String sizeStr = "";
					if (getChildren(childCpt).size() > 0) {
						sizeStr = " (" + getChildren(childCpt).size() + ")";
					}
					if (attributes.getStatusNid() == retired.getNid() || attributes.getStatusNid() == inactive.getNid()) {
						newChild = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.INACTIVE), childCpt.toString() + sizeStr, childCpt));
					} else if (attributes.isDefined()) {
						newChild = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.DEFINED), childCpt.toString() + sizeStr, childCpt));
					} else {
						newChild = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.PRIMITIVE), childCpt.toString() + sizeStr, childCpt));
					}
					if (getChildren(childCpt).size() > 0) {
						newChild.add(new DefaultMutableTreeNode("Loading..."));
					}
					node.add(newChild);
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (TerminologyException e1) {
					e1.printStackTrace();
				}

			}
		}
	}

	/**
	 * Sets the container panel.
	 *
	 * @param containerPanel the new container panel
	 */
	public void setContainerPanel(JTabbedPane containerPanel) {
		this.containerPanel = containerPanel;
	}

	/**
	 * Expand button action performed.
	 *
	 * @param e the e
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
			tree1.setModel(getParentsTreeModel((I_GetConceptData) comboBox1.getSelectedItem()));
			tree2.setModel(getChildrenTreeModel((I_GetConceptData) comboBox1.getSelectedItem()));
		}
	}

	/**
	 * R stat action performed.
	 */
	private void rStatActionPerformed() {
		if (comboBox1.getSelectedItem() != null) {
			tree1.setModel(getParentsTreeModel((I_GetConceptData) comboBox1.getSelectedItem()));
			tree2.setModel(getChildrenTreeModel((I_GetConceptData) comboBox1.getSelectedItem()));
		}
	}

	//
	// public static void applyDNDHack(JTree tree){
	// MouseListener dragListener = null;
	//
	// // the default dnd implemntation requires to first select node and then
	// drag
	// try{
	// Class clazz =
	// Class.forName("javax.swing.plaf.basic.BasicDragGestureRecognizer");
	// MouseListener[] mouseListeners = tree.getMouseListeners();
	// for(int i = 0; i<mouseListeners.length; i++){
	// if(clazz.isAssignableFrom(mouseListeners[i].getClass())){
	// dragListener = mouseListeners[i];
	// break;
	// }
	// }
	//
	// if(dragListener!=null){
	// tree.removeMouseListener(dragListener);
	// tree.removeMouseMotionListener((MouseMotionListener)dragListener);
	// tree.addMouseListener(dragListener);
	// tree.addMouseMotionListener((MouseMotionListener)dragListener);
	// }
	// } catch(ClassNotFoundException e){
	// e.printStackTrace();
	// }
	// }
	/**
	 * The Class HierarchyIconRenderer.
	 */
	class HierarchyIconRenderer extends DefaultTreeCellRenderer {

		/* (non-Javadoc)
		 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
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
					switch (type) {
					case IconUtilities.DEFINED:
						label.setToolTipText("Fully defined descendant");
						break;
					case IconUtilities.PRIMITIVE:
						label.setToolTipText("Primitive descendant");
						break;
					case IconUtilities.INACTIVE:
						label.setToolTipText("Inactive descendant");
						break;
					case IconUtilities.PRIMITIVE_PARENT:
						label.setToolTipText("Primitive ancestor");
						break;
					case IconUtilities.DEFINED_PARENT:
						label.setToolTipText("Fully defined ancestor");
						break;
					case IconUtilities.INACTIVE_PARENT:
						label.setToolTipText("Inactive ancestor");
						break;
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

		//======== this ========
		setPreferredSize(new Dimension(10, 20));
		setMinimumSize(new Dimension(10, 20));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {424, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0, 0.0, 1.0E-4};

		//======== scrollPane1 ========
		{

			//---- tree1 ----
			tree1.setVisibleRowCount(5);
			tree1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			tree1.addTreeWillExpandListener(new TreeWillExpandListener() {
				@Override
				public void treeWillExpand(TreeExpansionEvent e)
					throws ExpandVetoException
				{
					tree1TreeWillExpand(e);
				}
				@Override
				public void treeWillCollapse(TreeExpansionEvent e)
					throws ExpandVetoException
				{}
			});
			scrollPane1.setViewportView(tree1);
		}
		add(scrollPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel1 ========
		{
			panel1.setMinimumSize(new Dimension(10, 50));
			panel1.setPreferredSize(new Dimension(10, 50));
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {197, 0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

			//---- comboBox1 ----
			comboBox1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			comboBox1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					comboBox1ActionPerformed(e);
				}
			});
			panel1.add(comboBox1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

			//======== panel2 ========
			{
				panel2.setMinimumSize(new Dimension(10, 23));
				panel2.setPreferredSize(new Dimension(10, 23));
				panel2.setLayout(new GridBagLayout());
				((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
				((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

				//---- rInfer ----
				rInfer.setText("Inferred");
				rInfer.setSelected(true);
				rInfer.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				rInfer.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						radioButton1ActionPerformed();
					}
				});
				panel2.add(rInfer, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- rStat ----
				rStat.setText("Stated");
				rStat.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				rStat.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						rStatActionPerformed();
					}
				});
				panel2.add(rStat, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- expandButton ----
				expandButton.setText("Expand");
				expandButton.setMnemonic('E');
				expandButton.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				expandButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						expandButtonActionPerformed(e);
					}
				});
				panel2.add(expandButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			panel1.add(panel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== scrollPane2 ========
		{

			//---- tree2 ----
			tree2.setVisibleRowCount(5);
			tree2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			tree2.addTreeWillExpandListener(new TreeWillExpandListener() {
				@Override
				public void treeWillExpand(TreeExpansionEvent e)
					throws ExpandVetoException
				{
					tree2TreeWillExpand(e);
				}
				@Override
				public void treeWillCollapse(TreeExpansionEvent e)
					throws ExpandVetoException
				{}
			});
			scrollPane2.setViewportView(tree2);
		}
		add(scrollPane2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//---- buttonGroup1 ----
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
