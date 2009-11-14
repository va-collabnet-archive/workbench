package org.dwfa.ace.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.util.HashFunction;

public class TreeIdPath {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int[] ids;

	public TreeIdPath(Object[] path) {
		ids = new int[path.length];
		for (int i = 0; i < ids.length; i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path[i];
			I_GetConceptData cb = (I_GetConceptData) node.getUserObject();
			if (cb == null) {
				ids[i] = Integer.MIN_VALUE;
			} else {
				ids[i] = cb.getConceptId();
			}
		}
	}
	
	public boolean initiallyEqual(TreeIdPath another) {
		if (another.ids.length > ids.length) {
			return false;
		}
		for (int i = 0; i < another.ids.length; i++) {
			if (ids[i] != another.ids[i]) {
				return false;
			}
		}
		return true;
	}

	public TreeIdPath(TreePath path) {
		ids = new int[path.getPathCount()];
		for (int i = 0; i < ids.length; i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getPathComponent(i);
			I_GetConceptData cb = (I_GetConceptData) node.getUserObject();
			if (cb == null) {
				ids[i] = Integer.MIN_VALUE;
			} else {
				ids[i] = cb.getConceptId();
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		TreeIdPath another = (TreeIdPath) obj;
		if (ids.length != another.ids.length) {
			return false;
		}
		for (int i = 0; i < ids.length; i++) {
			if (ids[i] != another.ids[i]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		return HashFunction.hashCode(ids);
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		boolean first = true;
		for (int i: ids) {
			if (!first) {
				buf.append(", ");
			}
			buf.append(i);
			first = false;
		}
		buf.append("]");
		return buf.toString();
	}

	
}
