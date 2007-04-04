package org.dwfa.ace.tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.dwfa.ace.AceLog;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.vodb.types.ConceptBean;

public class ExpandPathToNodeStateListener implements ChangeListener{

	private JTreeWithDragImage tree;
	private ArrayList<I_GetConceptData> ancestors;
	private TreePath lastPath;
	private DefaultMutableTreeNode lastChildNode;

	
	
	public ExpandPathToNodeStateListener(JTreeWithDragImage tree, I_ConfigAceFrame config, I_GetConceptData focus) throws IOException {
		super();
		this.tree = tree;
		tree.addWorkerFinishedListener(this);
		ancestors = new ArrayList<I_GetConceptData>();
		ancestors.add(focus);
		List<I_RelTuple> rels = focus.getSourceRelTuples(config.getAllowedStatus(), config.getDestRelTypes(), 
				config.getViewPositionSet(), true);
		while (rels.size() > 0) {
			for (I_RelTuple r: rels) {
				ConceptBean parent = ConceptBean.get(r.getC2Id());
				ancestors.add(0, parent);
				AceLog.info("Adding parent: " + parent);
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
		AceLog.info("All should be collapsed.");
		
		for (I_GetConceptData child: ancestors) {
			boolean found = false;
			for (int i = 0; i < rootNode.getChildCount(); i++) {
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
				ConceptBeanForTree dbft = (ConceptBeanForTree) childNode.getUserObject();
				if (dbft.getCoreBean().equals(child)) {
					lastPath = new TreePath(childNode.getPath());
					tree.expandPath(lastPath);
					rootNode = childNode;
					found = true;
					break;
				}
			}
			if (found == false) {
				AceLog.info("Could not find node for: " + child);
			}
		}
		
		//TreePath pathToShow;
		//tree.expandPath(pathToShow);
		
		AceLog.info("Dropped on JTreeWithDragImage: " + focus);
		AceLog.info("Expansion list: " + ancestors);
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
					tree.expandPath(lastPath);
					rootNode = childNode;
					found = true;
					break;
				}
			}
			if (found == false) {
				AceLog.info("Could not find node for: " + child);
				allFound = false;
			} 
		}
		if (allFound) {
			tree.removeWorkerFinishedListener(this);
			tree.setSelectionPath(lastPath);
			tree.scrollPathToVisible(lastPath);
			int sibCount = 0;
			DefaultMutableTreeNode sibling = lastChildNode.getNextSibling();
			while (sibling != null && sibCount < 4) {
				tree.scrollPathToVisible(new TreePath(sibling.getPath()));
				sibling = sibling.getNextSibling();
				sibCount++;
			}
		}
	}
}
