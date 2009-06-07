package org.dwfa.ace.refset;

import javax.swing.tree.DefaultMutableTreeNode;

import org.dwfa.ace.jtreetable.AbstractTreeTableModel;

public class RefsetSpecTreeTableModel extends AbstractTreeTableModel {
	
	private enum COLUMNS { CLAUSE, TRUTH, OPERATOR, QUALIFIER };

	public RefsetSpecTreeTableModel(DefaultMutableTreeNode root) {
		super(root);
	}

	public int getColumnCount() {
		return COLUMNS.values().length;
	}

	public String getColumnName(int column) {
		return COLUMNS.values()[column].name();
	}

	public Object getValueAt(Object node, int column) {
		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) node;
		
		
		switch (COLUMNS.values()[column]) {
		case CLAUSE:
			
			break;

		case OPERATOR:
			
			break;
			
		case QUALIFIER: 
			
			break;
			
		case TRUTH:
			
			break;
		default:
			break;
		}
		// TODO Auto-generated method stub
		return null;
	}

	public Object getChild(Object node, int index) {
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node;
		return parent.getChildAt(index);
	}

	public int getChildCount(Object node) {
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node;
		return parent.getChildCount();
	}

}
