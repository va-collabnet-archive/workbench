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

public class FileLinkDynamicTree extends JPanel {
	FileLinkAPI flApi;
	protected DefaultMutableTreeNode rootNode;
	protected DefaultTreeModel treeModel;
	protected JTree tree;

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
	
	public JTree getTree(){
		return this.tree;
	}

	/** Remove the currently selected node. 
	 * @throws IOException */
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

	/** Add child to the currently selected node. 
	 * @throws Exception */
	public void addObject(File file) throws Exception {
		
		if(file.isDirectory()){
			addDir(file);
		}else{
			addFile(file);
		}
	}
	
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

	public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,Object child) {
		return addObject(parent, child, false);
	}

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

	class MyTreeModelListener implements TreeModelListener {
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

		public void treeNodesInserted(TreeModelEvent e) {
		}

		public void treeNodesRemoved(TreeModelEvent e) {
		}

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
