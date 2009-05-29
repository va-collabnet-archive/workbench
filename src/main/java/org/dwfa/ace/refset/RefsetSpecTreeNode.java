package org.dwfa.ace.refset;

import javax.swing.tree.DefaultMutableTreeNode;

public class RefsetSpecTreeNode extends DefaultMutableTreeNode implements Comparable<RefsetSpecTreeNode> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int compareTo(RefsetSpecTreeNode o) {
		if (this.userObject.getClass().equals(o.userObject.getClass())) {
			
		} else {
			
		}
		return 0;
	}

}
