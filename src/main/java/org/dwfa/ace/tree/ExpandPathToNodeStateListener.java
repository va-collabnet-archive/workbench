package org.dwfa.ace.tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.log.AceLog;
import org.dwfa.vodb.types.ConceptBean;

public class ExpandPathToNodeStateListener implements ChangeListener{

	private JTreeWithDragImage tree;
	private ArrayList<I_GetConceptData> ancestors;
	private TreePath lastPath;
	private DefaultMutableTreeNode lastChildNode;
	I_GetConceptData focus;

	
	
	public ExpandPathToNodeStateListener(JTreeWithDragImage tree, I_ConfigAceFrame config, I_GetConceptData focus) throws IOException {
		super();
		this.tree = tree;
		this.focus = focus;
		config.getParentExpandedNodes().clear();
		config.getChildrenExpandedNodes().clear();
		tree.addWorkerFinishedListener(this);
		ancestors = new ArrayList<I_GetConceptData>();
		ancestors.add(focus);
		List<I_RelTuple> rels = focus.getSourceRelTuples(config.getAllowedStatus(), config.getDestRelTypes(), 
				config.getViewPositionSet(), true);
		while (rels.size() > 0) {
			for (I_RelTuple r: rels) {
				ConceptBean parent = ConceptBean.get(r.getC2Id());
				ancestors.add(0, parent);
				AceLog.getAppLog().info("Adding parent: " + parent);
				rels = parent.getSourceRelTuples(config.getAllowedStatus(), config.getDestRelTypes(), 
						config.getViewPositionSet(), true);
				break;
			}
		}
		
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
		for (int i = 0; i < rootNode.getChildCount(); i++) {
			DefaultMutableTreeNode termRoot = (DefaultMutableTreeNode) rootNode.getChildAt(i);
			tree.collapsePath(new TreePath(termRoot.getPath()));
		}
		for (I_GetConceptData child: ancestors) {
			for (int i = 0; i < rootNode.getChildCount(); i++) {
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
				ConceptBeanForTree dbft = (ConceptBeanForTree) childNode.getUserObject();
				if (dbft.getCoreBean().equals(child)) {
					lastPath = new TreePath(childNode.getPath());
					if (child.equals(focus)) {
						tree.collapsePath(lastPath);
					} else {
						tree.expandPath(lastPath);
					}
					rootNode = childNode;
					break;
				}
			}
		}
		
		//TreePath pathToShow;
		//tree.expandPath(pathToShow);
		
		AceLog.getAppLog().info("Dropped on JTreeWithDragImage: " + focus);
		AceLog.getAppLog().info("Expansion list: " + ancestors);
	}



	public void stateChanged(ChangeEvent e) {
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) tree
				.getModel().getRoot();

		boolean allFound = true;
		for (I_GetConceptData child : ancestors) {
			boolean found = false;
			for (int i = 0; i < rootNode.getChildCount(); i++) {
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) rootNode
						.getChildAt(i);
				ConceptBeanForTree dbft = (ConceptBeanForTree) childNode.getUserObject();
				if (dbft.getCoreBean().equals(child)) {
					lastChildNode = childNode;
					lastPath = new TreePath(childNode.getPath());
					if (dbft.getCoreBean().equals(focus)) {
						tree.collapsePath(lastPath);
					} else {
						tree.expandPath(lastPath);
					}
					tree.scrollPathToVisible(lastPath);
					rootNode = childNode;
					found = true;
					break;
				}
			}
			if (found == false) {
				allFound = false;
			} 
		}
		if (allFound) {
			tree.removeWorkerFinishedListener(this);
			tree.setSelectionPath(lastPath);
			tree.scrollPathToVisible(lastPath);
			tree.scrollPathToVisible(lastPath);
			
			int sibCount = 0;
			DefaultMutableTreeNode sibling = lastChildNode.getNextSibling();
			while (sibling != null && sibCount < 4) {
				TreePath siblingPath = new TreePath(sibling.getPath());
				tree.collapsePath(siblingPath);
				tree.scrollPathToVisible(siblingPath);
				sibling = sibling.getNextSibling();
				sibCount++;
			}
		}
	}
}
