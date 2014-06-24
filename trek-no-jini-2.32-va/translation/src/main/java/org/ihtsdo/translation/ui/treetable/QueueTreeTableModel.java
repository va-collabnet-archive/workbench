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

/**
 * The Class QueueTreeTableModel.
 */
public class QueueTreeTableModel extends AbstractTreeTableModel implements TreeTableModel{

	// Names of the columns.
	/** The c names. */
	protected String[]  cNames = {"Source Name", "Status"};

	// Types of the columns.
	/** The c types. */
	protected Class[]  cTypes = {TreeTableModel.class, String.class};

	// The the returned file length for directories. 
	/** The Constant ZERO. */
	public static final Integer ZERO = new Integer(0);

	/** The Constant ROOT_NAME. */
	private static final String ROOT_NAME = "Worklist"; 

	/** The items. */
	private HashMap<String,Set<DefaultMutableTreeNode>> items;

	/** The col setted. */
	private boolean colSetted;

	/** The col pos. */
	private List<InboxColumn> colPos;

	/** The cfg. */
	private ConfigTranslationModule cfg;

	/**
	 * Instantiates a new queue tree table model.
	 *
	 * @param root the root
	 */
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

	/**
	 * Gets the tree table item.
	 *
	 * @param node the node
	 * @return the tree table item
	 */
	protected QueueTreeTableObj getTreeTableItem(Object node) {
		DefaultMutableTreeNode itemNode = ((DefaultMutableTreeNode)node); 
		return (QueueTreeTableObj)itemNode.getUserObject();
	}

	/**
	 * Gets the children.
	 *
	 * @param node the node
	 * @return the children
	 */
	protected Object[] getChildren(Object node) {
		DefaultMutableTreeNode itemNode = ((DefaultMutableTreeNode)node); 
		DefaultMutableTreeNode[] children=new DefaultMutableTreeNode[itemNode.getChildCount()];
		for (int i=0;i<itemNode.getChildCount();i++){
			children[i]=(DefaultMutableTreeNode) itemNode.getChildAt(i);
		}
		return children; 
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	@Override
	public int getChildCount(Object node) { 
		return((DefaultMutableTreeNode)node).getChildCount(); 
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	@Override
	public Object getChild(Object node, int i) { 
		return((DefaultMutableTreeNode)node).getChildAt(i);
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.translation.ui.treetable.TreeTableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return cNames.length;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.translation.ui.treetable.TreeTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		return cNames[column];
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.translation.ui.treetable.AbstractTreeTableModel#getColumnClass(int)
	 */
	public Class getColumnClass(int column) {
		return cTypes[column];
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.translation.ui.treetable.TreeTableModel#getValueAt(java.lang.Object, int)
	 */
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

	/* (non-Javadoc)
	 * @see org.ihtsdo.translation.ui.treetable.AbstractTreeTableModel#isLeaf(java.lang.Object)
	 */
	public boolean isLeaf(Object node) { 
		String leaf=getTreeTableItem(node).getObjType();
		return 	(leaf.equals("leaf")); }

}

