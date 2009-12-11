/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
		AceLog.getAppLog().info("All should be collapsed.");
		
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
				AceLog.getAppLog().info("Could not find node for: " + child);
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
					tree.expandPath(lastPath);
					rootNode = childNode;
					found = true;
					break;
				}
			}
			if (found == false) {
				AceLog.getAppLog().info("Could not find node for: " + child);
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
