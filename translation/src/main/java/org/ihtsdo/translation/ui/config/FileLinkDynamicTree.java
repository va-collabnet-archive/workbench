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
package org.ihtsdo.translation.ui.config;

import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.FileLink;
import org.ihtsdo.project.FileLinkAPI;

/**
 * The Class FileLinkDynamicTree.
 */
public class FileLinkDynamicTree extends JPanel {
	
	/** The fl api. */
	FileLinkAPI flApi;
	
	/** The root node. */
	protected DefaultMutableTreeNode rootNode;
	
	/** The tree model. */
	protected DefaultTreeModel treeModel;
	
	/** The tree. */
	protected JTree tree;

	/**
	 * Instantiates a new file link dynamic tree.
	 *
	 * @param config the config
	 */
	public FileLinkDynamicTree(I_ConfigAceFrame config) {
		super(new GridLayout(1, 0));
		this.flApi = new FileLinkAPI(config);

		rootNode = new DefaultMutableTreeNode("Root Node");
		treeModel = new DefaultTreeModel(rootNode);
		createNodes(rootNode);
		treeModel.addTreeModelListener(new MyTreeModelListener());
		tree = new JTree(treeModel);
		tree.setEditable(true);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);

		//=================== CONFIG TREE RENDERER CONFIGURATION ===================
		DefaultTreeCellRenderer rend = new DefaultTreeCellRenderer();
		rend.setOpenIcon(null);
		rend.setClosedIcon(null);
		rend.setLeafIcon(null);
		rend.setIconTextGap(-4);
		tree.setCellRenderer(rend);
		tree.setRowHeight(15);
		tree.setRootVisible(false);
		JScrollPane scrollPane = new JScrollPane();
		this.add(scrollPane);
		scrollPane.setViewportView(tree);
		//Selects the first row as default if the tree has elements.
		if(rootNode.getChildCount() != 0){
			tree.setSelectionRow(0);
		}
		
	}
	
	/**
	 * Gets the tree.
	 *
	 * @return the tree
	 */
	public JTree getTree(){
		return this.tree;
	}

	/**
	 * Remove the currently selected node.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void removeCurrentNode() throws IOException {
		TreePath currentSelection = tree.getSelectionPath();
		if (currentSelection != null) {
			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection.getLastPathComponent());
			MutableTreeNode parent = (MutableTreeNode) (currentNode.getParent());
			if (parent != null) {
				FileLinkNodeInfo nodeInfo = (FileLinkNodeInfo)currentNode.getUserObject();
				flApi.removeLinkFromConfig(nodeInfo.getFileLink());
				treeModel.removeNodeFromParent(currentNode);
				return;
			}
		}

	}

	/**
	 * Add child to the currently selected node.
	 *
	 * @param file the file
	 * @throws Exception the exception
	 */
	public void addObject(File file) throws Exception {
		
		if(file.isDirectory()){
			addDir(file);
		}else{
			addFile(file);
		}
	}
	
	/**
	 * Adds the dir.
	 *
	 * @param file the file
	 * @throws Exception the exception
	 */
	public void addDir(File file) throws Exception{
		File[] files = file.listFiles();
		for (File file2 : files) {
			if(file2.isDirectory()){
				addDir(file2);
			}else{
				addFile(file2);
			}
		}
	}
	
	/**
	 * Adds the file.
	 *
	 * @param file the file
	 * @throws Exception the exception
	 */
	public void addFile(File file) throws Exception{
		TreePath parentPath = tree.getSelectionPath();
		
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
		
		//Saves file link in configuration.
		CategoryNodeInfo categoryNode = (CategoryNodeInfo)parentNode.getUserObject();
		FileLink fileLink = new FileLink(file, categoryNode.getCategoryName());
		flApi.putLinkInConfig(fileLink);
		
		FileLinkNodeInfo fileNodeInfo = new FileLinkNodeInfo(fileLink);
		DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(fileNodeInfo);
		
		addObject(parentNode, fileNode, true);
	}

	/**
	 * Adds the object.
	 *
	 * @param parent the parent
	 * @param child the child
	 * @return the default mutable tree node
	 */
	public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,Object child) {
		return addObject(parent, child, false);
	}

	/**
	 * Adds the object.
	 *
	 * @param parent the parent
	 * @param child the child
	 * @param shouldBeVisible the should be visible
	 * @return the default mutable tree node
	 */
	public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child, boolean shouldBeVisible) {
		DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)child;

		if(parent == null){
			parent = rootNode;
		}
	
		treeModel.insertNodeInto(childNode, parent, parent.getChildCount());

		if (shouldBeVisible) {
			tree.scrollPathToVisible(new TreePath(childNode.getPath()));
		}
		return childNode;
	}
	
	/**
	 * Creates the nodes.
	 *
	 * @param top the top
	 */
	private void createNodes(DefaultMutableTreeNode top) {
		I_TermFactory tf = Terms.get();

		DefaultMutableTreeNode category = null;
		DefaultMutableTreeNode categoryFileLink = null;
		
		
		I_GetConceptData categoriesRoot;
		try {
			//categoriesRoot = tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());
			categoriesRoot = tf.getConcept(ArchitectonicAuxiliary.Concept.FILE_LINK_CATEGORY.getUids());
			Iterator<I_GetConceptData> s = flApi.getCategories(categoriesRoot).iterator();
			while (s.hasNext()) {
				I_GetConceptData iGetConceptData = (I_GetConceptData) s.next();
				category = new DefaultMutableTreeNode(new CategoryNodeInfo(iGetConceptData));
				addObject(null,category,false);
				for (FileLink fileLink : flApi.getLinksForCategory(iGetConceptData)) {
					categoryFileLink = new DefaultMutableTreeNode(new FileLinkNodeInfo(fileLink));
					addObject(category, categoryFileLink, false);
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * The listener interface for receiving myTreeModel events.
	 * The class that is interested in processing a myTreeModel
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addMyTreeModelListener<code> method. When
	 * the myTreeModel event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see MyTreeModelEvent
	 */
	class MyTreeModelListener implements TreeModelListener {
		
		/* (non-Javadoc)
		 * @see javax.swing.event.TreeModelListener#treeNodesChanged(javax.swing.event.TreeModelEvent)
		 */
		public void treeNodesChanged(TreeModelEvent e) {
			DefaultMutableTreeNode node;
			node = (DefaultMutableTreeNode) (e.getTreePath()
					.getLastPathComponent());

			/*
			 * If the event lists children, then the changed node is the child
			 * of the node we've already gotten. Otherwise, the changed node and
			 * the specified node are the same.
			 */

			int index = e.getChildIndices()[0];
			node = (DefaultMutableTreeNode) (node.getChildAt(index));

			System.out.println("The user has finished editing the node.");
			System.out.println("New value: " + node.getUserObject());
		}

		/* (non-Javadoc)
		 * @see javax.swing.event.TreeModelListener#treeNodesInserted(javax.swing.event.TreeModelEvent)
		 */
		public void treeNodesInserted(TreeModelEvent e) {
		}

		/* (non-Javadoc)
		 * @see javax.swing.event.TreeModelListener#treeNodesRemoved(javax.swing.event.TreeModelEvent)
		 */
		public void treeNodesRemoved(TreeModelEvent e) {
		}

		/* (non-Javadoc)
		 * @see javax.swing.event.TreeModelListener#treeStructureChanged(javax.swing.event.TreeModelEvent)
		 */
		public void treeStructureChanged(TreeModelEvent e) {
		}
	}
}

class CategoryNodeInfo {
	private I_GetConceptData categoryName;

	public CategoryNodeInfo(I_GetConceptData categoryName) {
		super();
		this.categoryName = categoryName;
	}

	public I_GetConceptData getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(I_GetConceptData categoryName) {
		this.categoryName = categoryName;
	}

	public String toString() {
		try {
			return this.categoryName.getInitialText();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}

class FileLinkNodeInfo {
	private FileLink fileLink;

	public FileLinkNodeInfo(FileLink fileLink) {
		super();
		this.fileLink = fileLink;
	}

	public FileLink getFileLink() {
		return fileLink;
	}

	public void setFileLink(FileLink fileLink) {
		this.fileLink = fileLink;
	}

	public String toString() {
		return fileLink.getName();
	}
}
