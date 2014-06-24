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

package org.ihtsdo.project.view;

import java.awt.Color;
import java.awt.Component;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.FilteredImageSource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.dwfa.ace.TermLabelMaker;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.dnd.ConceptTransferable;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.I_TerminologyProject.Type;
import org.ihtsdo.project.model.Partition;
import org.ihtsdo.project.model.PartitionScheme;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.project.view.details.PartitionDetailsPanel;
import org.ihtsdo.project.view.details.PartitionSchemeDetailsPanel;
import org.ihtsdo.project.view.details.ProjectDetailsContainer;
import org.ihtsdo.project.view.details.ProjectDetailsPanel;
import org.ihtsdo.project.view.details.WorkListDetailsPanel;
import org.ihtsdo.project.view.details.WorkSetDetailsPanel;

/**
 * The Class ProjectsPanel.
 * 
 * @author Alejandro Rodriguez
 */
public class ProjectsPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant PROJECTNODE. */
	public static final String PROJECTNODE = "P";

	/** The Constant WORKSETNODE. */
	public static final String WORKSETNODE = "WS";

	/** The Constant WORKLISTNODE. */
	public static final String WORKLISTNODE = "WL";

	/** The Constant PROJECTROOTNODE. */
	public static final String PROJECTROOTNODE = "PR";

	/** The Constant WORKSETROOTNODE. */
	public static final String WORKSETROOTNODE = "WSR";

	/** The Constant EXCREFSETROOTNODE. */
	public static final String EXCREFSETROOTNODE = "ERR";

	/** The Constant EXCREFSETNODE. */
	public static final String EXCREFSETNODE = "ERN";

	/** The Constant LNKREFSETROOTNODE. */
	public static final String LNKREFSETROOTNODE = "LRR";

	/** The Constant LNKREFSETNODE. */
	public static final String LNKREFSETNODE = "LRN";

	/** The Constant SRCREFSETROOTNODE. */
	public static final String SRCREFSETROOTNODE = "SRR";

	/** The Constant SRCREFSETNODE. */
	public static final String SRCREFSETNODE = "SRN";

	/** The Constant TGTREFSETROOTNODE. */
	public static final String TGTREFSETROOTNODE = "TRR";

	/** The Constant TGTREFSETNODE. */
	public static final String TGTREFSETNODE = "TRN";

	/** The Constant PARTSCHEMEROOTNODE. */
	public static final String PARTSCHEMEROOTNODE = "PSR";

	/** The Constant PARTSCHEMENODE. */
	public static final String PARTSCHEMENODE = "PSN";

	/** The Constant PARTITIONNODE. */
	public static final String PARTITIONNODE = "PTN";

	/** The tree model. */
	private DefaultTreeModel treeModel;

	/** The root node. */
	private DefaultMutableTreeNode rootNode;

	/** The config. */
	private I_ConfigAceFrame config;

	/** The worker. */
	private I_Work worker;

	/** The node opener. */
	private DefaultMutableTreeNode nodeOpener;

	/** The project root. */
	private DefaultMutableTreeNode projectRoot;

	/**
	 * Instantiates a new projects panel.
	 * 
	 * @param worker
	 *            the worker
	 * @throws RemoteException
	 *             the remote exception
	 * @throws TaskFailedException
	 *             the task failed exception
	 * @throws LeaseDeniedException
	 *             the lease denied exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws PrivilegedActionException
	 *             the privileged action exception
	 * @throws ConfigurationException
	 *             the configuration exception
	 * @throws TerminologyException
	 *             the terminology exception
	 */
	public ProjectsPanel(I_Work worker) throws RemoteException, TaskFailedException, IOException, InterruptedException, PrivilegedActionException,
			TerminologyException {

		initComponents();
		label2.setIcon(IconUtilities.helpIcon);
		label2.setText("");
		this.worker = worker;
		try {
			loadProjects();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AceLog.getAppLog().alertAndLogException(e);
		}
		jTree1.addMouseListener(new JTreeMouselistener(jTree1));
		jTree1.setCellRenderer(new TreeIconCellRenderer());
		boolean isProjectManager = TerminologyProjectDAO.checkPermissionForProject(config.getDbConfig().getUserConcept(),
				Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.localize().getNid()),
				Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PROJECT_MANAGER_ROLE.localize().getNid()), config);
		addProjectButton.setEnabled(isProjectManager);
		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(jTree1, DnDConstants.ACTION_COPY,
				new DragGestureListenerWithImage(new TermLabelDragSourceListener(), jTree1));

	}

	/**
	 * The Class TreeIconCellRenderer.
	 */
	public class TreeIconCellRenderer extends DefaultTreeCellRenderer {

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

			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			if (node != null && (node.getUserObject() instanceof TreeObj)) {
				TreeObj nodeObject = (TreeObj) node.getUserObject();
				setIcon(IconUtilities.getIconForProjectTree(nodeObject.getObjType()));
			}
			return this;
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
			// TODO Auto-generated method stub
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.
		 * DragSourceDragEvent)
		 */
		public void dragEnter(DragSourceDragEvent dsde) {
			// TODO Auto-generated method stub
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent
		 * )
		 */
		public void dragExit(DragSourceEvent dse) {
			// TODO Auto-generated method stub
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent
		 * )
		 */
		public void dragOver(DragSourceDragEvent dsde) {
			// TODO Auto-generated method stub
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.
		 * DragSourceDragEvent)
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
					if (objvalue instanceof I_GetConceptData) {
						I_GetConceptData obj = (I_GetConceptData) objvalue;
						Image dragImage = getDragImage(obj);
						Point imageOffset = new Point(-10, -(dragImage.getHeight(jTree) + 1));
						dge.startDrag(DragSource.DefaultCopyDrop, dragImage, imageOffset, getTransferable(obj), dsl);
					} else if (objvalue instanceof WorkSet) {

						WorkSet obj = (WorkSet) objvalue;
						I_GetConceptData con = obj.getConcept();
						Image dragImage = getDragImage(obj.getConcept());
						Point imageOffset = new Point(-10, -(dragImage.getHeight(jTree) + 1));
						dge.startDrag(DragSource.DefaultCopyDrop, dragImage, imageOffset, getTransferable(con), dsl);

					} else if (objvalue instanceof Partition) {
						Partition obj = (Partition) objvalue;
						I_GetConceptData con = obj.getConcept();
						Image dragImage = getDragImage(obj.getConcept());
						Point imageOffset = new Point(-10, -(dragImage.getHeight(jTree) + 1));
						dge.startDrag(DragSource.DefaultCopyDrop, dragImage, imageOffset, getTransferable(con), dsl);

					} else if (objvalue instanceof WorkList) {
						WorkList obj = (WorkList) objvalue;
						I_GetConceptData con = obj.getConcept();
						Image dragImage = getDragImage(obj.getConcept());
						Point imageOffset = new Point(-10, -(dragImage.getHeight(jTree) + 1));
						dge.startDrag(DragSource.DefaultCopyDrop, dragImage, imageOffset, getTransferable(con), dsl);

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
		 *             Signals that an I/O exception has occurred.
		 */
		private Transferable getTransferable(I_GetConceptData obj) throws TerminologyException, IOException {
			return new ConceptTransferable(Terms.get().getConcept(obj.getConceptNid()));
		}

		/**
		 * Gets the drag image.
		 * 
		 * @param obj
		 *            the obj
		 * @return the drag image
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		public Image getDragImage(I_GetConceptData obj) throws IOException {

			I_DescriptionTuple desc = obj.getDescTuple(config.getTreeDescPreferenceList(), config);
			if (desc == null) {
				desc = obj.getDescs().iterator().next().getFirstTuple();
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
	 * Load projects.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private void loadProjects() throws Exception {
		int i;
		rootNode = new DefaultMutableTreeNode("Root Node");
		treeModel = new DefaultTreeModel(rootNode);

		jTree1.setModel(treeModel);
		jTree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		jTree1.setRootVisible(false);
		config = Terms.get().getActiveAceFrameConfig();

		TreeObj translationProjectsNode = new TreeObj(PROJECTROOTNODE, "Projects", null);
		projectRoot = addObject(rootNode, translationProjectsNode, true);

		List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);

		for (i = 0; i < projects.size(); i++) {
			addProjectToTree(projectRoot, projects.get(i), false);
		}
		TreeNode[] path = projectRoot.getPath();
		jTree1.expandPath(new TreePath(path));
		jTree1.revalidate();

	}

	/**
	 * Load partition scheme root node detail.
	 * 
	 * @param node
	 *            the node
	 * @param workset
	 *            the workset
	 * @param visibleChildren
	 *            the visible children
	 */
	private void loadPartitionSchemeRootNodeDetail(DefaultMutableTreeNode node, WorkSet workset, boolean visibleChildren) {
		DefaultMutableTreeNode tmpNode;

		List<PartitionScheme> pSchemes = workset.getPartitionSchemes(config);

		if (pSchemes != null) {
			TreeMap<String, PartitionScheme> sm = new TreeMap<String, PartitionScheme>();
			for (PartitionScheme partitionS : pSchemes) {
				if (partitionS != null)
					sm.put(partitionS.getName(), partitionS);
			}
			for (String key : sm.keySet()) {
				PartitionScheme item = sm.get(key);
				addObject(node, new TreeObj(PARTSCHEMENODE, item.getName(), item), visibleChildren);
			}
		}
	}

	/**
	 * Load partition scheme root node detail.
	 * 
	 * @param node
	 *            the node
	 * @param partition
	 *            the partition
	 * @param visibleChildren
	 *            the visible children
	 */
	private void loadPartitionSchemeRootNodeDetail(DefaultMutableTreeNode node, Partition partition, boolean visibleChildren) {
		DefaultMutableTreeNode tmpNode;

		List<PartitionScheme> pSchemes = partition.getSubPartitionSchemes(config);

		if (pSchemes != null) {
			TreeMap<String, PartitionScheme> sm = new TreeMap<String, PartitionScheme>();
			for (PartitionScheme partitionS : pSchemes) {
				if (partitionS != null)
					sm.put(partitionS.getName(), partitionS);
			}
			for (String key : sm.keySet()) {
				PartitionScheme item = sm.get(key);
				addObject(node, new TreeObj(PARTSCHEMENODE, item.getName(), item), visibleChildren);
			}
		}
	}

	/**
	 * Load workset root node detail.
	 * 
	 * @param node
	 *            the node
	 * @param project
	 *            the project
	 * @param visibleChildren
	 *            the visible children
	 */
	private void loadWorksetRootNodeDetail(DefaultMutableTreeNode node, I_TerminologyProject project, boolean visibleChildren) {

		node.removeAllChildren();
		List<WorkSet> wslist = project.getWorkSets(config);
		if (wslist != null) {

			TreeMap<String, WorkSet> sm = new TreeMap<String, WorkSet>();
			for (WorkSet wSet : wslist) {
				if (wSet != null)
					sm.put(wSet.getName(), wSet);
			}
			for (String key : sm.keySet()) {
				WorkSet ws = sm.get(key);
				DefaultMutableTreeNode wstNode = addObject(node, new TreeObj(WORKSETNODE, ws.getName(), ws), false);
				loadWorksetNodeDetail(wstNode, ws, false);
			}
		}
	}

	/**
	 * Load workset node detail.
	 * 
	 * @param node
	 *            the node
	 * @param workset
	 *            the workset
	 * @param visibleChildren
	 *            the visible children
	 */
	private void loadWorksetNodeDetail(DefaultMutableTreeNode node, WorkSet workset, boolean visibleChildren) {
		DefaultMutableTreeNode tmpNode;

		tmpNode = addObject(node, new TreeObj(PARTSCHEMEROOTNODE, "Partition Schemes", workset), visibleChildren);
		List<PartitionScheme> pSchemes = workset.getPartitionSchemes(config);

		if (pSchemes != null) {
			TreeMap<String, PartitionScheme> sm = new TreeMap<String, PartitionScheme>();
			for (PartitionScheme partitionS : pSchemes) {
				if (partitionS != null)
					sm.put(partitionS.getName(), partitionS);
			}
			for (String key : sm.keySet()) {
				PartitionScheme item = sm.get(key);
				DefaultMutableTreeNode partScheNode = addObject(tmpNode, new TreeObj(PARTSCHEMENODE, item.getName(), item), visibleChildren);
				loadPartitionSchemeNodeDetail(partScheNode, item, visibleChildren);
			}
		}
		tmpNode = addObject(node, new TreeObj(EXCREFSETROOTNODE, "Exclusion Refsets", workset), visibleChildren);
		List<I_GetConceptData> wsl;
		try {
			wsl = workset.getExclusionRefsets();
			if (wsl != null) {
				for (I_GetConceptData ws : wsl) {
					addObject(tmpNode, new TreeObj(EXCREFSETNODE, ws.getInitialText(), ws), false);
				}
			}

			tmpNode = addObject(node, new TreeObj(SRCREFSETROOTNODE, "Source Refset", workset), visibleChildren);
			I_GetConceptData srcRefset = workset.getSourceRefset();
			if (srcRefset != null) {
				addObject(tmpNode, new TreeObj(SRCREFSETNODE, srcRefset.getInitialText(), srcRefset), false);

			}
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AceLog.getAppLog().alertAndLogException(e);
		}
		jTree1.revalidate();

	}

	/**
	 * Load partition scheme node detail.
	 * 
	 * @param node
	 *            the node
	 * @param ps
	 *            the ps
	 * @param visibleChildren
	 *            the visible children
	 */
	private void loadPartitionSchemeNodeDetail(DefaultMutableTreeNode node, PartitionScheme ps, boolean visibleChildren) {

		List<Partition> partitions = ps.getPartitions();

		if (partitions != null) {

			TreeMap<String, Partition> sm = new TreeMap<String, Partition>();
			for (Partition partition : partitions) {
				if (partition != null)
					sm.put(partition.getName(), partition);
			}
			for (String key : sm.keySet()) {
				Partition item = sm.get(key);
				DefaultMutableTreeNode partNode = addObject(node, new TreeObj(PARTITIONNODE, item.getName(), item), visibleChildren);
				loadPartitionNodeDetail(partNode, item, visibleChildren);
			}
		}
		jTree1.revalidate();

	}

	/**
	 * Load partition node detail.
	 * 
	 * @param node
	 *            the node
	 * @param p
	 *            the p
	 * @param visibleChildren
	 *            the visible children
	 */
	private void loadPartitionNodeDetail(DefaultMutableTreeNode node, Partition p, boolean visibleChildren) {
		DefaultMutableTreeNode tmpNode;

		List<PartitionScheme> pSchemes = new ArrayList<PartitionScheme>();
		pSchemes = p.getSubPartitionSchemes(config);

		if (pSchemes != null) {

			TreeMap<String, PartitionScheme> sm = new TreeMap<String, PartitionScheme>();
			for (PartitionScheme partitionS : pSchemes) {
				if (partitionS != null)
					sm.put(partitionS.getName(), partitionS);
			}
			if (sm.size() > 0) {
				tmpNode = addObject(node, new TreeObj(PARTSCHEMEROOTNODE, "Partition Schemes", p), visibleChildren);
				for (String key : sm.keySet()) {
					PartitionScheme item = sm.get(key);
					DefaultMutableTreeNode partScheNode = addObject(tmpNode, new TreeObj(PARTSCHEMENODE, item.getName(), item), visibleChildren);
					loadPartitionSchemeNodeDetail(partScheNode, item, visibleChildren);
				}

			}
		}
		List<WorkList> wlists = p.getWorkLists();

		if (wlists != null) {
			TreeMap<String, WorkList> sm = new TreeMap<String, WorkList>();
			for (WorkList wList : wlists) {
				if (wList != null)
					sm.put(wList.getName(), wList);
			}
			for (String key : sm.keySet()) {
				WorkList item = sm.get(key);
				addObject(node, new TreeObj(WORKLISTNODE, item.getName(), item), visibleChildren);
			}
		}
		jTree1.revalidate();
	}

	/**
	 * Adds the project to tree.
	 * 
	 * @param node
	 *            the node
	 * @param tProj
	 *            the project
	 * @param visibleChildren
	 *            the visible children
	 * @return the default mutable tree node
	 * @throws Exception
	 *             the exception
	 */
	private DefaultMutableTreeNode addProjectToTree(DefaultMutableTreeNode node, I_TerminologyProject tProj, boolean visibleChildren)
			throws Exception {

		DefaultMutableTreeNode tNode;
		tNode = addObject(node, new TreeObj(PROJECTNODE, tProj.getName(), tProj), visibleChildren);
		loadProjectNodeDetails(tNode, tProj, visibleChildren);
		return tNode;
	}

	/**
	 * Load project node details.
	 * 
	 * @param node
	 *            the node
	 * @param project
	 *            the project
	 * @param visibleChildren
	 *            the visible children
	 */
	private void loadProjectNodeDetails(DefaultMutableTreeNode node, I_TerminologyProject project, boolean visibleChildren) {
		DefaultMutableTreeNode tmpNode;
		try {
			tmpNode = addObject(node, new TreeObj(EXCREFSETROOTNODE, "Exclusion Refsets", project), visibleChildren);
			List<I_GetConceptData> wsl;
			wsl = project.getExclusionRefsets();
			if (wsl != null) {
				for (I_GetConceptData ws : wsl) {
					addObject(tmpNode, new TreeObj(EXCREFSETNODE, ws.getInitialText(), ws), visibleChildren);
				}
			}

			tmpNode = addObject(node, new TreeObj(LNKREFSETROOTNODE, "Linked Refsets", project), visibleChildren);
			wsl = project.getCommonRefsets();
			if (wsl != null) {
				for (I_GetConceptData ws : wsl) {
					addObject(tmpNode, new TreeObj(LNKREFSETNODE, ws.getInitialText(), ws), visibleChildren);
				}
			}

			if (project.getProjectType().equals(Type.TRANSLATION)) {
				TranslationProject translationProject = (TranslationProject) project;
				tmpNode = addObject(node, new TreeObj(SRCREFSETROOTNODE, "Source Languages", project), visibleChildren);
				wsl = translationProject.getSourceLanguageRefsets();
				if (wsl != null) {
					for (I_GetConceptData ws : wsl) {
						addObject(tmpNode, new TreeObj(SRCREFSETNODE, ws.getInitialText(), ws), visibleChildren);
					}
				}

				tmpNode = addObject(node, new TreeObj(TGTREFSETROOTNODE, "Target Language", project), visibleChildren);
				I_GetConceptData tl = translationProject.getTargetLanguageRefset();
				if (tl != null) {
					addObject(tmpNode, new TreeObj(TGTREFSETNODE, tl.getInitialText(), tl), visibleChildren);
				}
			}

			tmpNode = addObject(node, new TreeObj(WORKSETROOTNODE, "Worksets", project), false);
			tmpNode.add(new DefaultMutableTreeNode("Loading..."));
			// loadWorksetRootNodeDetail(tmpNode, project, visibleChildren);
			// List<WorkSet> wslist=project.getWorkSets(config);
			// if (wslist!=null){
			//
			// TreeMap<String,WorkSet> sm=new TreeMap<String, WorkSet>();
			// for (WorkSet wSet : wslist) {
			// if (wSet!=null)
			// sm.put(wSet.getName(), wSet);
			// }
			// for (String key:sm.keySet()){
			// WorkSet ws=sm.get(key);
			// addObject(tmpNode, new TreeObj(WORKSETNODE,ws.getName(),ws),
			// visibleChildren);
			// }
			// }
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			AceLog.getAppLog().alertAndLogException(e);
		}
		jTree1.revalidate();
	}

	/**
	 * Adds the object.
	 * 
	 * @param child
	 *            the child
	 * @return the default mutable tree node
	 */
	public DefaultMutableTreeNode addObject(Object child) {
		DefaultMutableTreeNode parentNode = null;
		TreePath parentPath = jTree1.getSelectionPath();

		if (parentPath == null) {
			// There's no selection. Default to the root node.
			parentNode = rootNode;
		} else {
			parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
		}

		return addObject(parentNode, child, true);
	}

	/**
	 * Adds the object.
	 * 
	 * @param parent
	 *            the parent
	 * @param child
	 *            the child
	 * @param shouldBeVisible
	 *            the should be visible
	 * @return the default mutable tree node
	 */
	public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child, boolean shouldBeVisible) {
		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);

		treeModel.insertNodeInto(childNode, parent, parent.getChildCount());

		// Make sure the user can see the lovely new node.
		if (shouldBeVisible) {
			jTree1.scrollPathToVisible(new TreePath(childNode.getPath()));
		}
		return childNode;
	}

	/**
	 * J tree1 value changed.
	 * 
	 * @param e
	 *            the e
	 */
	private void jTree1ValueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node;
		node = (DefaultMutableTreeNode) (jTree1.getLastSelectedPathComponent());

		if (node == null)
			return;

		nodeAction(node, true, false);
	}

	/**
	 * Gets the right panel.
	 * 
	 * @return the right panel
	 */
	private ProjectDetailsContainer getRightPanel() {
		ProjectDetailsContainer detPanel = null;
		AceFrameConfig aceConfig = (AceFrameConfig) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
		try {
			if (aceConfig == null) {
				aceConfig = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();
			}
			AceFrame ace = aceConfig.getAceFrame();
			JTabbedPane tp = ace.getCdePanel().getConceptTabs();
			if (tp != null) {
				boolean bPanelExists = false;
				int tabCount = tp.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.PROJECTS_DETAILS_TAB_NAME)) {
						detPanel = (ProjectDetailsContainer) tp.getComponentAt(i);
						tp.setSelectedIndex(i);
						bPanelExists = true;
						break;
					}
				}
				if (!bPanelExists) {
					detPanel = new ProjectDetailsContainer();
					tp.addTab(TranslationHelperPanel.PROJECTS_DETAILS_TAB_NAME, detPanel);
					tp.setSelectedIndex(tabCount);
				}
				tp.revalidate();
				tp.repaint();
			} else {
				throw new TaskFailedException("Cannot set panel to main panel.");
			}
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TaskFailedException e) {
			// TODO Auto-generated catch block
			AceLog.getAppLog().alertAndLogException(e);
		}
		return detPanel;
	}

	/**
	 * Node action.
	 * 
	 * @param node
	 *            the node
	 * @param openEditor
	 *            the open editor
	 * @param visibleChildren
	 *            the visible children
	 */
	private void nodeAction(DefaultMutableTreeNode node, boolean openEditor, boolean visibleChildren) {
		TreeObj to = (TreeObj) node.getUserObject();

		if (to.getObjType().equals(PROJECTROOTNODE)) {
			return;
		}

		if (to.getObjType().equals(PROJECTNODE)) {
			I_TerminologyProject proj = (I_TerminologyProject) to.getAtrValue();
			if (node.getChildCount() < 1) {
				loadProjectNodeDetails(node, proj, visibleChildren);
			}
			if (openEditor) {
				nodeOpener = node;
				showProjectDetails(proj);
			}
			return;
		}

		if (to.getObjType().equals(WORKSETROOTNODE)) {
			return;
		}

		if (to.getObjType().equals(WORKSETNODE)) {

			WorkSet ws = (WorkSet) to.getAtrValue();
			if (node.getChildCount() < 1) {
				loadWorksetNodeDetail(node, ws, visibleChildren);
			}
			if (openEditor) {
				nodeOpener = node;
				showWorksetDetails(ws);
			}
			return;

		}
		if (to.getObjType().equals(PARTSCHEMEROOTNODE)) {
			return;
		}

		if (to.getObjType().equals(PARTSCHEMENODE)) {

			PartitionScheme ps = (PartitionScheme) to.getAtrValue();
			if (node.getChildCount() < 1) {
				loadPartitionSchemeNodeDetail(node, ps, visibleChildren);
			}
			if (openEditor) {
				nodeOpener = node;
				showPartitionSchDetails(ps);
			}
			return;

		}

		if (to.getObjType().equals(PARTITIONNODE)) {

			Partition p = (Partition) to.getAtrValue();
			if (node.getChildCount() < 1) {
				loadPartitionNodeDetail(node, p, visibleChildren);
			}
			if (openEditor) {
				nodeOpener = node;
				showPartitionDetails(p);
			}
			return;

		}
		if (to.getObjType().equals(WORKLISTNODE)) {
			if (openEditor) {
				WorkList w = (WorkList) to.getAtrValue();
				nodeOpener = node;
				showWorklistDetails(w);
			}
			return;

		}

	}

	/**
	 * Show worklist details.
	 * 
	 * @param w
	 *            the w
	 */
	private void showWorklistDetails(WorkList w) {
		ProjectDetailsContainer panel = getRightPanel();
		panel.removeContent();
		;
		panel.addContent(new WorkListDetailsPanel(w, config, worker));
		panel.revalidate();

	}

	/**
	 * Show partition details.
	 * 
	 * @param p
	 *            the p
	 */
	private void showPartitionDetails(Partition p) {
		ProjectDetailsContainer panel = getRightPanel();
		panel.removeContent();
		;
		panel.addContent(new PartitionDetailsPanel(p, config));
		panel.revalidate();

	}

	/**
	 * Show partition sch details.
	 * 
	 * @param ps
	 *            the ps
	 */
	private void showPartitionSchDetails(PartitionScheme ps) {
		ProjectDetailsContainer panel = getRightPanel();
		panel.removeContent();
		;
		panel.addContent(new PartitionSchemeDetailsPanel(ps, config));
		panel.revalidate();

	}

	/**
	 * Show workset details.
	 * 
	 * @param ws
	 *            the ws
	 */
	private void showWorksetDetails(WorkSet ws) {
		ProjectDetailsContainer panel = getRightPanel();
		panel.removeContent();
		;
		panel.addContent(new WorkSetDetailsPanel(ws, config));
		panel.revalidate();

	}

	/**
	 * Show project details.
	 * 
	 * @param tProj
	 *            the proj
	 */
	private void showProjectDetails(I_TerminologyProject tProj) {
		ProjectDetailsContainer panel = getRightPanel();
		panel.removeContent();

		panel.addContent(new ProjectDetailsPanel(tProj, config));
		panel.revalidate();

	}

	/**
	 * Refresh node.
	 */
	public void refreshNode() {
		if (nodeOpener != null) {
			for (int i = nodeOpener.getChildCount() - 1; i > -1; i--) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeOpener.getChildAt(i);
				((DefaultTreeModel) jTree1.getModel()).removeNodeFromParent(node);
			}
			updateNode(nodeOpener);
			jTree1.revalidate();
			nodeAction(nodeOpener, false, true);
		}
	}

	/**
	 * Update node.
	 * 
	 * @param node
	 *            the node
	 */
	private void updateNode(DefaultMutableTreeNode node) {
		TreeObj to = (TreeObj) node.getUserObject();

		if (to.getObjType().equals(PROJECTNODE)) {
			I_TerminologyProject proj = (I_TerminologyProject) to.getAtrValue();
			to.setAtrName(proj.getName());
			return;
		}

		if (to.getObjType().equals(WORKSETNODE)) {

			WorkSet ws = (WorkSet) to.getAtrValue();
			to.setAtrName(ws.getName());
			return;

		}

		if (to.getObjType().equals(PARTSCHEMENODE)) {

			PartitionScheme ps = (PartitionScheme) to.getAtrValue();
			to.setAtrName(ps.getName());
			return;

		}

		if (to.getObjType().equals(PARTITIONNODE)) {

			Partition p = (Partition) to.getAtrValue();
			to.setAtrName(p.getName());
			return;

		}
		if (to.getObjType().equals(WORKLISTNODE)) {
			WorkList w = (WorkList) to.getAtrValue();
			to.setAtrName(w.getName());
			return;

		}

	}

	/**
	 * Button1 action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void button1ActionPerformed(ActionEvent e) {
		AceFrameConfig config;
		try {
			config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();
			AceFrame ace = config.getAceFrame();
			JTabbedPane tp = ace.getCdePanel().getLeftTabs();
			if (tp != null) {
				int tabCount = tp.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.PROJECT_MGR_TAB_NAME)) {
						tp.remove(i);
						tp.repaint();
						tp.revalidate();
					}

				}
			}

			JTabbedPane tpc = ace.getCdePanel().getConceptTabs();
			if (tpc != null) {
				int tabCount = tpc.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tpc.getTitleAt(i).equals(TranslationHelperPanel.PROJECTS_DETAILS_TAB_NAME)) {
						tpc.remove(i);
						tpc.repaint();
						tpc.revalidate();
					}

				}
			}
		} catch (TerminologyException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}

	/**
	 * Button2 action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void addProjectActionPerformed(ActionEvent e) {
		HashMap<String, Type> projectInfo = new ProjectNameTypeDialog().showModalDialog();
		if (projectInfo != null && !projectInfo.isEmpty()) {
			try {
				String projectName = projectInfo.keySet().iterator().next();
				I_TerminologyProject.Type projectType = projectInfo.get(projectName);
				I_TerminologyProject tProj = null;
				switch (projectType) {
				case TRANSLATION:
					tProj = TerminologyProjectDAO.createNewTranslationProject(projectName, config);
					break;
				case TERMINOLOGY:
					tProj = TerminologyProjectDAO.createNewTerminologyProject(projectName, config);
					break;
				case MAPPING:
					tProj = TerminologyProjectDAO.createNewMappingProject(projectName, config);
					break;
				default:
					break;
				}
				if (tProj != null) {
					Terms.get().commit();
					nodeOpener = addProjectToTree(projectRoot, tProj, false);
					showProjectDetails(tProj);

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							TranslationHelperPanel.refreshProjectPanelNode(config);
						}
					});
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * J tree1 tree will expand.
	 * 
	 * @param e
	 *            the e
	 * @throws ExpandVetoException
	 *             the expand veto exception
	 */
	private void jTree1TreeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
		TreePath path = e.getPath();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		
		Object obj = node.getUserObject();
		if (obj instanceof TreeObj) {
			TreeObj to = (TreeObj) obj;

			if (to.getObjType().equals(WORKSETROOTNODE)) {

				if (node.getFirstChild().toString().trim().equals("Loading...")) {
					node.removeAllChildren();
					TreeObj treeObj = (TreeObj) node.getUserObject();
					I_TerminologyProject project = (I_TerminologyProject) treeObj.getAtrValue();
					loadWorksetRootNodeDetail(node, project, false);
				}
			}
		}
	}

	/**
	 * Label2 mouse clicked.
	 * 
	 * @param e
	 *            the e
	 */
	private void label2MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("PROJECT_LEFT");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * The listener interface for receiving menuItemAction events. The class
	 * that is interested in processing a menuItemAction event implements this
	 * interface, and the object created with that class is registered with a
	 * component using the component's
	 * <code>addMenuItemActionListener<code> method. When
	 * the menuItemAction event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see MenuItemActionEvent
	 */
	class MenuItemActionListener implements ActionListener {

		/** The node type. */
		private String nodeType;
		private DefaultMutableTreeNode node;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (nodeType!=null && nodeType.equals(PROJECTROOTNODE)) {
				addProjectActionPerformed(e);
				return;
			}else if (node!=null){
				recursiveExpand(node);
			}

		}

		/**
		 * Sets the node type.
		 * 
		 * @param nodeType
		 *            the new node type
		 */
		public void setNodeType(String nodeType) {
			this.nodeType = nodeType;
		}
		public void setNode (DefaultMutableTreeNode node){
			this.node=node;
		}

	}

	/**
	 * The Class JTreeMouselistener.
	 */
	public class JTreeMouselistener extends MouseAdapter {

		/** The j tree. */
		private JTree jTree;

		/** The menu. */
		private JPopupMenu menu;

		/** The m item listener. */
		private MenuItemActionListener mItemListener;

		/** The m item. */
		private JMenuItem mItem;

		/** The x point. */
		private int xPoint;

		/** The y point. */
		private int yPoint;

		/**
		 * Instantiates a new j tree mouselistener.
		 * 
		 * @param jTree
		 *            the j tree
		 */
		JTreeMouselistener(JTree jTree) {
			this.jTree = jTree;
			menu = new JPopupMenu();
			mItem = new JMenuItem();
			mItemListener = new MenuItemActionListener();
			mItem.addActionListener(mItemListener);
			menu.add(mItem);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {

			if (e.getButton() == java.awt.event.MouseEvent.BUTTON3) {
				xPoint = e.getX();
				yPoint = e.getY();
				TreePath treePath = jTree.getPathForLocation(xPoint, yPoint);
				if (treePath != null) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();

					if (node != null) {
						TreeObj to = (TreeObj) node.getUserObject();

						if (to.getObjType().equals(PROJECTROOTNODE)) {
							try {
								boolean isProjectManager = TerminologyProjectDAO.checkPermissionForProject(config.getDbConfig().getUserConcept(),
										Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.localize().getNid()), Terms
												.get().getConcept(ArchitectonicAuxiliary.Concept.PROJECT_MANAGER_ROLE.localize().getNid()), config);
								if (isProjectManager) {
									mItem.setText("Create new project");
									mItemListener.setNodeType(PROJECTROOTNODE);

									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											menu.show(jTree, xPoint, yPoint);
										}
									});
								}
							} catch (IOException e1) {
								e1.printStackTrace();
							} catch (TerminologyException e1) {
								e1.printStackTrace();
							}

						}else{

							mItem.setText("Expand tree");
							mItemListener.setNode(node);

							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									menu.show(jTree, xPoint, yPoint);
								}
							});
						}
					}
				}

			}
		}

	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		panel2 = new JPanel();
		panel1 = new JPanel();
		label1 = new JLabel();
		addProjectButton = new JButton();
		button1 = new JButton();
		label2 = new JLabel();
		panel11 = new JPanel();
		scrollPane1 = new JScrollPane();
		jTree1 = new JTree();

		// ======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout) getLayout()).columnWidths = new int[] { 0, 0 };
		((GridBagLayout) getLayout()).rowHeights = new int[] { 0, 0 };
		((GridBagLayout) getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
		((GridBagLayout) getLayout()).rowWeights = new double[] { 1.0, 1.0E-4 };

		// ======== panel2 ========
		{
			panel2.setBackground(new Color(238, 238, 238));
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout) panel2.getLayout()).columnWidths = new int[] { 20, 0, 0 };
			((GridBagLayout) panel2.getLayout()).rowHeights = new int[] { 0, 0, 0 };
			((GridBagLayout) panel2.getLayout()).columnWeights = new double[] { 0.0, 1.0, 1.0E-4 };
			((GridBagLayout) panel2.getLayout()).rowWeights = new double[] { 0.0, 1.0, 1.0E-4 };

			// ======== panel1 ========
			{
				panel1.setBackground(new Color(238, 238, 238));
				panel1.setLayout(new GridBagLayout());
				((GridBagLayout) panel1.getLayout()).columnWidths = new int[] { 0, 61, 0, 0, 0, 0 };
				((GridBagLayout) panel1.getLayout()).rowHeights = new int[] { 0, 0 };
				((GridBagLayout) panel1.getLayout()).columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 0.0, 1.0E-4 };
				((GridBagLayout) panel1.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

				// ---- label1 ----
				label1.setText("Projects");
				panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0,
						0, 5), 0, 0));

				// ---- button2 ----
				addProjectButton.setText("Add Project");
				addProjectButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						addProjectActionPerformed(e);
					}
				});
				panel1.add(addProjectButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

				// ---- button1 ----
				button1.setText("Close");
				button1.setIcon(null);
				button1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						button1ActionPerformed(e);
					}
				});
				panel1.add(button1, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0,
						0, 5), 0, 0));

				// ---- label2 ----
				label2.setText("text");
				label2.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						label2MouseClicked(e);
					}
				});
				panel1.add(label2, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0,
						0, 0), 0, 0));
			}
			panel2.add(panel1, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

			// ======== panel11 ========
			{
				panel11.setBackground(new Color(220, 233, 249));
				panel11.setLayout(new GridBagLayout());
				((GridBagLayout) panel11.getLayout()).columnWidths = new int[] { 0, 0 };
				((GridBagLayout) panel11.getLayout()).rowHeights = new int[] { 0, 0 };
				((GridBagLayout) panel11.getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
				((GridBagLayout) panel11.getLayout()).rowWeights = new double[] { 1.0, 1.0E-4 };

				// ======== scrollPane1 ========
				{

					// ---- jTree1 ----
					jTree1.setRootVisible(false);
					jTree1.addTreeSelectionListener(new TreeSelectionListener() {
						@Override
						public void valueChanged(TreeSelectionEvent e) {
							jTree1ValueChanged(e);
						}
					});
					jTree1.addTreeWillExpandListener(new TreeWillExpandListener() {
						@Override
						public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
							jTree1TreeWillExpand(e);
						}

						@Override
						public void treeWillCollapse(TreeExpansionEvent e) throws ExpandVetoException {
						}
					});
					scrollPane1.setViewportView(jTree1);
				}
				panel11.add(scrollPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
			}
			panel2.add(panel11, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0,
					0), 0, 0));
		}
		add(panel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		// //GEN-END:initComponents
	}

	public void recursiveExpand(DefaultMutableTreeNode node) {
		nodeAction(node,false, true);
		TreePath tPath=new TreePath( node.getPath());
		jTree1.expandPath(tPath);
		for (int i=0;i<node.getChildCount();i++){
			DefaultMutableTreeNode nodeChild=(DefaultMutableTreeNode)node.getChildAt(i);
			if (nodeChild!=null){
				recursiveExpand(nodeChild);
			}
		}
		
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	/** The panel2. */
	private JPanel panel2;

	/** The panel1. */
	private JPanel panel1;

	/** The label1. */
	private JLabel label1;

	/** The button2. */
	private JButton addProjectButton;

	/** The button1. */
	private JButton button1;

	/** The label2. */
	private JLabel label2;

	/** The panel11. */
	private JPanel panel11;

	/** The scroll pane1. */
	private JScrollPane scrollPane1;

	/** The j tree1. */
	private JTree jTree1;

	// JFormDesigner - End of variables declaration //GEN-END:variables

	/**
	 * Refresh parent node.
	 */
	public void refreshParentNode() {
		if (nodeOpener != null) {
			DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) nodeOpener.getParent();
			for (int i = parentNode.getChildCount() - 1; i > -1; i--) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) parentNode.getChildAt(i);
				((DefaultTreeModel) jTree1.getModel()).removeNodeFromParent(node);
			}
			jTree1.revalidate();
			nodeOpener = null;
			TreeObj to = (TreeObj) parentNode.getUserObject();

			if (to.getObjType().equals(PROJECTROOTNODE)) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						try {

							loadProjects();
						} catch (Exception e) {
							AceLog.getAppLog().alertAndLogException(e);
						}

					}

				});
				return;
			}
			if (to.getObjType().equals(WORKSETROOTNODE)) {
				I_TerminologyProject project = (I_TerminologyProject) to.getAtrValue();
				loadWorksetRootNodeDetail(parentNode, project, true);
				return;
			}

			if (to.getObjType().equals(PARTSCHEMEROOTNODE)) {
				if (to.getAtrValue() instanceof WorkSet) {
					WorkSet workset = (WorkSet) to.getAtrValue();
					loadPartitionSchemeRootNodeDetail(parentNode, workset, true);
					return;
				}
				if (to.getAtrValue() instanceof Partition) {
					Partition partition = (Partition) to.getAtrValue();
					loadPartitionSchemeRootNodeDetail(parentNode, partition, true);
					return;
				}
				return;
			}

			nodeAction(parentNode, false, true);
		}

	}
}
