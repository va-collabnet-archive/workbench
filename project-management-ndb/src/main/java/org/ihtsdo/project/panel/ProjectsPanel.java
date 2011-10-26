/*
 * Created by JFormDesigner on Wed Mar 10 16:04:51 GMT-03:00 2010
 */

package org.ihtsdo.project.panel;


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
import java.util.List;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.jini.config.ConfigurationException;
import net.jini.core.lease.LeaseDeniedException;

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
import org.ihtsdo.project.model.Partition;
import org.ihtsdo.project.model.PartitionScheme;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.panel.details.PartitionDetailsPanel;
import org.ihtsdo.project.panel.details.PartitionSchemeDetailsPanel;
import org.ihtsdo.project.panel.details.ProjectDetailsContainer;
import org.ihtsdo.project.panel.details.ProjectDetailsPanel;
import org.ihtsdo.project.panel.details.WorkListDetailsPanel;
import org.ihtsdo.project.panel.details.WorkSetDetailsPanel;
import org.ihtsdo.project.util.IconUtilities;

/**
 * @author Alejandro Rodriguez
 */
public class ProjectsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public static final String PROJECTNODE = "P";

	public static final String WORKSETNODE = "WS";

	public static final String WORKLISTNODE = "WL";

	public static final String PROJECTROOTNODE = "PR";

	public static final String WORKSETROOTNODE = "WSR";

	public static final String EXCREFSETROOTNODE="ERR";


	public static final String EXCREFSETNODE="ERN";


	public static final String LNKREFSETROOTNODE="LRR";


	public static final String LNKREFSETNODE="LRN";


	public static final String SRCREFSETROOTNODE="SRR";


	public static final String SRCREFSETNODE="SRN";

	
	public static final String TGTREFSETROOTNODE="TRR";


	public static final String TGTREFSETNODE="TRN";


	public static final String PARTSCHEMEROOTNODE="PSR";


	public static final String PARTSCHEMENODE="PSN";


	public static final String PARTITIONNODE = "PTN";

	private DefaultTreeModel treeModel;
	private DefaultMutableTreeNode rootNode;
	private I_ConfigAceFrame config;
	private I_Work worker;
	private DefaultMutableTreeNode nodeOpener;


	private DefaultMutableTreeNode projectRoot;

	public ProjectsPanel(I_Work worker ) throws RemoteException, TaskFailedException, LeaseDeniedException, IOException, InterruptedException, PrivilegedActionException, ConfigurationException, TerminologyException{


		initComponents();
		label2.setIcon(IconUtilities.helpIcon);
		label2.setText("");
		this.worker=worker;
		try {
			loadProjects();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		jTree1.addMouseListener(new JTreeMouselistener(jTree1));
		jTree1.setCellRenderer(new TreeIconCellRenderer());
		boolean isProjectManager = TerminologyProjectDAO.checkPermissionForProject(
				config.getDbConfig().getUserConcept(), 
				Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.localize().getNid()),
				Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PROJECT_MANAGER_ROLE.localize().getNid()), 
				config);
		button2.setEnabled(isProjectManager);
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(jTree1, DnDConstants.ACTION_COPY,
            new DragGestureListenerWithImage(new TermLabelDragSourceListener(),jTree1));
		
	}
	public class TreeIconCellRenderer extends DefaultTreeCellRenderer{
		
	@Override
	public Component getTreeCellRendererComponent(
			JTree tree,
			Object value,
			boolean sel,
			boolean expanded,
			boolean leaf,
			int row,
			boolean hasFocus) {

		super.getTreeCellRendererComponent(
				tree, value, sel,
				expanded, leaf, row,
				hasFocus);
		DefaultMutableTreeNode node =  (DefaultMutableTreeNode)value;
		if (node!=null && (node.getUserObject() instanceof TreeObj)){
			TreeObj nodeObject = (TreeObj) node.getUserObject();
			setIcon(IconUtilities.getIconForProjectTree(nodeObject.getObjType()));
		}
		return this;
	}
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
                	}else if (objvalue instanceof WorkSet){
                		
                		WorkSet obj = (WorkSet) objvalue;
                		I_GetConceptData con=obj.getConcept();
                		Image dragImage = getDragImage(obj.getConcept());
                		Point imageOffset = new Point(-10, -(dragImage.getHeight(jTree) + 1));
                		dge.startDrag(DragSource.DefaultCopyDrop, dragImage, imageOffset, getTransferable(con), dsl);
                		
                	}else if (objvalue instanceof Partition){
                		Partition obj = (Partition) objvalue;
                		I_GetConceptData con=obj.getConcept();
                		Image dragImage = getDragImage(obj.getConcept());
                		Point imageOffset = new Point(-10, -(dragImage.getHeight(jTree) + 1));
                		dge.startDrag(DragSource.DefaultCopyDrop, dragImage, imageOffset, getTransferable(con), dsl);
                		
                	}else if (objvalue instanceof WorkList){
                		WorkList obj = (WorkList) objvalue;
                		I_GetConceptData con=obj.getConcept();
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
    private void loadProjects() throws Exception{
        int i;
        rootNode= new DefaultMutableTreeNode("Root Node");
        treeModel = new DefaultTreeModel(rootNode);
        
        jTree1.setModel(treeModel);
        jTree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTree1.setRootVisible(false);
		config= Terms.get().getActiveAceFrameConfig();

		projectRoot=addObject(rootNode,new TreeObj(PROJECTROOTNODE,"Translation Projects",null),true); 

		List<TranslationProject> projects=TerminologyProjectDAO.getAllTranslationProjects(config) ;

		for (i=0;i<projects.size();i++ ){
			addProjectToTree(projectRoot, projects.get(i), false);
		}

		jTree1.revalidate();

	}

	private void loadPartitionSchemeRootNodeDetail(DefaultMutableTreeNode node, WorkSet workset,boolean visibleChildren) {	
		DefaultMutableTreeNode tmpNode;


		List<PartitionScheme> pSchemes = workset.getPartitionSchemes(config);

		if (pSchemes!=null){
			TreeMap<String,PartitionScheme> sm=new TreeMap<String, PartitionScheme>();
			for (PartitionScheme partitionS : pSchemes) {
				if (partitionS!=null)
					sm.put(partitionS.getName(), partitionS);
			}
			for (String key:sm.keySet()){
				PartitionScheme item=sm.get(key);
				addObject(node, new TreeObj(PARTSCHEMENODE,item.getName(),item), visibleChildren);
			}
		}
	}

	private void loadPartitionSchemeRootNodeDetail(DefaultMutableTreeNode node, Partition partition,boolean visibleChildren) {	
		DefaultMutableTreeNode tmpNode;

		List<PartitionScheme> pSchemes = partition.getSubPartitionSchemes(config);

		if (pSchemes!=null){
			TreeMap<String,PartitionScheme> sm=new TreeMap<String, PartitionScheme>();
			for (PartitionScheme partitionS : pSchemes) {
				if (partitionS!=null)
					sm.put(partitionS.getName(), partitionS);
			}
			for (String key:sm.keySet()){
				PartitionScheme item=sm.get(key);
				addObject(node, new TreeObj(PARTSCHEMENODE,item.getName(),item), visibleChildren);
			}
		}
	}

	private void loadWorksetRootNodeDetail(DefaultMutableTreeNode node, TranslationProject project,boolean visibleChildren) {
	
		node.removeAllChildren();
		List<WorkSet> wslist=project.getWorkSets(config);
		if (wslist!=null){

			TreeMap<String,WorkSet> sm=new TreeMap<String, WorkSet>();
			for (WorkSet wSet : wslist) {
				if (wSet!=null)
					sm.put(wSet.getName(), wSet);
			}
			for (String key:sm.keySet()){
				WorkSet ws=sm.get(key);
				DefaultMutableTreeNode wstNode=addObject(node, new TreeObj(WORKSETNODE,ws.getName(),ws), false);
				loadWorksetNodeDetail(wstNode, ws, false);
			}
		}
	}
	private void loadWorksetNodeDetail(DefaultMutableTreeNode node, WorkSet workset,boolean visibleChildren) {	
		DefaultMutableTreeNode tmpNode;


		tmpNode=addObject(node,new TreeObj(PARTSCHEMEROOTNODE,"Partition Schemes" ,workset),visibleChildren);
		List<PartitionScheme> pSchemes = workset.getPartitionSchemes(config);

		if (pSchemes!=null){
			TreeMap<String,PartitionScheme> sm=new TreeMap<String, PartitionScheme>();
			for (PartitionScheme partitionS : pSchemes) {
				if (partitionS!=null)
					sm.put(partitionS.getName(), partitionS);
			}
			for (String key:sm.keySet()){
				PartitionScheme item=sm.get(key);
				DefaultMutableTreeNode partScheNode= addObject(tmpNode, new TreeObj(PARTSCHEMENODE,item.getName(),item), visibleChildren);
				loadPartitionSchemeNodeDetail(partScheNode, item, visibleChildren);
			}
		}
		tmpNode=addObject(node,new TreeObj(EXCREFSETROOTNODE,"Exclusion Refsets" ,workset),visibleChildren);
		List<I_GetConceptData> wsl;
		try {
			wsl = workset.getExclusionRefsets();
			if (wsl!=null){
				for (I_GetConceptData ws:wsl){
					addObject(tmpNode, new TreeObj(EXCREFSETNODE,ws.getInitialText(),ws), false);
				}
			}

			tmpNode=addObject(node,new TreeObj(SRCREFSETROOTNODE,"Source Refset" ,workset),visibleChildren);
			I_GetConceptData srcRefset = workset.getSourceRefset();
			if (srcRefset!=null){
				addObject(tmpNode, new TreeObj(SRCREFSETNODE,srcRefset.getInitialText(),srcRefset), false);
				
			}
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		jTree1.revalidate();


	}

	private void loadPartitionSchemeNodeDetail(DefaultMutableTreeNode node,
			PartitionScheme ps,boolean visibleChildren) {

		List<Partition> partitions = ps.getPartitions();

		if (partitions!=null){

			TreeMap<String,Partition> sm=new TreeMap<String, Partition>();
			for (Partition partition : partitions) {
				if (partition!=null)
					sm.put(partition.getName(), partition);
			}
			for (String key:sm.keySet()){
				Partition item=sm.get(key);
				DefaultMutableTreeNode partNode=addObject(node, new TreeObj(PARTITIONNODE,item.getName(),item), visibleChildren);
				loadPartitionNodeDetail(partNode, item, visibleChildren);
			}
		}
		jTree1.revalidate();

	}

	private void loadPartitionNodeDetail(DefaultMutableTreeNode node, Partition p,boolean visibleChildren) {
		DefaultMutableTreeNode tmpNode;

		List<PartitionScheme> pSchemes = new ArrayList<PartitionScheme>(); 
		pSchemes=p.getSubPartitionSchemes(config);

		if (pSchemes!=null){

			TreeMap<String,PartitionScheme> sm=new TreeMap<String, PartitionScheme>();
			for (PartitionScheme partitionS : pSchemes) {
				if (partitionS!=null)
					sm.put(partitionS.getName(), partitionS);
			}
			if (sm.size()>0){
				tmpNode=addObject(node,new TreeObj(PARTSCHEMEROOTNODE,"Partition Schemes" ,p),visibleChildren);
				for (String key:sm.keySet()){
					PartitionScheme item=sm.get(key);
					DefaultMutableTreeNode partScheNode=addObject(tmpNode, new TreeObj(PARTSCHEMENODE,item.getName(),item), visibleChildren);
					loadPartitionSchemeNodeDetail(partScheNode,item, visibleChildren);
				}

			}
		}
		List<WorkList> wlists = p.getWorkLists();

		if (wlists!=null){
			TreeMap<String,WorkList> sm=new TreeMap<String, WorkList>();
			for (WorkList wList : wlists) {
				if (wList!=null)
					sm.put(wList.getName(), wList);
			}
			for (String key:sm.keySet()){
				WorkList item=sm.get(key);
				addObject(node, new TreeObj(WORKLISTNODE,item.getName(),item), visibleChildren);
			}
		}
		jTree1.revalidate();
	}

	private DefaultMutableTreeNode addProjectToTree (DefaultMutableTreeNode node,TranslationProject project,boolean visibleChildren) throws Exception{

		DefaultMutableTreeNode tNode;
		tNode=addObject(node,new TreeObj(PROJECTNODE,project.getName(),project),visibleChildren);
		loadProjectNodeDetails(tNode, project, visibleChildren);
		return tNode;
	}

	private void loadProjectNodeDetails(DefaultMutableTreeNode node, TranslationProject project,boolean visibleChildren){
		DefaultMutableTreeNode tmpNode;
		try {
			tmpNode=addObject(node,new TreeObj(EXCREFSETROOTNODE,"Exclusion Refsets" ,project),visibleChildren);
			List<I_GetConceptData> wsl;
			wsl = project.getExclusionRefsets();
			if (wsl!=null){
				for (I_GetConceptData ws:wsl){
					addObject(tmpNode, new TreeObj(EXCREFSETNODE,ws.getInitialText(),ws), visibleChildren);
				}
			}

			tmpNode=addObject(node,new TreeObj(LNKREFSETROOTNODE,"Linked Refsets" ,project),visibleChildren);
			wsl=project.getCommonRefsets();
			if (wsl!=null){
				for (I_GetConceptData ws:wsl){
					addObject(tmpNode, new TreeObj(LNKREFSETNODE,ws.getInitialText(),ws), visibleChildren);
				}
			}

			tmpNode=addObject(node,new TreeObj(SRCREFSETROOTNODE,"Source Languages" ,project),visibleChildren);
			wsl=project.getSourceLanguageRefsets();
			if (wsl!=null){
				for (I_GetConceptData ws:wsl){
					addObject(tmpNode, new TreeObj(SRCREFSETNODE,ws.getInitialText(),ws), visibleChildren);
				}
			}

			tmpNode=addObject(node,new TreeObj(TGTREFSETROOTNODE,"Target Language" ,project),visibleChildren);
			I_GetConceptData tl=project.getTargetLanguageRefset();
			if (tl!=null){
				addObject(tmpNode, new TreeObj(TGTREFSETNODE,tl.getInitialText(),tl), visibleChildren);
			}

			tmpNode=addObject(node,new TreeObj(WORKSETROOTNODE,"Worksets" ,project),false);
			tmpNode.add(new DefaultMutableTreeNode("Loading..."));
//			loadWorksetRootNodeDetail(tmpNode, project, visibleChildren);
//			List<WorkSet> wslist=project.getWorkSets(config);
//			if (wslist!=null){
//
//				TreeMap<String,WorkSet> sm=new TreeMap<String, WorkSet>();
//				for (WorkSet wSet : wslist) {
//					if (wSet!=null)
//						sm.put(wSet.getName(), wSet);
//				}
//				for (String key:sm.keySet()){
//					WorkSet ws=sm.get(key);
//					addObject(tmpNode, new TreeObj(WORKSETNODE,ws.getName(),ws), visibleChildren);
//				}
//			}
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		jTree1.revalidate();
	}


	public DefaultMutableTreeNode addObject(Object child) {
		DefaultMutableTreeNode parentNode = null;
		TreePath parentPath = jTree1.getSelectionPath();

		if (parentPath == null) {
			//There's no selection. Default to the root node.
			parentNode = rootNode;
		} else {
			parentNode = (DefaultMutableTreeNode)
			(parentPath.getLastPathComponent());
		}

		return addObject(parentNode, child, true);
	}

	public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
			Object child,
			boolean shouldBeVisible) {
		DefaultMutableTreeNode childNode =
			new DefaultMutableTreeNode(child);

		treeModel.insertNodeInto(childNode, parent,
				parent.getChildCount());

		//Make sure the user can see the lovely new node.
		if (shouldBeVisible) {
			jTree1.scrollPathToVisible(new TreePath(childNode.getPath()));
		}
		return childNode;
	}


	private void jTree1ValueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node;
		node = (DefaultMutableTreeNode)(jTree1.getLastSelectedPathComponent());

		if (node == null) return;

		nodeAction(node,true,false);
	}

	private ProjectDetailsContainer getRightPanel(){
		ProjectDetailsContainer detPanel=null;
		AceFrameConfig aceConfig = (AceFrameConfig) worker
		.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
		try {
			if (aceConfig==null){
				aceConfig=(AceFrameConfig)Terms.get().getActiveAceFrameConfig();
			}
			AceFrame ace=aceConfig.getAceFrame();
			JTabbedPane tp=ace.getCdePanel().getConceptTabs();
			if (tp!=null){
				boolean bPanelExists=false;
				int tabCount=tp.getTabCount();
				for (int i=0;i<tabCount;i++){
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.PROJECTS_DETAILS_TAB_NAME)){
						detPanel = (ProjectDetailsContainer)tp.getComponentAt(i);
						tp.setSelectedIndex(i);
						bPanelExists=true;
						break;
					}
				}
				if (!bPanelExists){
					detPanel = new ProjectDetailsContainer();
					tp.addTab(TranslationHelperPanel.PROJECTS_DETAILS_TAB_NAME, detPanel);
					tp.setSelectedIndex(tabCount);
				}
				tp.revalidate();
				tp.repaint();
			}
			else{
				throw new TaskFailedException("Cannot set panel to main panel.");
			}
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TaskFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return detPanel;
	}

	private void nodeAction(DefaultMutableTreeNode node,boolean openEditor,boolean visibleChildren) {
		TreeObj to=(TreeObj)node.getUserObject();

		if (to.getObjType().equals(PROJECTROOTNODE)){
			return;
		}

		if (to.getObjType().equals(PROJECTNODE)){

			TranslationProject proj=(TranslationProject) to.getAtrValue();
			if (node.getChildCount()<1){
				loadProjectNodeDetails(node,proj,visibleChildren);
			}
			if (openEditor){
				nodeOpener=node;
				showProjectDetails(proj);
			}
			return;
		}

		if (to.getObjType().equals(WORKSETROOTNODE)){
			return;
		}

		if (to.getObjType().equals(WORKSETNODE)){

			WorkSet ws=(WorkSet) to.getAtrValue();
			if (node.getChildCount()<1){
				loadWorksetNodeDetail(node,ws,visibleChildren);
			}
			if (openEditor){
				nodeOpener=node;
				showWorksetDetails(ws);
			}
			return;

		}
		if (to.getObjType().equals(PARTSCHEMEROOTNODE)){
			return;
		}

		if (to.getObjType().equals(PARTSCHEMENODE)){

			PartitionScheme ps=(PartitionScheme) to.getAtrValue();
			if (node.getChildCount()<1){
				loadPartitionSchemeNodeDetail(node,ps,visibleChildren);
			}
			if (openEditor){
				nodeOpener=node;
				showPartitionSchDetails(ps);
			}
			return;

		}

		if (to.getObjType().equals(PARTITIONNODE)){

			Partition p=(Partition) to.getAtrValue();
			if (node.getChildCount()<1){
				loadPartitionNodeDetail(node,p,visibleChildren);
			}
			if (openEditor){
				nodeOpener=node;
				showPartitionDetails(p);
			}
			return;

		}
		if (to.getObjType().equals(WORKLISTNODE)){
			if (openEditor){
				WorkList w=(WorkList) to.getAtrValue();
				nodeOpener=node;
				showWorklistDetails(w);
			}
			return;

		}

	}


	private void showWorklistDetails(WorkList w) {
		ProjectDetailsContainer panel=getRightPanel();
		panel.removeContent();;
		panel.addContent(new WorkListDetailsPanel(w, config, worker));
		panel.revalidate();

	}

	private void showPartitionDetails(Partition p) {
		ProjectDetailsContainer panel=getRightPanel();
		panel.removeContent();;
		panel.addContent(new PartitionDetailsPanel(p, config));
		panel.revalidate();

	}

	private void showPartitionSchDetails(PartitionScheme ps) {
		ProjectDetailsContainer panel=getRightPanel();
		panel.removeContent();;
		panel.addContent(new PartitionSchemeDetailsPanel(ps, config));
		panel.revalidate();

	}

	private void showWorksetDetails(WorkSet ws) {
		ProjectDetailsContainer panel=getRightPanel();
		panel.removeContent();;
		panel.addContent(new WorkSetDetailsPanel(ws, config));
		panel.revalidate();

	}

	private void showProjectDetails(TranslationProject proj) {
		ProjectDetailsContainer panel=getRightPanel();
		panel.removeContent();

		panel.addContent(new ProjectDetailsPanel(proj, config));
		panel.revalidate();

	}

	public void refreshNode(){
		if (nodeOpener!=null){
			for (int i=nodeOpener.getChildCount()-1;i>-1;i--){
				DefaultMutableTreeNode node=(DefaultMutableTreeNode)nodeOpener.getChildAt(i);
				((DefaultTreeModel)jTree1.getModel()).removeNodeFromParent(node);
			}
			updateNode(nodeOpener);
			jTree1.revalidate();
			nodeAction(nodeOpener,false,true);
		}
	}

	private void updateNode(DefaultMutableTreeNode node) {
		TreeObj to=(TreeObj)node.getUserObject();

		if (to.getObjType().equals(PROJECTNODE)){

			TranslationProject proj=(TranslationProject) to.getAtrValue();
			to.setAtrName(proj.getName());
			return;
		}


		if (to.getObjType().equals(WORKSETNODE)){

			WorkSet ws=(WorkSet) to.getAtrValue();
			to.setAtrName(ws.getName());
			return;

		}

		if (to.getObjType().equals(PARTSCHEMENODE)){

			PartitionScheme ps=(PartitionScheme) to.getAtrValue();
			to.setAtrName(ps.getName());
			return;

		}

		if (to.getObjType().equals(PARTITIONNODE)){

			Partition p=(Partition) to.getAtrValue();
			to.setAtrName(p.getName());
			return;

		}
		if (to.getObjType().equals(WORKLISTNODE)){
			WorkList w=(WorkList) to.getAtrValue();
			to.setAtrName(w.getName());
			return;

		}

	}

	private void button1ActionPerformed(ActionEvent e) {
		AceFrameConfig config;
		try {
			config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();
			AceFrame ace=config.getAceFrame();
			JTabbedPane tp=ace.getCdePanel().getLeftTabs();
			if (tp!=null){
				int tabCount=tp.getTabCount();
				for (int i=0;i<tabCount;i++){
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.PROJECT_MGR_TAB_NAME)){
						tp.remove(i);
						tp.repaint();
						tp.revalidate();
					}

				}
			}

			JTabbedPane tpc=ace.getCdePanel().getConceptTabs();
			if (tpc!=null){
				int tabCount=tpc.getTabCount();
				for (int i=0;i<tabCount;i++){
					if (tpc.getTitleAt(i).equals(TranslationHelperPanel.PROJECTS_DETAILS_TAB_NAME)){
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

	private void button2ActionPerformed(ActionEvent e) {
		// TODO duplicated code with Action Listener = Refactor
		String projectName = JOptionPane.showInputDialog(null, "Enter new Project Name : ", 
				"", 1);
		if (projectName!=null && !projectName.trim().equals("")){
			try {
				TranslationProject tProj= TerminologyProjectDAO.createNewTranslationProject(projectName, config);
				if(tProj != null){
					Terms.get().commit();
					nodeOpener=addProjectToTree(projectRoot, tProj, false);
					showProjectDetails(tProj);
					
					SwingUtilities.invokeLater(new Runnable(){
						public void run(){
							TranslationHelperPanel.refreshProjectPanelNode(config);
						}
					});
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	private void jTree1TreeWillExpand(TreeExpansionEvent e)
		throws ExpandVetoException
	{
		TreePath path = e.getPath();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		Object obj=node.getUserObject();
		if (obj instanceof TreeObj){
			TreeObj to=(TreeObj)obj;

			if (to.getObjType().equals(WORKSETROOTNODE)){

				if (node.getFirstChild().toString().trim().equals("Loading...")) {
					node.removeAllChildren();
					TreeObj treeObj = (TreeObj) node.getUserObject();
					TranslationProject project = (TranslationProject) treeObj.getAtrValue();
					loadWorksetRootNodeDetail(node, project, false);
				}
			}
		}
	}

	private void label2MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("PROJECT_LEFT");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	class MenuItemActionListener implements ActionListener{

		private String nodeType;
		@Override
		public void actionPerformed(ActionEvent e) {
			if (nodeType.equals(PROJECTROOTNODE)){
				String projectName = JOptionPane.showInputDialog(null, "Enter new Project Name : ", 
						"", 1);
				if (projectName!=null && !projectName.trim().equals("")){
					try {
						TranslationProject tProj= TerminologyProjectDAO.createNewTranslationProject(projectName, config);
						if(tProj != null){
							Terms.get().commit();
							nodeOpener=addProjectToTree(projectRoot, tProj, false);
							showProjectDetails(tProj);
	
							SwingUtilities.invokeLater(new Runnable(){
								public void run(){
									TranslationHelperPanel.refreshProjectPanelNode(config);
								}
							});
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				return;
			}

		}
		public void setNodeType(String nodeType){
			this.nodeType=nodeType;
		}

	}
	public class JTreeMouselistener extends MouseAdapter {
		private JTree jTree;
		private JPopupMenu menu;
		private MenuItemActionListener mItemListener;
		private JMenuItem mItem;
		private int xPoint;
		private int yPoint;

		JTreeMouselistener (JTree jTree){
			this.jTree=jTree;
			menu=new JPopupMenu();
			mItem=new JMenuItem();
			mItemListener=new MenuItemActionListener();
			mItem.addActionListener(mItemListener);
			menu.add(mItem);
		}

		@Override
		public void mouseClicked(MouseEvent e) {

			if (e.getButton()== java.awt.event.MouseEvent.BUTTON3) {
				xPoint=e.getX();
				yPoint=e.getY();
				TreePath treePath =jTree.getPathForLocation(xPoint, yPoint);
				if (treePath!=null){
					DefaultMutableTreeNode node =(DefaultMutableTreeNode)treePath.getLastPathComponent();

					if (node != null) {
						TreeObj to=(TreeObj)node.getUserObject();

						if (to.getObjType().equals(PROJECTROOTNODE)){
							try {
								boolean isProjectManager = TerminologyProjectDAO.checkPermissionForProject(
										config.getDbConfig().getUserConcept(), 
										Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.localize().getNid()),
										Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PROJECT_MANAGER_ROLE.localize().getNid()), 
										config);
								if (isProjectManager) {
									mItem.setText("Create new project");
									mItemListener.setNodeType(PROJECTROOTNODE);

									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											menu.show(jTree,xPoint, yPoint);
										}
									});
								}
							} catch (IOException e1) {
								e1.printStackTrace();
							} catch (TerminologyException e1) {
								e1.printStackTrace();
							}


						}
					}
				}

			}
		}

	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel2 = new JPanel();
		panel1 = new JPanel();
		label1 = new JLabel();
		button2 = new JButton();
		button1 = new JButton();
		label2 = new JLabel();
		panel11 = new JPanel();
		scrollPane1 = new JScrollPane();
		jTree1 = new JTree();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

		//======== panel2 ========
		{
			panel2.setBackground(new Color(238, 238, 238));
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {20, 0, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

			//======== panel1 ========
			{
				panel1.setBackground(new Color(238, 238, 238));
				panel1.setLayout(new GridBagLayout());
				((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 61, 0, 0, 0, 0};
				((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 0.0, 0.0, 1.0E-4};
				((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

				//---- label1 ----
				label1.setText("Projects");
				panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- button2 ----
				button2.setText("Add Project");
				button2.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						button2ActionPerformed(e);
					}
				});
				panel1.add(button2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- button1 ----
				button1.setText("Close");
				button1.setIcon(null);
				button1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						button1ActionPerformed(e);
					}
				});
				panel1.add(button1, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- label2 ----
				label2.setText("text");
				label2.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						label2MouseClicked(e);
					}
				});
				panel1.add(label2, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			panel2.add(panel1, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//======== panel11 ========
			{
				panel11.setBackground(new Color(220, 233, 249));
				panel11.setLayout(new GridBagLayout());
				((GridBagLayout)panel11.getLayout()).columnWidths = new int[] {0, 0};
				((GridBagLayout)panel11.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel11.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
				((GridBagLayout)panel11.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

				//======== scrollPane1 ========
				{

					//---- jTree1 ----
					jTree1.setRootVisible(false);
					jTree1.addTreeSelectionListener(new TreeSelectionListener() {
						@Override
						public void valueChanged(TreeSelectionEvent e) {
							jTree1ValueChanged(e);
						}
					});
					jTree1.addTreeWillExpandListener(new TreeWillExpandListener() {
						@Override
						public void treeWillExpand(TreeExpansionEvent e)
							throws ExpandVetoException
						{
							jTree1TreeWillExpand(e);
						}
						@Override
						public void treeWillCollapse(TreeExpansionEvent e)
							throws ExpandVetoException
						{}
					});
					scrollPane1.setViewportView(jTree1);
				}
				panel11.add(scrollPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			panel2.add(panel11, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel2;
	private JPanel panel1;
	private JLabel label1;
	private JButton button2;
	private JButton button1;
	private JLabel label2;
	private JPanel panel11;
	private JScrollPane scrollPane1;
	private JTree jTree1;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	public void refreshParentNode() {
		if (nodeOpener!=null){
			DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) nodeOpener.getParent();
			for (int i=parentNode.getChildCount()-1;i>-1;i--){
				DefaultMutableTreeNode node=(DefaultMutableTreeNode)parentNode.getChildAt(i);
				((DefaultTreeModel)jTree1.getModel()).removeNodeFromParent(node);
			}
			jTree1.revalidate();
			nodeOpener=null;
			TreeObj to=(TreeObj)parentNode.getUserObject();

			if (to.getObjType().equals(PROJECTROOTNODE)){
				SwingUtilities.invokeLater(new Runnable(){

					@Override
					public void run() {
						try {

							loadProjects();
						} catch (Exception e) {
							e.printStackTrace();
						}

					}

				});
				return;
			}
			if (to.getObjType().equals(WORKSETROOTNODE)){
				TranslationProject project=(TranslationProject)to.getAtrValue();
				loadWorksetRootNodeDetail(parentNode, project, true);
				return;
			}

			if (to.getObjType().equals(PARTSCHEMEROOTNODE)){
				if (to.getAtrValue() instanceof WorkSet){
					WorkSet workset=(WorkSet)to.getAtrValue();
					loadPartitionSchemeRootNodeDetail(parentNode, workset, true);
					return;
				}
				if (to.getAtrValue() instanceof Partition){
					Partition partition=(Partition)to.getAtrValue();
					loadPartitionSchemeRootNodeDetail(parentNode, partition, true);
					return;
				}
				return;
			}

			nodeAction(parentNode,false,true);
		}
		
	}
}
