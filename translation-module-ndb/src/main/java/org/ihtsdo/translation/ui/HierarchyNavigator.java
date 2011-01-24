/*
 * Created by JFormDesigner on Mon Aug 31 13:11:07 ART 2009
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

/**
 * @author Guillermo Reynoso
 */
public class HierarchyNavigator extends JPanel {
	public I_ConfigAceFrame config;
	private I_IntSet allowedDestRelTypes;
	private I_IntSet allowedStatus;
	private I_GetConceptData inactive;
	private I_GetConceptData retired;
	private JTabbedPane containerPanel;
	public HierarchyNavigator() {
		initComponents();

		comboBox1.setTransferHandler(new ObjectTransferHandler());

		tree1.setCellRenderer(new HierarchyIconRenderer());
		tree2.setCellRenderer(new HierarchyIconRenderer());
		I_TermFactory termFactory = Terms.get();
		allowedDestRelTypes =  termFactory.newIntSet();
		allowedStatus =  termFactory.newIntSet();
		try {
			config=Terms.get().getActiveAceFrameConfig();
			//			allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
			allowedDestRelTypes=config.getDestRelTypes();
			//			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			allowedStatus=config.getAllowedStatus();

			inactive =Terms.get().getConcept(ArchitectonicAuxiliary.Concept.INACTIVE.getUids());
			retired =Terms.get().getConcept(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
			//			allowedStatus.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			//			allowedStatus.add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			//			config.setAllowedStatus(allowedStatus);
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		tree1.setTransferHandler(new ObjectTransferHandler());
		tree1.setRootVisible(false);
		tree1.setShowsRootHandles(true);
		tree1.setDragEnabled(true);
		tree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		ToolTipManager.sharedInstance().registerComponent(tree1);

		//		applyDNDHack(tree1);
		tree2.setTransferHandler(new ObjectTransferHandler());
		tree2.setRootVisible(false);
		tree2.setShowsRootHandles(true);
		tree2.setDragEnabled(true);
		tree2.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		ToolTipManager.sharedInstance().registerComponent(tree2);


		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(tree1, DnDConstants.ACTION_COPY,
				new DragGestureListenerWithImage(new TermLabelDragSourceListener(),tree1));


		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(tree2, DnDConstants.ACTION_COPY,
				new DragGestureListenerWithImage(new TermLabelDragSourceListener(),tree2));

		//		applyDNDHack(tree2);
	}
	public void setFocusConcept(I_GetConceptData focusConcept){
		comboBox1.addItem(focusConcept);
		comboBox1.setSelectedItem(focusConcept);
		//		tree1.setModel(getParentsTreeModel(focusConcept));
		//		tree2.setModel(getChildrenTreeModel(focusConcept));

	}
	private void comboBox1ActionPerformed(ActionEvent e) {
		tree1.setModel(getParentsTreeModel((I_GetConceptData) comboBox1.getSelectedItem()));
		tree2.setModel(getChildrenTreeModel((I_GetConceptData) comboBox1.getSelectedItem()));
	}

	private class TermLabelDragSourceListener implements DragSourceListener {

		public void dragDropEnd(DragSourceDropEvent dsde) {
			// TODO Auto-generated method stub
		}

		public void dragEnter(DragSourceDragEvent dsde) {
			// TODO Auto-generated method stub
		}

		public void dragExit(DragSourceEvent dse) {
			// TODO Auto-generated method stub
		}

		public void dragOver(DragSourceDragEvent dsde) {
			// TODO Auto-generated method stub
		}

		public void dropActionChanged(DragSourceDragEvent dsde) {
			// TODO Auto-generated method stub
		}
	}

	private class DragGestureListenerWithImage implements DragGestureListener {

		DragSourceListener dsl;
		JTree jTree;

		public DragGestureListenerWithImage(DragSourceListener dsl, JTree jTree) {

			super();
			this.jTree=jTree;
			this.dsl = dsl;
		}

		public void dragGestureRecognized(DragGestureEvent dge) {
			int selRow = jTree.getRowForLocation(dge.getDragOrigin().x, dge.getDragOrigin().y);
			TreePath path = jTree.getPathForLocation(dge.getDragOrigin().x, dge.getDragOrigin().y);
			if (selRow != -1) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				try {
					TreeObj To=(TreeObj)node.getUserObject();
					Object objvalue=To.getAtrValue();
					if (objvalue instanceof I_GetConceptData){
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

		private Transferable getTransferable(I_GetConceptData obj) throws TerminologyException, IOException {
			return new ConceptTransferable(Terms.get().getConcept(obj.getConceptNid()));
		}

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
			FilteredImageSource fis = new FilteredImageSource(dragImage.getSource(),
					TermLabelMaker.getTransparentFilter());
			dragImage = Toolkit.getDefaultToolkit().createImage(fis);
			return dragImage;
		}
	}
	class ObjectTransferHandler extends TransferHandler {
		public boolean importData(JComponent c, Transferable t) {
			if (canImport(c, t.getTransferDataFlavors())) {
				try {    			    	
					DataFlavor conceptBeanFlavor;
					conceptBeanFlavor = TerminologyTransferHandler.conceptBeanFlavor;


					if (hasConceptBeanFlavor(t.getTransferDataFlavors(), conceptBeanFlavor)) {

						I_GetConceptData concept=(I_GetConceptData)t.getTransferData(conceptBeanFlavor);
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
		protected Transferable createTransferable(JComponent c) {
			if (c instanceof JTree) {
				JTree tree = (JTree) c;
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				I_GetConceptData concept=(I_GetConceptData) node.getUserObject();

				return new ConceptTransferable(concept);
			}
			return null;
		}
		public int getSourceActions(JComponent c) {
			return COPY_OR_MOVE;
		}
		protected void exportDone(JComponent c, Transferable data, int action) {
			if (action == MOVE) {
				// not used
			}
		}
		public boolean canImport(JComponent c, DataFlavor[] flavors) {
			if (c.isEnabled() && c instanceof JComboBox) {   			    	
				DataFlavor conceptBeanFlavor;
				conceptBeanFlavor =TerminologyTransferHandler.conceptBeanFlavor;

				if (hasConceptBeanFlavor(flavors,conceptBeanFlavor )) {
					return true;
				}
			}
			return false;
		}
		private boolean hasConceptBeanFlavor(DataFlavor[] flavors,DataFlavor conceptBeanFlavor) {
			for (int i = 0; i < flavors.length; i++) {
				if (conceptBeanFlavor.equals(flavors[i])) {
					return true;
				}
			}

			return false;
		}
	}

	private List<? extends I_RelTuple> getChildren(I_GetConceptData concept) {
		List<? extends I_RelTuple> children = new ArrayList<I_RelTuple>();
		try {
			I_TermFactory termFactory = Terms.get(); 
			I_ConfigAceFrame config=termFactory.getActiveAceFrameConfig();
			if (rInfer.isSelected()){
				children =  concept.getDestRelTuples(allowedStatus, allowedDestRelTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy(),config.getClassifierConcept().getNid(), RelAssertionType.INFERRED);
			}else{
				children =  concept.getDestRelTuples(allowedStatus, allowedDestRelTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy(),config.getClassifierConcept().getNid(), RelAssertionType.STATED);
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return children;
	}

	private DefaultTreeModel getChildrenTreeModel(I_GetConceptData concept) {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Root");
		if (concept != null) {
			for (I_RelTuple child : getChildren(concept)) {
				DefaultMutableTreeNode newChild=null;
				try {
					 I_GetConceptData childCpt = Terms.get().getConcept( child.getC1Id());
					I_ConceptAttributeTuple attributes = childCpt.getConceptAttributeTuples(config.getPrecedence(), config.getConflictResolutionStrategy()).get(0);
					if (attributes.getStatusNid()==retired.getNid() ||attributes.getStatusNid()==inactive.getNid()){
						newChild = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.INACTIVE),childCpt.toString() + " (" + getChildren(childCpt).size() + ")",childCpt));
					}else if (attributes.isDefined()) {
						newChild = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.DEFINED),childCpt.toString() + " (" + getChildren(childCpt).size() + ")",childCpt));
					} else {
						newChild = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.PRIMITIVE),childCpt.toString() + " (" + getChildren(childCpt).size() + ")",childCpt));
					}
					newChild.add(new DefaultMutableTreeNode("Loading..."));
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

	private List<? extends I_RelTuple> getParents(I_GetConceptData concept) {
		List<? extends I_RelTuple> parents = new ArrayList<I_RelTuple>();
		try {
			I_TermFactory termFactory = Terms.get(); 
			I_ConfigAceFrame config=termFactory.getActiveAceFrameConfig();
			if (rInfer.isSelected()){
				parents =  concept.getSourceRelTuples(allowedStatus ,allowedDestRelTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy(),config.getClassifierConcept().getNid(),RelAssertionType.INFERRED);
			}else{
				parents =  concept.getSourceRelTuples(allowedStatus ,allowedDestRelTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy(),config.getClassifierConcept().getNid(),RelAssertionType.STATED);
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return parents;
	}

	private DefaultTreeModel getParentsTreeModel(I_GetConceptData concept) {
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Root");
		if (concept != null) {
			for (I_RelTuple parent : getParents(concept)) {
				DefaultMutableTreeNode newParent=null;
				try {
					 I_GetConceptData parentCpt = Terms.get().getConcept( parent.getC2Id());
					I_ConceptAttributeTuple attributes = parentCpt.getConceptAttributeTuples(config.getPrecedence(), config.getConflictResolutionStrategy()).get(0);
					if (attributes.getStatusNid()==retired.getNid() ||attributes.getStatusNid()==inactive.getNid()){
						newParent = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.INACTIVE_PARENT),parentCpt.toString() + " (" + getParents(parentCpt).size() + ")",parentCpt));
					}else if (attributes.isDefined()) {
						newParent = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.DEFINED_PARENT),parentCpt.toString() + " (" + getParents(parentCpt).size() + ")",parentCpt));
					} else {
						newParent = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.PRIMITIVE_PARENT),parentCpt.toString() + " (" + getParents(parentCpt).size() + ")",parentCpt));
					}
					newParent.add(new DefaultMutableTreeNode("Loading..."));
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

	private void tree1TreeWillExpand(TreeExpansionEvent e)
	throws ExpandVetoException
	{
		TreePath path = e.getPath();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		if (node.getFirstChild().toString().trim().equals("Loading...")) {
			node.removeAllChildren();
			TreeObj to = (TreeObj) node.getUserObject();
			I_GetConceptData nodeConcept = (I_GetConceptData) to.getAtrValue();
			for (I_RelTuple ancestor : getParents(nodeConcept)) {

				DefaultMutableTreeNode newParent=null;
				try {
					 I_GetConceptData parentCpt = Terms.get().getConcept( ancestor.getC2Id());
					I_ConceptAttributeTuple attributes = parentCpt.getConceptAttributeTuples(config.getPrecedence(), config.getConflictResolutionStrategy()).get(0);
					if (attributes.getStatusNid()==retired.getNid() ||attributes.getStatusNid()==inactive.getNid()){
						newParent = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.INACTIVE_PARENT),parentCpt.toString() + " (" + getParents(parentCpt).size() + ")",parentCpt));
					}else if (attributes.isDefined()) {
						newParent = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.DEFINED_PARENT),parentCpt.toString() + " (" + getParents(parentCpt).size() + ")",parentCpt));
					} else {
						newParent = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.PRIMITIVE_PARENT),parentCpt.toString() + " (" + getParents(parentCpt).size() + ")",parentCpt));
					}
					newParent.add(new DefaultMutableTreeNode("Loading..."));
					node.add(newParent);
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (TerminologyException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private void tree2TreeWillExpand(TreeExpansionEvent e)
	throws ExpandVetoException
	{
		TreePath path = e.getPath();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		if (node.getFirstChild().toString().trim().equals("Loading...")) {
			node.removeAllChildren();
			TreeObj to = (TreeObj) node.getUserObject();
			I_GetConceptData nodeConcept = (I_GetConceptData) to.getAtrValue();
			for (I_RelTuple descendant : getChildren(nodeConcept)) {
				DefaultMutableTreeNode newChild=null;
				try {

					 I_GetConceptData childCpt = Terms.get().getConcept( descendant.getC1Id());
					I_ConceptAttributeTuple attributes = childCpt.getConceptAttributeTuples(config.getPrecedence(), config.getConflictResolutionStrategy()).get(0);
					if (attributes.getStatusNid()==retired.getNid() ||attributes.getStatusNid()==inactive.getNid()){
						newChild = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.INACTIVE),childCpt.toString() + " (" + getChildren(childCpt).size() + ")",childCpt));
					}else if (attributes.isDefined()) {
						newChild = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.DEFINED),childCpt.toString() + " (" + getChildren(childCpt).size() + ")",childCpt));
					} else {
						newChild = new DefaultMutableTreeNode(new TreeObj(String.valueOf(IconUtilities.PRIMITIVE),childCpt.toString() + " (" + getChildren(childCpt).size() + ")",childCpt));
					}
					newChild.add(new DefaultMutableTreeNode("Loading..."));
					node.add(newChild);
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (TerminologyException e1) {
					e1.printStackTrace();
				}

			}	
		}
	}

	public void setContainerPanel(JTabbedPane containerPanel) {
		this.containerPanel = containerPanel;
	}
	
	private void expandButtonActionPerformed(ActionEvent e) {
		
		JPanel similarityPanel = (JPanel)containerPanel.getSelectedComponent();
		final int indx = containerPanel.getSelectedIndex();
		expandButton.setVisible(false);
		final JDialog similarityDialog = new JDialog();
		similarityDialog.setContentPane(similarityPanel);
		similarityDialog.setModal(true);
		similarityDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		similarityDialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				expandButton.setVisible(true);
				containerPanel.insertTab("Hierarchy", null, similarityDialog.getContentPane(), "Hierarchy", indx);
				containerPanel.setSelectedIndex(indx);
				similarityDialog.dispose();
			}
		});
		similarityDialog.setSize(new Dimension(700,550));
		Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension ventana = similarityDialog.getSize();
        similarityDialog.setLocation(
            (pantalla.width - ventana.width) / 2,
            (pantalla.height - ventana.height) / 2);

		similarityDialog.setVisible(true);
	}

	private void radioButton1ActionPerformed() {
		if (comboBox1.getSelectedItem()!=null){
			tree1.setModel(getParentsTreeModel((I_GetConceptData) comboBox1.getSelectedItem()));
			tree2.setModel(getChildrenTreeModel((I_GetConceptData) comboBox1.getSelectedItem()));
		}
	}

	private void rStatActionPerformed() {
		if (comboBox1.getSelectedItem()!=null){
			tree1.setModel(getParentsTreeModel((I_GetConceptData) comboBox1.getSelectedItem()));
			tree2.setModel(getChildrenTreeModel((I_GetConceptData) comboBox1.getSelectedItem()));
		}
	}
	//
	//	public static void applyDNDHack(JTree tree){ 
	//		MouseListener dragListener = null; 
	//
	//		// the default dnd implemntation requires to first select node and then drag 
	//		try{ 
	//			Class clazz = Class.forName("javax.swing.plaf.basic.BasicDragGestureRecognizer"); 
	//			MouseListener[] mouseListeners = tree.getMouseListeners(); 
	//			for(int i = 0; i<mouseListeners.length; i++){ 
	//				if(clazz.isAssignableFrom(mouseListeners[i].getClass())){ 
	//					dragListener = mouseListeners[i]; 
	//					break; 
	//				} 
	//			} 
	//
	//			if(dragListener!=null){ 
	//				tree.removeMouseListener(dragListener); 
	//				tree.removeMouseMotionListener((MouseMotionListener)dragListener); 
	//				tree.addMouseListener(dragListener); 
	//				tree.addMouseMotionListener((MouseMotionListener)dragListener); 
	//			} 
	//		} catch(ClassNotFoundException e){ 
	//			e.printStackTrace();    
	//		} 
	//	} 
	class HierarchyIconRenderer extends DefaultTreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(
				JTree tree,
				Object value,
				boolean sel,
				boolean expanded,
				boolean leaf,
				int row,
				boolean hasFocus) {

			JLabel label=(JLabel)super.getTreeCellRendererComponent(
					tree, value, sel,
					expanded, leaf, row,
					hasFocus);
			DefaultMutableTreeNode node =  (DefaultMutableTreeNode)value;
			if (node!=null && (node.getUserObject() instanceof TreeObj)){
				TreeObj nodeObject = (TreeObj) node.getUserObject();
				if (nodeObject!=null){
					Integer type=Integer.parseInt( nodeObject.getObjType());
					label.setIcon(IconUtilities.getIconForConceptDetails(type));
					switch (type){
					case IconUtilities.DEFINED: label.setToolTipText("Fully defined descendant");break; 
					case IconUtilities.PRIMITIVE: label.setToolTipText("Primitive descendant");break;
					case IconUtilities.INACTIVE: label.setToolTipText("Inactive descendant");break;
					case IconUtilities.PRIMITIVE_PARENT: label.setToolTipText("Primitive ancestor");break; 
					case IconUtilities.DEFINED_PARENT: label.setToolTipText("Fully defined ancestor");break;
					case IconUtilities.INACTIVE_PARENT: label.setToolTipText("Inactive ancestor");break;
					}
				}
			}
			return label;
		}

	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
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
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {424, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0, 0.0, 1.0E-4};

		//======== scrollPane1 ========
		{

			//---- tree1 ----
			tree1.setVisibleRowCount(5);
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
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {197, 0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

			//---- comboBox1 ----
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
				panel2.setLayout(new GridBagLayout());
				((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
				((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

				//---- rInfer ----
				rInfer.setText("Inferred");
				rInfer.setSelected(true);
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
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JScrollPane scrollPane1;
	private JTree tree1;
	private JPanel panel1;
	private JComboBox comboBox1;
	private JPanel panel2;
	private JRadioButton rInfer;
	private JRadioButton rStat;
	private JButton expandButton;
	private JScrollPane scrollPane2;
	private JTree tree2;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	
}
