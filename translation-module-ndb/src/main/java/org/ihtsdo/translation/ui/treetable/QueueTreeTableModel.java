package org.ihtsdo.translation.ui.treetable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.ui.ConfigTranslationModule;
import org.ihtsdo.translation.ui.ConfigTranslationModule.InboxColumn;

public class QueueTreeTableModel extends AbstractTreeTableModel implements TreeTableModel{

	// Names of the columns.
	protected String[]  cNames = {"Source Name", "Status"};

	// Types of the columns.
	protected Class[]  cTypes = {TreeTableModel.class, String.class};

	// The the returned file length for directories. 
	public static final Integer ZERO = new Integer(0);

	private static final String ROOT_NAME = "Worklist"; 

	private HashMap<String,Set<DefaultMutableTreeNode>> items;

	private boolean colSetted;

	private List<InboxColumn> colPos;

	private ConfigTranslationModule cfg;

	public QueueTreeTableModel(Object root) {
		super(root);
		try {
			cfg=LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		if (cfg!=null){
			LinkedHashSet<InboxColumn> colSet= cfg.getColumnsDisplayedInInbox();
			List<String> colName=new ArrayList<String>();
			List<Class> colType=new ArrayList<Class>();
			colPos=new ArrayList<InboxColumn>();

			colSetted=false;
			if (colSet!=null && colSet.size()>0){
				cNames=new String[colSet.size() + 1];
				cTypes=new Class[colSet.size() +1];
				colName.add("Source FSN");
				colType.add(TreeTableModel.class);
				for (InboxColumn iCol:colSet){
					colName.add(iCol.getColumnName());
					colPos.add(iCol);
					colType.add(iCol.getEditorClass());
					
				}
				colName.toArray(cNames);
				colType.toArray(cTypes);
				colSetted = true;
			}
		}
	}

	protected QueueTreeTableObj getTreeTableItem(Object node) {
		DefaultMutableTreeNode itemNode = ((DefaultMutableTreeNode)node); 
		return (QueueTreeTableObj)itemNode.getUserObject();
	}

	protected Object[] getChildren(Object node) {
		DefaultMutableTreeNode itemNode = ((DefaultMutableTreeNode)node); 
		DefaultMutableTreeNode[] children=new DefaultMutableTreeNode[itemNode.getChildCount()];
		for (int i=0;i<itemNode.getChildCount();i++){
			children[i]=(DefaultMutableTreeNode) itemNode.getChildAt(i);
		}
		return children; 
	}

	@Override
	public int getChildCount(Object node) { 
		return((DefaultMutableTreeNode)node).getChildCount(); 
	}

	@Override
	public Object getChild(Object node, int i) { 
		return((DefaultMutableTreeNode)node).getChildAt(i);
	}
	@Override
	public int getColumnCount() {
		return cNames.length;
	}

	@Override
	public String getColumnName(int column) {
		return cNames[column];
	}

	public Class getColumnClass(int column) {
		return cTypes[column];
	}

	@Override
	public Object getValueAt(Object node, int column) {
		QueueTreeTableObj item = getTreeTableItem(node);
		try {
			if (column==0)
				return item.getSourceFSN();
				
			if (colSetted){
				InboxColumn iCol=colPos.get(column-1);

				switch(iCol) {
				case SOURCE_PREFERRED:
					return item.getSourcePref();
				case TARGET_FSN:
					return item.getTargetFSN();
				case TARGET_PREFERRED:
					return item.getTargetPref();
				case STATUS:
					return item.getStatus();
				}
			}else{
				switch(column) {
				case 1:
					return item.getStatus();
				}
			}

		}
		catch  (SecurityException se) { }

		return null; 
	}

	public boolean isLeaf(Object node) { 
		String leaf=getTreeTableItem(node).getObjType();
		return 	(leaf.equals("leaf")); }

}

