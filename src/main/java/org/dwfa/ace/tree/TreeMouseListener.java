package org.dwfa.ace.tree;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.vodb.types.ThinRelTuple;

import com.sleepycat.je.DatabaseException;

public class TreeMouseListener implements MouseListener {

	private AceFrameConfig aceConfig;

	public TreeMouseListener(AceFrameConfig aceConfig) {
		super();
		this.aceConfig = aceConfig;
	}

	public void mousePressed(MouseEvent e) {
		JTree tree = (JTree) e.getSource();
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		int selRow = tree.getRowForLocation(e.getX(), e.getY());
		//System.out.println("Selected row: " + selRow);
		TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		if (selPath != null) {
			if (selRow != -1) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath
						.getLastPathComponent();
				TermTreeCellRenderer renderer = (TermTreeCellRenderer) tree
						.getCellRenderer();
				I_GetConceptDataForTree treeBean = (I_GetConceptDataForTree) node
						.getUserObject();
				renderer = (TermTreeCellRenderer) renderer
						.getTreeCellRendererComponent(tree, node, true, tree
								.isExpanded(selRow), node.isLeaf(), selRow,
								true);
				Rectangle bounds = tree.getRowBounds(selRow);
				if (e.getClickCount() == 1) {
					Rectangle iconBounds = renderer.getIconRect(treeBean
							.getParentDepth());

					if ((e.getPoint().x > bounds.x + iconBounds.x)
							&& (e.getPoint().x + 1 < bounds.x + iconBounds.x
									+ iconBounds.width)) {
						openOrCloseParent(tree, model, node, treeBean, bounds);
					}
				} else if (e.getClickCount() == 2) {
					openOrCloseParent(tree, model, node, treeBean, bounds);
				}
				//tree.setSelectionPath(new TreePath(selPath.getPath()));
				int newRow = tree.getRowForPath(selPath);
				//System.out.println("New row: " + newRow);
				tree.setSelectionInterval(newRow, newRow);
			}
		}
	}

	private void openOrCloseParent(JTree tree, DefaultTreeModel model, DefaultMutableTreeNode node, I_GetConceptDataForTree treeBean, Rectangle bounds) {
		boolean addNodes = !treeBean.isParentOpened();

		treeBean.setParentOpened(addNodes);
		tree.paintImmediately(bounds);
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node
				.getParent();
		I_GetConceptDataForTree parentBean = (I_GetConceptDataForTree) parentNode
				.getUserObject();
		if (addNodes) {
	        aceConfig.getParentExpandedNodes().add(treeBean.getConceptId());
			List<ThinRelTuple> tuples;
			try {
				tuples = treeBean.getSourceRelTuples(aceConfig
						.getAllowedStatus(), aceConfig
						.getDestRelTypes(), aceConfig
						.getViewPositionSet(), false);
				int[] newNodeIndices = new int[tuples.size()];
				int index = 0;
				int insertIndex = parentNode.getIndex(node);
				for (ThinRelTuple t : tuples) {
					newNodeIndices[index++] = insertIndex;
					
					if (t.getC2Id() == parentBean
							.getConceptId() && treeBean
							.getParentDepth() == 0) {
						System.out.println(" parent depth: " + treeBean
						.getParentDepth());
						continue;
					}
					
					ConceptBeanForTree extraParentBean = ConceptBeanForTree
							.get(t.getC2Id(), treeBean
									.getParentDepth() + 1, true);
					DefaultMutableTreeNode extraParentNode = new DefaultMutableTreeNode(
							extraParentBean);
					extraParentNode.setAllowsChildren(false);
					parentNode.insert(extraParentNode,
							insertIndex++);
					treeBean.getExtraParentNodes().add(
							extraParentNode);
				}
				model.nodesWereInserted(parentNode,
						newNodeIndices);

			} catch (DatabaseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else { // remove nodes
			removeAllExtraParents(model, treeBean, parentNode);
	        aceConfig.getParentExpandedNodes().remove(treeBean.getConceptId());
		}
		model.nodeStructureChanged(parentNode);
	}

	private void removeAllExtraParents(DefaultTreeModel model, I_GetConceptDataForTree treeBean, DefaultMutableTreeNode parentNode) {
		for (DefaultMutableTreeNode extraParentNode : treeBean
				.getExtraParentNodes()) {
			removeAllExtraParents(model, (I_GetConceptDataForTree) extraParentNode.getUserObject(), parentNode);
			int extraParentIndex = parentNode.getIndex(extraParentNode);
			parentNode.remove(extraParentIndex);
			model.nodesWereRemoved(parentNode, new int[] { extraParentIndex }, 
					new Object[] { extraParentNode });
		}
		treeBean.getExtraParentNodes().clear();
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
	}
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
	}


}
